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
    val context = LocalContext.current
    val prefs = androidx.compose.runtime.remember(context) { UserPreferences(context) }
    val selectedTheme by prefs.appTheme.collectAsState("dark")
    val accent by prefs.accentColor.collectAsState("white")
    val primary = when (accent) {
        "violet" -> Color(0xFFB69CFF)
        "blue" -> Color(0xFF83B9FF)
        "pink" -> Color(0xFFFFA6D2)
        "green" -> Color(0xFF9DDBB2)
        else -> Color.White
    }
    val secondary = when (accent) {
        "violet" -> Color(0xFF8B72D8)
        "blue" -> Color(0xFF3979C9)
        "pink" -> Color(0xFFD66C9F)
        "green" -> Color(0xFF438A61)
        else -> Color(0xFF77777D)
    }
    val scheme = if (selectedTheme == "light") lightColorScheme(
        primary = primary, onPrimary = Color(0xFF17171B), secondary = secondary, onSecondary = Color.White,
        background = Color(0xFFF5F5F7), onBackground = Color(0xFF17171B),
        surface = Color.White, onSurface = Color(0xFF17171B),
        surfaceVariant = Color(0xFFE8E8ED), onSurfaceVariant = Color(0xFF55555D),
        outline = Color(0xFFB8B8C0)
    ) else darkColorScheme(
        primary = primary, onPrimary = Color(0xFF111114), secondary = secondary, onSecondary = Color.White,
        background = if (selectedTheme == "amoled") Color.Black else Color(0xFF09090B),
        onBackground = Color.White,
        surface = if (selectedTheme == "amoled") Color.Black else Color(0xFF17171B),
        onSurface = Color.White, surfaceVariant = Color(0xFF29292F),
        onSurfaceVariant = Color(0xFFB9B9C2), outline = Color(0xFF484850)
    )
    MaterialTheme(colorScheme = scheme, typography = PokkeTypography, content = content)
}
