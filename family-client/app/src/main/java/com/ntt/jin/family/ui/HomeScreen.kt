package com.ntt.jin.family.ui

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavController

@Composable
fun HomeScreen(
    navController: NavController,
    homeViewModel: HomeViewModel) {
    val homeUiState = homeViewModel.homeUiState

    when (homeUiState) {
        is HomeUiState.Loading -> {
            Text("Loading...")
        }
        is HomeUiState.Success -> {
            val rooms = homeUiState.rooms
            // Display the list of rooms
            RoomCardGrid(
                items = rooms,
                navController = navController,
                homeViewModel = homeViewModel,
                localUser = homeViewModel.localUser
            )
        }
        is HomeUiState.Error -> {
            val message = (homeUiState as HomeUiState.Error).message
            Text("Error: $message")
        }
    }
}
