package com.example.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.layout.width
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.example.R
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.TouchApp
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.NoteEntity
import com.example.data.model.AppItem
import com.example.ui.theme.MayaTheme
import com.example.ui.viewmodel.MusicTrack
import java.util.Calendar
import kotlinx.coroutines.delay

/**
 * Functional Executive Glass Dashboard Card.
 * Combines App Branding on the left with an Interactive Analogue/Digital Clock on the right.
 * Long pressing or tapping 1s on the clock face opens the mini box selector with 5 digital clock styles.
 */
@Composable
fun RealDashboardCard(
    userName: String,
    recentlyUsedApps: List<AppItem>,
    latestNote: NoteEntity?,
    unreadNotificationsCount: Int,
    currentMusicTrack: MusicTrack? = null,
    isMusicPlaying: Boolean = false,
    onMusicPlayPauseToggle: () -> Unit = {},
    onMusicNext: () -> Unit = {},
    onAppClick: (AppItem) -> Unit,
    onOpenNotepad: () -> Unit,
    onOpenMusicPlayer: () -> Unit,
    onOpenVoiceAssistant: () -> Unit,
    onOpenNotifications: () -> Unit,
    onOpenEcosystemDirectory: () -> Unit,
    currentClockStyle: DashboardClockStyle = DashboardClockStyle.ANALOGUE,
    onClockStyleSelected: (DashboardClockStyle) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val theme = MayaTheme.colors

    // Clock state
    var calendar by remember { mutableStateOf(Calendar.getInstance()) }
    var showClockPicker by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        while (true) {
            calendar = Calendar.getInstance()
            delay(1000)
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .glassmorphic(
                shape = RoundedCornerShape(26.dp),
                backgroundColor = theme.surfaceGlass,
                borderColor = theme.borderGlass,
                borderWidth = 1.dp
            )
            .padding(horizontal = 20.dp, vertical = 18.dp)
            .testTag("executive_dashboard_card")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // LEFT SIDE: Clean Maya OS Branding strictly as "Maya OS" and "Powered by REXLIFY"
            Column(
                modifier = Modifier
                    .weight(1.1f)
                    .padding(end = 12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                            .border(1.dp, theme.borderGlass, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.maya_connect_official_logo),
                            contentDescription = "Maya OS Official Logo",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Maya OS",
                            style = TextStyle(
                                color = theme.textPrimary,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = (-0.4).sp
                            )
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Powered by REXLIFY",
                            style = TextStyle(
                                color = theme.accentPrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            )
                        )
                    }
                }
            }

            // RIGHT SIDE: Seamless Clock / Music Display directly on Dashboard (no nested box, no hold pill)
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.weight(0.9f)
            ) {
                if (isMusicPlaying && currentMusicTrack != null) {
                    // Compact Music Player in the exact designated clock slot
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clickable { onOpenMusicPlayer() }
                            .padding(6.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Equalizer,
                                contentDescription = null,
                                tint = Color(0xFFF472B6),
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "PLAYING",
                                style = TextStyle(
                                    color = Color(0xFFF472B6),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.5.sp
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = currentMusicTrack.title,
                            style = TextStyle(
                                color = theme.textPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        Text(
                            text = currentMusicTrack.artist,
                            style = TextStyle(
                                color = theme.accentPrimary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(theme.accentPrimary)
                                    .clickable { onMusicPlayPauseToggle() },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Pause,
                                    contentDescription = "Pause",
                                    tint = Color.White,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(theme.surfaceGlass)
                                    .clickable { onMusicNext() },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.SkipNext,
                                    contentDescription = "Next",
                                    tint = theme.textPrimary,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }
                } else {
                    // Clock Component directly on dashboard (no inner box, no extra pill underneath)
                    Box(
                        modifier = Modifier
                            .pointerInput(Unit) {
                                detectTapGestures(
                                    onLongPress = { showClockPicker = true },
                                    onTap = { showClockPicker = true }
                                )
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        when (currentClockStyle) {
                            DashboardClockStyle.ANALOGUE -> {
                                AnalogueWatchFace(
                                    calendar = calendar,
                                    accentColor = theme.accentPrimary,
                                    isDark = theme.isDark,
                                    sizeDp = 110
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
                    }
                }
            }
        }
    }

    // Mini Box Dialog for 5 Digital Clock Styles Carousel
    if (showClockPicker) {
        ClockPickerMiniBox(
            currentStyle = currentClockStyle,
            onStyleSelected = {
                onClockStyleSelected(it)
                showClockPicker = false
            },
            onDismiss = { showClockPicker = false }
        )
    }
}
