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
import com.ntt.jin.family.SkyWayCoroutineScope
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DirectChatViewModel(
    private val userRepository: UserRepository,
    private val skyWayDefaultScope: SkyWayCoroutineScope
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
                    (application as FamilyApplication).userRepository,
                    (application as FamilyApplication).skyWayDefaultScope

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

    var autoPublish by mutableStateOf(true)
        private set

    var p2pRoom: P2PRoom? = null
    var localP2PRoomMember: LocalP2PRoomMember? = null

    var localMemberName by mutableStateOf<String?>(null)
        private set

    fun updateLocalMemberName() {
        viewModelScope.launch {
            localMemberName = userRepository.getLocalUser().name
        }
    }

    private suspend fun captureLocalVideoSteam(context: Context) {
        Log.d(TAG, "captureLocalVideoSteam")
        val cameraList = CameraSource.getCameras(context).toList()
        cameraList.forEach {
            Log.d(TAG, "camera list: $it")
        }
        localVideoSources = cameraList
        val cameraOption = CameraSource.CapturingOptions(800,800)
        CameraSource.startCapturing(context, cameraList.first(), cameraOption)
        withContext(Dispatchers.Main) {
            localVideoStream = CameraSource.createStream()
        }
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
        if (p2pRoom == null) {
            Log.d(TAG, "p2p room not created/found")
            return
        }
        p2pRoom!!.onMemberLeftHandler = { member ->
            Log.d(TAG, "${member.name} p2p member left")
        }
        p2pRoom!!.onPublicationListChangedHandler = {
            Log.d(TAG, "publication list changed")
        }
        p2pRoom!!.onSubscriptionListChangedHandler = {
            Log.d(TAG, "subscription list changed")
        }
        p2pRoom!!.onStreamUnpublishedHandler = {
            Log.d(TAG, "p2pRoom streamUnpublishedHandler stream unpublished: ${it.id}")
        }
    }

    // remoteMemberName is not used now.
    private suspend fun createLocalMember(remoteMemberName: String) {
        if (p2pRoom == null) {
            Log.d(TAG, "p2p room not created/found")
            return
        }
        val localMemberName = userRepository.getLocalUser().name
        localP2PRoomMember = p2pRoom!!.join(RoomMember.Init(localMemberName))
        if (localP2PRoomMember == null) {
            Log.d(TAG, "p2p member join failed")
        }
        Log.d(TAG, "p2p member $localMemberName joined")
    }

    fun publishLocalAVStream() {
        viewModelScope.launch {
            publishLocalAVStreamInternal()
        }
    }

    private suspend fun publishLocalAVStreamInternal() {
        if (localP2PRoomMember == null) {
            Log.d(TAG, "localP2PRoomMember is null")
            return
        }
        if (localVideoStream != null) {
            Log.d(TAG, "localP2PRoomMember publish video")
            val publication = localP2PRoomMember!!.publish(localVideoStream!!)
            if (publication == null) {
                Log.d(TAG, "localP2PRoomMember publish video failed")
            } else {
                Log.d(TAG, "localP2PRoomMember publish video succeed")
                publication.onConnectionStateChangedHandler = {
                    Log.d(TAG, "localP2PRoomMember publish video connection state changed: $it")
                }
                publication.onSubscribedHandler = {
                    Log.d(TAG, "localP2PRoomMember publish video subscribed")
                }
                publication.onUnsubscribedHandler = {
                    Log.d(TAG, "publication onUnsubscribedHandler: localP2PRoomMember publish video unsubscribed")
                }
            }

        }
        if (localAudioStream != null) {
            Log.d(TAG, "localP2PRoomMember publish audio")
            val publication = localP2PRoomMember!!.publish(localAudioStream!!)
            if (publication == null) {
                Log.d(TAG, "localP2PRoomMember publish audio failed")
            } else {
                Log.d(TAG, "localP2PRoomMember publish audio succeed")
                publication.onConnectionStateChangedHandler = {
                    Log.d(TAG, "localP2PRoomMember publish audio connection state changed: $it")
                }
                publication.onSubscribedHandler = {
                    Log.d(TAG, "localP2PRoomMember publish audio subscribed")
                }
                publication.onUnsubscribedHandler = {
                    Log.d(TAG, "localP2PRoomMember publish audio unsubscribed")
                }
            }
        }
    }

    private var streamPublishedHandler: ((publication: RoomPublication) -> Unit)? = {
        Log.d(TAG, "gonna to subscribe stream by P2PRoom's streamPublishedHandler): publication id:${it.id}, publisher name: ${it.publisher?.name}")
        subscribeRemoteAVStreamInternal(it)
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

    private fun subscribeRemoteAVStream() {
        if (p2pRoom == null) {
            Log.d(TAG, "p2p room not created/found")
            return
        }
        p2pRoom!!.publications.forEach { publication ->
            Log.d(TAG, "gonna to subscribe  ${publication.publisher?.name} 's ${publication.stream?.contentType} stream directly by p2pRoom publications id: ${publication.id},")
            subscribeRemoteAVStreamInternal(publication)
        }
        p2pRoom!!.onStreamPublishedHandler = streamPublishedHandler
        if (localP2PRoomMember == null) {
            return
        }
        localP2PRoomMember!!.onStreamUnpublishedHandler = Handler@{
            Log.d(TAG, "localP2PRoomMember streamUnpublishedHandler stream unpublished: ${it.id}")

            it.stream?.let { stream ->
                if (stream.contentType == Stream.ContentType.VIDEO) {
                    remoteVideoStream?.removeAllRenderer()
                    remoteVideoStream?.dispose()
                    remoteVideoStream = null
                    Log.d(TAG, "remoteVideoStream disposed")
                } else if (stream.contentType == Stream.ContentType.AUDIO) {
                    remoteAudioStream?.dispose()
                    remoteAudioStream = null
                    Log.d(TAG, "remoteAudioStream disposed")
                }
            }
        }
    }

    fun startDirectChat(context: Context, remoteMemberName: String) {
        viewModelScope.launch {
            createRoom()
            createLocalMember(remoteMemberName)
            captureLocalVideoSteam(context)
            captureLocalAudioStream()
            if (autoPublish) {
                publishLocalAVStreamInternal()
            }
            subscribeRemoteAVStream()
        }
    }

    fun changeCamera(videoSource: String) {
        CameraSource.changeCamera(videoSource)
    }

    private suspend fun unpublishAll() {
        withContext(Dispatchers.Default) {
            if (localP2PRoomMember == null) {
                return@withContext
            }
            localP2PRoomMember!!.publications.forEach { roomPublication ->
                Log.d(TAG, "unpublish: ${roomPublication.id}")
                localP2PRoomMember!!.unpublish(roomPublication)
            }
        }
    }

    fun unpublishVideo() {
        viewModelScope.launch (Dispatchers.Default) {
            if (localP2PRoomMember == null) {
                return@launch
            }
            localP2PRoomMember!!.publications.forEach { roomPublication ->
                if (roomPublication.stream?.contentType == Stream.ContentType.VIDEO) {
                    Log.d(TAG, "unpublish video: ${roomPublication.id}")
                    localP2PRoomMember!!.unpublish(roomPublication)
                }

            }
        }
    }

    private fun stopLocalStream() {
        CameraSource.stopCapturing()
        AudioSource.stop()
        localVideoStream?.dispose()
        localVideoStream = null
        localAudioStream?.dispose()
        localAudioStream = null
    }

    private suspend fun unSubscribe(subscriptionId: String) {
        withContext(Dispatchers.Default) {
            if (localP2PRoomMember == null) {
                return@withContext
            }
            localP2PRoomMember!!.subscriptions.forEach { roomSubscription ->
                if (roomSubscription.id == subscriptionId) {
                    Log.d(TAG, "unsubscribe: ${roomSubscription.id}")
                    localP2PRoomMember!!.unsubscribe(roomSubscription.id)
                }
            }
        }
    }



    private fun stopRemoteStream() {
        remoteVideoStream?.dispose()
        remoteVideoStream = null
        remoteAudioStream?.dispose()
        remoteAudioStream = null
    }

    private suspend fun leaveRoom() {
        withContext(Dispatchers.Default) {
            if (localP2PRoomMember == null) {
                return@withContext
            }
            if (localP2PRoomMember!!.leave()) {
                localP2PRoomMember = null
                stopLocalStream()
                stopRemoteStream()
                Log.d(TAG, "localP2PRoomMember left")
            } else {
                Log.d(TAG, "localP2PRoomMember leave failed")
            }
        }
    }

    fun disposeRoom() {
        p2pRoom?.dispose()
    }

    fun closeRoom() {
        viewModelScope.launch {
            p2pRoom?.close()
        }
    }

    /**
     * NOTICE!!!
     * localP2PRoomMember.leave()　APIは suspend 関数のため、coroutineの生成が必要です。
     * ですが、viewModelScopeのonCleared()関数でviewModelScope.launchによる作れたcoroutineがキャンセルされるため
     * 別のCoroutineScopeを利用する必要があります。
     */
    override fun onCleared() {
        super.onCleared()
        Log.d(TAG, "onCleared")
        skyWayDefaultScope.launch {
            Log.d(TAG, "exit directChatScreen")
            leaveRoom()
        }
    }
}