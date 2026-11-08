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
import com.pokke.music.tv.navigation.TvNavHost
import com.pokke.music.ui.theme.PokkeTheme

class TvMainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
