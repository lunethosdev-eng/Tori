package com.pokke.music.ui.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.LibraryMusic
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.pokke.music.player.PlayerController
import com.pokke.music.ui.home.HomeScreen
import com.pokke.music.ui.library.LibraryScreen
import com.pokke.music.ui.player.FullPlayerScreen
import com.pokke.music.ui.player.LyricsScreen
import com.pokke.music.ui.queue.QueueScreen
import com.pokke.music.ui.search.SearchScreen
import com.pokke.music.ui.settings.SettingsScreen

sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object Search : Screen("search")
    data object Library : Screen("library")
    data object Player : Screen("player")
    data object Queue : Screen("queue")
    data object Lyrics : Screen("lyrics")
    data object Settings : Screen("settings")
}

data class BottomNavItem(
    val screen: Screen,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

private val bottomNavItems = listOf(
    BottomNavItem(Screen.Home, "Home", Icons.Filled.Home, Icons.Outlined.Home),
    BottomNavItem(Screen.Search, "Search", Icons.Filled.Search, Icons.Outlined.Search),
    BottomNavItem(Screen.Library, "Library", Icons.Filled.LibraryMusic, Icons.Outlined.LibraryMusic)
)

@Composable
fun PokkeNavHost() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val showBottomBar = currentRoute in listOf(
        Screen.Home.route, Screen.Search.route, Screen.Library.route
    )

    fun openSong(song: com.pokke.music.data.model.Song) {
        PlayerController.init(context)
        PlayerController.playSong(song)
        navController.navigate(Screen.Player.route)
    }

    Scaffold(
        containerColor = Color.Black,
        bottomBar = {
            if (showBottomBar) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = 24.dp, vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(32.dp))
                            .background(Color(0xFF1C1C1C))
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        bottomNavItems.forEach { item ->
                            val selected = currentRoute == item.screen.route
                            IconButton(
                                onClick = {
                                    navController.navigate(item.screen.route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                },
                                modifier = Modifier
                                    .size(56.dp)
                                    .then(
                                        if (selected) Modifier
                                            .clip(RoundedCornerShape(28.dp))
                                            .background(Color(0xFF2E2E2E))
                                        else Modifier
                                    )
                            ) {
                                Icon(
                                    imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                                    contentDescription = item.label,
                                    tint = if (selected) Color.White else Color(0xFF888888),
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding),
            enterTransition = { fadeIn(animationSpec = tween(250)) },
            exitTransition = { fadeOut(animationSpec = tween(250)) }
        ) {
            composable(Screen.Home.route) {
                HomeScreen(
                    onSongClick = { openSong(it) },
                    onOpenPlayer = { navController.navigate(Screen.Player.route) },
                    onOpenSettings = { navController.navigate(Screen.Settings.route) }
                )
            }
            composable(Screen.Search.route) {
                SearchScreen(onSongClick = { openSong(it) })
            }
            composable(Screen.Library.route) {
                LibraryScreen(onSongClick = { openSong(it) })
            }
            composable(Screen.Settings.route) {
                SettingsScreen(onBack = { navController.popBackStack() })
            }
            composable(
                route = Screen.Player.route,
                enterTransition = {
                    slideInVertically(initialOffsetY = { it }, animationSpec = tween(400)) + fadeIn(tween(400))
                },
                exitTransition = {
                    slideOutVertically(targetOffsetY = { it }, animationSpec = tween(350)) + fadeOut(tween(350))
                }
            ) {
                FullPlayerScreen(
                    onBack = { navController.popBackStack() },
                    onOpenQueue = { navController.navigate(Screen.Queue.route) },
                    onOpenLyrics = { navController.navigate(Screen.Lyrics.route) }
                )
            }
            composable(Screen.Queue.route) {
                QueueScreen(onBack = { navController.popBackStack() })
            }
            composable(Screen.Lyrics.route) {
                LyricsScreen(onBack = { navController.popBackStack() })
            }
        }
    }
}
