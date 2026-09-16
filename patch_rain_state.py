import re

with open('app/src/main/java/com/example/RainAudioService.kt', 'r') as f:
    content = f.read()

search = "val ambientThunderEnabled = MutableStateFlow(false)"
replace = "val ambientThunderEnabled = MutableStateFlow(false)\n    val lightningInterval = MutableStateFlow(\"Moderate\")"
content = content.replace(search, replace)

with open('app/src/main/java/com/example/RainAudioService.kt', 'w') as f:
    f.write(content)
