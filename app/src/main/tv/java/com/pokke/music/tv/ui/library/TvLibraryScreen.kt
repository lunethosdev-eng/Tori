package com.pokke.music.tv.ui.library

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.PlaylistPlay
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
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
import java.io.File

private enum class TvLibrarySection { HISTORY, DOWNLOADS, PLAYLISTS }

@Composable
fun TvLibraryScreen(repository: AuthRepository, onPlay: (Song) -> Unit) {
    val context = LocalContext.current
    val store = remember { LocalLibraryStore(context.applicationContext) }
    val session by repository.session.collectAsState()
    val history by store.history.collectAsState(initial = emptyList())
    val downloads by store.downloads.collectAsState(initial = emptyList())
    var section by remember { mutableStateOf(TvLibrarySection.HISTORY) }
    var playlists by remember { mutableStateOf<List<Playlist>>(emptyList()) }
    var selectedPlaylist by remember { mutableStateOf<Playlist?>(null) }
    var playlistSongs by remember { mutableStateOf<List<Song>>(emptyList()) }
    var loading by remember { mutableStateOf(false) }

    LaunchedEffect(session?.userId) {
        if (session == null) playlists = emptyList()
        else repository.getPlaylists().onSuccess { playlists = it }
    }
    LaunchedEffect(selectedPlaylist?.id) {
        val selected = selectedPlaylist ?: return@LaunchedEffect
        loading = true
        repository.getPlaylistSongs(selected.id).onSuccess { playlistSongs = it }.onFailure { playlistSongs = emptyList() }
        loading = false
    }

    val visibleSongs = when (section) {
        TvLibrarySection.HISTORY -> history
        TvLibrarySection.DOWNLOADS -> downloads.filter { it.audioUrl?.let(::File)?.exists() == true }
        TvLibrarySection.PLAYLISTS -> playlistSongs
    }

    Column(Modifier.fillMaxSize().background(Color(0xFF0C0C0E)).padding(horizontal = 36.dp, vertical = 24.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (selectedPlaylist != null) {
                Row(Modifier.clickable { selectedPlaylist = null; playlistSongs = emptyList() }.padding(end = 14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.ArrowBack, null, tint = Color.White.copy(.8f))
                }
            }
            Text(selectedPlaylist?.name ?: "Biblioteca", color = Color.White, fontSize = 30.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(20.dp))
        if (selectedPlaylist == null) {
            Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                TvLibraryShortcut("Recientes", history.size, Icons.Default.History, section == TvLibrarySection.HISTORY) { section = TvLibrarySection.HISTORY }
                TvLibraryShortcut("Descargas", downloads.size, Icons.Default.Download, section == TvLibrarySection.DOWNLOADS) { section = TvLibrarySection.DOWNLOADS }
                TvLibraryShortcut("Playlists", playlists.size, Icons.Default.PlaylistPlay, section == TvLibrarySection.PLAYLISTS) { section = TvLibrarySection.PLAYLISTS }
            }
        }
        Spacer(Modifier.height(24.dp))

        if (section == TvLibrarySection.PLAYLISTS && selectedPlaylist == null) {
            Text("Tus playlists", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 20.sp)
            Spacer(Modifier.height(12.dp))
            if (session == null) {
                Text("Inicia sesión para ver tus playlists sincronizadas.", color = Color.White.copy(.45f), fontSize = 15.sp)
            } else if (playlists.isEmpty()) {
                Text("Todavía no tienes playlists.", color = Color.White.copy(.45f), fontSize = 15.sp)
            } else {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    items(playlists, key = { it.id }) { playlist ->
                        var focused by remember { mutableStateOf(false) }
                        Column(
                            Modifier.width(170.dp).onFocusChanged { focused = it.isFocused }
                                .border(if (focused) 2.dp else 1.dp, if (focused) Color(0xFFFC3C44) else Color.White.copy(.08f), RoundedCornerShape(14.dp))
                                .background(if (focused) Color(0xFF26191C) else Color(0xFF17171B), RoundedCornerShape(14.dp))
                                .clickable { selectedPlaylist = playlist; section = TvLibrarySection.PLAYLISTS }
                                .padding(8.dp)
                        ) {
                            Box(Modifier.fillMaxWidth().aspectRatio(1f).clip(RoundedCornerShape(10.dp)).background(Color(0xFF25252B))) {
                                if (!playlist.coverUrl.isNullOrBlank()) AsyncImage(playlist.coverUrl, null, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                                else Icon(Icons.Default.PlaylistPlay, null, tint = Color.White.copy(.8f), modifier = Modifier.align(Alignment.Center).size(48.dp))
                            }
                            Spacer(Modifier.height(8.dp))
                            Text(playlist.name, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Text("${playlist.songCount} canciones", color = Color.White.copy(.5f), fontSize = 12.sp)
                        }
                    }
                }
            }
        } else {
            Text(
                when (section) {
                    TvLibrarySection.HISTORY -> "Escuchado recientemente"
                    TvLibrarySection.DOWNLOADS -> "Descargas"
                    TvLibrarySection.PLAYLISTS -> "Canciones de la playlist"
                },
                color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 20.sp
            )
            Spacer(Modifier.height(12.dp))
            when {
                loading -> Text("Cargando playlist…", color = Color.White.copy(.5f), fontSize = 15.sp)
                visibleSongs.isEmpty() -> Text(
                    when (section) {
                        TvLibrarySection.HISTORY -> "Todavía no hay reproducciones recientes."
                        TvLibrarySection.DOWNLOADS -> "Las canciones descargadas aparecerán aquí."
                        TvLibrarySection.PLAYLISTS -> "Esta playlist todavía está vacía."
                    }, color = Color.White.copy(.45f), fontSize = 15.sp
                )
                else -> LazyColumn(contentPadding = PaddingValues(bottom = 32.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(visibleSongs, key = { "${section.name}_${it.id}" }) { song ->
                        var focused by remember { mutableStateOf(false) }
                        Row(
                            Modifier.fillMaxWidth().onFocusChanged { focused = it.isFocused }
                                .border(if (focused) 2.dp else 1.dp, if (focused) Color(0xFFFC3C44) else Color.White.copy(.07f), RoundedCornerShape(12.dp))
                                .background(if (focused) Color(0xFF24171A) else Color(0xFF151518), RoundedCornerShape(12.dp))
                                .clickable { onPlay(song) }.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            AsyncImage(song.displayCoverUrl, null, Modifier.size(58.dp), contentScale = ContentScale.Crop)
                            Column(Modifier.weight(1f).padding(start = 14.dp)) {
                                Text(song.title, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Text(song.artist, color = Color.White.copy(.55f), fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            }
                            Text("▶", color = if (focused) Color(0xFFFC3C44) else Color.White.copy(.65f), fontSize = 18.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TvLibraryShortcut(
    title: String,
    count: Int,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    selected: Boolean,
    onClick: () -> Unit
) {
    var focused by remember { mutableStateOf(false) }
    val active = selected || focused
    Row(
        Modifier.widthIn(min = 190.dp).height(72.dp).onFocusChanged { focused = it.isFocused }
            .border(if (active) 2.dp else 1.dp, if (active) Color(0xFFFC3C44) else Color.White.copy(.10f), RoundedCornerShape(14.dp))
            .background(if (active) Color(0xFF26191C) else Color(0xFF17171B), RoundedCornerShape(14.dp))
            .clickable(onClick = onClick).padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = if (active) Color(0xFFFC3C44) else Color.White.copy(.75f), modifier = Modifier.size(24.dp))
        Spacer(Modifier.width(12.dp))
        Column {
            Text(title, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
            Text("$count", color = Color.White.copy(.48f), fontSize = 12.sp)
        }
    }
}
