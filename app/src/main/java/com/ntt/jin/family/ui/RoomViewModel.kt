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

class RoomViewModel(
    private val roomRepository: RoomRepository
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
                    (application as FamilyApplication).roomRepository
                ) as T
            }
        }

    }

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
                Log.d(TAG, "room $roomName not found")
                return@launch
            }

            localSfuRoomMember = sfuRoom!!.join(RoomMember.Init(memberName))
            if (localSfuRoomMember == null) {
                Log.d(TAG, "member $memberName join room failed")
                return@launch
            }
            Log.d(TAG, "room ${sfuRoom!!.name} found, member ${localSfuRoomMember!!.name?:"Anonymous"} joined.")
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
        if (localSfuRoomMember == null) {
            Log.d(TAG, "localSfuRoomMember is null")
            return
        }
        sfuRoom!!.publications.forEach { publication ->
            Log.d(TAG, "publication: ${publication.id} ${publication.contentType.name}")
        }
        sfuRoom!!.members.forEach { roomMember ->
            if (roomMember.id == localSfuRoomMember!!.id) {
                Log.d(TAG, "local member name: ${roomMember.name}, id: ${roomMember.id}")
                roomMember.publications.forEach { publication ->
                    Log.d(TAG, "local publication: ${publication.id} ${publication.contentType.name}")
                }
            } else {
                Log.d(TAG, "remote member name: ${roomMember.name}, id: ${roomMember.id}")
                roomMember.publications.forEach { publication ->
                    Log.d(TAG, "remote publication: ${publication.id} ${publication.contentType.name}")
                }
            }
            Log.d(TAG, "member: ${roomMember.name}")
        }
        sfuRoom!!.subscriptions.forEach { subscription ->
            Log.d(TAG, "subscription: ${subscription.publication.id}")
        }
        sfuRoom!!.onMemberListChangedHandler = {
            Log.d(TAG, "member list changed, member count is ${sfuRoom!!.members.size}")
            sfuRoom!!.members.forEach { roomMember ->
                Log.d(TAG, "member: ${roomMember.name}")
            }
        }
        sfuRoom!!.onMemberJoinedHandler = { roomMember ->
            Log.d(TAG, "member joined: ${roomMember.name}")
        }
        sfuRoom!!.onMemberLeftHandler = { roomMember ->
            Log.d(TAG, "member left: ${roomMember.name}")
        }
        sfuRoom!!.onClosedHandler = {
            Log.d(TAG, "room closed")
        }
        sfuRoom!!.onErrorHandler = { error ->
            Log.d(TAG, "room error: ${error.message}")
        }
        sfuRoom!!.onPublicationEnabledHandler = { publication ->
            Log.d(TAG, "publication enabled: ${publication.id}")
        }
        sfuRoom!!.onPublicationDisabledHandler = { publication ->
            Log.d(TAG, "publication disabled: ${publication.id}")
        }
        sfuRoom!!.onPublicationListChangedHandler = {
            Log.d(TAG, "publication list changed, publication count is ${sfuRoom!!.publications.size}")
        }
        sfuRoom!!.onPublicationSubscribedHandler = { subscription ->
            Log.d(TAG, "publication subscribed: ${subscription.publication.id}")
        }
        sfuRoom!!.onPublicationUnsubscribedHandler = { subscription ->
            Log.d(TAG, "publication unsubscribed: ${subscription.publication.id}")
        }
        sfuRoom!!.onStreamPublishedHandler = { stream ->
            Log.d(TAG, "stream published: ${stream.id} stream type: ${stream.contentType}")
        }
        sfuRoom!!.onStreamUnpublishedHandler = { stream ->
            Log.d(TAG, "stream unpublished: ${stream.id}")
        }
        sfuRoom!!.onSubscriptionListChangedHandler = {
            Log.d(TAG, "subscription list changed, subscription count is ${sfuRoom!!.subscriptions.size}")
        }

        localSfuRoomMember!!.onStreamPublishedHandler = { stream ->
            Log.d(TAG, "localSfuRoomMember stream published: ${stream.id}")
        }
        localSfuRoomMember!!.onStreamUnpublishedHandler = { stream ->
            Log.d(TAG, "localSfuRoomMember stream unpublished: ${stream.id}")
        }
        localSfuRoomMember!!.onSubscriptionListChangedHandler = {
            Log.d(TAG, "localSfuRoomMember subscription list changed, subscription count is ${localSfuRoomMember!!.subscriptions.size}")
        }
        localSfuRoomMember!!.onLeftHandler = {
            Log.d(TAG, "localSfuRoomMember member left")
        }
        localSfuRoomMember!!.onMetadataUpdatedHandler = { metadata ->
            Log.d(TAG, "localSfuRoomMember metadata updated: ${metadata.toString()}")
        }
        localSfuRoomMember!!.onPublicationListChangedHandler = {
            Log.d(TAG, "localSfuRoomMember publication list changed, publication count is ${localSfuRoomMember!!.publications.size}")
        }
        localSfuRoomMember!!.onPublicationSubscribedHandler = { publication ->
            Log.d(TAG, "localSfuRoomMember publication subscribed: ${publication.id}")
        }
        localSfuRoomMember!!.onPublicationUnsubscribedHandler = { publication ->
            Log.d(TAG, "localSfuRoomMember publication unsubscribed: ${publication.id}")
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
            Log.d(TAG, "publish enabled")
        }

        publication?.onDisabledHandler = {
            Log.d(TAG, "publish disabled")
        }
    }
}