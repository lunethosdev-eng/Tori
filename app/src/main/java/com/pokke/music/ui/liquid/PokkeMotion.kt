package com.pokke.music.ui.liquid

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer

/** Subtle, low-cost tactile compression. Reuse the same source as clickable/combinedClickable. */
@Composable
fun Modifier.pokkePressMotion(
    interactionSource: MutableInteractionSource,
    enabled: Boolean = true,
    pressedScale: Float = 0.965f
): Modifier {
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (enabled && pressed) pressedScale else 1f,
        animationSpec = spring(dampingRatio = 0.58f, stiffness = Spring.StiffnessMedium),
        label = "pokke-touch-scale"
    )
    return graphicsLayer {
        scaleX = scale
        scaleY = scale
    }
}
