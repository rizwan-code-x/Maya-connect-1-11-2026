package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.UUID

@Database(
    entities = [
        AppEntity::class,
        NoteEntity::class,
        SupportTicketEntity::class,
        NotificationEntity::class,
        EcosystemLinkEntity::class,
        TaskEntity::class,
        RegisteredUserEntity::class,
        AccountAuditLogEntity::class,
        DataDeletionRequestEntity::class,
        SecurityEventEntity::class
    ],
    version = 9,
    exportSchema = false
)
abstract class MayaDatabase : RoomDatabase() {
    abstract fun appDao(): AppDao
    abstract fun noteDao(): NoteDao
    abstract fun supportTicketDao(): SupportTicketDao
    abstract fun notificationDao(): NotificationDao
    abstract fun ecosystemLinkDao(): EcosystemLinkDao
    abstract fun taskDao(): TaskDao
    abstract fun registeredUserDao(): RegisteredUserDao
    abstract fun accountAuditLogDao(): AccountAuditLogDao
    abstract fun dataDeletionRequestDao(): DataDeletionRequestDao
    abstract fun securityEventDao(): SecurityEventDao

    companion object {
        @Volatile
        private var INSTANCE: MayaDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): MayaDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MayaDatabase::class.java,
                    "maya_connect_db"
                )
                    .fallbackToDestructiveMigration(dropAllTables = true)
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            INSTANCE?.let { database ->
                                scope.launch(Dispatchers.IO) {
                                    // 1. Initial Apps
                                    val entities = DefaultApps.initialApps.map { AppEntity.fromDomainModel(it) }
                                    database.appDao().insertApps(entities)

                                    // 2. Initial Notes for Notepad
                                    val initialNotes = listOf(
                                        NoteEntity(
                                            id = UUID.randomUUID().toString(),
                                            title = "Welcome to Maya Connect",
                                            content = "Maya Connect v3.0 is live! Explore the new liquid glass interface, modular dashboard, continuous voice assistant, and seamless app launcher.",
                                            timestamp = System.currentTimeMillis() - 3600000,
                                            colorHex = 0xFF4F46E5,
                                            isPinned = true
                                        ),
                                        NoteEntity(
                                            id = UUID.randomUUID().toString(),
                                            title = "REXLIFY Ecosystem Architecture",
                                            content = "Key priorities: NeuraX neural analytics, SIFRA assistant modules, and cloud persistent synchronization for Rizwan RR.",
                                            timestamp = System.currentTimeMillis() - 86400000,
                                            colorHex = 0xFF10B981,
                                            isPinned = false
                                        )
                                    )
                                    initialNotes.forEach { database.noteDao().insertNote(it) }

                                    // 3. Initial Notifications
                                    val initialNotifications = listOf(
                                        NotificationEntity(
                                            id = UUID.randomUUID().toString(),
                                            title = "Welcome, Rizwan RR",
                                            message = "Maya Connect has been upgraded to the futuristic glass design. Your personalized dashboard and apps are ready.",
                                            type = "ANNOUNCEMENT",
                                            timestamp = System.currentTimeMillis() - 1800000,
                                            isRead = false
                                        ),
                                        NotificationEntity(
                                            id = UUID.randomUUID().toString(),
                                            title = "Ecosystem Status: All Systems Operational",
                                            message = "REXLIFY, NeuraX, NooraX, and SIFRA network nodes are running with 100% uptime.",
                                            type = "ECOSYSTEM_UPDATE",
                                            timestamp = System.currentTimeMillis() - 7200000,
                                            isRead = false
                                        )
                                    )
                                    database.notificationDao().insertNotifications(initialNotifications)

                                    // 4. Initial Ecosystem Directory Links
                                    val initialLinks = listOf(
                                        EcosystemLinkEntity(
                                            id = "eco_rexlify",
                                            name = "REXLIFY Portal",
                                            url = "https://rexlify.netlify.app/",
                                            description = "Central digital application ecosystem and student technology hub.",
                                            iconKey = "REXLIFY",
                                            badge = "Core Hub",
                                            isOfficial = true,
                                            orderIndex = 1
                                        ),
                                        EcosystemLinkEntity(
                                            id = "eco_neurax",
                                            name = "NeuraX AI Suite",
                                            url = "https://neurax.ai.studio/",
                                            description = "AI-powered analyzer and daily-life helper application.",
                                            iconKey = "NEURAX",
                                            badge = "AI Engine",
                                            isOfficial = true,
                                            orderIndex = 2
                                        ),
                                        EcosystemLinkEntity(
                                            id = "eco_noorax",
                                            name = "NooraX Application",
                                            url = "https://noorax.ai.studio/",
                                            description = "Dedicated application project within the Maya ecosystem.",
                                            iconKey = "NOORAX",
                                            badge = "Product",
                                            isOfficial = true,
                                            orderIndex = 3
                                        ),
                                        EcosystemLinkEntity(
                                            id = "eco_sifra",
                                            name = "SIFRA AI Assistant",
                                            url = "https://rexlify.netlify.app/sifra",
                                            description = "Independent autonomous AI assistant project by Rizwan.",
                                            iconKey = "SIFRA",
                                            badge = "Autonomous AI",
                                            isOfficial = true,
                                            orderIndex = 4
                                        ),
                                        EcosystemLinkEntity(
                                            id = "eco_rodp",
                                            name = "RODP Platform",
                                            url = "https://rexlify.netlify.app/rodp",
                                            description = "Rizwan Online Dreams Platform for future digital utilities.",
                                            iconKey = "RODP",
                                            badge = "Platform",
                                            isOfficial = true,
                                            orderIndex = 5
                                        ),
                                        EcosystemLinkEntity(
                                            id = "eco_ph_veg",
                                            name = "PH VEG",
                                            url = "https://phvegetables.ai.studio/",
                                            description = "Official PH VEG e-commerce platform for fresh vegetables and agriculture produce.",
                                            iconKey = "PH_VEG",
                                            badge = "E-Commerce",
                                            isOfficial = true,
                                            orderIndex = 6
                                        ),
                                        EcosystemLinkEntity(
                                            id = "eco_status",
                                            name = "Ecosystem Health & Status",
                                            url = "https://rexlify.netlify.app/status",
                                            description = "Live uptime monitoring, latency tracking, and server health.",
                                            iconKey = "MAYA_OS",
                                            badge = "Status",
                                            isOfficial = true,
                                            orderIndex = 6
                                        )
                                    )
                                    database.ecosystemLinkDao().insertLinks(initialLinks)
                                    // 5. Initial Tasks (Google Tasks Style)
                                    val initialTasks = listOf(
                                        TaskEntity(
                                            id = UUID.randomUUID().toString(),
                                            title = "Check Maya Connect Ecosystem Updates",
                                            details = "Verify NeuraX, SIFRA, and RODP status in Maya Connect",
                                            dueDate = "Today",
                                            listCategory = "My Tasks",
                                            isCompleted = false,
                                            isStarred = true
                                        ),
                                        TaskEntity(
                                            id = UUID.randomUUID().toString(),
                                            title = "Review Daily Routine & Prayer Times",
                                            details = "Check real-time sunrise, sunset & Iftar times via Voice Assistant",
                                            dueDate = "Today",
                                            listCategory = "Daily Routine",
                                            isCompleted = false,
                                            isStarred = false
                                        ),
                                        TaskEntity(
                                            id = UUID.randomUUID().toString(),
                                            title = "Set Up Microsoft Edge & Apps Registry",
                                            details = "Organize digital apps grid in Application Management",
                                            dueDate = "Tomorrow",
                                            listCategory = "Work",
                                            isCompleted = true,
                                            completedAt = System.currentTimeMillis()
                                        )
                                    )
                                    initialTasks.forEach { database.taskDao().insertTask(it) }

                                    // 6. Initial Registered Users (for CEO Real-Time Admin Panel)
                                    val now = System.currentTimeMillis()
                                    val initialUsers = listOf(
                                        RegisteredUserEntity("usr_ceo_rizwan", "Rizwan RR", "CEO_PANEL", now - 86400000L * 30, "ACTIVE", 0, now),
                                        RegisteredUserEntity("usr_fam_arjun", "Arjun RRR", "RRR_FAMILY", now - 86400000L * 14, "ACTIVE", 0, now - 3600000L * 2),
                                        RegisteredUserEntity("usr_fam_priya", "Priya Sen", "RRR_FAMILY", now - 86400000L * 10, "ACTIVE", 0, now - 3600000L * 5),
                                        RegisteredUserEntity("usr_fam_karan", "Karan Roy", "RRR_FAMILY", now - 86400000L * 7, "ACTIVE", 0, now - 3600000L * 12),
                                        RegisteredUserEntity("usr_fam_ananya", "Ananya Das", "RRR_FAMILY", now - 86400000L * 3, "ACTIVE", 0, now - 1800000L),
                                        RegisteredUserEntity("usr_gst_alex", "Alex Rivera", "STANDARD", now - 86400000L * 5, "ACTIVE", 0, now - 7200000L),
                                        RegisteredUserEntity("usr_gst_rahul", "Rahul Sharma", "STANDARD", now - 86400000L * 4, "ACTIVE", 0, now - 3600000L * 3),
                                        RegisteredUserEntity("usr_gst_sneha", "Sneha Roy", "STANDARD", now - 86400000L * 2, "ACTIVE", 0, now - 3600000L * 6),
                                        RegisteredUserEntity("usr_gst_david", "David Miller", "STANDARD", now - 86400000L * 1, "WARNING", 1, now - 3600000L * 8),
                                        RegisteredUserEntity("usr_gst_tanvir", "Tanvir Ahmed", "STANDARD", now - 86400000L * 2, "ACTIVE", 0, now - 3600000L * 4),
                                        RegisteredUserEntity("usr_gst_meera", "Meera Mukherjee", "STANDARD", now - 86400000L * 1, "ACTIVE", 0, now - 900000L)
                                    )
                                    database.registeredUserDao().insertUsers(initialUsers)

                                    // 7. Initial Data Deletion Requests
                                    val initialDeletionRequests = listOf(
                                        DataDeletionRequestEntity(
                                            id = "DEL-1021",
                                            userId = "usr_gst_rahul",
                                            userName = "Rahul Sharma",
                                            requestDate = now - 86400000L * 2,
                                            status = "PENDING",
                                            reason = "Account cleanup under Privacy Policy Section 4."
                                        ),
                                        DataDeletionRequestEntity(
                                            id = "DEL-1019",
                                            userId = "usr_gst_alex",
                                            userName = "Alex Rivera",
                                            requestDate = now - 86400000L * 4,
                                            status = "APPROVED",
                                            reason = "User requested data purging.",
                                            processedDate = now - 86400000L * 3
                                        )
                                    )
                                    database.dataDeletionRequestDao().insertRequests(initialDeletionRequests)

                                    // 8. Initial Audit Logs
                                    val initialAuditLogs = listOf(
                                        AccountAuditLogEntity(
                                            id = UUID.randomUUID().toString(),
                                            adminName = "Rizwan RR",
                                            targetUserId = "usr_gst_david",
                                            targetUserName = "David Miller",
                                            action = "WARN",
                                            reason = "Issued policy compliance warning",
                                            timestamp = now - 86400000L * 1
                                        ),
                                        AccountAuditLogEntity(
                                            id = UUID.randomUUID().toString(),
                                            adminName = "Rizwan RR",
                                            targetUserId = "usr_gst_alex",
                                            targetUserName = "Alex Rivera",
                                            action = "DELETE_APPROVED",
                                            reason = "Approved data deletion request #DEL-1019",
                                            timestamp = now - 86400000L * 3
                                        )
                                    )
                                    database.accountAuditLogDao().insertLogs(initialAuditLogs)

                                    // 9. Initial Security Monitoring Events (Test Data for Admin Security Center)
                                    val initialSecurityEvents = com.example.data.security.SecurityHardeningEngine.buildInitialSampleSecurityEvents(now)
                                    database.securityEventDao().insertEvents(initialSecurityEvents)
                                }
                            }
                        }
                    }).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
