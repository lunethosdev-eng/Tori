package com.pokke.music.ui.liquid

import android.view.HapticFeedbackConstants
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import com.pokke.music.data.preferences.UserPreferences
import com.pokke.music.ui.motion.MotionSettings
import com.pokke.music.ui.motion.ToriMotionEngine

/** Press feedback routed through Tori's shared Motion Engine. */
@Composable
fun Modifier.pokkePressMotion(
    interactionSource: MutableInteractionSource,
    enabled: Boolean = true,
    pressedScale: Float = 0.965f
): Modifier {
    val context = LocalContext.current
    val view = LocalView.current
    val preferences = androidx.compose.runtime.remember(context) { UserPreferences(context) }
    val storedMotionSettings by preferences.motionSettings.collectAsState(initial = MotionSettings())
    val motionSettings = ToriMotionEngine.effectiveSettings(context, storedMotionSettings)
    val pressed by interactionSource.collectIsPressedAsState()
    val motionAllowed = enabled && ToriMotionEngine.isMotionEnabled(motionSettings)
    LaunchedEffect(pressed, motionSettings.haptics, motionAllowed) {
        if (pressed && motionAllowed && motionSettings.haptics) {
            view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
        }
    }
    val scale by animateFloatAsState(
        targetValue = if (motionAllowed && pressed) ToriMotionEngine.pressScale(motionSettings, pressedScale) else 1f,
        animationSpec = ToriMotionEngine.pressSpring(motionSettings),
        label = "tori-motion-press"
    )
    return graphicsLayer {
        scaleX = scale
        scaleY = scale
    }
}
