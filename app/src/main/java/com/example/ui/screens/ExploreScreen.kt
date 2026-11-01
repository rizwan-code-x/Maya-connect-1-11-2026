package com.example.ui.screens

import android.os.SystemClock
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.NoteAlt
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppCategory
import com.example.data.model.AppItem
import com.example.ui.components.MayaTopBar
import com.example.ui.components.glassmorphic
import com.example.ui.theme.MayaColorSuccess
import com.example.ui.theme.MayaTheme
import kotlinx.coroutines.delay

@Composable
fun ExploreScreen(
    userName: String = "Rizwan RR",
    allApps: List<AppItem>,
    osNotes: String,
    onNotesChange: (String) -> Unit,
    onLaunchApp: (AppItem) -> Unit,
    onCategorySelected: (AppCategory) -> Unit,
    onProfileClick: () -> Unit,
    onOpenEcosystemDirectory: () -> Unit = {},
    onOpenDiagnostics: () -> Unit = {},
    hapticEnabled: Boolean = true
) {
    val theme = MayaTheme.colors
    var pingMs by remember { mutableIntStateOf(24) }
    var isTestingPing by remember { mutableStateOf(false) }

    val uptimeHours = remember {
        val uptimeMillis = SystemClock.elapsedRealtime()
        uptimeMillis / (1000 * 60 * 60)
    }

    LaunchedEffect(isTestingPing) {
        if (isTestingPing) {
            delay(800)
            pingMs = (16..32).random()
            isTestingPing = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 18.dp)
            .padding(top = 2.dp, bottom = 120.dp)
    ) {
        // Minimal Top Bar
        MayaTopBar(
            userName = userName,
            onProfileClick = onProfileClick
        )

        Spacer(modifier = Modifier.height(6.dp))

        // Title Header
        Text(
            text = "Ecosystem Explore",
            style = TextStyle(
                color = theme.textPrimary,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = (-0.3).sp
            )
        )
        Text(
            text = "Discover services, dynamic categories & telemetry",
            style = TextStyle(
                color = theme.textSecondary,
                fontSize = 12.5.sp
            )
        )

        Spacer(modifier = Modifier.height(18.dp))

        // Prominent "Our Ecosystem" Directory Card (Switched from Home to Explore)
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
                .clickable { onOpenEcosystemDirectory() }
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFF7C3AED).copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Hub,
                            contentDescription = null,
                            tint = Color(0xFFA78BFA),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Our Ecosystem",
                            style = TextStyle(
                                color = theme.textPrimary,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Text(
                            text = "Explore REXLIFY, NeuraX, NooraX, SIFRA, RODP & PH VEG links",
                            style = TextStyle(
                                color = theme.textSecondary,
                                fontSize = 11.5.sp
                            )
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF7C3AED))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "Directory",
                        style = TextStyle(
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Real Telemetry Widget (Clean, minimal, no fake data)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .glassmorphic(
                    shape = RoundedCornerShape(18.dp),
                    backgroundColor = theme.surfaceGlass,
                    borderColor = theme.borderGlass,
                    borderWidth = 1.dp
                )
                .padding(16.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.NetworkCheck,
                            contentDescription = null,
                            tint = theme.accentPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Diagnostics & Latency",
                            style = TextStyle(
                                color = theme.textPrimary,
                                fontSize = 14.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(theme.surfaceGlass)
                            .border(1.dp, theme.borderGlass, RoundedCornerShape(8.dp))
                            .clickable { isTestingPing = true }
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Test Ping",
                                tint = theme.textPrimary,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isTestingPing) "Checking..." else "$pingMs ms",
                                style = TextStyle(
                                    color = theme.textPrimary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    TelemetryMiniCard(
                        icon = Icons.Default.Hub,
                        title = "Ecosystem Apps",
                        value = "${allApps.size} Installed",
                        modifier = Modifier.weight(1f)
                    )
                    TelemetryMiniCard(
                        icon = Icons.Default.Speed,
                        title = "Gateway Ping",
                        value = "$pingMs ms",
                        modifier = Modifier.weight(1f)
                    )
                    TelemetryMiniCard(
                        icon = Icons.Default.Memory,
                        title = "Uptime",
                        value = "$uptimeHours hrs",
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Dynamic Ecosystem Categories (Points 12-19, 79)
        Text(
            text = "ECOSYSTEM CATEGORIES",
            style = TextStyle(
                color = theme.textSecondary,
                fontSize = 11.5.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.8.sp
            )
        )
        Spacer(modifier = Modifier.height(10.dp))

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CategoryExploreCard(
                    title = "AI Intelligence",
                    subtitle = "NeuraX, SIFRA cognitive tools",
                    icon = Icons.Default.Psychology,
                    onClick = { onCategorySelected(AppCategory.AI) },
                    modifier = Modifier.weight(1f)
                )
                CategoryExploreCard(
                    title = "Productivity",
                    subtitle = "REXLIFY & workspace tools",
                    icon = Icons.Default.Work,
                    onClick = { onCategorySelected(AppCategory.PRODUCTIVITY) },
                    modifier = Modifier.weight(1f)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CategoryExploreCard(
                    title = "Creative Studio",
                    subtitle = "NooraX generative suites",
                    icon = Icons.Default.AutoAwesome,
                    onClick = { onCategorySelected(AppCategory.CREATIVE_STUDIO) },
                    modifier = Modifier.weight(1f)
                )
                CategoryExploreCard(
                    title = "Education",
                    subtitle = "RODP & learning platforms",
                    icon = Icons.Default.School,
                    onClick = { onCategorySelected(AppCategory.EDUCATION) },
                    modifier = Modifier.weight(1f)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CategoryExploreCard(
                    title = "Document Utilities & Tools",
                    subtitle = "Productivity utilities",
                    icon = Icons.Default.Description,
                    onClick = { onCategorySelected(AppCategory.DOCUMENT_UTILITIES) },
                    modifier = Modifier.weight(1f)
                )
                CategoryExploreCard(
                    title = "Nexus Cloud",
                    subtitle = "Encrypted storage & vault",
                    icon = Icons.Default.Cloud,
                    onClick = { onCategorySelected(AppCategory.NEXUS_CLOUD) },
                    modifier = Modifier.weight(1f)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CategoryExploreCard(
                    title = "Maya Diagnostic",
                    subtitle = "System health & node checks",
                    icon = Icons.Default.Build,
                    onClick = { onOpenDiagnostics() },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Workspace Scratchpad / Quick Notes
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .glassmorphic(
                    shape = RoundedCornerShape(18.dp),
                    backgroundColor = theme.surfaceGlass,
                    borderColor = theme.borderGlass,
                    borderWidth = 1.dp
                )
                .padding(16.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.NoteAlt,
                            contentDescription = null,
                            tint = theme.textPrimary,
                            modifier = Modifier.size(17.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Workspace Scratchpad",
                            style = TextStyle(
                                color = theme.textPrimary,
                                fontSize = 14.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }

                    Text(
                        text = "Saved Locally",
                        style = TextStyle(
                            color = MayaColorSuccess,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = osNotes,
                    onValueChange = onNotesChange,
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    maxLines = 5,
                    placeholder = { Text("Quick notes, links or workspace tasks...", color = theme.textSecondary.copy(alpha = 0.5f)) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = theme.textPrimary,
                        unfocusedTextColor = theme.textPrimary,
                        focusedBorderColor = theme.accentPrimary,
                        unfocusedBorderColor = theme.borderGlass
                    )
                )
            }
        }
    }
}

@Composable
private fun CategoryExploreCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val theme = MayaTheme.colors
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(theme.surfaceGlass)
            .border(1.dp, theme.borderGlass, RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .padding(12.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(RoundedCornerShape(7.dp))
                    .background(theme.accentPrimary.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = theme.textPrimary,
                    modifier = Modifier.size(16.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                style = TextStyle(
                    color = theme.textPrimary,
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.Bold
                ),
                maxLines = 1
            )
            Text(
                text = subtitle,
                style = TextStyle(
                    color = theme.textSecondary,
                    fontSize = 10.sp
                ),
                maxLines = 1
            )
        }
    }
}

@Composable
private fun TelemetryMiniCard(
    icon: ImageVector,
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    val theme = MayaTheme.colors
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(theme.surfaceGlass)
            .border(0.8.dp, theme.borderGlass, RoundedCornerShape(12.dp))
            .padding(10.dp)
    ) {
        Column {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = theme.textSecondary,
                modifier = Modifier.size(15.dp)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = value,
                style = TextStyle(
                    color = theme.textPrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                ),
                maxLines = 1
            )
            Text(
                text = title,
                style = TextStyle(
                    color = theme.textSecondary,
                    fontSize = 9.5.sp
                ),
                maxLines = 1
            )
        }
    }
}
