import re

with open('app/src/main/java/com/example/MainActivity.kt', 'r') as f:
    content = f.read()

content = content.replace('androidx.compose.material.icons.filled.Notifications', 'androidx.compose.material.icons.filled.CheckCircle')
content = content.replace('androidx.compose.material.icons.filled.NotificationsOff', 'androidx.compose.material.icons.filled.Clear')
content = content.replace('Icons.Default.Notifications', 'Icons.Default.KeyboardArrowUp')
content = content.replace('Icons.Default.NotificationsOff', 'Icons.Default.Clear')

with open('app/src/main/java/com/example/MainActivity.kt', 'w') as f:
    f.write(content)
