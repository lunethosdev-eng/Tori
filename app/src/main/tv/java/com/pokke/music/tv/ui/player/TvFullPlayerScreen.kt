package com.pokke.music.tv.ui.player

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Shuffle
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
                        IconButton(onClick = { /* shuffle */ }) { Icon(Icons.Default.Shuffle, null, tint = Color.White) }
                        IconButton(onClick = { PlayerController.playPrevious() }) { Icon(Icons.Default.SkipPrevious, null, tint = Color.White, modifier = Modifier.size(36.dp)) }
                        IconButton(onClick = { PlayerController.togglePlayPause() }) {
                            Icon(if (playing) Icons.Default.Pause else Icons.Default.PlayArrow, null, tint = Color.White, modifier = Modifier.size(48.dp))
                        }
                        IconButton(onClick = { PlayerController.playNext() }) { Icon(Icons.Default.SkipNext, null, tint = Color.White, modifier = Modifier.size(36.dp)) }
                        IconButton(onClick = { /* repeat */ }) { Icon(Icons.Default.Repeat, null, tint = Color.White) }
                    }
                    Spacer(Modifier.height(20.dp))
                    Text("🎤 Activar Lyrics (Karaoke)", color = Color(0xFFD0D0D8), fontSize = 15.sp)
                }
            }
        }
    }
}
