package com.example.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

val LocalMayaColors = compositionLocalOf { getMayaColorScheme(MayaThemeMode.DARK) }

object MayaTheme {
    val colors: MayaColorScheme
        @Composable
        @ReadOnlyComposable
        get() = LocalMayaColors.current
}

@Composable
fun MayaConnectTheme(
    themeMode: MayaThemeMode = MayaThemeMode.DARK,
    content: @Composable () -> Unit
) {
    val isSystemDark = androidx.compose.foundation.isSystemInDarkTheme()
    val mayaColors = getMayaColorScheme(themeMode, isSystemDark)
    val view = LocalView.current

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window ?: return@SideEffect
            window.statusBarColor = Color.Transparent.toArgb()
            window.navigationBarColor = Color.Transparent.toArgb()
            val insetsController = WindowCompat.getInsetsController(window, view)
            insetsController.isAppearanceLightStatusBars = !mayaColors.isDark
            insetsController.isAppearanceLightNavigationBars = !mayaColors.isDark
        }
    }

    val materialColorScheme = if (mayaColors.isDark) {
        darkColorScheme(
            primary = mayaColors.accentPrimary,
            secondary = mayaColors.accentSecondary,
            background = mayaColors.background,
            surface = mayaColors.surface,
            onPrimary = Color.Black,
            onSecondary = Color.White,
            onBackground = mayaColors.textPrimary,
            onSurface = mayaColors.textPrimary
        )
    } else {
        lightColorScheme(
            primary = mayaColors.accentPrimary,
            secondary = mayaColors.accentSecondary,
            background = mayaColors.background,
            surface = mayaColors.surface,
            onPrimary = Color.White,
            onSecondary = Color.White,
            onBackground = mayaColors.textPrimary,
            onSurface = mayaColors.textPrimary
        )
    }

    CompositionLocalProvider(LocalMayaColors provides mayaColors) {
        MaterialTheme(
            colorScheme = materialColorScheme,
            typography = Typography,
            content = content
        )
    }
}
