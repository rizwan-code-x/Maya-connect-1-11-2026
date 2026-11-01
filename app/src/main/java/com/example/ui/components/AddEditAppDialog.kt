package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.AppCategory
import com.example.data.model.AppItem
import com.example.data.model.AppStatus
import com.example.ui.theme.MayaTheme
import java.util.UUID

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddEditAppDialog(
    initialApp: AppItem? = null,
    onDismiss: () -> Unit,
    onSave: (AppItem) -> Unit
) {
    val theme = MayaTheme.colors
    var name by remember { mutableStateOf(initialApp?.name ?: "") }
    var tagline by remember { mutableStateOf(initialApp?.tagline ?: "") }
    var url by remember { mutableStateOf(initialApp?.url ?: "https://") }
    var fullDescription by remember { mutableStateOf(initialApp?.fullDescription ?: "") }
    var selectedCategory by remember { mutableStateOf(initialApp?.category ?: AppCategory.PRODUCTIVITY) }
    var selectedIcon by remember { mutableStateOf(initialApp?.iconKey ?: "DEFAULT") }
    var errorMessage by remember { mutableStateOf("") }

    val iconPresets = listOf("DEFAULT", "REXLIFY", "NEURAX", "NOORAX", "SIFRA", "RODP", "TERMINAL", "CLOUD")

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .clip(RoundedCornerShape(24.dp))
                .border(1.2.dp, theme.borderGlass, RoundedCornerShape(24.dp)),
            color = if (theme.isDark) Color(0xFF131620) else Color.White,
            contentColor = theme.textPrimary
        ) {
            Column(
                modifier = Modifier
                    .padding(22.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (initialApp == null) "Add Custom App" else "Edit App Settings",
                        style = TextStyle(
                            color = theme.textPrimary,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
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

                Spacer(modifier = Modifier.height(16.dp))

                // App Name Field
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("App Name", color = theme.textSecondary) },
                    placeholder = { Text("e.g. My Workspace", color = theme.textSecondary.copy(alpha = 0.5f)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = theme.textPrimary,
                        unfocusedTextColor = theme.textPrimary,
                        focusedBorderColor = theme.accentPrimary,
                        unfocusedBorderColor = theme.borderGlass
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                // App Tagline Field
                OutlinedTextField(
                    value = tagline,
                    onValueChange = { tagline = it },
                    label = { Text("Tagline / Short Description", color = theme.textSecondary) },
                    placeholder = { Text("e.g. Custom AI Workspace", color = theme.textSecondary.copy(alpha = 0.5f)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = theme.textPrimary,
                        unfocusedTextColor = theme.textPrimary,
                        focusedBorderColor = theme.accentPrimary,
                        unfocusedBorderColor = theme.borderGlass
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Target URL
                OutlinedTextField(
                    value = url,
                    onValueChange = { url = it },
                    label = { Text("Application URL (https://...)", color = theme.textSecondary) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = theme.textPrimary,
                        unfocusedTextColor = theme.textPrimary,
                        focusedBorderColor = theme.accentPrimary,
                        unfocusedBorderColor = theme.borderGlass
                    )
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Category selector
                Text(
                    text = "CATEGORY",
                    style = TextStyle(
                        color = theme.accentPrimary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                )
                Spacer(modifier = Modifier.height(6.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AppCategory.entries.filter { it != AppCategory.ALL }.forEach { cat ->
                        val isSel = cat == selectedCategory
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSel) theme.accentPrimary else theme.surfaceGlass)
                                .border(
                                    1.dp,
                                    if (isSel) theme.accentPrimary else theme.borderGlass,
                                    RoundedCornerShape(10.dp)
                                )
                                .clickable { selectedCategory = cat }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = cat.displayName,
                                style = TextStyle(
                                    color = if (isSel) Color.Black else theme.textPrimary,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Icon preset selector
                Text(
                    text = "ICON STYLE",
                    style = TextStyle(
                        color = theme.accentPrimary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                )
                Spacer(modifier = Modifier.height(6.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    iconPresets.forEach { iconKey ->
                        val isSel = iconKey == selectedIcon
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSel) theme.accentSecondary.copy(alpha = 0.3f) else theme.surfaceGlass)
                                .border(
                                    1.dp,
                                    if (isSel) theme.accentSecondary else theme.borderGlass,
                                    RoundedCornerShape(10.dp)
                                )
                                .clickable { selectedIcon = iconKey }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = iconKey,
                                style = TextStyle(
                                    color = if (isSel) theme.accentSecondary else theme.textSecondary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            )
                        }
                    }
                }

                if (errorMessage.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = errorMessage,
                        style = TextStyle(color = Color(0xFFFF5252), fontSize = 12.sp)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Save button
                GlassButton(
                    text = if (initialApp == null) "Add to Ecosystem" else "Save Changes",
                    onClick = {
                        if (name.isBlank()) {
                            errorMessage = "Please enter an application name."
                            return@GlassButton
                        }
                        if (url.isBlank() || !url.startsWith("http")) {
                            errorMessage = "Please enter a valid URL starting with https:// or http://"
                            return@GlassButton
                        }
                        val itemToSave = initialApp?.copy(
                            name = name.trim(),
                            tagline = tagline.trim(),
                            url = url.trim(),
                            category = selectedCategory,
                            iconKey = selectedIcon
                        ) ?: AppItem(
                            id = "app_" + UUID.randomUUID().toString().take(8),
                            name = name.trim(),
                            tagline = tagline.trim().ifEmpty { "Custom Ecosystem App" },
                            fullDescription = fullDescription.trim(),
                            url = url.trim(),
                            category = selectedCategory,
                            status = AppStatus.AVAILABLE,
                            isFeatured = false,
                            isFavorite = false,
                            isPinnedHome = true,
                            iconKey = selectedIcon,
                            primaryColorHex = 0xFF475569,
                            secondaryColorHex = 0xFF334155,
                            isCustom = true
                        )
                        onSave(itemToSave)
                        onDismiss()
                    },
                    icon = Icons.Default.Save,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
