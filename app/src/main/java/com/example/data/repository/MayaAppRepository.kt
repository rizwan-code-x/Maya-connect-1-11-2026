package com.example.data.repository

import com.example.data.local.AppDao
import com.example.data.local.AppEntity
import com.example.data.local.DefaultApps
import com.example.data.local.EcosystemLinkDao
import com.example.data.local.EcosystemLinkEntity
import com.example.data.local.NoteDao
import com.example.data.local.NoteEntity
import com.example.data.local.NotificationDao
import com.example.data.local.NotificationEntity
import com.example.data.local.SupportMessage
import com.example.data.local.SupportTicketDao
import com.example.data.local.SupportTicketEntity
import com.example.data.model.AppItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.util.UUID

import com.example.data.local.RegisteredUserDao
import com.example.data.local.RegisteredUserEntity
import com.example.data.local.TaskDao
import com.example.data.local.TaskEntity

import com.example.data.local.AccountAuditLogDao
import com.example.data.local.AccountAuditLogEntity
import com.example.data.local.DataDeletionRequestDao
import com.example.data.local.DataDeletionRequestEntity
import com.example.data.local.SecurityEventDao
import com.example.data.local.SecurityEventEntity
import com.example.data.remote.SupabaseService
import com.example.data.security.SecurityHardeningEngine

class MayaAppRepository(
    private val appDao: AppDao,
    private val noteDao: NoteDao,
    private val supportTicketDao: SupportTicketDao,
    private val notificationDao: NotificationDao,
    private val ecosystemLinkDao: EcosystemLinkDao,
    private val taskDao: TaskDao,
    private val registeredUserDao: RegisteredUserDao,
    private val accountAuditLogDao: AccountAuditLogDao,
    private val dataDeletionRequestDao: DataDeletionRequestDao,
    private val securityEventDao: SecurityEventDao
) {

    // --- Security Monitoring & Cyberattack Events (Security Center) ---
    val allSecurityEvents: Flow<List<SecurityEventEntity>> = securityEventDao.getAllSecurityEvents()

    suspend fun ensureSecurityEventsSeeded() = withContext(Dispatchers.IO) {
        val count = securityEventDao.getEventsCount()
        if (count == 0) {
            val sampleEvents = SecurityHardeningEngine.buildInitialSampleSecurityEvents(System.currentTimeMillis())
            securityEventDao.insertEvents(sampleEvents)
        }
    }

    suspend fun recordSecurityEvent(event: SecurityEventEntity) = withContext(Dispatchers.IO) {
        securityEventDao.insertEvent(event)
        SupabaseService.syncSecurityEventToSupabase(event)
    }

    suspend fun updateSecurityIncidentState(
        eventId: String,
        newStatus: String,
        investigationNotes: String,
        adminActionNote: String,
        isSourceBlocked: Boolean,
        adminName: String = "Rizwan RR"
    ) = withContext(Dispatchers.IO) {
        val existing = securityEventDao.getEventById(eventId) ?: return@withContext
        val timestampIso = SecurityHardeningEngine.formatUtcIsoTimestamp()
        val cleanNotes = SecurityHardeningEngine.sanitizeText(investigationNotes).take(2000)
        val updatedHistory = buildString {
            if (existing.adminActionHistory.isNotBlank()) {
                append(existing.adminActionHistory)
                append("\n")
            }
            append("[$timestampIso] Admin $adminName: $adminActionNote (Status -> $newStatus)")
        }
        securityEventDao.updateEventIncidentState(
            eventId = eventId,
            newStatus = newStatus,
            notes = cleanNotes,
            history = updatedHistory,
            isBlocked = isSourceBlocked
        )
        val updated = securityEventDao.getEventById(eventId)
        if (updated != null) {
            SupabaseService.syncSecurityEventToSupabase(updated)
        }
    }

    suspend fun unblockAllRestrictedSources(adminName: String = "Rizwan RR") = withContext(Dispatchers.IO) {
        val recoveryNote = "\n[${SecurityHardeningEngine.formatUtcIsoTimestamp()}] Admin $adminName: Authorized recovery & source unblock executed."
        securityEventDao.unblockAllBlockedSources(recoveryNote)
        SecurityHardeningEngine.unlockAllRateLimits()
    }

    suspend fun purgeOldResolvedSecurityEvents(retentionDays: Int = 30, adminName: String = "Rizwan RR") = withContext(Dispatchers.IO) {
        val cutoffMs = System.currentTimeMillis() - (retentionDays.toLong() * 86_400_000L)
        securityEventDao.purgeResolvedEventsOlderThan(cutoffMs)
        val auditLog = AccountAuditLogEntity(
            id = UUID.randomUUID().toString(),
            adminName = adminName,
            targetUserId = "SEC_RETENTION_POLICY",
            targetUserName = "Security Event Log Store",
            action = "PURGE_EXPIRED_LOGS",
            reason = "Executed DPDP/Privacy retention purge for resolved incidents older than $retentionDays days",
            timestamp = System.currentTimeMillis()
        )
        accountAuditLogDao.insertLog(auditLog)
    }

    // --- Registered Users (CEO & Admin Management) ---
    val allRegisteredUsers: Flow<List<RegisteredUserEntity>> = registeredUserDao.getAllUsers()
    val allAuditLogs: Flow<List<AccountAuditLogEntity>> = accountAuditLogDao.getAllLogs()
    val allDeletionRequests: Flow<List<DataDeletionRequestEntity>> = dataDeletionRequestDao.getAllRequests()

    suspend fun registerOrUpdateUser(
        userId: String,
        name: String,
        role: String
    ) = withContext(Dispatchers.IO) {
        val existing = registeredUserDao.getUserById(userId)
        val user = RegisteredUserEntity(
            userId = userId,
            name = name,
            role = role,
            registeredAt = existing?.registeredAt ?: System.currentTimeMillis(),
            status = existing?.status ?: "ACTIVE",
            warningCount = existing?.warningCount ?: 0,
            lastActiveAt = System.currentTimeMillis()
        )
        registeredUserDao.insertUser(user)
        SupabaseService.syncUserToSupabase(userId, name, role, user.status, user.warningCount)
    }

    suspend fun updateUserStatus(userId: String, status: String) = withContext(Dispatchers.IO) {
        registeredUserDao.updateUserStatus(userId, status)
    }

    suspend fun adminSetUserStatus(
        userId: String,
        userName: String,
        newStatus: String,
        reason: String,
        adminName: String = "Rizwan RR"
    ) = withContext(Dispatchers.IO) {
        registeredUserDao.updateUserStatus(userId, newStatus)
        val actionType = when (newStatus.uppercase()) {
            "SUSPENDED" -> "SUSPEND"
            "HOLD" -> "HOLD"
            "DEACTIVATED" -> "DEACTIVATE"
            "ACTIVE" -> "REACTIVATE"
            else -> newStatus.uppercase()
        }
        val auditId = UUID.randomUUID().toString()
        val log = AccountAuditLogEntity(
            id = auditId,
            adminName = adminName,
            targetUserId = userId,
            targetUserName = userName,
            action = actionType,
            reason = reason,
            timestamp = System.currentTimeMillis()
        )
        accountAuditLogDao.insertLog(log)

        val user = registeredUserDao.getUserById(userId)
        if (user != null) {
            SupabaseService.syncUserToSupabase(userId, user.name, user.role, newStatus, user.warningCount)
        }
        SupabaseService.logAuditToSupabase(auditId, adminName, userId, userName, actionType, reason)
    }

    suspend fun submitDataDeletionRequest(
        userId: String,
        userName: String,
        reason: String
    ) = withContext(Dispatchers.IO) {
        val randomNum = (1000..9999).random()
        val requestId = "DEL-$randomNum"
        val request = DataDeletionRequestEntity(
            id = requestId,
            userId = userId,
            userName = userName,
            requestDate = System.currentTimeMillis(),
            status = "PENDING",
            reason = reason.ifBlank { "User submitted deletion request under Privacy Policy." }
        )
        dataDeletionRequestDao.insertRequest(request)
        SupabaseService.syncDeletionRequestToSupabase(requestId, userId, userName, "PENDING", request.reason)
    }

    suspend fun adminApproveDeletionRequest(
        requestId: String,
        adminName: String = "Rizwan RR"
    ) = withContext(Dispatchers.IO) {
        val request = dataDeletionRequestDao.getRequestById(requestId) ?: return@withContext
        dataDeletionRequestDao.updateRequestStatus(
            requestId = requestId,
            status = "APPROVED",
            processedDate = System.currentTimeMillis(),
            rejectionReason = ""
        )
        // Purge user profile & records
        registeredUserDao.deleteUser(request.userId)

        val auditId = UUID.randomUUID().toString()
        val log = AccountAuditLogEntity(
            id = auditId,
            adminName = adminName,
            targetUserId = request.userId,
            targetUserName = request.userName,
            action = "DELETE_APPROVED",
            reason = "Approved data deletion request $requestId",
            timestamp = System.currentTimeMillis()
        )
        accountAuditLogDao.insertLog(log)
        SupabaseService.syncDeletionRequestToSupabase(requestId, request.userId, request.userName, "APPROVED", request.reason)
        SupabaseService.logAuditToSupabase(auditId, adminName, request.userId, request.userName, "DELETE_APPROVED", "Approved data deletion request $requestId")
    }

    suspend fun adminRejectDeletionRequest(
        requestId: String,
        rejectionReason: String,
        adminName: String = "Rizwan RR"
    ) = withContext(Dispatchers.IO) {
        val request = dataDeletionRequestDao.getRequestById(requestId) ?: return@withContext
        dataDeletionRequestDao.updateRequestStatus(
            requestId = requestId,
            status = "REJECTED",
            processedDate = System.currentTimeMillis(),
            rejectionReason = rejectionReason
        )

        val auditId = UUID.randomUUID().toString()
        val log = AccountAuditLogEntity(
            id = auditId,
            adminName = adminName,
            targetUserId = request.userId,
            targetUserName = request.userName,
            action = "DELETE_REJECTED",
            reason = rejectionReason.ifBlank { "Data deletion request rejected by Administrator." },
            timestamp = System.currentTimeMillis()
        )
        accountAuditLogDao.insertLog(log)
        SupabaseService.syncDeletionRequestToSupabase(requestId, request.userId, request.userName, "REJECTED", request.reason)
        SupabaseService.logAuditToSupabase(auditId, adminName, request.userId, request.userName, "DELETE_REJECTED", rejectionReason)
    }

    suspend fun issueWarningToUser(userId: String) = withContext(Dispatchers.IO) {
        registeredUserDao.issueWarningToUser(userId)
    }

    suspend fun updateUserRole(userId: String, newRole: String) = withContext(Dispatchers.IO) {
        registeredUserDao.updateUserRole(userId, newRole)
    }

    suspend fun deleteUser(userId: String) = withContext(Dispatchers.IO) {
        registeredUserDao.deleteUser(userId)
    }

    // --- Tasks (SIFRA Daily Task Neural Memory) ---
    val allTasks: Flow<List<TaskEntity>> = taskDao.getAllTasks()

    suspend fun insertTask(
        title: String,
        details: String = "",
        dueDate: String = "Today",
        dueTime: String = "",
        listCategory: String = "Personal",
        priority: String = "Medium",
        isStarred: Boolean = false
    ) = withContext(Dispatchers.IO) {
        val task = TaskEntity(
            id = UUID.randomUUID().toString(),
            title = title.ifBlank { "New Task" },
            details = details,
            dueDate = dueDate.ifBlank { "Today" },
            dueTime = dueTime,
            listCategory = listCategory.ifBlank { "Personal" },
            priority = priority.ifBlank { "Medium" },
            isCompleted = false,
            isStarred = isStarred,
            createdAt = System.currentTimeMillis()
        )
        taskDao.insertTask(task)
    }

    suspend fun updateTask(task: TaskEntity) = withContext(Dispatchers.IO) {
        taskDao.updateTask(task)
    }

    suspend fun updateTaskStatus(id: String, isCompleted: Boolean) = withContext(Dispatchers.IO) {
        taskDao.updateTaskStatus(id, isCompleted, if (isCompleted) System.currentTimeMillis() else 0L)
    }

    suspend fun toggleTaskStarred(id: String, currentStarred: Boolean) = withContext(Dispatchers.IO) {
        taskDao.updateTaskStarred(id, !currentStarred)
    }

    suspend fun deleteTask(id: String) = withContext(Dispatchers.IO) {
        taskDao.deleteTask(id)
    }

    suspend fun clearCompletedTasks() = withContext(Dispatchers.IO) {
        taskDao.clearCompletedTasks()
    }

    // --- Apps ---
    val allApps: Flow<List<AppItem>> = appDao.getAllApps().map { list ->
        list.map { it.toDomainModel() }
    }

    val allAppsAdmin: Flow<List<AppItem>> = appDao.getAllAppsAdmin().map { list ->
        list.map { it.toDomainModel() }
    }

    val favoriteApps: Flow<List<AppItem>> = appDao.getFavoriteApps().map { list ->
        list.map { it.toDomainModel() }
    }

    val recentlyUsedApps: Flow<List<AppItem>> = appDao.getRecentlyUsedApps().map { list ->
        list.map { it.toDomainModel() }
    }

    val featuredApps: Flow<List<AppItem>> = appDao.getFeaturedApps().map { list ->
        list.map { it.toDomainModel() }
    }

    suspend fun ensureDefaultAppsSeeded() = withContext(Dispatchers.IO) {
        val count = appDao.getAppsCount()
        if (count == 0) {
            val entities = DefaultApps.initialApps.map { AppEntity.fromDomainModel(it) }
            appDao.insertApps(entities)
        } else {
            appDao.deleteAppById("photos")
            appDao.deleteAppById("google_photos")
            appDao.deleteAppById("math_solver")
            DefaultApps.initialApps.forEach { defaultApp ->
                val existing = appDao.getAppById(defaultApp.id)
                if (existing == null) {
                    appDao.insertApp(AppEntity.fromDomainModel(defaultApp))
                } else if (!existing.isCustom && (existing.iconKey != defaultApp.iconKey || existing.url != defaultApp.url)) {
                    appDao.insertApp(
                        AppEntity.fromDomainModel(
                            defaultApp.copy(
                                isFavorite = existing.isFavorite,
                                isPinnedHome = existing.isPinnedHome,
                                launchCount = existing.launchCount
                            )
                        )
                    )
                }
            }
        }
    }

    suspend fun getAppById(id: String): AppItem? = withContext(Dispatchers.IO) {
        appDao.getAppById(id)?.toDomainModel()
    }

    suspend fun toggleFavorite(id: String, currentFavorite: Boolean) = withContext(Dispatchers.IO) {
        appDao.updateFavorite(id, !currentFavorite)
    }

    suspend fun togglePinnedHome(id: String, currentPinned: Boolean) = withContext(Dispatchers.IO) {
        appDao.updatePinnedHome(id, !currentPinned)
    }

    suspend fun toggleAppEnabled(id: String, currentEnabled: Boolean) = withContext(Dispatchers.IO) {
        appDao.updateAppEnabled(id, !currentEnabled)
    }

    suspend fun recordAppLaunch(id: String) = withContext(Dispatchers.IO) {
        appDao.recordAppLaunch(id, System.currentTimeMillis())
    }

    suspend fun updateAppUrl(id: String, newUrl: String) = withContext(Dispatchers.IO) {
        appDao.updateAppUrl(id, newUrl)
    }

    suspend fun insertOrUpdateApp(app: AppItem) = withContext(Dispatchers.IO) {
        appDao.insertApp(AppEntity.fromDomainModel(app))
    }

    suspend fun deleteApp(id: String) = withContext(Dispatchers.IO) {
        appDao.deleteAppById(id)
    }

    suspend fun resetToDefaultRegistry() = withContext(Dispatchers.IO) {
        appDao.clearAllApps()
        val entities = DefaultApps.initialApps.map { AppEntity.fromDomainModel(it) }
        appDao.insertApps(entities)
    }

    suspend fun deleteAllUserData() = withContext(Dispatchers.IO) {
        try {
            noteDao.clearAllNotes()
        } catch (_: Exception) {}
        try {
            taskDao.clearAllTasks()
        } catch (_: Exception) {}
        try {
            notificationDao.clearAllNotifications()
        } catch (_: Exception) {}
        try {
            appDao.clearAllApps()
            val entities = DefaultApps.initialApps.map { AppEntity.fromDomainModel(it) }
            appDao.insertApps(entities)
        } catch (_: Exception) {}
    }

    suspend fun clearAllTasks() = withContext(Dispatchers.IO) {
        try { taskDao.clearAllTasks() } catch (_: Exception) {}
    }

    suspend fun clearAllNotes() = withContext(Dispatchers.IO) {
        try { noteDao.clearAllNotes() } catch (_: Exception) {}
    }

    // --- Notes (Notepad) ---
    val allNotes: Flow<List<NoteEntity>> = noteDao.getAllNotes()

    suspend fun insertNote(title: String, content: String, colorHex: Long = 0xFF4F46E5) = withContext(Dispatchers.IO) {
        val note = NoteEntity(
            id = UUID.randomUUID().toString(),
            title = title.ifBlank { "Untitled Note" },
            content = content,
            timestamp = System.currentTimeMillis(),
            colorHex = colorHex,
            isPinned = false
        )
        noteDao.insertNote(note)
    }

    suspend fun updateNote(note: NoteEntity) = withContext(Dispatchers.IO) {
        noteDao.updateNote(note.copy(timestamp = System.currentTimeMillis()))
    }

    suspend fun deleteNote(id: String) = withContext(Dispatchers.IO) {
        noteDao.deleteNoteById(id)
    }

    // --- Support Tickets ---
    val allTickets: Flow<List<SupportTicketEntity>> = supportTicketDao.getAllTickets()
    val openTicketsCount: Flow<Int> = supportTicketDao.getOpenTicketsCount()

    suspend fun createTicket(
        subject: String,
        category: String,
        message: String,
        userDisplayName: String = "Rizwan RR"
    ): String = withContext(Dispatchers.IO) {
        val randomNum = (1000..9999).random()
        val ticketId = "#MC-2026-$randomNum"
        val initialList = listOf(
            SupportMessage(
                sender = "USER",
                senderName = userDisplayName,
                text = message,
                timestamp = System.currentTimeMillis()
            )
        )
        val ticket = SupportTicketEntity(
            id = ticketId,
            userDisplayName = userDisplayName,
            subject = subject,
            category = category,
            initialMessage = message,
            status = "OPEN",
            conversationJson = SupportTicketEntity.buildConversationJson(initialList),
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis(),
            hasUnreadReply = false
        )
        supportTicketDao.insertTicket(ticket)

        // Generate immediate confirmation notification
        val notification = NotificationEntity(
            id = UUID.randomUUID().toString(),
            title = "Support Ticket Created ($ticketId)",
            message = "Your inquiry \"$subject\" was submitted. Rizwan RR / Admin will review and respond shortly.",
            type = "SUPPORT_REPLY",
            relatedTicketId = ticketId,
            timestamp = System.currentTimeMillis(),
            isRead = false
        )
        notificationDao.insertNotification(notification)

        ticketId
    }

    suspend fun replyToTicket(
        ticketId: String,
        sender: String,
        senderName: String,
        replyText: String,
        newStatus: String? = null
    ) = withContext(Dispatchers.IO) {
        val ticket = supportTicketDao.getTicketById(ticketId) ?: return@withContext
        val currentMessages = ticket.getMessages().toMutableList()
        currentMessages.add(
            SupportMessage(
                sender = sender,
                senderName = senderName,
                text = replyText,
                timestamp = System.currentTimeMillis()
            )
        )

        val updatedTicket = ticket.copy(
            conversationJson = SupportTicketEntity.buildConversationJson(currentMessages),
            updatedAt = System.currentTimeMillis(),
            status = newStatus ?: ticket.status,
            hasUnreadReply = sender == "ADMIN"
        )
        supportTicketDao.updateTicket(updatedTicket)

        // When admin replies, push notification to user
        if (sender == "ADMIN") {
            val notification = NotificationEntity(
                id = UUID.randomUUID().toString(),
                title = "New Reply on $ticketId",
                message = "Admin responded: \"${replyText.take(60)}${if (replyText.length > 60) "..." else ""}\"",
                type = "SUPPORT_REPLY",
                relatedTicketId = ticketId,
                timestamp = System.currentTimeMillis(),
                isRead = false
            )
            notificationDao.insertNotification(notification)
        }
    }

    suspend fun resolveTicket(ticketId: String) = withContext(Dispatchers.IO) {
        val ticket = supportTicketDao.getTicketById(ticketId) ?: return@withContext
        supportTicketDao.updateTicket(
            ticket.copy(
                status = "RESOLVED",
                updatedAt = System.currentTimeMillis()
            )
        )
    }

    suspend fun deleteTicket(ticketId: String) = withContext(Dispatchers.IO) {
        supportTicketDao.deleteTicketById(ticketId)
    }

    // --- Notifications ---
    val allNotifications: Flow<List<NotificationEntity>> = notificationDao.getAllNotifications()
    val unreadNotificationCount: Flow<Int> = notificationDao.getUnreadCount()

    suspend fun markNotificationAsRead(id: String) = withContext(Dispatchers.IO) {
        notificationDao.markAsRead(id)
    }

    suspend fun markAllNotificationsAsRead() = withContext(Dispatchers.IO) {
        notificationDao.markAllAsRead()
    }

    suspend fun deleteNotification(id: String) = withContext(Dispatchers.IO) {
        notificationDao.deleteNotificationById(id)
    }

    suspend fun sendBroadcastAnnouncement(title: String, message: String) = withContext(Dispatchers.IO) {
        val notification = NotificationEntity(
            id = UUID.randomUUID().toString(),
            title = title,
            message = message,
            type = "ANNOUNCEMENT",
            timestamp = System.currentTimeMillis(),
            isRead = false
        )
        notificationDao.insertNotification(notification)
    }

    // --- Ecosystem Links ---
    val allEcosystemLinks: Flow<List<EcosystemLinkEntity>> = ecosystemLinkDao.getAllLinks()

    suspend fun insertOrUpdateEcosystemLink(link: EcosystemLinkEntity) = withContext(Dispatchers.IO) {
        ecosystemLinkDao.insertLink(link)
    }

    suspend fun deleteEcosystemLink(id: String) = withContext(Dispatchers.IO) {
        ecosystemLinkDao.deleteLinkById(id)
    }
}
