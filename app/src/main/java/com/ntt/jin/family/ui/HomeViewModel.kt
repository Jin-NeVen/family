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
import com.ntt.jin.family.data.AuthTokenRepository
import com.ntt.jin.family.data.HomeRepository
import com.ntt.jin.family.data.User
import com.ntt.jin.family.data.UserRepository
import com.ntt.skyway.core.SkyWayContext
import com.ntt.skyway.core.util.Logger
import com.ntt.skyway.room.sfu.SFURoom
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlin.random.Random

class HomeViewModel(
    private val homeRepository: HomeRepository,
    private val userRepository: UserRepository,
    private val authTokenRepository: AuthTokenRepository
) : ViewModel(){
//    private val _homeUiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
//    val homeUiState: StateFlow<HomeUiState> = _homeUiState
    var homeUiState by mutableStateOf<HomeUiState>(HomeUiState.Loading)
        private set

    var roomJoined by mutableStateOf(false)
        private set

    //TODO create the localUser in data layer.
    lateinit var localUser: User

    init {
        viewModelScope.launch {
            localUser = userRepository.getLocalUser()
        }
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