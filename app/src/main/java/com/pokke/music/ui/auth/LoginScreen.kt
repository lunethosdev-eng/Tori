package com.pokke.music.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pokke.music.data.repository.AuthRepository
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    repository: AuthRepository,
    onContinueAsGuest: () -> Unit,
    onLoginSuccess: () -> Unit
) {
    var register by remember { mutableStateOf(false) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var displayName by remember { mutableStateOf("") }
    var busy by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf<String?>(null) }
    var error by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val session by repository.session.collectAsState()

    if (session != null) {
        ProfileScreen(repository = repository, onBack = {}, onSignedOut = onContinueAsGuest)
        return
    }

    Column(
        modifier = Modifier.fillMaxSize().background(Color(0xFF0A0A0A)).verticalScroll(rememberScrollState()).padding(horizontal = 26.dp, vertical = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Pokke", color = Color.White, fontSize = 36.sp, fontWeight = FontWeight.Bold)
        Text("Tu música, a tu manera", color = Color.White.copy(.58f), fontSize = 14.sp)
        Spacer(Modifier.height(38.dp))
        Text(if (register) "Crear cuenta" else "Bienvenido de nuevo", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(8.dp))
        Text(if (register) "Tu cuenta Kokoro sincronizará tu música." else "Inicia sesión para sincronizar tus datos.", color = Color.White.copy(.55f), fontSize = 13.sp)
        Spacer(Modifier.height(26.dp))
        if (register) {
            OutlinedTextField(value = displayName, onValueChange = { displayName = it }, label = { Text("Nombre para mostrar") }, singleLine = true, modifier = Modifier.fillMaxWidth(), colors = fieldColors())
            Spacer(Modifier.height(12.dp))
        }
        OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Correo electrónico") }, singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email), modifier = Modifier.fillMaxWidth(), colors = fieldColors())
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(value = password, onValueChange = { password = it }, label = { Text("Contraseña") }, singleLine = true, visualTransformation = PasswordVisualTransformation(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password), modifier = Modifier.fillMaxWidth(), colors = fieldColors())
        if (message != null) {
            Spacer(Modifier.height(12.dp))
            Text(message!!, color = if (error) Color(0xFFFF8A80) else Color(0xFF9BE7B0), fontSize = 13.sp)
        }
        Spacer(Modifier.height(22.dp))
        Button(
            enabled = !busy,
            onClick = {
                busy = true; message = null
                scope.launch {
                    if (register) {
                        repository.signUp(email, password, displayName).onSuccess { created ->
                            if (created == null) message = "Cuenta creada. Revisa tu correo para confirmar y luego inicia sesión."
                            else { message = "Cuenta creada correctamente"; onLoginSuccess() }
                        }.onFailure { error = true; message = it.localizedMessage ?: "No se pudo crear la cuenta" }
                    } else {
                        repository.signIn(email, password).onSuccess { onLoginSuccess() }
                            .onFailure { error = true; message = it.localizedMessage ?: "No se pudo iniciar sesión" }
                    }
                    busy = false
                }
            },
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(28.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.Black)
        ) {
            if (busy) CircularProgressIndicator(Modifier.size(20.dp), color = Color.Black, strokeWidth = 2.dp)
            else Text(if (register) "Crear cuenta" else "Iniciar sesión", fontWeight = FontWeight.SemiBold)
        }
        TextButton(onClick = { register = !register; message = null; error = false }) {
            Text(if (register) "Ya tengo una cuenta · Iniciar sesión" else "¿No tienes cuenta? Regístrate", color = Color.White.copy(.8f))
        }
        Spacer(Modifier.height(10.dp))
        OutlinedButton(onClick = onContinueAsGuest, modifier = Modifier.fillMaxWidth().height(50.dp), shape = RoundedCornerShape(26.dp), colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)) {
            Text("Continuar como invitado")
        }
    }
}

@Composable
private fun fieldColors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = Color.White, unfocusedTextColor = Color.White,
    focusedBorderColor = Color.White, unfocusedBorderColor = Color.White.copy(.25f),
    focusedLabelColor = Color.White, unfocusedLabelColor = Color.White.copy(.55f),
    cursorColor = Color.White
)
