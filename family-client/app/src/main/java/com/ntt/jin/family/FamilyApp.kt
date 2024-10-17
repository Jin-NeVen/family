package com.ntt.jin.family

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.ntt.jin.family.ui.HomeViewModel

// CompositionLocal for accessing the application context, global
val LocalAppContext = compositionLocalOf<Context> { error("LocalAppContext not initialized") }

//Notice Since We need to share RoomViewModel in different screens, we use CompositionLocal to pass it
val LocalHomeViewModel = compositionLocalOf<HomeViewModel> { error("LocalHomeViewModel not initialized") }


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun FamilyApp(homeViewModel: HomeViewModel) {
    val navController = rememberNavController()
    CompositionLocalProvider(
        LocalHomeViewModel provides viewModel( factory = HomeViewModel.Factory),
    ) {
        FamilyNavigation(
            homeViewModel = homeViewModel,
            navController = navController,
            modifier = Modifier.fillMaxSize()
        )
    }

}

