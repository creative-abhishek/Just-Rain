package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = RainPrimary,
    secondary = RainSecondary,
    tertiary = RainAccent,
    background = RainBackground,
    surface = RainSurface,
    onPrimary = RainBackground,
    onSecondary = RainText,
    onBackground = RainText,
    onSurface = RainText
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}
