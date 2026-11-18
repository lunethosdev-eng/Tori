package com.pokke.music.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.pokke.music.data.model.Song
import com.pokke.music.data.preferences.UserPreferences
import com.pokke.music.data.repository.AuthRepository
import com.pokke.music.data.repository.MultiMusicRepository
import com.pokke.music.player.PlayerController
import com.pokke.music.ui.liquid.MoshiiGlass
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import java.util.Calendar

/**
 * Home estilo Moshii / Apple Music:
 * saludo, atajos, rieles horizontales de recientes y recomendaciones.
 * Sin tarjetas enormes de "Vuelve a escuchar". Player BitChord intacto.
 */
@Composable
fun HomeScreen(
    onSongClick: (Song) -> Unit,
    onOpenPlayer: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenProfile: () -> Unit,
    onOpenLibrary: () -> Unit = {},
    onOpenSearch: () -> Unit = {},
    onScrollStateChanged: (Boolean) -> Unit = {},
    authRepository: AuthRepository
) {
    val multi = remember { MultiMusicRepository() }
    var recent by remember { mutableStateOf<List<Song>>(emptyList()) }
    var recommended by remember { mutableStateOf<List<Song>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var displayName by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val ctx = LocalContext.current
    val prefs = remember { UserPreferences(ctx) }
    val glass by prefs.liquidGlassEnabled.collectAsState(false)
    val current by PlayerController.currentSong.collectAsState()

    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    val greeting = when {
        hour < 12 -> "Buenos días"
        hour < 19 -> "Buenas tardes"
        else -> "Buenas noches"
    }

    LaunchedEffect(Unit) {
        recent = multi.getRecentAll().getOrDefault(emptyList()).take(24)
        recommended = withContext(Dispatchers.IO) {
            multi.searchAll("top hits", limit = 20).getOrDefault(emptyList())
        }
        val profileName = authRepository.getProfile().getOrNull()?.displayName
        val nick = prefs.onboardingNickname.firstOrNull().orEmpty()
        displayName = when {
            !profileName.isNullOrBlank() -> profileName
            nick.isNotBlank() -> nick
            else -> ""
        }
        loading = false
    }

    LaunchedEffect(listState.firstVisibleItemIndex, listState.firstVisibleItemScrollOffset) {
        onScrollStateChanged(listState.firstVisibleItemIndex > 0 || listState.firstVisibleItemScrollOffset > 40)
    }

    Box(Modifier.fillMaxSize().background(Color(0xFF07070A))) {
        LazyColumn(
            state = listState,
            contentPadding = PaddingValues(bottom = 140.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            item {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(
                            if (displayName.isNotBlank()) "$greeting, $displayName" else greeting,
                            color = Color.White,
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "Tu música, a tu manera",
                            color = Color.White.copy(0.45f),
                            fontSize = 13.sp
                        )
                    }
                    IconButton(onClick = onOpenSearch) {
                        Icon(Icons.Default.Search, null, tint = Color.White)
                    }
                    IconButton(onClick = onOpenSettings) {
                        Icon(Icons.Default.Settings, null, tint = Color.White.copy(0.85f))
                    }
                }
            }

            // Quick shortcuts (Moshii-style chips)
            item {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    QuickChip("Biblioteca", Icons.Default.LibraryMusic, Modifier.weight(1f), glass, onOpenLibrary)
                    QuickChip("Buscar", Icons.Default.Search, Modifier.weight(1f), glass, onOpenSearch)
                }
            }

            // Now playing compact (optional strip, not huge card)
            current?.let { song ->
                item {
                    MoshiiGlass(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                            .clickable(onClick = onOpenPlayer),
                        enabled = glass,
                        shape = RoundedCornerShape(16.dp),
                        opacity = 0.12f
                    ) {
                        Row(
                            Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            AsyncImage(
                                model = song.displayCoverUrl ?: song.coverUrl,
                                contentDescription = null,
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0xFF22222A)),
                                contentScale = ContentScale.Crop
                            )
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) {
                                Text("En reproducción", color = Color.White.copy(0.45f), fontSize = 11.sp)
                                Text(
                                    song.title,
                                    color = Color.White,
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    song.artist,
                                    color = Color.White.copy(0.55f),
                                    fontSize = 12.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }

            if (recent.isNotEmpty()) {
                item { SectionTitle("Recientes") }
                item {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 14.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(recent, key = { "r_${it.id}" }) { song ->
                            SongCard(song) {
                                val idx = recent.indexOfFirst { it.id == song.id }.coerceAtLeast(0)
                                PlayerController.playQueue(recent, idx)
                                onSongClick(song)
                            }
                        }
                    }
                }
            }

            if (recommended.isNotEmpty()) {
                item { SectionTitle("Para ti") }
                item {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 14.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(recommended, key = { "p_${it.id}" }) { song ->
                            SongCard(song) {
                                val idx = recommended.indexOfFirst { it.id == song.id }.coerceAtLeast(0)
                                PlayerController.playQueue(recommended, idx)
                                onSongClick(song)
                            }
                        }
                    }
                }
            }

            if (!loading && recent.isEmpty() && recommended.isEmpty()) {
                item {
                    Text(
                        "Explora o busca música para empezar.",
                        color = Color.White.copy(0.45f),
                        modifier = Modifier.padding(24.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(
        title,
        color = Color.White,
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp,
        modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 18.dp, bottom = 10.dp)
    )
}

@Composable
private fun QuickChip(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier,
    glass: Boolean,
    onClick: () -> Unit
) {
    MoshiiGlass(
        modifier = modifier.clickable(onClick = onClick),
        enabled = glass,
        shape = RoundedCornerShape(14.dp),
        opacity = 0.10f
    ) {
        Row(
            Modifier.padding(horizontal = 14.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, null, tint = Color.White, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(10.dp))
            Text(label, color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
        }
    }
}

@Composable
private fun SongCard(song: Song, onClick: () -> Unit) {
    Column(
        Modifier
            .width(140.dp)
            .clickable(onClick = onClick)
    ) {
        AsyncImage(
            model = song.displayCoverUrl ?: song.coverUrl,
            contentDescription = null,
            modifier = Modifier
                .size(140.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(Color(0xFF1A1A22)),
            contentScale = ContentScale.Crop
        )
        Spacer(Modifier.height(8.dp))
        Text(
            song.title,
            color = Color.White,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            song.artist,
            color = Color.White.copy(0.5f),
            fontSize = 12.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
