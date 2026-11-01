package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.example.R
import com.example.data.auth.AuthCredentialsConfig
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Policy
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.EcosystemLinkEntity
import com.example.ui.components.glassmorphic
import com.example.ui.components.springPressEffect
import com.example.ui.theme.MayaTheme

@Composable
fun OfficialProfileScreen(
    ecosystemLinks: List<EcosystemLinkEntity>,
    onClose: () -> Unit,
    onOpenAdminPanel: () -> Unit,
    onOpenEcosystemLink: (EcosystemLinkEntity) -> Unit,
    onOpenTerms: () -> Unit = {},
    onOpenPrivacyPolicy: () -> Unit = {},
    currentUserRole: com.example.data.auth.UserRole = com.example.data.auth.UserRole.STANDARD,
    hapticEnabled: Boolean = true
) {
    BackHandler { onClose() }

    val theme = MayaTheme.colors
    val context = LocalContext.current

    var showAdminPasswordDialog by remember { mutableStateOf(false) }
    var showRestrictedDialog by remember { mutableStateOf(false) }
    var passwordInput by remember { mutableStateOf("") }
    var passwordError by remember { mutableStateOf<String?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(theme.backgroundGradient)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 18.dp, vertical = 12.dp)
        ) {
            // Top Navigation Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(theme.surfaceGlass)
                        .border(1.dp, theme.borderGlass, CircleShape)
                        .clickable { onClose() }
                        .testTag("official_profile_back"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = theme.textPrimary,
                        modifier = Modifier.size(19.dp)
                    )
                }

                Text(
                    text = "Official Profile",
                    style = TextStyle(
                        color = theme.textPrimary,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                )

                Box(modifier = Modifier.size(38.dp))
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Hero Brand Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(26.dp))
                    .glassmorphic(
                        shape = RoundedCornerShape(26.dp),
                        backgroundColor = theme.surfaceGlass,
                        borderColor = theme.borderGlass,
                        borderWidth = 1.dp
                    )
                    .padding(22.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(84.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                            .border(2.dp, Color(0xFF38BDF8).copy(alpha = 0.6f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.maya_connect_official_logo),
                            contentDescription = "Maya Connect Official Logo",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Verified Blue-Tick "Maya Connect" Title — Click prompts Admin Password Dialog
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                if (currentUserRole == com.example.data.auth.UserRole.CEO_PANEL) {
                                    passwordInput = ""
                                    passwordError = null
                                    showAdminPasswordDialog = true
                                } else {
                                    showRestrictedDialog = true
                                }
                            }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                            .testTag("official_brand_verified_click")
                    ) {
                        Text(
                            text = "Maya Connect",
                            style = TextStyle(
                                color = theme.textPrimary,
                                fontSize = 23.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = (-0.4).sp
                            )
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.Default.Verified,
                            contentDescription = "Verified Ecosystem",
                            tint = Color(0xFF38BDF8),
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Unified Intelligent Workspace & Neural OS",
                        style = TextStyle(
                            color = theme.textSecondary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        ),
                        textAlign = TextAlign.Center
                    )

                    if (currentUserRole == com.example.data.auth.UserRole.CEO_PANEL) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    Brush.linearGradient(
                                        listOf(Color(0xFF7C3AED), Color(0xFF0284C7))
                                    )
                                )
                                .clickable {
                                    passwordInput = ""
                                    passwordError = null
                                    showAdminPasswordDialog = true
                                }
                                .padding(horizontal = 16.dp, vertical = 10.dp)
                                .testTag("ceo_open_admin_panel_btn"),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.AdminPanelSettings,
                                    contentDescription = "Admin Panel",
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Open Admin Control Panel",
                                    style = TextStyle(
                                        color = Color.White,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Founder & Developer Profile Card
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(18.dp))
                            .background(
                                Brush.linearGradient(
                                    listOf(Color(0xFF2E1045), Color(0xFF130922))
                                )
                            )
                            .border(1.dp, Color(0xFFA855F7).copy(alpha = 0.5f), RoundedCornerShape(18.dp))
                            .padding(16.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(
                                            Brush.linearGradient(
                                                listOf(Color(0xFF7C3AED), Color(0xFF38BDF8))
                                            )
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "R",
                                        style = TextStyle(
                                            color = Color.White,
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.Black
                                        )
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "Rizwan Roushan Rownaq",
                                            style = TextStyle(
                                                color = Color.White,
                                                fontSize = 15.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        )
                                        Spacer(modifier = Modifier.width(5.dp))
                                        Icon(
                                            imageVector = Icons.Default.Verified,
                                            contentDescription = "Verified Developer",
                                            tint = Color(0xFF38BDF8),
                                            modifier = Modifier.size(17.dp)
                                        )
                                    }
                                    Text(
                                        text = "Software Engineer & Artificial Intelligence Expert",
                                        style = TextStyle(
                                            color = Color(0xFFC084FC),
                                            fontSize = 10.5.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Clickable Portfolio Link Card
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0x2238BDF8))
                                    .border(1.dp, Color(0x6638BDF8), RoundedCornerShape(12.dp))
                                    .clickable {
                                        try {
                                            val webIntent = Intent(
                                                Intent.ACTION_VIEW,
                                                Uri.parse("https://rizwanr.ai.studio")
                                            )
                                            context.startActivity(webIntent)
                                        } catch (e: Exception) {
                                            // Handle intent launcher if fallback needed
                                        }
                                    }
                                    .padding(horizontal = 14.dp, vertical = 9.dp)
                                    .testTag("developer_portfolio_link")
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Code,
                                            contentDescription = null,
                                            tint = Color(0xFF38BDF8),
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "https://rizwanr.ai.studio",
                                            style = TextStyle(
                                                color = Color(0xFF38BDF8),
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        )
                                    }
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                        contentDescription = "Open Web Link",
                                        tint = Color(0xFF38BDF8),
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Tech Architecture Specs
            Text(
                text = "System Architecture",
                style = TextStyle(
                    color = theme.textPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            )

            Spacer(modifier = Modifier.height(10.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .glassmorphic(
                        shape = RoundedCornerShape(20.dp),
                        backgroundColor = theme.surfaceGlass,
                        borderColor = theme.borderGlass,
                        borderWidth = 1.dp
                    )
                    .padding(16.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    TechSpecRow("Version", "Maya Connect v3.0 Ultra")
                    TechSpecRow("UI Design System", "Liquid Glassmorphism 3.0")
                    TechSpecRow("AI Intelligence", "Google Gemini 2.5 Flash")
                    TechSpecRow("Local Persistence", "SQLite Room DB v4 (5 Entities)")
                    TechSpecRow("Network Engine", "Chromium Software Safe-Raster")
                    TechSpecRow("Security Level", "AES-256 Node Isolation")
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Official Ecosystem Links
            Text(
                text = "Official REXLIFY Portals",
                style = TextStyle(
                    color = theme.textPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            )

            Spacer(modifier = Modifier.height(10.dp))

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                ecosystemLinks.take(10).forEach { link ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .glassmorphic(
                                shape = RoundedCornerShape(16.dp),
                                backgroundColor = theme.surfaceGlass,
                                borderColor = theme.borderGlass,
                                borderWidth = 1.dp
                            )
                            .clickable { onOpenEcosystemLink(link) }
                            .padding(horizontal = 14.dp, vertical = 12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(Color(0xFF0284C7).copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Language,
                                        contentDescription = null,
                                        tint = Color(0xFF38BDF8),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = link.name,
                                        style = TextStyle(
                                            color = theme.textPrimary,
                                            fontSize = 13.5.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    )
                                    Text(
                                        text = link.url,
                                        style = TextStyle(
                                            color = theme.textSecondary,
                                            fontSize = 11.sp
                                        ),
                                        maxLines = 1
                                    )
                                }
                            }
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                tint = theme.textSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Footer info
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Terms & Conditions",
                        style = TextStyle(
                            color = theme.accentPrimary,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        modifier = Modifier.clickable { onOpenTerms() }
                    )
                    Text(
                        text = "•",
                        style = TextStyle(color = theme.textSecondary, fontSize = 11.sp)
                    )
                    Text(
                        text = "Privacy Policy",
                        style = TextStyle(
                            color = theme.accentPrimary,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        modifier = Modifier.clickable { onOpenPrivacyPolicy() }
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Crafted with precision by Rizwan RR",
                    style = TextStyle(
                        color = theme.textSecondary,
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Medium
                    )
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = "© 2026 REXLIFY Ecosystem. All rights reserved.",
                    style = TextStyle(
                        color = theme.textSecondary.copy(alpha = 0.6f),
                        fontSize = 10.sp
                    )
                )
            }

            Spacer(modifier = Modifier.height(30.dp))
        }

        // Admin Security Password Modal Dialog
        if (showAdminPasswordDialog) {
            Dialog(
                onDismissRequest = { showAdminPasswordDialog = false }
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(22.dp))
                        .background(if (theme.isDark) Color(0xFF131722) else Color(0xFFFFFFFF))
                        .border(1.dp, theme.borderGlass, RoundedCornerShape(22.dp))
                        .padding(20.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF7C3AED).copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "Security Password",
                                tint = Color(0xFFA78BFA),
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "Admin Access Authentication",
                            style = TextStyle(
                                color = theme.textPrimary,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )

                        Text(
                            text = "Enter secure admin password to open Admin Control Center",
                            style = TextStyle(
                                color = theme.textSecondary,
                                fontSize = 11.5.sp,
                                textAlign = TextAlign.Center
                            )
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        OutlinedTextField(
                            value = passwordInput,
                            onValueChange = {
                                passwordInput = it
                                passwordError = null
                            },
                            label = { Text("Admin Password") },
                            placeholder = { Text("Enter Admin Password") },
                            singleLine = true,
                            visualTransformation = PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Password,
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    val cleanPass = passwordInput.trim()
                                    if (cleanPass == "RTX@0012352" || AuthCredentialsConfig.isValidAdminGatePassword(cleanPass)) {
                                        showAdminPasswordDialog = false
                                        onOpenAdminPanel()
                                    } else {
                                        passwordError = "Incorrect password! Access denied."
                                    }
                                }
                            ),
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = theme.textPrimary,
                                unfocusedTextColor = theme.textPrimary,
                                focusedBorderColor = theme.accentPrimary,
                                unfocusedBorderColor = theme.borderGlass
                            )
                        )

                        val currentError = passwordError
                        if (!currentError.isNullOrEmpty()) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = currentError,
                                style = TextStyle(
                                    color = Color(0xFFEF4444),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(theme.surfaceGlass)
                                    .border(1.dp, theme.borderGlass, RoundedCornerShape(12.dp))
                                    .clickable { showAdminPasswordDialog = false }
                                    .padding(vertical = 12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Cancel",
                                    style = TextStyle(
                                        color = theme.textPrimary,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFF7C3AED))
                                    .clickable {
                                        val cleanPass = passwordInput.trim()
                                        if (cleanPass == "RTX@0012352" || AuthCredentialsConfig.isValidAdminGatePassword(cleanPass)) {
                                            showAdminPasswordDialog = false
                                            onOpenAdminPanel()
                                        } else {
                                            passwordError = "Incorrect password! Access denied."
                                        }
                                    }
                                    .padding(vertical = 12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Authenticate",
                                    style = TextStyle(
                                        color = Color.White,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                        }
                    }
                }
            }

            // Restricted Access Dialog for Non-CEO Roles
            if (showRestrictedDialog) {
                Dialog(onDismissRequest = { showRestrictedDialog = false }) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(24.dp))
                            .glassmorphic(
                                shape = RoundedCornerShape(24.dp),
                                backgroundColor = if (theme.isDark) Color(0xFF131824) else Color(0xFFFFFFFF),
                                borderColor = theme.borderGlass,
                                borderWidth = 1.dp
                            )
                            .padding(22.dp)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(54.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFEF4444).copy(alpha = 0.15f))
                                    .border(1.dp, Color(0xFFEF4444).copy(alpha = 0.4f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = null,
                                    tint = Color(0xFFEF4444),
                                    modifier = Modifier.size(26.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            Text(
                                text = "Admin Portal Restricted",
                                style = TextStyle(
                                    color = theme.textPrimary,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "Admin Control Center is restricted to CEO and Family Admin accounts. Standard and Family users do not have administrative authorization.",
                                style = TextStyle(
                                    color = theme.textSecondary,
                                    fontSize = 13.sp,
                                    lineHeight = 18.sp,
                                    textAlign = TextAlign.Center
                                )
                            )

                            Spacer(modifier = Modifier.height(20.dp))

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(theme.accentPrimary)
                                    .clickable { showRestrictedDialog = false }
                                    .padding(vertical = 12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Understood",
                                    style = TextStyle(
                                        color = Color.White,
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
}

@Composable
private fun TechSpecRow(label: String, value: String) {
    val theme = MayaTheme.colors
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = TextStyle(
                color = theme.textSecondary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
        )
        Text(
            text = value,
            style = TextStyle(
                color = theme.textPrimary,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )
        )
    }
}
