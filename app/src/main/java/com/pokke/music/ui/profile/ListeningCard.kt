package com.pokke.music.ui.profile

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
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

fun currentLyricLine(lyrics: List<LyricLine>, positionMs: Long): LyricLine? {
    if (lyrics.isEmpty()) return null
    var current: LyricLine? = null
    for (line in lyrics) {
        if (line.timeMs <= positionMs) current = line else break
    }
    return current
}

@Composable
fun KaraokeLyricText(
    text: String,
    accent: Color,
    ink: Color,
    modifier: Modifier = Modifier
) {
    val clean = text.trim().removePrefix("\"").removeSuffix("\"")
    // Strip leading [mm:ss.xx] timestamps if present
    val body = clean.replace(Regex("^\\[\\d{2}:\\d{2}(?:\\.\\d+)?]\\s*"), "")
    val transition = rememberInfiniteTransition(label = "karaoke")
    val progress by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = (1200 + body.length * 40).coerceIn(1400, 4200), easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "kprog"
    )
    // Color pairs: highlight 2 characters at a time
    val pairCount = ((body.length + 1) / 2).coerceAtLeast(1)
    val activePair = (progress * pairCount).toInt().coerceIn(0, pairCount - 1)
    val activeStart = activePair * 2

    val gradientColors = listOf(
        accent,
        Color(0xFFFF9FCB),
        Color(0xFF8CB7FF),
        Color(0xFF8FE0B0),
        Color(0xFFE5C17A)
    )
    val hi = gradientColors[activePair % gradientColors.size]

    Text(
        text = buildAnnotatedString {
            body.forEachIndexed { i, c ->
                val isActive = i == activeStart || i == activeStart + 1
                withStyle(
                    SpanStyle(
                        color = if (isActive) hi else ink.copy(alpha = 0.55f),
                        fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
                        fontStyle = FontStyle.Italic
                    )
                ) { append(c) }
            }
        },
        fontSize = 14.sp,
        lineHeight = 20.sp,
        maxLines = 3,
        overflow = TextOverflow.Ellipsis,
        modifier = modifier
    )
}

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
        else song.lyricsText?.lineSequence()?.map { it.trim() }?.firstOrNull { it.isNotBlank() }?.let { LyricLine(0, it) }
    }

    Column(
        modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(
                Brush.verticalGradient(
                    listOf(cardBg, cardBg.copy(alpha = 0.92f), accent.copy(alpha = 0.08f))
                )
            )
            .padding(14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.MusicNote, null, tint = accent, modifier = Modifier.size(14.dp))
            Spacer(Modifier.width(6.dp))
            Text(
                if (playing || live) "Escuchando en Pokke" else "Escuchó recientemente",
                color = muted, fontSize = 12.sp, fontWeight = FontWeight.Medium
            )
        }
        Spacer(Modifier.height(10.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(56.dp).clip(RoundedCornerShape(10.dp)).background(Color(0xFF1A1D2A))) {
                song.displayCoverUrl?.let {
                    AsyncImage(it, "Cover", Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                }
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(song.title, color = ink, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(song.artist, color = muted, fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                if (live && duration > 0) {
                    Spacer(Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp)),
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
        if (lyric != null && lyric.text.isNotBlank()) {
            Spacer(Modifier.height(12.dp))
            Box(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.Black.copy(alpha = 0.28f))
                    .padding(horizontal = 12.dp, vertical = 10.dp)
            ) {
                KaraokeLyricText(text = lyric.text, accent = accent, ink = ink)
            }
        }
    }
}
