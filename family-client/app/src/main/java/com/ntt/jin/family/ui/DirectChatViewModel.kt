package com.ntt.jin.family.ui

import android.content.Context
import android.graphics.Camera
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import com.ntt.jin.family.FamilyApplication
import com.ntt.jin.family.data.UserRepository
import com.ntt.skyway.core.content.local.LocalVideoStream
import com.ntt.skyway.core.content.local.source.CameraSource
import com.ntt.skyway.room.p2p.P2PRoom
import com.ntt.skyway.room.member.RoomMember

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

    var remoteVideoStream by mutableStateOf<LocalVideoStream?>(null)
        private set

    suspend fun chatWith(context: Context, memberName: String) {
        val directChatRoomName = "${userRepository.getLocalUser().name}-${memberName}"
        Log.d(TAG, "chat room name: $directChatRoomName")
        val p2pRoom = P2PRoom.findOrCreate(directChatRoomName)
        if (p2pRoom == null) {
            Log.d(TAG, "p2p room not created/found")
            return
        }
        val localP2PRoomMember = p2pRoom!!.join(RoomMember.Init(memberName))
        if (localP2PRoomMember == null) {
            Log.d(TAG, "p2p member join failed")
            return
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
    }

    fun changeCamera(videoSource: String) {
        CameraSource.changeCamera(videoSource)
    }

}