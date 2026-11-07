package com.pokke.music.ui.sheets

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties

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
    onSongInfo: () -> Unit,
    glass: Boolean = true
) {
    // Floating panel anchored top-end (from the ⋯ button)
    Popup(
        alignment = Alignment.TopEnd,
        offset = IntOffset((-12), 96),
        onDismissRequest = onDismiss,
        properties = PopupProperties(focusable = true)
    ) {
        Column(
            Modifier
                .width(280.dp)
                .shadow(24.dp, RoundedCornerShape(18.dp), clip = false)
                .clip(RoundedCornerShape(18.dp))
                .then(
                    if (glass) Modifier
                        .background(Color(0xCC2A2A32))
                        .border(0.7.dp, Color.White.copy(0.22f), RoundedCornerShape(18.dp))
                    else Modifier.background(Color(0xF21C1C22))
                )
                .padding(vertical = 8.dp)
        ) {
            MenuItem(Icons.Default.Share, "Compartir canción", onShareSong, glass)
            MenuItem(Icons.Default.Favorite, "Agregar / quitar de favoritos", onFavorite, glass)
            MenuItem(Icons.Default.Download, "Descargar para offline", onDownload, glass)
            HorizontalDivider(color = Color.White.copy(.12f), modifier = Modifier.padding(vertical = 6.dp, horizontal = 12.dp))
            MenuItem(Icons.Default.Queue, "Reproducir después", onPlayNext, glass)
            MenuItem(Icons.Default.Queue, "Agregar a la cola", onAddToQueue, glass)
            MenuItem(Icons.Default.Queue, "Agregar a una playlist", onAddToPlaylist, glass)
            MenuItem(Icons.Default.Delete, "Quitar de la cola", onRemoveFromQueue, glass)
            HorizontalDivider(color = Color.White.copy(.12f), modifier = Modifier.padding(vertical = 6.dp, horizontal = 12.dp))
            MenuItem(Icons.Default.Timer, "Temporizador de apagado", onSleepTimer, glass)
            MenuItem(Icons.Default.Info, "Información de la canción", onSongInfo, glass)
        }
    }
}

@Composable
private fun MenuItem(icon: ImageVector, title: String, onClick: () -> Unit, glass: Boolean) {
    Row(
        Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = Color.White.copy(if (glass) 0.95f else 0.9f), modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(14.dp))
        Text(title, color = Color.White, fontSize = 14.sp)
    }
}
