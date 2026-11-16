package com.pokke.music.tv.navigation

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
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
import androidx.activity.compose.BackHandler
import androidx.navigation.NavType
import androidx.navigation.navArgument
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.pokke.music.data.repository.AuthRepository
import com.pokke.music.data.repository.MusicRepository
import com.pokke.music.data.model.Song
import com.pokke.music.util.ShareLinks
import com.pokke.music.ui.profile.PublicProfileScreen
import com.pokke.music.ui.library.PublicPlaylistScreen
import com.pokke.music.ui.player.LyricsScreen
import com.pokke.music.data.preferences.UserPreferences
import com.pokke.music.ui.motion.MotionSettings
import com.pokke.music.ui.motion.ToriMotionEngine
import kotlinx.coroutines.flow.first
import com.pokke.music.player.PlayerController
import com.pokke.music.tv.ui.auth.TvAuthScreen
import com.pokke.music.tv.ui.home.TvHomeScreen
import com.pokke.music.tv.ui.library.TvLibraryScreen
import com.pokke.music.tv.ui.player.TvFullPlayerScreen
import com.pokke.music.tv.ui.player.TvMiniPlayer
import com.pokke.music.tv.ui.profile.TvProfileMenuScreen
import com.pokke.music.tv.ui.search.TvSearchScreen
import com.pokke.music.ui.liquid.LiquidGlassPanel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import android.content.Intent
import android.net.Uri

object TvRoutes {
    const val Home = "tv_home"
    const val Library = "tv_library"
    const val Search = "tv_search"
    const val Profile = "tv_profile"
    const val Auth = "tv_auth"
    const val Player = "tv_player"
    const val LinkDevice = "tv_link"
    const val PublicProfile = "tv_public_profile/{username}"
    const val PublicPlaylist = "tv_public_playlist/{playlistId}"
    const val Lyrics = "tv_lyrics"
    fun publicProfile(username: String) = "tv_public_profile/${Uri.encode(username)}"
    fun publicPlaylist(id: String) = "tv_public_playlist/${Uri.encode(id)}"
}

private data class TvTab(val route: String, val label: String, val icon: ImageVector)

@Composable
fun TvNavHost(deepLinkUri: Uri? = null, onDeepLinkHandled: () -> Unit = {}) {
    val context = LocalContext.current
    val auth = remember { AuthRepository(context) }
    val prefs = remember { UserPreferences(context) }
    val motionSettings by prefs.motionSettings.collectAsState(initial = MotionSettings())
    val tvMotionSettings = ToriMotionEngine.effectiveSettings(context, motionSettings)
    val tvTransitionDuration = ToriMotionEngine.transitionDurationMillis(tvMotionSettings, 280)
    val nav = rememberNavController()
    val backStack by nav.currentBackStackEntryAsState()
    val route = backStack?.destination?.route ?: TvRoutes.Home
    val song by PlayerController.currentSong.collectAsState()
    val session by auth.session.collectAsState()
    var sharedLyricsSong by remember { mutableStateOf<Song?>(null) }
    val showChrome = route !in setOf(TvRoutes.Player, TvRoutes.Auth, TvRoutes.Lyrics)

    LaunchedEffect(auth) { auth.restoreSession() }
    LaunchedEffect(deepLinkUri) {
        when (val target = ShareLinks.parse(deepLinkUri)) {
            is ShareLinks.Target.Profile -> nav.navigate(TvRoutes.publicProfile(target.username))
            is ShareLinks.Target.Playlist -> nav.navigate(TvRoutes.publicPlaylist(target.id))
            is ShareLinks.Target.Song -> MusicRepository().getSongById(target.id).onSuccess { linkedSong ->
                if (linkedSong != null) { PlayerController.init(context); PlayerController.playSong(linkedSong); nav.navigate(TvRoutes.Player) }
            }
            is ShareLinks.Target.Lyrics -> MusicRepository().getSongById(target.id).onSuccess { linkedSong ->
                if (linkedSong != null) { sharedLyricsSong = linkedSong; PlayerController.init(context); PlayerController.playSong(linkedSong); nav.navigate(TvRoutes.Lyrics) }
            }
            is ShareLinks.Target.Box -> runCatching { context.startActivity(Intent(Intent.ACTION_VIEW, ShareLinks.webForTarget(target))) }
            null -> Unit
        }
        if (deepLinkUri != null) onDeepLinkHandled()
    }
    BackHandler(enabled = route != TvRoutes.Home) {
        if (!nav.popBackStack()) nav.navigate(TvRoutes.Home) { popUpTo(TvRoutes.Home) { inclusive = false } }
    }

    // Account-synced Home and appearance settings are shared with the phone app.
    LaunchedEffect(session?.userId) {
        if (session != null) {
            val remoteResult = auth.getAccountAppSettings()
            val remote = remoteResult.getOrNull()
            var uploadSucceeded = true
            if (remote != null) prefs.applyAccountSyncedSettings(remote.toString())
            else {
                val local = prefs.accountSyncedSettings.first()
                uploadSucceeded = auth.saveAccountAppSettings(org.json.JSONObject(local)).isSuccess
            }
            var lastLocal = if (uploadSucceeded) prefs.accountSyncedSettings.first() else ""
            var lastRemote = remote?.toString() ?: lastLocal
            while (true) {
                kotlinx.coroutines.delay(30_000)
                var localNow = prefs.accountSyncedSettings.first()
                if (localNow != lastLocal) {
                    val saved = auth.saveAccountAppSettings(org.json.JSONObject(localNow))
                    if (saved.isSuccess) { lastLocal = localNow; lastRemote = localNow }
                }
                val latest = auth.getAccountAppSettings()
                val raw = latest.getOrNull()?.toString()
                if (latest.isSuccess && raw != null && raw != lastRemote) {
                    localNow = prefs.accountSyncedSettings.first()
                    if (localNow == lastLocal) {
                        prefs.applyAccountSyncedSettings(raw)
                        lastLocal = prefs.accountSyncedSettings.first()
                    } else {
                        val saved = auth.saveAccountAppSettings(org.json.JSONObject(localNow))
                        if (saved.isSuccess) { lastLocal = localNow; lastRemote = localNow }
                    }
                    if (localNow == lastLocal) lastRemote = raw
                }
            }
        }
    }

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
                LiquidGlassPanel(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 28.dp, vertical = 10.dp),
                    cornerRadius = 28.dp
                ) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 8.dp),
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
                                .clip(RoundedCornerShape(24.dp))
                                .background(
                                    when {
                                        selected -> Color.White.copy(0.14f)
                                        focused -> Color.White.copy(0.08f)
                                        else -> Color.Transparent
                                    }
                                )
                                .clickable {
                                    nav.navigate(tab.route) { launchSingleTop = true; restoreState = true }
                                }
                                .focusable()
                                .padding(horizontal = 16.dp, vertical = 10.dp),
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
                            .clip(RoundedCornerShape(50))
                            .background(if (pFocus) Color.White.copy(0.12f) else Color(0xFF1C1C24))
                            .clickable { nav.navigate(if (session == null) TvRoutes.Auth else TvRoutes.Profile) }
                            .focusable()
                            .padding(10.dp)
                    ) {
                        Icon(Icons.Default.Person, null, tint = Color.White, modifier = Modifier.size(22.dp))
                    }
                }
                }
            }

            Box(Modifier.weight(1f).fillMaxWidth()) {
                NavHost(
                    navController = nav,
                    startDestination = TvRoutes.Home,
                    enterTransition = { fadeIn(tween(tvTransitionDuration)) + slideInHorizontally(tween(tvTransitionDuration)) { it / 16 } },
                    exitTransition = { fadeOut(tween(tvTransitionDuration)) + slideOutHorizontally(tween(tvTransitionDuration)) { -it / 24 } },
                    popEnterTransition = { fadeIn(tween(tvTransitionDuration)) + slideInHorizontally(tween(tvTransitionDuration)) { -it / 16 } },
                    popExitTransition = { fadeOut(tween(tvTransitionDuration)) + slideOutHorizontally(tween(tvTransitionDuration)) { it / 24 } }
                ) {
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
                    composable(TvRoutes.PublicProfile, arguments = listOf(navArgument("username") { type = NavType.StringType })) { entry ->
                        val username = entry.arguments?.getString("username").orEmpty()
                        PublicProfileScreen(username = username, repository = auth, onBack = { nav.popBackStack() })
                    }
                    composable(TvRoutes.PublicPlaylist, arguments = listOf(navArgument("playlistId") { type = NavType.StringType })) { entry ->
                        val playlistId = entry.arguments?.getString("playlistId").orEmpty()
                        PublicPlaylistScreen(playlistId = playlistId, repository = auth, onBack = { nav.popBackStack() }, onPlay = { songs, index ->
                            PlayerController.init(context); PlayerController.playQueue(songs, index); nav.navigate(TvRoutes.Player)
                        })
                    }
                    composable(TvRoutes.Lyrics) { LyricsScreen(onBack = { nav.popBackStack() }, songOverride = sharedLyricsSong) }
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
