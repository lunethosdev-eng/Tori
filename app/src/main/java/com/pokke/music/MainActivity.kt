package com.pokke.music

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.pokke.music.ui.navigation.PokkeNavHost
import com.pokke.music.ui.theme.PokkeTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PokkeTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    PokkeNavHost()
                }
            }
        }
    }
}
