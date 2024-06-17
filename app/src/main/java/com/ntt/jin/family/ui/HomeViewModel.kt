package com.ntt.jin.family.ui

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.ntt.jin.family.FamilyApplication
import com.ntt.jin.family.data.HomeRepository
import com.ntt.skyway.room.sfu.SFURoom
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class HomeViewModel(private val homeRepository: HomeRepository) : ViewModel(){
    private val _homeUiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val homeUiState: StateFlow<HomeUiState> = _homeUiState

    var roomJoined by mutableStateOf(false)
        private set

    fun loadRooms() {
        viewModelScope.launch {
            try {
                _homeUiState.value = HomeUiState.Loading
                val rooms = homeRepository.getRooms()
                _homeUiState.value = HomeUiState.Success(rooms)
            } catch (e: Exception) {
                _homeUiState.value = HomeUiState.Error("Failed to load rooms")
            }
        }
    }



    companion object {
        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory{
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(
                modelClass: Class<T>,
                extras: CreationExtras
            ): T {
                val application = checkNotNull(extras[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY])

                val homeRepository = (application as FamilyApplication).homeRepository
                return HomeViewModel(
                    homeRepository,
                ) as T
            }
        }
    }
}