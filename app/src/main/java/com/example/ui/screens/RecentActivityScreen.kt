package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.ClearAll
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppItem
import com.example.ui.components.AppLogoIcon
import com.example.ui.components.GlassButton
import com.example.ui.components.LiveStatusBadge
import com.example.ui.components.MayaTopBar
import com.example.ui.components.glassmorphic
import com.example.ui.components.rememberChromeSmoothFlingBehavior
import com.example.ui.theme.MayaTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun RecentActivityScreen(
    userName: String,
    recentlyUsedApps: List<AppItem>,
    onResumeApp: (AppItem) -> Unit,
    onAppClick: (AppItem) -> Unit,
    onProfileClick: () -> Unit,
    onClearHistory: () -> Unit,
    hapticEnabled: Boolean = true
) {
    val theme = MayaTheme.colors
    val latestApp = recentlyUsedApps.firstOrNull()
    val dateFormat = SimpleDateFormat("h:mm a • MMM d", Locale.getDefault())

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        flingBehavior = rememberChromeSmoothFlingBehavior(),
        contentPadding = PaddingValues(start = 18.dp, end = 18.dp, top = 2.dp, bottom = 120.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Minimal Top Bar
        item {
            MayaTopBar(
                userName = userName,
                onProfileClick = onProfileClick
            )
        }

        // Title Header
        item {
            Column(modifier = Modifier.padding(horizontal = 4.dp)) {
                Text(
                    text = "Recent Activity",
                    style = TextStyle(
                        color = theme.textPrimary,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.3).sp
                    )
                )
                Text(
                    text = "Continue your previous workspace sessions",
                    style = TextStyle(
                        color = theme.textSecondary,
                        fontSize = 12.5.sp
                    )
                )
            }
        }

        // Point 49: Resume Previous App Card
        if (latestApp != null) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(theme.surfaceGlass)
                        .border(1.2.dp, theme.accentPrimary.copy(alpha = 0.35f), RoundedCornerShape(20.dp))
                        .padding(18.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "RESUME PREVIOUS APP",
                                style = TextStyle(
                                    color = theme.accentPrimary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.8.sp
                                )
                            )
                            LiveStatusBadge(status = latestApp.status)
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            AppLogoIcon(app = latestApp, size = 48.dp, cornerRadius = 12.dp)
                            Spacer(modifier = Modifier.width(14.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = latestApp.name,
                                    style = TextStyle(
                                        color = theme.textPrimary,
                                        fontSize = 17.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                                Text(
                                    text = latestApp.tagline,
                                    style = TextStyle(
                                        color = theme.textSecondary,
                                        fontSize = 11.5.sp
                                    ),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        GlassButton(
                            text = "Continue / Resume →",
                            onClick = { onResumeApp(latestApp) },
                            icon = Icons.Default.PlayArrow,
                            primaryColor = theme.buttonBackground,
                            modifier = Modifier.fillMaxWidth(),
                            hapticEnabled = hapticEnabled
                        )
                    }
                }
            }
        }

        // Recent Activity History List
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, start = 2.dp, end = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Previous Sessions (${recentlyUsedApps.size})",
                    style = TextStyle(
                        color = theme.textPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                )

                if (recentlyUsedApps.isNotEmpty()) {
                    Text(
                        text = "Clear History",
                        style = TextStyle(
                            color = theme.textSecondary,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Medium
                        ),
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .clickable { onClearHistory() }
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
        }

        if (recentlyUsedApps.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(theme.surfaceGlass)
                        .border(1.dp, theme.borderGlass, RoundedCornerShape(18.dp))
                        .padding(28.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = null,
                            tint = theme.textSecondary,
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "No recent activity recorded",
                            style = TextStyle(
                                color = theme.textPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                        Text(
                            text = "Launch an application to track your session history",
                            style = TextStyle(
                                color = theme.textSecondary,
                                fontSize = 11.5.sp
                            )
                        )
                    }
                }
            }
        } else {
            items(items = recentlyUsedApps, key = { "recent_" + it.id }) { app ->
                RecentAppItemRow(
                    app = app,
                    lastOpenedText = if (app.lastOpenedTimestamp > 0) {
                        dateFormat.format(Date(app.lastOpenedTimestamp))
                    } else "Recently",
                    onClick = { onAppClick(app) }
                )
            }
        }
    }
}

@Composable
private fun RecentAppItemRow(
    app: AppItem,
    lastOpenedText: String,
    onClick: () -> Unit
) {
    val theme = MayaTheme.colors

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(theme.surfaceGlass)
            .border(1.dp, theme.borderGlass, RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            AppLogoIcon(app = app, size = 36.dp, cornerRadius = 9.dp)
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = app.name,
                    style = TextStyle(
                        color = theme.textPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
                Text(
                    text = lastOpenedText,
                    style = TextStyle(
                        color = theme.textSecondary,
                        fontSize = 11.sp
                    )
                )
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "Open",
                style = TextStyle(
                    color = theme.accentPrimary,
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.SemiBold
                )
            )
            Spacer(modifier = Modifier.width(3.dp))
            Icon(
                imageVector = Icons.Default.ArrowForward,
                contentDescription = null,
                tint = theme.accentPrimary,
                modifier = Modifier.size(12.dp)
            )
        }
    }
}
