package com.pokke.music.ui.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.animateContentSize
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
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.LibraryMusic
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Forum
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
import com.pokke.music.ui.social.ChatScreen
import com.pokke.music.ui.player.MiniPlayerBar
import com.pokke.music.ui.social.StoryPublisherScreen
import com.pokke.music.ui.social.FriendsMapScreen
import com.pokke.music.ui.liquid.LocalAppBackdrop
import com.pokke.music.ui.backdrop.drawBackdrop
import com.pokke.music.ui.backdrop.effects.blur
import com.pokke.music.ui.backdrop.effects.lens
import com.pokke.music.ui.backdrop.effects.vibrancy
import com.pokke.music.ui.shapes.Capsule
import com.pokke.music.data.remote.UpdateChecker
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import android.content.Intent
import android.net.Uri
import com.pokke.music.BuildConfig

sealed class Screen(val route: String) {
    data object Auth : Screen("auth")
    data object Profile : Screen("profile")
    data object PublicProfile : Screen("user/{username}") {
        fun create(username: String) = "user/${username.trim().lowercase().removePrefix("@")}"
    }
    data object Home : Screen("home")
    data object Library : Screen("library")
    data object Search : Screen("search")
    data object Chats : Screen("chats")
    data object Player : Screen("player")
    data object Queue : Screen("queue")
    data object Lyrics : Screen("lyrics")
    data object Settings : Screen("settings")
    data object Admin : Screen("admin")
    data object StoryPublish : Screen("story_publish")
    data object Maps : Screen("maps")
}

private data class NavItem(val screen: Screen, val selected: ImageVector, val normal: ImageVector)
private val baseNavItems = listOf(
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

    var updateInfo by remember { mutableStateOf<com.pokke.music.data.remote.AppRelease?>(null) }
    LaunchedEffect(Unit) {
        UpdateChecker.fetchLatest().onSuccess { rel ->
            if (UpdateChecker.isNewer(rel.tag, BuildConfig.VERSION_NAME)) updateInfo = rel
        }
    }
    updateInfo?.let { rel ->
        AlertDialog(
            onDismissRequest = { updateInfo = null },
            title = { Text("Nueva versión ${rel.tag}") },
            text = {
                Text((rel.name + "\n\n" + rel.body).take(900).ifBlank { "Hay una actualización disponible." })
            },
            confirmButton = {
                TextButton(onClick = {
                    if (rel.htmlUrl.isNotBlank()) {
                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(rel.htmlUrl)))
                    }
                    updateInfo = null
                }) { Text("Ver release") }
            },
            dismissButton = {
                TextButton(onClick = { updateInfo = null }) { Text("Después") }
            }
        )
    }

    LaunchedEffect(auth) { auth.restoreSession() }
    val glass by prefs.liquidGlassEnabled.collectAsState(false)
    val appBackdrop = LocalAppBackdrop.current
    val chatsEnabled by prefs.experimentalChatsEnabled.collectAsState(false)
    val backStack by nav.currentBackStackEntryAsState()
    val route = backStack?.destination?.route
    var navCompact by remember { mutableStateOf(false) }
    val showNav = route in listOf(Screen.Home.route, Screen.Search.route, Screen.Library.route, Screen.Chats.route)
    val navItems = remember(chatsEnabled) { if (chatsEnabled) baseNavItems + NavItem(Screen.Chats, Icons.Filled.Forum, Icons.Outlined.Forum) else baseNavItems }
    var avatar by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(route) { auth.getProfile().onSuccess { avatar = it?.avatarUrl } }

    Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        // SharedTransition removed — caused NoSuchMethodError on FullPlayer
            Box(Modifier.fillMaxSize()) {
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
                    onScrollStateChanged = { navCompact = it },
                    authRepository = auth
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
            composable(Screen.Chats.route) {
                if (chatsEnabled) ChatScreen(authRepository = auth, onOpenMaps = { nav.navigate(Screen.Maps.route) }, onOpenStory = { nav.navigate(Screen.StoryPublish.route) })
                else SettingsScreen(onBack = { nav.popBackStack() }, authRepository = auth)
            }
            composable(Screen.Maps.route) {
                FriendsMapScreen(authRepository = auth, onBack = { nav.popBackStack() })
            }
            composable(Screen.StoryPublish.route) {
                StoryPublisherScreen(authRepository = auth, onBack = { nav.popBackStack() }, onPublished = { nav.popBackStack() })
            }
            composable(Screen.Settings.route) {
                SettingsScreen(
                    onBack = { nav.popBackStack() },
                    onOpenAdmin = { nav.navigate(Screen.Admin.route) },
                    authRepository = auth
                )
            }
            composable(Screen.Admin.route) {
                AdminPanelScreen(repository = auth, onBack = { nav.popBackStack() })
            }
            composable(Screen.Player.route) {
                FullPlayerScreen(
                    onBack = { nav.popBackStack() },
                    onOpenQueue = { nav.navigate(Screen.Queue.route) },
                    onOpenLyrics = { nav.navigate(Screen.Lyrics.route) },
                    authRepository = auth,
                    onRequireLogin = { nav.navigate(Screen.Auth.route) }
                )
            }
            composable(Screen.Queue.route) { QueueScreen(onBack = { nav.popBackStack() }) }
            composable(Screen.Lyrics.route) { LyricsScreen(onBack = { nav.popBackStack() }) }
                }
            }

        val currentSong by PlayerController.currentSong.collectAsState()
        val showMini = currentSong != null && route !in listOf(Screen.Player.route, Screen.Lyrics.route, Screen.Auth.route)
        MiniPlayerBar(
            visible = showMini,
            glass = glass,
            compact = navCompact,
            onOpenPlayer = { nav.navigate(Screen.Player.route) },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = if (showNav) 78.dp else 16.dp)
                .navigationBarsPadding()
        )

        AnimatedVisibility(visible = showNav, modifier = Modifier.align(Alignment.BottomCenter), enter = fadeIn(androidx.compose.animation.core.tween(150)) + slideInVertically(androidx.compose.animation.core.tween(180)) { it / 3 }, exit = fadeOut(androidx.compose.animation.core.tween(110)) + slideOutVertically(androidx.compose.animation.core.tween(140)) { it / 3 }) {
            // Liquid Glass bottom bar (Kyant-inspired surface when toggle ON)
            Row(
                modifier = Modifier.padding(bottom = 10.dp).navigationBarsPadding()
                    .animateContentSize(androidx.compose.animation.core.tween(190))
                    .clip(RoundedCornerShape(40.dp)).background(
                        if (glass) Color(0xCC292930) else Color(0xFF19191E)
                    )
                    .padding(horizontal = if (navCompact) 6.dp else 9.dp, vertical = if (navCompact) 5.dp else 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(7.dp)
            ) {
                (if (navCompact) baseNavItems else navItems).forEach { item ->
                    val selected = route == item.screen.route
                    Box(Modifier.size(if (navCompact) 44.dp else 50.dp).clip(CircleShape).background(if (selected) Color.White.copy(if (glass) .20f else .12f) else Color.Transparent)
                        .clickable { nav.navigate(item.screen.route) { popUpTo(nav.graph.findStartDestination().id) { saveState = true }; launchSingleTop = true; restoreState = true } }, contentAlignment = Alignment.Center) {
                        Icon(if (selected) item.selected else item.normal, item.screen.route, tint = if (selected) Color.White else Color(0xFF929297), modifier = Modifier.size(24.dp))
                    }
                }
                if (!navCompact) Box(Modifier.size(if (navCompact) 44.dp else 50.dp).clip(CircleShape).background(Color(0xFF29292E)).clickable { nav.navigate(if (auth.session.value == null) Screen.Auth.route else Screen.Profile.route) }, contentAlignment = Alignment.Center) {
                    if (!avatar.isNullOrBlank()) AsyncImage(avatar, "Perfil", Modifier.fillMaxSize().clip(CircleShape))
                    else Icon(Icons.Default.Person, "Perfil", tint = Color.White, modifier = Modifier.size(23.dp))
                }
            }
        }
    }
}
