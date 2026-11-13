package com.pokke.music.tv.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pokke.music.data.repository.AuthRepository
import kotlinx.coroutines.launch

@Composable
fun TvAuthScreen(auth: AuthRepository, onDone: () -> Unit) {
    var phase by remember { mutableStateOf(0) } // 0 welcome 1 login 2 register user 3 mangas
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var message by remember { mutableStateOf<String?>(null) }
    var busy by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Row(Modifier.fillMaxSize()) {
        // Left form
        Column(
            Modifier.weight(1f).fillMaxHeight().padding(48.dp),
            verticalArrangement = Arrangement.Center
        ) {
            when (phase) {
                0 -> {
                    Text("Pokke", color = Color.White, fontSize = 42.sp, fontWeight = FontWeight.Bold)
                    Text("Tu música en la gran pantalla.", color = Color(0xFFC8C8D0), fontSize = 18.sp)
                    Spacer(Modifier.height(28.dp))
                    Button(
                        onClick = { phase = 1 },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.Black),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.height(48.dp)
                    ) { Text("Empezar ahora", fontWeight = FontWeight.Bold) }
                }
                1 -> {
                    Text("Inicia sesión en Pokke", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(20.dp))
                    OutlinedTextField(email, { email = it }, label = { Text("Correo") }, singleLine = true, modifier = Modifier.fillMaxWidth(),
                        colors = fieldColors())
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(password, { password = it }, label = { Text("Contraseña") }, singleLine = true, modifier = Modifier.fillMaxWidth(),
                        colors = fieldColors())
                    Spacer(Modifier.height(18.dp))
                    Button(
                        onClick = {
                            scope.launch {
                                busy = true
                                auth.signIn(email.trim(), password)
                                    .onSuccess { onDone() }
                                    .onFailure { message = it.message }
                                busy = false
                            }
                        },
                        enabled = !busy,
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.Black)
                    ) { Text(if (busy) "…" else "Iniciar Sesión", fontWeight = FontWeight.Bold) }
                    TextButton(onClick = { phase = 2 }) { Text("¿No tienes cuenta? Registrarse", color = Color(0xFFAAA)) }
                }
                2 -> {
                    Text("Crea tu cuenta en Pokke", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(username, { username = it }, label = { Text("Nombre de usuario") }, singleLine = true, modifier = Modifier.fillMaxWidth(), colors = fieldColors())
                    Spacer(Modifier.height(10.dp))
                    OutlinedTextField(email, { email = it }, label = { Text("Correo") }, singleLine = true, modifier = Modifier.fillMaxWidth(), colors = fieldColors())
                    Spacer(Modifier.height(10.dp))
                    OutlinedTextField(password, { password = it }, label = { Text("Contraseña") }, singleLine = true, modifier = Modifier.fillMaxWidth(), colors = fieldColors())
                    Text("Tu foto y banner se configuran desde el teléfono.", color = Color(0xFF888), fontSize = 12.sp, modifier = Modifier.padding(top = 8.dp))
                    Spacer(Modifier.height(16.dp))
                    Button(onClick = { phase = 3 }, modifier = Modifier.fillMaxWidth().height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.Black)) {
                        Text("Continuar", fontWeight = FontWeight.Bold)
                    }
                }
                else -> {
                    Text("Mangas favoritos (MangaDex)", color = Color.White, fontSize = 26.sp, fontWeight = FontWeight.Bold)
                    Text("Puedes añadirlos después desde el teléfono.", color = Color(0xFFAAA), modifier = Modifier.padding(vertical = 12.dp))
                    Button(
                        onClick = {
                            scope.launch {
                                busy = true
                                auth.signUp(email.trim(), password, username.ifBlank { email.substringBefore('@') })
                                    .onSuccess { onDone() }
                                    .onFailure { message = it.message }
                                busy = false
                            }
                        },
                        enabled = !busy,
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.Black)
                    ) { Text(if (busy) "…" else "Crear cuenta", fontWeight = FontWeight.Bold) }
                }
            }
            message?.let { Text(it, color = Color(0xFFFF8A80), modifier = Modifier.padding(top = 12.dp)) }
        }
        // Right showcase panel
        Box(
            Modifier.weight(1f).fillMaxHeight().background(
                Brush.verticalGradient(listOf(Color(0xFF1A1035), Color(0xFF0E1A2E), Color(0xFF101018)))
            ),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(32.dp)) {
                Text("Descubre los mejores temas", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                Text("y canvas visuales en TV", color = Color(0xFFC8C8D0), fontSize = 16.sp)
            }
        }
    }
}

@Composable
private fun fieldColors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = Color.White,
    unfocusedTextColor = Color.White,
    focusedBorderColor = Color.White,
    unfocusedBorderColor = Color(0xFF444),
    focusedLabelColor = Color(0xFFAAA),
    unfocusedLabelColor = Color(0xFF777)
)
