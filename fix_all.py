import re

with open('app/src/main/java/com/example/MainActivity.kt', 'r') as f:
    content = f.read()

content = content.replace('androidx.compose.material.icons.filled.CheckCircleOff', 'androidx.compose.material.icons.filled.Clear')
content = content.replace('androidx.compose.material.icons.filled.CheckCircle', 'androidx.compose.material.icons.filled.Check')
content = content.replace('import androidx.compose.material.icons.filled.Clear', '')

with open('app/src/main/java/com/example/MainActivity.kt', 'w') as f:
    f.write(content)
