package com.pokke.music.ui.player

import android.content.Intent
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.pokke.music.data.model.LyricLine
import com.pokke.music.data.repository.MusicRepository
import com.pokke.music.player.PlayerController

@Composable
fun LyricsScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val song by PlayerController.currentSong.collectAsState()
    val positionMs by PlayerController.positionMs.collectAsState()
    val durationMs by PlayerController.durationMs.collectAsState()
    val isPlaying by PlayerController.isPlaying.collectAsState()
    var lyricsText by remember(song?.id) { mutableStateOf(song?.lyricsText.orEmpty()) }
    var lyricsError by remember(song?.id) { mutableStateOf<String?>(null) }
    val listState = rememberLazyListState()
    val lines = remember(lyricsText) { parseLyrics(lyricsText) }
    val activeIndex = remember(lines, positionMs) {
        if (lines.isEmpty()) -1 else lines.indexOfLast { it.timeMs <= positionMs }.coerceAtLeast(0)
    }

    LaunchedEffect(song?.id, song?.lyricsText, song?.lyricsUrl) {
        val current = song ?: return@LaunchedEffect
        lyricsText = current.lyricsText.orEmpty()
        if (lyricsText.isBlank() && !current.lyricsUrl.isNullOrBlank()) {
            MusicRepository().getLyrics(current)
                .onSuccess { lyricsText = it.orEmpty() }
                .onFailure { lyricsError = it.localizedMessage ?: "No se pudieron cargar las letras" }
        }
    }

    LaunchedEffect(activeIndex) {
        if (activeIndex >= 0) listState.animateScrollToItem((activeIndex + 1).coerceAtMost(lines.lastIndex))
    }

    val cover = song?.displayCoverUrl
    Box(Modifier.fillMaxSize()) {
        if (!cover.isNullOrBlank()) AsyncImage(
            model = cover, contentDescription = null, contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize().blur(70.dp).graphicsLayer { alpha = .35f }
        )
        Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color(0xAA000000), Color(0xF20A0A0A)))))

        Column(Modifier.fillMaxSize().statusBarsPadding()) {
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (!cover.isNullOrBlank()) AsyncImage(
                    model = cover, contentDescription = null, contentScale = ContentScale.Crop,
                    modifier = Modifier.size(48.dp).clip(RoundedCornerShape(8.dp))
                )
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(song?.title ?: "Letras", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
                    Text(song?.artist.orEmpty(), color = Color.White.copy(alpha = .65f), fontSize = 13.sp, maxLines = 1)
                }
                IconButton(onClick = {
                    val shareText = buildString {
                        append(song?.title.orEmpty()).append(" — ").append(song?.artist.orEmpty()).append("\n\n")
                        append(lyricsText)
                    }
                    context.startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"; putExtra(Intent.EXTRA_TEXT, shareText)
                    }, "Compartir letras"))
                }) { Icon(Icons.Default.Share, contentDescription = "Compartir", tint = Color.White) }
                IconButton(onClick = onBack) { Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = Color.White) }
            }

            when {
                song == null -> Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Text("No hay ninguna canción reproduciéndose", color = Color.White.copy(alpha = .6f))
                }
                lines.isEmpty() -> Box(Modifier.weight(1f).fillMaxWidth().padding(28.dp), contentAlignment = Alignment.Center) {
                    Text(lyricsError ?: "Todavía no hay letras disponibles para esta canción", color = Color.White.copy(alpha = .65f), textAlign = TextAlign.Center)
                }
                else -> LazyColumn(
                    state = listState,
                    modifier = Modifier.weight(1f).fillMaxWidth().padding(horizontal = 28.dp),
                    verticalArrangement = Arrangement.spacedBy(18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    item { Spacer(Modifier.height(40.dp)) }
                    itemsIndexed(lines) { index, line ->
                        val current = index == activeIndex
                        val scale by animateFloatAsState(
                            targetValue = if (current) 1.06f else 1f,
                            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                            label = "lyricScale"
                        )
                        Text(
                            text = line.text,
                            color = if (current) Color.White else Color.White.copy(alpha = .38f),
                            fontSize = if (current) 22.sp else 18.sp,
                            fontWeight = if (current) FontWeight.SemiBold else FontWeight.Normal,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.scale(scale).clickable { PlayerController.seekTo(line.timeMs) }.padding(vertical = 4.dp)
                        )
                    }
                    item { Spacer(Modifier.height(70.dp)) }
                }
            }

            Column(Modifier.fillMaxWidth().background(Color(0xEE0A0A0A)).padding(horizontal = 24.dp, vertical = 14.dp)) {
                val progress = if (durationMs > 0) (positionMs.toFloat() / durationMs).coerceIn(0f, 1f) else 0f
                Slider(
                    value = progress,
                    onValueChange = PlayerController::seekToFraction,
                    colors = SliderDefaults.colors(thumbColor = Color.White, activeTrackColor = Color.White, inactiveTrackColor = Color.White.copy(alpha = .25f))
                )
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(formatTime(positionMs), color = Color.White.copy(alpha = .6f), fontSize = 12.sp)
                    Text("-${formatTime((durationMs - positionMs).coerceAtLeast(0L))}", color = Color.White.copy(alpha = .6f), fontSize = 12.sp)
                }
                Spacer(Modifier.height(10.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = PlayerController::playPrevious) { Icon(Icons.Default.SkipPrevious, null, tint = Color.White, modifier = Modifier.size(34.dp)) }
                    Box(Modifier.size(54.dp).clip(CircleShape).background(Color.White).clickable { PlayerController.togglePlayPause() }, contentAlignment = Alignment.Center) {
                        Icon(if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow, null, tint = Color.Black, modifier = Modifier.size(30.dp))
                    }
                    IconButton(onClick = PlayerController::playNext) { Icon(Icons.Default.SkipNext, null, tint = Color.White, modifier = Modifier.size(34.dp)) }
                }
            }
        }
    }
}

private fun parseLyrics(raw: String): List<LyricLine> {
    if (raw.isBlank()) return emptyList()
    val pattern = Regex("\\[(\\d{1,2}):(\\d{2})(?:\\.(\\d{1,3}))?]")
    val parsed = raw.lineSequence().flatMap { line ->
        val matches = pattern.findAll(line).toList()
        if (matches.isEmpty()) {
            val text = line.trim()
            if (text.isEmpty() || text.startsWith("Letras de", ignoreCase = true) || text.startsWith("Procesado", ignoreCase = true)) emptySequence()
            else sequenceOf(LyricLine(0L, text))
        } else {
            val text = line.substring(matches.last().range.last + 1).trim()
            if (text.isBlank()) emptySequence() else matches.asSequence().map { match ->
                val minutes = match.groupValues[1].toLongOrNull() ?: 0L
                val seconds = match.groupValues[2].toLongOrNull() ?: 0L
                val fraction = match.groupValues[3].padEnd(3, '0').take(3).toLongOrNull() ?: 0L
                LyricLine((minutes * 60L + seconds) * 1000L + fraction, text)
            }
        }
    }.toList().sortedBy { it.timeMs }
    return parsed
}

private fun formatTime(ms: Long): String {
    val totalSeconds = (ms.coerceAtLeast(0L) / 1000L).toInt()
    return "%d:%02d".format(totalSeconds / 60, totalSeconds % 60)
}
