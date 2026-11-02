package com.pokke.music.ui.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.LibraryMusic
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.pokke.music.ui.home.HomeScreen
import com.pokke.music.ui.player.FullPlayerScreen
import com.pokke.music.ui.queue.QueueScreen

sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object Search : Screen("search")
    data object Library : Screen("library")
    data object Player : Screen("player")
    data object Queue : Screen("queue")
    data object Lyrics : Screen("lyrics")
}

data class BottomNavItem(
    val screen: Screen,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

val bottomNavItems = listOf(
    BottomNavItem(Screen.Home, "Home", Icons.Filled.Home, Icons.Outlined.Home),
    BottomNavItem(Screen.Search, "Search", Icons.Filled.Search, Icons.Outlined.Search),
    BottomNavItem(Screen.Library, "Library", Icons.Filled.LibraryMusic, Icons.Outlined.LibraryMusic)
)

@Composable
fun PokkeNavHost() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val showBottomBar = currentRoute in listOf(
        Screen.Home.route,
        Screen.Search.route,
        Screen.Library.route
    )

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    containerColor = Color(0xFF121212),
                    tonalElevation = 0.dp
                ) {
                    bottomNavItems.forEach { item ->
                        val selected = currentRoute == item.screen.route
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                navController.navigate(item.screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = {
                                Icon(
                                    imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                                    contentDescription = item.label
                                )
                            },
                            label = { Text(item.label) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color.White,
                                selectedTextColor = Color.White,
                                unselectedIconColor = Color(0xFFB3B3B3),
                                unselectedTextColor = Color(0xFFB3B3B3),
                                indicatorColor = Color(0xFF2A2A2A)
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding),
            enterTransition = { fadeIn(animationSpec = tween(300)) },
            exitTransition = { fadeOut(animationSpec = tween(300)) }
        ) {
            composable(Screen.Home.route) {
                HomeScreen(
                    onSongClick = { navController.navigate(Screen.Player.route) },
                    onOpenPlayer = { navController.navigate(Screen.Player.route) }
                )
            }
            composable(Screen.Search.route) {
                // Placeholder Search
                androidx.compose.foundation.layout.Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = androidx.compose.ui.Alignment.Center
                ) {
                    Text("Search", color = Color.White)
                }
            }
            composable(Screen.Library.route) {
                androidx.compose.foundation.layout.Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = androidx.compose.ui.Alignment.Center
                ) {
                    Text("Library", color = Color.White)
                }
            }
            composable(
                route = Screen.Player.route,
                enterTransition = {
                    slideInVertically(
                        initialOffsetY = { it },
                        animationSpec = tween(400)
                    ) + fadeIn(tween(400))
                },
                exitTransition = {
                    slideOutVertically(
                        targetOffsetY = { it },
                        animationSpec = tween(350)
                    ) + fadeOut(tween(350))
                }
            ) {
                FullPlayerScreen(
                    onBack = { navController.popBackStack() },
                    onOpenQueue = { navController.navigate(Screen.Queue.route) },
                    onOpenLyrics = { navController.navigate(Screen.Lyrics.route) }
                )
            }
            composable(
                route = Screen.Queue.route,
                enterTransition = {
                    slideInVertically(initialOffsetY = { it }, animationSpec = tween(350)) + fadeIn()
                },
                exitTransition = {
                    slideOutVertically(targetOffsetY = { it }, animationSpec = tween(300)) + fadeOut()
                }
            ) {
                QueueScreen(onBack = { navController.popBackStack() })
            }
            composable(
                route = Screen.Lyrics.route,
                enterTransition = { fadeIn(tween(300)) },
                exitTransition = { fadeOut(tween(250)) }
            ) {
                com.pokke.music.ui.player.LyricsScreen(
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}
