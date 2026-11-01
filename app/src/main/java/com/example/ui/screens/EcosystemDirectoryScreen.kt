package com.example.ui.screens

import android.content.Intent
import android.net.Uri
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
import androidx.compose.material.icons.automirrored.filled.Launch
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.EcosystemLinkEntity
import com.example.data.model.AppItem
import com.example.ui.components.glassmorphic
import com.example.ui.components.rememberChromeSmoothFlingBehavior
import com.example.ui.theme.MayaTheme
import com.example.ui.viewmodel.MayaViewModel

/**
 * Point 7: Official "Our Ecosystem" Directory displaying configured portals,
 * REXLIFY hubs, links, and direct opening.
 */
@Composable
fun EcosystemDirectoryScreen(
    viewModel: MayaViewModel,
    onClose: () -> Unit,
    onOpenAdmin: () -> Unit = {},
    onLaunchUrl: (url: String, title: String) -> Unit
) {
    val theme = MayaTheme.colors
    val context = LocalContext.current
    val links by viewModel.allEcosystemLinks.collectAsStateWithLifecycle()

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
                    text = "Our Ecosystem Directory",
                    style = TextStyle(
                        color = theme.textPrimary,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
                Text(
                    text = "Official REXLIFY & Maya Connect Nodes",
                    style = TextStyle(
                        color = theme.accentPrimary,
                        fontSize = 11.sp
                    )
                )
            }

            IconButton(
                onClick = onOpenAdmin,
                modifier = Modifier
                    .clip(CircleShape)
                    .background(theme.surfaceGlass)
                    .border(1.dp, theme.borderGlass, CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.AdminPanelSettings,
                    contentDescription = "Admin Management",
                    tint = theme.textPrimary
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 2. Verified Banner
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .glassmorphic(
                    shape = RoundedCornerShape(20.dp),
                    backgroundColor = theme.surfaceGlass,
                    borderColor = Color(0x3310B981),
                    borderWidth = 1.dp
                )
                .padding(14.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color(0x2210B981)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Verified,
                        contentDescription = null,
                        tint = Color(0xFF10B981),
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Official Ecosystem Directory",
                        style = TextStyle(
                            color = theme.textPrimary,
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Text(
                        text = "All links verified and maintained by founder Rizwan RR.",
                        style = TextStyle(
                            color = theme.textSecondary,
                            fontSize = 11.sp
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 3. Links List
        LazyColumn(
            modifier = Modifier.weight(1f),
            flingBehavior = rememberChromeSmoothFlingBehavior(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(links, key = { it.id }) { link ->
                EcosystemLinkCard(
                    link = link,
                    onClick = {
                        if (link.url.startsWith("http://") || link.url.startsWith("https://")) {
                            onLaunchUrl(link.url, link.name)
                        } else {
                            try {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(link.url))
                                context.startActivity(intent)
                            } catch (_: Exception) {}
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun EcosystemLinkCard(
    link: EcosystemLinkEntity,
    onClick: () -> Unit
) {
    val theme = MayaTheme.colors

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(theme.surfaceGlass)
            .border(1.dp, theme.borderGlass, RoundedCornerShape(18.dp))
            .clickable { onClick() }
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            Brush.linearGradient(
                                listOf(Color(0xFF0284C7), Color(0xFF7C3AED))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Language,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = link.name,
                            style = TextStyle(
                                color = theme.textPrimary,
                                fontSize = 14.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(theme.accentPrimary.copy(alpha = 0.15f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = link.badge,
                                style = TextStyle(
                                    color = theme.accentPrimary,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = link.description,
                        style = TextStyle(
                            color = theme.textSecondary,
                            fontSize = 11.5.sp
                        ),
                        maxLines = 2
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Icon(
                imageVector = Icons.AutoMirrored.Filled.Launch,
                contentDescription = "Open",
                tint = theme.accentPrimary,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}
