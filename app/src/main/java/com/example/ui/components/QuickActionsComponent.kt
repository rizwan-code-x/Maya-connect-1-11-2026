package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Icon
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
import com.example.ui.theme.MayaTheme
import com.example.ui.theme.MayaThemeMode

data class QuickActionTile(
    val id: String,
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val isActive: Boolean,
    val primaryColor: Color,
    val onClick: () -> Unit
)

/**
 * Quick Actions Component
 * Android OS Quick Settings styled grid displaying frequently used system toggles and app shortcuts.
 */
@Composable
fun QuickActionsComponent(
    isMusicPlaying: Boolean = false,
    currentThemeMode: MayaThemeMode = MayaThemeMode.DARK,
    onToggleTheme: () -> Unit = {},
    onOpenMusicPlayer: () -> Unit = {},
    onOpenJioSphere: () -> Unit = {},
    onOpenSifra: () -> Unit = {},
    onOpenNotepad: () -> Unit = {},
    onOpenMaps: () -> Unit = {},
    onOpenAppDrawer: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val theme = MayaTheme.colors
    val tiles = listOf(
        QuickActionTile(
            id = "tile_theme",
            title = "Theme",
            subtitle = if (currentThemeMode == MayaThemeMode.DARK) "Dark Mode" else "Light Mode",
            icon = if (currentThemeMode == MayaThemeMode.DARK) Icons.Default.DarkMode else Icons.Default.LightMode,
            isActive = currentThemeMode == MayaThemeMode.DARK,
            primaryColor = Color(0xFF6366F1),
            onClick = onToggleTheme
        ),
        QuickActionTile(
            id = "tile_jiosphere",
            title = "JioSphere",
            subtitle = "Browser",
            icon = Icons.Default.Language,
            isActive = true,
            primaryColor = Color(0xFF0078FF),
            onClick = onOpenJioSphere
        ),
        QuickActionTile(
            id = "tile_sifra",
            title = "SIFRA AI",
            subtitle = "Intelligence",
            icon = Icons.Default.AutoAwesome,
            isActive = true,
            primaryColor = Color(0xFF38BDF8),
            onClick = onOpenSifra
        ),
        QuickActionTile(
            id = "tile_notepad",
            title = "Notepad",
            subtitle = "Quick Note",
            icon = Icons.Default.EditNote,
            isActive = true,
            primaryColor = Color(0xFFF59E0B),
            onClick = onOpenNotepad
        ),
        QuickActionTile(
            id = "tile_maps",
            title = "Google Maps",
            subtitle = "Navigation",
            icon = Icons.Default.Map,
            isActive = true,
            primaryColor = Color(0xFF10B981),
            onClick = onOpenMaps
        ),
        QuickActionTile(
            id = "tile_app_drawer",
            title = "All Apps",
            subtitle = "App Drawer",
            icon = Icons.Default.Apps,
            isActive = true,
            primaryColor = Color(0xFFEC4899),
            onClick = onOpenAppDrawer
        )
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("quick_actions_component")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 2.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Quick Actions",
                style = TextStyle(
                    color = theme.textPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.2).sp
                )
            )

            Text(
                text = "Android OS Grid",
                style = TextStyle(
                    color = theme.accentPrimary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // 4-column compact grid of Android OS styled tiles
        LazyVerticalGrid(
            columns = GridCells.Fixed(4),
            modifier = Modifier
                .fillMaxWidth()
                .height(168.dp),
            flingBehavior = rememberChromeSmoothFlingBehavior(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            userScrollEnabled = false
        ) {
            items(items = tiles, key = { it.id }) { tile ->
                QuickActionCard(tile = tile)
            }
        }
    }
}

@Composable
private fun QuickActionCard(tile: QuickActionTile) {
    val theme = MayaTheme.colors

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(78.dp)
            .clip(RoundedCornerShape(16.dp))
            .glassmorphic(
                shape = RoundedCornerShape(16.dp),
                backgroundColor = if (tile.isActive) {
                    tile.primaryColor.copy(alpha = if (theme.isDark) 0.22f else 0.16f)
                } else {
                    theme.surfaceGlass
                },
                borderColor = if (tile.isActive) {
                    tile.primaryColor.copy(alpha = 0.45f)
                } else {
                    theme.borderGlass
                },
                borderWidth = 1.dp
            )
            .clickable { tile.onClick() }
            .padding(8.dp)
            .testTag(tile.id),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(30.dp)
                    .clip(CircleShape)
                    .background(
                        if (tile.isActive) {
                            tile.primaryColor.copy(alpha = 0.25f)
                        } else {
                            theme.textSecondary.copy(alpha = 0.1f)
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = tile.icon,
                    contentDescription = tile.title,
                    tint = if (tile.isActive) tile.primaryColor else theme.textSecondary,
                    modifier = Modifier.size(17.dp)
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = tile.title,
                style = TextStyle(
                    color = theme.textPrimary,
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.Bold
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = tile.subtitle,
                style = TextStyle(
                    color = if (tile.isActive) tile.primaryColor else theme.textSecondary,
                    fontSize = 8.5.sp,
                    fontWeight = FontWeight.Medium
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
