import re

with open('app/src/main/java/com/example/MainActivity.kt', 'r') as f:
    content = f.read()

content = content.replace('import androidx.compose.material.icons.filled.Close', 'import androidx.compose.material.icons.filled.Close\nimport androidx.compose.material.icons.filled.VolumeUp\nimport androidx.compose.material.icons.filled.VolumeOff\nimport androidx.compose.material.icons.filled.Image')

with open('app/src/main/java/com/example/MainActivity.kt', 'w') as f:
    f.write(content)
