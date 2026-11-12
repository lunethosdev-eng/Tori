package com.pokke.music.tv.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.pokke.music.data.repository.AuthRepository

@Composable
fun TvProfileMenuScreen(
    auth: AuthRepository,
    onAuth: () -> Unit,
    onLinkDevice: () -> Unit,
    onBack: () -> Unit
) {
    val session by auth.session.collectAsState()
    var profile by remember { mutableStateOf<com.pokke.music.data.model.UserProfile?>(null) }
    LaunchedEffect(session) {
        if (session != null) auth.getProfile().onSuccess { profile = it }
    }

    Column(Modifier.fillMaxSize()) {
        Box(
            Modifier
                .fillMaxWidth()
                .height(200.dp)
                .background(Brush.horizontalGradient(listOf(Color(0xFF2A1848), Color(0xFF12121A))))
        ) {
            profile?.bannerUrl?.let {
                AsyncImage(it, null, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                Box(Modifier.fillMaxSize().background(Color.Black.copy(0.45f)))
            }
            Row(Modifier.align(Alignment.BottomStart).padding(36.dp), verticalAlignment = Alignment.CenterVertically) {
                AsyncImage(
                    profile?.avatarUrl, null,
                    Modifier.size(72.dp).background(Color(0xFF333), CircleShape),
                    contentScale = ContentScale.Crop
                )
                Spacer(Modifier.width(16.dp))
                Column {
                    Text(profile?.displayName ?: "Invitado", color = Color.White, fontSize = 26.sp, fontWeight = FontWeight.Bold)
                    Text("@" + (profile?.username ?: "—"), color = Color(0xFFC8C8D0))
                }
            }
        }
        val items = listOf(
            "👤  Ver Perfil" to { },
            "🔒  Privacidad y Seguridad" to { },
            "🔑  Cambiar Contraseña" to { },
            "🔗  Vincular Dispositivo" to onLinkDevice,
            "⚙️  Configuración" to { },
            if (session == null) "➡️  Iniciar sesión" to onAuth else "🚪  Cerrar sesión" to { }
        )
        Spacer(Modifier.height(16.dp))
        items.forEach { (label, action) ->
            var focused by remember { mutableStateOf(false) }
            Box(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 36.dp, vertical = 6.dp)
                    .onFocusChanged { focused = it.isFocused }
                    .background(if (focused) Color.White.copy(0.12f) else Color.Transparent, RoundedCornerShape(12.dp))
                    .border(if (focused) 2.dp else 0.dp, Color.White, RoundedCornerShape(12.dp))
                    .padding(horizontal = 20.dp, vertical = 16.dp)
            ) {
                Text(label, color = Color.White, fontSize = 18.sp)
            }
        }
    }
}
