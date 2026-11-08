package com.pokke.music

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.SystemBarStyle
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.pokke.music.data.preferences.UserPreferences
import com.pokke.music.player.PlayerController
import com.pokke.music.ui.liquid.GlassBackdropHost
import com.pokke.music.ui.navigation.PokkeNavHost
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
            val context = LocalContext.current
            val prefs = remember { UserPreferences(context) }
            val glass by prefs.liquidGlassEnabled.collectAsState(false)
            PokkeTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    // Only capture backdrop layer when user enables Liquid Glass (avoids home crash / GPU cost)
                    GlassBackdropHost(enabled = glass) {
                        PokkeNavHost()
                    }
                }
            }
        }
    }
    override fun onDestroy() {
        if (isFinishing) PlayerController.release()
        super.onDestroy()
    }
}
