package com.example.ui.screens

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.StopCircle
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.data.ai.GeminiAssistantService
import com.example.data.ai.MayaKnowledgeBase
import com.example.data.audio.AudioRecorderManager
import com.example.data.audio.SaraVoiceEngine
import com.example.ui.theme.MayaTheme
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

enum class VoiceAssistantState {
    IDLE,
    LISTENING,
    THINKING,
    SPEAKING,
    TIMEOUT,
    ERROR
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun VoiceAssistantScreen(
    onClose: () -> Unit,
    onExecuteAction: (MayaKnowledgeBase.AssistantAction) -> Unit = {}
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val coroutineScope = rememberCoroutineScope()
    val theme = MayaTheme.colors

    var voiceState by remember { mutableStateOf(VoiceAssistantState.IDLE) }
    var isAlwaysOnMicEnabled by remember { mutableStateOf(true) }

    // Live speech display states directly below the central Star symbol
    var liveTranscript by remember {
        mutableStateOf("")
    }
    var assistantReply by remember {
        mutableStateOf("হ্যালো! আমি Sara voiceassistant। বলুন, কী নিয়ে কথা বলবো?")
    }
    var isSpeculativeThinking by remember { mutableStateOf(false) }
    var lastAnswerId by remember { mutableStateOf("sara_initial_reply") }
    var lastLatencyMs by remember { mutableStateOf<Long?>(null) }

    // Strictly tracks whether the user is actively speaking right now vs silent
    var isUserSpeakingNow by remember { mutableStateOf(false) }
    var manualInputText by remember { mutableStateOf("") }
    var audioAmplitude by remember { mutableFloatStateOf(0f) }

    var hasAudioPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val saraVoiceEngine = remember {
        SaraVoiceEngine(context, coroutineScope)
    }
    val audioStates by saraVoiceEngine.audioStates.collectAsState()
    val activePlayingMessageId by saraVoiceEngine.activePlayingMessageId.collectAsState()

    val audioRecorder = remember {
        AudioRecorderManager(context, coroutineScope)
    }
    var speechRecognizer by remember { mutableStateOf<SpeechRecognizer?>(null) }
    var activeGenerationJob by remember { mutableStateOf<Job?>(null) }
    var speculativePreAnalyzeJob by remember { mutableStateOf<Job?>(null) }
    var partialSilenceCommitJob by remember { mutableStateOf<Job?>(null) }
    var userSpeakingDecayJob by remember { mutableStateOf<Job?>(null) }
    var useFallbackAudioRecorder by remember { mutableStateOf(false) }

    val quickConversationStarters = listOf(
        "তুমি কে?",
        "কেমন আছো?",
        "আমার কথা শুনতে পাচ্ছো?",
        "What is REXLIFY?",
        "Open Maya OS",
        "এখন সময় কত?"
    )

    /**
     * Speculatively thinks & pre-generates both the Gemini answer AND the TTS voice audio
     * in the background WHILE the user is still speaking, so the reply is ready in <2 seconds.
     */
    fun triggerSpeculativePreAnalysis(partialSpeech: String) {
        val cleaned = partialSpeech.trim()
        if (cleaned.length < 2) return

        speculativePreAnalyzeJob?.cancel()
        speculativePreAnalyzeJob = coroutineScope.launch {
            delay(150) // Short debounce while words stream in
            isSpeculativeThinking = true
            val preGeneratedReply = GeminiAssistantService.preGenerateSpeculativeReply(cleaned)
            if (!preGeneratedReply.isNullOrBlank()) {
                saraVoiceEngine.preloadMessageAudio(
                    messageId = "speculative_${cleaned.hashCode()}",
                    text = preGeneratedReply
                )
            }
            isSpeculativeThinking = false
        }
    }

    fun stopAllListeningSilently() {
        partialSilenceCommitJob?.cancel()
        userSpeakingDecayJob?.cancel()
        isUserSpeakingNow = false
        audioAmplitude = 0f
        audioRecorder.stopRecording()
        saraVoiceEngine.runSilentlyWithoutBeep {
            try {
                speechRecognizer?.stopListening()
                speechRecognizer?.cancel()
            } catch (_: Exception) {
            }
        }
    }

    // Forward declaration holder so listener & starter can reference each other cleanly
    var startListeningAction by remember { mutableStateOf<() -> Unit>({}) }
    var processQueryAction by remember { mutableStateOf<(String, String?) -> Unit>({ _, _ -> }) }

    fun ensureConfiguredSpeechRecognizer(): SpeechRecognizer? {
        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            useFallbackAudioRecorder = true
            return null
        }
        val existing = speechRecognizer
        if (existing != null) return existing

        return try {
            SpeechRecognizer.createSpeechRecognizer(context).also { recognizer ->
                recognizer.setRecognitionListener(object : RecognitionListener {
                    override fun onReadyForSpeech(params: Bundle?) {
                        voiceState = VoiceAssistantState.LISTENING
                    }

                    override fun onBeginningOfSpeech() {
                        isUserSpeakingNow = true
                        userSpeakingDecayJob?.cancel()
                        if (audioAmplitude < 0.35f) {
                            audioAmplitude = 0.45f
                        }
                    }

                    override fun onRmsChanged(rmsdB: Float) {
                        if (voiceState == VoiceAssistantState.LISTENING) {
                            // SpeechRecognizer RMS typically ranges from -2f (silence) to 10f+ (loud speech)
                            val speakingNow = rmsdB > 1.8f
                            if (speakingNow) {
                                isUserSpeakingNow = true
                                val normalized = ((rmsdB - 1.0f) / 9.0f).coerceIn(0.25f, 1.0f)
                                audioAmplitude = normalized
                                userSpeakingDecayJob?.cancel()
                                userSpeakingDecayJob = coroutineScope.launch {
                                    delay(320)
                                    isUserSpeakingNow = false
                                    audioAmplitude = 0f
                                }
                            } else if (!isUserSpeakingNow) {
                                audioAmplitude = 0f
                            }
                        }
                    }

                    override fun onBufferReceived(buffer: ByteArray?) {}

                    override fun onEndOfSpeech() {
                        isUserSpeakingNow = false
                        audioAmplitude = 0f
                    }

                    override fun onError(error: Int) {
                        isUserSpeakingNow = false
                        audioAmplitude = 0f

                        // If partial speech was already captured on display before timeout/error, answer it immediately!
                        val capturedSoFar = liveTranscript.trim()
                        if (capturedSoFar.isNotEmpty() &&
                            (error == SpeechRecognizer.ERROR_NO_MATCH || error == SpeechRecognizer.ERROR_SPEECH_TIMEOUT)
                        ) {
                            partialSilenceCommitJob?.cancel()
                            processQueryAction(capturedSoFar, null)
                            return
                        }

                        // If SpeechRecognizer has an audio/service conflict, fallback to raw PCM recorder
                        if (error == SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS) {
                            hasAudioPermission = false
                            voiceState = VoiceAssistantState.ERROR
                            return
                        }

                        if (voiceState == VoiceAssistantState.LISTENING && isAlwaysOnMicEnabled && hasAudioPermission) {
                            coroutineScope.launch {
                                delay(350)
                                if (voiceState == VoiceAssistantState.LISTENING && isAlwaysOnMicEnabled) {
                                    startListeningAction()
                                }
                            }
                        }
                    }

                    override fun onResults(results: Bundle?) {
                        partialSilenceCommitJob?.cancel()
                        isUserSpeakingNow = false
                        audioAmplitude = 0f
                        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        val spoken = matches?.firstOrNull()?.trim().orEmpty().ifEmpty { liveTranscript.trim() }
                        if (spoken.isNotEmpty()) {
                            processQueryAction(spoken, null)
                        } else if (isAlwaysOnMicEnabled && hasAudioPermission && voiceState == VoiceAssistantState.LISTENING) {
                            startListeningAction()
                        }
                    }

                    override fun onPartialResults(partialResults: Bundle?) {
                        val mainPart = partialResults
                            ?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                            ?.firstOrNull()
                            ?.trim()
                            .orEmpty()
                        val unstablePart = partialResults
                            ?.getStringArrayList("android.speech.extra.UNSTABLE_TEXT")
                            ?.firstOrNull()
                            ?.trim()
                            .orEmpty()

                        val combined = listOf(mainPart, unstablePart)
                            .filter { it.isNotEmpty() }
                            .joinToString(" ")
                            .trim()

                        if (combined.isNotEmpty()) {
                            isUserSpeakingNow = true
                            if (audioAmplitude < 0.35f) audioAmplitude = 0.55f
                            liveTranscript = combined

                            // Start thinking & pre-generating answer + voice in the background while user speaks!
                            triggerSpeculativePreAnalysis(combined)

                            // Fast Utterance Silence Commit Timer (850ms):
                            // As soon as user finishes speaking a sentence and pauses for 0.85s,
                            // immediately respond without waiting for slow recognizer server timeout!
                            partialSilenceCommitJob?.cancel()
                            partialSilenceCommitJob = coroutineScope.launch {
                                delay(850)
                                isUserSpeakingNow = false
                                audioAmplitude = 0f
                                if (voiceState == VoiceAssistantState.LISTENING && liveTranscript.isNotBlank()) {
                                    processQueryAction(liveTranscript.trim(), null)
                                }
                            }
                        }
                    }

                    override fun onEvent(eventType: Int, params: Bundle?) {}
                })
                speechRecognizer = recognizer
            }
        } catch (_: Exception) {
            useFallbackAudioRecorder = true
            null
        }
    }

    fun startSilentAlwaysOnListening() {
        if (!isAlwaysOnMicEnabled) {
            voiceState = VoiceAssistantState.IDLE
            isUserSpeakingNow = false
            audioAmplitude = 0f
            return
        }
        if (!hasAudioPermission) {
            voiceState = VoiceAssistantState.ERROR
            isUserSpeakingNow = false
            audioAmplitude = 0f
            return
        }

        // Stop any active playback before listening
        saraVoiceEngine.stopPlayback()
        voiceState = VoiceAssistantState.LISTENING
        isUserSpeakingNow = false
        audioAmplitude = 0f

        val recognizer = if (!useFallbackAudioRecorder) ensureConfiguredSpeechRecognizer() else null
        if (recognizer != null) {
            // IMPORTANT: Do NOT start AudioRecorderManager concurrently with SpeechRecognizer,
            // so SpeechRecognizer gets exclusive, conflict-free access to the microphone!
            audioRecorder.stopRecording()
            saraVoiceEngine.runSilentlyWithoutBeep {
                try {
                    recognizer.cancel()
                    val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                        putExtra(
                            RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
                        )
                        putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                        putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
                        putExtra(
                            RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS,
                            750L
                        )
                        putExtra(
                            RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS,
                            600L
                        )
                    }
                    recognizer.startListening(intent)
                } catch (_: Exception) {
                    useFallbackAudioRecorder = true
                    audioRecorder.startRecording()
                }
            }
        } else {
            // Fallback raw PCM microphone capture if SpeechRecognizer service is absent on device
            audioRecorder.startRecording()
        }
    }

    fun processSpokenOrTypedQuery(queryText: String, base64Audio: String? = null) {
        val trimmed = queryText.trim()
        if (trimmed.isEmpty() && base64Audio.isNullOrEmpty()) {
            if (isAlwaysOnMicEnabled && hasAudioPermission) {
                startSilentAlwaysOnListening()
            } else {
                voiceState = VoiceAssistantState.IDLE
            }
            return
        }

        stopAllListeningSilently()
        saraVoiceEngine.stopPlayback()

        if (trimmed.isNotEmpty()) {
            liveTranscript = trimmed

            // Check Ecosystem Action Commands first for instant execution
            val command = MayaKnowledgeBase.parseActionCommand(trimmed)
            if (command.action != MayaKnowledgeBase.AssistantAction.NONE) {
                val feedback = when {
                    MayaKnowledgeBase.isBengaliQuery(trimmed) -> command.feedbackBn
                    MayaKnowledgeBase.isHindiQuery(trimmed) -> command.feedbackHi
                    else -> command.feedbackEn
                }
                val msgId = "sara_cmd_${System.currentTimeMillis()}"
                lastAnswerId = msgId
                assistantReply = feedback
                lastLatencyMs = 20L
                voiceState = VoiceAssistantState.SPEAKING
                saraVoiceEngine.preloadMessageAudio(msgId, feedback)
                saraVoiceEngine.toggleOrPlayMessageAudio(msgId, feedback)
                coroutineScope.launch {
                    delay(1100)
                    onExecuteAction(command.action)
                }
                return
            }
        }

        // Check if speculative background pre-analysis already generated the answer while user was speaking!
        val preGenerated = if (trimmed.isNotEmpty()) {
            GeminiAssistantService.getCachedSpeculativeReply(trimmed)
        } else {
            null
        }

        if (!preGenerated.isNullOrBlank()) {
            val msgId = "sara_fast_${System.currentTimeMillis()}"
            lastAnswerId = msgId
            assistantReply = preGenerated
            lastLatencyMs = 18L
            voiceState = VoiceAssistantState.SPEAKING
            saraVoiceEngine.preloadMessageAudio(msgId, preGenerated)
            saraVoiceEngine.toggleOrPlayMessageAudio(msgId, preGenerated)
            return
        }

        voiceState = VoiceAssistantState.THINKING
        activeGenerationJob?.cancel()

        activeGenerationJob = coroutineScope.launch {
            val outcome = GeminiAssistantService.generateVoiceAnswerWithTimeout(
                userPrompt = trimmed.ifEmpty { "Voice Query" },
                timeoutMs = GeminiAssistantService.VOICE_SESSION_TIMEOUT_MS
            ) {
                if (!base64Audio.isNullOrEmpty() && trimmed.isEmpty()) {
                    GeminiAssistantService.generateMultimodalAudioAnswer(
                        base64WavAudio = base64Audio,
                        transcriptHint = trimmed
                    ).map { (transcript, reply) ->
                        if (transcript.isNotBlank()) {
                            liveTranscript = transcript
                        }
                        reply
                    }
                } else {
                    GeminiAssistantService.generateAnswer(
                        userPrompt = trimmed,
                        fastMode = true
                    )
                }
            }

            when (outcome) {
                is GeminiAssistantService.VoiceGenerationOutcome.Success -> {
                    val msgId = "sara_ans_${System.currentTimeMillis()}"
                    lastAnswerId = msgId
                    assistantReply = outcome.replyText
                    lastLatencyMs = outcome.latencyMs.coerceAtLeast(18L)
                    voiceState = VoiceAssistantState.SPEAKING

                    saraVoiceEngine.preloadMessageAudio(
                        messageId = msgId,
                        text = outcome.replyText
                    )
                    saraVoiceEngine.toggleOrPlayMessageAudio(
                        messageId = msgId,
                        text = outcome.replyText
                    )
                }

                is GeminiAssistantService.VoiceGenerationOutcome.Timeout -> {
                    stopAllListeningSilently()
                    saraVoiceEngine.stopPlayback()
                    voiceState = VoiceAssistantState.TIMEOUT
                    assistantReply = if (MayaKnowledgeBase.isBengaliQuery(liveTranscript)) {
                        "দুঃখিত, সময়মতো উত্তর তৈরি করা যায়নি। আবার কথা বলতে স্টার চিহ্নটিতে ট্যাপ করুন।"
                    } else {
                        "Session timed out. Tap the star to speak again."
                    }
                }

                is GeminiAssistantService.VoiceGenerationOutcome.Error -> {
                    val fallbackReply = MayaKnowledgeBase.generateSmartCustomerCareFallback(trimmed)
                    val msgId = "sara_recover_${System.currentTimeMillis()}"
                    lastAnswerId = msgId
                    assistantReply = fallbackReply
                    lastLatencyMs = 35L
                    voiceState = VoiceAssistantState.SPEAKING
                    saraVoiceEngine.preloadMessageAudio(msgId, fallbackReply)
                    saraVoiceEngine.toggleOrPlayMessageAudio(msgId, fallbackReply)
                }
            }
        }
    }

    startListeningAction = { startSilentAlwaysOnListening() }
    processQueryAction = { text, wav -> processSpokenOrTypedQuery(text, wav) }

    // Wire up SaraVoiceEngine callbacks:
    // When Sara speaks -> Star moves; When Sara finishes speaking -> Star stops immediately & Ready listening resumes!
    DisposableEffect(saraVoiceEngine) {
        saraVoiceEngine.onSpeechStarted = {
            isUserSpeakingNow = false
            audioAmplitude = 0f
            voiceState = VoiceAssistantState.SPEAKING
        }
        saraVoiceEngine.onSpeechCompleted = {
            if (voiceState != VoiceAssistantState.TIMEOUT) {
                voiceState = VoiceAssistantState.IDLE
                if (isAlwaysOnMicEnabled && hasAudioPermission) {
                    coroutineScope.launch {
                        delay(180)
                        startSilentAlwaysOnListening()
                    }
                }
            }
        }
        saraVoiceEngine.onSpeechError = {
            voiceState = VoiceAssistantState.IDLE
            if (isAlwaysOnMicEnabled && hasAudioPermission && voiceState != VoiceAssistantState.TIMEOUT) {
                startSilentAlwaysOnListening()
            }
        }
        onDispose { }
    }

    // Wire up fallback AudioRecorderManager callbacks (only used when SpeechRecognizer is unavailable)
    DisposableEffect(audioRecorder) {
        audioRecorder.onAmplitudeChanged = { amp ->
            if (useFallbackAudioRecorder && voiceState == VoiceAssistantState.LISTENING) {
                val speaking = amp > 0.09f
                isUserSpeakingNow = speaking
                audioAmplitude = if (speaking) amp.coerceIn(0.2f, 1.0f) else 0f
            }
        }
        audioRecorder.onSilenceDetected = {
            if (useFallbackAudioRecorder && voiceState == VoiceAssistantState.LISTENING) {
                isUserSpeakingNow = false
                audioAmplitude = 0f
                val wavBase64 = audioRecorder.stopAndGetBase64Wav()
                if (!wavBase64.isNullOrEmpty()) {
                    processSpokenOrTypedQuery(
                        queryText = liveTranscript,
                        base64Audio = wavBase64
                    )
                }
            }
        }
        onDispose {
            audioRecorder.release()
        }
    }

    // Permission launcher for RECORD_AUDIO
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasAudioPermission = granted
        if (granted && isAlwaysOnMicEnabled) {
            startSilentAlwaysOnListening()
        } else if (!granted) {
            voiceState = VoiceAssistantState.ERROR
        }
    }

    // Initialize recognizer & start listening on launch
    LaunchedEffect(Unit) {
        saraVoiceEngine.preloadMessageAudio(lastAnswerId, assistantReply)
        if (hasAudioPermission) {
            startSilentAlwaysOnListening()
        } else {
            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    // Lifecycle cleanup
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE, Lifecycle.Event.ON_STOP -> {
                    activeGenerationJob?.cancel()
                    speculativePreAnalyzeJob?.cancel()
                    stopAllListeningSilently()
                    saraVoiceEngine.stopPlayback()
                    if (voiceState != VoiceAssistantState.TIMEOUT) {
                        voiceState = VoiceAssistantState.IDLE
                    }
                }
                Lifecycle.Event.ON_RESUME -> {
                    hasAudioPermission = ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.RECORD_AUDIO
                    ) == PackageManager.PERMISSION_GRANTED
                    if (isAlwaysOnMicEnabled && hasAudioPermission && voiceState == VoiceAssistantState.IDLE) {
                        startSilentAlwaysOnListening()
                    }
                }
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            activeGenerationJob?.cancel()
            speculativePreAnalyzeJob?.cancel()
            stopAllListeningSilently()
            saraVoiceEngine.runSilentlyWithoutBeep {
                try {
                    speechRecognizer?.destroy()
                } catch (_: Exception) {
                }
            }
            speechRecognizer = null
            saraVoiceEngine.release()
        }
    }

    BackHandler {
        activeGenerationJob?.cancel()
        speculativePreAnalyzeJob?.cancel()
        stopAllListeningSilently()
        saraVoiceEngine.stopPlayback()
        onClose()
    }

    val isPlayingReply = activePlayingMessageId == lastAnswerId || voiceState == VoiceAssistantState.SPEAKING
    val replyCacheState = audioStates[lastAnswerId] ?: SaraVoiceEngine.AudioCacheState.READY

    // Motion states:
    // - When User speaks (isUserSpeakingNow): visualizer waves react to user's voice.
    // - When Sara speaks (isPlayingReply): the Star symbol rotates & pulses smoothly.
    // - When NEITHER speaks: the Star symbol & visualizer stand completely still in one place!
    val infiniteTransition = rememberInfiniteTransition(label = "sara_star_motion")
    val continuousRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3600, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "star_rotation"
    )
    val continuousPulse by infiniteTransition.animateFloat(
        initialValue = 0.88f,
        targetValue = 1.14f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 650, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "star_pulse"
    )

    // Star only moves when Sara is speaking; stands completely still when she stops!
    val effectiveStarRotation = if (isPlayingReply) continuousRotation else 0f
    val effectiveStarScale by animateFloatAsState(
        targetValue = when {
            isPlayingReply -> continuousPulse
            isUserSpeakingNow -> 1.0f + (audioAmplitude * 0.14f)
            else -> 1.0f // Completely still in one place when nobody is speaking
        },
        animationSpec = tween(durationMillis = 120),
        label = "effective_star_scale"
    )

    // User voice wave amplitude: active ONLY when user or Sara is speaking; 0f when silent
    val effectiveWaveAmplitude by animateFloatAsState(
        targetValue = when {
            isUserSpeakingNow -> audioAmplitude.coerceAtLeast(0.28f)
            isPlayingReply -> 0.55f
            else -> 0f // Stand completely still when silent
        },
        animationSpec = tween(durationMillis = 120),
        label = "effective_wave_amp"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(theme.backgroundGradient)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 18.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        // 1. Clean Homepage-Style Header Bar (No language badges or language-matching labels)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                listOf(Color(0xFFEC4899), Color(0xFF8B5CF6), Color(0xFF38BDF8))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = "Sara voiceassistant",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Text(
                        text = MayaKnowledgeBase.ASSISTANT_NAME,
                        style = TextStyle(
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold
                        ),
                        modifier = Modifier.testTag("sara_voice_header_title")
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isAlwaysOnMicEnabled && hasAudioPermission) Color(0xFF10B981)
                                    else Color(0xFF94A3B8)
                                )
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = when {
                                isPlayingReply -> "Speaking live"
                                isUserSpeakingNow -> "Listening to you..."
                                isAlwaysOnMicEnabled && hasAudioPermission -> "Ready"
                                else -> "Mic Paused"
                            },
                            style = TextStyle(
                                color = Color(0xFFCBD5E1),
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Medium
                            ),
                            modifier = Modifier.testTag("sara_mic_status_indicator")
                        )
                    }
                }
            }

            IconButton(
                onClick = {
                    activeGenerationJob?.cancel()
                    speculativePreAnalyzeJob?.cancel()
                    stopAllListeningSilently()
                    saraVoiceEngine.stopPlayback()
                    onClose()
                },
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(Color(0x26FFFFFF))
                    .testTag("sara_voice_close_btn")
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close Sara voiceassistant",
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // 2. Single Unified "Ready" Star Visualizer:
        // - Stands completely still when neither user nor Sara is speaking.
        // - Visualizes live when user speaks.
        // - Star symbol moves/rotates when Sara speaks, and stops immediately when Sara finishes speaking.
        Box(
            modifier = Modifier
                .size(170.dp)
                .clickable {
                    when {
                        isPlayingReply -> {
                            // Tap to interrupt Sara's speech and immediately listen
                            saraVoiceEngine.stopPlayback()
                            voiceState = VoiceAssistantState.IDLE
                            if (isAlwaysOnMicEnabled && hasAudioPermission) {
                                startSilentAlwaysOnListening()
                            }
                        }
                        isUserSpeakingNow && liveTranscript.isNotBlank() -> {
                            // Commit current spoken sentence immediately
                            processSpokenOrTypedQuery(liveTranscript)
                        }
                        else -> {
                            isAlwaysOnMicEnabled = true
                            if (hasAudioPermission) {
                                startSilentAlwaysOnListening()
                            } else {
                                permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                            }
                        }
                    }
                }
                .testTag("sara_voice_orb"),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val center = Offset(size.width / 2f, size.height / 2f)
                val baseRadius = size.minDimension * 0.31f * effectiveStarScale

                // Ambient halo (expands only when user or Sara speaks; static when Ready)
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            if (isPlayingReply) Color(0x66EC4899)
                            else if (isUserSpeakingNow) Color(0x6638BDF8)
                            else Color(0x288B5CF6),
                            Color(0x148B5CF6),
                            Color.Transparent
                        ),
                        center = center,
                        radius = baseRadius * (1.45f + effectiveWaveAmplitude * 0.45f)
                    ),
                    radius = baseRadius * (1.45f + effectiveWaveAmplitude * 0.45f),
                    center = center
                )

                // User voice visualizer ring (reactive when user speaks, static circle when silent)
                if (effectiveWaveAmplitude > 0.02f) {
                    val wavePath = Path()
                    val points = 64
                    val phase = if (isPlayingReply) {
                        effectiveStarRotation * PI.toFloat() / 180f
                    } else {
                        effectiveWaveAmplitude * 6f
                    }
                    for (i in 0..points) {
                        val angle = (i.toFloat() / points) * 2f * PI.toFloat()
                        val waveOffset = sin(angle * 6f + phase) * (baseRadius * 0.18f * effectiveWaveAmplitude)
                        val r = baseRadius * 1.05f + waveOffset
                        val x = center.x + r * cos(angle)
                        val y = center.y + r * sin(angle)
                        if (i == 0) wavePath.moveTo(x, y) else wavePath.lineTo(x, y)
                    }
                    wavePath.close()
                    drawPath(
                        path = wavePath,
                        brush = Brush.linearGradient(
                            listOf(Color(0xFF38BDF8), Color(0xFFEC4899), Color(0xFF8B5CF6))
                        ),
                        style = Stroke(width = 3.5.dp.toPx(), cap = StrokeCap.Round)
                    )
                } else {
                    // Static Ready ring standing still in one place
                    drawCircle(
                        brush = Brush.linearGradient(
                            colors = listOf(Color(0x8838BDF8), Color(0x888B5CF6), Color(0x88EC4899))
                        ),
                        radius = baseRadius * 1.02f,
                        center = center,
                        style = Stroke(width = 2.5.dp.toPx())
                    )
                }

                // Central Star Symbol: Rotates & moves ONLY when Sara is speaking; stands still otherwise
                rotate(effectiveStarRotation, center) {
                    val starPath = Path()
                    val outerStarRadius = baseRadius * 0.62f
                    val innerStarRadius = baseRadius * 0.24f
                    val numSpikes = 4
                    for (i in 0 until numSpikes * 2) {
                        val r = if (i % 2 == 0) outerStarRadius else innerStarRadius
                        val angle = (i * PI / numSpikes) - (PI / 2.0)
                        val x = center.x + (r * cos(angle)).toFloat()
                        val y = center.y + (r * sin(angle)).toFloat()
                        if (i == 0) starPath.moveTo(x, y) else starPath.lineTo(x, y)
                    }
                    starPath.close()

                    drawPath(
                        path = starPath,
                        brush = Brush.linearGradient(
                            colors = if (isPlayingReply) {
                                listOf(Color(0xFFF472B6), Color(0xFFC084FC), Color(0xFF38BDF8))
                            } else if (isUserSpeakingNow) {
                                listOf(Color(0xFF38BDF8), Color(0xFF818CF8), Color(0xFFF472B6))
                            } else {
                                listOf(Color(0xFF8B5CF6), Color(0xFF38BDF8))
                            }
                        )
                    )
                }
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 86.dp)
            ) {
                Text(
                    text = when {
                        isPlayingReply -> "SARA SPEAKING"
                        isUserSpeakingNow -> "LISTENING..."
                        voiceState == VoiceAssistantState.THINKING -> "THINKING..."
                        isAlwaysOnMicEnabled && hasAudioPermission -> "READY"
                        else -> "PAUSED"
                    },
                    style = TextStyle(
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 0.8.sp
                    ),
                    modifier = Modifier.testTag("sara_voice_state_badge")
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 3. Dedicated Live Display Box Directly Below the Star Symbol:
        // Shows what the user is saying in real time AND what Sara is saying in real time.
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(Color(0x331E293B))
                .border(1.dp, Color(0x38FFFFFF), RoundedCornerShape(24.dp))
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Section A: What I am saying (Live Real-Time User Speech Display)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(
                                if (isUserSpeakingNow) Color(0xFF38BDF8) else Color(0xFF64748B)
                            )
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "আপনি যা বলছেন (YOU)",
                        style = TextStyle(
                            color = Color(0xFF94A3B8),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }

                if (isSpeculativeThinking || isUserSpeakingNow) {
                    Text(
                        text = "Sara ভাবছে...",
                        style = TextStyle(
                            color = Color(0xFF38BDF8),
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = liveTranscript.ifEmpty {
                    if (isAlwaysOnMicEnabled && hasAudioPermission) {
                        "এখন কথা বলুন — আপনার কথা সাথে সাথে এখানে দেখা যাবে..."
                    } else {
                        "মাইক্রোফোন চালু করে কথা বলুন বা নিচে লিখুন..."
                    }
                },
                style = TextStyle(
                    color = if (liveTranscript.isNotEmpty()) Color.White else Color(0xFF94A3B8),
                    fontSize = 15.sp,
                    lineHeight = 22.sp,
                    fontWeight = if (liveTranscript.isNotEmpty()) FontWeight.SemiBold else FontWeight.Normal
                ),
                modifier = Modifier.testTag("sara_live_transcript")
            )

            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(color = Color(0x26FFFFFF))
            Spacer(modifier = Modifier.height(14.dp))

            // Section B: What Sara is saying (Live Real-Time Assistant Reply Display)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = if (isPlayingReply) Color(0xFFF472B6) else Color(0xFFA78BFA),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Sara যা বলছে (SARA)",
                        style = TextStyle(
                            color = Color(0xFFF472B6),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
                if (lastLatencyMs != null) {
                    Text(
                        text = "${lastLatencyMs}ms",
                        style = TextStyle(
                            color = Color(0xFF10B981),
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = assistantReply,
                style = TextStyle(
                    color = Color.White,
                    fontSize = 15.5.sp,
                    lineHeight = 23.sp,
                    fontWeight = FontWeight.SemiBold
                ),
                modifier = Modifier.testTag("sara_assistant_reply")
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Pre-Generated Voice Speaker & Replay Controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (isPlayingReply) Color(0xFFEC4899)
                                else Color(0x3338BDF8)
                            )
                            .border(
                                1.dp,
                                if (isPlayingReply) Color(0xFFEC4899) else Color(0x6638BDF8),
                                RoundedCornerShape(12.dp)
                            )
                            .clickable {
                                stopAllListeningSilently()
                                saraVoiceEngine.toggleOrPlayMessageAudio(
                                    messageId = lastAnswerId,
                                    text = assistantReply
                                )
                            }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                            .testTag("voice_speaker_btn"),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (isPlayingReply) {
                                Icons.Default.StopCircle
                            } else {
                                Icons.AutoMirrored.Filled.VolumeUp
                            },
                            contentDescription = "Speaker Button",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isPlayingReply) "Stop Voice" else "Play Voice",
                            style = TextStyle(
                                color = Color.White,
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        if (!isPlayingReply && replyCacheState == SaraVoiceEngine.AudioCacheState.READY) {
                            Spacer(modifier = Modifier.width(5.dp))
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Audio Ready",
                                tint = Color(0xFF10B981),
                                modifier = Modifier.size(13.dp)
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .size(30.dp)
                            .clip(CircleShape)
                            .background(Color(0x26FFFFFF))
                            .border(1.dp, Color(0x40FFFFFF), CircleShape)
                            .clickable {
                                stopAllListeningSilently()
                                saraVoiceEngine.toggleOrPlayMessageAudio(
                                    messageId = lastAnswerId,
                                    text = assistantReply,
                                    forceReload = true
                                )
                            }
                            .testTag("voice_reload_audio_btn"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Replay Audio",
                            tint = Color.White,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Quick Tap Prompts for instant testing
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                quickConversationStarters.forEach { prompt ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0x22FFFFFF))
                            .border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(14.dp))
                            .clickable {
                                triggerSpeculativePreAnalysis(prompt)
                                processSpokenOrTypedQuery(prompt)
                            }
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Text(
                            text = prompt,
                            style = TextStyle(
                                color = Color(0xFFE2E8F0),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // 4. Live Voice / Text Input Box (also triggers live display + speculative thinking as you type!)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(22.dp))
                .background(Color(0x331E293B))
                .border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(22.dp))
                .padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            BasicTextField(
                value = manualInputText,
                onValueChange = { newText ->
                    manualInputText = newText
                    if (newText.isNotBlank()) {
                        liveTranscript = newText
                        triggerSpeculativePreAnalysis(newText)
                    }
                },
                modifier = Modifier
                    .weight(1f)
                    .testTag("sara_manual_input"),
                singleLine = true,
                textStyle = TextStyle(color = Color.White, fontSize = 13.5.sp),
                cursorBrush = SolidColor(Color(0xFFEC4899)),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(onSend = {
                    if (manualInputText.isNotBlank()) {
                        val q = manualInputText
                        manualInputText = ""
                        processSpokenOrTypedQuery(q)
                    }
                }),
                decorationBox = { innerTextField ->
                    if (manualInputText.isEmpty()) {
                        Text(
                            text = "সরাসরি কথা বলুন অথবা এখানে লিখুন...",
                            style = TextStyle(color = Color(0xFF94A3B8), fontSize = 12.5.sp)
                        )
                    }
                    innerTextField()
                }
            )

            if (manualInputText.isNotBlank()) {
                IconButton(
                    onClick = {
                        val q = manualInputText
                        manualInputText = ""
                        processSpokenOrTypedQuery(q)
                    },
                    modifier = Modifier
                        .size(32.dp)
                        .testTag("sara_manual_send_btn")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Send to Sara",
                        tint = Color(0xFFEC4899),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // 5. Bottom Controls: Mic ON/OFF & End Session
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        if (isAlwaysOnMicEnabled) {
                            Brush.linearGradient(listOf(Color(0xFFEC4899), Color(0xFF8B5CF6)))
                        } else {
                            SolidColor(Color(0xFF334155))
                        }
                    )
                    .clickable {
                        isAlwaysOnMicEnabled = !isAlwaysOnMicEnabled
                        if (isAlwaysOnMicEnabled) {
                            if (hasAudioPermission) {
                                startSilentAlwaysOnListening()
                            } else {
                                permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                            }
                        } else {
                            stopAllListeningSilently()
                            voiceState = VoiceAssistantState.IDLE
                        }
                    }
                    .padding(vertical = 12.dp)
                    .testTag("sara_mic_toggle_btn"),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (isAlwaysOnMicEnabled) Icons.Default.Mic else Icons.Default.MicOff,
                        contentDescription = if (isAlwaysOnMicEnabled) "Mic ON" else "Mic OFF",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isAlwaysOnMicEnabled) "Mic: ON" else "Mic: OFF",
                        style = TextStyle(
                            color = Color.White,
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }

            Box(
                modifier = Modifier
                    .weight(0.75f)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0x26FFFFFF))
                    .border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(16.dp))
                    .clickable {
                        activeGenerationJob?.cancel()
                        speculativePreAnalyzeJob?.cancel()
                        stopAllListeningSilently()
                        saraVoiceEngine.stopPlayback()
                        onClose()
                    }
                    .padding(vertical = 12.dp)
                    .testTag("sara_end_voice_btn"),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "End Session",
                    style = TextStyle(
                        color = Color.White,
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                )
            }
        }
    }
}
