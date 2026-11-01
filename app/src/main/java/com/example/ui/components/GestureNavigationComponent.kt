package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.example.ui.theme.MayaTheme
import kotlin.math.roundToInt

/**
 * Modern OS Gesture Navigation Bar Pill
 * Positioned at the bottom of the screen.
 * - Swipe up: returns to Home Screen (swipe-up-to-home).
 * - Swipe left/right: cycles between adjacent tabs.
 * - Tap: quick return to Home.
 */
@Composable
fun GestureNavigationPill(
    onSwipeUpHome: () -> Unit,
    onSwipeAdjacentTab: (forward: Boolean) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val theme = MayaTheme.colors
    var accumulatedDragY by remember { mutableFloatStateOf(0f) }
    var accumulatedDragX by remember { mutableFloatStateOf(0f) }
    var isDragging by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(bottom = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .width(130.dp)
                .height(26.dp)
                .clip(RoundedCornerShape(13.dp))
                .background(Color.Transparent)
                .pointerInput(Unit) {
                    detectTapGestures(
                        onTap = { onSwipeUpHome() }
                    )
                }
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = {
                            accumulatedDragY = 0f
                            accumulatedDragX = 0f
                            isDragging = true
                        },
                        onDrag = { change, dragAmount ->
                            change.consume()
                            accumulatedDragY += dragAmount.y
                            accumulatedDragX += dragAmount.x
                        },
                        onDragEnd = {
                            if (accumulatedDragY < -24f) {
                                // Swiped up! Go home
                                onSwipeUpHome()
                            } else if (accumulatedDragX > 40f) {
                                // Swiped right: previous tab
                                onSwipeAdjacentTab(false)
                            } else if (accumulatedDragX < -40f) {
                                // Swiped left: next tab
                                onSwipeAdjacentTab(true)
                            }
                            accumulatedDragY = 0f
                            accumulatedDragX = 0f
                            isDragging = false
                        },
                        onDragCancel = {
                            accumulatedDragY = 0f
                            accumulatedDragX = 0f
                            isDragging = false
                        }
                    )
                }
                .testTag("gesture_navigation_pill_container"),
            contentAlignment = Alignment.Center
        ) {
            // Pill Visual
            Box(
                modifier = Modifier
                    .width(if (isDragging) 110.dp else 100.dp)
                    .height(4.5.dp)
                    .clip(CircleShape)
                    .background(
                        if (theme.isDark) Color(0x99FFFFFF) else Color(0x880F172A)
                    )
            )
        }
    }
}

/**
 * Edge Swipe Back Gesture Detector Overlay
 * Wraps any screen and detects left-edge inward swipes to trigger swipe-to-go-back.
 * Shows a glowing back cue bubble when dragged inwards from the left edge.
 */
@Composable
fun EdgeSwipeBackGestureWrapper(
    onSwipeBack: () -> Unit,
    modifier: Modifier = Modifier,
    edgeWidthDp: Int = 16,
    thresholdDp: Float = 42f,
    content: @Composable BoxScope.() -> Unit
) {
    val theme = MayaTheme.colors
    var isEdgeDragging by remember { mutableStateOf(false) }
    var dragProgressX by remember { mutableFloatStateOf(0f) }
    var dragStartY by remember { mutableFloatStateOf(400f) }

    Box(modifier = modifier.fillMaxSize()) {
        content()

        // Left Edge Horizontal-Only Detection Strip (never blocks vertical scrolling)
        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .fillMaxHeight()
                .width(edgeWidthDp.dp)
                .pointerInput(Unit) {
                    detectHorizontalDragGestures(
                        onDragStart = { offset ->
                            dragStartY = offset.y
                            dragProgressX = 0f
                            isEdgeDragging = true
                        },
                        onHorizontalDrag = { change, dragAmount ->
                            if (dragAmount > 0f || dragProgressX > 0f) {
                                change.consume()
                                dragProgressX += dragAmount
                            }
                        },
                        onDragEnd = {
                            if (dragProgressX > thresholdDp) {
                                onSwipeBack()
                            }
                            isEdgeDragging = false
                            dragProgressX = 0f
                        },
                        onDragCancel = {
                            isEdgeDragging = false
                            dragProgressX = 0f
                        }
                    )
                }
        )

        // Visual Back Cue Indicator when dragging from the left edge
        if (isEdgeDragging && dragProgressX > 10f) {
            val visualX = (dragProgressX * 0.65f).coerceIn(12f, 55f)
            Box(
                modifier = Modifier
                    .offset { IntOffset(visualX.roundToInt(), (dragStartY - 24).roundToInt()) }
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            listOf(theme.accentPrimary, theme.accentPrimary.copy(alpha = 0.85f))
                        )
                    )
                    .border(1.5.dp, Color.White.copy(alpha = 0.6f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Swipe Back",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}
