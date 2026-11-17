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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.pokke.music.data.repository.AuthRepository

/**
 * Menú de perfil en TV — limpio, sin emojis, con foco legible para D-pad.
 */
@Composable
fun TvProfileMenuScreen(
    auth: AuthRepository,
    onLinkDevice: () -> Unit,
    onAuth: () -> Unit,
    onBack: () -> Unit = {}
) {
    val session by auth.session.collectAsState()
    var profile by remember { mutableStateOf<com.pokke.music.data.model.UserProfile?>(null) }
    LaunchedEffect(session) {
        if (session != null) auth.getProfile().onSuccess { profile = it }
    }

    Column(Modifier.fillMaxSize().background(Color(0xFF0A0A0C))) {
        Box(
            Modifier
                .fillMaxWidth()
                .height(220.dp)
                .background(Brush.horizontalGradient(listOf(Color(0xFF1A1028), Color(0xFF0E0E14))))
        ) {
            profile?.bannerUrl?.let {
                AsyncImage(it, null, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                Box(Modifier.fillMaxSize().background(Color.Black.copy(0.5f)))
            }
            Row(
                Modifier.align(Alignment.BottomStart).padding(40.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AsyncImage(
                    model = profile?.avatarUrl,
                    contentDescription = null,
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF2A2A32)),
                    contentScale = ContentScale.Crop
                )
                Spacer(Modifier.width(20.dp))
                Column {
                    Text(
                        profile?.displayName ?: "Invitado",
                        color = Color.White,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "@" + (profile?.username ?: "—"),
                        color = Color.White.copy(0.65f),
                        fontSize = 16.sp
                    )
                }
            }
        }

        val items = buildList {
            add("Vincular dispositivo" to onLinkDevice)
            if (session == null) add("Iniciar sesión" to onAuth)
            else add("Cerrar sesión" to { /* handled by caller if needed */ onAuth() })
        }

        Spacer(Modifier.height(24.dp))
        items.forEach { (label, action) ->
            var focused by remember { mutableStateOf(false) }
            Box(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 40.dp, vertical = 8.dp)
                    .onFocusChanged { focused = it.isFocused }
                    .clip(RoundedCornerShape(14.dp))
                    .background(if (focused) Color.White.copy(0.14f) else Color(0xFF16161C))
                    .border(
                        width = if (focused) 2.dp else 1.dp,
                        color = if (focused) Color.White else Color.White.copy(0.08f),
                        shape = RoundedCornerShape(14.dp)
                    )
                    .padding(horizontal = 24.dp, vertical = 18.dp)
            ) {
                Text(label, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Medium)
            }
        }
    }
}
