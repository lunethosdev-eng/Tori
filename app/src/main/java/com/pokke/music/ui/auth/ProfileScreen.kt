package com.pokke.music.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pokke.music.data.repository.AuthRepository
import kotlinx.coroutines.launch

@Composable
fun ProfileScreen(repository: AuthRepository, onSignedOut: () -> Unit) {
    val session by repository.session.collectAsState()
    val scope = rememberCoroutineScope()
    var displayName by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var bio by remember { mutableStateOf("") }
    var email by remember { mutableStateOf(session?.email.orEmpty()) }
    var loading by remember { mutableStateOf(true) }
    var saving by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(session?.userId) {
        loading = true
        repository.getProfile().onSuccess { profile ->
            displayName = profile?.displayName.orEmpty()
            username = profile?.username.orEmpty()
            bio = profile?.bio.orEmpty()
        }.onFailure { message = it.localizedMessage }
        loading = false
    }

    Column(Modifier.fillMaxSize().background(Color(0xFF0A0A0A)).verticalScroll(rememberScrollState()).padding(22.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.AccountCircle, null, tint = Color.White, modifier = Modifier.size(42.dp))
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text("Tu perfil", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                Text(email, color = Color.White.copy(.55f), fontSize = 12.sp)
            }
            IconButton(onClick = { scope.launch { repository.signOut(); onSignedOut() } }) {
                Icon(Icons.Default.Logout, "Cerrar sesión", tint = Color.White.copy(.8f))
            }
        }
        Spacer(Modifier.height(24.dp))
        if (loading) LinearProgressIndicator(Modifier.fillMaxWidth(), color = Color.White)
        else {
            ProfileField("Nombre", displayName, { displayName = it })
            ProfileField("Nombre de usuario", username, { username = it })
            ProfileField("Biografía", bio, { bio = it }, singleLine = false)
            if (message != null) Text(message!!, color = Color(0xFFFF8A80), fontSize = 12.sp, modifier = Modifier.padding(top = 8.dp))
            Spacer(Modifier.height(18.dp))
            Button(
                enabled = !saving,
                onClick = {
                    saving = true; message = null
                    scope.launch {
                        repository.updateProfile(displayName, username, bio)
                            .onSuccess { message = "Perfil actualizado" }
                            .onFailure { message = it.localizedMessage ?: "No se pudo guardar" }
                        saving = false
                    }
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(26.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.Black)
            ) {
                if (saving) CircularProgressIndicator(Modifier.size(18.dp), color = Color.Black, strokeWidth = 2.dp) else Text("Guardar perfil")
            }
        }
    }
}

@Composable
private fun ProfileField(label: String, value: String, onChange: (String) -> Unit, singleLine: Boolean = true) {
    OutlinedTextField(
        value = value, onValueChange = onChange, label = { Text(label) }, singleLine = singleLine,
        minLines = if (singleLine) 1 else 3, modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = Color.White, unfocusedTextColor = Color.White,
            focusedBorderColor = Color.White, unfocusedBorderColor = Color.White.copy(.25f),
            focusedLabelColor = Color.White, unfocusedLabelColor = Color.White.copy(.55f), cursorColor = Color.White
        )
    )
}
