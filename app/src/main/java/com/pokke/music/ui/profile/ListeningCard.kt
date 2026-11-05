package com.pokke.music.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.pokke.music.data.model.LyricLine
import com.pokke.music.data.model.Song
import com.pokke.music.player.PlayerController

private fun formatMs(ms: Long): String {
    val total = (ms / 1000).toInt().coerceAtLeast(0)
    return "%d:%02d".format(total / 60, total % 60)
}

/** Pick the lyric line that is currently active at [positionMs]. */
fun currentLyricLine(lyrics: List<LyricLine>, positionMs: Long): LyricLine? {
    if (lyrics.isEmpty()) return null
    var current: LyricLine? = null
    for (line in lyrics) {
        if (line.timeMs <= positionMs) current = line else break
    }
    return current
}

/**
 * Discord-style "Listening to" card with live progress + current lyric line.
 */
@Composable
fun ListeningCard(
    accent: Color,
    cardBg: Color,
    ink: Color,
    muted: Color,
    live: Boolean = true,
    fallbackSong: Song? = null,
    showLyrics: Boolean = true,
    modifier: Modifier = Modifier
) {
    val current by PlayerController.currentSong.collectAsState()
    val playing by PlayerController.isPlaying.collectAsState()
    val position by PlayerController.positionMs.collectAsState()
    val duration by PlayerController.durationMs.collectAsState()

    val song = if (live) current else fallbackSong
    if (song == null) return

    val progress = if (duration > 0) (position.toFloat() / duration).coerceIn(0f, 1f) else 0f
    val lyric = remember(song.id, position, showLyrics) {
        if (!showLyrics) null
        else if (song.lyrics.isNotEmpty()) currentLyricLine(song.lyrics, position)
        else song.lyricsText?.lineSequence()?.firstOrNull { it.isNotBlank() }?.let { LyricLine(0, it) }
    }

    Column(
        modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(cardBg)
            .padding(14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.MusicNote, null, tint = accent, modifier = Modifier.size(14.dp))
            Spacer(Modifier.width(6.dp))
            Text(
                if (playing || live) "Escuchando en Pokke" else "Escuchó recientemente",
                color = muted,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
        }
        Spacer(Modifier.height(10.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF1A1D2A))
            ) {
                val cover = song.displayCoverUrl
                if (!cover.isNullOrBlank()) {
                    AsyncImage(cover, "Cover", Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                }
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    song.title,
                    color = ink,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    song.artist,
                    color = muted,
                    fontSize = 13.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (live && duration > 0) {
                    Spacer(Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier.fillMaxWidth().height(3.dp).clip(RoundedCornerShape(2.dp)),
                        color = accent,
                        trackColor = Color.White.copy(alpha = 0.12f)
                    )
                    Spacer(Modifier.height(4.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(formatMs(position), color = muted, fontSize = 11.sp)
                        Text(formatMs(duration), color = muted, fontSize = 11.sp)
                    }
                }
            }
        }
        // Current lyric line
        if (lyric != null && lyric.text.isNotBlank()) {
            Spacer(Modifier.height(12.dp))
            Box(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(accent.copy(alpha = 0.10f))
                    .padding(horizontal = 12.dp, vertical = 10.dp)
            ) {
                Text(
                    text = "“${lyric.text.trim()}”",
                    color = ink.copy(alpha = 0.92f),
                    fontSize = 13.sp,
                    fontStyle = FontStyle.Italic,
                    lineHeight = 18.sp,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
