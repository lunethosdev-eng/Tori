package com.pokke.music.ui.liquid

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import com.pokke.music.ui.backdrop.Backdrop

/**
 * Liquid Glass host.
 *
 * The previous layerBackdrop capture path crashed RenderThread on several
 * devices when Media3 SurfaceView was active (play from search / full player).
 * Until a stable capture path is validated, glass mode uses the Material
 * translucent fallback (LocalAppBackdrop = null) so the app never hard-crashes.
 * LiquidToggle / liquid chrome still switch visual style via the glass flag.
 */
val LocalAppBackdrop = staticCompositionLocalOf<Backdrop?> { null }

@Composable
fun GlassBackdropHost(
    enabled: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    // Always null backdrop for stability. Chrome components check prefs.glass
    // and use translucent Material surfaces when LocalAppBackdrop is null.
    CompositionLocalProvider(LocalAppBackdrop provides null) {
        Box(modifier.fillMaxSize()) { content() }
    }
}
