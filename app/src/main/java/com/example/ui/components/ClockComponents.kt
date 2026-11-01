package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.QueryBuilder
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.MayaTheme
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlinx.coroutines.delay

/**
 * Supported Clock Display Styles on Maya OS Dashboard.
 */
enum class DashboardClockStyle(val displayName: String, val description: String) {
    ANALOGUE("Analogue Watch", "Mechanical watch face with sweeping hands & brand insignia"),
    NEON_CYBERPUNK("Neon Cyberpunk", "High-contrast glowing cyan & purple LCD aesthetic"),
    MINIMALIST("Minimalist Sans", "Ultra-clean modern typographic chronometer"),
    FUTURISTIC_DUAL("Futuristic Dual-Tone", "Split-color technical digital display"),
    RETRO_LED("Retro Segmented LED", "Vintage emerald/amber glowing segmented matrix"),
    BOLD_BAUHAUS("Bold Bauhaus", "Contemporary stacked typographic architecture")
}

/**
 * Interactive Dashboard Clock Card
 * Features an analogue watch or selected digital clock face with "Maya OS • Designed by Rizwan RR" branding.
 * Long pressing for 1 second opens the mini box with 5 digital clock styles to scroll and select!
 */
@Composable
fun DashboardClockWidget(
    currentStyle: DashboardClockStyle = DashboardClockStyle.ANALOGUE,
    onClockStyleSelected: (DashboardClockStyle) -> Unit,
    modifier: Modifier = Modifier
) {
    val theme = MayaTheme.colors
    var showClockPicker by remember { mutableStateOf(false) }

    // Live Clock State
    var calendar by remember { mutableStateOf(Calendar.getInstance()) }

    LaunchedEffect(Unit) {
        while (true) {
            calendar = Calendar.getInstance()
            delay(1000)
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .glassmorphic(
                shape = RoundedCornerShape(24.dp),
                backgroundColor = if (theme.isDark) Color(0xD4151A24) else Color(0xEBFFFFFF),
                borderColor = theme.borderGlass,
                borderWidth = 1.dp
            )
            .pointerInput(Unit) {
                detectTapGestures(
                    onLongPress = {
                        // 1 second long press triggers clock selection mini box!
                        showClockPicker = true
                    },
                    onTap = {
                        // Quick tap hint to long-press
                    }
                )
            }
            .padding(16.dp)
            .testTag("dashboard_clock_widget")
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top Bar with App Branding and Style Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF10B981))
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "MAYA OS",
                        style = TextStyle(
                            color = theme.accentPrimary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 1.2.sp
                        )
                    )
                }

                // 1-sec press hint button / badge
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(theme.accentPrimary.copy(alpha = 0.12f))
                        .clickable { showClockPicker = true }
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.TouchApp,
                        contentDescription = "Hold 1s for Clock Styles",
                        tint = theme.accentPrimary,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Hold 1s to Change",
                        style = TextStyle(
                            color = theme.accentPrimary,
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Main Clock Face Display
            when (currentStyle) {
                DashboardClockStyle.ANALOGUE -> {
                    AnalogueWatchFace(
                        calendar = calendar,
                        accentColor = theme.accentPrimary,
                        isDark = theme.isDark
                    )
                }
                DashboardClockStyle.NEON_CYBERPUNK -> {
                    NeonCyberpunkClock(calendar = calendar)
                }
                DashboardClockStyle.MINIMALIST -> {
                    MinimalistClock(calendar = calendar, isDark = theme.isDark)
                }
                DashboardClockStyle.FUTURISTIC_DUAL -> {
                    FuturisticDualClock(calendar = calendar, isDark = theme.isDark)
                }
                DashboardClockStyle.RETRO_LED -> {
                    RetroLedClock(calendar = calendar)
                }
                DashboardClockStyle.BOLD_BAUHAUS -> {
                    BoldBauhausClock(calendar = calendar, isDark = theme.isDark)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Prominent Owner & Brand Tagline
            Text(
                text = "Maya OS • Designed by Rizwan RR",
                style = TextStyle(
                    color = theme.textSecondary,
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 0.6.sp,
                    textAlign = TextAlign.Center
                )
            )
        }
    }

    // Mini Box Dialog for 5 Digital Clock Styles Carousel
    if (showClockPicker) {
        ClockPickerMiniBox(
            currentStyle = currentStyle,
            onStyleSelected = {
                onClockStyleSelected(it)
                showClockPicker = false
            },
            onDismiss = { showClockPicker = false }
        )
    }
}

/**
 * Beautiful Analogue Watch Face with live Hour, Minute, and Second sweep hands.
 */
@Composable
fun AnalogueWatchFace(
    calendar: Calendar,
    accentColor: Color,
    isDark: Boolean,
    sizeDp: Int = 140
) {
    val hours = calendar.get(Calendar.HOUR)
    val minutes = calendar.get(Calendar.MINUTE)
    val seconds = calendar.get(Calendar.SECOND)

    val dialBg = if (isDark) Color(0xFF0F131C) else Color(0xFFF1F5F9)
    val tickColor = if (isDark) Color(0xFF94A3B8) else Color(0xFF475569)
    val hourHandColor = if (isDark) Color(0xFFFFFFFF) else Color(0xFF0F172A)
    val minuteHandColor = if (isDark) Color(0xFFCBD5E1) else Color(0xFF334155)
    val secondHandColor = Color(0xFFEF4444) // Vibrant chronometer red

    val textMeasurer = androidx.compose.ui.text.rememberTextMeasurer()
    val hourTextLayouts = remember(textMeasurer, isDark, sizeDp) {
        val numberStyle = TextStyle(
            color = if (isDark) Color(0xFFE2E8F0) else Color(0xFF1E293B),
            fontSize = (sizeDp * 0.075f).sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.SansSerif
        )
        List(12) { idx ->
            val hourNum = if (idx == 0) 12 else idx
            textMeasurer.measure(text = hourNum.toString(), style = numberStyle)
        }
    }

    Box(
        modifier = Modifier.size(sizeDp.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(sizeDp.dp)) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val radius = size.minDimension / 2f - 4.dp.toPx()

            // Outer dial rim gradient
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(dialBg, dialBg.copy(alpha = 0.95f), accentColor.copy(alpha = 0.25f)),
                    center = center,
                    radius = radius
                ),
                radius = radius,
                center = center
            )

            // Bezel circle border
            drawCircle(
                color = accentColor.copy(alpha = 0.4f),
                radius = radius,
                center = center,
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.dp.toPx())
            )

            // Inner subtle concentric ring
            drawCircle(
                color = tickColor.copy(alpha = 0.15f),
                radius = radius * 0.65f,
                center = center,
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.dp.toPx())
            )

            // 12-hour tick marks and numbered hour markers 1 through 12
            for (i in 0 until 12) {
                val angleRad = (i * 30.0 - 90.0) * (PI / 180.0)
                val isMajor = (i % 3 == 0)
                val tickLength = if (isMajor) radius * 0.16f else radius * 0.08f
                val strokeWidth = if (isMajor) 2.5.dp.toPx() else 1.2.dp.toPx()
                val tickAlpha = if (isMajor) 0.9f else 0.45f

                val startX = center.x + (radius - tickLength) * cos(angleRad).toFloat()
                val startY = center.y + (radius - tickLength) * sin(angleRad).toFloat()
                val endX = center.x + radius * cos(angleRad).toFloat()
                val endY = center.y + radius * sin(angleRad).toFloat()

                drawLine(
                    color = if (isMajor) accentColor.copy(alpha = tickAlpha) else tickColor.copy(alpha = tickAlpha),
                    start = Offset(startX, startY),
                    end = Offset(endX, endY),
                    strokeWidth = strokeWidth,
                    cap = StrokeCap.Round
                )

                // Hour numbers 1 through 12 (cached layout)
                val textRadius = radius * 0.72f
                val textX = center.x + textRadius * cos(angleRad).toFloat()
                val textY = center.y + textRadius * sin(angleRad).toFloat()
                val textLayoutResult = hourTextLayouts[i]

                drawText(
                    textLayoutResult = textLayoutResult,
                    topLeft = Offset(
                        x = textX - textLayoutResult.size.width / 2f,
                        y = textY - textLayoutResult.size.height / 2f
                    )
                )
            }

            // Calculations for hands
            val hourAngle = ((hours + minutes / 60.0) * 30.0 - 90.0) * (PI / 180.0)
            val minuteAngle = ((minutes + seconds / 60.0) * 6.0 - 90.0) * (PI / 180.0)
            val secondAngle = (seconds * 6.0 - 90.0) * (PI / 180.0)

            // Hour Hand
            val hourLength = radius * 0.45f
            val hourEnd = Offset(
                center.x + hourLength * cos(hourAngle).toFloat(),
                center.y + hourLength * sin(hourAngle).toFloat()
            )
            drawLine(
                color = hourHandColor,
                start = center,
                end = hourEnd,
                strokeWidth = 3.5.dp.toPx(),
                cap = StrokeCap.Round
            )

            // Minute Hand
            val minuteLength = radius * 0.65f
            val minuteEnd = Offset(
                center.x + minuteLength * cos(minuteAngle).toFloat(),
                center.y + minuteLength * sin(minuteAngle).toFloat()
            )
            drawLine(
                color = minuteHandColor,
                start = center,
                end = minuteEnd,
                strokeWidth = 2.5.dp.toPx(),
                cap = StrokeCap.Round
            )

            // Second Hand
            val secondLength = radius * 0.82f
            val secondTail = radius * 0.16f
            val secondEnd = Offset(
                center.x + secondLength * cos(secondAngle).toFloat(),
                center.y + secondLength * sin(secondAngle).toFloat()
            )
            val secondTailEnd = Offset(
                center.x - secondTail * cos(secondAngle).toFloat(),
                center.y - secondTail * sin(secondAngle).toFloat()
            )
            drawLine(
                color = secondHandColor,
                start = secondTailEnd,
                end = secondEnd,
                strokeWidth = 1.5.dp.toPx(),
                cap = StrokeCap.Round
            )

            // Center Pivot Cap
            drawCircle(
                color = secondHandColor,
                radius = 3.5.dp.toPx(),
                center = center
            )
            drawCircle(
                color = Color.White,
                radius = 1.5.dp.toPx(),
                center = center
            )
        }

        // Insignia Text inside Watch
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(top = 28.dp)
        ) {
            Text(
                text = "REXLIFY",
                style = TextStyle(
                    color = accentColor.copy(alpha = 0.8f),
                    fontSize = 8.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.5.sp
                )
            )
        }
    }
}

// -----------------------------------------------------------------------------------------
// 5 DISTINCT DIGITAL CLOCK STYLES
// -----------------------------------------------------------------------------------------

/**
 * 1. Neon Cyberpunk Digital Clock
 */
@Composable
fun NeonCyberpunkClock(calendar: Calendar) {
    val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault()).format(calendar.time)
    val sec = String.format("%02d", calendar.get(Calendar.SECOND))
    val dateStr = SimpleDateFormat("EEE, dd MMM", Locale.getDefault()).format(calendar.time).uppercase()

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF090D16))
            .border(1.dp, Color(0xFF06B6D4).copy(alpha = 0.4f), RoundedCornerShape(16.dp))
            .padding(vertical = 12.dp, horizontal = 16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = timeFormat,
                style = TextStyle(
                    color = Color(0xFF06B6D4),
                    fontSize = 44.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 2.sp,
                    fontFamily = FontFamily.Monospace
                )
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = sec,
                style = TextStyle(
                    color = Color(0xFFA855F7),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                ),
                modifier = Modifier.padding(bottom = 6.dp)
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color(0x33A855F7))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "CYBERPUNK v3",
                    style = TextStyle(color = Color(0xFFA855F7), fontSize = 8.5.sp, fontWeight = FontWeight.Bold)
                )
            }
            Text(
                text = dateStr,
                style = TextStyle(color = Color(0xFF94A3B8), fontSize = 10.sp, fontWeight = FontWeight.Medium)
            )
        }
    }
}

/**
 * 2. Minimalist Sans Digital Clock
 */
@Composable
fun MinimalistClock(calendar: Calendar, isDark: Boolean) {
    val timeFormat = SimpleDateFormat("hh:mm", Locale.getDefault()).format(calendar.time)
    val amPm = SimpleDateFormat("a", Locale.getDefault()).format(calendar.time).uppercase()
    val fullDate = SimpleDateFormat("EEEE, MMMM d", Locale.getDefault()).format(calendar.time)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = timeFormat,
                style = TextStyle(
                    color = if (isDark) Color.White else Color(0xFF0F172A),
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Light,
                    letterSpacing = (-1).sp
                )
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = amPm,
                style = TextStyle(
                    color = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            )
        }

        Box(
            modifier = Modifier
                .width(40.dp)
                .height(2.dp)
                .background(if (isDark) Color(0x33FFFFFF) else Color(0x22000000))
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = fullDate,
            style = TextStyle(
                color = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B),
                fontSize = 11.5.sp,
                fontWeight = FontWeight.Normal
            )
        )
    }
}

/**
 * 3. Futuristic Dual-Tone Digital Clock
 */
@Composable
fun FuturisticDualClock(calendar: Calendar, isDark: Boolean) {
    val hours = SimpleDateFormat("HH", Locale.getDefault()).format(calendar.time)
    val minutes = SimpleDateFormat("mm", Locale.getDefault()).format(calendar.time)
    val seconds = SimpleDateFormat("ss", Locale.getDefault()).format(calendar.time)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(if (isDark) Color(0xFF0B132B) else Color(0xFFE2E8F0))
            .border(1.dp, Color(0xFF0284C7).copy(alpha = 0.35f), RoundedCornerShape(18.dp))
            .padding(12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            // Hours Pill
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF0284C7))
                    .padding(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Text(
                    text = hours,
                    style = TextStyle(
                        color = Color.White,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Black
                    )
                )
            }

            Text(
                text = ":",
                style = TextStyle(
                    color = Color(0xFF0284C7),
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold
                ),
                modifier = Modifier.padding(horizontal = 6.dp)
            )

            // Minutes Pill
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (isDark) Color(0xFF1C2541) else Color.White)
                    .padding(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Text(
                    text = minutes,
                    style = TextStyle(
                        color = if (isDark) Color.White else Color(0xFF0F172A),
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Black
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "TELEMETRY SYNC • $seconds SEC",
            style = TextStyle(
                color = Color(0xFF0284C7),
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
        )
    }
}

/**
 * 4. Retro Segmented LED Clock
 */
@Composable
fun RetroLedClock(calendar: Calendar) {
    val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(calendar.time)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFF051B11))
            .border(1.5.dp, Color(0xFF10B981).copy(alpha = 0.5f), RoundedCornerShape(14.dp))
            .padding(vertical = 14.dp, horizontal = 16.dp)
    ) {
        Text(
            text = timeFormat,
            style = TextStyle(
                color = Color(0xFF10B981),
                fontSize = 38.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 3.sp
            )
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "DIGITAL MATRIX LED CHRONOMETER",
            style = TextStyle(
                color = Color(0xFF059669),
                fontSize = 8.5.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.2.sp
            )
        )
    }
}

/**
 * 5. Bold Bauhaus Graphic Clock
 */
@Composable
fun BoldBauhausClock(calendar: Calendar, isDark: Boolean) {
    val hours = SimpleDateFormat("HH", Locale.getDefault()).format(calendar.time)
    val minutes = SimpleDateFormat("mm", Locale.getDefault()).format(calendar.time)
    val dateMonth = SimpleDateFormat("dd MMM", Locale.getDefault()).format(calendar.time).uppercase()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left Large Digits
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = hours,
                style = TextStyle(
                    color = Color(0xFFE11D48), // Bauhaus Red
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Black,
                    lineHeight = 36.sp
                )
            )
            Text(
                text = minutes,
                style = TextStyle(
                    color = if (isDark) Color.White else Color(0xFF0F172A),
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Black,
                    lineHeight = 36.sp
                )
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        // Vertical divider & date
        Box(
            modifier = Modifier
                .width(3.dp)
                .height(60.dp)
                .background(Color(0xFFE11D48))
        )

        Spacer(modifier = Modifier.width(14.dp))

        Column {
            Text(
                text = "BAUHAUS",
                style = TextStyle(
                    color = Color(0xFFE11D48),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )
            )
            Text(
                text = dateMonth,
                style = TextStyle(
                    color = if (isDark) Color.White else Color(0xFF334155),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            )
            Text(
                text = "STUDIO 01",
                style = TextStyle(
                    color = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B),
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Medium
                )
            )
        }
    }
}

/**
 * Clock Picker Mini Box Dialog
 * Horizontally scrollable carousel displaying all clock styles:
 * Analogue Watch + the 5 Digital Clock Styles!
 * Clicking any saves and applies it to the dashboard.
 */
@Composable
fun ClockPickerMiniBox(
    currentStyle: DashboardClockStyle,
    onStyleSelected: (DashboardClockStyle) -> Unit,
    onDismiss: () -> Unit
) {
    val theme = MayaTheme.colors
    val scrollState = rememberScrollState()
    val calendar = remember { Calendar.getInstance() }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .clip(RoundedCornerShape(26.dp))
                .background(if (theme.isDark) Color(0xF2121722) else Color(0xF7FFFFFF))
                .border(1.2.dp, theme.borderGlass, RoundedCornerShape(26.dp))
                .padding(20.dp)
                .testTag("clock_picker_mini_box")
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(theme.accentPrimary.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Schedule,
                                contentDescription = null,
                                tint = theme.accentPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Select Clock Face",
                                style = TextStyle(
                                    color = theme.textPrimary,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            Text(
                                text = "Scroll across 5 digital styles • Tap to save",
                                style = TextStyle(
                                    color = theme.textSecondary,
                                    fontSize = 11.sp
                                )
                            )
                        }
                    }

                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = theme.textSecondary,
                        modifier = Modifier
                            .size(20.dp)
                            .clickable { onDismiss() }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Horizontal scrollable carousel containing Analogue + 5 Digital Clock Styles
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(scrollState),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    DashboardClockStyle.entries.forEach { style ->
                        val isSelected = currentStyle == style

                        Column(
                            modifier = Modifier
                                .width(220.dp)
                                .clip(RoundedCornerShape(18.dp))
                                .background(
                                    if (isSelected) theme.accentPrimary.copy(alpha = 0.18f)
                                    else theme.surfaceGlass
                                )
                                .border(
                                    if (isSelected) 2.dp else 1.dp,
                                    if (isSelected) theme.accentPrimary else theme.borderGlass,
                                    RoundedCornerShape(18.dp)
                                )
                                .clickable {
                                    onStyleSelected(style)
                                }
                                .padding(14.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // Style Header Pill
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = style.displayName,
                                    style = TextStyle(
                                        color = if (isSelected) theme.accentPrimary else theme.textPrimary,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Selected",
                                        tint = theme.accentPrimary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Mini Preview Box
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(100.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (theme.isDark) Color(0xFF0C0F17) else Color(0xFFF8FAFC)),
                                contentAlignment = Alignment.Center
                            ) {
                                when (style) {
                                    DashboardClockStyle.ANALOGUE -> {
                                        AnalogueWatchFace(
                                            calendar = calendar,
                                            accentColor = theme.accentPrimary,
                                            isDark = theme.isDark,
                                            sizeDp = 80
                                        )
                                    }
                                    DashboardClockStyle.NEON_CYBERPUNK -> {
                                        Text(
                                            text = "10:45",
                                            style = TextStyle(
                                                color = Color(0xFF06B6D4),
                                                fontSize = 24.sp,
                                                fontWeight = FontWeight.Black,
                                                fontFamily = FontFamily.Monospace
                                            )
                                        )
                                    }
                                    DashboardClockStyle.MINIMALIST -> {
                                        Text(
                                            text = "10:45 AM",
                                            style = TextStyle(
                                                color = if (theme.isDark) Color.White else Color(0xFF0F172A),
                                                fontSize = 20.sp,
                                                fontWeight = FontWeight.Light
                                            )
                                        )
                                    }
                                    DashboardClockStyle.FUTURISTIC_DUAL -> {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = "10",
                                                style = TextStyle(color = Color(0xFF0284C7), fontSize = 20.sp, fontWeight = FontWeight.Bold)
                                            )
                                            Text(text = ":", style = TextStyle(color = theme.textSecondary, fontSize = 20.sp))
                                            Text(
                                                text = "45",
                                                style = TextStyle(color = if (theme.isDark) Color.White else Color(0xFF0F172A), fontSize = 20.sp, fontWeight = FontWeight.Bold)
                                            )
                                        }
                                    }
                                    DashboardClockStyle.RETRO_LED -> {
                                        Text(
                                            text = "10:45:00",
                                            style = TextStyle(
                                                color = Color(0xFF10B981),
                                                fontSize = 18.sp,
                                                fontWeight = FontWeight.Bold,
                                                fontFamily = FontFamily.Monospace
                                            )
                                        )
                                    }
                                    DashboardClockStyle.BOLD_BAUHAUS -> {
                                        Text(
                                            text = "10\n45",
                                            style = TextStyle(
                                                color = Color(0xFFE11D48),
                                                fontSize = 16.sp,
                                                fontWeight = FontWeight.Black,
                                                textAlign = TextAlign.Center
                                            )
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = style.description,
                                style = TextStyle(
                                    color = theme.textSecondary,
                                    fontSize = 10.sp,
                                    textAlign = TextAlign.Center
                                ),
                                maxLines = 2
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            // Action button
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(
                                        if (isSelected) theme.accentPrimary else theme.buttonBackground
                                    )
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (isSelected) "Applied" else "Tap to Save",
                                    style = TextStyle(
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Tip: You can re-open this selector anytime by holding the dashboard clock for 1 second.",
                    style = TextStyle(
                        color = theme.textSecondary,
                        fontSize = 10.5.sp,
                        textAlign = TextAlign.Center
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
