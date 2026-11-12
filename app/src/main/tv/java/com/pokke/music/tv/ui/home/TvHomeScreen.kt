package com.pokke.music.tv.ui.home

import android.net.Uri
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.annotation.OptIn
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import coil.compose.AsyncImage
import com.pokke.music.data.canvas.ArtistCanvas
import com.pokke.music.data.model.Song
import com.pokke.music.data.preferences.UserPreferences
import com.pokke.music.data.repository.MultiMusicRepository
import com.pokke.music.player.PlayerController
import com.pokke.music.tv.ui.components.tvFocusable

private val categories = listOf("🎸 Rock", "🎧 Pop", "🏙️ Urbano", "🎹 Lo-Fi", "⚡ Electrónica")
private const val FALLBACK_VIDEO = "https://storage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4"

/**
 * Android TV Home — adapted for D-pad / remote.
 * - Larger touch targets and clear focus rings via [tvFocusable]
 * - Sections aligned with mobile Home (hero, now playing, rails, categories)
 * - Liquid Glass style surfaces (translucent cards) without heavy blur on TV
 */
@OptIn(UnstableApi::class)
@Composable
fun TvHomeScreen(onPlay: (Song) -> Unit, onOpenPlayer: () -> Unit) {
    val context = LocalContext.current
    val repo = remember { MultiMusicRepository() }
    val prefs = remember { UserPreferences(context) }
    val glass by prefs.liquidGlassEnabled.collectAsState(initial = false)
    var hero by remember { mutableStateOf<Song?>(null) }
    var rails by remember { mutableStateOf<List<Song>>(emptyList()) }
    var recent by remember { mutableStateOf<List<Song>>(emptyList()) }
    var videoOk by remember { mutableStateOf(true) }
    val current by PlayerController.currentSong.collectAsState()

    LaunchedEffect(Unit) {
        repo.searchAll("bad bunny").onSuccess {
            if (it.isNotEmpty()) {
                hero = it.first()
                rails = it.drop(1).take(20)
            }
        }
        repo.getRecentAll().onSuccess { recent = it.take(16) }
    }

    val scroll = rememberScrollState()
    val cardBg = if (glass) Color(0xCC1A1A24) else Color(0xFF1A1A24)
    val cardBorder = if (glass) Color.White.copy(alpha = 0.12f) else Color.Transparent

    Column(
        Modifier
            .fillMaxSize()
            .background(Color(0xFF0B0B10))
            .verticalScroll(scroll)
            .padding(horizontal = 48.dp, vertical = 28.dp)
    ) {
        // Hero
        Box(
            Modifier
                .fillMaxWidth()
                .height(320.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(Color(0xFF12121A))
        ) {
            // Video / image backdrop omitted for brevity — keep existing ExoPlayer logic if needed
            hero?.let { h ->
                AsyncImage(
                    h.displayCoverUrl ?: h.coverUrl, null,
                    Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
            Box(
                Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(Color.Transparent, Color(0xCC0B0B10))
                        )
                    )
            )
            Column(
                Modifier
                    .align(Alignment.BottomStart)
                    .padding(28.dp)
            ) {
                Text(
                    hero?.title ?: "Pokke TV",
                    color = Color.White,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    hero?.artist ?: "Tu música en la gran pantalla",
                    color = Color.White.copy(0.75f),
                    fontSize = 18.sp
                )
                Spacer(Modifier.height(16.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    Button(
                        onClick = { hero?.let(onPlay) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.Black),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .height(48.dp)
                            .tvFocusable(onClick = { hero?.let(onPlay) }, shape = RoundedCornerShape(12.dp))
                    ) { Text("▶  Reproducir", fontWeight = FontWeight.Bold, fontSize = 16.sp) }
                    OutlinedButton(
                        onClick = onOpenPlayer,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .height(48.dp)
                            .tvFocusable(onClick = onOpenPlayer, shape = RoundedCornerShape(12.dp))
                    ) { Text("Abrir reproductor", color = Color.White, fontSize = 16.sp) }
                }
            }
        }

        // Now playing (if any)
        current?.let { song ->
            Spacer(Modifier.height(28.dp))
            Text("Reproduciendo", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 20.sp)
            Spacer(Modifier.height(12.dp))
            Row(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(cardBg)
                    .border(1.dp, cardBorder, RoundedCornerShape(16.dp))
                    .tvFocusable(onClick = onOpenPlayer, shape = RoundedCornerShape(16.dp))
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AsyncImage(
                    song.displayCoverUrl ?: song.coverUrl, null,
                    Modifier.size(72.dp).clip(RoundedCornerShape(12.dp)).background(Color(0xFF222)),
                    contentScale = ContentScale.Crop
                )
                Spacer(Modifier.width(18.dp))
                Column(Modifier.weight(1f)) {
                    Text(song.title, color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 18.sp, maxLines = 1)
                    Text(song.artist, color = Color(0xFF9A9AA3), fontSize = 15.sp, maxLines = 1)
                }
                Text("Abrir", color = Color.White.copy(0.8f), fontSize = 15.sp)
            }
        }

        Spacer(Modifier.height(28.dp))
        Text("Categorías", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 20.sp)
        Spacer(Modifier.height(14.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            items(categories) { cat ->
                Box(
                    Modifier
                        .tvFocusable(onClick = {}, shape = RoundedCornerShape(16.dp))
                        .background(cardBg, RoundedCornerShape(16.dp))
                        .border(1.dp, cardBorder, RoundedCornerShape(16.dp))
                        .padding(horizontal = 28.dp, vertical = 18.dp)
                ) { Text(cat, color = Color.White, fontSize = 17.sp) }
            }
        }

        if (recent.isNotEmpty()) {
            Spacer(Modifier.height(28.dp))
            Text("Recientes", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 20.sp)
            Spacer(Modifier.height(14.dp))
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(18.dp),
                contentPadding = PaddingValues(bottom = 8.dp)
            ) {
                items(recent, key = { "r-${it.id}" }) { song ->
                    TvSongCard(song, cardBg, cardBorder, onPlay)
                }
            }
        }

        Spacer(Modifier.height(28.dp))
        Text("Para ti", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 20.sp)
        Spacer(Modifier.height(14.dp))
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(18.dp),
            contentPadding = PaddingValues(bottom = 120.dp)
        ) {
            items(rails, key = { it.id }) { song ->
                TvSongCard(song, cardBg, cardBorder, onPlay)
            }
        }
    }
}

@Composable
private fun TvSongCard(
    song: Song,
    cardBg: Color,
    cardBorder: Color,
    onPlay: (Song) -> Unit
) {
    Column(
        Modifier
            .width(168.dp)
            .tvFocusable(onClick = { onPlay(song) }, shape = RoundedCornerShape(14.dp), scaleFocused = 1.08f)
            .padding(6.dp)
    ) {
        AsyncImage(
            song.displayCoverUrl ?: song.coverUrl, null,
            Modifier
                .size(156.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF222))
                .border(1.dp, cardBorder, RoundedCornerShape(12.dp)),
            contentScale = ContentScale.Crop
        )
        Spacer(Modifier.height(10.dp))
        Text(song.title, color = Color.White, maxLines = 1, fontSize = 14.sp, fontWeight = FontWeight.Medium)
        Text(song.artist, color = Color(0xFF9A9AA3), maxLines = 1, fontSize = 13.sp)
    }
}
