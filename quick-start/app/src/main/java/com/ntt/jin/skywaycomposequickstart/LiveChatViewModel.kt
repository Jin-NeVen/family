package com.ntt.jin.skywaycomposequickstart

import android.content.Context
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.ntt.skyway.core.content.Stream
import com.ntt.skyway.core.content.local.LocalAudioStream
import com.ntt.skyway.core.content.local.LocalVideoStream
import com.ntt.skyway.core.content.local.source.AudioSource
import com.ntt.skyway.core.content.local.source.CameraSource
import com.ntt.skyway.core.content.remote.RemoteAudioStream
import com.ntt.skyway.core.content.remote.RemoteVideoStream
import com.ntt.skyway.room.RoomPublication
import com.ntt.skyway.room.member.RoomMember
import com.ntt.skyway.room.p2p.LocalP2PRoomMember
import com.ntt.skyway.room.p2p.P2PRoom
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LiveChatViewModel(
    val applicationContext: Context,
    val skyWayDefaultScope: SkyWayCoroutineScope
    ): ViewModel()  {
    companion object {
        private const val TAG = "LiveChatViewModel"
        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory{
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(
                modelClass: Class<T>,
                extras: CreationExtras
            ): T {
                val application = checkNotNull(extras[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY])
                return LiveChatViewModel(
                    applicationContext = application.applicationContext,
                    (application as MainApplication).skyWayDefaultScope,
                ) as T
            }
        }
    }

    var p2pRoom: P2PRoom? = null
    var localP2PRoomMember: LocalP2PRoomMember? = null

    var localVideoStream by mutableStateOf<LocalVideoStream?>(null)
        private set

    var localAudioStream by mutableStateOf<LocalAudioStream?>(null)
        private set

    var remoteAudioStream by mutableStateOf<RemoteAudioStream?>(null)
        private set

    var remoteVideoStream by mutableStateOf<RemoteVideoStream?>(null)
        private set

    fun startLiveChat() {
        viewModelScope.launch {
            createRoom()
            createLocalMember()
            captureLocalVideoStream()
            captureLocalAudioStream()
            publishLocalAVStream()
            subscribeRemoteAVStream()
        }
    }
    private suspend fun createRoom() {
        val directChatRoomName = "DirectChatRoom"
        p2pRoom = P2PRoom.findOrCreate(directChatRoomName)
    }

    private suspend fun createLocalMember() {
        if (p2pRoom == null) {
            Log.d(TAG, "p2p room not created/found")
            return
        }
        val localMemberName = "User-" + String.randomString(3)
        localP2PRoomMember = p2pRoom!!.join(RoomMember.Init(localMemberName))
        if (localP2PRoomMember == null) {
            Log.d(TAG, "p2p member join failed")
        }
    }

    private suspend fun captureLocalVideoStream() {
        val cameraList = CameraSource.getCameras(applicationContext).toList()
        val cameraOption = CameraSource.CapturingOptions(800, 800)
        CameraSource.startCapturing(applicationContext, cameraList.first(), cameraOption)
        withContext(Dispatchers.Main) {
            localVideoStream = CameraSource.createStream()
        }
    }

    private fun captureLocalAudioStream() {
        AudioSource.start()
        localAudioStream = AudioSource.createStream()
    }

    private suspend fun publishLocalAVStream() {
        if (localP2PRoomMember == null) {
            Log.d(TAG, "localP2PRoomMember is null")
            return
        }
        localP2PRoomMember!!.publish(localVideoStream!!)
        localP2PRoomMember!!.publish(localAudioStream!!)
    }

    private fun subscribeRemoteAVStreamInternal(publication: RoomPublication) {
        viewModelScope.launch {
            if (p2pRoom == null || localP2PRoomMember == null) {
                return@launch
            }
            if (publication.publisher?.id == localP2PRoomMember!!.id) {
                Log.d(TAG, "cancel this subscription since it is local publication.publisher name: ${publication.publisher?.name}")
                return@launch
            }
            Log.d(TAG, "localP2PRoomMember start to subscribe. publisher name: ${publication.publisher?.name} publication id: ${publication.id}, publication stream type: ${publication.stream?.contentType}")
            val subscription = localP2PRoomMember!!.subscribe(publication)
            if (subscription == null) {
                Log.d(TAG, "subscription is null")
                return@launch
            }
            if (subscription.stream == null) {
                Log.d(TAG, "subscription stream is null")
                return@launch
            }
            subscription.stream?.let { stream ->
                Log.d(TAG, "localP2PRoomMember subscription finished. subscription id: ${subscription.id}, subscription stream id: ${stream.id}, steam type: ${stream.contentType}")
                if (stream.contentType == Stream.ContentType.VIDEO) {
                    withContext(Dispatchers.Main) {
                        remoteVideoStream = subscription.stream as RemoteVideoStream
                    }
                }
                if (stream.contentType == Stream.ContentType.AUDIO) {
                    remoteAudioStream = subscription.stream as RemoteAudioStream
                }
            }
        }
    }

    private var onStreamPublishedHandler: ((publication: RoomPublication) -> Unit)? = {
        Log.d(TAG, "onStreamPublishedHandler: ${it.publisher?.name}")
        subscribeRemoteAVStreamInternal(it)
    }
    fun subscribeRemoteAVStream() {
        if (p2pRoom == null) {
            Log.d(TAG, "p2p room not created/found")
            return
        }
        p2pRoom!!.publications.forEach { publication ->
            Log.d(TAG, "subscribeRemoteAVStream: ${publication.publisher?.name}")
            subscribeRemoteAVStreamInternal(publication)
        }
        p2pRoom!!.onStreamPublishedHandler = onStreamPublishedHandler

    }
}