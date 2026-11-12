package com.pokke.music.tv.ui.search

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import com.pokke.music.tv.ui.components.tvFocusable

@Composable
fun TvSearchScreen(onPlay: (Song) -> Unit) {
    val repo = remember { MultiMusicRepository() }
    var query by remember { mutableStateOf("") }
    var results by remember { mutableStateOf<List<Song>>(emptyList()) }
    val cats = listOf("🎸 Rock", "🎧 Pop", "🏙️ Urbano", "🎹 Lo-Fi")

    LaunchedEffect(query) {
        if (query.length < 2) { results = emptyList(); return@LaunchedEffect }
        repo.searchAll(query).onSuccess { results = it.take(30) }
    }

    Column(Modifier.fillMaxSize().padding(horizontal = 36.dp)) {
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            placeholder = { Text("¿Qué quieres escuchar hoy en Pokke?", color = Color(0xFF7A7A85)) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = Color.White,
                unfocusedBorderColor = Color(0xFF333340)
            )
        )
        Spacer(Modifier.height(22.dp))
        if (query.isBlank()) {
            Text("Explorar por categorías", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 18.sp)
            Spacer(Modifier.height(12.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                items(cats.size) { i ->
                    var focused by remember { mutableStateOf(false) }
                    Box(
                        Modifier
                            .width(160.dp)
                            .height(100.dp)
                            .tvFocusable(onClick = { query = cats[i].substringAfter(" ").trim() }, shape = RoundedCornerShape(16.dp))
                            .onFocusChanged { focused = it.isFocused }
                            .background(if (focused) Color(0xFF2A2A38) else Color(0xFF16161E), RoundedCornerShape(16.dp))
                            .border(if (focused) 2.dp else 0.dp, Color.White, RoundedCornerShape(16.dp))
                            .padding(16.dp)
                    ) {
                        Text(cats[i], color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Medium)
                    }
                }
            }
        } else {
            LazyVerticalGrid(columns = GridCells.Fixed(5), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(results, key = { it.id }) { song ->
                    var focused by remember { mutableStateOf(false) }
                    Column(
                        Modifier
                            .tvFocusable(onClick = { onPlay(song) }, shape = RoundedCornerShape(10.dp), scaleFocused = 1.06f)
                            .onFocusChanged { focused = it.isFocused }
                            .border(if (focused) 2.dp else 0.dp, Color.White, RoundedCornerShape(10.dp))
                            .padding(6.dp)
                    ) {
                        AsyncImage(song.displayCoverUrl, null, Modifier.aspectRatio(1f), contentScale = ContentScale.Crop)
                        Text(song.title, color = Color.White, maxLines = 1, fontSize = 13.sp)
                        Text(song.artist, color = Color(0xFF9A9AA3), maxLines = 1, fontSize = 11.sp)
                    }
                }
            }
        }
    }
}
