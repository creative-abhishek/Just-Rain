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
    val hapticsEnabled = MutableStateFlow(true)

    // Queue for instant touch-triggered splashes: Pair(pan, volumeFactor)
    val splashQueue = ConcurrentLinkedQueue<Pair<Float, Float>>()

    // Manual thunder flash and sound trigger
    @Volatile var triggerThunder: Boolean = false
}

class RainAudioService : Service() {

    private val job = SupervisorJob()
    private val serviceScope = CoroutineScope(Dispatchers.Default + job)

    @Volatile private var isRunning = false

    private var mpBackgroundRain: MediaPlayer? = null
    private var mpLightRain: MediaPlayer? = null
    private var mpHeavyRain: MediaPlayer? = null
    private var mpWind1: MediaPlayer? = null
    private var mpHeavyWind1: MediaPlayer? = null
    private var mpAlwaysOnThunder: MediaPlayer? = null

    private var soundPool: SoundPool? = null
    private var thunderIds = mutableListOf<Int>()
    private var dropIds = mutableListOf<Int>()

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
        mpBackgroundRain = createLoopingMediaPlayer(R.raw.background_rain)
        mpLightRain = createLoopingMediaPlayer(R.raw.light_rain)
        mpHeavyRain = createLoopingMediaPlayer(R.raw.heavy_rain)
        mpWind1 = createLoopingMediaPlayer(R.raw.wind1)
        mpHeavyWind1 = createLoopingMediaPlayer(R.raw.heavy_wind_1)
        mpAlwaysOnThunder = createLoopingMediaPlayer(R.raw.always_on_thunder)

        // Initialize SoundPool
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_MEDIA)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
        soundPool = SoundPool.Builder()
            .setMaxStreams(10)
            .setAudioAttributes(audioAttributes)
            .build()

        soundPool?.let { sp ->
            thunderIds.add(sp.load(this, R.raw.heavy_thunder_1, 1))
            thunderIds.add(sp.load(this, R.raw.thunder_2, 1))
            thunderIds.add(sp.load(this, R.raw.thunder3, 1))
            thunderIds.add(sp.load(this, R.raw.thunder_4, 1))

            dropIds.add(sp.load(this, R.raw.drop_1, 1))
            dropIds.add(sp.load(this, R.raw.drop_2, 1))
            dropIds.add(sp.load(this, R.raw.drop_3, 1))
            dropIds.add(sp.load(this, R.raw.drop_4, 1))
            dropIds.add(sp.load(this, R.raw.drop_5, 1))
            dropIds.add(sp.load(this, R.raw.drop_6, 1))
        }
    }

    private fun createLoopingMediaPlayer(resId: Int): MediaPlayer? {
        return try {
            val mp = MediaPlayer.create(this, resId)
            mp?.isLooping = true
            mp?.setVolume(0f, 0f)
            mp
        } catch (e: Exception) {
            null
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

        mpBackgroundRain?.start()
        mpLightRain?.start()
        mpHeavyRain?.start()
        mpWind1?.start()
        mpHeavyWind1?.start()
        mpAlwaysOnThunder?.start()

        serviceScope.launch {
            audioLoop()
        }
    }

    private fun stopPlayback() {
        isRunning = false
        mpBackgroundRain?.pause()
        mpLightRain?.pause()
        mpHeavyRain?.pause()
        mpWind1?.pause()
        mpHeavyWind1?.pause()
        mpAlwaysOnThunder?.pause()
    }

    private suspend fun audioLoop() {
        while (isRunning) {
            val masterVol = RainState.volume.value
            val rainInt = RainState.rainIntensity.value
            val windFreq = RainState.windFrequency.value
            val stormMode = RainState.isStormMode.value
            val thunderOn = RainState.thunderEnabled.value

            // Background rain is always on when volume > 0, base level
            setVolume(mpBackgroundRain, masterVol * 0.4f)
            
            // Mix light rain and heavy rain based on intensity
            setVolume(mpLightRain, masterVol * (1f - rainInt) * 0.8f)
            setVolume(mpHeavyRain, masterVol * rainInt * 1.0f)
            
            // Mix wind based on wind frequency
            val windBase = if (stormMode) 0.8f else 0.4f
            setVolume(mpWind1, masterVol * (1f - windFreq) * windBase)
            setVolume(mpHeavyWind1, masterVol * windFreq * windBase)

            // Background thunder rumble
            if (stormMode && thunderOn) {
                setVolume(mpAlwaysOnThunder, masterVol * 0.5f)
            } else {
                setVolume(mpAlwaysOnThunder, 0f)
            }

            // Handle Thunder events
            if (RainState.triggerThunder && stormMode && thunderOn) {
                RainState.triggerThunder = false
                if (thunderIds.isNotEmpty()) {
                    val thunderId = thunderIds[Random.nextInt(thunderIds.size)]
                    soundPool?.play(thunderId, masterVol, masterVol, 1, 0, 1f)
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

    private fun setVolume(mp: MediaPlayer?, vol: Float) {
        mp?.setVolume(vol, vol)
    }

    override fun onDestroy() {
        super.onDestroy()
        stopPlayback()
        
        mpBackgroundRain?.release()
        mpLightRain?.release()
        mpHeavyRain?.release()
        mpWind1?.release()
        mpHeavyWind1?.release()
        mpAlwaysOnThunder?.release()
        
        soundPool?.release()
        soundPool = null
        
        serviceScope.cancel()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
