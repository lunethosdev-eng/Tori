package com.pokke.music.player

import android.content.Context
import android.net.Uri
import android.widget.VideoView
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.music.bitchord.data.canvas.CanvasArtwork
import com.music.bitchord.data.model.Song as BitChordSong
import com.music.bitchord.ui.player.CanvasVideoSpec
import com.music.bitchord.ui.player.PlayerHost
import com.music.bitchord.ui.player.PlayerPlatform
import com.music.bitchord.ui.player.PokkeDefaultPlayerHost
import com.pokke.music.data.model.Song
import java.util.concurrent.ConcurrentHashMap

/** Supplies Pokke animated cover URLs to the original BitChord player surface. */
class PokkeAndroidPlayerHost(context: Context) : PlayerHost by PokkeDefaultPlayerHost {
    private val app = context.applicationContext
    // volume is provided by PokkeDefaultPlayerHost (PlayerHost delegation).
    // Custom SystemVolume override removed: SystemVolume symbol is not always
    // visible to the app module from bitchord-sharedui KMP on AGP 9.

    override fun cachedCanvas(song: BitChordSong): CanvasArtwork? = canvasForSong(song)

    override suspend fun canvasFor(song: BitChordSong): CanvasArtwork? = canvasForSong(song)

    private fun canvasForSong(song: BitChordSong): CanvasArtwork? {
        val source = artworkByVideoId[song.videoId] ?: return null
        val url = source.animatedCoverUrl?.takeIf(::isVideoUrl) ?: return null
        return CanvasArtwork(url = url, title = source.title, artist = source.artist, album = source.album)
    }

    @Composable
    override fun CanvasVideo(spec: CanvasVideoSpec, modifier: Modifier) {
        AndroidView(
            modifier = modifier,
            factory = { viewContext ->
                VideoView(viewContext).apply {
                    setVideoURI(Uri.parse(spec.canvas.url))
                    setOnPreparedListener { player ->
                        player.isLooping = true
                        player.setVolume(0f, 0f)
                        if (spec.isPlaying) start()
                        spec.onRenderedChanged(true)
                    }
                    setOnErrorListener { _, _, _ ->
                        spec.onRenderedChanged(false)
                        true
                    }
                }
            },
            update = { video ->
                if (spec.isPlaying && !video.isPlaying) video.start()
                else if (!spec.isPlaying && video.isPlaying) video.pause()
            },
            onRelease = { it.stopPlayback() },
        )
    }

    companion object {
        private val artworkByVideoId = ConcurrentHashMap<String, Song>()

        fun register(song: Song) {
            val id = song.youtubeId ?: song.id
            artworkByVideoId[id] = song
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
