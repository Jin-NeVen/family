package com.ntt.jin.family.ui

import android.content.Context
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ntt.skyway.core.content.Encoding
import com.ntt.skyway.core.content.local.LocalVideoStream
import com.ntt.skyway.core.content.local.source.CameraSource
import com.ntt.skyway.core.content.local.source.VideoSource
import com.ntt.skyway.room.RoomPublication
import com.ntt.skyway.room.member.RoomMember
import com.ntt.skyway.room.sfu.LocalSFURoomMember
import com.ntt.skyway.room.sfu.SFURoom
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class CameraType {
    FOREGROUND, BACKGROUND
}

class RoomViewModel: ViewModel() {

    var roomJoined by mutableStateOf(false)
        private set

    var sfuRoom: SFURoom? = null

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
    val localVideoStream: MutableStateFlow<LocalVideoStream?> = _localVideoStream


    private var publication: RoomPublication? = null

    fun getVideoSources(context: Context) {
        _videoResources.value = CameraSource.getCameras(context)
    }

    fun joinRoom(roomName: String, memberName: String) {
        viewModelScope.launch {
            //TODO this is not good.
            sfuRoom = SFURoom.find(name = roomName)
            if (sfuRoom == null) {
                //TODO join room failed
                return@launch
            }

            localSfuRoomMember = sfuRoom!!.join(com.ntt.skyway.room.member.RoomMember.Init(memberName))
            if (localSfuRoomMember == null) {
                return@launch
            }
            Log.d("App", "room joined")
            roomJoined = true
            setupSfuRoomHandler()
        }
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

    fun setupSfuRoomHandler() {
        if (sfuRoom == null) {
            //TODO join room failed
            return
        }
        sfuRoom!!.publications.forEach { publication ->
            Log.d("App", "publication: ${publication.id}")
        }
        sfuRoom!!.members.forEach { roomMember ->
            Log.d("App", "member: ${roomMember.name}")
        }
        sfuRoom!!.subscriptions.forEach { subscription ->
            Log.d("App", "subscription: ${subscription.publication.id}")
        }
        sfuRoom!!.onMemberListChangedHandler = {
            Log.d("App", "member list changed, member count is ${sfuRoom!!.members.size}")
            sfuRoom!!.members.forEach { roomMember ->
                Log.d("App", "member: ${roomMember.name}")
            }
        }
        sfuRoom!!.onMemberJoinedHandler = { roomMember ->
            Log.d("App", "member joined: ${roomMember.name}")
        }
        sfuRoom!!.onMemberLeftHandler = { roomMember ->
            Log.d("App", "member left: ${roomMember.name}")
        }
        sfuRoom!!.onClosedHandler = {
            Log.d("App", "room closed")
        }
        sfuRoom!!.onErrorHandler = { error ->
            Log.d("App", "room error: ${error.message}")
        }
        sfuRoom!!.onPublicationEnabledHandler = { publication ->
            Log.d("App", "publication enabled: ${publication.id}")
        }
        sfuRoom!!.onPublicationDisabledHandler = { publication ->
            Log.d("App", "publication disabled: ${publication.id}")
        }
        sfuRoom!!.onPublicationListChangedHandler = {
            Log.d("App", "publication list changed, publication count is ${sfuRoom!!.publications.size}")
        }
        sfuRoom!!.onPublicationSubscribedHandler = { subscription ->
            Log.d("App", "publication subscribed: ${subscription.publication.id}")
        }
        sfuRoom!!.onPublicationUnsubscribedHandler = { subscription ->
            Log.d("App", "publication unsubscribed: ${subscription.publication.id}")
        }
        sfuRoom!!.onStreamPublishedHandler = { stream ->
            Log.d("App", "stream published: ${stream.id}")
        }
        sfuRoom!!.onStreamUnpublishedHandler = { stream ->
            Log.d("App", "stream unpublished: ${stream.id}")
        }
        sfuRoom!!.onSubscriptionListChangedHandler = {
            Log.d("App", "subscription list changed, subscription count is ${sfuRoom!!.subscriptions.size}")
        }
    }

    fun leaveRoom() {

    }

    fun switchCamera() {

    }

    suspend fun publishAvStream() {
        val encodingLow = Encoding("low", 10_000, 10.0)
        val encodingHigh = Encoding("high", 200_000, 1.0)
        val options = RoomPublication.Options(encodings =  mutableListOf(encodingLow, encodingHigh))

        publication = _localVideoStream.value?.let { localSfuRoomMember?.publish(it, options) }

        publication?.onEnabledHandler = {
            Log.d("App", "publish enabled")
        }

        publication?.onDisabledHandler = {
            Log.d("App", "publish disabled")
        }
    }

}