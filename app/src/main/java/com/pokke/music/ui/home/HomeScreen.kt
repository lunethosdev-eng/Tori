package com.pokke.music.ui.home

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.pokke.music.data.model.Song
import com.pokke.music.data.preferences.UserPreferences
import com.pokke.music.data.repository.AuthRepository
import com.pokke.music.data.repository.MultiMusicRepository
import com.pokke.music.player.PlayerController
import com.pokke.music.ui.liquid.LocalAppBackdrop
import com.pokke.music.ui.liquid.pokkePressMotion
import kotlinx.coroutines.flow.firstOrNull
import org.json.JSONArray
import java.util.Calendar

/**
 * Home rediseñado – estilo Apple Music / Spotify minimalista.
 * - Sin tarjeta grande "Vuelve a escuchar"
 * - Sin bloque "Reproduciendo" destacado
 * - Secciones limpias: saludo, accesos rápidos, recientes, para ti
 * - Liquid Glass respetado
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
    var loading by remember { mutableStateOf(true) }
    var displayName by remember { mutableStateOf("tú") }
    val listState = rememberLazyListState()
    val ctx = LocalContext.current
    val prefs = remember { UserPreferences(ctx) }

    val glass by prefs.liquidGlassEnabled.collectAsState(false)
    val showQuickAccess by prefs.homeShowQuickAccess.collectAsState(true)
    val showRecent by prefs.homeShowRecent.collectAsState(true)
    val showRecommendations by prefs.homeShowRecommendations.collectAsState(true)
    val showLibraryShortcuts by prefs.homeShowLibraryShortcuts.collectAsState(true)
    val density by prefs.homeDensity.collectAsState("medium")
    val favoriteArtistsRaw by prefs.onboardingArtists.collectAsState("[]")

    val favoriteArtists = remember(favoriteArtistsRaw) {
        runCatching {
            val array = JSONArray(favoriteArtistsRaw)
            buildList {
                for (index in 0 until array.length()) {
                    val item = array.optJSONObject(index) ?: continue
                    val id = item.optLong("id", -1L)
                    val name = item.optString("name").trim()
                    if (id > 0 && name.isNotBlank()) {
                        add(Triple(id, name, item.optString("image").takeIf { it.startsWith("http") }))
                    }
                }
            }.take(16)
        }.getOrDefault(emptyList())
    }

    val backdrop = LocalAppBackdrop.current

    val coverSize: Dp = when (density) {
        "small" -> 108.dp
        "large" -> 142.dp
        else -> 124.dp
    }

    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    val greeting = when {
        hour < 12 -> "Buenos días"
        hour < 19 -> "Buenas tardes"
        else -> "Buenas noches"
    }

    LaunchedEffect(Unit) {
        multi.getRecentAll().onSuccess { recent = it.take(40) }
        val onboardingName = prefs.onboardingNickname.firstOrNull().orEmpty()
        authRepository.currentUser()?.displayName?.let { displayName = it }
            ?: run { if (onboardingName.isNotBlank()) displayName = onboardingName }
        loading = false
    }

    LaunchedEffect(listState.firstVisibleItemIndex, listState.firstVisibleItemScrollOffset) {
        onScrollStateChanged(listState.firstVisibleItemIndex > 0 || listState.firstVisibleItemScrollOffset > 40)
    }

    val current by PlayerController.currentSong.collectAsState()

    Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize().statusBarsPadding(),
            contentPadding = PaddingValues(bottom = 130.dp)
        ) {
            // ── Header limpio ──────────────────────────────────────────────
            item {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(
                            greeting,
                            color = MaterialTheme.colorScheme.onSurface.copy(0.55f),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            displayName.replaceFirstChar { it.uppercase() },
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    IconButton(onClick = onOpenSearch) {
                        Icon(Icons.Default.Search, contentDescription = "Buscar", tint = MaterialTheme.colorScheme.onSurface)
                    }
                    IconButton(onClick = onOpenSettings) {
                        Icon(Icons.Default.Settings, contentDescription = "Ajustes", tint = MaterialTheme.colorScheme.onSurface)
                    }
                }
            }

            // ── Accesos rápidos (chips) ────────────────────────────────────
            if (showQuickAccess) {
                item {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 20.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.padding(bottom = 8.dp)
                    ) {
                        item {
                            QuickChip(Icons.Default.Shuffle, "Aleatorio", glass) {
                                // shuffle recent or library
                                recent.firstOrNull()?.let { onSongClick(it) }
                            }
                        }
                        item {
                            QuickChip(Icons.Default.Favorite, "Favoritos", glass, onOpenLibrary)
                        }
                        item {
                            QuickChip(Icons.Default.LibraryMusic, "Biblioteca", glass, onOpenLibrary)
                        }
                    }
                }
            }

            // ── Artistas favoritos (si existen) ────────────────────────────
            if (favoriteArtists.isNotEmpty()) {
                item {
                    SectionTitle("Artistas", 18.dp)
                }
                item {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 20.dp),
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        items(favoriteArtists, key = { it.first }) { (id, name, image) ->
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.width(76.dp)
                            ) {
                                AsyncImage(
                                    model = image,
                                    contentDescription = name,
                                    modifier = Modifier
                                        .size(72.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.surfaceVariant),
                                    contentScale = ContentScale.Crop
                                )
                                Spacer(Modifier.height(6.dp))
                                Text(
                                    name,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontSize = 12.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }

            // ── Recientes ──────────────────────────────────────────────────
            if (showRecent && recent.isNotEmpty()) {
                item {
                    SectionTitle("Recientes", 22.dp)
                }
                item {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 20.dp),
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        items(recent.take(12), key = { it.id }) { song ->
                            RecentCard(song = song, coverSize = coverSize, onClick = { onSongClick(song) })
                        }
                    }
                }
            }

            // ── Para ti / recomendaciones ──────────────────────────────────
            if (showRecommendations && recent.size > 4) {
                item {
                    SectionTitle("Para ti", 24.dp)
                }
                items(recent.drop(4).take(10), key = { "ft-${it.id}" }) { song ->
                    SongListRow(song = song, onClick = { onSongClick(song) })
                }
            }

            // ── Atajos de biblioteca ───────────────────────────────────────
            if (showLibraryShortcuts) {
                item {
                    SectionTitle("Tu biblioteca", 22.dp)
                }
                item {
                    LibraryShortcutsRow(glass = glass, onOpenLibrary = onOpenLibrary)
                }
            }
        }
    }
}

@Composable
private fun SectionTitle(text: String, top: Dp) {
    Text(
        text,
        color = MaterialTheme.colorScheme.onSurface,
        fontSize = 20.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(start = 20.dp, top = top, bottom = 12.dp)
    )
}

@Composable
private fun QuickChip(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    glass: Boolean,
    onClick: () -> Unit
) {
    val interaction = remember { MutableInteractionSource() }
    Row(
        Modifier
            .pokkePressMotion(interaction)
            .clip(RoundedCornerShape(50))
            .background(
                if (glass) MaterialTheme.colorScheme.surface.copy(0.55f)
                else MaterialTheme.colorScheme.surfaceVariant.copy(0.7f)
            )
            .clickable(interactionSource = interaction, indication = null, onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurface)
        Spacer(Modifier.width(7.dp))
        Text(label, color = MaterialTheme.colorScheme.onSurface, fontSize = 13.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun RecentCard(song: Song, coverSize: Dp, onClick: () -> Unit) {
    val interaction = remember { MutableInteractionSource() }
    Column(
        Modifier
            .width(coverSize)
            .pokkePressMotion(interaction)
            .clickable(interactionSource = interaction, indication = null, onClick = onClick)
    ) {
        AsyncImage(
            model = song.displayCoverUrl ?: song.coverUrl,
            contentDescription = null,
            modifier = Modifier
                .size(coverSize)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentScale = ContentScale.Crop
        )
        Spacer(Modifier.height(8.dp))
        Text(
            song.title,
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            song.artist,
            color = MaterialTheme.colorScheme.onSurface.copy(0.55f),
            fontSize = 12.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun SongListRow(song: Song, onClick: () -> Unit) {
    val interaction = remember { MutableInteractionSource() }
    Row(
        Modifier
            .fillMaxWidth()
            .pokkePressMotion(interaction)
            .clickable(interactionSource = interaction, indication = null, onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = song.displayCoverUrl ?: song.coverUrl,
            contentDescription = null,
            modifier = Modifier
                .size(52.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentScale = ContentScale.Crop
        )
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(
                song.title,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                song.artist,
                color = MaterialTheme.colorScheme.onSurface.copy(0.55f),
                fontSize = 13.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun LibraryShortcutsRow(glass: Boolean, onOpenLibrary: () -> Unit) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.padding(bottom = 16.dp)
    ) {
        item { QuickChip(Icons.Default.Favorite, "Me gusta", glass, onOpenLibrary) }
        item { QuickChip(Icons.Default.LibraryMusic, "Playlists", glass, onOpenLibrary) }
        item { QuickChip(Icons.Default.PlayArrow, "Recientes", glass, onOpenLibrary) }
    }
}
