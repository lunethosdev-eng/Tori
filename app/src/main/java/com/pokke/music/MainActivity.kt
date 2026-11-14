package com.pokke.music

import android.os.Bundle
import android.content.Intent
import android.net.Uri
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.SystemBarStyle
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.pokke.music.player.PlayerController
import com.pokke.music.player.PokkeAndroidPlayerHost
import com.pokke.music.ui.navigation.PokkeNavHost
import com.pokke.music.ui.liquid.GlassBackdropHost
import com.pokke.music.data.preferences.UserPreferences
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.LocalContext
import com.pokke.music.ui.theme.PokkeTheme

class MainActivity : ComponentActivity() {
    private var pluginDeepLinkUri by mutableStateOf<Uri?>(null)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        pluginDeepLinkUri = intent?.data
        PlayerController.init(this)
        PokkeAndroidPlayerHost.install(applicationContext)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
        )
        setContent {
            PokkeTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    val prefs = UserPreferences(LocalContext.current)
                    val glass by prefs.liquidGlassEnabled.collectAsState(false)
                    val performance by prefs.liquidPerformanceMode.collectAsState(true)
                    // A single root capture feeds all glass surfaces; TextureView Canvas avoids SurfaceView capture conflicts.
                    GlassBackdropHost(enabled = glass, performanceMode = performance) { PokkeNavHost(pluginDeepLinkUri = pluginDeepLinkUri, onPluginDeepLinkHandled = { pluginDeepLinkUri = null }) }
                }
            }
        }
    }
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        pluginDeepLinkUri = intent.data
    }
    override fun onDestroy() {
        if (isFinishing) PlayerController.release()
        super.onDestroy()
    }
}
