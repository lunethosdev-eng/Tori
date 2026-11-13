package com.pokke.music.tv.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.border
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp

/**
 * D-pad / remote focus chrome for Android TV.
 * Scale + white ring when focused; Center/Enter and click invoke [onClick].
 */
@Composable
fun Modifier.tvFocusable(
    onClick: (() -> Unit)? = null,
    shape: Shape = RoundedCornerShape(12.dp),
    scaleFocused: Float = 1.06f,
): Modifier {
    var focused by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        if (focused) scaleFocused else 1f,
        animationSpec = tween(140),
        label = "tvFocusScale"
    )
    return this
        .scale(scale)
        .onFocusChanged { focused = it.isFocused }
        .focusable()
        .then(
            if (focused) Modifier.border(2.5.dp, Color.White, shape)
            else Modifier
        )
        .onKeyEvent { e ->
            if (onClick != null && e.type == KeyEventType.KeyUp &&
                (e.key == Key.Enter || e.key == Key.DirectionCenter)
            ) {
                onClick()
                true
            } else false
        }
        .then(
            if (onClick != null) Modifier.pointerInput(onClick) {
                detectTapGestures { onClick() }
            } else Modifier
        )
}
