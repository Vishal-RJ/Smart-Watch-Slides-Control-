package com.presentation.wearclicker.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.wear.compose.material.Colors
import androidx.wear.compose.material.MaterialTheme

val BackgroundBlack = Color(0xFF000000) // Pure solid black for OLED power efficiency
val WaveLightBlue = Color(0xFF4FC3F7)   // Primary wave color (Light Blue 300)
val WaveCyan = Color(0xFF81D4FA)        // Secondary wave glow (Light Blue 200)
val WaveAccent = Color(0xFF00E5FF)      // Accent cyan for double tap
val TextLight = Color(0xFFECEFF1)
val TextMuted = Color(0xFF78909C)

val StatusConnected = Color(0xFF00E676)
val StatusConnecting = Color(0xFFFFB300)
val StatusError = Color(0xFFFF5252)

private val ClickerColorPalette = Colors(
    primary = WaveLightBlue,
    primaryVariant = WaveCyan,
    secondary = WaveAccent,
    background = BackgroundBlack,
    surface = BackgroundBlack,
    onPrimary = BackgroundBlack,
    onSecondary = BackgroundBlack,
    onBackground = TextLight,
    onSurface = TextLight,
    onError = Color.White
)

@Composable
fun PresentationClickerTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colors = ClickerColorPalette,
        content = content
    )
}
