package com.pokke.music.ui.sheets

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Queue
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun OverflowMenuSheet(
    onDismiss: () -> Unit,
    onShareSong: () -> Unit,
    onFavorite: () -> Unit,
    onDownload: () -> Unit,
    onPlayNext: () -> Unit,
    onAddToQueue: () -> Unit,
    onAddToPlaylist: () -> Unit,
    onSleepTimer: () -> Unit,
    onRemoveFromQueue: () -> Unit,
    onSongInfo: () -> Unit
) {
    Column(Modifier.fillMaxWidth().padding(bottom = 28.dp)) {
        MenuItem(Icons.Default.Share, "Compartir canción", onShareSong)
        MenuItem(Icons.Default.Favorite, "Agregar / quitar de favoritos", onFavorite)
        MenuItem(Icons.Default.Download, "Descargar para escuchar sin conexión", onDownload)
        HorizontalDivider(color = Color.White.copy(.12f), modifier = Modifier.padding(vertical = 7.dp))
        MenuItem(Icons.Default.Queue, "Reproducir después", onPlayNext)
        MenuItem(Icons.Default.Queue, "Agregar a la cola", onAddToQueue)
        MenuItem(Icons.Default.Queue, "Agregar a una playlist", onAddToPlaylist)
        MenuItem(Icons.Default.Delete, "Quitar de la cola", onRemoveFromQueue)
        HorizontalDivider(color = Color.White.copy(.12f), modifier = Modifier.padding(vertical = 7.dp))
        MenuItem(Icons.Default.Timer, "Temporizador de apagado", onSleepTimer)
        MenuItem(Icons.Default.Info, "Información de la canción", onSongInfo)
    }
}

@Composable
private fun MenuItem(icon: ImageVector, title: String, onClick: () -> Unit) {
    Row(Modifier.fillMaxWidth().clickable(onClick = onClick).padding(horizontal = 24.dp, vertical = 14.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = Color.White, modifier = Modifier.size(22.dp))
        Spacer(Modifier.width(20.dp))
        Text(title, color = Color.White, fontSize = 15.sp)
    }
}
