package com.ntt.jin.family

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import com.ntt.jin.family.ui.DirectChatScreen
import com.ntt.jin.family.ui.HomeScreen
import com.ntt.jin.family.ui.HomeViewModel
import com.ntt.jin.family.ui.RoomLiveScreen
import com.ntt.jin.family.ui.RoomViewModel
import com.ntt.jin.family.ui.RoomViewModelFactory

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

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun FamilyNavigation(
    homeViewModel: HomeViewModel,
    navController: NavHostController,
    modifier: Modifier) {
//    val parentEntry = remember(navController.getBackStackEntry("home"))
    NavHost(
        navController = navController,
        startDestination = "home",
        modifier = modifier.padding()
    ) {
        composable("home") {
            Log.d("FamilyNavigation", "home")
            HomeScreen(
                homeViewModel = homeViewModel,
                navController = navController,
            )
        }
        composable("contacts") {
            // Display the contacts screen
        }
        composable("room/{roomName}") { backStackEntry ->
            Log.d("FamilyNavigation", "room name: ${backStackEntry.arguments?.getString("roomName")}")
            val roomName = backStackEntry.arguments?.getString("roomName")
            // Display the room details screen
            requireNotNull(roomName)
            val roomViewModel = viewModel<RoomViewModel>(
                factory = RoomViewModelFactory(roomName)
            )
            RoomLiveScreen(
                roomViewModel = roomViewModel,
                navController = navController,
                modifier = modifier,
            )
        }
        composable("member/{memberName}") { backStackEntry ->
            Log.d("FamilyNavigation", "member name: ${backStackEntry.arguments?.getString("memberName")}")
            val memberName = backStackEntry.arguments?.getString("memberName")
            // Display the room details screen
            requireNotNull(memberName)
            DirectChatScreen(
                memberName,
                navController = navController,
            )
        }

    }
}

@Composable
fun currentRoute(navController: NavHostController): String? {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    return navBackStackEntry?.destination?.route
}