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

/**
 * Shared glass panel. Uses GlassSafety so mid-range devices (A71, etc.)
 * never run the full lens + chromatic path that causes RenderThread SIGSEGV.
 */
@Composable
fun LiquidGlassPanel(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 24.dp,
    fallbackColor: Color = Color.Transparent,
    content: @Composable BoxScope.() -> Unit
) {
    val backdrop = LocalAppBackdrop.current
    val enabled = LocalGlassEnabled.current && LocalGlassScreenActive.current && backdrop != null
    val performance = LocalGlassPerformanceMode.current || GlassSafety.preferSafeGlass
    val shape = RoundedCornerShape(cornerRadius)

    val surfaceModifier = if (enabled) {
        val blurDp = GlassSafety.blurRadiusDp(performance)
        val scale = GlassSafety.backdropScale(performance)
        val useLens = GlassSafety.allowLens(performance)
        val useChromatic = GlassSafety.allowChromatic(performance)

        Modifier.drawBackdrop(
            backdrop = requireNotNull(backdrop),
            shape = { shape },
            effects = {
                vibrancy()
                blur(blurDp.dp.toPx())
                if (useLens) {
                    lens(
                        refractionHeight = (if (performance) 6f else 12f).dp.toPx(),
                        refractionAmount = (if (performance) 8f else 18f).dp.toPx(),
                        chromaticAberration = useChromatic
                    )
                }
            },
            onDrawSurface = {
                drawRect(Color(0xB81A1B24))
            },
            backdropScale = scale
        ).clip(shape)
    } else {
        Modifier.background(fallbackColor, shape).clip(shape)
    }

    Box(modifier.fillMaxWidth().then(surfaceModifier), content = content)
}
