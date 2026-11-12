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

/**
 * Provides a layer [Backdrop] captured from [content] so liquid controls
 * (toggle/button/tabs/slider) can refract the real background like iOS glass.
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
