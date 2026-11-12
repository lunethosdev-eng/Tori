package com.pokke.music.tv.ui.player

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.pokke.music.player.PlayerController

@Composable
fun TvFullPlayerScreen(onBack: () -> Unit) {
    val song by PlayerController.currentSong.collectAsState()
    val playing by PlayerController.isPlaying.collectAsState()
    val pos by PlayerController.positionMs.collectAsState()
    val dur by PlayerController.durationMs.collectAsState()
    val queue by PlayerController.queue.collectAsState()
    val queueIndex by PlayerController.currentIndex.collectAsState()
    val shuffle by PlayerController.shuffleEnabled.collectAsState()
    val repeat by PlayerController.repeatMode.collectAsState()
    var showQueue by remember { mutableStateOf(false) }
    var showLyrics by remember { mutableStateOf(false) }
    val progress = if (dur > 0) pos.toFloat() / dur else 0f

    fun fmt(ms: Long): String {
        val s = (ms / 1000).toInt()
        return "%d:%02d".format(s / 60, s % 60)
    }

    Box(Modifier.fillMaxSize()) {
        song?.coverUrl?.let {
            AsyncImage(it, null, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
            Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color.Black.copy(0.55f), Color.Black.copy(0.92f)))))
        } ?: Box(Modifier.fillMaxSize().background(Color(0xFF0B0B10)))

        Column(Modifier.fillMaxSize().padding(40.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White)
                }
                Text("Reproduciendo", color = Color(0xFFB8B8C0), fontSize = 14.sp)
            }
            Spacer(Modifier.height(24.dp))
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                AsyncImage(
                    song?.coverUrl, null,
                    Modifier.size(280.dp).background(Color(0xFF222), RoundedCornerShape(16.dp)),
                    contentScale = ContentScale.Crop
                )
                Spacer(Modifier.width(40.dp))
                Column(Modifier.weight(1f)) {
                    Text(song?.title ?: "—", color = Color.White, fontSize = 34.sp, fontWeight = FontWeight.Bold)
                    Text(song?.artist ?: "", color = Color(0xFFC8C8D0), fontSize = 20.sp)
                    Spacer(Modifier.height(28.dp))
                    Slider(
                        value = progress,
                        onValueChange = { v -> if (dur > 0) PlayerController.seekTo((v * dur).toLong()) },
                        colors = SliderDefaults.colors(thumbColor = Color.White, activeTrackColor = Color.White, inactiveTrackColor = Color.White.copy(0.25f))
                    )
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(fmt(pos), color = Color(0xFFAAA), fontSize = 12.sp)
                        Text(fmt(dur), color = Color(0xFFAAA), fontSize = 12.sp)
                    }
                    Spacer(Modifier.height(18.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(18.dp), verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { PlayerController.toggleShuffle() }) { Icon(Icons.Default.Shuffle, null, tint = if (shuffle) Color(0xFF8AB4F8) else Color.White) }
                        IconButton(onClick = { PlayerController.playPrevious() }) { Icon(Icons.Default.SkipPrevious, null, tint = Color.White, modifier = Modifier.size(36.dp)) }
                        IconButton(onClick = { PlayerController.togglePlayPause() }) {
                            Icon(if (playing) Icons.Default.Pause else Icons.Default.PlayArrow, null, tint = Color.White, modifier = Modifier.size(48.dp))
                        }
                        IconButton(onClick = { PlayerController.playNext() }) { Icon(Icons.Default.SkipNext, null, tint = Color.White, modifier = Modifier.size(36.dp)) }
                        IconButton(onClick = { PlayerController.cycleRepeatMode() }) { Icon(Icons.Default.Repeat, null, tint = if (repeat != androidx.media3.common.Player.REPEAT_MODE_OFF) Color(0xFF8AB4F8) else Color.White) }
                    }
                    Spacer(Modifier.height(20.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                        androidx.compose.material3.OutlinedButton(onClick = { showQueue = !showQueue }) {
                            Icon(Icons.Default.QueueMusic, null)
                            Spacer(Modifier.width(8.dp))
                            Text(if (showQueue) "Ocultar cola" else "Cola (${queue.size})")
                        }
                        androidx.compose.material3.OutlinedButton(onClick = { showLyrics = !showLyrics }) {
                            Text(if (showLyrics) "Ocultar letras" else "Letras")
                        }
                    }
                    if (showQueue) {
                        Spacer(Modifier.height(12.dp))
                        Text("A continuación", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                        LazyColumn(Modifier.heightIn(max = 180.dp)) {
                            itemsIndexed(queue) { index, queuedSong ->
                                Row(
                                    Modifier.fillMaxWidth()
                                        .background(if (index == queueIndex) Color.White.copy(0.12f) else Color.Transparent, RoundedCornerShape(8.dp))
                                        .padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(Modifier.weight(1f)) {
                                        Text(queuedSong.title, color = Color.White, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                        Text(queuedSong.artist, color = Color(0xFFAAAAAA), fontSize = 11.sp)
                                    }
                                    androidx.compose.material3.TextButton(onClick = { PlayerController.playQueueItem(index) }) { Text("Reproducir") }
                                }
                            }
                        }
                    }
                    if (showLyrics) {
                        Spacer(Modifier.height(12.dp))
                        Text("Letras", color = Color.White, fontWeight = FontWeight.SemiBold)
                        Text(song?.lyricsText ?: "No hay letras disponibles para esta canción.", color = Color(0xFFD0D0D8), fontSize = 14.sp)
                    }
                }
            }
        }
    }
}
