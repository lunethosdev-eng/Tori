package com.pokke.music.tv.ui.player

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
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
import com.pokke.music.player.PlayerController
import com.pokke.music.tv.ui.components.tvFocusable

/**
 * Full-screen TV player: large cover, clear focus targets, progress and transport controls.
 */
@Composable
fun TvFullPlayerScreen(onBack: () -> Unit) {
    val song by PlayerController.currentSong.collectAsState()
    val playing by PlayerController.isPlaying.collectAsState()
    val pos by PlayerController.positionMs.collectAsState()
    val dur by PlayerController.durationMs.collectAsState()
    val shuffle by PlayerController.shuffleEnabled.collectAsState(initial = false)
    val repeat by PlayerController.repeatMode.collectAsState(initial = 0)
    val progress = if (dur > 0) (pos.toFloat() / dur).coerceIn(0f, 1f) else 0f
    val cover = song?.displayCoverUrl ?: song?.coverUrl

    fun fmt(ms: Long): String {
        val s = (ms / 1000).coerceAtLeast(0).toInt()
        return "%d:%02d".format(s / 60, s % 60)
    }

    Box(Modifier.fillMaxSize().background(Color(0xFF07070C))) {
        if (!cover.isNullOrBlank()) {
            AsyncImage(
                model = cover,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer { alpha = 0.35f; scaleX = 1.12f; scaleY = 1.12f }
                    .blur(48.dp)
            )
            Box(
                Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(Color.Black.copy(0.35f), Color.Black.copy(0.75f), Color(0xF007070C))
                        )
                    )
            )
        }

        Column(
            Modifier
                .fillMaxSize()
                .padding(horizontal = 48.dp, vertical = 32.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .size(52.dp)
                        .tvFocusable(onClick = onBack, shape = CircleShape, scaleFocused = 1.08f)
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = Color.White, modifier = Modifier.size(28.dp))
                }
                Spacer(Modifier.width(16.dp))
                Text(
                    "Reproduciendo",
                    color = Color.White.copy(0.55f),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(Modifier.height(24.dp))

            Row(
                Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    Modifier
                        .size(320.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(Color(0xFF1A1A22))
                        .border(1.dp, Color.White.copy(0.12f), RoundedCornerShape(24.dp))
                ) {
                    if (!cover.isNullOrBlank()) {
                        AsyncImage(
                            model = cover,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }

                Spacer(Modifier.width(48.dp))

                Column(Modifier.weight(1f)) {
                    Text(
                        song?.title ?: "Sin reproducción",
                        color = Color.White,
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        song?.artist ?: "—",
                        color = Color.White.copy(0.7f),
                        fontSize = 22.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    song?.album?.takeIf { it.isNotBlank() }?.let {
                        Spacer(Modifier.height(4.dp))
                        Text(it, color = Color.White.copy(0.45f), fontSize = 16.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }

                    Spacer(Modifier.height(36.dp))

                    Slider(
                        value = progress,
                        onValueChange = { v -> if (dur > 0) PlayerController.seekTo((v * dur).toLong()) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = SliderDefaults.colors(
                            thumbColor = Color.White,
                            activeTrackColor = Color.White,
                            inactiveTrackColor = Color.White.copy(0.22f)
                        )
                    )
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(fmt(pos), color = Color.White.copy(0.55f), fontSize = 13.sp)
                        Text(fmt(dur), color = Color.White.copy(0.55f), fontSize = 13.sp)
                    }

                    Spacer(Modifier.height(28.dp))

                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(20.dp, Alignment.CenterHorizontally),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { PlayerController.toggleShuffle() },
                            modifier = Modifier
                                .size(56.dp)
                                .tvFocusable(onClick = { PlayerController.toggleShuffle() }, shape = CircleShape)
                        ) {
                            Icon(
                                Icons.Default.Shuffle,
                                contentDescription = "Aleatorio",
                                tint = if (shuffle) Color(0xFFBBA5FF) else Color.White.copy(0.7f),
                                modifier = Modifier.size(28.dp)
                            )
                        }
                        IconButton(
                            onClick = { PlayerController.playPrevious() },
                            modifier = Modifier
                                .size(64.dp)
                                .tvFocusable(onClick = { PlayerController.playPrevious() }, shape = CircleShape, scaleFocused = 1.1f)
                        ) {
                            Icon(Icons.Default.SkipPrevious, contentDescription = "Anterior", tint = Color.White, modifier = Modifier.size(40.dp))
                        }
                        Box(
                            Modifier
                                .size(88.dp)
                                .clip(CircleShape)
                                .background(Color.White)
                                .tvFocusable(onClick = { PlayerController.togglePlayPause() }, shape = CircleShape, scaleFocused = 1.08f),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                if (playing) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = if (playing) "Pausar" else "Reproducir",
                                tint = Color.Black,
                                modifier = Modifier.size(48.dp)
                            )
                        }
                        IconButton(
                            onClick = { PlayerController.playNext() },
                            modifier = Modifier
                                .size(64.dp)
                                .tvFocusable(onClick = { PlayerController.playNext() }, shape = CircleShape, scaleFocused = 1.1f)
                        ) {
                            Icon(Icons.Default.SkipNext, contentDescription = "Siguiente", tint = Color.White, modifier = Modifier.size(40.dp))
                        }
                        IconButton(
                            onClick = { PlayerController.cycleRepeatMode() },
                            modifier = Modifier
                                .size(56.dp)
                                .tvFocusable(onClick = { PlayerController.cycleRepeatMode() }, shape = CircleShape)
                        ) {
                            Icon(
                                Icons.Default.Repeat,
                                contentDescription = "Repetir",
                                tint = if (repeat != 0) Color(0xFFBBA5FF) else Color.White.copy(0.7f),
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
