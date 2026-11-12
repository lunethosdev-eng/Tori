package com.pokke.music.ui.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pokke.music.auth.external.DiscordLoginScreen
import com.pokke.music.auth.external.ExternalAuthStore
import com.pokke.music.auth.external.ExternalSession
import com.pokke.music.auth.external.SpotifyLoginScreen
import com.pokke.music.auth.external.YtMusicLoginScreen
import com.pokke.music.data.preferences.UserPreferences
import kotlinx.coroutines.launch
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material3.MaterialTheme

/**
 * Cuentas externas (Spotify / Discord / YT Music) — solo si el flag global está ON en Settings.
 */
@Composable
fun ExternalAccountsSection(prefs: UserPreferences) {
    val scope = rememberCoroutineScope()
    val ctx = LocalContext.current
    val store = remember { ExternalAuthStore(ctx) }
    val enabled by prefs.externalAccountsEnabled.collectAsState(false)
    val spotifyOn by prefs.spotifyLoginEnabled.collectAsState(false)
    val discordOn by prefs.discordLoginEnabled.collectAsState(false)
    val ytOn by prefs.ytMusicLoginEnabled.collectAsState(false)
    var sessions by remember { mutableStateOf(store.sessions()) }
    var openLogin by remember { mutableStateOf<String?>(null) }

    Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)) {
        Text("Cuentas externas", style = MaterialTheme.typography.titleMedium)
        Text(
            "Opcional. Cookies cifradas en el dispositivo. No sustituyen la cuenta Pokke (Supabase).",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(0.6f)
        )
        RowToggle("Activar cuentas externas", enabled) {
            scope.launch { prefs.setExternalAccountsEnabled(it) }
        }
        if (enabled) {
            RowToggle("Spotify login", spotifyOn) { scope.launch { prefs.setSpotifyLoginEnabled(it) } }
            RowToggle("Discord login", discordOn) { scope.launch { prefs.setDiscordLoginEnabled(it) } }
            RowToggle("YT Music login", ytOn) { scope.launch { prefs.setYtMusicLoginEnabled(it) } }

            if (spotifyOn) TextButton(onClick = { openLogin = "spotify" }) { Text("Conectar Spotify") }
            if (discordOn) TextButton(onClick = { openLogin = "discord" }) { Text("Conectar Discord") }
            if (ytOn) TextButton(onClick = { openLogin = "ytmusic" }) { Text("Conectar YT Music") }

            sessions.forEach { s ->
                Text("• ${s.provider}: ${s.label.ifBlank { "sesión" }}", fontSize = 13.sp)
            }
        }
    }

    when (openLogin) {
        "spotify" -> SpotifyLoginScreen(onConnected = { token ->
            store.upsert(ExternalSession("spotify", "Spotify", "sp_dc=$token"))
            sessions = store.sessions()
            openLogin = null
        })
        "discord" -> DiscordLoginScreen(onTokenCaptured = { token ->
            store.upsert(ExternalSession("discord", "Discord", token))
            sessions = store.sessions()
            openLogin = null
        })
        "ytmusic" -> YtMusicLoginScreen(
            mode = com.pokke.music.auth.external.WebSessionMode.SIGN_IN,
            onCaptured = { captured ->
                store.upsert(ExternalSession("ytmusic", "YT Music", captured.cookie))
                sessions = store.sessions()
                openLogin = null
            }
        )
    }
}

@Composable
private fun RowToggle(label: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(
        Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, modifier = Modifier.padding(end = 12.dp))
        Switch(checked = checked, onCheckedChange = onChange)
    }
}
