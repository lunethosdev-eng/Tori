package com.pokke.music.ui.player

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.pokke.music.player.PlayerController

@Composable
fun MiniPlayerBar(
    visible: Boolean,
    glass: Boolean,
    onOpenPlayer: () -> Unit,
    modifier: Modifier = Modifier
) {
    val song by PlayerController.currentSong.collectAsState()
    val playing by PlayerController.isPlaying.collectAsState()
    val position by PlayerController.positionMs.collectAsState()
    val duration by PlayerController.durationMs.collectAsState()
    val progress = if (duration > 0) (position.toFloat() / duration).coerceIn(0f, 1f) else 0f

    AnimatedVisibility(
        visible = visible && song != null,
        enter = slideInVertically { it } + fadeIn(),
        exit = slideOutVertically { it } + fadeOut(),
        modifier = modifier
    ) {
        val s = song ?: return@AnimatedVisibility
        Column(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                .background(if (glass) Color(0xCC1C1C22) else Color(0xF01A1A1F))
                .clickable(onClick = onOpenPlayer)
        ) {
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth().height(2.dp),
                color = Color.White,
                trackColor = Color.White.copy(.15f)
            )
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AsyncImage(
                    s.coverUrl, null,
                    Modifier.size(44.dp).clip(RoundedCornerShape(8.dp)).background(Color(0xFF2A2A30)),
                    contentScale = ContentScale.Crop
                )
                Spacer(Modifier.width(10.dp))
                Column(Modifier.weight(1f)) {
                    Text(s.title, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(s.artist, color = Color.White.copy(.55f), fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                IconButton(onClick = { PlayerController.togglePlayPause() }) {
                    Icon(if (playing) Icons.Default.Pause else Icons.Default.PlayArrow, null, tint = Color.White)
                }
                IconButton(onClick = { PlayerController.playNext() }) {
                    Icon(Icons.Default.SkipNext, null, tint = Color.White)
                }
            }
        }
    }
}
