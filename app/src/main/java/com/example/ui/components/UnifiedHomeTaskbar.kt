package com.example.ui.components

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
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.FormatListBulleted
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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

private val GoldenAccent = Color(0xFFEAB308)
private val DarkGlass = Color(0xCC12121A)
private val GoldenBorder = Color(0x33EAB308)

/**
 * Unified Custom Bottom Taskbar Component for Maya Connect Home Screen.
 * Unifies REXLIFY Music, Notepad, and SIFRA Daily Task with golden-accented glassmorphic dock styling.
 */
@Composable
fun UnifiedHomeTaskbar(
    onOpenMusicPlayer: () -> Unit,
    onOpenNotepad: () -> Unit,
    onOpenDailyTask: () -> Unit,
    unreadTasksCount: Int = 0,
    modifier: Modifier = Modifier,
    hapticEnabled: Boolean = true
) {
    val theme = MayaTheme.colors

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(DarkGlass)
            .border(
                1.dp,
                Brush.horizontalGradient(
                    listOf(
                        GoldenBorder,
                        theme.borderGlass,
                        GoldenBorder
                    )
                ),
                RoundedCornerShape(22.dp)
            )
            .padding(horizontal = 12.dp, vertical = 10.dp)
            .testTag("unified_home_taskbar")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 1. REXLIFY Music
            TaskbarItem(
                title = "Music",
                subtitle = "REXLIFY Audio",
                icon = Icons.Default.Headphones,
                accentColor = Color(0xFF0284C7),
                onClick = onOpenMusicPlayer,
                modifier = Modifier.weight(1f),
                testTag = "taskbar_music_btn"
            )

            // 2. Notepad
            TaskbarItem(
                title = "Notepad",
                subtitle = "Quick Notes",
                icon = Icons.Default.EditNote,
                accentColor = Color(0xFF10B981),
                onClick = onOpenNotepad,
                modifier = Modifier.weight(1f),
                testTag = "taskbar_notepad_btn"
            )

            // 3. Daily Task (Golden SIFRA Accent)
            TaskbarItem(
                title = "Daily Task",
                subtitle = "SIFRA Neural",
                icon = Icons.Default.FormatListBulleted,
                accentColor = GoldenAccent,
                badgeCount = unreadTasksCount,
                onClick = onOpenDailyTask,
                modifier = Modifier.weight(1f),
                isHighlighted = true,
                testTag = "taskbar_daily_task_btn"
            )
        }
    }
}

@Composable
private fun TaskbarItem(
    title: String,
    subtitle: String,
    icon: ImageVector,
    accentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    badgeCount: Int = 0,
    isHighlighted: Boolean = false,
    testTag: String = ""
) {
    val theme = MayaTheme.colors

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(if (isHighlighted) GoldenAccent.copy(alpha = 0.12f) else theme.surfaceGlass)
            .border(
                1.dp,
                if (isHighlighted) GoldenAccent.copy(alpha = 0.4f) else theme.borderGlass,
                RoundedCornerShape(16.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 10.dp, vertical = 10.dp)
            .testTag(testTag)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start,
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(accentColor.copy(alpha = 0.2f))
                    .border(0.8.dp, accentColor.copy(alpha = 0.4f), RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = accentColor,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = TextStyle(
                        color = if (isHighlighted) GoldenAccent else theme.textPrimary,
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = subtitle,
                    style = TextStyle(
                        color = theme.textSecondary,
                        fontSize = 9.5.sp
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            if (badgeCount > 0) {
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(GoldenAccent)
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "$badgeCount",
                        style = TextStyle(
                            color = Color.Black,
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }
        }
    }
}
