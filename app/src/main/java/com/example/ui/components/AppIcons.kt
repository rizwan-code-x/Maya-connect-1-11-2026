package com.example.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.NoteAlt
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.AppItem

@Composable
fun AppLogoIcon(
    app: AppItem,
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
    cornerRadius: Dp = 14.dp
) {
    val key = app.iconKey.uppercase()

    val logoResId = when (key) {
        "MAYA_CONNECT", "MAYA_CONNECT_OFFICIAL", "MAYA" -> R.drawable.maya_connect_official_logo
        "MAYA_OS", "MAYAOS" -> R.drawable.maya_os_official_icon
        "SIFRA" -> R.drawable.logo_sifra
        "NOORAX", "NOORAX_AI" -> R.drawable.logo_noorax
        "RODP" -> R.drawable.logo_rodp
        "REXLIFY" -> R.drawable.logo_rexlify
        "NEURAX" -> R.drawable.logo_neurax
        "PH_VEG", "PHVEG", "PH_VEG_ECOMMERCE" -> R.drawable.logo_ph_veg
        "GEMINI", "GOOGLE_GEMINI" -> R.drawable.logo_gemini
        "CHATGPT", "CHAT_GPT", "OPENAI" -> R.drawable.logo_chatgpt
        "MS_EDGE", "EDGE", "MICROSOFT_EDGE" -> R.drawable.logo_edge
        "MAPS", "GOOGLE_MAPS" -> R.drawable.logo_maps
        "MATH", "MATH_SOLVER" -> R.drawable.logo_math
        else -> null
    }

    if (logoResId != null) {
        val iconBgColor = when (key) {
            "MAYA_CONNECT", "MAYA_CONNECT_OFFICIAL", "MAYA", "MAYA_OS", "MAYAOS" -> Color(0xFF0F172A)
            "GEMINI", "GOOGLE_GEMINI" -> Color(0xFF0F172A)
            "CHATGPT", "CHAT_GPT", "OPENAI" -> Color(0xFF10A37F)
            "MS_EDGE", "EDGE", "MICROSOFT_EDGE" -> Color(0xFF0F172A)
            "MAPS", "GOOGLE_MAPS" -> Color(0xFFFFFFFF)
            "MATH", "MATH_SOLVER" -> Color(0xFF1E3A8A)
            else -> Color(0xFF0F172A)
        }
        val innerPadding = when (key) {
            "MAYA_CONNECT", "MAYA_CONNECT_OFFICIAL", "MAYA", "MAYA_OS", "MAYAOS" -> 0.dp
            "GEMINI", "GOOGLE_GEMINI" -> 7.dp
            "MS_EDGE", "EDGE", "MICROSOFT_EDGE" -> 6.dp
            "MAPS", "GOOGLE_MAPS" -> 7.dp
            "MATH", "MATH_SOLVER" -> 6.dp
            "CHATGPT", "CHAT_GPT", "OPENAI" -> 5.dp
            else -> 0.dp
        }
        Box(
            modifier = modifier
                .size(size)
                .clip(RoundedCornerShape(cornerRadius))
                .background(iconBgColor)
                .border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(cornerRadius)),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = logoResId),
                contentDescription = app.name,
                contentScale = if (innerPadding == 0.dp) ContentScale.Crop else ContentScale.Fit,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            )
        }
        return
    }

    // Determine brand gradient based on app key
    val gradient = when (key) {
        "INSTAGRAM" -> Brush.linearGradient(
            colors = listOf(Color(0xFF833AB4), Color(0xFFFD1D1D), Color(0xFFFCB045))
        )
        "FACEBOOK" -> Brush.linearGradient(
            colors = listOf(Color(0xFF1877F2), Color(0xFF0C5EC7))
        )
        "GOOGLE_TASKS", "TASKS" -> Brush.linearGradient(
            colors = listOf(Color(0xFF4285F4), Color(0xFF1A73E8))
        )
        "GEMINI" -> Brush.linearGradient(
            colors = listOf(Color(0xFF1B72E8), Color(0xFF8E24AA), Color(0xFFD93025))
        )
        "CHATGPT" -> Brush.linearGradient(
            colors = listOf(Color(0xFF10A37F), Color(0xFF0D8C6D))
        )
        "CLAUDE" -> Brush.linearGradient(
            colors = listOf(Color(0xFFD97706), Color(0xFFCC5500), Color(0xFF993300))
        )
        "YOUTUBE" -> Brush.linearGradient(
            colors = listOf(Color(0xFFFF0000), Color(0xFFCC0000))
        )
        "GMAIL" -> Brush.linearGradient(
            colors = listOf(Color(0xFFEA4335), Color(0xFFC5221F))
        )
        "GOOGLE_SEARCH" -> Brush.linearGradient(
            colors = listOf(Color(0xFF4285F4), Color(0xFF34A853), Color(0xFFFBBC05), Color(0xFFEA4335))
        )
        "MAYA_OS" -> Brush.linearGradient(
            colors = listOf(Color(0xFF6366F1), Color(0xFF4F46E5), Color(0xFF312E81))
        )
        "MAYA_CONNECT" -> Brush.linearGradient(
            colors = listOf(Color(0xFF0284C7), Color(0xFF0369A1))
        )
        "REXLIFY" -> Brush.linearGradient(
            colors = listOf(Color(0xFF0F172A), Color(0xFF334155))
        )
        "NEURAX" -> Brush.linearGradient(
            colors = listOf(Color(0xFF7C3AED), Color(0xFF4C1D95))
        )
        "NOORAX" -> Brush.linearGradient(
            colors = listOf(Color(0xFFEC4899), Color(0xFFBE185D))
        )
        "SIFRA" -> Brush.linearGradient(
            colors = listOf(Color(0xFF059669), Color(0xFF047857))
        )
        "RODP" -> Brush.linearGradient(
            colors = listOf(Color(0xFFEA580C), Color(0xFFC2410C))
        )
        "MS_EDGE", "EDGE" -> Brush.linearGradient(
            colors = listOf(Color(0xFF0078D4), Color(0xFF005A9E), Color(0xFF00205B))
        )
        "REXLIFY_MUSIC", "MUSIC" -> Brush.linearGradient(
            colors = listOf(Color(0xFF7E22CE), Color(0xFF581C87), Color(0xFF3B0764))
        )
        "RIZWAN", "RIZWAN_RR" -> Brush.linearGradient(
            colors = listOf(Color(0xFF0F172A), Color(0xFF1E293B), Color(0xFF334155))
        )
        "NOTEPAD" -> Brush.linearGradient(
            colors = listOf(Color(0xFF6366F1), Color(0xFF4F46E5), Color(0xFF3730A3))
        )
        else -> Brush.linearGradient(
            colors = listOf(Color(app.primaryColorHex), Color(app.secondaryColorHex))
        )
    }

    Box(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(cornerRadius))
            .background(gradient)
            .border(1.dp, Color(0x4DFFFFFF), RoundedCornerShape(cornerRadius))
            .drawBehind {
                drawLine(
                    color = Color(0x66FFFFFF),
                    start = Offset(0f, 1f),
                    end = Offset(this.size.width, 1f),
                    strokeWidth = 2f
                )
            },
        contentAlignment = Alignment.Center
    ) {
        when (key) {
            "MAYA_OS" -> {
                Icon(
                    imageVector = Icons.Default.Layers,
                    contentDescription = "Maya OS",
                    tint = Color.White,
                    modifier = Modifier.size(size * 0.58f)
                )
            }
            "MAYA_CONNECT" -> {
                Icon(
                    imageVector = Icons.Default.Hub,
                    contentDescription = "Maya Connect",
                    tint = Color.White,
                    modifier = Modifier.size(size * 0.58f)
                )
            }
            "REXLIFY" -> {
                Icon(
                    imageVector = Icons.Default.Hub,
                    contentDescription = "REXLIFY",
                    tint = Color.White,
                    modifier = Modifier.size(size * 0.55f)
                )
            }
            "NEURAX" -> {
                Icon(
                    imageVector = Icons.Default.Psychology,
                    contentDescription = "NeuraX",
                    tint = Color.White,
                    modifier = Modifier.size(size * 0.58f)
                )
            }
            "NOORAX" -> {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = "NooraX",
                    tint = Color.White,
                    modifier = Modifier.size(size * 0.55f)
                )
            }
            "SIFRA" -> {
                Icon(
                    imageVector = Icons.Default.Code,
                    contentDescription = "SIFRA",
                    tint = Color.White,
                    modifier = Modifier.size(size * 0.55f)
                )
            }
            "RODP" -> {
                Icon(
                    imageVector = Icons.Default.RocketLaunch,
                    contentDescription = "RODP",
                    tint = Color.White,
                    modifier = Modifier.size(size * 0.55f)
                )
            }
            "INSTAGRAM" -> {
                Icon(
                    imageVector = Icons.Default.CameraAlt,
                    contentDescription = "Instagram",
                    tint = Color.White,
                    modifier = Modifier.size(size * 0.58f)
                )
            }
            "FACEBOOK" -> {
                Text(
                    text = "f",
                    style = TextStyle(
                        color = Color.White,
                        fontSize = (size.value * 0.65f).sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.SansSerif
                    )
                )
            }
            "GOOGLE_TASKS", "TASKS" -> {
                Icon(
                    imageVector = Icons.Default.TaskAlt,
                    contentDescription = "Google Tasks",
                    tint = Color.White,
                    modifier = Modifier.size(size * 0.60f)
                )
            }
            "GEMINI" -> {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = "Google Gemini",
                    tint = Color.White,
                    modifier = Modifier.size(size * 0.62f)
                )
            }
            "CHATGPT" -> {
                Icon(
                    imageVector = Icons.Default.SmartToy,
                    contentDescription = "ChatGPT",
                    tint = Color.White,
                    modifier = Modifier.size(size * 0.58f)
                )
            }
            "CLAUDE" -> {
                Text(
                    text = "C",
                    style = TextStyle(
                        color = Color.White,
                        fontSize = (size.value * 0.55f).sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Serif
                    )
                )
            }
            "GOOGLE_SEARCH" -> {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Google",
                    tint = Color.White,
                    modifier = Modifier.size(size * 0.55f)
                )
            }
            "YOUTUBE" -> {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "YouTube",
                    tint = Color.White,
                    modifier = Modifier.size(size * 0.62f)
                )
            }
            "GMAIL" -> {
                Icon(
                    imageVector = Icons.Default.Email,
                    contentDescription = "Gmail",
                    tint = Color.White,
                    modifier = Modifier.size(size * 0.58f)
                )
            }
            "MAPS" -> {
                Icon(
                    imageVector = Icons.Default.Map,
                    contentDescription = "Maps",
                    tint = Color.White,
                    modifier = Modifier.size(size * 0.55f)
                )
            }
            "SETTINGS" -> {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Settings",
                    tint = Color.White,
                    modifier = Modifier.size(size * 0.55f)
                )
            }
            "DIALER" -> {
                Icon(
                    imageVector = Icons.Default.Phone,
                    contentDescription = "Phone",
                    tint = Color.White,
                    modifier = Modifier.size(size * 0.55f)
                )
            }
            "MESSAGES" -> {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Chat,
                    contentDescription = "Messages",
                    tint = Color.White,
                    modifier = Modifier.size(size * 0.55f)
                )
            }
            "CHROME" -> {
                Icon(
                    imageVector = Icons.Default.Language,
                    contentDescription = "Chrome",
                    tint = Color.White,
                    modifier = Modifier.size(size * 0.55f)
                )
            }
            "CLOUD" -> {
                Icon(
                    imageVector = Icons.Default.Cloud,
                    contentDescription = "Cloud",
                    tint = Color.White,
                    modifier = Modifier.size(size * 0.55f)
                )
            }
            "JIOSPHERE" -> {
                Icon(
                    imageVector = Icons.Default.Public,
                    contentDescription = "JioSphere",
                    tint = Color(0xFF67E8F9),
                    modifier = Modifier.size(size * 0.58f)
                )
            }
            "REXLIFY_MUSIC", "MUSIC" -> {
                Icon(
                    imageVector = Icons.Default.MusicNote,
                    contentDescription = "REXLIFY Music",
                    tint = Color(0xFFF472B6),
                    modifier = Modifier.size(size * 0.60f)
                )
            }
            "RIZWAN", "RIZWAN_RR" -> {
                Text(
                    text = "RR",
                    style = TextStyle(
                        color = Color(0xFFFBBF24),
                        fontSize = (size.value * 0.45f).sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.SansSerif
                    )
                )
            }
            "NOTEPAD" -> {
                Icon(
                    imageVector = Icons.Default.NoteAlt,
                    contentDescription = "Notepad",
                    tint = Color.White,
                    modifier = Modifier.size(size * 0.58f)
                )
            }
            "MS_EDGE", "EDGE", "MICROSOFT_EDGE" -> {
                Image(
                    painter = painterResource(id = R.drawable.logo_edge),
                    contentDescription = "Microsoft Edge",
                    modifier = Modifier.size(size * 0.75f)
                )
            }
            "MATH", "MATH_SOLVER" -> {
                Image(
                    painter = painterResource(id = R.drawable.logo_math),
                    contentDescription = "Math",
                    modifier = Modifier.size(size * 0.75f)
                )
            }
            else -> {
                val initial = if (app.name.isNotEmpty()) app.name.take(1).uppercase() else "M"
                Text(
                    text = initial,
                    style = TextStyle(
                        color = Color.White,
                        fontSize = (size.value * 0.45f).sp,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }
    }
}
