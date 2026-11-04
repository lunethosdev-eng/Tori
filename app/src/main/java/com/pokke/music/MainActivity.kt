package com.pokke.music

import android.os.Bundle
import android.os.Build
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.pokke.music.player.PlayerController
import com.pokke.music.ui.navigation.PokkeNavHost
import com.pokke.music.ui.theme.PokkeTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        PlayerController.init(this)
        if (Build.VERSION.SDK_INT >= 33 && ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 3104)
        }
        enableEdgeToEdge()
        setContent {
            PokkeTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    PokkeNavHost()
                }
            }
        }
    }

    override fun onDestroy() {
        // Don't release on config change; only when finishing
        if (isFinishing) {
            PlayerController.release()
        }
        super.onDestroy()
    }
}
