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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Piano
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.LocationCity
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
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

private data class TvCategory(val label: String, val query: String, val icon: ImageVector)

private val categories = listOf(
    TvCategory("Rock", "rock", Icons.Default.MusicNote),
    TvCategory("Pop", "pop", Icons.Default.Headphones),
    TvCategory("Urbano", "urbano reggaeton", Icons.Default.LocationCity),
    TvCategory("Lo-Fi", "lofi", Icons.Default.Piano),
    TvCategory("Electronica", "electronic", Icons.Default.Bolt),
)

/**
 * Android TV Home — D-pad friendly.
 * Hero plays ArtistCanvas (local assets) or animatedCover / cover from Ukiyo.
 * Categories use Material icons (no emoji). Sections are focusable rails.
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
    var selectedCategory by remember { mutableStateOf(categories.first().label) }
    val current by PlayerController.currentSong.collectAsState()
    val heroFocus = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        repo.searchAll("bad bunny").onSuccess {
            if (it.isNotEmpty()) {
                hero = it.first()
                rails = it.drop(1).take(20)
            }
        }
        repo.getRecentAll().onSuccess { recent = it.take(16) }
        runCatching { heroFocus.requestFocus() }
    }

    LaunchedEffect(selectedCategory) {
        val q = categories.firstOrNull { it.label == selectedCategory }?.query ?: return@LaunchedEffect
        repo.searchAll(q).onSuccess {
            if (it.isNotEmpty()) {
                hero = it.first()
                rails = it.take(24)
            }
        }
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
        // —— Hero with canvas video ——
        Box(
            Modifier
                .fillMaxWidth()
                .height(340.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(Color(0xFF12121A))
                .focusRequester(heroFocus)
                .tvFocusable(onClick = { hero?.let(onPlay) }, shape = RoundedCornerShape(20.dp), scaleFocused = 1.02f)
        ) {
            val h = hero
            if (h != null) {
                HeroCanvasBackdrop(song = h)
            }
            Box(
                Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(Color.Transparent, Color(0xCC0B0B10), Color(0xF00B0B10))
                        )
                    )
            )
            Column(
                Modifier
                    .align(Alignment.BottomStart)
                    .padding(28.dp)
            ) {
                Text("DESTACADO", color = Color.White.copy(0.55f), fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(6.dp))
                Text(
                    hero?.title ?: "Pokke TV",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 32.sp,
                    maxLines = 2
                )
                Text(
                    hero?.artist ?: "Elige una categoria o reproduce",
                    color = Color(0xFFC8C8D0),
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
                    ) {
                        Icon(Icons.Default.PlayArrow, null, Modifier.size(22.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Reproducir", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
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

        // Now playing
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
                    .tvFocusable(onClick = onOpenPlayer, shape = RoundedCornerShape(16.dp), scaleFocused = 1.03f)
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
            }
        }

        // Categories (SVG/vector icons, no emoji)
        Spacer(Modifier.height(28.dp))
        Text("Categorias", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 20.sp)
        Spacer(Modifier.height(12.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            items(categories, key = { it.label }) { cat ->
                val selected = selectedCategory == cat.label
                Row(
                    Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .background(if (selected) Color.White else cardBg)
                        .border(1.dp, if (selected) Color.White else cardBorder, RoundedCornerShape(14.dp))
                        .tvFocusable(
                            onClick = { selectedCategory = cat.label },
                            shape = RoundedCornerShape(14.dp),
                            scaleFocused = 1.08f
                        )
                        .padding(horizontal = 18.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        cat.icon, null,
                        tint = if (selected) Color.Black else Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(Modifier.width(10.dp))
                    Text(
                        cat.label,
                        color = if (selected) Color.Black else Color.White,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp
                    )
                }
            }
        }

        // Rails for category / search
        Spacer(Modifier.height(28.dp))
        Text(selectedCategory, color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 20.sp)
        Spacer(Modifier.height(12.dp))
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(18.dp),
            contentPadding = PaddingValues(bottom = 8.dp)
        ) {
            items(rails, key = { it.id }) { song ->
                TvSongCard(song, cardBg, cardBorder, onPlay)
            }
        }

        // Recent
        if (recent.isNotEmpty()) {
            Spacer(Modifier.height(24.dp))
            Text("Recientes", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 20.sp)
            Spacer(Modifier.height(12.dp))
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(18.dp),
                contentPadding = PaddingValues(bottom = 120.dp)
            ) {
                items(recent, key = { "r_" + it.id }) { song ->
                    TvSongCard(song, cardBg, cardBorder, onPlay)
                }
            }
        } else {
            Spacer(Modifier.height(120.dp))
        }
    }
}

@OptIn(UnstableApi::class)
@Composable
private fun HeroCanvasBackdrop(song: Song) {
    val context = LocalContext.current
    val canvasUri = remember(song.id, song.artist, song.animatedCoverUrl) {
        val animated = song.animatedCoverUrl?.takeIf { it.isNotBlank() }
        when {
            animated != null && (animated.endsWith(".mp4", true) || animated.endsWith(".webm", true) || animated.contains("/video")) ->
                Uri.parse(animated)
            else -> ArtistCanvas.uriFor(context, song.artist, song.id)
        }
    }
    if (canvasUri != null) {
        val exo = remember(canvasUri) {
            ExoPlayer.Builder(context).build().apply {
                setMediaItem(MediaItem.fromUri(canvasUri))
                repeatMode = Player.REPEAT_MODE_ONE
                volume = 0f
                prepare()
                playWhenReady = true
            }
        }
        DisposableEffect(exo) { onDispose { exo.release() } }
        AndroidView(
            factory = { ctx ->
                PlayerView(ctx).apply {
                    useController = false
                    resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                    player = exo
                    layoutParams = FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                }
            },
            modifier = Modifier.fillMaxSize()
        )
    } else {
        AsyncImage(
            song.displayCoverUrl ?: song.coverUrl, null,
            Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
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
        Spacer(Modifier.height(8.dp))
        Text(song.title, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, maxLines = 2)
        Text(song.artist, color = Color(0xFF9A9AA3), fontSize = 12.sp, maxLines = 1)
    }
}
