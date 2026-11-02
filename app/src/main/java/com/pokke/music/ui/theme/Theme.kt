package com.pokke.music.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = PokkePrimary,
    onPrimary = Color.Black,
    secondary = PokkeSecondary,
    onSecondary = Color.Black,
    background = PokkeBlack,
    onBackground = PokkeOnSurface,
    surface = PokkeSurface,
    onSurface = PokkeOnSurface,
    surfaceVariant = PokkeSurfaceVariant,
    onSurfaceVariant = PokkeOnSurfaceVariant,
    outline = Color(0xFF333333)
)

@Composable
fun PokkeTheme(
    darkTheme: Boolean = true, // Always dark for music app premium feel
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = PokkeTypography,
        content = content
    )
}
