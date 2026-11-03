package com.pokke.music.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.outlined.Science
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pokke.music.data.preferences.UserPreferences
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val prefs = remember { UserPreferences(context) }
    val scope = rememberCoroutineScope()

    val liquidGlass by prefs.liquidGlassEnabled.collectAsState(initial = false)
    val autoAnimated by prefs.autoAnimatedCovers.collectAsState(initial = true)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0A0A0A))
            .verticalScroll(rememberScrollState())
    ) {
        // Top bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White
                )
            }
            Text(
                text = "Settings",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Experimental section
        SectionHeader("Experimental")

        SettingToggle(
            title = "Liquid Glass (Eco Glass)",
            subtitle = "Efecto líquido premium en la barra de navegación y transiciones. Puede afectar el rendimiento.",
            checked = liquidGlass,
            experimental = true,
            onCheckedChange = { enabled ->
                scope.launch { prefs.setLiquidGlass(enabled) }
            }
        )

        HorizontalDivider(
            color = Color.White.copy(alpha = 0.08f),
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
        )

        // Playback section
        SectionHeader("Reproducción")

        SettingToggle(
            title = "Covers animados automáticos",
            subtitle = "Si la canción tiene cover animado (Ukiyo), se reproduce en loop automáticamente. Si está apagado, aparece un botón manual.",
            checked = autoAnimated,
            experimental = false,
            onCheckedChange = { enabled ->
                scope.launch { prefs.setAutoAnimatedCovers(enabled) }
            }
        )

        HorizontalDivider(
            color = Color.White.copy(alpha = 0.08f),
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
        )

        // Account
        SectionHeader("Cuenta")

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { /* navigate to login */ }
                .padding(horizontal = 20.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Iniciar sesión / Registrarse", color = Color.White, fontSize = 15.sp)
                Text(
                    "Desbloquea más personalización de perfil",
                    color = Color.White.copy(0.55f),
                    fontSize = 13.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(40.dp))
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title.uppercase(),
        color = Color.White.copy(alpha = 0.45f),
        fontSize = 12.sp,
        fontWeight = FontWeight.Medium,
        modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
    )
}

@Composable
private fun SettingToggle(
    title: String,
    subtitle: String,
    checked: Boolean,
    experimental: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(title, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Medium)
                if (experimental) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        Icons.Outlined.Science,
                        contentDescription = "Experimental",
                        tint = Color(0xFFFFB74D),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = subtitle,
                color = Color.White.copy(alpha = 0.55f),
                fontSize = 13.sp,
                lineHeight = 18.sp
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = Color(0xFF4CAF50),
                uncheckedThumbColor = Color(0xFFBDBDBD),
                uncheckedTrackColor = Color(0xFF424242)
            )
        )
    }
}
