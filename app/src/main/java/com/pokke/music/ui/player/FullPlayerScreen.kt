package com.pokke.music.ui.player

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.outlined.QueueMusic
import androidx.compose.material.icons.outlined.Speaker
import androidx.compose.material.icons.outlined.Subtitles
import androidx.compose.material3.*
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.pokke.music.player.PlayerController
import com.pokke.music.ui.sheets.AudioOutputSheet
import com.pokke.music.ui.sheets.OverflowMenuSheet
import com.pokke.music.ui.sheets.ShareLyricsSheet

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FullPlayerScreen(
    onBack: () -> Unit,
    onOpenQueue: () -> Unit,
    onOpenLyrics: () -> Unit
) {
    val context = LocalContext.current
    val current by PlayerController.currentSong.collectAsState()
    val isPlaying by PlayerController.isPlaying.collectAsState()
    val positionMs by PlayerController.positionMs.collectAsState()
    val durationMs by PlayerController.durationMs.collectAsState()

    val cover = current?.coverUrl

    var volume by remember { mutableFloatStateOf(0.8f) }
    var showAudioOutput by remember { mutableStateOf(false) }
    var showOverflow by remember { mutableStateOf(false) }
    var showShareLyrics by remember { mutableStateOf(false) }

    val progress = if (durationMs > 0) (positionMs.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f) else 0f

    val playScale by animateFloatAsState(
        targetValue = if (isPlaying) 1f else 0.92f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "playScale"
    )

    Box(modifier = Modifier.fillMaxSize()) {
        if (!cover.isNullOrBlank()) {
            AsyncImage(
                model = ImageRequest.Builder(context).data(cover).crossfade(true).build(),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .blur(60.dp)
                    .graphicsLayer { alpha = 0.55f }
            )
        }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(listOf(Color(0x99000000), Color(0xEE000000))))
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = 24.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Close", tint = Color.White, modifier = Modifier.size(32.dp))
                }
                Text("Playing from Buscar", color = Color.White.copy(alpha = 0.7f), fontSize = 13.sp)
                IconButton(onClick = { showOverflow = true }) {
                    Icon(Icons.Default.MoreHoriz, contentDescription = "More", tint = Color.White)
                }
            }

            Spacer(modifier = Modifier.weight(0.05f))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp)
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF1A1A1A))
            ) {
                if (!cover.isNullOrBlank()) {
                    AsyncImage(
                        model = ImageRequest.Builder(context).data(cover).crossfade(true).build(),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = current?.title ?: "From The Start",
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = current?.artist ?: "Laufey",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 15.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Listening to you harp on 'bout some new s...",
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 14.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onOpenLyrics() }
                    .padding(vertical = 4.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Column {
                Slider(
                    value = progress,
                    onValueChange = { PlayerController.seekToFraction(it) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = SliderDefaults.colors(
                        thumbColor = Color.White,
                        activeTrackColor = Color.White,
                        inactiveTrackColor = Color.White.copy(alpha = 0.25f)
                    )
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(formatTime(positionMs), color = Color.White.copy(alpha = 0.6f), fontSize = 12.sp)
                    Text("-${formatTime(durationMs - positionMs)}", color = Color.White.copy(alpha = 0.6f), fontSize = 12.sp)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = {}, modifier = Modifier.size(56.dp)) {
                    Icon(Icons.Default.SkipPrevious, contentDescription = null, tint = Color.White, modifier = Modifier.size(40.dp))
                }
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .scale(playScale)
                        .clip(CircleShape)
                        .background(Color.White)
                        .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) {
                            PlayerController.togglePlayPause()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = null,
                        tint = Color.Black,
                        modifier = Modifier.size(40.dp)
                    )
                }
                IconButton(onClick = {}, modifier = Modifier.size(56.dp)) {
                    Icon(Icons.Default.SkipNext, contentDescription = null, tint = Color.White, modifier = Modifier.size(40.dp))
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.VolumeUp, contentDescription = null, tint = Color.White.copy(alpha = 0.6f), modifier = Modifier.size(20.dp))
                Slider(
                    value = volume,
                    onValueChange = { volume = it },
                    modifier = Modifier.weight(1f).padding(horizontal = 12.dp),
                    colors = SliderDefaults.colors(
                        thumbColor = Color.White,
                        activeTrackColor = Color.White,
                        inactiveTrackColor = Color.White.copy(alpha = 0.25f)
                    )
                )
            }

            Spacer(modifier = Modifier.weight(0.1f))

            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onOpenLyrics) {
                    Icon(Icons.Outlined.Subtitles, contentDescription = "Lyrics", tint = Color.White.copy(alpha = 0.85f), modifier = Modifier.size(26.dp))
                }
                IconButton(onClick = { showAudioOutput = true }) {
                    Icon(Icons.Outlined.Speaker, contentDescription = "Audio Output", tint = Color.White.copy(alpha = 0.85f), modifier = Modifier.size(26.dp))
                }
                IconButton(onClick = onOpenQueue) {
                    Icon(Icons.Outlined.QueueMusic, contentDescription = "Queue", tint = Color.White.copy(alpha = 0.85f), modifier = Modifier.size(26.dp))
                }
            }

            Text(
                text = "CMF Buds 2a",
                color = Color.White.copy(alpha = 0.5f),
                fontSize = 12.sp,
                modifier = Modifier.align(Alignment.CenterHorizontally).padding(bottom = 16.dp)
            )
        }
    }

    if (showAudioOutput) {
        ModalBottomSheet(onDismissRequest = { showAudioOutput = false }, containerColor = Color(0xFF1C1C1C)) {
            AudioOutputSheet(onDismiss = { showAudioOutput = false })
        }
    }
    if (showOverflow) {
        ModalBottomSheet(onDismissRequest = { showOverflow = false }, containerColor = Color(0xFF1C1C1C)) {
            OverflowMenuSheet(
                onDismiss = { showOverflow = false },
                onShareLyrics = {
                    showOverflow = false
                    showShareLyrics = true
                }
            )
        }
    }
    if (showShareLyrics) {
        ModalBottomSheet(onDismissRequest = { showShareLyrics = false }, containerColor = Color(0xFF1C1C1C)) {
            ShareLyricsSheet(onDismiss = { showShareLyrics = false })
        }
    }
}

private fun formatTime(ms: Long): String {
    if (ms <= 0) return "0:00"
    val totalSec = (ms / 1000).toInt()
    return "%d:%02d".format(totalSec / 60, totalSec % 60)
}
