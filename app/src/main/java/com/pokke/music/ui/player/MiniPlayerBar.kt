package com.pokke.music.ui.player

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.pokke.music.ui.backdrop.drawBackdrop
import com.pokke.music.ui.backdrop.effects.blur
import com.pokke.music.player.PlayerController
import com.pokke.music.ui.liquid.LocalAppBackdrop

/**
 * Floating pill mini-player (Apple Music style).
 * glass=true → liquid glass look; glass=false → Material solid.
 * No SharedTransition API (crashes on device / fails compile without OptIn).
 */
@Composable
fun MiniPlayerBar(
    visible: Boolean,
    glass: Boolean,
    onOpenPlayer: () -> Unit,
    compact: Boolean = false,
    modifier: Modifier = Modifier
) {
    val backdrop = LocalAppBackdrop.current
    val playerDock = LocalPlayerDock.current
    val surfaceColor = MaterialTheme.colorScheme.surface
    val song by PlayerController.currentSong.collectAsState()
    val playing by PlayerController.isPlaying.collectAsState()
    val position by PlayerController.positionMs.collectAsState()
    val duration by PlayerController.durationMs.collectAsState()
    val progress = if (duration > 0) (position.toFloat() / duration).coerceIn(0f, 1f) else 0f
    val coverSize by animateFloatAsState(
        if (compact) 36f else 44f,
        spring(stiffness = Spring.StiffnessMediumLow),
        label = "cover"
    )

    AnimatedVisibility(
        visible = visible && song != null,
        enter = slideInVertically(tween(180)) { it / 3 } + fadeIn(tween(140)),
        exit = slideOutVertically(tween(130)) { it / 3 } + fadeOut(tween(110)),
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
                    if (glass && backdrop != null) {
                        Modifier
                            .drawBackdrop(
                                backdrop = backdrop,
                                shape = { RoundedCornerShape(28.dp) },
                                effects = { blur(16f.dp.toPx()) },
                                onDrawSurface = { drawRect(surfaceColor.copy(alpha = 0.76f)) }
                            )
                            .border(0.7.dp, Color.White.copy(0.24f), RoundedCornerShape(28.dp))
                    } else {
                        Modifier.background(MaterialTheme.colorScheme.surface)
                    }
                )
                .clickable(onClick = onOpenPlayer)
                .padding(horizontal = 10.dp, vertical = if (compact) 8.dp else 10.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                AsyncImage(
                    model = s.displayCoverUrl ?: s.coverUrl,
                    contentDescription = null,
                    modifier = Modifier
                        .size(coverSize.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFF2A2A30))
                        .onGloballyPositioned { coordinates ->
                            playerDock?.reportMiniArt(coordinates, 10.dp)
                        },
                    contentScale = ContentScale.Crop
                )
                Spacer(Modifier.width(10.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        s.title,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = if (compact) 13.sp else 14.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (!compact) {
                        Text(
                            s.artist,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.62f),
                            fontSize = 12.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Spacer(Modifier.height(4.dp))
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .height(2.dp)
                            .clip(RoundedCornerShape(1.dp))
                            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.18f))
                    ) {
                        Box(
                            Modifier
                                .fillMaxWidth(progress)
                                .fillMaxHeight()
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(
                                            MaterialTheme.colorScheme.primary,
                                            MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                                        )
                                    )
                                )
                        )
                    }
                }
                IconButton(onClick = { PlayerController.togglePlayPause() }, modifier = Modifier.size(40.dp)) {
                    Icon(
                        if (playing) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(22.dp)
                    )
                }
                if (!compact) {
                    IconButton(onClick = { PlayerController.playNext() }, modifier = Modifier.size(40.dp)) {
                        Icon(
                            Icons.Default.SkipNext,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }
        }
    }
}
