package com.pokke.music.ui.social

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pokke.music.data.repository.AuthRepository
import com.pokke.music.data.repository.SocialRepository
import kotlinx.coroutines.launch

/**
 * Friend locations list (map canvas can be layered with Google Maps later).
 * Location share is OFF by default until the user enables a mode.
 */
@Composable
fun FriendsMapScreen(authRepository: AuthRepository, onBack: () -> Unit) {
    val social = remember(authRepository) { SocialRepository(authRepository) }
    val scope = rememberCoroutineScope()
    var locations by remember { mutableStateOf<List<SocialRepository.FriendLocation>>(emptyList()) }
    var shareMode by remember { mutableStateOf("none") } // none | all_friends | selected
    var shareMusic by remember { mutableStateOf(false) }
    var shareBattery by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(false) }

    fun refresh() {
        scope.launch {
            loading = true
            social.friendLocations()
                .onSuccess { locations = it }
                .onFailure { error = it.message }
            loading = false
        }
    }

    LaunchedEffect(Unit) { refresh() }

    Column(
        Modifier
            .fillMaxSize()
            .background(Color(0xFF09090B))
            .statusBarsPadding()
    ) {
        Row(
            Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White)
            }
            Column(Modifier.weight(1f)) {
                Text("Mapas con amigos", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                Text("Ubicación desactivada por defecto", color = Color.White.copy(.5f), fontSize = 12.sp)
            }
            TextButton(onClick = { refresh() }) { Text("Actualizar", color = Color.White) }
        }

        Card(
            Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF16161A)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(Modifier.padding(16.dp)) {
                Text("Tu privacidad", color = Color.White, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(8.dp))
                listOf(
                    "none" to "No compartir",
                    "all_friends" to "Todos mis amigos",
                    "selected" to "Solo seleccionados"
                ).forEach { (id, label) ->
                    Row(
                        Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = shareMode == id,
                            onClick = {
                                shareMode = id
                                scope.launch {
                                    if (id == "none") social.stopSharingLocation()
                                    // real GPS publish happens when user grants OS permission + we have coords
                                }
                            },
                            colors = RadioButtonDefaults.colors(selectedColor = Color(0xFFE5C17A))
                        )
                        Text(label, color = Color.White, fontSize = 14.sp)
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Switch(shareMusic, { shareMusic = it }, enabled = shareMode != "none")
                    Text("Compartir música actual", color = Color.White.copy(.8f), fontSize = 13.sp)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Switch(shareBattery, { shareBattery = it }, enabled = shareMode != "none")
                    Text("Compartir batería", color = Color.White.copy(.8f), fontSize = 13.sp)
                }
                Text(
                    "La ubicación del sistema solo se publica tras conceder permiso y elegir un modo distinto de “No compartir”.",
                    color = Color.White.copy(.45f),
                    fontSize = 11.sp,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }

        Spacer(Modifier.height(12.dp))
        Text(
            "Amigos visibles (${locations.size})",
            color = Color.White.copy(.7f),
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
        if (loading) {
            Box(Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color.White)
            }
        }
        error?.let {
            Text(it, color = Color(0xFFFF8A80), modifier = Modifier.padding(16.dp), fontSize = 13.sp)
        }
        LazyColumn(Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
            items(locations, key = { it.userId }) { loc ->
                Column(
                    Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .background(Color(0xFF16161A), RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    Text(
                        loc.displayName ?: loc.username ?: loc.userId.take(8),
                        color = Color.White,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        "lat ${"%.4f".format(loc.lat)} · lng ${"%.4f".format(loc.lng)}",
                        color = Color.White.copy(.55f),
                        fontSize = 12.sp
                    )
                    if (loc.shareMusic && !loc.currentTrack.isNullOrBlank()) {
                        Text("♪ ${loc.currentTrack}", color = Color(0xFFE5C17A), fontSize = 12.sp)
                    }
                    if (loc.shareBattery && loc.batteryPct != null) {
                        Text("🔋 ${loc.batteryPct}%", color = Color.White.copy(.7f), fontSize = 12.sp)
                    }
                }
            }
        }
    }
}
