package com.ntt.jin.family.ui

import android.content.Context
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.ntt.jin.family.DebugInfo
import com.ntt.jin.family.FamilyApplication
import com.ntt.jin.family.data.AuthTokenRepository
import com.ntt.jin.family.data.HomeRepository
import com.ntt.jin.family.data.RoomListRepository
import com.ntt.jin.family.data.User
import com.ntt.jin.family.data.UserRepository
import com.ntt.skyway.core.SkyWayContext
import com.ntt.skyway.core.util.Logger
import com.ntt.skyway.room.sfu.SFURoom
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * HomeViewModelのscopeはMainActivity
 * MainActivity完全破棄されるまでHomeViewModelが生存し続ける
 */
class HomeViewModel(
    private val homeRepository: HomeRepository,
    private val userRepository: UserRepository,
    private val authTokenRepository: AuthTokenRepository,
    private val roomListRepository: RoomListRepository,
    private val applicationContext: Context
) : ViewModel(){
    var isSkyWayInitialized by mutableStateOf(false)

    var homeUiState by mutableStateOf<HomeUiState>(HomeUiState.Loading)
        private set

    var onlineSfuRoomList by mutableStateOf<List<SFURoom>>(emptyList())
        private set

    //TODO create the localUser in data layer.
    lateinit var localUser: User

    init {
        viewModelScope.launch {
            localUser = userRepository.getLocalUser()
            setupSkyWayContext(applicationContext)
            Log.d(TAG, "HomeViewModel init")
        }
    }

    private suspend fun setupSkyWayContext(applicationContext: Context) {
        // ServerよりSkyWay Auth Tokenを取得し、SkyWayContext.Optionsにセット
        val option = authTokenRepository.getAuthToken()?.let {
            SkyWayContext.Options(
                authToken = it,
                logLevel = Logger.LogLevel.VERBOSE
            )
        }
        if (option == null) {
            Log.d("App", "skyway setup failed")
        }
        SkyWayContext.onErrorHandler = { error ->
            Log.d("App", "skyway setup failed: ${error.message}")
        }
        isSkyWayInitialized =  SkyWayContext.setup(applicationContext, option!!)
        if (isSkyWayInitialized) {
            Log.d("App", "skyway setup succeed")
        }
    }

    private suspend fun loadRooms() {
        try {
            homeUiState = HomeUiState.Loading
            val rooms = homeRepository.getRooms()
            homeUiState = HomeUiState.Success(rooms)
            Log.d(TAG, "room count: ${onlineSfuRoomList.size}")
        } catch (e: Exception) {
            homeUiState = HomeUiState.Error("Failed to load rooms")
        }
    }

    fun startRoomsStateChecker() {
        Log.d(TAG, "startRoomsStateChecker")
        viewModelScope.launch(Dispatchers.IO) {
            DebugInfo.logCurrentThreadInfo(TAG, "room state checker")
            loadRooms()
            checkRoomsState()
            //TODO this would cause the unexpected behavior on DirectChatScreen.
//            while (true) {
//                checkRoomsState()
//                // 60秒（60000ミリ秒）待機
//                // NOTICE change this value to your desired interval
//                delay(60000L)
//            }
        }
    }

    private suspend fun checkRoomsState() {
        val roomNameList = roomListRepository.getRoomNameList()
        val tempList = roomNameList.mapNotNull { roomName ->
            SFURoom.find(name = roomName)
        }
        if (onlineSfuRoomList.size != tempList.size ||
            onlineSfuRoomList.sortedBy { it.id } == tempList.sortedBy { it.id }) {
            onlineSfuRoomList = tempList
        }
    }

    fun leaveRoom() {
        viewModelScope.launch {
            localUser.localSFURoomMember?.leave()
        }
    }

    override fun onCleared() {
        super.onCleared()
        SkyWayContext.dispose()
        Log.d(TAG, "onCleared")
    }

    companion object {
        const val TAG = "HomeViewModel"
        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory{
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(
                modelClass: Class<T>,
                extras: CreationExtras
            ): T {
                val application = checkNotNull(extras[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY])
                return HomeViewModel(
                    (application as FamilyApplication).homeRepository,
                    application.userRepository,
                    application.authTokenRepository,
                    application.roomListRepository,
                    application
                ) as T
            }
        }
    }
}