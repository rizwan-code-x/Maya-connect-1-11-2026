package com.example.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// Functional action colors (consistent semantic meaning)
val MayaColorSuccess = Color(0xFF10B981)
val MayaColorWarning = Color(0xFFF59E0B)
val MayaColorError = Color(0xFFEF4444)
val MayaColorFavorite = Color(0xFFFBBF24)
val MayaColorInfo = Color(0xFF64748B)

enum class MayaThemeMode(val title: String, val description: String) {
    DARK("Dark Glass", "Futuristic deep space obsidian with silver glass reflections"),
    LIGHT("Light Glass", "Clean crystal silver interface with translucent depth"),
    SYSTEM("System Default", "Follows your Android device dynamic theme mode"),
    COSMIC("Cosmic Prism", "Iridescent liquid glass inspired by holographic optics"),
    PINK("Rose Quartz", "Sophisticated translucent magenta & soft rose glass"),
    SAFFRON("Amber Gold", "Warm modern saffron & honey amber gold glass"),
    SUNSET("Sunset Glass", "Dusk sunset dark gradient with amber & purple glass highlights")
}

data class MayaColorScheme(
    val mode: MayaThemeMode,
    val isDark: Boolean,
    val background: Color,
    val surface: Color,
    val surfaceGlass: Color,
    val borderGlass: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val accentPrimary: Color,
    val accentSecondary: Color,
    val softHighlight: Color,
    val cardBackground: Color,
    val buttonBackground: Color,
    val backgroundGradient: Brush
)

fun getMayaColorScheme(mode: MayaThemeMode, systemIsDark: Boolean = true): MayaColorScheme {
    val effectiveMode = if (mode == MayaThemeMode.SYSTEM) {
        if (systemIsDark) MayaThemeMode.DARK else MayaThemeMode.LIGHT
    } else {
        mode
    }

    return when (effectiveMode) {
        MayaThemeMode.DARK -> MayaColorScheme(
            mode = MayaThemeMode.DARK,
            isDark = true,
            background = Color(0xFF0B0E14),
            surface = Color(0xFF131822),
            surfaceGlass = Color(0x1FFFFFFF),
            borderGlass = Color(0x29FFFFFF),
            textPrimary = Color(0xFFF8FAFC),
            textSecondary = Color(0xFF94A3B8),
            accentPrimary = Color(0xFF38BDF8),
            accentSecondary = Color(0xFF818CF8),
            softHighlight = Color(0xFFFFFFFF),
            cardBackground = Color(0x14FFFFFF),
            buttonBackground = Color(0xFF1E293B),
            backgroundGradient = Brush.verticalGradient(
                colors = listOf(
                    Color(0xFF0F172A),
                    Color(0xFF0B0F19),
                    Color(0xFF06090F)
                )
            )
        )

        MayaThemeMode.LIGHT -> MayaColorScheme(
            mode = MayaThemeMode.LIGHT,
            isDark = false,
            background = Color(0xFFF8FAFC),
            surface = Color(0xFFFFFFFF),
            surfaceGlass = Color(0xCCFFFFFF),
            borderGlass = Color(0x26000000),
            textPrimary = Color(0xFF0F172A),
            textSecondary = Color(0xFF64748B),
            accentPrimary = Color(0xFF0284C7),
            accentSecondary = Color(0xFF4F46E5),
            softHighlight = Color(0xFF0F172A),
            cardBackground = Color(0xEEFFFFFF),
            buttonBackground = Color(0xFFE2E8F0),
            backgroundGradient = Brush.verticalGradient(
                colors = listOf(
                    Color(0xFFFFFFFF),
                    Color(0xFFF8FAFC),
                    Color(0xFFF1F5F9)
                )
            )
        )

        MayaThemeMode.COSMIC -> MayaColorScheme(
            mode = MayaThemeMode.COSMIC,
            isDark = true,
            background = Color(0xFF0D111E),
            surface = Color(0xFF151C2F),
            surfaceGlass = Color(0x26FFFFFF),
            borderGlass = Color(0x40818CF8),
            textPrimary = Color(0xFFFFFFFF),
            textSecondary = Color(0xFFA5B4FC),
            accentPrimary = Color(0xFF67E8F9),
            accentSecondary = Color(0xFFC084FC),
            softHighlight = Color(0xFFF472B6),
            cardBackground = Color(0x1C223654),
            buttonBackground = Color(0xFF312E81),
            backgroundGradient = Brush.verticalGradient(
                colors = listOf(
                    Color(0xFF16192E),
                    Color(0xFF0F1424),
                    Color(0xFF080B14)
                )
            )
        )

        MayaThemeMode.PINK -> MayaColorScheme(
            mode = MayaThemeMode.PINK,
            isDark = true,
            background = Color(0xFF160E16),
            surface = Color(0xFF221623),
            surfaceGlass = Color(0x24F472B6),
            borderGlass = Color(0x3DF472B6),
            textPrimary = Color(0xFFFFF1F6),
            textSecondary = Color(0xFFC7A6B6),
            accentPrimary = Color(0xFFF472B6),
            accentSecondary = Color(0xFFDB2777),
            softHighlight = Color(0xFFFDF2F8),
            cardBackground = Color(0x1A2F1A2D),
            buttonBackground = Color(0xFF831843),
            backgroundGradient = Brush.verticalGradient(
                colors = listOf(
                    Color(0xFF201322),
                    Color(0xFF160E16),
                    Color(0xFF0E080E)
                )
            )
        )

        MayaThemeMode.SAFFRON -> MayaColorScheme(
            mode = MayaThemeMode.SAFFRON,
            isDark = true,
            background = Color(0xFF15100B),
            surface = Color(0xFF221911),
            surfaceGlass = Color(0x24F59E0B),
            borderGlass = Color(0x3DF59E0B),
            textPrimary = Color(0xFFFFF9ED),
            textSecondary = Color(0xFFD4B797),
            accentPrimary = Color(0xFFF59E0B),
            accentSecondary = Color(0xFFD97706),
            softHighlight = Color(0xFFFEF3C7),
            cardBackground = Color(0x1A2E2014),
            buttonBackground = Color(0xFF78350F),
            backgroundGradient = Brush.verticalGradient(
                colors = listOf(
                    Color(0xFF20170F),
                    Color(0xFF15100B),
                    Color(0xFF0D0A07)
                )
            )
        )

        MayaThemeMode.SUNSET -> MayaColorScheme(
            mode = MayaThemeMode.SUNSET,
            isDark = true,
            background = Color(0xFF130E18),
            surface = Color(0xFF201627),
            surfaceGlass = Color(0x28F97316),
            borderGlass = Color(0x3DF97316),
            textPrimary = Color(0xFFFFF7ED),
            textSecondary = Color(0xFFCBD5E1),
            accentPrimary = Color(0xFFF97316),
            accentSecondary = Color(0xFFA855F7),
            softHighlight = Color(0xFFFFEDD5),
            cardBackground = Color(0x222A1732),
            buttonBackground = Color(0xFF7C2D12),
            backgroundGradient = Brush.verticalGradient(
                colors = listOf(
                    Color(0xFF26122D),
                    Color(0xFF1A0E22),
                    Color(0xFF0F0814)
                )
            )
        )

        MayaThemeMode.SYSTEM -> getMayaColorScheme(MayaThemeMode.DARK)
    }
}
