package com.ntt.jin.family

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navArgument
import com.ntt.jin.family.ui.HomeScreen
import com.ntt.jin.family.ui.HomeViewModel
import com.ntt.jin.family.ui.RoomLiveScreen

@Composable
fun BottomNavigationBar(navController: NavHostController) {
    val items = listOf("Home", "Contacts")
    NavigationBar {
        items.forEach { screen ->
            NavigationBarItem(
                selected = currentRoute(navController) == screen,
                label = { Text(screen) },
                icon = {
                    Icon(
                        imageVector = when (screen) {
                            "Home" -> Icons.Filled.Home
                            "Contacts" -> Icons.Filled.Person
                            else -> Icons.Filled.Home
                        },
                        contentDescription = null
                    )
                },
                onClick = {
                    navController.navigate(screen) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
            )
        }
    }
}

@Composable
fun FamilyNavigation(
    homeViewModel: HomeViewModel,
    navController: NavHostController,
    innerPadding: PaddingValues) {
    NavHost(
        navController = navController,
        startDestination = "home",
        modifier = Modifier.padding(innerPadding)
    ) {
        composable("home") {
            HomeScreen(
                homeViewModel = homeViewModel,
                navController = navController,
            )
        }
        composable("contacts") {
            // Display the contacts screen
        }
        composable("room/{roomName}") { backStackEntry ->
            val roomName = backStackEntry.arguments?.getString("roomName")
            // Display the room details screen
            requireNotNull(roomName)
            RoomLiveScreen(
                roomName = roomName,
                roomViewModel = LocalRoomViewModel.current
            )
        }
    }
}

@Composable
fun currentRoute(navController: NavHostController): String? {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    return navBackStackEntry?.destination?.route
}