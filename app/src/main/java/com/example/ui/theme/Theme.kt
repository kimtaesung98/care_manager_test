package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF90CAF9),
    onPrimary = Color(0xFF0D1E36),
    primaryContainer = Color(0xFF1565C0),
    onPrimaryContainer = Color.White,
    secondary = Color(0xFFB0BEC5),
    tertiary = Color(0xFF80CBC4),
    background = DarkBackground,
    onBackground = Color(0xFFE2E8F4),
    surface = DarkSurface,
    onSurface = Color(0xFFE2E8F4),
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = Color(0xFF94A3B8),
    error = CriticalRed,
    onError = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = PrimaryBlue,
    onPrimary = Color.White,
    primaryContainer = PrimaryBlueLight, // D3E4FF
    onPrimaryContainer = PrimaryBlue,    // 1565C0
    secondary = Color(0xFF455A64),
    tertiary = SafeGreen,
    background = NaturalBackgroundLight, // F8F9FF
    onBackground = NaturalTextSlate,     // 0F172A
    surface = NaturalCardWhite,          // FFFFFF
    onSurface = NaturalTextSlate,        // 0F172A
    surfaceVariant = Color(0xFFE2E8F0),   // Clean blue-slate divider tint
    onSurfaceVariant = Color(0xFF475569),
    error = CriticalRed,
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = false, // Defaults to Light Mode representing the Natural Tones theme beautifully
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

