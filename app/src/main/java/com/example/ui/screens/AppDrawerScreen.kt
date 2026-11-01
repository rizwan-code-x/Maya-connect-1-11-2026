package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SearchOff
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppCategory
import com.example.data.model.AppItem
import com.example.data.model.SearchHelper
import com.example.ui.components.AppLogoIcon
import com.example.ui.components.PersistentStatusBar
import com.example.ui.components.glassmorphic
import com.example.ui.components.rememberChromeSmoothFlingBehavior
import com.example.ui.components.springPressEffect
import com.example.ui.theme.MayaTheme

/**
 * AppDrawerScreen
 * High-performance App Drawer housing all installed applications using LazyVerticalGrid,
 * category filter tabs, fast search input, and responsive glass styling.
 */
@Composable
fun AppDrawerScreen(
    allApps: List<AppItem>,
    onClose: () -> Unit,
    onAppClick: (AppItem) -> Unit,
    onAppLongClick: (AppItem) -> Unit = {},
    hapticEnabled: Boolean = true
) {
    BackHandler { onClose() }

    val theme = MayaTheme.colors
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(AppCategory.ALL) }

    val filteredApps = remember(allApps, searchQuery, selectedCategory) {
        allApps.filter { app ->
            val matchesCategory = selectedCategory == AppCategory.ALL || app.category == selectedCategory
            matchesCategory && SearchHelper.matchesQuery(app, searchQuery)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(if (theme.isDark) Color(0xF80B0F17) else Color(0xFAF8FAFC))
            .statusBarsPadding()
            .navigationBarsPadding()
            .testTag("app_drawer_screen")
    ) {
        Column(modifier = Modifier.fillMaxSize()) {

            // Top Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(theme.accentPrimary.copy(alpha = 0.18f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Apps,
                            contentDescription = null,
                            tint = theme.accentPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "App Drawer",
                            style = TextStyle(
                                color = theme.textPrimary,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = (-0.3).sp
                            )
                        )
                        Text(
                            text = "${filteredApps.size} of ${allApps.size} Applications",
                            style = TextStyle(
                                color = theme.textSecondary,
                                fontSize = 11.sp
                            )
                        )
                    }
                }

                // Close Button
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(theme.surfaceGlass)
                        .border(1.dp, theme.borderGlass, CircleShape)
                        .clickable { onClose() }
                        .testTag("app_drawer_close_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close App Drawer",
                        tint = theme.textPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            // Global Search Bar at top of App Drawer
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .testTag("global_app_drawer_search_bar")
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(22.dp))
                            .glassmorphic(
                                shape = RoundedCornerShape(22.dp),
                                backgroundColor = if (theme.isDark) Color(0xD9171C26) else Color(0xF2FFFFFF),
                                borderColor = if (searchQuery.isNotEmpty()) theme.accentPrimary else theme.borderGlass,
                                borderWidth = if (searchQuery.isNotEmpty()) 1.5.dp else 1.dp
                            )
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(theme.accentPrimary.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search",
                                tint = theme.accentPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        BasicTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            modifier = Modifier.weight(1f).testTag("app_drawer_search_input"),
                            singleLine = true,
                            textStyle = TextStyle(
                                color = theme.textPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Normal
                            ),
                            cursorBrush = SolidColor(theme.accentPrimary),
                            decorationBox = { innerTextField ->
                                if (searchQuery.isEmpty()) {
                                    Text(
                                        text = "Search all installed applications by name...",
                                        style = TextStyle(
                                            color = theme.textSecondary.copy(alpha = 0.65f),
                                            fontSize = 13.5.sp
                                        )
                                    )
                                }
                                innerTextField()
                            }
                        )

                        if (searchQuery.isNotEmpty()) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Clear",
                                tint = theme.textSecondary,
                                modifier = Modifier
                                    .size(18.dp)
                                    .clickable { searchQuery = "" }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                        }

                        // App count pill
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(theme.accentPrimary.copy(alpha = 0.15f))
                                .padding(horizontal = 6.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = "${filteredApps.size}",
                                style = TextStyle(
                                    color = theme.accentPrimary,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }
                }
            }

            // Category Chips Carousel
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 18.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(
                    AppCategory.ALL,
                    AppCategory.AI,
                    AppCategory.PRODUCTIVITY,
                    AppCategory.CREATIVE_STUDIO,
                    AppCategory.EDUCATION,
                    AppCategory.DOCUMENT_UTILITIES,
                    AppCategory.NEXUS_CLOUD,
                    AppCategory.MAYA_DIAGNOSTIC
                ).forEach { cat ->
                    val isSelected = selectedCategory == cat
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (isSelected) theme.accentPrimary
                                else theme.surfaceGlass
                            )
                            .border(
                                1.dp,
                                if (isSelected) theme.accentPrimary else theme.borderGlass,
                                RoundedCornerShape(12.dp)
                            )
                            .clickable { selectedCategory = cat }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = cat.displayName,
                            style = TextStyle(
                                color = if (isSelected) Color.White else theme.textSecondary,
                                fontSize = 11.5.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // LazyVerticalGrid for all application icons
            if (filteredApps.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.SearchOff,
                            contentDescription = null,
                            tint = theme.textSecondary.copy(alpha = 0.5f),
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No apps found matching \"$searchQuery\"",
                            style = TextStyle(
                                color = theme.textSecondary,
                                fontSize = 13.5.sp,
                                textAlign = TextAlign.Center
                            )
                        )
                    }
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(4),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 14.dp),
                    flingBehavior = rememberChromeSmoothFlingBehavior(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(top = 8.dp, bottom = 40.dp)
                ) {
                    items(items = filteredApps, key = { "drawer_grid_" + it.id }) { app ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .springPressEffect(
                                    hapticEnabled = hapticEnabled,
                                    scaleTarget = 0.90f,
                                    onClick = {
                                        onAppClick(app)
                                        onClose()
                                    },
                                    onLongClick = { onAppLongClick(app) }
                                )
                                .padding(horizontal = 2.dp, vertical = 4.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            AppLogoIcon(
                                app = app,
                                size = 52.dp,
                                cornerRadius = 14.dp
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = app.name,
                                style = TextStyle(
                                    color = theme.textPrimary,
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Medium,
                                    textAlign = TextAlign.Center
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }
    }
}
