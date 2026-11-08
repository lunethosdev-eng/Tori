package com.pokke.music.tv.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.pokke.music.data.model.Song
import com.pokke.music.data.repository.MultiMusicRepository

private val categories = listOf("🎸 Rock", "🎧 Pop", "🏙️ Urbano", "🎹 Lo-Fi", "⚡ Electrónica")

@Composable
fun TvHomeScreen(
    onPlay: (Song) -> Unit,
    onOpenPlayer: () -> Unit
) {
    val repo = remember { MultiMusicRepository() }
    var hero by remember { mutableStateOf<Song?>(null) }
    var rails by remember { mutableStateOf<List<Song>>(emptyList()) }

    LaunchedEffect(Unit) {
        repo.searchAll("top hits").onSuccess {
            hero = it.firstOrNull()
            rails = it.drop(1).take(20)
        }
    }

    Column(Modifier.fillMaxSize().padding(horizontal = 36.dp)) {
        // Hero canvas card
        Box(
            Modifier
                .fillMaxWidth()
                .height(280.dp)
                .background(
                    Brush.horizontalGradient(listOf(Color(0xFF1A1030), Color(0xFF0E1A2E), Color(0xFF101018))),
                    RoundedCornerShape(20.dp)
                )
                .border(1.dp, Color.White.copy(0.08f), RoundedCornerShape(20.dp))
        ) {
            hero?.coverUrl?.let { url ->
                AsyncImage(url, null, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                Box(Modifier.fillMaxSize().background(Brush.horizontalGradient(listOf(Color.Black.copy(0.75f), Color.Black.copy(0.25f)))))
            }
            Column(Modifier.align(Alignment.CenterStart).padding(32.dp)) {
                Text("Canción Top de Pokke", color = Color(0xFFB8B8C0), fontSize = 13.sp)
                Spacer(Modifier.height(6.dp))
                Text(hero?.title ?: "Explora música", color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.Bold)
                Text(listOfNotNull(hero?.artist, hero?.album).joinToString(" • ").ifBlank { "Pokke TV" }, color = Color(0xFFC8C8D0), fontSize = 16.sp)
                Spacer(Modifier.height(18.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button(
                        onClick = { hero?.let(onPlay) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.Black),
                        shape = RoundedCornerShape(10.dp)
                    ) { Text("▶  Reproducir", fontWeight = FontWeight.Bold) }
                    OutlinedButton(
                        onClick = { /* save later */ },
                        shape = RoundedCornerShape(10.dp)
                    ) { Text("+ Guardar en Biblioteca", color = Color.White) }
                }
            }
        }

        Spacer(Modifier.height(28.dp))
        Text("Explorar categorías", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 18.sp)
        Spacer(Modifier.height(12.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            items(categories) { cat ->
                var focused by remember { mutableStateOf(false) }
                Box(
                    Modifier
                        .onFocusChanged { focused = it.isFocused }
                        .background(if (focused) Color.White.copy(0.18f) else Color(0xFF1A1A22), RoundedCornerShape(14.dp))
                        .border(if (focused) 2.dp else 0.dp, Color.White, RoundedCornerShape(14.dp))
                        .padding(horizontal = 22.dp, vertical = 14.dp)
                ) {
                    Text(cat, color = Color.White, fontSize = 15.sp)
                }
            }
        }

        Spacer(Modifier.height(28.dp))
        Text("Para ti", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 18.sp)
        Spacer(Modifier.height(12.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            items(rails, key = { it.id }) { song ->
                var focused by remember { mutableStateOf(false) }
                Column(
                    Modifier
                        .width(140.dp)
                        .onFocusChanged { focused = it.isFocused }
                        .border(if (focused) 2.dp else 0.dp, Color.White, RoundedCornerShape(12.dp))
                        .padding(4.dp)
                ) {
                    AsyncImage(
                        song.coverUrl, null,
                        Modifier.size(132.dp).background(Color(0xFF222), RoundedCornerShape(10.dp)),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(song.title, color = Color.White, maxLines = 1, fontSize = 13.sp)
                    Text(song.artist, color = Color(0xFF9A9AA3), maxLines = 1, fontSize = 12.sp)
                }
            }
        }
    }
}
