import re

with open('app/src/main/java/com/example/MainActivity.kt', 'r') as f:
    content = f.read()

# Replace the Canvas part
search = """        // 1. Core Fullscreen Interactive Particle Animation
        Canvas(modifier = Modifier.fillMaxSize().testTag("rain_canvas")) {"""

replace = """        // Background Image
        if (backgroundImageUri != null) {
            coil.compose.AsyncImage(
                model = backgroundImageUri,
                contentDescription = "Background Image",
                contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }

        // 1. Core Fullscreen Interactive Particle Animation
        Canvas(modifier = Modifier.fillMaxSize().testTag("rain_canvas")) {"""

content = content.replace(search, replace)

# Persist the URI permission if possible
uri_search = """        onResult = { uri -> RainState.backgroundImageUri.value = uri }"""
uri_replace = """        onResult = { uri -> 
            uri?.let {
                try {
                    context.contentResolver.takePersistableUriPermission(
                        it,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )
                } catch (e: Exception) {
                    // Ignore if not supported
                }
            }
            RainState.backgroundImageUri.value = uri 
        }"""
content = content.replace(uri_search, uri_replace)

with open('app/src/main/java/com/example/MainActivity.kt', 'w') as f:
    f.write(content)
