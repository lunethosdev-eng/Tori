package com.pokke.music.tv.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.random.Random

@Composable
fun TvLinkDeviceScreen(onBack: () -> Unit) {
    val pin = remember { (1000..9999).random().toString().toCharArray().joinToString(" ") }
    Row(Modifier.fillMaxSize().padding(36.dp)) {
        Column(Modifier.weight(1f)) {
            TextButton(onClick = onBack) { Text("← Volver", color = Color.White) }
            Text("Gestión de Dispositivos Vinculados", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(24.dp))
            Box(
                Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF16161E), RoundedCornerShape(16.dp))
                    .padding(24.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Text("NUEVA VINCULACIÓN", color = Color(0xFFAAA), fontSize = 12.sp)
                    Spacer(Modifier.height(16.dp))
                    // QR placeholder (phone will scan via deep link later)
                    Box(
                        Modifier
                            .size(180.dp)
                            .background(Color.White, RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("QR\nPokke", color = Color.Black, textAlign = TextAlign.Center, fontWeight = FontWeight.Bold)
                    }
                    Spacer(Modifier.height(16.dp))
                    Text("PIN Manual: $pin", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                    Text("Muestra esto para conectar desde el teléfono.", color = Color(0xFF9A9AA3), fontSize = 13.sp)
                }
            }
        }
        Spacer(Modifier.width(24.dp))
        Column(Modifier.weight(1f).padding(top = 48.dp)) {
            Text("DISPOSITIVOS CONECTADOS", color = Color(0xFFAAA), fontSize = 12.sp)
            Spacer(Modifier.height(12.dp))
            listOf("📱 iPhone 15 Pro (Carlos)", "📱 iPad Air (Sala)").forEach {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .background(Color(0xFF16161E), RoundedCornerShape(12.dp))
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(it, color = Color.White)
                    Text("❌ Cerrar sesión", color = Color(0xFFFF8A80))
                }
            }
        }
    }
}
