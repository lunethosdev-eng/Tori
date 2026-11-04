package com.pokke.music.ui.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.pokke.music.data.model.Song
import com.pokke.music.data.repository.AuthRepository
import com.pokke.music.data.repository.MusicRepository

@Composable
fun HomeScreen(
    onSongClick: (Song) -> Unit,
    onOpenPlayer: () -> Unit,
    onOpenSettings: () -> Unit = {},
    onOpenProfile: () -> Unit = {},
    onScrollStateChanged: (Boolean) -> Unit = {},
    authRepository: AuthRepository? = null
) {
    val listState = rememberLazyListState()
    val repo = remember { MusicRepository() }
    var songs by remember { mutableStateOf<List<Song>>(emptyList()) }
    var avatarUrl by remember { mutableStateOf<String?>(null) }
    val isScrolled by remember { derivedStateOf { listState.firstVisibleItemIndex > 0 || listState.firstVisibleItemScrollOffset > 48 } }
    LaunchedEffect(isScrolled) { onScrollStateChanged(isScrolled) }
    LaunchedEffect(Unit) { repo.getRecentSongs(40).onSuccess { songs = it } }
    LaunchedEffect(authRepository) { authRepository?.getProfile()?.onSuccess { avatarUrl = it?.avatarUrl } }

    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize().background(Color(0xFF080808)),
        contentPadding = PaddingValues(top = 0.dp, bottom = 116.dp)
    ) {
        item {
            Row(
                Modifier.fillMaxWidth().statusBarsPadding().padding(start = 22.dp, end = 18.dp, top = 12.dp, bottom = 26.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f)) {
                    Text("POKKE", color = Color.White.copy(.55f), fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 3.sp)
                    Text("Tu espacio musical", color = Color.White, fontSize = 27.sp, fontWeight = FontWeight.Bold)
                }
                IconButton(onClick = onOpenSettings, modifier = Modifier.size(46.dp).clip(CircleShape).background(Color(0xFF1B1B1D))) {
                    Icon(Icons.Default.Settings, null, tint = Color.White)
                }
                Spacer(Modifier.width(10.dp))
                Box(Modifier.size(46.dp).clip(CircleShape).background(Color(0xFF29292C)).clickable { onOpenProfile() }, contentAlignment = Alignment.Center) {
                    if (!avatarUrl.isNullOrBlank()) AsyncImage(model = avatarUrl, contentDescription = "Foto de perfil", modifier = Modifier.fillMaxSize().clip(CircleShape))
                    else Text("P", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 19.sp)
                }
            }
        }
        item {
            Column(Modifier.fillMaxWidth().padding(horizontal = 22.dp).padding(bottom = 22.dp)) {
                Text("Buenas tardes", color = Color.White, fontSize = 30.sp, fontWeight = FontWeight.Bold)
                Text("Vuelve a lo que te gusta escuchar.", color = Color.White.copy(.55f), fontSize = 14.sp, modifier = Modifier.padding(top = 5.dp))
            }
        }
        if (songs.isNotEmpty()) {
            item {
                val featured = songs.first()
                Box(Modifier.fillMaxWidth().padding(horizontal = 20.dp).padding(bottom = 25.dp).height(176.dp).clip(RoundedCornerShape(24.dp)).clickable { onSongClick(featured) }) {
                    if (!featured.displayCoverUrl.isNullOrBlank()) AsyncImage(featured.displayCoverUrl, null, modifier = Modifier.fillMaxSize(), contentScale = androidx.compose.ui.layout.ContentScale.Crop)
                    Box(Modifier.fillMaxSize().background(Brush.horizontalGradient(listOf(Color(0xE6000000), Color(0x33000000)))))
                    Column(Modifier.align(Alignment.BottomStart).padding(20.dp)) {
                        Text("PARA TI", color = Color.White.copy(.7f), fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
                        Text(featured.title, color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text(featured.artist, color = Color.White.copy(.8f), fontSize = 14.sp)
                    }
                }
            }
            item { Text("Escuchado recientemente", color = Color.White, fontSize = 19.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 22.dp, vertical = 8.dp)) }
        }
        items(songs.drop(if (songs.isNotEmpty()) 1 else 0), key = { it.id }) { song ->
            Row(Modifier.fillMaxWidth().clickable { onSongClick(song) }.padding(horizontal = 22.dp, vertical = 9.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(58.dp).clip(RoundedCornerShape(13.dp)).background(Color(0xFF202024))) {
                    if (!song.displayCoverUrl.isNullOrBlank()) AsyncImage(song.displayCoverUrl, null, modifier = Modifier.fillMaxSize(), contentScale = androidx.compose.ui.layout.ContentScale.Crop)
                }
                Spacer(Modifier.width(15.dp))
                Column(Modifier.weight(1f)) {
                    Text(song.title, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(song.artist, color = Color.White.copy(.55f), fontSize = 14.sp, modifier = Modifier.padding(top = 4.dp), maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
        }
    }
}
