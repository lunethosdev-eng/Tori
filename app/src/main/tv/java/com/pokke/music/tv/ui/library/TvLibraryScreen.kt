package com.pokke.music.tv.ui.library

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.pokke.music.data.model.Song
import com.pokke.music.data.repository.LocalLibraryStore
import com.pokke.music.tv.ui.components.tvFocusable
import androidx.compose.ui.platform.LocalContext

@Composable
fun TvLibraryScreen(onPlay: (Song) -> Unit) {
    val context = LocalContext.current
    val store = remember(context) { LocalLibraryStore(context) }
    val recent by store.history.collectAsState(initial = emptyList())
    val downloads by store.downloads.collectAsState(initial = emptyList())

    Column(Modifier.fillMaxSize().padding(horizontal = 40.dp, vertical = 20.dp)) {
        Text("Tu biblioteca", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 30.sp)
        Spacer(Modifier.height(26.dp))
        Text("Descargas", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 20.sp)
        Spacer(Modifier.height(12.dp))
        if (downloads.isEmpty()) {
            Text("Las canciones que descargues aparecerán aquí.", color = Color(0xFF9A9AA3), fontSize = 15.sp)
        } else {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(18.dp)) {
                items(downloads, key = { it.id }) { song -> SongCard(song, onPlay) }
            }
        }
        Spacer(Modifier.height(30.dp))
        Text("Escuchado recientemente", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 20.sp)
        Spacer(Modifier.height(12.dp))
        if (recent.isEmpty()) {
            Text("Cuando reproduzcas música, tu historial aparecerá aquí.", color = Color(0xFF9A9AA3), fontSize = 15.sp)
        } else {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(18.dp)) {
                items(recent, key = { it.id }) { song -> SongCard(song, onPlay) }
            }
        }
    }
}

@Composable
private fun SongCard(song: Song, onPlay: (Song) -> Unit) {
    Column(
        Modifier.width(158.dp)
            .tvFocusable(onClick = { onPlay(song) }, shape = RoundedCornerShape(14.dp), scaleFocused = 1.06f)
            .padding(5.dp)
    ) {
        AsyncImage(
            song.displayCoverUrl,
            contentDescription = song.title,
            modifier = Modifier.size(148.dp).clip(RoundedCornerShape(12.dp)).background(Color(0xFF22222A)),
            contentScale = ContentScale.Crop
        )
        Spacer(Modifier.height(9.dp))
        Text(song.title, color = Color.White, maxLines = 1, fontSize = 14.sp, fontWeight = FontWeight.Medium)
        Text(song.artist, color = Color(0xFF9A9AA3), maxLines = 1, fontSize = 12.sp)
    }
}
