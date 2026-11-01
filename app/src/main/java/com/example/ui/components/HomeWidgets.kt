package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudQueue
import androidx.compose.material.icons.filled.EventNote
import androidx.compose.material.icons.filled.Headset
import androidx.compose.material.icons.filled.Power
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Watch
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.MayaTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Supported Home Screen Widget Module Types
 */
enum class HomeWidgetType(
    val id: String,
    val displayName: String,
    val description: String,
    val icon: ImageVector
) {
    WEATHER(
        id = "weather",
        displayName = "Weather Forecast",
        description = "Live temperature, sky conditions, and humidity",
        icon = Icons.Default.WbSunny
    ),
    CALENDAR(
        id = "calendar",
        displayName = "Calendar & Schedule",
        description = "Today's schedule, key appointments, and date",
        icon = Icons.Default.CalendarMonth
    ),
    BATTERY_STATUS(
        id = "battery",
        displayName = "Battery & Power Monitor",
        description = "Live charge status, battery health, and power mode",
        icon = Icons.Default.BatteryChargingFull
    )
}

/**
 * Customizable Home Screen Widget System Container
 * Displays active modules in a liquid glassmorphic design and gives users an "Edit Widgets" modal trigger.
 */
@Composable
fun HomeWidgetSection(
    activeWidgets: List<HomeWidgetType>,
    onCustomizeClick: () -> Unit,
    modifier: Modifier = Modifier,
    hapticEnabled: Boolean = true
) {
    val theme = MayaTheme.colors

    Column(modifier = modifier.fillMaxWidth()) {
        // Section Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 2.dp, vertical = 2.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF38BDF8))
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "System Widgets",
                    style = TextStyle(
                        color = theme.textPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.2).sp
                    )
                )
            }

            // Customize / Edit Widgets Action Button
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(theme.accentPrimary.copy(alpha = 0.12f))
                    .border(1.dp, theme.accentPrimary.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                    .clickable { onCustomizeClick() }
                    .padding(horizontal = 8.dp, vertical = 4.dp)
                    .testTag("customize_widgets_btn")
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Tune,
                        contentDescription = "Customize Widgets",
                        tint = theme.accentPrimary,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Customize",
                        style = TextStyle(
                            color = theme.accentPrimary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (activeWidgets.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .glassmorphic(
                        shape = RoundedCornerShape(18.dp),
                        backgroundColor = theme.surfaceGlass,
                        borderColor = theme.borderGlass,
                        borderWidth = 1.dp
                    )
                    .clickable { onCustomizeClick() }
                    .padding(18.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "No widgets currently added",
                        style = TextStyle(color = theme.textPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Tap to add Weather, Calendar, or Battery modules",
                        style = TextStyle(color = theme.accentPrimary, fontSize = 11.5.sp)
                    )
                }
            }
        } else {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                activeWidgets.forEach { widgetType ->
                    when (widgetType) {
                        HomeWidgetType.WEATHER -> WeatherWidgetCard(hapticEnabled = hapticEnabled)
                        HomeWidgetType.CALENDAR -> CalendarWidgetCard(hapticEnabled = hapticEnabled)
                        HomeWidgetType.BATTERY_STATUS -> BatteryStatusWidgetCard(hapticEnabled = hapticEnabled)
                    }
                }
            }
        }
    }
}

/**
 * 1. Live Weather Forecast Module
 */
@Composable
fun WeatherWidgetCard(
    modifier: Modifier = Modifier,
    hapticEnabled: Boolean = true
) {
    val theme = MayaTheme.colors
    var isCelsius by remember { mutableStateOf(true) }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .glassmorphic(
                shape = RoundedCornerShape(20.dp),
                backgroundColor = if (theme.isDark) Color(0xD0131B2A) else Color(0xECF0FDF4),
                borderColor = if (theme.isDark) Color(0x3338BDF8) else Color(0x440284C7),
                borderWidth = 1.dp
            )
            .padding(16.dp)
            .testTag("weather_widget_card")
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    listOf(Color(0xFFF59E0B), Color(0xFFEF4444))
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.WbSunny,
                            contentDescription = "Weather",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Global Weather",
                            style = TextStyle(
                                color = theme.textPrimary,
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Text(
                            text = "Kolkata / Silicon Valley • Real-time",
                            style = TextStyle(color = theme.textSecondary, fontSize = 10.5.sp)
                        )
                    }
                }

                // Temp scale toggle chip (°C / °F)
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(theme.accentPrimary.copy(alpha = 0.15f))
                        .border(1.dp, theme.accentPrimary.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                        .clickable { isCelsius = !isCelsius }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (isCelsius) "°C / °F" else "°F / °C",
                        style = TextStyle(color = theme.accentPrimary, fontSize = 10.5.sp, fontWeight = FontWeight.Bold)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Large Temperature Display
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = if (isCelsius) "26°" else "79°",
                        style = TextStyle(
                            color = theme.textPrimary,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = (-1).sp
                        )
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Column(modifier = Modifier.padding(bottom = 4.dp)) {
                        Text(
                            text = "Sunny & Clear",
                            style = TextStyle(color = Color(0xFFF59E0B), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = if (isCelsius) "H: 29° • L: 21°" else "H: 84° • L: 70°",
                            style = TextStyle(color = theme.textSecondary, fontSize = 10.sp)
                        )
                    }
                }

                // Weather Telemetry Badges
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TelemetryPill(icon = Icons.Default.CloudQueue, label = "62% Humidity")
                    TelemetryPill(icon = Icons.Default.Air, label = "12 km/h")
                }
            }
        }
    }
}

/**
 * 2. Smart Calendar & Upcoming Schedule Module
 */
@Composable
fun CalendarWidgetCard(
    modifier: Modifier = Modifier,
    hapticEnabled: Boolean = true
) {
    val theme = MayaTheme.colors
    val todayDate = remember {
        SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.getDefault()).format(Date())
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .glassmorphic(
                shape = RoundedCornerShape(20.dp),
                backgroundColor = if (theme.isDark) Color(0xD0171626) else Color(0xECFAF5FF),
                borderColor = if (theme.isDark) Color(0x33A855F7) else Color(0x447C3AED),
                borderWidth = 1.dp
            )
            .padding(16.dp)
            .testTag("calendar_widget_card")
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    listOf(Color(0xFF7C3AED), Color(0xFF4F46E5))
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = "Calendar",
                            tint = Color.White,
                            modifier = Modifier.size(19.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Calendar & Agenda",
                            style = TextStyle(
                                color = theme.textPrimary,
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Text(
                            text = todayDate,
                            style = TextStyle(color = theme.textSecondary, fontSize = 10.5.sp)
                        )
                    }
                }

                // Event Count Pill
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0x227C3AED))
                        .padding(horizontal = 7.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "2 Events",
                        style = TextStyle(color = Color(0xFFA78BFA), fontSize = 10.5.sp, fontWeight = FontWeight.Bold)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Upcoming Event Snippet 1
            ScheduleEventItem(
                time = "10:30 AM",
                title = "Rizwan RR Official Ecosystem Briefing",
                category = "Keynote",
                accentColor = Color(0xFF0284C7)
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Upcoming Event Snippet 2
            ScheduleEventItem(
                time = "02:00 PM",
                title = "Maya Neural Architecture Sync",
                category = "Development",
                accentColor = Color(0xFF10B981)
            )
        }
    }
}

/**
 * 3. Battery & Power Monitor Module
 */
@Composable
fun BatteryStatusWidgetCard(
    modifier: Modifier = Modifier,
    hapticEnabled: Boolean = true
) {
    val theme = MayaTheme.colors
    var isPowerSaverOn by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .glassmorphic(
                shape = RoundedCornerShape(20.dp),
                backgroundColor = if (theme.isDark) Color(0xD012211C) else Color(0xECF0FDF4),
                borderColor = if (theme.isDark) Color(0x3310B981) else Color(0x44059669),
                borderWidth = 1.dp
            )
            .padding(16.dp)
            .testTag("battery_widget_card")
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    listOf(Color(0xFF10B981), Color(0xFF047857))
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.BatteryChargingFull,
                            contentDescription = "Battery",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Battery & Health",
                            style = TextStyle(
                                color = theme.textPrimary,
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Text(
                            text = "Fast Charging • 100% Health",
                            style = TextStyle(color = Color(0xFF10B981), fontSize = 10.5.sp, fontWeight = FontWeight.SemiBold)
                        )
                    }
                }

                // Big Percentage
                Text(
                    text = "98%",
                    style = TextStyle(
                        color = theme.textPrimary,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black
                    )
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Battery Level Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(theme.surface.copy(alpha = 0.5f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.98f)
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(
                            Brush.horizontalGradient(
                                listOf(Color(0xFF10B981), Color(0xFF34D399))
                            )
                        )
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Accessories telemetry & Power Saver Switch
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AccessoryBatteryBadge(icon = Icons.Default.Headset, label = "Buds 85%")
                    AccessoryBatteryBadge(icon = Icons.Default.Watch, label = "Watch 92%")
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Eco Saver",
                        style = TextStyle(
                            color = theme.textSecondary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Switch(
                        checked = isPowerSaverOn,
                        onCheckedChange = { isPowerSaverOn = it },
                        modifier = Modifier.size(width = 36.dp, height = 22.dp),
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = Color(0xFF10B981),
                            uncheckedThumbColor = theme.textSecondary,
                            uncheckedTrackColor = theme.surfaceGlass
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun TelemetryPill(icon: ImageVector, label: String) {
    val theme = MayaTheme.colors
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(theme.surface.copy(alpha = 0.4f))
            .padding(horizontal = 7.dp, vertical = 4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = theme.accentPrimary,
            modifier = Modifier.size(12.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = label,
            style = TextStyle(color = theme.textSecondary, fontSize = 10.sp, fontWeight = FontWeight.Medium)
        )
    }
}

@Composable
private fun ScheduleEventItem(
    time: String,
    title: String,
    category: String,
    accentColor: Color
) {
    val theme = MayaTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(theme.surface.copy(alpha = 0.45f))
            .padding(horizontal = 10.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(accentColor.copy(alpha = 0.2f))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = time,
                    style = TextStyle(color = accentColor, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = title,
                style = TextStyle(color = theme.textPrimary, fontSize = 11.5.sp, fontWeight = FontWeight.Medium),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Text(
            text = category,
            style = TextStyle(color = theme.textSecondary, fontSize = 9.5.sp)
        )
    }
}

@Composable
private fun AccessoryBatteryBadge(icon: ImageVector, label: String) {
    val theme = MayaTheme.colors
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(theme.surface.copy(alpha = 0.45f))
            .padding(horizontal = 7.dp, vertical = 4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = theme.textSecondary,
            modifier = Modifier.size(13.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = label,
            style = TextStyle(color = theme.textSecondary, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
        )
    }
}

/**
 * Customizable Widgets Dialog
 * Allows users to toggle, reorder, and add/remove Weather, Calendar, and Battery modules.
 */
@Composable
fun CustomizeWidgetsDialog(
    currentWidgets: List<HomeWidgetType>,
    onSaveWidgets: (List<HomeWidgetType>) -> Unit,
    onDismiss: () -> Unit
) {
    val theme = MayaTheme.colors
    var activeSet by remember { mutableStateOf(currentWidgets.toSet()) }

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .clip(RoundedCornerShape(24.dp))
                .background(if (theme.isDark) Color(0xF2121622) else Color(0xF8FFFFFF))
                .border(1.2.dp, theme.borderGlass, RoundedCornerShape(24.dp))
                .padding(20.dp)
                .testTag("customize_widgets_dialog")
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(theme.accentPrimary.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Tune,
                                contentDescription = null,
                                tint = theme.accentPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Customize Widgets",
                            style = TextStyle(
                                color = theme.textPrimary,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
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

                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Select which modules appear on your home screen:",
                    style = TextStyle(color = theme.textSecondary, fontSize = 12.sp)
                )

                Spacer(modifier = Modifier.height(14.dp))

                HomeWidgetType.entries.forEach { type ->
                    val isChecked = activeSet.contains(type)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(
                                if (isChecked) theme.accentPrimary.copy(alpha = 0.12f)
                                else theme.surface.copy(alpha = 0.3f)
                            )
                            .border(
                                width = if (isChecked) 1.dp else 0.5.dp,
                                color = if (isChecked) theme.accentPrimary else theme.borderGlass,
                                shape = RoundedCornerShape(14.dp)
                            )
                            .clickable {
                                activeSet = if (isChecked) activeSet - type else activeSet + type
                            }
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(theme.accentPrimary.copy(alpha = 0.18f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = type.icon,
                                    contentDescription = null,
                                    tint = theme.accentPrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = type.displayName,
                                    style = TextStyle(
                                        color = theme.textPrimary,
                                        fontSize = 13.5.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                                Text(
                                    text = type.description,
                                    style = TextStyle(color = theme.textSecondary, fontSize = 10.5.sp),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        Switch(
                            checked = isChecked,
                            onCheckedChange = { checked ->
                                activeSet = if (checked) activeSet + type else activeSet - type
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = theme.accentPrimary
                            )
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(theme.accentPrimary)
                            .clickable {
                                // Order preserving
                                val ordered = HomeWidgetType.entries.filter { activeSet.contains(it) }
                                onSaveWidgets(ordered)
                                onDismiss()
                            }
                            .padding(horizontal = 18.dp, vertical = 9.dp)
                            .testTag("save_widgets_btn")
                    ) {
                        Text(
                            text = "Save Layout",
                            style = TextStyle(color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        )
                    }
                }
            }
        }
    }
}
