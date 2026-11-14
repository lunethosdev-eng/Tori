package com.pokke.music.ui.home

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
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
import com.pokke.music.data.plugins.PluginRepository
import com.pokke.music.player.PlayerController
import androidx.compose.ui.platform.LocalContext
import java.util.Calendar
import kotlinx.coroutines.delay
import com.pokke.music.ui.liquid.LocalAppBackdrop
import com.pokke.music.ui.liquid.pokkePressMotion

/**
 * Rediseño del Home de Pokke.
 * - Usa el mismo sistema Liquid Glass (LocalAppBackdrop + drawBackdrop + blur/lens/vibrancy)
 *   que ya emplea BitChord / el paquete liquid existente.
 * - Secciones modulables controladas por UserPreferences (persistentes).
 * - Composición más limpia, jerarquía clara y densidad configurable.
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
    val showNowPlaying by prefs.homeShowNowPlaying.collectAsState(true)
    val showQuickAccess by prefs.homeShowQuickAccess.collectAsState(true)
    val showRecent by prefs.homeShowRecent.collectAsState(true)
    val showRecommendations by prefs.homeShowRecommendations.collectAsState(true)
    val showLibraryShortcuts by prefs.homeShowLibraryShortcuts.collectAsState(true)
    val sectionOrder by prefs.homeSectionOrder.collectAsState("now_playing,quick_access,recent,recommendations,library")
    val layout by prefs.homeLayout.collectAsState("comfortable")
    val density by prefs.homeDensity.collectAsState("medium")

    val backdrop = LocalAppBackdrop.current

    val coverSize: Dp = when (density) {
        "small" -> 100.dp
        "large" -> 148.dp
        else -> 128.dp
    }
    val rowGap: Dp = when (layout) {
        "compact" -> 8.dp
        "dense" -> 6.dp
        else -> 12.dp
    }
    val sectionTop: Dp = when (layout) {
        "compact" -> 12.dp
        "dense" -> 10.dp
        else -> 18.dp
    }

    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    val greetingBase = when {
        hour < 12 -> "Buenos días"
        hour < 19 -> "Buenas tardes"
        else -> "Buenas noches"
    }

    var headerTitle by remember { mutableStateOf("") }
    var showGreeting by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        multi.getRecentAll().onSuccess { recent = it.take(40) }
        authRepository.getProfile().onSuccess {
            displayName = it?.displayName?.ifBlank { null } ?: it?.username ?: "tú"
        }
        headerTitle = "$greetingBase, $displayName"
        showGreeting = true
        delay(5500)
        showGreeting = false
        headerTitle = "Inicio"
        loading = false
    }
    LaunchedEffect(displayName) {
        if (showGreeting) headerTitle = "$greetingBase, $displayName"
    }

    LaunchedEffect(listState.firstVisibleItemIndex, listState.firstVisibleItemScrollOffset) {
        onScrollStateChanged(listState.firstVisibleItemIndex > 0 || listState.firstVisibleItemScrollOffset > 40)
    }

    val current by PlayerController.currentSong.collectAsState()
    val customBg by prefs.homeCustomBgUrl.collectAsState("")
    val pluginRepository = remember(ctx) { PluginRepository(ctx) }
    val pluginBackground = remember(ctx) { pluginRepository.activeThemeBackground() }
    val pluginBackgroundColor = remember(ctx) {
        pluginRepository.activeThemeBackgroundColor()?.let { runCatching { Color(android.graphics.Color.parseColor(it)) }.getOrNull() }
    }
    val pluginHomeCards = remember(ctx) { pluginRepository.activeHomeCards() }

    val orderedSections = remember(sectionOrder) {
        sectionOrder.split(",").map { it.trim() }.filter { it.isNotEmpty() }
    }

    Box(Modifier.fillMaxSize().animateContentSize().background(pluginBackgroundColor ?: MaterialTheme.colorScheme.background)) {
        if (pluginBackground != null) {
            AsyncImage(
                model = pluginBackground,
                contentDescription = "Fondo del plugin activo",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            Box(Modifier.fillMaxSize().background(Color(0x9909090D)))
        }
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize().statusBarsPadding(),
            contentPadding = PaddingValues(bottom = 140.dp)
        ) {
            // Header con Liquid Glass surface
            item {
                val headerModifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                        .clip(RoundedCornerShape(22.dp))
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = if (glass) 0.72f else 1f))
                        .then(
                            if (glass) Modifier.border(
                                width = 0.8.dp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.14f),
                                shape = RoundedCornerShape(22.dp)
                            ) else Modifier
                        )

                Row(
                    headerModifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(Modifier.weight(1f)) {
                        AnimatedContent(
                            targetState = headerTitle,
                            transitionSpec = {
                                (fadeIn(tween(180)) + slideInVertically(tween(210)) { -it / 4 }) togetherWith
                                    (fadeOut(tween(120)) + slideOutVertically(tween(160)) { it / 4 })
                            },
                            label = "headerTitle"
                        ) { title ->
                            Text(
                                title,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.Bold,
                                fontSize = 24.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                    IconButton(onClick = onOpenSettings) {
                        Icon(Icons.Default.Settings, null, tint = MaterialTheme.colorScheme.onSurface.copy(.9f))
                    }
                }
            }

            // Widgets declarativos instalados por plugins con permiso ui.home_widgets.
            pluginHomeCards.forEachIndexed { index, card ->
                val title = card.optString("title").take(60)
                val subtitle = card.optString("subtitle").take(120)
                val action = card.optString("action")
                item(key = "community-plugin-card-$index") {
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 5.dp)
                            .clip(RoundedCornerShape(18.dp))
                            .background(MaterialTheme.colorScheme.surface.copy(alpha = if (glass) .78f else 1f))
                            .clickable {
                                when (action) {
                                    "open:home" -> Unit
                                    "open:library" -> onOpenLibrary()
                                    "open:search" -> onOpenSearch()
                                    "open:profile" -> onOpenProfile()
                                    "open:settings" -> onOpenSettings()
                                    "open:player" -> onOpenPlayer()
                                    "open:queue", "open:lyrics" -> onOpenPlayer()
                                }
                            }
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        val iconAsset = card.optString("iconAsset")
                        val iconFile = iconAsset.takeIf { it.startsWith("assets/") }?.let { java.io.File(card.optString("_pluginDir"), it) }
                        if (iconFile?.isFile == true) AsyncImage(model = iconFile, contentDescription = null, modifier = Modifier.size(36.dp))
                        Column(Modifier.weight(1f)) {
                            Text(title, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                            if (subtitle.isNotBlank()) Text(subtitle, color = MaterialTheme.colorScheme.onSurface.copy(.65f), fontSize = 12.sp)
                        }
                    }
                }
            }

            // Secciones según orden y visibilidad
            orderedSections.forEach { sectionId ->
                when (sectionId) {
                    "now_playing" -> if (false && showNowPlaying) { // disabled: global MiniPlayerBar is enough
                        current?.let { song ->
                            item(key = "now_playing") {
                                NowPlayingCard(
                                    song = song,
                                    glass = glass,
                                    backdrop = backdrop,
                                    onOpenPlayer = onOpenPlayer
                                )
                            }
                        }
                    }
                    "quick_access" -> if (showQuickAccess) {
                        item(key = "quick_access") {
                            QuickAccessRow(
                                glass = glass,
                                backdrop = backdrop,
                                sectionTop = sectionTop,
                                onOpenPlayer = onOpenPlayer,
                                onOpenProfile = onOpenProfile,
                                recentSongs = recent,
                                onOpenLibrary = onOpenLibrary,
                                onOpenSearch = onOpenSearch
                            )
                        }
                    }
                    "recent" -> if (showRecent && recent.isNotEmpty()) {
                        item(key = "recent_header") {
                            SectionTitle("Recientes", sectionTop)
                        }
                        item(key = "recent_row") {
                            LazyRow(
                                contentPadding = PaddingValues(horizontal = 16.dp),
                                horizontalArrangement = Arrangement.spacedBy(rowGap)
                            ) {
                                items(recent.take(14), key = { it.id }) { song ->
                                    SongCoverCard(
                                        song = song,
                                        coverSize = coverSize,
                                        onClick = { onSongClick(song) }
                                    )
                                }
                            }
                        }
                    }
                    "recommendations" -> if (showRecommendations) {
                        item(key = "reco_header") {
                            SectionTitle("Para ti", sectionTop)
                        }
                        items(
                            items = recent.drop(8).ifEmpty { recent }.take(12),
                            key = { "ft-${it.id}" }
                        ) { song ->
                            SongListRow(song = song, onClick = { onSongClick(song) })
                        }
                    }
                    "library" -> if (showLibraryShortcuts) {
                        item(key = "library_header") {
                            SectionTitle("Tu biblioteca", sectionTop)
                        }
                        item(key = "library_shortcuts") {
                            LibraryShortcutsRow(glass = glass, backdrop = backdrop, onOpenLibrary = onOpenLibrary)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionTitle(title: String, top: Dp) {
    Text(
        title,
        color = MaterialTheme.colorScheme.onSurface,
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp,
        modifier = Modifier.padding(start = 16.dp, top = top, bottom = 12.dp)
    )
}

@Composable
private fun NowPlayingCard(
    song: Song,
    glass: Boolean,
    backdrop: Any?,
    onOpenPlayer: () -> Unit
) {
    val shape = RoundedCornerShape(18.dp)
    val base = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 8.dp)
        .clip(shape)

    val surfaceMod = base
        .background(
            Brush.horizontalGradient(
                listOf(
                    if (glass) Color(0xCC2A2038) else Color(0xFF2A2038),
                    if (glass) Color(0xCC1A2430) else Color(0xFF1A2430)
                )
            )
        )
        .then(
            if (glass) Modifier.border(0.7.dp, Color.White.copy(alpha = 0.12f), shape)
            else Modifier
        )

    Row(
        surfaceMod
            .clickable(onClick = onOpenPlayer)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            song.displayCoverUrl ?: song.coverUrl, null,
            Modifier.size(56.dp).clip(RoundedCornerShape(12.dp)).background(Color(0xFF2A2A30)),
            contentScale = ContentScale.Crop
        )
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text("Reproduciendo", color = MaterialTheme.colorScheme.onSurface.copy(.5f), fontSize = 11.sp)
            Text(song.title, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(song.artist, color = MaterialTheme.colorScheme.onSurface.copy(.6f), fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        Box(
            Modifier.size(40.dp).clip(CircleShape).background(Color.White)
                .clickable { PlayerController.togglePlayPause() },
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.PlayArrow, null, tint = Color.Black)
        }
    }
}

@Composable
private fun QuickAccessRow(
    glass: Boolean,
    backdrop: Any?,
    sectionTop: Dp,
    onOpenPlayer: () -> Unit,
    onOpenProfile: () -> Unit,
    recentSongs: List<Song>,
    onOpenLibrary: () -> Unit,
    onOpenSearch: () -> Unit
) {
    Column(Modifier.padding(top = sectionTop)) {
        SectionTitle("Accesos rápidos", 0.dp)
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                QuickChip(
                    icon = Icons.Default.Shuffle,
                    label = "Aleatorio",
                    glass = glass,
                    onClick = { if (recentSongs.isNotEmpty()) PlayerController.playQueue(recentSongs.shuffled()) }
                )
            }
            item {
                QuickChip(
                    icon = Icons.Default.Favorite,
                    label = "Favoritos",
                    glass = glass,
                    onClick = onOpenLibrary
                )
            }
            item {
                QuickChip(
                    icon = Icons.Default.LibraryMusic,
                    label = "Biblioteca",
                    glass = glass,
                    onClick = onOpenLibrary
                )
            }
            item {
                QuickChip(
                    icon = Icons.Default.Search,
                    label = "Buscar",
                    glass = glass,
                    onClick = onOpenSearch
                )
            }
        }
    }
}

@Composable
private fun QuickChip(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    glass: Boolean,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(16.dp)
    Row(
        Modifier
            .clip(shape)
            .background(MaterialTheme.colorScheme.surface.copy(alpha = if (glass) 0.75f else 1f))
            .border(0.6.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f), shape)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(8.dp))
        Text(label, color = MaterialTheme.colorScheme.onSurface, fontSize = 13.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun SongCoverCard(song: Song, coverSize: Dp, onClick: () -> Unit) {
    val interaction = remember { MutableInteractionSource() }
    Column(Modifier.width(coverSize).pokkePressMotion(interaction).clickable(interactionSource = interaction, indication = null, onClick = onClick)) {
        AsyncImage(
            song.displayCoverUrl ?: song.coverUrl, null,
            Modifier.size(coverSize).clip(RoundedCornerShape(14.dp)).background(MaterialTheme.colorScheme.surface),
            contentScale = ContentScale.Crop
        )
        Spacer(Modifier.height(8.dp))
        Text(song.title, color = MaterialTheme.colorScheme.onSurface, fontSize = 13.sp, fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis)
        Text(song.artist, color = MaterialTheme.colorScheme.onSurface.copy(.5f), fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
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
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            song.displayCoverUrl ?: song.coverUrl, null,
            Modifier.size(52.dp).clip(RoundedCornerShape(10.dp)).background(MaterialTheme.colorScheme.surface),
            contentScale = ContentScale.Crop
        )
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(song.title, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(song.artist, color = MaterialTheme.colorScheme.onSurface.copy(.5f), fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
private fun LibraryShortcutsRow(glass: Boolean, backdrop: Any?, onOpenLibrary: () -> Unit) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            QuickChip(Icons.Default.Favorite, "Me gusta", glass, onOpenLibrary)
        }
        item {
            QuickChip(Icons.Default.LibraryMusic, "Playlists", glass, onOpenLibrary)
        }
        item {
            QuickChip(Icons.Default.PlayArrow, "Recientes", glass, onOpenLibrary)
        }
    }
}
