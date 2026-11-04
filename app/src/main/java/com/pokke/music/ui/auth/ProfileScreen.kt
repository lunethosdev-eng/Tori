package com.pokke.music.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.pokke.music.data.repository.AuthRepository
import kotlinx.coroutines.launch

@Composable
fun ProfileScreen(repository: AuthRepository, onBack: () -> Unit, onSignedOut: () -> Unit) {
    val session by repository.session.collectAsState()
    val scope = rememberCoroutineScope()
    var displayName by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var bio by remember { mutableStateOf("") }
    var avatar by remember { mutableStateOf("") }
    var banner by remember { mutableStateOf("") }
    var theme by remember { mutableStateOf("midnight") }
    var loading by remember { mutableStateOf(true) }
    var saving by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(session?.userId) {
        loading = true
        repository.getProfile().onSuccess { p ->
            displayName = p?.displayName.orEmpty(); username = p?.username.orEmpty(); bio = p?.bio.orEmpty()
            avatar = p?.avatarUrl.orEmpty(); banner = p?.bannerUrl.orEmpty(); theme = p?.theme ?: "midnight"
        }.onFailure { message = it.localizedMessage }
        loading = false
    }

    val profileBackground = when (theme) {
        "ocean" -> Color(0xFF07151D)
        "violet" -> Color(0xFF17101F)
        "sunset" -> Color(0xFF21120F)
        else -> Color(0xFF09090B)
    }
    Column(Modifier.fillMaxSize().background(profileBackground).verticalScroll(rememberScrollState())) {
        Box(Modifier.fillMaxWidth().height(210.dp)) {
            if (banner.isNotBlank()) AsyncImage(banner, null, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
            else Box(Modifier.fillMaxSize().background(androidx.compose.ui.graphics.Brush.linearGradient(listOf(Color(0xFF373044), Color(0xFF101014)))))
            Box(Modifier.fillMaxSize().background(Color.Black.copy(.32f)))
            IconButton(onClick = onBack, modifier = Modifier.align(Alignment.TopStart).statusBarsPadding().padding(start = 10.dp)) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver", tint = Color.White)
            }
            IconButton(onClick = { scope.launch { repository.signOut(); onSignedOut() } }, modifier = Modifier.align(Alignment.TopEnd).statusBarsPadding().padding(end = 10.dp)) {
                Icon(Icons.Default.Logout, "Cerrar sesión", tint = Color.White)
            }
            Box(Modifier.align(Alignment.BottomStart).padding(start = 24.dp, bottom = 18.dp).size(82.dp).clip(CircleShape).background(Color(0xFF25252A)), contentAlignment = Alignment.Center) {
                if (avatar.isNotBlank()) AsyncImage(avatar, "Foto de perfil", Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                else Text("${displayName.firstOrNull()?.uppercaseChar() ?: 'P'}", color = Color.White, fontSize = 30.sp, fontWeight = FontWeight.Bold)
            }
        }
        Column(Modifier.padding(horizontal = 22.dp, vertical = 18.dp)) {
            Text("Tu perfil", color = Color.White, fontSize = 27.sp, fontWeight = FontWeight.Bold)
            Text(session?.email.orEmpty(), color = Color.White.copy(.55f), fontSize = 13.sp, modifier = Modifier.padding(top = 3.dp, bottom = 22.dp))
            Text("PERSONALIZACIÓN", color = Color.White.copy(.55f), fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.5.sp)
            Spacer(Modifier.height(12.dp))
            ProfileField("Nombre", displayName) { displayName = it }
            ProfileField("Nombre de usuario", username) { username = it }
            ProfileField("Biografía", bio, singleLine = false) { bio = it }
            ProfileField("URL de foto de perfil", avatar) { avatar = it }
            ProfileField("URL del banner", banner) { banner = it }
            Text("Tema del perfil", color = Color.White.copy(.7f), fontSize = 13.sp, modifier = Modifier.padding(top = 8.dp, bottom = 8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("midnight", "ocean", "violet", "sunset").forEach { value ->
                    val selected = theme == value
                    Box(Modifier.weight(1f).clip(RoundedCornerShape(13.dp)).background(if (selected) Color(0xFF45404F) else Color(0xFF1B1B20)).clickable { theme = value }.padding(vertical = 12.dp), contentAlignment = Alignment.Center) {
                        Text(value.replaceFirstChar { it.uppercase() }, color = Color.White, fontSize = 11.sp, fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal)
                    }
                }
            }
            if (message != null) Text(message!!, color = Color(0xFFFFA6A6), fontSize = 12.sp, modifier = Modifier.padding(top = 12.dp))
            Spacer(Modifier.height(20.dp))
            Button(onClick = {
                saving = true; message = null
                scope.launch {
                    repository.updateProfile(displayName, username, bio, avatar, banner, theme)
                        .onSuccess { message = "Perfil guardado correctamente" }
                        .onFailure { message = it.localizedMessage ?: "No se pudo guardar" }
                    saving = false
                }
            }, enabled = !loading && !saving, modifier = Modifier.fillMaxWidth().height(54.dp), shape = RoundedCornerShape(28.dp), colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.Black)) {
                if (saving) CircularProgressIndicator(Modifier.size(18.dp), color = Color.Black, strokeWidth = 2.dp) else Text("Guardar cambios", fontWeight = FontWeight.SemiBold)
            }
            Spacer(Modifier.height(28.dp))
        }
    }
}

@Composable
private fun ProfileField(label: String, value: String, singleLine: Boolean = true, onChange: (String) -> Unit) {
    OutlinedTextField(value, onChange, label = { Text(label) }, singleLine = singleLine, minLines = if (singleLine) 1 else 3,
        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp), shape = RoundedCornerShape(15.dp),
        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White,
            focusedBorderColor = Color.White.copy(.8f), unfocusedBorderColor = Color.White.copy(.18f),
            focusedLabelColor = Color.White, unfocusedLabelColor = Color.White.copy(.55f), cursorColor = Color.White))
}
