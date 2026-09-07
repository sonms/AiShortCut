package com.sonms.aishortcut.presentation.main

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.sonms.aishortcut.core.designsystem.AiShortCutTheme
import com.sonms.aishortcut.presentation.discover.DiscoverScreen
import com.sonms.aishortcut.presentation.home.HomeScreen
import com.sonms.aishortcut.presentation.saved.SavedScreen
import org.koin.compose.KoinApplication
import org.koin.core.KoinApplication
import org.koin.dsl.koinConfiguration

private enum class TopDestination(val route: String, val label: String, val icon: ImageVector) {
    Home("home", "Home", Icons.Outlined.Home),
    Discover("discover", "Discover", Icons.Outlined.Search),
    Saved("saved", "Saved", Icons.Outlined.FavoriteBorder),
}

@Composable
fun MainApp() {
    KoinApplication(
        configuration = koinConfiguration(declaration = { modules(appModule) }),
        content = {
            AiShortCutTheme {
                val navController = rememberNavController()
                val currentRoute by navController.currentBackStackEntryAsState()
                val selectedRoute = currentRoute?.destination?.route

                Scaffold(
                    bottomBar = {
                        NavigationBar {
                            TopDestination.entries.forEach { dest ->
                                NavigationBarItem(
                                    selected = selectedRoute == dest.route,
                                    onClick = {
                                        navController.navigate(dest.route) {
                                            popUpTo(navController.graph.findStartDestination().id) {
                                                saveState = true
                                            }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    },
                                    icon = { Icon(dest.icon, contentDescription = dest.label) },
                                    label = { Text(dest.label) },
                                )
                            }
                        }
                    },
                ) { padding ->
                    NavHost(
                        navController = navController,
                        startDestination = TopDestination.Home.route,
                        modifier = Modifier.fillMaxSize().padding(padding),
                    ) {
                        composable(TopDestination.Home.route) { HomeScreen() }
                        composable(TopDestination.Discover.route) { DiscoverScreen() }
                        composable(TopDestination.Saved.route) { SavedScreen() }
                    }
                }
            }
        })
}
