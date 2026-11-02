package com.pokke.music.data.model

data class Song(
    val id: String,
    val title: String,
    val artist: String,
    val album: String = "",
    val coverUrl: String = "",
    val durationMs: Long = 0L,
    val lyrics: List<LyricLine> = emptyList()
)

data class LyricLine(
    val timeMs: Long,
    val text: String
)

data class Playlist(
    val id: String,
    val name: String,
    val coverUrl: String = "",
    val songCount: Int = 0
)
