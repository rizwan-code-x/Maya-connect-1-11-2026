package com.example.data.audio

import android.content.Context
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin

/**
 * RexlifyIntroAudioGenerator
 * Synthesizes a pristine 10-second "Welcome to REXLIFY" introductory audio track (PCM 16-bit 44.1kHz Stereo WAV)
 * with a harmonic chord progression and melodic chimes, stored locally in internal storage.
 */
object RexlifyIntroAudioGenerator {

    private const val SAMPLE_RATE = 44100
    private const val DURATION_SEC = 10
    private const val NUM_CHANNELS = 2
    private const val BITS_PER_SAMPLE = 16

    /**
     * Generates or retrieves the 10-second introductory WAV audio file.
     */
    fun getOrCreateIntroTrackFile(context: Context): File {
        val audioDir = File(context.filesDir, "rexlify_music")
        if (!audioDir.exists()) {
            audioDir.mkdirs()
        }

        val targetFile = File(audioDir, "welcome_to_rexlify.wav")
        if (targetFile.exists() && targetFile.length() > 10000) {
            return targetFile
        }

        try {
            generateWavFile(targetFile)
        } catch (_: Exception) {
            // If generation encounters any error, ensure an empty or partial file doesn't block playback
        }
        return targetFile
    }

    private fun generateWavFile(file: File) {
        val totalSamples = SAMPLE_RATE * DURATION_SEC
        val shortBuffer = ShortArray(totalSamples * NUM_CHANNELS)

        // Melodic notes (Frequencies in Hz): C4 (261.63), E4 (329.63), G4 (392.00), B4 (493.88), C5 (523.25), E5 (659.25), G5 (783.99)
        val melodyNotes = listOf(
            261.63, // 0.0s - C4
            329.63, // 1.2s - E4
            392.00, // 2.4s - G4
            493.88, // 3.6s - B4
            523.25, // 4.8s - C5 (Peak Welcome)
            659.25, // 6.0s - E5
            783.99, // 7.2s - G5 shimmer
            523.25  // 8.4s - C5 resolution
        )
        val noteStartTimes = listOf(0.0, 1.2, 2.4, 3.6, 4.8, 6.0, 7.2, 8.4)

        for (i in 0 until totalSamples) {
            val t = i.toDouble() / SAMPLE_RATE // Current time in seconds

            var left = 0.0
            var right = 0.0

            // 1. Warm harmonic drone pad (C3 130.81Hz + G3 196.00Hz + C4 261.63Hz)
            val padEnvelope = when {
                t < 1.0 -> t // 1s fade-in
                t > 8.5 -> (10.0 - t).coerceAtLeast(0.0) / 1.5 // gentle 1.5s fade-out
                else -> 1.0
            }
            val padBase = sin(2 * PI * 130.81 * t) * 0.18 +
                    sin(2 * PI * 196.00 * t) * 0.12 +
                    sin(2 * PI * 261.63 * t) * 0.08
            val padSound = padBase * padEnvelope

            left += padSound * 0.9
            right += padSound * 0.9

            // 2. Melodic Chime notes
            for (idx in melodyNotes.indices) {
                val noteStart = noteStartTimes[idx]
                if (t >= noteStart) {
                    val noteAge = t - noteStart
                    if (noteAge < 3.0) {
                        // Exponential chime decay envelope
                        val decay = exp(-noteAge * 1.8)
                        val freq = melodyNotes[idx]

                        // Fundamental + pleasant sparkle harmonics
                        val chime = sin(2 * PI * freq * noteAge) * 0.40 +
                                sin(2 * PI * (freq * 2.0) * noteAge) * 0.15 +
                                sin(2 * PI * (freq * 3.0) * noteAge) * 0.05

                        val pan = sin(idx.toDouble() * 0.8) * 0.35 // slight stereo panning

                        left += chime * decay * (0.5 - pan)
                        right += chime * decay * (0.5 + pan)
                    }
                }
            }

            // Overall master volume limiter and soft fade
            val masterFade = when {
                t < 0.2 -> t / 0.2
                t > 9.4 -> (10.0 - t).coerceAtLeast(0.0) / 0.6
                else -> 1.0
            }

            val finalLeft = (left * masterFade * 26000.0).coerceIn(-32767.0, 32767.0).toInt().toShort()
            val finalRight = (right * masterFade * 26000.0).coerceIn(-32767.0, 32767.0).toInt().toShort()

            shortBuffer[i * 2] = finalLeft
            shortBuffer[i * 2 + 1] = finalRight
        }

        // Write standard 44-byte WAV header + PCM 16-bit audio bytes
        val byteData = ByteArray(shortBuffer.size * 2)
        ByteBuffer.wrap(byteData).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().put(shortBuffer)

        FileOutputStream(file).use { fos ->
            writeWavHeader(fos, byteData.size)
            fos.write(byteData)
            fos.flush()
        }
    }

    private fun writeWavHeader(out: FileOutputStream, pcmDataLength: Int) {
        val totalDataLen = pcmDataLength + 36
        val byteRate = SAMPLE_RATE * NUM_CHANNELS * (BITS_PER_SAMPLE / 8)

        val header = ByteArray(44)
        // RIFF header
        header[0] = 'R'.code.toByte()
        header[1] = 'I'.code.toByte()
        header[2] = 'F'.code.toByte()
        header[3] = 'F'.code.toByte()
        header[4] = (totalDataLen and 0xff).toByte()
        header[5] = ((totalDataLen shr 8) and 0xff).toByte()
        header[6] = ((totalDataLen shr 16) and 0xff).toByte()
        header[7] = ((totalDataLen shr 24) and 0xff).toByte()
        header[8] = 'W'.code.toByte()
        header[9] = 'A'.code.toByte()
        header[10] = 'V'.code.toByte()
        header[11] = 'E'.code.toByte()
        // fmt chunk
        header[12] = 'f'.code.toByte()
        header[13] = 'm'.code.toByte()
        header[14] = 't'.code.toByte()
        header[15] = ' '.code.toByte()
        header[16] = 16 // Subchunk1Size for PCM
        header[17] = 0
        header[18] = 0
        header[19] = 0
        header[20] = 1 // AudioFormat: 1 = PCM
        header[21] = 0
        header[22] = NUM_CHANNELS.toByte()
        header[23] = 0
        header[24] = (SAMPLE_RATE and 0xff).toByte()
        header[25] = ((SAMPLE_RATE shr 8) and 0xff).toByte()
        header[26] = ((SAMPLE_RATE shr 16) and 0xff).toByte()
        header[27] = ((SAMPLE_RATE shr 24) and 0xff).toByte()
        header[28] = (byteRate and 0xff).toByte()
        header[29] = ((byteRate shr 8) and 0xff).toByte()
        header[30] = ((byteRate shr 16) and 0xff).toByte()
        header[31] = ((byteRate shr 24) and 0xff).toByte()
        header[32] = (NUM_CHANNELS * (BITS_PER_SAMPLE / 8)).toByte() // BlockAlign
        header[33] = 0
        header[34] = BITS_PER_SAMPLE.toByte()
        header[35] = 0
        // data chunk
        header[36] = 'd'.code.toByte()
        header[37] = 'a'.code.toByte()
        header[38] = 't'.code.toByte()
        header[39] = 'a'.code.toByte()
        header[40] = (pcmDataLength and 0xff).toByte()
        header[41] = ((pcmDataLength shr 8) and 0xff).toByte()
        header[42] = ((pcmDataLength shr 16) and 0xff).toByte()
        header[43] = ((pcmDataLength shr 24) and 0xff).toByte()

        out.write(header)
    }
}
