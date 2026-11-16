package com.pokke.music.ui.liquid

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.pokke.music.ui.backdrop.Backdrop
import com.pokke.music.ui.backdrop.backdrops.layerBackdrop
import com.pokke.music.ui.backdrop.backdrops.rememberLayerBackdrop

/** Shared visual runtime state for phone and Android TV. */
val LocalAppBackdrop = staticCompositionLocalOf<Backdrop?> { null }
val LocalGlassEnabled = staticCompositionLocalOf { false }
val LocalGlassPerformanceMode = staticCompositionLocalOf { true }
val LocalGlassScreenActive = staticCompositionLocalOf { true }

/**
 * Records one backdrop for the currently resumed app window. In performance mode the
 * recorder is detached whenever the activity is not STARTED, so hidden/background
 * screens do not keep a live glass capture running.
 */
@Composable
fun GlassBackdropHost(
    enabled: Boolean,
    performanceMode: Boolean = true,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    var lifecycleActive by remember(lifecycleOwner) {
        mutableStateOf(lifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED))
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            lifecycleActive = when (event) {
                Lifecycle.Event.ON_START, Lifecycle.Event.ON_RESUME -> true
                Lifecycle.Event.ON_STOP, Lifecycle.Event.ON_DESTROY -> false
                else -> lifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val captureActive = enabled && (!performanceMode || lifecycleActive)
    if (!captureActive) {
        CompositionLocalProvider(
            LocalAppBackdrop provides null,
            LocalGlassEnabled provides enabled,
            LocalGlassPerformanceMode provides performanceMode,
            LocalGlassScreenActive provides lifecycleActive
        ) {
            Box(modifier.fillMaxSize()) { content() }
        }
        return
    }

    // A single shared recording avoids one GPU capture per card or screen component.
    val source = rememberLayerBackdrop()
    CompositionLocalProvider(
        LocalAppBackdrop provides source,
        LocalGlassEnabled provides enabled,
        LocalGlassPerformanceMode provides performanceMode,
        LocalGlassScreenActive provides lifecycleActive
    ) {
        Box(modifier.fillMaxSize().layerBackdrop(source)) { content() }
    }
}
