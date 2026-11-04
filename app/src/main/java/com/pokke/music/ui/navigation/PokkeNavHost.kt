package com.pokke.music.ui.navigation

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
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
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.pokke.music.data.preferences.UserPreferences
import com.pokke.music.player.PlayerController
import com.pokke.music.ui.auth.LoginScreen
import com.pokke.music.ui.home.HomeScreen
import com.pokke.music.ui.library.LibraryScreen
import com.pokke.music.ui.player.FullPlayerScreen
import com.pokke.music.ui.player.LyricsScreen
import com.pokke.music.ui.queue.QueueScreen
import com.pokke.music.ui.search.SearchScreen
import com.pokke.music.ui.settings.SettingsScreen

sealed class Screen(val route: String) {
    data object Auth : Screen("auth")
    data object Home : Screen("home")
    data object Library : Screen("library")
    data object Search : Screen("search")
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

// Orden: Home, Library, Search (Search al final dentro del mismo nav por defecto)
private val bottomNavItems = listOf(
    BottomNavItem(Screen.Home, "Home", Icons.Filled.Home, Icons.Outlined.Home),
    BottomNavItem(Screen.Library, "Library", Icons.Filled.LibraryMusic, Icons.Outlined.LibraryMusic),
    BottomNavItem(Screen.Search, "Search", Icons.Filled.Search, Icons.Outlined.Search)
)

@Composable
fun PokkeNavHost() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val prefs = remember { UserPreferences(context) }
    
    val liquidGlassEnabled by prefs.liquidGlassEnabled.collectAsState(initial = false)
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    var isScrolled by remember { mutableStateOf(false) }

    val showBottomBar = currentRoute in listOf(
        Screen.Home.route, Screen.Search.route, Screen.Library.route
    )

    // Animación de separación de tipo gota líquida (Liquid Glass) al hacer scroll
    val searchSeparation = remember { Animatable(0f) }
    LaunchedEffect(isScrolled) {
        searchSeparation.animateTo(
            targetValue = if (isScrolled) 1f else 0f,
            animationSpec = spring(
                dampingRatio = if (liquidGlassEnabled) 0.55f else Spring.DampingRatioNoBouncy,
                stiffness = Spring.StiffnessMediumLow
            )
        )
    }

    Scaffold(
        containerColor = Color.Black,
        bottomBar = {
            if (showBottomBar) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp * searchSeparation.value)
                    ) {
                        // Contenedor Nav Principal (Home, Library y opcionalmente Perfil y Search si no hay scroll)
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(32.dp))
                                .background(
                                    if (liquidGlassEnabled) Color.White.copy(alpha = 0.10f)
                                    else Color(0xFF141414)
                                )
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Elementos fijos (Home, Library)
                            bottomNavItems.take(2).forEach { item ->
                                val selected = currentRoute == item.screen.route
                                NavIconButton(
                                    selected = selected,
                                    selectedIcon = item.selectedIcon,
                                    unselectedIcon = item.unselectedIcon,
                                    label = item.label,
                                    onClick = {
                                        navController.navigate(item.screen.route) {
                                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    }
                                )
                            }

                            // Si hay scroll, el perfil aparece integrado en el nav
                            if (isScrolled) {
                                Spacer(modifier = Modifier.width(4.dp))
                                Box(
                                    modifier = Modifier
                                        .size(46.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF252525))
                                        .clickable { navController.navigate(Screen.Auth.route) },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = "Profile",
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }

                            // Si NO hay scroll, Search vive dentro de este mismo contenedor (al final)
                            if (!isScrolled) {
                                Spacer(modifier = Modifier.width(4.dp))
                                val searchItem = bottomNavItems[2]
                                val selected = currentRoute == searchItem.screen.route
                                NavIconButton(
                                    selected = selected,
                                    selectedIcon = searchItem.selectedIcon,
                                    unselectedIcon = searchItem.unselectedIcon,
                                    label = searchItem.label,
                                    onClick = {
                                        navController.navigate(searchItem.screen.route) {
                                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    }
                                )
                            }
                        }

                        // Search Separado en burbuja independiente cuando se hace scroll (Efecto Gota Líquida)
                        if (isScrolled) {
                            val searchSelected = currentRoute == Screen.Search.route
                            Box(
                                modifier = Modifier
                                    .graphicsLayer {
                                        scaleX = 0.8f + (0.2f * searchSeparation.value)
                                        scaleY = scaleX
                                    }
                                    .size(54.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (searchSelected) Color(0xFF2E2E2E)
                                        else if (liquidGlassEnabled) Color.White.copy(alpha = 0.12f)
                                        else Color(0xFF1C1C1C)
                                    )
                                    .clickable {
                                        navController.navigate(Screen.Search.route) {
                                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (searchSelected) Icons.Filled.Search else Icons.Outlined.Search,
                                    contentDescription = "Search",
                                    tint = if (searchSelected) Color.White else Color(0xFF888888),
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
            composable(Screen.Auth.route) {
                LoginScreen(onContinueAsGuest = { navController.popBackStack() }, onLoginSuccess = { navController.popBackStack() })
            }
            composable(Screen.Home.route) {
                HomeScreen(
                    onSongClick = { song ->
                        PlayerController.init(context)
                        PlayerController.playSong(song)
                        navController.navigate(Screen.Player.route)
                    },
                    onOpenPlayer = { navController.navigate(Screen.Player.route) },
                    onOpenSettings = { navController.navigate(Screen.Settings.route) },
                    onOpenProfile = { navController.navigate(Screen.Auth.route) },
                    onScrollStateChanged = { scrolled -> isScrolled = scrolled }
                )
            }
            composable(Screen.Search.route) {
                SearchScreen(
                    onSongClick = { song ->
                        PlayerController.init(context)
                        PlayerController.playSong(song)
                        navController.navigate(Screen.Player.route)
                    },
                    onPlayQueue = { songs, index ->
                        PlayerController.init(context)
                        PlayerController.playQueue(songs, index)
                        navController.navigate(Screen.Player.route)
                    }
                )
            }
            composable(Screen.Library.route) {
                LibraryScreen(onSongClick = { song ->
                    PlayerController.init(context)
                    PlayerController.playSong(song)
                    navController.navigate(Screen.Player.route)
                })
            }
            composable(Screen.Settings.route) {
                SettingsScreen(onBack = { navController.popBackStack() })
            }
            composable(
                route = Screen.Player.route,
                enterTransition = { slideInVertically(initialOffsetY = { it }, animationSpec = tween(400)) + fadeIn(tween(400)) },
                exitTransition = { slideOutVertically(targetOffsetY = { it }, animationSpec = tween(350)) + fadeOut(tween(350)) }
            ) {
                FullPlayerScreen(
                    onBack = { navController.popBackStack() },
                    onOpenQueue = { navController.navigate(Screen.Queue.route) },
                    onOpenLyrics = { navController.navigate(Screen.Lyrics.route) }
                )
            }
            composable(Screen.Queue.route) { QueueScreen(onBack = { navController.popBackStack() }) }
            composable(Screen.Lyrics.route) { LyricsScreen(onBack = { navController.popBackStack() }) }
        }
    }
}

@Composable
private fun NavIconButton(
    selected: Boolean,
    selectedIcon: ImageVector,
    unselectedIcon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(if (selected) Color(0xFF2E2E2E) else Color.Transparent)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = if (selected) selectedIcon else unselectedIcon,
            contentDescription = label,
            tint = if (selected) Color.White else Color(0xFF888888),
            modifier = Modifier.size(24.dp)
        )
    }
}
