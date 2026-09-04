import re

with open('app/src/main/java/com/example/MainActivity.kt', 'r') as f:
    content = f.read()

content = content.replace('androidx.compose.material.icons.filled.Check', 'androidx.compose.material.icons.Icons.Default.Check')
content = content.replace('androidx.compose.material.icons.filled.Clear', 'androidx.compose.material.icons.Icons.Default.Clear')

with open('app/src/main/java/com/example/MainActivity.kt', 'w') as f:
    f.write(content)
