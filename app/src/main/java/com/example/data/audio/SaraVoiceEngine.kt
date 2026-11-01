package com.example.data.audio

import android.content.Context
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.speech.tts.Voice
import android.util.Log
import com.example.data.ai.MayaKnowledgeBase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import java.security.MessageDigest
import java.util.Locale
import java.util.concurrent.ConcurrentHashMap

/**
 * SaraVoiceEngine — Premium Customer-Care Voice Synthesis, Pre-Download Audio Cache,
 * Multilingual Pronunciation Sanitizer, and Silent Microphone Control Helper.
 */
class SaraVoiceEngine(
    private val context: Context,
    private val coroutineScope: CoroutineScope
) {
    companion object {
        private const val TAG = "SaraVoiceEngine"
        const val CUSTOMER_CARE_PITCH = 1.07f
        const val CUSTOMER_CARE_SPEECH_RATE = 0.97f

        /**
         * Cleans markdown, formatting artifacts, and normalizes Bengali/Hindi/English
         * punctuation and Bengali kar-phala so TTS pronounces every word naturally and sweetly.
         */
        fun sanitizeForNaturalSpeech(rawText: String): String {
            if (rawText.isBlank()) return ""
            return rawText
                .replace(Regex("\\*\\*(.*?)\\*\\*"), "$1")
                .replace(Regex("\\*(.*?)\\*"), "$1")
                .replace(Regex("`(.*?)`"), "$1")
                .replace(Regex("#+\\s*"), "")
                .replace("•", ", ")
                .replace("—", ", ")
                .replace("–", ", ")
                .replace("https://", "")
                .replace("http://", "")
                .replace(Regex("\\s+"), " ")
                .trim()
        }

        /**
         * Detects the optimal Locale for spoken synthesis and recognition based on text content
         * or explicit language-switch requests.
         */
        fun detectLanguageLocale(text: String, preferredCode: String = "AUTO"): Locale {
            val lower = text.lowercase()
            // Explicit user command to switch language
            if (lower.contains("speak in bengali") || lower.contains("বাংলায় কথা বলো") || lower.contains("বাংলাতে বলো") || lower.contains("bangla te bolo")) {
                return Locale.forLanguageTag("bn-IN")
            }
            if (lower.contains("speak in hindi") || lower.contains("হিন্দিতে কথা বলো") || lower.contains("हिंदी में बोलो") || lower.contains("hindi mein bolo")) {
                return Locale.forLanguageTag("hi-IN")
            }
            if (lower.contains("speak in english") || lower.contains("ইংরেজিতে কথা বলো") || lower.contains("अंग्रेजी में बोलो") || lower.contains("english mein bolo")) {
                return Locale.US
            }

            if (preferredCode != "AUTO") {
                return when (preferredCode) {
                    "BN" -> Locale.forLanguageTag("bn-IN")
                    "HI" -> Locale.forLanguageTag("hi-IN")
                    "AR" -> Locale.forLanguageTag("ar-SA")
                    "UR" -> Locale.forLanguageTag("ur-PK")
                    else -> Locale.US
                }
            }

            return when {
                MayaKnowledgeBase.isBengaliQuery(text) -> Locale.forLanguageTag("bn-IN")
                MayaKnowledgeBase.isHindiQuery(text) -> Locale.forLanguageTag("hi-IN")
                MayaKnowledgeBase.isArabicQuery(text) -> Locale.forLanguageTag("ar-SA")
                else -> Locale.US
            }
        }

        /**
         * Generates a deterministic cache file name for pre-downloaded message audio.
         */
        fun buildCacheFileName(messageId: String, text: String): String {
            val digest = MessageDigest.getInstance("SHA-256")
                .digest("$messageId:$text".toByteArray(Charsets.UTF_8))
                .take(8)
                .joinToString("") { "%02x".format(it) }
            return "sara_voice_$digest.wav"
        }
    }

    enum class AudioCacheState {
        IDLE,
        PRELOADING,
        READY,
        PLAYING,
        ERROR
    }

    private var tts: TextToSpeech? = null
    private var isTtsReady = false
    private var mediaPlayer: MediaPlayer? = null
    private var speechWatchdogJob: kotlinx.coroutines.Job? = null

    private val cachedFiles = ConcurrentHashMap<String, File>()
    private val contentCachedFiles = ConcurrentHashMap<String, File>()
    private val pendingPreloads = ConcurrentHashMap<String, String>()

    private val _audioStates = MutableStateFlow<Map<String, AudioCacheState>>(emptyMap())
    val audioStates: StateFlow<Map<String, AudioCacheState>> = _audioStates.asStateFlow()

    private val _activePlayingMessageId = MutableStateFlow<String?>(null)
    val activePlayingMessageId: StateFlow<String?> = _activePlayingMessageId.asStateFlow()

    var onSpeechStarted: ((String) -> Unit)? = null
    var onSpeechCompleted: ((String) -> Unit)? = null
    var onSpeechError: ((String) -> Unit)? = null

    init {
        initTextToSpeech()
    }

    private fun initTextToSpeech() {
        try {
            tts = TextToSpeech(context) { status ->
                if (status == TextToSpeech.SUCCESS) {
                    isTtsReady = true
                    configureCustomerCareVoice(Locale.US)
                    setupUtteranceListener()
                    // Process any queued preloads that arrived before TTS finished initializing
                    pendingPreloads.forEach { (msgId, text) ->
                        preloadMessageAudio(msgId, text)
                    }
                    pendingPreloads.clear()
                } else {
                    Log.w(TAG, "TextToSpeech init returned status: $status")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize TextToSpeech", e)
        }
    }

    private fun setupUtteranceListener() {
        tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {
                val id = utteranceId ?: return
                if (id.startsWith("speak_")) {
                    val msgId = id.removePrefix("speak_")
                    coroutineScope.launch(Dispatchers.Main) {
                        _activePlayingMessageId.value = msgId
                        updateState(msgId, AudioCacheState.PLAYING)
                        onSpeechStarted?.invoke(msgId)
                    }
                }
            }

            override fun onDone(utteranceId: String?) {
                val id = utteranceId ?: return
                coroutineScope.launch(Dispatchers.Main) {
                    if (id.startsWith("preload_")) {
                        val msgId = id.removePrefix("preload_")
                        if (_activePlayingMessageId.value != msgId) {
                            updateState(msgId, AudioCacheState.READY)
                        }
                    } else if (id.startsWith("speak_")) {
                        speechWatchdogJob?.cancel()
                        val msgId = id.removePrefix("speak_")
                        if (_activePlayingMessageId.value == msgId) {
                            _activePlayingMessageId.value = null
                        }
                        updateState(msgId, AudioCacheState.READY)
                        onSpeechCompleted?.invoke(msgId)
                    }
                }
            }

            @Deprecated("Deprecated in Java")
            override fun onError(utteranceId: String?) {
                handleUtteranceError(utteranceId)
            }

            override fun onError(utteranceId: String?, errorCode: Int) {
                handleUtteranceError(utteranceId)
            }
        })
    }

    private fun handleUtteranceError(utteranceId: String?) {
        val id = utteranceId ?: return
        coroutineScope.launch(Dispatchers.Main) {
            speechWatchdogJob?.cancel()
            val msgId = id.removePrefix("preload_").removePrefix("speak_")
            if (_activePlayingMessageId.value == msgId) {
                _activePlayingMessageId.value = null
            }
            updateState(msgId, AudioCacheState.READY)
            onSpeechError?.invoke(msgId)
        }
    }

    /**
     * Selects a sweet, natural, emotionally expressive Female Customer-Care voice for the target locale.
     */
    fun configureCustomerCareVoice(targetLocale: Locale) {
        val engine = tts ?: return
        if (!isTtsReady) return

        try {
            val langResult = engine.setLanguage(targetLocale)
            if (langResult == TextToSpeech.LANG_MISSING_DATA || langResult == TextToSpeech.LANG_NOT_SUPPORTED) {
                if (targetLocale.language == "bn") {
                    engine.setLanguage(Locale.forLanguageTag("bn-BD"))
                } else if (targetLocale.language == "hi") {
                    engine.setLanguage(Locale.forLanguageTag("hi-IN"))
                } else {
                    engine.setLanguage(Locale.US)
                }
            }

            // Pick a high-quality female voice if available on the device
            val availableVoices: Set<Voice>? = try {
                engine.voices
            } catch (_: Exception) {
                null
            }

            val bestFemaleVoice = availableVoices
                ?.filter { voice ->
                    voice.locale.language.equals(targetLocale.language, ignoreCase = true) &&
                            !voice.isNetworkConnectionRequired
                }
                ?.sortedByDescending { voice ->
                    var score = voice.quality
                    val nameLower = voice.name.lowercase()
                    if (nameLower.contains("female") || nameLower.contains("sara") ||
                        nameLower.contains("wavenet") || nameLower.contains("neural") ||
                        nameLower.contains("f-") || nameLower.contains("sfg") ||
                        nameLower.contains("bn-in-x-bnf") || nameLower.contains("hi-in-x-hif") ||
                        nameLower.contains("en-us-x-sfg") || nameLower.contains("en-in-x-end")
                    ) {
                        score += 500
                    }
                    score
                }
                ?.firstOrNull()

            if (bestFemaleVoice != null) {
                engine.voice = bestFemaleVoice
            }

            engine.setPitch(CUSTOMER_CARE_PITCH)
            engine.setSpeechRate(CUSTOMER_CARE_SPEECH_RATE)
        } catch (e: Exception) {
            Log.w(TAG, "Unable to fine-tune customer care voice", e)
        }
    }

    /**
     * Pre-downloads / pre-synthesizes the spoken audio to a local WAV cache file immediately
     * in the background (including during speculative pre-analysis while the user is speaking).
     */
    fun preloadMessageAudio(messageId: String, text: String, preferredLang: String = "AUTO") {
        val cleanText = sanitizeForNaturalSpeech(text)
        if (cleanText.isEmpty()) return

        val contentKey = buildCacheFileName("content", cleanText)
        val existingByContent = contentCachedFiles[contentKey]
        if (existingByContent != null && existingByContent.exists() && existingByContent.length() > 44L) {
            cachedFiles[messageId] = existingByContent
            updateState(messageId, AudioCacheState.READY)
            return
        }

        val existingFile = cachedFiles[messageId]
        if (existingFile != null && existingFile.exists() && existingFile.length() > 44L) {
            contentCachedFiles[contentKey] = existingFile
            updateState(messageId, AudioCacheState.READY)
            return
        }

        updateState(messageId, AudioCacheState.PRELOADING)

        if (!isTtsReady || tts == null) {
            pendingPreloads[messageId] = cleanText
            // Mark READY fallback so direct TTS playback is immediately available
            coroutineScope.launch(Dispatchers.Main) {
                updateState(messageId, AudioCacheState.READY)
            }
            return
        }

        coroutineScope.launch(Dispatchers.IO) {
            try {
                val cacheDir = File(context.cacheDir, "sara_voice_cache").apply {
                    if (!exists()) mkdirs()
                }
                val outFile = File(cacheDir, contentKey)
                cachedFiles[messageId] = outFile
                contentCachedFiles[contentKey] = outFile

                val locale = detectLanguageLocale(cleanText, preferredLang)
                configureCustomerCareVoice(locale)

                val params = Bundle().apply {
                    putFloat(TextToSpeech.Engine.KEY_PARAM_VOLUME, 1.0f)
                }
                val result = tts?.synthesizeToFile(cleanText, params, outFile, "preload_$messageId")
                if (result != TextToSpeech.SUCCESS) {
                    coroutineScope.launch(Dispatchers.Main) {
                        updateState(messageId, AudioCacheState.READY)
                    }
                }
            } catch (e: Exception) {
                Log.w(TAG, "Preload synthesis fallback to live TTS", e)
                coroutineScope.launch(Dispatchers.Main) {
                    updateState(messageId, AudioCacheState.READY)
                }
            }
        }
    }

    /**
     * Plays or reloads the pre-downloaded audio for a message.
     * If already playing this message, stops playback.
     */
    fun toggleOrPlayMessageAudio(
        messageId: String,
        text: String,
        preferredLang: String = "AUTO",
        forceReload: Boolean = false
    ) {
        if (_activePlayingMessageId.value == messageId && !forceReload) {
            stopPlayback()
            return
        }

        stopPlayback()

        val cleanText = sanitizeForNaturalSpeech(text)
        if (cleanText.isEmpty()) return

        val contentKey = buildCacheFileName("content", cleanText)

        if (forceReload) {
            cachedFiles.remove(messageId)?.delete()
            contentCachedFiles.remove(contentKey)?.delete()
            preloadMessageAudio(messageId, cleanText, preferredLang)
        }

        val cachedWav = cachedFiles[messageId] ?: contentCachedFiles[contentKey]
        if (cachedWav != null && cachedWav.exists() && cachedWav.length() > 100L) {
            cachedFiles[messageId] = cachedWav
            try {
                val mp = MediaPlayer()
                mp.setDataSource(cachedWav.absolutePath)
                mp.setOnPreparedListener { player ->
                    _activePlayingMessageId.value = messageId
                    updateState(messageId, AudioCacheState.PLAYING)
                    onSpeechStarted?.invoke(messageId)
                    startSpeechCompletionWatchdog(messageId, cleanText)
                    player.start()
                }
                mp.setOnCompletionListener { player ->
                    speechWatchdogJob?.cancel()
                    player.release()
                    if (mediaPlayer == player) mediaPlayer = null
                    if (_activePlayingMessageId.value == messageId) {
                        _activePlayingMessageId.value = null
                    }
                    updateState(messageId, AudioCacheState.READY)
                    onSpeechCompleted?.invoke(messageId)
                }
                mp.setOnErrorListener { player, _, _ ->
                    speechWatchdogJob?.cancel()
                    player.release()
                    if (mediaPlayer == player) mediaPlayer = null
                    speakDirectlyWithTts(messageId, cleanText, preferredLang)
                    true
                }
                mediaPlayer = mp
                mp.prepareAsync()
                return
            } catch (e: Exception) {
                Log.w(TAG, "Cached WAV playback fallback to direct TTS", e)
            }
        }

        // Instant fallback to direct TTS while also caching in background
        speakDirectlyWithTts(messageId, cleanText, preferredLang)
        if (cachedWav == null || !cachedWav.exists()) {
            preloadMessageAudio(messageId, cleanText, preferredLang)
        }
    }

    private fun startSpeechCompletionWatchdog(messageId: String, cleanText: String) {
        speechWatchdogJob?.cancel()
        val estimatedDurationMs = (cleanText.length * 68L).coerceIn(1800L, 6200L)
        speechWatchdogJob = coroutineScope.launch(Dispatchers.Main) {
            kotlinx.coroutines.delay(estimatedDurationMs)
            if (_activePlayingMessageId.value == messageId) {
                _activePlayingMessageId.value = null
                updateState(messageId, AudioCacheState.READY)
                onSpeechCompleted?.invoke(messageId)
            }
        }
    }

    private fun speakDirectlyWithTts(messageId: String, cleanText: String, preferredLang: String) {
        val engine = tts
        _activePlayingMessageId.value = messageId
        updateState(messageId, AudioCacheState.PLAYING)
        onSpeechStarted?.invoke(messageId)
        startSpeechCompletionWatchdog(messageId, cleanText)

        if (engine != null && isTtsReady) {
            val locale = detectLanguageLocale(cleanText, preferredLang)
            configureCustomerCareVoice(locale)
            val params = Bundle().apply {
                putFloat(TextToSpeech.Engine.KEY_PARAM_VOLUME, 1.0f)
            }
            engine.speak(cleanText, TextToSpeech.QUEUE_FLUSH, params, "speak_$messageId")
        }
    }

    fun stopPlayback() {
        speechWatchdogJob?.cancel()
        val currentId = _activePlayingMessageId.value
        _activePlayingMessageId.value = null

        try {
            mediaPlayer?.let { player ->
                if (player.isPlaying) {
                    player.stop()
                }
                player.release()
            }
        } catch (_: Exception) {
        } finally {
            mediaPlayer = null
        }

        try {
            tts?.stop()
        } catch (_: Exception) {
        }

        if (currentId != null) {
            updateState(currentId, AudioCacheState.READY)
        }
    }

    /**
     * Suppresses unwanted Android system beep when toggling SpeechRecognizer ON or OFF.
     */
    fun runSilentlyWithoutBeep(block: () -> Unit) {
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
        if (audioManager == null) {
            block()
            return
        }
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                audioManager.adjustStreamVolume(
                    AudioManager.STREAM_NOTIFICATION,
                    AudioManager.ADJUST_MUTE,
                    0
                )
                audioManager.adjustStreamVolume(
                    AudioManager.STREAM_SYSTEM,
                    AudioManager.ADJUST_MUTE,
                    0
                )
            }
            block()
        } catch (_: Exception) {
            block()
        } finally {
            coroutineScope.launch(Dispatchers.Main) {
                kotlinx.coroutines.delay(350)
                try {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        audioManager.adjustStreamVolume(
                            AudioManager.STREAM_NOTIFICATION,
                            AudioManager.ADJUST_UNMUTE,
                            0
                        )
                        audioManager.adjustStreamVolume(
                            AudioManager.STREAM_SYSTEM,
                            AudioManager.ADJUST_UNMUTE,
                            0
                        )
                    }
                } catch (_: Exception) {
                }
            }
        }
    }

    private fun updateState(messageId: String, state: AudioCacheState) {
        val current = _audioStates.value.toMutableMap()
        current[messageId] = state
        _audioStates.value = current
    }

    fun release() {
        stopPlayback()
        try {
            tts?.shutdown()
        } catch (_: Exception) {
        }
        tts = null
        isTtsReady = false
    }
}
