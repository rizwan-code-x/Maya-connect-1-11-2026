package com.example.ui.screens

import android.content.Intent
import android.net.Uri
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import com.example.ui.components.rememberChromeSmoothFlingBehavior
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.HeadsetMic
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.QuestionAnswer
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import com.example.data.auth.AuthCredentialsConfig
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.SupportTicketEntity
import com.example.ui.components.glassmorphic
import com.example.ui.theme.MayaTheme
import com.example.ui.viewmodel.MayaViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Points 8 & 10: Dedicated Customer Care & Support Interface with ticket creation,
 * status tracking, admin responses, and conversation history.
 */
@Composable
fun CustomerSupportScreen(
    viewModel: MayaViewModel,
    onClose: () -> Unit
) {
    val context = LocalContext.current
    val theme = MayaTheme.colors
    val tickets by viewModel.allTickets.collectAsStateWithLifecycle()
    var selectedTicket by remember { mutableStateOf<SupportTicketEntity?>(null) }
    var showCreateDialog by remember { mutableStateOf(false) }
    var selectedFilter by remember { mutableStateOf("ALL") }

    val filteredTickets = remember(tickets, selectedFilter) {
        when (selectedFilter) {
            "OPEN" -> tickets.filter { it.status == "OPEN" }
            "RESOLVED" -> tickets.filter { it.status == "RESOLVED" }
            else -> tickets
        }
    }

    BackHandler {
        if (selectedTicket != null) {
            selectedTicket = null
        } else {
            onClose()
        }
    }

    // If viewing a single ticket conversation
    if (selectedTicket != null) {
        val current = tickets.find { it.id == selectedTicket!!.id } ?: selectedTicket!!
        TicketConversationView(
            ticket = current,
            onBack = { selectedTicket = null },
            onSendReply = { replyText ->
                viewModel.replyToSupportTicket(current.id, replyText)
            }
        )
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(theme.backgroundGradient)
            .statusBarsPadding()
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        // 1. Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onClose,
                modifier = Modifier
                    .clip(CircleShape)
                    .background(theme.surfaceGlass)
                    .border(1.dp, theme.borderGlass, CircleShape)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = theme.textPrimary
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Customer Care & Support",
                    style = TextStyle(
                        color = theme.textPrimary,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
                Text(
                    text = "Official Maya Connect Desk",
                    style = TextStyle(
                        color = theme.accentPrimary,
                        fontSize = 11.sp
                    )
                )
            }

            IconButton(
                onClick = { showCreateDialog = true },
                modifier = Modifier
                    .clip(CircleShape)
                    .background(theme.accentPrimary)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "New Ticket",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 2. Desk Intro Banner
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .glassmorphic(
                    shape = RoundedCornerShape(20.dp),
                    backgroundColor = theme.surfaceGlass,
                    borderColor = theme.borderGlass,
                    borderWidth = 1.dp
                )
                .padding(14.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(Color(0x2238BDF8)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.HeadsetMic,
                        contentDescription = null,
                        tint = Color(0xFF38BDF8),
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Need Assistance or Have Feedback?",
                        style = TextStyle(
                            color = theme.textPrimary,
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Text(
                        text = "Submit a ticket for technical support, bug reports, or feature ideas.",
                        style = TextStyle(
                            color = theme.textSecondary,
                            fontSize = 11.sp
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Official Direct Email Support Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(
                    Brush.horizontalGradient(
                        listOf(Color(0xFF0F172A), Color(0xFF1E293B))
                    )
                )
                .border(1.dp, Color(0xFF0284C7).copy(alpha = 0.4f), RoundedCornerShape(18.dp))
                .clickable {
                    try {
                        val intent = Intent(Intent.ACTION_SENDTO).apply {
                            data = Uri.parse("mailto:${AuthCredentialsConfig.OFFICIAL_SUPPORT_EMAIL}")
                            putExtra(Intent.EXTRA_SUBJECT, "[Maya Connect Support Request]")
                        }
                        context.startActivity(intent)
                    } catch (_: Exception) {}
                }
                .padding(14.dp)
        ) {
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
                            .background(Color(0xFF0284C7).copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Email,
                            contentDescription = null,
                            tint = Color(0xFF38BDF8),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Official Customer Care Email",
                            style = TextStyle(
                                color = Color.White,
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Text(
                            text = AuthCredentialsConfig.OFFICIAL_SUPPORT_EMAIL,
                            style = TextStyle(
                                color = Color(0xFF38BDF8),
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF0284C7).copy(alpha = 0.3f))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Email",
                            style = TextStyle(
                                color = Color(0xFFE0F2FE),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.OpenInNew,
                            contentDescription = null,
                            tint = Color(0xFFE0F2FE),
                            modifier = Modifier.size(13.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 3. Filter Tabs (All, Open, Resolved)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("ALL" to "All Tickets", "OPEN" to "Active / Open", "RESOLVED" to "Resolved").forEach { (key, label) ->
                val isSelected = selectedFilter == key
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isSelected) theme.accentPrimary else theme.surfaceGlass)
                        .border(
                            1.dp,
                            if (isSelected) theme.accentPrimary else theme.borderGlass,
                            RoundedCornerShape(12.dp)
                        )
                        .clickable { selectedFilter = key }
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

        Spacer(modifier = Modifier.height(14.dp))

        // 4. Tickets List
        if (filteredTickets.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.QuestionAnswer,
                        contentDescription = null,
                        tint = theme.textSecondary,
                        modifier = Modifier.size(46.dp)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "No Support Tickets Found",
                        style = TextStyle(
                            color = theme.textPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Tap the + button to create a new ticket or inquiry",
                        style = TextStyle(
                            color = theme.textSecondary,
                            fontSize = 12.sp
                        )
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                flingBehavior = rememberChromeSmoothFlingBehavior(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredTickets, key = { it.id }) { ticket ->
                    SupportTicketCard(
                        ticket = ticket,
                        onClick = { selectedTicket = ticket }
                    )
                }
            }
        }
    }

    // Submit New Ticket Dialog
    if (showCreateDialog) {
        CreateTicketDialog(
            onDismiss = { showCreateDialog = false },
            onSubmit = { subject, category, message ->
                viewModel.submitSupportTicket(subject, category, message)
                showCreateDialog = false
            }
        )
    }
}

@Composable
private fun SupportTicketCard(
    ticket: SupportTicketEntity,
    onClick: () -> Unit
) {
    val theme = MayaTheme.colors
    val dateStr = remember(ticket.createdAt) {
        SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(ticket.createdAt))
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(theme.surfaceGlass)
            .border(1.dp, theme.borderGlass, RoundedCornerShape(18.dp))
            .clickable { onClick() }
            .padding(14.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = ticket.id,
                        style = TextStyle(
                            color = theme.accentPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(theme.surface.copy(alpha = 0.5f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = ticket.category,
                            style = TextStyle(
                                color = theme.textSecondary,
                                fontSize = 9.5.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                    }
                }

                // Status Chip
                val statusColor = when (ticket.status) {
                    "RESOLVED" -> Color(0xFF10B981)
                    "IN_PROGRESS" -> Color(0xFFF59E0B)
                    else -> Color(0xFF38BDF8)
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(statusColor.copy(alpha = 0.16f))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = ticket.status,
                        style = TextStyle(
                            color = statusColor,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = ticket.subject,
                style = TextStyle(
                    color = theme.textPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(3.dp))

            Text(
                text = ticket.initialMessage,
                style = TextStyle(
                    color = theme.textSecondary,
                    fontSize = 12.sp
                ),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Submitted $dateStr",
                    style = TextStyle(color = theme.textSecondary.copy(alpha = 0.7f), fontSize = 10.5.sp)
                )

                if (ticket.hasUnreadReply) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFFEF4444))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "New Admin Reply",
                            style = TextStyle(color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TicketConversationView(
    ticket: SupportTicketEntity,
    onBack: () -> Unit,
    onSendReply: (String) -> Unit
) {
    val theme = MayaTheme.colors
    val messages = remember(ticket.conversationJson) { ticket.getMessages() }
    var replyText by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(theme.backgroundGradient)
            .statusBarsPadding()
            .imePadding()
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .clip(CircleShape)
                    .background(theme.surfaceGlass)
                    .border(1.dp, theme.borderGlass, CircleShape)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = theme.textPrimary
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = ticket.id,
                    style = TextStyle(
                        color = theme.accentPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
                Text(
                    text = ticket.subject,
                    style = TextStyle(
                        color = theme.textPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Messages List
        LazyColumn(
            modifier = Modifier.weight(1f),
            flingBehavior = rememberChromeSmoothFlingBehavior(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(messages) { msg ->
                val isAdmin = msg.sender == "ADMIN"
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = if (isAdmin) Alignment.Start else Alignment.End
                ) {
                    Text(
                        text = if (isAdmin) "Admin (${msg.senderName})" else "You (${msg.senderName})",
                        style = TextStyle(
                            color = if (isAdmin) Color(0xFF38BDF8) else theme.accentPrimary,
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.85f)
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                if (isAdmin) Color(0x330284C7) else theme.surfaceGlass
                            )
                            .border(
                                1.dp,
                                if (isAdmin) Color(0x550284C7) else theme.borderGlass,
                                RoundedCornerShape(16.dp)
                            )
                            .padding(12.dp)
                    ) {
                        Text(
                            text = msg.text,
                            style = TextStyle(
                                color = theme.textPrimary,
                                fontSize = 13.5.sp,
                                lineHeight = 19.sp
                            )
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Reply Input
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(theme.surfaceGlass)
                .border(1.dp, theme.borderGlass, RoundedCornerShape(20.dp))
                .padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            BasicTextField(
                value = replyText,
                onValueChange = { replyText = it },
                modifier = Modifier.weight(1f),
                textStyle = TextStyle(color = theme.textPrimary, fontSize = 13.5.sp),
                cursorBrush = SolidColor(theme.accentPrimary),
                decorationBox = { inner ->
                    if (replyText.isEmpty()) {
                        Text(
                            text = "Write your reply...",
                            style = TextStyle(color = theme.textSecondary, fontSize = 13.sp)
                        )
                    }
                    inner()
                }
            )

            IconButton(
                onClick = {
                    if (replyText.isNotBlank()) {
                        onSendReply(replyText.trim())
                        replyText = ""
                    }
                },
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = "Send",
                    tint = theme.accentPrimary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
private fun CreateTicketDialog(
    onDismiss: () -> Unit,
    onSubmit: (subject: String, category: String, message: String) -> Unit
) {
    val theme = MayaTheme.colors
    var subject by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("General") }
    var message by remember { mutableStateOf("") }

    val categories = listOf("General", "Bug Report", "App Request", "Ecosystem", "Feedback")

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .glassmorphic(
                    shape = RoundedCornerShape(24.dp),
                    backgroundColor = theme.surface,
                    borderColor = theme.borderGlass,
                    borderWidth = 1.dp
                )
                .padding(20.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Submit Support Ticket",
                        style = TextStyle(
                            color = theme.textPrimary,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = theme.textSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Subject
                BasicTextField(
                    value = subject,
                    onValueChange = { subject = it },
                    singleLine = true,
                    textStyle = TextStyle(color = theme.textPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold),
                    cursorBrush = SolidColor(theme.accentPrimary),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(theme.surfaceGlass)
                        .border(1.dp, theme.borderGlass, RoundedCornerShape(12.dp))
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    decorationBox = { inner ->
                        if (subject.isEmpty()) {
                            Text("Subject / Issue Summary...", style = TextStyle(color = theme.textSecondary, fontSize = 14.sp))
                        }
                        inner()
                    }
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Category Chips
                Text(
                    text = "CATEGORY",
                    style = TextStyle(color = theme.textSecondary, fontSize = 10.5.sp, fontWeight = FontWeight.Bold)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    categories.take(3).forEach { cat ->
                        val isSel = cat == category
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSel) theme.accentPrimary else theme.surfaceGlass)
                                .clickable { category = cat }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = cat,
                                style = TextStyle(
                                    color = if (isSel) Color.White else theme.textSecondary,
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Message
                BasicTextField(
                    value = message,
                    onValueChange = { message = it },
                    textStyle = TextStyle(color = theme.textPrimary, fontSize = 13.5.sp, lineHeight = 19.sp),
                    cursorBrush = SolidColor(theme.accentPrimary),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(theme.surfaceGlass)
                        .border(1.dp, theme.borderGlass, RoundedCornerShape(12.dp))
                        .padding(12.dp),
                    decorationBox = { inner ->
                        if (message.isEmpty()) {
                            Text("Describe your inquiry or issue in detail...", style = TextStyle(color = theme.textSecondary, fontSize = 13.sp))
                        }
                        inner()
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Submit Button
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(theme.accentPrimary)
                        .clickable {
                            if (subject.isNotBlank() && message.isNotBlank()) {
                                onSubmit(subject.trim(), category, message.trim())
                            }
                        }
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Submit Ticket",
                        style = TextStyle(color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    )
                }
            }
        }
    }
}
