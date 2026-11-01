package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import com.example.ui.components.rememberChromeSmoothFlingBehavior
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.StopCircle
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.data.ai.GeminiAssistantService
import com.example.data.ai.MayaKnowledgeBase
import com.example.data.audio.SaraVoiceEngine
import com.example.ui.components.glassmorphic
import com.example.ui.theme.MayaTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class ChatMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val sender: MessageSender,
    val text: String,
    val timestamp: String = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date()),
    val responseTimeMs: Long? = null
)

enum class MessageSender {
    USER, GEMINI
}

@Composable
fun ChatAssistantScreen(
    initialPrompt: String = "",
    onClose: () -> Unit,
    onExecuteAction: (MayaKnowledgeBase.AssistantAction) -> Unit = {}
) {
    val theme = MayaTheme.colors
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val coroutineScope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    val saraVoiceEngine = remember {
        SaraVoiceEngine(context, coroutineScope)
    }
    val audioStates by saraVoiceEngine.audioStates.collectAsState()
    val activePlayingMessageId by saraVoiceEngine.activePlayingMessageId.collectAsState()

    val welcomeMessage = remember {
        ChatMessage(
            id = "welcome_msg_sara",
            sender = MessageSender.GEMINI,
            text = "নমস্কার! Hello! I am Sara voiceassistant, your premium customer-care AI assistant. Ask me anything in Bengali, Hindi, or English—and tap the Speaker button below any answer to hear it instantly!",
            responseTimeMs = 45L
        )
    }

    val messages = remember {
        mutableStateListOf(welcomeMessage)
    }

    var inputText by remember { mutableStateOf(initialPrompt) }
    var isGenerating by remember { mutableStateOf(false) }

    val suggestions = listOf(
        "তুমি কে?",
        "What is REXLIFY?",
        "Who is the developer?",
        "হিন্দিতে কথা বলো",
        "Tell me about Maya OS",
        "Open NeuraX"
    )

    // Pre-download audio for welcome message immediately on launch
    LaunchedEffect(Unit) {
        saraVoiceEngine.preloadMessageAudio(welcomeMessage.id, welcomeMessage.text)
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_PAUSE || event == Lifecycle.Event.ON_STOP) {
                saraVoiceEngine.stopPlayback()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            saraVoiceEngine.release()
        }
    }

    fun addAssistantMessageAndPreloadAudio(replyText: String, elapsedMs: Long) {
        val newMsg = ChatMessage(
            sender = MessageSender.GEMINI,
            text = replyText,
            responseTimeMs = elapsedMs
        )
        messages.add(newMsg)
        // Immediately pre-download / pre-synthesize spoken audio so Speaker button plays with zero delay
        saraVoiceEngine.preloadMessageAudio(newMsg.id, newMsg.text)
    }

    fun sendMessage(promptText: String) {
        val trimmed = promptText.trim()
        if (trimmed.isEmpty() || isGenerating) return

        messages.add(ChatMessage(sender = MessageSender.USER, text = trimmed))
        inputText = ""

        // Check Action Commands first
        val command = MayaKnowledgeBase.parseActionCommand(trimmed)
        if (command.action != MayaKnowledgeBase.AssistantAction.NONE) {
            val feedback = when {
                MayaKnowledgeBase.isBengaliQuery(trimmed) -> command.feedbackBn
                MayaKnowledgeBase.isHindiQuery(trimmed) -> command.feedbackHi
                else -> command.feedbackEn
            }
            addAssistantMessageAndPreloadAudio(feedback, 35L)
            coroutineScope.launch {
                listState.animateScrollToItem(messages.size - 1)
                delay(900)
                onExecuteAction(command.action)
            }
            return
        }

        isGenerating = true

        coroutineScope.launch {
            listState.animateScrollToItem(messages.size - 1)
            val startMs = System.currentTimeMillis()
            // Ultra-fast <2s analysis target
            val fastResult = withTimeoutOrNull(GeminiAssistantService.FAST_CHAT_TIMEOUT_MS) {
                GeminiAssistantService.generateAnswer(trimmed, fastMode = true)
            }
            val elapsedMs = (System.currentTimeMillis() - startMs).coerceAtLeast(25L)
            isGenerating = false

            val finalAnswer = fastResult?.getOrNull()
                ?: MayaKnowledgeBase.generateSmartCustomerCareFallback(trimmed)

            addAssistantMessageAndPreloadAudio(finalAnswer, elapsedMs)
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    LaunchedEffect(initialPrompt) {
        if (initialPrompt.isNotBlank()) {
            sendMessage(initialPrompt)
        }
    }

    BackHandler {
        saraVoiceEngine.stopPlayback()
        onClose()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(theme.background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
    ) {
        // 1. Header Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .glassmorphic(
                    shape = RoundedCornerShape(0.dp),
                    backgroundColor = if (theme.isDark) Color(0xF2111520) else Color(0xF2FFFFFF),
                    borderColor = theme.borderGlass,
                    borderWidth = 1.dp
                )
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = {
                        saraVoiceEngine.stopPlayback()
                        onClose()
                    },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = theme.textPrimary
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                colors = listOf(Color(0xFFEC4899), Color(0xFF8B5CF6))
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
                            color = theme.textPrimary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        modifier = Modifier.testTag("chat_assistant_title")
                    )
                    Text(
                        text = "Premium Customer Care • <2s Fast AI • Preloaded Audio",
                        style = TextStyle(
                            color = theme.accentPrimary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    )
                }
            }

            IconButton(
                onClick = {
                    saraVoiceEngine.stopPlayback()
                    messages.clear()
                    val clearedMsg = ChatMessage(
                        sender = MessageSender.GEMINI,
                        text = "Chat cleared! I am Sara voiceassistant. How can I help you next in Bengali, Hindi, or English?",
                        responseTimeMs = 20L
                    )
                    messages.add(clearedMsg)
                    saraVoiceEngine.preloadMessageAudio(clearedMsg.id, clearedMsg.text)
                },
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.DeleteOutline,
                    contentDescription = "Clear Chat",
                    tint = theme.textSecondary
                )
            }
        }

        // 2. Chat Messages Area
        LazyColumn(
            state = listState,
            flingBehavior = rememberChromeSmoothFlingBehavior(),
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            items(messages, key = { it.id }) { msg ->
                val isUser = msg.sender == MessageSender.USER
                val isPlayingThis = activePlayingMessageId == msg.id
                val cacheState = audioStates[msg.id] ?: SaraVoiceEngine.AudioCacheState.READY

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
                ) {
                    Box(
                        modifier = Modifier
                            .widthIn(max = 325.dp)
                            .clip(
                                RoundedCornerShape(
                                    topStart = 18.dp,
                                    topEnd = 18.dp,
                                    bottomStart = if (isUser) 18.dp else 4.dp,
                                    bottomEnd = if (isUser) 4.dp else 18.dp
                                )
                            )
                            .background(
                                if (isUser) {
                                    Brush.linearGradient(
                                        listOf(theme.accentPrimary, theme.accentSecondary)
                                    )
                                } else {
                                    SolidColor(
                                        if (theme.isDark) Color(0xFF1E2433) else Color(0xFFF1F5F9)
                                    )
                                }
                            )
                            .border(
                                1.dp,
                                if (isUser) Color.Transparent else theme.borderGlass,
                                RoundedCornerShape(
                                    topStart = 18.dp,
                                    topEnd = 18.dp,
                                    bottomStart = if (isUser) 18.dp else 4.dp,
                                    bottomEnd = if (isUser) 4.dp else 18.dp
                                )
                            )
                            .padding(horizontal = 14.dp, vertical = 10.dp)
                    ) {
                        Column {
                            Text(
                                text = msg.text,
                                style = TextStyle(
                                    color = if (isUser) Color.White else theme.textPrimary,
                                    fontSize = 14.sp,
                                    lineHeight = 20.sp,
                                    fontWeight = FontWeight.Normal
                                )
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            // Assistant Message Footer: Speaker Button (Pre-downloaded Audio) + Reload Audio + Copy
                            if (!isUser) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        // Speaker Button to play/stop pre-downloaded audio
                                        Row(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(
                                                    if (isPlayingThis) Color(0xFFEC4899)
                                                    else theme.accentPrimary.copy(alpha = 0.16f)
                                                )
                                                .border(
                                                    1.dp,
                                                    if (isPlayingThis) Color(0xFFEC4899)
                                                    else theme.accentPrimary.copy(alpha = 0.4f),
                                                    RoundedCornerShape(12.dp)
                                                )
                                                .clickable {
                                                    saraVoiceEngine.toggleOrPlayMessageAudio(
                                                        messageId = msg.id,
                                                        text = msg.text
                                                    )
                                                }
                                                .padding(horizontal = 10.dp, vertical = 5.dp)
                                                .testTag("chat_speaker_btn"),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = if (isPlayingThis) {
                                                    Icons.Default.StopCircle
                                                } else {
                                                    Icons.AutoMirrored.Filled.VolumeUp
                                                },
                                                contentDescription = if (isPlayingThis) "Stop Speaker" else "Play Speaker Audio",
                                                tint = if (isPlayingThis) Color.White else theme.accentPrimary,
                                                modifier = Modifier.size(15.dp)
                                            )
                                            Spacer(modifier = Modifier.width(5.dp))
                                            Text(
                                                text = when {
                                                    isPlayingThis -> "Playing..."
                                                    cacheState == SaraVoiceEngine.AudioCacheState.PRELOADING -> "Downloading Audio..."
                                                    else -> "Listen (Ready)"
                                                },
                                                style = TextStyle(
                                                    color = if (isPlayingThis) Color.White else theme.accentPrimary,
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            )
                                            if (!isPlayingThis && cacheState == SaraVoiceEngine.AudioCacheState.READY) {
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Icon(
                                                    imageVector = Icons.Default.CheckCircle,
                                                    contentDescription = "Audio Pre-Downloaded",
                                                    tint = Color(0xFF10B981),
                                                    modifier = Modifier.size(12.dp)
                                                )
                                            }
                                        }

                                        // Reload / Re-synthesize Audio Button
                                        Box(
                                            modifier = Modifier
                                                .size(26.dp)
                                                .clip(CircleShape)
                                                .background(theme.surfaceGlass)
                                                .border(1.dp, theme.borderGlass, CircleShape)
                                                .clickable {
                                                    saraVoiceEngine.toggleOrPlayMessageAudio(
                                                        messageId = msg.id,
                                                        text = msg.text,
                                                        forceReload = true
                                                    )
                                                }
                                                .testTag("chat_reload_audio_btn"),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Refresh,
                                                contentDescription = "Reload Audio",
                                                tint = theme.textSecondary,
                                                modifier = Modifier.size(13.dp)
                                            )
                                        }
                                    }

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        if (msg.responseTimeMs != null) {
                                            Text(
                                                text = "${msg.responseTimeMs}ms • ",
                                                style = TextStyle(
                                                    color = Color(0xFF10B981),
                                                    fontSize = 9.5.sp,
                                                    fontWeight = FontWeight.SemiBold
                                                )
                                            )
                                        }
                                        Text(
                                            text = msg.timestamp,
                                            style = TextStyle(
                                                color = theme.textSecondary.copy(alpha = 0.7f),
                                                fontSize = 10.sp
                                            )
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Icon(
                                            imageVector = Icons.Default.ContentCopy,
                                            contentDescription = "Copy",
                                            tint = theme.textSecondary.copy(alpha = 0.7f),
                                            modifier = Modifier
                                                .size(14.dp)
                                                .clickable {
                                                    val clipboard =
                                                        context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                                    clipboard.setPrimaryClip(
                                                        ClipData.newPlainText("Sara Response", msg.text)
                                                    )
                                                    Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
                                                }
                                        )
                                    }
                                }
                            } else {
                                Row(
                                    modifier = Modifier.align(Alignment.End),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = msg.timestamp,
                                        style = TextStyle(
                                            color = Color.White.copy(alpha = 0.7f),
                                            fontSize = 10.sp
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }

            if (isGenerating) {
                item {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (theme.isDark) Color(0xFF1E2433) else Color(0xFFF1F5F9))
                            .padding(horizontal = 14.dp, vertical = 10.dp)
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = theme.accentPrimary,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Sara is analyzing (<2s fast response)...",
                            style = TextStyle(
                                color = theme.textSecondary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                        )
                    }
                }
            }
        }

        // 3. Quick Suggestions
        if (messages.size <= 2 && !isGenerating) {
            LazyRow(
                flingBehavior = rememberChromeSmoothFlingBehavior(),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(suggestions) { suggestion ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(theme.surfaceGlass)
                            .border(1.dp, theme.borderGlass, RoundedCornerShape(20.dp))
                            .clickable { sendMessage(suggestion) }
                            .padding(horizontal = 12.dp, vertical = 7.dp)
                    ) {
                        Text(
                            text = suggestion,
                            style = TextStyle(
                                color = theme.textSecondary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        )
                    }
                }
            }
        }

        // 4. Input Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .glassmorphic(
                    shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
                    backgroundColor = if (theme.isDark) Color(0xF2111520) else Color(0xF2FFFFFF),
                    borderColor = theme.borderGlass,
                    borderWidth = 1.dp
                )
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(24.dp))
                    .background(theme.surfaceGlass)
                    .border(1.dp, theme.borderGlass, RoundedCornerShape(24.dp))
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                BasicTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    textStyle = TextStyle(
                        color = theme.textPrimary,
                        fontSize = 14.sp
                    ),
                    cursorBrush = SolidColor(theme.accentPrimary),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                    keyboardActions = KeyboardActions(onSend = { sendMessage(inputText) }),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("chat_input_field"),
                    decorationBox = { innerTextField ->
                        if (inputText.isEmpty()) {
                            Text(
                                text = "Ask Sara anything in বাংলা, हिंदी, or English...",
                                style = TextStyle(
                                    color = theme.textSecondary.copy(alpha = 0.65f),
                                    fontSize = 14.sp
                                )
                            )
                        }
                        innerTextField()
                    }
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(
                        if (inputText.isNotBlank() && !isGenerating) {
                            theme.accentPrimary
                        } else {
                            theme.surfaceGlass
                        }
                    )
                    .clickable(enabled = inputText.isNotBlank() && !isGenerating) {
                        sendMessage(inputText)
                    }
                    .testTag("chat_send_btn"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = "Send",
                    tint = if (inputText.isNotBlank() && !isGenerating) Color.White else theme.textSecondary.copy(alpha = 0.5f),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}
