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
import androidx.compose.material.icons.filled.CreditCardOff
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.PermIdentity
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WorkOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.glassmorphic
import com.example.ui.theme.MayaColorError
import com.example.ui.theme.MayaTheme

@Composable
fun PrivacyPolicyScreen(
    userName: String = "Rizwan RR",
    onClose: () -> Unit,
    onOpenTermsAndConditions: () -> Unit = {},
    onDeleteAllOnDeviceData: (onComplete: () -> Unit) -> Unit = {},
    hapticEnabled: Boolean = true
) {
    val theme = MayaTheme.colors
    val context = LocalContext.current
    var showConfirmDeleteDialog by remember { mutableStateOf(false) }

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
                            text = "Privacy Policy",
                            style = TextStyle(
                                color = theme.textPrimary,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Text(
                            text = "Maya Connect • Data Protection & Security",
                            style = TextStyle(
                                color = theme.textSecondary,
                                fontSize = 11.5.sp
                            )
                        )
                    }
                }
            }

            // Scrollable Content
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 18.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Policy Info Banner
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
                            text = "MAYA CONNECT — PRIVACY POLICY",
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
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = theme.textSecondary,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Location: West Bengal, India",
                                style = TextStyle(
                                    color = theme.textSecondary,
                                    fontSize = 11.sp
                                )
                            )
                        }
                    }
                }

                // Section 1
                LegalSectionCard(
                    icon = Icons.Default.PermIdentity,
                    title = "1. Information Collection",
                    content = "Maya Connect may collect a user's name when required for profile display, personalization, or connectivity between supported REXLIFY applications.\n\nOther information (such as task checklists, quick notes, and app launch preferences) is stored locally in private on-device Room database storage only where necessary for implemented features and disclosed to users."
                )

                // Section 2
                LegalSectionCard(
                    icon = Icons.Default.WorkOutline,
                    title = "2. Use of Information",
                    content = "Information is used for the operation of Maya Connect, profile personalization, application connectivity, security, and technical support, as applicable."
                )

                // Section 3
                LegalSectionCard(
                    icon = Icons.Default.Security,
                    title = "3. User Privacy, Security & Abuse-Prevention Monitoring",
                    content = "Users are responsible for protecting their devices, passwords, and account credentials. Users should avoid submitting sensitive personal information unnecessarily.\n\nTo protect the REXLIFY ecosystem against brute-force attacks, SQL/XSS injection, and unauthorized access, Maya Connect logs security and abuse-prevention telemetry (event timestamp, affected module, user role/session correlation ID, device/OS version, and approximate IP-derived region/country with VPN/Proxy disclaimer). Plaintext passwords, OTPs, and session tokens are never logged. Maya Connect never secretly activates GPS, camera, or microphone for background tracking. Resolved security logs are retained for up to 30 days and restricted strictly to authorized Security & CEO Administrators."
                )

                // Section 4 (Crucial: Data Deletion & Working Process)
                LegalSectionCard(
                    icon = Icons.Default.DeleteOutline,
                    title = "4. Data Deletion",
                    content = "Users may stop using Maya Connect at any time. Uninstalling the Application removes it from the device but may not delete remotely stored data. Where applicable, users may request deletion of their personal information by contacting rexlifyofficialsupportteam@gmail.com.\n\nDeletion will be handled in accordance with applicable law and any legitimate retention requirements."
                )

                // Working Data Deletion Process Box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .glassmorphic(
                            shape = RoundedCornerShape(16.dp),
                            backgroundColor = Color(0x12EF4444),
                            borderColor = Color(0x44EF4444),
                            borderWidth = 1.dp
                        )
                        .padding(16.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.DeleteForever,
                                contentDescription = null,
                                tint = MayaColorError,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Working Data-Deletion Tools",
                                style = TextStyle(
                                    color = MayaColorError,
                                    fontSize = 13.5.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }

                        Text(
                            text = "You can initiate immediate on-device data clearing or dispatch a remote data deletion request directly to our privacy officer:",
                            style = TextStyle(
                                color = theme.textSecondary,
                                fontSize = 11.5.sp,
                                lineHeight = 16.sp
                            )
                        )

                        // Action 1: On-device data clearing
                        Button(
                            onClick = { showConfirmDeleteDialog = true },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MayaColorError)
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeleteForever,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Clear All On-Device Data Now",
                                style = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            )
                        }

                        // Action 2: Remote server deletion request email
                        OutlinedButton(
                            onClick = {
                                try {
                                    val intent = Intent(Intent.ACTION_SENDTO).apply {
                                        data = Uri.parse("mailto:rexlifyofficialsupportteam@gmail.com")
                                        putExtra(
                                            Intent.EXTRA_SUBJECT,
                                            "Maya Connect - Personal Data Deletion Request"
                                        )
                                        putExtra(
                                            Intent.EXTRA_TEXT,
                                            "Dear REXLIFY Privacy Team,\n\nI am formally requesting the complete deletion of any personal information associated with my use of Maya Connect under Section 4 of the Maya Connect Privacy Policy.\n\nUser Profile Name: $userName\nPlatform: Maya Connect Android\nDate: October 27, 2026\n\nPlease confirm when deletion is completed in accordance with applicable laws.\n\nThank you."
                                        )
                                    }
                                    context.startActivity(intent)
                                } catch (_: Exception) {
                                    Toast.makeText(context, "Email: rexlifyofficialsupportteam@gmail.com", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Email,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Email Remote Deletion Request",
                                style = TextStyle(fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold)
                            )
                        }
                    }
                }

                // Section 5
                LegalSectionCard(
                    icon = Icons.Default.Share,
                    title = "5. Data Sharing",
                    content = "REXLIFY does not intend to sell users' personal information. Information may be shared with supported connected applications or service providers only where necessary, appropriately disclosed, and legally permitted. Third-party services follow their own privacy policies."
                )

                // Section 6
                LegalSectionCard(
                    icon = Icons.Default.CreditCardOff,
                    title = "6. Financial Information",
                    content = "Maya Connect's current intended core functionality does not include processing payments, money transfers, or banking transactions."
                )

                // Section 7
                LegalSectionCard(
                    icon = Icons.Default.Security,
                    title = "7. Changes and Contact",
                    content = "This Policy may be updated when the Application or its data practices change. Users should review the latest version when appropriate.\n\nPrivacy Support: privacy@rexlify.com\nSecondary Support: rexlifyofficialsupportteam@gmail.com\nDeveloper / Publisher: REXLIFY\nLocation: West Bengal, India\n\nThis Policy reflects the Application's actual data collection and processing practices."
                )

                // Action Buttons Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 6.dp, bottom = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onOpenTermsAndConditions,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Description,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Terms & Conditions",
                            style = TextStyle(fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                        )
                    }

                    Button(
                        onClick = {
                            try {
                                val intent = Intent(Intent.ACTION_SENDTO).apply {
                                    data = Uri.parse("mailto:rexlifyofficialsupportteam@gmail.com")
                                    putExtra(Intent.EXTRA_SUBJECT, "Maya Connect - Privacy Inquiry")
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
                            text = "Email Privacy",
                            style = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        )
                    }
                }
            }
        }

        // Confirmation Dialog for On-Device Data Deletion
        if (showConfirmDeleteDialog) {
            AlertDialog(
                onDismissRequest = { showConfirmDeleteDialog = false },
                icon = {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = MayaColorError,
                        modifier = Modifier.size(32.dp)
                    )
                },
                title = {
                    Text(
                        text = "Delete On-Device Data?",
                        style = TextStyle(fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    )
                },
                text = {
                    Text(
                        text = "This will permanently remove all your local tasks, notes, search history, custom apps, and preferences stored in on-device Room database. Default ecosystem apps will be restored.\n\nDo you want to proceed?",
                        style = TextStyle(fontSize = 13.sp, lineHeight = 18.sp)
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showConfirmDeleteDialog = false
                            onDeleteAllOnDeviceData {
                                Toast.makeText(
                                    context,
                                    "On-device data successfully deleted and restored to defaults",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MayaColorError)
                    ) {
                        Text("Delete Everything")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showConfirmDeleteDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}
