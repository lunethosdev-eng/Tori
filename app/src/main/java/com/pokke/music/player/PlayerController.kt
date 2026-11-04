package com.pokke.music.player

import android.content.ComponentName
import android.content.Context
import android.net.Uri
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.FutureCallback
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import com.pokke.music.data.model.Song
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** UI-facing controller. Actual playback is owned by [PlaybackService]. */
object PlayerController {
    private var controller: MediaController? = null
    private var controllerFuture: ListenableFuture<MediaController>? = null
    private var pendingQueue: Pair<List<Song>, Int>? = null
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    private val _currentSong = MutableStateFlow<Song?>(null)
    val currentSong: StateFlow<Song?> = _currentSong.asStateFlow()
    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()
    private val _positionMs = MutableStateFlow(0L)
    val positionMs: StateFlow<Long> = _positionMs.asStateFlow()
    private val _durationMs = MutableStateFlow(0L)
    val durationMs: StateFlow<Long> = _durationMs.asStateFlow()
    private val _queue = MutableStateFlow<List<Song>>(emptyList())
    val queue: StateFlow<List<Song>> = _queue.asStateFlow()
    private val _currentIndex = MutableStateFlow(0)
    val currentIndex: StateFlow<Int> = _currentIndex.asStateFlow()
    private val _playbackError = MutableStateFlow<String?>(null)
    val playbackError: StateFlow<String?> = _playbackError.asStateFlow()

    private val listener = object : Player.Listener {
        override fun onIsPlayingChanged(isPlaying: Boolean) {
            _isPlaying.value = isPlaying
        }

        override fun onPlaybackStateChanged(playbackState: Int) {
            syncPlaybackState()
            if (playbackState == Player.STATE_ENDED) playNext()
        }

        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
            syncCurrentItem()
            syncPlaybackState()
        }

        override fun onTimelineChanged(timeline: androidx.media3.common.Timeline, reason: Int) {
            syncQueue()
            syncCurrentItem()
        }

        override fun onPlayerError(error: androidx.media3.common.PlaybackException) {
            _playbackError.value = error.localizedMessage ?: "No se pudo reproducir este audio"
            _isPlaying.value = false
        }

        override fun onPositionDiscontinuity(
            oldPosition: Player.PositionInfo,
            newPosition: Player.PositionInfo,
            reason: Int
        ) {
            syncPlaybackState()
        }
    }

    fun init(context: Context) {
        if (controller != null || controllerFuture != null) return
        val token = SessionToken(context.applicationContext, ComponentName(context, PlaybackService::class.java))
        val future = MediaController.Builder(context.applicationContext, token).buildAsync()
        controllerFuture = future
        Futures.addCallback(future, object : FutureCallback<MediaController> {
            override fun onSuccess(mediaController: MediaController?) {
                if (mediaController == null) return
                controller = mediaController
                mediaController.addListener(listener)
                syncQueue()
                syncCurrentItem()
                syncPlaybackState()
                startPositionTicker()
                pendingQueue?.let { (songs, index) ->
                    pendingQueue = null
                    playQueue(songs, index)
                }
            }

            override fun onFailure(t: Throwable) {
                _playbackError.value = t.localizedMessage ?: "No se pudo iniciar el reproductor"
                controllerFuture = null
            }
        }, MoreExecutors.directExecutor())
    }

    private fun startPositionTicker() {
        scope.launch {
            while (isActive && controller != null) {
                syncPlaybackState()
                delay(400)
            }
        }
    }

    private fun toMediaItem(song: Song): MediaItem {
        val metadata = MediaMetadata.Builder()
            .setTitle(song.title)
            .setArtist(song.artist)
            .setAlbumTitle(song.album)
            .setArtworkUri(song.coverUrl?.takeIf { it.isNotBlank() }?.let(Uri::parse))
            .build()
        return MediaItem.Builder()
            .setMediaId(song.id)
            .setUri(song.audioUrl ?: "")
            .setMediaMetadata(metadata)
            .build()
    }

    fun playSong(song: Song) = playQueue(listOf(song), 0)

    fun playQueue(songs: List<Song>, startIndex: Int = 0) {
        val player = controller ?: run {
            pendingQueue = songs to startIndex
            _playbackError.value = null
            return
        }
        val playable = songs.filter { !it.audioUrl.isNullOrBlank() }
        if (playable.isEmpty()) {
            _playbackError.value = "Esta canción no tiene una URL de audio disponible"
            return
        }
        val requested = songs.getOrNull(startIndex)
        val safeIndex = requested?.let { playable.indexOfFirst { item -> item.id == it.id } }
            ?.takeIf { it >= 0 } ?: 0
        _playbackError.value = null
        _queue.value = playable
        player.setMediaItems(playable.map(::toMediaItem), safeIndex, 0L)
        player.prepare()
        player.playWhenReady = true
        player.play()
        syncCurrentItem()
    }

    fun addToQueue(song: Song, playNext: Boolean = false) {
        val player = controller ?: return
        if (song.audioUrl.isNullOrBlank()) return
        val insertAt = if (playNext) {
            (player.currentMediaItemIndex + 1).coerceIn(0, player.mediaItemCount)
        } else {
            player.mediaItemCount
        }
        player.addMediaItem(insertAt, toMediaItem(song))
        val updated = _queue.value.toMutableList().apply { add(insertAt.coerceAtMost(size), song) }
        _queue.value = updated
    }

    fun playQueueItem(index: Int) {
        val player = controller ?: return
        if (index in 0 until player.mediaItemCount) {
            player.seekToDefaultPosition(index)
            player.playWhenReady = true
            player.play()
        }
    }

    fun removeQueueItem(index: Int) {
        val player = controller ?: return
        if (index !in 0 until player.mediaItemCount) return
        player.removeMediaItem(index)
        syncQueue()
        syncCurrentItem()
    }

    fun moveQueueItem(from: Int, to: Int) {
        val player = controller ?: return
        if (from !in 0 until player.mediaItemCount || to !in 0 until player.mediaItemCount) return
        player.moveMediaItem(from, to)
        syncQueue()
        syncCurrentItem()
    }

    fun clearQueue() {
        controller?.clearMediaItems()
        _queue.value = emptyList()
        _currentSong.value = null
        _positionMs.value = 0L
        _durationMs.value = 0L
        _isPlaying.value = false
        _currentIndex.value = 0
    }

    fun playNext() {
        val player = controller ?: return
        if (player.hasNextMediaItem()) player.seekToNextMediaItem()
    }

    fun playPrevious() {
        val player = controller ?: return
        if (player.currentPosition > 3_000L) player.seekTo(0L)
        else if (player.hasPreviousMediaItem()) player.seekToPreviousMediaItem()
        else player.seekTo(0L)
    }

    fun togglePlayPause() {
        val player = controller ?: return
        if (player.isPlaying) player.pause() else player.play()
    }

    fun seekTo(positionMs: Long) {
        val player = controller ?: return
        val target = positionMs.coerceIn(0L, player.duration.takeIf { it > 0 } ?: Long.MAX_VALUE)
        player.seekTo(target)
        _positionMs.value = target
    }

    fun seekToFraction(fraction: Float) {
        val duration = _durationMs.value
        if (duration > 0L) seekTo((fraction.coerceIn(0f, 1f) * duration).toLong())
    }

    fun setVolume(volume: Float) {
        controller?.volume = volume.coerceIn(0f, 1f)
    }

    fun pollPosition() = syncPlaybackState()

    private fun syncQueue() {
        val player = controller ?: return
        val known = _queue.value.associateBy { it.id }
        val items = (0 until player.mediaItemCount).mapNotNull { index ->
            val mediaItem = player.getMediaItemAt(index)
            known[mediaItem.mediaId] ?: Song(
                id = mediaItem.mediaId,
                title = mediaItem.mediaMetadata.title?.toString().orEmpty(),
                artist = mediaItem.mediaMetadata.artist?.toString().orEmpty(),
                album = mediaItem.mediaMetadata.albumTitle?.toString(),
                coverUrl = mediaItem.mediaMetadata.artworkUri?.toString(),
                audioUrl = mediaItem.localConfiguration?.uri?.toString()
            )
        }
        _queue.value = items
    }

    private fun syncCurrentItem() {
        val player = controller ?: return
        val index = player.currentMediaItemIndex.coerceAtLeast(0)
        _currentIndex.value = index
        val mediaItem = player.currentMediaItem
        if (mediaItem == null) {
            _currentSong.value = null
            return
        }
        _currentSong.value = _queue.value.getOrNull(index) ?: Song(
            id = mediaItem.mediaId,
            title = mediaItem.mediaMetadata.title?.toString().orEmpty(),
            artist = mediaItem.mediaMetadata.artist?.toString().orEmpty(),
            album = mediaItem.mediaMetadata.albumTitle?.toString(),
            coverUrl = mediaItem.mediaMetadata.artworkUri?.toString(),
            audioUrl = mediaItem.localConfiguration?.uri?.toString()
        )
    }

    private fun syncPlaybackState() {
        val player = controller ?: return
        _isPlaying.value = player.isPlaying
        _positionMs.value = player.currentPosition.coerceAtLeast(0L)
        val actualDuration = player.duration
        _durationMs.value = if (actualDuration > 0L) actualDuration
            else _currentSong.value?.durationMs?.coerceAtLeast(0L) ?: 0L
        _currentIndex.value = player.currentMediaItemIndex.coerceAtLeast(0)
    }

    /** Keep the MediaSession alive when the Activity is closed. */
    fun release() {
        // Deliberately do not release the MediaController here: playback must outlive the UI.
    }
}
