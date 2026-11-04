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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import com.pokke.music.data.preferences.UserPreferences
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val prefs = remember(context) { UserPreferences(context) }
    val scope = rememberCoroutineScope()
    val glass by prefs.liquidGlassEnabled.collectAsState(true)
    val blur by prefs.blurPlayerBackground.collectAsState(true)
    val accent by prefs.accentColor.collectAsState("white")
    val theme by prefs.appTheme.collectAsState("dark")

    Column(Modifier.fillMaxSize().background(Color(0xFF09090B)).verticalScroll(rememberScrollState()).statusBarsPadding()) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White) }
            Column { Text("Ajustes", color = Color.White, fontSize = 23.sp, fontWeight = FontWeight.Bold); Text("Personaliza tu experiencia", color = Color.White.copy(.5f), fontSize = 12.sp) }
        }
        Section("APARIENCIA")
        Toggle("Liquid Glass", "Superficies translúcidas y reflejos animados en controles compatibles.", glass) { scope.launch { prefs.setLiquidGlass(it) } }
        Toggle("Fondo desenfocado en el reproductor", "Usa la portada como fondo ambiental.", blur) { scope.launch { prefs.setBlurPlayerBackground(it) } }
        Text("Tema", color = Color.White.copy(.75f), fontSize = 14.sp, modifier = Modifier.padding(start = 22.dp, top = 12.dp, bottom = 8.dp))
        Row(Modifier.fillMaxWidth().padding(horizontal = 18.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("dark" to "Oscuro", "amoled" to "AMOLED").forEach { (id, label) ->
                ChoiceChip(label, theme == id) { scope.launch { prefs.setAppTheme(id) } }
            }
        }
        Text("Color de acento", color = Color.White.copy(.75f), fontSize = 14.sp, modifier = Modifier.padding(start = 22.dp, top = 14.dp, bottom = 8.dp))
        Row(Modifier.fillMaxWidth().padding(horizontal = 18.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("white" to Color.White, "violet" to Color(0xFFB69CFF), "blue" to Color(0xFF83B9FF), "pink" to Color(0xFFFFA6D2), "green" to Color(0xFF9DDBB2)).forEach { (id, color) ->
                Box(Modifier.weight(1f).height(42.dp).background(color, RoundedCornerShape(12.dp)).clickable { scope.launch { prefs.setAccentColor(id) } }, contentAlignment = Alignment.Center) {
                    if (accent == id) Text("✓", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            }
        }
        Section("REPRODUCCIÓN")
        InfoRow("Motor de audio", "Android Media3 · controles de reproducción del sistema")
        Section("LETRAS Y PERFIL")
        InfoRow("Proveedor de letras", "LRCLIB · sincronización por líneas cuando está disponible")
        InfoRow("Personalización del perfil", "Foto, banner, biografía y tema se guardan en Kokoro")
        Spacer(Modifier.height(100.dp))
    }
}

@Composable private fun Section(text: String) { Text(text, color = Color.White.copy(.45f), fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.4.sp, modifier = Modifier.padding(start = 22.dp, top = 22.dp, bottom = 8.dp)) }
@Composable private fun Toggle(title: String, subtitle: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth().clickable { onChange(!checked) }.padding(horizontal = 22.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) { Text(title, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Medium); Text(subtitle, color = Color.White.copy(.5f), fontSize = 12.sp, lineHeight = 17.sp, modifier = Modifier.padding(top = 3.dp)) }
        Switch(checked, onChange, colors = SwitchDefaults.colors(checkedThumbColor = Color.Black, checkedTrackColor = Color.White, uncheckedTrackColor = Color(0xFF333338)))
    }
}
@Composable private fun RowScope.ChoiceChip(label: String, selected: Boolean, onClick: () -> Unit) {
    Box(Modifier.weight(1f).background(if (selected) Color.White else Color(0xFF202024), RoundedCornerShape(12.dp)).clickable { onClick() }.padding(vertical = 12.dp), contentAlignment = Alignment.Center) { Text(label, color = if (selected) Color.Black else Color.White, fontSize = 12.sp) }
}
@Composable private fun InfoRow(title: String, subtitle: String) { Column(Modifier.fillMaxWidth().padding(horizontal = 22.dp, vertical = 10.dp)) { Text(title, color = Color.White, fontSize = 14.sp); Text(subtitle, color = Color.White.copy(.5f), fontSize = 12.sp) } }
