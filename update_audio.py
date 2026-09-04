import re

with open('app/src/main/java/com/example/RainAudioService.kt', 'r') as f:
    content = f.read()

# Replace variables
content = content.replace('private var mpLightRain: MediaPlayer? = null', 'private var mpLightRain: GaplessAudioTrack? = null')
content = content.replace('private var mpHeavyRain: MediaPlayer? = null', 'private var mpHeavyRain: GaplessAudioTrack? = null')
content = content.replace('private var mpWind: MediaPlayer? = null', 'private var mpWind: GaplessAudioTrack? = null')
content = content.replace('private var mpAlwaysOnThunder: MediaPlayer? = null', 'private var mpAlwaysOnThunder: GaplessAudioTrack? = null')

# Replace initialization
content = content.replace('mpLightRain = createLoopingMediaPlayer(R.raw.light_rain)', 'mpLightRain = GaplessAudioTrack(this, R.raw.light_rain)')
content = content.replace('mpHeavyRain = createLoopingMediaPlayer(R.raw.heavy_rain)', 'mpHeavyRain = GaplessAudioTrack(this, R.raw.heavy_rain)')
content = content.replace('mpWind = createLoopingMediaPlayer(R.raw.wind)', 'mpWind = GaplessAudioTrack(this, R.raw.wind)')
content = content.replace('mpAlwaysOnThunder = createLoopingMediaPlayer(R.raw.always_on_thunder)', 'mpAlwaysOnThunder = GaplessAudioTrack(this, R.raw.always_on_thunder)')

# Remove createLoopingMediaPlayer and setVolume helper
content = re.sub(r'private fun createLoopingMediaPlayer.*?\}\s*\}', '', content, flags=re.DOTALL)
content = re.sub(r'private fun setVolume\(mp: MediaPlayer\?, vol: Float\).*?\}', '', content, flags=re.DOTALL)

# Audio loop changes
loop_start = content.find('val masterVol = RainState.volume.value')
loop_code = """val isMuted = RainState.isMuted.value
            val masterVol = if (isMuted) 0f else RainState.volume.value"""
content = content.replace('val masterVol = RainState.volume.value', loop_code)

content = content.replace('setVolume(mpLightRain, masterVol)', 'mpLightRain?.setVolume(masterVol)')
content = content.replace('setVolume(mpHeavyRain, masterVol * rainInt)', 'mpHeavyRain?.setVolume(masterVol * rainInt)')
content = content.replace('setVolume(mpWind, windVol)', 'mpWind?.setVolume(windVol)')
content = content.replace('setVolume(mpAlwaysOnThunder, masterVol * 0.7f)', 'mpAlwaysOnThunder?.setVolume(masterVol * 0.7f)')
content = content.replace('setVolume(mpAlwaysOnThunder, 0f)', 'mpAlwaysOnThunder?.setVolume(0f)')

# Append GaplessAudioTrack
gapless_class = """
class GaplessAudioTrack(private val context: Context, private val resId: Int) {
    private var activeMp: MediaPlayer? = null
    private var nextMp: MediaPlayer? = null
    private var currentVol = 0f

    init {
        activeMp = createPlayer()
        nextMp = createPlayer()
        try {
            activeMp?.setNextMediaPlayer(nextMp)
        } catch (e: Exception) {}
        setupCompletionListener(activeMp)
        setupCompletionListener(nextMp)
    }

    private fun createPlayer(): MediaPlayer? {
        return try {
            val mp = MediaPlayer.create(context, resId)
            mp?.setVolume(currentVol, currentVol)
            mp
        } catch (e: Exception) {
            null
        }
    }

    private fun setupCompletionListener(mp: MediaPlayer?) {
        mp?.setOnCompletionListener { completedPlayer ->
            activeMp = nextMp
            completedPlayer.seekTo(0)
            nextMp = completedPlayer
            try {
                activeMp?.setNextMediaPlayer(nextMp)
            } catch (e: Exception) {}
        }
    }

    fun start() {
        activeMp?.start()
    }

    fun pause() {
        activeMp?.pause()
        nextMp?.pause()
    }

    fun setVolume(vol: Float) {
        currentVol = vol
        activeMp?.setVolume(vol, vol)
        nextMp?.setVolume(vol, vol)
    }

    fun release() {
        activeMp?.release()
        nextMp?.release()
    }
}
"""

content += gapless_class

with open('app/src/main/java/com/example/RainAudioService.kt', 'w') as f:
    f.write(content)
