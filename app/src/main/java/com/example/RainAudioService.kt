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
import android.media.MediaPlayer
import android.media.SoundPool
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentLinkedQueue
import kotlin.random.Random

object RainState {
    val isPlaying = MutableStateFlow(false)
    val rainIntensity = MutableStateFlow(0.4f)  // 0.0 to 1.0
    val windFrequency = MutableStateFlow(0.3f)  // 0.0 to 1.0
    val volume = MutableStateFlow(0.5f)         // 0.0 to 1.0
    val touchIntensity = MutableStateFlow(0.0f) // 0.0 to 1.0
    val isStormMode = MutableStateFlow(false)
    val thunderEnabled = MutableStateFlow(true)
    val ambientThunderEnabled = MutableStateFlow(false)
    val hapticsEnabled = MutableStateFlow(true)
    val isMuted = MutableStateFlow(false)
    val rippleLevel = MutableStateFlow(0.33f) // 0.0 to 1.0
    val rippleRadiusMultiplier = MutableStateFlow(0.5f) // 0.0 to 1.0
    val rippleDurationMultiplier = MutableStateFlow(0.5f) // 0.0 to 1.0
    val rippleShapeMultiplier = MutableStateFlow(0.35f) // 0.0 to 1.0
    val rainDropSizeMultiplier = MutableStateFlow(0.5f) // 0.0 to 1.0
    val backgroundImageUri = MutableStateFlow<android.net.Uri?>(null)

    // Queue for instant touch-triggered splashes: Pair(pan, volumeFactor)
    val splashQueue = ConcurrentLinkedQueue<Pair<Float, Float>>()

    // Manual thunder flash and sound trigger
    @Volatile var triggerThunder: Boolean = false
}

class RainAudioService : Service() {

    private val job = SupervisorJob()
    private val serviceScope = CoroutineScope(Dispatchers.Default + job)

    @Volatile private var isRunning = false

    private var mpLightRain: LoopingAudioTrack? = null
    private var mpHeavyRain: LoopingAudioTrack? = null
    private var mpWind: LoopingAudioTrack? = null
    private var mpAlwaysOnThunder: LoopingAudioTrack? = null

    private var soundPool: SoundPool? = null
    
    private val dropIds = mutableListOf<Int>()
    private val thunderResIds = listOf(R.raw.thunder_1, R.raw.thunder_2, R.raw.thunder_3, R.raw.thunder_4, R.raw.thunder_5)
    private val activeThunderPlayers = java.util.concurrent.ConcurrentLinkedQueue<android.media.MediaPlayer>()

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
        initAudio()
    }

    private fun initAudio() {
        // Initialize MediaPlayers
        mpLightRain = LoopingAudioTrack(this, R.raw.light_rain)
        mpHeavyRain = LoopingAudioTrack(this, R.raw.heavy_rain)
        mpWind = LoopingAudioTrack(this, R.raw.wind)
        mpAlwaysOnThunder = LoopingAudioTrack(this, R.raw.always_on_thunder)

        // Initialize SoundPool
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_MEDIA)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
        soundPool = SoundPool.Builder()
            .setMaxStreams(25)
            .setAudioAttributes(audioAttributes)
            .build()

        soundPool?.let { sp ->

            dropIds.add(sp.load(this, R.raw.drop_1, 1))
            dropIds.add(sp.load(this, R.raw.drop_2, 1))
            dropIds.add(sp.load(this, R.raw.drop_3, 1))
            dropIds.add(sp.load(this, R.raw.drop_4, 1))
            dropIds.add(sp.load(this, R.raw.drop_5, 1))
            dropIds.add(sp.load(this, R.raw.drop_6, 1))
        }
    }

    

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                if (!isRunning) {
                    startForegroundService()
                    startPlayback()
                    RainState.isPlaying.value = true
                }
            }
            ACTION_STOP -> {
                stopPlayback()
                RainState.isPlaying.value = false
                stopSelf()
            }
            ACTION_TOGGLE -> {
                if (isRunning) {
                    stopPlayback()
                    RainState.isPlaying.value = false
                    stopForeground(STOP_FOREGROUND_REMOVE)
                } else {
                    startForegroundService()
                    startPlayback()
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
            .setContentText("Playing high-quality ambient rain and wind...")
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setOngoing(true)
            .setContentIntent(pendingIntent)
            .addAction(android.R.drawable.ic_media_pause, "Turn Off", togglePendingIntent)
            .setCategory(Notification.CATEGORY_SERVICE)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .build()
    }

    private fun startPlayback() {
        if (isRunning) return
        isRunning = true

        mpLightRain?.start()
        mpHeavyRain?.start()
        mpWind?.start()
        mpAlwaysOnThunder?.start()

        serviceScope.launch {
            audioLoop()
        }
    }

    private fun stopPlayback() {
        isRunning = false
        mpLightRain?.pause()
        mpHeavyRain?.pause()
        mpWind?.pause()
        mpAlwaysOnThunder?.pause()
    }

    private suspend fun audioLoop() {
        while (isRunning) {
            val isMuted = RainState.isMuted.value
            val masterVol = if (isMuted) 0f else RainState.volume.value
            val rainInt = RainState.rainIntensity.value
            val windFreq = RainState.windFrequency.value
            val stormMode = RainState.isStormMode.value
            val thunderOn = RainState.thunderEnabled.value
            val ambientThunderOn = RainState.ambientThunderEnabled.value

            // Mix light rain and heavy rain based on intensity
            mpLightRain?.setVolume(masterVol) // Light rain is always full volume
            mpHeavyRain?.setVolume(masterVol * rainInt) // Heavy rain scales with rain intensity
            
            // Wind volume is 0 at center (0.5), and 100% at extremes (0.0 or 1.0)
            val windVol = masterVol * (kotlin.math.abs(windFreq - 0.5f) * 2f)
            mpWind?.setVolume(windVol)

            // Background thunder rumble
            if (ambientThunderOn) {
                mpAlwaysOnThunder?.setVolume(masterVol * 0.7f)
            } else {
                mpAlwaysOnThunder?.setVolume(0f)
            }

            // Update active thunder players volumes
            for (mp in activeThunderPlayers) {
                try {
                    mp.setVolume(masterVol, masterVol)
                } catch (e: Exception) {}
            }

            // Handle Thunder events
            if (RainState.triggerThunder && thunderOn) {
                RainState.triggerThunder = false
                if (thunderResIds.isNotEmpty()) {
                    try {
                        val resId = thunderResIds[Random.nextInt(thunderResIds.size)]
                        val mp = MediaPlayer.create(this@RainAudioService, resId)
                        mp?.setVolume(masterVol, masterVol)
                        mp?.setOnCompletionListener { 
                            it.release()
                            activeThunderPlayers.remove(it)
                        }
                        mp?.let { activeThunderPlayers.add(it) }
                        mp?.start()
                    } catch (e: Exception) {
                        // Safe catch
                    }
                }
            }

            // Process Splash Queue
            while (RainState.splashQueue.isNotEmpty()) {
                val splashParams = RainState.splashQueue.poll() ?: break
                val pan = splashParams.first
                val touchInt = splashParams.second
                
                if (dropIds.isNotEmpty()) {
                    val dropId = dropIds[Random.nextInt(dropIds.size)]
                    
                    val leftVol = masterVol * (1f - pan) * (0.3f + touchInt * 0.7f)
                    val rightVol = masterVol * (pan) * (0.3f + touchInt * 0.7f)
                    
                    soundPool?.play(dropId, leftVol, rightVol, 0, 0, 0.9f + Random.nextFloat() * 0.2f)
                }
            }

            delay(50) // Update volumes and check queues at ~20Hz
        }
    }

    

    override fun onDestroy() {
        super.onDestroy()
        stopPlayback()
        
        mpLightRain?.release()
        mpHeavyRain?.release()
        mpWind?.release()
        mpAlwaysOnThunder?.release()
        
        for (mp in activeThunderPlayers) {
            try {
                mp.release()
            } catch (e: Exception) {}
        }
        activeThunderPlayers.clear()

        soundPool?.release()
        soundPool = null
        
        serviceScope.cancel()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}

class LoopingAudioTrack(private val context: Context, private val resId: Int) {
    private var mp: MediaPlayer? = null
    private var currentVol = 0f

    init {
        try {
            mp = MediaPlayer.create(context, resId)
            mp?.isLooping = true
            mp?.setVolume(currentVol, currentVol)
        } catch (e: Exception) {
            mp = null
        }
    }

    fun start() {
        mp?.start()
    }

    fun pause() {
        mp?.pause()
    }

    fun setVolume(vol: Float) {
        currentVol = vol
        mp?.setVolume(vol, vol)
    }

    fun release() {
        mp?.release()
        mp = null
    }
}
