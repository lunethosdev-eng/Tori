package com.pokke.music.ui.queue

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.pokke.music.data.model.Song
import com.pokke.music.player.PlayerController

@Composable
fun QueueScreen(onBack: () -> Unit) {
    val current by PlayerController.currentSong.collectAsState()
    val isPlaying by PlayerController.isPlaying.collectAsState()
    val queue by PlayerController.queue.collectAsState()
    val currentIndex by PlayerController.currentIndex.collectAsState()
    val cover = current?.coverUrl
    val upcoming = queue.mapIndexedNotNull { index, song ->
        if (index > currentIndex) index to song else null
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (!cover.isNullOrBlank()) {
            AsyncImage(
                model = cover,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize().blur(70.dp).graphicsLayer { alpha = 0.30f }
            )
        }
        Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color(0xBB000000), Color(0xF0000000)))))

        Column(modifier = Modifier.fillMaxSize().statusBarsPadding()) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f)) {
                    Text("Cola de reproducción", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                    Text("${queue.size} canciones", color = Color.White.copy(alpha = 0.55f), fontSize = 13.sp)
                }
                Text("Listo", color = Color.White.copy(alpha = 0.75f), modifier = Modifier.clickable(onClick = onBack).padding(8.dp))
            }

            if (current != null) {
                Text("REPRODUCIENDO AHORA", color = Color.White.copy(alpha = 0.55f), fontSize = 11.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp))
                QueueSongRow(
                    song = current!!,
                    active = true,
                    playing = isPlaying,
                    onClick = { PlayerController.togglePlayPause() },
                    onRemove = null,
                    onMoveUp = null,
                    onMoveDown = null
                )
            }

            Text("A CONTINUACIÓN", color = Color.White.copy(alpha = 0.55f), fontSize = 11.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp))

            LazyColumn(modifier = Modifier.weight(1f)) {
                if (upcoming.isEmpty()) {
                    item {
                        Text(
                            "No hay más canciones en la cola",
                            color = Color.White.copy(alpha = 0.45f),
                            fontSize = 13.sp,
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
                        )
                    }
                }
                items(upcoming, key = { it.first }) { (index, song) ->
                    QueueSongRow(
                        song = song,
                        active = false,
                        playing = false,
                        onClick = { PlayerController.playQueueItem(index) },
                        onRemove = { PlayerController.removeQueueItem(index) },
                        onMoveUp = if (index > currentIndex + 1) ({ PlayerController.moveQueueItem(index, index - 1) }) else null,
                        onMoveDown = if (index < queue.lastIndex) ({ PlayerController.moveQueueItem(index, index + 1) }) else null
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth().background(Color(0xEE111111)).padding(horizontal = 24.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { PlayerController.playPrevious() }) {
                    Icon(Icons.Default.SkipPrevious, contentDescription = "Anterior", tint = Color.White, modifier = Modifier.size(30.dp))
                }
                IconButton(onClick = { PlayerController.togglePlayPause() }) {
                    Icon(if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow, contentDescription = "Reproducir o pausar", tint = Color.White, modifier = Modifier.size(34.dp))
                }
                IconButton(onClick = { PlayerController.playNext() }) {
                    Icon(Icons.Default.SkipNext, contentDescription = "Siguiente", tint = Color.White, modifier = Modifier.size(30.dp))
                }
                IconButton(onClick = { PlayerController.clearQueue() }) {
                    Icon(Icons.Default.Close, contentDescription = "Vaciar cola", tint = Color.White.copy(alpha = 0.7f))
                }
            }
        }
    }
}

@Composable
private fun QueueSongRow(
    song: Song,
    active: Boolean,
    playing: Boolean,
    onClick: () -> Unit,
    onRemove: (() -> Unit)?,
    onMoveUp: (() -> Unit)?,
    onMoveDown: (() -> Unit)?
) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(Modifier.size(48.dp).clip(RoundedCornerShape(7.dp)).background(Color(0xFF292929))) {
            if (!song.coverUrl.isNullOrBlank()) {
                AsyncImage(model = song.coverUrl, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
            }
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(song.title, color = if (active) Color(0xFF9BE7B0) else Color.White, fontSize = 14.sp, fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(song.artist, color = Color.White.copy(alpha = 0.58f), fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        if (onMoveUp != null) IconButton(onClick = onMoveUp) { Icon(Icons.Default.ArrowUpward, contentDescription = "Subir", tint = Color.White.copy(alpha = 0.6f)) }
        if (onMoveDown != null) IconButton(onClick = onMoveDown) { Icon(Icons.Default.ArrowDownward, contentDescription = "Bajar", tint = Color.White.copy(alpha = 0.6f)) }
        if (onRemove != null) IconButton(onClick = onRemove) { Icon(Icons.Default.Close, contentDescription = "Quitar", tint = Color.White.copy(alpha = 0.6f)) }
    }
}
