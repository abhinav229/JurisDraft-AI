package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = LegalGoldAccent,
    onPrimary = LegalNavyDark,
    primaryContainer = LegalNavyPrimary,
    onPrimaryContainer = LegalGoldLight,
    secondary = LegalGoldLight,
    onSecondary = LegalNavyDark,
    secondaryContainer = LegalSlate,
    onSecondaryContainer = Color.White,
    background = LegalNavyDark,
    onBackground = Color(0xFFF1F5F9),
    surface = LegalCardDark,
    onSurface = Color(0xFFF1F5F9),
    surfaceVariant = LegalNavyPrimary,
    onSurfaceVariant = Color(0xFFCBD5E1),
    outline = LegalCardBorder
)

private val LightColorScheme = lightColorScheme(
    primary = LegalNavyPrimary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE2E8F0),
    onPrimaryContainer = LegalNavyPrimary,
    secondary = LegalGoldAccent,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFEF3C7),
    onSecondaryContainer = LegalGoldDark,
    background = Color(0xFFF8FAFC),
    onBackground = Color(0xFF0F172A),
    surface = Color.White,
    onSurface = Color(0xFF0F172A),
    surfaceVariant = Color(0xFFF1F5F9),
    onSurfaceVariant = Color(0xFF475569),
    outline = Color(0xFFCBD5E1)
)

@Composable
fun JurisDraftTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
