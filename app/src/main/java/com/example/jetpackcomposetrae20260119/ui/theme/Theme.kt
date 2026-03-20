package com.example.jetpackcomposetrae20260119.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = CopperGlow,
    onPrimary = Midnight,
    primaryContainer = MidnightSoft,
    onPrimaryContainer = Fog,
    secondary = Sand,
    onSecondary = Midnight,
    tertiary = Moss,
    onTertiary = Fog,
    background = Midnight,
    onBackground = Fog,
    surface = MidnightSoft,
    onSurface = Fog,
    surfaceVariant = Color(0xFF313B46),
    onSurfaceVariant = Color(0xFFD3CCC2),
    outline = Color(0xFF80776B),
    error = Color(0xFFD99388),
    onError = Midnight
)

private val LightColorScheme = lightColorScheme(
    primary = Copper,
    onPrimary = Porcelain,
    primaryContainer = Color(0xFFF1E0C8),
    onPrimaryContainer = Ink,
    secondary = Midnight,
    onSecondary = Porcelain,
    secondaryContainer = Color(0xFFDCE3EA),
    onSecondaryContainer = Midnight,
    tertiary = Moss,
    onTertiary = Porcelain,
    background = Mist,
    onBackground = Ink,
    surface = Porcelain,
    onSurface = Ink,
    surfaceVariant = Fog,
    onSurfaceVariant = Slate,
    outline = Outline,
    error = Garnet,
    onError = Porcelain
)

@Composable
fun Jetpackcomposetrae20260119Theme(
    darkTheme: Boolean = false,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme && dynamicColor) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
