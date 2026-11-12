package com.pokke.music.ui.library

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import com.pokke.music.data.model.Playlist
import com.pokke.music.data.model.Song
import com.pokke.music.data.repository.AuthRepository
import com.pokke.music.data.repository.LocalLibraryStore
import com.pokke.music.data.repository.DownloadQueueManager
import com.pokke.music.ui.theme.PokkeBlack
import kotlinx.coroutines.launch
import java.io.File

private enum class LibraryTab { DOWNLOADS, FAVORITES, HISTORY }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(
    repository: AuthRepository,
    isVisible: Boolean = true,
    onSongClick: (Song) -> Unit = {},
    onLoginRequired: () -> Unit = {}
) {
    val context = LocalContext.current
    val localStore = remember { LocalLibraryStore(context.applicationContext) }
    val scope = rememberCoroutineScope()
    val session by repository.session.collectAsState()
    val downloads by localStore.downloads.collectAsState(initial = emptyList())
    val downloadTasks by DownloadQueueManager.get(context.applicationContext).tasks.collectAsState()
    val history by localStore.history.collectAsState(initial = emptyList())
    var favorites by remember { mutableStateOf<List<Song>>(emptyList()) }
    var playlists by remember { mutableStateOf<List<Playlist>>(emptyList()) }
    var selectedPlaylist by remember { mutableStateOf<Playlist?>(null) }
    var playlistSongs by remember { mutableStateOf<List<Song>>(emptyList()) }
    var tab by remember { mutableStateOf(LibraryTab.DOWNLOADS) }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var showCreate by remember { mutableStateOf(false) }
    var playlistName by remember { mutableStateOf("") }
    var playlistDescription by remember { mutableStateOf("") }

    fun refreshRemote() {
        if (session == null) return
        scope.launch {
            loading = true
            error = null
            repository.getFavorites().onSuccess { favorites = it }.onFailure { error = it.localizedMessage }
            repository.getPlaylists().onSuccess { playlists = it }.onFailure { error = it.localizedMessage }
            loading = false
        }
    }

    LaunchedEffect(session?.userId, isVisible) {
        if (session == null) {
            favorites = emptyList()
            playlists = emptyList()
            selectedPlaylist = null
            playlistSongs = emptyList()
        } else if (isVisible) refreshRemote()
    }

    LaunchedEffect(selectedPlaylist?.id) {
        val selected = selectedPlaylist ?: return@LaunchedEffect
        loading = true
        repository.getPlaylistSongs(selected.id).onSuccess { playlistSongs = it }.onFailure { error = it.localizedMessage }
        loading = false
    }

    val visibleSongs = when (tab) {
        LibraryTab.DOWNLOADS -> downloads.filter { it.audioUrl?.let(::File)?.exists() == true }
        LibraryTab.FAVORITES -> favorites
        LibraryTab.HISTORY -> history
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().background(PokkeBlack).statusBarsPadding(),
        contentPadding = PaddingValues(bottom = 110.dp)
    ) {
        item {
            Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 18.dp), verticalAlignment = Alignment.CenterVertically) {
                if (selectedPlaylist != null) IconButton(onClick = { selectedPlaylist = null; playlistSongs = emptyList() }) {
                    Icon(Icons.Default.ArrowBack, "Volver", tint = Color.White)
                }
                Text(selectedPlaylist?.name ?: "Biblioteca", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                if (session != null && selectedPlaylist == null) IconButton(onClick = { showCreate = true }) {
                    Icon(Icons.Default.Add, "Crear playlist", tint = Color.White)
                }
                if (session != null) IconButton(onClick = { refreshRemote() }) {
                    Icon(Icons.Default.History, "Actualizar Kokoro", tint = Color.White.copy(.75f))
                }
            }
        }

        if (selectedPlaylist != null) {
            item { Text("${playlistSongs.size} canciones · Kokoro", color = Color.White.copy(.5f), fontSize = 12.sp, modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)) }
            items(playlistSongs, key = { "playlist_${it.id}" }) { song ->
                SongRow(song, trailing = "Quitar", onClick = { onSongClick(song) }, onTrailing = {
                    scope.launch {
                        repository.removeSongFromPlaylist(selectedPlaylist!!.id, song.id)
                            .onSuccess { playlistSongs = playlistSongs.filterNot { it.id == song.id }; refreshRemote() }
                            .onFailure { error = it.localizedMessage }
                    }
                })
            }
            if (playlistSongs.isEmpty()) item { EmptyLibraryText(if (loading) "Cargando playlist…" else "Esta playlist todavía está vacía.") }
        } else {
            item {
                Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    LibraryShortcut("Descargas", downloads.size, Icons.Default.Download, Modifier.weight(1f)) { tab = LibraryTab.DOWNLOADS }
                    LibraryShortcut("Favoritos", favorites.size, Icons.Default.Favorite, Modifier.weight(1f)) {
                        tab = LibraryTab.FAVORITES
                        refreshRemote()
                    }
                    LibraryShortcut("Historial", history.size, Icons.Default.History, Modifier.weight(1f)) { tab = LibraryTab.HISTORY }
                }
                Spacer(Modifier.height(22.dp))
            }
            item {
                Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("Tus playlists", color = Color.White, fontSize = 19.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                    if (session != null) TextButton(onClick = { showCreate = true }) { Text("Nueva", color = Color.White) }
                }
            }
            if (session == null) item { EmptyLibraryText("Inicia sesión con Kokoro para sincronizar favoritos y playlists.") }
            else if (playlists.isEmpty()) item { EmptyLibraryText("Todavía no tienes playlists en Kokoro.") }
            items(playlists, key = { "pl_${it.id}" }) { playlist ->
                Row(Modifier.fillMaxWidth().clickable { selectedPlaylist = playlist }.padding(horizontal = 20.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(54.dp).clip(RoundedCornerShape(10.dp)).background(Color(0xFF252525)), contentAlignment = Alignment.Center) {
                        if (!playlist.coverUrl.isNullOrBlank()) AsyncImage(model = playlist.coverUrl, contentDescription = null, modifier = Modifier.fillMaxSize())
                        else Icon(Icons.Default.QueueMusic, null, tint = Color.White.copy(.8f), modifier = Modifier.size(27.dp))
                    }
                    Column(Modifier.weight(1f).padding(start = 13.dp)) {
                        Text(playlist.name, color = Color.White, fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text("${playlist.songCount} canciones · Kokoro", color = Color.White.copy(.5f), fontSize = 12.sp)
                    }
                    IconButton(onClick = { scope.launch { repository.deletePlaylist(playlist.id).onSuccess { refreshRemote() }.onFailure { error = it.localizedMessage } } }) {
                        Icon(Icons.Default.DeleteOutline, "Eliminar playlist", tint = Color.White.copy(.55f))
                    }
                }
            }
            item {
                Spacer(Modifier.height(18.dp))
                Text(when (tab) { LibraryTab.DOWNLOADS -> "Descargas"; LibraryTab.FAVORITES -> "Favoritos de Kokoro"; LibraryTab.HISTORY -> "Recién escuchadas" }, color = Color.White, fontSize = 19.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp))
            }
            if (tab == LibraryTab.DOWNLOADS) {
                val activeTasks = downloadTasks.values.filter { it.status in setOf(
                    DownloadQueueManager.Status.QUEUED,
                    DownloadQueueManager.Status.RUNNING,
                    DownloadQueueManager.Status.PAUSED,
                    DownloadQueueManager.Status.FAILED,
                ) }.sortedBy { it.id }
                items(activeTasks, key = { "download_task_${it.id}" }) { task ->
                    Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 7.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text(task.song.title, color = Color.White, fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Text(
                                    when (task.status) {
                                        DownloadQueueManager.Status.QUEUED -> "En cola"
                                        DownloadQueueManager.Status.RUNNING -> if (task.totalBytes > 0) "Descargando · ${(task.progress * 100).toInt()}%" else "Descargando…"
                                        DownloadQueueManager.Status.PAUSED -> "Pausada"
                                        DownloadQueueManager.Status.FAILED -> task.error ?: "Error de descarga"
                                        else -> ""
                                    },
                                    color = if (task.status == DownloadQueueManager.Status.FAILED) Color(0xFFFF8A80) else Color.White.copy(.6f),
                                    fontSize = 12.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                            if (task.status == DownloadQueueManager.Status.RUNNING || task.status == DownloadQueueManager.Status.QUEUED) {
                                TextButton(onClick = { DownloadQueueManager.get(context).pause(task.id) }) { Text("Pausar") }
                            } else if (task.status == DownloadQueueManager.Status.PAUSED || task.status == DownloadQueueManager.Status.FAILED) {
                                TextButton(onClick = { DownloadQueueManager.get(context).resume(task.id) }) { Text("Reanudar") }
                            }
                            TextButton(onClick = { DownloadQueueManager.get(context).cancel(task.id) }) { Text("Cancelar") }
                        }
                        if (task.status == DownloadQueueManager.Status.RUNNING) {
                            if (task.totalBytes > 0) {
                                LinearProgressIndicator(
                                    progress = task.progress,
                                    modifier = Modifier.fillMaxWidth().padding(top = 5.dp),
                                    color = Color.White,
                                    trackColor = Color.White.copy(.14f),
                                )
                            } else {
                                LinearProgressIndicator(
                                    modifier = Modifier.fillMaxWidth().padding(top = 5.dp),
                                    color = Color.White,
                                    trackColor = Color.White.copy(.14f),
                                )
                            }
                        }
                    }
                }
            }
            if (tab == LibraryTab.FAVORITES && session == null) {
                item {
                    Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp)) {
                        EmptyLibraryText("Los favoritos se guardan en tu cuenta Kokoro, no solo en este teléfono.")
                        TextButton(onClick = onLoginRequired) { Text("Iniciar sesión", color = Color.White) }
                    }
                }
            } else if (loading && tab == LibraryTab.FAVORITES && favorites.isEmpty()) {
                item { EmptyLibraryText("Cargando favoritos de Kokoro…") }
            } else if (visibleSongs.isEmpty()) {
                item { EmptyLibraryText(when (tab) { LibraryTab.DOWNLOADS -> "Las canciones descargadas aparecerán aquí."; LibraryTab.FAVORITES -> "Todavía no has guardado canciones con ❤️."; LibraryTab.HISTORY -> "Tu historial de reproducción aparecerá aquí." }) }
            }
            items(visibleSongs, key = { "${tab.name}_${it.id}" }) { song ->
                SongRow(song, trailing = if (tab == LibraryTab.DOWNLOADS || tab == LibraryTab.FAVORITES) "Quitar" else null, onClick = { onSongClick(song) }, onTrailing = {
                    scope.launch {
                        if (tab == LibraryTab.DOWNLOADS) localStore.removeDownload(song)
                        else if (tab == LibraryTab.FAVORITES) repository.setFavorite(song, false)
                            .onSuccess { favorites = favorites.filterNot { it.id == song.id } }
                            .onFailure { error = it.localizedMessage }
                    }
                })
            }
            if (error != null) item { Text(error.orEmpty(), color = Color(0xFFFF8A80), fontSize = 12.sp, modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) }
        }
    }

    if (showCreate) AlertDialog(
        onDismissRequest = { showCreate = false },
        title = { Text("Nueva playlist") },
        text = {
            Column {
                OutlinedTextField(value = playlistName, onValueChange = { playlistName = it }, label = { Text("Nombre") }, singleLine = true)
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(value = playlistDescription, onValueChange = { playlistDescription = it }, label = { Text("Descripción (opcional)") })
            }
        },
        confirmButton = { TextButton(onClick = {
            scope.launch {
                repository.createPlaylist(playlistName, playlistDescription).onSuccess {
                    playlistName = ""; playlistDescription = ""; showCreate = false; refreshRemote()
                }.onFailure { error = it.localizedMessage }
            }
        }, enabled = playlistName.isNotBlank()) { Text("Crear") } },
        dismissButton = { TextButton(onClick = { showCreate = false }) { Text("Cancelar") } }
    )
}

@Composable
private fun LibraryShortcut(title: String, count: Int, icon: androidx.compose.ui.graphics.vector.ImageVector, modifier: Modifier, onClick: () -> Unit) {
    Column(modifier.clip(RoundedCornerShape(15.dp)).background(Color(0xFF191919)).clickable(onClick = onClick).padding(12.dp)) {
        Icon(icon, null, tint = Color.White, modifier = Modifier.size(23.dp))
        Spacer(Modifier.height(12.dp))
        Text(title, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
        Text("$count", color = Color.White.copy(.5f), fontSize = 12.sp)
    }
}

@Composable
private fun SongRow(song: Song, trailing: String?, onClick: () -> Unit, onTrailing: () -> Unit) {
    Row(Modifier.fillMaxWidth().clickable(enabled = !song.audioUrl.isNullOrBlank(), onClick = onClick).padding(horizontal = 20.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        AsyncImage(model = song.displayCoverUrl, contentDescription = null, modifier = Modifier.size(50.dp).clip(RoundedCornerShape(8.dp)))
        Column(Modifier.weight(1f).padding(start = 12.dp)) {
            Text(song.title, color = Color.White, fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(song.artist, color = Color.White.copy(.55f), fontSize = 12.sp, maxLines = 1)
        }
        if (trailing != null) TextButton(onClick = onTrailing) { Text(trailing, color = Color.White.copy(.65f), fontSize = 12.sp) }
    }
}

@Composable
private fun EmptyLibraryText(text: String) {
    Text(text, color = Color.White.copy(.45f), fontSize = 13.sp, modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp))
}
