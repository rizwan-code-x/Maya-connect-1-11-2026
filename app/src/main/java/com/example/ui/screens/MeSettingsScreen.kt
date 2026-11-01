package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.Image
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.example.R
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Animation
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ClearAll
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.WbSunny
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Policy
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.TextButton
import androidx.compose.ui.window.Dialog
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppItem
import com.example.ui.components.AppLogoIcon
import com.example.ui.components.MayaTopBar
import com.example.ui.components.rememberChromeSmoothFlingBehavior
import com.example.ui.components.glassmorphic
import com.example.ui.theme.MayaColorError
import com.example.ui.theme.MayaColorSuccess
import com.example.ui.theme.MayaTheme
import com.example.ui.theme.MayaThemeMode
import com.example.data.auth.UserRole
import com.example.data.auth.UserSession
import androidx.compose.material.icons.automirrored.filled.Logout

@Composable
fun MeSettingsScreen(
    userName: String = "Rizwan RR",
    onUserNameChange: (String) -> Unit = {},
    userSession: UserSession? = null,
    onLogout: () -> Unit = {},
    currentThemeMode: MayaThemeMode,
    hapticEnabled: Boolean,
    reduceMotion: Boolean,
    notificationsEnabled: Boolean,
    allAppsAdmin: List<AppItem>,
    onThemeModeSelected: (MayaThemeMode) -> Unit,
    onHapticsToggled: (Boolean) -> Unit,
    onReduceMotionToggled: (Boolean) -> Unit,
    onNotificationsToggled: (Boolean) -> Unit,
    onToggleAppEnabled: (AppItem) -> Unit,
    onClearRecentHistory: () -> Unit,
    onEditApp: (AppItem) -> Unit,
    onAddApp: () -> Unit,
    onResetDefaults: () -> Unit,
    onOpenTerms: () -> Unit = {},
    onOpenPrivacyPolicy: () -> Unit = {},
    onDeleteAllUserData: (onComplete: () -> Unit) -> Unit = {},
    onSubmitDataDeletionRequest: (reason: String) -> Unit = {}
) {
    val theme = MayaTheme.colors
    val context = LocalContext.current
    var isEditingName by remember { mutableStateOf(false) }
    var tempName by remember { mutableStateOf(userName) }
    var showDataDeletionDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 18.dp)
            .padding(top = 2.dp, bottom = 120.dp)
    ) {
        // Minimal Top Bar (Point 22)
        MayaTopBar(
            userName = userName,
            onProfileClick = {}
        )

        Spacer(modifier = Modifier.height(6.dp))

        // Profile Card with Editable User Name (Points 27-28)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .glassmorphic(
                    shape = RoundedCornerShape(20.dp),
                    backgroundColor = theme.surfaceGlass,
                    borderColor = theme.borderGlass,
                    borderWidth = 1.dp
                )
                .padding(18.dp)
        ) {
            Column {
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
                                .size(50.dp)
                                .clip(CircleShape)
                                .background(Color.White)
                                .border(1.5.dp, theme.borderGlass, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.maya_connect_official_logo),
                                contentDescription = "Official Profile Logo",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape)
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = userName,
                                style = TextStyle(
                                    color = theme.textPrimary,
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            Text(
                                text = "$userName's Digital Ecosystem",
                                style = TextStyle(
                                    color = theme.textSecondary,
                                    fontSize = 11.5.sp
                                )
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(theme.surfaceGlass)
                            .border(0.8.dp, theme.borderGlass, CircleShape)
                            .clickable { isEditingName = !isEditingName },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Profile Name",
                            tint = theme.textPrimary,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                }

                if (isEditingName) {
                    Spacer(modifier = Modifier.height(14.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = tempName,
                            onValueChange = { tempName = it },
                            label = { Text("Your Name", fontSize = 11.sp) },
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = theme.textPrimary,
                                unfocusedTextColor = theme.textPrimary,
                                focusedBorderColor = theme.accentPrimary,
                                unfocusedBorderColor = theme.borderGlass
                            )
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(theme.buttonBackground)
                                .clickable {
                                    if (tempName.isNotBlank()) {
                                        onUserNameChange(tempName.trim())
                                        isEditingName = false
                                        Toast.makeText(context, "Profile name updated", Toast.LENGTH_SHORT).show()
                                    }
                                }
                                .padding(horizontal = 14.dp, vertical = 12.dp)
                        ) {
                            Text(
                                text = "Save",
                                style = TextStyle(
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Role Badge & User UUID
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val role = userSession?.role ?: UserRole.STANDARD
                    val (badgeBg, badgeText, badgeBorder) = when (role) {
                        UserRole.STANDARD -> Triple(Color(0xFF0284C7).copy(alpha = 0.2f), "Standard User", Color(0xFF0284C7).copy(alpha = 0.5f))
                        UserRole.BRANCH_ADMIN -> Triple(Color(0xFF10B981).copy(alpha = 0.2f), "Branch Admin", Color(0xFF10B981).copy(alpha = 0.5f))
                        UserRole.RRR_FAMILY -> Triple(Color(0xFF8B5CF6).copy(alpha = 0.2f), "RRR Family", Color(0xFF8B5CF6).copy(alpha = 0.5f))
                        UserRole.CEO_PANEL -> Triple(Color(0xFFF59E0B).copy(alpha = 0.2f), "CEO & Family Admin", Color(0xFFF59E0B).copy(alpha = 0.5f))
                        UserRole.SECURITY_ADMIN -> Triple(Color(0xFFEF4444).copy(alpha = 0.2f), "Security Administrator", Color(0xFFEF4444).copy(alpha = 0.5f))
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(badgeBg)
                            .border(1.dp, badgeBorder, RoundedCornerShape(8.dp))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = badgeText,
                            style = TextStyle(
                                color = theme.textPrimary,
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                    }

                    Text(
                        text = "ID: ${userSession?.userId ?: "usr_guest"}",
                        style = TextStyle(
                            color = theme.textSecondary.copy(alpha = 0.7f),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Log Out / Switch Account Button
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFEF4444).copy(alpha = 0.12f))
                        .border(1.dp, Color(0xFFEF4444).copy(alpha = 0.35f), RoundedCornerShape(12.dp))
                        .clickable { onLogout() }
                        .padding(vertical = 10.dp, horizontal = 12.dp)
                        .testTag("me_logout_btn"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Logout,
                            contentDescription = "Log Out",
                            tint = Color(0xFFEF4444),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Log Out / Switch Account",
                            style = TextStyle(
                                color = Color(0xFFEF4444),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(22.dp))

        // ==========================================
        // 1. APPEARANCE (Points 2-6: Four Complete Themes)
        // ==========================================
        SectionHeader(icon = Icons.Default.Palette, title = "APPEARANCE")
        Spacer(modifier = Modifier.height(10.dp))

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            ThemeOptionCard(
                mode = MayaThemeMode.DARK,
                icon = Icons.Default.DarkMode,
                isSelected = currentThemeMode == MayaThemeMode.DARK,
                accentColor = Color(0xFFE2E8F0),
                onSelect = { onThemeModeSelected(MayaThemeMode.DARK) }
            )

            ThemeOptionCard(
                mode = MayaThemeMode.SUNSET,
                icon = Icons.Default.WbSunny,
                isSelected = currentThemeMode == MayaThemeMode.SUNSET,
                accentColor = Color(0xFFF97316),
                onSelect = { onThemeModeSelected(MayaThemeMode.SUNSET) }
            )

            ThemeOptionCard(
                mode = MayaThemeMode.LIGHT,
                icon = Icons.Default.LightMode,
                isSelected = currentThemeMode == MayaThemeMode.LIGHT,
                accentColor = Color(0xFF1E293B),
                onSelect = { onThemeModeSelected(MayaThemeMode.LIGHT) }
            )

            ThemeOptionCard(
                mode = MayaThemeMode.PINK,
                icon = Icons.Default.AutoAwesome,
                isSelected = currentThemeMode == MayaThemeMode.PINK,
                accentColor = Color(0xFFF472B6),
                onSelect = { onThemeModeSelected(MayaThemeMode.PINK) }
            )

            ThemeOptionCard(
                mode = MayaThemeMode.SAFFRON,
                icon = Icons.Default.WbSunny,
                isSelected = currentThemeMode == MayaThemeMode.SAFFRON,
                accentColor = Color(0xFFF59E0B),
                onSelect = { onThemeModeSelected(MayaThemeMode.SAFFRON) }
            )
        }

        Spacer(modifier = Modifier.height(22.dp))

        // ==========================================
        // 2. HAPTICS (Point 110, 89)
        // ==========================================
        SectionHeader(icon = Icons.Default.Vibration, title = "HAPTICS")
        Spacer(modifier = Modifier.height(10.dp))

        SettingsToggleCard(
            icon = Icons.Default.Vibration,
            title = "Haptic Tactile Feedback",
            subtitle = "Tactile vibration feedback on button presses",
            checked = hapticEnabled,
            onCheckedChange = onHapticsToggled,
            accentColor = theme.accentPrimary
        )

        Spacer(modifier = Modifier.height(22.dp))

        // ==========================================
        // 3. ANIMATIONS (Point 110, 88)
        // ==========================================
        SectionHeader(icon = Icons.Default.Animation, title = "ANIMATIONS")
        Spacer(modifier = Modifier.height(10.dp))

        SettingsToggleCard(
            icon = Icons.Default.Animation,
            title = "Reduce Motion",
            subtitle = "Minimize spring transitions for performance",
            checked = reduceMotion,
            onCheckedChange = onReduceMotionToggled,
            accentColor = theme.accentSecondary
        )

        Spacer(modifier = Modifier.height(22.dp))

        // ==========================================
        // 4. NOTIFICATIONS (Point 110, 93)
        // ==========================================
        SectionHeader(icon = Icons.Default.Notifications, title = "NOTIFICATIONS")
        Spacer(modifier = Modifier.height(10.dp))

        SettingsToggleCard(
            icon = Icons.Default.Notifications,
            title = "Ecosystem Notifications",
            subtitle = "Receive ecosystem updates & background sync alerts",
            checked = notificationsEnabled,
            onCheckedChange = onNotificationsToggled,
            accentColor = MayaColorSuccess
        )

        Spacer(modifier = Modifier.height(22.dp))

        // ==========================================
        // 5. APP PREFERENCES (Point 110, 111)
        // ==========================================
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            SectionHeader(icon = Icons.Default.Settings, title = "APP PREFERENCES")
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { onAddApp() }
                    .padding(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    tint = theme.textPrimary,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(3.dp))
                Text(
                    text = "Add App",
                    style = TextStyle(
                        color = theme.textPrimary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }
        Spacer(modifier = Modifier.height(10.dp))

        // Clear Recent Apps History
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .glassmorphic(
                    shape = RoundedCornerShape(14.dp),
                    backgroundColor = theme.surfaceGlass,
                    borderColor = theme.borderGlass,
                    borderWidth = 1.dp
                )
                .clickable {
                    onClearRecentHistory()
                    Toast.makeText(context, "Cleared recently used app history", Toast.LENGTH_SHORT).show()
                }
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.ClearAll,
                        contentDescription = null,
                        tint = theme.textSecondary,
                        modifier = Modifier.size(17.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Clear Recently Used Apps",
                        style = TextStyle(
                            color = theme.textPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                }
                Text(
                    text = "Clear",
                    style = TextStyle(
                        color = theme.textSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Application Management Modal Trigger Card (এপ্লিকেশন ম্যানেজমেন্ট)
        var showAppManagementDialog by remember { mutableStateOf(false) }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .glassmorphic(
                    shape = RoundedCornerShape(16.dp),
                    backgroundColor = theme.surfaceGlass,
                    borderColor = theme.borderGlass,
                    borderWidth = 1.dp
                )
                .clickable { showAppManagementDialog = true }
                .padding(16.dp)
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
                            .size(38.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(theme.accentPrimary.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = null,
                            tint = theme.accentPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Application Management",
                            style = TextStyle(
                                color = theme.textPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Text(
                            text = "Manage ${allAppsAdmin.size} apps, toggle status, edit URLs & launch settings",
                            style = TextStyle(
                                color = theme.textSecondary,
                                fontSize = 11.sp
                            )
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(theme.accentPrimary)
                        .padding(horizontal = 12.dp, vertical = 7.dp)
                ) {
                    Text(
                        text = "Manage",
                        style = TextStyle(
                            color = if (theme.isDark) Color.Black else Color.White,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }
        }

        if (showAppManagementDialog) {
            AppManagementDialog(
                allAppsAdmin = allAppsAdmin,
                onDismiss = { showAppManagementDialog = false },
                onToggleAppEnabled = onToggleAppEnabled,
                onEditApp = onEditApp,
                onAddApp = {
                    showAppManagementDialog = false
                    onAddApp()
                }
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Reset to Defaults Button
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(Color(0x14EF4444))
                .border(1.dp, Color(0x33EF4444), RoundedCornerShape(14.dp))
                .clickable {
                    onResetDefaults()
                    Toast.makeText(context, "Restored default Maya Connect ecosystem registry", Toast.LENGTH_SHORT).show()
                }
                .padding(12.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.RestartAlt,
                    contentDescription = null,
                    tint = MayaColorError,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Reset Registry to Default Ecosystem",
                    style = TextStyle(
                        color = MayaColorError,
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // ==========================================
        // 6. LEGAL & COMPLIANCE (Terms, Privacy & Deletion)
        // ==========================================
        SectionHeader(icon = Icons.Default.Security, title = "LEGAL & COMPLIANCE")
        Spacer(modifier = Modifier.height(10.dp))

        // Terms & Conditions Entry Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .glassmorphic(
                    shape = RoundedCornerShape(16.dp),
                    backgroundColor = theme.surfaceGlass,
                    borderColor = theme.borderGlass,
                    borderWidth = 1.dp
                )
                .clickable { onOpenTerms() }
                .padding(16.dp)
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
                            .size(38.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(theme.accentPrimary.copy(alpha = 0.18f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Description,
                            contentDescription = null,
                            tint = theme.accentPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Terms & Conditions",
                            style = TextStyle(
                                color = theme.textPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Text(
                            text = "Voluntary use, user responsibility, no penalty for discontinuing • Oct 27, 2026",
                            style = TextStyle(
                                color = theme.textSecondary,
                                fontSize = 11.sp,
                                lineHeight = 15.sp
                            )
                        )
                    }
                }
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = "Open Terms",
                    tint = theme.textSecondary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Privacy Policy Entry Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .glassmorphic(
                    shape = RoundedCornerShape(16.dp),
                    backgroundColor = theme.surfaceGlass,
                    borderColor = theme.borderGlass,
                    borderWidth = 1.dp
                )
                .clickable { onOpenPrivacyPolicy() }
                .padding(16.dp)
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
                            .size(38.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(theme.accentSecondary.copy(alpha = 0.18f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Policy,
                            contentDescription = null,
                            tint = theme.accentSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Privacy Policy",
                            style = TextStyle(
                                color = theme.textPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Text(
                            text = "Actual data handling, security, no sale of info & deletion rights",
                            style = TextStyle(
                                color = theme.textSecondary,
                                fontSize = 11.sp,
                                lineHeight = 15.sp
                            )
                        )
                    }
                }
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = "Open Privacy Policy",
                    tint = theme.textSecondary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Working Data Deletion Request Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .glassmorphic(
                    shape = RoundedCornerShape(16.dp),
                    backgroundColor = Color(0x10EF4444),
                    borderColor = Color(0x33EF4444),
                    borderWidth = 1.dp
                )
                .clickable { showDataDeletionDialog = true }
                .padding(16.dp)
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
                            .size(38.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0x22EF4444)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteForever,
                            contentDescription = null,
                            tint = MayaColorError,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Data-Deletion Request",
                            style = TextStyle(
                                color = MayaColorError,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Text(
                            text = "Delete on-device Room data or email privacy@rexlify.com",
                            style = TextStyle(
                                color = theme.textSecondary,
                                fontSize = 11.sp
                            )
                        )
                    }
                }
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = "Open Deletion Tools",
                    tint = MayaColorError,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // ==========================================
        // 7. ABOUT MAYA CONNECT
        // ==========================================
        SectionHeader(icon = Icons.Default.Info, title = "ABOUT MAYA CONNECT")
        Spacer(modifier = Modifier.height(10.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .glassmorphic(
                    shape = RoundedCornerShape(16.dp),
                    backgroundColor = theme.surfaceGlass,
                    borderColor = theme.borderGlass,
                    borderWidth = 1.dp
                )
                .padding(16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Maya Connect • Official Ecosystem Hub",
                    style = TextStyle(
                        color = theme.textPrimary,
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
                Text(
                    text = "A high-performance Android OS workspace unifying REXLIFY, NeuraX, NooraX, SIFRA, RODP, and PH VEG.\n\nFree & Voluntary: No penalty or fees for discontinuing use or uninstalling Maya Connect.",
                    style = TextStyle(
                        color = theme.textSecondary,
                        fontSize = 11.5.sp,
                        lineHeight = 16.sp
                    )
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Official Customer Support: rexlifyofficialsupportteam@gmail.com\nDeveloper / Publisher: REXLIFY • West Bengal, India",
                    style = TextStyle(
                        color = theme.textSecondary,
                        fontSize = 10.5.sp,
                        lineHeight = 15.sp,
                        fontWeight = FontWeight.Medium
                    )
                )
            }
        }
    }

    // Interactive Data Deletion Request Dialog
    if (showDataDeletionDialog) {
        AlertDialog(
            onDismissRequest = { showDataDeletionDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.DeleteForever,
                    contentDescription = null,
                    tint = MayaColorError,
                    modifier = Modifier.size(34.dp)
                )
            },
            title = {
                Text(
                    text = "Eligible Data Deletion Options",
                    style = TextStyle(fontWeight = FontWeight.Bold, fontSize = 16.sp)
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Under Section 4 of our Privacy Policy, you have the full right to delete your data at any time without penalty.\n\nChoose an action:",
                        style = TextStyle(fontSize = 12.5.sp, lineHeight = 17.sp)
                    )

                    Button(
                        onClick = {
                            showDataDeletionDialog = false
                            onDeleteAllUserData {
                                Toast.makeText(
                                    context,
                                    "All on-device tasks, notes, history & profile data cleared.",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = MayaColorError),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(
                            text = "Clear All On-Device Data",
                            style = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        )
                    }

                    OutlinedButton(
                        onClick = {
                            onSubmitDataDeletionRequest("User requested account and personal data deletion via MeSettings")
                            showDataDeletionDialog = false
                            Toast.makeText(context, "Data deletion request submitted to REXLIFY Admin", Toast.LENGTH_LONG).show()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(
                            text = "Submit Server Deletion Request",
                            style = TextStyle(fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                        )
                    }

                    OutlinedButton(
                        onClick = {
                            showDataDeletionDialog = false
                            try {
                                val intent = Intent(Intent.ACTION_SENDTO).apply {
                                    data = Uri.parse("mailto:privacy@rexlify.com")
                                    putExtra(Intent.EXTRA_SUBJECT, "Maya Connect - Data Deletion Request")
                                    putExtra(
                                        Intent.EXTRA_TEXT,
                                        "Dear REXLIFY Privacy Officer,\n\nI am requesting the deletion of my personal information in accordance with Section 4 of the Maya Connect Privacy Policy.\n\nUser Name: $userName\nPlatform: Maya Connect Android\nDate: October 27, 2026\n\nThank you."
                                    )
                                }
                                context.startActivity(intent)
                            } catch (_: Exception) {
                                Toast.makeText(context, "Email privacy@rexlify.com", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(
                            text = "Email Remote Deletion Request",
                            style = TextStyle(fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold)
                        )
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showDataDeletionDialog = false }) {
                    Text("Close")
                }
            }
        )
    }
}

@Composable
private fun SectionHeader(icon: ImageVector, title: String) {
    val theme = MayaTheme.colors
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = theme.textSecondary,
            modifier = Modifier.size(15.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = title,
            style = TextStyle(
                color = theme.textSecondary,
                fontSize = 11.5.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.8.sp
            )
        )
    }
}

@Composable
private fun SettingsToggleCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    accentColor: Color
) {
    val theme = MayaTheme.colors
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .glassmorphic(
                shape = RoundedCornerShape(16.dp),
                backgroundColor = theme.surfaceGlass,
                borderColor = theme.borderGlass,
                borderWidth = 1.dp
            )
            .padding(16.dp)
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
                        .size(34.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(theme.surfaceGlass),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = theme.textPrimary,
                        modifier = Modifier.size(17.dp)
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
            }

            Spacer(modifier = Modifier.width(10.dp))

            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = accentColor
                )
            )
        }
    }
}

@Composable
private fun ThemeOptionCard(
    mode: MayaThemeMode,
    icon: ImageVector,
    isSelected: Boolean,
    accentColor: Color,
    onSelect: () -> Unit
) {
    val theme = MayaTheme.colors
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(if (isSelected) theme.surfaceGlass else theme.surfaceGlass.copy(alpha = 0.4f))
            .border(
                1.dp,
                if (isSelected) theme.accentPrimary else theme.borderGlass,
                RoundedCornerShape(14.dp)
            )
            .clickable { onSelect() }
            .padding(13.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) theme.accentPrimary.copy(alpha = 0.2f) else theme.surfaceGlass),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = if (isSelected) theme.textPrimary else theme.textSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = mode.title,
                        style = TextStyle(
                            color = theme.textPrimary,
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Text(
                        text = mode.description,
                        style = TextStyle(
                            color = theme.textSecondary,
                            fontSize = 10.5.sp
                        )
                    )
                }
            }

            if (isSelected) {
                Box(
                    modifier = Modifier
                        .size(22.dp)
                        .clip(CircleShape)
                        .background(theme.accentPrimary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Selected",
                        tint = if (theme.isDark) Color.Black else Color.White,
                        modifier = Modifier.size(13.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun RegistryAppAdminRow(
    app: AppItem,
    onToggleEnabled: () -> Unit,
    onEdit: () -> Unit
) {
    val theme = MayaTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(if (app.isEnabled) theme.surfaceGlass else theme.surfaceGlass.copy(alpha = 0.3f))
            .border(1.dp, theme.borderGlass, RoundedCornerShape(12.dp))
            .padding(horizontal = 12.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            AppLogoIcon(app = app, size = 30.dp, cornerRadius = 7.dp)
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = app.name,
                        style = TextStyle(
                            color = if (app.isEnabled) theme.textPrimary else theme.textSecondary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    if (!app.isEnabled) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "(Disabled)",
                            style = TextStyle(
                                color = MayaColorError,
                                fontSize = 9.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }
                Text(
                    text = app.url.ifEmpty { "Not configured" },
                    style = TextStyle(
                        color = theme.textSecondary,
                        fontSize = 10.sp
                    ),
                    maxLines = 1
                )
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(26.dp)
                    .clip(CircleShape)
                    .background(theme.surfaceGlass)
                    .clickable { onToggleEnabled() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (app.isEnabled) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                    contentDescription = if (app.isEnabled) "Disable app" else "Enable app",
                    tint = if (app.isEnabled) theme.textPrimary else MayaColorError,
                    modifier = Modifier.size(14.dp)
                )
            }

            Spacer(modifier = Modifier.width(6.dp))

            Box(
                modifier = Modifier
                    .size(26.dp)
                    .clip(CircleShape)
                    .background(theme.surfaceGlass)
                    .clickable { onEdit() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Configure",
                    tint = theme.textSecondary,
                    modifier = Modifier.size(13.dp)
                )
            }
        }
    }
}

@Composable
fun AppManagementDialog(
    allAppsAdmin: List<AppItem>,
    onDismiss: () -> Unit,
    onToggleAppEnabled: (AppItem) -> Unit,
    onEditApp: (AppItem) -> Unit,
    onAddApp: () -> Unit
) {
    val theme = MayaTheme.colors
    var searchQuery by remember { mutableStateOf("") }

    val filteredApps = remember(allAppsAdmin, searchQuery) {
        if (searchQuery.isBlank()) {
            allAppsAdmin
        } else {
            allAppsAdmin.filter {
                it.name.contains(searchQuery, ignoreCase = true) ||
                it.id.contains(searchQuery, ignoreCase = true) ||
                it.url.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(580.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(if (theme.isDark) Color(0xFF131722) else Color(0xFFF8FAFC))
                .border(1.dp, theme.borderGlass, RoundedCornerShape(24.dp))
                .padding(20.dp)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Application Management",
                            style = TextStyle(
                                color = theme.textPrimary,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Text(
                            text = "Application Management • ${allAppsAdmin.size} Apps",
                            style = TextStyle(
                                color = theme.textSecondary,
                                fontSize = 11.5.sp
                            )
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(theme.surfaceGlass)
                            .clickable { onDismiss() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = theme.textPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Search & Add Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Search applications...", fontSize = 12.sp) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = null,
                                tint = theme.textSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = theme.textPrimary,
                            unfocusedTextColor = theme.textPrimary,
                            focusedBorderColor = theme.accentPrimary,
                            unfocusedBorderColor = theme.borderGlass
                        )
                    )

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(theme.accentPrimary)
                            .clickable { onAddApp() }
                            .padding(horizontal = 12.dp, vertical = 14.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                tint = if (theme.isDark) Color.Black else Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Add",
                                style = TextStyle(
                                    color = if (theme.isDark) Color.Black else Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // App Organized Grid View (2-Columns)
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier.weight(1f),
                    flingBehavior = rememberChromeSmoothFlingBehavior(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredApps, key = { it.id }) { app ->
                        GridAppAdminCard(
                            app = app,
                            onToggleEnabled = { onToggleAppEnabled(app) },
                            onEdit = { onEditApp(app) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun GridAppAdminCard(
    app: AppItem,
    onToggleEnabled: () -> Unit,
    onEdit: () -> Unit
) {
    val theme = MayaTheme.colors
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(if (app.isEnabled) theme.surfaceGlass else theme.surfaceGlass.copy(alpha = 0.3f))
            .border(1.dp, if (app.isEnabled) theme.borderGlass else MayaColorError.copy(alpha = 0.3f), RoundedCornerShape(14.dp))
            .padding(12.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                AppLogoIcon(app = app, size = 32.dp, cornerRadius = 8.dp)

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(26.dp)
                            .clip(CircleShape)
                            .background(theme.surfaceGlass)
                            .clickable { onToggleEnabled() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (app.isEnabled) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = if (app.isEnabled) "Disable app" else "Enable app",
                            tint = if (app.isEnabled) theme.textPrimary else MayaColorError,
                            modifier = Modifier.size(13.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    Box(
                        modifier = Modifier
                            .size(26.dp)
                            .clip(CircleShape)
                            .background(theme.surfaceGlass)
                            .clickable { onEdit() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Configure",
                            tint = theme.textSecondary,
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = app.name,
                style = TextStyle(
                    color = if (app.isEnabled) theme.textPrimary else theme.textSecondary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                ),
                maxLines = 1
            )

            Text(
                text = if (app.isEnabled) app.tagline.ifBlank { "Active" } else "Disabled",
                style = TextStyle(
                    color = if (app.isEnabled) theme.textSecondary else MayaColorError,
                    fontSize = 10.sp
                ),
                maxLines = 1
            )
        }
    }
}
