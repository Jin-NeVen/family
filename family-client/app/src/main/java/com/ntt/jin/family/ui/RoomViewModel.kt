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
import com.ntt.jin.family.FamilyApplication
import com.ntt.jin.family.data.RoomRepository
import com.ntt.jin.family.data.RoomRepositoryImpl
import com.ntt.jin.family.data.UserRepository
import com.ntt.jin.family.ui.HomeViewModel.Companion
import com.ntt.skyway.core.channel.Subscription
import com.ntt.skyway.core.content.Encoding
import com.ntt.skyway.core.content.Stream
import com.ntt.skyway.core.content.local.LocalVideoStream
import com.ntt.skyway.core.content.local.source.CameraSource
import com.ntt.skyway.core.content.local.source.VideoSource
import com.ntt.skyway.core.content.remote.RemoteVideoStream
import com.ntt.skyway.room.RoomPublication
import com.ntt.skyway.room.RoomSubscription
import com.ntt.skyway.room.member.RoomMember
import com.ntt.skyway.room.sfu.LocalSFURoomMember
import com.ntt.skyway.room.sfu.SFURoom
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class CameraType {
    FOREGROUND, BACKGROUND
}

class RoomViewModel(
    private val roomRepository: RoomRepository,
    private val userRepository: UserRepository
): ViewModel() {

    companion object {
        val TAG = "RoomViewModel"

        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory{
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(
                modelClass: Class<T>,
                extras: CreationExtras
            ): T {
                val application = checkNotNull(extras[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY])
                return RoomViewModel(
                    RoomRepositoryImpl(),
                    (application as FamilyApplication).userRepository,
                ) as T
            }
        }
    }

    var remoteRoomVideoStream by mutableStateOf<RemoteVideoStream?>(null)
        private set

    var sfuRoom: SFURoom? = null

    var roomMembers by mutableStateOf(emptyList<RoomMember>())
        private set

    //created by SFURoom.find(),
    private val _sfuRoom = MutableStateFlow<SFURoom?>(null)
//    val sfuRoom: StateFlow<SFURoom?> = _sfuRoom

    private var localSfuRoomMember: LocalSFURoomMember? = null

    //created by sfuRoom.join
    private val _localSfuRoomMember = MutableStateFlow<LocalSFURoomMember?>(null)
//    val localSfuRoomMember: StateFlow<LocalSFURoomMember?> = _localSfuRoomMember

    private val _videoResources = MutableStateFlow<Set<String>>(emptySet())
    val videoResources: StateFlow<Set<String>> = _videoResources

    //create by CameraSource.createdStream,
    //used by localSfuRoomMember.publish
    private val _localVideoStream = MutableStateFlow<LocalVideoStream?>(null)
    val localVideoStream: StateFlow<LocalVideoStream?> = _localVideoStream.asStateFlow()

//    private val _remoteRoomVideoStream = MutableStateFlow<RemoteVideoStream?>(null)
//    val remoteRoomVideoStream: StateFlow<RemoteVideoStream?> = _remoteRoomVideoStream.asStateFlow()



    private var publication: RoomPublication? = null

    fun getVideoSources(context: Context) {
        _videoResources.value = CameraSource.getCameras(context)
    }

    private fun getCameraDeviceName(cameraType: CameraType = CameraType.BACKGROUND): String {
        //TODO
        return _videoResources.value.first()
    }

    fun createLocalVideoStream(context: Context) {
        val cameraDeviceName = getCameraDeviceName()
        CameraSource.startCapturing(
            context,
            cameraDeviceName,
            //TODO maybe we need to get screen size here for showing full screen video
            CameraSource.CapturingOptions(160, 90)
            )
        _localVideoStream.value = CameraSource.createStream()
    }

    fun leaveRoom() {

    }

    fun switchCamera() {

    }

    fun subscribeRoomLive(roomName: String) {
        Log.d(TAG, "subscribeRoomLive is called" )
        viewModelScope.launch {
            val sfuRoom = SFURoom.find(name = roomName)
            if (sfuRoom == null) {
                Log.d(TAG, "room $roomName not found")
                return@launch
            }
            val roomMemberMe = userRepository.getLocalUser().localSFURoomMember
//            val roomMemberMe = sfuRoom.join(RoomMember.Init(userRepository.getLocalUser().name))
            if (roomMemberMe == null) {
                Log.d(TAG, "member me join room failed")
                return@launch
            }

            userRepository.setLocalUserCurrentRoom(sfuRoom)
            userRepository.setLocalUserCurrentMember(roomMemberMe)
            sfuRoom.publications.forEach { publication ->
                if (publication.contentType == Stream.ContentType.VIDEO) {
                    val videoSubscription = roomMemberMe.subscribe(publication)
                    if (videoSubscription != null) {
                        remoteRoomVideoStream = videoSubscription.stream as RemoteVideoStream
                    }
                    Log.d(TAG, "video subscribed.subscriber: ${roomMemberMe.name}")
                } else if (publication.contentType == Stream.ContentType.AUDIO) {
                    // skip subscribing the audio since it would make terrible noise
//                    roomMemberMe.subscribe(publication)
                    Log.d(TAG, "audio subscribed. subscriber: ${roomMemberMe.name}")
                }
            }
            sfuRoom.onMemberListChangedHandler = {
                Log.d(TAG, "member list changed, member count is ${sfuRoom.members.size}")
                roomMembers = sfuRoom.members.filter { it.name != roomMemberMe.name }
            }
            roomMembers = sfuRoom.members.filter { it.name != roomMemberMe.name }
        }
    }

    suspend fun publishAvStream() {
        val encodingLow = Encoding("low", 10_000, 10.0)
        val encodingHigh = Encoding("high", 200_000, 1.0)
        val options = RoomPublication.Options(encodings =  mutableListOf(encodingLow, encodingHigh))

        publication = _localVideoStream.value?.let { localSfuRoomMember?.publish(it, options) }

        publication?.onEnabledHandler = {
            Log.d(TAG, "publish enabled")
        }

        publication?.onDisabledHandler = {
            Log.d(TAG, "publish disabled")
        }
    }

    override fun onCleared() {
        super.onCleared()
        Log.d(TAG, "onCleared")
        viewModelScope.launch() {
            if (localSfuRoomMember != null) {
                Log.d(TAG, "leave room")
                localSfuRoomMember!!.leave()
            } else {
                Log.d(TAG, "localSfuRoomMember is null")
            }
        }
    }
}