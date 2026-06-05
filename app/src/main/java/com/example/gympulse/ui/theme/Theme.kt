package com.example.gympulse.ui.theme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val CyanPrimary    = Color(0xFF00BCD4)
val CyanLight      = Color(0xFF4DD0E1)
val BackgroundDark = Color(0xFF121212)
val SurfaceDark    = Color(0xFF1E1E1E)
val CardDark       = Color(0xFF2A2A2A)
val TextPrimary    = Color(0xFFFFFFFF)
val TextSecondary  = Color(0xFFAAAAAA)

private val DarkColorScheme = darkColorScheme(
    primary        = CyanPrimary,
    onPrimary      = Color.Black,
    background     = BackgroundDark,
    onBackground   = TextPrimary,
    surface        = SurfaceDark,
    onSurface      = TextPrimary,
    surfaceVariant = CardDark,
    onSurfaceVariant = TextSecondary,
    secondary      = CyanLight,
)

@Composable
fun GymPulseTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        content = content
    )
}