package com.pokke.music.tv.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.pokke.music.data.model.Song
import com.pokke.music.data.repository.MultiMusicRepository
import com.pokke.music.data.repository.LocalLibraryStore
import kotlinx.coroutines.launch
import android.widget.Toast
import com.pokke.music.tv.ui.components.tvFocusable

@Composable
fun TvHomeScreen(onPlay: (Song) -> Unit, onOpenPlayer: () -> Unit) {
    val context = LocalContext.current
    val repo = remember { MultiMusicRepository() }
    var hero by remember { mutableStateOf<Song?>(null) }
    var rails by remember { mutableStateOf<List<Song>>(emptyList()) }
    LaunchedEffect(Unit) {
        // Prefer artists that have local canvas
        repo.searchAll("bad bunny").onSuccess {
            if (it.isNotEmpty()) {
                hero = it.first()
                rails = it.drop(1).take(20)
                return@LaunchedEffect
            }
        }
        repo.searchAll("depresion sonora").onSuccess {
            if (it.isNotEmpty()) {
                hero = it.first()
                rails = it.take(20)
                return@LaunchedEffect
            }
        }
        repo.searchAll("top hits").onSuccess {
            hero = it.firstOrNull()
            rails = it.drop(1).take(20)
        }
    }

    val scope = rememberCoroutineScope()
    val library = remember(context) { LocalLibraryStore(context) }

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 40.dp, vertical = 8.dp)) {
        Text("POKKE TV", color = Color(0xFF7A7A88), fontSize = 12.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Box(Modifier.fillMaxWidth().height(340.dp).clip(RoundedCornerShape(24.dp)).background(Color(0xFF101018))) {
            hero?.displayCoverUrl?.let { AsyncImage(it, null, Modifier.fillMaxSize(), contentScale = ContentScale.Crop) }
            Box(Modifier.fillMaxSize().background(Brush.horizontalGradient(listOf(Color.Black.copy(0.85f), Color.Black.copy(0.2f), Color.Transparent))))
            Column(Modifier.align(Alignment.CenterStart).padding(36.dp)) {
                Text("DESTACADO", color = Color(0xFF3797F0), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(6.dp))
                Text(hero?.title ?: "Explora música", color = Color.White, fontSize = 36.sp, fontWeight = FontWeight.Bold)
                Text(listOfNotNull(hero?.artist, hero?.album).joinToString(" • "), color = Color(0xFFC8C8D0), fontSize = 18.sp)
                Spacer(Modifier.height(20.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    Button(
                        onClick = { hero?.let(onPlay) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.Black),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.height(48.dp).tvFocusable(onClick = { hero?.let(onPlay) }, shape = RoundedCornerShape(12.dp))
                    ) { Text("▶  Reproducir", fontWeight = FontWeight.Bold, fontSize = 16.sp) }
                    OutlinedButton(
                        onClick = {
                            hero?.let { selected ->
                                scope.launch {
                                    runCatching { library.download(selected) }
                                        .onSuccess { Toast.makeText(context, "Añadida a descargas", Toast.LENGTH_SHORT).show() }
                                        .onFailure { Toast.makeText(context, it.localizedMessage ?: "No se pudo añadir", Toast.LENGTH_LONG).show() }
                                }
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.height(48.dp).tvFocusable(
                            onClick = {
                                hero?.let { selected ->
                                    scope.launch {
                                        runCatching { library.download(selected) }
                                            .onSuccess { Toast.makeText(context, "Añadida a descargas", Toast.LENGTH_SHORT).show() }
                                            .onFailure { Toast.makeText(context, it.localizedMessage ?: "No se pudo añadir", Toast.LENGTH_LONG).show() }
                                    }
                                }
                            }, shape = RoundedCornerShape(12.dp)
                        )
                    ) { Text("↓ Descargar", color = Color.White, fontSize = 16.sp) }
                }
            }
        }
        Spacer(Modifier.height(32.dp))
        Text("Para ti", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 20.sp)
        Spacer(Modifier.height(14.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(18.dp), contentPadding = PaddingValues(bottom = 120.dp)) {
            items(rails, key = { it.id }) { song ->
                Column(
                    Modifier.width(160.dp).tvFocusable(onClick = { onPlay(song) }, shape = RoundedCornerShape(14.dp), scaleFocused = 1.1f).padding(6.dp)
                ) {
                    AsyncImage(song.coverUrl, null, Modifier.size(148.dp).clip(RoundedCornerShape(12.dp)).background(Color(0xFF222)), contentScale = ContentScale.Crop)
                    Spacer(Modifier.height(10.dp))
                    Text(song.title, color = Color.White, maxLines = 1, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                    Text(song.artist, color = Color(0xFF9A9AA3), maxLines = 1, fontSize = 13.sp)
                }
            }
        }
    }
}
