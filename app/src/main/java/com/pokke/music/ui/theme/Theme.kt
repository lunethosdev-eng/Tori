package com.pokke.music.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.pokke.music.data.preferences.UserPreferences

@Composable
fun PokkeTheme(content: @Composable () -> Unit) {
    val prefs = androidx.compose.runtime.remember { UserPreferences(LocalContext.current) }
    val selectedTheme by prefs.appTheme.collectAsState("dark")
    val accent by prefs.accentColor.collectAsState("white")
    val primary = when (accent) {
        "violet" -> Color(0xFFB69CFF)
        "blue" -> Color(0xFF83B9FF)
        "pink" -> Color(0xFFFFA6D2)
        "green" -> Color(0xFF9DDBB2)
        else -> Color.White
    }
    val scheme = if (selectedTheme == "light") lightColorScheme(primary = primary, onPrimary = Color.Black, background = Color(0xFFF5F5F7), onBackground = Color(0xFF151515), surface = Color.White, onSurface = Color(0xFF151515))
    else darkColorScheme(primary = primary, onPrimary = Color.Black, secondary = primary.copy(.8f), onSecondary = Color.Black, background = Color.Black, onBackground = Color.White, surface = if (selectedTheme == "amoled") Color.Black else Color(0xFF121214), onSurface = Color.White, surfaceVariant = Color(0xFF242428), onSurfaceVariant = Color(0xFFB9B9BE), outline = Color(0xFF45454A))
    MaterialTheme(colorScheme = scheme, typography = PokkeTypography, content = content)
}
