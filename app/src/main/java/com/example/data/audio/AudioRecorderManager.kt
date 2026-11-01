package com.example.data.audio

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.util.Base64
import android.util.Log
import androidx.core.content.ContextCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.sqrt

/**
 * Manages low-latency PCM audio recording from microphone
 * for Gemini Multimodal Audio Input and real-time visualization.
 */
class AudioRecorderManager(
    private val context: Context,
    private val coroutineScope: CoroutineScope
) {
    companion object {
        private const val TAG = "AudioRecorderManager"
        const val SAMPLE_RATE = 16000
        const val CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO
        const val AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT
    }

    private var audioRecord: AudioRecord? = null
    private var recordingJob: Job? = null
    private val recordedPcmStream = ByteArrayOutputStream()

    var isRecording: Boolean = false
        private set

    var onAmplitudeChanged: ((Float) -> Unit)? = null
    var onSilenceDetected: (() -> Unit)? = null

    private var consecutiveSilenceFrames = 0
    private var speechDetected = false

    fun hasPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
    }

    @SuppressLint("MissingPermission")
    fun startRecording(): Boolean {
        if (!hasPermission()) {
            Log.w(TAG, "Cannot start recording: RECORD_AUDIO permission not granted")
            return false
        }

        if (isRecording) {
            stopRecording()
        }

        val minBufferSize = AudioRecord.getMinBufferSize(
            SAMPLE_RATE,
            CHANNEL_CONFIG,
            AUDIO_FORMAT
        )

        if (minBufferSize == AudioRecord.ERROR || minBufferSize == AudioRecord.ERROR_BAD_VALUE) {
            Log.e(TAG, "Invalid buffer size: $minBufferSize")
            return false
        }

        val bufferSize = (minBufferSize * 2).coerceAtLeast(4096)

        try {
            audioRecord = AudioRecord(
                MediaRecorder.AudioSource.MIC,
                SAMPLE_RATE,
                CHANNEL_CONFIG,
                AUDIO_FORMAT,
                bufferSize
            )

            if (audioRecord?.state != AudioRecord.STATE_INITIALIZED) {
                Log.e(TAG, "AudioRecord initialization failed")
                audioRecord?.release()
                audioRecord = null
                return false
            }

            recordedPcmStream.reset()
            speechDetected = false
            consecutiveSilenceFrames = 0
            audioRecord?.startRecording()
            isRecording = true

            recordingJob = coroutineScope.launch(Dispatchers.IO) {
                val readBuffer = ByteArray(bufferSize)

                while (isActive && isRecording) {
                    val bytesRead = audioRecord?.read(readBuffer, 0, readBuffer.size) ?: -1
                    if (bytesRead > 0) {
                        recordedPcmStream.write(readBuffer, 0, bytesRead)

                        // Calculate RMS amplitude for real-time visualization & silence detection
                        val rms = calculateRms(readBuffer, bytesRead)
                        val normalizedAmplitude = (rms / 32767.0 * 4.0).coerceIn(0.0, 1.0).toFloat()

                        launch(Dispatchers.Main) {
                            onAmplitudeChanged?.invoke(normalizedAmplitude)
                        }

                        // Silence watchdog (auto-stops when user finishes speaking)
                        if (normalizedAmplitude > 0.08f) {
                            speechDetected = true
                            consecutiveSilenceFrames = 0
                        } else if (speechDetected) {
                            consecutiveSilenceFrames++
                            // 16kHz with 4096 bytes ~ 8 frames per second. ~15 frames = ~1.8 seconds of silence
                            if (consecutiveSilenceFrames > 15) {
                                launch(Dispatchers.Main) {
                                    onSilenceDetected?.invoke()
                                }
                            }
                        }
                    }
                }
            }

            Log.d(TAG, "AudioRecord started successfully at 16kHz mono")
            return true
        } catch (e: Exception) {
            Log.e(TAG, "Error starting AudioRecord", e)
            isRecording = false
            audioRecord?.release()
            audioRecord = null
            return false
        }
    }

    fun stopRecording(): ByteArray? {
        if (!isRecording && recordedPcmStream.size() == 0) return null

        isRecording = false
        recordingJob?.cancel()
        recordingJob = null

        try {
            audioRecord?.stop()
            audioRecord?.release()
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping AudioRecord", e)
        } finally {
            audioRecord = null
        }

        onAmplitudeChanged?.invoke(0f)

        val pcmData = recordedPcmStream.toByteArray()
        if (pcmData.isEmpty()) return null

        return pcmToWav(pcmData, SAMPLE_RATE, 1, 16)
    }

    fun stopAndGetBase64Wav(): String? {
        val wavBytes = stopRecording() ?: return null
        return Base64.encodeToString(wavBytes, Base64.NO_WRAP)
    }

    fun release() {
        isRecording = false
        recordingJob?.cancel()
        recordingJob = null
        try {
            audioRecord?.stop()
            audioRecord?.release()
        } catch (_: Exception) {}
        audioRecord = null
        recordedPcmStream.reset()
        onAmplitudeChanged = null
        onSilenceDetected = null
    }

    private fun calculateRms(buffer: ByteArray, bytesRead: Int): Double {
        var sum = 0.0
        val shortBuffer = ByteBuffer.wrap(buffer, 0, bytesRead).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer()
        val count = shortBuffer.remaining()
        if (count == 0) return 0.0

        while (shortBuffer.hasRemaining()) {
            val sample = shortBuffer.get().toDouble()
            sum += sample * sample
        }
        return sqrt(sum / count)
    }

    /**
     * Converts raw PCM byte array to a standard standard 44-byte header WAV byte array
     * accepted by Google Gemini multimodal API.
     */
    private fun pcmToWav(pcmData: ByteArray, sampleRate: Int, channels: Int, bitsPerSample: Int): ByteArray {
        val totalAudioLen = pcmData.size
        val totalDataLen = totalAudioLen + 36
        val byteRate = sampleRate * channels * bitsPerSample / 8

        val header = ByteArray(44)
        // RIFF chunk descriptor
        header[0] = 'R'.code.toByte()
        header[1] = 'I'.code.toByte()
        header[2] = 'F'.code.toByte()
        header[3] = 'F'.code.toByte()
        header[4] = (totalDataLen and 0xff).toByte()
        header[5] = (totalDataLen shr 8 and 0xff).toByte()
        header[6] = (totalDataLen shr 16 and 0xff).toByte()
        header[7] = (totalDataLen shr 24 and 0xff).toByte()
        header[8] = 'W'.code.toByte()
        header[9] = 'A'.code.toByte()
        header[10] = 'V'.code.toByte()
        header[11] = 'E'.code.toByte()
        // 'fmt ' sub-chunk
        header[12] = 'f'.code.toByte()
        header[13] = 'm'.code.toByte()
        header[14] = 't'.code.toByte()
        header[15] = ' '.code.toByte()
        header[16] = 16 // 16 for PCM
        header[17] = 0
        header[18] = 0
        header[19] = 0
        header[20] = 1 // Audio format 1 = PCM
        header[21] = 0
        header[22] = channels.toByte()
        header[23] = 0
        header[24] = (sampleRate and 0xff).toByte()
        header[25] = (sampleRate shr 8 and 0xff).toByte()
        header[26] = (sampleRate shr 16 and 0xff).toByte()
        header[27] = (sampleRate shr 24 and 0xff).toByte()
        header[28] = (byteRate and 0xff).toByte()
        header[29] = (byteRate shr 8 and 0xff).toByte()
        header[30] = (byteRate shr 16 and 0xff).toByte()
        header[31] = (byteRate shr 24 and 0xff).toByte()
        header[32] = (channels * bitsPerSample / 8).toByte() // Block align
        header[33] = 0
        header[34] = bitsPerSample.toByte()
        header[35] = 0
        // 'data' sub-chunk
        header[36] = 'd'.code.toByte()
        header[37] = 'a'.code.toByte()
        header[38] = 't'.code.toByte()
        header[39] = 'a'.code.toByte()
        header[40] = (totalAudioLen and 0xff).toByte()
        header[41] = (totalAudioLen shr 8 and 0xff).toByte()
        header[42] = (totalAudioLen shr 16 and 0xff).toByte()
        header[43] = (totalAudioLen shr 24 and 0xff).toByte()

        val wavOut = ByteArrayOutputStream(totalAudioLen + 44)
        wavOut.write(header)
        wavOut.write(pcmData)
        return wavOut.toByteArray()
    }
}
