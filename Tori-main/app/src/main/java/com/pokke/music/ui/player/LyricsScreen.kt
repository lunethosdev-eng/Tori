package com.pokke.music.ui.player

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.shape.RoundedCornerShape
import coil.compose.AsyncImage
import com.pokke.music.data.model.LyricLine
import com.pokke.music.data.remote.MusicMetadataApi
import com.pokke.music.player.PlayerController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun LyricsScreen(onBack: () -> Unit) {
    val song by PlayerController.currentSong.collectAsState()
    val position by PlayerController.positionMs.collectAsState()
    val duration by PlayerController.durationMs.collectAsState()
    val playing by PlayerController.isPlaying.collectAsState()
    var lines by remember { mutableStateOf<List<LyricLine>>(emptyList()) }
    var plain by remember { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()
    val active = lines.indexOfLast { it.timeMs <= position }.coerceAtLeast(0)

    LaunchedEffect(song?.id) {
        val track = song ?: return@LaunchedEffect
        lines = track.lyrics; plain = track.lyricsText
        if (lines.isEmpty()) {
            loading = true
            val result = withContext(Dispatchers.IO) {
                runCatching { MusicMetadataApi.lyrics(track.title, track.artist, track.album, track.durationSeconds) }
            }
            result.onSuccess { lines = it.lines; plain = it.plain }
            loading = false
        }
    }
    LaunchedEffect(active, lines.size) { if (lines.isNotEmpty() && active in lines.indices) listState.animateScrollToItem((active - 2).coerceAtLeast(0)) }

    Box(Modifier.fillMaxSize().background(Color(0xFF0A0A0A))) {
        AsyncImage(song?.displayCoverUrl, null, Modifier.fillMaxSize().blur(65.dp).graphicsLayer { alpha = .43f }, contentScale = ContentScale.Crop)
        Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color(0x33000000), Color(0x99000000), Color(0xE6000000)))))
        Column(Modifier.fillMaxSize().statusBarsPadding()) {
            Row(Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                AsyncImage(song?.displayCoverUrl, null, Modifier.size(48.dp).clip(RoundedCornerShape(10.dp)), contentScale = ContentScale.Crop)
                Column(Modifier.weight(1f).padding(start = 12.dp)) {
                    Text(song?.title ?: "Letras", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
                    Text(song?.artist.orEmpty(), color = Color.White.copy(.6f), fontSize = 13.sp)
                }
                IconButton(onClick = onBack) { Icon(Icons.Default.Close, "Cerrar", tint = Color.White) }
            }
            if (loading) Text("Buscando letras en LRCLIB…", color = Color.White.copy(.55f), fontSize = 13.sp, modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp))
            if (lines.isNotEmpty()) {
                LazyColumn(state = listState, modifier = Modifier.weight(1f).fillMaxWidth(), contentPadding = PaddingValues(start = 24.dp, end = 24.dp, top = 70.dp, bottom = 60.dp), verticalArrangement = Arrangement.spacedBy(22.dp)) {
                    itemsIndexed(lines) { index, line ->
                        val selected = index == active
                        val color by animateColorAsState(if (selected) Color.White else Color.White.copy(.35f), label = "lyricColor")
                        val scale by animateFloatAsState(if (selected) 1f else .96f, animationSpec = spring(), label = "lyricScale")
                        Text(line.text, color = color, fontSize = if (selected) 27.sp else 23.sp, fontWeight = if (selected) FontWeight.Bold else FontWeight.SemiBold, lineHeight = 34.sp, modifier = Modifier.fillMaxWidth().scale(scale).clickable { PlayerController.seekTo(line.timeMs) })
                    }
                }
            } else {
                Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Text(if (loading) "" else plain ?: "No se encontraron letras sincronizadas para esta canción.", color = Color.White.copy(.7f), fontSize = 20.sp, lineHeight = 30.sp, textAlign = TextAlign.Start, modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp))
                }
            }
            Column(Modifier.fillMaxWidth().padding(horizontal = 24.dp).padding(bottom = 16.dp)) {
                Slider(value = if (duration > 0) position.toFloat().div(duration).coerceIn(0f, 1f) else 0f, onValueChange = { PlayerController.seekTo((it * duration).toLong()) }, colors = SliderDefaults.colors(thumbColor = Color.White, activeTrackColor = Color.White, inactiveTrackColor = Color.White.copy(.3f)))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { PlayerController.seekTo((position - 10000).coerceAtLeast(0)) }) { Icon(Icons.Default.SkipPrevious, null, tint = Color.White, modifier = Modifier.size(30.dp)) }
                    IconButton(onClick = { PlayerController.togglePlayPause() }) { Icon(if (playing) Icons.Default.Pause else Icons.Default.PlayArrow, null, tint = Color.White, modifier = Modifier.size(38.dp)) }
                    IconButton(onClick = { PlayerController.seekTo((position + 10000).coerceAtMost(duration)) }) { Icon(Icons.Default.SkipNext, null, tint = Color.White, modifier = Modifier.size(30.dp)) }
                }
            }
        }
    }
}
