package com.pokke.music.ui.sheets

import android.content.Context
import android.media.AudioManager
import android.media.MediaRouter2
import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.outlined.Cast
import androidx.compose.material.icons.outlined.GraphicEq
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private data class OutputRoute(val id: String, val name: String, val selected: Boolean, val system: Boolean)

@Composable
fun AudioOutputSheet(onDismiss: () -> Unit) {
    val context = LocalContext.current
    val audioManager = remember { context.getSystemService(Context.AUDIO_SERVICE) as AudioManager }
    var routes by remember { mutableStateOf<List<OutputRoute>>(emptyList()) }
    var volume by remember { mutableFloatStateOf(audioManager.getStreamVolume(AudioManager.STREAM_MUSIC).toFloat()) }
    val maxVolume = remember { audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC).coerceAtLeast(1).toFloat() }
    var routeError by remember { mutableStateOf<String?>(null) }

    fun refresh() {
        if (Build.VERSION.SDK_INT >= 30) {
            val router = MediaRouter2.getInstance(context)
            val selectedIds = router.systemController.selectedRoutes.map { it.id }.toSet()
            routes = router.routes.map { OutputRoute(it.id, it.name.toString(), it.id in selectedIds, it.isSystemRoute) }
        } else {
            routes = listOf(OutputRoute("phone", "Este teléfono", true, true))
        }
        if (routes.isEmpty()) routes = listOf(OutputRoute("phone", "Este teléfono", true, true))
    }
    LaunchedEffect(Unit) { refresh() }

    Column(Modifier.fillMaxWidth().padding(bottom = 26.dp)) {
        Box(Modifier.align(Alignment.CenterHorizontally).padding(top = 4.dp, bottom = 18.dp).size(width = 38.dp, height = 4.dp).clip(CircleShape).background(Color.White.copy(.3f)))
        Text("Salida de audio", color = Color.White, fontSize = 21.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(horizontal = 22.dp, vertical = 10.dp))
        routes.forEach { route ->
            OutputItem(
                icon = if (route.name.contains("phone", true) || route.name.contains("teléfono", true)) Icons.Default.PhoneAndroid else Icons.Outlined.Cast,
                title = route.name,
                subtitle = if (route.selected) "Reproduciendo aquí" else if (route.system) "Dispositivo disponible" else null,
                selected = route.selected,
                onClick = {
                    routeError = null
                    if (Build.VERSION.SDK_INT >= 30) {
                        runCatching {
                            val router = MediaRouter2.getInstance(context)
                            val target = router.routes.firstOrNull { it.id == route.id }
                            if (target != null) router.transferTo(target) else error("La salida ya no está disponible")
                            refresh()
                        }.onFailure { routeError = it.localizedMessage ?: "Android no permitió cambiar la salida" }
                    } else routeError = "La selección de rutas multimedia requiere Android 11 o posterior."
                }
            )
        }
        if (routeError != null) Text(routeError!!, color = Color(0xFFFFA6A6), fontSize = 12.sp, modifier = Modifier.padding(horizontal = 24.dp, vertical = 6.dp))
        Spacer(Modifier.height(10.dp))
        Row(Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Outlined.GraphicEq, null, tint = Color.White.copy(.7f), modifier = Modifier.size(22.dp))
            Slider(value = volume, onValueChange = { volume = it; audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, it.toInt(), 0) }, valueRange = 0f..maxVolume, modifier = Modifier.weight(1f).padding(start = 12.dp), colors = SliderDefaults.colors(thumbColor = Color.White, activeTrackColor = Color.White, inactiveTrackColor = Color.White.copy(.25f)))
        }
        Text("El cambio de dispositivo se realiza mediante el selector de rutas multimedia de Android.", color = Color.White.copy(.42f), fontSize = 11.sp, modifier = Modifier.padding(horizontal = 24.dp, vertical = 6.dp))
    }
}

@Composable
private fun OutputItem(icon: ImageVector, title: String, subtitle: String?, selected: Boolean, onClick: () -> Unit) {
    Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp).clip(RoundedCornerShape(20.dp)).background(if (selected) Color(0xFF353238) else Color(0xFF242226)).clickable(onClick = onClick).padding(horizontal = 14.dp, vertical = 17.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(46.dp).clip(CircleShape).background(Color.White.copy(.10f)), contentAlignment = Alignment.Center) { Icon(icon, null, tint = Color.White, modifier = Modifier.size(22.dp)) }
        Column(Modifier.weight(1f).padding(start = 14.dp)) { Text(title, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Medium); if (subtitle != null) Text(subtitle, color = Color.White.copy(.55f), fontSize = 12.sp) }
        if (selected) Icon(Icons.Default.Check, null, tint = Color.White, modifier = Modifier.size(22.dp))
    }
}
