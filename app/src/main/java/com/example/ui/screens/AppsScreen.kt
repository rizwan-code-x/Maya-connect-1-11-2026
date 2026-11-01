package com.example.ui.screens

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppCategory
import com.example.data.model.AppItem
import com.example.data.model.SearchHelper
import com.example.ui.components.GlassSearchBar
import com.example.ui.components.MayaTopBar
import com.example.ui.components.StandardAppCard
import com.example.ui.components.rememberChromeSmoothFlingBehavior
import com.example.ui.theme.MayaTheme

@Composable
fun AppsScreen(
    userName: String = "Rizwan RR",
    allApps: List<AppItem>,
    searchQuery: String,
    selectedCategory: AppCategory,
    onSearchQueryChange: (String) -> Unit,
    onCategorySelected: (AppCategory) -> Unit,
    onAppClick: (AppItem) -> Unit,
    onAppLongClick: (AppItem) -> Unit,
    onFavoriteToggle: (AppItem) -> Unit,
    onAddCustomAppClick: () -> Unit,
    onProfileClick: () -> Unit,
    hapticEnabled: Boolean = true
) {
    val theme = MayaTheme.colors

    val filteredApps = androidx.compose.runtime.remember(allApps, selectedCategory, searchQuery) {
        allApps.filter { app ->
            val matchesCategory = selectedCategory == AppCategory.ALL || app.category == selectedCategory
            matchesCategory && SearchHelper.matchesQuery(app, searchQuery)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.fillMaxSize(),
            flingBehavior = rememberChromeSmoothFlingBehavior(),
            contentPadding = PaddingValues(start = 18.dp, end = 18.dp, top = 2.dp, bottom = 120.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Minimal Header
            item(span = { GridItemSpan(2) }) {
                MayaTopBar(
                    userName = userName,
                    onProfileClick = onProfileClick
                )
            }

            // Title & Add Action
            item(span = { GridItemSpan(2) }) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Ecosystem Hub",
                            style = TextStyle(
                                color = theme.textPrimary,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = (-0.3).sp
                            )
                        )
                        Text(
                            text = "Browse and launch digital ecosystem applications",
                            style = TextStyle(
                                color = theme.textSecondary,
                                fontSize = 12.5.sp
                            )
                        )
                    }

                    // Add Custom App Button
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(theme.surfaceGlass)
                            .border(1.dp, theme.borderGlass, RoundedCornerShape(10.dp))
                            .clickable { onAddCustomAppClick() }
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Add",
                                tint = theme.textPrimary,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "New App",
                                style = TextStyle(
                                    color = theme.textPrimary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }
                }
            }

            // Search input
            item(span = { GridItemSpan(2) }) {
                GlassSearchBar(
                    query = searchQuery,
                    onQueryChange = onSearchQueryChange,
                    placeholder = "Search ${allApps.size} apps and tools..."
                )
            }

            // Clean 7 Categories Horizontal Tabs (Points 12-19)
            item(span = { GridItemSpan(2) }) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AppCategory.entries.forEach { cat ->
                        val isSelected = cat == selectedCategory
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) theme.accentPrimary else theme.surfaceGlass)
                                .border(
                                    1.dp,
                                    if (isSelected) theme.accentPrimary else theme.borderGlass,
                                    RoundedCornerShape(12.dp)
                                )
                                .clickable { onCategorySelected(cat) }
                                .padding(horizontal = 12.dp, vertical = 7.dp)
                        ) {
                            Text(
                                text = cat.displayName,
                                style = TextStyle(
                                    color = if (isSelected) {
                                        if (theme.isDark) Color.Black else Color.White
                                    } else theme.textSecondary,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                )
                            )
                        }
                    }
                }
            }

            // Category Apps Count
            item(span = { GridItemSpan(2) }) {
                Text(
                    text = if (selectedCategory == AppCategory.ALL) "Applications (${filteredApps.size})" else "${selectedCategory.displayName} (${filteredApps.size})",
                    style = TextStyle(
                        color = theme.textPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    modifier = Modifier.padding(top = 4.dp, start = 2.dp)
                )
            }

            // App Cards Grid
            items(items = filteredApps, key = { it.id }) { app ->
                StandardAppCard(
                    app = app,
                    onClick = { onAppClick(app) },
                    onLongClick = { onAppLongClick(app) },
                    onFavoriteToggle = { onFavoriteToggle(app) },
                    hapticEnabled = hapticEnabled
                )
            }

            if (filteredApps.isEmpty()) {
                item(span = { GridItemSpan(2) }) {
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
                                imageVector = Icons.Default.SearchOff,
                                contentDescription = null,
                                tint = theme.textSecondary,
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "No applications found",
                                style = TextStyle(
                                    color = theme.textPrimary,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}
