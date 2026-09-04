import re

with open('app/src/main/java/com/example/MainActivity.kt', 'r') as f:
    content = f.read()

# Replace all VolumeUp and VolumeOff with Notifications and NotificationsOff
content = content.replace('androidx.compose.material.icons.automirrored.filled.VolumeUp', 'androidx.compose.material.icons.filled.Notifications')
content = content.replace('androidx.compose.material.icons.automirrored.filled.VolumeOff', 'androidx.compose.material.icons.filled.NotificationsOff')
content = content.replace('Icons.Default.VolumeUp', 'Icons.Default.Notifications')
content = content.replace('Icons.Default.VolumeOff', 'Icons.Default.NotificationsOff')

with open('app/src/main/java/com/example/MainActivity.kt', 'w') as f:
    f.write(content)
