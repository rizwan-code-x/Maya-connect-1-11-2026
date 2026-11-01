package com.example.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.ai.MayaKnowledgeBase
import com.example.data.model.AppItem
import com.example.data.model.EmergencyMode
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.AddEditAppDialog
import com.example.ui.components.AppActionSheet
import com.example.ui.components.AppDetailsDialog
import com.example.ui.components.EdgeSwipeBackGestureWrapper
import com.example.ui.components.GestureNavigationPill
import com.example.ui.components.GlassBottomNavigationBar
import com.example.ui.components.NavigationTab
import com.example.ui.components.NotificationShadeComponent
import com.example.ui.screens.AdminPanelScreen
import com.example.ui.screens.AllAppsLauncherScreen
import com.example.ui.screens.AppDrawerScreen
import com.example.ui.screens.AppsScreen
import com.example.ui.screens.ChatAssistantScreen
import com.example.ui.screens.CustomerSupportScreen
import com.example.ui.screens.EcosystemDirectoryScreen
import com.example.ui.screens.EcosystemLandingScreen
import com.example.ui.screens.ExploreScreen
import com.example.ui.screens.GoogleTasksScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.InAppBrowserScreen
import com.example.ui.screens.MayaOSView
import com.example.ui.screens.MeSettingsScreen
import com.example.ui.screens.MusicPlayerScreen
import com.example.ui.screens.NotepadScreen
import com.example.ui.screens.LoginFlowScreen
import com.example.ui.components.UnauthorizedAppDialog
import com.example.ui.screens.NotificationCenterScreen
import com.example.ui.screens.OfficialProfileScreen
import com.example.ui.screens.RecentActivityScreen
import com.example.ui.screens.TermsAndConditionsScreen
import com.example.ui.screens.PrivacyPolicyScreen
import com.example.ui.screens.MayaDiagnosticsScreen
import com.example.ui.screens.VoiceAssistantScreen
import androidx.activity.compose.BackHandler
import com.example.ui.theme.MayaConnectTheme
import com.example.ui.theme.MayaTheme
import com.example.ui.viewmodel.MayaViewModel

@Composable
fun MayaApp(viewModel: MayaViewModel) {
    BackHandler { viewModel.navigateBack() }
    val isUserLoggedIn by viewModel.isUserLoggedIn.collectAsStateWithLifecycle()
    val userSession by viewModel.userSession.collectAsStateWithLifecycle()
    val currentUserRole by viewModel.currentUserRole.collectAsStateWithLifecycle()
    val unauthorizedAppDialog by viewModel.unauthorizedAppDialog.collectAsStateWithLifecycle()

    val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
    val currentTab by viewModel.currentTab.collectAsStateWithLifecycle()
    val allApps by viewModel.allApps.collectAsStateWithLifecycle()
    val allAppsAdmin by viewModel.allAppsAdmin.collectAsStateWithLifecycle()
    val favoriteApps by viewModel.favoriteApps.collectAsStateWithLifecycle()
    val recentlyUsedApps by viewModel.recentlyUsedApps.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()
    val hapticEnabled by viewModel.hapticEnabled.collectAsStateWithLifecycle()
    val reduceMotion by viewModel.reduceMotion.collectAsStateWithLifecycle()
    val notificationsEnabled by viewModel.notificationsEnabled.collectAsStateWithLifecycle()
    val osNotes by viewModel.osNotes.collectAsStateWithLifecycle()

    // Notes, Support Tickets, Notifications, Ecosystem Links
    val allNotes by viewModel.allNotes.collectAsStateWithLifecycle()
    val allTickets by viewModel.allTickets.collectAsStateWithLifecycle()
    val openTicketsCount by viewModel.openTicketsCount.collectAsStateWithLifecycle()
    val allNotifications by viewModel.allNotifications.collectAsStateWithLifecycle()
    val unreadNotificationsCount by viewModel.unreadNotificationCount.collectAsStateWithLifecycle()
    val allEcosystemLinks by viewModel.allEcosystemLinks.collectAsStateWithLifecycle()

    // Maya OS States
    val isMayaOsActive by viewModel.isMayaOsActive.collectAsStateWithLifecycle()
    val userName by viewModel.userName.collectAsStateWithLifecycle()
    val mayaOsWallpaper by viewModel.mayaOsWallpaper.collectAsStateWithLifecycle()
    val customWallpaperUri by viewModel.customWallpaperUri.collectAsStateWithLifecycle()
    val customWallpapers by viewModel.customWallpapers.collectAsStateWithLifecycle()
    val mayaOsGridSize by viewModel.mayaOsGridSize.collectAsStateWithLifecycle()
    val dashboardClockStyle by viewModel.dashboardClockStyle.collectAsStateWithLifecycle()
    val showAppDrawer by viewModel.showAppDrawer.collectAsStateWithLifecycle()
    val homeFolders by viewModel.homeFolders.collectAsStateWithLifecycle()
    val isNotificationShadeOpen by viewModel.isNotificationShadeOpen.collectAsStateWithLifecycle()
    val activeHomeWidgets by viewModel.activeHomeWidgets.collectAsStateWithLifecycle()
    val isGestureNavEnabled by viewModel.isGestureNavEnabled.collectAsStateWithLifecycle()

    // Assistant Screens
    val showChatAssistant by viewModel.showChatAssistant.collectAsStateWithLifecycle()
    val chatAssistantInitialQuery by viewModel.chatAssistantInitialQuery.collectAsStateWithLifecycle()
    val showVoiceAssistant by viewModel.showVoiceAssistant.collectAsStateWithLifecycle()
    val showEcosystemLanding by viewModel.showEcosystemLanding.collectAsStateWithLifecycle()

    // New Modular Screens
    val showNotificationCenter by viewModel.showNotificationCenter.collectAsStateWithLifecycle()
    val showAllAppsLauncher by viewModel.showAllAppsLauncher.collectAsStateWithLifecycle()
    val showMusicPlayer by viewModel.showMusicPlayer.collectAsStateWithLifecycle()
    val musicPlaylist by viewModel.musicPlaylist.collectAsStateWithLifecycle()
    val currentTrackIdx by viewModel.currentTrackIndex.collectAsStateWithLifecycle()
    val isMusicPlaying by viewModel.isMusicPlaying.collectAsStateWithLifecycle()
    val currentMusicTrack = musicPlaylist.getOrNull(currentTrackIdx)
    val showNotepad by viewModel.showNotepad.collectAsStateWithLifecycle()
    val allTasks by viewModel.allTasks.collectAsStateWithLifecycle()
    val showGoogleTasks by viewModel.showGoogleTasks.collectAsStateWithLifecycle()
    val showEcosystemDirectory by viewModel.showEcosystemDirectory.collectAsStateWithLifecycle()
    val showCustomerSupport by viewModel.showCustomerSupport.collectAsStateWithLifecycle()
    val showOfficialProfile by viewModel.showOfficialProfile.collectAsStateWithLifecycle()
    val showAdminPanel by viewModel.showAdminPanel.collectAsStateWithLifecycle()
    val allRegisteredUsers by viewModel.allRegisteredUsers.collectAsStateWithLifecycle()
    val allDeletionRequests by viewModel.allDeletionRequests.collectAsStateWithLifecycle()
    val allAuditLogs by viewModel.allAuditLogs.collectAsStateWithLifecycle()
    val allSecurityEvents by viewModel.allSecurityEvents.collectAsStateWithLifecycle()
    val showTermsAndConditions by viewModel.showTermsAndConditions.collectAsStateWithLifecycle()
    val showPrivacyPolicy by viewModel.showPrivacyPolicy.collectAsStateWithLifecycle()
    val showDiagnostics by viewModel.showDiagnostics.collectAsStateWithLifecycle()

    // Admin Auth State
    val isAdminAuthenticated by viewModel.isAdminAuthenticated.collectAsStateWithLifecycle()
    val adminLoginError by viewModel.adminLoginError.collectAsStateWithLifecycle()

    // Emergency Situation State & Family Members (Firebase Realtime Database via MaintenanceService)
    val emergencyState by viewModel.emergencySystemState.collectAsStateWithLifecycle()
    val emergencyAlertTitle by viewModel.emergencyAlertTitle.collectAsStateWithLifecycle()
    val emergencyAlertMessage by viewModel.emergencyAlertMessage.collectAsStateWithLifecycle()
    val familyMembers by viewModel.familyMembers.collectAsStateWithLifecycle()

    // Active App Launch & Dialogs
    val activeAppForLaunch by viewModel.activeAppForLaunch.collectAsStateWithLifecycle()
    val selectedAppForAction by viewModel.selectedAppForAction.collectAsStateWithLifecycle()
    val selectedAppForDetails by viewModel.selectedAppForDetails.collectAsStateWithLifecycle()
    val selectedAppForEdit by viewModel.selectedAppForEdit.collectAsStateWithLifecycle()
    val showAddAppDialog by viewModel.showAddAppDialog.collectAsStateWithLifecycle()

    MayaConnectTheme(themeMode = themeMode) {
        if (!isUserLoggedIn) {
            LoginFlowScreen(
                onStandardLogin = { name -> viewModel.loginStandard(name) },
                onFamilyLogin = { name -> viewModel.loginFamily(name) },
                onCeoLogin = { name -> viewModel.loginCeo(name) },
                onVerifyFamilyCredentials = { id, pass -> viewModel.verifyAndAuthenticateFamilyMember(id, pass) },
                initialLockoutMillis = viewModel.getRemainingLockoutMillis(),
                onRecordFailedAttempt = { viewModel.recordFailedAttempt() },
                onResetFailedAttempts = { viewModel.resetFailedAttempts() },
                hapticEnabled = hapticEnabled
            )
            return@MayaConnectTheme
        }

        val theme = MayaTheme.colors

        EdgeSwipeBackGestureWrapper(
            onSwipeBack = { viewModel.navigateBack() }
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(theme.backgroundGradient)
            ) {
            // Main OS Screen Router
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
            ) {
                AnimatedContent(
                    targetState = currentTab,
                    transitionSpec = {
                        if (reduceMotion) {
                            fadeIn(animationSpec = tween(50)) togetherWith fadeOut(animationSpec = tween(50))
                        } else {
                            fadeIn(animationSpec = tween(220, easing = LinearEasing)) togetherWith
                                    fadeOut(animationSpec = tween(180, easing = LinearEasing))
                        }
                    },
                    label = "TabContent"
                ) { tab ->
                    when (tab) {
                        NavigationTab.HOME -> {
                            HomeScreen(
                                userName = userName,
                                allApps = allApps,
                                recentlyUsedApps = recentlyUsedApps,
                                latestNote = allNotes.firstOrNull(),
                                unreadNotificationsCount = unreadNotificationsCount,
                                currentMusicTrack = currentMusicTrack,
                                isMusicPlaying = isMusicPlaying,
                                onMusicPlayPauseToggle = { viewModel.toggleMusicPlayPause() },
                                onMusicNext = { viewModel.nextTrack() },
                                searchQuery = searchQuery,
                                onSearchQueryChange = { viewModel.onSearchQueryChange(it) },
                                onAppClick = { viewModel.launchApp(it) },
                                onAppLongClick = { viewModel.selectAppForAction(it) },
                                onFavoriteToggle = { viewModel.toggleFavorite(it) },
                                onSeeAllAppsClick = { viewModel.openAllAppsLauncher() },
                                onProfileClick = { viewModel.onTabSelected(NavigationTab.ME) },
                                onNotificationClick = { viewModel.openNotificationCenter() },
                                onOpenNotepad = { viewModel.openNotepad() },
                                onOpenMusicPlayer = { viewModel.openMusicPlayer() },
                                onOpenEcosystemDirectory = { viewModel.openEcosystemDirectory() },
                                onOpenCustomerSupport = { viewModel.openCustomerSupport() },
                                onOpenOfficialProfile = { viewModel.openOfficialProfile() },
                                onOpenGoogleTasks = { viewModel.openGoogleTasks() },
                                onOpenChatAssistant = { viewModel.openChatAssistant(it) },
                                onOpenVoiceAssistant = { viewModel.openVoiceAssistant() },
                                currentClockStyle = dashboardClockStyle,
                                onClockStyleSelected = { viewModel.setDashboardClockStyle(it) },
                                onOpenAppDrawer = { viewModel.openAppDrawer() },
                                currentThemeMode = themeMode,
                                onToggleTheme = {
                                    val next = if (themeMode == com.example.ui.theme.MayaThemeMode.DARK) com.example.ui.theme.MayaThemeMode.LIGHT else com.example.ui.theme.MayaThemeMode.DARK
                                    viewModel.setThemeMode(next)
                                },
                                onOpenJioSphere = {
                                    allApps.find { it.id == "jiosphere" }?.let { viewModel.launchApp(it) }
                                },
                                onOpenSifra = {
                                    allApps.find { it.id == "sifra" }?.let { viewModel.launchApp(it) }
                                },
                                onOpenMaps = {
                                    allApps.find { it.id == "google_maps" }?.let { viewModel.launchApp(it) }
                                },
                                folders = homeFolders,
                                onCreateFolder = { app1, app2 -> viewModel.createFolder(app1, app2) },
                                onAddAppToFolder = { folderId, app -> viewModel.addAppToFolder(folderId, app) },
                                onRenameFolder = { folderId, newName -> viewModel.renameFolder(folderId, newName) },
                                onRemoveAppFromFolder = { folderId, appId -> viewModel.removeAppFromFolder(folderId, appId) },
                                onDissolveFolder = { folderId -> viewModel.dissolveFolder(folderId) },
                                onOpenNotificationShade = { viewModel.openNotificationShade() },
                                activeWidgets = activeHomeWidgets,
                                onSaveWidgets = { viewModel.setHomeWidgets(it) },
                                userRole = currentUserRole,
                                emergencyState = emergencyState,
                                onSwitchAccount = { viewModel.logout() },
                                hapticEnabled = hapticEnabled
                            )
                        }

                        NavigationTab.APPS -> {
                            AppsScreen(
                                userName = userName,
                                allApps = allApps,
                                searchQuery = searchQuery,
                                selectedCategory = selectedCategory,
                                onSearchQueryChange = { viewModel.onSearchQueryChange(it) },
                                onCategorySelected = { viewModel.onCategorySelected(it) },
                                onAppClick = { viewModel.launchApp(it) },
                                onAppLongClick = { viewModel.selectAppForAction(it) },
                                onFavoriteToggle = { viewModel.toggleFavorite(it) },
                                onAddCustomAppClick = { viewModel.setShowAddAppDialog(true) },
                                onProfileClick = { viewModel.onTabSelected(NavigationTab.ME) },
                                hapticEnabled = hapticEnabled
                            )
                        }

                        NavigationTab.EXPLORE -> {
                            ExploreScreen(
                                userName = userName,
                                allApps = allApps,
                                osNotes = osNotes,
                                onNotesChange = { viewModel.updateNotes(it) },
                                onLaunchApp = { viewModel.launchApp(it) },
                                onCategorySelected = { category ->
                                    viewModel.onCategorySelected(category)
                                    viewModel.onTabSelected(NavigationTab.APPS)
                                },
                                onProfileClick = { viewModel.onTabSelected(NavigationTab.ME) },
                                onOpenEcosystemDirectory = { viewModel.openEcosystemDirectory() },
                                onOpenDiagnostics = { viewModel.openDiagnostics() },
                                hapticEnabled = hapticEnabled
                            )
                        }

                        NavigationTab.RECENT -> {
                            RecentActivityScreen(
                                userName = userName,
                                recentlyUsedApps = recentlyUsedApps,
                                onResumeApp = { viewModel.launchApp(it) },
                                onAppClick = { viewModel.launchApp(it) },
                                onProfileClick = { viewModel.onTabSelected(NavigationTab.ME) },
                                onClearHistory = { viewModel.clearRecentHistory() },
                                hapticEnabled = hapticEnabled
                            )
                        }

                        NavigationTab.ME -> {
                            MeSettingsScreen(
                                userName = userName,
                                onUserNameChange = { viewModel.updateUserName(it) },
                                userSession = userSession,
                                onLogout = { viewModel.logout() },
                                currentThemeMode = themeMode,
                                hapticEnabled = hapticEnabled,
                                reduceMotion = reduceMotion,
                                notificationsEnabled = notificationsEnabled,
                                allAppsAdmin = allAppsAdmin,
                                onThemeModeSelected = { viewModel.setThemeMode(it) },
                                onHapticsToggled = { viewModel.toggleHaptics(it) },
                                onReduceMotionToggled = { viewModel.toggleReduceMotion(it) },
                                onNotificationsToggled = { viewModel.toggleNotifications(it) },
                                onToggleAppEnabled = { viewModel.toggleAppEnabled(it) },
                                onClearRecentHistory = { viewModel.clearRecentHistory() },
                                onEditApp = { viewModel.selectAppForEdit(it) },
                                onAddApp = { viewModel.setShowAddAppDialog(true) },
                                onResetDefaults = { viewModel.resetToDefaults() },
                                onOpenTerms = { viewModel.openTermsAndConditions() },
                                onOpenPrivacyPolicy = { viewModel.openPrivacyPolicy() },
                                onDeleteAllUserData = { onComplete -> viewModel.deleteAllUserData(onComplete) },
                                onSubmitDataDeletionRequest = { reason -> viewModel.submitDataDeletionRequest(reason) }
                            )
                        }
                    }
                }
            }

            // Floating Bottom Navigation Bar (Hidden when any overlay is active or Mode 3 Lockout is active for Users/Family)
            val isMode3LockoutForNonCeo = emergencyState.activeMode == EmergencyMode.HIGH_SECURITY_LOCKDOWN &&
                    currentUserRole != com.example.data.auth.UserRole.CEO_PANEL
            val isAnyOverlayActive = activeAppForLaunch != null ||
                    isMode3LockoutForNonCeo ||
                    isMayaOsActive ||
                    showChatAssistant ||
                    showVoiceAssistant ||
                    showEcosystemLanding ||
                    showNotificationCenter ||
                    showAllAppsLauncher ||
                    showAppDrawer ||
                    showMusicPlayer ||
                    showNotepad ||
                    showGoogleTasks ||
                    showEcosystemDirectory ||
                    showCustomerSupport ||
                    showOfficialProfile ||
                    showAdminPanel ||
                    showTermsAndConditions ||
                    showPrivacyPolicy ||
                    showDiagnostics

            if (!isAnyOverlayActive) {
                GlassBottomNavigationBar(
                    currentTab = currentTab,
                    onTabSelected = { viewModel.onTabSelected(it) },
                    modifier = Modifier.align(Alignment.BottomCenter),
                    hapticEnabled = hapticEnabled
                )
            }

            // Maya OS View
            AnimatedVisibility(
                visible = isMayaOsActive,
                enter = if (reduceMotion) fadeIn(animationSpec = tween(80)) else slideInVertically(
                    initialOffsetY = { it / 6 },
                    animationSpec = tween(300, easing = FastOutSlowInEasing)
                ) + fadeIn(animationSpec = tween(250)),
                exit = if (reduceMotion) fadeOut(animationSpec = tween(80)) else slideOutVertically(
                    targetOffsetY = { it / 6 },
                    animationSpec = tween(240, easing = FastOutSlowInEasing)
                ) + fadeOut(animationSpec = tween(200))
            ) {
                MayaOSView(
                    userName = userName,
                    allApps = allApps,
                    currentThemeMode = themeMode,
                    wallpaper = mayaOsWallpaper,
                    gridSize = mayaOsGridSize,
                    onThemeModeSelected = { viewModel.setThemeMode(it) },
                    onWallpaperSelected = { viewModel.setMayaOsWallpaper(it) },
                    onGridSizeSelected = { viewModel.setMayaOsGridSize(it) },
                    onUserNameChange = { viewModel.updateUserName(it) },
                    onExitMayaOs = { viewModel.closeMayaOs() },
                    onConfigureApp = { appToConfig ->
                        viewModel.selectAppForEdit(appToConfig)
                    },
                    customWallpaperUri = customWallpaperUri,
                    customWallpapersList = customWallpapers,
                    onCustomWallpaperSelected = { viewModel.setCustomWallpaper(it) },
                    onClearCustomWallpaper = { viewModel.clearCustomWallpaper() },
                    onAddCustomWallpaper = { viewModel.addCustomWallpaperFromUri(it) },
                    hapticEnabled = hapticEnabled
                )
            }

            // In-App Seamless Browser Launch Screen
            val launchEnterTransition = if (reduceMotion) {
                fadeIn(animationSpec = tween(100))
            } else {
                slideInVertically(
                    initialOffsetY = { it / 4 },
                    animationSpec = tween(280, easing = FastOutSlowInEasing)
                ) + fadeIn(animationSpec = tween(250)) + scaleIn(
                    initialScale = 0.92f,
                    animationSpec = tween(280, easing = FastOutSlowInEasing)
                )
            }

            val launchExitTransition = if (reduceMotion) {
                fadeOut(animationSpec = tween(100))
            } else {
                slideOutVertically(
                    targetOffsetY = { it / 4 },
                    animationSpec = tween(220, easing = FastOutSlowInEasing)
                ) + fadeOut(animationSpec = tween(200)) + scaleOut(
                    targetScale = 0.94f,
                    animationSpec = tween(220, easing = FastOutSlowInEasing)
                )
            }

            AnimatedVisibility(
                visible = activeAppForLaunch != null,
                enter = launchEnterTransition,
                exit = launchExitTransition
            ) {
                activeAppForLaunch?.let { app ->
                    InAppBrowserScreen(
                        app = app,
                        onClose = { viewModel.closeLaunchedApp() },
                        onConfigureApp = { appToConfig ->
                            viewModel.closeLaunchedApp()
                            viewModel.selectAppForEdit(appToConfig)
                        },
                        onSecurityEventDetected = { category, severity, endpoint, reason, errorCode, mitigation ->
                            viewModel.recordClientSecurityEvent(
                                category = category,
                                severity = severity,
                                endpoint = endpoint,
                                reason = reason,
                                errorCode = errorCode,
                                mitigation = mitigation
                            )
                        }
                    )
                }
            }

            fun executeAssistantAction(action: MayaKnowledgeBase.AssistantAction) {
                when (action) {
                    MayaKnowledgeBase.AssistantAction.OPEN_REXLIFY -> {
                        viewModel.closeChatAssistant()
                        viewModel.closeVoiceAssistant()
                        allApps.find { it.id == "rexlify" }?.let { viewModel.launchApp(it) }
                    }
                    MayaKnowledgeBase.AssistantAction.OPEN_NEURAX -> {
                        viewModel.closeChatAssistant()
                        viewModel.closeVoiceAssistant()
                        allApps.find { it.id == "neurax" }?.let { viewModel.launchApp(it) }
                    }
                    MayaKnowledgeBase.AssistantAction.OPEN_NOORAX -> {
                        viewModel.closeChatAssistant()
                        viewModel.closeVoiceAssistant()
                        allApps.find { it.id == "noorax" }?.let { viewModel.launchApp(it) }
                    }
                    MayaKnowledgeBase.AssistantAction.OPEN_SIFRA -> {
                        viewModel.closeChatAssistant()
                        viewModel.closeVoiceAssistant()
                        allApps.find { it.id == "sifra" }?.let { viewModel.launchApp(it) }
                    }
                    MayaKnowledgeBase.AssistantAction.OPEN_RODP -> {
                        viewModel.closeChatAssistant()
                        viewModel.closeVoiceAssistant()
                        allApps.find { it.id == "rodp" }?.let { viewModel.launchApp(it) }
                    }
                    MayaKnowledgeBase.AssistantAction.OPEN_MAYA_OS -> {
                        viewModel.closeChatAssistant()
                        viewModel.closeVoiceAssistant()
                        viewModel.openMayaOs()
                    }
                    MayaKnowledgeBase.AssistantAction.OPEN_SETTINGS,
                    MayaKnowledgeBase.AssistantAction.CHANGE_THEME -> {
                        viewModel.closeChatAssistant()
                        viewModel.closeVoiceAssistant()
                        viewModel.onTabSelected(NavigationTab.ME)
                    }
                    MayaKnowledgeBase.AssistantAction.GO_HOME -> {
                        viewModel.closeChatAssistant()
                        viewModel.closeVoiceAssistant()
                        viewModel.onTabSelected(NavigationTab.HOME)
                    }
                    MayaKnowledgeBase.AssistantAction.SHOW_RECENT_ACTIVITY -> {
                        viewModel.closeChatAssistant()
                        viewModel.closeVoiceAssistant()
                        viewModel.onTabSelected(NavigationTab.EXPLORE)
                    }
                    MayaKnowledgeBase.AssistantAction.NONE -> {}
                }
            }

            // Chat Assistant Screen
            AnimatedVisibility(
                visible = showChatAssistant,
                enter = if (reduceMotion) fadeIn(animationSpec = tween(80)) else slideInVertically(
                    initialOffsetY = { it / 4 },
                    animationSpec = tween(280, easing = FastOutSlowInEasing)
                ) + fadeIn(animationSpec = tween(220)),
                exit = if (reduceMotion) fadeOut(animationSpec = tween(80)) else slideOutVertically(
                    targetOffsetY = { it / 4 },
                    animationSpec = tween(220, easing = FastOutSlowInEasing)
                ) + fadeOut(animationSpec = tween(180))
            ) {
                ChatAssistantScreen(
                    initialPrompt = chatAssistantInitialQuery,
                    onClose = { viewModel.closeChatAssistant() },
                    onExecuteAction = { executeAssistantAction(it) }
                )
            }

            // Voice Assistant Screen
            AnimatedVisibility(
                visible = showVoiceAssistant,
                enter = if (reduceMotion) fadeIn(animationSpec = tween(80)) else slideInVertically(
                    initialOffsetY = { it / 4 },
                    animationSpec = tween(280, easing = FastOutSlowInEasing)
                ) + fadeIn(animationSpec = tween(220)),
                exit = if (reduceMotion) fadeOut(animationSpec = tween(80)) else slideOutVertically(
                    targetOffsetY = { it / 4 },
                    animationSpec = tween(220, easing = FastOutSlowInEasing)
                ) + fadeOut(animationSpec = tween(180))
            ) {
                VoiceAssistantScreen(
                    onClose = { viewModel.closeVoiceAssistant() },
                    onExecuteAction = { executeAssistantAction(it) }
                )
            }

            // Digital Ecosystem Landing Page
            AnimatedVisibility(
                visible = showEcosystemLanding,
                enter = if (reduceMotion) fadeIn(animationSpec = tween(80)) else slideInVertically(
                    initialOffsetY = { it / 4 },
                    animationSpec = tween(280, easing = FastOutSlowInEasing)
                ) + fadeIn(animationSpec = tween(220)),
                exit = if (reduceMotion) fadeOut(animationSpec = tween(80)) else slideOutVertically(
                    targetOffsetY = { it / 4 },
                    animationSpec = tween(220, easing = FastOutSlowInEasing)
                ) + fadeOut(animationSpec = tween(180))
            ) {
                EcosystemLandingScreen(
                    onClose = { viewModel.closeEcosystemLanding() },
                    onVisitWebsite = {
                        viewModel.closeEcosystemLanding()
                        allApps.find { it.id == "rexlify" }?.let { viewModel.launchApp(it) }
                    },
                    onLaunchMayaOs = {
                        viewModel.closeEcosystemLanding()
                        viewModel.openMayaOs()
                    },
                    hapticEnabled = hapticEnabled
                )
            }

            // Point 10: Notification Center Screen
            AnimatedVisibility(
                visible = showNotificationCenter,
                enter = slideInVertically(initialOffsetY = { -it / 4 }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { -it / 4 }) + fadeOut()
            ) {
                NotificationCenterScreen(
                    viewModel = viewModel,
                    onClose = { viewModel.closeNotificationCenter() },
                    onNavigateToSupport = {
                        viewModel.closeNotificationCenter()
                        viewModel.openCustomerSupport()
                    }
                )
            }

            // Point 5: Full-screen All Apps Launcher Screen
            AnimatedVisibility(
                visible = showAllAppsLauncher,
                enter = slideInVertically(initialOffsetY = { it / 4 }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it / 4 }) + fadeOut()
            ) {
                AllAppsLauncherScreen(
                    allApps = allApps,
                    onClose = { viewModel.closeAllAppsLauncher() },
                    onAppClick = { viewModel.launchApp(it) },
                    onAppLongClick = { viewModel.selectAppForAction(it) },
                    onAddAppClick = { viewModel.setShowAddAppDialog(true) },
                    hapticEnabled = hapticEnabled
                )
            }

            // High-Performance Swipeable/Button-Triggered App Drawer Screen
            AnimatedVisibility(
                visible = showAppDrawer,
                enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
            ) {
                AppDrawerScreen(
                    allApps = allApps,
                    onClose = { viewModel.closeAppDrawer() },
                    onAppClick = { viewModel.launchApp(it) },
                    onAppLongClick = { viewModel.selectAppForAction(it) },
                    hapticEnabled = hapticEnabled
                )
            }

            // Point 6: Music Module Screen
            AnimatedVisibility(
                visible = showMusicPlayer,
                enter = slideInVertically(initialOffsetY = { it / 4 }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it / 4 }) + fadeOut()
            ) {
                MusicPlayerScreen(
                    viewModel = viewModel,
                    onClose = { viewModel.closeMusicPlayer() }
                )
            }

            // Point 6: Notepad Module Screen
            AnimatedVisibility(
                visible = showNotepad,
                enter = slideInVertically(initialOffsetY = { it / 4 }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it / 4 }) + fadeOut()
            ) {
                NotepadScreen(
                    viewModel = viewModel,
                    onClose = { viewModel.closeNotepad() }
                )
            }

            // Google Tasks Daily Task Manager Overlay Screen
            AnimatedVisibility(
                visible = showGoogleTasks,
                enter = slideInVertically(initialOffsetY = { it / 4 }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it / 4 }) + fadeOut()
            ) {
                GoogleTasksScreen(
                    tasks = allTasks,
                    onClose = { viewModel.closeGoogleTasks() },
                    onCreateTask = { title, details, dueDate, category ->
                        viewModel.createTask(title, details, dueDate, "", category, "Medium", false)
                    },
                    onCreateTaskFull = { title, details, dueDate, dueTime, category, priority, isStarred ->
                        viewModel.createTask(title, details, dueDate, dueTime, category, priority, isStarred)
                    },
                    onToggleTaskStatus = { taskId, isCompleted ->
                        viewModel.toggleTaskStatus(taskId, isCompleted)
                    },
                    onToggleTaskStarred = { taskId, currentStarred ->
                        viewModel.toggleTaskStarred(taskId, currentStarred)
                    },
                    onDeleteTask = { taskId ->
                        viewModel.deleteTask(taskId)
                    },
                    onClearCompleted = {
                        viewModel.clearCompletedTasks()
                    }
                )
            }

            // Point 6: Ecosystem Directory Screen
            AnimatedVisibility(
                visible = showEcosystemDirectory,
                enter = slideInVertically(initialOffsetY = { it / 4 }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it / 4 }) + fadeOut()
            ) {
                EcosystemDirectoryScreen(
                    viewModel = viewModel,
                    onClose = { viewModel.closeEcosystemDirectory() },
                    onLaunchUrl = { url, title ->
                        val tempApp = AppItem(
                            id = "portal_${System.currentTimeMillis()}",
                            name = title,
                            tagline = "Official Ecosystem Portal",
                            fullDescription = "Official Portal: $title",
                            url = url,
                            category = com.example.data.model.AppCategory.ALL
                        )
                        viewModel.launchApp(tempApp)
                    }
                )
            }

            // Point 7: Customer Support Screen
            AnimatedVisibility(
                visible = showCustomerSupport,
                enter = slideInVertically(initialOffsetY = { it / 4 }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it / 4 }) + fadeOut()
            ) {
                CustomerSupportScreen(
                    viewModel = viewModel,
                    onClose = { viewModel.closeCustomerSupport() }
                )
            }

            // Point 8: Official Profile Screen
            AnimatedVisibility(
                visible = showOfficialProfile,
                enter = slideInVertically(initialOffsetY = { it / 4 }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it / 4 }) + fadeOut()
            ) {
                OfficialProfileScreen(
                    ecosystemLinks = allEcosystemLinks,
                    onClose = { viewModel.closeOfficialProfile() },
                    onOpenAdminPanel = {
                        viewModel.closeOfficialProfile()
                        viewModel.openAdminPanelAuthenticated()
                    },
                    onOpenEcosystemLink = { link ->
                        val tempApp = AppItem(
                            id = link.id,
                            name = link.name,
                            tagline = link.badge,
                            fullDescription = link.description,
                            url = link.url,
                            category = com.example.data.model.AppCategory.ALL
                        )
                        viewModel.launchApp(tempApp)
                    },
                    onOpenTerms = {
                        viewModel.closeOfficialProfile()
                        viewModel.openTermsAndConditions()
                    },
                    onOpenPrivacyPolicy = {
                        viewModel.closeOfficialProfile()
                        viewModel.openPrivacyPolicy()
                    },
                    currentUserRole = currentUserRole,
                    hapticEnabled = hapticEnabled
                )
            }

            // Point 9: Admin Panel Screen
            AnimatedVisibility(
                visible = showAdminPanel,
                enter = slideInVertically(initialOffsetY = { it / 4 }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it / 4 }) + fadeOut()
            ) {
                AdminPanelScreen(
                    isAuthenticated = isAdminAuthenticated,
                    loginError = adminLoginError,
                    allAppsAdmin = allAppsAdmin,
                    allTickets = allTickets,
                    openTicketsCount = openTicketsCount,
                    ecosystemLinks = allEcosystemLinks,
                    registeredUsers = allRegisteredUsers,
                    deletionRequests = allDeletionRequests,
                    auditLogs = allAuditLogs,
                    securityEvents = allSecurityEvents,
                    emergencyState = emergencyState,
                    familyMembers = familyMembers,
                    onLogin = { viewModel.adminLogin(it) },
                    onLoginWithPasskey = { p, k -> viewModel.adminLoginWithPasskey(p, k) },
                    onVerifyPassword = { viewModel.adminVerifyPassword(it) },
                    onSetEmergencyMode = { mode, reason -> viewModel.adminSetEmergencyMode(mode, reason) },
                    onUpdateDisabledFeatures = { viewModel.adminUpdateDisabledFeatures(it) },
                    onToggleFeatureRestriction = { viewModel.adminToggleFeatureRestriction(it) },
                    onUpdateCustomDurationHours = { viewModel.adminUpdateCustomDurationHours(it) },
                    onCreateOrUpdateFamilyMember = { id, pass, name, details ->
                        viewModel.adminCreateOrUpdateFamilyMember(id, pass, name, details)
                    },
                    onToggleFamilyMemberActive = { viewModel.adminToggleFamilyMemberActive(it) },
                    onDeleteFamilyMember = { viewModel.adminDeleteFamilyMember(it) },
                    onLogout = { viewModel.adminLogout() },
                    onClose = { viewModel.closeAdminPanel() },
                    onToggleAppEnabled = { viewModel.toggleAppEnabled(it) },
                    onEditApp = { viewModel.selectAppForEdit(it) },
                    onAddApp = { viewModel.setShowAddAppDialog(true) },
                    onDeleteApp = { viewModel.deleteApp(it) },
                    onAdminReplyTicket = { ticketId, reply, newStatus ->
                        viewModel.adminReplyToTicket(ticketId, reply, newStatus)
                    },
                    onResolveTicket = { viewModel.adminResolveTicket(it) },
                    onBroadcastAnnouncement = { title, msg ->
                        viewModel.adminBroadcastAnnouncement(title, msg)
                    },
                    onSaveEcosystemLink = { viewModel.adminSaveEcosystemLink(it) },
                    onDeleteEcosystemLink = { viewModel.adminDeleteEcosystemLink(it) },
                    onIssueWarning = { userId, name, reason ->
                        viewModel.adminIssueWarning(userId, name, reason)
                    },
                    onToggleSuspend = { userId, status, name ->
                        viewModel.adminToggleSuspend(userId, status, name)
                    },
                    onSetUserStatus = { userId, name, status, reason ->
                        viewModel.adminSetUserStatus(userId, name, status, reason)
                    },
                    onApproveDeletionRequest = { requestId ->
                        viewModel.adminApproveDeletionRequest(requestId)
                    },
                    onRejectDeletionRequest = { requestId, reason ->
                        viewModel.adminRejectDeletionRequest(requestId, reason)
                    },
                    onUpgradeUserRole = { userId, role, name ->
                        viewModel.adminUpgradeUserRole(userId, role, name)
                    },
                    onSendMessageToUser = { userId, name, title, msg ->
                        viewModel.adminSendMessageToUser(userId, name, title, msg)
                    },
                    onDeleteUser = { viewModel.adminDeleteUser(it) },
                    onUpdateSecurityIncident = { eventId, newStatus, notes, actionDesc, isBlocked ->
                        viewModel.adminUpdateSecurityIncident(eventId, newStatus, notes, actionDesc, isBlocked)
                    },
                    onUnblockAllSecuritySources = {
                        viewModel.adminUnblockAllSecuritySources()
                    },
                    onRunSecuritySelfTest = {
                        viewModel.adminRunSecuritySelfTest()
                    },
                    onPurgeOldResolvedSecurityEvents = {
                        viewModel.adminPurgeOldResolvedSecurityEvents()
                    },
                    hapticEnabled = hapticEnabled
                )
            }

            // Terms & Conditions Screen Overlay
            AnimatedVisibility(
                visible = showTermsAndConditions,
                enter = slideInVertically(initialOffsetY = { it / 4 }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it / 4 }) + fadeOut()
            ) {
                TermsAndConditionsScreen(
                    onClose = { viewModel.closeTermsAndConditions() },
                    onOpenPrivacyPolicy = {
                        viewModel.closeTermsAndConditions()
                        viewModel.openPrivacyPolicy()
                    },
                    hapticEnabled = hapticEnabled
                )
            }

            // Privacy Policy Screen Overlay
            AnimatedVisibility(
                visible = showPrivacyPolicy,
                enter = slideInVertically(initialOffsetY = { it / 4 }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it / 4 }) + fadeOut()
            ) {
                PrivacyPolicyScreen(
                    userName = userName,
                    onClose = { viewModel.closePrivacyPolicy() },
                    onOpenTermsAndConditions = {
                        viewModel.closePrivacyPolicy()
                        viewModel.openTermsAndConditions()
                    },
                    onDeleteAllOnDeviceData = { onComplete ->
                        viewModel.deleteAllUserData(onComplete)
                    },
                    hapticEnabled = hapticEnabled
                )
            }

            // Maya Diagnostics Overlay
            AnimatedVisibility(
                visible = showDiagnostics,
                enter = slideInVertically(initialOffsetY = { it / 4 }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it / 4 }) + fadeOut()
            ) {
                MayaDiagnosticsScreen(
                    viewModel = viewModel,
                    onClose = { viewModel.closeDiagnostics() }
                )
            }

            // Long-Press Action Sheet Bottom Sheet
            selectedAppForAction?.let { app ->
                AppActionSheet(
                    app = app,
                    onDismiss = { viewModel.selectAppForAction(null) },
                    onOpenApp = { viewModel.launchApp(app) },
                    onToggleFavorite = { viewModel.toggleFavorite(app) },
                    onTogglePinHome = { viewModel.togglePinHome(app) },
                    onViewDetails = { viewModel.selectAppForDetails(app) },
                    onEditApp = { viewModel.selectAppForEdit(app) },
                    onDeleteApp = if (app.isCustom) {
                        { viewModel.deleteApp(app.id) }
                    } else null
                )
            }

            // App Details Dialog
            selectedAppForDetails?.let { app ->
                AppDetailsDialog(
                    app = app,
                    onDismiss = { viewModel.selectAppForDetails(null) },
                    onOpenApp = { viewModel.launchApp(app) },
                    onToggleFavorite = { viewModel.toggleFavorite(app) },
                    onEditApp = {
                        viewModel.selectAppForDetails(null)
                        viewModel.selectAppForEdit(app)
                    }
                )
            }

            // Add Custom App Dialog
            if (showAddAppDialog) {
                AddEditAppDialog(
                    initialApp = null,
                    onDismiss = { viewModel.setShowAddAppDialog(false) },
                    onSave = { newApp -> viewModel.saveApp(newApp) }
                )
            }

            // Edit App Settings / URL Dialog
            selectedAppForEdit?.let { app ->
                AddEditAppDialog(
                    initialApp = app,
                    onDismiss = { viewModel.selectAppForEdit(null) },
                    onSave = { updatedApp -> viewModel.saveApp(updatedApp) }
                )
            }

            // Unauthorized Application Authorization Dialog (Standard Users)
            unauthorizedAppDialog?.let { restrictedApp ->
                UnauthorizedAppDialog(
                    app = restrictedApp,
                    onDismiss = { viewModel.dismissUnauthorizedAppDialog() },
                    onContactSupport = { viewModel.openCustomerSupportFromUnauthorizedDialog() }
                )
            }

            // Downward-Swipeable Notification Shade Component (System Alerts & Quick Toggles)
            NotificationShadeComponent(
                isOpen = isNotificationShadeOpen,
                onClose = { viewModel.closeNotificationShade() }
            )

            // Modern OS Gesture Navigation Bar Pill (Swipe Up to Home, Swipe Left/Right to Switch Tabs)
            if (isGestureNavEnabled) {
                GestureNavigationPill(
                    onSwipeUpHome = { viewModel.navigateToHome() },
                    onSwipeAdjacentTab = { forward -> viewModel.navigateAdjacentTab(forward) }
                )
            }

            // Emergency Alert Dialog (Option 1 & Option 2 feedback)
            if (emergencyAlertMessage != null) {
                androidx.compose.material3.AlertDialog(
                    onDismissRequest = { viewModel.dismissEmergencyAlert() },
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
                            text = emergencyAlertTitle ?: "System Notice",
                            style = TextStyle(
                                fontWeight = FontWeight.Bold,
                                fontSize = 17.sp,
                                color = theme.textPrimary
                            )
                        )
                    },
                    text = {
                        Text(
                            text = emergencyAlertMessage!!,
                            style = TextStyle(
                                fontSize = 13.5.sp,
                                lineHeight = 19.sp,
                                color = theme.textSecondary
                            )
                        )
                    },
                    confirmButton = {
                        Button(
                            onClick = { viewModel.dismissEmergencyAlert() },
                            colors = ButtonDefaults.buttonColors(containerColor = theme.accentPrimary)
                        ) {
                            Text("Acknowledge", color = Color.White)
                        }
                    }
                )
            }

            // Option 3: Handled directly on HomeScreen for Standard Users & RRR Family (CEO Panel is exempt)
        }
    }
}
}

@Composable
private fun HighSecurityLockdownOverlay(
    onAdminUnlock: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF090D16))
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(26.dp))
                .background(Color(0xFF131B2E))
                .border(1.5.dp, Color(0xFFDC2626).copy(alpha = 0.6f), RoundedCornerShape(26.dp))
                .padding(24.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(76.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            listOf(Color(0xFFDC2626), Color(0xFF7F1D1D))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Shield,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(42.dp)
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFDC2626).copy(alpha = 0.2f))
                    .border(1.dp, Color(0xFFDC2626), RoundedCornerShape(8.dp))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "STATUS: MAXIMUM SECURITY ENFORCED",
                    style = TextStyle(
                        color = Color(0xFFEF4444),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "HIGH-SECURITY LOCKDOWN",
                style = TextStyle(
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                ),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Major System Maintenance & Threat Protocols Active",
                style = TextStyle(
                    color = Color(0xFFF87171),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                ),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "All public and standard user services are temporarily suspended under high-security guidelines. The system is undergoing scheduled core infrastructure maintenance and threat response mitigation.",
                style = TextStyle(
                    color = Color(0xFF94A3B8),
                    fontSize = 12.5.sp,
                    lineHeight = 18.sp,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onAdminUnlock,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626))
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AdminPanelSettings,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Administrator Emergency Access",
                        style = TextStyle(
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Support: rexlifyofficialsupportteam@gmail.com",
                style = TextStyle(
                    color = Color(0xFF64748B),
                    fontSize = 11.5.sp
                )
            )
        }
    }
}
