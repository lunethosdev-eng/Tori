package com.pokke.music.data.model

data class Song(
    val id: String,
    val title: String,
    val artist: String,
    val album: String? = null,
    val coverUrl: String? = null,
    val animatedCoverUrl: String? = null,
    val audioUrl: String? = null,
    val durationSeconds: Int = 0,
    val lyricsText: String? = null,
    val lyricsUrl: String? = null,
    val genre: String? = null,
    val youtubeId: String? = null,
    // Optional parsed lyrics for UI demos
    val lyrics: List<LyricLine> = emptyList()
) {
    val hasAnimatedCover: Boolean
        get() = !animatedCoverUrl.isNullOrBlank()

    val durationMs: Long
        get() = durationSeconds * 1000L
}

data class LyricLine(
    val timeMs: Long,
    val text: String
)

data class Playlist(
    val id: String,
    val name: String,
    val coverUrl: String? = null,
    val description: String? = null,
    val isPublic: Boolean = false,
    val songCount: Int = 0
)

data class UserProfile(
    val id: String,
    val displayName: String? = null,
    val username: String? = null,
    val avatarUrl: String? = null,
    val bannerUrl: String? = null,
    val bio: String? = null,
    val theme: String? = "default",
    val frame: String? = "none",
    val isPublic: Boolean = true,
    val isArtist: Boolean = false,
    val badge: String? = "none"
)
