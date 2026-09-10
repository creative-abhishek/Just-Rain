import re

with open('app/src/main/java/com/example/RainAudioService.kt', 'r') as f:
    content = f.read()

# Fix types of mpLightRain etc.
content = re.sub(r'GaplessAudioTrack', 'LoopingAudioTrack', content)

# Fix thunderIds / dropIds
content = re.sub(r'private var thunderIds = mutableListOf<Int>\(\)', '', content)
content = re.sub(r'private var dropIds = mutableListOf<Int>\(\)', 
                 'private val dropIds = mutableListOf<Int>()\n    private val thunderResIds = listOf(R.raw.thunder_1, R.raw.thunder_2, R.raw.thunder_3, R.raw.thunder_4, R.raw.thunder_5)\n    private val activeThunderPlayers = java.util.concurrent.ConcurrentLinkedQueue<android.media.MediaPlayer>()', content)

with open('app/src/main/java/com/example/RainAudioService.kt', 'w') as f:
    f.write(content)
