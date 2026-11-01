package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.HeadsetMic
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.NotificationEntity
import com.example.ui.components.glassmorphic
import com.example.ui.components.rememberChromeSmoothFlingBehavior
import com.example.ui.theme.MayaTheme
import com.example.ui.viewmodel.MayaViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Point 10: Dedicated Notification Centre with unread badges,
 * announcement broadcasts, support ticket reply alerts, and mark-as-read.
 */
@Composable
fun NotificationCenterScreen(
    viewModel: MayaViewModel,
    onClose: () -> Unit,
    onNavigateToSupport: () -> Unit
) {
    val theme = MayaTheme.colors
    val notifications by viewModel.allNotifications.collectAsStateWithLifecycle()
    val unreadCount by viewModel.unreadNotificationCount.collectAsStateWithLifecycle()
    var selectedFilter by remember { mutableStateOf("ALL") }

    val filteredNotifications = remember(notifications, selectedFilter) {
        when (selectedFilter) {
            "UNREAD" -> notifications.filter { !it.isRead }
            "SUPPORT" -> notifications.filter { it.type == "SUPPORT_REPLY" }
            "ANNOUNCEMENTS" -> notifications.filter { it.type == "ANNOUNCEMENT" || it.type == "ECOSYSTEM_UPDATE" }
            else -> notifications
        }
    }

    BackHandler {
        onClose()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(theme.backgroundGradient)
            .statusBarsPadding()
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        // 1. Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onClose,
                modifier = Modifier
                    .clip(CircleShape)
                    .background(theme.surfaceGlass)
                    .border(1.dp, theme.borderGlass, CircleShape)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = theme.textPrimary
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Notification Centre",
                    style = TextStyle(
                        color = theme.textPrimary,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
                Text(
                    text = if (unreadCount > 0) "$unreadCount Unread Alerts" else "All Caught Up",
                    style = TextStyle(
                        color = if (unreadCount > 0) Color(0xFF38BDF8) else theme.textSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                )
            }

            // Mark All Read
            IconButton(
                onClick = { viewModel.markAllNotificationsAsRead() },
                modifier = Modifier
                    .clip(CircleShape)
                    .background(theme.surfaceGlass)
                    .border(1.dp, theme.borderGlass, CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.DoneAll,
                    contentDescription = "Mark all read",
                    tint = theme.textPrimary,
                    modifier = Modifier.size(19.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 2. Filter Pills
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf(
                "ALL" to "All (${notifications.size})",
                "UNREAD" to "Unread ($unreadCount)",
                "SUPPORT" to "Support Replies",
                "ANNOUNCEMENTS" to "Announcements"
            ).forEach { (key, label) ->
                val isSelected = selectedFilter == key
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isSelected) theme.accentPrimary else theme.surfaceGlass)
                        .border(
                            1.dp,
                            if (isSelected) theme.accentPrimary else theme.borderGlass,
                            RoundedCornerShape(12.dp)
                        )
                        .clickable { selectedFilter = key }
                        .padding(horizontal = 11.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = label,
                        style = TextStyle(
                            color = if (isSelected) Color.White else theme.textSecondary,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 3. Notification List
        if (filteredNotifications.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.NotificationsNone,
                        contentDescription = null,
                        tint = theme.textSecondary,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "No Notifications",
                        style = TextStyle(
                            color = theme.textPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "System updates and support replies will appear here",
                        style = TextStyle(
                            color = theme.textSecondary,
                            fontSize = 12.sp
                        )
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                flingBehavior = rememberChromeSmoothFlingBehavior(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredNotifications, key = { it.id }) { notif ->
                    NotificationCard(
                        notification = notif,
                        onClick = {
                            viewModel.markNotificationAsRead(notif.id)
                            if (notif.type == "SUPPORT_REPLY") {
                                onNavigateToSupport()
                            }
                        },
                        onDelete = { viewModel.deleteNotification(notif.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun NotificationCard(
    notification: NotificationEntity,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val theme = MayaTheme.colors
    val dateStr = remember(notification.timestamp) {
        SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault()).format(Date(notification.timestamp))
    }

    val icon = when (notification.type) {
        "SUPPORT_REPLY" -> Icons.Default.HeadsetMic
        "ANNOUNCEMENT" -> Icons.Default.Campaign
        else -> Icons.Default.Info
    }

    val iconColor = when (notification.type) {
        "SUPPORT_REPLY" -> Color(0xFF38BDF8)
        "ANNOUNCEMENT" -> Color(0xFFF59E0B)
        else -> theme.accentPrimary
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(
                if (!notification.isRead) theme.accentPrimary.copy(alpha = 0.12f) else theme.surfaceGlass
            )
            .border(
                1.dp,
                if (!notification.isRead) theme.accentPrimary.copy(alpha = 0.35f) else theme.borderGlass,
                RoundedCornerShape(18.dp)
            )
            .clickable { onClick() }
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Row(
                verticalAlignment = Alignment.Top,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(iconColor.copy(alpha = 0.18f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconColor,
                        modifier = Modifier.size(19.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = notification.title,
                            style = TextStyle(
                                color = theme.textPrimary,
                                fontSize = 13.5.sp,
                                fontWeight = if (!notification.isRead) FontWeight.Bold else FontWeight.SemiBold
                            )
                        )
                        if (!notification.isRead) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF38BDF8))
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(3.dp))

                    Text(
                        text = notification.message,
                        style = TextStyle(
                            color = theme.textSecondary,
                            fontSize = 12.sp,
                            lineHeight = 17.sp
                        )
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = dateStr,
                        style = TextStyle(
                            color = theme.textSecondary.copy(alpha = 0.6f),
                            fontSize = 10.sp
                        )
                    )
                }
            }

            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Dismiss",
                    tint = theme.textSecondary.copy(alpha = 0.5f),
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}
