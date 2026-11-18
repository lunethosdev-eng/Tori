package com.pokke.music.tv.ui.search

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import com.pokke.music.data.model.Song
import com.pokke.music.data.preferences.UserPreferences
import com.pokke.music.data.repository.MultiMusicRepository
import com.pokke.music.remote.TvRemoteBus
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun TvSearchScreen(onPlay: (Song) -> Unit) {
    val repo = remember { MultiMusicRepository() }
    val context = LocalContext.current
    val prefs = remember { UserPreferences(context) }
    val ytCatalogEnabled by prefs.youtubeCatalogEnabled.collectAsState(initial = false)
    val searchHistory by prefs.searchHistory.collectAsState(initial = emptyList())
    val scope = rememberCoroutineScope()
    var query by remember { mutableStateOf("") }
    var results by remember { mutableStateOf<List<Song>>(emptyList()) }
    var loading by remember { mutableStateOf(false) }
    var focusedSearch by remember { mutableStateOf(false) }
    val categories = listOf("Rock", "Pop", "Urbano", "Lo-Fi", "Electrónica")

    LaunchedEffect(Unit) {
        TvRemoteBus.searchText.collect { value -> if (value != null) query = value }
    }

    LaunchedEffect(query, ytCatalogEnabled) {
        if (query.trim().length < 2) {
            results = emptyList()
            loading = false
            return@LaunchedEffect
        }
        delay(350)
        loading = true
        repo.searchAll(query.trim(), includeYoutube = ytCatalogEnabled)
            .onSuccess { results = it.take(40) }
            .onFailure { results = emptyList() }
        loading = false
    }

    Column(Modifier.fillMaxSize().background(Color(0xFF0C0C0E)).padding(horizontal = 36.dp, vertical = 24.dp)) {
        Text("Buscar", color = Color.White, fontSize = 30.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(16.dp))
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            placeholder = { Text("Canción, artista o álbum…", color = Color(0xFF7A7A85)) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().height(60.dp)
                .onFocusChanged { focusedSearch = it.isFocused }
                .border(if (focusedSearch) 2.dp else 1.dp, if (focusedSearch) Color(0xFFFC3C44) else Color(0xFF333340), RoundedCornerShape(16.dp)),
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                cursorColor = Color(0xFFFC3C44),
                focusedBorderColor = Color.Transparent,
                unfocusedBorderColor = Color.Transparent
            )
        )
        Spacer(Modifier.height(16.dp))
        if (query.isBlank()) {
            Text("Explorar por categorías", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 19.sp)
            Spacer(Modifier.height(12.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                items(categories.size) { index ->
                    val category = categories[index]
                    var focused by remember { mutableStateOf(false) }
                    Box(
                        Modifier.width(170.dp).height(88.dp)
                            .onFocusChanged { focused = it.isFocused }
                            .border(if (focused) 2.dp else 1.dp, if (focused) Color(0xFFFC3C44) else Color.White.copy(.08f), RoundedCornerShape(14.dp))
                            .background(if (focused) Color(0xFF26191C) else Color(0xFF17171B), RoundedCornerShape(14.dp))
                            .clickable { query = category }
                            .padding(16.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Text(category, color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.Medium)
                    }
                }
            }
            if (searchHistory.isNotEmpty()) {
                Spacer(Modifier.height(24.dp))
                Text("BÚSQUEDAS RECIENTES", color = Color.White.copy(.55f), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    items(searchHistory.take(8)) { item ->
                        Text(item, color = Color.White.copy(.8f), fontSize = 15.sp,
                            modifier = Modifier.fillMaxWidth().clickable { query = item }.padding(vertical = 9.dp, horizontal = 4.dp))
                    }
                }
            }
        } else {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("CATÁLOGO TORI", color = Color.White.copy(.55f), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.width(14.dp))
                Text(if (ytCatalogEnabled) "YouTube Music activo" else "YouTube Music desactivado en Ajustes", color = Color.White.copy(.4f), fontSize = 11.sp)
            }
            Spacer(Modifier.height(10.dp))
            when {
                loading -> Text("Buscando…", color = Color.White.copy(.5f), fontSize = 15.sp)
                results.isEmpty() -> Text("No se encontraron canciones para esta búsqueda.", color = Color.White.copy(.5f), fontSize = 15.sp)
                else -> LazyColumn(contentPadding = PaddingValues(bottom = 32.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(results, key = { it.id }) { song ->
                        var focused by remember { mutableStateOf(false) }
                        Row(
                            Modifier.fillMaxWidth()
                                .onFocusChanged { focused = it.isFocused }
                                .border(if (focused) 2.dp else 1.dp, if (focused) Color(0xFFFC3C44) else Color.White.copy(.07f), RoundedCornerShape(12.dp))
                                .background(if (focused) Color(0xFF26191C) else Color(0xFF17171B), RoundedCornerShape(12.dp))
                                .clickable {
                                    scope.launch { prefs.addSearchHistory(query.trim()) }
                                    onPlay(song)
                                }
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            AsyncImage(song.displayCoverUrl, null, Modifier.size(58.dp), contentScale = ContentScale.Crop)
                            Column(Modifier.weight(1f).padding(start = 14.dp)) {
                                Text(song.title, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Text(song.artist, color = Color.White.copy(.55f), fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            }
                            Text("▶", color = if (focused) Color(0xFFFC3C44) else Color.White.copy(.65f), fontSize = 18.sp)
                        }
                    }
                }
            }
        }
    }
}
