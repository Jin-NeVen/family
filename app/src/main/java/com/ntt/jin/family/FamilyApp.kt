package com.ntt.jin.family

import android.annotation.SuppressLint
import android.content.Context
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.ntt.jin.family.ui.HomeScreen
import com.ntt.jin.family.ui.HomeViewModel

// CompositionLocal for accessing the application context, global
val LocalAppContext = compositionLocalOf<Context> { error("LocalAppContext not initialized") }


@Composable
fun FamilyApp(homeViewModel: HomeViewModel) {
    val navController = rememberNavController()
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

