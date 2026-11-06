package com.pokke.music.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pokke.music.data.repository.AuthRepository
import kotlinx.coroutines.launch

private const val OWNER_EMAIL = "lunethos.dev@gmail.com"
private const val OWNER_PIN = "4049959"

@Composable
fun LoginScreen(
    repository: AuthRepository,
    onLoginSuccess: () -> Unit,
    onContinueAsGuest: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var displayName by remember { mutableStateOf("") }
    var register by remember { mutableStateOf(false) }
    var busy by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf<String?>(null) }
    var error by remember { mutableStateOf(false) }

    // Owner PIN gate (only for owner email after password auth)
    var needOwnerPin by remember { mutableStateOf(false) }
    var ownerPin by remember { mutableStateOf("") }

    Column(
        Modifier
            .fillMaxSize()
            .background(Color(0xFF09090B))
            .verticalScroll(rememberScrollState())
            .statusBarsPadding()
            .padding(horizontal = 24.dp, vertical = 28.dp)
    ) {
        Text("Pokke", color = Color.White, fontSize = 34.sp, fontWeight = FontWeight.Bold)
        Text(
            if (needOwnerPin) "Verificación Owner"
            else if (register) "Crear cuenta"
            else "Iniciar sesión",
            color = Color.White.copy(.65f),
            fontSize = 15.sp,
            modifier = Modifier.padding(top = 6.dp, bottom = 28.dp)
        )

        if (needOwnerPin) {
            Text(
                "Esta cuenta requiere PIN de Owner. Solo el dueño puede completar el acceso.",
                color = Color.White.copy(.55f),
                fontSize = 13.sp,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            OutlinedTextField(
                value = ownerPin,
                onValueChange = { ownerPin = it.filter { ch -> ch.isDigit() }.take(12) },
                label = { Text("PIN") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                colors = fieldColors()
            )
            if (message != null) {
                Spacer(Modifier.height(10.dp))
                Text(message!!, color = if (error) Color(0xFFFF8A80) else Color(0xFF9BE7B0), fontSize = 13.sp)
            }
            Spacer(Modifier.height(18.dp))
            Button(
                enabled = !busy,
                onClick = {
                    if (ownerPin == OWNER_PIN) {
                        needOwnerPin = false
                        onLoginSuccess()
                    } else {
                        error = true
                        message = "PIN incorrecto"
                        // Sign out so non-owners can't stay half-logged
                        scope.launch { repository.signOut() }
                        needOwnerPin = false
                        ownerPin = ""
                    }
                },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE5C17A), contentColor = Color.Black)
            ) { Text("Verificar PIN", fontWeight = FontWeight.Bold) }
            TextButton(onClick = {
                scope.launch { repository.signOut() }
                needOwnerPin = false
                ownerPin = ""
                message = null
            }) { Text("Cancelar", color = Color.White.copy(.7f)) }
            return
        }

        if (register) {
            OutlinedTextField(
                value = displayName, onValueChange = { displayName = it },
                label = { Text("Nombre visible") }, singleLine = true,
                modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp), colors = fieldColors()
            )
        }
        OutlinedTextField(
            value = email, onValueChange = { email = it },
            label = { Text("Email") }, singleLine = true,
            modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp), colors = fieldColors()
        )
        OutlinedTextField(
            value = password, onValueChange = { password = it },
            label = { Text("Contraseña") }, singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(), colors = fieldColors()
        )
        if (message != null) {
            Spacer(Modifier.height(12.dp))
            Text(message!!, color = if (error) Color(0xFFFF8A80) else Color(0xFF9BE7B0), fontSize = 13.sp)
        }
        Spacer(Modifier.height(22.dp))
        Button(
            enabled = !busy,
            onClick = {
                busy = true; message = null; error = false
                scope.launch {
                    if (register) {
                        repository.signUp(email, password, displayName).onSuccess { created ->
                            if (created == null) message = "Cuenta creada. Revisa tu correo para confirmar y luego inicia sesión."
                            else {
                                // Owner signup still needs PIN to enter app
                                if (email.trim().equals(OWNER_EMAIL, ignoreCase = true)) {
                                    needOwnerPin = true
                                    message = null
                                } else {
                                    message = "Cuenta creada correctamente"
                                    onLoginSuccess()
                                }
                            }
                        }.onFailure { error = true; message = it.localizedMessage ?: "No se pudo crear la cuenta" }
                    } else {
                        repository.signIn(email, password).onSuccess {
                            if (email.trim().equals(OWNER_EMAIL, ignoreCase = true)) {
                                needOwnerPin = true
                            } else onLoginSuccess()
                        }.onFailure { error = true; message = it.localizedMessage ?: "No se pudo iniciar sesión" }
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
            Text(
                if (register) "Ya tengo una cuenta · Iniciar sesión" else "¿No tienes cuenta? Regístrate",
                color = Color.White.copy(.8f)
            )
        }
        Spacer(Modifier.height(10.dp))
        OutlinedButton(
            onClick = onContinueAsGuest,
            modifier = Modifier.fillMaxWidth().height(50.dp),
            shape = RoundedCornerShape(26.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
        ) { Text("Continuar como invitado") }
    }
}

@Composable
private fun fieldColors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = Color.White, unfocusedTextColor = Color.White,
    focusedBorderColor = Color.White, unfocusedBorderColor = Color.White.copy(.25f),
    focusedLabelColor = Color.White, unfocusedLabelColor = Color.White.copy(.55f),
    cursorColor = Color.White
)
