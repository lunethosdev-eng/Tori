package com.pokke.music.tv.ui.player

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.pokke.music.player.PlayerController

@Composable
fun TvMiniPlayer(onOpen: () -> Unit, modifier: Modifier = Modifier) {
    val song by PlayerController.currentSong.collectAsState()
    val playing by PlayerController.isPlaying.collectAsState()
    val s = song ?: return

    Row(
        modifier
            .fillMaxWidth()
            .padding(horizontal = 48.dp)
            .background(Color(0xEE1A1A22), RoundedCornerShape(16.dp))
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(s.coverUrl, null, Modifier.size(48.dp).background(Color(0xFF333), RoundedCornerShape(8.dp)), contentScale = ContentScale.Crop)
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(s.title, color = Color.White, maxLines = 1, overflow = TextOverflow.Ellipsis, fontWeight = FontWeight.SemiBold)
            Text(s.artist, color = Color(0xFF9A9AA3), maxLines = 1, fontSize = 12.sp)
        }
        IconButton(onClick = { PlayerController.playPrevious() }) { Icon(Icons.Default.SkipPrevious, null, tint = Color.White) }
        IconButton(onClick = { PlayerController.togglePlayPause() }) {
            Icon(if (playing) Icons.Default.Pause else Icons.Default.PlayArrow, null, tint = Color.White)
        }
        IconButton(onClick = { PlayerController.playNext() }) { Icon(Icons.Default.SkipNext, null, tint = Color.White) }
        IconButton(onClick = onOpen) { Text("⤢", color = Color.White, fontSize = 18.sp) }
    }
}
