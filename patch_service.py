import re

with open('app/src/main/java/com/example/RainAudioService.kt', 'r') as f:
    content = f.read()

search = "val rippleDurationMultiplier = MutableStateFlow(0.5f) // 0.0 to 1.0"
replace = """val rippleDurationMultiplier = MutableStateFlow(0.5f) // 0.0 to 1.0
    val rippleShapeMultiplier = MutableStateFlow(0.35f) // 0.0 to 1.0
    val rainDropSizeMultiplier = MutableStateFlow(0.5f) // 0.0 to 1.0"""
content = content.replace(search, replace)

with open('app/src/main/java/com/example/RainAudioService.kt', 'w') as f:
    f.write(content)
