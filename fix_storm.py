import re

with open('app/src/main/java/com/example/MainActivity.kt', 'r') as f:
    content = f.read()

# Replace STORM INTENSITY with RAIN INTENSITY
content = content.replace('text = "STORM INTENSITY",', 'text = "RAIN INTENSITY",')

# Replace the LaunchedEffect condition for Lightning
content = content.replace('if (isStormMode && isPlaying && thunderEnabled) {', 'if (isPlaying && thunderEnabled) {')

# Replace DiagnosticRow for Lightning Interval
content = content.replace('value = if (isStormMode) "7s - 18s" else "Disabled"', 'value = if (thunderEnabled) "7s - 18s" else "Disabled"')

with open('app/src/main/java/com/example/MainActivity.kt', 'w') as f:
    f.write(content)

with open('app/src/main/java/com/example/RainAudioService.kt', 'r') as f:
    audio_content = f.read()

# Replace thunder sound trigger condition
audio_content = audio_content.replace('if (RainState.triggerThunder && stormMode && thunderOn) {', 'if (RainState.triggerThunder && thunderOn) {')

with open('app/src/main/java/com/example/RainAudioService.kt', 'w') as f:
    f.write(audio_content)

