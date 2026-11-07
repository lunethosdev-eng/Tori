package com.pokke.music.ui.player

import androidx.compose.animation.*
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.pokke.music.player.PlayerController

/**
 * Floating pill mini-player (Apple Music style).
 * glass=true → liquid glass look; glass=false → Material Expressive solid.
 * compact=true shrinks on scroll.
 */
@Composable
fun MiniPlayerBar(
    visible: Boolean,
    glass: Boolean,
    onOpenPlayer: () -> Unit,
    compact: Boolean = false,
    modifier: Modifier = Modifier
) {
    val song by PlayerController.currentSong.collectAsState()
    val playing by PlayerController.isPlaying.collectAsState()
    val position by PlayerController.positionMs.collectAsState()
    val duration by PlayerController.durationMs.collectAsState()
    val progress = if (duration > 0) (position.toFloat() / duration).coerceIn(0f, 1f) else 0f
    val coverSize by animateFloatAsState(if (compact) 36f else 44f, spring(stiffness = Spring.StiffnessMediumLow), label = "cover")

    AnimatedVisibility(
        visible = visible && song != null,
        enter = slideInVertically(spring()) { it / 2 } + fadeIn(),
        exit = slideOutVertically(spring()) { it / 2 } + fadeOut(),
        modifier = modifier
    ) {
        val s = song ?: return@AnimatedVisibility
        Box(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = if (compact) 28.dp else 16.dp)
                .shadow(if (glass) 18.dp else 8.dp, RoundedCornerShape(28.dp), clip = false)
                .clip(RoundedCornerShape(28.dp))
                .then(
                    if (glass) Modifier
                        .background(Color(0xB31C1C1E))
                        .border(0.6.dp, Color.White.copy(0.18f), RoundedCornerShape(28.dp))
                    else Modifier.background(Color(0xF21A1A1F))
                )
                .clickable(onClick = onOpenPlayer)
                .padding(horizontal = 10.dp, vertical = if (compact) 8.dp else 10.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                AsyncImage(
                    s.displayCoverUrl ?: s.coverUrl, null,
                    Modifier
                        .size(coverSize.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFF2A2A30)),
                    contentScale = ContentScale.Crop
                )
                Spacer(Modifier.width(10.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        s.title,
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = if (compact) 13.sp else 14.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (!compact) {
                        Text(
                            s.artist,
                            color = Color.White.copy(.55f),
                            fontSize = 12.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    // thin progress under text
                    Spacer(Modifier.height(4.dp))
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .height(2.dp)
                            .clip(RoundedCornerShape(1.dp))
                            .background(Color.White.copy(.15f))
                    ) {
                        Box(
                            Modifier
                                .fillMaxWidth(progress)
                                .fillMaxHeight()
                                .background(
                                    if (glass) Brush.horizontalGradient(listOf(Color.White.copy(.9f), Color.White.copy(.5f)))
                                    else Brush.horizontalGradient(listOf(Color.White, Color.White.copy(.7f)))
                                )
                        )
                    }
                }
                IconButton(onClick = { PlayerController.togglePlayPause() }, Modifier.size(40.dp)) {
                    Icon(
                        if (playing) Icons.Default.Pause else Icons.Default.PlayArrow,
                        null, tint = Color.White, modifier = Modifier.size(22.dp)
                    )
                }
                if (!compact) {
                    IconButton(onClick = { PlayerController.playNext() }, Modifier.size(40.dp)) {
                        Icon(Icons.Default.SkipNext, null, tint = Color.White, modifier = Modifier.size(22.dp))
                    }
                }
            }
        }
    }
}
