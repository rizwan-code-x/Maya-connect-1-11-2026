package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Launch
import androidx.compose.material.icons.filled.MicNone
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import com.example.data.model.AppItem
import com.example.ui.theme.MayaColorFavorite
import com.example.ui.theme.MayaTheme

// Points 31-34: Side-by-side Chat Assistant & Voice Assistant Cards
@Composable
fun AssistantCard(
    title: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    hapticEnabled: Boolean = true
) {
    val theme = MayaTheme.colors

    Box(
        modifier = modifier
            .springPressEffect(
                hapticEnabled = hapticEnabled,
                scaleTarget = 0.95f,
                onClick = onClick
            )
            .clip(RoundedCornerShape(16.dp))
            .background(theme.surfaceGlass)
            .border(1.dp, theme.borderGlass, RoundedCornerShape(16.dp))
            .padding(14.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(theme.accentPrimary.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = theme.accentPrimary,
                    modifier = Modifier.size(17.dp)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = title,
                style = TextStyle(
                    color = theme.textPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = "Under Construction",
                style = TextStyle(
                    color = theme.textSecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                ),
                maxLines = 1
            )
        }
    }
}

// Points 35-42: Transparent Three-Color Digital Ecosystem Card
@Composable
fun MainDigitalEcosystemCard(
    onCardClick: () -> Unit,
    modifier: Modifier = Modifier,
    hapticEnabled: Boolean = true
) {
    val theme = MayaTheme.colors

    val cardBorderBrush = Brush.linearGradient(
        colors = listOf(
            theme.accentPrimary.copy(alpha = 0.55f),
            theme.accentSecondary.copy(alpha = 0.35f),
            theme.softHighlight.copy(alpha = 0.20f)
        )
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(theme.cardBackground)
            .border(1.dp, cardBorderBrush, RoundedCornerShape(22.dp))
            .springPressEffect(
                hapticEnabled = hapticEnabled,
                scaleTarget = 0.97f,
                onClick = onCardClick
            )
            .padding(20.dp)
            .testTag("digital_ecosystem_card")
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Your Digital Ecosystem",
                    style = TextStyle(
                        color = theme.textPrimary,
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.3).sp
                    )
                )

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(theme.accentPrimary.copy(alpha = 0.15f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "EXPLORE →",
                        style = TextStyle(
                            color = theme.accentPrimary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Unified workspace hub connecting REXLIFY, NeuraX, NooraX, SIFRA, and RODP in one interconnected environment. Tap to view ecosystem overview & official portal.",
                style = TextStyle(
                    color = theme.textSecondary,
                    fontSize = 12.5.sp,
                    lineHeight = 17.5.sp
                )
            )
        }
    }
}

@Composable
fun QuickAccessAppChip(
    app: AppItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    hapticEnabled: Boolean = true
) {
    val theme = MayaTheme.colors

    Box(
        modifier = modifier
            .springPressEffect(
                hapticEnabled = hapticEnabled,
                scaleTarget = 0.94f,
                onClick = onClick
            )
            .glassmorphic(
                shape = RoundedCornerShape(14.dp),
                backgroundColor = theme.surfaceGlass,
                borderColor = theme.borderGlass,
                borderWidth = 1.dp
            )
            .padding(horizontal = 12.dp, vertical = 9.dp)
            .testTag("quick_chip_${app.id}")
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            AppLogoIcon(app = app, size = 28.dp, cornerRadius = 8.dp)
            Spacer(modifier = Modifier.width(9.dp))
            Column {
                Text(
                    text = app.name,
                    style = TextStyle(
                        color = theme.textPrimary,
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = app.category.displayName,
                    style = TextStyle(
                        color = theme.textSecondary,
                        fontSize = 9.5.sp
                    ),
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
fun StandardAppCard(
    app: AppItem,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onFavoriteToggle: () -> Unit,
    modifier: Modifier = Modifier,
    hapticEnabled: Boolean = true
) {
    val theme = MayaTheme.colors
    val starColor by animateColorAsState(
        targetValue = if (app.isFavorite) MayaColorFavorite else theme.textSecondary.copy(alpha = 0.35f),
        animationSpec = tween(180),
        label = "starColor"
    )

    GlassCard(
        modifier = modifier.testTag("app_card_${app.id}"),
        cornerRadius = 18,
        backgroundColor = theme.surfaceGlass,
        borderColor = theme.borderGlass,
        borderWidth = 1,
        hapticEnabled = hapticEnabled,
        onLongClick = onLongClick,
        onClick = onClick
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // Top: App Icon + Status + Favorite Star
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                AppLogoIcon(app = app, size = 42.dp, cornerRadius = 11.dp)

                Row(verticalAlignment = Alignment.CenterVertically) {
                    LiveStatusBadge(status = app.status)
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .size(26.dp)
                            .clip(CircleShape)
                            .clickable { onFavoriteToggle() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (app.isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
                            contentDescription = if (app.isFavorite) "Favorited" else "Favorite",
                            tint = starColor,
                            modifier = Modifier.size(17.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // App Name & Tagline
            Text(
                text = app.name,
                style = TextStyle(
                    color = theme.textPrimary,
                    fontSize = 14.5.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.2).sp
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(3.dp))

            Text(
                text = app.tagline,
                style = TextStyle(
                    color = theme.textSecondary,
                    fontSize = 11.sp,
                    lineHeight = 14.5.sp
                ),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Bottom: Category & Open action
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = app.category.displayName,
                    style = TextStyle(
                        color = theme.textSecondary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(7.dp))
                        .background(theme.accentPrimary.copy(alpha = 0.12f))
                        .padding(horizontal = 7.dp, vertical = 3.5.dp)
                ) {
                    Text(
                        text = "Open",
                        style = TextStyle(
                            color = theme.textPrimary,
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = null,
                        tint = theme.textPrimary,
                        modifier = Modifier.size(10.dp)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppActionSheet(
    app: AppItem,
    onDismiss: () -> Unit,
    onOpenApp: () -> Unit,
    onToggleFavorite: () -> Unit,
    onTogglePinHome: () -> Unit,
    onViewDetails: () -> Unit,
    onEditApp: () -> Unit,
    onDeleteApp: (() -> Unit)? = null
) {
    val theme = MayaTheme.colors
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = if (theme.isDark) theme.surface else Color.White,
        contentColor = theme.textPrimary,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 22.dp)
                .padding(bottom = 32.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AppLogoIcon(app = app, size = 46.dp, cornerRadius = 12.dp)
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = app.name,
                            style = TextStyle(
                                color = theme.textPrimary,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        LiveStatusBadge(status = app.status)
                    }
                    Text(
                        text = app.tagline,
                        style = TextStyle(
                            color = theme.textSecondary,
                            fontSize = 11.5.sp
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .clip(CircleShape)
                        .background(theme.surfaceGlass)
                        .clickable { onDismiss() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = theme.textPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Action Items
            ActionSheetItem(
                icon = Icons.Default.Launch,
                title = "Open Application",
                subtitle = "Launch in Maya Connect Workspace",
                accentColor = theme.accentPrimary,
                onClick = {
                    onDismiss()
                    onOpenApp()
                }
            )

            ActionSheetItem(
                icon = if (app.isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
                title = if (app.isFavorite) "Remove from Favorites" else "Add to Favorites",
                subtitle = if (app.isFavorite) "Remove priority pin" else "Pin on top and home",
                accentColor = MayaColorFavorite,
                onClick = {
                    onToggleFavorite()
                    onDismiss()
                }
            )

            ActionSheetItem(
                icon = if (app.isPinnedHome) Icons.Default.PushPin else Icons.Outlined.PushPin,
                title = if (app.isPinnedHome) "Unpin from Home" else "Pin to Home",
                subtitle = "Manage Home screen presence",
                accentColor = theme.accentSecondary,
                onClick = {
                    onTogglePinHome()
                    onDismiss()
                }
            )

            ActionSheetItem(
                icon = Icons.Default.Info,
                title = "App Details",
                subtitle = "View application specifications & endpoint",
                accentColor = theme.textSecondary,
                onClick = {
                    onDismiss()
                    onViewDetails()
                }
            )

            ActionSheetItem(
                icon = Icons.Default.Edit,
                title = "Configure Endpoint",
                subtitle = "Modify application URL or name",
                accentColor = theme.accentPrimary,
                onClick = {
                    onDismiss()
                    onEditApp()
                }
            )

            if (app.isCustom && onDeleteApp != null) {
                ActionSheetItem(
                    icon = Icons.Default.Delete,
                    title = "Delete Custom App",
                    subtitle = "Remove app from registry",
                    accentColor = Color(0xFFEF4444),
                    onClick = {
                        onDismiss()
                        onDeleteApp()
                    }
                )
            }
        }
    }
}

@Composable
private fun ActionSheetItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    accentColor: Color,
    onClick: () -> Unit
) {
    val theme = MayaTheme.colors

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(horizontal = 10.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(RoundedCornerShape(9.dp))
                .background(accentColor.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = accentColor,
                modifier = Modifier.size(18.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = TextStyle(
                    color = theme.textPrimary,
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.SemiBold
                )
            )
            Text(
                text = subtitle,
                style = TextStyle(
                    color = theme.textSecondary,
                    fontSize = 11.sp
                )
            )
        }

        Icon(
            imageVector = Icons.Default.ArrowForward,
            contentDescription = null,
            tint = theme.textSecondary.copy(alpha = 0.35f),
            modifier = Modifier.size(15.dp)
        )
    }
}
