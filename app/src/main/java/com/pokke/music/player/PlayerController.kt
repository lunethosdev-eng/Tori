package com.pokke.music.player

import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.pokke.music.data.model.Song
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Singleton-style holder for the app ExoPlayer.
 * Call [init] once from MainActivity / Application.
 */
object PlayerController {

    private var exo: ExoPlayer? = null

    private val _currentSong = MutableStateFlow<Song?>(null)
    val currentSong: StateFlow<Song?> = _currentSong.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _positionMs = MutableStateFlow(0L)
    val positionMs: StateFlow<Long> = _positionMs.asStateFlow()

    private val _durationMs = MutableStateFlow(0L)
    val durationMs: StateFlow<Long> = _durationMs.asStateFlow()

    fun init(context: Context) {
        if (exo != null) return
        exo = ExoPlayer.Builder(context.applicationContext).build().also { player ->
            player.addListener(object : Player.Listener {
                override fun onIsPlayingChanged(playing: Boolean) {
                    _isPlaying.value = playing
                }

                override fun onPlaybackStateChanged(playbackState: Int) {
                    if (playbackState == Player.STATE_READY) {
                        _durationMs.value = player.duration.coerceAtLeast(0L)
                    }
                }
            })
        }
    }

    fun playSong(song: Song) {
        val player = exo ?: return
        _currentSong.value = song
        val url = song.audioUrl
        if (url.isNullOrBlank()) {
            // No audio — still show UI with cover
            _positionMs.value = 0L
            _durationMs.value = song.durationMs
            _isPlaying.value = false
            return
        }
        player.setMediaItem(MediaItem.fromUri(url))
        player.prepare()
        player.playWhenReady = true
        _isPlaying.value = true
        _durationMs.value = if (song.durationMs > 0) song.durationMs else 0L
    }

    fun togglePlayPause() {
        val player = exo ?: return
        if (player.isPlaying) player.pause() else player.play()
    }

    fun seekTo(positionMs: Long) {
        exo?.seekTo(positionMs)
        _positionMs.value = positionMs
    }

    fun seekToFraction(fraction: Float) {
        val dur = _durationMs.value.coerceAtLeast(1L)
        seekTo((fraction * dur).toLong())
    }

    /** Call from a ticker / LaunchedEffect while screen is visible */
    fun pollPosition() {
        val player = exo ?: return
        _positionMs.value = player.currentPosition.coerceAtLeast(0L)
        if (player.duration > 0) _durationMs.value = player.duration
    }

    fun release() {
        exo?.release()
        exo = null
    }
}
