package com.ntt.jin.family.ui

import android.content.Context
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.ntt.jin.family.DebugInfo
import com.ntt.jin.family.FamilyApplication
import com.ntt.jin.family.data.AuthTokenRepository
import com.ntt.jin.family.data.HomeRepository
import com.ntt.jin.family.data.RoomListRepository
import com.ntt.jin.family.data.User
import com.ntt.jin.family.data.UserRepository
import com.ntt.jin.family.ui.RoomViewModel.Companion
import com.ntt.skyway.core.SkyWayContext
import com.ntt.skyway.core.content.Stream
import com.ntt.skyway.core.content.local.source.CameraSource
import com.ntt.skyway.core.content.remote.RemoteVideoStream
import com.ntt.skyway.core.util.Logger
import com.ntt.skyway.room.Room
import com.ntt.skyway.room.RoomPublication
import com.ntt.skyway.room.sfu.SFURoom
import com.ntt.skyway.room.member.RoomMember
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.random.Random

class HomeViewModel(
    private val homeRepository: HomeRepository,
    private val userRepository: UserRepository,
    private val authTokenRepository: AuthTokenRepository,
    private val roomListRepository: RoomListRepository,
) : ViewModel(){
    var isSkyWayInitialized by mutableStateOf(false)

    var homeUiState by mutableStateOf<HomeUiState>(HomeUiState.Loading)
        private set

    var onlineSfuRoomList by mutableStateOf<List<SFURoom>>(emptyList())
        private set

    var joinedRoomName by mutableStateOf("")
        private set

    //TODO create the localUser in data layer.
    lateinit var localUser: User

    init {
        viewModelScope.launch {
            localUser = userRepository.getLocalUser()
        }
    }

    fun setupSkyWayContext(applicationContext: Context) {
        viewModelScope.launch {
            // ServerよりSkyWay Auth Tokenを取得し、SkyWayContext.Optionsにセット
            val option = authTokenRepository.getAuthToken()?.let {
                SkyWayContext.Options(
                    authToken = it,
                    logLevel = Logger.LogLevel.VERBOSE
                )
            }

            // SkyWay Auth Token取得失敗した場合、SkyWayの初期化は行わない
            // 必要に応じてAuth Token再取得するるか、エラーメッセージをユーザに表示するか
            if (option == null) {
                Log.d("App", "skyway setup failed")
            }
            isSkyWayInitialized =  SkyWayContext.setup(applicationContext, option!!, onErrorHandler = { error ->
                Log.d("App", "skyway setup failed: ${error.message}")
            })
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
            //Check this later.
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

    //TODO maybe we should move this to RoomViewModel
    fun joinRoom(roomName: String, memberName: String) {
        viewModelScope.launch {
            //get the SFURoom again for member to join.
            val room = SFURoom.find(name = roomName)
            if (room == null) {
                Log.d(TAG, "real room $roomName not found")
                return@launch
            }
            val localSfuRoomMember = room.join(RoomMember.Init(memberName))
            if (localSfuRoomMember == null) {
                Log.d(TAG, "member $memberName join room failed")
                return@launch
            }
            Log.d(
                TAG,
                "room ${room.name} found, member ${localSfuRoomMember!!.name ?: "Anonymous"} joined."
            )
            localUser.joinedRoom = room
            localUser.localSFURoomMember = localSfuRoomMember
            setupSfuRoomHandler()
            //this is used by Navigation.
            joinedRoomName = roomName
        }
    }

    fun leaveRoom() {
        viewModelScope.launch {
            joinedRoomName = ""
            localUser.localSFURoomMember?.leave()
        }
    }

    private fun setupSfuRoomHandler() {
        if (localUser.joinedRoom == null) {
            //TODO join room failed
            return
        }
        if (localUser.localSFURoomMember == null) {
            Log.d(TAG, "localSfuRoomMember is null")
            return
        }
        localUser.joinedRoom!!.publications.forEach { publication ->
            Log.d(TAG, "publication: ${publication.id} ${publication.contentType.name}")
        }
        localUser.joinedRoom!!.members.forEach { roomMember ->
            if (roomMember.id == localUser.localSFURoomMember!!.id) {
                Log.d(TAG, "local member name: ${roomMember.name}, id: ${roomMember.id}")
                roomMember.publications.forEach { publication ->
                    Log.d(TAG, "local publication: ${publication.id} ${publication.contentType.name}")
//                    localUser.localSFURoomMember!!.subscribe(publication)
                }
            } else {
                Log.d(TAG, "remote member name: ${roomMember.name}, id: ${roomMember.id}")
                roomMember.publications.forEach { publication ->
                    Log.d(TAG, "remote publication: ${publication.id} ${publication.contentType.name}")
                }
            }
            Log.d(TAG, "member: ${roomMember.name}")
        }
        localUser.joinedRoom!!.subscriptions.forEach { subscription ->
            Log.d(TAG, "subscription: ${subscription.publication.id}")
        }
        localUser.joinedRoom!!.onMemberListChangedHandler = {
            Log.d(TAG, "member list changed, member count is ${localUser.joinedRoom!!.members.size}")
            localUser.joinedRoom!!.members.forEach { roomMember ->
                Log.d(TAG, "member: ${roomMember.name}")
            }
        }
        localUser.joinedRoom!!.onMemberJoinedHandler = { roomMember ->
            Log.d(TAG, "member joined: ${roomMember.name}")
        }
        localUser.joinedRoom!!.onMemberLeftHandler = { roomMember ->
            Log.d(TAG, "member left: ${roomMember.name}")
        }
        localUser.joinedRoom!!.onClosedHandler = {
            Log.d(TAG, "room closed")
        }
        localUser.joinedRoom!!.onErrorHandler = { error ->
            Log.d(TAG, "room error: ${error.message}")
        }
        localUser.joinedRoom!!.onPublicationEnabledHandler = { publication ->
            Log.d(TAG, "publication enabled: ${publication.id}")
        }
        localUser.joinedRoom!!.onPublicationDisabledHandler = { publication ->
            Log.d(TAG, "publication disabled: ${publication.id}")
        }
        localUser.joinedRoom!!.onPublicationListChangedHandler = {
            Log.d(TAG, "publication list changed, publication count is ${localUser.joinedRoom!!.publications.size}")
        }
        localUser.joinedRoom!!.onPublicationSubscribedHandler = { subscription ->
            Log.d(TAG, "publication subscribed: ${subscription.publication.id}")
        }
        localUser.joinedRoom!!.onPublicationUnsubscribedHandler = { subscription ->
            Log.d(TAG, "publication unsubscribed: ${subscription.publication.id}")
        }
        localUser.joinedRoom!!.onStreamPublishedHandler = { stream ->
            Log.d(TAG, "stream published: ${stream.id} stream type: ${stream.contentType}")
        }
        localUser.joinedRoom!!.onStreamUnpublishedHandler = { stream ->
            Log.d(TAG, "stream unpublished: ${stream.id}")
        }
        localUser.joinedRoom!!.onSubscriptionListChangedHandler = {
            Log.d(TAG, "subscription list changed, subscription count is ${localUser.joinedRoom!!.subscriptions.size}")
        }

        localUser.localSFURoomMember!!.onStreamPublishedHandler = { stream ->
            Log.d(TAG, "localSfuRoomMember stream published: ${stream.id}")
        }
        localUser.localSFURoomMember!!.onStreamUnpublishedHandler = { stream ->
            Log.d(TAG, "localSfuRoomMember stream unpublished: ${stream.id}")
        }
        localUser.localSFURoomMember!!.onSubscriptionListChangedHandler = {
            Log.d(TAG, "localSfuRoomMember subscription list changed, subscription count is ${localUser.localSFURoomMember!!.subscriptions.size}")
        }
        localUser.localSFURoomMember!!.onLeftHandler = {
            Log.d(TAG, "localSfuRoomMember member left")
            //update state
            joinedRoomName = ""
        }
        localUser.localSFURoomMember!!.onMetadataUpdatedHandler = { metadata ->
            Log.d(TAG, "localSfuRoomMember metadata updated: ${metadata.toString()}")
        }
        localUser.localSFURoomMember!!.onPublicationListChangedHandler = {
            Log.d(TAG, "localSfuRoomMember publication list changed, publication count is ${localUser.localSFURoomMember!!.publications.size}")
        }
        localUser.localSFURoomMember!!.onPublicationSubscribedHandler = { publication ->
            Log.d(TAG, "localSfuRoomMember publication subscribed: ${publication.id}")
        }
        localUser.localSFURoomMember!!.onPublicationUnsubscribedHandler = { publication ->
            Log.d(TAG, "localSfuRoomMember publication unsubscribed: ${publication.id}")
        }
    }

    override fun onCleared() {
        super.onCleared()
        SkyWayContext.dispose()
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
                    application.roomListRepository
                ) as T
            }
        }
    }
}