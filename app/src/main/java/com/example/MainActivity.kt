package com.example

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsDraggedAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState

import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear

import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.CloudQueue
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.ui.draw.alpha
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Palette
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.items
import com.example.themes.DummyThemeData
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.VolumeDown
import androidx.compose.material.icons.filled.KeyboardDoubleArrowUp
import androidx.compose.material.icons.filled.KeyboardDoubleArrowDown
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.outlined.NightsStay
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.Brush
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameMillis
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.RainAccent
import com.example.ui.theme.RainBackground
import com.example.ui.theme.RainMuted
import com.example.ui.theme.RainPrimary
import com.example.ui.theme.RainSurface
import com.example.ui.theme.RainText
import kotlin.random.Random

class MainActivity : ComponentActivity() {

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { _ ->
        // Permission result ignored gracefully
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val windowInsetsController =
            androidx.core.view.WindowCompat.getInsetsController(window, window.decorView)
        windowInsetsController.systemBarsBehavior =
            androidx.core.view.WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        windowInsetsController.hide(androidx.core.view.WindowInsetsCompat.Type.systemBars())

        // Gracefully request notification permissions for background sleep controls on Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        // Start rain service on launch so user gets ambient sounds instantly
        startRainService()

        setContent {
            MyApplicationTheme {
                Scaffold(
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("main_scaffold"),
                    containerColor = RainBackground
                ) { innerPadding ->
                    RainAppScreen(
                        modifier = Modifier.fillMaxSize(),
                        onStartService = { startRainService() },
                        onStopService = { stopRainService() }
                    )
                }
            }
        }
    }

    private fun startRainService() {
        val intent = Intent(this, RainAudioService::class.java).apply {
            action = RainAudioService.ACTION_START
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
    }

    private fun stopRainService() {
        val intent = Intent(this, RainAudioService::class.java).apply {
            action = RainAudioService.ACTION_STOP
        }
        startService(intent)
    }

    override fun onDestroy() {
        // Stop audio if Background Sleep Mode is OFF
        // In this applet, we default to let the user control it,
        // but if they leave playing without sleep mode toggle, we can optionally stop.
        super.onDestroy()
    }
}

// Haptic Helpers
fun triggerTouchHaptic(context: Context, intensity: Float) {
    if (!RainState.hapticsEnabled.value) return
    try {
        val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vibratorManager?.defaultVibrator
        } else {
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }

        vibrator?.let { v ->
            if (v.hasVibrator()) {
                val duration = 12L
                val amp = (intensity * 110f + 15f).toInt().coerceIn(1, 255)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    v.vibrate(VibrationEffect.createOneShot(duration, amp))
                } else {
                    v.vibrate(duration)
                }
            }
        }
    } catch (e: Exception) {
        // Safe catch for simulator environments
    }
}

fun triggerThunderHaptic(context: Context) {
    if (!RainState.hapticsEnabled.value) return
    try {
        val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vibratorManager?.defaultVibrator
        } else {
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }

        vibrator?.let { v ->
            if (v.hasVibrator()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    // Create deep rolling rumble: flash, delay, rolling thunder
                    val timings = longArrayOf(0, 100, 80, 200, 120, 450)
                    val amplitudes = intArrayOf(0, 200, 0, 90, 0, 45)
                    v.vibrate(VibrationEffect.createWaveform(timings, amplitudes, -1))
                } else {
                    v.vibrate(800)
                }
            }
        }
    } catch (e: Exception) {
        // Safe catch
    }
}

// Particle System Models
class RainDrop(
    var x: Float,
    var y: Float,
    var speed: Float,
    var length: Float,
    var width: Float,
    var alpha: Float,
    var targetGroundY: Float = 0f
)

class SplashParticle(
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    var size: Float,
    var alpha: Float,
    val maxAge: Float,
    var age: Float = 0f
)

class PuddleRipple(
    val x: Float,
    val y: Float,
    var radius: Float = 0f,
    val maxRadius: Float,
    var alpha: Float = 1f,
    val maxAge: Float = 0.5f,
    var age: Float = 0f
)

class RainParticleSystem {
    val dropPath = androidx.compose.ui.graphics.Path()
    val drops = mutableListOf<RainDrop>()
    val splashes = mutableListOf<SplashParticle>()
    val ripples = mutableListOf<PuddleRipple>()
    var activeBolts: List<List<Offset>> = emptyList()
    var width = 0f
    var height = 0f

    fun generateLightningBolts() {
        val boltCount = kotlin.random.Random.nextInt(1, 4)
        val newBolts = mutableListOf<List<Offset>>()
        for (i in 0 until boltCount) {
            val segments = mutableListOf<Offset>()
            var cx = kotlin.random.Random.nextFloat() * width
            var cy = 0f // Start from top
            segments.add(Offset(cx, cy))
            
            // Randomly go through the screen only within a range (upper side)
            val maxDepth = height * (0.3f + kotlin.random.Random.nextFloat() * 0.4f)
            
            while (cy < maxDepth) {
                cx += (kotlin.random.Random.nextFloat() - 0.5f) * 120f // jagged X
                cy += 30f + kotlin.random.Random.nextFloat() * 70f // jagged Y downward
                segments.add(Offset(cx, cy))
                
                // Sometimes branch off
                if (kotlin.random.Random.nextFloat() < 0.2f) {
                    val branch = mutableListOf<Offset>()
                    var bx = cx
                    var by = cy
                    branch.add(Offset(bx, by))
                    for (b in 0..3) {
                        bx += (kotlin.random.Random.nextFloat() - 0.5f) * 80f
                        by += 20f + kotlin.random.Random.nextFloat() * 40f
                        branch.add(Offset(bx, by))
                    }
                    newBolts.add(branch)
                }
            }
            newBolts.add(segments)
        }
        activeBolts = newBolts
    }
    
    fun clearBolts() {
        activeBolts = emptyList()
    }

    fun initialize(w: Float, h: Float) {
        if (width == w && height == h) return
        width = w
        height = h
        drops.clear()
        
        // Populate initial falling drops across the screen
        val initialCount = (w * 0.12f).toInt().coerceIn(30, 180)
        for (i in 0 until initialCount) {
            drops.add(
                RainDrop(
                    x = Random.nextFloat() * w,
                    y = Random.nextFloat() * h,
                    speed = 1400f + Random.nextFloat() * 1000f,
                    length = 15f + Random.nextFloat() * 20f,
                    width = 2f + Random.nextFloat() * 2f,
                    alpha = 0.4f + Random.nextFloat() * 0.5f,
                    targetGroundY = if (RainState.rippleLevel.value <= 0.01f) h + 100f else h * (1f - RainState.rippleLevel.value) + Random.nextFloat() * (h * RainState.rippleLevel.value)
                )
            )
        }
    }

    fun update(dt: Float, rainIntensity: Float, windFrequency: Float, isStorm: Boolean, radiusMulti: Float = 0.5f, durationMulti: Float = 0.5f) {
        if (width == 0f || height == 0f) return

        // Drop count scaled by intensity
        val targetDrops = (width * 0.08f + rainIntensity * width * 0.35f).toInt().coerceIn(20, 250)

        while (drops.size < targetDrops) {
            drops.add(
                RainDrop(
                    x = Random.nextFloat() * width,
                    y = -80f,
                    speed = 1500f + Random.nextFloat() * 1100f,
                    length = 15f + Random.nextFloat() * 20f,
                    width = 2f + Random.nextFloat() * 2f,
                    alpha = 0.4f + Random.nextFloat() * 0.5f,
                    targetGroundY = if (RainState.rippleLevel.value <= 0.01f) height + 100f else height * (1f - RainState.rippleLevel.value) + Random.nextFloat() * (height * RainState.rippleLevel.value)
                )
            )
        }
        while (drops.size > targetDrops) {
            drops.removeAt(drops.lastIndex)
        }

        // Wind angle calculation
        val windSlope = (windFrequency - 0.5f) * 3500f // Increased slope
        val dx = windSlope * dt

        // Update active drops
        val iterator = drops.listIterator()
        while (iterator.hasNext()) {
            val d = iterator.next()
            d.y += d.speed * dt
            d.x += dx

            // Horizontal screen wrap
            if (d.x < -100f) d.x = width + 50f
            if (d.x > width + 100f) d.x = -50f

            val groundY = d.targetGroundY
            if (d.y >= groundY) {
                // Splash/Ripple triggers
                if (ripples.size < 120) {
                    val scaleR = 0.2f + (radiusMulti * 2.3f)
                    val maxR = (25f + Random.nextFloat() * 35f + rainIntensity * 25f) * scaleR
                    ripples.add(
                        PuddleRipple(
                            x = d.x,
                            y = groundY,
                            maxRadius = maxR,
                            maxAge = (0.35f + Random.nextFloat() * 0.25f) * (0.2f + (durationMulti * 2.8f))
                        )
                    )
                }

                if (splashes.size < 120) {
                    val count = (1 + (rainIntensity * 3).toInt()).coerceAtMost(4)
                    for (p in 0 until count) {
                        splashes.add(
                            SplashParticle(
                                x = d.x,
                                y = groundY,
                                vx = (Random.nextFloat() * 160f - 80f) + windSlope * 0.08f,
                                vy = -(130f + Random.nextFloat() * 180f),
                                size = 1f + Random.nextFloat() * 1.5f,
                                alpha = d.alpha * 0.9f,
                                maxAge = 0.15f + Random.nextFloat() * 0.2f
                            )
                        )
                    }
                }

                // Reset drop to the top
                d.y = -60f
                d.x = Random.nextFloat() * width
                d.speed = 1400f + Random.nextFloat() * 1000f
                d.length = 15f + Random.nextFloat() * 20f + rainIntensity * 30f
                d.width = 2f + Random.nextFloat() * 2f
                d.alpha = 0.4f + Random.nextFloat() * 0.5f
                d.targetGroundY = if (RainState.rippleLevel.value <= 0.01f) height + 100f else height * (1f - RainState.rippleLevel.value) + Random.nextFloat() * (height * RainState.rippleLevel.value)
            }
        }

        // Update Splash Particles
        val splashIt = splashes.listIterator()
        while (splashIt.hasNext()) {
            val s = splashIt.next()
            s.age += dt
            if (s.age >= s.maxAge) {
                splashIt.remove()
            } else {
                s.x += s.vx * dt
                s.y += s.vy * dt
                s.vy += 920f * dt // Gravity pull
                s.alpha = (1f - s.age / s.maxAge) * 0.8f
            }
        }

        // Update Ripples
        val rippleIt = ripples.listIterator()
        while (rippleIt.hasNext()) {
            val r = rippleIt.next()
            r.age += dt
            if (r.age >= r.maxAge) {
                rippleIt.remove()
            } else {
                val progress = r.age / r.maxAge
                r.radius = r.maxRadius * progress
                r.alpha = 1f - progress
            }
        }
    }

    fun injectTouchSplash(x: Float, y: Float, intensity: Float, radiusMulti: Float = 0.5f, durationMulti: Float = 0.5f) {
        val numSplash = (10 + (intensity * 15).toInt()).coerceAtMost(25)
        for (i in 0 until numSplash) {
            splashes.add(
                SplashParticle(
                    x = x,
                    y = y,
                    vx = Random.nextFloat() * 320f - 160f,
                    vy = -(160f + Random.nextFloat() * 360f),
                    size = 1.8f + Random.nextFloat() * 2.5f,
                    alpha = 0.85f,
                    maxAge = 0.25f + Random.nextFloat() * 0.35f
                )
            )
        }

        ripples.add(
            PuddleRipple(
                x = x,
                y = y,
                maxRadius = (45f + intensity * 60f) * (0.2f + (radiusMulti * 2.3f)),
                maxAge = (0.55f + intensity * 0.25f) * (0.2f + (durationMulti * 2.8f))
            )
        )
    }
}

@Composable
fun RainAppScreen(
    modifier: Modifier = Modifier,
    onStartService: () -> Unit,
    onStopService: () -> Unit
) {
    val context = LocalContext.current

    // Gather states
    val isPlaying by RainState.isPlaying.collectAsStateWithLifecycle()
    val rainIntensity by RainState.rainIntensity.collectAsStateWithLifecycle()
    val windFrequency by RainState.windFrequency.collectAsStateWithLifecycle()
    val volume by RainState.volume.collectAsStateWithLifecycle()
    val isStormMode by RainState.isStormMode.collectAsStateWithLifecycle()
    val hapticsEnabled by RainState.hapticsEnabled.collectAsStateWithLifecycle()
    val thunderEnabled by RainState.thunderEnabled.collectAsStateWithLifecycle()
    val rippleLevel by RainState.rippleLevel.collectAsStateWithLifecycle()
    val rippleRadiusMulti by RainState.rippleRadiusMultiplier.collectAsStateWithLifecycle()
    val rippleDurationMulti by RainState.rippleDurationMultiplier.collectAsStateWithLifecycle()
    val backgroundImageUri by RainState.backgroundImageUri.collectAsStateWithLifecycle()
    
    val imagePickerLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia(),
        onResult = { uri -> 
            uri?.let {
                try {
                    context.contentResolver.takePersistableUriPermission(
                        it,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )
                } catch (e: Exception) {
                    // Ignore if not supported
                }
            }
            RainState.backgroundImageUri.value = uri 
        }
    )

    var backgroundSleepMode by remember { mutableStateOf(true) }
    var showGestureHint by remember { mutableStateOf(true) }
    var showStatsOverlay by remember { mutableStateOf(false) }
    var showThemesPage by remember { mutableStateOf(false) }
    var showControlPanel by remember { mutableStateOf(false) }
    var isLocked by remember { mutableStateOf(false) }
    var lockButtonVisible by remember { mutableStateOf(false) }

    // Visual ticker states
    val particleSystem = remember { RainParticleSystem() }
    var lightningAlpha by remember { mutableFloatStateOf(0f) }

    // HUD Auto-Fade Timings
    var lastInteractionTime by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var showHud by remember { mutableStateOf(true) }
    var frameTime by remember { mutableLongStateOf(0L) }

    LaunchedEffect(lastInteractionTime, showControlPanel) {
        // Keep HUD visible for 5.5 seconds, then fade out if playing
        if (!showControlPanel) {
            kotlinx.coroutines.delay(5500)
            if (isPlaying && !showControlPanel) {
                showHud = false
            }
        }
    }

    LaunchedEffect(lastInteractionTime) {
        if (isLocked) {
            lockButtonVisible = true
            kotlinx.coroutines.delay(2000)
            lockButtonVisible = false
        }
    }

    // Double-flicker Lightning animation loop
    LaunchedEffect(isStormMode, isPlaying, thunderEnabled) {
        if (isPlaying && thunderEnabled) {
            while (true) {
                // Flash every 7 to 18 seconds of deep storm
                val nextFlashDelay = Random.nextLong(7000, 18000)
                kotlinx.coroutines.delay(nextFlashDelay)

                // Lightning Strike 1 (Initial burst)
                particleSystem.generateLightningBolts()
                lightningAlpha = 0.5f // Decreased brightness
                RainState.triggerThunder = true
                triggerThunderHaptic(context)
                kotlinx.coroutines.delay(70)

                lightningAlpha = 0.1f // Decreased brightness
                kotlinx.coroutines.delay(50)

                // Lightning Strike 2 (Main burst)
                particleSystem.generateLightningBolts()
                lightningAlpha = 0.75f // Decreased brightness
                kotlinx.coroutines.delay(120)

                // Exponential-like decay
                val steps = 12
                for (s in 0 until steps) {
                    lightningAlpha -= 0.75f / steps
                    kotlinx.coroutines.delay(25)
                }
                lightningAlpha = 0f
                particleSystem.clearBolts()
            }
        } else {
            lightningAlpha = 0f
        }
    }

    // High performance drawing frame loop
    LaunchedEffect(isPlaying) {
        if (isPlaying) {
            var lastTime = withFrameMillis { it }
            while (true) {
                withFrameMillis { now ->
                    val dt = ((now - lastTime) / 1000f).coerceAtMost(0.033f) // clamp dt to avoid giant jumps
                    lastTime = now
                    particleSystem.update(dt, rainIntensity, windFrequency, isStormMode, rippleRadiusMulti, rippleDurationMulti)
                    frameTime = now
                }
            }
        }
    }

    // Dynamic Life Cycle handling: If Sleep Mode is disabled, shut down audio when activity goes to background
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner, backgroundSleepMode) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_STOP) {
                if (!backgroundSleepMode) {
                    onStopService()
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // Gesture detection wrapper matching Clean Minimalism aesthetics
    Box(
        modifier = modifier
            .background(Color.Black) // Always AMOLED Pitch Black
            .pointerInput(isLocked) {
                if (isLocked) return@pointerInput
                detectDragGestures(
                    onDragStart = { offset ->
                        lastInteractionTime = System.currentTimeMillis()
                        if (!showControlPanel) {
                            showHud = false
                        }

                        // Translate to stereo panning (-1f to 1f)
                        val pan = (offset.x / size.width) * 2f - 1f
                        val mappedTouchY = 1f - (offset.y / size.height)

                        RainState.splashQueue.offer(Pair(pan, mappedTouchY))
                        particleSystem.injectTouchSplash(offset.x, offset.y, mappedTouchY)
                        triggerTouchHaptic(context, mappedTouchY)
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        lastInteractionTime = System.currentTimeMillis()
                        if (!showControlPanel) {
                            showHud = false
                        }

                        // Horizontal swipe changes wind frequency (direction/speed)
                        val deltaX = dragAmount.x / size.width
                        
                        val targetWind = (RainState.windFrequency.value + deltaX * 1.5f).coerceIn(0f, 1f)
                        RainState.windFrequency.value = targetWind

                        // Continuous soft tactile ticks during adjustments
                        if (Random.nextFloat() < 0.22f) {
                            triggerTouchHaptic(context, 0.25f)
                        }
                    }
                )
            }
            .pointerInput(isLocked) {
                detectTapGestures { offset ->
                    lastInteractionTime = System.currentTimeMillis()
                    if (isLocked) {
                        lockButtonVisible = true
                    } else {
                        if (showControlPanel) {
                            showControlPanel = false
                        } else {
                            showHud = !showHud
                        }

                        val pan = (offset.x / size.width) * 2f - 1f
                        val touchY = 1f - (offset.y / size.height)

                        RainState.splashQueue.offer(Pair(pan, touchY))
                        particleSystem.injectTouchSplash(offset.x, offset.y, touchY)
                        triggerTouchHaptic(context, touchY)
                    }
                }
            }
    ) {
        // Background Image
        if (backgroundImageUri != null) {
            coil.compose.AsyncImage(
                model = backgroundImageUri,
                contentDescription = "Background Image",
                contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }

        // 1. Core Fullscreen Interactive Particle Animation
        Canvas(modifier = Modifier.fillMaxSize().testTag("rain_canvas")) {
            val currentFrame = frameTime
            particleSystem.initialize(size.width, size.height)

            // Draw concentric ripples (Puddle level)
            for (r in particleSystem.ripples) {
                drawOval(
                    color = Color(0xFF60A5FA).copy(alpha = r.alpha * 0.9f),
                    topLeft = Offset(r.x - r.radius, r.y - r.radius * 0.35f),
                    size = Size(r.radius * 2f, r.radius * 0.7f),
                    style = Stroke(width = 1.8.dp.toPx())
                )
                drawOval(
                    color = Color(0xFF38BDF8).copy(alpha = r.alpha * 0.7f),
                    topLeft = Offset(r.x - r.radius * 1.3f, r.y - r.radius * 0.35f * 1.3f),
                    size = Size(r.radius * 2.6f, r.radius * 0.7f * 1.3f),
                    style = Stroke(width = 0.8.dp.toPx())
                )
            }

            // Draw falling raindrop streaks (Wind slanted)
            val windSlope = (windFrequency - 0.5f) * 3500f
            for (d in particleSystem.drops) {
                val headX = d.x
                val headY = d.y
                
                val tailX = d.x - (windSlope / d.speed) * d.length
                val tailY = d.y - d.length
                
                val w = d.width.dp.toPx()

                val dropColor = Color.White.copy(alpha = d.alpha * 0.5f)

                // Draw bottom round part (head)
                drawCircle(
                    color = dropColor,
                    radius = w / 2f,
                    center = Offset(headX, headY)
                )

                // Draw sharp tail
                particleSystem.dropPath.reset()
                particleSystem.dropPath.moveTo(tailX, tailY)

                val dx = headX - tailX
                val dy = headY - tailY
                val len = kotlin.math.sqrt(dx * dx + dy * dy)
                if (len > 0) {
                    val nx = -dy / len * (w / 2f)
                    val ny = dx / len * (w / 2f)

                    particleSystem.dropPath.lineTo(headX + nx, headY + ny)
                    particleSystem.dropPath.lineTo(headX - nx, headY - ny)
                    particleSystem.dropPath.close()

                    drawPath(
                        path = particleSystem.dropPath,
                        color = dropColor
                    )
                }
            }

            // Draw splashing droplets
            for (s in particleSystem.splashes) {
                drawCircle(
                    color = Color(0xFFE2E8F0).copy(alpha = s.alpha),
                    radius = s.size.dp.toPx(),
                    center = Offset(s.x, s.y)
                )
            }
            
            // Draw Lightning Bolts
            if (lightningAlpha > 0f) {
                val boltAlpha = lightningAlpha.coerceIn(0f, 1f)
                val bright = 0.6f + kotlin.random.Random.nextFloat() * 0.4f
                for (bolt in particleSystem.activeBolts) {
                    for (i in 0 until bolt.size - 1) {
                        drawLine(
                            color = Color.White.copy(alpha = boltAlpha * bright),
                            start = bolt[i],
                            end = bolt[i+1],
                            strokeWidth = 3.dp.toPx() * (1f - (i.toFloat() / bolt.size)),
                            cap = StrokeCap.Round
                        )
                    }
                }
            }
        }

        // 2. Beautiful top radial gradient layer from Clean Minimalism
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0x1838BDF8), Color.Transparent),
                        center = Offset(x = 540f, y = -150f),
                        radius = 800f
                    )
                )
                .pointerInput(Unit) {} // non-interactive
        )

        // 3. Dynamic Lightning overlay (Lightning flicker)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFEAF4FF).copy(alpha = lightningAlpha.coerceIn(0f, 1f)))
                .testTag("lightning_flash")
        )

        // Lock Button Overlay
        AnimatedVisibility(
            visible = !showHud && lockButtonVisible,
            enter = fadeIn(animationSpec = tween(300)),
            exit = fadeOut(animationSpec = tween(500)),
            modifier = Modifier.align(Alignment.CenterStart)
        ) {
            Box(
                modifier = Modifier
                    .padding(start = 16.dp)
                    .size(48.dp)
                    .background(Color(0x40000000), shape = CircleShape)
                    .border(1.dp, Color(0x30FFFFFF), CircleShape)
                    .clickable {
                        isLocked = !isLocked
                        if (!isLocked) {
                            showHud = true
                        }
                        triggerTouchHaptic(context, 0.4f)
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isLocked) Icons.Default.Lock else Icons.Default.LockOpen,
                    contentDescription = if (isLocked) "Unlock" else "Lock",
                    tint = if (isLocked) Color(0xFF38BDF8) else Color(0xFF94A3B8),
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        // 4. Clean Minimalism Immersive HUD
        AnimatedVisibility(
            visible = showHud,
            enter = fadeIn(animationSpec = tween(500)),
            exit = fadeOut(animationSpec = tween(700)),
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.25f)) // subtle dark vignette
            ) {
                // Header Block (Atmosphere / Just the Rain)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 24.dp, vertical = 20.dp)
                        .align(Alignment.TopCenter),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "ATMOSPHERE",
                            color = Color(0xFF94A3B8), // Slate-400
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 2.sp,
                            fontFamily = FontFamily.SansSerif,
                            modifier = Modifier.testTag("atmosphere_label")
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Just the Rain",
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Light,
                            letterSpacing = (-0.5).sp,
                            fontFamily = FontFamily.SansSerif,
                            modifier = Modifier.testTag("app_title")
                        )
                    }

                    // Top-right controls: Haptics and Settings
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .background(Color(0xFF0F172A).copy(alpha = 0.5f), shape = RoundedCornerShape(16.dp))
                                .border(
                                    1.dp,
                                    Color(0xFF1E293B),
                                    shape = RoundedCornerShape(16.dp)
                                )
                                .clickable {
                                    val newVal = !RainState.hapticsEnabled.value
                                    RainState.hapticsEnabled.value = newVal
                                    if (newVal) triggerTouchHaptic(context, 0.4f)
                                }
                                .testTag("haptics_toggle_button"),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Vibration,
                                contentDescription = "Toggle Haptics",
                                tint = if (hapticsEnabled) Color(0xFF38BDF8) else Color(0xFF94A3B8),
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .background(Color(0xFF0F172A).copy(alpha = 0.5f), shape = RoundedCornerShape(16.dp))
                                .border(
                                    1.dp,
                                    Color(0xFF1E293B),
                                    shape = RoundedCornerShape(16.dp)
                                )
                                .clickable {
                                    isLocked = true
                                    showHud = false
                                    lockButtonVisible = false
                                    triggerTouchHaptic(context, 0.4f)
                                }
                                .testTag("lock_toggle_button"),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.LockOpen,
                                contentDescription = "Lock Screen",
                                tint = Color(0xFF94A3B8),
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .background(Color(0xFF0F172A).copy(alpha = 0.5f), shape = RoundedCornerShape(16.dp))
                                .border(
                                    1.dp,
                                    Color(0xFF1E293B),
                                    shape = RoundedCornerShape(16.dp)
                                )
                                .clickable {
                                    showThemesPage = true
                                    triggerTouchHaptic(context, 0.4f)
                                }
                                .testTag("themes_toggle_button"),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Palette,
                                contentDescription = "Themes",
                                tint = if (showThemesPage) Color(0xFF38BDF8) else Color(0xFF94A3B8),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .background(Color(0xFF0F172A).copy(alpha = 0.5f), shape = RoundedCornerShape(16.dp))
                                .border(
                                    1.dp,
                                    Color(0xFF1E293B),
                                    shape = RoundedCornerShape(16.dp)
                                )
                                .clickable {
                                    showStatsOverlay = !showStatsOverlay
                                    triggerTouchHaptic(context, 0.4f)
                                }
                                .testTag("info_toggle_button"),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Toggle Audio Diagnostics",
                                tint = if (showStatsOverlay) Color(0xFF38BDF8) else Color(0xFF94A3B8),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                // Central Storm Intensity Indicator & Interactive Sliders
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    if (!showStatsOverlay) {
                        // Central Display
                        Text(
                            text = "${(rainIntensity * 100).toInt()}%",
                            color = Color(0xFFE0F2FE),
                            fontSize = 68.sp,
                            fontWeight = FontWeight.ExtraLight,
                            fontFamily = FontFamily.SansSerif,
                            modifier = Modifier.testTag("intensity_text")
                        )
                        Text(
                            text = "RAIN INTENSITY",
                            color = Color(0xFF64748B),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            letterSpacing = 3.sp,
                            fontFamily = FontFamily.SansSerif,
                            modifier = Modifier.testTag("storm_intensity_label")
                        )
                    }
                }

                // Left side: Rain Intensity Vertical Visualizer & Slider
                Column(
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .padding(start = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.WaterDrop,
                        contentDescription = "Intensity Up",
                        tint = Color(0xFF64748B),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    // Vertical Bar Slider Track
                    Box(
                        modifier = Modifier
                            .width(32.dp)
                            .height(128.dp)
                            .background(Color(0xFF1E293B), shape = RoundedCornerShape(100.dp))
                            .pointerInput(Unit) {
                                detectDragGestures { change, dragAmount ->
                                    change.consume()
                                    val deltaY = -dragAmount.y / 128.dp.toPx()
                                    val newInt = (RainState.rainIntensity.value + deltaY).coerceIn(0f, 1f)
                                    RainState.rainIntensity.value = newInt
                                    RainState.isStormMode.value = newInt > 0.55f
                                    triggerTouchHaptic(context, 0.2f)
                                }
                            },
                        contentAlignment = Alignment.BottomCenter
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .fillMaxHeight(rainIntensity)
                                .background(Color(0xFF38BDF8), shape = RoundedCornerShape(100.dp))
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Icon(
                        imageVector = Icons.Default.WaterDrop, // Could use something else for low intensity if available, but WaterDrop works for now
                        contentDescription = "Intensity Down",
                        tint = Color(0xFF64748B),
                        modifier = Modifier.size(10.dp)
                    )
                }

                // Right side: Volume Vertical Visualizer & Slider
                Column(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(end = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowUp,
                        contentDescription = "Volume Up",
                        tint = Color(0xFF64748B),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    // Vertical Bar Slider Track
                    Box(
                        modifier = Modifier
                            .width(32.dp)
                            .height(128.dp)
                            .background(Color(0xFF1E293B), shape = RoundedCornerShape(100.dp))
                            .pointerInput(Unit) {
                                detectDragGestures { change, dragAmount ->
                                    change.consume()
                                    val deltaY = -dragAmount.y / 128.dp.toPx()
                                    val newVol = (RainState.volume.value + deltaY).coerceIn(0f, 1f)
                                    RainState.volume.value = newVol
                                    triggerTouchHaptic(context, 0.2f)
                                }
                            },
                        contentAlignment = Alignment.BottomCenter
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .fillMaxHeight(volume)
                                .background(Color(0xFF60A5FA), shape = RoundedCornerShape(100.dp))
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Icon(
                        imageVector = Icons.Default.VolumeDown,
                        contentDescription = "Volume Down",
                        tint = Color(0xFF64748B),
                        modifier = Modifier.size(14.dp)
                    )
                }

                // Footer Section
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 24.dp)
                        .background(
                            color = Color(0x990F172A),
                            shape = RoundedCornerShape(32.dp)
                        )
                        .border(
                            1.dp,
                            Color(0x40FFFFFF),
                            shape = RoundedCornerShape(32.dp)
                        )
                        .padding(horizontal = 20.dp, vertical = 20.dp)
                        .align(Alignment.BottomCenter),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Swipe horizontal indicators (Soft Rain <---> Thunderstorm)
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            modifier = Modifier.align(Alignment.CenterStart),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Arrow back",
                                tint = if (!isStormMode) Color(0xFF94A3B8) else Color(0xFF475569),
                                modifier = Modifier.size(12.dp)
                            )
                            Text(
                                text = "Light Rain",
                                color = if (!isStormMode) Color(0xFF94A3B8) else Color(0xFF475569),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                letterSpacing = 1.sp,
                                fontFamily = FontFamily.SansSerif
                            )
                        }

                        // Center handle for control panel
                        IconButton(
                            onClick = { 
                                showControlPanel = !showControlPanel 
                                triggerTouchHaptic(context, 0.4f)
                            },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = if (showControlPanel) Icons.Default.KeyboardArrowDown else Icons.Default.KeyboardArrowUp,
                                contentDescription = "Toggle Control Panel",
                                tint = Color(0xFF64748B),
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Row(
                            modifier = Modifier.align(Alignment.CenterEnd),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "Heavy Rain",
                                color = if (isStormMode) Color(0xFF94A3B8) else Color(0xFF475569),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                letterSpacing = 1.sp,
                                fontFamily = FontFamily.SansSerif
                            )
                            Icon(
                                imageVector = Icons.Default.ArrowForward,
                                contentDescription = "Arrow forward",
                                tint = if (isStormMode) Color(0xFF94A3B8) else Color(0xFF475569),
                                modifier = Modifier.size(12.dp)
                            )
                        }
                    }

                    AnimatedVisibility(
                        visible = showControlPanel,
                        enter = fadeIn(animationSpec = tween(300)) + expandVertically(animationSpec = tween(300)),
                        exit = fadeOut(animationSpec = tween(300)) + shrinkVertically(animationSpec = tween(300))
                    ) {
                        Column {
                            Spacer(modifier = Modifier.height(20.dp))

                            // Functional shortcut cards
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                // Sleep Mode control card
                                Row(
                                    modifier = Modifier
                                        .weight(1f)
                                        .background(Color(0x200F172A), shape = RoundedCornerShape(16.dp))
                                        .border(
                                            1.dp,
                                            Color(0xFF1E293B),
                                            shape = RoundedCornerShape(16.dp)
                                        )
                                        .clickable {
                                            backgroundSleepMode = !backgroundSleepMode
                                            triggerTouchHaptic(context, 0.6f)
                                        }
                                        .padding(14.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Icon(
                                            imageVector = Icons.Default.NightsStay,
                                            contentDescription = "Sleep Mode",
                                            tint = if (backgroundSleepMode) Color(0xFF60A5FA) else Color(0xFF475569),
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.height(10.dp))
                                        Text(
                                            text = "Sleep Mode",
                                            color = Color.White,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = if (backgroundSleepMode) "Active" else "Disabled",
                                            color = Color(0xFF64748B),
                                            fontSize = 10.sp,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }

                                    // Active green dot status indicator
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .background(
                                                color = if (backgroundSleepMode) Color(0xFF22C55E) else Color(0xFF64748B),
                                                shape = CircleShape
                                            )
                                    )
                                }

                                // Thunder control card
                                Row(
                                    modifier = Modifier
                                        .weight(1f)
                                        .background(Color(0x200F172A), shape = RoundedCornerShape(16.dp))
                                        .border(
                                            1.dp,
                                            Color(0xFF1E293B),
                                            shape = RoundedCornerShape(16.dp)
                                        )
                                        .clickable {
                                            RainState.thunderEnabled.value = !RainState.thunderEnabled.value
                                            triggerTouchHaptic(context, 0.6f)
                                        }
                                        .padding(14.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Icon(
                                            imageVector = Icons.Default.FlashOn,
                                            contentDescription = "Thunder",
                                            tint = if (thunderEnabled) Color(0xFF60A5FA) else Color(0xFF475569),
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.height(10.dp))
                                        Text(
                                            text = "Thunder",
                                            color = Color.White,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = if (thunderEnabled) "Active" else "Disabled",
                                            color = Color(0xFF64748B),
                                            fontSize = 10.sp,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                    
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .background(
                                                color = if (thunderEnabled) Color(0xFF22C55E) else Color(0xFF64748B),
                                                shape = CircleShape
                                            )
                                    )
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            // Bottom row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                // Binaural / Playback control card
                                Row(
                                    modifier = Modifier
                                        .weight(1f)
                                        .background(Color(0x200F172A), shape = RoundedCornerShape(16.dp))
                                        .border(
                                            1.dp,
                                            Color(0xFF1E293B),
                                            shape = RoundedCornerShape(16.dp)
                                        )
                                        .clickable {
                                            RainState.isMuted.value = !RainState.isMuted.value
                                            triggerTouchHaptic(context, 0.8f)
                                        }
                                        .padding(14.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    val isMuted by RainState.isMuted.collectAsStateWithLifecycle()
                                    Column(modifier = Modifier.weight(1f)) {
                                        Icon(
                                            imageVector = if (!isMuted) Icons.Default.Check else Icons.Default.Clear,
                                            contentDescription = "Mute Toggle",
                                            tint = if (!isMuted) Color(0xFF60A5FA) else Color(0xFF475569),
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.height(10.dp))
                                        Text(
                                            text = "Audio",
                                            color = Color.White,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = if (!isMuted) "Active" else "Muted",
                                            color = Color(0xFF64748B),
                                            fontSize = 10.sp
                                        )
                                    }

                                    // HQ Status Badge
                                    Box(
                                        modifier = Modifier
                                            .border(1.dp, Color(0xFF334155), shape = RoundedCornerShape(4.dp))
                                            .padding(horizontal = 4.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "HQ",
                                            color = Color(0xFF64748B),
                                            fontSize = 8.sp,
                                            fontWeight = FontWeight.Black
                                        )
                                    }
                                }

                                // Ambient Thunder control card
                                val ambientThunderEnabled by RainState.ambientThunderEnabled.collectAsStateWithLifecycle()
                                Row(
                                    modifier = Modifier
                                        .weight(1f)
                                        .background(Color(0x200F172A), shape = RoundedCornerShape(16.dp))
                                        .border(
                                            1.dp,
                                            Color(0xFF1E293B),
                                            shape = RoundedCornerShape(16.dp)
                                        )
                                        .clickable {
                                            RainState.ambientThunderEnabled.value = !RainState.ambientThunderEnabled.value
                                            triggerTouchHaptic(context, 0.6f)
                                        }
                                        .padding(14.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Icon(
                                            imageVector = Icons.Default.CloudQueue,
                                            contentDescription = "Ambient Thunder",
                                            tint = if (ambientThunderEnabled) Color(0xFF60A5FA) else Color(0xFF475569),
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.height(10.dp))
                                        Text(
                                            text = "Amb. Thunder",
                                            color = Color.White,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = if (ambientThunderEnabled) "Active" else "Disabled",
                                            color = Color(0xFF64748B),
                                            fontSize = 10.sp
                                        )
                                    }
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .background(
                                                color = if (ambientThunderEnabled) Color(0xFF22C55E) else Color(0xFF64748B),
                                                shape = CircleShape
                                            )
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Themes Page Overlay
        AnimatedVisibility(
            visible = showThemesPage,
            enter = fadeIn(animationSpec = tween(300)),
            exit = fadeOut(animationSpec = tween(300))
        ) {
            ThemesScreen(
                onBack = { showThemesPage = false }
            )
        }
        
        // Interactive Diagnostic Stats Overlay
        if (showStatsOverlay) {
            val levelSliderInteractionSource = remember { MutableInteractionSource() }
            val isLevelSliderDragged by levelSliderInteractionSource.collectIsDraggedAsState()
            val isLevelSliderPressed by levelSliderInteractionSource.collectIsPressedAsState()
            val isLevelSliderActive = isLevelSliderDragged || isLevelSliderPressed

            val radiusSliderInteractionSource = remember { MutableInteractionSource() }
            val isRadiusSliderDragged by radiusSliderInteractionSource.collectIsDraggedAsState()
            val isRadiusSliderPressed by radiusSliderInteractionSource.collectIsPressedAsState()
            val isRadiusSliderActive = isRadiusSliderDragged || isRadiusSliderPressed

            val durationSliderInteractionSource = remember { MutableInteractionSource() }
            val isDurationSliderDragged by durationSliderInteractionSource.collectIsDraggedAsState()
            val isDurationSliderPressed by durationSliderInteractionSource.collectIsPressedAsState()
            val isDurationSliderActive = isDurationSliderDragged || isDurationSliderPressed

            val isSliderActive = isLevelSliderActive || isRadiusSliderActive || isDurationSliderActive

            val overlayDimAlpha by androidx.compose.animation.core.animateFloatAsState(targetValue = if (isSliderActive) 0f else 0.6f, label = "")
            val contentAlpha by androidx.compose.animation.core.animateFloatAsState(targetValue = if (isSliderActive) 0f else 1f, label = "")
            val cardBgAlpha by androidx.compose.animation.core.animateFloatAsState(targetValue = if (isSliderActive) 0f else 0.95f, label = "")
            val cardBorderAlpha by androidx.compose.animation.core.animateFloatAsState(targetValue = if (isSliderActive) 0f else 1f, label = "")

            BackHandler { showStatsOverlay = false }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = overlayDimAlpha))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = { showStatsOverlay = false }
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (isLevelSliderActive && rippleLevel > 0.01f) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight(rippleLevel)
                            .align(Alignment.BottomCenter)
                            .background(Color(0xFF60A5FA).copy(alpha = 0.2f))
                            .border(1.dp, Color(0xFF60A5FA).copy(alpha = 0.5f))
                    )
                }

                Card(
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .testTag("stats_card")
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = {}
                        ),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0B0F19).copy(alpha = cardBgAlpha)),
                    shape = RoundedCornerShape(20.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E293B).copy(alpha = cardBorderAlpha))
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Column(modifier = Modifier.alpha(contentAlpha)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Settings",
                                    color = Color(0xFF38BDF8),
                                    fontSize = 13.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold
                                )
                                IconButton(
                                    onClick = { showStatsOverlay = false },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close, // placeholder for tiny close
                                        contentDescription = "Close",
                                        tint = Color(0xFF64748B),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            DiagnosticRow(label = "Active Audio Buffer", value = "4096 Bytes")
                            DiagnosticRow(label = "Sample Resolution", value = "44.1kHz stereo PCM")
                            DiagnosticRow(label = "Dynamic Rain Rate", value = "${(rainIntensity * 100).toInt()}%")
                            DiagnosticRow(label = "Wind Modulation", value = "${(kotlin.math.abs(windFrequency - 0.5f) * 200).toInt()}%")
                            DiagnosticRow(label = "Volume Setting", value = "${(volume * 100).toInt()}%")
                            DiagnosticRow(label = "Lightning Interval", value = if (thunderEnabled) "7s - 18s" else "Disabled")
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("Ripple Animation Level", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                        androidx.compose.material3.Slider(
                            value = rippleLevel,
                            onValueChange = { RainState.rippleLevel.value = it },
                            valueRange = 0f..1f,
                            modifier = Modifier.fillMaxWidth().alpha(if (isSliderActive && !isLevelSliderActive) 0f else 1f),
                            interactionSource = levelSliderInteractionSource
                        )
                        
                        Column(modifier = Modifier.alpha(contentAlpha)) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Ripple Radius", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                        androidx.compose.material3.Slider(
                            value = rippleRadiusMulti,
                            onValueChange = { RainState.rippleRadiusMultiplier.value = it },
                            valueRange = 0f..1f,
                            modifier = Modifier.fillMaxWidth().alpha(if (isSliderActive && !isRadiusSliderActive) 0f else 1f),
                            interactionSource = radiusSliderInteractionSource
                        )
                        
                        Column(modifier = Modifier.alpha(contentAlpha)) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Ripple Animation Duration", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                        androidx.compose.material3.Slider(
                            value = rippleDurationMulti,
                            onValueChange = { RainState.rippleDurationMultiplier.value = it },
                            valueRange = 0f..1f,
                            modifier = Modifier.fillMaxWidth().alpha(if (isSliderActive && !isDurationSliderActive) 0f else 1f),
                            interactionSource = durationSliderInteractionSource
                        )
                        
                        Column(modifier = Modifier.alpha(contentAlpha)) {
                            Spacer(modifier = Modifier.height(8.dp))
                            androidx.compose.material3.Button(
                                onClick = { 
                                    imagePickerLauncher.launch(
                                        androidx.activity.result.PickVisualMediaRequest(
                                            androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia.ImageOnly
                                        )
                                    ) 
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B))
                            ) {
                                Icon(imageVector = Icons.Default.Image, contentDescription = null, modifier = Modifier.size(18.dp), tint = Color.White)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Set Background Image", color = Color.White, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DiagnosticRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, color = Color(0xFF64748B), fontSize = 11.sp, fontFamily = FontFamily.Monospace)
        Text(text = value, color = Color(0xFFE2E8F0), fontSize = 11.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
    }
}



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThemesScreen(onBack: () -> Unit) {
    BackHandler { onBack() }
    
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color(0xFF0B0F19),
        topBar = {
            TopAppBar(
                title = { Text("Atmospheres", color = Color.White, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF0B0F19),
                    titleContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(DummyThemeData.themes) { theme ->
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(9f / 16f)
                            .clickable { /* Select theme */ },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155))
                    ) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            // Dummy content for now
                            Icon(
                                imageVector = Icons.Default.Palette,
                                contentDescription = null,
                                tint = Color(0xFF64748B),
                                modifier = Modifier.size(48.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = theme.name,
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "3D Pack",
                        color = Color(0xFF38BDF8),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
