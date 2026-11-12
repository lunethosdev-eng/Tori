package com.pokke.music.tv

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.pokke.music.player.PlayerController
import com.pokke.music.player.PokkeAndroidPlayerHost
import com.pokke.music.tv.navigation.TvNavHost
import com.pokke.music.ui.theme.PokkeTheme

/**
 * Entry point for the Android TV / leanback APK (applicationId com.pokke.music.tv).
 * Must not be confused with phone [com.pokke.music.MainActivity].
 */
class TvMainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Same player stack as phone — required before any playback UI
        PlayerController.init(this)
        PokkeAndroidPlayerHost.install(applicationContext)
        enableEdgeToEdge()
        setContent {
            PokkeTheme {
                Surface(Modifier.fillMaxSize(), color = Color(0xFF0B0B10)) {
                    TvNavHost()
                }
            }
        }
    }

    override fun onDestroy() {
        if (isFinishing) PlayerController.release()
        super.onDestroy()
    }
}
