package com.pokke.music.ui.sheets

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Album
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Queue
import androidx.compose.material.icons.filled.Radio
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material.icons.outlined.HighQuality
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun OverflowMenuSheet(
    onDismiss: () -> Unit,
    onShareLyrics: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 32.dp)
    ) {
        MenuItem(Icons.Default.Share, "Compartir", onClick = onShareLyrics)
        MenuItem(Icons.Outlined.HighQuality, "Mejorar calidad", onClick = {})
        MenuItem(Icons.Default.VideoLibrary, "Convertir a video", onClick = {})
        HorizontalDivider(color = Color.White.copy(0.1f), modifier = Modifier.padding(vertical = 8.dp))
        MenuItem(Icons.Default.Radio, "Iniciar radio", onClick = {})
        MenuItem(Icons.Default.Queue, "Reproducir después", onClick = {})
        MenuItem(Icons.Default.Queue, "Agregar a la cola", onClick = {})
        MenuItem(Icons.Default.Download, "Descargar", onClick = {})
        MenuItem(Icons.Default.Delete, "Quitar de la caché", onClick = {})
        HorizontalDivider(color = Color.White.copy(0.1f), modifier = Modifier.padding(vertical = 8.dp))
        MenuItem(Icons.Default.Album, "Abrir álbum", subtitle = "Bewitched", onClick = {})
        MenuItem(Icons.Default.Mic, "Abrir artista", subtitle = "Laufey", onClick = {})
        MenuItem(Icons.Default.Info, "Información de la canción", onClick = {})
        HorizontalDivider(color = Color.White.copy(0.1f), modifier = Modifier.padding(vertical = 8.dp))
        MenuItem(Icons.Default.Timer, "Temporizador", onClick = {})
        MenuItem(Icons.Outlined.Tune, "Lyrics offset", onClick = {})
        MenuItem(Icons.Default.BugReport, "Copiar registro", onClick = {})
    }
}

@Composable
private fun MenuItem(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 24.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(22.dp))
        Spacer(modifier = Modifier.width(20.dp))
        Column {
            Text(title, color = Color.White, fontSize = 15.sp)
            if (subtitle != null) {
                Text(subtitle, color = Color.White.copy(0.5f), fontSize = 13.sp)
            }
        }
    }
}
