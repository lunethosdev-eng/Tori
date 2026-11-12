package com.pokke.music.tv.ui.profile

import android.graphics.Bitmap
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pokke.music.data.pairing.DevicePairing
import com.pokke.music.tv.ui.components.tvFocusable
import com.pokke.music.util.QrBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun TvLinkDeviceScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val session = remember { DevicePairing.createSession(context) }
    var qrBmp by remember { mutableStateOf<Bitmap?>(null) }
    var linked by remember { mutableStateOf(DevicePairing.linkedDevices(context)) }

    LaunchedEffect(session.code) {
        qrBmp = withContext(Dispatchers.Default) {
            QrBitmap.encode(QrBitmap.pairPayload(session.code, session.pin), 640)
        }
        // poll linked list (phone writes same prefs only if same device — for multi-device use Supabase later)
        while (true) {
            kotlinx.coroutines.delay(2000)
            linked = DevicePairing.linkedDevices(context)
        }
    }

    val radar = rememberInfiniteTransition(label = "radar")
    val pulse by radar.animateFloat(
        0.45f, 1f,
        infiniteRepeatable(tween(1500, easing = LinearEasing), RepeatMode.Reverse),
        label = "pulse"
    )

    Row(Modifier.fillMaxSize().padding(36.dp)) {
        Column(Modifier.weight(1f)) {
            TextButton(onClick = onBack, modifier = Modifier.tvFocusable(onClick = onBack)) {
                Text("← Volver", color = Color.White)
            }
            Text("Gestión de Dispositivos Vinculados", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(20.dp))
            Box(
                Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF16161E), RoundedCornerShape(16.dp))
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("NUEVA VINCULACIÓN", color = Color(0xFFAAA), fontSize = 12.sp)
                    Spacer(Modifier.height(12.dp))
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(240.dp)) {
                        Canvas(Modifier.fillMaxSize()) {
                            val r = size.minDimension / 2
                            drawCircle(
                                Color.White.copy(alpha = 0.18f * pulse),
                                radius = r * pulse,
                                style = Stroke(width = 3f)
                            )
                            drawCircle(
                                Color(0xFF3797F0).copy(alpha = 0.25f * (1.3f - pulse)),
                                radius = r * 0.72f * pulse,
                                style = Stroke(width = 2.5f)
                            )
                        }
                        qrBmp?.let { bmp ->
                            Image(
                                bitmap = bmp.asImageBitmap(),
                                contentDescription = "QR pair",
                                modifier = Modifier
                                    .size(180.dp)
                                    .background(Color.White, RoundedCornerShape(12.dp))
                                    .padding(8.dp)
                            )
                        } ?: Box(
                            Modifier.size(180.dp).background(Color.White, RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) { Text("…", color = Color.Black) }
                    }
                    Spacer(Modifier.height(14.dp))
                    Text(
                        "PIN: ${session.pin.toCharArray().joinToString(" ")}",
                        color = Color.White,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "Escanea el QR con el teléfono o introduce el PIN en Pokke móvil.",
                        color = Color(0xFF9A9AA3),
                        fontSize = 13.sp
                    )
                    Text("código: ${session.code}", color = Color(0xFF666), fontSize = 11.sp, modifier = Modifier.padding(top = 6.dp))
                }
            }
        }
        Spacer(Modifier.width(24.dp))
        Column(Modifier.weight(1f).padding(top = 48.dp)) {
            Text("DISPOSITIVOS CONECTADOS", color = Color(0xFFAAA), fontSize = 12.sp)
            Spacer(Modifier.height(12.dp))
            if (linked.isEmpty()) {
                Text("Ninguno todavía — escanea el QR desde el teléfono.", color = Color(0xFF888))
            }
            linked.forEach { device ->
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .tvFocusable(
                            onClick = {
                                DevicePairing.unlink(context, device)
                                linked = DevicePairing.linkedDevices(context)
                            },
                            shape = RoundedCornerShape(12.dp)
                        )
                        .background(Color(0xFF16161E), RoundedCornerShape(12.dp))
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("📱 $device", color = Color.White)
                    Text("❌ Cerrar sesión", color = Color(0xFFFF8A80))
                }
            }
        }
    }
}
