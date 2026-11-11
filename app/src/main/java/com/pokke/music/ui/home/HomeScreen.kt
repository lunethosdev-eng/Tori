package com.pokke.music.ui.home

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.pokke.music.data.model.Song
import com.pokke.music.data.preferences.UserPreferences
import com.pokke.music.data.repository.AuthRepository
import com.pokke.music.data.repository.MultiMusicRepository
import com.pokke.music.player.PlayerController
import androidx.compose.ui.platform.LocalContext
import java.util.Calendar
import kotlinx.coroutines.delay
import com.pokke.music.ui.liquid.LocalAppBackdrop
import com.pokke.music.ui.backdrop.drawBackdrop
import com.pokke.music.ui.backdrop.effects.blur
import com.pokke.music.ui.backdrop.effects.lens
import com.pokke.music.ui.backdrop.effects.vibrancy

@Composable
fun HomeScreen(
    onSongClick: (Song) -> Unit,
    onOpenPlayer: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenProfile: () -> Unit,
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
    val backdrop = LocalAppBackdrop.current
    val headerSurfaceColor = MaterialTheme.colorScheme.surface
    val headerOutlineColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.22f)

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

    // compact nav signal on scroll
    LaunchedEffect(listState.firstVisibleItemIndex, listState.firstVisibleItemScrollOffset) {
        onScrollStateChanged(listState.firstVisibleItemIndex > 0 || listState.firstVisibleItemScrollOffset > 40)
    }

    val current by PlayerController.currentSong.collectAsState()

    Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize().statusBarsPadding(),
            contentPadding = PaddingValues(bottom = 140.dp)
        ) {
            // Liquid / Material header
            item {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                        .then(
                            Modifier
                                .clip(RoundedCornerShape(22.dp))
                                .background(MaterialTheme.colorScheme.surface.copy(alpha = if (glass) 0.88f else 1f))
                        )
                        .padding(horizontal = 12.dp, vertical = 8.dp),
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

            // Now playing compact card
            current?.let { song ->
                item {
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                            .clip(RoundedCornerShape(18.dp))
                            .background(Brush.horizontalGradient(listOf(Color(0xFF2A2038), Color(0xFF1A2430))))
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
            }

            item {
                Text("Recientes", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold, fontSize = 20.sp,
                    modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 12.dp))
            }
            item {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(recent.take(14), key = { it.id }) { song ->
                        Column(Modifier.width(128.dp).clickable { onSongClick(song) }) {
                            AsyncImage(
                                song.displayCoverUrl ?: song.coverUrl, null,
                                Modifier.size(128.dp).clip(RoundedCornerShape(14.dp)).background(MaterialTheme.colorScheme.surface),
                                contentScale = ContentScale.Crop
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(song.title, color = MaterialTheme.colorScheme.onSurface, fontSize = 13.sp, fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Text(song.artist, color = MaterialTheme.colorScheme.onSurface.copy(.5f), fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                    }
                }
            }

            item {
                Text("Para ti", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold, fontSize = 20.sp,
                    modifier = Modifier.padding(start = 16.dp, top = 22.dp, bottom = 8.dp))
            }
            items(recent.drop(8).ifEmpty { recent }, key = { "ft-${it.id}" }) { song ->
                Row(
                    Modifier.fillMaxWidth().clickable { onSongClick(song) }
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
        }
    }
}
