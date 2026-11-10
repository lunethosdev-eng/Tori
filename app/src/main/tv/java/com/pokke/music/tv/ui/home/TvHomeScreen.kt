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
import com.pokke.music.data.canvas.ArtistCanvas
import com.pokke.music.data.model.Song
import com.pokke.music.data.repository.MultiMusicRepository
import com.pokke.music.tv.ui.components.tvFocusable

private val categories = listOf("🎸 Rock", "🎧 Pop", "🏙️ Urbano", "🎹 Lo-Fi", "⚡ Electrónica")
private const val FALLBACK_VIDEO = "https://storage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4"

@OptIn(UnstableApi::class)
@Composable
fun TvHomeScreen(onPlay: (Song) -> Unit, onOpenPlayer: () -> Unit) {
    val context = LocalContext.current
    val repo = remember { MultiMusicRepository() }
    var hero by remember { mutableStateOf<Song?>(null) }
    var rails by remember { mutableStateOf<List<Song>>(emptyList()) }
    var videoOk by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        // Prefer artists that have local canvas
        repo.searchAll("bad bunny").onSuccess {
            if (it.isNotEmpty()) {
                hero = it.first()
                rails = it.drop(1).take(20)
                return@LaunchedEffect
            }
        }
        repo.searchAll("depresion sonora").onSuccess {
            if (it.isNotEmpty()) {
                hero = it.first()
                rails = it.take(20)
                return@LaunchedEffect
            }
        }
        repo.searchAll("top hits").onSuccess {
            hero = it.firstOrNull()
            rails = it.drop(1).take(20)
        }
    }

    val exo = remember {
        ExoPlayer.Builder(context).build().apply {
            repeatMode = Player.REPEAT_MODE_ONE
            volume = 0f
            playWhenReady = true
        }
    }
    DisposableEffect(Unit) { onDispose { exo.release() } }

    LaunchedEffect(hero?.id, hero?.artist) {
        val local = ArtistCanvas.uriFor(context, hero?.artist, hero?.id)
        val uri = local ?: Uri.parse(FALLBACK_VIDEO)
        runCatching {
            exo.setMediaItem(MediaItem.fromUri(uri))
            exo.prepare()
            exo.play()
            videoOk = true
        }.onFailure { videoOk = false }
    }

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 40.dp, vertical = 8.dp)) {
        Text("POKKE TV", color = Color(0xFF7A7A88), fontSize = 12.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Box(Modifier.fillMaxWidth().height(340.dp).clip(RoundedCornerShape(24.dp)).background(Color(0xFF101018))) {
            if (videoOk) {
                AndroidView(
                    factory = { ctx ->
                        PlayerView(ctx).apply {
                            player = exo
                            useController = false
                            resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                            layoutParams = FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
                        }
                    },
                    modifier = Modifier.fillMaxSize(),
                    update = { it.player = exo }
                )
            } else {
                hero?.coverUrl?.let { AsyncImage(it, null, Modifier.fillMaxSize(), contentScale = ContentScale.Crop) }
            }
            Box(Modifier.fillMaxSize().background(Brush.horizontalGradient(listOf(Color.Black.copy(0.85f), Color.Black.copy(0.2f), Color.Transparent))))
            Column(Modifier.align(Alignment.CenterStart).padding(36.dp)) {
                Text("DESTACADO", color = Color(0xFF3797F0), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(6.dp))
                Text(hero?.title ?: "Explora música", color = Color.White, fontSize = 36.sp, fontWeight = FontWeight.Bold)
                Text(listOfNotNull(hero?.artist, hero?.album).joinToString(" • "), color = Color(0xFFC8C8D0), fontSize = 18.sp)
                Spacer(Modifier.height(20.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    Button(
                        onClick = { hero?.let(onPlay) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.Black),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.height(48.dp).tvFocusable(onClick = { hero?.let(onPlay) }, shape = RoundedCornerShape(12.dp))
                    ) { Text("▶  Reproducir", fontWeight = FontWeight.Bold, fontSize = 16.sp) }
                    OutlinedButton(
                        onClick = {},
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.height(48.dp).tvFocusable(onClick = {}, shape = RoundedCornerShape(12.dp))
                    ) { Text("+ Biblioteca", color = Color.White, fontSize = 16.sp) }
                }
            }
        }
        Spacer(Modifier.height(32.dp))
        Text("Categorías", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 20.sp)
        Spacer(Modifier.height(14.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            items(categories) { cat ->
                Box(
                    Modifier.tvFocusable(onClick = {}, shape = RoundedCornerShape(16.dp))
                        .background(Color(0xFF1A1A24), RoundedCornerShape(16.dp))
                        .padding(horizontal = 28.dp, vertical = 18.dp)
                ) { Text(cat, color = Color.White, fontSize = 17.sp) }
            }
        }
        Spacer(Modifier.height(32.dp))
        Text("Para ti", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 20.sp)
        Spacer(Modifier.height(14.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(18.dp), contentPadding = PaddingValues(bottom = 120.dp)) {
            items(rails, key = { it.id }) { song ->
                Column(
                    Modifier.width(160.dp).tvFocusable(onClick = { onPlay(song) }, shape = RoundedCornerShape(14.dp), scaleFocused = 1.1f).padding(6.dp)
                ) {
                    AsyncImage(song.coverUrl, null, Modifier.size(148.dp).clip(RoundedCornerShape(12.dp)).background(Color(0xFF222)), contentScale = ContentScale.Crop)
                    Spacer(Modifier.height(10.dp))
                    Text(song.title, color = Color.White, maxLines = 1, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                    Text(song.artist, color = Color(0xFF9A9AA3), maxLines = 1, fontSize = 13.sp)
                }
            }
        }
    }
}
