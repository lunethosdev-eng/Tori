package com.pokke.music.ui.settings

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
import com.pokke.music.data.repository.AuthRepository
import kotlinx.coroutines.launch

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
                Text("Preferencias que sí se guardan en el dispositivo", color = Color.White.copy(.5f), fontSize = 12.sp)
            }
        }

        Section("APARIENCIA")
        Toggle(
            "Liquid Glass",
            "Efectos de superficie translúcida / blur en controles compatibles (API 31+). Apagado por defecto.",
            glass
        ) { scope.launch { prefs.setLiquidGlass(it) } }
        Toggle(
            "Fondo desenfocado en el reproductor",
            "Usa la portada como fondo ambiental del player.",
            blur
        ) { scope.launch { prefs.setBlurPlayerBackground(it) } }
        Toggle(
            "Portadas animadas",
            "Prefiere animated_cover cuando exista.",
            animated
        ) { scope.launch { prefs.setAutoAnimatedCovers(it) } }

        Text("Tema", color = Color.White.copy(.75f), fontSize = 14.sp, fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(start = 22.dp, top = 8.dp, bottom = 8.dp))
        Row(Modifier.fillMaxWidth().padding(horizontal = 22.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ChoiceChip("Oscuro", theme == "dark") { scope.launch { prefs.setAppTheme("dark") } }
            ChoiceChip("Claro", theme == "light") { scope.launch { prefs.setAppTheme("light") } }
            ChoiceChip("Sistema", theme == "system") { scope.launch { prefs.setAppTheme("system") } }
        }

        Text("Color de acento", color = Color.White.copy(.75f), fontSize = 14.sp, fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(start = 22.dp, top = 16.dp, bottom = 8.dp))
        Row(Modifier.fillMaxWidth().padding(horizontal = 22.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(
                "white" to Color.White,
                "violet" to Color(0xFFB69CFF),
                "blue" to Color(0xFF83B9FF),
                "pink" to Color(0xFFFFA6D2),
                "green" to Color(0xFF9DDBB2)
            ).forEach { (id, color) ->
                Box(
                    Modifier.weight(1f).height(42.dp).background(color, RoundedCornerShape(12.dp))
                        .clickable { scope.launch { prefs.setAccentColor(id) } },
                    contentAlignment = Alignment.Center
                ) { if (accent == id) Text("✓", color = Color.Black, fontWeight = FontWeight.Bold) }
            }
        }

        Section("PRIVACIDAD Y PERFIL")
        Toggle(
            "Mostrar lo que escucho",
            "Otros pueden ver la canción actual en tu perfil público.",
            showListening
        ) { scope.launch { prefs.setShowListeningOnProfile(it) } }

        Section("REPRODUCCIÓN")
        Toggle(
            "Reproducción gapless",
            "Intenta encadenar pistas sin silencio.",
            gapless
        ) { scope.launch { prefs.setGaplessPlayback(it) } }

        if (isOwner && onOpenAdmin != null) {
            Section("OWNER")
            Button(
                onClick = onOpenAdmin,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 22.dp, vertical = 8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE5C17A), contentColor = Color.Black)
            ) { Text("Admin Panel (Owner)", fontWeight = FontWeight.Bold) }
        }

        Spacer(Modifier.height(100.dp))
    }
}

@Composable private fun Section(text: String) {
    Text(text, color = Color.White.copy(.45f), fontSize = 11.sp, fontWeight = FontWeight.Bold,
        letterSpacing = 1.4.sp, modifier = Modifier.padding(start = 22.dp, top = 22.dp, bottom = 8.dp))
}

@Composable private fun Toggle(title: String, subtitle: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth().clickable { onChange(!checked) }.padding(horizontal = 22.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(title, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Medium)
            Text(subtitle, color = Color.White.copy(.5f), fontSize = 12.sp, lineHeight = 17.sp, modifier = Modifier.padding(top = 3.dp))
        }
        Switch(checked, onChange, colors = SwitchDefaults.colors(
            checkedThumbColor = Color.Black, checkedTrackColor = Color.White, uncheckedTrackColor = Color(0xFF333338)
        ))
    }
}

@Composable private fun RowScope.ChoiceChip(label: String, selected: Boolean, onClick: () -> Unit) {
    Box(Modifier.weight(1f).background(if (selected) Color.White else Color(0xFF202024), RoundedCornerShape(12.dp))
        .clickable { onClick() }.padding(vertical = 12.dp), contentAlignment = Alignment.Center) {
        Text(label, color = if (selected) Color.Black else Color.White, fontSize = 12.sp)
    }
}
