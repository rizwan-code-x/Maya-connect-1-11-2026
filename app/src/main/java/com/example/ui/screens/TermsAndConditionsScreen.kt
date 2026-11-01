package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Policy
import androidx.compose.material.icons.filled.Scale
import androidx.compose.material.icons.filled.Update
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.glassmorphic
import com.example.ui.theme.MayaTheme

@Composable
fun TermsAndConditionsScreen(
    onClose: () -> Unit,
    onOpenPrivacyPolicy: () -> Unit = {},
    hapticEnabled: Boolean = true
) {
    val theme = MayaTheme.colors
    val context = LocalContext.current

    BackHandler {
        onClose()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(theme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            // Glass Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .glassmorphic(
                        shape = RoundedCornerShape(0.dp),
                        backgroundColor = if (theme.isDark) Color(0xF0131620) else Color(0xF0FFFFFF),
                        borderColor = theme.borderGlass,
                        borderWidth = 1.dp
                    )
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(theme.surfaceGlass)
                            .border(1.dp, theme.borderGlass, CircleShape)
                            .clickable { onClose() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = theme.textPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column {
                        Text(
                            text = "Terms & Conditions",
                            style = TextStyle(
                                color = theme.textPrimary,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Text(
                            text = "Maya Connect • Official Legal Terms",
                            style = TextStyle(
                                color = theme.textSecondary,
                                fontSize = 11.5.sp
                            )
                        )
                    }
                }
            }

            // Scrollable Legal Content
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 18.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Document Info Banner
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
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "MAYA CONNECT — TERMS & CONDITIONS",
                            style = TextStyle(
                                color = theme.accentPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Text(
                            text = "Last Updated: October 27, 2026 • Developer: REXLIFY",
                            style = TextStyle(
                                color = theme.textSecondary,
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Medium
                            )
                        )
                        Text(
                            text = "Location: West Bengal, India",
                            style = TextStyle(
                                color = theme.textSecondary,
                                fontSize = 11.sp
                            )
                        )
                    }
                }

                // Section 1
                LegalSectionCard(
                    icon = Icons.Default.VerifiedUser,
                    title = "1. Acceptance and Voluntary Use",
                    content = "Maya Connect is an application developed by REXLIFY. Users choose voluntarily to install and use the Application. Users may stop using it and uninstall it at any time without any barrier."
                )

                // Section 2
                LegalSectionCard(
                    icon = Icons.Default.AccountCircle,
                    title = "2. User Responsibility",
                    content = "Users are responsible for their own activities, the information they submit, the privacy of their devices and accounts, and the protection of their login credentials. Users should avoid sharing sensitive information unless necessary."
                )

                // Section 3
                LegalSectionCard(
                    icon = Icons.Default.Lock,
                    title = "3. Data and Account Security",
                    content = "Users should maintain appropriate security measures on their devices and accounts and delete their information when they no longer wish to retain it. Uninstalling the Application may not delete information already stored on remote servers; users should use the available deletion process where applicable.\n\nREXLIFY will take reasonable security measures appropriate to its services. However, no online service can guarantee absolute security. REXLIFY cannot guarantee that unauthorized access, cyberattacks, technical failures, or service interruptions will never occur."
                )

                // Section 4
                LegalSectionCard(
                    icon = Icons.Default.Scale,
                    title = "4. Limitation of Liability",
                    content = "To the extent permitted by applicable law, REXLIFY is not responsible for losses arising from circumstances beyond its reasonable control, including third-party service failures, unauthorized access, or cyberattacks, despite reasonable security measures.\n\nNothing in these Terms excludes liability that cannot legally be excluded or limits any mandatory rights available to users under applicable law."
                )

                // Section 5 (Crucial: Explicit No Penalty / No Fee Clause)
                LegalSectionCard(
                    icon = Icons.Default.CheckCircle,
                    title = "5. No Penalty for Discontinuing Use",
                    content = "Maya Connect does not charge users a penalty merely for stopping use or uninstalling the Application. No penalty or fee (such as ₹1,000 or any other amount) is imposed under these Terms solely because a user chooses to leave the platform.",
                    isHighlight = true
                )

                // Section 6
                LegalSectionCard(
                    icon = Icons.Default.OpenInNew,
                    title = "6. Third-Party Services",
                    content = "External applications, websites, and services accessible through Maya Connect (such as Microsoft Edge, Gemini, YouTube, or Google Maps) may have their own terms, privacy policies, and security practices. Users should review those policies before using external services."
                )

                // Section 7
                LegalSectionCard(
                    icon = Icons.Default.Update,
                    title = "7. Updates and Termination",
                    content = "REXLIFY may update, suspend, or discontinue features when necessary. Material changes to these Terms will be communicated where required by applicable law."
                )

                // Section 8
                LegalSectionCard(
                    icon = Icons.Default.Gavel,
                    title = "8. Governing Law and Contact",
                    content = "These Terms are governed by the applicable laws of India. Any dispute will be handled by a competent authority or court in accordance with applicable law.\n\nOfficial Support: rexlifyofficialsupportteam@gmail.com\nDeveloper / Publisher: REXLIFY • West Bengal, India\n\nBy using Maya Connect, users acknowledge these Terms, subject to applicable law."
                )

                // Action Buttons Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 6.dp, bottom = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onOpenPrivacyPolicy,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Policy,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Privacy Policy",
                            style = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        )
                    }

                    Button(
                        onClick = {
                            try {
                                val intent = Intent(Intent.ACTION_SENDTO).apply {
                                    data = Uri.parse("mailto:rexlifyofficialsupportteam@gmail.com")
                                    putExtra(Intent.EXTRA_SUBJECT, "Maya Connect - Terms & Support Inquiry")
                                }
                                context.startActivity(intent)
                            } catch (_: Exception) {
                                Toast.makeText(context, "Email: rexlifyofficialsupportteam@gmail.com", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = theme.accentPrimary)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Email,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Email Support",
                            style = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        )
                    }
                }
            }
        }
    }
}

@Composable
internal fun LegalSectionCard(
    icon: ImageVector,
    title: String,
    content: String,
    isHighlight: Boolean = false
) {
    val theme = MayaTheme.colors

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .glassmorphic(
                shape = RoundedCornerShape(16.dp),
                backgroundColor = if (isHighlight) theme.accentPrimary.copy(alpha = 0.12f) else theme.surfaceGlass,
                borderColor = if (isHighlight) theme.accentPrimary.copy(alpha = 0.4f) else theme.borderGlass,
                borderWidth = 1.dp
            )
            .padding(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isHighlight) theme.accentPrimary.copy(alpha = 0.2f) else theme.borderGlass),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = if (isHighlight) theme.accentPrimary else theme.textPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Text(
                    text = title,
                    style = TextStyle(
                        color = theme.textPrimary,
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
            }

            Text(
                text = content,
                style = TextStyle(
                    color = theme.textSecondary,
                    fontSize = 12.sp,
                    lineHeight = 18.sp
                )
            )
        }
    }
}
