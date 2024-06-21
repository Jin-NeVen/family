package com.ntt.jin.family.ui

import android.content.Context
import android.graphics.Camera
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.ntt.jin.family.FamilyApplication
import com.ntt.jin.family.data.UserRepository
import com.ntt.skyway.core.content.local.LocalVideoStream
import com.ntt.skyway.core.content.local.source.CameraSource
import com.ntt.skyway.core.content.remote.RemoteVideoStream
import com.ntt.skyway.room.p2p.P2PRoom
import com.ntt.skyway.room.member.RoomMember
import com.ntt.skyway.room.p2p.LocalP2PRoomMember
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DirectChatViewModel(
    private val userRepository: UserRepository,
): ViewModel() {

    companion object {
        val TAG = "DirectChatViewModel"
        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory{
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(
                modelClass: Class<T>,
                extras: CreationExtras
            ): T {
                val application = checkNotNull(extras[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY])
                return DirectChatViewModel(
                    (application as FamilyApplication).userRepository
                ) as T
            }
        }
    }

    var localVideoSources by mutableStateOf(emptyList<String>())
        private set

    var localVideoStream by mutableStateOf<LocalVideoStream?>(null)
        private set

    var remoteVideoStream by mutableStateOf<RemoteVideoStream?>(null)
        private set

    var localP2PRoomMember: LocalP2PRoomMember? = null

    suspend fun chatWith(context: Context, memberName: String) {
        //TODO this should not be a fixed value
        val directChatRoomName = "DirectChatRoom"
        Log.d(TAG, "chat room name: $directChatRoomName")
        val p2pRoom = P2PRoom.findOrCreate(directChatRoomName)
        if (p2pRoom == null) {
            Log.d(TAG, "p2p room not created/found")
            return
        }
        localP2PRoomMember = p2pRoom!!.join(RoomMember.Init(memberName))
        if (localP2PRoomMember == null) {
            Log.d(TAG, "p2p member join failed")
            return
        }
        localP2PRoomMember!!.onLeftHandler = {
            Log.d(TAG, "${localP2PRoomMember!!.name} p2p member left")
        }
        p2pRoom.publications.forEach { publication ->
            if (publication.publisher?.id == localP2PRoomMember!!.id) {
                return@forEach
            }
            val subscription = localP2PRoomMember!!.subscribe(publication)
            remoteVideoStream = subscription?.stream as RemoteVideoStream
        }
        val deviceList = CameraSource.getCameras(context).toList()
        deviceList.forEach {
            Log.d(TAG, "camera list: $it")
        }
        localVideoSources = deviceList
        Log.d(TAG, "member joined")
        val cameraOption = CameraSource.CapturingOptions(800,800)
        CameraSource.startCapturing(context, deviceList.first(), cameraOption)
        localVideoStream = CameraSource.createStream()

        localP2PRoomMember!!.publish(localVideoStream!!)
    }

    fun changeCamera(videoSource: String) {
        CameraSource.changeCamera(videoSource)
    }

    fun exit() {
        CoroutineScope(Dispatchers.IO).launch {
            Log.d(TAG, "exit directChatScreen")
            CameraSource.stopCapturing()
            remoteVideoStream?.removeAllRenderer()
            remoteVideoStream?.dispose()
            localVideoStream?.removeAllRenderer()
            localVideoStream?.dispose()
            if (localP2PRoomMember != null) {
                Log.d(TAG, "call leave room")
                localP2PRoomMember!!.leave()
            } else {
                Log.d(TAG, "localP2PRoomMember is null")
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        Log.d(TAG, "onCleared")
        exit()
    }

}