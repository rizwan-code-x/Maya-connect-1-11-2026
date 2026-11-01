package com.example.ui.components

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AirplanemodeActive
import androidx.compose.material.icons.filled.AirplanemodeInactive
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.BluetoothDisabled
import androidx.compose.material.icons.filled.BrightnessLow
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DoNotDisturb
import androidx.compose.material.icons.filled.DoNotDisturbOff
import androidx.compose.material.icons.filled.FlashlightOff
import androidx.compose.material.icons.filled.FlashlightOn
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.ScreenRotation
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SignalCellular4Bar
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.MayaTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.delay

data class QuickToggleItem(
    val id: String,
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val isActive: Boolean,
    val activeColor: Color,
    val onToggle: () -> Unit
)

data class SystemAlert(
    val id: String,
    val appTitle: String,
    val message: String,
    val timestamp: String,
    val category: String,
    val icon: ImageVector = Icons.Default.Notifications
)

/**
 * Downward-Swipeable Notification Shade Component
 * Slides down smoothly from the status bar to show recent system alerts and quick toggle buttons
 * for Bluetooth, Do Not Disturb, Airplane Mode, Wi-Fi, and screen controls.
 */
@Composable
fun NotificationShadeComponent(
    isOpen: Boolean,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (!isOpen) return

    BackHandler { onClose() }

    val theme = MayaTheme.colors

    // Live clock & date
    var currentTimeString by remember {
        mutableStateOf(SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date()))
    }
    var currentDateString by remember {
        mutableStateOf(SimpleDateFormat("EEEE, MMMM d", Locale.getDefault()).format(Date()))
    }

    LaunchedEffect(Unit) {
        while (true) {
            val now = Date()
            currentTimeString = SimpleDateFormat("HH:mm", Locale.getDefault()).format(now)
            currentDateString = SimpleDateFormat("EEEE, MMMM d", Locale.getDefault()).format(now)
            delay(1000)
        }
    }

    // Toggle States
    var isBluetoothOn by remember { mutableStateOf(true) }
    var isDndOn by remember { mutableStateOf(false) }
    var isAirplaneModeOn by remember { mutableStateOf(false) }
    var isWifiOn by remember { mutableStateOf(true) }
    var isFlashlightOn by remember { mutableStateOf(false) }
    var isAutoRotateOn by remember { mutableStateOf(true) }
    var brightnessLevel by remember { mutableFloatStateOf(0.75f) }

    // System Alerts List
    val alerts = remember {
        mutableStateListOf(
            SystemAlert(
                id = "alert_1",
                appTitle = "Maya OS Core",
                message = "Workspace node is running optimal with 100% security verification.",
                timestamp = "Just now",
                category = "SYSTEM"
            ),
            SystemAlert(
                id = "alert_2",
                appTitle = "REXLIFY Music",
                message = "10-Second Welcome to REXLIFY intro track generated and ready.",
                timestamp = "2m ago",
                category = "AUDIO"
            ),
            SystemAlert(
                id = "alert_3",
                appTitle = "SIFRA AI Intelligence",
                message = "Model connection established at https://sifrar.ai.studio.",
                timestamp = "10m ago",
                category = "AI"
            ),
            SystemAlert(
                id = "alert_4",
                appTitle = "Battery Manager",
                message = "Battery at 98% • Super fast charging connected.",
                timestamp = "25m ago",
                category = "POWER"
            )
        )
    }

    val quickToggles = listOf(
        QuickToggleItem(
            id = "toggle_bluetooth",
            title = "Bluetooth",
            subtitle = if (isBluetoothOn) "Connected" else "Off",
            icon = if (isBluetoothOn) Icons.Default.Bluetooth else Icons.Default.BluetoothDisabled,
            isActive = isBluetoothOn,
            activeColor = Color(0xFF3B82F6),
            onToggle = { isBluetoothOn = !isBluetoothOn }
        ),
        QuickToggleItem(
            id = "toggle_dnd",
            title = "Do Not Disturb",
            subtitle = if (isDndOn) "Total Silence" else "Off",
            icon = if (isDndOn) Icons.Default.DoNotDisturb else Icons.Default.DoNotDisturbOff,
            isActive = isDndOn,
            activeColor = Color(0xFFEF4444),
            onToggle = { isDndOn = !isDndOn }
        ),
        QuickToggleItem(
            id = "toggle_airplane",
            title = "Airplane Mode",
            subtitle = if (isAirplaneModeOn) "Radios Off" else "Normal",
            icon = if (isAirplaneModeOn) Icons.Default.AirplanemodeActive else Icons.Default.AirplanemodeInactive,
            isActive = isAirplaneModeOn,
            activeColor = Color(0xFFF59E0B),
            onToggle = { isAirplaneModeOn = !isAirplaneModeOn }
        ),
        QuickToggleItem(
            id = "toggle_wifi",
            title = "Wi-Fi",
            subtitle = if (isWifiOn) "Maya_5GHz" else "Off",
            icon = if (isWifiOn) Icons.Default.Wifi else Icons.Default.WifiOff,
            isActive = isWifiOn,
            activeColor = Color(0xFF10B981),
            onToggle = { isWifiOn = !isWifiOn }
        ),
        QuickToggleItem(
            id = "toggle_flashlight",
            title = "Flashlight",
            subtitle = if (isFlashlightOn) "Torch On" else "Off",
            icon = if (isFlashlightOn) Icons.Default.FlashlightOn else Icons.Default.FlashlightOff,
            isActive = isFlashlightOn,
            activeColor = Color(0xFFEAB308),
            onToggle = { isFlashlightOn = !isFlashlightOn }
        ),
        QuickToggleItem(
            id = "toggle_autorotate",
            title = "Auto Rotate",
            subtitle = if (isAutoRotateOn) "Portrait/Landscape" else "Locked",
            icon = Icons.Default.ScreenRotation,
            isActive = isAutoRotateOn,
            activeColor = Color(0xFF8B5CF6),
            onToggle = { isAutoRotateOn = !isAutoRotateOn }
        )
    )

    // Swipe up to dismiss gesture state
    val draggableState = rememberDraggableState { delta ->
        if (delta < -30f) {
            onClose()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.65f))
            .clickable { onClose() }
            .testTag("notification_shade_overlay")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
                .background(if (theme.isDark) Color(0xF60F131D) else Color(0xFBF8FAFD))
                .border(
                    1.dp,
                    theme.borderGlass,
                    RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp)
                )
                .clickable(enabled = false) {} // Prevent click-through dismiss
                .statusBarsPadding()
                .padding(horizontal = 18.dp, vertical = 12.dp)
                .draggable(
                    state = draggableState,
                    orientation = Orientation.Vertical
                )
                .testTag("notification_shade_content")
        ) {
            // Top Status & Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = currentTimeString,
                        style = TextStyle(
                            color = theme.textPrimary,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = (-0.5).sp
                        )
                    )
                    Text(
                        text = currentDateString,
                        style = TextStyle(
                            color = theme.textSecondary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(theme.surfaceGlass)
                            .border(1.dp, theme.borderGlass, RoundedCornerShape(10.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "Maya OS • Notification Shade",
                            style = TextStyle(
                                color = theme.accentPrimary,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(theme.surfaceGlass)
                            .border(1.dp, theme.borderGlass, CircleShape)
                            .clickable { onClose() }
                            .testTag("notification_shade_close_btn"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close Shade",
                            tint = theme.textPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Quick Toggle Buttons Grid (2 rows x 3 columns)
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(144.dp),
                flingBehavior = rememberChromeSmoothFlingBehavior(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                userScrollEnabled = false
            ) {
                items(items = quickToggles, key = { it.id }) { item ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(68.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                if (item.isActive) item.activeColor.copy(alpha = if (theme.isDark) 0.25f else 0.18f)
                                else theme.surfaceGlass
                            )
                            .border(
                                1.dp,
                                if (item.isActive) item.activeColor.copy(alpha = 0.5f) else theme.borderGlass,
                                RoundedCornerShape(16.dp)
                            )
                            .clickable { item.onToggle() }
                            .padding(horizontal = 8.dp, vertical = 6.dp)
                            .testTag(item.id),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(26.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (item.isActive) item.activeColor.copy(alpha = 0.28f)
                                        else theme.textSecondary.copy(alpha = 0.1f)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = item.icon,
                                    contentDescription = item.title,
                                    tint = if (item.isActive) item.activeColor else theme.textSecondary,
                                    modifier = Modifier.size(15.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = item.title,
                                style = TextStyle(
                                    color = theme.textPrimary,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = item.subtitle,
                                style = TextStyle(
                                    color = if (item.isActive) item.activeColor else theme.textSecondary,
                                    fontSize = 8.5.sp,
                                    fontWeight = FontWeight.Medium
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Brightness Slider
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(theme.surfaceGlass)
                    .border(1.dp, theme.borderGlass, RoundedCornerShape(14.dp))
                    .padding(horizontal = 14.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.BrightnessLow,
                    contentDescription = null,
                    tint = theme.textSecondary,
                    modifier = Modifier.size(18.dp)
                )

                Slider(
                    value = brightnessLevel,
                    onValueChange = { brightnessLevel = it },
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 8.dp)
                        .testTag("notification_shade_brightness_slider"),
                    colors = SliderDefaults.colors(
                        thumbColor = theme.accentPrimary,
                        activeTrackColor = theme.accentPrimary,
                        inactiveTrackColor = theme.borderGlass
                    )
                )

                Icon(
                    imageVector = Icons.Default.WbSunny,
                    contentDescription = null,
                    tint = theme.accentPrimary,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Recent System Alerts Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "System Alerts",
                        style = TextStyle(
                            color = theme.textPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(theme.accentPrimary.copy(alpha = 0.2f))
                            .padding(horizontal = 6.dp, vertical = 1.dp)
                    ) {
                        Text(
                            text = "${alerts.size}",
                            style = TextStyle(
                                color = theme.accentPrimary,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }

                if (alerts.isNotEmpty()) {
                    Text(
                        text = "Clear All",
                        style = TextStyle(
                            color = theme.accentPrimary,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .clickable { alerts.clear() }
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                            .testTag("notification_shade_clear_all_btn")
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // System Alerts List
            if (alerts.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No active system alerts",
                        style = TextStyle(
                            color = theme.textSecondary,
                            fontSize = 12.sp
                        )
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp),
                    flingBehavior = rememberChromeSmoothFlingBehavior(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(items = alerts, key = { it.id }) { alert ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(theme.surfaceGlass)
                                .border(1.dp, theme.borderGlass, RoundedCornerShape(12.dp))
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(30.dp)
                                        .clip(CircleShape)
                                        .background(theme.accentPrimary.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = alert.icon,
                                        contentDescription = null,
                                        tint = theme.accentPrimary,
                                        modifier = Modifier.size(15.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = alert.appTitle,
                                            style = TextStyle(
                                                color = theme.textPrimary,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "• ${alert.timestamp}",
                                            style = TextStyle(
                                                color = theme.textSecondary,
                                                fontSize = 9.5.sp
                                            )
                                        )
                                    }
                                    Text(
                                        text = alert.message,
                                        style = TextStyle(
                                            color = theme.textSecondary,
                                            fontSize = 11.sp
                                        ),
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }

                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Dismiss Alert",
                                tint = theme.textSecondary.copy(alpha = 0.6f),
                                modifier = Modifier
                                    .size(16.dp)
                                    .clickable { alerts.remove(alert) }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Bottom drag handle to swipe up to close
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onClose() }
                    .padding(vertical = 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowUp,
                    contentDescription = "Swipe up to close shade",
                    tint = theme.textSecondary,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "Swipe up to close",
                    style = TextStyle(
                        color = theme.textSecondary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium
                    )
                )
            }
        }
    }
}
