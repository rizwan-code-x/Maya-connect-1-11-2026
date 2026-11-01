package com.example.ui.screens

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Build
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.webkit.RenderProcessGoneDetail
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.LinkOff
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.data.config.CentralAppConfig
import com.example.data.model.AppItem
import com.example.data.model.AppStatus
import com.example.ui.components.AppLogoIcon
import com.example.ui.components.GlassButton
import com.example.ui.components.LiveStatusBadge
import com.example.ui.components.glassmorphic
import com.example.ui.theme.MayaTheme
import com.example.data.security.SecurityHardeningEngine
import kotlinx.coroutines.delay

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun InAppBrowserScreen(
    app: AppItem,
    onClose: () -> Unit,
    onConfigureApp: (AppItem) -> Unit = {},
    onSecurityEventDetected: (
        category: String,
        severity: String,
        endpoint: String,
        reason: String,
        errorCode: String,
        mitigation: String
    ) -> Unit = { _, _, _, _, _, _ -> }
) {
    val context = LocalContext.current
    val theme = MayaTheme.colors

    // Detect if running inside a headless cloud container lacking a physical GPU renderNode
    val isHeadlessCloudContainer = remember {
        val isEmulatorBuild = Build.HARDWARE.contains("ranchu", ignoreCase = true) ||
                Build.HARDWARE.contains("goldfish", ignoreCase = true) ||
                Build.FINGERPRINT.contains("generic", ignoreCase = true) ||
                Build.PRODUCT.contains("sdk_gphone", ignoreCase = true) ||
                Build.PRODUCT.contains("vbox", ignoreCase = true)
        val hasRenderNode = try {
            java.io.File("/dev/dri/renderD128").exists() || java.io.File("/dev/dri/card0").exists()
        } catch (_: Exception) {
            false
        }
        isEmulatorBuild && !hasRenderNode
    }

    var webViewInstance by remember { mutableStateOf<WebView?>(null) }
    var useSoftwareFallback by remember { mutableStateOf(isHeadlessCloudContainer) }
    var pageProgress by remember { mutableFloatStateOf(0f) }
    var isLoading by remember { mutableStateOf(true) }
    var isTimedOut by remember { mutableStateOf(false) }
    var hasError by remember { mutableStateOf(false) }
    var isOffline by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var canGoBack by remember { mutableStateOf(false) }
    var retryCount by remember { mutableIntStateOf(0) }

    // File chooser callback for web file uploads with strict extension & path-traversal sanitization
    var fileUploadCallback by remember { mutableStateOf<ValueCallback<Array<Uri>>?>(null) }
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenMultipleDocuments()
    ) { uris ->
        val safeUris = uris.filter { uri ->
            val validation = SecurityHardeningEngine.validateSelectedFileUri(uri)
            if (!validation.isSafe) {
                onSecurityEventDetected(
                    validation.detectedCategory ?: "MALICIOUS_FILE_UPLOAD",
                    validation.severity,
                    "WebView/FilePicker (${app.name})",
                    validation.detectionReason ?: "Blocked unsafe file upload in WebView.",
                    validation.errorCode ?: "SEC_ERR_415_UNSAFE_FILE",
                    "Rejected malicious file selection before passing URI to WebView."
                )
            }
            validation.isSafe
        }
        fileUploadCallback?.onReceiveValue(safeUris.toTypedArray())
        fileUploadCallback = null
    }

    // Intercept hardware/gesture Back button (Points 63, 64)
    BackHandler {
        if (!hasError && !isOffline && !isTimedOut && webViewInstance?.canGoBack() == true) {
            try {
                webViewInstance?.goBack()
            } catch (_: Exception) {
                onClose()
            }
        } else {
            onClose()
        }
    }

    // Clean up WebView and memory codecs when composable is destroyed
    DisposableEffect(app.id) {
        onDispose {
            try {
                fileUploadCallback?.onReceiveValue(null)
                fileUploadCallback = null
                if (!hasError) {
                    webViewInstance?.apply {
                        stopLoading()
                        loadUrl("about:blank")
                        onPause()
                        pauseTimers()
                    }
                }
                webViewInstance?.destroy()
            } catch (_: Exception) {
            }
            webViewInstance = null
        }
    }

    // Check connectivity helper
    fun checkIsOnline(): Boolean {
        return try {
            val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager ?: return true
            val activeNetwork = cm.activeNetwork ?: return true
            val caps = cm.getNetworkCapabilities(activeNetwork) ?: return true
            caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        } catch (_: Exception) {
            true
        }
    }

    // 1.8-second max loading watchdog & connectivity checker
    LaunchedEffect(app.id, retryCount) {
        isOffline = !checkIsOnline()
        isTimedOut = false
        delay(1800)
        // Automatically reveal live rendering page after 1.8s max
        if (isLoading && !hasError && !isOffline) {
            isLoading = false
        }
        delay(13200)
        if (pageProgress < 0.2f && !hasError && !isOffline) {
            isTimedOut = true
        }
    }

    // Status pre-check: Coming Soon, Maintenance, Unconfigured
    val isComingSoon = app.status == AppStatus.COMING_SOON
    val isMaintenance = app.status == AppStatus.MAINTENANCE
    val isMissingConfig = app.url.isBlank() ||
            (!app.url.startsWith("http://", ignoreCase = true) &&
             !app.url.startsWith("https://", ignoreCase = true) &&
             !CentralAppConfig.isValidHttpsUrl(app.url))

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
            // Glass OS Top Header for In-App Browser
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .glassmorphic(
                        shape = RoundedCornerShape(0.dp),
                        backgroundColor = if (theme.isDark) Color(0xF0131620) else Color(0xF0FFFFFF),
                        borderColor = theme.borderGlass,
                        borderWidth = 1.dp
                    )
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left: Close & Return to Maya OS + App Identity
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(theme.surfaceGlass)
                                .border(1.dp, theme.borderGlass, CircleShape)
                                .clickable { onClose() },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Exit to Maya OS",
                                tint = theme.textPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        AppLogoIcon(app = app, size = 32.dp, cornerRadius = 9.dp)

                        Spacer(modifier = Modifier.width(10.dp))

                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = app.name,
                                    style = TextStyle(
                                        color = theme.textPrimary,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                LiveStatusBadge(status = app.status)
                            }
                            Text(
                                text = "Maya Connect Workspace • 120Hz Smooth Engine",
                                style = TextStyle(
                                    color = theme.textSecondary,
                                    fontSize = 10.sp
                                )
                            )
                        }
                    }

                    // Right Controls: Browser Navigation, Reload, Open External
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        if (canGoBack) {
                            Box(
                                modifier = Modifier
                                    .size(30.dp)
                                    .clip(CircleShape)
                                    .background(theme.surfaceGlass)
                                    .clickable { webViewInstance?.goBack() },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Back",
                                    tint = theme.textPrimary,
                                    modifier = Modifier.size(15.dp)
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .size(30.dp)
                                .clip(CircleShape)
                                .background(theme.surfaceGlass)
                                .clickable {
                                    if (hasError || isTimedOut || isOffline || webViewInstance == null) {
                                        hasError = false
                                        isTimedOut = false
                                        isOffline = !checkIsOnline()
                                        retryCount++
                                    } else {
                                        try {
                                            webViewInstance?.reload()
                                        } catch (_: Exception) {
                                            retryCount++
                                        }
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Reload",
                                tint = theme.textPrimary,
                                modifier = Modifier.size(15.dp)
                            )
                        }

                        if (!isComingSoon && !isMissingConfig) {
                            Box(
                                modifier = Modifier
                                    .size(30.dp)
                                    .clip(CircleShape)
                                    .background(theme.surfaceGlass)
                                    .clickable {
                                        try {
                                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(app.url))
                                            context.startActivity(intent)
                                        } catch (_: Exception) {
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.OpenInBrowser,
                                    contentDescription = "Open in Chrome/Browser",
                                    tint = theme.accentPrimary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Top Animated Progress Bar
            if (isLoading && !isComingSoon && !isMaintenance && !isMissingConfig) {
                val animatedProgress by animateFloatAsState(
                    targetValue = pageProgress,
                    animationSpec = tween(150, easing = LinearEasing),
                    label = "pageProgress"
                )
                LinearProgressIndicator(
                    progress = { animatedProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(2.5.dp),
                    color = theme.accentPrimary,
                    trackColor = Color.Transparent
                )
            }

            // Real-Time Offline Banner Notification (English)
            if (isOffline) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF991B1B))
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.WifiOff,
                            contentDescription = "Offline Indicator",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "You are currently offline. Online web applications require an active internet connection to load. Offline local tools (Daily Task, Notepad, Music, Diagnostics, Maya OS) remain functional. Please connect to Wi-Fi or mobile data.",
                            style = TextStyle(
                                color = Color.White,
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Medium
                            ),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Main Web View Container & Special State Handling
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                if (isComingSoon) {
                    // Coming Soon State View (Point 84)
                    SpecialStatusView(
                        icon = Icons.Default.HourglassTop,
                        iconTint = theme.accentSecondary,
                        title = "Coming Soon",
                        subtitle = "${app.name} is currently under active ecosystem development. It will be available in an upcoming Maya Connect update.",
                        primaryButtonText = "Back to Maya OS",
                        onPrimaryClick = onClose
                    )
                } else if (isMaintenance) {
                    // Maintenance State View (Point 95)
                    SpecialStatusView(
                        icon = Icons.Default.Build,
                        iconTint = Color(0xFFFFB703),
                        title = "Currently under maintenance",
                        subtitle = "${app.name} servers are temporarily undergoing scheduled maintenance. Please check back shortly.",
                        primaryButtonText = "Return to Home",
                        onPrimaryClick = onClose
                    )
                } else if (isMissingConfig) {
                    // Missing Configuration View (Point 114)
                    SpecialStatusView(
                        icon = Icons.Default.LinkOff,
                        iconTint = Color(0xFFFF9E00),
                        title = "This application is not configured yet.",
                        subtitle = "Please specify a valid secure HTTPS endpoint URL for ${app.name} to connect to its live service.",
                        primaryButtonText = "Configure URL",
                        onPrimaryClick = {
                            onClose()
                            onConfigureApp(app)
                        }
                    )
                } else {
                    // Standard Active WebView Container: ONLY mounted when active with no fatal errors or offline status
                    if (!hasError && !isOffline && !isTimedOut) {
                        key(retryCount, useSoftwareFallback) {
                            AndroidView(
                                factory = { ctx ->
                                    WebView(ctx).apply {
                                        layoutParams = ViewGroup.LayoutParams(
                                            ViewGroup.LayoutParams.MATCH_PARENT,
                                            ViewGroup.LayoutParams.MATCH_PARENT
                                        )

                                        // Pre-create WebView cache directories to prevent WASM/HTTP cache creation errors
                                        try {
                                            val webViewCacheDir = java.io.File(ctx.cacheDir, "WebView/Default/HTTP Cache/Code Cache/wasm")
                                            if (!webViewCacheDir.exists()) {
                                                webViewCacheDir.mkdirs()
                                            }
                                        } catch (_: Exception) {}

                                        // Use full 120Hz GPU Hardware Tile Compositing on real mobile devices;
                                        // use software layer only in headless virtual containers without /dev/dri/renderD128
                                        if (useSoftwareFallback) {
                                            setLayerType(View.LAYER_TYPE_SOFTWARE, null)
                                        } else {
                                            setLayerType(View.LAYER_TYPE_NONE, null)
                                        }

                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                            try {
                                                setRendererPriorityPolicy(
                                                    if (useSoftwareFallback) WebView.RENDERER_PRIORITY_BOUND else WebView.RENDERER_PRIORITY_IMPORTANT,
                                                    false
                                                )
                                            } catch (_: Exception) {}
                                        }

                                        // Native 120Hz responsive scrolling properties
                                        isVerticalScrollBarEnabled = true
                                        isHorizontalScrollBarEnabled = false
                                        isScrollbarFadingEnabled = true
                                        // Disable Compose nested scroll interception so Chromium's C++ compositor handles 120Hz flings directly
                                        isNestedScrollingEnabled = false
                                        isScrollContainer = true
                                        isFocusable = true
                                        isFocusableInTouchMode = true
                                        overScrollMode = View.OVER_SCROLL_ALWAYS

                                        // Zero-overhead touch dispatch: request disallow intercept only on ACTION_DOWN
                                        setOnTouchListener { v, event ->
                                            when (event.actionMasked) {
                                                MotionEvent.ACTION_DOWN -> {
                                                    v.parent?.requestDisallowInterceptTouchEvent(true)
                                                }
                                                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                                                    v.parent?.requestDisallowInterceptTouchEvent(false)
                                                }
                                            }
                                            false
                                        }

                                        // Ensure WebView remote debugging is disabled in production
                                        try {
                                            WebView.setWebContentsDebuggingEnabled(false)
                                        } catch (_: Exception) {}

                                        @Suppress("DEPRECATION")
                                        settings.apply {
                                            javaScriptEnabled = true
                                            domStorageEnabled = true
                                            databaseEnabled = true
                                            useWideViewPort = true
                                            loadWithOverviewMode = true
                                            // Enforce strict HTTPS subresources; block cleartext mixed content
                                            mixedContentMode = WebSettings.MIXED_CONTENT_NEVER_ALLOW
                                            cacheMode = WebSettings.LOAD_DEFAULT
                                            setSupportZoom(true)
                                            builtInZoomControls = true
                                            displayZoomControls = false
                                            // Enable high render thread priority & offscreen tile pre-rasterization for 120Hz scrolling
                                            try {
                                                setRenderPriority(WebSettings.RenderPriority.HIGH)
                                            } catch (_: Exception) {}
                                            offscreenPreRaster = !useSoftwareFallback
                                            // Enable auto media playback for background YouTube audio
                                            mediaPlaybackRequiresUserGesture = false
                                            // Strict WebView file & content sandbox hardening
                                            allowFileAccess = false
                                            allowContentAccess = false
                                            allowFileAccessFromFileURLs = false
                                            allowUniversalAccessFromFileURLs = false
                                            saveFormData = false
                                            javaScriptCanOpenWindowsAutomatically = false
                                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                                safeBrowsingEnabled = true
                                            }
                                        }

                                        webChromeClient = object : WebChromeClient() {
                                            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                                                if (isLoading) {
                                                    pageProgress = newProgress / 100f
                                                }
                                                if (newProgress >= 20 && isLoading) {
                                                    isLoading = false
                                                }
                                            }

                                            // Point 67: File Upload Support
                                            override fun onShowFileChooser(
                                                webView: WebView?,
                                                filePathCallback: ValueCallback<Array<Uri>>?,
                                                fileChooserParams: FileChooserParams?
                                            ): Boolean {
                                                fileUploadCallback?.onReceiveValue(null)
                                                fileUploadCallback = filePathCallback
                                                return try {
                                                    filePickerLauncher.launch(arrayOf("*/*"))
                                                    true
                                                } catch (_: Exception) {
                                                    fileUploadCallback?.onReceiveValue(null)
                                                    fileUploadCallback = null
                                                    false
                                                }
                                            }
                                        }

                                        // Shared 120Hz Smooth Website Scrolling & Passive Touch Listener script
                                        val smoothHzScrollScript = """
                                            (function() {
                                                try {
                                                    if (!window.__mayaHzPatched) {
                                                        window.__mayaHzPatched = true;
                                                        var origAdd = EventTarget.prototype.addEventListener;
                                                        EventTarget.prototype.addEventListener = function(type, listener, options) {
                                                            if (type === 'touchstart' || type === 'touchmove' || type === 'wheel' || type === 'mousewheel') {
                                                                var opts = options;
                                                                if (typeof options === 'boolean') {
                                                                    opts = { capture: options, passive: true };
                                                                } else if (typeof options === 'object' && options !== null) {
                                                                    if (options.passive === undefined) {
                                                                        opts = Object.assign({}, options, { passive: true });
                                                                    }
                                                                } else {
                                                                    opts = { passive: true };
                                                                }
                                                                return origAdd.call(this, type, listener, opts);
                                                            }
                                                            return origAdd.call(this, type, listener, options);
                                                        };
                                                    }
                                                    if (!document.getElementById('maya-120hz-scroll-style')) {
                                                        var style = document.createElement('style');
                                                        style.id = 'maya-120hz-scroll-style';
                                                        style.innerHTML = 'html, body { -webkit-overflow-scrolling: touch !important; overscroll-behavior-y: auto !important; touch-action: pan-x pan-y pinch-zoom !important; -webkit-tap-highlight-color: transparent; }';
                                                        (document.head || document.documentElement).appendChild(style);
                                                    }
                                                } catch(e) {}
                                            })();
                                        """.trimIndent()

                                        webViewClient = object : WebViewClient() {
                                            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                                                isLoading = true
                                                hasError = false
                                                isTimedOut = false
                                                isOffline = false
                                                canGoBack = view?.canGoBack() == true
                                                // Inject passive touch listeners & visibility overrides early
                                                try {
                                                    view?.evaluateJavascript(smoothHzScrollScript, null)
                                                    view?.evaluateJavascript(
                                                        """
                                                        (function() {
                                                            try {
                                                                Object.defineProperty(document, 'hidden', {get: function() { return false; }, configurable: true});
                                                                Object.defineProperty(document, 'visibilityState', {get: function() { return 'visible'; }, configurable: true});
                                                                document.addEventListener('visibilitychange', function(e) { e.stopImmediatePropagation(); }, true);
                                                            } catch(e) {}
                                                        })();
                                                        """.trimIndent(),
                                                        null
                                                    )
                                                } catch (_: Exception) {}
                                            }

                                            override fun onPageFinished(view: WebView?, url: String?) {
                                                isLoading = false
                                                canGoBack = view?.canGoBack() == true
                                                // Background YouTube playback script and Foreground Service registration
                                                if (url?.contains("youtube") == true) {
                                                    try {
                                                        com.example.service.YouTubeMediaPlaybackService.startService(context, app.name)
                                                        view?.evaluateJavascript(
                                                            """
                                                            (function() {
                                                                try {
                                                                    Object.defineProperty(document, 'hidden', {get: function() { return false; }, configurable: true});
                                                                    Object.defineProperty(document, 'visibilityState', {get: function() { return 'visible'; }, configurable: true});
                                                                    window.addEventListener('pagehide', function(e) { e.stopImmediatePropagation(); }, true);
                                                                } catch(e) {}
                                                            })();
                                                            """.trimIndent(),
                                                            null
                                                        )
                                                    } catch (_: Exception) {}
                                                }

                                                // Ensure 120Hz native compositor momentum scrolling is applied once DOM head is ready
                                                try {
                                                    view?.evaluateJavascript(smoothHzScrollScript, null)
                                                } catch (_: Exception) {}

                                                // Point 9: Google Maps Navigation — Suppress repeated app download prompts
                                                if (url?.contains("maps.google") == true || url?.contains("google.com/maps") == true) {
                                                    try {
                                                        view?.evaluateJavascript(
                                                            """
                                                            (function() {
                                                                try {
                                                                    var style = document.createElement('style');
                                                                    style.innerHTML = '.app-promo, .promo-banner, #mobile-banner, .ml-app-promo, [aria-label*="Get the app"], [aria-label*="Use the app"], .app-banner, [data-ved*="app"] { display: none !important; }';
                                                                    document.head.appendChild(style);
                                                                } catch(e) {}
                                                            })();
                                                            """.trimIndent(),
                                                            null
                                                        )
                                                    } catch (_: Exception) {}
                                                }
                                            }

                                            override fun onReceivedError(
                                                view: WebView?,
                                                request: WebResourceRequest?,
                                                error: WebResourceError?
                                            ) {
                                                if (request?.isForMainFrame == true) {
                                                    isLoading = false
                                                    if (!checkIsOnline()) {
                                                        isOffline = true
                                                    } else {
                                                        hasError = true
                                                        errorMessage = error?.description?.toString() ?: "Connection failed"
                                                    }
                                                }
                                            }

                                            // Prevents app crash when virtual emulator renderer process is killed or GPU resets
                                            override fun onRenderProcessGone(
                                                view: WebView?,
                                                detail: RenderProcessGoneDetail?
                                            ): Boolean {
                                                try {
                                                    (view?.parent as? ViewGroup)?.removeView(view)
                                                    view?.stopLoading()
                                                    view?.destroy()
                                                } catch (_: Exception) {}
                                                webViewInstance = null
                                                if (!useSoftwareFallback) {
                                                    // Seamlessly switch to software fallback and reload automatically
                                                    useSoftwareFallback = true
                                                    hasError = false
                                                    retryCount++
                                                } else {
                                                    isLoading = false
                                                    hasError = true
                                                    errorMessage = "Graphics rendering fallback active in streaming environment. Tap below to reload or open in browser."
                                                }
                                                return true // Returning true prevents the host app process from crashing
                                            }

                                            override fun shouldOverrideUrlLoading(
                                                view: WebView?,
                                                request: WebResourceRequest?
                                            ): Boolean {
                                                val targetUrl = request?.url?.toString() ?: return false
                                                // Suppress intent and market redirects to prevent repeated app download prompts
                                                if (targetUrl.startsWith("intent://", ignoreCase = true) ||
                                                    targetUrl.startsWith("market://", ignoreCase = true)
                                                ) {
                                                    return true
                                                }
                                                val validation = SecurityHardeningEngine.validateWebViewNavigationUrl(targetUrl)
                                                if (!validation.isAllowedInWebView) {
                                                    onSecurityEventDetected(
                                                        "UNSAFE_WEBVIEW_REDIRECT",
                                                        validation.severity,
                                                        "WebView/${app.id}/shouldOverrideUrlLoading",
                                                        validation.reason ?: "Blocked untrusted or non-HTTPS WebView redirect.",
                                                        validation.errorCode ?: "SEC_ERR_403_WEBVIEW_REDIRECT",
                                                        "Navigation cancelled inside WebView sandbox."
                                                    )
                                                    return true
                                                }
                                                return false
                                            }
                                        }

                                        isOffline = !checkIsOnline()
                                        if (!isOffline) {
                                            loadUrl(app.url)
                                        }
                                        webViewInstance = this
                                    }
                                },
                                update = { view ->
                                    if (!hasError) {
                                        webViewInstance = view
                                    }
                                },
                                onRelease = { view ->
                                    try {
                                        if (!hasError) {
                                            view.stopLoading()
                                            view.loadUrl("about:blank")
                                            view.onPause()
                                            view.pauseTimers()
                                        }
                                        view.destroy()
                                    } catch (_: Exception) {
                                    }
                                },
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }

                    // Point 59: Initial Web Loading Screen (Logo + Name + Connecting)
                    if (isLoading && !hasError && !isOffline && !isTimedOut) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(theme.background),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                AppLogoIcon(app = app, size = 64.dp, cornerRadius = 18.dp)
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = app.name,
                                    style = TextStyle(
                                        color = theme.textPrimary,
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(16.dp),
                                        color = theme.accentPrimary,
                                        strokeWidth = 2.dp
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Connecting to ${app.name}...",
                                        style = TextStyle(
                                            color = theme.textSecondary,
                                            fontSize = 12.5.sp
                                        )
                                    )
                                }
                            }
                        }
                    }

                    // Point 61: Network Offline Error Screen
                    if (isOffline) {
                        SpecialStatusView(
                            icon = Icons.Default.WifiOff,
                            iconTint = Color(0xFFFF5252),
                            title = "No Internet Connection",
                            subtitle = "Maya Connect couldn't connect right now. Please check your Wi-Fi or mobile data.",
                            primaryButtonText = "Try Again",
                            onPrimaryClick = {
                                isOffline = !checkIsOnline()
                                if (!isOffline) {
                                    hasError = false
                                    isTimedOut = false
                                    retryCount++
                                }
                            }
                        )
                    } else if (isTimedOut) {
                        // Point 60: Loading Timeout Error Screen
                        SpecialStatusView(
                            icon = Icons.Default.HourglassTop,
                            iconTint = Color(0xFFFFB703),
                            title = "Connection Timeout",
                            subtitle = "The application is taking longer than expected to load. Would you like to retry or open externally?",
                            primaryButtonText = "Try Again",
                            onPrimaryClick = {
                                isTimedOut = false
                                hasError = false
                                retryCount++
                            },
                            secondaryButtonText = "Open in Browser",
                            onSecondaryClick = {
                                try {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(app.url))
                                    context.startActivity(intent)
                                } catch (_: Exception) {}
                            }
                        )
                    } else if (hasError) {
                        // General Web Error Screen / Media Limitation Screen
                        val isCodecLimitation = errorMessage.contains("video") || errorMessage.contains("codec") || errorMessage.contains("graphics") || errorMessage.contains("emulator") || errorMessage.contains("rendering") || errorMessage.contains("streaming")
                        SpecialStatusView(
                            icon = if (isCodecLimitation) Icons.Default.OpenInBrowser else Icons.Default.WifiOff,
                            iconTint = if (isCodecLimitation) Color(0xFFFFB703) else Color(0xFFFF5252),
                            title = if (isCodecLimitation) "Graphics / Rendering Notice" else "Something went wrong",
                            subtitle = if (errorMessage.isNotBlank()) errorMessage else "Maya Connect couldn't load ${app.name}. Tap below to try again.",
                            primaryButtonText = if (isCodecLimitation) "Open in Browser" else "Try Again",
                            onPrimaryClick = {
                                if (isCodecLimitation) {
                                    try {
                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(app.url))
                                        context.startActivity(intent)
                                    } catch (_: Exception) {}
                                } else {
                                    hasError = false
                                    isTimedOut = false
                                    retryCount++
                                }
                            },
                            secondaryButtonText = if (isCodecLimitation) "Try Again Here" else "Open in Browser",
                            onSecondaryClick = {
                                if (isCodecLimitation) {
                                    hasError = false
                                    isTimedOut = false
                                    retryCount++
                                } else {
                                    try {
                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(app.url))
                                        context.startActivity(intent)
                                    } catch (_: Exception) {}
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SpecialStatusView(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: Color,
    title: String,
    subtitle: String,
    primaryButtonText: String,
    onPrimaryClick: () -> Unit,
    secondaryButtonText: String? = null,
    onSecondaryClick: (() -> Unit)? = null
) {
    val theme = MayaTheme.colors
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(theme.background)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(68.dp)
                    .clip(CircleShape)
                    .background(iconTint.copy(alpha = 0.15f))
                    .border(1.dp, iconTint.copy(alpha = 0.4f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(32.dp)
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = title,
                style = TextStyle(
                    color = theme.textPrimary,
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Bold
                ),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = subtitle,
                style = TextStyle(
                    color = theme.textSecondary,
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                ),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(0.85f)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                GlassButton(
                    text = primaryButtonText,
                    onClick = onPrimaryClick,
                    icon = Icons.Default.Refresh
                )
                if (secondaryButtonText != null && onSecondaryClick != null) {
                    GlassButton(
                        text = secondaryButtonText,
                        onClick = onSecondaryClick,
                        icon = Icons.Default.OpenInBrowser,
                        primaryColor = theme.accentSecondary
                    )
                }
            }
        }
    }
}
