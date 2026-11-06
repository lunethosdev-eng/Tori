package com.pokke.music.ui.social

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color as AColor
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.pokke.music.data.repository.AuthRepository
import com.pokke.music.data.repository.SocialRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.maplibre.android.MapLibre
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.Style
import org.maplibre.android.annotations.MarkerOptions

/**
 * Free/open map via MapLibre (no Google API key / no card).
 * Default style: MapLibre demotiles (works offline-demo style JSON).
 * Location share is OFF until the user picks a mode + grants OS permission.
 */
@Composable
fun FriendsMapScreen(authRepository: AuthRepository, onBack: () -> Unit) {
    val context = LocalContext.current
    val social = remember(authRepository) { SocialRepository(authRepository) }
    val scope = rememberCoroutineScope()
    var locations by remember { mutableStateOf<List<SocialRepository.FriendLocation>>(emptyList()) }
    var shareMode by remember { mutableStateOf("none") }
    var shareMusic by remember { mutableStateOf(false) }
    var shareBattery by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var mapReady by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { /* user response; publish only if granted and mode != none */ }

    fun refresh() {
        scope.launch {
            social.friendLocations()
                .onSuccess { locations = it }
                .onFailure { error = it.message }
        }
    }

    LaunchedEffect(Unit) {
        runCatching { MapLibre.getInstance(context) }
        refresh()
        while (true) {
            delay(20_000)
            refresh()
        }
    }

    Column(
        Modifier
            .fillMaxSize()
            .background(Color(0xFF09090B))
            .statusBarsPadding()
    ) {
        Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White)
            }
            Column(Modifier.weight(1f)) {
                Text("Mapas con amigos", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                Text("MapLibre · sin Google · ubicación off por defecto", color = Color.White.copy(.5f), fontSize = 12.sp)
            }
        }

        // Visual map (free tiles)
        Box(
            Modifier
                .fillMaxWidth()
                .height(280.dp)
                .padding(horizontal = 12.dp)
                .background(Color(0xFF121218), RoundedCornerShape(16.dp))
        ) {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { ctx ->
                    MapView(ctx).apply {
                        onCreate(null)
                        getMapAsync { map ->
                            map.setStyle(
                                Style.Builder().fromUri("https://demotiles.maplibre.org/style.json")
                            ) {
                                map.cameraPosition = CameraPosition.Builder()
                                    .target(LatLng(13.6929, -89.2182)) // San Salvador default
                                    .zoom(11.0)
                                    .tilt(45.0) // 3D-ish pitch
                                    .build()
                                mapReady = true
                                locations.forEach { loc ->
                                    map.addMarker(
                                        MarkerOptions()
                                            .position(LatLng(loc.lat, loc.lng))
                                            .title(loc.displayName ?: loc.username ?: "Amigo")
                                    )
                                }
                            }
                        }
                        onStart(); onResume()
                    }
                },
                update = { view ->
                    view.getMapAsync { map ->
                        map.clear()
                        locations.forEach { loc ->
                            if (loc.lat != 0.0 || loc.lng != 0.0) {
                                map.addMarker(
                                    MarkerOptions()
                                        .position(LatLng(loc.lat, loc.lng))
                                        .title(loc.displayName ?: loc.username ?: "Amigo")
                                )
                            }
                        }
                    }
                }
            )
            if (!mapReady) {
                Text(
                    "Cargando mapa abierto…",
                    color = Color.White.copy(.6f),
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }

        Card(
            Modifier.fillMaxWidth().padding(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF16161A)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(Modifier.padding(14.dp)) {
                Text("Privacidad", color = Color.White, fontWeight = FontWeight.SemiBold)
                listOf(
                    "none" to "No compartir",
                    "all_friends" to "Todos mis amigos",
                    "selected" to "Solo seleccionados"
                ).forEach { (id, label) ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(
                            selected = shareMode == id,
                            onClick = {
                                shareMode = id
                                scope.launch {
                                    if (id == "none") social.stopSharingLocation()
                                    else {
                                        val fine = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                                        if (fine != PackageManager.PERMISSION_GRANTED) {
                                            permissionLauncher.launch(
                                                arrayOf(
                                                    Manifest.permission.ACCESS_FINE_LOCATION,
                                                    Manifest.permission.ACCESS_COARSE_LOCATION
                                                )
                                            )
                                        }
                                    }
                                }
                            },
                            colors = RadioButtonDefaults.colors(selectedColor = Color(0xFFE5C17A))
                        )
                        Text(label, color = Color.White, fontSize = 14.sp)
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Switch(shareMusic, { shareMusic = it }, enabled = shareMode != "none")
                    Text("Compartir música", color = Color.White.copy(.8f), fontSize = 13.sp)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Switch(shareBattery, { shareBattery = it }, enabled = shareMode != "none")
                    Text("Compartir batería", color = Color.White.copy(.8f), fontSize = 13.sp)
                }
            }
        }

        error?.let {
            Text(it, color = Color(0xFFFF8A80), fontSize = 12.sp, modifier = Modifier.padding(horizontal = 16.dp))
        }

        Text(
            "Amigos visibles (${locations.size})",
            color = Color.White.copy(.65f),
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
        )
        LazyColumn(Modifier.fillMaxSize().padding(horizontal = 12.dp)) {
            items(locations, key = { it.userId }) { loc ->
                Column(
                    Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                        .background(Color(0xFF16161A), RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    Text(loc.displayName ?: loc.username ?: loc.userId.take(8), color = Color.White, fontWeight = FontWeight.Medium)
                    Text("lat ${"%.4f".format(loc.lat)} · lng ${"%.4f".format(loc.lng)}", color = Color.White.copy(.55f), fontSize = 12.sp)
                }
            }
        }
    }
}
