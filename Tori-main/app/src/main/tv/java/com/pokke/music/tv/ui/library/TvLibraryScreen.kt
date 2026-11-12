package com.pokke.music.tv.ui.library

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.pokke.music.data.model.Song
import com.pokke.music.data.repository.MultiMusicRepository

@Composable
fun TvLibraryScreen(onPlay: (Song) -> Unit) {
    val repo = remember { MultiMusicRepository() }
    var recent by remember { mutableStateOf<List<Song>>(emptyList()) }
    LaunchedEffect(Unit) {
        repo.searchAll("trending").onSuccess { recent = it.take(12) }
    }

    val quick = listOf("❤️ Liked Songs", "⬇️ Downloaded", "🎶 Playlists", "⭐ Most played")

    Column(Modifier.fillMaxSize().padding(horizontal = 36.dp)) {
        Text("Accesos rápidos", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 18.sp)
        Spacer(Modifier.height(14.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            items(quick) { q ->
                var focused by remember { mutableStateOf(false) }
                Box(
                    Modifier
                        .width(180.dp)
                        .height(90.dp)
                        .onFocusChanged { focused = it.isFocused }
                        .background(if (focused) Color(0xFF2A2A38) else Color(0xFF16161E), RoundedCornerShape(14.dp))
                        .border(if (focused) 2.dp else 0.dp, Color.White, RoundedCornerShape(14.dp))
                        .padding(16.dp)
                ) {
                    Text(q, color = Color.White, fontWeight = FontWeight.Medium)
                }
            }
        }
        Spacer(Modifier.height(28.dp))
        Text("Escuchado recientemente", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 18.sp)
        Spacer(Modifier.height(12.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            items(recent, key = { it.id }) { song ->
                var focused by remember { mutableStateOf(false) }
                Column(
                    Modifier
                        .width(140.dp)
                        .onFocusChanged { focused = it.isFocused }
                        .border(if (focused) 2.dp else 0.dp, Color.White, RoundedCornerShape(12.dp))
                        .padding(4.dp)
                ) {
                    AsyncImage(song.coverUrl, null, Modifier.size(132.dp), contentScale = ContentScale.Crop)
                    Spacer(Modifier.height(8.dp))
                    Text(song.title, color = Color.White, maxLines = 1, fontSize = 13.sp)
                }
            }
        }
    }
}
