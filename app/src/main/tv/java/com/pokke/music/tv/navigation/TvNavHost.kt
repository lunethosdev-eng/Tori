package com.pokke.music.tv.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.key.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.pokke.music.data.repository.AuthRepository
import com.pokke.music.player.PlayerController
import com.pokke.music.tv.ui.auth.TvAuthScreen
import com.pokke.music.tv.ui.home.TvHomeScreen
import com.pokke.music.tv.ui.library.TvLibraryScreen
import com.pokke.music.tv.ui.player.TvFullPlayerScreen
import com.pokke.music.tv.ui.player.TvMiniPlayer
import com.pokke.music.tv.ui.profile.TvProfileMenuScreen
import com.pokke.music.tv.ui.search.TvSearchScreen
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object TvRoutes {
    const val Home = "tv_home"
    const val Library = "tv_library"
    const val Search = "tv_search"
    const val Profile = "tv_profile"
    const val Auth = "tv_auth"
    const val Player = "tv_player"
    const val LinkDevice = "tv_link"
}

private data class TvTab(val route: String, val label: String, val icon: ImageVector)

@Composable
fun TvNavHost() {
    val context = LocalContext.current
    val auth = remember { AuthRepository(context) }
    val nav = rememberNavController()
    val backStack by nav.currentBackStackEntryAsState()
    val route = backStack?.destination?.route ?: TvRoutes.Home
    val song by PlayerController.currentSong.collectAsState()
    val session by auth.session.collectAsState()
    val showChrome = route !in setOf(TvRoutes.Player, TvRoutes.Auth)

    val tabs = listOf(
        TvTab(TvRoutes.Home, "Inicio", Icons.Default.Home),
        TvTab(TvRoutes.Library, "Library", Icons.Default.LibraryMusic),
        TvTab(TvRoutes.Search, "Search", Icons.Default.Search),
        TvTab(TvRoutes.Profile, "Perfil", Icons.Default.Person)
    )

    val clock = remember {
        SimpleDateFormat("h:mm a • EEE, d MMM", Locale("es", "ES"))
    }
    var now by remember { mutableStateOf(clock.format(Date())) }
    LaunchedEffect(Unit) {
        while (true) {
            now = clock.format(Date())
            kotlinx.coroutines.delay(30_000)
        }
    }

    Box(Modifier.fillMaxSize().background(Color(0xFF0B0B10))) {
        Column(Modifier.fillMaxSize()) {
            if (showChrome) {
                // Top bar — time + tabs (D-pad friendly)
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 36.dp, vertical = 18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(now, color = Color(0xFFB8B8C0), fontSize = 14.sp)
                    Spacer(Modifier.width(28.dp))
                    tabs.forEach { tab ->
                        val selected = route == tab.route
                        var focused by remember { mutableStateOf(false) }
                        Row(
                            Modifier
                                .padding(end = 14.dp)
                                .onFocusChanged { focused = it.isFocused }
                                .background(
                                    when {
                                        selected -> Color.White.copy(0.14f)
                                        focused -> Color.White.copy(0.08f)
                                        else -> Color.Transparent
                                    },
                                    RoundedCornerShape(24.dp)
                                )
                                .padding(horizontal = 16.dp, vertical = 10.dp)
                                .onKeyEvent { e ->
                                    if (e.type == KeyEventType.KeyUp &&
                                        (e.key == Key.DirectionCenter || e.key == Key.Enter)
                                    ) {
                                        nav.navigate(tab.route) {
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                        true
                                    } else false
                                },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(tab.icon, null, tint = if (selected) Color.White else Color(0xFF9A9AA3), modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(tab.label, color = if (selected) Color.White else Color(0xFF9A9AA3), fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal)
                        }
                    }
                    Spacer(Modifier.weight(1f))
                    // Profile shortcut
                    var pFocus by remember { mutableStateOf(false) }
                    Box(
                        Modifier
                            .onFocusChanged { pFocus = it.isFocused }
                            .background(if (pFocus) Color.White.copy(0.12f) else Color(0xFF1C1C24), RoundedCornerShape(50))
                            .padding(10.dp)
                            .onKeyEvent { e ->
                                if (e.type == KeyEventType.KeyUp && (e.key == Key.DirectionCenter || e.key == Key.Enter)) {
                                    nav.navigate(if (session == null) TvRoutes.Auth else TvRoutes.Profile)
                                    true
                                } else false
                            }
                    ) {
                        Icon(Icons.Default.Person, null, tint = Color.White, modifier = Modifier.size(22.dp))
                    }
                }
            }

            Box(Modifier.weight(1f).fillMaxWidth()) {
                NavHost(navController = nav, startDestination = TvRoutes.Home) {
                    composable(TvRoutes.Home) {
                        TvHomeScreen(
                            onPlay = { song ->
                                PlayerController.init(context)
                                PlayerController.playSong(song)
                                nav.navigate(TvRoutes.Player)
                            },
                            onOpenPlayer = { nav.navigate(TvRoutes.Player) }
                        )
                    }
                    composable(TvRoutes.Library) {
                        TvLibraryScreen(
                            onPlay = { song ->
                                PlayerController.init(context)
                                PlayerController.playSong(song)
                                nav.navigate(TvRoutes.Player)
                            }
                        )
                    }
                    composable(TvRoutes.Search) {
                        TvSearchScreen(
                            onPlay = { song ->
                                PlayerController.init(context)
                                PlayerController.playSong(song)
                                nav.navigate(TvRoutes.Player)
                            }
                        )
                    }
                    composable(TvRoutes.Profile) {
                        TvProfileMenuScreen(
                            auth = auth,
                            onAuth = { nav.navigate(TvRoutes.Auth) },
                            onLinkDevice = { nav.navigate(TvRoutes.LinkDevice) },
                            onBack = { nav.popBackStack() }
                        )
                    }
                    composable(TvRoutes.LinkDevice) {
                        com.pokke.music.tv.ui.profile.TvLinkDeviceScreen(onBack = { nav.popBackStack() })
                    }
                    composable(TvRoutes.Auth) {
                        TvAuthScreen(auth = auth, onDone = {
                            nav.navigate(TvRoutes.Home) { popUpTo(TvRoutes.Auth) { inclusive = true } }
                        })
                    }
                    composable(TvRoutes.Player) {
                        TvFullPlayerScreen(onBack = { nav.popBackStack() })
                    }
                }

                if (showChrome && song != null) {
                    TvMiniPlayer(
                        modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 24.dp),
                        onOpen = { nav.navigate(TvRoutes.Player) }
                    )
                }
            }
        }
    }
}
