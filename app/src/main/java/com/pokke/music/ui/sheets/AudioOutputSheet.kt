package com.pokke.music.ui.sheets

import android.content.Context
import android.media.AudioDeviceInfo
import android.media.AudioManager
import android.media.MediaRoute2Info
import android.media.MediaRouter2
import android.os.Build
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Speaker
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** Uses Android's media routing framework instead of pretending an AudioDeviceInfo click changes output. */
@Composable
fun AudioOutputSheet(onDismiss: () -> Unit) {
    val context = LocalContext.current
    val router = remember { if (Build.VERSION.SDK_INT >= 30) MediaRouter2.getInstance(context) else null }
    val systemController = remember(router) { router?.systemController }
    val selectedRoutes = remember(systemController) { systemController?.selectedRoutes.orEmpty() }
    val selectableRoutes = remember(systemController) { systemController?.selectableRoutes.orEmpty() }
    val routes = (selectedRoutes + selectableRoutes).distinctBy { it.id }
    val selectedIds = selectedRoutes.map { it.id }.toSet()
    val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    val outputs = audioManager.getDevices(AudioManager.GET_DEVICES_OUTPUTS).toList()

    Column(Modifier.fillMaxWidth().padding(horizontal = 22.dp).padding(bottom = 28.dp)) {
        Text("Salida de audio", color = Color.White, fontSize = 19.sp, modifier = Modifier.padding(top = 20.dp, bottom = 6.dp))
        Text("Elige dónde se reproduce la música", color = Color.White.copy(.55f), fontSize = 12.sp)
        Spacer(Modifier.height(12.dp))

        if (routes.isNotEmpty()) {
            routes.forEach { route ->
                val selected = route.id in selectedIds
                Row(
                    Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp))
                        .clickable(enabled = !selected) { transferToRoute(context, router, route) }
                        .padding(horizontal = 10.dp, vertical = 13.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(routeIcon(route), null, tint = Color.White.copy(.85f))
                    Column(Modifier.weight(1f).padding(start = 13.dp)) {
                        Text(route.name.toString(), color = Color.White, fontSize = 14.sp)
                        Text(if (selected) "En uso" else "Disponible", color = if (selected) Color(0xFF9BE7B0) else Color.White.copy(.48f), fontSize = 11.sp)
                    }
                    if (selected) Text("✓", color = Color(0xFF9BE7B0), fontSize = 18.sp)
                }
            }
        } else {
            val labels = outputs.filter { it.type != AudioDeviceInfo.TYPE_UNKNOWN }
                .map { it.productName?.toString()?.takeIf(String::isNotBlank) ?: deviceTypeName(it.type) }
                .distinct()
            if (labels.isEmpty()) Text("Android no reporta salidas externas conectadas.", color = Color.White.copy(.7f), fontSize = 13.sp)
            else labels.forEach { label -> Text(label, color = Color.White.copy(.7f), fontSize = 13.sp, modifier = Modifier.padding(vertical = 5.dp)) }
        }

        if (Build.VERSION.SDK_INT >= 34) {
            Spacer(Modifier.height(12.dp))
            Button(
                onClick = {
                    val opened = runCatching { router?.showSystemOutputSwitcher() == true }.getOrDefault(false)
                    if (!opened) Toast.makeText(context, "Android no pudo abrir el selector de audio.", Toast.LENGTH_LONG).show()
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.Black)
            ) { Text("Abrir selector de Android") }
        } else if (routes.isEmpty()) {
            Spacer(Modifier.height(10.dp))
            Text("Con Android 13, cambia la salida desde el selector de audio de la notificación multimedia.", color = Color.White.copy(.55f), fontSize = 12.sp)
            TextButton(onClick = {
                audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_SAME, AudioManager.FLAG_SHOW_UI)
            }, modifier = Modifier.align(Alignment.End)) { Text("Mostrar controles de volumen") }
        }
    }
}

private fun transferToRoute(context: Context, router: MediaRouter2?, route: MediaRoute2Info) {
    val success = runCatching {
        if (router == null) false else {
            router.transferTo(route)
            true
        }
    }.getOrDefault(false)
    Toast.makeText(
        context,
        if (success) "Solicitando salida: ${route.name}" else "No se pudo cambiar a esa salida.",
        Toast.LENGTH_SHORT
    ).show()
}

private fun routeIcon(route: MediaRoute2Info) = when {
    route.name.toString().contains("bluetooth", ignoreCase = true) || route.name.toString().contains("buds", ignoreCase = true) -> Icons.Default.Bluetooth
    route.name.toString().contains("speaker", ignoreCase = true) || route.name.toString().contains("altavoz", ignoreCase = true) -> Icons.Default.PhoneAndroid
    else -> Icons.Default.Speaker
}

private fun deviceTypeName(type: Int): String = when (type) {
    AudioDeviceInfo.TYPE_BUILTIN_SPEAKER -> "Altavoz del teléfono"
    AudioDeviceInfo.TYPE_BUILTIN_EARPIECE -> "Auricular del teléfono"
    AudioDeviceInfo.TYPE_WIRED_HEADPHONES, AudioDeviceInfo.TYPE_WIRED_HEADSET -> "Auriculares con cable"
    AudioDeviceInfo.TYPE_BLUETOOTH_A2DP, AudioDeviceInfo.TYPE_BLUETOOTH_SCO,
    AudioDeviceInfo.TYPE_BLE_HEADSET, AudioDeviceInfo.TYPE_BLE_SPEAKER -> "Bluetooth"
    AudioDeviceInfo.TYPE_USB_DEVICE, AudioDeviceInfo.TYPE_USB_HEADSET -> "USB"
    else -> "Salida de audio"
}
