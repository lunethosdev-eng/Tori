package com.pokke.music.ui.theme

import androidx.compose.ui.graphics.Color

val PokkeViolet = Color(0xFF8B5CF6)
val PokkeOcean = Color(0xFF38BDF8)
val PokkeRose = Color(0xFFF472B6)
val PokkeForest = Color(0xFF34D399)
val PokkeMono = Color(0xFFE5E5EA)

val BackgroundDark = Color(0xFF0B0B0E)
val SurfaceDark = Color(0xFF16161C)
val SurfaceElevated = Color(0xFF1E1E26)

fun accentColor(theme: String): Color = when (theme.lowercase()) {
    "ocean" -> PokkeOcean
    "rose" -> PokkeRose
    "forest" -> PokkeForest
    "mono" -> PokkeMono
    else -> PokkeViolet
}
