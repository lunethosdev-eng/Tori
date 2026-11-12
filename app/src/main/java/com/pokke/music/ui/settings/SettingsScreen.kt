package com.pokke.music.ui.settings

import com.pokke.music.player.PlayerController
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import com.pokke.music.data.preferences.UserPreferences
import com.pokke.music.ui.settings.ExternalAccountsSection
import com.pokke.music.data.repository.AuthRepository
import kotlinx.coroutines.launch
import com.pokke.music.ui.liquid.LocalAppBackdrop
import com.pokke.music.ui.liquid.LiquidToggle

private const val OWNER_EMAIL = "lunethos.dev@gmail.com"

@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onOpenAdmin: (() -> Unit)? = null,
    authRepository: AuthRepository? = null
) {
    val context = LocalContext.current
    val prefs = remember(context) { UserPreferences(context) }
    val scope = rememberCoroutineScope()
    val glass by prefs.liquidGlassEnabled.collectAsState(initial = false)
    val blur by prefs.blurPlayerBackground.collectAsState(initial = true)
    val accent by prefs.accentColor.collectAsState(initial = "white")
    val theme by prefs.appTheme.collectAsState(initial = "dark")
    val gapless by prefs.gaplessPlayback.collectAsState(initial = true)
    val showListening by prefs.showListeningOnProfile.collectAsState(initial = true)
    val animated by prefs.autoAnimatedCovers.collectAsState(initial = true)
    val liquidSlider by prefs.liquidSliderEnabled.collectAsState(initial = true)
    val performance by prefs.liquidPerformanceMode.collectAsState(initial = true)
    val chatsEnabled by prefs.experimentalChatsEnabled.collectAsState(initial = false)
    val mapsEnabled by prefs.experimentalMapsEnabled.collectAsState(initial = false)

    val session by (authRepository?.session ?: kotlinx.coroutines.flow.MutableStateFlow(null)).collectAsState()
    val isOwner = session?.email?.equals(OWNER_EMAIL, ignoreCase = true) == true


    Column(
        Modifier
            .fillMaxSize()
            .background(Color(0xFF09090B))
            .verticalScroll(rememberScrollState())
            .statusBarsPadding()
    ) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White)
            }
            Column {
                Text("Ajustes", color = Color.White, fontSize = 23.sp, fontWeight = FontWeight.Bold)
                Text(
                    if (glass) "Liquid Glass activo · Backdrop vendored" else "Preferencias del dispositivo",
                    color = Color.White.copy(.5f),
                    fontSize = 12.sp
                )
            }
        }

        Section("APARIENCIA")
        LiquidOrMaterialToggle(
            title = "Liquid Glass",
            subtitle = "Backdrop liquid glass (código fuente vendored, sin AAR). Apagado por defecto.",
            checked = glass,
            glass = false,
            onChange = { scope.launch { prefs.setLiquidGlass(it) } }
        )
        LiquidOrMaterialToggle(
            title = "Fondo desenfocado en el reproductor",
            subtitle = "Portada como fondo ambiental del player.",
            checked = blur,
            glass = false,
            onChange = { scope.launch { prefs.setBlurPlayerBackground(it) } }
        )
        LiquidOrMaterialToggle(
            title = "Portadas animadas",
            subtitle = "Prefiere animated_cover cuando exista.",
            checked = animated,
            glass = false,
            onChange = { scope.launch { prefs.setAutoAnimatedCovers(it) } }
        )
        LiquidOrMaterialToggle(
            title = "Liquid Slider",
            subtitle = "Sliders con vidrio líquido (opcional, más costo de GPU).",
            checked = liquidSlider,
            glass = false,
            onChange = { scope.launch { prefs.setLiquidSlider(it) } }
        )

        if (glass) {
            LiquidOrMaterialToggle(
                title = "Modo rendimiento",
                subtitle = "Prioriza efectos solo en la pantalla visible y reduce capas y trabajo gráfico fuera de ella.",
                checked = performance,
                glass = false,
                onChange = { scope.launch { prefs.setLiquidPerformanceMode(it) } }
            )
            Text("El modo rendimiento evita mantener capturas y efectos de cristal en pantallas que no están visibles.", color = Color.White.copy(.42f), fontSize = 11.sp, modifier = Modifier.padding(horizontal = 22.dp, vertical = 4.dp))
        }


        Section("PERSONALIZAR INICIO")
        Text(
            "Controla qué secciones aparecen en la pantalla principal y su densidad. Los cambios se guardan y se aplican al volver a Inicio.",
            color = Color.White.copy(.5f),
            fontSize = 12.sp,
            modifier = Modifier.padding(horizontal = 22.dp, vertical = 6.dp)
        )
        val showNowPlaying by prefs.homeShowNowPlaying.collectAsState(initial = true)
        val showQuick by prefs.homeShowQuickAccess.collectAsState(initial = true)
        val showRecent by prefs.homeShowRecent.collectAsState(initial = true)
        val showReco by prefs.homeShowRecommendations.collectAsState(initial = true)
        val showLib by prefs.homeShowLibraryShortcuts.collectAsState(initial = true)
        val homeLayout by prefs.homeLayout.collectAsState(initial = "comfortable")
        val homeDensity by prefs.homeDensity.collectAsState(initial = "medium")

        LiquidOrMaterialToggle(
            title = "Mostrar «Reproduciendo»",
            subtitle = "Tarjeta de la canción actual en el Home.",
            checked = showNowPlaying,
            glass = false,
            onChange = { scope.launch { prefs.setHomeShowNowPlaying(it) } }
        )
        LiquidOrMaterialToggle(
            title = "Accesos rápidos",
            subtitle = "Chips de aleatorio, favoritos, biblioteca y búsqueda.",
            checked = showQuick,
            glass = false,
            onChange = { scope.launch { prefs.setHomeShowQuickAccess(it) } }
        )
        LiquidOrMaterialToggle(
            title = "Recientes",
            subtitle = "Fila horizontal de canciones recientes.",
            checked = showRecent,
            glass = false,
            onChange = { scope.launch { prefs.setHomeShowRecent(it) } }
        )
        LiquidOrMaterialToggle(
            title = "Para ti / recomendaciones",
            subtitle = "Lista de sugerencias basada en recientes.",
            checked = showReco,
            glass = false,
            onChange = { scope.launch { prefs.setHomeShowRecommendations(it) } }
        )
        LiquidOrMaterialToggle(
            title = "Atajos de biblioteca",
            subtitle = "Accesos a me gusta, playlists y recientes.",
            checked = showLib,
            glass = false,
            onChange = { scope.launch { prefs.setHomeShowLibraryShortcuts(it) } }
        )

        // Layout density
        Text("Distribución del Home", color = Color.White.copy(.7f), fontSize = 13.sp, fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(start = 22.dp, top = 12.dp, bottom = 6.dp))
        Row(Modifier.padding(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("compact" to "Compacta", "comfortable" to "Cómoda", "dense" to "Densa").forEach { (id, label) ->
                val selected = homeLayout == id
                Text(
                    label,
                    color = if (selected) Color.Black else Color.White,
                    fontSize = 12.sp,
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (selected) Color.White else Color.White.copy(0.12f))
                        .clickable { scope.launch { prefs.setHomeLayout(id) } }
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                )
            }
        }
        Text("Tamaño de carátulas", color = Color.White.copy(.7f), fontSize = 13.sp, fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(start = 22.dp, top = 12.dp, bottom = 6.dp))
        Row(Modifier.padding(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("small" to "Pequeño", "medium" to "Medio", "large" to "Grande").forEach { (id, label) ->
                val selected = homeDensity == id
                Text(
                    label,
                    color = if (selected) Color.Black else Color.White,
                    fontSize = 12.sp,
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (selected) Color.White else Color.White.copy(0.12f))
                        .clickable { scope.launch { prefs.setHomeDensity(id) } }
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                )
            }
        }
        TextButton(
            onClick = { scope.launch { prefs.resetHomeCustomization() } },
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Text("Restaurar distribución predeterminada")
        }

        Section("FUENTES Y CATÁLOGO")
        val ytCatalog by prefs.youtubeCatalogEnabled.collectAsState(initial = false)
        LiquidOrMaterialToggle(
            title = "Catálogo YouTube Music / InnerTube",
            subtitle = "Permite buscar y reproducir desde YouTube Music. Independiente del inicio de sesión. Si se desactiva, Pokke usa solo las demás fuentes.",
            checked = ytCatalog,
            glass = false,
            onChange = { scope.launch { prefs.setYoutubeCatalogEnabled(it) } }
        )


        Section("CUENTAS EXTERNAS")
        ExternalAccountsSection(prefs)

        Text(
            "Tema",
            color = Color.White.copy(.75f),
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(start = 22.dp, top = 8.dp, bottom = 8.dp)
        )
        Row(Modifier.fillMaxWidth().padding(horizontal = 22.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ChoiceChip("Oscuro", theme == "dark") { scope.launch { prefs.setAppTheme("dark") } }
            ChoiceChip("Claro", theme == "light") { scope.launch { prefs.setAppTheme("light") } }
            ChoiceChip("Sistema", theme == "system") { scope.launch { prefs.setAppTheme("system") } }
        }

        Text(
            "Color de acento",
            color = Color.White.copy(.75f),
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(start = 22.dp, top = 16.dp, bottom = 8.dp)
        )
        Row(Modifier.fillMaxWidth().padding(horizontal = 22.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(
                "white" to Color.White,
                "violet" to Color(0xFFB69CFF),
                "blue" to Color(0xFF83B9FF),
                "pink" to Color(0xFFFFA6D2),
                "green" to Color(0xFF9DDBB2)
            ).forEach { (id, color) ->
                Box(
                    Modifier
                        .weight(1f)
                        .height(42.dp)
                        .background(color, RoundedCornerShape(12.dp))
                        .clickable { scope.launch { prefs.setAccentColor(id) } },
                    contentAlignment = Alignment.Center
                ) {
                    if (accent == id) Text("✓", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            }
        }

        Section("PRIVACIDAD Y PERFIL")
        LiquidOrMaterialToggle(
            title = "Mostrar lo que escucho",
            subtitle = "Otros pueden ver la canción actual en tu perfil público.",
            checked = showListening,
            glass = false,
            onChange = { scope.launch { prefs.setShowListeningOnProfile(it) } }
        )

        Section("REPRODUCCIÓN")
        LiquidOrMaterialToggle(
            title = "Reproducción gapless",
            subtitle = "Intenta encadenar pistas sin silencio.",
            checked = gapless,
            glass = false,
            onChange = { scope.launch { prefs.setGaplessPlayback(it); PlayerController.setGapless(it) } }
        )

        Section("FUNCIONES EXPERIMENTALES")
        Text("Funciones alfa: pueden cambiar y requieren la migración SQL correspondiente en Supabase.", color = Color.White.copy(.48f), fontSize = 12.sp, modifier = Modifier.padding(horizontal = 22.dp, vertical = 4.dp))
        LiquidOrMaterialToggle(
            title = "Chats · Alfa 1.0",
            subtitle = "Conversaciones entre amigos aceptados. Activa el acceso experimental.",
            checked = chatsEnabled,
            glass = false,
            onChange = { scope.launch { prefs.setExperimentalChats(it) } }
        )
        LiquidOrMaterialToggle(
            title = "Mapas con amigos · Alfa 1.0",
            subtitle = "Requiere Chats. La ubicación se comparte solo después de conceder permiso y elegir amigos.",
            checked = mapsEnabled,
            glass = false,
            onChange = { if (chatsEnabled) scope.launch { prefs.setExperimentalMaps(it) } },
        )
        if (!chatsEnabled && mapsEnabled) {
            Text("Activa Chats antes de habilitar Mapas.", color = Color(0xFFE5C17A), fontSize = 12.sp, modifier = Modifier.padding(horizontal = 22.dp))
        }

        if (isOwner && onOpenAdmin != null) {
            Section("OWNER")
            Button(
                onClick = onOpenAdmin,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 22.dp, vertical = 8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFE5C17A),
                    contentColor = Color.Black
                )
            ) {
                Text("Admin Panel (Owner)", fontWeight = FontWeight.Bold)
            }
        }

        Spacer(Modifier.height(100.dp))
    }
}

@Composable
private fun LiquidOrMaterialToggle(
    title: String,
    subtitle: String,
    checked: Boolean,
    glass: Boolean,
    onChange: (Boolean) -> Unit
) {
    val appBackdrop = LocalAppBackdrop.current
    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 22.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f).clickable { onChange(!checked) }) {
            Text(title, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Medium)
            Text(
                subtitle,
                color = Color.White.copy(.5f),
                fontSize = 12.sp,
                lineHeight = 17.sp,
                modifier = Modifier.padding(top = 3.dp)
            )
        }
        if (glass && appBackdrop != null) {
            LiquidToggle(selected = { checked }, onSelect = onChange, backdrop = appBackdrop, modifier = Modifier.size(width = 54.dp, height = 34.dp))
        } else {
            Switch(
                checked = checked,
                onCheckedChange = onChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.Black,
                    checkedTrackColor = Color.White,
                    uncheckedTrackColor = Color(0xFF333338)
                )
            )
        }
    }
}

@Composable
private fun Section(text: String) {
    Text(
        text,
        color = Color.White.copy(.45f),
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.4.sp,
        modifier = Modifier.padding(start = 22.dp, top = 22.dp, bottom = 8.dp)
    )
}

@Composable
private fun RowScope.ChoiceChip(label: String, selected: Boolean, onClick: () -> Unit) {
    Box(
        Modifier
            .weight(1f)
            .background(if (selected) Color.White else Color(0xFF202024), RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(label, color = if (selected) Color.Black else Color.White, fontSize = 12.sp)
    }
}
