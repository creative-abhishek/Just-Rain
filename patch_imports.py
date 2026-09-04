import re

with open('app/src/main/java/com/example/MainActivity.kt', 'r') as f:
    content = f.read()

imports = """
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsDraggedAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
"""

content = content.replace('import androidx.compose.animation.shrinkVertically', 'import androidx.compose.animation.shrinkVertically\n' + imports)

with open('app/src/main/java/com/example/MainActivity.kt', 'w') as f:
    f.write(content)
