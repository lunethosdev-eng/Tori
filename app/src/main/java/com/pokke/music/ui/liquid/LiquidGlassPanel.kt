package com.pokke.music.ui.liquid

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.pokke.music.ui.backdrop.drawBackdrop
import com.pokke.music.ui.backdrop.effects.blur
import com.pokke.music.ui.backdrop.effects.lens
import com.pokke.music.ui.backdrop.effects.vibrancy

/** Shared glass panel used by phone and TV chrome; falls back cleanly when disabled. */
@Composable
fun LiquidGlassPanel(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 24.dp,
    fallbackColor: Color = Color.Transparent,
    content: @Composable BoxScope.() -> Unit
) {
    val backdrop = LocalAppBackdrop.current
    val enabled = LocalGlassEnabled.current && LocalGlassScreenActive.current && backdrop != null
    val performance = LocalGlassPerformanceMode.current
    val shape = RoundedCornerShape(cornerRadius)

    val surfaceModifier = if (enabled) {
        Modifier.drawBackdrop(
            backdrop = requireNotNull(backdrop),
            shape = { shape },
            effects = {
                vibrancy()
                blur((if (performance) 4f else 9f).dp.toPx())
                lens(
                    refractionHeight = (if (performance) 8f else 14f).dp.toPx(),
                    refractionAmount = (if (performance) 12f else 22f).dp.toPx(),
                    chromaticAberration = !performance
                )
            },
            onDrawSurface = {
                drawRect(Color(0xB81A1B24))
            },
            backdropScale = if (performance) 0.55f else 1f
        ).clip(shape)
    } else {
        Modifier.background(fallbackColor, shape).clip(shape)
    }

    Box(modifier.fillMaxWidth().then(surfaceModifier), content = content)
}
