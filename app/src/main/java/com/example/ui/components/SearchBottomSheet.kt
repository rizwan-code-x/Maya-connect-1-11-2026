package com.example.ui.components

import android.content.Intent
import android.net.Uri
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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.ai.GeminiAssistantService
import com.example.data.model.AppItem
import com.example.data.model.SearchHelper
import com.example.ui.theme.MayaTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SearchBottomSheet(
    initialQuery: String = "",
    allApps: List<AppItem>,
    onDismiss: () -> Unit,
    onAppClick: (AppItem) -> Unit,
    onOpenVoiceAssistant: () -> Unit,
    onOpenChatAssistant: (query: String) -> Unit
) {
    val theme = MayaTheme.colors
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var query by remember { mutableStateOf(initialQuery) }
    var geminiAnswer by remember { mutableStateOf<String?>(null) }
    var isGeminiLoading by remember { mutableStateOf(false) }

    val matchingApps = remember(allApps, query) {
        if (query.isBlank()) emptyList()
        else allApps.filter { SearchHelper.matchesQuery(it, query) }
    }

    val quickPills = listOf(
        "Weather Today",
        "Sunrise & Sunset",
        "Current Date & Time",
        "Iftar Time",
        "Latest News",
        "REXLIFY",
        "Gemini AI"
    )

    // Trigger Gemini real-time lookup when user types information query
    LaunchedEffect(query) {
        val trimmed = query.trim()
        if (trimmed.length >= 3) {
            isGeminiLoading = true
            geminiAnswer = null
            delay(600) // Debounce input
            GeminiAssistantService.generateAnswer(trimmed)
                .onSuccess { answer ->
                    geminiAnswer = answer
                    isGeminiLoading = false
                }
                .onFailure {
                    isGeminiLoading = false
                }
        } else {
            geminiAnswer = null
            isGeminiLoading = false
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.88f)
                .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                .background(if (theme.isDark) Color(0xFF0F131D) else Color(0xFFF1F5F9))
                .border(1.dp, theme.borderGlass, RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                .padding(20.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Drag Handle Pill
                Box(
                    modifier = Modifier
                        .width(42.dp)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(theme.textSecondary.copy(alpha = 0.4f))
                        .align(Alignment.CenterHorizontally)
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Top Search Header with Input and Voice Button
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .glassmorphic(
                            shape = RoundedCornerShape(20.dp),
                            backgroundColor = theme.surfaceGlass,
                            borderColor = theme.borderGlass,
                            borderWidth = 1.dp
                        )
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = theme.accentPrimary,
                        modifier = Modifier.size(20.dp)
                    )

                    Spacer(modifier = Modifier.width(10.dp))

                    BasicTextField(
                        value = query,
                        onValueChange = { query = it },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        textStyle = TextStyle(
                            color = theme.textPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium
                        ),
                        cursorBrush = SolidColor(theme.accentPrimary),
                        decorationBox = { innerTextField ->
                            if (query.isEmpty()) {
                                Text(
                                    text = "Search apps or ask Gemini AI...",
                                    style = TextStyle(
                                        color = theme.textSecondary.copy(alpha = 0.65f),
                                        fontSize = 14.sp
                                    )
                                )
                            }
                            innerTextField()
                        }
                    )

                    if (query.isNotEmpty()) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Clear",
                            tint = theme.textSecondary,
                            modifier = Modifier
                                .size(18.dp)
                                .clickable { query = "" }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }

                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(theme.accentPrimary.copy(alpha = 0.15f))
                            .clickable {
                                onDismiss()
                                onOpenVoiceAssistant()
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Mic,
                            contentDescription = "Voice Search",
                            tint = theme.accentPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Quick Topic Pills
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    quickPills.forEach { pill ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(theme.surfaceGlass)
                                .border(0.8.dp, theme.borderGlass, RoundedCornerShape(12.dp))
                                .clickable { query = pill }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = pill,
                                style = TextStyle(
                                    color = theme.textPrimary,
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Main Content List: Gemini Real-Time Info + Matching Apps
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    flingBehavior = rememberChromeSmoothFlingBehavior(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // 1. Gemini AI Real-Time Information Card
                    if (query.isNotBlank()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(
                                        Brush.linearGradient(
                                            listOf(Color(0xFF1E293B), Color(0xFF0F172A))
                                        )
                                    )
                                    .border(1.dp, Color(0xFF38BDF8).copy(alpha = 0.4f), RoundedCornerShape(20.dp))
                                    .padding(16.dp)
                            ) {
                                Column {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.AutoAwesome,
                                                contentDescription = null,
                                                tint = Color(0xFF38BDF8),
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = "Gemini Real-Time Intelligence",
                                                style = TextStyle(
                                                    color = Color(0xFF38BDF8),
                                                    fontSize = 13.5.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            )
                                        }

                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(Color(0x3338BDF8))
                                                .clickable {
                                                    val queryLower = query.lowercase()
                                                    val isRealTimeWebQuery = queryLower.contains("weather") ||
                                                            queryLower.contains("sunrise") ||
                                                            queryLower.contains("sunset") ||
                                                            queryLower.contains("date") ||
                                                            queryLower.contains("iftar") ||
                                                            queryLower.contains("time") ||
                                                            queryLower.contains("news")

                                                    if (isRealTimeWebQuery) {
                                                        try {
                                                            val webIntent = Intent(
                                                                Intent.ACTION_VIEW,
                                                                Uri.parse("https://www.google.com/search?q=" + Uri.encode(query))
                                                            )
                                                            context.startActivity(webIntent)
                                                        } catch (_: Exception) {
                                                            onOpenChatAssistant(query)
                                                        }
                                                    } else {
                                                        onDismiss()
                                                        onOpenChatAssistant(query)
                                                    }
                                                }
                                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                        ) {
                                            Text(
                                                text = "Google Search →",
                                                style = TextStyle(
                                                    color = Color(0xFF38BDF8),
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))

                                    if (isGeminiLoading) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            CircularProgressIndicator(
                                                modifier = Modifier.size(14.dp),
                                                color = Color(0xFF38BDF8),
                                                strokeWidth = 2.dp
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = "Retrieving real-time info for \"$query\"...",
                                                style = TextStyle(
                                                    color = Color(0xFF94A3B8),
                                                    fontSize = 12.sp
                                                )
                                            )
                                        }
                                    } else if (geminiAnswer != null) {
                                        Text(
                                            text = geminiAnswer ?: "",
                                            style = TextStyle(
                                                color = Color.White,
                                                fontSize = 13.5.sp,
                                                lineHeight = 20.sp
                                            )
                                        )
                                    } else {
                                        Text(
                                            text = "Ask Gemini anything about weather, dates, sunrise/sunset, or ecosystem knowledge.",
                                            style = TextStyle(
                                                color = Color(0xFF94A3B8),
                                                fontSize = 12.5.sp
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // 2. Matching Installed Applications
                    if (matchingApps.isNotEmpty()) {
                        item {
                            Text(
                                text = "MATCHING APPLICATIONS (${matchingApps.size})",
                                style = TextStyle(
                                    color = theme.textSecondary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.5.sp
                                )
                            )
                        }

                        items(matchingApps, key = { it.id }) { app ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(theme.surfaceGlass)
                                    .border(1.dp, theme.borderGlass, RoundedCornerShape(16.dp))
                                    .clickable {
                                        onDismiss()
                                        onAppClick(app)
                                    }
                                    .padding(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        AppLogoIcon(app = app, size = 36.dp, cornerRadius = 10.dp)
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column {
                                            Text(
                                                text = app.name,
                                                style = TextStyle(
                                                    color = theme.textPrimary,
                                                    fontSize = 14.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            )
                                            Text(
                                                text = app.tagline,
                                                style = TextStyle(
                                                    color = theme.textSecondary,
                                                    fontSize = 11.5.sp
                                                ),
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }

                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                        contentDescription = "Open App",
                                        tint = theme.accentPrimary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    } else if (query.isNotBlank() && !isGeminiLoading) {
                        item {
                            Text(
                                text = "No matching installed apps for \"$query\". Showing Gemini AI intelligence above.",
                                style = TextStyle(
                                    color = theme.textSecondary,
                                    fontSize = 11.5.sp
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}
