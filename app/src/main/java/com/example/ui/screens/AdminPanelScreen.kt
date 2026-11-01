package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import com.example.ui.components.rememberChromeSmoothFlingBehavior
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.EcosystemLinkEntity
import com.example.data.local.SupportTicketEntity
import com.example.data.model.AppItem
import com.example.ui.components.glassmorphic
import com.example.ui.components.springPressEffect
import com.example.ui.theme.MayaTheme

import com.example.data.local.RegisteredUserEntity
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Upgrade
import androidx.compose.material.icons.filled.Warning

import com.example.data.local.AccountAuditLogEntity
import com.example.data.local.DataDeletionRequestEntity
import com.example.data.local.SecurityEventEntity
import com.example.data.auth.AuthCredentialsConfig
import com.example.data.model.EmergencyFeatureCatalog
import com.example.data.model.EmergencyFeatureItem
import com.example.data.model.EmergencyMode
import com.example.data.model.EmergencySystemState
import com.example.data.model.FamilyMemberAccount
import androidx.compose.material.icons.filled.Dangerous
import androidx.compose.material.icons.filled.Engineering
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material.icons.filled.LockClock
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Security

private enum class AdminTab(val title: String) {
    OVERVIEW("Overview"),
    EMERGENCY("Emergency Situation"),
    FAMILY("Family Members"),
    SECURITY("Security Center"),
    USERS("Users"),
    DELETION("Deletion Requests"),
    AUDIT("Audit Logs"),
    APPS("Apps"),
    SUPPORT("Support Desk"),
    BROADCAST("Broadcast"),
    ECOSYSTEM("Ecosystem")
}

@Composable
fun AdminPanelScreen(
    isAuthenticated: Boolean,
    loginError: String?,
    allAppsAdmin: List<AppItem>,
    allTickets: List<SupportTicketEntity>,
    openTicketsCount: Int,
    ecosystemLinks: List<EcosystemLinkEntity>,
    registeredUsers: List<RegisteredUserEntity> = emptyList(),
    deletionRequests: List<DataDeletionRequestEntity> = emptyList(),
    auditLogs: List<AccountAuditLogEntity> = emptyList(),
    securityEvents: List<SecurityEventEntity> = emptyList(),
    emergencyState: EmergencySystemState = EmergencySystemState(),
    familyMembers: List<FamilyMemberAccount> = emptyList(),
    onLogin: (String) -> Boolean,
    onLoginWithPasskey: (password: String, passkey: String) -> Boolean = { _, _ -> false },
    onVerifyPassword: (String) -> Boolean = { AuthCredentialsConfig.isValidAdminGatePassword(it) },
    onSetEmergencyMode: (EmergencyMode, String) -> Unit = { _, _ -> },
    onUpdateDisabledFeatures: (Set<String>) -> Unit = {},
    onToggleFeatureRestriction: (String) -> Unit = {},
    onUpdateCustomDurationHours: (Int) -> Unit = {},
    onCreateOrUpdateFamilyMember: (id: String, password: String, memberName: String, details: String) -> Unit = { _, _, _, _ -> },
    onToggleFamilyMemberActive: (String) -> Unit = {},
    onDeleteFamilyMember: (String) -> Unit = {},
    onLogout: () -> Unit,
    onClose: () -> Unit,
    onToggleAppEnabled: (AppItem) -> Unit,
    onEditApp: (AppItem) -> Unit,
    onAddApp: () -> Unit,
    onDeleteApp: (String) -> Unit,
    onAdminReplyTicket: (ticketId: String, reply: String, newStatus: String?) -> Unit,
    onResolveTicket: (String) -> Unit,
    onBroadcastAnnouncement: (title: String, message: String) -> Unit,
    onSaveEcosystemLink: (EcosystemLinkEntity) -> Unit,
    onDeleteEcosystemLink: (String) -> Unit,
    onIssueWarning: (userId: String, userName: String, reason: String) -> Unit = { _, _, _ -> },
    onToggleSuspend: (userId: String, currentStatus: String, userName: String) -> Unit = { _, _, _ -> },
    onSetUserStatus: (userId: String, userName: String, status: String, reason: String) -> Unit = { _, _, _, _ -> },
    onApproveDeletionRequest: (requestId: String) -> Unit = {},
    onRejectDeletionRequest: (requestId: String, reason: String) -> Unit = { _, _ -> },
    onUpgradeUserRole: (userId: String, newRole: String, userName: String) -> Unit = { _, _, _ -> },
    onSendMessageToUser: (userId: String, userName: String, title: String, message: String) -> Unit = { _, _, _, _ -> },
    onDeleteUser: (userId: String) -> Unit = {},
    onUpdateSecurityIncident: (
        eventId: String,
        newStatus: String,
        notes: String,
        actionDescription: String,
        isBlocked: Boolean
    ) -> Unit = { _, _, _, _, _ -> },
    onUnblockAllSecuritySources: () -> Unit = {},
    onRunSecuritySelfTest: () -> Unit = {},
    onPurgeOldResolvedSecurityEvents: () -> Unit = {},
    hapticEnabled: Boolean = true
) {
    BackHandler { onClose() }
    val theme = MayaTheme.colors

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(theme.backgroundGradient)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        if (!isAuthenticated) {
            // Password & 2FA Passkey Authentication Gate
            AdminLoginGate(
                loginError = loginError,
                onVerifyPassword = onVerifyPassword,
                onLoginWithPasskey = onLoginWithPasskey,
                onDirectLogin = onLogin,
                onClose = onClose
            )
        } else {
            // Authenticated Admin Dashboard
            AdminDashboardContent(
                allAppsAdmin = allAppsAdmin,
                allTickets = allTickets,
                openTicketsCount = openTicketsCount,
                ecosystemLinks = ecosystemLinks,
                registeredUsers = registeredUsers,
                deletionRequests = deletionRequests,
                auditLogs = auditLogs,
                securityEvents = securityEvents,
                emergencyState = emergencyState,
                familyMembers = familyMembers,
                onSetEmergencyMode = onSetEmergencyMode,
                onUpdateDisabledFeatures = onUpdateDisabledFeatures,
                onToggleFeatureRestriction = onToggleFeatureRestriction,
                onUpdateCustomDurationHours = onUpdateCustomDurationHours,
                onCreateOrUpdateFamilyMember = onCreateOrUpdateFamilyMember,
                onToggleFamilyMemberActive = onToggleFamilyMemberActive,
                onDeleteFamilyMember = onDeleteFamilyMember,
                onLogout = onLogout,
                onClose = onClose,
                onToggleAppEnabled = onToggleAppEnabled,
                onEditApp = onEditApp,
                onAddApp = onAddApp,
                onDeleteApp = onDeleteApp,
                onAdminReplyTicket = onAdminReplyTicket,
                onResolveTicket = onResolveTicket,
                onBroadcastAnnouncement = onBroadcastAnnouncement,
                onSaveEcosystemLink = onSaveEcosystemLink,
                onDeleteEcosystemLink = onDeleteEcosystemLink,
                onIssueWarning = onIssueWarning,
                onToggleSuspend = onToggleSuspend,
                onSetUserStatus = onSetUserStatus,
                onApproveDeletionRequest = onApproveDeletionRequest,
                onRejectDeletionRequest = onRejectDeletionRequest,
                onUpgradeUserRole = onUpgradeUserRole,
                onSendMessageToUser = onSendMessageToUser,
                onDeleteUser = onDeleteUser,
                onUpdateSecurityIncident = onUpdateSecurityIncident,
                onUnblockAllSecuritySources = onUnblockAllSecuritySources,
                onRunSecuritySelfTest = onRunSecuritySelfTest,
                onPurgeOldResolvedSecurityEvents = onPurgeOldResolvedSecurityEvents,
                hapticEnabled = hapticEnabled
            )
        }
    }
}

private enum class AdminGateStep {
    PASSWORD,
    PASSKEY
}

@Composable
private fun AdminLoginGate(
    loginError: String?,
    onVerifyPassword: (String) -> Boolean,
    onLoginWithPasskey: (password: String, passkey: String) -> Boolean,
    onDirectLogin: (String) -> Boolean,
    onClose: () -> Unit
) {
    val theme = MayaTheme.colors
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var stepError by remember { mutableStateOf<String?>(null) }

    val activeError = stepError ?: loginError

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
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
                .padding(24.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                listOf(Color(0xFF7C3AED), Color(0xFF4C1D95))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AdminPanelSettings,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Administrator Access",
                    style = TextStyle(
                        color = theme.textPrimary,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                )

                Text(
                    text = "Enter Admin Panel Password to Unlock Controls",
                    style = TextStyle(
                        color = theme.textSecondary,
                        fontSize = 12.sp
                    ),
                    modifier = Modifier.padding(top = 4.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = {
                        password = it
                        if (stepError != null) stepError = null
                    },
                    label = { Text("Admin Password") },
                    placeholder = { Text("Enter Admin Panel password") },
                    singleLine = true,
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = "Toggle password visibility",
                                tint = theme.textSecondary
                            )
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("admin_password_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = theme.accentPrimary,
                        unfocusedBorderColor = theme.borderGlass,
                        focusedTextColor = theme.textPrimary,
                        unfocusedTextColor = theme.textPrimary
                    ),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = {
                        val cleanPass = password.trim()
                        if (onDirectLogin(cleanPass)) {
                            stepError = null
                        } else {
                            stepError = "Invalid administrator password. Access denied."
                        }
                    })
                )

                if (activeError != null) {
                    Text(
                        text = activeError,
                        style = TextStyle(color = Color(0xFFEF4444), fontSize = 12.sp),
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = {
                        val cleanPass = password.trim()
                        if (onDirectLogin(cleanPass)) {
                            stepError = null
                        } else {
                            stepError = "Invalid administrator password. Access denied."
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("admin_login_submit"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF7C3AED)
                    )
                ) {
                    Text(
                        text = "Authenticate & Unlock",
                        style = TextStyle(
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Cancel",
                    style = TextStyle(
                        color = theme.textSecondary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    ),
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onClose() }
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                )
            }
        }
    }
}

@Composable
private fun AdminDashboardContent(
    allAppsAdmin: List<AppItem>,
    allTickets: List<SupportTicketEntity>,
    openTicketsCount: Int,
    ecosystemLinks: List<EcosystemLinkEntity>,
    registeredUsers: List<RegisteredUserEntity> = emptyList(),
    deletionRequests: List<DataDeletionRequestEntity> = emptyList(),
    auditLogs: List<AccountAuditLogEntity> = emptyList(),
    securityEvents: List<SecurityEventEntity> = emptyList(),
    emergencyState: EmergencySystemState = EmergencySystemState(),
    familyMembers: List<FamilyMemberAccount> = emptyList(),
    onSetEmergencyMode: (EmergencyMode, String) -> Unit = { _, _ -> },
    onUpdateDisabledFeatures: (Set<String>) -> Unit = {},
    onToggleFeatureRestriction: (String) -> Unit = {},
    onUpdateCustomDurationHours: (Int) -> Unit = {},
    onCreateOrUpdateFamilyMember: (id: String, password: String, memberName: String, details: String) -> Unit = { _, _, _, _ -> },
    onToggleFamilyMemberActive: (String) -> Unit = {},
    onDeleteFamilyMember: (String) -> Unit = {},
    onLogout: () -> Unit,
    onClose: () -> Unit,
    onToggleAppEnabled: (AppItem) -> Unit,
    onEditApp: (AppItem) -> Unit,
    onAddApp: () -> Unit,
    onDeleteApp: (String) -> Unit,
    onAdminReplyTicket: (ticketId: String, reply: String, newStatus: String?) -> Unit,
    onResolveTicket: (String) -> Unit,
    onBroadcastAnnouncement: (title: String, message: String) -> Unit,
    onSaveEcosystemLink: (EcosystemLinkEntity) -> Unit,
    onDeleteEcosystemLink: (String) -> Unit,
    onIssueWarning: (userId: String, userName: String, reason: String) -> Unit,
    onToggleSuspend: (userId: String, currentStatus: String, userName: String) -> Unit,
    onSetUserStatus: (userId: String, userName: String, status: String, reason: String) -> Unit,
    onApproveDeletionRequest: (requestId: String) -> Unit,
    onRejectDeletionRequest: (requestId: String, reason: String) -> Unit,
    onUpgradeUserRole: (userId: String, newRole: String, userName: String) -> Unit,
    onSendMessageToUser: (userId: String, userName: String, title: String, message: String) -> Unit,
    onDeleteUser: (userId: String) -> Unit,
    onUpdateSecurityIncident: (
        eventId: String,
        newStatus: String,
        notes: String,
        actionDescription: String,
        isBlocked: Boolean
    ) -> Unit = { _, _, _, _, _ -> },
    onUnblockAllSecuritySources: () -> Unit = {},
    onRunSecuritySelfTest: () -> Unit = {},
    onPurgeOldResolvedSecurityEvents: () -> Unit = {},
    hapticEnabled: Boolean = true
) {
    val theme = MayaTheme.colors
    var currentTab by remember { mutableStateOf(AdminTab.OVERVIEW) }

    Column(modifier = Modifier.fillMaxSize()) {
        // Admin Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF7C3AED).copy(alpha = 0.25f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AdminPanelSettings,
                        contentDescription = null,
                        tint = Color(0xFFA78BFA),
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Admin Panel",
                        style = TextStyle(
                            color = theme.textPrimary,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Text(
                        text = "Rizwan RR (Superuser • SecOps)",
                        style = TextStyle(
                            color = Color(0xFFA78BFA),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                // Logout
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(theme.surfaceGlass)
                        .border(1.dp, theme.borderGlass, CircleShape)
                        .clickable { onLogout() }
                        .testTag("admin_logout_btn"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Logout,
                        contentDescription = "Logout",
                        tint = Color(0xFFEF4444),
                        modifier = Modifier.size(17.dp)
                    )
                }

                // Close
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(theme.surfaceGlass)
                        .border(1.dp, theme.borderGlass, CircleShape)
                        .clickable { onClose() }
                        .testTag("admin_close_btn"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = theme.textPrimary,
                        modifier = Modifier.size(17.dp)
                    )
                }
            }
        }

        // Navigation Tabs Carousel
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            AdminTab.entries.forEach { tab ->
                val isSelected = tab == currentTab
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (isSelected) Color(0xFF7C3AED)
                            else theme.surfaceGlass
                        )
                        .border(
                            1.dp,
                            if (isSelected) Color(0xFFA78BFA) else theme.borderGlass,
                            RoundedCornerShape(12.dp)
                        )
                        .clickable { currentTab = tab }
                        .padding(horizontal = 14.dp, vertical = 7.dp)
                        .testTag("admin_tab_${tab.name.lowercase()}")
                ) {
                    Text(
                        text = tab.title,
                        style = TextStyle(
                            color = if (isSelected) Color.White else theme.textSecondary,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Tab Content
        Box(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
                .padding(horizontal = 16.dp)
        ) {
            when (currentTab) {
                AdminTab.OVERVIEW -> AdminOverviewTab(
                    allAppsAdmin = allAppsAdmin,
                    allTickets = allTickets,
                    openTicketsCount = openTicketsCount,
                    ecosystemLinks = ecosystemLinks,
                    registeredUsers = registeredUsers,
                    emergencyState = emergencyState,
                    onNavigateToEmergency = { currentTab = AdminTab.EMERGENCY }
                )
                AdminTab.EMERGENCY -> EmergencySituationTab(
                    emergencyState = emergencyState,
                    onSetEmergencyMode = onSetEmergencyMode,
                    onUpdateDisabledFeatures = onUpdateDisabledFeatures,
                    onToggleFeatureRestriction = onToggleFeatureRestriction,
                    onUpdateCustomDurationHours = onUpdateCustomDurationHours
                )
                AdminTab.FAMILY -> FamilyMembersAdminTab(
                    familyMembers = familyMembers,
                    onCreateOrUpdateFamilyMember = onCreateOrUpdateFamilyMember,
                    onToggleFamilyMemberActive = onToggleFamilyMemberActive,
                    onDeleteFamilyMember = onDeleteFamilyMember
                )
                AdminTab.SECURITY -> SecurityCenterSection(
                    securityEvents = securityEvents,
                    onUpdateIncidentState = onUpdateSecurityIncident,
                    onUnblockAllSources = onUnblockAllSecuritySources,
                    onRunSecuritySelfTest = onRunSecuritySelfTest,
                    onPurgeOldResolvedEvents = onPurgeOldResolvedSecurityEvents
                )
                AdminTab.USERS -> AdminUsersTab(
                    users = registeredUsers,
                    onIssueWarning = onIssueWarning,
                    onToggleSuspend = onToggleSuspend,
                    onSetUserStatus = onSetUserStatus,
                    onUpgradeUserRole = onUpgradeUserRole,
                    onSendMessageToUser = onSendMessageToUser,
                    onDeleteUser = onDeleteUser
                )
                AdminTab.DELETION -> AdminDeletionTab(
                    requests = deletionRequests,
                    onApprove = onApproveDeletionRequest,
                    onReject = onRejectDeletionRequest
                )
                AdminTab.AUDIT -> AdminAuditTab(
                    logs = auditLogs
                )
                AdminTab.APPS -> AdminAppsTab(
                    apps = allAppsAdmin,
                    onToggleEnabled = onToggleAppEnabled,
                    onEdit = onEditApp,
                    onAdd = onAddApp,
                    onDelete = onDeleteApp
                )
                AdminTab.SUPPORT -> AdminSupportTab(
                    tickets = allTickets,
                    onReply = onAdminReplyTicket,
                    onResolve = onResolveTicket
                )
                AdminTab.BROADCAST -> AdminBroadcastTab(
                    onSend = onBroadcastAnnouncement
                )
                AdminTab.ECOSYSTEM -> AdminEcosystemTab(
                    links = ecosystemLinks,
                    onSave = onSaveEcosystemLink,
                    onDelete = onDeleteEcosystemLink
                )
            }
        }
    }
}

@Composable
private fun AdminOverviewTab(
    allAppsAdmin: List<AppItem>,
    allTickets: List<SupportTicketEntity>,
    openTicketsCount: Int,
    ecosystemLinks: List<EcosystemLinkEntity>,
    registeredUsers: List<RegisteredUserEntity> = emptyList(),
    emergencyState: EmergencySystemState = EmergencySystemState(),
    onNavigateToEmergency: () -> Unit = {}
) {
    val theme = MayaTheme.colors
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Emergency Status Banner Card
        val (badgeTitle, badgeColor, badgeSub) = when (emergencyState.activeMode) {
            EmergencyMode.NORMAL -> Triple(
                "NORMAL OPERATIONAL MODE",
                Color(0xFF10B981),
                "All systems and application modules are operating normally."
            )
            EmergencyMode.MAINTENANCE_FREEZE -> Triple(
                "OPTION 1 ACTIVE: SYSTEM FREEZE",
                Color(0xFFEF4444),
                "System is completely frozen in real-time. Maintenance alert active."
            )
            EmergencyMode.SELECTIVE_FEATURE_CONTROL -> Triple(
                "OPTION 2 ACTIVE: SELECTIVE CONTROL",
                Color(0xFFF59E0B),
                "${emergencyState.disabledFeatureIds.size} modules restricted by Admin Rizwan RR."
            )
            EmergencyMode.HIGH_SECURITY_LOCKDOWN -> Triple(
                "OPTION 3 ACTIVE: HIGH-SECURITY LOCKDOWN",
                Color(0xFFDC2626),
                "Major maintenance window active. Public services suspended."
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(badgeColor.copy(alpha = 0.15f))
                .border(1.dp, badgeColor.copy(alpha = 0.4f), RoundedCornerShape(18.dp))
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(badgeColor.copy(alpha = 0.25f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = null,
                            tint = badgeColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = badgeTitle,
                            style = TextStyle(
                                color = badgeColor,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Text(
                            text = badgeSub,
                            style = TextStyle(
                                color = theme.textSecondary,
                                fontSize = 11.5.sp
                            )
                        )
                    }
                }
                Button(
                    onClick = onNavigateToEmergency,
                    colors = ButtonDefaults.buttonColors(containerColor = badgeColor),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "Manage",
                        style = TextStyle(
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }
        }
        // Metrics Grid (2x2)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MetricCard(
                title = "Total Users",
                value = "${registeredUsers.size}",
                sub = "${registeredUsers.count { it.status == "ACTIVE" }} active",
                color = Color(0xFF8B5CF6),
                modifier = Modifier.weight(1f)
            )
            MetricCard(
                title = "Total Apps",
                value = "${allAppsAdmin.size}",
                sub = "${allAppsAdmin.count { it.isEnabled }} active",
                color = Color(0xFF0284C7),
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MetricCard(
                title = "Open Tickets",
                value = "$openTicketsCount",
                sub = "${allTickets.size} total submitted",
                color = if (openTicketsCount > 0) Color(0xFFEF4444) else Color(0xFF10B981),
                modifier = Modifier.weight(1f)
            )
            MetricCard(
                title = "Ecosystem Portals",
                value = "${ecosystemLinks.size}",
                sub = "Official REXLIFY directory",
                color = Color(0xFF7C3AED),
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MetricCard(
                title = "System Health",
                value = "100%",
                sub = "Room DB v6 Operational",
                color = Color(0xFF10B981),
                modifier = Modifier.weight(1f)
            )
            MetricCard(
                title = "Security Clearance",
                value = "CEO Tier",
                sub = "Superuser Rizwan RR",
                color = Color(0xFF38BDF8),
                modifier = Modifier.weight(1f)
            )
        }

        // Quick Admin Tips
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
                .padding(16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "Ecosystem Control Info",
                    style = TextStyle(
                        color = theme.textPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
                Text(
                    text = "• Changes made here to Apps and Portals synchronize across the app.\n" +
                            "• Support replies appear instantly in the user's Customer Care desk.\n" +
                            "• Broadcast announcements trigger notifications for all connected nodes.",
                    style = TextStyle(
                        color = theme.textSecondary,
                        fontSize = 12.sp,
                        lineHeight = 18.sp
                    )
                )
            }
        }
    }
}

@Composable
private fun MetricCard(
    title: String,
    value: String,
    sub: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    val theme = MayaTheme.colors
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .glassmorphic(
                shape = RoundedCornerShape(18.dp),
                backgroundColor = theme.surfaceGlass,
                borderColor = theme.borderGlass,
                borderWidth = 1.dp
            )
            .padding(14.dp)
    ) {
        Column {
            Text(
                text = title,
                style = TextStyle(color = theme.textSecondary, fontSize = 11.5.sp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = TextStyle(
                    color = color,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black
                )
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = sub,
                style = TextStyle(color = theme.textSecondary.copy(alpha = 0.8f), fontSize = 10.sp)
            )
        }
    }
}

@Composable
private fun AdminAppsTab(
    apps: List<AppItem>,
    onToggleEnabled: (AppItem) -> Unit,
    onEdit: (AppItem) -> Unit,
    onAdd: () -> Unit,
    onDelete: (String) -> Unit
) {
    val theme = MayaTheme.colors
    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Manage Apps (${apps.size})",
                style = TextStyle(
                    color = theme.textPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            )
            Button(
                onClick = onAdd,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Add App", style = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Bold))
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            flingBehavior = rememberChromeSmoothFlingBehavior(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(bottom = 30.dp)
        ) {
            items(apps, key = { it.id }) { app ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .glassmorphic(
                            shape = RoundedCornerShape(14.dp),
                            backgroundColor = theme.surfaceGlass,
                            borderColor = theme.borderGlass,
                            borderWidth = 1.dp
                        )
                        .padding(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = app.name,
                                    style = TextStyle(
                                        color = theme.textPrimary,
                                        fontSize = 13.5.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                                if (app.isCustom) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Custom",
                                        style = TextStyle(color = Color(0xFF38BDF8), fontSize = 9.sp)
                                    )
                                }
                            }
                            Text(
                                text = app.url,
                                style = TextStyle(color = theme.textSecondary, fontSize = 11.sp),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Switch(
                                checked = app.isEnabled,
                                onCheckedChange = { onToggleEnabled(app) },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = Color(0xFF10B981)
                                )
                            )
                            IconButton(onClick = { onEdit(app) }) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Edit",
                                    tint = theme.textSecondary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            if (app.isCustom) {
                                IconButton(onClick = { onDelete(app.id) }) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Delete",
                                        tint = Color(0xFFEF4444),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AdminSupportTab(
    tickets: List<SupportTicketEntity>,
    onReply: (ticketId: String, reply: String, newStatus: String?) -> Unit,
    onResolve: (String) -> Unit
) {
    val theme = MayaTheme.colors
    var replyDialogTicket by remember { mutableStateOf<SupportTicketEntity?>(null) }
    var replyMessage by remember { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        flingBehavior = rememberChromeSmoothFlingBehavior(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(top = 8.dp, bottom = 40.dp)
    ) {
        if (tickets.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No user support tickets found.",
                        style = TextStyle(color = theme.textSecondary, fontSize = 13.sp)
                    )
                }
            }
        }

        items(tickets, key = { it.id }) { ticket ->
            val isOpen = ticket.status == "OPEN"
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
                    .padding(14.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = ticket.subject,
                            style = TextStyle(
                                color = theme.textPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isOpen) Color(0x30EF4444) else Color(0x3010B981))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = ticket.status,
                                style = TextStyle(
                                    color = if (isOpen) Color(0xFFEF4444) else Color(0xFF10B981),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }

                    Text(
                        text = "By ${ticket.userDisplayName} • Category: ${ticket.category}",
                        style = TextStyle(color = theme.textSecondary, fontSize = 11.sp)
                    )

                    Text(
                        text = ticket.initialMessage,
                        style = TextStyle(color = theme.textPrimary, fontSize = 12.5.sp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        if (isOpen) {
                            Button(
                                onClick = { onResolve(ticket.id) },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text("Mark Resolved", style = TextStyle(fontSize = 11.sp))
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        Button(
                            onClick = {
                                replyDialogTicket = ticket
                                replyMessage = ""
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C3AED)),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                        ) {
                            Text("Send Admin Reply", style = TextStyle(fontSize = 11.sp))
                        }
                    }
                }
            }
        }
    }

    // Admin Reply Dialog
    replyDialogTicket?.let { ticket ->
        androidx.compose.ui.window.Dialog(onDismissRequest = { replyDialogTicket = null }) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(22.dp))
                    .background(Color(0xFF1E1B4B))
                    .border(1.dp, Color(0xFF8B5CF6), RoundedCornerShape(22.dp))
                    .padding(20.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Reply to Ticket: ${ticket.subject}",
                        style = TextStyle(color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    )
                    OutlinedTextField(
                        value = replyMessage,
                        onValueChange = { replyMessage = it },
                        placeholder = { Text("Type official resolution or reply...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFFA78BFA),
                            unfocusedBorderColor = Color(0xFF4C1D95),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Text(
                            text = "Cancel",
                            color = Color(0xFF94A3B8),
                            modifier = Modifier
                                .clickable { replyDialogTicket = null }
                                .padding(10.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (replyMessage.isNotBlank()) {
                                    onReply(ticket.id, replyMessage, "RESOLVED")
                                    replyDialogTicket = null
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8B5CF6))
                        ) {
                            Text("Send & Resolve")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AdminBroadcastTab(
    onSend: (title: String, message: String) -> Unit
) {
    val theme = MayaTheme.colors
    var title by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    var sentSuccess by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(
            text = "Broadcast Announcement",
            style = TextStyle(
                color = theme.textPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        )
        Text(
            text = "Sends a push-style announcement to all user nodes inside Maya Connect.",
            style = TextStyle(color = theme.textSecondary, fontSize = 12.sp)
        )

        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Announcement Title") },
            placeholder = { Text("e.g. Maya OS v3.2 System Update") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = theme.accentPrimary,
                unfocusedBorderColor = theme.borderGlass,
                focusedTextColor = theme.textPrimary,
                unfocusedTextColor = theme.textPrimary
            )
        )

        OutlinedTextField(
            value = message,
            onValueChange = { message = it },
            label = { Text("Announcement Message") },
            placeholder = { Text("Details of changes, new ecosystem nodes, or maintenance...") },
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = theme.accentPrimary,
                unfocusedBorderColor = theme.borderGlass,
                focusedTextColor = theme.textPrimary,
                unfocusedTextColor = theme.textPrimary
            )
        )

        Button(
            onClick = {
                if (title.isNotBlank() && message.isNotBlank()) {
                    onSend(title, message)
                    title = ""
                    message = ""
                    sentSuccess = true
                }
            },
            modifier = Modifier.fillMaxWidth().height(48.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C3AED))
        ) {
            Icon(imageVector = Icons.Default.Campaign, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Send Announcement", style = TextStyle(fontWeight = FontWeight.Bold))
        }

        if (sentSuccess) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0x2010B981))
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF10B981))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Broadcast transmitted successfully to all nodes.",
                    style = TextStyle(color = Color(0xFF10B981), fontSize = 12.5.sp, fontWeight = FontWeight.Bold)
                )
            }
        }
    }
}

@Composable
private fun AdminEcosystemTab(
    links: List<EcosystemLinkEntity>,
    onSave: (EcosystemLinkEntity) -> Unit,
    onDelete: (String) -> Unit
) {
    val theme = MayaTheme.colors
    var showDialog by remember { mutableStateOf(false) }
    var editingLink by remember { mutableStateOf<EcosystemLinkEntity?>(null) }
    var title by remember { mutableStateOf("") }
    var url by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Ecosystem Directory (${links.size})",
                style = TextStyle(color = theme.textPrimary, fontSize = 15.sp, fontWeight = FontWeight.Bold)
            )
            Button(
                onClick = {
                    editingLink = null
                    title = ""
                    url = "https://"
                    description = ""
                    showDialog = true
                },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C3AED)),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Add Portal", style = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Bold))
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            flingBehavior = rememberChromeSmoothFlingBehavior(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(bottom = 30.dp)
        ) {
            items(links, key = { it.id }) { link ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .glassmorphic(
                            shape = RoundedCornerShape(14.dp),
                            backgroundColor = theme.surfaceGlass,
                            borderColor = theme.borderGlass,
                            borderWidth = 1.dp
                        )
                        .padding(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = link.name,
                                style = TextStyle(color = theme.textPrimary, fontSize = 13.5.sp, fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = link.url,
                                style = TextStyle(color = theme.textSecondary, fontSize = 11.sp),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Row {
                            IconButton(onClick = {
                                editingLink = link
                                title = link.name
                                url = link.url
                                description = link.description
                                showDialog = true
                            }) {
                                Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit", tint = theme.textSecondary, modifier = Modifier.size(18.dp))
                            }
                            IconButton(onClick = { onDelete(link.id) }) {
                                Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFEF4444), modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            }
        }
    }

    if (showDialog) {
        androidx.compose.ui.window.Dialog(onDismissRequest = { showDialog = false }) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(22.dp))
                    .background(Color(0xFF1E1B4B))
                    .border(1.dp, Color(0xFF8B5CF6), RoundedCornerShape(22.dp))
                    .padding(20.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = if (editingLink == null) "Add Portal" else "Edit Portal",
                        style = TextStyle(color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    )
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Portal Title") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFFA78BFA),
                            unfocusedBorderColor = Color(0xFF4C1D95),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )
                    OutlinedTextField(
                        value = url,
                        onValueChange = { url = it },
                        label = { Text("URL") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFFA78BFA),
                            unfocusedBorderColor = Color(0xFF4C1D95),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Description") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFFA78BFA),
                            unfocusedBorderColor = Color(0xFF4C1D95),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Text(
                            text = "Cancel",
                            color = Color(0xFF94A3B8),
                            modifier = Modifier
                                .clickable { showDialog = false }
                                .padding(10.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (title.isNotBlank() && url.isNotBlank()) {
                                    val newEntity = editingLink?.copy(
                                        name = title,
                                        url = url,
                                        description = description
                                    ) ?: EcosystemLinkEntity(
                                        id = "custom_${System.currentTimeMillis()}",
                                        name = title,
                                        url = url,
                                        description = description
                                    )
                                    onSave(newEntity)
                                    showDialog = false
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8B5CF6))
                        ) {
                            Text("Save Portal")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AdminUsersTab(
    users: List<RegisteredUserEntity>,
    onIssueWarning: (userId: String, userName: String, reason: String) -> Unit,
    onToggleSuspend: (userId: String, currentStatus: String, userName: String) -> Unit,
    onSetUserStatus: (userId: String, userName: String, status: String, reason: String) -> Unit,
    onUpgradeUserRole: (userId: String, newRole: String, userName: String) -> Unit,
    onSendMessageToUser: (userId: String, userName: String, title: String, message: String) -> Unit,
    onDeleteUser: (userId: String) -> Unit
) {
    val theme = MayaTheme.colors
    var searchQuery by remember { mutableStateOf("") }
    var selectedStatusFilter by remember { mutableStateOf("ALL") }

    // Dialog States
    var userForCeoView by remember { mutableStateOf<RegisteredUserEntity?>(null) }
    var userForWarning by remember { mutableStateOf<RegisteredUserEntity?>(null) }
    var warningReason by remember { mutableStateOf("") }

    var userForMessage by remember { mutableStateOf<RegisteredUserEntity?>(null) }
    var messageTitle by remember { mutableStateOf("") }
    var messageBody by remember { mutableStateOf("") }

    var userForStatusChange by remember { mutableStateOf<Pair<RegisteredUserEntity, String>?>(null) }
    var statusReason by remember { mutableStateOf("") }

    val filteredUsers = remember(users, searchQuery, selectedStatusFilter) {
        users.filter { user ->
            val matchesSearch = searchQuery.isBlank() ||
                    user.name.contains(searchQuery, ignoreCase = true) ||
                    user.userId.contains(searchQuery, ignoreCase = true)
            val matchesStatus = when (selectedStatusFilter) {
                "ACTIVE" -> user.status.equals("ACTIVE", ignoreCase = true)
                "WARNING" -> user.status.equals("WARNING", ignoreCase = true)
                "SUSPENDED" -> user.status.equals("SUSPENDED", ignoreCase = true)
                "HOLD" -> user.status.equals("HOLD", ignoreCase = true)
                "DEACTIVATED" -> user.status.equals("DEACTIVATED", ignoreCase = true)
                else -> true
            }
            matchesSearch && matchesStatus
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Search Input
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search users by name or ID...", color = theme.textSecondary) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF8B5CF6),
                unfocusedBorderColor = theme.borderGlass,
                focusedTextColor = theme.textPrimary,
                unfocusedTextColor = theme.textPrimary
            ),
            shape = RoundedCornerShape(14.dp)
        )

        // Status Filter Chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("ALL" to "All Users", "ACTIVE" to "Active", "SUSPENDED" to "Suspended", "HOLD" to "Hold", "DEACTIVATED" to "Deactivated").forEach { (filterKey, label) ->
                val isSelected = selectedStatusFilter == filterKey
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isSelected) Color(0xFF8B5CF6) else theme.surfaceGlass)
                        .border(1.dp, if (isSelected) Color(0xFFA78BFA) else theme.borderGlass, RoundedCornerShape(10.dp))
                        .clickable { selectedStatusFilter = filterKey }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = label,
                        style = TextStyle(
                            color = if (isSelected) Color.White else theme.textSecondary,
                            fontSize = 11.5.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        )
                    )
                }
            }
        }

        // Users List
        if (filteredUsers.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No matching users found",
                    color = theme.textSecondary,
                    fontSize = 13.sp
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                flingBehavior = rememberChromeSmoothFlingBehavior(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredUsers, key = { it.userId }) { user ->
                    UserManagementCard(
                        user = user,
                        onViewCeoModeClick = { userForCeoView = user },
                        onSetStatusClick = { targetStatus ->
                            userForStatusChange = Pair(user, targetStatus)
                            statusReason = "Administrative review by CEO (Rizwan RR)"
                        },
                        onIssueWarningClick = {
                            userForWarning = user
                            warningReason = "Adhere to REXLIFY Community Terms"
                        },
                        onUpgradeRoleClick = {
                            onUpgradeUserRole(user.userId, "RRR_FAMILY", user.name)
                        },
                        onSendMessageClick = {
                            userForMessage = user
                            messageTitle = "Admin Announcement"
                            messageBody = ""
                        },
                        onDeleteClick = {
                            onDeleteUser(user.userId)
                        }
                    )
                }
            }
        }
    }

    // CEO Mode User Profile Inspector Dialog
    if (userForCeoView != null) {
        val target = userForCeoView!!
        androidx.compose.ui.window.Dialog(onDismissRequest = { userForCeoView = null }) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(22.dp))
                    .background(Color(0xFF0F172A))
                    .border(1.5.dp, Color(0xFF38BDF8), RoundedCornerShape(22.dp))
                    .padding(20.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Shield,
                                contentDescription = null,
                                tint = Color(0xFF38BDF8),
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "CEO MODE INSPECTOR",
                                style = TextStyle(
                                    color = Color(0xFF38BDF8),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                )
                            )
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF1E293B))
                                .clickable { userForCeoView = null }
                                .padding(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    HorizontalDivider(color = Color(0xFF334155))

                    // User Identity Block
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF0284C7).copy(alpha = 0.2f))
                                .border(1.5.dp, Color(0xFF0284C7), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = target.name.take(1).uppercase(),
                                style = TextStyle(color = Color(0xFF38BDF8), fontSize = 20.sp, fontWeight = FontWeight.Bold)
                            )
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column {
                            Text(
                                text = target.name,
                                style = TextStyle(color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "ID: ${target.userId}",
                                style = TextStyle(color = Color(0xFF94A3B8), fontSize = 12.sp)
                            )
                            Text(
                                text = "Role: ${target.role} • Status: ${target.status}",
                                style = TextStyle(color = Color(0xFF38BDF8), fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold)
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0xFF1E293B))
                            .padding(12.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = "Real-Time Telemetry & User Tracking",
                                style = TextStyle(color = Color.White, fontSize = 12.5.sp, fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "• Active Session Node: Connected\n" +
                                        "• Warning Count: ${target.warningCount}\n" +
                                        "• Registered Date: ${if (target.registeredAt > 0) java.text.SimpleDateFormat("MMM dd, yyyy HH:mm", java.util.Locale.getDefault()).format(java.util.Date(target.registeredAt)) else "Recent"}\n" +
                                        "• Database Security: Verified Real-Time Record",
                                style = TextStyle(color = Color(0xFF94A3B8), fontSize = 11.5.sp, lineHeight = 17.sp)
                            )
                        }
                    }

                    // Actions Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = {
                                userForMessage = target
                                messageTitle = "CEO Direct Communication"
                                messageBody = "Notice from Founder & CEO Rizwan RR."
                                userForCeoView = null
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Send Message", color = Color.White, fontSize = 12.sp)
                        }

                        Button(
                            onClick = { userForCeoView = null },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF38BDF8)),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("← Exit CEO Mode", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }

    // Status Change Dialog (Suspend, Hold, Deactivate, Reactivate)
    if (userForStatusChange != null) {
        val (targetUser, targetStatus) = userForStatusChange!!
        androidx.compose.ui.window.Dialog(onDismissRequest = { userForStatusChange = null }) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xFF0F172A))
                    .border(1.dp, Color(0xFF8B5CF6), RoundedCornerShape(20.dp))
                    .padding(20.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Set Status for ${targetUser.name}: $targetStatus",
                        style = TextStyle(color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "Provide reason for changing account status to $targetStatus:",
                        style = TextStyle(color = Color(0xFF94A3B8), fontSize = 12.sp)
                    )
                    OutlinedTextField(
                        value = statusReason,
                        onValueChange = { statusReason = it },
                        label = { Text("Action Reason") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF8B5CF6),
                            unfocusedBorderColor = Color(0xFF334155),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Text(
                            text = "Cancel",
                            color = Color(0xFF94A3B8),
                            modifier = Modifier
                                .clickable { userForStatusChange = null }
                                .padding(10.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (statusReason.isNotBlank()) {
                                    onSetUserStatus(targetUser.userId, targetUser.name, targetStatus, statusReason.trim())
                                    userForStatusChange = null
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8B5CF6))
                        ) {
                            Text("Confirm $targetStatus", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    // Warning Dialog
    if (userForWarning != null) {
        val target = userForWarning!!
        androidx.compose.ui.window.Dialog(onDismissRequest = { userForWarning = null }) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xFF0F172A))
                    .border(1.dp, Color(0xFFF59E0B), RoundedCornerShape(20.dp))
                    .padding(20.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Issue Warning to ${target.name}",
                        style = TextStyle(color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "This will increment warning count and notify the user.",
                        style = TextStyle(color = Color(0xFF94A3B8), fontSize = 12.sp)
                    )
                    OutlinedTextField(
                        value = warningReason,
                        onValueChange = { warningReason = it },
                        label = { Text("Reason for Warning") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFFF59E0B),
                            unfocusedBorderColor = Color(0xFF334155),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Text(
                            text = "Cancel",
                            color = Color(0xFF94A3B8),
                            modifier = Modifier
                                .clickable { userForWarning = null }
                                .padding(10.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (warningReason.isNotBlank()) {
                                    onIssueWarning(target.userId, target.name, warningReason.trim())
                                    userForWarning = null
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF59E0B))
                        ) {
                            Text("Issue Warning", color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    // Direct Message Dialog
    if (userForMessage != null) {
        val target = userForMessage!!
        androidx.compose.ui.window.Dialog(onDismissRequest = { userForMessage = null }) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xFF0F172A))
                    .border(1.dp, Color(0xFF38BDF8), RoundedCornerShape(20.dp))
                    .padding(20.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Direct Message: ${target.name}",
                        style = TextStyle(color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    )
                    OutlinedTextField(
                        value = messageTitle,
                        onValueChange = { messageTitle = it },
                        label = { Text("Title") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF38BDF8),
                            unfocusedBorderColor = Color(0xFF334155),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )
                    OutlinedTextField(
                        value = messageBody,
                        onValueChange = { messageBody = it },
                        label = { Text("Message Body") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF38BDF8),
                            unfocusedBorderColor = Color(0xFF334155),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Text(
                            text = "Cancel",
                            color = Color(0xFF94A3B8),
                            modifier = Modifier
                                .clickable { userForMessage = null }
                                .padding(10.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (messageBody.isNotBlank()) {
                                    onSendMessageToUser(target.userId, target.name, messageTitle.ifBlank { "Admin Notice" }, messageBody.trim())
                                    userForMessage = null
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7))
                        ) {
                            Text("Send Notification", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun UserManagementCard(
    user: RegisteredUserEntity,
    onViewCeoModeClick: () -> Unit = {},
    onSetStatusClick: (status: String) -> Unit,
    onIssueWarningClick: () -> Unit,
    onUpgradeRoleClick: () -> Unit,
    onSendMessageClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val theme = MayaTheme.colors
    val status = user.status.uppercase()

    val roleColor = when (user.role) {
        "CEO_PANEL" -> Color(0xFF38BDF8)
        "RRR_FAMILY" -> Color(0xFFA855F7)
        else -> Color(0xFF94A3B8)
    }

    val roleLabel = when (user.role) {
        "CEO_PANEL" -> "CEO Panel"
        "RRR_FAMILY" -> "RRR Family"
        else -> "Standard Guest"
    }

    val statusColor = when (status) {
        "SUSPENDED", "DEACTIVATED" -> Color(0xFFEF4444)
        "HOLD", "WARNING" -> Color(0xFFF59E0B)
        else -> Color(0xFF10B981)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .glassmorphic(
                shape = RoundedCornerShape(18.dp),
                backgroundColor = theme.surfaceGlass,
                borderColor = if (status != "ACTIVE") statusColor.copy(alpha = 0.5f) else theme.borderGlass,
                borderWidth = 1.dp
            )
            .padding(14.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            // User Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(roleColor.copy(alpha = 0.2f))
                            .border(1.5.dp, roleColor.copy(alpha = 0.6f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = user.name.take(1).uppercase(),
                            style = TextStyle(color = roleColor, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = user.name,
                            style = TextStyle(color = theme.textPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = user.userId,
                            style = TextStyle(color = theme.textSecondary, fontSize = 11.sp)
                        )
                    }
                }

                // Badges
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(roleColor.copy(alpha = 0.15f))
                            .border(1.dp, roleColor.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(text = roleLabel, style = TextStyle(color = roleColor, fontSize = 10.5.sp, fontWeight = FontWeight.Bold))
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(statusColor.copy(alpha = 0.15f))
                            .border(1.dp, statusColor.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(text = status, style = TextStyle(color = statusColor, fontSize = 10.5.sp, fontWeight = FontWeight.Bold))
                    }
                }
            }

            // Interactive Action Buttons Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // View CEO Mode Profile Inspector Button
                ActionButton(text = "View CEO Mode", color = Color(0xFF38BDF8), icon = Icons.Default.Visibility, onClick = onViewCeoModeClick)
                // Status Control Buttons
                if (status != "ACTIVE") {
                    ActionButton(text = "Reactivate", color = Color(0xFF10B981), icon = Icons.Default.CheckCircle) {
                        onSetStatusClick("ACTIVE")
                    }
                }
                if (status != "SUSPENDED") {
                    ActionButton(text = "Suspend", color = Color(0xFFEF4444), icon = Icons.Default.Block) {
                        onSetStatusClick("SUSPENDED")
                    }
                }
                if (status != "HOLD") {
                    ActionButton(text = "Hold", color = Color(0xFFF59E0B), icon = Icons.Default.Warning) {
                        onSetStatusClick("HOLD")
                    }
                }
                if (status != "DEACTIVATED") {
                    ActionButton(text = "Deactivate", color = Color(0xFF64748B), icon = Icons.Default.Lock) {
                        onSetStatusClick("DEACTIVATED")
                    }
                }

                // Warn
                ActionButton(text = "Warn", color = Color(0xFFF59E0B), icon = Icons.Default.Warning, onClick = onIssueWarningClick)

                // Make Family
                if (user.role == "STANDARD") {
                    ActionButton(text = "Make Family", color = Color(0xFFA855F7), icon = Icons.Default.Upgrade, onClick = onUpgradeRoleClick)
                }

                // Message
                ActionButton(text = "Message", color = Color(0xFF38BDF8), icon = Icons.Default.Message, onClick = onSendMessageClick)

                // Delete
                ActionButton(text = "Delete", color = Color(0xFF64748B), icon = Icons.Default.Delete, onClick = onDeleteClick)
            }
        }
    }
}

@Composable
private fun AdminDeletionTab(
    requests: List<DataDeletionRequestEntity>,
    onApprove: (requestId: String) -> Unit,
    onReject: (requestId: String, reason: String) -> Unit
) {
    val theme = MayaTheme.colors
    var rejectRequestDialog by remember { mutableStateOf<DataDeletionRequestEntity?>(null) }
    var rejectReason by remember { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        flingBehavior = rememberChromeSmoothFlingBehavior(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(top = 8.dp, bottom = 40.dp)
    ) {
        if (requests.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No user data deletion requests submitted.",
                        style = TextStyle(color = theme.textSecondary, fontSize = 13.sp)
                    )
                }
            }
        }

        items(requests, key = { it.id }) { request ->
            val isPending = request.status == "PENDING"
            val badgeColor = when (request.status) {
                "PENDING" -> Color(0xFFF59E0B)
                "APPROVED", "COMPLETED" -> Color(0xFF10B981)
                else -> Color(0xFFEF4444)
            }

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
                    .padding(14.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Request ${request.id} • ${request.userName}",
                            style = TextStyle(color = theme.textPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(badgeColor.copy(alpha = 0.2f))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = request.status,
                                style = TextStyle(color = badgeColor, fontSize = 10.5.sp, fontWeight = FontWeight.Bold)
                            )
                        }
                    }

                    Text(
                        text = "User ID: ${request.userId} • Submitted: ${java.text.SimpleDateFormat("MMM dd, yyyy HH:mm", java.util.Locale.getDefault()).format(java.util.Date(request.requestDate))}",
                        style = TextStyle(color = theme.textSecondary, fontSize = 11.sp)
                    )

                    Text(
                        text = "Reason: ${request.reason}",
                        style = TextStyle(color = theme.textPrimary, fontSize = 12.sp)
                    )

                    if (!request.rejectionReason.isNullOrBlank()) {
                        Text(
                            text = "Rejection Reason: ${request.rejectionReason}",
                            style = TextStyle(color = Color(0xFFEF4444), fontSize = 11.5.sp)
                        )
                    }

                    if (isPending) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            Button(
                                onClick = {
                                    rejectRequestDialog = request
                                    rejectReason = "Insufficient verification / duplicate request."
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text("Reject Request", style = TextStyle(fontSize = 11.sp, fontWeight = FontWeight.Bold))
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = { onApprove(request.id) },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                            ) {
                                Text("Approve & Delete Data", style = TextStyle(fontSize = 11.sp, fontWeight = FontWeight.Bold))
                            }
                        }
                    }
                }
            }
        }
    }

    rejectRequestDialog?.let { request ->
        androidx.compose.ui.window.Dialog(onDismissRequest = { rejectRequestDialog = null }) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xFF0F172A))
                    .border(1.dp, Color(0xFFEF4444), RoundedCornerShape(20.dp))
                    .padding(20.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Reject Request ${request.id}",
                        style = TextStyle(color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    )
                    OutlinedTextField(
                        value = rejectReason,
                        onValueChange = { rejectReason = it },
                        label = { Text("Rejection Reason") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFFEF4444),
                            unfocusedBorderColor = Color(0xFF334155),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Text(
                            text = "Cancel",
                            color = Color(0xFF94A3B8),
                            modifier = Modifier
                                .clickable { rejectRequestDialog = null }
                                .padding(10.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (rejectReason.isNotBlank()) {
                                    onReject(request.id, rejectReason.trim())
                                    rejectRequestDialog = null
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                        ) {
                            Text("Reject Request", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AdminAuditTab(
    logs: List<AccountAuditLogEntity>
) {
    val theme = MayaTheme.colors

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        flingBehavior = rememberChromeSmoothFlingBehavior(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(top = 8.dp, bottom = 40.dp)
    ) {
        if (logs.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No audit log history recorded yet.",
                        style = TextStyle(color = theme.textSecondary, fontSize = 13.sp)
                    )
                }
            }
        }

        items(logs, key = { it.id }) { log ->
            val actionColor = when (log.action.uppercase()) {
                "SUSPEND", "DEACTIVATE", "DELETE_APPROVED" -> Color(0xFFEF4444)
                "HOLD", "WARN" -> Color(0xFFF59E0B)
                "REACTIVATE", "DELETE_REJECTED" -> Color(0xFF10B981)
                else -> Color(0xFF38BDF8)
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .glassmorphic(
                        shape = RoundedCornerShape(14.dp),
                        backgroundColor = theme.surfaceGlass,
                        borderColor = theme.borderGlass,
                        borderWidth = 1.dp
                    )
                    .padding(12.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(actionColor.copy(alpha = 0.2f))
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = log.action,
                                    style = TextStyle(color = actionColor, fontSize = 10.5.sp, fontWeight = FontWeight.Bold)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "${log.adminName} → ${log.targetUserName}",
                                style = TextStyle(color = theme.textPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            )
                        }

                        Text(
                            text = java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date(log.timestamp)),
                            style = TextStyle(color = theme.textSecondary, fontSize = 10.5.sp)
                        )
                    }

                    Text(
                        text = "Target ID: ${log.targetUserId} • Reason: ${log.reason}",
                        style = TextStyle(color = theme.textSecondary, fontSize = 11.5.sp)
                    )
                }
            }
        }
    }
}

@Composable
private fun ActionButton(
    text: String,
    color: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(color.copy(alpha = 0.12f))
            .border(1.dp, color.copy(alpha = 0.35f), RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .padding(horizontal = 10.dp, vertical = 5.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(13.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = text,
                style = TextStyle(
                    color = color,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
            )
        }
    }
}

@Composable
private fun EmergencySituationTab(
    emergencyState: EmergencySystemState,
    onSetEmergencyMode: (EmergencyMode, String) -> Unit,
    onUpdateDisabledFeatures: (Set<String>) -> Unit,
    onToggleFeatureRestriction: (String) -> Unit,
    onUpdateCustomDurationHours: (Int) -> Unit = {}
) {
    val theme = MayaTheme.colors
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<String?>(null) }
    var showConfirmDialogForMode by remember { mutableStateOf<EmergencyMode?>(null) }
    var customReasonInput by remember { mutableStateOf("") }
    var savedFeedbackMessage by remember { mutableStateOf<String?>(null) }
    var customHoursInput by remember(emergencyState.customDurationHours) {
        mutableStateOf(emergencyState.customDurationHours.toString())
    }

    var localDisabledFeatures by remember(emergencyState.disabledFeatureIds) {
        mutableStateOf(emergencyState.disabledFeatureIds)
    }

    val allFeatures = remember { EmergencyFeatureCatalog.ALL_FEATURES }
    val categories = remember(allFeatures) {
        listOf("All") + allFeatures.map { it.category }.distinct().sorted()
    }

    val filteredFeatures = remember(allFeatures, searchQuery, selectedCategory) {
        allFeatures.filter { item ->
            val matchQuery = searchQuery.isBlank() ||
                item.name.contains(searchQuery, ignoreCase = true) ||
                item.description.contains(searchQuery, ignoreCase = true) ||
                item.category.contains(searchQuery, ignoreCase = true)
            val matchCat = selectedCategory == null || selectedCategory == "All" || item.category == selectedCategory
            matchQuery && matchCat
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("emergency_situation_tab"),
        contentPadding = PaddingValues(vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        flingBehavior = rememberChromeSmoothFlingBehavior()
    ) {
        // 1. Header Banner
        item {
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
                    .padding(18.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(
                                        Brush.linearGradient(
                                            listOf(Color(0xFFDC2626), Color(0xFF991B1B))
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Shield,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Emergency Operations",
                                    style = TextStyle(
                                        color = theme.textPrimary,
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                                Text(
                                    text = "Admin Protocol • Rizwan RR Control",
                                    style = TextStyle(
                                        color = theme.textSecondary,
                                        fontSize = 12.sp
                                    )
                                )
                            }
                        }

                        val (statusText, statusBg, statusColor) = when (emergencyState.activeMode) {
                            EmergencyMode.NORMAL -> Triple("NORMAL", Color(0xFF10B981).copy(alpha = 0.15f), Color(0xFF10B981))
                            EmergencyMode.MAINTENANCE_FREEZE -> Triple("FREEZE ACTIVE", Color(0xFFEF4444).copy(alpha = 0.2f), Color(0xFFEF4444))
                            EmergencyMode.SELECTIVE_FEATURE_CONTROL -> Triple("SELECTIVE ACTIVE", Color(0xFFF59E0B).copy(alpha = 0.2f), Color(0xFFF59E0B))
                            EmergencyMode.HIGH_SECURITY_LOCKDOWN -> Triple("LOCKDOWN ACTIVE", Color(0xFFDC2626).copy(alpha = 0.25f), Color(0xFFDC2626))
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(statusBg)
                                .border(1.dp, statusColor.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                .padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Text(
                                text = statusText,
                                style = TextStyle(
                                    color = statusColor,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }

                    if (emergencyState.activeMode != EmergencyMode.NORMAL) {
                        Spacer(modifier = Modifier.height(14.dp))
                        Button(
                            onClick = {
                                onSetEmergencyMode(EmergencyMode.NORMAL, "Disengaged emergency by Administrator")
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(42.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Return All Systems to Normal",
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

        // 2. OPTION 1: Complete Emergency Freeze Card
        item {
            val isFreezeActive = emergencyState.activeMode == EmergencyMode.MAINTENANCE_FREEZE
            EmergencyModeCard(
                optionBadge = "OPTION 1",
                title = "Home Page Maintenance Notice (Mode 1)",
                subtitle = "Shows on the Home Page that this application is under maintenance",
                description = "When turned ON, the Home Page displays a prominent notice stating that this application is currently under maintenance, and app launches are paused.",
                icon = Icons.Default.Engineering,
                isActive = isFreezeActive,
                activeColor = Color(0xFFEF4444),
                actionLabel = if (isFreezeActive) "TURN OFF MODE 1" else "TURN ON MODE 1 (UNDER MAINTENANCE)",
                onAction = {
                    if (isFreezeActive) {
                        onSetEmergencyMode(EmergencyMode.NORMAL, "System freeze lifted")
                    } else {
                        showConfirmDialogForMode = EmergencyMode.MAINTENANCE_FREEZE
                    }
                }
            )
        }

        // 3. OPTION 2: Specific Maintenance & Custom Hours Duration Card
        item {
            val isSelectiveActive = emergencyState.activeMode == EmergencyMode.SELECTIVE_FEATURE_CONTROL
            EmergencyModeCard(
                optionBadge = "OPTION 2",
                title = "Specific Maintenance & Custom Hours (Mode 2)",
                subtitle = "Shows specific modules in maintenance + custom duration in hours on Home Page",
                description = "Displays on the Home Page which specific features/modules are currently undergoing maintenance and for how many hours (${emergencyState.customDurationHours}h configured below).",
                icon = Icons.Default.Tune,
                isActive = isSelectiveActive,
                activeColor = Color(0xFFF59E0B),
                actionLabel = if (isSelectiveActive) "TURN OFF MODE 2" else "TURN ON MODE 2 (SPECIFIC + HOURS)",
                onAction = {
                    if (isSelectiveActive) {
                        onSetEmergencyMode(EmergencyMode.NORMAL, "Selective mode disengaged")
                    } else {
                        showConfirmDialogForMode = EmergencyMode.SELECTIVE_FEATURE_CONTROL
                    }
                }
            )
        }

        // 3B. Custom Hours Duration Configuration Card for Mode 2
        item {
            val presetHours = listOf(1, 2, 4, 6, 12, 24, 48)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .glassmorphic(
                        shape = RoundedCornerShape(20.dp),
                        backgroundColor = theme.surfaceGlass,
                        borderColor = Color(0xFFF59E0B).copy(alpha = 0.45f),
                        borderWidth = 1.dp
                    )
                    .padding(16.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Custom Maintenance Duration (Hours)",
                                style = TextStyle(
                                    color = theme.textPrimary,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            Text(
                                text = "Set how many hours Mode 2 maintenance will remain active on Home Page",
                                style = TextStyle(
                                    color = theme.textSecondary,
                                    fontSize = 11.5.sp
                                )
                            )
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFFF59E0B).copy(alpha = 0.2f))
                                .border(1.dp, Color(0xFFF59E0B), RoundedCornerShape(10.dp))
                                .padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Text(
                                text = "${emergencyState.customDurationHours} Hours",
                                style = TextStyle(
                                    color = Color(0xFFFBBF24),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Preset Hour Chips
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        presetHours.forEach { hrs ->
                            val isSelected = emergencyState.customDurationHours == hrs
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(
                                        if (isSelected) Color(0xFFF59E0B) else theme.surfaceGlass
                                    )
                                    .border(
                                        1.dp,
                                        if (isSelected) Color(0xFFFBBF24) else theme.borderGlass,
                                        RoundedCornerShape(10.dp)
                                    )
                                    .clickable {
                                        customHoursInput = hrs.toString()
                                        onUpdateCustomDurationHours(hrs)
                                        savedFeedbackMessage = "Maintenance duration set to $hrs hour(s)"
                                    }
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = "${hrs}h",
                                    style = TextStyle(
                                        color = if (isSelected) Color.White else theme.textPrimary,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Custom Hours Input Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = customHoursInput,
                            onValueChange = { value ->
                                customHoursInput = value.filter { it.isDigit() }.take(4)
                            },
                            label = { Text("Custom Hours") },
                            placeholder = { Text("e.g. 8") },
                            singleLine = true,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("custom_duration_hours_input"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFFF59E0B),
                                unfocusedBorderColor = theme.borderGlass,
                                focusedTextColor = theme.textPrimary,
                                unfocusedTextColor = theme.textPrimary
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )

                        Button(
                            onClick = {
                                val parsed = customHoursInput.toIntOrNull()?.coerceIn(1, 720) ?: 4
                                customHoursInput = parsed.toString()
                                onUpdateCustomDurationHours(parsed)
                                savedFeedbackMessage = "Custom duration saved: $parsed hour(s)"
                            },
                            modifier = Modifier.height(50.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF59E0B))
                        ) {
                            Text(
                                text = "Save Hours",
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

        // 4. A-TO-Z Feature Checklist & Configuration
        item {
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
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.ListAlt,
                                contentDescription = null,
                                tint = theme.accentPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "A-to-Z Application Modules",
                                style = TextStyle(
                                    color = theme.textPrimary,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }

                        Text(
                            text = "${allFeatures.size - localDisabledFeatures.size}/${allFeatures.size} Active",
                            style = TextStyle(
                                color = theme.textSecondary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("emergency_feature_search"),
                        placeholder = { Text("Filter modules A-Z...") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search",
                                tint = theme.textSecondary
                            )
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Clear",
                                        tint = theme.textSecondary
                                    )
                                }
                            }
                        },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = theme.accentPrimary,
                            unfocusedBorderColor = theme.borderGlass,
                            focusedTextColor = theme.textPrimary,
                            unfocusedTextColor = theme.textPrimary
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        categories.forEach { cat ->
                            val isSelected = (selectedCategory == cat) || (selectedCategory == null && cat == "All")
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) theme.accentPrimary else theme.surfaceGlass)
                                    .border(1.dp, if (isSelected) theme.accentPrimary else theme.borderGlass, RoundedCornerShape(8.dp))
                                    .clickable {
                                        selectedCategory = if (cat == "All") null else cat
                                    }
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = cat,
                                    style = TextStyle(
                                        color = if (isSelected) Color.White else theme.textSecondary,
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                localDisabledFeatures = emptySet()
                                onUpdateDisabledFeatures(emptySet())
                                savedFeedbackMessage = "All 25 modules unlocked & enabled"
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(36.dp),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = theme.surfaceGlass)
                        ) {
                            Text(
                                text = "Enable All",
                                style = TextStyle(color = theme.textPrimary, fontSize = 11.5.sp)
                            )
                        }

                        Button(
                            onClick = {
                                val nonCore = setOf("rexlify_music", "youtube", "ph_veg", "rodp", "ms_edge")
                                localDisabledFeatures = nonCore
                                onUpdateDisabledFeatures(nonCore)
                                savedFeedbackMessage = "Locked 5 non-core modules"
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(36.dp),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = theme.surfaceGlass)
                        ) {
                            Text(
                                text = "Restrict Non-Core",
                                style = TextStyle(color = theme.textPrimary, fontSize = 11.5.sp)
                            )
                        }
                    }

                    if (savedFeedbackMessage != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = savedFeedbackMessage!!,
                            style = TextStyle(color = Color(0xFF10B981), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        )
                    }
                }
            }
        }

        // 5. Individual Module Switches (A-Z)
        items(filteredFeatures, key = { it.id }) { item ->
            val isRestricted = localDisabledFeatures.contains(item.id.lowercase())
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .glassmorphic(
                        shape = RoundedCornerShape(16.dp),
                        backgroundColor = if (isRestricted) Color(0xFFEF4444).copy(alpha = 0.08f) else theme.surfaceGlass,
                        borderColor = if (isRestricted) Color(0xFFEF4444).copy(alpha = 0.35f) else theme.borderGlass,
                        borderWidth = 1.dp
                    )
                    .padding(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = item.name,
                                style = TextStyle(
                                    color = if (isRestricted) Color(0xFFEF4444) else theme.textPrimary,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(theme.surfaceGlass)
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = item.category,
                                    style = TextStyle(
                                        color = theme.textSecondary,
                                        fontSize = 9.5.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = item.description,
                            style = TextStyle(
                                color = theme.textSecondary,
                                fontSize = 11.5.sp,
                                lineHeight = 15.sp
                            )
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (isRestricted) "STATUS: RESTRICTED / DISABLED" else "STATUS: OPERATIONAL & ACTIVE",
                            style = TextStyle(
                                color = if (isRestricted) Color(0xFFEF4444) else Color(0xFF10B981),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }

                    Switch(
                        checked = !isRestricted,
                        onCheckedChange = { isEnabled ->
                            val updated = localDisabledFeatures.toMutableSet()
                            if (isEnabled) {
                                updated.remove(item.id.lowercase())
                            } else {
                                updated.add(item.id.lowercase())
                            }
                            localDisabledFeatures = updated
                            onUpdateDisabledFeatures(updated)
                            savedFeedbackMessage = if (isEnabled) "${item.name} enabled" else "${item.name} restricted"
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = Color(0xFF10B981),
                            uncheckedThumbColor = Color.White,
                            uncheckedTrackColor = Color(0xFFEF4444)
                        ),
                        modifier = Modifier.testTag("toggle_feature_${item.id}")
                    )
                }
            }
        }

        // 6. OPTION 3: High-Security Lockdown Modal Card
        item {
            val isLockdownActive = emergencyState.activeMode == EmergencyMode.HIGH_SECURITY_LOCKDOWN
            EmergencyModeCard(
                optionBadge = "OPTION 3",
                title = "Full Home Page Lockout for Users & Family (Mode 3)",
                subtitle = "Home Page shows ONLY: 'You cannot use this application right now. Please try again after some time.'",
                description = "When turned ON, Standard Users and RRR Family Members see ONLY the message 'You cannot use this application right now. Please try again after some time.' on the Home Page and no Home Page buttons work for them. CEO Panel remains completely exempt and retains full access.",
                icon = Icons.Default.LockClock,
                isActive = isLockdownActive,
                activeColor = Color(0xFFDC2626),
                actionLabel = if (isLockdownActive) "TURN OFF MODE 3" else "TURN ON MODE 3 (LOCK USERS & FAMILY)",
                onAction = {
                    if (isLockdownActive) {
                        onSetEmergencyMode(EmergencyMode.NORMAL, "High-security lockdown lifted by Admin")
                    } else {
                        showConfirmDialogForMode = EmergencyMode.HIGH_SECURITY_LOCKDOWN
                    }
                }
            )
        }
    }

    // Confirmation Dialog
    if (showConfirmDialogForMode != null) {
        val targetMode = showConfirmDialogForMode!!
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showConfirmDialogForMode = null },
            icon = {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = Color(0xFFEF4444),
                    modifier = Modifier.size(32.dp)
                )
            },
            title = {
                Text(
                    text = "Confirm Emergency Activation",
                    style = TextStyle(fontWeight = FontWeight.Bold, fontSize = 17.sp)
                )
            },
            text = {
                Column {
                    Text(
                        text = "Are you sure you want to engage ${targetMode.name}? This will affect application behavior in real time across all user sessions.",
                        style = TextStyle(fontSize = 13.sp, lineHeight = 18.sp)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = customReasonInput,
                        onValueChange = { customReasonInput = it },
                        label = { Text("Reason for Log & Notice") },
                        placeholder = { Text("e.g. Core infrastructure upgrade") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val reason = if (customReasonInput.isNotBlank()) customReasonInput.trim()
                        else "Admin initiated ${targetMode.name}"
                        onSetEmergencyMode(targetMode, reason)
                        showConfirmDialogForMode = null
                        customReasonInput = ""
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626))
                ) {
                    Text("Activate Mode", color = Color.White)
                }
            },
            dismissButton = {
                androidx.compose.material3.TextButton(
                    onClick = { showConfirmDialogForMode = null }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun EmergencyModeCard(
    optionBadge: String,
    title: String,
    subtitle: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isActive: Boolean,
    activeColor: Color,
    actionLabel: String,
    onAction: () -> Unit
) {
    val theme = MayaTheme.colors
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .glassmorphic(
                shape = RoundedCornerShape(20.dp),
                backgroundColor = if (isActive) activeColor.copy(alpha = 0.12f) else theme.surfaceGlass,
                borderColor = if (isActive) activeColor.copy(alpha = 0.5f) else theme.borderGlass,
                borderWidth = 1.dp
            )
            .padding(18.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(
                                if (isActive) activeColor.copy(alpha = 0.25f) else theme.surfaceGlass
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = if (isActive) activeColor else theme.accentPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isActive) activeColor.copy(alpha = 0.25f) else theme.borderGlass)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = optionBadge,
                                style = TextStyle(
                                    color = if (isActive) activeColor else theme.textSecondary,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = title,
                            style = TextStyle(
                                color = theme.textPrimary,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }

                if (isActive) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(activeColor.copy(alpha = 0.2f))
                            .border(1.dp, activeColor, RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "ACTIVE",
                            style = TextStyle(
                                color = activeColor,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = subtitle,
                style = TextStyle(
                    color = theme.textPrimary.copy(alpha = 0.85f),
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.SemiBold
                )
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = description,
                style = TextStyle(
                    color = theme.textSecondary,
                    fontSize = 12.sp,
                    lineHeight = 17.sp
                )
            )

            Spacer(modifier = Modifier.height(14.dp))

            Button(
                onClick = onAction,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .testTag("emergency_action_${optionBadge.lowercase().replace(" ", "_")}"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isActive) theme.surfaceGlass else activeColor
                ),
                border = if (isActive) androidx.compose.foundation.BorderStroke(1.dp, activeColor) else null
            ) {
                Text(
                    text = actionLabel,
                    style = TextStyle(
                        color = if (isActive) activeColor else Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }
    }
}

@Composable
private fun FamilyMembersAdminTab(
    familyMembers: List<FamilyMemberAccount>,
    onCreateOrUpdateFamilyMember: (id: String, password: String, memberName: String, details: String) -> Unit,
    onToggleFamilyMemberActive: (String) -> Unit,
    onDeleteFamilyMember: (String) -> Unit
) {
    val theme = MayaTheme.colors
    var memberNameInput by remember { mutableStateOf("") }
    var familyIdInput by remember { mutableStateOf("") }
    var familyPasswordInput by remember { mutableStateOf("") }
    var memberDetailsInput by remember { mutableStateOf("") }
    var feedbackBanner by remember { mutableStateOf<String?>(null) }
    var revealedPasswordIds by remember { mutableStateOf(setOf<String>()) }

    val dateFormatter = remember {
        java.text.SimpleDateFormat("dd MMM yyyy, hh:mm a", java.util.Locale.getDefault())
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("admin_family_members_tab"),
        contentPadding = PaddingValues(vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        flingBehavior = rememberChromeSmoothFlingBehavior()
    ) {
        // 1. Header & Realtime Database Sync Badge
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .glassmorphic(
                        shape = RoundedCornerShape(20.dp),
                        backgroundColor = theme.surfaceGlass,
                        borderColor = Color(0xFF0EA5E9).copy(alpha = 0.45f),
                        borderWidth = 1.dp
                    )
                    .padding(18.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.linearGradient(
                                        listOf(Color(0xFF0EA5E9), Color(0xFF2563EB))
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Group,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "RRR Family Members & Credentials",
                                style = TextStyle(
                                    color = theme.textPrimary,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            Text(
                                text = "Firebase Realtime Database • Create IDs & Track Logins",
                                style = TextStyle(
                                    color = theme.textSecondary,
                                    fontSize = 11.5.sp
                                )
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFF0EA5E9).copy(alpha = 0.18f))
                            .border(1.dp, Color(0xFF38BDF8).copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Text(
                            text = "${familyMembers.size} Accounts",
                            style = TextStyle(
                                color = Color(0xFF38BDF8),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }
            }
        }

        // 2. Create New Family Member Card (Realtime Database)
        item {
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
                    .padding(18.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Create New Family Member Login",
                        style = TextStyle(
                            color = theme.textPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Assign a custom Login ID, Password, Member Name, and Details. New credentials sync immediately to Firebase Realtime Database.",
                        style = TextStyle(
                            color = theme.textSecondary,
                            fontSize = 12.sp,
                            lineHeight = 17.sp
                        )
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedTextField(
                        value = memberNameInput,
                        onValueChange = {
                            memberNameInput = it
                            feedbackBanner = null
                        },
                        label = { Text("Family Member Full Name") },
                        placeholder = { Text("e.g. Arman RRR") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("family_member_name_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF0EA5E9),
                            unfocusedBorderColor = theme.borderGlass,
                            focusedTextColor = theme.textPrimary,
                            unfocusedTextColor = theme.textPrimary
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = familyIdInput,
                            onValueChange = {
                                familyIdInput = it
                                feedbackBanner = null
                            },
                            label = { Text("Login ID") },
                            placeholder = { Text("e.g. arman_rrr") },
                            singleLine = true,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("family_member_id_input"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF0EA5E9),
                                unfocusedBorderColor = theme.borderGlass,
                                focusedTextColor = theme.textPrimary,
                                unfocusedTextColor = theme.textPrimary
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )

                        OutlinedTextField(
                            value = familyPasswordInput,
                            onValueChange = {
                                familyPasswordInput = it
                                feedbackBanner = null
                            },
                            label = { Text("Password") },
                            placeholder = { Text("e.g. Rexlify@2026") },
                            singleLine = true,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("family_member_password_input"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF0EA5E9),
                                unfocusedBorderColor = theme.borderGlass,
                                focusedTextColor = theme.textPrimary,
                                unfocusedTextColor = theme.textPrimary
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = memberDetailsInput,
                        onValueChange = {
                            memberDetailsInput = it
                            feedbackBanner = null
                        },
                        label = { Text("Member Role / Details") },
                        placeholder = { Text("e.g. Core RRR Family • Executive Access") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("family_member_details_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF0EA5E9),
                            unfocusedBorderColor = theme.borderGlass,
                            focusedTextColor = theme.textPrimary,
                            unfocusedTextColor = theme.textPrimary
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )

                    if (feedbackBanner != null) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = feedbackBanner!!,
                            style = TextStyle(
                                color = Color(0xFF10B981),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = {
                            val cleanId = familyIdInput.trim()
                            val cleanPass = familyPasswordInput.trim()
                            val cleanName = memberNameInput.trim().ifBlank { cleanId }
                            val cleanDetails = memberDetailsInput.trim().ifBlank { "Verified RRR Family Member" }
                            if (cleanId.isNotEmpty() && cleanPass.isNotEmpty()) {
                                onCreateOrUpdateFamilyMember(cleanId, cleanPass, cleanName, cleanDetails)
                                feedbackBanner = "Family Member '$cleanName' (ID: $cleanId) saved to Realtime Database!"
                                memberNameInput = ""
                                familyIdInput = ""
                                familyPasswordInput = ""
                                memberDetailsInput = ""
                            } else {
                                feedbackBanner = "Please enter both Login ID and Password."
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp)
                            .testTag("create_family_member_btn"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0EA5E9))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Create & Sync Family Member Account",
                            style = TextStyle(
                                color = Color.White,
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }
            }
        }

        // 3. Family Members List Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Registered & Logged-In Family Members (${familyMembers.size})",
                    style = TextStyle(
                        color = theme.textPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
                Text(
                    text = "Realtime Sync",
                    style = TextStyle(
                        color = Color(0xFF10B981),
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                )
            }
        }

        // 4. Family Member Cards
        items(familyMembers, key = { it.id }) { member ->
            val isPasswordVisible = revealedPasswordIds.contains(member.id)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .glassmorphic(
                        shape = RoundedCornerShape(18.dp),
                        backgroundColor = theme.surfaceGlass,
                        borderColor = if (member.isActive) Color(0xFF0EA5E9).copy(alpha = 0.35f) else Color(0xFFEF4444).copy(alpha = 0.4f),
                        borderWidth = 1.dp
                    )
                    .padding(16.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (member.isActive) Color(0xFF0EA5E9).copy(alpha = 0.2f)
                                        else Color(0xFFEF4444).copy(alpha = 0.2f)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = member.memberName.take(1).uppercase(),
                                    style = TextStyle(
                                        color = if (member.isActive) Color(0xFF38BDF8) else Color(0xFFEF4444),
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = member.memberName,
                                        style = TextStyle(
                                            color = theme.textPrimary,
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(
                                                if (member.isActive) Color(0xFF10B981).copy(alpha = 0.18f)
                                                else Color(0xFFEF4444).copy(alpha = 0.18f)
                                            )
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = if (member.isActive) "ACTIVE" else "DISABLED",
                                            style = TextStyle(
                                                color = if (member.isActive) Color(0xFF10B981) else Color(0xFFEF4444),
                                                fontSize = 9.5.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        )
                                    }
                                }
                                Text(
                                    text = member.details,
                                    style = TextStyle(
                                        color = theme.textSecondary,
                                        fontSize = 12.sp
                                    )
                                )
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Switch(
                                checked = member.isActive,
                                onCheckedChange = { onToggleFamilyMemberActive(member.id) },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = Color(0xFF10B981),
                                    uncheckedThumbColor = Color.White,
                                    uncheckedTrackColor = Color(0xFFEF4444)
                                )
                            )
                            if (!member.id.equals("admin", ignoreCase = true)) {
                                IconButton(onClick = { onDeleteFamilyMember(member.id) }) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Delete Family Member",
                                        tint = Color(0xFFEF4444)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Credentials & Login Telemetry Box
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.Black.copy(alpha = 0.22f))
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Login ID: ${member.id}",
                                style = TextStyle(
                                    color = Color(0xFF38BDF8),
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .clickable {
                                        revealedPasswordIds = if (isPasswordVisible) {
                                            revealedPasswordIds - member.id
                                        } else {
                                            revealedPasswordIds + member.id
                                        }
                                    }
                                    .padding(horizontal = 4.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "Password: ${if (isPasswordVisible) member.password else "••••••••"}",
                                    style = TextStyle(
                                        color = theme.textPrimary,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Icon(
                                    imageVector = if (isPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = "Show/Hide Password",
                                    tint = theme.textSecondary,
                                    modifier = Modifier.size(15.dp)
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Total Logins: ${member.loginCount}",
                                style = TextStyle(
                                    color = theme.textSecondary,
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            )
                            val lastLoginText = if (member.lastLoginAt > 0L) {
                                "Last Login: ${dateFormatter.format(java.util.Date(member.lastLoginAt))}"
                            } else {
                                "Last Login: Never logged in yet"
                            }
                            Text(
                                text = lastLoginText,
                                style = TextStyle(
                                    color = if (member.lastLoginAt > 0L) Color(0xFF10B981) else theme.textSecondary,
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}



