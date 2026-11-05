package com.pokke.music.ui.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.LibraryMusic
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Icon
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.pokke.music.data.preferences.UserPreferences
import com.pokke.music.data.repository.AuthRepository
import com.pokke.music.player.PlayerController
import com.pokke.music.ui.auth.LoginScreen
import com.pokke.music.ui.auth.ProfileScreen
import com.pokke.music.ui.home.HomeScreen
import com.pokke.music.ui.library.LibraryScreen
import com.pokke.music.ui.player.FullPlayerScreen
import com.pokke.music.ui.player.LyricsScreen
import com.pokke.music.ui.queue.QueueScreen
import com.pokke.music.ui.search.SearchScreen
import com.pokke.music.ui.settings.SettingsScreen
import com.pokke.music.ui.admin.AdminPanelScreen

sealed class Screen(val route: String) {
    data object Auth : Screen("auth")
    data object Profile : Screen("profile")
    data object PublicProfile : Screen("user/{username}") {
        fun create(username: String) = "user/${username.trim().lowercase().removePrefix("@")}"
    }
    data object Home : Screen("home")
    data object Library : Screen("library")
    data object Search : Screen("search")
    data object Player : Screen("player")
    data object Queue : Screen("queue")
    data object Lyrics : Screen("lyrics")
    data object Settings : Screen("settings")
    data object Admin : Screen("admin")
}

private data class NavItem(val screen: Screen, val selected: ImageVector, val normal: ImageVector)
private val navItems = listOf(
    NavItem(Screen.Home, Icons.Filled.Home, Icons.Outlined.Home),
    NavItem(Screen.Library, Icons.Filled.LibraryMusic, Icons.Outlined.LibraryMusic),
    NavItem(Screen.Search, Icons.Filled.Search, Icons.Outlined.Search)
)

@Composable
fun PokkeNavHost() {
    val nav = rememberNavController()
    val context = LocalContext.current
    val prefs = remember { UserPreferences(context) }
    val auth = remember(context) { AuthRepository(context) }
    LaunchedEffect(auth) { auth.restoreSession() }
    val glass by prefs.liquidGlassEnabled.collectAsState(true)
    val backStack by nav.currentBackStackEntryAsState()
    val route = backStack?.destination?.route
    val showNav = route in listOf(Screen.Home.route, Screen.Search.route, Screen.Library.route)
    var avatar by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(route) { auth.getProfile().onSuccess { avatar = it?.avatarUrl } }

    Box(Modifier.fillMaxSize().background(Color(0xFF080808))) {
        NavHost(navController = nav, startDestination = Screen.Home.route, modifier = Modifier.fillMaxSize()) {
            composable(Screen.Auth.route) {
                LoginScreen(repository = auth, onContinueAsGuest = { nav.popBackStack() }, onLoginSuccess = { nav.popBackStack() })
            }
            composable(Screen.Profile.route) {
                ProfileScreen(repository = auth, onBack = { nav.popBackStack() }, onSignedOut = { nav.navigate(Screen.Auth.route) { popUpTo(0) } })
            }
            composable(Screen.Home.route) {
                HomeScreen(
                    onSongClick = { PlayerController.init(context); PlayerController.playSong(it); nav.navigate(Screen.Player.route) },
                    onOpenPlayer = { nav.navigate(Screen.Player.route) },
                    onOpenSettings = { nav.navigate(Screen.Settings.route) },
                    onOpenProfile = { nav.navigate(if (auth.session.value == null) Screen.Auth.route else Screen.Profile.route) },
                    onScrollStateChanged = {}, authRepository = auth
                )
            }
            composable(Screen.Search.route) {
                SearchScreen(
                    authRepository = auth,
                    isVisible = route == Screen.Search.route,
                    onSongClick = { PlayerController.init(context); PlayerController.playSong(it); nav.navigate(Screen.Player.route) },
                    onPlayQueue = { songs, index -> PlayerController.init(context); PlayerController.playQueue(songs, index); nav.navigate(Screen.Player.route) },
                    onUserClick = { username -> nav.navigate(Screen.PublicProfile.create(username)) }
                )
            }
            composable(Screen.PublicProfile.route) { entry ->
                val username = entry.arguments?.getString("username").orEmpty()
                com.pokke.music.ui.profile.PublicProfileScreen(
                    username = username,
                    repository = auth,
                    onBack = { nav.popBackStack() }
                )
            }
            composable(Screen.Library.route) {
                LibraryScreen(repository = auth, isVisible = route == Screen.Library.route,
                    onLoginRequired = { nav.navigate(Screen.Auth.route) },
                    onSongClick = { PlayerController.init(context); PlayerController.playSong(it); nav.navigate(Screen.Player.route) })
            }
            composable(Screen.Settings.route) {
                SettingsScreen(
                    onBack = { nav.popBackStack() },
                    onOpenAdmin = { nav.navigate(Screen.Admin.route) }
                )
            }
            composable(Screen.Admin.route) {
                AdminPanelScreen(repository = auth, onBack = { nav.popBackStack() })
            }
            composable(Screen.Player.route) {
                FullPlayerScreen(onBack = { nav.popBackStack() }, onOpenQueue = { nav.navigate(Screen.Queue.route) },
                    onOpenLyrics = { nav.navigate(Screen.Lyrics.route) }, authRepository = auth, onRequireLogin = { nav.navigate(Screen.Auth.route) })
            }
            composable(Screen.Queue.route) { QueueScreen(onBack = { nav.popBackStack() }) }
            composable(Screen.Lyrics.route) { LyricsScreen(onBack = { nav.popBackStack() }) }
        }

        AnimatedVisibility(visible = showNav, modifier = Modifier.align(Alignment.BottomCenter), enter = fadeIn() + slideInVertically { it / 2 }, exit = fadeOut() + slideOutVertically { it / 2 }) {
            Row(
                modifier = Modifier.padding(bottom = 10.dp).navigationBarsPadding().clip(RoundedCornerShape(40.dp))
                    .background(if (glass) Color(0xB82A292D) else Color(0xF01A1A1D))
                    .padding(horizontal = 9.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(7.dp)
            ) {
                navItems.forEach { item ->
                    val selected = route == item.screen.route
                    Box(Modifier.size(50.dp).clip(CircleShape).background(if (selected) Color.White.copy(if (glass) .20f else .12f) else Color.Transparent)
                        .clickable { nav.navigate(item.screen.route) { popUpTo(nav.graph.findStartDestination().id) { saveState = true }; launchSingleTop = true; restoreState = true } }, contentAlignment = Alignment.Center) {
                        Icon(if (selected) item.selected else item.normal, item.screen.route, tint = if (selected) Color.White else Color(0xFF929297), modifier = Modifier.size(24.dp))
                    }
                }
                Box(Modifier.size(50.dp).clip(CircleShape).background(Color(0xFF29292E)).clickable { nav.navigate(if (auth.session.value == null) Screen.Auth.route else Screen.Profile.route) }, contentAlignment = Alignment.Center) {
                    if (!avatar.isNullOrBlank()) AsyncImage(avatar, "Perfil", Modifier.fillMaxSize().clip(CircleShape))
                    else Icon(Icons.Default.Person, "Perfil", tint = Color.White, modifier = Modifier.size(23.dp))
                }
            }
        }
    }
}
