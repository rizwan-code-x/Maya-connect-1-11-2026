package com.example.ui.screens

import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import com.example.ui.components.rememberChromeSmoothFlingBehavior
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.PlaylistPlay
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RepeatOne
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.viewmodel.MayaViewModel
import com.example.ui.viewmodel.MusicTrack
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

private enum class RexlifyMusicTab {
    NOW_PLAYING,
    PLAYLIST
}

/**
 * REXLIFY MUSIC — Exact UI & Functional Replication of the Reference Design
 *
 * Includes:
 * 1. Now Playing Screen:
 *    - Dark OLED black canvas with glowing purple/magenta upper gradient
 *    - Top row: collapse arrow (left), horizontal pill indicator (center), 3-dot menu (right)
 *    - Song Title, Artist Name, and interactive Heart/Favorite icon
 *    - Center circular album artwork with 2 concentric translucent glass rings
 *    - Side peek circular artwork previews for Previous track (left) and Next track (right)
 *    - Smooth slide/scale/rotation transition when changing tracks (each song has its own unique image)
 *    - Glowing circular progress arc along the bottom-left inner ring with inline time indicator (e.g., 3:23)
 *    - Bottom playback controls: Shuffle, Previous, Play/Pause, Next, Repeat
 * 2. Playlist Screen:
 *    - Vertically stacked track list with rounded-square unique artwork thumbnails, title, artist, duration, and 3-dot edit menu
 *    - Preserves exact original song filename when importing local audio files
 *    - Floating glassmorphic Mini-Player positioned directly above the bottom navigation bar
 * 3. Floating 5-Icon Glassmorphic Bottom Navigation Bar on both screens
 */
@Composable
fun MusicPlayerScreen(
    viewModel: MayaViewModel,
    onClose: () -> Unit
) {
    val tracks by viewModel.musicPlaylist.collectAsStateWithLifecycle()
    val currentTrackIdx by viewModel.currentTrackIndex.collectAsStateWithLifecycle()
    val isPlaying by viewModel.isMusicPlaying.collectAsStateWithLifecycle()
    val progressSec by viewModel.playbackProgressSec.collectAsStateWithLifecycle()
    val playbackError by viewModel.musicPlaybackError.collectAsStateWithLifecycle()
    val isRepeat by viewModel.isMusicRepeat.collectAsStateWithLifecycle()
    val isShuffle by viewModel.isMusicShuffle.collectAsStateWithLifecycle()
    val volume by viewModel.musicVolume.collectAsStateWithLifecycle()

    var activeTab by remember { mutableStateOf(RexlifyMusicTab.NOW_PLAYING) }
    // Direction of track change: +1 for Next (slides in from right), -1 for Previous (slides in from left)
    var transitionDirection by remember { mutableIntStateOf(1) }
    var previousObservedIdx by remember { mutableIntStateOf(currentTrackIdx) }

    LaunchedEffect(currentTrackIdx) {
        if (currentTrackIdx != previousObservedIdx) {
            previousObservedIdx = currentTrackIdx
        }
    }

    // Dialog states for 3-dot menu, song editing, and equalizer/volume sheet
    var showTopOptionsSheet by remember { mutableStateOf(false) }
    var showEqualizerDialog by remember { mutableStateOf(false) }
    var trackBeingEdited by remember { mutableStateOf<MusicTrack?>(null) }
    var editTitleText by remember { mutableStateOf("") }
    var editArtistText by remember { mutableStateOf("") }

    val currentTrack: MusicTrack? = tracks.getOrNull(currentTrackIdx)
    val prevTrack: MusicTrack? = if (tracks.isNotEmpty()) {
        tracks[(currentTrackIdx - 1 + tracks.size) % tracks.size]
    } else null
    val nextTrack: MusicTrack? = if (tracks.isNotEmpty()) {
        tracks[(currentTrackIdx + 1) % tracks.size]
    } else null

    // Local audio picker (preserves exact filename in MayaViewModel.importAudioUris)
    val audioPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenMultipleDocuments()
    ) { uris: List<Uri> ->
        if (uris.isNotEmpty()) {
            viewModel.importAudioUris(uris)
        }
    }

    BackHandler {
        if (activeTab == RexlifyMusicTab.PLAYLIST) {
            activeTab = RexlifyMusicTab.NOW_PLAYING
        } else {
            onClose()
        }
    }

    fun formatDuration(sec: Int): String {
        val safeSec = sec.coerceAtLeast(0)
        val m = safeSec / 60
        val s = safeSec % 60
        return "%d:%02d".format(m, s)
    }

    // Continuous smooth rotation for center vinyl artwork when playing
    val infiniteTransition = rememberInfiniteTransition(label = "rexlifyTurntable")
    val vinylRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 22000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "vinylRotation"
    )

    val orbGlowPulse by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "orbGlowPulse"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF040307))
    ) {
        // Upper Purple / Magenta Ambient Glow Background (Exact match to reference image)
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            // Vertical smooth base gradient from rich orchid/purple at top into deep black
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFA663B8),
                        Color(0xFF7D3C98),
                        Color(0xFF431C53),
                        Color(0xFF170920),
                        Color(0xFF050408),
                        Color(0xFF030205)
                    ),
                    startY = 0f,
                    endY = h * 0.62f
                )
            )

            // Soft radial highlight near top-center for luminous depth
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFFD291DF).copy(alpha = 0.42f),
                        Color(0xFF8E44AD).copy(alpha = 0.18f),
                        Color.Transparent
                    ),
                    center = Offset(w * 0.48f, h * 0.08f),
                    radius = w * 0.85f
                ),
                center = Offset(w * 0.48f, h * 0.08f),
                radius = w * 0.85f
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            // 1. TOP BAR (Collapse Chevron Left, Pill Indicator Center, 3-Dot Menu Right)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {
                        if (activeTab == RexlifyMusicTab.PLAYLIST) {
                            activeTab = RexlifyMusicTab.NOW_PLAYING
                        } else {
                            onClose()
                        }
                    },
                    modifier = Modifier
                        .size(40.dp)
                        .testTag("music_collapse_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = "Collapse",
                        tint = Color.White.copy(alpha = 0.92f),
                        modifier = Modifier.size(28.dp)
                    )
                }

                // Small centered horizontal pill indicator
                Box(
                    modifier = Modifier
                        .width(34.dp)
                        .height(4.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.42f))
                )

                // Three-dot menu on the right
                IconButton(
                    onClick = { showTopOptionsSheet = true },
                    modifier = Modifier
                        .size(40.dp)
                        .testTag("music_top_more_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "More Options",
                        tint = Color.White.copy(alpha = 0.92f),
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            // Optional playback error notice
            if (playbackError != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 4.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0x33EF4444))
                        .border(1.dp, Color(0x66EF4444), RoundedCornerShape(12.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = playbackError ?: "",
                        style = TextStyle(color = Color(0xFFFCA5A5), fontSize = 11.sp)
                    )
                }
            }

            // MAIN CONTENT AREA: Switches between NOW PLAYING and PLAYLIST
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                if (activeTab == RexlifyMusicTab.NOW_PLAYING) {
                    // ==================== SCREEN 1: NOW PLAYING ====================
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Spacer(modifier = Modifier.height(18.dp))

                        // Song Title, Artist & Favorite Heart Row
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 30.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = currentTrack?.title ?: "Skin",
                                    style = TextStyle(
                                        color = Color.White,
                                        fontSize = 31.sp,
                                        fontWeight = FontWeight.Light,
                                        letterSpacing = 0.2.sp
                                    ),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = currentTrack?.artist ?: "Flume",
                                    style = TextStyle(
                                        color = Color.White.copy(alpha = 0.65f),
                                        fontSize = 14.5.sp,
                                        fontWeight = FontWeight.Normal
                                    ),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            IconButton(
                                onClick = {
                                    currentTrack?.let { viewModel.toggleTrackFavorite(it.id) }
                                },
                                modifier = Modifier
                                    .size(42.dp)
                                    .testTag("music_favorite_btn")
                            ) {
                                val isFav = currentTrack?.isFavorite == true
                                Icon(
                                    imageVector = if (isFav) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                    contentDescription = "Favorite",
                                    tint = if (isFav) Color(0xFFF472B6) else Color.White.copy(alpha = 0.88f),
                                    modifier = Modifier.size(23.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.weight(0.45f))

                        // CENTER CIRCULAR TURNTABLE + SIDE PEEK ARTWORKS
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(310.dp)
                                .clipToBounds()
                                .pointerInput(tracks.size) {
                                    var totalDrag = 0f
                                    detectHorizontalDragGestures(
                                        onDragStart = { totalDrag = 0f },
                                        onHorizontalDrag = { _, dragAmount ->
                                            totalDrag += dragAmount
                                        },
                                        onDragEnd = {
                                            if (totalDrag < -65f) {
                                                transitionDirection = 1
                                                viewModel.nextTrack()
                                            } else if (totalDrag > 65f) {
                                                transitionDirection = -1
                                                viewModel.previousTrack()
                                            }
                                        }
                                    )
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            // 1. Left Side Peek Circle (Previous Track's Unique Artwork)
                            if (prevTrack != null) {
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.CenterStart)
                                        .offset(x = (-58).dp)
                                        .size(116.dp)
                                        .clip(CircleShape)
                                        .clickable(
                                            interactionSource = remember { MutableInteractionSource() },
                                            indication = null
                                        ) {
                                            transitionDirection = -1
                                            viewModel.previousTrack()
                                        }
                                ) {
                                    TrackAlbumArtwork(
                                        track = prevTrack,
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .graphicsLayer { alpha = 0.85f }
                                    )
                                }
                            }

                            // 2. Right Side Peek Circle (Next Track's Unique Artwork)
                            if (nextTrack != null) {
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.CenterEnd)
                                        .offset(x = 58.dp)
                                        .size(116.dp)
                                        .clip(CircleShape)
                                        .clickable(
                                            interactionSource = remember { MutableInteractionSource() },
                                            indication = null
                                        ) {
                                            transitionDirection = 1
                                            viewModel.nextTrack()
                                        }
                                ) {
                                    TrackAlbumArtwork(
                                        track = nextTrack,
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .graphicsLayer { alpha = 0.85f }
                                    )
                                }
                            }

                            // 3. Concentric Translucent Glass Rings + Circular Progress Arc
                            val trackDuration = (currentTrack?.durationSec ?: 203).coerceAtLeast(1)
                            val displaySec = if (progressSec > 0) progressSec else currentTrack?.durationSec ?: 203
                            val progressFraction = if (progressSec > 0) {
                                (progressSec.toFloat() / trackDuration.toFloat()).coerceIn(0.05f, 1f)
                            } else {
                                0.34f // Matches the reference image arc when at initial position
                            }

                            Canvas(
                                modifier = Modifier
                                    .size(292.dp)
                                    .clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = null
                                    ) {
                                        // Tap ring to advance +10s smoothly
                                        viewModel.seekForward10()
                                    }
                            ) {
                                val center = Offset(size.width / 2f, size.height / 2f)
                                val outerRadius = size.minDimension / 2f - 2.dp.toPx()
                                val innerRingRadius = outerRadius * 0.785f

                                // Outer translucent glass ring fill
                                drawCircle(
                                    brush = Brush.radialGradient(
                                        colors = listOf(
                                            Color(0xFF1A1423).copy(alpha = 0.55f),
                                            Color(0xFF3E2D4C).copy(alpha = 0.32f),
                                            Color.White.copy(alpha = 0.16f)
                                        ),
                                        center = center,
                                        radius = outerRadius
                                    ),
                                    radius = outerRadius,
                                    center = center
                                )

                                // Outer specular glowing border
                                drawCircle(
                                    brush = Brush.sweepGradient(
                                        colors = listOf(
                                            Color.White.copy(alpha = 0.25f),
                                            Color.White.copy(alpha = 0.52f),
                                            Color.White.copy(alpha = 0.20f),
                                            Color.White.copy(alpha = 0.55f),
                                            Color.White.copy(alpha = 0.25f)
                                        ),
                                        center = center
                                    ),
                                    radius = outerRadius,
                                    center = center,
                                    style = Stroke(width = 1.3.dp.toPx())
                                )

                                // Darker inner recessed chamber
                                drawCircle(
                                    brush = Brush.radialGradient(
                                        colors = listOf(
                                            Color(0xFF0E0B14).copy(alpha = 0.85f),
                                            Color(0xFF1A1424).copy(alpha = 0.65f)
                                        ),
                                        center = center,
                                        radius = innerRingRadius
                                    ),
                                    radius = innerRingRadius,
                                    center = center
                                )

                                // Subtle full circumference inner track ring
                                drawCircle(
                                    color = Color.White.copy(alpha = 0.14f),
                                    radius = innerRingRadius,
                                    center = center,
                                    style = Stroke(width = 1.dp.toPx())
                                )

                                // Glowing blue-lavender playback progress arc ending near 6 o'clock (102 degrees)
                                val sweepAngle = 82f + (progressFraction * 160f).coerceAtMost(240f)
                                val startAngle = 104f + (80f - sweepAngle)
                                val arcTopLeft = Offset(center.x - innerRingRadius, center.y - innerRingRadius)
                                val arcSize = Size(innerRingRadius * 2f, innerRingRadius * 2f)

                                // Soft outer glow of the progress arc
                                drawArc(
                                    color = Color(0xFF93C5FD).copy(alpha = 0.32f),
                                    startAngle = startAngle,
                                    sweepAngle = sweepAngle,
                                    useCenter = false,
                                    topLeft = arcTopLeft,
                                    size = arcSize,
                                    style = Stroke(width = 5.dp.toPx(), cap = StrokeCap.Round)
                                )

                                // Crisp glowing progress arc line
                                drawArc(
                                    brush = Brush.sweepGradient(
                                        colors = listOf(
                                            Color(0xFF60A5FA),
                                            Color(0xFFA78BFA),
                                            Color(0xFFC4B5FD),
                                            Color(0xFF93C5FD)
                                        ),
                                        center = center
                                    ),
                                    startAngle = startAngle,
                                    sweepAngle = sweepAngle,
                                    useCenter = false,
                                    topLeft = arcTopLeft,
                                    size = arcSize,
                                    style = Stroke(width = 2.2.dp.toPx(), cap = StrokeCap.Round)
                                )
                            }

                            // 4. Center Animated Album Artwork (Transitions from Next/Prev peek + Rotates)
                            AnimatedContent(
                                targetState = currentTrack ?: MusicTrack(
                                    id = "default",
                                    title = "Skin",
                                    artist = "Flume",
                                    durationSec = 203,
                                    artworkStyleId = "skin"
                                ),
                                transitionSpec = {
                                    val dir = transitionDirection
                                    (slideInHorizontally(
                                        animationSpec = tween(420, easing = FastOutSlowInEasing),
                                        initialOffsetX = { fullWidth -> dir * (fullWidth * 3 / 4) }
                                    ) + scaleIn(
                                        initialScale = 0.68f,
                                        animationSpec = tween(420, easing = FastOutSlowInEasing)
                                    ) + fadeIn(animationSpec = tween(320)))
                                        .togetherWith(
                                            slideOutHorizontally(
                                                animationSpec = tween(420, easing = FastOutSlowInEasing),
                                                targetOffsetX = { fullWidth -> -dir * (fullWidth * 3 / 4) }
                                            ) + scaleOut(
                                                targetScale = 0.68f,
                                                animationSpec = tween(420, easing = FastOutSlowInEasing)
                                            ) + fadeOut(animationSpec = tween(280))
                                        ) using SizeTransform(clip = false)
                                },
                                label = "centerArtworkTransition"
                            ) { animatedTrack ->
                                Box(
                                    modifier = Modifier
                                        .size(176.dp)
                                        .shadow(24.dp, CircleShape, spotColor = Color.Black)
                                        .clip(CircleShape)
                                        .graphicsLayer {
                                            rotationZ = if (isPlaying) vinylRotation else 0f
                                        }
                                        .clickable {
                                            viewModel.toggleMusicPlayPause()
                                        }
                                ) {
                                    TrackAlbumArtwork(
                                        track = animatedTrack,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                            }

                            // 5. Elapsed / Duration Timestamp on Bottom of Inner Ring (e.g. "3:23")
                            Box(
                                modifier = Modifier
                                    .align(Alignment.Center)
                                    .offset(y = 113.dp)
                                    .background(Color(0xFF120E1A).copy(alpha = 0.72f), RoundedCornerShape(6.dp))
                                    .padding(horizontal = 6.dp, vertical = 1.dp)
                            ) {
                                Text(
                                    text = formatDuration(displaySec),
                                    style = TextStyle(
                                        color = Color.White.copy(alpha = 0.92f),
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.Medium,
                                        letterSpacing = 0.3.sp
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.weight(0.55f))

                        // BOTTOM PLAYBACK CONTROLS ROW (Shuffle, Prev, Play/Pause, Next, Repeat)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 28.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Shuffle
                            IconButton(
                                onClick = { viewModel.toggleMusicShuffle() },
                                modifier = Modifier
                                    .size(44.dp)
                                    .testTag("music_shuffle_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Shuffle,
                                    contentDescription = "Shuffle",
                                    tint = if (isShuffle) Color(0xFFD8B4FE) else Color.White.copy(alpha = 0.85f),
                                    modifier = Modifier.size(21.dp)
                                )
                            }

                            // Previous Track
                            IconButton(
                                onClick = {
                                    transitionDirection = -1
                                    viewModel.previousTrack()
                                },
                                modifier = Modifier
                                    .size(48.dp)
                                    .testTag("music_prev_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.SkipPrevious,
                                    contentDescription = "Previous",
                                    tint = Color.White,
                                    modifier = Modifier.size(28.dp)
                                )
                            }

                            // Play / Pause
                            IconButton(
                                onClick = { viewModel.toggleMusicPlayPause() },
                                modifier = Modifier
                                    .size(56.dp)
                                    .testTag("music_play_pause_btn")
                            ) {
                                Icon(
                                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                    contentDescription = if (isPlaying) "Pause" else "Play",
                                    tint = Color.White,
                                    modifier = Modifier.size(34.dp)
                                )
                            }

                            // Next Track
                            IconButton(
                                onClick = {
                                    transitionDirection = 1
                                    viewModel.nextTrack()
                                },
                                modifier = Modifier
                                    .size(48.dp)
                                    .testTag("music_next_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.SkipNext,
                                    contentDescription = "Next",
                                    tint = Color.White,
                                    modifier = Modifier.size(28.dp)
                                )
                            }

                            // Repeat
                            IconButton(
                                onClick = { viewModel.toggleMusicRepeat() },
                                modifier = Modifier
                                    .size(44.dp)
                                    .testTag("music_repeat_btn")
                            ) {
                                Icon(
                                    imageVector = if (isRepeat == 2) Icons.Default.RepeatOne else Icons.Default.Repeat,
                                    contentDescription = "Repeat",
                                    tint = if (isRepeat > 0) Color(0xFFD8B4FE) else Color.White.copy(alpha = 0.85f),
                                    modifier = Modifier.size(21.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(26.dp))
                    }
                } else {
                    // ==================== SCREEN 2: PLAYLIST SCREEN ====================
                    Box(modifier = Modifier.fillMaxSize()) {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            flingBehavior = rememberChromeSmoothFlingBehavior(),
                            contentPadding = PaddingValues(
                                start = 22.dp,
                                end = 16.dp,
                                top = 8.dp,
                                bottom = 92.dp // Space for floating Mini-Player
                            )
                        ) {
                            itemsIndexed(
                                items = tracks,
                                key = { _, track -> track.id }
                            ) { index, track ->
                                val isCurrent = index == currentTrackIdx
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            transitionDirection = if (index >= currentTrackIdx) 1 else -1
                                            viewModel.playTrack(index)
                                        }
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 11.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        // Left Rounded-Square Unique Album Artwork
                                        Box(
                                            modifier = Modifier
                                                .size(54.dp)
                                                .shadow(8.dp, RoundedCornerShape(14.dp))
                                                .clip(RoundedCornerShape(14.dp))
                                                .border(
                                                    width = if (isCurrent) 1.2.dp else 0.5.dp,
                                                    color = if (isCurrent) Color(0xFFD8B4FE) else Color.White.copy(alpha = 0.18f),
                                                    shape = RoundedCornerShape(14.dp)
                                                )
                                        ) {
                                            TrackAlbumArtwork(
                                                track = track,
                                                modifier = Modifier.fillMaxSize()
                                            )
                                        }

                                        Spacer(modifier = Modifier.width(16.dp))

                                        // Center Song Title & Artist Name
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = track.title,
                                                style = TextStyle(
                                                    color = if (isCurrent) Color(0xFFF3E8FF) else Color.White,
                                                    fontSize = 15.5.sp,
                                                    fontWeight = if (isCurrent) FontWeight.SemiBold else FontWeight.Medium
                                                ),
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Spacer(modifier = Modifier.height(3.dp))
                                            Text(
                                                text = track.artist,
                                                style = TextStyle(
                                                    color = Color.White.copy(alpha = 0.52f),
                                                    fontSize = 12.5.sp,
                                                    fontWeight = FontWeight.Normal
                                                ),
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }

                                        Spacer(modifier = Modifier.width(8.dp))

                                        // Right Duration + 3-Dot Edit Button
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = formatDuration(track.durationSec),
                                                style = TextStyle(
                                                    color = Color.White.copy(alpha = 0.88f),
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Medium
                                                )
                                            )
                                            IconButton(
                                                onClick = {
                                                    trackBeingEdited = track
                                                    editTitleText = track.title
                                                    editArtistText = track.artist
                                                },
                                                modifier = Modifier
                                                    .size(32.dp)
                                                    .testTag("track_edit_more_${index}")
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.MoreVert,
                                                    contentDescription = "Edit Song",
                                                    tint = Color.White.copy(alpha = 0.65f),
                                                    modifier = Modifier.size(17.dp)
                                                )
                                            }
                                        }
                                    }

                                    // Subtle horizontal divider indented from thumbnail
                                    HorizontalDivider(
                                        modifier = Modifier.padding(start = 70.dp, end = 8.dp),
                                        thickness = 0.6.dp,
                                        color = Color.White.copy(alpha = 0.10f)
                                    )
                                }
                            }
                        }

                        // FLOATING MINI-PLAYER (Positioned directly above the Bottom Navigation Bar)
                        if (currentTrack != null) {
                            val miniProgress = if (progressSec > 0) {
                                (progressSec.toFloat() / currentTrack.durationSec.coerceAtLeast(1).toFloat())
                                    .coerceIn(0.15f, 1f)
                            } else {
                                0.35f
                            }

                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .padding(horizontal = 18.dp, vertical = 10.dp)
                                    .fillMaxWidth()
                                    .height(70.dp)
                                    .shadow(20.dp, RoundedCornerShape(24.dp), spotColor = Color.Black)
                                    .clip(RoundedCornerShape(24.dp))
                                    .background(Color(0xFF120F1D).copy(alpha = 0.95f))
                                    .border(
                                        width = 1.dp,
                                        brush = Brush.horizontalGradient(
                                            listOf(
                                                Color.White.copy(alpha = 0.22f),
                                                Color(0xFFA855F7).copy(alpha = 0.35f),
                                                Color.White.copy(alpha = 0.16f)
                                            )
                                        ),
                                        shape = RoundedCornerShape(24.dp)
                                    )
                                    .clickable {
                                        activeTab = RexlifyMusicTab.NOW_PLAYING
                                    }
                                    .testTag("playlist_mini_player")
                            ) {
                                // Subtle purple progress tint fill from left (exact match to reference mini-player)
                                Box(
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .fillMaxWidth(miniProgress)
                                        .background(
                                            Brush.horizontalGradient(
                                                colors = listOf(
                                                    Color(0xFF582C75).copy(alpha = 0.45f),
                                                    Color(0xFF7E3F98).copy(alpha = 0.52f)
                                                )
                                            )
                                        )
                                )

                                Row(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(horizontal = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Mini-player thumbnail
                                    Box(
                                        modifier = Modifier
                                            .size(48.dp)
                                            .clip(RoundedCornerShape(13.dp))
                                            .border(0.6.dp, Color.White.copy(alpha = 0.25f), RoundedCornerShape(13.dp))
                                    ) {
                                        TrackAlbumArtwork(
                                            track = currentTrack,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(14.dp))

                                    // Mini-player Title & Artist
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = currentTrack.title,
                                            style = TextStyle(
                                                color = Color.White,
                                                fontSize = 15.sp,
                                                fontWeight = FontWeight.Medium
                                            ),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = currentTrack.artist,
                                            style = TextStyle(
                                                color = Color.White.copy(alpha = 0.58f),
                                                fontSize = 12.sp
                                            ),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }

                                    // Mini-player Play/Pause & Next controls
                                    IconButton(
                                        onClick = { viewModel.toggleMusicPlayPause() },
                                        modifier = Modifier.size(38.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                            contentDescription = if (isPlaying) "Pause" else "Play",
                                            tint = Color.White,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }

                                    IconButton(
                                        onClick = {
                                            transitionDirection = 1
                                            viewModel.nextTrack()
                                        },
                                        modifier = Modifier.size(38.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.SkipNext,
                                            contentDescription = "Next",
                                            tint = Color.White,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 3. FLOATING 5-ICON BOTTOM NAVIGATION BAR (Shared across both screens)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp, vertical = 10.dp)
                    .height(66.dp)
                    .shadow(22.dp, RoundedCornerShape(34.dp), spotColor = Color.Black)
                    .clip(RoundedCornerShape(34.dp))
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFF151221).copy(alpha = 0.94f),
                                Color(0xFF0B0912).copy(alpha = 0.98f)
                            )
                        )
                    )
                    .border(
                        width = 1.dp,
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.22f),
                                Color.White.copy(alpha = 0.08f)
                            )
                        ),
                        shape = RoundedCornerShape(34.dp)
                    )
            ) {
                // Subtle purple bottom glow under active tab
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val glowX = when (activeTab) {
                        RexlifyMusicTab.PLAYLIST -> size.width * 0.31f
                        RexlifyMusicTab.NOW_PLAYING -> size.width * 0.50f
                    }
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color(0xFF9333EA).copy(alpha = 0.42f),
                                Color.Transparent
                            ),
                            center = Offset(glowX, size.height * 0.85f),
                            radius = size.height * 0.9f
                        ),
                        center = Offset(glowX, size.height * 0.85f),
                        radius = size.height * 0.9f
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 1. Home Icon
                    IconButton(
                        onClick = { activeTab = RexlifyMusicTab.NOW_PLAYING },
                        modifier = Modifier
                            .size(44.dp)
                            .testTag("music_nav_home")
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Home,
                            contentDescription = "Music Home",
                            tint = if (activeTab == RexlifyMusicTab.NOW_PLAYING) {
                                Color.White.copy(alpha = 0.85f)
                            } else {
                                Color.White.copy(alpha = 0.45f)
                            },
                            modifier = Modifier.size(23.dp)
                        )
                    }

                    // 2. Playlist / Queue Icon
                    IconButton(
                        onClick = { activeTab = RexlifyMusicTab.PLAYLIST },
                        modifier = Modifier
                            .size(44.dp)
                            .testTag("music_nav_playlist")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.PlaylistPlay,
                            contentDescription = "Playlist",
                            tint = if (activeTab == RexlifyMusicTab.PLAYLIST) {
                                Color.White
                            } else {
                                Color.White.copy(alpha = 0.75f)
                            },
                            modifier = Modifier.size(26.dp)
                        )
                    }

                    // 3. Center Swirling Iridescent Artwork Orb (Switches to Now Playing)
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .graphicsLayer {
                                scaleX = if (isPlaying) orbGlowPulse else 1f
                                scaleY = if (isPlaying) orbGlowPulse else 1f
                            }
                            .shadow(12.dp, CircleShape, spotColor = Color(0xFFA855F7))
                            .clip(CircleShape)
                            .border(1.2.dp, Color.White.copy(alpha = 0.55f), CircleShape)
                            .clickable {
                                activeTab = RexlifyMusicTab.NOW_PLAYING
                            }
                            .testTag("music_nav_center_orb")
                    ) {
                        IridescentCenterOrbCanvas(modifier = Modifier.fillMaxSize())
                    }

                    // 4. Audio Waveform / Equalizer Icon
                    IconButton(
                        onClick = { showEqualizerDialog = true },
                        modifier = Modifier
                            .size(44.dp)
                            .testTag("music_nav_waveform")
                    ) {
                        Icon(
                            imageVector = Icons.Default.GraphicEq,
                            contentDescription = "Audio Equalizer & Volume",
                            tint = Color.White.copy(alpha = 0.55f),
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    // 5. Bell / Import & Library Icon
                    IconButton(
                        onClick = { showTopOptionsSheet = true },
                        modifier = Modifier
                            .size(44.dp)
                            .testTag("music_nav_bell")
                    ) {
                        Icon(
                            imageVector = Icons.Default.NotificationsNone,
                            contentDescription = "Import & Music Options",
                            tint = Color.White.copy(alpha = 0.55f),
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }
        }

        // ==================== DIALOG 1: 3-DOT EDIT TRACK MODAL ====================
        val editingTrack = trackBeingEdited
        if (editingTrack != null) {
            val editingIndex = tracks.indexOfFirst { it.id == editingTrack.id }
            AlertDialog(
                onDismissRequest = { trackBeingEdited = null },
                containerColor = Color(0xFF161124),
                titleContentColor = Color.White,
                textContentColor = Color.White,
                title = {
                    Text(
                        text = "Edit Song Details",
                        style = TextStyle(
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedTextField(
                            value = editTitleText,
                            onValueChange = { editTitleText = it },
                            label = { Text("Song Name", color = Color.White.copy(alpha = 0.7f)) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = Color(0xFFA855F7),
                                unfocusedBorderColor = Color.White.copy(alpha = 0.25f)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = editArtistText,
                            onValueChange = { editArtistText = it },
                            label = { Text("Artist Name", color = Color.White.copy(alpha = 0.7f)) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = Color(0xFFA855F7),
                                unfocusedBorderColor = Color.White.copy(alpha = 0.25f)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        if (editingIndex >= 0 && tracks.size > 1) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0x26EF4444))
                                    .clickable {
                                        viewModel.removeTrack(editingIndex)
                                        trackBeingEdited = null
                                    }
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DeleteOutline,
                                    contentDescription = "Delete Song",
                                    tint = Color(0xFFF87171),
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Remove from Playlist",
                                    style = TextStyle(
                                        color = Color(0xFFF87171),
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                )
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            viewModel.updateTrackDetails(
                                trackId = editingTrack.id,
                                newTitle = editTitleText,
                                newArtist = editArtistText
                            )
                            trackBeingEdited = null
                        }
                    ) {
                        Text("Save", color = Color(0xFFD8B4FE), fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { trackBeingEdited = null }) {
                        Text("Cancel", color = Color.White.copy(alpha = 0.7f))
                    }
                }
            )
        }

        // ==================== DIALOG 2: TOP 3-DOT MENU (IMPORT & EDIT CURRENT) ====================
        if (showTopOptionsSheet) {
            AlertDialog(
                onDismissRequest = { showTopOptionsSheet = false },
                containerColor = Color(0xFF161124),
                titleContentColor = Color.White,
                textContentColor = Color.White,
                title = {
                    Text(
                        text = "REXLIFY Music Options",
                        style = TextStyle(
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        // 1. Import Local Audio File (Preserves exact song filename)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(Color(0xFF7E22CE).copy(alpha = 0.28f))
                                .border(1.dp, Color(0xFFA855F7).copy(alpha = 0.5f), RoundedCornerShape(14.dp))
                                .clickable {
                                    showTopOptionsSheet = false
                                    audioPickerLauncher.launch(arrayOf("audio/*", "application/ogg"))
                                }
                                .padding(horizontal = 14.dp, vertical = 12.dp)
                                .testTag("import_music_primary_btn"),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                tint = Color(0xFFD8B4FE),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Import Song from Device",
                                    style = TextStyle(
                                        color = Color.White,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                )
                                Text(
                                    text = "Keeps exact original song filename",
                                    style = TextStyle(
                                        color = Color.White.copy(alpha = 0.6f),
                                        fontSize = 11.5.sp
                                    )
                                )
                            }
                        }

                        // 2. Edit Current Song Name / Artist
                        if (currentTrack != null) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(Color.White.copy(alpha = 0.06f))
                                    .border(1.dp, Color.White.copy(alpha = 0.14f), RoundedCornerShape(14.dp))
                                    .clickable {
                                        showTopOptionsSheet = false
                                        trackBeingEdited = currentTrack
                                        editTitleText = currentTrack.title
                                        editArtistText = currentTrack.artist
                                    }
                                    .padding(horizontal = 14.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(19.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "Edit Current Song (${currentTrack.title})",
                                        style = TextStyle(
                                            color = Color.White,
                                            fontSize = 13.5.sp,
                                            fontWeight = FontWeight.Medium
                                        ),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "Rename song title or artist",
                                        style = TextStyle(
                                            color = Color.White.copy(alpha = 0.6f),
                                            fontSize = 11.5.sp
                                        )
                                    )
                                }
                            }
                        }

                        // 3. Switch View (Now Playing / Playlist)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(Color.White.copy(alpha = 0.06f))
                                .border(1.dp, Color.White.copy(alpha = 0.14f), RoundedCornerShape(14.dp))
                                .clickable {
                                    showTopOptionsSheet = false
                                    activeTab = if (activeTab == RexlifyMusicTab.NOW_PLAYING) {
                                        RexlifyMusicTab.PLAYLIST
                                    } else {
                                        RexlifyMusicTab.NOW_PLAYING
                                    }
                                }
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.PlaylistPlay,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(21.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = if (activeTab == RexlifyMusicTab.NOW_PLAYING) {
                                    "Open Playlist Queue (${tracks.size} Songs)"
                                } else {
                                    "Return to Now Playing Turntable"
                                },
                                style = TextStyle(
                                    color = Color.White,
                                    fontSize = 13.5.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            )
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showTopOptionsSheet = false }) {
                        Text("Close", color = Color(0xFFD8B4FE))
                    }
                }
            )
        }

        // ==================== DIALOG 3: HI-FI VOLUME & SCRUBBER ====================
        if (showEqualizerDialog) {
            AlertDialog(
                onDismissRequest = { showEqualizerDialog = false },
                containerColor = Color(0xFF161124),
                titleContentColor = Color.White,
                textContentColor = Color.White,
                title = {
                    Text(
                        text = "Audio & Playback Control",
                        style = TextStyle(
                            color = Color.White,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        if (currentTrack != null) {
                            Text(
                                text = "Seek Position (${formatDuration(progressSec)} / ${formatDuration(currentTrack.durationSec)})",
                                style = TextStyle(color = Color.White.copy(alpha = 0.8f), fontSize = 12.5.sp)
                            )
                            Slider(
                                value = progressSec.toFloat().coerceIn(0f, currentTrack.durationSec.toFloat()),
                                onValueChange = { viewModel.seekMusicProgress(it.toInt()) },
                                valueRange = 0f..currentTrack.durationSec.toFloat().coerceAtLeast(1f),
                                colors = SliderDefaults.colors(
                                    thumbColor = Color(0xFFD8B4FE),
                                    activeTrackColor = Color(0xFFA855F7),
                                    inactiveTrackColor = Color.White.copy(alpha = 0.2f)
                                )
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                                contentDescription = null,
                                tint = Color(0xFFD8B4FE),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Master Volume: ${(volume * 100).toInt()}%",
                                style = TextStyle(color = Color.White.copy(alpha = 0.8f), fontSize = 12.5.sp)
                            )
                        }
                        Slider(
                            value = volume,
                            onValueChange = { viewModel.setMusicVolume(it) },
                            valueRange = 0f..1f,
                            colors = SliderDefaults.colors(
                                thumbColor = Color(0xFFD8B4FE),
                                activeTrackColor = Color(0xFFA855F7),
                                inactiveTrackColor = Color.White.copy(alpha = 0.2f)
                            )
                        )
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showEqualizerDialog = false }) {
                        Text("Done", color = Color(0xFFD8B4FE), fontWeight = FontWeight.Bold)
                    }
                }
            )
        }
    }
}

/**
 * Renders the center swirling iridescent 3D ribbon orb in the Bottom Navigation Bar.
 */
@Composable
private fun IridescentCenterOrbCanvas(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val center = Offset(w / 2f, h / 2f)

        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color(0xFF4C1D95),
                    Color(0xFF1E1B4B),
                    Color(0xFF090714)
                ),
                center = center,
                radius = w / 2f
            )
        )

        // Swirling iridescent ribbons matching the reference center button
        val ribbonColors = listOf(
            Color(0xFF38BDF8),
            Color(0xFF818CF8),
            Color(0xFFC084FC),
            Color(0xFFF472B6),
            Color(0xFFE879F9)
        )
        for (i in 0 until 6) {
            val angle = i * (PI / 3.0)
            val offsetCenter = Offset(
                x = center.x + (cos(angle) * w * 0.16f).toFloat(),
                y = center.y + (sin(angle) * h * 0.16f).toFloat()
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        ribbonColors[i % ribbonColors.size].copy(alpha = 0.88f),
                        ribbonColors[(i + 2) % ribbonColors.size].copy(alpha = 0.35f),
                        Color.Transparent
                    ),
                    center = offsetCenter,
                    radius = w * 0.34f
                ),
                center = offsetCenter,
                radius = w * 0.34f
            )
        }
    }
}

/**
 * Renders a completely distinct, high-art visual cover for EVERY song in the playlist,
 * closely replicating the reference artwork thumbnails ("Skin", "Odyssey", "Beautiful Now",
 * "Towards Home", "Loyal", "Icon", "The First Day", "Starfall") as well as embedded ID3
 * cover art or unique procedural artwork for user-imported songs.
 */
@Composable
private fun TrackAlbumArtwork(
    track: MusicTrack,
    modifier: Modifier = Modifier
) {
    // 1. If the user imported a local song that contains embedded album art, render it directly
    val embeddedBitmap = remember(track.id, track.embeddedArtBytes) {
        track.embeddedArtBytes?.let { bytes ->
            try {
                BitmapFactory.decodeByteArray(bytes, 0, bytes.size)?.asImageBitmap()
            } catch (_: Exception) {
                null
            }
        }
    }

    if (embeddedBitmap != null) {
        Image(
            bitmap = embeddedBitmap,
            contentDescription = track.title,
            contentScale = ContentScale.Crop,
            modifier = modifier
        )
        return
    }

    // 2. Otherwise render bespoke high-resolution artwork matching each track's unique style
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val center = Offset(w / 2f, h / 2f)

        when (track.artworkStyleId) {
            "skin" -> {
                // "Skin" by Flume — Soft lilac-pink background with a curving botanical floral helix
                drawRect(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color(0xFFD6ACE8),
                            Color(0xFFE5B8E8),
                            Color(0xFFC8A4E6)
                        ),
                        start = Offset(0f, 0f),
                        end = Offset(w, h)
                    )
                )

                // Draw the signature S-curve floral bloom from top-center to bottom-right
                val petalCount = 28
                for (i in 0 until petalCount) {
                    val t = i.toFloat() / (petalCount - 1).toFloat()
                    // S-curve spine coordinates
                    val spineX = w * (0.48f - 0.16f * sin(t * PI.toFloat() * 1.15f) + 0.22f * t * t)
                    val spineY = h * (0.16f + 0.76f * t)
                    val spread = w * (0.04f + 0.18f * t)

                    val petalColor = when {
                        t < 0.35f -> Color(0xFFFDF2F8)
                        t < 0.62f -> Color(0xFFF5D0FE)
                        else -> Color(0xFF5B5BD6)
                    }
                    val accentColor = if (t < 0.55f) Color(0xFFD97757) else Color(0xFF3730A3)

                    for (cluster in -2..2) {
                        val px = spineX + cluster * spread * 0.42f
                        val py = spineY + (cluster % 2) * spread * 0.22f
                        val r = spread * (0.48f - kotlin.math.abs(cluster) * 0.08f)

                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    petalColor,
                                    accentColor.copy(alpha = 0.78f),
                                    Color.Transparent
                                ),
                                center = Offset(px, py),
                                radius = r.coerceAtLeast(2f)
                            ),
                            center = Offset(px, py),
                            radius = r.coerceAtLeast(2f)
                        )
                    }
                }
            }

            "odyssey" -> {
                // "Odyssey" by Roary — Silver metallic sphere levitating above a 3D white geometric pyramid
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFFD1D5DB),
                            Color(0xFFE5E7EB),
                            Color(0xFF9CA3AF)
                        )
                    )
                )

                // Left sunlit face of pyramid
                val leftPyramid = Path().apply {
                    moveTo(w * 0.52f, h * 0.44f)
                    lineTo(w * 0.06f, h * 1.0f)
                    lineTo(w * 0.52f, h * 1.0f)
                    close()
                }
                drawPath(
                    path = leftPyramid,
                    brush = Brush.linearGradient(
                        colors = listOf(Color(0xFFF9FAFB), Color(0xFFD1D5DB)),
                        start = Offset(w * 0.25f, h * 0.5f),
                        end = Offset(w * 0.52f, h)
                    )
                )

                // Right shaded face of pyramid
                val rightPyramid = Path().apply {
                    moveTo(w * 0.52f, h * 0.44f)
                    lineTo(w * 0.96f, h * 1.0f)
                    lineTo(w * 0.52f, h * 1.0f)
                    close()
                }
                drawPath(
                    path = rightPyramid,
                    brush = Brush.linearGradient(
                        colors = listOf(Color(0xFF9CA3AF), Color(0xFF6B7280)),
                        start = Offset(w * 0.52f, h * 0.5f),
                        end = Offset(w * 0.9f, h)
                    )
                )

                // Levitating 3D chrome sphere at apex
                val sphereCenter = Offset(w * 0.52f, h * 0.36f)
                val sphereRadius = w * 0.14f
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color.White,
                            Color(0xFFD1D5DB),
                            Color(0xFF4B5563)
                        ),
                        center = Offset(sphereCenter.x - sphereRadius * 0.3f, sphereCenter.y - sphereRadius * 0.3f),
                        radius = sphereRadius * 1.3f
                    ),
                    center = sphereCenter,
                    radius = sphereRadius
                )
            }

            "beautiful_now" -> {
                // "Beautiful Now" by Zedd & Marshmello — White canvas with vibrant multicolor paint splash
                drawRect(color = Color(0xFFF8FAFC))

                val splashColors = listOf(
                    Triple(0.18f, 0.28f, Color(0xFF10B981)),
                    Triple(0.32f, 0.52f, Color(0xFFEC4899)),
                    Triple(0.22f, 0.68f, Color(0xFF06B6D4)),
                    Triple(0.46f, 0.38f, Color(0xFFF59E0B)),
                    Triple(0.55f, 0.58f, Color(0xFF7C3AED)),
                    Triple(0.38f, 0.45f, Color(0xFF1E1B4B))
                )
                for ((rx, ry, col) in splashColors) {
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                col,
                                col.copy(alpha = 0.65f),
                                Color.Transparent
                            ),
                            center = Offset(w * rx, h * ry),
                            radius = w * 0.36f
                        ),
                        center = Offset(w * rx, h * ry),
                        radius = w * 0.36f
                    )
                }
            }

            "towards_home" -> {
                // "Towards Home" by Shawn Williams — Sunset beach reflection with lone silhouette
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF0284C7),
                            Color(0xFF7DD3FC),
                            Color(0xFFFDE68A),
                            Color(0xFFD97706),
                            Color(0xFF451A03)
                        )
                    )
                )
                // Horizon glow
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color(0xFFFFFBEB).copy(alpha = 0.75f),
                            Color.Transparent
                        ),
                        startY = h * 0.42f,
                        endY = h * 0.64f
                    )
                )
                // Lone silhouette at center horizon
                drawRoundRect(
                    color = Color(0xFF0F172A),
                    topLeft = Offset(w * 0.47f, h * 0.50f),
                    size = Size(w * 0.06f, h * 0.16f),
                    cornerRadius = CornerRadius(w * 0.03f, w * 0.03f)
                )
                drawCircle(
                    color = Color(0xFF0F172A),
                    center = Offset(w * 0.50f, h * 0.47f),
                    radius = w * 0.03f
                )
            }

            "loyal" -> {
                // "Loyal" by Chris Brown — Neon pop-art pink backdrop with cyan/crimson portrait silhouette
                drawRect(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color(0xFFF472B6),
                            Color(0xFFE879F9),
                            Color(0xFFC084FC)
                        )
                    )
                )
                // Stylized shoulders/torso
                val torsoPath = Path().apply {
                    moveTo(w * 0.22f, h)
                    cubicTo(w * 0.25f, h * 0.58f, w * 0.78f, h * 0.58f, w * 0.82f, h)
                    close()
                }
                drawPath(
                    path = torsoPath,
                    brush = Brush.linearGradient(
                        colors = listOf(Color(0xFF06B6D4), Color(0xFF9333EA), Color(0xFFEF4444))
                    )
                )
                // Stylized head
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0xFFE11D48), Color(0xFF881337)),
                        center = Offset(w * 0.56f, h * 0.36f),
                        radius = w * 0.16f
                    ),
                    center = Offset(w * 0.56f, h * 0.36f),
                    radius = w * 0.16f
                )
            }

            "icon" -> {
                // "Icon" by Roary — Classical marble bust with floating geometric particles
                drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFFF3F4F6),
                            Color(0xFFD1D5DB),
                            Color(0xFF6B7280)
                        ),
                        center = center,
                        radius = w * 0.75f
                    )
                )
                // Classical bust silhouette
                drawCircle(
                    brush = Brush.linearGradient(
                        colors = listOf(Color.White, Color(0xFF9CA3AF), Color(0xFF374151)),
                        start = Offset(w * 0.25f, h * 0.25f),
                        end = Offset(w * 0.68f, h * 0.75f)
                    ),
                    center = Offset(w * 0.45f, h * 0.46f),
                    radius = w * 0.26f
                )
                // Floating dark marble fragments on right
                for (i in 0..6) {
                    val fx = w * (0.62f + (i % 3) * 0.10f)
                    val fy = h * (0.24f + i * 0.09f)
                    drawCircle(
                        color = Color(0xFF1F2937).copy(alpha = 0.75f),
                        center = Offset(fx, fy),
                        radius = w * (0.025f + (i % 2) * 0.015f)
                    )
                }
            }

            "first_day" -> {
                // "The First Day" by Lights & Motion — Glowing cyan/blue aurora borealis
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF090D16),
                            Color(0xFF0EA5E9),
                            Color(0xFF67E8F9),
                            Color(0xFF0F172A)
                        )
                    )
                )
                // Aurora curtains
                for (band in 0..4) {
                    val bx = w * (0.18f + band * 0.16f)
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color(0xFFA5F3FC).copy(alpha = 0.75f),
                                Color(0xFF0284C7).copy(alpha = 0.25f),
                                Color.Transparent
                            ),
                            center = Offset(bx, h * 0.42f),
                            radius = w * 0.35f
                        ),
                        center = Offset(bx, h * 0.42f),
                        radius = w * 0.35f
                    )
                }
                // Dark mountain base
                val mountain = Path().apply {
                    moveTo(0f, h)
                    lineTo(0f, h * 0.72f)
                    lineTo(w * 0.35f, h * 0.62f)
                    lineTo(w * 0.72f, h * 0.70f)
                    lineTo(w, h * 0.60f)
                    lineTo(w, h)
                    close()
                }
                drawPath(path = mountain, color = Color(0xFF020617))
            }

            "starfall" -> {
                // "Starfall" by Roary — Violet twilight sky with lone silhouette on ridge
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFFF5D0FE),
                            Color(0xFFC084FC),
                            Color(0xFF6B21A8),
                            Color(0xFF1E1B4B)
                        )
                    )
                )
                // Ridge at bottom
                val ridge = Path().apply {
                    moveTo(0f, h)
                    lineTo(0f, h * 0.78f)
                    lineTo(w * 0.48f, h * 0.68f)
                    lineTo(w, h * 0.82f)
                    lineTo(w, h)
                    close()
                }
                drawPath(path = ridge, color = Color(0xFF0F0720))
                // Lone standing figure
                drawRoundRect(
                    color = Color(0xFF090414),
                    topLeft = Offset(w * 0.46f, h * 0.50f),
                    size = Size(w * 0.07f, h * 0.20f),
                    cornerRadius = CornerRadius(w * 0.03f, w * 0.03f)
                )
                drawCircle(
                    color = Color(0xFF090414),
                    center = Offset(w * 0.495f, h * 0.46f),
                    radius = w * 0.035f
                )
            }

            else -> {
                // Unique procedural art for every user-imported song based on its title/id hash
                val seed = (track.title.hashCode() xor track.id.hashCode())
                val c1 = Color(track.albumArtGradient.getOrElse(0) { 0xFFA855F7 })
                val c2 = Color(track.albumArtGradient.getOrElse(1) { 0xFF3B82F6 })
                val c3 = Color(track.albumArtGradient.getOrElse(2) { 0xFF0F172A })

                drawRect(
                    brush = Brush.linearGradient(
                        colors = listOf(c1, c2, c3),
                        start = Offset(0f, 0f),
                        end = Offset(w, h)
                    )
                )

                for (i in 0..4) {
                    val angle = ((seed + i * 73) % 360) * (PI / 180.0)
                    val ox = center.x + (cos(angle) * w * 0.24f).toFloat()
                    val oy = center.y + (sin(angle) * h * 0.24f).toFloat()
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.45f),
                                c1.copy(alpha = 0.35f),
                                Color.Transparent
                            ),
                            center = Offset(ox, oy),
                            radius = w * (0.28f + (i % 3) * 0.08f)
                        ),
                        center = Offset(ox, oy),
                        radius = w * (0.28f + (i % 3) * 0.08f)
                    )
                }
            }
        }
    }
}
