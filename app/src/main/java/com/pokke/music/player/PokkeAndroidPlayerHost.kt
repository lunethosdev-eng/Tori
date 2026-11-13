package com.pokke.music.player

import android.content.Context
import android.net.Uri
import android.widget.VideoView
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.viewinterop.AndroidView
import com.music.bitchord.data.canvas.CanvasArtwork
import com.music.bitchord.data.model.Song as BitChordSong
import com.music.bitchord.ui.player.CanvasVideoSpec
import com.music.bitchord.ui.player.PlayerHost
import com.music.bitchord.ui.player.PlayerPlatform
import com.music.bitchord.ui.player.PlayerSettingsSource
import com.music.bitchord.ui.player.PokkeDefaultPlayerHost
import com.pokke.music.data.model.Song
import com.pokke.music.data.canvas.ArtistCanvas
import com.pokke.music.data.canvas.ExternalCanvasRepository
import com.pokke.music.data.preferences.UserPreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/** Supplies Pokke animated cover URLs to the original BitChord player surface. */
class PokkeAndroidPlayerHost(context: Context) : PlayerHost by PokkeDefaultPlayerHost {
    private val app = context.applicationContext
    private val preferenceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val canvasEnabledState = MutableStateFlow(true)
    @Volatile private var localCanvasEnabled: Boolean = true

    override val settings: PlayerSettingsSource = object : PlayerSettingsSource by PokkeDefaultPlayerHost.settings {
        override val animatedCanvas: StateFlow<Boolean> = canvasEnabledState
    }

    init {
        preferenceScope.launch {
            UserPreferences(app).pokkeAnimatedCoversEnabled.collect { enabled ->
                // This preference controls only the bundled local library;
                // externally supplied animated artwork remains available.
                localCanvasEnabled = enabled
            }
        }
    }

    // volume is provided by PokkeDefaultPlayerHost (PlayerHost delegation).
    // Custom SystemVolume override removed: SystemVolume symbol is not always
    // visible to the app module from bitchord-sharedui KMP on AGP 9.

    override fun cachedCanvas(song: BitChordSong): CanvasArtwork? = canvasForSong(song)

    override suspend fun canvasFor(song: BitChordSong): CanvasArtwork? {
        // Refresh preferences on the suspending path; cachedCanvas remains a
        // cheap, non-blocking lookup for bundled clips.
        val preferences = UserPreferences(app)
        localCanvasEnabled = runCatching { preferences.pokkeAnimatedCoversEnabled.first() }.getOrDefault(true)
        val externalEnabled = runCatching { preferences.externalCanvasEnabled.first() }.getOrDefault(true)
        val source = artworkFor(song.videoId)
        val suppliedUrl = source?.animatedCoverUrl?.takeIf(::isVideoUrl)
        if (suppliedUrl != null) {
            return CanvasArtwork(url = suppliedUrl, title = source.title, artist = source.artist, album = source.album)
        }
        if (externalEnabled) {
            ExternalCanvasRepository.find(song.title, song.artist, song.albumName)?.let { return it }
        }
        return canvasForSong(song)
    }

    private fun canvasForSong(song: BitChordSong): CanvasArtwork? {
        val source = artworkFor(song.videoId)
        val remoteUrl = source?.animatedCoverUrl?.takeIf(::isVideoUrl)
        if (remoteUrl != null) {
            return CanvasArtwork(url = remoteUrl, title = source.title, artist = source.artist, album = source.album)
        }
        if (!localCanvasEnabled) return null
        val localUri = ArtistCanvas.uriFor(app, source?.artist ?: song.artist, source?.id ?: song.videoId)
            ?: return null
        return CanvasArtwork(
            url = localUri.toString(),
            title = source?.title ?: song.title,
            artist = source?.artist ?: song.artist,
            album = source?.album,
        )
    }

    @Composable
    override fun CanvasVideo(spec: CanvasVideoSpec, modifier: Modifier) {
        val shouldPlay = spec.isPlaying && !spec.pausedForTransition
        AndroidView(
            modifier = modifier.graphicsLayer {
                alpha = spec.presentationAlpha().coerceIn(0f, 1f)
            },
            factory = { viewContext ->
                VideoView(viewContext).apply {
                    tag = shouldPlay
                    setVideoURI(Uri.parse(spec.canvas.url))
                    setOnPreparedListener { player ->
                        player.isLooping = true
                        player.setVolume(0f, 0f)
                        if (spec.startPositionMs > 0L) seekTo(spec.startPositionMs.toInt())
                        if (tag as? Boolean == true) start()
                        spec.onRenderedChanged(true)
                    }
                    setOnErrorListener { _, _, _ ->
                        spec.onRenderedChanged(false)
                        true
                    }
                }
            },
            update = { video ->
                video.tag = shouldPlay
                if (shouldPlay && !video.isPlaying) {
                    video.start()
                    spec.onResumed()
                } else if (!shouldPlay && video.isPlaying) {
                    video.pause()
                }
            },
            onRelease = { it.stopPlayback() },
        )
    }

    companion object {
        private val artworkByVideoId = object : LinkedHashMap<String, Song>(128, 0.75f, true) {
            override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, Song>?): Boolean = size > 128
        }

        private fun artworkFor(id: String): Song? = synchronized(artworkByVideoId) { artworkByVideoId[id] }

        fun register(song: Song) = synchronized(artworkByVideoId) {
            // Keep both identifiers: queue entries may arrive keyed by the
            // database id while the shared player asks by YouTube video id.
            artworkByVideoId[song.id] = song
            song.youtubeId?.takeIf(String::isNotBlank)?.let { artworkByVideoId[it] = song }
        }

        private fun isVideoUrl(url: String): Boolean {
            val path = runCatching { Uri.parse(url).lastPathSegment.orEmpty() }.getOrDefault("").lowercase()
            return path.endsWith(".mp4") || path.endsWith(".webm") || path.endsWith(".m3u8")
        }

        fun install(context: Context) {
            PlayerPlatform.install(PokkeAndroidPlayerHost(context))
        }
    }
}
