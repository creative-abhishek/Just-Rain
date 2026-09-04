package com.example.themes

import android.net.Uri

data class ThemePack(
    val id: String,
    val name: String,
    val description: String,
    // Layered PNG paths/URIs to create a 3D effect
    val backgroundLayerUri: Uri? = null, // The far background (sky, mountains)
    val middleLayerUri: Uri? = null,     // The middle ground (buildings, trees) - rain can fall behind this
    val foregroundLayerUri: Uri? = null, // The foreground (window frame, close objects) - rain falls strictly behind this
    val occlusionMaskUri: Uri? = null    // Mask defining exactly where rain particles should NOT be rendered
)

object DummyThemeData {
    val themes = listOf(
        ThemePack("1", "Cyber Neon", "A futuristic cyberpunk city view from a high-rise."),
        ThemePack("2", "Zen Garden", "A peaceful Japanese garden with a small pond."),
        ThemePack("3", "Dark Forest", "Spooky and mysterious deep woods."),
        ThemePack("4", "Cozy Cabin", "Looking out from a warm, fire-lit wooden cabin."),
        ThemePack("5", "Space Station", "Orbiting earth with a stellar backdrop."),
        ThemePack("6", "Gothic Castle", "A rainy night at an ancient castle.")
    )
}
