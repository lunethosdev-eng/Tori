package com.pokke.music.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.pokke.music.data.preferences.UserPreferences

private fun pokkeDarkScheme(accent: Color) = darkColorScheme(
    primary = accent,
    onPrimary = Color.Black,
    secondary = accent.copy(alpha = 0.85f),
    onSecondary = Color.Black,
    tertiary = accent.copy(alpha = 0.65f),
    background = BackgroundDark,
    onBackground = Color.White,
    surface = SurfaceDark,
    onSurface = Color.White,
    surfaceVariant = SurfaceElevated,
    onSurfaceVariant = Color(0xFFB0B0B8),
    outline = Color.White.copy(alpha = 0.18f),
)

@Composable
fun PokkeTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val prefs = UserPreferences(context)
    val accentKey by prefs.appAccentTheme.collectAsState("violet")
    val accent = accentColor(accentKey)
    MaterialTheme(
        colorScheme = pokkeDarkScheme(accent),
        content = content
    )
}
