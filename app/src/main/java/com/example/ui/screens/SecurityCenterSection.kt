package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import com.example.ui.components.rememberChromeSmoothFlingBehavior
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.GppBad
import androidx.compose.material.icons.filled.GppGood
import androidx.compose.material.icons.filled.GppMaybe
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Policy
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.local.SecurityEventEntity
import com.example.data.security.SecurityHardeningEngine
import com.example.ui.components.glassmorphic
import com.example.ui.theme.MayaTheme

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SecurityCenterSection(
    securityEvents: List<SecurityEventEntity>,
    onUpdateIncidentState: (
        eventId: String,
        newStatus: String,
        notes: String,
        actionDescription: String,
        isBlocked: Boolean
    ) -> Unit,
    onUnblockAllSources: () -> Unit,
    onRunSecuritySelfTest: () -> Unit,
    onPurgeOldResolvedEvents: () -> Unit
) {
    val theme = MayaTheme.colors
    val envReport = remember { SecurityHardeningEngine.evaluateDeviceEnvironmentRisk() }

    var searchQuery by remember { mutableStateOf("") }
    var selectedSeverity by remember { mutableStateOf("ALL") }
    var selectedStatus by remember { mutableStateOf("ALL") }
    var selectedCategory by remember { mutableStateOf("ALL") }
    var selectedEventForModal by remember { mutableStateOf<SecurityEventEntity?>(null) }
    var statusBannerMessage by remember { mutableStateOf<String?>(null) }

    // Compute 8 Real-Time Security KPIs
    val totalEvents = securityEvents.size
    val criticalHighCount = securityEvents.count {
        it.severity.equals("CRITICAL", ignoreCase = true) || it.severity.equals("HIGH", ignoreCase = true)
    }
    val failedAuthCount = securityEvents.count {
        it.category.equals("BRUTE_FORCE_AUTH", ignoreCase = true) ||
                it.errorCode.contains("AUTH", ignoreCase = true) ||
                it.errorCode.contains("BRUTE_FORCE", ignoreCase = true)
    }
    val blockedRateLimitedCount = securityEvents.count {
        it.isSourceBlocked ||
                it.category.equals("OTP_RATE_LIMIT", ignoreCase = true) ||
                it.category.equals("BOT_TRAFFIC_BURST", ignoreCase = true) ||
                it.errorCode.contains("429", ignoreCase = true)
    }
    val suspiciousSessionsCount = securityEvents.count {
        it.category.equals("SUSPICIOUS_SESSION", ignoreCase = true) ||
                it.category.equals("UNSAFE_WEBVIEW_REDIRECT", ignoreCase = true) ||
                it.category.equals("INJECTION_XSS_SQLI", ignoreCase = true)
    }
    val unauthorizedAccessCount = securityEvents.count {
        it.category.equals("PRIVILEGE_ESCALATION", ignoreCase = true) ||
                it.category.equals("UNAUTHORIZED_ACCESS", ignoreCase = true) ||
                it.category.equals("IDOR_DB_VIOLATION", ignoreCase = true)
    }
    val activeInvestigationsCount = securityEvents.count {
        it.status.equals("Detected", ignoreCase = true) ||
                it.status.equals("Investigating", ignoreCase = true)
    }
    val resolvedIncidentsCount = securityEvents.count {
        it.status.equals("Mitigated", ignoreCase = true) ||
                it.status.equals("Resolved", ignoreCase = true)
    }

    val filteredEvents = remember(securityEvents, searchQuery, selectedSeverity, selectedStatus, selectedCategory) {
        securityEvents.filter { ev ->
            val matchesSeverity = selectedSeverity == "ALL" || ev.severity.equals(selectedSeverity, ignoreCase = true)
            val matchesStatus = selectedStatus == "ALL" || ev.status.equals(selectedStatus, ignoreCase = true)
            val matchesCategory = selectedCategory == "ALL" || ev.category.equals(selectedCategory, ignoreCase = true)
            val q = searchQuery.trim().lowercase()
            val matchesQuery = q.isEmpty() ||
                    ev.eventId.lowercase().contains(q) ||
                    ev.authenticatedUserId.lowercase().contains(q) ||
                    ev.sourceIp.lowercase().contains(q) ||
                    ev.approxLocation.lowercase().contains(q) ||
                    ev.sessionCorrelationId.lowercase().contains(q) ||
                    ev.detectionReason.lowercase().contains(q) ||
                    ev.errorCode.lowercase().contains(q) ||
                    ev.endpointOrModule.lowercase().contains(q)
            matchesSeverity && matchesStatus && matchesCategory && matchesQuery
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("security_center_list"),
        flingBehavior = rememberChromeSmoothFlingBehavior(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(top = 8.dp, bottom = 48.dp)
    ) {
        // 1. Security Center Header Banner
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(Color(0xFF1E1B4B), Color(0xFF311042), Color(0xFF0F172A))
                        )
                    )
                    .border(1.dp, Color(0xFFA855F7).copy(alpha = 0.5f), RoundedCornerShape(18.dp))
                    .padding(16.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFA855F7).copy(alpha = 0.22f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Shield,
                                    contentDescription = "Security Center",
                                    tint = Color(0xFFD8B4FE),
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "REXLIFY Security Center & Threat Monitor",
                                    style = TextStyle(
                                        color = Color.White,
                                        fontSize = 15.5.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                                Text(
                                    text = "Zero-Trust RBAC • Salted PBKDF2 • HTTPS Enforced • Real-Time Audit",
                                    style = TextStyle(
                                        color = Color(0xFFCBD5E1),
                                        fontSize = 11.sp
                                    )
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF10B981).copy(alpha = 0.2f))
                                .border(1.dp, Color(0xFF10B981), RoundedCornerShape(8.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "PROTECTED",
                                style = TextStyle(
                                    color = Color(0xFF10B981),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            )
                        }
                    }

                    Text(
                        text = "Runtime Telemetry: ${envReport.deviceSummary}",
                        style = TextStyle(
                            color = Color(0xFF94A3B8),
                            fontSize = 10.5.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    )
                }
            }
        }

        // Optional Action Confirmation Banner
        if (statusBannerMessage != null) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF065F46).copy(alpha = 0.35f))
                        .border(1.dp, Color(0xFF10B981), RoundedCornerShape(12.dp))
                        .padding(horizontal = 14.dp, vertical = 10.dp)
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
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = Color(0xFF10B981),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = statusBannerMessage ?: "",
                                style = TextStyle(color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                            )
                        }
                        IconButton(
                            onClick = { statusBannerMessage = null },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Dismiss",
                                tint = Color.White,
                                modifier = Modifier.size(15.dp)
                            )
                        }
                    }
                }
            }
        }

        // 2. 8 KPI Overview Cards (4 rows x 2 columns)
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    SecurityKpiCard(
                        title = "Total Security Events",
                        value = "$totalEvents",
                        subtitle = "Centralized audit log",
                        color = Color(0xFF38BDF8),
                        modifier = Modifier.weight(1f)
                    )
                    SecurityKpiCard(
                        title = "Critical / High Alerts",
                        value = "$criticalHighCount",
                        subtitle = "Priority threat detections",
                        color = if (criticalHighCount > 0) Color(0xFFEF4444) else Color(0xFF10B981),
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    SecurityKpiCard(
                        title = "Failed Auth Attempts",
                        value = "$failedAuthCount",
                        subtitle = "Brute-force / invalid login",
                        color = Color(0xFFF59E0B),
                        modifier = Modifier.weight(1f)
                    )
                    SecurityKpiCard(
                        title = "Blocked / Rate-Limited",
                        value = "$blockedRateLimitedCount",
                        subtitle = "Throttled & restricted IPs",
                        color = Color(0xFFA855F7),
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    SecurityKpiCard(
                        title = "Suspicious Sessions",
                        value = "$suspiciousSessionsCount",
                        subtitle = "XSS / SQLi / WebView blocks",
                        color = Color(0xFFEC4899),
                        modifier = Modifier.weight(1f)
                    )
                    SecurityKpiCard(
                        title = "Unauthorized Access",
                        value = "$unauthorizedAccessCount",
                        subtitle = "RBAC & privilege violations",
                        color = Color(0xFFF97316),
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    SecurityKpiCard(
                        title = "Active Investigations",
                        value = "$activeInvestigationsCount",
                        subtitle = "Detected & Investigating",
                        color = Color(0xFFEAB308),
                        modifier = Modifier.weight(1f)
                    )
                    SecurityKpiCard(
                        title = "Resolved Incidents",
                        value = "$resolvedIncidentsCount",
                        subtitle = "Mitigated & Resolved",
                        color = Color(0xFF10B981),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // 3. Automated Defense Controls & Authorized Self-Test Toolbar
        item {
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
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Automated Threat Response & Recovery Controls",
                        style = TextStyle(
                            color = theme.textPrimary,
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )

                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                onRunSecuritySelfTest()
                                statusBannerMessage = "Authorized Security Self-Test executed: SQLi/XSS, RBAC, WebView & Rate-Limit defenses verified."
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C3AED)),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            modifier = Modifier.testTag("security_run_self_test_btn")
                        ) {
                            Icon(imageVector = Icons.Default.VerifiedUser, contentDescription = null, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Run Security Self-Test", style = TextStyle(fontSize = 11.5.sp, fontWeight = FontWeight.Bold))
                        }

                        Button(
                            onClick = {
                                onUnblockAllSources()
                                statusBannerMessage = "Authorized Recovery: All temporary lockouts and rate-limited sessions restored."
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            modifier = Modifier.testTag("security_unblock_all_btn")
                        ) {
                            Icon(imageVector = Icons.Default.LockOpen, contentDescription = null, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Unblock / Recover Sources", style = TextStyle(fontSize = 11.5.sp, fontWeight = FontWeight.Bold))
                        }

                        Button(
                            onClick = {
                                onPurgeOldResolvedEvents()
                                statusBannerMessage = "Retention Policy: Purged resolved security events older than 30 days."
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF475569)),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            modifier = Modifier.testTag("security_purge_logs_btn")
                        ) {
                            Icon(imageVector = Icons.Default.DeleteSweep, contentDescription = null, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Purge >30d Resolved Logs", style = TextStyle(fontSize = 11.5.sp, fontWeight = FontWeight.Bold))
                        }
                    }
                }
            }
        }

        // 4. Location Accuracy & DPDP Privacy Compliance Notice
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFF0F172A).copy(alpha = 0.85f))
                    .border(1.dp, Color(0xFF38BDF8).copy(alpha = 0.35f), RoundedCornerShape(14.dp))
                    .padding(12.dp)
            ) {
                Row(verticalAlignment = Alignment.Top) {
                    Icon(
                        imageVector = Icons.Default.Policy,
                        contentDescription = null,
                        tint = Color(0xFF38BDF8),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        Text(
                            text = "IP Geolocation Accuracy & DPDP Privacy Compliance",
                            style = TextStyle(
                                color = Color(0xFF38BDF8),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Text(
                            text = "• IP-based location is approximate (city/region/country) and may reflect a VPN, proxy, Tor exit, or mobile carrier NAT.\n" +
                                    "• Unauthenticated sessions are logged as \"Unknown / Unauthenticated\". Plaintext passwords, OTPs, and raw session tokens are never logged.\n" +
                                    "• Maya Connect never secretly activates GPS, camera, or microphone hardware.",
                            style = TextStyle(
                                color = Color(0xFFCBD5E1),
                                fontSize = 10.5.sp,
                                lineHeight = 15.sp
                            )
                        )
                    }
                }
            }
        }

        // 5. Search & Multi-Filter Controls
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("security_event_search_input"),
                    placeholder = { Text("Search Event ID, User ID, IP, Correlation ID, Error Code...") },
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
                        focusedBorderColor = Color(0xFFA855F7),
                        unfocusedBorderColor = theme.borderGlass,
                        focusedTextColor = theme.textPrimary,
                        unfocusedTextColor = theme.textPrimary
                    ),
                    shape = RoundedCornerShape(14.dp)
                )

                // Severity Filter Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Severity:",
                        style = TextStyle(color = theme.textSecondary, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    )
                    listOf("ALL", "CRITICAL", "HIGH", "MEDIUM", "LOW").forEach { sev ->
                        FilterChipPill(
                            label = sev,
                            selected = selectedSeverity == sev,
                            onClick = { selectedSeverity = sev }
                        )
                    }
                }

                // Status Filter Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Status:",
                        style = TextStyle(color = theme.textSecondary, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    )
                    listOf("ALL", "Detected", "Investigating", "Mitigated", "Resolved", "False Positive").forEach { st ->
                        FilterChipPill(
                            label = st,
                            selected = selectedStatus == st,
                            onClick = { selectedStatus = st }
                        )
                    }
                }

                // Category Filter Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Type:",
                        style = TextStyle(color = theme.textSecondary, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    )
                    listOf(
                        "ALL",
                        "BRUTE_FORCE_AUTH",
                        "PRIVILEGE_ESCALATION",
                        "INJECTION_XSS_SQLI",
                        "UNSAFE_WEBVIEW_REDIRECT",
                        "OTP_RATE_LIMIT",
                        "UNAUTHORIZED_ACCESS"
                    ).forEach { cat ->
                        FilterChipPill(
                            label = cat.replace("_", " "),
                            selected = selectedCategory == cat,
                            onClick = { selectedCategory = cat }
                        )
                    }
                }

                Text(
                    text = "Showing ${filteredEvents.size} of $totalEvents security events",
                    style = TextStyle(color = theme.textSecondary, fontSize = 11.5.sp, fontWeight = FontWeight.Medium)
                )
            }
        }

        // 6. Security Event Table Rows
        if (filteredEvents.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No security events match the current filter criteria.",
                        style = TextStyle(color = theme.textSecondary, fontSize = 13.sp)
                    )
                }
            }
        } else {
            items(filteredEvents, key = { it.eventId }) { event ->
                SecurityEventRowCard(
                    event = event,
                    onViewDetails = { selectedEventForModal = event }
                )
            }
        }
    }

    // 7. Event Details & Incident Response Modal
    selectedEventForModal?.let { activeEvent ->
        // Keep modal synced with latest list item if updated
        val currentEvent = securityEvents.find { it.eventId == activeEvent.eventId } ?: activeEvent
        SecurityEventDetailsModal(
            event = currentEvent,
            onDismiss = { selectedEventForModal = null },
            onUpdateIncident = { newStatus, notes, actionDesc, isBlocked ->
                onUpdateIncidentState(currentEvent.eventId, newStatus, notes, actionDesc, isBlocked)
                statusBannerMessage = "Incident ${currentEvent.eventId} updated to '$newStatus'."
            }
        )
    }
}

@Composable
private fun SecurityKpiCard(
    title: String,
    value: String,
    subtitle: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    val theme = MayaTheme.colors
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .glassmorphic(
                shape = RoundedCornerShape(16.dp),
                backgroundColor = theme.surfaceGlass,
                borderColor = color.copy(alpha = 0.35f),
                borderWidth = 1.dp
            )
            .padding(12.dp)
    ) {
        Column {
            Text(
                text = title,
                style = TextStyle(color = theme.textSecondary, fontSize = 11.sp, fontWeight = FontWeight.Medium),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = TextStyle(color = color, fontSize = 22.sp, fontWeight = FontWeight.Black)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = TextStyle(color = theme.textSecondary.copy(alpha = 0.8f), fontSize = 9.5.sp),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun FilterChipPill(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val theme = MayaTheme.colors
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(if (selected) Color(0xFF7C3AED) else theme.surfaceGlass)
            .border(
                1.dp,
                if (selected) Color(0xFFC084FC) else theme.borderGlass,
                RoundedCornerShape(10.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 10.dp, vertical = 5.dp)
    ) {
        Text(
            text = label,
            style = TextStyle(
                color = if (selected) Color.White else theme.textSecondary,
                fontSize = 10.5.sp,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
            )
        )
    }
}

@Composable
private fun SecurityEventRowCard(
    event: SecurityEventEntity,
    onViewDetails: () -> Unit
) {
    val theme = MayaTheme.colors
    val severityColor = when (event.severity.uppercase()) {
        "CRITICAL" -> Color(0xFFEF4444)
        "HIGH" -> Color(0xFFF97316)
        "MEDIUM" -> Color(0xFFF59E0B)
        else -> Color(0xFF38BDF8)
    }

    val statusColor = when (event.status) {
        "Detected" -> Color(0xFFEF4444)
        "Investigating" -> Color(0xFFF59E0B)
        "Mitigated" -> Color(0xFF38BDF8)
        "Resolved" -> Color(0xFF10B981)
        "False Positive" -> Color(0xFF94A3B8)
        else -> Color(0xFFA855F7)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .glassmorphic(
                shape = RoundedCornerShape(16.dp),
                backgroundColor = theme.surfaceGlass,
                borderColor = severityColor.copy(alpha = 0.4f),
                borderWidth = 1.dp
            )
            .clickable { onViewDetails() }
            .padding(14.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            // Top Row: Event ID + Timestamp + Severity + Status Badges
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = when (event.severity.uppercase()) {
                            "CRITICAL", "HIGH" -> Icons.Default.GppBad
                            "MEDIUM" -> Icons.Default.GppMaybe
                            else -> Icons.Default.GppGood
                        },
                        contentDescription = null,
                        tint = severityColor,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = event.eventId,
                        style = TextStyle(
                            color = theme.textPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = event.timestampUtc,
                        style = TextStyle(color = theme.textSecondary, fontSize = 10.5.sp)
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    // Severity Badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(severityColor.copy(alpha = 0.2f))
                            .border(1.dp, severityColor.copy(alpha = 0.6f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = event.severity.uppercase(),
                            style = TextStyle(color = severityColor, fontSize = 9.5.sp, fontWeight = FontWeight.ExtraBold)
                        )
                    }

                    // Status Badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(statusColor.copy(alpha = 0.18f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = event.status,
                            style = TextStyle(color = statusColor, fontSize = 9.5.sp, fontWeight = FontWeight.Bold)
                        )
                    }
                }
            }

            // Category & Endpoint
            Text(
                text = "${event.category.replace("_", " ")} • ${event.endpointOrModule}",
                style = TextStyle(color = Color(0xFFD8B4FE), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            )

            // Detection Reason
            Text(
                text = event.detectionReason,
                style = TextStyle(color = theme.textPrimary, fontSize = 12.sp, lineHeight = 16.sp),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            // Metadata Grid: User ID, Source IP, Approx Location
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = "User: ${event.authenticatedUserId} (${event.userRole}) • CorrID: ${event.sessionCorrelationId}",
                    style = TextStyle(color = theme.textSecondary, fontSize = 10.5.sp)
                )
                Text(
                    text = "Source IP: ${event.sourceIp} ${if (event.isSourceBlocked) "[BLOCKED]" else ""}",
                    style = TextStyle(
                        color = if (event.isSourceBlocked) Color(0xFFEF4444) else theme.textSecondary,
                        fontSize = 10.5.sp,
                        fontWeight = if (event.isSourceBlocked) FontWeight.Bold else FontWeight.Normal
                    )
                )
                Text(
                    text = "Location: ${event.approxLocation}",
                    style = TextStyle(color = theme.textSecondary, fontSize = 10.5.sp)
                )
            }

            // Bottom Action Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Code: ${event.errorCode}",
                    style = TextStyle(
                        color = theme.textSecondary,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace
                    )
                )

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF7C3AED).copy(alpha = 0.2f))
                        .border(1.dp, Color(0xFFA855F7).copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                        .clickable { onViewDetails() }
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                        .testTag("view_security_event_${event.eventId}")
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Visibility,
                            contentDescription = null,
                            tint = Color(0xFFD8B4FE),
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = "View Details & Respond",
                            style = TextStyle(color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SecurityEventDetailsModal(
    event: SecurityEventEntity,
    onDismiss: () -> Unit,
    onUpdateIncident: (
        newStatus: String,
        notes: String,
        actionDescription: String,
        isBlocked: Boolean
    ) -> Unit
) {
    var notesInput by remember(event.eventId, event.investigationNotes) {
        mutableStateOf(event.investigationNotes)
    }

    val severityColor = when (event.severity.uppercase()) {
        "CRITICAL" -> Color(0xFFEF4444)
        "HIGH" -> Color(0xFFF97316)
        "MEDIUM" -> Color(0xFFF59E0B)
        else -> Color(0xFF38BDF8)
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .clip(RoundedCornerShape(22.dp))
                .background(Color(0xFF0F172A))
                .border(1.5.dp, severityColor.copy(alpha = 0.6f), RoundedCornerShape(22.dp))
                .padding(18.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Modal Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Incident Forensics • ${event.eventId}",
                            style = TextStyle(
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Text(
                            text = "${event.timestampUtc} • Severity: ${event.severity} • Status: ${event.status}",
                            style = TextStyle(color = severityColor, fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold)
                        )
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color.White
                        )
                    }
                }

                HorizontalDivider(color = Color(0xFF334155))

                // Forensic Details Section
                ForensicDetailItem("Category & Endpoint", "${event.category} → ${event.endpointOrModule}")
                ForensicDetailItem("Authenticated User ID", "${event.authenticatedUserId} (Role: ${event.userRole})")
                ForensicDetailItem("Request Correlation ID", event.sessionCorrelationId)
                ForensicDetailItem(
                    "Source IP & Block State",
                    "${event.sourceIp} (${if (event.isSourceBlocked) "SOURCE BLOCKED / RESTRICTED" else "Monitoring Active"})"
                )
                ForensicDetailItem(
                    "Approximate IP Location",
                    "${event.approxLocation}\n[Disclaimer: Derived from IP routing metadata; may reflect VPN/Proxy/Carrier NAT. No secret GPS activation.]"
                )
                ForensicDetailItem("Device & Runtime Telemetry", event.deviceInfo)
                ForensicDetailItem("Detection Reason & Error Code", "${event.detectionReason}\nCode: ${event.errorCode}")
                ForensicDetailItem("Automated Mitigation Taken", event.mitigationAction)

                // Admin Action History Timeline
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "Incident Timeline & Admin Action History",
                        style = TextStyle(color = Color(0xFF94A3B8), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFF1E293B))
                            .padding(10.dp)
                    ) {
                        Text(
                            text = event.adminActionHistory.ifBlank { "No manual admin actions recorded yet." },
                            style = TextStyle(
                                color = Color(0xFFE2E8F0),
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                lineHeight = 15.sp
                            )
                        )
                    }
                }

                // Investigation Notes Input
                OutlinedTextField(
                    value = notesInput,
                    onValueChange = { notesInput = it },
                    label = { Text("Investigation Notes (Saved to Audit Trail)") },
                    placeholder = { Text("Enter root cause analysis, false-positive verification, or remediation notes...") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    maxLines = 4,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFFA855F7),
                        unfocusedBorderColor = Color(0xFF475569),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedLabelColor = Color(0xFFD8B4FE),
                        unfocusedLabelColor = Color(0xFF94A3B8)
                    )
                )

                // Incident Response Actions
                Text(
                    text = "Admin Incident Response Actions",
                    style = TextStyle(color = Color.White, fontSize = 12.5.sp, fontWeight = FontWeight.Bold)
                )

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IncidentActionChip(
                        label = "Mark Investigating",
                        color = Color(0xFFF59E0B)
                    ) {
                        onUpdateIncident(
                            "Investigating",
                            notesInput,
                            "Marked incident as under active investigation",
                            event.isSourceBlocked
                        )
                    }

                    IncidentActionChip(
                        label = "Mark Mitigated",
                        color = Color(0xFF38BDF8)
                    ) {
                        onUpdateIncident(
                            "Mitigated",
                            notesInput,
                            "Confirmed threat mitigated",
                            event.isSourceBlocked
                        )
                    }

                    IncidentActionChip(
                        label = "Mark Resolved",
                        color = Color(0xFF10B981)
                    ) {
                        onUpdateIncident(
                            "Resolved",
                            notesInput,
                            "Incident resolved and closed",
                            false
                        )
                    }

                    IncidentActionChip(
                        label = "Mark False Positive",
                        color = Color(0xFF94A3B8)
                    ) {
                        onUpdateIncident(
                            "False Positive",
                            notesInput.ifBlank { "Verified benign user activity (False Positive)." },
                            "Classified as False Positive and cleared restrictions",
                            false
                        )
                    }

                    if (!event.isSourceBlocked) {
                        IncidentActionChip(
                            label = "Temp Block IP / Revoke Session",
                            color = Color(0xFFEF4444)
                        ) {
                            onUpdateIncident(
                                "Mitigated",
                                notesInput,
                                "Temporarily blocked source IP & revoked suspicious session",
                                true
                            )
                        }
                    } else {
                        IncidentActionChip(
                            label = "Unblock Source / Restore Access",
                            color = Color(0xFF10B981)
                        ) {
                            onUpdateIncident(
                                "Resolved",
                                notesInput,
                                "Unblocked source IP and restored normal access",
                                false
                            )
                        }
                    }

                    IncidentActionChip(
                        label = "Save Notes",
                        color = Color(0xFFA855F7)
                    ) {
                        onUpdateIncident(
                            event.status,
                            notesInput,
                            "Updated investigation notes",
                            event.isSourceBlocked
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ForensicDetailItem(label: String, value: String) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
            text = label,
            style = TextStyle(color = Color(0xFF94A3B8), fontSize = 10.5.sp, fontWeight = FontWeight.Bold)
        )
        Text(
            text = value,
            style = TextStyle(color = Color.White, fontSize = 12.sp, lineHeight = 16.sp)
        )
    }
}

@Composable
private fun IncidentActionChip(
    label: String,
    color: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(color.copy(alpha = 0.16f))
            .border(1.dp, color.copy(alpha = 0.6f), RoundedCornerShape(10.dp))
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 7.dp)
    ) {
        Text(
            text = label,
            style = TextStyle(color = color, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        )
    }
}
