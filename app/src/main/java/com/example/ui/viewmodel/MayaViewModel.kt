package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.EcosystemLinkEntity
import com.example.data.local.MayaDatabase
import com.example.data.local.NoteEntity
import com.example.data.local.NotificationEntity
import com.example.data.local.SupportTicketEntity
import com.example.data.local.TaskEntity
import com.example.data.model.AppCategory
import com.example.data.model.AppFolder
import com.example.data.model.AppItem
import com.example.data.model.EmergencyFeatureCatalog
import com.example.data.model.EmergencyFeatureItem
import com.example.data.model.EmergencyMode
import com.example.data.model.EmergencySystemState
import com.example.data.model.FamilyMemberAccount
import com.example.data.model.SearchHelper
import com.example.data.repository.MayaAppRepository
import com.example.data.service.MaintenanceService
import com.example.ui.components.NavigationTab
import com.example.ui.theme.MayaThemeMode
import com.example.data.audio.RexlifyIntroAudioGenerator
import com.example.data.auth.AuthCredentialsConfig
import com.example.data.auth.SecurityPermission
import com.example.data.auth.UserRole
import com.example.data.auth.UserSession
import com.example.data.auth.UserSessionManager
import com.example.data.local.SecurityEventEntity
import com.example.data.security.SecurityHardeningEngine
import com.example.ui.components.DashboardClockStyle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

data class MusicTrack(
    val id: String,
    val title: String,
    val artist: String,
    val durationSec: Int,
    val uriString: String? = null,
    val albumArtGradient: List<Long> = listOf(0xFF9B59B6, 0xFF6C3483),
    val description: String = "",
    val artworkStyleId: String = "skin",
    val isFavorite: Boolean = false,
    val embeddedArtBytes: ByteArray? = null
)

class MayaViewModel(application: Application) : AndroidViewModel(application) {

    private val database = MayaDatabase.getDatabase(application, viewModelScope)
    private val repository = MayaAppRepository(
        database.appDao(),
        database.noteDao(),
        database.supportTicketDao(),
        database.notificationDao(),
        database.ecosystemLinkDao(),
        database.taskDao(),
        database.registeredUserDao(),
        database.accountAuditLogDao(),
        database.dataDeletionRequestDao(),
        database.securityEventDao()
    )

    // --- Registered Users, Audit & Security Events Flows (CEO & Security Admin Management) ---
    val allRegisteredUsers: StateFlow<List<com.example.data.local.RegisteredUserEntity>> = repository.allRegisteredUsers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allAuditLogs: StateFlow<List<com.example.data.local.AccountAuditLogEntity>> = repository.allAuditLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allDeletionRequests: StateFlow<List<com.example.data.local.DataDeletionRequestEntity>> = repository.allDeletionRequests
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allSecurityEvents: StateFlow<List<SecurityEventEntity>> = repository.allSecurityEvents
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Apps Flows ---
    val allApps: StateFlow<List<AppItem>> = repository.allApps
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), com.example.data.local.DefaultApps.initialApps)

    val allAppsAdmin: StateFlow<List<AppItem>> = repository.allAppsAdmin
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), com.example.data.local.DefaultApps.initialApps)

    val favoriteApps: StateFlow<List<AppItem>> = repository.favoriteApps
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), com.example.data.local.DefaultApps.initialApps.filter { it.isFavorite })

    val recentlyUsedApps: StateFlow<List<AppItem>> = repository.recentlyUsedApps
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val featuredApps: StateFlow<List<AppItem>> = repository.featuredApps
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), com.example.data.local.DefaultApps.initialApps.filter { it.isFeatured })

    // --- Notes Flow (Notepad) ---
    val allNotes: StateFlow<List<NoteEntity>> = repository.allNotes
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Support Tickets Flow ---
    val allTickets: StateFlow<List<SupportTicketEntity>> = repository.allTickets
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val openTicketsCount: StateFlow<Int> = repository.openTicketsCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // --- Notifications Flow ---
    val allNotifications: StateFlow<List<NotificationEntity>> = repository.allNotifications
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val unreadNotificationCount: StateFlow<Int> = repository.unreadNotificationCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // --- Ecosystem Links Flow ---
    val allEcosystemLinks: StateFlow<List<EcosystemLinkEntity>> = repository.allEcosystemLinks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Google Tasks Flow ---
    val allTasks: StateFlow<List<com.example.data.local.TaskEntity>> = repository.allTasks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _showGoogleTasks = MutableStateFlow(false)
    val showGoogleTasks = _showGoogleTasks.asStateFlow()

    // UI & Navigation State
    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow(AppCategory.ALL)
    val selectedCategory = _selectedCategory.asStateFlow()

    private val _currentTab = MutableStateFlow(NavigationTab.HOME)
    val currentTab = _currentTab.asStateFlow()

    private val _themeMode = MutableStateFlow(MayaThemeMode.DARK)
    val themeMode = _themeMode.asStateFlow()

    private val _hapticEnabled = MutableStateFlow(true)
    val hapticEnabled = _hapticEnabled.asStateFlow()

    private val _reduceMotion = MutableStateFlow(false)
    val reduceMotion = _reduceMotion.asStateFlow()

    private val _notificationsEnabled = MutableStateFlow(true)
    val notificationsEnabled = _notificationsEnabled.asStateFlow()

    // Authentication & Profile Session Management
    private val sessionManager = UserSessionManager(application)
    private val _userSession = MutableStateFlow<UserSession?>(sessionManager.getCurrentSession())
    val userSession = _userSession.asStateFlow()

    private val _isUserLoggedIn = MutableStateFlow(sessionManager.isLoggedIn())
    val isUserLoggedIn = _isUserLoggedIn.asStateFlow()

    private val _currentUserRole = MutableStateFlow(sessionManager.getCurrentSession()?.role ?: UserRole.STANDARD)
    val currentUserRole = _currentUserRole.asStateFlow()

    private val _unauthorizedAppDialog = MutableStateFlow<AppItem?>(null)
    val unauthorizedAppDialog = _unauthorizedAppDialog.asStateFlow()

    // Owner Identity: Synced from persistent user session
    private val _userName = MutableStateFlow(sessionManager.getCurrentSession()?.userName ?: "User")
    val userName = _userName.asStateFlow()

    // Maya OS State & Wallpaper Customization
    private val _isMayaOsActive = MutableStateFlow(false)
    val isMayaOsActive = _isMayaOsActive.asStateFlow()

    private val _mayaOsWallpaper = MutableStateFlow("dynamic_abstract")
    val mayaOsWallpaper = _mayaOsWallpaper.asStateFlow()

    private val _customWallpaperUri = MutableStateFlow<String?>(null)
    val customWallpaperUri = _customWallpaperUri.asStateFlow()

    private val _customWallpapers = MutableStateFlow<List<String>>(emptyList())
    val customWallpapers = _customWallpapers.asStateFlow()

    private val _mayaOsGridSize = MutableStateFlow(4)
    val mayaOsGridSize = _mayaOsGridSize.asStateFlow()

    private val _mayaOsRecentApps = MutableStateFlow<List<AppItem>>(emptyList())
    val mayaOsRecentApps = _mayaOsRecentApps.asStateFlow()

    // Dashboard Clock Style (Analogue Watch or 5 Digital Clocks)
    private val _dashboardClockStyle = MutableStateFlow(DashboardClockStyle.ANALOGUE)
    val dashboardClockStyle = _dashboardClockStyle.asStateFlow()

    // Home Screen Widgets
    private val _activeHomeWidgets = MutableStateFlow<List<com.example.ui.components.HomeWidgetType>>(
        listOf(
            com.example.ui.components.HomeWidgetType.WEATHER,
            com.example.ui.components.HomeWidgetType.CALENDAR,
            com.example.ui.components.HomeWidgetType.BATTERY_STATUS
        )
    )
    val activeHomeWidgets: StateFlow<List<com.example.ui.components.HomeWidgetType>> = _activeHomeWidgets.asStateFlow()

    // App Drawer Overlay State
    private val _showAppDrawer = MutableStateFlow(false)
    val showAppDrawer = _showAppDrawer.asStateFlow()

    // Home Screen Folders (Drag and Drop Organization)
    private val _homeFolders = MutableStateFlow<List<AppFolder>>(emptyList())
    val homeFolders = _homeFolders.asStateFlow()

    // Downward-Swipeable Notification Shade State
    private val _isNotificationShadeOpen = MutableStateFlow(false)
    val isNotificationShadeOpen = _isNotificationShadeOpen.asStateFlow()

    // Active App Launch & Dialogs
    private val _activeAppForLaunch = MutableStateFlow<AppItem?>(null)
    val activeAppForLaunch = _activeAppForLaunch.asStateFlow()

    private val _selectedAppForAction = MutableStateFlow<AppItem?>(null)
    val selectedAppForAction = _selectedAppForAction.asStateFlow()

    private val _selectedAppForDetails = MutableStateFlow<AppItem?>(null)
    val selectedAppForDetails = _selectedAppForDetails.asStateFlow()

    private val _selectedAppForEdit = MutableStateFlow<AppItem?>(null)
    val selectedAppForEdit = _selectedAppForEdit.asStateFlow()

    private val _showAddAppDialog = MutableStateFlow(false)
    val showAddAppDialog = _showAddAppDialog.asStateFlow()

    private val _osNotes = MutableStateFlow("Maya Connect v3.0 • REXLIFY Ecosystem Node Active\nAll services operational and secure.")
    val osNotes = _osNotes.asStateFlow()

    // Assistant Screens
    private val _showChatAssistant = MutableStateFlow(false)
    val showChatAssistant = _showChatAssistant.asStateFlow()

    private val _chatAssistantInitialQuery = MutableStateFlow("")
    val chatAssistantInitialQuery = _chatAssistantInitialQuery.asStateFlow()

    private val _showVoiceAssistant = MutableStateFlow(false)
    val showVoiceAssistant = _showVoiceAssistant.asStateFlow()

    private val _showEcosystemLanding = MutableStateFlow(false)
    val showEcosystemLanding = _showEcosystemLanding.asStateFlow()

    // --- Points 3, 5, 7, 8, 9, 10, 11: Dedicated Module Screens ---
    private val _showNotificationCenter = MutableStateFlow(false)
    val showNotificationCenter = _showNotificationCenter.asStateFlow()

    private val _showAllAppsLauncher = MutableStateFlow(false)
    val showAllAppsLauncher = _showAllAppsLauncher.asStateFlow()

    private val _showMusicPlayer = MutableStateFlow(false)
    val showMusicPlayer = _showMusicPlayer.asStateFlow()

    private val _showNotepad = MutableStateFlow(false)
    val showNotepad = _showNotepad.asStateFlow()

    private val _showEcosystemDirectory = MutableStateFlow(false)
    val showEcosystemDirectory = _showEcosystemDirectory.asStateFlow()

    private val _showCustomerSupport = MutableStateFlow(false)
    val showCustomerSupport = _showCustomerSupport.asStateFlow()

    private val _showOfficialProfile = MutableStateFlow(false)
    val showOfficialProfile = _showOfficialProfile.asStateFlow()

    private val _showAdminPanel = MutableStateFlow(false)
    val showAdminPanel = _showAdminPanel.asStateFlow()

    private val _showTermsAndConditions = MutableStateFlow(false)
    val showTermsAndConditions = _showTermsAndConditions.asStateFlow()

    private val _showPrivacyPolicy = MutableStateFlow(false)
    val showPrivacyPolicy = _showPrivacyPolicy.asStateFlow()

    private val _showDiagnostics = MutableStateFlow(false)
    val showDiagnostics = _showDiagnostics.asStateFlow()

    // Admin Authentication State
    private val _isAdminAuthenticated = MutableStateFlow(false)
    val isAdminAuthenticated = _isAdminAuthenticated.asStateFlow()

    private val _adminLoginError = MutableStateFlow<String?>(null)
    val adminLoginError = _adminLoginError.asStateFlow()

    // Emergency Situation & Realtime Family Management via MaintenanceService (Firebase Realtime DB + Local)
    val maintenanceService = MaintenanceService(sessionManager)
    val emergencySystemState: StateFlow<EmergencySystemState> = maintenanceService.emergencyState
    val familyMembers: StateFlow<List<FamilyMemberAccount>> = maintenanceService.familyMembers

    private val _emergencyAlertTitle = MutableStateFlow<String?>(null)
    val emergencyAlertTitle: StateFlow<String?> = _emergencyAlertTitle.asStateFlow()

    private val _emergencyAlertMessage = MutableStateFlow<String?>(null)
    val emergencyAlertMessage: StateFlow<String?> = _emergencyAlertMessage.asStateFlow()

    fun dismissEmergencyAlert() {
        _emergencyAlertTitle.value = null
        _emergencyAlertMessage.value = null
    }

    fun showEmergencyAlert(title: String, message: String) {
        _emergencyAlertTitle.value = title
        _emergencyAlertMessage.value = message
    }

    fun adminSetEmergencyMode(
        mode: EmergencyMode,
        reason: String = "Admin action",
        customDurationHours: Int = emergencySystemState.value.customDurationHours
    ) {
        maintenanceService.updateEmergencyMode(mode, reason, customDurationHours)
        recordClientSecurityEvent(
            category = "SYSTEM_CONFIGURATION",
            severity = if (mode == EmergencyMode.NORMAL) "LOW" else "CRITICAL",
            endpoint = "/admin/emergency/mode",
            reason = "Admin (Rizwan RR) updated emergency mode to ${mode.name} (${customDurationHours}h): $reason",
            errorCode = "EMERGENCY-STATUS-${mode.name}",
            mitigation = "Emergency mode synced via MaintenanceService Realtime Database.",
            status = "Resolved"
        )
    }

    fun adminUpdateCustomDurationHours(hours: Int) {
        maintenanceService.updateCustomDurationHours(hours)
    }

    fun adminUpdateDisabledFeatures(disabledIds: Set<String>) {
        maintenanceService.updateDisabledFeatures(disabledIds)
        recordClientSecurityEvent(
            category = "SYSTEM_CONFIGURATION",
            severity = "MEDIUM",
            endpoint = "/admin/emergency/features",
            reason = "Admin (Rizwan RR) updated selective feature restrictions (${disabledIds.size} restricted features).",
            errorCode = "EMERGENCY-FEATURES-UPDATED",
            mitigation = "Updated selective feature access restrictions in Realtime Database.",
            status = "Resolved"
        )
    }

    fun adminToggleFeatureRestriction(featureId: String) {
        val current = emergencySystemState.value.disabledFeatureIds.toMutableSet()
        val cleanId = featureId.trim().lowercase()
        if (current.contains(cleanId)) {
            current.remove(cleanId)
        } else {
            current.add(cleanId)
        }
        adminUpdateDisabledFeatures(current)
    }

    fun adminCreateOrUpdateFamilyMember(
        id: String,
        password: String,
        memberName: String,
        details: String
    ) {
        val account = maintenanceService.createOrUpdateFamilyMember(id, password, memberName, details)
        viewModelScope.launch {
            repository.registerOrUpdateUser(
                userId = "rrr_family_${account.id.lowercase()}",
                name = account.memberName,
                role = UserRole.RRR_FAMILY.name
            )
        }
    }

    fun adminToggleFamilyMemberActive(id: String) {
        maintenanceService.toggleFamilyMemberActive(id)
    }

    fun adminDeleteFamilyMember(id: String) {
        maintenanceService.deleteFamilyMember(id)
    }

    fun verifyAndAuthenticateFamilyMember(id: String, password: String): FamilyMemberAccount? {
        return maintenanceService.authenticateFamilyMember(id, password)
    }

    fun checkEmergencyBeforeAction(targetId: String, targetName: String): Boolean {
        val state = emergencySystemState.value
        when (state.activeMode) {
            EmergencyMode.NORMAL -> return true
            EmergencyMode.MAINTENANCE_FREEZE -> {
                showEmergencyAlert(
                    title = "System Under Maintenance",
                    message = "This application is currently under maintenance. All application services are temporarily paused. Please try again shortly."
                )
                return false
            }
            EmergencyMode.SELECTIVE_FEATURE_CONTROL -> {
                if (state.isAppOrFeatureDisabled(targetId)) {
                    showEmergencyAlert(
                        title = "Specific Maintenance Active",
                        message = "'$targetName' is currently undergoing scheduled maintenance for ${state.customDurationHours} hour(s). Other modules remain operational."
                    )
                    return false
                }
                return true
            }
            EmergencyMode.HIGH_SECURITY_LOCKDOWN -> {
                // Mode 3 applies ONLY to Standard Users and RRR Family, NOT CEO Panel
                if (_currentUserRole.value == UserRole.CEO_PANEL) {
                    return true
                }
                showEmergencyAlert(
                    title = "Service Temporarily Unavailable",
                    message = "You cannot use this application right now. Please try again after some time."
                )
                return false
            }
        }
    }

    // --- REXLIFY Music Module State (starts completely empty per specification) ---
    private val _musicPlaylist = MutableStateFlow<List<MusicTrack>>(emptyList())
    val musicPlaylist: StateFlow<List<MusicTrack>> = _musicPlaylist.asStateFlow()

    private val _currentTrackIndex = MutableStateFlow(0)
    val currentTrackIndex = _currentTrackIndex.asStateFlow()

    private val _isMusicPlaying = MutableStateFlow(false)
    val isMusicPlaying = _isMusicPlaying.asStateFlow()

    private val _playbackProgressSec = MutableStateFlow(0)
    val playbackProgressSec = _playbackProgressSec.asStateFlow()

    private val _musicPlaybackError = MutableStateFlow<String?>(null)
    val musicPlaybackError = _musicPlaybackError.asStateFlow()

    private val _isMusicRepeat = MutableStateFlow(0) // 0 = OFF, 1 = REPEAT_ALL, 2 = REPEAT_ONE
    val isMusicRepeat = _isMusicRepeat.asStateFlow()

    private val _isMusicShuffle = MutableStateFlow(false)
    val isMusicShuffle = _isMusicShuffle.asStateFlow()

    private val _musicVolume = MutableStateFlow(0.85f)
    val musicVolume = _musicVolume.asStateFlow()

    private var mediaPlayer: android.media.MediaPlayer? = null
    private var musicPlaybackJob: Job? = null

    // Filtered apps based on search & category
    val filteredApps: StateFlow<List<AppItem>> = combine(
        allApps,
        searchQuery,
        selectedCategory
    ) { apps, query, category ->
        apps.filter { app ->
            val matchesCategory = category == AppCategory.ALL || app.category == category
            val matchesQuery = SearchHelper.matchesQuery(app, query)
            matchesCategory && matchesQuery
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val tabHistory = mutableListOf<NavigationTab>(NavigationTab.HOME)

    init {
        viewModelScope.launch {
            repository.ensureDefaultAppsSeeded()
            repository.ensureSecurityEventsSeeded()
        }

        // Restore persistent user preferences
        try {
            val savedClock = sessionManager.getSavedClockStyle()
            _dashboardClockStyle.value = DashboardClockStyle.valueOf(savedClock)
        } catch (_: Exception) {}

        try {
            val savedTheme = sessionManager.getSavedThemeMode()
            _themeMode.value = MayaThemeMode.valueOf(savedTheme)
        } catch (_: Exception) {}

        try {
            val savedWidgetNames = sessionManager.getSavedHomeWidgets()
            _activeHomeWidgets.value = savedWidgetNames.mapNotNull {
                try { com.example.ui.components.HomeWidgetType.valueOf(it) } catch (_: Exception) { null }
            }.ifEmpty {
                listOf(
                    com.example.ui.components.HomeWidgetType.WEATHER,
                    com.example.ui.components.HomeWidgetType.CALENDAR,
                    com.example.ui.components.HomeWidgetType.BATTERY_STATUS
                )
            }
        } catch (_: Exception) {}

        try {
            _mayaOsWallpaper.value = sessionManager.getSavedMayaOsWallpaper()
        } catch (_: Exception) {}

        _hapticEnabled.value = sessionManager.getSavedHapticEnabled()
        _reduceMotion.value = sessionManager.getSavedReduceMotion()
        _notificationsEnabled.value = sessionManager.getSavedNotificationsEnabled()

        // Initialize REXLIFY Music default tracks matching the reference UI with distinct artwork per track
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val introFile = RexlifyIntroAudioGenerator.getOrCreateIntroTrackFile(getApplication())
                val audioUri = android.net.Uri.fromFile(introFile).toString()
                val defaultTracks = listOf(
                    MusicTrack(
                        id = "rexlify_track_skin",
                        title = "Skin",
                        artist = "Flume",
                        durationSec = 203, // 3:23
                        uriString = audioUri,
                        albumArtGradient = listOf(0xFFE0B0FF, 0xFF7B68EE, 0xFF4B0082),
                        description = "Flume • Skin",
                        artworkStyleId = "skin",
                        isFavorite = false
                    ),
                    MusicTrack(
                        id = "rexlify_track_odyssey",
                        title = "Odyssey",
                        artist = "Roary",
                        durationSec = 176, // 2:56
                        uriString = audioUri,
                        albumArtGradient = listOf(0xFFD1D5DB, 0xFF9CA3AF, 0xFF4B5563),
                        description = "Roary • Odyssey",
                        artworkStyleId = "odyssey",
                        isFavorite = false
                    ),
                    MusicTrack(
                        id = "rexlify_track_beautiful_now",
                        title = "Beautiful Now",
                        artist = "Zedd & Marshmello",
                        durationSec = 203, // 3:23
                        uriString = audioUri,
                        albumArtGradient = listOf(0xFFEC4899, 0xFF06B6D4, 0xFFF59E0B),
                        description = "Zedd & Marshmello • Beautiful Now",
                        artworkStyleId = "beautiful_now",
                        isFavorite = true
                    ),
                    MusicTrack(
                        id = "rexlify_track_towards_home",
                        title = "Towards Home",
                        artist = "Shawn Williams",
                        durationSec = 232, // 3:52
                        uriString = audioUri,
                        albumArtGradient = listOf(0xFF38BDF8, 0xFFFDE68A, 0xFF78350F),
                        description = "Shawn Williams • Towards Home",
                        artworkStyleId = "towards_home",
                        isFavorite = false
                    ),
                    MusicTrack(
                        id = "rexlify_track_loyal",
                        title = "Loyal",
                        artist = "Chris Brown",
                        durationSec = 222, // 3:42
                        uriString = audioUri,
                        albumArtGradient = listOf(0xFFF43F5E, 0xFFA855F7, 0xFF06B6D4),
                        description = "Chris Brown • Loyal",
                        artworkStyleId = "loyal",
                        isFavorite = false
                    ),
                    MusicTrack(
                        id = "rexlify_track_icon",
                        title = "Icon",
                        artist = "Roary",
                        durationSec = 174, // 2:54
                        uriString = audioUri,
                        albumArtGradient = listOf(0xFFE5E7EB, 0xFF9CA3AF, 0xFF1F2937),
                        description = "Roary • Icon",
                        artworkStyleId = "icon",
                        isFavorite = false
                    ),
                    MusicTrack(
                        id = "rexlify_track_first_day",
                        title = "The First Day",
                        artist = "Lights & Motion",
                        durationSec = 163, // 2:43
                        uriString = audioUri,
                        albumArtGradient = listOf(0xFF22D3EE, 0xFF0284C7, 0xFF0F172A),
                        description = "Lights & Motion • The First Day",
                        artworkStyleId = "first_day",
                        isFavorite = false
                    ),
                    MusicTrack(
                        id = "rexlify_track_starfall",
                        title = "Starfall",
                        artist = "Roary",
                        durationSec = 208, // 3:28
                        uriString = audioUri,
                        albumArtGradient = listOf(0xFFE879F9, 0xFF7E22CE, 0xFF1E1B4B),
                        description = "Roary • Starfall",
                        artworkStyleId = "starfall",
                        isFavorite = false
                    )
                )
                if (_musicPlaylist.value.isEmpty()) {
                    _musicPlaylist.value = defaultTracks
                }
            } catch (_: Exception) {}

            // Load any saved custom wallpapers from app internal storage
            try {
                val wpDir = java.io.File(getApplication<Application>().filesDir, "wallpapers")
                if (wpDir.exists()) {
                    val files = wpDir.listFiles()?.filter { it.isFile }?.map { it.absolutePath } ?: emptyList()
                    _customWallpapers.value = files
                }
            } catch (_: Exception) {}
        }
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun onCategorySelected(category: AppCategory) {
        _selectedCategory.value = category
    }

    fun onTabSelected(tab: NavigationTab) {
        if (_currentTab.value != tab) {
            _currentTab.value = tab
            if (tabHistory.lastOrNull() != tab) {
                tabHistory.add(tab)
            }
        }
    }

    fun setThemeMode(mode: MayaThemeMode) {
        _themeMode.value = mode
        sessionManager.saveThemeMode(mode.name)
    }

    fun toggleHaptics(enabled: Boolean) {
        _hapticEnabled.value = enabled
        sessionManager.saveHapticEnabled(enabled)
    }

    fun toggleReduceMotion(enabled: Boolean) {
        _reduceMotion.value = enabled
        sessionManager.saveReduceMotion(enabled)
    }

    fun toggleNotifications(enabled: Boolean) {
        _notificationsEnabled.value = enabled
        sessionManager.saveNotificationsEnabled(enabled)
    }

    fun updateNotes(notes: String) {
        _osNotes.value = notes
    }

    // Maya OS
    fun openMayaOs() {
        _isMayaOsActive.value = true
    }

    fun closeMayaOs() {
        _isMayaOsActive.value = false
    }

    fun updateUserName(name: String) {
        if (name.isNotBlank()) {
            val clean = name.trim()
            _userName.value = clean
            sessionManager.updateUserName(clean)
            _userSession.value = sessionManager.getCurrentSession()
        }
    }

    // --- Authentication Actions ---
    fun loginStandard(name: String) {
        val session = sessionManager.loginStandard(name)
        _userSession.value = session
        _isUserLoggedIn.value = true
        _currentUserRole.value = UserRole.STANDARD
        _userName.value = session.userName
        _isAdminAuthenticated.value = false
        viewModelScope.launch {
            repository.clearAllTasks()
            repository.clearAllNotes()
            repository.registerOrUpdateUser(session.userId, session.userName, UserRole.STANDARD.name)
            // Automated welcome notifications for standard/guest users (2 notifications)
            repository.sendBroadcastAnnouncement(
                title = "Welcome to Maya Connect!",
                message = "Welcome, ${session.userName}! Maya Connect by REXLIFY offers fast access to our ecosystem. Explore apps, notes, tasks, and connect with our services."
            )
            repository.sendBroadcastAnnouncement(
                title = "Upgrade to REXLIFY Family",
                message = "Unlock full unrestricted access to premium internal tools by joining the RRR Family. Contact support or enter Family credentials."
            )
        }
    }

    fun loginFamily(name: String = "RRR Family Member") {
        val session = sessionManager.loginFamily(name)
        _userSession.value = session
        _isUserLoggedIn.value = true
        _currentUserRole.value = UserRole.RRR_FAMILY
        _userName.value = session.userName
        _isAdminAuthenticated.value = false
        viewModelScope.launch {
            repository.registerOrUpdateUser(session.userId, session.userName, UserRole.RRR_FAMILY.name)
            // Automated welcome notification for family members (1 notification)
            repository.sendBroadcastAnnouncement(
                title = "Welcome to RRR Family!",
                message = "Welcome, ${session.userName}! You have received Full Premium Access directly from REXLIFY. All ecosystem apps, advanced features, and developer utilities are now unlocked."
            )
        }
    }

    fun loginCeo(name: String = "Rizwan RR") {
        val session = sessionManager.loginCeo(name)
        _userSession.value = session
        _isUserLoggedIn.value = true
        _currentUserRole.value = UserRole.CEO_PANEL
        _userName.value = session.userName
        _isAdminAuthenticated.value = true
        viewModelScope.launch {
            repository.registerOrUpdateUser(session.userId, session.userName, UserRole.CEO_PANEL.name)
            // Automated welcome notification for CEO (1 notification)
            repository.sendBroadcastAnnouncement(
                title = "Executive Clearance Enabled",
                message = "Welcome CEO Rizwan RR. Administrator and Founder executive access enabled across Maya Connect, Ecosystem databases, and User Management."
            )
        }
    }

    // CEO / Admin User Moderation & Status Control Functions
    fun adminSetUserStatus(userId: String, userName: String, status: String, reason: String) {
        viewModelScope.launch {
            repository.adminSetUserStatus(userId, userName, status, reason, "Rizwan RR")
            repository.sendBroadcastAnnouncement(
                title = "Account Status Alert for $userName",
                message = "Account status updated to $status by Administrator. Reason: $reason"
            )
        }
    }

    fun submitDataDeletionRequest(reason: String) {
        val session = _userSession.value
        val currentUserId = session?.userId ?: "usr_guest"
        val currentUserName = session?.userName ?: _userName.value
        viewModelScope.launch {
            repository.submitDataDeletionRequest(currentUserId, currentUserName, reason)
            repository.sendBroadcastAnnouncement(
                title = "Data Deletion Request Filed",
                message = "Your account data deletion request has been submitted to REXLIFY Data Protection Officer. Request ID will be processed shortly."
            )
        }
    }

    fun adminApproveDeletionRequest(requestId: String) {
        viewModelScope.launch {
            repository.adminApproveDeletionRequest(requestId, "Rizwan RR")
            repository.sendBroadcastAnnouncement(
                title = "Data Deletion Request Approved",
                message = "Deletion request $requestId has been approved and user data purged."
            )
        }
    }

    fun adminRejectDeletionRequest(requestId: String, rejectionReason: String) {
        viewModelScope.launch {
            repository.adminRejectDeletionRequest(requestId, rejectionReason, "Rizwan RR")
            repository.sendBroadcastAnnouncement(
                title = "Data Deletion Request Status",
                message = "Deletion request $requestId was reviewed: $rejectionReason"
            )
        }
    }

    fun adminIssueWarning(userId: String, userName: String, reason: String = "Policy Compliance Notice") {
        viewModelScope.launch {
            repository.issueWarningToUser(userId)
            repository.sendBroadcastAnnouncement(
                title = "Notice for $userName",
                message = "Official Warning issued: $reason. Please adhere to REXLIFY Ecosystem community guidelines."
            )
        }
    }

    fun adminToggleSuspend(userId: String, currentStatus: String, userName: String) {
        viewModelScope.launch {
            val newStatus = if (currentStatus.equals("SUSPENDED", ignoreCase = true)) "ACTIVE" else "SUSPENDED"
            repository.adminSetUserStatus(userId, userName, newStatus, "Status toggled by Admin", "Rizwan RR")
        }
    }

    fun adminUpgradeUserRole(userId: String, newRole: String, userName: String) {
        viewModelScope.launch {
            val hasRbac = _isAdminAuthenticated.value ||
                SecurityHardeningEngine.hasPermission(_currentUserRole.value, SecurityPermission.MANAGE_USERS_AND_ROLES)
            if (!hasRbac) {
                recordClientSecurityEvent(
                    category = "PRIVILEGE_ESCALATION",
                    severity = "CRITICAL",
                    endpoint = "/admin/users/role-mutation",
                    reason = "Unauthorized attempt to change user '$userId' role to '$newRole' without MANAGE_USERS_AND_ROLES permission.",
                    errorCode = "RBAC-403-ROLE-MUTATION",
                    mitigation = "Blocked role mutation request and logged security incident."
                )
                return@launch
            }
            repository.updateUserRole(userId, newRole)
            recordClientSecurityEvent(
                category = "ADMIN_SECURITY_AUDIT",
                severity = "LOW",
                endpoint = "/admin/users/role-mutation",
                reason = "Authorized administrator updated role for user '$userName' ($userId) to '$newRole'.",
                errorCode = "RBAC-200-ROLE-UPDATED",
                mitigation = "Audit log recorded in Room & Supabase security_events."
            )
            repository.sendBroadcastAnnouncement(
                title = "User Access Role Upgraded",
                message = "User $userName has been granted $newRole access privileges."
            )
        }
    }

    fun adminSendMessageToUser(userId: String, userName: String, title: String, message: String) {
        viewModelScope.launch {
            repository.sendBroadcastAnnouncement(
                title = "To $userName: $title",
                message = message
            )
        }
    }

    fun adminDeleteUser(userId: String) {
        viewModelScope.launch {
            val hasRbac = _isAdminAuthenticated.value ||
                SecurityHardeningEngine.hasPermission(_currentUserRole.value, SecurityPermission.MANAGE_USERS_AND_ROLES)
            if (!hasRbac) {
                recordClientSecurityEvent(
                    category = "UNAUTHORIZED_ACCESS",
                    severity = "HIGH",
                    endpoint = "/admin/users/delete",
                    reason = "Unauthorized attempt to delete user '$userId' without MANAGE_USERS_AND_ROLES permission.",
                    errorCode = "RBAC-403-USER-DELETE",
                    mitigation = "Request rejected by RBAC guard."
                )
                return@launch
            }
            repository.deleteUser(userId)
        }
    }

    fun logout() {
        sessionManager.logout()
        _userSession.value = null
        _isUserLoggedIn.value = false
        _currentUserRole.value = UserRole.STANDARD
        _isAdminAuthenticated.value = false
        _activeAppForLaunch.value = null
        _unauthorizedAppDialog.value = null
    }

    fun getRemainingLockoutMillis(): Long = sessionManager.getRemainingLockoutMillis()

    fun recordFailedAttempt(): Long {
        val lockoutMs = sessionManager.recordFailedAttempt()
        val severity = if (lockoutMs >= 60_000L) "HIGH" else "MEDIUM"
        recordClientSecurityEvent(
            category = "BRUTE_FORCE_AUTH",
            severity = severity,
            endpoint = "/auth/privileged-portal-login",
            reason = "Failed authentication attempt on Privileged Login Portal (progressive cooldown = ${lockoutMs / 1000}s).",
            errorCode = if (lockoutMs > 0L) "AUTH-429-COOLDOWN" else "AUTH-401-INVALID-CREDENTIAL",
            mitigation = if (lockoutMs > 0L) "Activated progressive brute-force cooldown (${lockoutMs / 1000}s)." else "Recorded failed attempt counter."
        )
        return lockoutMs
    }

    fun resetFailedAttempts() = sessionManager.resetFailedAttempts()

    fun dismissUnauthorizedAppDialog() {
        _unauthorizedAppDialog.value = null
    }

    fun openCustomerSupportFromUnauthorizedDialog() {
        _unauthorizedAppDialog.value = null
        openCustomerSupport()
    }

    fun setMayaOsWallpaper(wallpaper: String) {
        _mayaOsWallpaper.value = wallpaper
        sessionManager.saveMayaOsWallpaper(wallpaper)
    }

    fun setDashboardClockStyle(style: DashboardClockStyle) {
        _dashboardClockStyle.value = style
        sessionManager.saveClockStyle(style.name)
    }

    fun setHomeWidgets(widgets: List<com.example.ui.components.HomeWidgetType>) {
        _activeHomeWidgets.value = widgets
        sessionManager.saveHomeWidgets(widgets.map { it.name })
    }

    // --- Gesture Navigation ---
    private val _isGestureNavEnabled = MutableStateFlow(true)
    val isGestureNavEnabled: StateFlow<Boolean> = _isGestureNavEnabled.asStateFlow()

    fun setGestureNavEnabled(enabled: Boolean) {
        _isGestureNavEnabled.value = enabled
    }

    fun navigateBack() {
        if (_isNotificationShadeOpen.value) { _isNotificationShadeOpen.value = false; return }
        if (_showAppDrawer.value) { _showAppDrawer.value = false; return }
        if (_showNotificationCenter.value) { _showNotificationCenter.value = false; return }
        if (_showAllAppsLauncher.value) { _showAllAppsLauncher.value = false; return }
        if (_showMusicPlayer.value) { _showMusicPlayer.value = false; return }
        if (_showNotepad.value) { _showNotepad.value = false; return }
        if (_showGoogleTasks.value) { _showGoogleTasks.value = false; return }
        if (_showEcosystemDirectory.value) { _showEcosystemDirectory.value = false; return }
        if (_showCustomerSupport.value) { _showCustomerSupport.value = false; return }
        if (_showOfficialProfile.value) { _showOfficialProfile.value = false; return }
        if (_showAdminPanel.value) { _showAdminPanel.value = false; return }
        if (_showTermsAndConditions.value) { _showTermsAndConditions.value = false; return }
        if (_showPrivacyPolicy.value) { _showPrivacyPolicy.value = false; return }
        if (_showDiagnostics.value) { _showDiagnostics.value = false; return }
        if (_showChatAssistant.value) { _showChatAssistant.value = false; return }
        if (_showVoiceAssistant.value) { _showVoiceAssistant.value = false; return }
        if (_activeAppForLaunch.value != null) { _activeAppForLaunch.value = null; return }
        if (_isMayaOsActive.value) { _isMayaOsActive.value = false; return }

        // Android System Navigation Tab Backstack Logic
        if (tabHistory.size > 1) {
            tabHistory.removeAt(tabHistory.size - 1)
            val previousTab = tabHistory.last()
            _currentTab.value = previousTab
        } else if (_currentTab.value != NavigationTab.HOME) {
            _currentTab.value = NavigationTab.HOME
            tabHistory.clear()
            tabHistory.add(NavigationTab.HOME)
        }
    }

    fun navigateToHome() {
        _isNotificationShadeOpen.value = false
        _showAppDrawer.value = false
        _showNotificationCenter.value = false
        _showAllAppsLauncher.value = false
        _showMusicPlayer.value = false
        _showNotepad.value = false
        _showEcosystemDirectory.value = false
        _showCustomerSupport.value = false
        _showOfficialProfile.value = false
        _showAdminPanel.value = false
        _showTermsAndConditions.value = false
        _showPrivacyPolicy.value = false
        _showChatAssistant.value = false
        _showVoiceAssistant.value = false
        _activeAppForLaunch.value = null
        _currentTab.value = NavigationTab.HOME
    }

    fun navigateAdjacentTab(forward: Boolean) {
        val tabs = NavigationTab.entries
        val currentIndex = tabs.indexOf(_currentTab.value)
        val nextIndex = if (forward) {
            (currentIndex + 1).coerceAtMost(tabs.size - 1)
        } else {
            (currentIndex - 1).coerceAtLeast(0)
        }
        _currentTab.value = tabs[nextIndex]
    }

    fun openAppDrawer() {
        _showAppDrawer.value = true
    }

    fun closeAppDrawer() {
        _showAppDrawer.value = false
    }

    fun toggleAppDrawer() {
        _showAppDrawer.value = !_showAppDrawer.value
    }

    fun setCustomWallpaper(uriOrPath: String) {
        _customWallpaperUri.value = uriOrPath
    }

    fun clearCustomWallpaper() {
        _customWallpaperUri.value = null
    }

    fun addCustomWallpaperFromUri(sourceUri: android.net.Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val app = getApplication<Application>()
                val wpDir = java.io.File(app.filesDir, "wallpapers")
                if (!wpDir.exists()) {
                    wpDir.mkdirs()
                }
                val destFile = java.io.File(wpDir, "custom_wallpaper_${System.currentTimeMillis()}.jpg")
                app.contentResolver.openInputStream(sourceUri)?.use { input ->
                    java.io.FileOutputStream(destFile).use { output ->
                        input.copyTo(output)
                    }
                }
                val path = destFile.absolutePath
                val current = _customWallpapers.value.toMutableList()
                if (!current.contains(path)) {
                    current.add(0, path)
                    _customWallpapers.value = current
                }
                _customWallpaperUri.value = path
            } catch (_: Exception) {}
        }
    }

    fun deleteCustomWallpaper(path: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val file = java.io.File(path)
                if (file.exists()) {
                    file.delete()
                }
                val current = _customWallpapers.value.filter { it != path }
                _customWallpapers.value = current
                if (_customWallpaperUri.value == path) {
                    _customWallpaperUri.value = null
                }
            } catch (_: Exception) {}
        }
    }

    fun setMayaOsGridSize(size: Int) {
        _mayaOsGridSize.value = size
    }

    // Notification Shade Controls
    fun openNotificationShade() {
        _isNotificationShadeOpen.value = true
    }

    fun closeNotificationShade() {
        _isNotificationShadeOpen.value = false
    }

    fun toggleNotificationShade() {
        _isNotificationShadeOpen.value = !_isNotificationShadeOpen.value
    }

    // Home Screen Folder Management (Drag & Drop Auto-Naming)
    fun createFolder(app1: AppItem, app2: AppItem): AppFolder {
        val currentFolders = _homeFolders.value.toMutableList()
        val autoName = AppFolder.generateFolderName(listOf(app1, app2))
        val newFolder = AppFolder(
            name = autoName,
            appIds = listOf(app1.id, app2.id),
            colorHex = app1.primaryColorHex
        )
        currentFolders.add(0, newFolder)
        _homeFolders.value = currentFolders
        return newFolder
    }

    fun addAppToFolder(folderId: String, app: AppItem) {
        val currentFolders = _homeFolders.value.toMutableList()
        val index = currentFolders.indexOfFirst { it.id == folderId }
        if (index != -1) {
            val oldFolder = currentFolders[index]
            if (!oldFolder.appIds.contains(app.id)) {
                val updatedAppIds = oldFolder.appIds + app.id
                currentFolders[index] = oldFolder.copy(appIds = updatedAppIds)
                _homeFolders.value = currentFolders
            }
        }
    }

    fun removeAppFromFolder(folderId: String, appId: String) {
        val currentFolders = _homeFolders.value.toMutableList()
        val index = currentFolders.indexOfFirst { it.id == folderId }
        if (index != -1) {
            val oldFolder = currentFolders[index]
            val updatedAppIds = oldFolder.appIds.filter { it != appId }
            if (updatedAppIds.size <= 1) {
                currentFolders.removeAt(index)
            } else {
                currentFolders[index] = oldFolder.copy(appIds = updatedAppIds)
            }
            _homeFolders.value = currentFolders
        }
    }

    fun renameFolder(folderId: String, newName: String) {
        val currentFolders = _homeFolders.value.toMutableList()
        val index = currentFolders.indexOfFirst { it.id == folderId }
        if (index != -1 && newName.isNotBlank()) {
            currentFolders[index] = currentFolders[index].copy(name = newName.trim(), isAutoNamed = false)
            _homeFolders.value = currentFolders
        }
    }

    fun dissolveFolder(folderId: String) {
        val currentFolders = _homeFolders.value.filter { it.id != folderId }
        _homeFolders.value = currentFolders
    }

    fun launchAppFromMayaOs(app: AppItem) {
        if (app.id == "maya_connect") {
            closeMayaOs()
            return
        }
        if (!checkEmergencyBeforeAction(app.id, app.name)) {
            return
        }
        if (_currentUserRole.value == UserRole.STANDARD &&
            !AuthCredentialsConfig.isAppAuthorizedForRole(app.id, UserRole.STANDARD)
        ) {
            recordClientSecurityEvent(
                category = "UNAUTHORIZED_ACCESS",
                severity = "MEDIUM",
                endpoint = "/maya-os/launch/${app.id}",
                reason = "Standard user attempted to launch restricted application '${app.name}' (${app.id}).",
                errorCode = "RBAC-403-APP-RESTRICTED",
                mitigation = "Blocked launch and displayed UnauthorizedAppDialog."
            )
            _unauthorizedAppDialog.value = app
            return
        }
        if (app.id == "google_tasks" || app.id == "daily_task") {
            openGoogleTasks()
            return
        }
        viewModelScope.launch {
            repository.recordAppLaunch(app.id)
            val currentRecents = _mayaOsRecentApps.value.filter { it.id != app.id }
            _mayaOsRecentApps.value = (listOf(app) + currentRecents).take(10)
            _activeAppForLaunch.value = app
        }
    }

    fun launchApp(app: AppItem) {
        if (!checkEmergencyBeforeAction(app.id, app.name)) {
            return
        }
        if (_currentUserRole.value == UserRole.STANDARD &&
            !AuthCredentialsConfig.isAppAuthorizedForRole(app.id, UserRole.STANDARD)
        ) {
            recordClientSecurityEvent(
                category = "UNAUTHORIZED_ACCESS",
                severity = "MEDIUM",
                endpoint = "/launcher/app/${app.id}",
                reason = "Standard user attempted to open restricted application '${app.name}' (${app.id}).",
                errorCode = "RBAC-403-APP-RESTRICTED",
                mitigation = "Blocked launch and displayed UnauthorizedAppDialog."
            )
            _unauthorizedAppDialog.value = app
            return
        }
        if (app.id == "maya_os") {
            openMayaOs()
            return
        }
        if (app.id == "rexlify_music") {
            openMusicPlayer()
            return
        }
        if (app.id == "notepad") {
            openNotepad()
            return
        }
        if (app.id == "google_tasks" || app.id == "daily_task") {
            openGoogleTasks()
            return
        }
        if (app.id == "ms_edge") {
            try {
                val pm = getApplication<Application>().packageManager
                val intent = pm.getLaunchIntentForPackage(com.example.data.config.CentralAppConfig.EDGE_BROWSER_PACKAGE)
                if (intent != null) {
                    intent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                    getApplication<Application>().startActivity(intent)
                    viewModelScope.launch { repository.recordAppLaunch(app.id) }
                    return
                }
            } catch (_: Exception) {}
        }
        viewModelScope.launch {
            repository.recordAppLaunch(app.id)
            val currentRecents = _mayaOsRecentApps.value.filter { it.id != app.id }
            _mayaOsRecentApps.value = (listOf(app) + currentRecents).take(10)
            _activeAppForLaunch.value = app
        }
    }

    fun closeLaunchedApp() {
        _activeAppForLaunch.value = null
    }

    fun selectAppForAction(app: AppItem?) {
        _selectedAppForAction.value = app
    }

    fun selectAppForDetails(app: AppItem?) {
        _selectedAppForDetails.value = app
    }

    fun selectAppForEdit(app: AppItem?) {
        _selectedAppForEdit.value = app
    }

    // Assistant toggles
    fun openChatAssistant(initialQuery: String = "") {
        if (!checkEmergencyBeforeAction("chat_assistant", "Chat Assistant")) return
        _chatAssistantInitialQuery.value = initialQuery
        _showChatAssistant.value = true
    }

    fun closeChatAssistant() {
        _showChatAssistant.value = false
        _chatAssistantInitialQuery.value = ""
    }

    fun openVoiceAssistant() {
        if (!checkEmergencyBeforeAction("chat_assistant", "Sara Voice Assistant")) return
        _showVoiceAssistant.value = true
    }

    fun closeVoiceAssistant() {
        _showVoiceAssistant.value = false
    }

    fun openEcosystemLanding() {
        if (!checkEmergencyBeforeAction("ecosystem_directory", "Ecosystem Directory")) return
        _showEcosystemLanding.value = true
    }

    fun closeEcosystemLanding() {
        _showEcosystemLanding.value = false
    }

    fun setShowAddAppDialog(show: Boolean) {
        _showAddAppDialog.value = show
    }

    // Notification Center
    fun openNotificationCenter() {
        _showNotificationCenter.value = true
    }

    fun closeNotificationCenter() {
        _showNotificationCenter.value = false
    }

    fun markNotificationAsRead(id: String) {
        viewModelScope.launch {
            repository.markNotificationAsRead(id)
        }
    }

    fun markAllNotificationsAsRead() {
        viewModelScope.launch {
            repository.markAllNotificationsAsRead()
        }
    }

    fun deleteNotification(id: String) {
        viewModelScope.launch {
            repository.deleteNotification(id)
        }
    }

    // All Apps Launcher
    fun openAllAppsLauncher() {
        if (!checkEmergencyBeforeAction("app_launcher", "All Apps Launcher")) return
        _showAllAppsLauncher.value = true
    }

    fun closeAllAppsLauncher() {
        _showAllAppsLauncher.value = false
    }

    // Music Player Controls
    fun openMusicPlayer() {
        if (!checkEmergencyBeforeAction("rexlify_music", "Rexlify Music Player")) return
        _showMusicPlayer.value = true
    }

    fun closeMusicPlayer() {
        _showMusicPlayer.value = false
    }

    fun toggleMusicPlayPause() {
        if (_isMusicPlaying.value) {
            pauseMusic()
        } else {
            playMusic()
        }
    }

    fun importAudioUris(uris: List<android.net.Uri>) {
        if (uris.isEmpty()) return
        val app = getApplication<Application>()
        val contentResolver = app.contentResolver
        val currentList = _musicPlaylist.value.toMutableList()
        val retriever = android.media.MediaMetadataRetriever()
        val newTracks = mutableListOf<MusicTrack>()

        val customArtStyles = listOf(
            "imported_nebula",
            "imported_prism",
            "imported_cyber_wave",
            "imported_golden_horizon",
            "imported_emerald_pulse",
            "imported_crimson_vinyl",
            "imported_arctic_glass",
            "imported_ultraviolet"
        )

        val gradients = listOf(
            listOf(0xFFA855F7, 0xFF6366F1, 0xFF1E1B4B),
            listOf(0xFFEC4899, 0xFF8B5CF6, 0xFF3B0764),
            listOf(0xFF06B6D4, 0xFF3B82F6, 0xFF0F172A),
            listOf(0xFFF59E0B, 0xFFEF4444, 0xFF450A0A),
            listOf(0xFF10B981, 0xFF059669, 0xFF064E3B),
            listOf(0xFFF43F5E, 0xFFD946EF, 0xFF4A044E)
        )

        for (uri in uris) {
            try {
                // Take persistable read permission if supported by provider
                try {
                    contentResolver.takePersistableUriPermission(
                        uri,
                        android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )
                } catch (_: Exception) {}

                // 1. Extract exact original display name from ContentResolver so the song name is NEVER altered
                var exactFileName: String? = null
                try {
                    contentResolver.query(
                        uri,
                        arrayOf(android.provider.OpenableColumns.DISPLAY_NAME),
                        null,
                        null,
                        null
                    )?.use { cursor ->
                        if (cursor.moveToFirst()) {
                            val nameIdx = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                            if (nameIdx >= 0) {
                                exactFileName = cursor.getString(nameIdx)
                            }
                        }
                    }
                } catch (_: Exception) {}

                // Strip only known audio file extension while keeping the exact song name intact
                val cleanedDisplayName = exactFileName?.let { raw ->
                    val dotIndex = raw.lastIndexOf('.')
                    if (dotIndex > 0) {
                        val ext = raw.substring(dotIndex + 1).lowercase()
                        if (ext in setOf("mp3", "wav", "flac", "m4a", "aac", "ogg", "opus", "wma", "mp4")) {
                            raw.substring(0, dotIndex)
                        } else {
                            raw
                        }
                    } else {
                        raw
                    }
                }?.trim()

                var metadataTitle: String? = null
                var artist: String? = null
                var durSec = 200
                var embeddedArt: ByteArray? = null

                try {
                    retriever.setDataSource(app, uri)
                    metadataTitle = retriever.extractMetadata(android.media.MediaMetadataRetriever.METADATA_KEY_TITLE)
                    artist = retriever.extractMetadata(android.media.MediaMetadataRetriever.METADATA_KEY_ARTIST)
                    val durStr = retriever.extractMetadata(android.media.MediaMetadataRetriever.METADATA_KEY_DURATION)
                    val durMs = durStr?.toLongOrNull() ?: 0L
                    if (durMs > 0) {
                        durSec = (durMs / 1000).toInt().coerceAtLeast(1)
                    }
                    embeddedArt = retriever.embeddedPicture
                } catch (_: Exception) {}

                // Prioritize exact file display name from user's device, then metadata title, then last path segment
                val finalTitle = when {
                    !cleanedDisplayName.isNullOrBlank() -> cleanedDisplayName
                    !metadataTitle.isNullOrBlank() -> metadataTitle.trim()
                    else -> uri.lastPathSegment
                        ?.substringAfterLast('/')
                        ?.substringBeforeLast('.')
                        ?.takeIf { it.isNotBlank() }
                        ?: "Track ${currentList.size + newTracks.size + 1}"
                }

                val finalArtist = if (!artist.isNullOrBlank()) artist.trim() else "Local Artist"

                val totalIdx = currentList.size + newTracks.size
                val gradient = gradients[totalIdx % gradients.size]
                val artStyle = customArtStyles[totalIdx % customArtStyles.size]

                newTracks.add(
                    MusicTrack(
                        id = java.util.UUID.randomUUID().toString(),
                        title = finalTitle,
                        artist = finalArtist,
                        durationSec = durSec,
                        uriString = uri.toString(),
                        albumArtGradient = gradient,
                        description = "Imported from device",
                        artworkStyleId = artStyle,
                        isFavorite = false,
                        embeddedArtBytes = embeddedArt
                    )
                )
            } catch (_: Exception) {}
        }

        try {
            retriever.release()
        } catch (_: Exception) {}

        if (newTracks.isNotEmpty()) {
            val firstNewIndex = currentList.size
            currentList.addAll(newTracks)
            _musicPlaylist.value = currentList
            playTrack(firstNewIndex)
        }
    }

    fun updateTrackDetails(trackId: String, newTitle: String, newArtist: String) {
        val trimmedTitle = newTitle.trim()
        if (trimmedTitle.isEmpty()) return
        val trimmedArtist = newArtist.trim().ifEmpty { "Unknown Artist" }
        _musicPlaylist.value = _musicPlaylist.value.map { track ->
            if (track.id == trackId) {
                track.copy(title = trimmedTitle, artist = trimmedArtist)
            } else {
                track
            }
        }
    }

    fun toggleTrackFavorite(trackId: String) {
        _musicPlaylist.value = _musicPlaylist.value.map { track ->
            if (track.id == trackId) {
                track.copy(isFavorite = !track.isFavorite)
            } else {
                track
            }
        }
    }

    fun playTrack(index: Int) {
        val list = _musicPlaylist.value
        if (list.isEmpty()) return
        val targetIndex = index.coerceIn(0, list.size - 1)
        _currentTrackIndex.value = targetIndex
        _playbackProgressSec.value = 0
        _musicPlaybackError.value = null

        val track = list[targetIndex]
        val isSynthesizedDefault = track.id.startsWith("rexlify_track_")

        try {
            mediaPlayer?.stop()
            mediaPlayer?.release()
        } catch (_: Exception) {}
        mediaPlayer = null

        if (track.uriString != null) {
            try {
                mediaPlayer = android.media.MediaPlayer().apply {
                    setAudioAttributes(
                        android.media.AudioAttributes.Builder()
                            .setContentType(android.media.AudioAttributes.CONTENT_TYPE_MUSIC)
                            .setUsage(android.media.AudioAttributes.USAGE_MEDIA)
                            .build()
                    )
                    setDataSource(getApplication(), android.net.Uri.parse(track.uriString))
                    val vol = _musicVolume.value
                    setVolume(vol, vol)
                    isLooping = isSynthesizedDefault
                    prepareAsync()
                    setOnPreparedListener { mp ->
                        mp.start()
                        _isMusicPlaying.value = true
                        startProgressJob()
                    }
                    setOnCompletionListener {
                        if (!isSynthesizedDefault) {
                            handleTrackFinished()
                        }
                    }
                    setOnErrorListener { _, what, extra ->
                        _musicPlaybackError.value = "Playback error ($what, $extra): Inaccessible audio stream."
                        _isMusicPlaying.value = false
                        true
                    }
                }
                return
            } catch (e: Exception) {
                _musicPlaybackError.value = "Unable to open audio: ${e.message}"
            }
        }

        // Fallback for simulated / ambient playback if uri is direct
        _isMusicPlaying.value = true
        startProgressJob()
    }

    private fun startProgressJob() {
        musicPlaybackJob?.cancel()
        musicPlaybackJob = viewModelScope.launch {
            while (isActive && _isMusicPlaying.value) {
                delay(1000)
                val list = _musicPlaylist.value
                val track = list.getOrNull(_currentTrackIndex.value)
                if (track != null) {
                    val isSynthesizedDefault = track.id.startsWith("rexlify_track_")
                    val currentPos = mediaPlayer?.currentPosition
                    if (!isSynthesizedDefault && currentPos != null && currentPos > 0) {
                        _playbackProgressSec.value = (currentPos / 1000).coerceIn(0, track.durationSec)
                    } else if (_playbackProgressSec.value < track.durationSec) {
                        _playbackProgressSec.value += 1
                    } else {
                        handleTrackFinished()
                    }
                }
            }
        }
    }

    private fun handleTrackFinished() {
        val list = _musicPlaylist.value
        if (list.isEmpty()) return
        when (_isMusicRepeat.value) {
            2 -> { // REPEAT ONE
                playTrack(_currentTrackIndex.value)
            }
            1 -> { // REPEAT ALL
                nextTrack()
            }
            else -> {
                if (_currentTrackIndex.value < list.size - 1) {
                    nextTrack()
                } else {
                    pauseMusic()
                    _playbackProgressSec.value = 0
                }
            }
        }
    }

    fun playMusic() {
        val list = _musicPlaylist.value
        if (list.isEmpty()) return
        val mp = mediaPlayer
        if (mp != null) {
            try {
                mp.start()
                _isMusicPlaying.value = true
                startProgressJob()
                return
            } catch (_: Exception) {}
        }
        playTrack(_currentTrackIndex.value)
    }

    fun pauseMusic() {
        _isMusicPlaying.value = false
        musicPlaybackJob?.cancel()
        try {
            mediaPlayer?.pause()
        } catch (_: Exception) {}
    }

    fun nextTrack() {
        val list = _musicPlaylist.value
        if (list.isEmpty()) return
        val nextIdx = if (_isMusicShuffle.value && list.size > 1) {
            var r = (0 until list.size).random()
            while (r == _currentTrackIndex.value) {
                r = (0 until list.size).random()
            }
            r
        } else {
            (_currentTrackIndex.value + 1) % list.size
        }
        playTrack(nextIdx)
    }

    fun previousTrack() {
        val list = _musicPlaylist.value
        if (list.isEmpty()) return
        val prevIdx = if (_currentTrackIndex.value > 0) _currentTrackIndex.value - 1 else list.size - 1
        playTrack(prevIdx)
    }

    fun seekForward10() {
        val list = _musicPlaylist.value
        val track = list.getOrNull(_currentTrackIndex.value) ?: return
        val newSec = (_playbackProgressSec.value + 10).coerceAtMost(track.durationSec)
        seekMusicProgress(newSec)
    }

    fun seekBackward10() {
        val newSec = (_playbackProgressSec.value - 10).coerceAtLeast(0)
        seekMusicProgress(newSec)
    }

    fun seekMusicProgress(sec: Int) {
        val list = _musicPlaylist.value
        val track = list.getOrNull(_currentTrackIndex.value) ?: return
        val targetSec = sec.coerceIn(0, track.durationSec)
        _playbackProgressSec.value = targetSec
        try {
            if (!track.id.startsWith("rexlify_track_")) {
                mediaPlayer?.seekTo(targetSec * 1000)
            }
        } catch (_: Exception) {}
    }

    fun setMusicVolume(volume: Float) {
        val vol = volume.coerceIn(0f, 1f)
        _musicVolume.value = vol
        try {
            mediaPlayer?.setVolume(vol, vol)
        } catch (_: Exception) {}
    }

    fun toggleMusicRepeat() {
        _isMusicRepeat.value = (_isMusicRepeat.value + 1) % 3
    }

    fun toggleMusicShuffle() {
        _isMusicShuffle.value = !_isMusicShuffle.value
    }

    fun removeTrack(index: Int) {
        val list = _musicPlaylist.value.toMutableList()
        if (index in list.indices) {
            val wasPlaying = index == _currentTrackIndex.value && _isMusicPlaying.value
            list.removeAt(index)
            _musicPlaylist.value = list
            if (list.isEmpty()) {
                pauseMusic()
                _currentTrackIndex.value = 0
                _playbackProgressSec.value = 0
            } else if (index <= _currentTrackIndex.value) {
                _currentTrackIndex.value = (_currentTrackIndex.value - 1).coerceAtLeast(0)
                if (wasPlaying) {
                    playTrack(_currentTrackIndex.value)
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        maintenanceService.cleanup()
        try {
            mediaPlayer?.stop()
            mediaPlayer?.release()
        } catch (_: Exception) {}
        mediaPlayer = null
    }

    // Notepad Actions
    fun openNotepad() {
        if (!checkEmergencyBeforeAction("notepad", "Notepad")) return
        _showNotepad.value = true
    }

    fun closeNotepad() {
        _showNotepad.value = false
    }

    fun createNote(title: String, content: String, colorHex: Long) {
        viewModelScope.launch {
            val titleCheck = SecurityHardeningEngine.validateAndSanitizeInput("note.title", title, maxLength = 200)
            val contentCheck = SecurityHardeningEngine.validateAndSanitizeInput("note.content", content, maxLength = 5000)
            if (!titleCheck.isSafe || !contentCheck.isSafe) {
                val threatDesc = titleCheck.detectionReason ?: contentCheck.detectionReason ?: "Malicious input pattern"
                recordClientSecurityEvent(
                    category = titleCheck.detectedCategory ?: contentCheck.detectedCategory ?: "INJECTION_XSS_SQLI",
                    severity = "HIGH",
                    endpoint = "/notepad/create",
                    reason = threatDesc,
                    errorCode = titleCheck.errorCode ?: contentCheck.errorCode ?: "SEC-INJ-400",
                    mitigation = "Sanitized note content before parameterized Room insert."
                )
            }
            repository.insertNote(titleCheck.sanitizedValue, contentCheck.sanitizedValue, colorHex)
        }
    }

    fun updateNote(note: NoteEntity) {
        viewModelScope.launch {
            val titleCheck = SecurityHardeningEngine.validateAndSanitizeInput("note.title", note.title, maxLength = 200)
            val contentCheck = SecurityHardeningEngine.validateAndSanitizeInput("note.content", note.content, maxLength = 5000)
            repository.updateNote(
                note.copy(
                    title = titleCheck.sanitizedValue,
                    content = contentCheck.sanitizedValue
                )
            )
        }
    }

    fun deleteNote(id: String) {
        viewModelScope.launch {
            repository.deleteNote(id)
        }
    }

    // Our Ecosystem Directory
    fun openEcosystemDirectory() {
        if (!checkEmergencyBeforeAction("ecosystem_directory", "Ecosystem Directory")) return
        _showEcosystemDirectory.value = true
    }

    fun closeEcosystemDirectory() {
        _showEcosystemDirectory.value = false
    }

    // Customer Care / Support
    fun openCustomerSupport() {
        if (!checkEmergencyBeforeAction("customer_support", "Customer Support")) return
        _showCustomerSupport.value = true
    }

    fun closeCustomerSupport() {
        _showCustomerSupport.value = false
    }

    fun submitSupportTicket(subject: String, category: String, message: String) {
        viewModelScope.launch {
            val rateCheck = SecurityHardeningEngine.checkAndRecordRequest("/support/tickets/create")
            if (!rateCheck.allowed) {
                recordClientSecurityEvent(
                    category = "BOT_TRAFFIC_BURST",
                    severity = "MEDIUM",
                    endpoint = "/support/tickets/create",
                    reason = rateCheck.reason ?: "Ticket submission rate limit exceeded",
                    errorCode = rateCheck.errorCode ?: "DOS-429-TICKET-FLOOD",
                    mitigation = "Throttled rapid ticket submission (${rateCheck.remainingLockoutMs / 1000}s cooldown)."
                )
                return@launch
            }
            val subjCheck = SecurityHardeningEngine.validateAndSanitizeInput("ticket.subject", subject, maxLength = 200)
            val msgCheck = SecurityHardeningEngine.validateAndSanitizeInput("ticket.message", message, maxLength = 3000)
            if (!subjCheck.isSafe || !msgCheck.isSafe) {
                recordClientSecurityEvent(
                    category = subjCheck.detectedCategory ?: msgCheck.detectedCategory ?: "INJECTION_XSS_SQLI",
                    severity = "HIGH",
                    endpoint = "/support/tickets/create",
                    reason = subjCheck.detectionReason ?: msgCheck.detectionReason ?: "Suspicious payload in support ticket",
                    errorCode = subjCheck.errorCode ?: msgCheck.errorCode ?: "SEC-INJ-400",
                    mitigation = "Sanitized HTML/SQLi tokens prior to storage."
                )
            }
            repository.createTicket(subjCheck.sanitizedValue, category, msgCheck.sanitizedValue, _userName.value)
        }
    }

    fun replyToSupportTicket(ticketId: String, message: String) {
        viewModelScope.launch {
            val msgCheck = SecurityHardeningEngine.validateAndSanitizeInput("ticket.reply", message, maxLength = 3000)
            if (!msgCheck.isSafe) {
                recordClientSecurityEvent(
                    category = msgCheck.detectedCategory ?: "INJECTION_XSS_SQLI",
                    severity = "HIGH",
                    endpoint = "/support/tickets/reply",
                    reason = msgCheck.detectionReason ?: "Suspicious payload in ticket reply",
                    errorCode = msgCheck.errorCode ?: "SEC-INJ-400",
                    mitigation = "Sanitized reply payload."
                )
            }
            repository.replyToTicket(ticketId, "USER", _userName.value, msgCheck.sanitizedValue)
        }
    }

    // Official Profile vs User Profile
    fun openOfficialProfile() {
        _showOfficialProfile.value = true
    }

    fun closeOfficialProfile() {
        _showOfficialProfile.value = false
    }

    // Admin Panel Actions
    fun openAdminPanel() {
        _showAdminPanel.value = true
    }

    fun openAdminPanelAuthenticated() {
        SecurityHardeningEngine.resetEndpointLockout("/auth/admin-login-gate")
        _isAdminAuthenticated.value = true
        _adminLoginError.value = null
        _showAdminPanel.value = true
    }

    fun closeAdminPanel() {
        _showAdminPanel.value = false
    }

    fun adminLogin(password: String): Boolean {
        val clean = password.trim()
        // Always accept exact master password RTX@0012352 immediately without lockout interference
        if (clean == "RTX@0012352" || AuthCredentialsConfig.isValidAdminGatePassword(clean)) {
            SecurityHardeningEngine.resetEndpointLockout("/auth/admin-login-gate")
            _isAdminAuthenticated.value = true
            _adminLoginError.value = null
            recordClientSecurityEvent(
                category = "ADMIN_SECURITY_AUDIT",
                severity = "LOW",
                endpoint = "/auth/admin-login-gate",
                reason = "Administrator authenticated via master password verification.",
                errorCode = "AUTH-200-ADMIN-VERIFIED",
                mitigation = "Granted session-scoped Admin Panel & Security Center clearance.",
                status = "Resolved"
            )
            return true
        }

        val failDecision = SecurityHardeningEngine.recordAuthFailure("/auth/admin-login-gate")
        _adminLoginError.value = "Invalid administrator password. Access denied."
        recordClientSecurityEvent(
            category = "BRUTE_FORCE_AUTH",
            severity = failDecision.triggeredSeverity ?: "HIGH",
            endpoint = "/auth/admin-login-gate",
            reason = failDecision.reason ?: "Failed Admin Panel authentication attempt (invalid password hash).",
            errorCode = failDecision.errorCode ?: "AUTH-401-ADMIN-DENIED",
            mitigation = "Rejected login via constant-time PBKDF2 comparison; logged attempt."
        )
        return false
    }

    /**
     * Verifies Admin Password (RTX@0012352) and logs in directly.
     */
    fun adminVerifyPassword(password: String): Boolean {
        return adminLogin(password)
    }

    /**
     * Verifies step 2 Admin Passkey (any 1 of 4 authorized passkeys: RRR@123, RTECH, Ruma, RRR@007).
     */
    fun adminVerifyPasskey(passkey: String): Boolean {
        val valid = AuthCredentialsConfig.isValidAdminPasskey(passkey)
        if (!valid) {
            _adminLoginError.value = "Invalid security passkey. Access denied."
        } else {
            _adminLoginError.value = null
        }
        return valid
    }

    /**
     * Completes two-factor Admin authentication (Password + Passkey).
     */
    fun adminLoginWithPasskey(password: String, passkey: String): Boolean {
        val rateStatus = SecurityHardeningEngine.checkAndRecordRequest(
            endpointKey = "/auth/admin-login-gate",
            maxRequestsPerWindow = 12,
            windowMs = 60_000L
        )
        if (!rateStatus.allowed) {
            val retrySec = (rateStatus.remainingLockoutMs / 1000).coerceAtLeast(1)
            _adminLoginError.value = "Too many attempts. Please wait ${retrySec}s before retrying."
            return false
        }
        if (!AuthCredentialsConfig.isValidAdminGatePassword(password)) {
            _adminLoginError.value = "Invalid administrator password. Access denied."
            return false
        }
        if (!AuthCredentialsConfig.isValidAdminPasskey(passkey)) {
            _adminLoginError.value = "Invalid security passkey. Access denied."
            return false
        }
        SecurityHardeningEngine.resetEndpointLockout("/auth/admin-login-gate")
        _isAdminAuthenticated.value = true
        _adminLoginError.value = null
        recordClientSecurityEvent(
            category = "ADMIN_SECURITY_AUDIT",
            severity = "LOW",
            endpoint = "/auth/admin-login-gate",
            reason = "Administrator authenticated via salted PBKDF2-HMAC-SHA256 password & 2FA Passkey verification.",
            errorCode = "AUTH-200-ADMIN-2FA-VERIFIED",
            mitigation = "Granted session-scoped Admin Panel & Security Center clearance.",
            status = "Resolved"
        )
        return true
    }

    fun adminLogout() {
        _isAdminAuthenticated.value = false
        _adminLoginError.value = null
    }

    // --- Security Center (Admin Security Monitoring & Incident Response) ---
    fun recordClientSecurityEvent(
        category: String,
        severity: String,
        endpoint: String,
        reason: String,
        errorCode: String,
        mitigation: String,
        status: String = "Detected",
        isBlocked: Boolean = false
    ) {
        viewModelScope.launch {
            val session = _userSession.value
            val userIdDisplay = if (session != null && _isUserLoggedIn.value) {
                "${session.userName} (${session.userId})"
            } else {
                "Unknown / Unauthenticated"
            }
            val roleDisplay = session?.role?.name ?: _currentUserRole.value.name
            val correlationId = session?.sessionCorrelationId ?: "unauth_session"
            val event = SecurityHardeningEngine.createSecurityEvent(
                category = category,
                severity = severity,
                endpointOrModule = endpoint,
                authenticatedUserId = userIdDisplay,
                userRole = roleDisplay,
                sessionCorrelationId = correlationId,
                detectionReason = reason,
                errorCode = errorCode,
                mitigationAction = mitigation,
                status = status,
                isSourceBlocked = isBlocked
            )
            repository.recordSecurityEvent(event)
        }
    }

    fun adminUpdateSecurityIncident(
        eventId: String,
        newStatus: String,
        notes: String,
        actionDescription: String,
        isBlocked: Boolean
    ) {
        viewModelScope.launch {
            repository.updateSecurityIncidentState(
                eventId = eventId,
                newStatus = newStatus,
                investigationNotes = notes,
                adminActionNote = actionDescription,
                isSourceBlocked = isBlocked
            )
        }
    }

    fun adminUnblockAllSecuritySources() {
        viewModelScope.launch {
            SecurityHardeningEngine.unlockAllRateLimits()
            sessionManager.resetFailedAttempts()
            repository.unblockAllRestrictedSources()
            recordClientSecurityEvent(
                category = "ADMIN_SECURITY_AUDIT",
                severity = "LOW",
                endpoint = "/admin/security-center/unblock-all",
                reason = "Administrator cleared all temporary IP/endpoint blocks and reset brute-force lockouts.",
                errorCode = "SEC-200-RECOVERY",
                mitigation = "All rate limiters and temporary source blocks cleared.",
                status = "Resolved"
            )
        }
    }

    fun adminRunSecuritySelfTest() {
        viewModelScope.launch {
            val sqliCheck = SecurityHardeningEngine.validateAndSanitizeInput(
                fieldName = "selftest.sqli",
                rawInput = "' OR '1'='1 -- UNION SELECT password FROM users"
            )
            val xssCheck = SecurityHardeningEngine.validateAndSanitizeInput(
                fieldName = "selftest.xss",
                rawInput = "<script>alert(document.cookie)</script>"
            )
            val urlCheck = SecurityHardeningEngine.validateWebViewNavigationUrl("javascript:alert(1)")
            val pbkdf2Valid = AuthCredentialsConfig.isValidAdminGatePassword("RTX@0012352") &&
                !AuthCredentialsConfig.isValidAdminGatePassword("wrong_password")
            val envReport = SecurityHardeningEngine.evaluateDeviceEnvironmentRisk()

            val allPassed = !sqliCheck.isSafe && !xssCheck.isSafe && !urlCheck.isAllowedInWebView && pbkdf2Valid
            recordClientSecurityEvent(
                category = "ADMIN_SECURITY_AUDIT",
                severity = if (allPassed) "LOW" else "HIGH",
                endpoint = "/admin/security-center/self-test",
                reason = "Authorized Security Self-Test completed: SQLi Guard=${!sqliCheck.isSafe}, XSS Guard=${!xssCheck.isSafe}, WebView JS Scheme Blocked=${!urlCheck.isAllowedInWebView}, PBKDF2 Constant-Time Verifier=$pbkdf2Valid, Device Posture=${envReport.deviceSummary}",
                errorCode = if (allPassed) "AUDIT-200-SELFTEST-PASS" else "AUDIT-500-SELFTEST-WARN",
                mitigation = "Verified all active security defenses in real time without disrupting user sessions.",
                status = "Resolved"
            )
        }
    }

    fun adminPurgeOldResolvedSecurityEvents() {
        viewModelScope.launch {
            repository.purgeOldResolvedSecurityEvents(retentionDays = 30)
        }
    }

    fun adminReplyToTicket(ticketId: String, reply: String, newStatus: String? = null) {
        viewModelScope.launch {
            repository.replyToTicket(ticketId, "ADMIN", "Rizwan RR (Admin)", reply, newStatus)
        }
    }

    fun adminResolveTicket(ticketId: String) {
        viewModelScope.launch {
            repository.resolveTicket(ticketId)
        }
    }

    fun adminBroadcastAnnouncement(title: String, message: String) {
        viewModelScope.launch {
            repository.sendBroadcastAnnouncement(title, message)
        }
    }

    fun adminSaveEcosystemLink(link: EcosystemLinkEntity) {
        viewModelScope.launch {
            repository.insertOrUpdateEcosystemLink(link)
        }
    }

    fun adminDeleteEcosystemLink(id: String) {
        viewModelScope.launch {
            repository.deleteEcosystemLink(id)
        }
    }

    // General App toggles
    fun toggleFavorite(app: AppItem) {
        viewModelScope.launch {
            repository.toggleFavorite(app.id, app.isFavorite)
        }
    }

    fun togglePinHome(app: AppItem) {
        viewModelScope.launch {
            repository.togglePinnedHome(app.id, app.isPinnedHome)
        }
    }

    fun toggleAppEnabled(app: AppItem) {
        viewModelScope.launch {
            repository.toggleAppEnabled(app.id, app.isEnabled)
        }
    }

    fun clearRecentHistory() {
        viewModelScope.launch {
            val currentApps = allApps.value
            currentApps.forEach {
                repository.insertOrUpdateApp(it.copy(lastOpenedTimestamp = 0L))
            }
        }
    }

    fun saveApp(app: AppItem) {
        viewModelScope.launch {
            repository.insertOrUpdateApp(app)
        }
    }

    fun deleteApp(id: String) {
        viewModelScope.launch {
            repository.deleteApp(id)
        }
    }

    fun resetToDefaults() {
        viewModelScope.launch {
            repository.resetToDefaultRegistry()
        }
    }

    // SIFRA Daily Task Neural Memory Actions
    fun openGoogleTasks() {
        if (!checkEmergencyBeforeAction("google_tasks", "Google Tasks")) return
        _showGoogleTasks.value = true
    }

    fun closeGoogleTasks() {
        _showGoogleTasks.value = false
    }

    fun openDailyTask() {
        if (!checkEmergencyBeforeAction("google_tasks", "Daily Task")) return
        _showGoogleTasks.value = true
    }

    fun closeDailyTask() {
        _showGoogleTasks.value = false
    }

    fun createTask(
        title: String,
        details: String = "",
        dueDate: String = "Today",
        dueTime: String = "",
        listCategory: String = "Personal",
        priority: String = "Medium",
        isStarred: Boolean = false
    ) {
        viewModelScope.launch {
            repository.insertTask(title, details, dueDate, dueTime, listCategory, priority, isStarred)
        }
    }

    fun updateTask(task: TaskEntity) {
        viewModelScope.launch {
            repository.updateTask(task)
        }
    }

    fun toggleTaskStatus(id: String, currentStatus: Boolean) {
        viewModelScope.launch {
            repository.updateTaskStatus(id, !currentStatus)
        }
    }

    fun toggleTaskStarred(id: String, currentStarred: Boolean) {
        viewModelScope.launch {
            repository.toggleTaskStarred(id, currentStarred)
        }
    }

    fun deleteTask(id: String) {
        viewModelScope.launch {
            repository.deleteTask(id)
        }
    }

    fun clearCompletedTasks() {
        viewModelScope.launch {
            repository.clearCompletedTasks()
        }
    }

    // Terms & Conditions and Privacy Policy Overlays
    fun openTermsAndConditions() {
        _showTermsAndConditions.value = true
    }

    fun closeTermsAndConditions() {
        _showTermsAndConditions.value = false
    }

    fun openPrivacyPolicy() {
        _showPrivacyPolicy.value = true
    }

    fun closePrivacyPolicy() {
        _showPrivacyPolicy.value = false
    }

    fun openDiagnostics() {
        if (!checkEmergencyBeforeAction("device_diagnostics", "Device Diagnostics")) return
        _showDiagnostics.value = true
    }

    fun closeDiagnostics() {
        _showDiagnostics.value = false
    }

    // Eligible Data Deletion Process
    fun deleteAllUserData(onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            repository.deleteAllUserData()
            _userName.value = "User"
            _osNotes.value = ""
            onComplete()
        }
    }
}
