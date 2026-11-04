package com.pokke.music.ui.library

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.pokke.music.data.model.Playlist
import com.pokke.music.data.model.Song
import com.pokke.music.data.repository.AuthRepository
import kotlinx.coroutines.launch

@Composable
fun LibraryScreen(
    repository: AuthRepository,
    isVisible: Boolean = true,
    onSongClick: (Song) -> Unit = {},
    onLoginRequired: () -> Unit = {}
) {
    val session by repository.session.collectAsState()
    val scope = rememberCoroutineScope()
    var favorites by remember { mutableStateOf<List<Song>>(emptyList()) }
    var playlists by remember { mutableStateOf<List<Playlist>>(emptyList()) }
    var selectedPlaylist by remember { mutableStateOf<Playlist?>(null) }
    var playlistSongs by remember { mutableStateOf<List<Song>>(emptyList()) }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var showCreate by remember { mutableStateOf(false) }
    var newName by remember { mutableStateOf("") }
    var newDescription by remember { mutableStateOf("") }

    fun refresh() {
        if (session == null) return
        scope.launch {
            loading = true; error = null
            repository.getFavorites().onSuccess { favorites = it }.onFailure { error = it.localizedMessage }
            repository.getPlaylists().onSuccess { playlists = it }.onFailure { error = it.localizedMessage }
            loading = false
        }
    }
    LaunchedEffect(session?.userId, isVisible) {
        if (session == null) { favorites = emptyList(); playlists = emptyList() }
        else if (isVisible) { favorites = emptyList(); playlists = emptyList(); refresh() }
    }
    LaunchedEffect(selectedPlaylist?.id) {
        val selected = selectedPlaylist ?: return@LaunchedEffect
        loading = true
        repository.getPlaylistSongs(selected.id).onSuccess { playlistSongs = it }.onFailure { error = it.localizedMessage }
        loading = false
    }

    Column(Modifier.fillMaxSize().background(Color(0xFF080808)).statusBarsPadding()) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 16.dp), verticalAlignment = Alignment.CenterVertically) {
            if (selectedPlaylist != null) {
                IconButton(onClick = { selectedPlaylist = null; playlistSongs = emptyList() }) { Icon(Icons.Default.ArrowBack, null, tint = Color.White) }
            }
            Text(selectedPlaylist?.name ?: "Tu biblioteca", color = Color.White, fontSize = 26.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
            if (session != null && selectedPlaylist == null) IconButton(onClick = { showCreate = true }) { Icon(Icons.Default.Add, "Crear playlist", tint = Color.White) }
        }
        if (session == null) {
            Column(Modifier.fillMaxSize().padding(28.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                Icon(Icons.Default.Favorite, null, tint = Color.White.copy(.7f), modifier = Modifier.size(42.dp))
                Spacer(Modifier.height(16.dp))
                Text("Tu música, sincronizada", color = Color.White, fontSize = 19.sp, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(8.dp))
                Text("Inicia sesión para guardar favoritos y playlists en todos tus dispositivos.", color = Color.White.copy(.55f), fontSize = 13.sp)
                Spacer(Modifier.height(18.dp))
                Button(onClick = onLoginRequired, colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.Black)) { Text("Iniciar sesión") }
            }
        } else if (loading && favorites.isEmpty() && playlists.isEmpty() && playlistSongs.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = Color.White) }
        } else if (selectedPlaylist != null) {
            LazyColumn(contentPadding = PaddingValues(bottom = 110.dp)) {
                item { Text("${playlistSongs.size} canciones", color = Color.White.copy(.5f), modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp), fontSize = 12.sp) }
                items(playlistSongs, key = { it.id }) { song -> LibrarySongRow(song, onSongClick) }
                if (playlistSongs.isEmpty()) item { EmptyLibraryText("Esta playlist está vacía. Reproduce una canción y añádela desde el reproductor.") }
            }
        } else {
            LazyColumn(contentPadding = PaddingValues(bottom = 110.dp)) {
                item { SectionTitle("Favoritos", favorites.size) }
                if (favorites.isEmpty()) item { EmptyLibraryText("Todavía no has guardado canciones con ❤️") }
                items(favorites.take(8), key = { "fav-${it.id}" }) { song -> LibrarySongRow(song, onSongClick) }
                item {
                    Row(Modifier.fillMaxWidth().padding(start = 20.dp, end = 20.dp, top = 22.dp, bottom = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text("Playlists", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                        TextButton(onClick = { showCreate = true }) { Text("Crear", color = Color.White.copy(.8f)) }
                    }
                }
                if (playlists.isEmpty()) item { EmptyLibraryText("Crea tu primera playlist para organizar tu música.") }
                items(playlists, key = { it.id }) { playlist ->
                    Row(Modifier.fillMaxWidth().clickable { selectedPlaylist = playlist }.padding(horizontal = 20.dp, vertical = 9.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(56.dp).clip(RoundedCornerShape(10.dp)).background(Color(0xFF242424)), contentAlignment = Alignment.Center) {
                            if (!playlist.coverUrl.isNullOrBlank()) AsyncImage(ImageRequest.Builder(LocalContext.current).data(playlist.coverUrl).crossfade(true).build(), null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                            else Icon(Icons.Default.QueueMusic, null, tint = Color.White.copy(.7f), modifier = Modifier.size(26.dp))
                        }
                        Spacer(Modifier.width(13.dp))
                        Column(Modifier.weight(1f)) {
                            Text(playlist.name, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Text("${playlist.songCount} canciones", color = Color.White.copy(.5f), fontSize = 12.sp)
                        }
                        IconButton(onClick = { scope.launch { repository.deletePlaylist(playlist.id).onSuccess { refresh() }.onFailure { error = it.localizedMessage } } }) { Icon(Icons.Default.Delete, "Eliminar playlist", tint = Color.White.copy(.45f)) }
                    }
                }
                if (error != null) item { Text(error!!, color = Color(0xFFFF8A80), modifier = Modifier.padding(20.dp), fontSize = 12.sp) }
            }
        }
    }

    if (showCreate) AlertDialog(
        onDismissRequest = { showCreate = false },
        title = { Text("Nueva playlist") },
        text = {
            Column {
                OutlinedTextField(value = newName, onValueChange = { newName = it }, label = { Text("Nombre") }, singleLine = true)
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(value = newDescription, onValueChange = { newDescription = it }, label = { Text("Descripción (opcional)") })
            }
        },
        confirmButton = {
            TextButton(enabled = newName.isNotBlank(), onClick = {
                scope.launch {
                    repository.createPlaylist(newName, newDescription).onSuccess { newName = ""; newDescription = ""; showCreate = false; refresh() }.onFailure { error = it.localizedMessage }
                }
            }) { Text("Crear") }
        },
        dismissButton = { TextButton(onClick = { showCreate = false }) { Text("Cancelar") } }
    )
}

@Composable
private fun SectionTitle(title: String, count: Int) {
    Text("$title · $count", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp))
}

@Composable
private fun EmptyLibraryText(text: String) {
    Text(text, color = Color.White.copy(.45f), fontSize = 13.sp, modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 14.dp))
}

@Composable
private fun LibrarySongRow(song: Song, onSongClick: (Song) -> Unit) {
    Row(Modifier.fillMaxWidth().clickable(enabled = !song.audioUrl.isNullOrBlank()) { onSongClick(song) }.padding(horizontal = 20.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(48.dp).clip(RoundedCornerShape(8.dp)).background(Color(0xFF242424))) {
            if (!song.displayCoverUrl.isNullOrBlank()) AsyncImage(ImageRequest.Builder(LocalContext.current).data(song.displayCoverUrl).crossfade(true).build(), null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(song.title, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(song.artist, color = Color.White.copy(.55f), fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}
