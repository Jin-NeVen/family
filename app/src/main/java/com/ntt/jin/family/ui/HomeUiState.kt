package com.ntt.jin.family.ui

import com.ntt.jin.family.data.Room

sealed class HomeUiState {
    data object Loading : HomeUiState()
    data class Success(val rooms: List<Room>) : HomeUiState()
    data class Error(val message: String) : HomeUiState()
}

