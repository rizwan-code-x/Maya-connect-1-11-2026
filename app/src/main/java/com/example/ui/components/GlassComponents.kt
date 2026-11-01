package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.example.R
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Apps
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppStatus
import com.example.ui.theme.MayaColorFavorite
import com.example.ui.theme.MayaColorSuccess
import com.example.ui.theme.MayaColorWarning
import com.example.ui.theme.MayaTheme

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    cornerRadius: Int = 18,
    backgroundColor: Color? = null,
    borderColor: Color? = null,
    borderWidth: Int = 1,
    hapticEnabled: Boolean = true,
    onLongClick: (() -> Unit)? = null,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    val shape = RoundedCornerShape(cornerRadius.dp)
    val clickModifier = if (onClick != null) {
        Modifier.springPressEffect(
            hapticEnabled = hapticEnabled,
            onLongClick = onLongClick,
            onClick = onClick
        )
    } else {
        Modifier
    }

    Box(
        modifier = modifier
            .then(clickModifier)
            .glassmorphic(
                shape = shape,
                backgroundColor = backgroundColor,
                borderColor = borderColor,
                borderWidth = borderWidth.dp
            )
    ) {
        content()
    }
}

@Composable
fun GlassButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    primaryColor: Color? = null,
    textColor: Color? = null,
    hapticEnabled: Boolean = true,
    testTag: String = "glass_button"
) {
    val theme = MayaTheme.colors
    val bg = primaryColor ?: theme.buttonBackground
    val textCol = textColor ?: if (theme.isDark) Color.White else Color.White

    Box(
        modifier = modifier
            .testTag(testTag)
            .springPressEffect(
                hapticEnabled = hapticEnabled,
                scaleTarget = 0.96f,
                onClick = onClick
            )
            .clip(RoundedCornerShape(14.dp))
            .background(bg)
            .border(1.dp, theme.borderGlass, RoundedCornerShape(14.dp))
            .padding(horizontal = 18.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = textCol,
                    modifier = Modifier.size(17.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = text,
                style = TextStyle(
                    color = textCol,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    letterSpacing = 0.2.sp
                )
            )
        }
    }
}

@Composable
fun LiveStatusBadge(
    status: AppStatus,
    modifier: Modifier = Modifier
) {
    val (beaconColor, text) = when (status) {
        AppStatus.AVAILABLE, AppStatus.ACTIVE -> MayaColorSuccess to status.label
        AppStatus.COMING_SOON -> MayaColorWarning to status.label
        AppStatus.MAINTENANCE -> MayaColorWarning to status.label
    }

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(beaconColor.copy(alpha = 0.12f))
            .border(0.6.dp, beaconColor.copy(alpha = 0.35f), RoundedCornerShape(8.dp))
            .padding(horizontal = 7.dp, vertical = 2.5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(5.dp)
                .clip(CircleShape)
                .background(beaconColor)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = text,
            style = TextStyle(
                color = beaconColor,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.3.sp
            )
        )
    }
}

// Points 3 & 10: Clean top navigation: Brand on left, Notification Bell & Profile Avatar on right
@Composable
fun MayaTopBar(
    userName: String,
    onProfileClick: () -> Unit,
    onMayaConnectClick: () -> Unit = {},
    onNotificationClick: () -> Unit = {},
    unreadNotificationsCount: Int = 0,
    modifier: Modifier = Modifier
) {
    val theme = MayaTheme.colors
    val initials = if (userName.isNotBlank()) userName.take(1).uppercase() else "R"

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left: Clean Maya Connect branding - Click opens Official Profile
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .clickable { onMayaConnectClick() }
                .padding(vertical = 2.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color.White)
                    .border(1.dp, theme.borderGlass, RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.maya_connect_official_logo),
                    contentDescription = "Maya Connect Official Logo",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(10.dp))
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = "Maya Connect",
                    style = TextStyle(
                        color = theme.textPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.3).sp
                    )
                )
                Text(
                    text = "Rizwan RR Ecosystem",
                    style = TextStyle(
                        color = theme.textSecondary,
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Medium
                    )
                )
            }
        }

        // Right: Notification Bell & Profile Avatar Button (Points 3, 10, 24-25)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Notification Center Bell with Badge
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(theme.surfaceGlass)
                    .border(1.dp, theme.borderGlass, CircleShape)
                    .clickable { onNotificationClick() }
                    .testTag("notification_button"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = androidx.compose.material.icons.Icons.Default.Notifications,
                    contentDescription = "Notification Centre",
                    tint = theme.textPrimary,
                    modifier = Modifier.size(19.dp)
                )
                if (unreadNotificationsCount > 0) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .size(16.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFEF4444)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (unreadNotificationsCount > 9) "9+" else "$unreadNotificationsCount",
                            style = TextStyle(
                                color = Color.White,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }
            }

            // Profile Avatar Button with New Official Logo
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(Color.White)
                    .border(1.dp, theme.borderGlass, CircleShape)
                    .clickable { onProfileClick() }
                    .testTag("profile_button"),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.maya_connect_official_logo),
                    contentDescription = "Profile $initials",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                )
            }
        }
    }
}

@Composable
fun GlassSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "Search apps, services & tools"
) {
    val theme = MayaTheme.colors

    Box(
        modifier = modifier
            .fillMaxWidth()
            .glassmorphic(
                shape = RoundedCornerShape(16.dp),
                backgroundColor = theme.surfaceGlass,
                borderColor = theme.borderGlass,
                borderWidth = 1.dp
            )
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search",
                tint = theme.textSecondary,
                modifier = Modifier.size(19.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.CenterStart
            ) {
                if (query.isEmpty()) {
                    Text(
                        text = placeholder,
                        style = TextStyle(
                            color = theme.textSecondary,
                            fontSize = 14.sp
                        )
                    )
                }
                BasicTextField(
                    value = query,
                    onValueChange = onQueryChange,
                    textStyle = TextStyle(
                        color = theme.textPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    ),
                    cursorBrush = SolidColor(theme.textPrimary),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("search_input"),
                    singleLine = true
                )
            }
            if (query.isNotEmpty()) {
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .size(22.dp)
                        .clip(CircleShape)
                        .background(theme.textSecondary.copy(alpha = 0.2f))
                        .clickable { onQueryChange("") },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = "Clear search",
                        tint = theme.textPrimary,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}

// Points 43-50: 5 Navigation Tabs (Home, Apps, Explore, Recent Activity, Me)
enum class NavigationTab(val title: String) {
    HOME("Home"),
    APPS("Apps"),
    EXPLORE("Explore"),
    RECENT("Recent"),
    ME("Me")
}

@Composable
fun GlassBottomNavigationBar(
    currentTab: NavigationTab,
    onTabSelected: (NavigationTab) -> Unit,
    modifier: Modifier = Modifier,
    hapticEnabled: Boolean = true
) {
    val theme = MayaTheme.colors

    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 18.dp, vertical = 10.dp)
    ) {
        // Floating clean glass bottom bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .glassmorphic(
                    shape = RoundedCornerShape(22.dp),
                    backgroundColor = if (theme.isDark) Color(0xF2151922) else Color(0xF5FFFFFF),
                    borderColor = theme.borderGlass,
                    borderWidth = 1.dp
                )
                .padding(horizontal = 6.dp, vertical = 6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                NavigationTab.entries.forEach { tab ->
                    val isSelected = tab == currentTab
                    val icon = when (tab) {
                        NavigationTab.HOME -> if (isSelected) Icons.Filled.Home else Icons.Outlined.Home
                        NavigationTab.APPS -> if (isSelected) Icons.Filled.Apps else Icons.Outlined.Apps
                        NavigationTab.EXPLORE -> if (isSelected) Icons.Filled.Explore else Icons.Outlined.Explore
                        NavigationTab.RECENT -> if (isSelected) Icons.Filled.History else Icons.Outlined.History
                        NavigationTab.ME -> if (isSelected) Icons.Filled.Person else Icons.Outlined.Person
                    }

                    val activeGlowColor by animateColorAsState(
                        targetValue = if (isSelected) theme.accentPrimary.copy(alpha = 0.14f) else Color.Transparent,
                        animationSpec = tween(200),
                        label = "activeGlow"
                    )

                    val activeContentColor by animateColorAsState(
                        targetValue = if (isSelected) theme.textPrimary else theme.textSecondary,
                        animationSpec = tween(200),
                        label = "activeContent"
                    )

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .background(activeGlowColor)
                            .springPressEffect(
                                hapticEnabled = hapticEnabled,
                                scaleTarget = 0.94f,
                                onClick = { onTabSelected(tab) }
                            )
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                            .testTag("nav_${tab.name.lowercase()}"),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = tab.title,
                                tint = activeContentColor,
                                modifier = Modifier.size(21.dp)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = tab.title,
                                style = TextStyle(
                                    color = activeContentColor,
                                    fontSize = 10.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}
