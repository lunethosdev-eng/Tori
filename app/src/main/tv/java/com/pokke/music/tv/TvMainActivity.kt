package com.pokke.music.tv

import android.os.Bundle
import android.content.Intent
import android.net.Uri
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.pokke.music.data.preferences.UserPreferences
import com.pokke.music.ui.liquid.GlassBackdropHost
import com.pokke.music.player.PlayerController
import com.pokke.music.player.PokkeAndroidPlayerHost
import com.pokke.music.tv.navigation.TvNavHost
import com.pokke.music.ui.theme.PokkeTheme
import com.pokke.music.remote.TvRemoteServer

/**
 * Entry point for the Android TV / leanback APK (applicationId com.pokke.music.tv).
 * Must not be confused with phone [com.pokke.music.MainActivity].
 */
class TvMainActivity : ComponentActivity() {
    private var shareDeepLinkUri by mutableStateOf<Uri?>(null)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        shareDeepLinkUri = intent?.data
        // Same player stack as phone — required before any playback UI
        PlayerController.init(this)
        PokkeAndroidPlayerHost.install(applicationContext)
        enableEdgeToEdge()
        setContent {
            PokkeTheme {
                Surface(Modifier.fillMaxSize(), color = Color(0xFF0B0B10)) {
                    val preferences = UserPreferences(LocalContext.current)
                    val glass by preferences.liquidGlassEnabled.collectAsState(initial = false)
                    val performance by preferences.liquidPerformanceMode.collectAsState(initial = true)
                    GlassBackdropHost(enabled = glass, performanceMode = performance) {
                        TvNavHost(deepLinkUri = shareDeepLinkUri, onDeepLinkHandled = { shareDeepLinkUri = null })
                    }
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        TvRemoteServer.start(this)
    }

    override fun onStop() {
        TvRemoteServer.stop()
        super.onStop()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        shareDeepLinkUri = intent.data
    }

    override fun onDestroy() {
        if (isFinishing) PlayerController.release()
        super.onDestroy()
    }
}
