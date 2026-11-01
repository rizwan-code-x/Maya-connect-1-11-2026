package com.example.ui.components

import androidx.compose.animation.core.AnimationState
import androidx.compose.animation.core.DecayAnimationSpec
import androidx.compose.animation.core.animateDecay
import androidx.compose.animation.core.exponentialDecay
import androidx.compose.foundation.gestures.FlingBehavior
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.ScrollScope
import androidx.compose.foundation.gestures.ScrollableState
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.MotionDurationScale
import androidx.compose.ui.composed
import kotlinx.coroutines.withContext
import kotlin.math.abs
import kotlin.math.sign

/**
 * 120Hz-tuned high-friction deceleration multiplier modeled after modern Chrome mobile compositor
 * scrolling physics. Provides controlled momentum, crisp deceleration, and sub-pixel tail stability.
 */
const val CHROME_SCROLL_FRICTION_MULTIPLIER = 1.65f

/**
 * Upper velocity clamp (px/sec) to prevent sudden multi-page visual tearing on 120Hz panels
 * while preserving natural fast-flick responsiveness.
 */
const val CHROME_MAX_FLING_VELOCITY = 6800f

/**
 * Velocity threshold below which the fling settles cleanly without late-frame micro-jitter.
 */
const val CHROME_MIN_VELOCITY_THRESHOLD = 0.5f

private val Chrome120HzMotionDurationScale = object : MotionDurationScale {
    override val scaleFactor: Float get() = 1f
}

/**
 * Custom [FlingBehavior] engineered for 120Hz-tuned, high-friction, fluid scrolling across all
 * LazyColumn, LazyRow, and LazyVerticalGrid containers.
 */
class ChromeSmoothFlingBehavior(
    private val flingDecay: DecayAnimationSpec<Float>,
    private val maxFlingVelocity: Float = CHROME_MAX_FLING_VELOCITY,
    private val motionDurationScale: MotionDurationScale = Chrome120HzMotionDurationScale
) : FlingBehavior {

    val frictionConfigured: Boolean = true

    override suspend fun ScrollScope.performFling(initialVelocity: Float): Float {
        if (abs(initialVelocity) <= 1f) {
            return initialVelocity
        }

        // Clamp extreme peak velocity for smooth 120Hz frame pacing and apply subtle velocity smoothing
        val clampedVelocity = initialVelocity.coerceIn(-maxFlingVelocity, maxFlingVelocity)
        val smoothedInitialVelocity = if (abs(clampedVelocity) > 2400f) {
            clampedVelocity * 0.92f
        } else {
            clampedVelocity * 0.96f
        }

        return withContext(motionDurationScale) {
            var velocityLeft = smoothedInitialVelocity
            var lastValue = 0f
            var subPixelCarry = 0f

            val animationState = AnimationState(
                initialValue = 0f,
                initialVelocity = smoothedInitialVelocity
            )

            animationState.animateDecay(flingDecay) {
                val rawDelta = value - lastValue + subPixelCarry
                lastValue = value

                val consumed = scrollBy(rawDelta)
                subPixelCarry = (rawDelta - consumed).coerceIn(-0.45f, 0.45f)
                velocityLeft = this.velocity

                // Stop immediately if scroll container hit top/bottom edge or settled at low velocity
                if (abs(rawDelta - consumed) > 0.5f || abs(velocityLeft) < CHROME_MIN_VELOCITY_THRESHOLD) {
                    this.cancelAnimation()
                }
            }

            if (abs(velocityLeft) < CHROME_MIN_VELOCITY_THRESHOLD) 0f else velocityLeft * sign(initialVelocity).let { if (it == 0f) 1f else 1f }
        }
    }
}

/**
 * Remembers a 120Hz-tuned, high-friction [FlingBehavior] matching Chrome's fluid mobile scroll deceleration.
 */
@Composable
fun rememberChromeSmoothFlingBehavior(
    frictionMultiplier: Float = CHROME_SCROLL_FRICTION_MULTIPLIER,
    maxFlingVelocity: Float = CHROME_MAX_FLING_VELOCITY
): FlingBehavior {
    val decaySpec = remember(frictionMultiplier) {
        exponentialDecay<Float>(
            frictionMultiplier = frictionMultiplier,
            absVelocityThreshold = CHROME_MIN_VELOCITY_THRESHOLD
        )
    }
    return remember(decaySpec, maxFlingVelocity) {
        ChromeSmoothFlingBehavior(
            flingDecay = decaySpec,
            maxFlingVelocity = maxFlingVelocity
        )
    }
}

/**
 * Convenience [Modifier.scrollable] wrapper pre-configured with [rememberChromeSmoothFlingBehavior].
 */
fun Modifier.chromeSmoothScrollable(
    state: ScrollableState,
    orientation: Orientation,
    enabled: Boolean = true,
    reverseDirection: Boolean = false
): Modifier = composed {
    val flingBehavior = rememberChromeSmoothFlingBehavior()
    this.scrollable(
        state = state,
        orientation = orientation,
        enabled = enabled,
        reverseDirection = reverseDirection,
        flingBehavior = flingBehavior
    )
}
