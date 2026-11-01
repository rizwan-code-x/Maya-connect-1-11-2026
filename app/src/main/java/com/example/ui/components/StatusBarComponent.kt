package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryFull
import androidx.compose.material.icons.filled.SignalCellular4Bar
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.MayaTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.delay

/**
 * Persistent Top Status Bar Component
 * Displays system-like status telemetry: Live clock, carrier/OS branding badge,
 * cellular connectivity, Wi-Fi status, and battery percentage in liquid glassmorphic styling.
 */
@Composable
fun PersistentStatusBar(
    modifier: Modifier = Modifier,
    systemLabel: String = "Maya OS • REXLIFY",
    batteryPercentage: Int = 98,
    onStatusClick: () -> Unit = {},
    onOpenNotificationShade: () -> Unit = {}
) {
    val theme = MayaTheme.colors

    var currentTimeString by remember {
        mutableStateOf(SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date()))
    }

    LaunchedEffect(Unit) {
        while (true) {
            currentTimeString = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
            delay(1000)
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 4.dp)
            .pointerInput(Unit) {
                detectVerticalDragGestures { _, dragAmount ->
                    if (dragAmount > 20f) {
                        onOpenNotificationShade()
                    }
                }
            }
            .testTag("persistent_top_status_bar")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .glassmorphic(
                    shape = RoundedCornerShape(20.dp),
                    backgroundColor = if (theme.isDark) Color(0xD0121620) else Color(0xE8FFFFFF),
                    borderColor = theme.borderGlass,
                    borderWidth = 1.dp
                )
                .clickable {
                    onOpenNotificationShade()
                    onStatusClick()
                }
                .padding(horizontal = 14.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left: Live Clock & System Brand Pill
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = currentTimeString,
                    style = TextStyle(
                        color = theme.textPrimary,
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.2.sp
                    )
                )

                Spacer(modifier = Modifier.width(8.dp))

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(theme.accentPrimary.copy(alpha = 0.15f))
                        .border(1.dp, theme.accentPrimary.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 7.dp, vertical = 2.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(5.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF10B981))
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = systemLabel,
                            style = TextStyle(
                                color = theme.accentPrimary,
                                fontSize = 9.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }
            }

            // Right: Connectivity & Battery Indicators
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Cellular indicator
                Icon(
                    imageVector = Icons.Default.SignalCellular4Bar,
                    contentDescription = "Cellular Signal",
                    tint = theme.textPrimary,
                    modifier = Modifier.size(13.dp)
                )

                // Wi-Fi indicator
                Icon(
                    imageVector = Icons.Default.Wifi,
                    contentDescription = "Wi-Fi Connected",
                    tint = theme.textPrimary,
                    modifier = Modifier.size(14.dp)
                )

                // Battery pill
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (theme.isDark) Color(0x33FFFFFF) else Color(0x1A000000))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.BatteryFull,
                        contentDescription = "Battery $batteryPercentage%",
                        tint = if (batteryPercentage > 20) Color(0xFF10B981) else Color(0xFFEF4444),
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = "$batteryPercentage%",
                        style = TextStyle(
                            color = theme.textPrimary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }
        }
    }
}
