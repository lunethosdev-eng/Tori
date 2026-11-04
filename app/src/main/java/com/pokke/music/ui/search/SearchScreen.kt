package com.pokke.music.ui.search

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.pokke.music.data.model.Album
import com.pokke.music.data.model.Artist
import com.pokke.music.data.model.Song
import com.pokke.music.data.repository.MusicRepository
import com.pokke.music.ui.theme.PokkeBlack
import kotlinx.coroutines.delay

private enum class SearchKind(val label: String) { SONGS("Canciones"), ARTISTS("Artistas"), ALBUMS("Álbumes") }
private data class SearchDetail(val title: String, val artist: String? = null, val isArtist: Boolean)

@Composable
fun SearchScreen(
    onSongClick: (Song) -> Unit = {},
    onPlayQueue: (List<Song>, Int) -> Unit = { songs, index -> songs.getOrNull(index)?.let(onSongClick) }
) {
    val repo = remember { MusicRepository() }
    var query by remember { mutableStateOf("") }
    var kind by remember { mutableStateOf(SearchKind.SONGS) }
    var songs by remember { mutableStateOf<List<Song>>(emptyList()) }
    var artists by remember { mutableStateOf<List<Artist>>(emptyList()) }
    var albums by remember { mutableStateOf<List<Album>>(emptyList()) }
    var detail by remember { mutableStateOf<SearchDetail?>(null) }
    var detailSongs by remember { mutableStateOf<List<Song>>(emptyList()) }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(query, kind) {
        detail = null
        if (query.isBlank()) {
            songs = emptyList(); artists = emptyList(); albums = emptyList()
            loading = false; error = null
            return@LaunchedEffect
        }
        delay(320)
        loading = true
        error = null
        when (kind) {
            SearchKind.SONGS -> repo.searchSongs(query)
                .onSuccess { songs = it }
                .onFailure { error = it.localizedMessage ?: "No se pudo buscar en Ukiyo"; songs = emptyList() }
            SearchKind.ARTISTS -> repo.searchArtists(query)
                .onSuccess { artists = it }
                .onFailure { error = it.localizedMessage ?: "No se pudieron buscar artistas"; artists = emptyList() }
            SearchKind.ALBUMS -> repo.searchAlbums(query)
                .onSuccess { albums = it }
                .onFailure { error = it.localizedMessage ?: "No se pudieron buscar álbumes"; albums = emptyList() }
        }
        loading = false
    }

    LaunchedEffect(detail) {
        val selected = detail ?: return@LaunchedEffect
        loading = true
        error = null
        val result = if (selected.isArtist) repo.getSongsByArtist(selected.title)
        else repo.getSongsByAlbum(selected.title, selected.artist)
        result.onSuccess { detailSongs = it }
            .onFailure { error = it.localizedMessage ?: "No se pudo cargar el catálogo"; detailSongs = emptyList() }
        loading = false
    }

    Column(
        modifier = Modifier.fillMaxSize().background(PokkeBlack).statusBarsPadding()
    ) {
        if (detail != null) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(start = 8.dp, end = 20.dp, top = 14.dp, bottom = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { detail = null; detailSongs = emptyList() }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Volver", tint = Color.White)
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(detail!!.title, color = Color.White, fontSize = 19.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(if (detail!!.isArtist) "Artista" else "Álbum · ${detail!!.artist.orEmpty()}", color = Color.White.copy(alpha = .55f), fontSize = 12.sp)
                }
            }
        } else {
            SearchField(query = query, onQueryChange = { query = it })
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SearchKind.entries.forEach { item ->
                    FilterChip(
                        selected = kind == item,
                        onClick = { kind = item },
                        label = { Text(item.label) }
                    )
                }
            }
        }

        when {
            loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = Color.White) }
            error != null -> Box(Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
                Text(error.orEmpty(), color = Color(0xFFFF8A80))
            }
            detail != null -> {
                if (detailSongs.isEmpty()) EmptyMessage("Este artista o álbum todavía no tiene canciones disponibles")
                else LazyColumn(contentPadding = PaddingValues(bottom = 112.dp)) {
                    itemsIndexed(detailSongs, key = { _, song -> song.id }) { index, song ->
                        SongRow(song) { onPlayQueue(detailSongs, index) }
                    }
                }
            }
            query.isBlank() -> EmptyMessage("Busca canciones, artistas o álbumes de Ukiyo")
            kind == SearchKind.SONGS -> {
                if (songs.isEmpty()) EmptyMessage("No se encontraron canciones")
                else LazyColumn(contentPadding = PaddingValues(bottom = 112.dp)) {
                    itemsIndexed(songs, key = { _, song -> song.id }) { index, song -> SongRow(song) { onPlayQueue(songs, index) } }
                }
            }
            kind == SearchKind.ARTISTS -> {
                if (artists.isEmpty()) EmptyMessage("No se encontraron artistas")
                else LazyColumn(contentPadding = PaddingValues(bottom = 112.dp)) {
                    itemsIndexed(artists, key = { _, artist -> artist.id.ifBlank { artist.name } }) { _, artist ->
                        ArtistRow(artist) { detail = SearchDetail(artist.name, isArtist = true) }
                    }
                }
            }
            else -> {
                if (albums.isEmpty()) EmptyMessage("No se encontraron álbumes")
                else LazyColumn(contentPadding = PaddingValues(bottom = 112.dp)) {
                    itemsIndexed(albums, key = { _, album -> "${album.name}-${album.artist}" }) { _, album ->
                        AlbumRow(album) { detail = SearchDetail(album.name, album.artist, isArtist = false) }
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchField(query: String, onQueryChange: (String) -> Unit) {
    Box(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp)
            .clip(RoundedCornerShape(24.dp)).background(Color(0xFF1C1C1C)).padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Search, contentDescription = null, tint = Color.White.copy(alpha = .55f), modifier = Modifier.size(22.dp))
            Spacer(Modifier.width(12.dp))
            BasicTextField(
                value = query, onValueChange = onQueryChange, singleLine = true,
                textStyle = TextStyle(color = Color.White, fontSize = 16.sp), cursorBrush = SolidColor(Color.White),
                modifier = Modifier.weight(1f),
                decorationBox = { inner ->
                    if (query.isEmpty()) Text("Buscar en Ukiyo…", color = Color.White.copy(alpha = .4f), fontSize = 16.sp)
                    inner()
                }
            )
            if (query.isNotEmpty()) IconButton(onClick = { onQueryChange("") }, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Default.Clear, contentDescription = "Borrar", tint = Color.White.copy(alpha = .65f), modifier = Modifier.size(18.dp))
            }
        }
    }
}

@Composable
private fun SongRow(song: Song, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable(enabled = !song.audioUrl.isNullOrBlank(), onClick = onClick).padding(horizontal = 20.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        CoverImage(song.displayCoverUrl, 52)
        Spacer(Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(song.title, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(
                if (song.audioUrl.isNullOrBlank()) "${song.artist} · Audio no disponible" else song.artist,
                color = Color.White.copy(alpha = .58f), fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis
            )
        }
        song.durationSeconds.takeIf { it > 0 }?.let { Text(formatDuration(it), color = Color.White.copy(alpha = .4f), fontSize = 11.sp) }
    }
}

@Composable
private fun ArtistRow(artist: Artist, onClick: () -> Unit) {
    Row(Modifier.fillMaxWidth().clickable(onClick = onClick).padding(horizontal = 20.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
        CoverImage(artist.imageUrl, 52, circle = true)
        Spacer(Modifier.width(14.dp))
        Column {
            Text(artist.name, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Medium)
            Text("Artista", color = Color.White.copy(alpha = .55f), fontSize = 13.sp)
        }
    }
}

@Composable
private fun AlbumRow(album: Album, onClick: () -> Unit) {
    Row(Modifier.fillMaxWidth().clickable(onClick = onClick).padding(horizontal = 20.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
        CoverImage(album.coverUrl, 52)
        Spacer(Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(album.name, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(album.artist, color = Color.White.copy(alpha = .55f), fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        Text("${album.songCount} pistas", color = Color.White.copy(alpha = .4f), fontSize = 11.sp)
    }
}

@Composable
private fun CoverImage(url: String?, size: Int, circle: Boolean = false) {
    val context = LocalContext.current
    Box(Modifier.size(size.dp).clip(if (circle) androidx.compose.foundation.shape.CircleShape else RoundedCornerShape(9.dp)).background(Color(0xFF292929))) {
        if (!url.isNullOrBlank()) AsyncImage(
            model = ImageRequest.Builder(context).data(url).crossfade(true).build(),
            contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize()
        )
    }
}

@Composable
private fun EmptyMessage(message: String) {
    Box(Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
        Text(message, color = Color.White.copy(alpha = .45f), fontSize = 14.sp)
    }
}

private fun formatDuration(seconds: Int): String = "%d:%02d".format(seconds / 60, seconds % 60)
