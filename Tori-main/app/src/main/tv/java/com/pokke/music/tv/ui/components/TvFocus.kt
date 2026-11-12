package com.pokke.music.tv.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.shape.RoundedCornerShape

/**
 * D-pad friendly focus: scale 1.08 + white border + OK/Enter activates [onClick].
 */
@Composable
fun Modifier.tvFocusable(
    onClick: () -> Unit,
    shape: Shape = RoundedCornerShape(12.dp),
    scaleFocused: Float = 1.08f
): Modifier {
    val interaction = remember { MutableInteractionSource() }
    val focused by interaction.collectIsFocusedAsState()
    val scale by animateFloatAsState(
        if (focused) scaleFocused else 1f,
        spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMediumLow),
        label = "tvFocusScale"
    )
    return this
        .scale(scale)
        .then(if (focused) Modifier.border(2.dp, Color.White, shape) else Modifier)
        .focusable(interactionSource = interaction)
        .clickable(interactionSource = interaction, indication = null, onClick = onClick)
        .onKeyEvent { e ->
            if (e.type == KeyEventType.KeyUp && (e.key == Key.DirectionCenter || e.key == Key.Enter)) {
                onClick()
                true
            } else false
        }
}
