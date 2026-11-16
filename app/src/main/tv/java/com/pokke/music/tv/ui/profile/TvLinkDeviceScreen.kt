package com.pokke.music.tv.ui.profile

import android.graphics.Bitmap
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pokke.music.data.pairing.DeviceLinkRepository
import com.pokke.music.tv.ui.components.tvFocusable
import com.pokke.music.util.QrBitmap
import kotlinx.coroutines.delay
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun TvLinkDeviceScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val repository = remember { DeviceLinkRepository() }
    var qr by remember { mutableStateOf<Bitmap?>(null) }
    var code by remember { mutableStateOf("············") }
    var pin by remember { mutableStateOf("------") }
    var status by remember { mutableStateOf("Preparando vinculación segura…") }
    var approved by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        val created = repository.createTvPairing(android.os.Build.MODEL ?: "Pokke Android TV")
        created.onSuccess { pairing ->
            code = pairing.code; pin = pairing.pin
            qr = withContext(Dispatchers.Default) { QrBitmap.encode(QrBitmap.pairPayload(pairing.id, pairing.code, pairing.pin, pairing.secret), 640) }
            status = "Escanea el QR desde Pokke móvil o introduce el código y PIN."
            while (!approved && System.currentTimeMillis() / 1000 < pairing.expiresAt) {
                delay(2500)
                repository.pollTvPairing(pairing).onSuccess { if (it) { approved = true; status = "¡Dispositivo vinculado correctamente!" } }
            }
            if (!approved) status = "El código expiró. Vuelve a abrir esta pantalla para generar otro."
        }.onFailure { status = "No se pudo generar el código: ${it.message ?: "error de conexión"}" }
    }

    val transition = rememberInfiniteTransition(label = "pairing")
    val alpha by transition.animateFloat(0.25f, 0.8f, infiniteRepeatable(tween(1600), RepeatMode.Reverse), label = "pulse")
    Row(Modifier.fillMaxSize().background(Color(0xFF09090D)).padding(36.dp), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.Center) {
            TextButton(onClick = onBack, modifier = Modifier.tvFocusable(onClick = onBack)) { Text("← Volver", color = Color.White) }
            Text("Vincula tu Android TV", color = Color.White, fontSize = 30.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(12.dp))
            Text("Tu teléfono principal autorizará esta televisión para usar tu cuenta.", color = Color(0xFFB7B7C2), fontSize = 16.sp)
            Spacer(Modifier.height(28.dp))
            Text("CÓDIGO DE VINCULACIÓN", color = Color(0xFF9999A5), fontSize = 12.sp)
            Text(code.chunked(4).joinToString(" "), color = Color.White, fontSize = 25.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(10.dp))
            Text("PIN  $pin", color = Color(0xFFB9D7FF), fontSize = 22.sp, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(18.dp))
            Text(status, color = if (approved) Color(0xFF9BE7B0) else Color.White.copy(alpha = alpha + .2f), fontSize = 14.sp)
        }
        Spacer(Modifier.width(32.dp))
        Box(Modifier.size(300.dp).background(Color.White, RoundedCornerShape(22.dp)).padding(18.dp), contentAlignment = Alignment.Center) {
            qr?.let { Image(it.asImageBitmap(), contentDescription = "QR seguro para vincular Pokke TV", modifier = Modifier.fillMaxSize()) }
                ?: Text("QR", color = Color.DarkGray)
        }
    }
}
