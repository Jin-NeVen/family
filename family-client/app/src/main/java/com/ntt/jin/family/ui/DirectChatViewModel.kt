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
import com.ntt.skyway.core.content.Stream
import com.ntt.skyway.core.content.local.LocalAudioStream
import com.ntt.skyway.core.content.local.LocalVideoStream
import com.ntt.skyway.core.content.local.source.AudioSource
import com.ntt.skyway.core.content.local.source.CameraSource
import com.ntt.skyway.core.content.remote.RemoteAudioStream
import com.ntt.skyway.core.content.remote.RemoteVideoStream
import com.ntt.skyway.room.RoomPublication
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

    var localAudioStream by mutableStateOf<LocalAudioStream?>(null)
        private set

    var remoteVideoStream by mutableStateOf<RemoteVideoStream?>(null)
        private set
    var remoteAudioStream by mutableStateOf<RemoteAudioStream?>(null)
        private set

    var p2pRoom: P2PRoom? = null
    var localP2PRoomMember: LocalP2PRoomMember? = null

    private fun captureLocalVideoSteam(context: Context) {
        Log.d(TAG, "captureLocalVideoSteam")
        val cameraList = CameraSource.getCameras(context).toList()
        cameraList.forEach {
            Log.d(TAG, "camera list: $it")
        }
        localVideoSources = cameraList
        val cameraOption = CameraSource.CapturingOptions(800,800)
        CameraSource.startCapturing(context, cameraList.first(), cameraOption)
        localVideoStream = CameraSource.createStream()
    }
    private fun captureLocalAudioStream() {
        Log.d(TAG, "captureLocalAudioStream")
        AudioSource.start()
        localAudioStream = AudioSource.createStream()
    }

    private suspend fun createRoom() {
        Log.d(TAG, "createRoom")
        val directChatRoomName = "DirectChatRoom"
        p2pRoom = P2PRoom.findOrCreate(directChatRoomName)
    }

    private suspend fun createLocalMember(localMemberName: String) {
        if (p2pRoom == null) {
            Log.d(TAG, "p2p room not created/found")
            return
        }
        localP2PRoomMember = p2pRoom!!.join(RoomMember.Init(localMemberName))
        if (localP2PRoomMember == null) {
            Log.d(TAG, "p2p member join failed")
        }
        Log.d(TAG, "p2p member $localMemberName joined")
    }

    private suspend fun publishLocalAVStream() {
        if (localP2PRoomMember == null) {
            Log.d(TAG, "localP2PRoomMember is null")
            return
        }
        localP2PRoomMember!!.publish(localVideoStream!!)
        localP2PRoomMember!!.publish(localAudioStream!!)
        Log.d(TAG, "localP2PRoomMember published AVStream")
    }

    private var streamPublishedHandler: ((publication: RoomPublication) -> Unit)? = {
        Log.d(TAG, " subscribe stream by P2PRoom's streamPublishedHandler): ${it.id}")
        subscribeRemoteAVStreamInternal(it)
    }

    private fun subscribeRemoteAVStreamInternal(publication: RoomPublication) {
        viewModelScope.launch {
            if (p2pRoom == null || localP2PRoomMember == null) {
                return@launch
            }
            if (publication.publisher?.id == localP2PRoomMember!!.id) {
                return@launch
            }
            Log.d(TAG, "subscribe stream: ${publication.id}")
            val subscription = localP2PRoomMember!!.subscribe(publication)
            subscription?.stream?.let { stream ->
                if (stream.contentType == Stream.ContentType.VIDEO) {
                    remoteVideoStream = subscription.stream as RemoteVideoStream
                }
                if (stream.contentType == Stream.ContentType.AUDIO) {
                    remoteAudioStream = subscription.stream as RemoteAudioStream
                }
            }
        }

    }

    private fun subscribeRemoteAVStream() {
        if (p2pRoom == null) {
            Log.d(TAG, "p2p room not created/found")
            return
        }
        p2pRoom!!.publications.forEach { publication ->
            Log.d(TAG, "subscribe stream directly by p2pRoom publications: ${publication.id}")
            subscribeRemoteAVStreamInternal(publication)
        }
        p2pRoom!!.onStreamPublishedHandler = streamPublishedHandler
        if (localP2PRoomMember == null) {
            return
        }
        localP2PRoomMember!!.onStreamUnpublishedHandler = Handler@{
            Log.d(TAG, "streamUnpublishedHandler stream unpublished: ${it.id}")
            it.stream?.let { stream ->
                if (stream.contentType == Stream.ContentType.VIDEO) {
                    remoteVideoStream?.removeAllRenderer()
                    remoteVideoStream?.dispose()
                    remoteVideoStream = null
                } else if (stream.contentType == Stream.ContentType.AUDIO) {
                    remoteAudioStream?.dispose()
                    remoteAudioStream = null
                }
            }
        }
    }

    fun startDirectChat(context: Context, memberName: String) {
        viewModelScope.launch {
            createRoom()
            createLocalMember(memberName)
            captureLocalVideoSteam(context)
            captureLocalAudioStream()
            publishLocalAVStream()
            subscribeRemoteAVStream()
        }
    }

    suspend fun chatWith(context: Context, memberName: String) {
        //TODO this should not be a fixed value
        val directChatRoomName = "DirectChatRoom"
        Log.d(TAG, "chat room name: $directChatRoomName")
        p2pRoom = P2PRoom.findOrCreate(directChatRoomName)
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
        p2pRoom!!.publications.forEach { publication ->
            if (publication.publisher?.id == localP2PRoomMember!!.id) {
                return@forEach
            }
            val subscription = localP2PRoomMember!!.subscribe(publication)
            subscription?.stream?.let { stream ->
                if (stream.contentType == Stream.ContentType.VIDEO) {
                    remoteVideoStream = subscription.stream as RemoteVideoStream
                }
                if (stream.contentType == Stream.ContentType.AUDIO) {
                    remoteAudioStream = subscription.stream as RemoteAudioStream
                }
            }
        }
        val cameraList = CameraSource.getCameras(context).toList()
        cameraList.forEach {
            Log.d(TAG, "camera list: $it")
        }
        localVideoSources = cameraList
        Log.d(TAG, "member joined")
        val cameraOption = CameraSource.CapturingOptions(800,800)
        CameraSource.startCapturing(context, cameraList.first(), cameraOption)
        localVideoStream = CameraSource.createStream()

        localP2PRoomMember!!.publish(localVideoStream!!)
    }

    fun changeCamera(videoSource: String) {
        CameraSource.changeCamera(videoSource)
    }

    private fun cleanUp() {
        CoroutineScope(Dispatchers.Default).launch {
            Log.d(TAG, "exit directChatScreen")
            CameraSource.stopCapturing()
            AudioSource.stop()
            if (localP2PRoomMember != null) {
                localP2PRoomMember!!.publications.forEach { roomPublication ->
                    Log.d(TAG, "unpublish: ${roomPublication.id}")
                    localP2PRoomMember!!.unpublish(roomPublication)
                }
                localP2PRoomMember!!.subscriptions.forEach { roomSubscription ->
                    Log.d(TAG, "unsubscribe: ${roomSubscription.id}")
                    localP2PRoomMember!!.unsubscribe(roomSubscription.id)
                }
            }
            remoteVideoStream?.removeAllRenderer()
            remoteVideoStream?.dispose()
            remoteVideoStream = null
            remoteAudioStream?.dispose()
            remoteAudioStream = null
            localVideoStream?.removeAllRenderer()
            localVideoStream?.dispose()
            localVideoStream = null
            localAudioStream?.dispose()
            localAudioStream = null
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
        cleanUp()
    }

}