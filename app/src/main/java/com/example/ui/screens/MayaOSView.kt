package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import com.example.ui.components.rememberChromeSmoothFlingBehavior
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.BatteryFull
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Wallpaper
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.config.CentralAppConfig
import com.example.data.model.AppCategory
import com.example.data.model.AppItem
import com.example.data.model.AppRegistry
import com.example.data.model.AppStatus
import com.example.ui.components.AppLogoIcon
import com.example.ui.components.GlassButton
import com.example.ui.components.glassmorphic
import com.example.ui.components.springPressEffect
import com.example.ui.theme.MayaColorScheme
import com.example.ui.theme.MayaTheme
import com.example.ui.theme.MayaThemeMode
import java.net.URLEncoder
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.delay

/**
 * MayaOSView
 * Full-screen container for the OS-style environment, supporting navigation and history tracking
 * for the internal ecosystem apps (REXLIFY, NeuraX, NooraX, SIFRA, RODP, etc.).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MayaOSView(
    userName: String,
    allApps: List<AppItem>,
    currentThemeMode: MayaThemeMode,
    wallpaper: String,
    gridSize: Int,
    onThemeModeSelected: (MayaThemeMode) -> Unit,
    onWallpaperSelected: (String) -> Unit,
    onGridSizeSelected: (Int) -> Unit,
    onUserNameChange: (String) -> Unit,
    onExitMayaOs: () -> Unit,
    onConfigureApp: (AppItem) -> Unit = {},
    customWallpaperUri: String? = null,
    customWallpapersList: List<String> = emptyList(),
    onCustomWallpaperSelected: (String) -> Unit = {},
    onClearCustomWallpaper: () -> Unit = {},
    onAddCustomWallpaper: (android.net.Uri) -> Unit = {},
    hapticEnabled: Boolean = true
) {
    val theme = MayaTheme.colors
    val context = LocalContext.current

    // Internal navigation state & history tracking for ecosystem apps
    var activeInternalApp by remember { mutableStateOf<AppItem?>(null) }
    val internalAppHistory = remember { mutableStateListOf<AppItem>() }

    var isAppDrawerOpen by remember { mutableStateOf(false) }
    var showCustomizationSheet by remember { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf(false) }
    var showRecentsDialog by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var drawerSearchQuery by remember { mutableStateOf("") }

    // Live clock for OS status bar & At-a-glance
    var currentTimeString by remember {
        mutableStateOf(SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date()))
    }
    var currentDateString by remember {
        mutableStateOf(SimpleDateFormat("EEEE, MMM d", Locale.getDefault()).format(Date()))
    }

    LaunchedEffect(Unit) {
        while (true) {
            val now = Date()
            currentTimeString = SimpleDateFormat("HH:mm", Locale.getDefault()).format(now)
            currentDateString = SimpleDateFormat("EEEE, MMM d", Locale.getDefault()).format(now)
            delay(10000)
        }
    }

    // Comprehensive Android Back Navigation Handler
    BackHandler {
        if (activeInternalApp != null) {
            // Close active ecosystem app, return to Maya OS home
            activeInternalApp = null
        } else if (showSettingsDialog) {
            showSettingsDialog = false
        } else if (showCustomizationSheet) {
            showCustomizationSheet = false
        } else if (showRecentsDialog) {
            showRecentsDialog = false
        } else if (isAppDrawerOpen) {
            isAppDrawerOpen = false
        } else {
            // Exit Maya OS, return to Maya Connect dashboard
            onExitMayaOs()
        }
    }

    // Launch an application within Maya OS container with navigation & history tracking
    fun launchInternalApp(app: AppItem) {
        if (app.id == "maya_connect") {
            onExitMayaOs()
            return
        }
        if (app.id == "maya_settings") {
            showSettingsDialog = true
            return
        }
        // Track history
        internalAppHistory.removeAll { it.id == app.id }
        internalAppHistory.add(0, app)
        activeInternalApp = app
    }

    // Curated apps using centralized AppRegistry + system apps
    val registryApps = remember {
        AppRegistry.toAppItems()
    }

    val mayaConnectApp = remember {
        AppItem(
            id = "maya_connect",
            name = "Maya Connect",
            tagline = "Unified Ecosystem Hub",
            fullDescription = "Return to Maya Connect main dashboard and workspace hub.",
            url = "maya://connect",
            category = AppCategory.PRODUCTIVITY,
            status = AppStatus.AVAILABLE,
            iconKey = "MAYA_CONNECT",
            primaryColorHex = 0xFF1E293B,
            secondaryColorHex = 0xFF0F172A
        )
    }

    val phoneApp = remember {
        AppItem(
            id = "phone_dialer",
            name = "Phone",
            tagline = "Dialer & Contacts",
            fullDescription = "Place calls and access your contacts.",
            url = "tel:",
            category = AppCategory.PRODUCTIVITY,
            status = AppStatus.AVAILABLE,
            iconKey = "DIALER",
            primaryColorHex = 0xFF10B981,
            secondaryColorHex = 0xFF059669
        )
    }

    val chromeApp = remember {
        AppItem(
            id = "chrome_browser",
            name = "Chrome",
            tagline = "Web Browser",
            fullDescription = "Explore the web securely.",
            url = "https://www.google.com",
            category = AppCategory.PRODUCTIVITY,
            status = AppStatus.AVAILABLE,
            iconKey = "CHROME",
            primaryColorHex = 0xFF3B82F6,
            secondaryColorHex = 0xFF1D4ED8
        )
    }

    val osSettingsApp = remember {
        AppItem(
            id = "maya_settings",
            name = "Settings",
            tagline = "Maya OS Settings",
            fullDescription = "Manage wallpaper, theme, layout, and personal preferences.",
            url = "maya://settings",
            category = AppCategory.MAYA_DIAGNOSTIC,
            status = AppStatus.AVAILABLE,
            iconKey = "SETTINGS",
            primaryColorHex = 0xFF475569,
            secondaryColorHex = 0xFF334155
        )
    }

    // Merged app list ensuring AppRegistry apps (REXLIFY, NeuraX, NooraX, SIFRA, RODP) are always present
    val mergedAllApps = remember(allApps, registryApps) {
        val nonOsApps = allApps.filter { it.id != "maya_os" }
        val ids = nonOsApps.map { it.id }.toSet()
        val missingRegistry = registryApps.filter { it.id !in ids }
        listOf(mayaConnectApp) + nonOsApps + missingRegistry
    }

    // Desktop Grid Apps
    val desktopGridApps = remember(mergedAllApps) {
        listOf(mayaConnectApp) + mergedAllApps.filter { it.id != "maya_connect" }.take(11)
    }

    // Dock Apps
    val dockApps = remember(mergedAllApps) {
        val ytApp = mergedAllApps.find { it.id == "youtube" } ?: AppItem(
            id = "youtube",
            name = "YouTube",
            tagline = "Videos",
            fullDescription = "YouTube streaming",
            url = CentralAppConfig.YOUTUBE_URL,
            category = AppCategory.CREATIVE_STUDIO,
            iconKey = "YOUTUBE",
            primaryColorHex = 0xFFDC2626,
            secondaryColorHex = 0xFF991B1B
        )
        listOf(phoneApp, chromeApp, mayaConnectApp, ytApp, osSettingsApp)
    }

    // Execute Google Search query
    fun performSearch(query: String) {
        val trimmed = query.trim()
        if (trimmed.isEmpty()) return
        val encoded = try {
            URLEncoder.encode(trimmed, "UTF-8")
        } catch (_: Exception) {
            trimmed
        }
        val searchApp = AppItem(
            id = "google_search_active",
            name = "Google: $trimmed",
            tagline = "Search Results",
            fullDescription = "Google search query results",
            url = "https://www.google.com/search?q=$encoded",
            category = AppCategory.PRODUCTIVITY,
            iconKey = "GOOGLE_SEARCH"
        )
        launchInternalApp(searchApp)
    }

    // Wallpaper background
    val wallpaperBrush = remember(wallpaper, theme) {
        createWallpaperBrush(wallpaper, theme)
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .then(
                if (!isAppDrawerOpen && activeInternalApp == null) {
                    Modifier.pointerInput(Unit) {
                        detectTapGestures(
                            onLongPress = {
                                // Hold 1 second opens wallpaper settings
                                showCustomizationSheet = true
                            }
                        )
                    }
                } else {
                    Modifier
                }
            )
    ) {
        // Desktop Wallpaper rendering (Custom Storage Image or System Preset Brush)
        if (customWallpaperUri != null) {
            AsyncImage(
                model = customWallpaperUri,
                contentDescription = "Custom Storage Wallpaper",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = if (theme.isDark) 0.35f else 0.15f))
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(wallpaperBrush)
            )
        }

        val screenHeight = maxHeight
        val isCompactHeight = screenHeight < 680.dp

        // Full Screen Container for Maya OS
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            // 1. TOP OS STATUS BAR & RECENT APPS BUTTON
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Clock & Return pill
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(theme.surfaceGlass.copy(alpha = 0.5f))
                        .clickable { onExitMayaOs() }
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Return to Maya Connect",
                        tint = theme.textPrimary,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Maya Connect",
                        style = TextStyle(
                            color = theme.textPrimary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "•  $currentTimeString",
                        style = TextStyle(
                            color = theme.textSecondary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    )
                }

                // System indicators & Recent Apps history shortcut
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (internalAppHistory.isNotEmpty()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(theme.accentPrimary.copy(alpha = 0.15f))
                                .clickable { showRecentsDialog = true }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.History,
                                contentDescription = "Recent Apps",
                                tint = theme.accentPrimary,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${internalAppHistory.size}",
                                style = TextStyle(
                                    color = theme.accentPrimary,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }

                    Icon(
                        imageVector = Icons.Default.Wifi,
                        contentDescription = "Wi-Fi",
                        tint = theme.textPrimary,
                        modifier = Modifier.size(15.dp)
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.BatteryFull,
                            contentDescription = "Battery",
                            tint = theme.textPrimary,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = "98%",
                            style = TextStyle(
                                color = theme.textPrimary,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // 2. AT-A-GLANCE WIDGET & USER PERSONALIZATION
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 22.dp, vertical = if (isCompactHeight) 4.dp else 8.dp)
            ) {
                Text(
                    text = currentDateString,
                    style = TextStyle(
                        color = theme.textPrimary,
                        fontSize = if (isCompactHeight) 22.sp else 26.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.5).sp
                    )
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.WbSunny,
                        contentDescription = "Weather",
                        tint = theme.accentPrimary,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = "28°C  Partly Sunny",
                        style = TextStyle(
                            color = theme.textSecondary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    )
                    Text(
                        text = "•",
                        style = TextStyle(color = theme.textSecondary, fontSize = 10.sp)
                    )
                    Text(
                        text = "$userName's Maya OS",
                        style = TextStyle(
                            color = theme.textSecondary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(if (isCompactHeight) 6.dp else 10.dp))

            // 3. FUNCTIONAL GOOGLE SEARCH BOX
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(CircleShape)
                        .background(
                            if (theme.isDark) Color(0xD91F2430) else Color(0xF2FFFFFF)
                        )
                        .border(1.dp, theme.borderGlass, CircleShape)
                        .padding(horizontal = 14.dp, vertical = 9.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(theme.accentPrimary.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "G",
                            style = TextStyle(
                                color = theme.accentPrimary,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    BasicTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        textStyle = TextStyle(
                            color = theme.textPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Normal
                        ),
                        cursorBrush = SolidColor(theme.accentPrimary),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(onSearch = { performSearch(searchQuery) }),
                        decorationBox = { innerTextField ->
                            if (searchQuery.isEmpty()) {
                                Text(
                                    text = "Search or type URL...",
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
                                .clickable { searchQuery = "" }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }

                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = "Voice Search",
                        tint = theme.accentPrimary,
                        modifier = Modifier
                            .size(20.dp)
                            .clickable {
                                Toast.makeText(context, "Voice Search listening...", Toast.LENGTH_SHORT).show()
                            }
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = theme.textPrimary,
                        modifier = Modifier
                            .size(20.dp)
                            .clickable { performSearch(searchQuery) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(if (isCompactHeight) 8.dp else 14.dp))

            // 4. HOME SCREEN APP GRID
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onLongPress = {
                                showCustomizationSheet = true
                            }
                        )
                    }
            ) {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(gridSize),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 14.dp),
                    flingBehavior = rememberChromeSmoothFlingBehavior(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(if (isCompactHeight) 10.dp else 16.dp),
                    contentPadding = PaddingValues(vertical = 4.dp)
                ) {
                    items(items = desktopGridApps, key = { "grid_" + it.id }) { app ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .springPressEffect(
                                    hapticEnabled = hapticEnabled,
                                    scaleTarget = 0.90f,
                                    onClick = { launchInternalApp(app) }
                                )
                                .padding(horizontal = 2.dp, vertical = 4.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            AppLogoIcon(
                                app = app,
                                size = if (gridSize == 5 || isCompactHeight) 48.dp else 54.dp,
                                cornerRadius = if (gridSize == 5 || isCompactHeight) 13.dp else 15.dp
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
            }

            // 5. SWIPE UP DRAWER HANDLE
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isAppDrawerOpen = true }
                    .pointerInput(Unit) {
                        detectVerticalDragGestures { _, dragAmount ->
                            if (dragAmount < -15f && !isAppDrawerOpen && activeInternalApp == null) {
                                isAppDrawerOpen = true
                            }
                        }
                    }
                    .padding(vertical = 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowUpward,
                    contentDescription = "Swipe up for all apps",
                    tint = theme.textSecondary.copy(alpha = 0.7f),
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = "Swipe up for All Apps",
                    style = TextStyle(
                        color = theme.textSecondary.copy(alpha = 0.8f),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium
                    )
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // 6. FLOATING PREMIUM DOCK
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp, vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(26.dp))
                        .glassmorphic(
                            shape = RoundedCornerShape(26.dp),
                            backgroundColor = if (theme.isDark) Color(0xD9151922) else Color(0xEBFFFFFF),
                            borderColor = theme.borderGlass,
                            borderWidth = 1.dp
                        )
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    dockApps.forEach { dockApp ->
                        Box(
                            modifier = Modifier
                                .springPressEffect(
                                    hapticEnabled = hapticEnabled,
                                    scaleTarget = 0.90f,
                                    onClick = {
                                        if (dockApp.id == "phone_dialer") {
                                            try {
                                                val intent = Intent(Intent.ACTION_DIAL)
                                                context.startActivity(intent)
                                            } catch (_: Exception) {
                                                Toast.makeText(context, "Dialer opened", Toast.LENGTH_SHORT).show()
                                            }
                                        } else {
                                            launchInternalApp(dockApp)
                                        }
                                    }
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            AppLogoIcon(
                                app = dockApp,
                                size = 44.dp,
                                cornerRadius = 12.dp
                            )
                        }
                    }
                }
            }

            // Bottom navigation gesture bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .width(72.dp)
                        .height(4.dp)
                        .clip(CircleShape)
                        .background(theme.textSecondary.copy(alpha = 0.35f))
                        .clickable { onExitMayaOs() }
                )
            }
        }

        // ==========================================
        // 7. INTERNAL ECOSYSTEM APP VIEWER CONTAINER
        // ==========================================
        AnimatedVisibility(
            visible = activeInternalApp != null,
            enter = slideInVertically(initialOffsetY = { it / 4 }, animationSpec = tween(260, easing = FastOutSlowInEasing)) + fadeIn() + scaleIn(initialScale = 0.94f),
            exit = slideOutVertically(targetOffsetY = { it / 4 }, animationSpec = tween(220, easing = FastOutSlowInEasing)) + fadeOut() + scaleOut(targetScale = 0.96f)
        ) {
            activeInternalApp?.let { app ->
                InAppBrowserScreen(
                    app = app,
                    onClose = { activeInternalApp = null },
                    onConfigureApp = { appToConfig ->
                        activeInternalApp = null
                        onConfigureApp(appToConfig)
                    }
                )
            }
        }

        // ==========================================
        // 8. APP DRAWER OVERLAY
        // ==========================================
        AnimatedVisibility(
            visible = isAppDrawerOpen && activeInternalApp == null,
            enter = slideInVertically(initialOffsetY = { it }, animationSpec = tween(280, easing = FastOutSlowInEasing)) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }, animationSpec = tween(240, easing = FastOutSlowInEasing)) + fadeOut()
        ) {
            val drawerFiltered = remember(mergedAllApps, drawerSearchQuery) {
                if (drawerSearchQuery.isBlank()) mergedAllApps
                else mergedAllApps.filter {
                    it.name.contains(drawerSearchQuery, ignoreCase = true) ||
                            it.tagline.contains(drawerSearchQuery, ignoreCase = true) ||
                            it.category.displayName.contains(drawerSearchQuery, ignoreCase = true)
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(if (theme.isDark) Color(0xF70D0F14) else Color(0xFAFFFFFF))
                    .statusBarsPadding()
                    .navigationBarsPadding()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 18.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable { isAppDrawerOpen = false }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close App Drawer",
                                tint = theme.textPrimary,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "All Applications",
                                style = TextStyle(
                                    color = theme.textPrimary,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }

                        Text(
                            text = "${drawerFiltered.size} Apps",
                            style = TextStyle(
                                color = theme.textSecondary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        )
                    }

                    // Search Bar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(CircleShape)
                            .background(theme.surfaceGlass)
                            .border(1.dp, theme.borderGlass, CircleShape)
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            tint = theme.textSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        BasicTextField(
                            value = drawerSearchQuery,
                            onValueChange = { drawerSearchQuery = it },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            textStyle = TextStyle(color = theme.textPrimary, fontSize = 14.sp),
                            cursorBrush = SolidColor(theme.accentPrimary),
                            decorationBox = { inner ->
                                if (drawerSearchQuery.isEmpty()) {
                                    Text(
                                        text = "Search all apps...",
                                        style = TextStyle(
                                            color = theme.textSecondary.copy(alpha = 0.6f),
                                            fontSize = 13.5.sp
                                        )
                                    )
                                }
                                inner()
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    LazyVerticalGrid(
                        columns = GridCells.Fixed(4),
                        modifier = Modifier.weight(1f),
                        flingBehavior = rememberChromeSmoothFlingBehavior(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        contentPadding = PaddingValues(bottom = 24.dp)
                    ) {
                        items(items = drawerFiltered, key = { "drawer_all_" + it.id }) { app ->
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .springPressEffect(
                                        hapticEnabled = hapticEnabled,
                                        scaleTarget = 0.90f,
                                        onClick = {
                                            isAppDrawerOpen = false
                                            launchInternalApp(app)
                                        }
                                    )
                                    .padding(horizontal = 2.dp, vertical = 4.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                AppLogoIcon(app = app, size = 52.dp, cornerRadius = 14.dp)
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
                }
            }
        }

        // ==========================================
        // 9. RECENT APPS HISTORY SWITCHER
        // ==========================================
        if (showRecentsDialog && activeInternalApp == null) {
            Dialog(onDismissRequest = { showRecentsDialog = false }) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth(0.92f)
                        .clip(RoundedCornerShape(20.dp))
                        .border(1.dp, theme.borderGlass, RoundedCornerShape(20.dp)),
                    color = if (theme.isDark) Color(0xFF131620) else Color.White,
                    contentColor = theme.textPrimary
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.History,
                                    contentDescription = null,
                                    tint = theme.accentPrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Recent Applications (${internalAppHistory.size})",
                                    style = TextStyle(
                                        color = theme.textPrimary,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = theme.textSecondary,
                                modifier = Modifier
                                    .size(18.dp)
                                    .clickable { showRecentsDialog = false }
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        if (internalAppHistory.isEmpty()) {
                            Text(
                                text = "No applications opened yet in this Maya OS session.",
                                style = TextStyle(color = theme.textSecondary, fontSize = 12.sp)
                            )
                        } else {
                            LazyRow(
                                flingBehavior = rememberChromeSmoothFlingBehavior(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                contentPadding = PaddingValues(vertical = 6.dp)
                            ) {
                                items(items = internalAppHistory, key = { "recent_hist_" + it.id }) { app ->
                                    Column(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(14.dp))
                                            .background(theme.surfaceGlass)
                                            .border(1.dp, theme.borderGlass, RoundedCornerShape(14.dp))
                                            .clickable {
                                                showRecentsDialog = false
                                                launchInternalApp(app)
                                            }
                                            .padding(12.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        AppLogoIcon(app = app, size = 42.dp, cornerRadius = 12.dp)
                                        Spacer(modifier = Modifier.height(6.dp))
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

        // ==========================================
        // 10. LONG PRESS CUSTOMIZATION SHEET
        // ==========================================
        if (showCustomizationSheet && activeInternalApp == null) {
            ModalBottomSheet(
                onDismissRequest = { showCustomizationSheet = false },
                sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
                containerColor = if (theme.isDark) Color(0xFF131620) else Color(0xFFFFFFFF),
                contentColor = theme.textPrimary
            ) {
                val photoPickerLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.PickVisualMedia(),
                    onResult = { uri ->
                        if (uri != null) {
                            onAddCustomWallpaper(uri)
                        }
                    }
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 22.dp, vertical = 10.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Wallpaper Settings",
                                style = TextStyle(
                                    color = theme.textPrimary,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            Text(
                                text = "Customize desktop background & storage textures",
                                style = TextStyle(
                                    color = theme.textSecondary,
                                    fontSize = 11.sp
                                )
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = theme.textSecondary,
                            modifier = Modifier
                                .size(20.dp)
                                .clickable { showCustomizationSheet = false }
                        )
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // SECTION 1: SYSTEM PRESET WALLPAPERS
                    Text(
                        text = "SECTION 1 — SYSTEM PRESET WALLPAPERS",
                        style = TextStyle(
                            color = theme.accentPrimary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.8.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(
                            "dynamic_abstract" to "Abstract",
                            "deep_nebula" to "Nebula",
                            "glass_prism" to "Prism",
                            "minimal_dark" to "Charcoal"
                        ).forEach { (key, label) ->
                            val isSelected = (wallpaper == key && customWallpaperUri == null)
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isSelected) theme.accentPrimary.copy(alpha = 0.2f) else theme.surfaceGlass)
                                    .border(
                                        if (isSelected) 1.5.dp else 1.dp,
                                        if (isSelected) theme.accentPrimary else theme.borderGlass,
                                        RoundedCornerShape(12.dp)
                                    )
                                    .clickable {
                                        onClearCustomWallpaper()
                                        onWallpaperSelected(key)
                                    }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    style = TextStyle(
                                        color = if (isSelected) theme.accentPrimary else theme.textSecondary,
                                        fontSize = 11.5.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                    )
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // SECTION 2: CUSTOM STORAGE WALLPAPERS
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "STORAGE SECTION 2 — CUSTOM WALLPAPERS",
                            style = TextStyle(
                                color = Color(0xFF10B981),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.8.sp
                            )
                        )

                        if (customWallpaperUri != null) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color(0x3310B981))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "Custom Active",
                                    style = TextStyle(
                                        color = Color(0xFF10B981),
                                        fontSize = 9.5.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Storage Action Buttons: Import & Clear
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Import from Device Storage
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(14.dp))
                                .background(theme.accentPrimary.copy(alpha = 0.15f))
                                .border(1.dp, theme.accentPrimary.copy(alpha = 0.4f), RoundedCornerShape(14.dp))
                            .clickable {
                                try {
                                    photoPickerLauncher.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                } catch (_: Exception) {
                                    Toast.makeText(context, "Storage picker launched", Toast.LENGTH_SHORT).show()
                                }
                            }
                            .padding(vertical = 11.dp, horizontal = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.AddPhotoAlternate,
                                    contentDescription = "Import from Storage",
                                    tint = theme.accentPrimary,
                                    modifier = Modifier.size(17.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Import Wallpaper",
                                    style = TextStyle(
                                        color = theme.accentPrimary,
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                        }

                        // Clear Custom Wallpaper Button
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(14.dp))
                                .background(
                                    if (customWallpaperUri != null) Color(0x22EF4444)
                                    else theme.surfaceGlass
                                )
                                .border(
                                    1.dp,
                                    if (customWallpaperUri != null) Color(0xFFEF4444).copy(alpha = 0.5f)
                                    else theme.borderGlass,
                                    RoundedCornerShape(14.dp)
                                )
                                .clickable {
                                    onClearCustomWallpaper()
                                    Toast.makeText(context, "Custom wallpaper cleared", Toast.LENGTH_SHORT).show()
                                }
                                .padding(vertical = 11.dp, horizontal = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Clear Custom Wallpaper",
                                    tint = if (customWallpaperUri != null) Color(0xFFEF4444) else theme.textSecondary,
                                    modifier = Modifier.size(17.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Clear Wallpaper",
                                    style = TextStyle(
                                        color = if (customWallpaperUri != null) Color(0xFFEF4444) else theme.textSecondary,
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                        }
                    }

                    // Display stored wallpapers in Section 2
                    if (customWallpapersList.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Imported Wallpapers in Storage (${customWallpapersList.size}):",
                            style = TextStyle(
                                color = theme.textSecondary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        LazyRow(
                            flingBehavior = rememberChromeSmoothFlingBehavior(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            contentPadding = PaddingValues(vertical = 4.dp)
                        ) {
                            items(items = customWallpapersList) { path ->
                                val isCurSelected = customWallpaperUri == path
                                Box(
                                    modifier = Modifier
                                        .size(72.dp, 96.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .border(
                                            if (isCurSelected) 2.dp else 1.dp,
                                            if (isCurSelected) Color(0xFF10B981) else theme.borderGlass,
                                            RoundedCornerShape(10.dp)
                                        )
                                        .clickable { onCustomWallpaperSelected(path) }
                                ) {
                                    AsyncImage(
                                        model = path,
                                        contentDescription = "Custom wallpaper thumbnail",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    Text(
                        text = "THEME",
                        style = TextStyle(
                            color = theme.accentPrimary,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.8.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        MayaThemeMode.entries.forEach { mode ->
                            val isSelected = currentThemeMode == mode
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isSelected) theme.accentPrimary.copy(alpha = 0.2f) else theme.surfaceGlass)
                                    .border(
                                        if (isSelected) 1.5.dp else 1.dp,
                                        if (isSelected) theme.accentPrimary else theme.borderGlass,
                                        RoundedCornerShape(12.dp)
                                    )
                                    .clickable { onThemeModeSelected(mode) }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = mode.title.replace(" Mode", ""),
                                    style = TextStyle(
                                        color = if (isSelected) theme.accentPrimary else theme.textSecondary,
                                        fontSize = 11.5.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                    )
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    Text(
                        text = "HOME GRID LAYOUT",
                        style = TextStyle(
                            color = theme.accentPrimary,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.8.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        listOf(4 to "4 x 5 (Standard)", 5 to "5 x 5 (Compact)").forEach { (cols, label) ->
                            val isSelected = gridSize == cols
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isSelected) theme.accentPrimary.copy(alpha = 0.2f) else theme.surfaceGlass)
                                    .border(
                                        if (isSelected) 1.5.dp else 1.dp,
                                        if (isSelected) theme.accentPrimary else theme.borderGlass,
                                        RoundedCornerShape(12.dp)
                                    )
                                    .clickable { onGridSizeSelected(cols) }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    style = TextStyle(
                                        color = if (isSelected) theme.accentPrimary else theme.textSecondary,
                                        fontSize = 11.5.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                    )
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    GlassButton(
                        text = "Open Maya OS Settings",
                        onClick = {
                            showCustomizationSheet = false
                            showSettingsDialog = true
                        },
                        icon = Icons.Default.Settings,
                        primaryColor = theme.buttonBackground,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(14.dp))
                }
            }
        }

        // ==========================================
        // 11. MAYA OS SETTINGS DIALOG
        // ==========================================
        if (showSettingsDialog && activeInternalApp == null) {
            Dialog(
                onDismissRequest = { showSettingsDialog = false },
                properties = DialogProperties(usePlatformDefaultWidth = false)
            ) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth(0.94f)
                        .clip(RoundedCornerShape(24.dp))
                        .border(1.2.dp, theme.borderGlass, RoundedCornerShape(24.dp)),
                    color = if (theme.isDark) Color(0xFF131620) else Color(0xFFFFFFFF),
                    contentColor = theme.textPrimary
                ) {
                    var tempProfileName by remember { mutableStateOf(userName) }
                    var isEditingName by remember { mutableStateOf(false) }

                    Column(
                        modifier = Modifier
                            .padding(20.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(theme.accentPrimary.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Settings,
                                        contentDescription = null,
                                        tint = theme.accentPrimary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "Maya OS Settings",
                                        style = TextStyle(
                                            color = theme.textPrimary,
                                            fontSize = 17.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                    Text(
                                        text = "System & Environment Preferences",
                                        style = TextStyle(
                                            color = theme.textSecondary,
                                            fontSize = 11.sp
                                        )
                                    )
                                }
                            }

                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = theme.textSecondary,
                                modifier = Modifier
                                    .size(20.dp)
                                    .clickable { showSettingsDialog = false }
                            )
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        // Personalization
                        Text(
                            text = "USER PROFILE",
                            style = TextStyle(
                                color = theme.accentPrimary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            )
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(theme.surfaceGlass)
                                .border(1.dp, theme.borderGlass, RoundedCornerShape(14.dp))
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = userName,
                                    style = TextStyle(
                                        color = theme.textPrimary,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                                Text(
                                    text = "Synchronized with Maya Connect profile",
                                    style = TextStyle(color = theme.textSecondary, fontSize = 11.sp)
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(theme.accentPrimary.copy(alpha = 0.15f))
                                    .clickable { isEditingName = !isEditingName }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = if (isEditingName) "Cancel" else "Edit",
                                    style = TextStyle(
                                        color = theme.accentPrimary,
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                        }

                        if (isEditingName) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedTextField(
                                    value = tempProfileName,
                                    onValueChange = { tempProfileName = it },
                                    label = { Text("Profile Name", fontSize = 11.sp) },
                                    singleLine = true,
                                    modifier = Modifier.weight(1f),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = theme.textPrimary,
                                        unfocusedTextColor = theme.textPrimary,
                                        focusedBorderColor = theme.accentPrimary,
                                        unfocusedBorderColor = theme.borderGlass
                                    )
                                )
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(theme.buttonBackground)
                                        .clickable {
                                            if (tempProfileName.isNotBlank()) {
                                                onUserNameChange(tempProfileName.trim())
                                                isEditingName = false
                                            }
                                        }
                                        .padding(horizontal = 14.dp, vertical = 12.dp)
                                ) {
                                    Text(
                                        text = "Save",
                                        style = TextStyle(
                                            color = Color.White,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        // App Registry section
                        Text(
                            text = "CENTRAL ECOSYSTEM APPS (APP REGISTRY)",
                            style = TextStyle(
                                color = theme.accentPrimary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            )
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            AppRegistry.coreEcosystemApps.forEach { appReg ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(theme.surfaceGlass)
                                        .border(1.dp, theme.borderGlass, RoundedCornerShape(12.dp))
                                        .padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = appReg.name,
                                            style = TextStyle(
                                                color = theme.textPrimary,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        )
                                        Text(
                                            text = appReg.url,
                                            style = TextStyle(
                                                color = theme.textSecondary,
                                                fontSize = 10.5.sp
                                            ),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(theme.accentPrimary.copy(alpha = 0.15f))
                                            .padding(horizontal = 8.dp, vertical = 3.dp)
                                    ) {
                                        Text(
                                            text = appReg.category.displayName,
                                            style = TextStyle(
                                                color = theme.accentPrimary,
                                                fontSize = 9.5.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        GlassButton(
                            text = "Return to Maya Connect Hub",
                            onClick = {
                                showSettingsDialog = false
                                onExitMayaOs()
                            },
                            icon = Icons.Default.ArrowBack,
                            primaryColor = theme.buttonBackground,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}

private fun createWallpaperBrush(wallpaper: String, theme: MayaColorScheme): Brush {
    return when (wallpaper) {
        "deep_nebula" -> {
            Brush.radialGradient(
                colors = listOf(
                    theme.accentPrimary.copy(alpha = if (theme.isDark) 0.35f else 0.20f),
                    theme.surface,
                    theme.background
                ),
                center = Offset(300f, 400f),
                radius = 1200f
            )
        }
        "glass_prism" -> {
            Brush.linearGradient(
                colors = listOf(
                    theme.background,
                    theme.surfaceGlass,
                    theme.background
                ),
                start = Offset(0f, 0f),
                end = Offset(1000f, 1800f)
            )
        }
        "minimal_dark" -> {
            Brush.verticalGradient(
                colors = listOf(
                    theme.background,
                    theme.surface,
                    theme.background
                )
            )
        }
        else -> { // "dynamic_abstract" (default)
            Brush.verticalGradient(
                colors = listOf(
                    theme.surface,
                    theme.background,
                    if (theme.isDark) Color(0xFF090B0F) else Color(0xFFE2E8F0)
                )
            )
        }
    }
}
