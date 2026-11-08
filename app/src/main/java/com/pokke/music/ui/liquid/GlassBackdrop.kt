package com.pokke.music.ui.liquid

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.backdrops.layerBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop

/**
 * Provides a layer [Backdrop] captured from [content] so liquid controls
 * can refract the real background like iOS glass.
 * When [enabled] is false, no capture runs (avoids crashes / GPU cost).
 */
val LocalAppBackdrop = staticCompositionLocalOf<Backdrop?> { null }

@Composable
fun GlassBackdropHost(
    enabled: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    if (!enabled) {
        CompositionLocalProvider(LocalAppBackdrop provides null) {
            Box(modifier.fillMaxSize()) { content() }
        }
        return
    }
    val backdrop = rememberLayerBackdrop()
    CompositionLocalProvider(LocalAppBackdrop provides backdrop) {
        Box(modifier.fillMaxSize().layerBackdrop(backdrop)) {
            content()
        }
    }
}
