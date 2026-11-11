package com.pokke.music.ui.player

/** Pull contract used by BitChord's original mini-player gesture implementation. */
interface MiniPlayerPull {
    /** Finger delta in pixels; negative values move upward. */
    fun drag(delta: Float)
    /** Finger release velocity in pixels per second. */
    fun release(velocity: Float)
}
