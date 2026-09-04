import re

with open('app/src/main/java/com/example/MainActivity.kt', 'r') as f:
    content = f.read()

content = content.replace('import androidx.compose.material.icons.Icons.Default.Check', 'import androidx.compose.material.icons.filled.Check\nimport androidx.compose.material.icons.filled.Clear')
content = content.replace('androidx.compose.material.icons.Icons.Default.Check', 'Icons.Default.Check')
content = content.replace('androidx.compose.material.icons.Icons.Default.Clear', 'Icons.Default.Clear')
content = content.replace('import androidx.compose.material.icons.Icons.Default.Clear', '')

with open('app/src/main/java/com/example/MainActivity.kt', 'w') as f:
    f.write(content)
