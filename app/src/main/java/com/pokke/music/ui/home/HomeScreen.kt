package com.pokke.music.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
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
import com.pokke.music.data.repository.AuthRepository
import com.pokke.music.data.repository.MultiMusicRepository
import com.pokke.music.player.PlayerController
import java.util.Calendar

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
    var displayName by remember { mutableStateOf<String?>(null) }
    var avatar by remember { mutableStateOf<String?>(null) }

    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    val greeting = when {
        hour < 12 -> "Buenos días"
        hour < 19 -> "Buenas tardes"
        else -> "Buenas noches"
    }

    LaunchedEffect(Unit) {
        loading = true
        multi.getRecentAll().onSuccess { recent = it.take(30) }
        authRepository.getProfile().onSuccess {
            displayName = it?.displayName ?: it?.username
            avatar = it?.avatarUrl
        }
        loading = false
    }

    val current by PlayerController.currentSong.collectAsState()

    Box(Modifier.fillMaxSize().background(Color(0xFF08080A))) {
        LazyColumn(
            Modifier.fillMaxSize().statusBarsPadding(),
            contentPadding = PaddingValues(bottom = 120.dp)
        ) {
            item {
                Row(
                    Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(greeting, color = Color.White.copy(.55f), fontSize = 13.sp)
                        Text(
                            displayName?.let { "Hola, $it" } ?: "Tu espacio musical",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 24.sp
                        )
                    }
                    IconButton(onClick = onOpenSettings) {
                        Icon(Icons.Default.Settings, null, tint = Color.White.copy(.8f))
                    }
                    Box(
                        Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF1C1C22))
                            .clickable(onClick = onOpenProfile),
                        contentAlignment = Alignment.Center
                    ) {
                        if (!avatar.isNullOrBlank()) {
                            AsyncImage(avatar, null, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                        } else {
                            Text((displayName ?: "P").take(1).uppercase(), color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Hero / now playing
            current?.let { song ->
                item {
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 8.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(
                                Brush.horizontalGradient(listOf(Color(0xFF2A1F3D), Color(0xFF1A2332)))
                            )
                            .clickable(onClick = onOpenPlayer)
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AsyncImage(
                            song.coverUrl, null,
                            Modifier.size(64.dp).clip(RoundedCornerShape(12.dp)).background(Color(0xFF2A2A30)),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(Modifier.width(14.dp))
                        Column(Modifier.weight(1f)) {
                            Text("Reproduciendo", color = Color.White.copy(.55f), fontSize = 11.sp)
                            Text(song.title, color = Color.White, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Text(song.artist, color = Color.White.copy(.65f), fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                        Box(
                            Modifier.size(44.dp).clip(CircleShape).background(Color.White).clickable {
                                PlayerController.togglePlayPause()
                            },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.PlayArrow, null, tint = Color.Black)
                        }
                    }
                }
            }

            item {
                Text(
                    "Recientes",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    modifier = Modifier.padding(start = 20.dp, top = 18.dp, bottom = 10.dp)
                )
            }

            item {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(recent.take(12), key = { it.id }) { song ->
                        Column(
                            Modifier
                                .width(132.dp)
                                .clickable { onSongClick(song) }
                        ) {
                            AsyncImage(
                                song.coverUrl, null,
                                Modifier
                                    .size(132.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(Color(0xFF1A1A20)),
                                contentScale = ContentScale.Crop
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(song.title, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Text(song.artist, color = Color.White.copy(.5f), fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                    }
                }
            }

            item {
                Text(
                    "Para ti",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    modifier = Modifier.padding(start = 20.dp, top = 22.dp, bottom = 10.dp)
                )
            }

            items(recent.drop(12).ifEmpty { recent }, key = { "list-${it.id}" }) { song ->
                Row(
                    Modifier
                        .fillMaxWidth()
                        .clickable { onSongClick(song) }
                        .padding(horizontal = 20.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AsyncImage(
                        song.coverUrl, null,
                        Modifier.size(52.dp).clip(RoundedCornerShape(10.dp)).background(Color(0xFF1A1A20)),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(song.title, color = Color.White, fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text(song.artist, color = Color.White.copy(.5f), fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }
            }

            if (loading && recent.isEmpty()) {
                item {
                    Text("Cargando catálogo…", color = Color.White.copy(.4f), modifier = Modifier.padding(24.dp))
                }
            }
        }
    }
}
