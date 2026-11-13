package com.pokke.music.ui.liquid

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import com.pokke.music.ui.backdrop.Backdrop
import com.pokke.music.ui.backdrop.backdrops.layerBackdrop
import com.pokke.music.ui.backdrop.backdrops.rememberLayerBackdrop

/** App-wide Liquid Glass source. A single recorded layer is shared by glass surfaces. */
val LocalAppBackdrop = staticCompositionLocalOf<Backdrop?> { null }
val LocalGlassPerformanceMode = staticCompositionLocalOf { true }

@Composable
fun GlassBackdropHost(
    enabled: Boolean,
    performanceMode: Boolean = true,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    if (!enabled) {
        CompositionLocalProvider(
            LocalAppBackdrop provides null,
            LocalGlassPerformanceMode provides performanceMode
        ) {
            Box(modifier.fillMaxSize()) { content() }
        }
        return
    }

    // Capture once at the root instead of creating a separate recording for every card.
    // The Canvas player uses TextureView (not SurfaceView) so it participates in this layer.
    val source = rememberLayerBackdrop()
    CompositionLocalProvider(
        LocalAppBackdrop provides source,
        LocalGlassPerformanceMode provides performanceMode
    ) {
        Box(modifier.fillMaxSize().layerBackdrop(source)) { content() }
    }
}
