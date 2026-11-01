package com.example.ui.components

import android.view.HapticFeedbackConstants
import android.view.View
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.MayaTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

fun performHaptic(view: View) {
    try {
        view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
    } catch (_: Exception) {
    }
}

fun Modifier.glassmorphic(
    shape: Shape = RoundedCornerShape(18.dp),
    backgroundColor: Color? = null,
    borderColor: Color? = null,
    borderWidth: Dp = 1.dp,
    elevation: Dp = 0.dp
): Modifier = composed {
    val theme = MayaTheme.colors
    val bg = backgroundColor ?: theme.surfaceGlass
    val border = borderColor ?: theme.borderGlass

    this
        .then(
            if (elevation > 0.dp) {
                Modifier.shadow(elevation, shape, clip = false)
            } else {
                Modifier
            }
        )
        .clip(shape)
        .background(bg)
        .border(borderWidth, border, shape)
        .drawBehind {
            // Subtle top-edge clean reflection line
            val highlightColor = if (theme.isDark) Color(0x1AFFFFFF) else Color(0x33FFFFFF)
            drawLine(
                color = highlightColor,
                start = Offset(0f, 1f),
                end = Offset(size.width, 1f),
                strokeWidth = 1f
            )
        }
}

fun Modifier.springPressEffect(
    hapticEnabled: Boolean = true,
    scaleTarget: Float = 0.96f,
    onLongClick: (() -> Unit)? = null,
    onClick: () -> Unit
): Modifier = composed {
    val view = LocalView.current
    val coroutineScope = rememberCoroutineScope()
    val scale = remember { Animatable(1f) }
    val currentOnClick by rememberUpdatedState(onClick)
    val currentOnLongClick by rememberUpdatedState(onLongClick)

    this
        .graphicsLayer {
            val s = scale.value
            scaleX = s
            scaleY = s
        }
        .pointerInput(currentOnLongClick != null) {
            detectTapGestures(
                onPress = {
                    val pressJob = coroutineScope.launch {
                        // Brief delay so scroll gestures never trigger press scale animations
                        delay(45)
                        scale.animateTo(
                            scaleTarget,
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioNoBouncy,
                                stiffness = Spring.StiffnessHigh
                            )
                        )
                    }
                    val released = tryAwaitRelease()
                    pressJob.cancel()
                    if (scale.value != 1f) {
                        coroutineScope.launch {
                            scale.animateTo(
                                1f,
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioNoBouncy,
                                    stiffness = Spring.StiffnessMedium
                                )
                            )
                        }
                    }
                    if (released) {
                        if (hapticEnabled) performHaptic(view)
                        currentOnClick()
                    }
                },
                onLongPress = if (currentOnLongClick != null) {
                    {
                        if (hapticEnabled) performHaptic(view)
                        currentOnLongClick?.invoke()
                    }
                } else null
            )
        }
}
