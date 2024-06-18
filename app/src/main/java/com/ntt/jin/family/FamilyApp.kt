package com.ntt.jin.family

import android.annotation.SuppressLint
import android.content.Context
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.ntt.jin.family.ui.HomeScreen
import com.ntt.jin.family.ui.HomeViewModel
import com.ntt.jin.family.ui.RoomViewModel

// CompositionLocal for accessing the application context, global
val LocalAppContext = compositionLocalOf<Context> { error("LocalAppContext not initialized") }

//Notice Since We need to share RoomViewModel in different screens, we use CompositionLocal to pass it
val LocalRoomViewModel = compositionLocalOf<RoomViewModel> { error("LocalRoomViewModel not initialized") }


@Composable
fun FamilyApp(homeViewModel: HomeViewModel) {
    val navController = rememberNavController()
    CompositionLocalProvider(
        LocalRoomViewModel provides viewModel( factory = RoomViewModel.Factory),
    ) {
        Scaffold(
            bottomBar = { BottomNavigationBar(navController) },
            content = { innerPadding ->
                FamilyNavigation(
                    homeViewModel = homeViewModel,
                    navController = navController,
                    innerPadding = innerPadding
                )
            }
        )
    }
}

