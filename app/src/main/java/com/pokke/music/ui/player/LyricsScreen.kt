package com.pokke.music.ui.player

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import coil.compose.AsyncImage
import com.pokke.music.data.model.LyricLine
import com.pokke.music.data.model.Song

private val demoSong = Song(
    id = "1",
    title = "From The Start",
    artist = "Laufey",
    coverUrl = "https://i.scdn.co/image/ab67616d0000b273c5649add07ed3740e1a0c3e0",
    durationMs = 168000L,
    lyrics = listOf(
        LyricLine(0, "What's a girl to do"),
        LyricLine(5000, "Lying on my bed staring"),
        LyricLine(10000, "Into the blue?"),
        LyricLine(15000, "Unrequited terrifying"),
        LyricLine(20000, "Love is driving me a bit insane"),
        LyricLine(25000, "Have to get this off my chest"),
        LyricLine(30000, "I'm telling you today"),
        LyricLine(35000, "That when I talk to you"),
        LyricLine(40000, "Oh, cupid walks right through"),
        LyricLine(45000, "And shoots an arrow through my heart"),
        LyricLine(50000, "And I sound like a loon"),
        LyricLine(55000, "But don't you feel it, too?"),
        LyricLine(60000, "Confess I loved you from the start")
    )
)

@Composable
fun LyricsScreen(onBack: () -> Unit) {
    var currentLineIndex by remember { mutableStateOf(8) } // "Oh, cupid walks right through"
    var progress by remember { mutableFloatStateOf(0.45f) }
    var isPlaying by remember { mutableStateOf(true) }
    val listState = rememberLazyListState()

    Box(modifier = Modifier.fillMaxSize()) {
        // Blurred background
        AsyncImage(
            model = demoSong.coverUrl,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .blur(80.dp)
                .graphicsLayer { alpha = 0.4f }
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0xAA000000), Color(0xEE000000))
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AsyncImage(
                    model = demoSong.coverUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(8.dp))
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = demoSong.title,
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = demoSong.artist,
                        color = Color.White.copy(alpha = 0.65f),
                        fontSize = 13.sp
                    )
                }
                IconButton(onClick = { /* share */ }) {
                    Icon(Icons.Default.Share, contentDescription = "Share", tint = Color.White)
                }
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                }
            }

            // Lyrics list
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 28.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                item { Spacer(modifier = Modifier.height(40.dp)) }
                itemsIndexed(demoSong.lyrics) { index, line ->
                    val isCurrent = index == currentLineIndex
                    val scale by animateFloatAsState(
                        targetValue = if (isCurrent) 1.08f else 1f,
                        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                        label = "lyricScale"
                    )
                    Text(
                        text = line.text,
                        color = if (isCurrent) Color.White else Color.White.copy(alpha = 0.35f),
                        fontSize = if (isCurrent) 22.sp else 18.sp,
                        fontWeight = if (isCurrent) FontWeight.SemiBold else FontWeight.Normal,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .scale(scale)
                            .clickable { currentLineIndex = index }
                            .padding(vertical = 4.dp)
                    )
                }
                item { Spacer(modifier = Modifier.height(80.dp)) }
            }

            // Bottom controls
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xEE0A0A0A))
                    .padding(horizontal = 24.dp, vertical = 16.dp)
            ) {
                Slider(
                    value = progress,
                    onValueChange = { progress = it },
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
                    Text("1:17", color = Color.White.copy(alpha = 0.6f), fontSize = 12.sp)
                    Text("-1:32", color = Color.White.copy(alpha = 0.6f), fontSize = 12.sp)
                }
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = {}) {
                        Icon(Icons.Default.SkipPrevious, null, tint = Color.White, modifier = Modifier.size(36.dp))
                    }
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                            .clickable { isPlaying = !isPlaying },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            if (isPlaying) Icons.Default.Pause else Icons.Default.SkipPrevious, // placeholder
                            contentDescription = null,
                            tint = Color.Black,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                    IconButton(onClick = {}) {
                        Icon(Icons.Default.SkipNext, null, tint = Color.White, modifier = Modifier.size(36.dp))
                    }
                }
            }
        }
    }
}
