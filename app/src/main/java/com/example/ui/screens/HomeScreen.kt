package com.example.ui.screens

import android.content.Intent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import com.example.ui.components.UnifiedHomeTaskbar
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Engineering
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LockClock
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.example.R
import com.example.data.model.EmergencyMode
import com.example.data.model.EmergencySystemState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.NoteEntity
import com.example.data.model.AppItem
import com.example.data.model.SearchHelper
import com.example.ui.components.AppLogoIcon
import com.example.ui.components.CustomizeWidgetsDialog
import com.example.ui.components.DashboardClockStyle
import com.example.ui.components.HomeWidgetSection
import com.example.ui.components.HomeWidgetType
import com.example.ui.components.MayaTopBar
import com.example.ui.components.PersistentStatusBar
import com.example.ui.components.QuickActionsComponent
import com.example.ui.components.RealDashboardCard
import com.example.ui.components.glassmorphic
import com.example.ui.components.rememberChromeSmoothFlingBehavior
import com.example.ui.components.springPressEffect
import com.example.ui.theme.MayaTheme
import com.example.ui.theme.MayaThemeMode
import com.example.ui.viewmodel.MusicTrack
import java.util.Calendar
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.offset
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.window.Dialog
import com.example.data.model.AppFolder
import com.example.ui.components.FolderDetailDialog
import com.example.ui.components.FolderGridItem
import kotlin.math.roundToInt

import androidx.compose.material.icons.filled.FormatListBulleted

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HomeScreen(
    userName: String = "Rizwan RR",
    allApps: List<AppItem>,
    recentlyUsedApps: List<AppItem> = emptyList(),
    latestNote: NoteEntity? = null,
    unreadNotificationsCount: Int = 0,
    currentMusicTrack: MusicTrack? = null,
    isMusicPlaying: Boolean = false,
    onMusicPlayPauseToggle: () -> Unit = {},
    onMusicNext: () -> Unit = {},
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onAppClick: (AppItem) -> Unit,
    onAppLongClick: (AppItem) -> Unit,
    onFavoriteToggle: (AppItem) -> Unit,
    onSeeAllAppsClick: () -> Unit,
    onProfileClick: () -> Unit,
    onNotificationClick: () -> Unit,
    onOpenNotepad: () -> Unit = {},
    onOpenMusicPlayer: () -> Unit = {},
    onOpenEcosystemDirectory: () -> Unit = {},
    onOpenCustomerSupport: () -> Unit = {},
    onOpenOfficialProfile: () -> Unit = {},
    onOpenGoogleTasks: () -> Unit = {},
    onOpenChatAssistant: (initialQuery: String) -> Unit = {},
    onOpenVoiceAssistant: () -> Unit = {},
    currentClockStyle: DashboardClockStyle = DashboardClockStyle.ANALOGUE,
    onClockStyleSelected: (DashboardClockStyle) -> Unit = {},
    onOpenAppDrawer: () -> Unit = {},
    currentThemeMode: MayaThemeMode = MayaThemeMode.DARK,
    onToggleTheme: () -> Unit = {},
    onOpenJioSphere: () -> Unit = {},
    onOpenSifra: () -> Unit = {},
    onOpenMaps: () -> Unit = {},
    folders: List<AppFolder> = emptyList(),
    onCreateFolder: (AppItem, AppItem) -> Unit = { _, _ -> },
    onAddAppToFolder: (String, AppItem) -> Unit = { _, _ -> },
    onRenameFolder: (String, String) -> Unit = { _, _ -> },
    onRemoveAppFromFolder: (String, String) -> Unit = { _, _ -> },
    onDissolveFolder: (String) -> Unit = {},
    onOpenNotificationShade: () -> Unit = {},
    activeWidgets: List<HomeWidgetType> = listOf(
        HomeWidgetType.WEATHER,
        HomeWidgetType.CALENDAR,
        HomeWidgetType.BATTERY_STATUS
    ),
    onSaveWidgets: (List<HomeWidgetType>) -> Unit = {},
    userRole: com.example.data.auth.UserRole = com.example.data.auth.UserRole.STANDARD,
    emergencyState: EmergencySystemState = EmergencySystemState(),
    onSwitchAccount: () -> Unit = {},
    hapticEnabled: Boolean = true
) {
    val theme = MayaTheme.colors
    val context = LocalContext.current

    // Mode 3: Full Home Page Lockout for Standard Users and RRR Family (CEO Panel is Exempt)
    if (emergencyState.activeMode == EmergencyMode.HIGH_SECURITY_LOCKDOWN &&
        userRole != com.example.data.auth.UserRole.CEO_PANEL
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .testTag("home_mode_3_lockout_screen"),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(28.dp))
                    .glassmorphic(
                        shape = RoundedCornerShape(28.dp),
                        backgroundColor = theme.surfaceGlass,
                        borderColor = Color(0xFFEF4444).copy(alpha = 0.55f),
                        borderWidth = 1.5.dp
                    )
                    .padding(28.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(84.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                        .border(2.dp, Color(0xFFEF4444).copy(alpha = 0.6f), CircleShape),
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

                Spacer(modifier = Modifier.height(18.dp))

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFEF4444).copy(alpha = 0.15f))
                        .border(1.dp, Color(0xFFEF4444).copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 12.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = "SERVICE TEMPORARILY UNAVAILABLE",
                        style = TextStyle(
                            color = Color(0xFFEF4444),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.6.sp
                        )
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "You cannot use this application right now. Please try again after some time.",
                    style = TextStyle(
                        color = theme.textPrimary,
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold,
                        lineHeight = 26.sp,
                        textAlign = TextAlign.Center
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "All Home Page actions and services are currently disabled for Users and Family accounts while system maintenance is in progress.",
                    style = TextStyle(
                        color = theme.textSecondary,
                        fontSize = 13.sp,
                        lineHeight = 19.sp,
                        textAlign = TextAlign.Center
                    )
                )

                Spacer(modifier = Modifier.height(22.dp))

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(theme.surfaceGlass)
                        .border(1.dp, theme.borderGlass, RoundedCornerShape(10.dp))
                        .clickable { onSwitchAccount() }
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "CEO Panel Sign In",
                        style = TextStyle(
                            color = theme.textSecondary,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                }
            }
        }
        return
    }

    // Dynamic greeting based on current time
    val greeting = remember {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        when (hour) {
            in 5..11 -> "Good Morning"
            in 12..16 -> "Good Afternoon"
            in 17..21 -> "Good Evening"
            else -> "Good Night"
        }
    }

    // Filtered apps if searching; otherwise top 8-12 pinned/favorite apps for 2-3 rows
    val isSearching = searchQuery.isNotBlank()
    val searchResults = remember(allApps, searchQuery, userRole) {
        if (isSearching) {
            val matched = allApps.filter { SearchHelper.matchesQuery(it, searchQuery) }
            if (userRole == com.example.data.auth.UserRole.STANDARD) {
                matched.filter { com.example.data.auth.AuthCredentialsConfig.isAppAuthorizedForRole(it.id, com.example.data.auth.UserRole.STANDARD) }
            } else {
                matched
            }
        } else {
            emptyList()
        }
    }

    val visibleHomeApps = remember(allApps, userRole) {
        if (userRole == com.example.data.auth.UserRole.STANDARD) {
            allApps.filter {
                com.example.data.auth.AuthCredentialsConfig.isAppAuthorizedForRole(it.id, com.example.data.auth.UserRole.STANDARD)
            }
        } else {
            val pinned = allApps.filter { it.isPinnedHome }
            if (pinned.isNotEmpty()) {
                pinned.take(12)
            } else {
                allApps.take(12)
            }
        }
    }

    // Filter out apps that are already organized inside folders
    val unFolderedHomeApps = remember(visibleHomeApps, folders) {
        visibleHomeApps.filter { app ->
            folders.none { folder -> folder.appIds.contains(app.id) }
        }
    }

    val appsToDisplay = if (isSearching) searchResults else unFolderedHomeApps
    val searchSuggestions = listOf("Gemini", "ChatGPT", "Claude", "Maya OS", "REXLIFY", "YouTube", "Tasks")

    // Drag-and-drop & folder interaction states
    var draggedApp by remember { mutableStateOf<AppItem?>(null) }
    var dragCurrentOffset by remember { mutableStateOf(Offset.Zero) }
    var dragStartOffsetInRoot by remember { mutableStateOf(Offset.Zero) }
    val itemBounds = remember { HashMap<String, Rect>() }
    var hoveredTargetId by remember { mutableStateOf<String?>(null) }
    var activeFolderForDetail by remember { mutableStateOf<AppFolder?>(null) }
    var showCreateFolderDialog by remember { mutableStateOf(false) }
    var showWidgetCustomizeDialog by remember { mutableStateOf(false) }
    var showSearchBottomSheet by remember { mutableStateOf(false) }
    var manualApp1 by remember { mutableStateOf<AppItem?>(null) }
    var manualApp2 by remember { mutableStateOf<AppItem?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(4),
            modifier = Modifier.fillMaxSize(),
            flingBehavior = rememberChromeSmoothFlingBehavior(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 2.dp, bottom = 110.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
        // 1. Top Navigation Bar: Brand Identity (Click -> Official Profile), Profile ("R" -> Settings "Me") & Notification Bell
        item(span = { GridItemSpan(4) }) {
            MayaTopBar(
                userName = userName,
                unreadNotificationsCount = unreadNotificationsCount,
                onNotificationClick = onNotificationClick,
                onMayaConnectClick = onOpenOfficialProfile,
                onProfileClick = onProfileClick
            )
        }

        // 1B. Emergency Maintenance Status Cards on Home Page (Mode 1, Mode 2, or CEO Notice for Mode 3)
        if (emergencyState.activeMode != EmergencyMode.NORMAL) {
            item(span = { GridItemSpan(4) }) {
                when (emergencyState.activeMode) {
                    EmergencyMode.MAINTENANCE_FREEZE -> {
                        // Mode 1: Home Page Banner showing the application is under maintenance
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(20.dp))
                                .background(Color(0xFFEF4444).copy(alpha = 0.14f))
                                .border(1.5.dp, Color(0xFFEF4444).copy(alpha = 0.55f), RoundedCornerShape(20.dp))
                                .padding(16.dp)
                                .testTag("home_mode_1_maintenance_card")
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFEF4444).copy(alpha = 0.22f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Engineering,
                                        contentDescription = "Under Maintenance",
                                        tint = Color(0xFFEF4444),
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "This Application is Currently Under Maintenance",
                                        style = TextStyle(
                                            color = theme.textPrimary,
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = emergencyState.maintenanceReason.ifBlank {
                                            "System maintenance is active. Please wait until maintenance is completed."
                                        },
                                        style = TextStyle(
                                            color = theme.textSecondary,
                                            fontSize = 12.sp,
                                            lineHeight = 16.sp
                                        )
                                    )
                                }
                            }
                        }
                    }

                    EmergencyMode.SELECTIVE_FEATURE_CONTROL -> {
                        // Mode 2: Specific Maintenance & Custom Duration in Hours
                        val specificModules = emergencyState.getDisabledFeatureNames()
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(20.dp))
                                .background(Color(0xFFF59E0B).copy(alpha = 0.14f))
                                .border(1.5.dp, Color(0xFFF59E0B).copy(alpha = 0.55f), RoundedCornerShape(20.dp))
                                .padding(16.dp)
                                .testTag("home_mode_2_specific_maintenance_card")
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
                                                .size(38.dp)
                                                .clip(CircleShape)
                                                .background(Color(0xFFF59E0B).copy(alpha = 0.24f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Tune,
                                                contentDescription = "Specific Maintenance",
                                                tint = Color(0xFFF59E0B),
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text(
                                                text = "Specific Maintenance Active",
                                                style = TextStyle(
                                                    color = theme.textPrimary,
                                                    fontSize = 15.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            )
                                            Text(
                                                text = "Scheduled Duration: ${emergencyState.customDurationHours} Hour(s)",
                                                style = TextStyle(
                                                    color = Color(0xFFF59E0B),
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.SemiBold
                                                )
                                            )
                                        }
                                    }

                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Color(0xFFF59E0B).copy(alpha = 0.2f))
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = "${emergencyState.customDurationHours}h Window",
                                            style = TextStyle(
                                                color = Color(0xFFF59E0B),
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                Text(
                                    text = if (specificModules.isNotEmpty()) {
                                        "Modules Under Maintenance (${specificModules.size}): ${specificModules.joinToString(", ")}"
                                    } else {
                                        "Specific modules are being serviced for ${emergencyState.customDurationHours} hour(s). All other features remain active."
                                    },
                                    style = TextStyle(
                                        color = theme.textSecondary,
                                        fontSize = 12.sp,
                                        lineHeight = 17.sp
                                    )
                                )
                            }
                        }
                    }

                    EmergencyMode.HIGH_SECURITY_LOCKDOWN -> {
                        // CEO Panel exemption notice when Mode 3 is active
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color(0xFFDC2626).copy(alpha = 0.14f))
                                .border(1.dp, Color(0xFFDC2626).copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                                .padding(12.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Shield,
                                    contentDescription = null,
                                    tint = Color(0xFFEF4444),
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Mode 3 Active: Home Page locked for Users & Family (CEO Panel Exempt)",
                                    style = TextStyle(
                                        color = theme.textPrimary,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                )
                            }
                        }
                    }

                    EmergencyMode.NORMAL -> {}
                }
            }
        }

        // 2. Personalized Greeting & Summary Banner
        item(span = { GridItemSpan(4) }) {
            Column(modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)) {
                Text(
                    text = greeting,
                    style = TextStyle(
                        color = theme.textSecondary,
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Medium
                    )
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Hello, $userName",
                        style = TextStyle(
                            color = theme.textPrimary,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = (-0.4).sp
                        )
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Your summary for today",
                    style = TextStyle(
                        color = theme.textSecondary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Normal
                    )
                )
            }
        }

        // 3. Persistent Search Bar with Gemini AI Query Integration
        item(span = { GridItemSpan(4) }) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(22.dp))
                        .glassmorphic(
                            shape = RoundedCornerShape(22.dp),
                            backgroundColor = if (theme.isDark) Color(0xD9171C26) else Color(0xF2FFFFFF),
                            borderColor = theme.borderGlass,
                            borderWidth = 1.dp
                        )
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = theme.accentPrimary,
                            modifier = Modifier
                                .size(20.dp)
                                .clickable { showSearchBottomSheet = true }
                        )

                        Spacer(modifier = Modifier.width(10.dp))

                        BasicTextField(
                            value = searchQuery,
                            onValueChange = onSearchQueryChange,
                            modifier = Modifier.weight(1f).testTag("home_search_input"),
                            singleLine = true,
                            textStyle = TextStyle(
                                color = theme.textPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Normal
                            ),
                            cursorBrush = SolidColor(theme.accentPrimary),
                            decorationBox = { innerTextField ->
                                if (searchQuery.isEmpty()) {
                                    Text(
                                        text = "Search apps or ask Sara AI...",
                                        style = TextStyle(
                                            color = theme.textSecondary.copy(alpha = 0.65f),
                                            fontSize = 13.5.sp
                                        )
                                    )
                                }
                                innerTextField()
                            }
                        )

                        if (searchQuery.isNotEmpty()) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Clear",
                                tint = theme.textSecondary,
                                modifier = Modifier
                                    .size(18.dp)
                                    .clickable { onSearchQueryChange("") }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                        }

                        // Gemini AI Search Bottom Sheet Shortcut
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(theme.accentPrimary.copy(alpha = 0.15f))
                                .clickable { showSearchBottomSheet = true }
                                .testTag("home_gemini_search_btn"),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = "Sara AI Search Sheet",
                                tint = theme.accentPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(6.dp))

                        // Sara voiceassistant Shortcut Next to Search Bar
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(
                                    Brush.linearGradient(
                                        colors = listOf(Color(0xFFEC4899), Color(0xFF8B5CF6))
                                    )
                                )
                                .clickable { onOpenVoiceAssistant() }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                                .testTag("home_voice_search_btn"),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Mic,
                                contentDescription = "Sara voiceassistant",
                                tint = Color.White,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Sara",
                                style = TextStyle(
                                    color = Color.White,
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }
                }

                // Dropdown Floating Search Window under Search Box
                if (searchQuery.isNotBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(18.dp))
                            .glassmorphic(
                                shape = RoundedCornerShape(18.dp),
                                backgroundColor = if (theme.isDark) Color(0xF0131722) else Color(0xF5FFFFFF),
                                borderColor = theme.borderGlass,
                                borderWidth = 1.dp
                            )
                            .padding(14.dp)
                    ) {
                        Column {
                            // 1. "Ask Gemini" & Google Search Real-Time Action Card
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(
                                        Brush.linearGradient(
                                            colors = listOf(Color(0xFF1B72E8).copy(alpha = 0.22f), Color(0xFF8E24AA).copy(alpha = 0.22f))
                                        )
                                    )
                                    .border(1.dp, Color(0xFF1B72E8).copy(alpha = 0.4f), RoundedCornerShape(14.dp))
                                    .clickable {
                                        val queryLower = searchQuery.lowercase()
                                        val isRealTimeQuery = queryLower.contains("weather") ||
                                                queryLower.contains("date") ||
                                                queryLower.contains("time") ||
                                                queryLower.contains("sunrise") ||
                                                queryLower.contains("sunset") ||
                                                queryLower.contains("iftar") ||
                                                queryLower.contains("news") ||
                                                queryLower.contains("আবহাওয়া") ||
                                                queryLower.contains("তারিখ") ||
                                                queryLower.contains("সূর্যোদয়") ||
                                                queryLower.contains("সূর্যাস্ত") ||
                                                queryLower.contains("ইফতার")

                                        if (isRealTimeQuery) {
                                            try {
                                                val intent = Intent(
                                                    Intent.ACTION_VIEW,
                                                    android.net.Uri.parse("https://www.google.com/search?q=" + android.net.Uri.encode(searchQuery))
                                                )
                                                context.startActivity(intent)
                                            } catch (_: Exception) {
                                                onOpenChatAssistant(searchQuery)
                                            }
                                        } else {
                                            onOpenChatAssistant(searchQuery)
                                        }
                                    }
                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.AutoAwesome,
                                        contentDescription = null,
                                        tint = Color(0xFF38BDF8),
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Ask Gemini: \"$searchQuery\"",
                                        style = TextStyle(
                                            color = theme.textPrimary,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold
                                        ),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                    contentDescription = null,
                                    tint = Color(0xFF38BDF8),
                                    modifier = Modifier.size(15.dp)
                                )
                            }

                            // 2. Matching App Instant Results inside Window
                            if (searchResults.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = "Matching Applications (${searchResults.size})",
                                    style = TextStyle(
                                        color = theme.textSecondary,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                                Spacer(modifier = Modifier.height(6.dp))

                                FlowRow(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    searchResults.take(6).forEach { app ->
                                        Row(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(10.dp))
                                                .background(theme.surfaceGlass)
                                                .border(0.8.dp, theme.borderGlass, RoundedCornerShape(10.dp))
                                                .clickable { onAppClick(app) }
                                                .padding(horizontal = 10.dp, vertical = 6.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            AppLogoIcon(app = app, size = 22.dp, cornerRadius = 6.dp)
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = app.name,
                                                style = TextStyle(
                                                    color = theme.textPrimary,
                                                    fontSize = 11.5.sp,
                                                    fontWeight = FontWeight.SemiBold
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
        }

        // 4. Executive Dashboard
        if (!isSearching) {
            // Executive Glass Dashboard (App Branding on Left, Interactive Watch on Right)
            item(span = { GridItemSpan(4) }) {
                RealDashboardCard(
                    userName = userName,
                    recentlyUsedApps = recentlyUsedApps,
                    latestNote = latestNote,
                    unreadNotificationsCount = unreadNotificationsCount,
                    currentMusicTrack = currentMusicTrack,
                    isMusicPlaying = isMusicPlaying,
                    onMusicPlayPauseToggle = onMusicPlayPauseToggle,
                    onMusicNext = onMusicNext,
                    onAppClick = onAppClick,
                    onOpenNotepad = onOpenNotepad,
                    onOpenMusicPlayer = onOpenMusicPlayer,
                    onOpenVoiceAssistant = onOpenVoiceAssistant,
                    onOpenNotifications = onNotificationClick,
                    onOpenEcosystemDirectory = onOpenEcosystemDirectory,
                    currentClockStyle = currentClockStyle,
                    onClockStyleSelected = onClockStyleSelected
                )
            }
        }

        // 5. Point 5: "Your Apps" Section Header with App Drawer Trigger
        item(span = { GridItemSpan(4) }) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp, start = 2.dp, end = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isSearching) "Search Results (${appsToDisplay.size})" else "Your Apps",
                    style = TextStyle(
                        color = theme.textPrimary,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.2).sp
                    )
                )

                if (!isSearching) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Create Folder Shortcut Button
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(theme.accentPrimary.copy(alpha = 0.15f))
                                .border(1.dp, theme.accentPrimary.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                                .clickable {
                                    manualApp1 = null
                                    manualApp2 = null
                                    showCreateFolderDialog = true
                                }
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                                .testTag("home_create_folder_btn")
                        ) {
                            Text(
                                text = "+ Folder",
                                style = TextStyle(
                                    color = theme.accentPrimary,
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }

                        // App Drawer Trigger button
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(theme.accentPrimary.copy(alpha = 0.15f))
                                .border(1.dp, theme.accentPrimary.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                                .clickable { onOpenAppDrawer() }
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                                .testTag("home_app_drawer_btn")
                        ) {
                            Text(
                                text = "App Drawer",
                                style = TextStyle(
                                    color = theme.accentPrimary,
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }

                        Text(
                            text = "See All (${allApps.size}) →",
                            style = TextStyle(
                                color = theme.accentPrimary,
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { onSeeAllAppsClick() }
                                .padding(horizontal = 6.dp, vertical = 3.dp)
                                .testTag("home_see_all_apps")
                        )
                    }
                }
            }
        }

        // 6. Point 5: Render Created Folders (When not searching)
        if (!isSearching && folders.isNotEmpty()) {
            items(
                items = folders,
                key = { "folder_${it.id}" }
            ) { folder ->
                val folderApps = remember(folder, allApps) {
                    allApps.filter { folder.appIds.contains(it.id) }
                }
                val isHovered = hoveredTargetId == "folder_${folder.id}"

                Box(
                    modifier = Modifier
                        .onGloballyPositioned { coords ->
                            itemBounds["folder_${folder.id}"] = coords.boundsInRoot()
                        }
                ) {
                    FolderGridItem(
                        folder = folder,
                        apps = folderApps,
                        onClick = { activeFolderForDetail = folder },
                        isHoveredTarget = isHovered,
                        hapticEnabled = hapticEnabled
                    )
                }
            }
        }

        // 7. Point 5: 4-Column Applications Grid with Drag & Drop to Create/Add to Folders
        items(
            items = appsToDisplay,
            key = { it.id }
        ) { app ->
            val isHovered = hoveredTargetId == app.id
            val isBeingDragged = draggedApp?.id == app.id

            Box(
                modifier = Modifier
                    .onGloballyPositioned { coords ->
                        itemBounds[app.id] = coords.boundsInRoot()
                    }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .border(
                            width = if (isHovered) 2.dp else 0.dp,
                            color = if (isHovered) theme.accentPrimary else Color.Transparent,
                            shape = RoundedCornerShape(16.dp)
                        )
                        .background(
                            if (isHovered) theme.accentPrimary.copy(alpha = 0.28f)
                            else Color.Transparent,
                            RoundedCornerShape(16.dp)
                        )
                        .springPressEffect(
                            hapticEnabled = hapticEnabled,
                            scaleTarget = 0.90f,
                            onClick = { onAppClick(app) },
                            onLongClick = { onAppLongClick(app) }
                        )
                        .pointerInput(app.id) {
                            detectDragGesturesAfterLongPress(
                                onDragStart = {
                                    draggedApp = app
                                    val startPos = itemBounds[app.id]?.topLeft ?: Offset.Zero
                                    dragStartOffsetInRoot = startPos
                                    dragCurrentOffset = Offset.Zero
                                },
                                onDrag = { change, dragAmount ->
                                    change.consume()
                                    dragCurrentOffset += dragAmount
                                    val currentPointerInRoot = dragStartOffsetInRoot + dragCurrentOffset + Offset(35f, 35f)
                                    hoveredTargetId = itemBounds.entries.firstOrNull { (id, rect) ->
                                        id != app.id && rect.contains(currentPointerInRoot)
                                    }?.key
                                },
                                onDragEnd = {
                                    val target = hoveredTargetId
                                    val dragged = draggedApp
                                    if (target != null && dragged != null) {
                                        if (target.startsWith("folder_")) {
                                            val folderId = target.removePrefix("folder_")
                                            onAddAppToFolder(folderId, dragged)
                                        } else {
                                            val targetApp = allApps.find { it.id == target }
                                            if (targetApp != null) {
                                                onCreateFolder(dragged, targetApp)
                                            }
                                        }
                                    }
                                    draggedApp = null
                                    dragCurrentOffset = Offset.Zero
                                    hoveredTargetId = null
                                },
                                onDragCancel = {
                                    draggedApp = null
                                    dragCurrentOffset = Offset.Zero
                                    hoveredTargetId = null
                                }
                            )
                        }
                        .padding(horizontal = 2.dp, vertical = 4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    AppLogoIcon(
                        app = app,
                        size = 52.dp,
                        cornerRadius = 15.dp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = app.name,
                        style = TextStyle(
                            color = theme.textPrimary,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.Center
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        // Empty Search Results State
        if (isSearching && appsToDisplay.isEmpty()) {
            item(span = { GridItemSpan(4) }) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(theme.surfaceGlass)
                        .border(1.dp, theme.borderGlass, RoundedCornerShape(20.dp))
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.SearchOff,
                            contentDescription = null,
                            tint = theme.textSecondary,
                            modifier = Modifier.size(38.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "No apps found",
                            style = TextStyle(
                                color = theme.textPrimary,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Search installed apps or ask Gemini AI.",
                            style = TextStyle(
                                color = theme.textSecondary,
                                fontSize = 12.sp
                            )
                        )
                        Spacer(modifier = Modifier.height(14.dp))

                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            searchSuggestions.forEach { suggestion ->
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(theme.accentPrimary.copy(alpha = 0.12f))
                                        .border(0.8.dp, theme.borderGlass, RoundedCornerShape(8.dp))
                                        .clickable { onSearchQueryChange(suggestion) }
                                        .padding(horizontal = 10.dp, vertical = 5.dp)
                                ) {
                                    Text(
                                        text = suggestion,
                                        style = TextStyle(
                                            color = theme.textPrimary,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 7. Point 6: Three Horizontally Arranged Feature Buttons (Music, Notepad, Our Ecosystem)
        if (!isSearching) {
            item(span = { GridItemSpan(4) }) {
                UnifiedHomeTaskbar(
                    onOpenMusicPlayer = onOpenMusicPlayer,
                    onOpenNotepad = onOpenNotepad,
                    onOpenDailyTask = onOpenGoogleTasks,
                    modifier = Modifier.padding(top = 4.dp),
                    hapticEnabled = hapticEnabled
                )
            }

            // 8. Point 7: Additional Dedicated Action Buttons (Share App APK + rizwanr.ai.studio & Customer Care / Support)
            item(span = { GridItemSpan(4) }) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 2.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Share App Button (Shares full APK file + rizwanr.ai.studio link)
                        ActionPillButton(
                            title = "Share App (APK)",
                            subtitle = "rizwanr.ai.studio",
                            icon = Icons.Default.Share,
                            accentColor = Color(0xFF38BDF8),
                            onClick = {
                                com.example.ui.components.AppShareHelper.shareFullApkWithLink(context)
                            },
                            modifier = Modifier.weight(1f),
                            testTag = "home_share_app_btn",
                            hapticEnabled = hapticEnabled
                        )

                        // Customer Care / Support Button
                        ActionPillButton(
                            title = "Customer Care",
                            subtitle = "Help & Tickets",
                            icon = Icons.Default.SupportAgent,
                            accentColor = Color(0xFFA78BFA),
                            onClick = onOpenCustomerSupport,
                            modifier = Modifier.weight(1f),
                            testTag = "home_customer_care_btn",
                            hapticEnabled = hapticEnabled
                        )
                    }

                    // Dedicated rizwanr.ai.studio link option below Share App
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .glassmorphic(
                                shape = RoundedCornerShape(14.dp),
                                backgroundColor = theme.surfaceGlass,
                                borderColor = Color(0xFF38BDF8).copy(alpha = 0.35f),
                                borderWidth = 1.dp
                            )
                            .clickable {
                                com.example.ui.components.AppShareHelper.shareFullApkWithLink(context)
                            }
                            .padding(horizontal = 14.dp, vertical = 9.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "Share APK & Link",
                                tint = Color(0xFF38BDF8),
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Full APK + Official Link: ",
                                style = TextStyle(
                                    color = theme.textSecondary,
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            )
                            Text(
                                text = "rizwanr.ai.studio",
                                style = TextStyle(
                                    color = Color(0xFF38BDF8),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                        Text(
                            text = "Copy / Share",
                            style = TextStyle(
                                color = theme.accentPrimary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            ),
                            modifier = Modifier.clickable {
                                com.example.ui.components.AppShareHelper.copyOfficialLink(context)
                                com.example.ui.components.AppShareHelper.shareFullApkWithLink(context)
                            }
                        )
                    }
                }
            }

            // 9. Maya Connect Official Profile banner shortcut
            item(span = { GridItemSpan(4) }) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .glassmorphic(
                            shape = RoundedCornerShape(18.dp),
                            backgroundColor = theme.surfaceGlass,
                            borderColor = theme.borderGlass,
                            borderWidth = 1.dp
                        )
                        .clickable { onOpenOfficialProfile() }
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                        .testTag("home_official_profile_banner")
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
                                    .clip(CircleShape)
                                    .background(Color.White)
                                    .border(1.dp, theme.borderGlass, CircleShape),
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
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Maya Connect Official Profile",
                                    style = TextStyle(
                                        color = theme.textPrimary,
                                        fontSize = 13.5.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                                Text(
                                    text = "Founder: Rizwan RR • System Specs & Admin",
                                    style = TextStyle(
                                        color = theme.textSecondary,
                                        fontSize = 11.sp
                                    )
                                )
                            }
                        }
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "View Profile",
                            tint = theme.textSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }

    // Floating Draggable App Ghost
    if (draggedApp != null) {
        val dragged = draggedApp!!
        val position = dragStartOffsetInRoot + dragCurrentOffset
        Box(
            modifier = Modifier
                .offset { IntOffset(position.x.roundToInt(), position.y.roundToInt()) }
                .size(68.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(theme.accentPrimary.copy(alpha = 0.35f))
                .border(2.dp, theme.accentPrimary, RoundedCornerShape(18.dp))
                .padding(6.dp),
            contentAlignment = Alignment.Center
        ) {
            AppLogoIcon(app = dragged, size = 48.dp, cornerRadius = 14.dp)
        }
    }

    // Folder Detail Dialog (Launch, Rename, Ungroup/Dissolve)
    activeFolderForDetail?.let { folder ->
        val folderApps = allApps.filter { folder.appIds.contains(it.id) }
        FolderDetailDialog(
            folder = folder,
            apps = folderApps,
            onDismiss = { activeFolderForDetail = null },
            onAppClick = { app ->
                activeFolderForDetail = null
                onAppClick(app)
            },
            onRenameFolder = { newName ->
                onRenameFolder(folder.id, newName)
                activeFolderForDetail = folder.copy(name = newName)
            },
            onRemoveAppFromFolder = { app ->
                onRemoveAppFromFolder(folder.id, app.id)
                val remaining = folder.appIds.filter { it != app.id }
                if (remaining.isEmpty()) {
                    activeFolderForDetail = null
                } else {
                    activeFolderForDetail = folder.copy(appIds = remaining)
                }
            },
            onDissolveFolder = {
                onDissolveFolder(folder.id)
                activeFolderForDetail = null
            },
            hapticEnabled = hapticEnabled
        )
    }

    // Search Bottom Sheet Dialog Modal
    if (showSearchBottomSheet) {
        com.example.ui.components.SearchBottomSheet(
            initialQuery = searchQuery,
            allApps = allApps,
            onDismiss = { showSearchBottomSheet = false },
            onAppClick = onAppClick,
            onOpenVoiceAssistant = onOpenVoiceAssistant,
            onOpenChatAssistant = onOpenChatAssistant
        )
    }

    // Manual Folder Creation / Pairing Dialog
    if (showCreateFolderDialog) {
        Dialog(onDismissRequest = { showCreateFolderDialog = false }) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .clip(RoundedCornerShape(24.dp))
                    .background(if (theme.isDark) Color(0xF2121622) else Color(0xF8FFFFFF))
                    .border(1.2.dp, theme.borderGlass, RoundedCornerShape(24.dp))
                    .padding(20.dp)
                    .testTag("create_folder_dialog")
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Create App Folder",
                            style = TextStyle(
                                color = theme.textPrimary,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = theme.textSecondary,
                            modifier = Modifier
                                .size(20.dp)
                                .clickable { showCreateFolderDialog = false }
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Select two apps to automatically merge and name a folder:",
                        style = TextStyle(color = theme.textSecondary, fontSize = 12.sp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "First App: ${manualApp1?.name ?: "None selected"}",
                        style = TextStyle(
                            color = if (manualApp1 != null) theme.accentPrimary else theme.textSecondary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Second App: ${manualApp2?.name ?: "None selected"}",
                        style = TextStyle(
                            color = if (manualApp2 != null) theme.accentPrimary else theme.textSecondary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    )

                    if (manualApp1 != null && manualApp2 != null) {
                        val previewFolder = remember(manualApp1, manualApp2) {
                            AppFolder.createWithAutoName(manualApp1!!, manualApp2!!)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Auto-generated Name: \"${previewFolder.name}\"",
                            style = TextStyle(
                                color = Color(0xFF10B981),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    LazyVerticalGrid(
                        columns = GridCells.Fixed(4),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp),
                        flingBehavior = rememberChromeSmoothFlingBehavior(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(unFolderedHomeApps) { app ->
                            val isSelected = manualApp1?.id == app.id || manualApp2?.id == app.id
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isSelected) theme.accentPrimary.copy(alpha = 0.25f) else theme.surfaceGlass)
                                    .border(
                                        if (isSelected) 1.5.dp else 0.8.dp,
                                        if (isSelected) theme.accentPrimary else theme.borderGlass,
                                        RoundedCornerShape(12.dp)
                                    )
                                    .clickable {
                                        if (manualApp1 == null) {
                                            manualApp1 = app
                                        } else if (manualApp1?.id == app.id) {
                                            manualApp1 = null
                                        } else if (manualApp2 == null) {
                                            manualApp2 = app
                                        } else if (manualApp2?.id == app.id) {
                                            manualApp2 = null
                                        } else {
                                            manualApp2 = app
                                        }
                                    }
                                    .padding(4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    AppLogoIcon(app = app, size = 32.dp, cornerRadius = 8.dp)
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = app.name,
                                        style = TextStyle(color = theme.textPrimary, fontSize = 9.sp),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (manualApp1 != null && manualApp2 != null) theme.accentPrimary
                                    else theme.accentPrimary.copy(alpha = 0.35f)
                                )
                                .clickable(enabled = manualApp1 != null && manualApp2 != null) {
                                    if (manualApp1 != null && manualApp2 != null) {
                                        onCreateFolder(manualApp1!!, manualApp2!!)
                                        showCreateFolderDialog = false
                                        manualApp1 = null
                                        manualApp2 = null
                                    }
                                }
                                .padding(horizontal = 16.dp, vertical = 9.dp)
                                .testTag("confirm_create_folder_btn")
                        ) {
                            Text(
                                text = "Create Folder",
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
    }

    // Customize Widgets Dialog
    if (showWidgetCustomizeDialog) {
        CustomizeWidgetsDialog(
            currentWidgets = activeWidgets,
            onSaveWidgets = onSaveWidgets,
            onDismiss = { showWidgetCustomizeDialog = false }
        )
    }
}
}

/**
 * Point 6: Translucent Feature Button (Music, Notepad, Our Ecosystem)
 */
@Composable
private fun TranslucentFeatureButton(
    title: String,
    icon: ImageVector,
    accentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    testTag: String = "translucent_feature_btn",
    hapticEnabled: Boolean = true
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
            .springPressEffect(
                hapticEnabled = hapticEnabled,
                scaleTarget = 0.94f,
                onClick = onClick
            )
            .padding(vertical = 12.dp, horizontal = 8.dp)
            .testTag(testTag),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(accentColor.copy(alpha = 0.18f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = accentColor,
                    modifier = Modifier.size(19.dp)
                )
            }
            Spacer(modifier = Modifier.height(7.dp))
            Text(
                text = title,
                style = TextStyle(
                    color = theme.textPrimary,
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/**
 * Point 7: Action Pill Button (Share App & Customer Care)
 */
@Composable
private fun ActionPillButton(
    title: String,
    subtitle: String,
    icon: ImageVector,
    accentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    testTag: String = "action_pill_btn",
    hapticEnabled: Boolean = true
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
            .springPressEffect(
                hapticEnabled = hapticEnabled,
                scaleTarget = 0.95f,
                onClick = onClick
            )
            .padding(horizontal = 14.dp, vertical = 11.dp)
            .testTag(testTag)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(accentColor.copy(alpha = 0.16f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = accentColor,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = title,
                    style = TextStyle(
                        color = theme.textPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = subtitle,
                    style = TextStyle(
                        color = theme.textSecondary,
                        fontSize = 10.5.sp
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
