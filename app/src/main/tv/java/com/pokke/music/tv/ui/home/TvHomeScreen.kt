package com.pokke.music.tv.ui.home

import android.net.Uri
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.annotation.OptIn
import androidx.compose.foundation.background
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
import com.pokke.music.data.model.Song
import com.pokke.music.data.repository.MultiMusicRepository
import com.pokke.music.tv.ui.components.tvFocusable

private val categories = listOf("🎸 Rock", "🎧 Pop", "🏙️ Urbano", "🎹 Lo-Fi", "⚡ Electrónica")

/** Default loop canvas until per-artist videos are provided. */
private const val DEFAULT_HERO_VIDEO =
    "https://storage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4"

@OptIn(UnstableApi::class)
@Composable
fun TvHomeScreen(
    onPlay: (Song) -> Unit,
    onOpenPlayer: () -> Unit
) {
    val context = LocalContext.current
    val repo = remember { MultiMusicRepository() }
    var hero by remember { mutableStateOf<Song?>(null) }
    var rails by remember { mutableStateOf<List<Song>>(emptyList()) }
    var videoOk by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        repo.searchAll("top hits").onSuccess {
            hero = it.firstOrNull()
            rails = it.drop(1).take(24)
        }
    }

    // Hero video player (muted loop)
    val exo = remember {
        ExoPlayer.Builder(context).build().apply {
            repeatMode = Player.REPEAT_MODE_ONE
            volume = 0f
            playWhenReady = true
        }
    }
    DisposableEffect(Unit) {
        onDispose { exo.release() }
    }
    LaunchedEffect(hero?.id, videoOk) {
        val url = DEFAULT_HERO_VIDEO
        runCatching {
            exo.setMediaItem(MediaItem.fromUri(Uri.parse(url)))
            exo.prepare()
            exo.play()
            videoOk = true
        }.onFailure { videoOk = false }
    }

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 36.dp)
    ) {
        Box(
            Modifier
                .fillMaxWidth()
                .height(320.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(Color(0xFF101018))
        ) {
            // Video layer
            if (videoOk) {
                AndroidView(
                    factory = { ctx ->
                        PlayerView(ctx).apply {
                            player = exo
                            useController = false
                            resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                            layoutParams = FrameLayout.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.MATCH_PARENT
                            )
                        }
                    },
                    modifier = Modifier.fillMaxSize(),
                    update = { it.player = exo }
                )
            } else {
                // Fallback cover
                hero?.coverUrl?.let {
                    AsyncImage(it, null, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                }
            }
            // Scrim for readability
            Box(
                Modifier.fillMaxSize().background(
                    Brush.horizontalGradient(listOf(Color.Black.copy(0.82f), Color.Black.copy(0.25f), Color.Transparent))
                )
            )
            Column(Modifier.align(Alignment.CenterStart).padding(32.dp)) {
                Text("Canción Top de Pokke", color = Color(0xFFB8B8C0), fontSize = 13.sp)
                Spacer(Modifier.height(6.dp))
                Text(
                    hero?.title ?: "Explora música",
                    color = Color.White,
                    fontSize = 34.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    listOfNotNull(hero?.artist, hero?.album).joinToString(" • ").ifBlank { "Pokke TV" },
                    color = Color(0xFFC8C8D0),
                    fontSize = 16.sp
                )
                Spacer(Modifier.height(18.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button(
                        onClick = { hero?.let(onPlay) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.Black),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.tvFocusable(onClick = { hero?.let(onPlay) }, shape = RoundedCornerShape(10.dp))
                    ) { Text("▶  Reproducir", fontWeight = FontWeight.Bold) }
                    OutlinedButton(
                        onClick = { },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.tvFocusable(onClick = { }, shape = RoundedCornerShape(10.dp))
                    ) { Text("+ Guardar en Biblioteca", color = Color.White) }
                }
            }
        }

        Spacer(Modifier.height(28.dp))
        Text("Explorar categorías", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 18.sp)
        Spacer(Modifier.height(12.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            items(categories) { cat ->
                Box(
                    Modifier
                        .tvFocusable(onClick = { }, shape = RoundedCornerShape(14.dp))
                        .background(Color(0xFF1A1A22), RoundedCornerShape(14.dp))
                        .padding(horizontal = 22.dp, vertical = 14.dp)
                ) { Text(cat, color = Color.White, fontSize = 15.sp) }
            }
        }

        Spacer(Modifier.height(28.dp))
        Text("Para ti", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 18.sp)
        Spacer(Modifier.height(12.dp))
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {
            items(rails, key = { it.id }) { song ->
                Column(
                    Modifier
                        .width(148.dp)
                        .tvFocusable(onClick = { onPlay(song) }, shape = RoundedCornerShape(12.dp), scaleFocused = 1.1f)
                        .padding(4.dp)
                ) {
                    AsyncImage(
                        song.coverUrl, null,
                        Modifier.size(140.dp).clip(RoundedCornerShape(10.dp)).background(Color(0xFF222)),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(song.title, color = Color.White, maxLines = 1, fontSize = 13.sp)
                    Text(song.artist, color = Color(0xFF9A9AA3), maxLines = 1, fontSize = 12.sp)
                }
            }
        }
    }
}
