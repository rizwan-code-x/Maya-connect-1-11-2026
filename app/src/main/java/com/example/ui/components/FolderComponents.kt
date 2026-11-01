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
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderOpen
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.AppFolder
import com.example.data.model.AppItem
import com.example.ui.theme.MayaTheme

/**
 * Folder Grid Item
 * Displays an app folder on the home screen with a 2x2 mini grid preview of the apps inside.
 */
@Composable
fun FolderGridItem(
    folder: AppFolder,
    apps: List<AppItem>,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isHoveredTarget: Boolean = false,
    hapticEnabled: Boolean = true
) {
    val theme = MayaTheme.colors
    val previewApps = apps.take(4)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .springPressEffect(
                hapticEnabled = hapticEnabled,
                scaleTarget = 0.92f,
                onClick = onClick
            )
            .padding(horizontal = 2.dp, vertical = 4.dp)
            .testTag("folder_item_${folder.id}"),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Folder Icon / Container (Squircle Glass Card with 2x2 Mini Apps)
        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(RoundedCornerShape(15.dp))
                .background(
                    if (isHoveredTarget) theme.accentPrimary.copy(alpha = 0.35f)
                    else if (theme.isDark) Color(0xD91E2433) else Color(0xF0E2E8F0)
                )
                .border(
                    if (isHoveredTarget) 2.dp else 1.dp,
                    if (isHoveredTarget) theme.accentPrimary else theme.borderGlass,
                    RoundedCornerShape(15.dp)
                )
                .padding(5.dp),
            contentAlignment = Alignment.Center
        ) {
            if (previewApps.isEmpty()) {
                Icon(
                    imageVector = Icons.Default.Folder,
                    contentDescription = folder.name,
                    tint = theme.accentPrimary,
                    modifier = Modifier.size(24.dp)
                )
            } else {
                // 2x2 Mini Grid
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Row(
                        modifier = Modifier.weight(1f).fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        previewApps.getOrNull(0)?.let { app ->
                            AppLogoIcon(app = app, size = 18.dp, cornerRadius = 5.dp)
                        } ?: Spacer(modifier = Modifier.weight(1f))

                        previewApps.getOrNull(1)?.let { app ->
                            AppLogoIcon(app = app, size = 18.dp, cornerRadius = 5.dp)
                        } ?: Spacer(modifier = Modifier.weight(1f))
                    }

                    Row(
                        modifier = Modifier.weight(1f).fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        previewApps.getOrNull(2)?.let { app ->
                            AppLogoIcon(app = app, size = 18.dp, cornerRadius = 5.dp)
                        } ?: Spacer(modifier = Modifier.weight(1f))

                        previewApps.getOrNull(3)?.let { app ->
                            AppLogoIcon(app = app, size = 18.dp, cornerRadius = 5.dp)
                        } ?: Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Folder Title
        Text(
            text = folder.name,
            style = TextStyle(
                color = theme.textPrimary,
                fontSize = 11.5.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center
            ),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        // Count Pill
        Text(
            text = "${apps.size} apps",
            style = TextStyle(
                color = theme.textSecondary.copy(alpha = 0.8f),
                fontSize = 9.sp,
                fontWeight = FontWeight.Medium
            )
        )
    }
}

/**
 * Folder Detail Dialog
 * Opens when a folder is tapped, displaying the apps in a clean grid with renaming & dissolving options.
 */
@Composable
fun FolderDetailDialog(
    folder: AppFolder,
    apps: List<AppItem>,
    onDismiss: () -> Unit,
    onAppClick: (AppItem) -> Unit,
    onRenameFolder: (String) -> Unit,
    onRemoveAppFromFolder: (AppItem) -> Unit,
    onDissolveFolder: () -> Unit,
    hapticEnabled: Boolean = true
) {
    val theme = MayaTheme.colors
    var isEditingName by remember { mutableStateOf(false) }
    var editedName by remember { mutableStateOf(folder.name) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .clip(RoundedCornerShape(26.dp))
                .background(if (theme.isDark) Color(0xF2121622) else Color(0xF8FFFFFF))
                .border(1.2.dp, theme.borderGlass, RoundedCornerShape(26.dp))
                .padding(20.dp)
                .testTag("folder_detail_dialog")
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Header with Editable Name and Close Button
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
                                .size(34.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(theme.accentPrimary.copy(alpha = 0.18f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.FolderOpen,
                                contentDescription = null,
                                tint = theme.accentPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        if (isEditingName) {
                            BasicTextField(
                                value = editedName,
                                onValueChange = { editedName = it },
                                singleLine = true,
                                textStyle = TextStyle(
                                    color = theme.textPrimary,
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                cursorBrush = SolidColor(theme.accentPrimary),
                                modifier = Modifier.weight(1f)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(theme.accentPrimary)
                                    .clickable {
                                        if (editedName.isNotBlank()) {
                                            onRenameFolder(editedName.trim())
                                            isEditingName = false
                                        }
                                    }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "Done",
                                    style = TextStyle(color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                )
                            }
                        } else {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.clickable { isEditingName = true }
                            ) {
                                Text(
                                    text = folder.name,
                                    style = TextStyle(
                                        color = theme.textPrimary,
                                        fontSize = 17.sp,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Rename Folder",
                                    tint = theme.textSecondary,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }

                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = theme.textSecondary,
                        modifier = Modifier
                            .size(20.dp)
                            .clickable { onDismiss() }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Apps in folder (3 or 4 columns)
                LazyVerticalGrid(
                    columns = GridCells.Fixed(4),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    flingBehavior = rememberChromeSmoothFlingBehavior(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(vertical = 4.dp)
                ) {
                    items(items = apps, key = { "folder_app_" + it.id }) { app ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .springPressEffect(
                                    hapticEnabled = hapticEnabled,
                                    scaleTarget = 0.90f,
                                    onClick = {
                                        onDismiss()
                                        onAppClick(app)
                                    }
                                )
                                .padding(2.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            AppLogoIcon(app = app, size = 48.dp, cornerRadius = 13.dp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = app.name,
                                style = TextStyle(
                                    color = theme.textPrimary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    textAlign = TextAlign.Center
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Bottom Action: Dissolve / Ungroup Folder
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0x1DEF4444))
                        .border(1.dp, Color(0xFFEF4444).copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                        .clickable {
                            onDissolveFolder()
                            onDismiss()
                        }
                        .padding(horizontal = 12.dp, vertical = 9.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Ungroup Folder",
                        tint = Color(0xFFEF4444),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Ungroup Folder (Return Apps to Home)",
                        style = TextStyle(
                            color = Color(0xFFEF4444),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }
        }
    }
}
