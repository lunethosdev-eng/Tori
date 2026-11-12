package com.pokke.music.ui.pairing

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pokke.music.data.pairing.DevicePairing
import com.pokke.music.ui.theme.PokkeTheme

/**
 * Handles pokke://pair?code=...&pin=... from QR scan / deep link.
 * Also allows manual PIN entry.
 */
class PairDeviceActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val data = intent?.data
        val codeArg = data?.getQueryParameter("code").orEmpty()
        val pinArg = data?.getQueryParameter("pin").orEmpty()

        setContent {
            PokkeTheme {
                var code by remember { mutableStateOf(codeArg) }
                var pin by remember { mutableStateOf(pinArg) }
                var status by remember { mutableStateOf<String?>(null) }

                Column(
                    Modifier
                        .fillMaxSize()
                        .background(Color(0xFF0B0B10))
                        .padding(24.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Vincular TV", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(16.dp))
                    OutlinedTextField(code, { code = it }, label = { Text("Código") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(pin, { pin = it }, label = { Text("PIN") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    Spacer(Modifier.height(16.dp))
                    Button(
                        onClick = {
                            val name = android.os.Build.MODEL ?: "Teléfono"
                            val ok = DevicePairing.confirmPair(this@PairDeviceActivity, code.trim(), pin.trim(), name)
                            status = if (ok) "Vinculado ✓" else "Código o PIN incorrecto"
                            Toast.makeText(this@PairDeviceActivity, status, Toast.LENGTH_SHORT).show()
                            if (ok) finish()
                        },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) { Text("Confirmar vínculo") }
                    status?.let {
                        Spacer(Modifier.height(12.dp))
                        Text(it, color = if (it.startsWith("Vinculado")) Color(0xFF9BE7B0) else Color(0xFFFF8A80))
                    }
                }
            }
        }
    }
}
