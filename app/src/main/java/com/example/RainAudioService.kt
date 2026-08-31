package com.example

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentLinkedQueue
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.sin
import kotlin.random.Random

object RainState {
    val isPlaying = MutableStateFlow(false)
    val rainIntensity = MutableStateFlow(0.4f)  // 0.0 to 1.0
    val windFrequency = MutableStateFlow(0.3f)  // 0.0 to 1.0
    val volume = MutableStateFlow(0.5f)         // 0.0 to 1.0
    val touchIntensity = MutableStateFlow(0.0f) // 0.0 to 1.0
    val isStormMode = MutableStateFlow(false)
    val thunderEnabled = MutableStateFlow(true)
    val hapticsEnabled = MutableStateFlow(true)

    // Queue for instant touch-triggered splashes: Pair(pan, volumeFactor)
    val splashQueue = ConcurrentLinkedQueue<Pair<Float, Float>>()

    // Manual thunder flash and sound trigger
    @Volatile var triggerThunder: Boolean = false
}

class RainAudioService : Service() {

    private val job = SupervisorJob()
    private val serviceScope = CoroutineScope(Dispatchers.Default + job)

    private var audioTrack: AudioTrack? = null
    private var synthesisThread: Thread? = null
    @Volatile private var isRunning = false

    companion object {
        private const val CHANNEL_ID = "rain_ambient_channel"
        private const val NOTIFICATION_ID = 8827

        const val ACTION_START = "com.example.action.START"
        const val ACTION_STOP = "com.example.action.STOP"
        const val ACTION_TOGGLE = "com.example.action.TOGGLE"
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                if (!isRunning) {
                    startForegroundService()
                    startSynthesis()
                    RainState.isPlaying.value = true
                }
            }
            ACTION_STOP -> {
                stopSynthesis()
                RainState.isPlaying.value = false
                stopSelf()
            }
            ACTION_TOGGLE -> {
                if (isRunning) {
                    stopSynthesis()
                    RainState.isPlaying.value = false
                    stopForeground(STOP_FOREGROUND_REMOVE)
                } else {
                    startForegroundService()
                    startSynthesis()
                    RainState.isPlaying.value = true
                }
            }
        }
        return START_NOT_STICKY
    }

    private fun startForegroundService() {
        val notification = createNotification()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Rain Ambient Sleep Service"
            val descriptionText = "Plays background sleep audio for Just the Rain"
            val importance = NotificationManager.IMPORTANCE_LOW
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
                setShowBadge(false)
            }
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(): Notification {
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val toggleIntent = Intent(this, RainAudioService::class.java).apply {
            action = ACTION_STOP
        }
        val togglePendingIntent = PendingIntent.getService(
            this, 1, toggleIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Just the Rain")
            .setContentText("Synthesizing ambient rain and wind...")
            .setSmallIcon(android.R.drawable.ic_media_play) // Standard system icon, or fallback
            .setOngoing(true)
            .setContentIntent(pendingIntent)
            .addAction(android.R.drawable.ic_media_pause, "Turn Off", togglePendingIntent)
            .setCategory(Notification.CATEGORY_SERVICE)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .build()
    }

    private fun startSynthesis() {
        if (isRunning) return
        isRunning = true

        val sampleRate = 44100
        val minBufferSize = AudioTrack.getMinBufferSize(
            sampleRate,
            AudioFormat.CHANNEL_OUT_STEREO,
            AudioFormat.ENCODING_PCM_16BIT
        )
        // Use a 4KB buffer for snappy responses (~1024 stereo frames)
        val bufferSize = (minBufferSize * 2).coerceAtLeast(4096)

        audioTrack = AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build()
            )
            .setAudioFormat(
                AudioFormat.Builder()
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setSampleRate(sampleRate)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_STEREO)
                    .build()
            )
            .setBufferSizeInBytes(bufferSize)
            .setTransferMode(AudioTrack.MODE_STREAM)
            .build()

        synthesisThread = Thread {
            synthesizeAudioLoop(bufferSize)
        }.apply {
            priority = Thread.MAX_PRIORITY
            start()
        }
    }

    private fun stopSynthesis() {
        isRunning = false
        synthesisThread?.join()
        synthesisThread = null
        audioTrack?.apply {
            try {
                stop()
                release()
            } catch (e: Exception) {
                // Ignore
            }
        }
        audioTrack = null
    }

    private fun synthesizeAudioLoop(bufferSize: Int) {
        val track = audioTrack ?: return
        val buffer = ShortArray(bufferSize / 2) // each short is 1 sample, stereo = 2 shorts per frame
        track.play()

        // Synthesis states
        var windFilterState = 0f
        var cutoffFilter = 0.05f
        var windPanAngle = 0f
        var windGustPhase = 0f

        // Storm rumble states
        var thunderFilterState = 0f
        var thunderIntensity = 0f

        // Preallocated Pool of active raindrops for performance
        val maxRaindrops = 64
        class Raindrop {
            var active = false
            var age = 0
            var maxAge = 0
            var panLeft = 0.5f
            var panRight = 0.5f
            var frequency = 800f
            var damping = 8f
            var amplitude = 0.05f
        }
        val activeDrops = Array(maxRaindrops) { Raindrop() }

        // Pool of touch interactive splashes
        val maxSplashDrops = 16
        val activeSplashes = Array(maxSplashDrops) { Raindrop() }

        while (isRunning) {
            // Read state parameters atomically (or read latest values from Flow)
            val currentVol = RainState.volume.value
            val currentRain = RainState.rainIntensity.value
            val currentWind = RainState.windFrequency.value
            val stormMode = RainState.isStormMode.value
            val currentTouch = RainState.touchIntensity.value

            // Handle manual thunder trigger
            if (RainState.triggerThunder) {
                RainState.triggerThunder = false
                thunderIntensity = 1.0f
            }

            // Probability of starting a raindrop in each sample frame
            // Scale by currentRain to control density
            val dropSpawnProbability = 0.0001f + currentRain * 0.015f

            // Fill the buffer with stereo samples
            var idx = 0
            while (idx < buffer.size) {
                // 1. Synthesize Wind (Filtered Pink/Brown noise)
                val whiteNoise = Random.nextFloat() * 2f - 1f
                // Low-pass filter frequency changes with windIntensity
                val windIntensity = Math.abs(currentWind - 0.5f) * 2f
                val targetCutoff = 0.01f + (windIntensity * 0.07f)
                cutoffFilter = cutoffFilter * 0.9999f + targetCutoff * 0.0001f
                windFilterState = windFilterState * (1f - cutoffFilter) + whiteNoise * cutoffFilter

                // Spatial stereo panning of wind (very slow breeze drifting)
                windPanAngle += 0.00002f
                val baseWindLeftPan = 1.0f - currentWind
                val drift = 0.2f * sin(windPanAngle)
                val windLeftPan = (baseWindLeftPan + drift).coerceIn(0f, 1f)
                val windRightPan = 1f - windLeftPan

                // Wind gust amplitude envelopes
                windGustPhase += 0.0001f * (0.5f + windIntensity * 2.5f)
                val gust = 0.3f + 0.7f * (0.5f + 0.5f * sin(windGustPhase) * cos(windGustPhase * 0.61f))
                
                // Base wind scale: higher storm = higher wind volume. Zero at currentWind = 0.5.
                val baseWindVol = (if (stormMode) 0.40f else 0.20f) * windIntensity
                val windOutVol = currentVol * baseWindVol * gust
                
                var outLeft = windFilterState * windLeftPan * windOutVol
                var outRight = windFilterState * windRightPan * windOutVol

                // 2. Synthesize Thunder Rumble
                if (thunderIntensity > 0f) {
                    val thunderWhite = Random.nextFloat() * 2f - 1f
                    // Heavy low pass filter (cutoff ~20Hz)
                    thunderFilterState = thunderFilterState * 0.98f + thunderWhite * 0.02f
                    // Rolling thunder spatial effect
                    val thunderSample = thunderFilterState * thunderIntensity * 0.45f * currentVol
                    outLeft += thunderSample * 0.6f
                    outRight += thunderSample * 0.6f

                    // Thunder decay: rolls on for several seconds
                    thunderIntensity -= 1f / 44100f / 3.5f // 3.5 second decay
                }

                // 3. Process Splash Queue (from user taps)
                while (RainState.splashQueue.isNotEmpty()) {
                    val splashParams = RainState.splashQueue.poll() ?: break
                    val pan = splashParams.first // -1f (left) to 1f (right)
                    val touchInt = splashParams.second

                    // Spawn multiple drops for a rich splash sound effect!
                    val numSplashDrops = (3 + (touchInt * 5).toInt()).coerceAtMost(5)
                    for (d in 0 until numSplashDrops) {
                        val slot = activeSplashes.indexOfFirst { !it.active }
                        if (slot != -1) {
                            val drop = activeSplashes[slot]
                            drop.active = true
                            drop.age = 0
                            // Splashes are low pitch, hollow, dripping sounds (150Hz - 450Hz)
                            drop.frequency = 120f + Random.nextFloat() * 320f + (1f - touchInt) * 200f
                            // Quick decay
                            drop.damping = 4.0f + Random.nextFloat() * 4.0f
                            drop.maxAge = (44100f * (0.05f + Random.nextFloat() * 0.15f)).toInt()
                            // Binaural stereo panning
                            val splashPan = (pan + 1f) / 2f // Convert to 0.0 to 1.0
                            val jitteredPan = (splashPan + (Random.nextFloat() * 0.2f - 0.1f)).coerceIn(0f, 1f)
                            drop.panLeft = 1f - jitteredPan
                            drop.panRight = jitteredPan
                            // Scale by touch intensity
                            drop.amplitude = (0.2f + Random.nextFloat() * 0.4f) * (0.3f + touchInt * 0.7f)
                        }
                    }
                }

                // 4. Spawn Random Background Raindrops
                if (Random.nextFloat() < dropSpawnProbability) {
                    val slot = activeDrops.indexOfFirst { !it.active }
                    if (slot != -1) {
                        val drop = activeDrops[slot]
                        drop.active = true
                        drop.age = 0
                        // Short raindrop clicks (500Hz to 1800Hz)
                        drop.frequency = 400f + Random.nextFloat() * 1200f - currentRain * 200f
                        // Damping is how quickly the droplet decays
                        drop.damping = 6.0f + Random.nextFloat() * 9.0f
                        // Duration (10ms to 120ms)
                        drop.maxAge = (44100f * (0.01f + Random.nextFloat() * 0.11f)).toInt()
                        // Fully random binaural spatial layout
                        val pan = Random.nextFloat()
                        drop.panLeft = 1f - pan
                        drop.panRight = pan
                        // Scale amplitude
                        drop.amplitude = 0.04f + Random.nextFloat() * 0.18f
                    }
                }

                // 5. Synthesize Active Background Raindrops
                var rainL = 0f
                var rainR = 0f
                for (i in 0 until maxRaindrops) {
                    val drop = activeDrops[i]
                    if (drop.active) {
                        val t = drop.age.toFloat() / 44100f
                        // Exponential decaying wave
                        val sample = sin(2f * PI.toFloat() * drop.frequency * t) * exp(-drop.damping * t * 100f) * drop.amplitude
                        rainL += sample * drop.panLeft
                        rainR += sample * drop.panRight

                        drop.age++
                        if (drop.age >= drop.maxAge) {
                            drop.active = false
                        }
                    }
                }

                // 6. Synthesize Active Interactive Splashes
                for (i in 0 until maxSplashDrops) {
                    val drop = activeSplashes[i]
                    if (drop.active) {
                        val t = drop.age.toFloat() / 44100f
                        val sample = sin(2f * PI.toFloat() * drop.frequency * t) * exp(-drop.damping * t * 100f) * drop.amplitude
                        rainL += sample * drop.panLeft
                        rainR += sample * drop.panRight

                        drop.age++
                        if (drop.age >= drop.maxAge) {
                            drop.active = false
                        }
                    }
                }

                // Accumulate wind, rain, and apply master volume
                val finalL = (outLeft + rainL * 0.8f) * currentVol
                val finalR = (outRight + rainR * 0.8f) * currentVol

                // Convert float to 16-bit PCM short (clamping to avoid clipping)
                val shortL = (finalL * 32767f).coerceIn(-32768f, 32767f).toInt().toShort()
                val shortR = (finalR * 32767f).coerceIn(-32768f, 32767f).toInt().toShort()

                buffer[idx++] = shortL
                buffer[idx++] = shortR
            }

            // Write chunk to AudioTrack
            track.write(buffer, 0, buffer.size)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        stopSynthesis()
        serviceScope.cancel()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
