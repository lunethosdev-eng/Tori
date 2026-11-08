package com.pokke.music

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.SystemBarStyle
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.pokke.music.player.PlayerController
import com.pokke.music.ui.navigation.PokkeNavHost
import com.pokke.music.ui.liquid.GlassBackdropHost
import com.pokke.music.data.preferences.UserPreferences
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import com.pokke.music.ui.theme.PokkeTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        PlayerController.init(this)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
        )
        setContent {
            PokkeTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    val prefs = UserPreferences(LocalContext.current)
                    val glass by prefs.liquidGlassEnabled.collectAsState(false)
                    // Only host layer when glass ON; avoids RenderThread stack overflow when OFF
                    GlassBackdropHost(enabled = glass) { PokkeNavHost() }
                }
            }
        }
    }
    override fun onDestroy() {
        if (isFinishing) PlayerController.release()
        super.onDestroy()
    }
}
