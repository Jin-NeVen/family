package com.ntt.jin.family.ui

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
import com.ntt.skyway.core.content.remote.RemoteVideoStream
import com.ntt.skyway.room.member.RoomMember
import com.ntt.skyway.room.sfu.LocalSFURoomMember
import com.ntt.skyway.room.sfu.SFURoom
import kotlinx.coroutines.launch

class RoomViewModel(
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
                    (application as FamilyApplication).userRepository,
                ) as T
            }
        }
    }

    var sfuRoomName: String = ""
    var sfuRoom: SFURoom? = null

    private var localSfuRoomMember: LocalSFURoomMember? = null

    var remoteRoomVideoStream by mutableStateOf<RemoteVideoStream?>(null)
        private set

    var roomMembers by mutableStateOf(emptyList<RoomMember>())
        private set

    fun joinRoom(roomName: String, memberName: String) {
        sfuRoomName = roomName
        viewModelScope.launch {
            //get the SFURoom again for member to join.
            sfuRoom = SFURoom.find(name = roomName)
            if (sfuRoom == null) {
                Log.d(TAG, "real room $roomName not found")
                return@launch
            }
            localSfuRoomMember = sfuRoom!!.join(RoomMember.Init(memberName))
            if (localSfuRoomMember == null) {
                Log.d(TAG, "member $memberName join room failed")
                return@launch
            }
            Log.d(
                HomeViewModel.TAG,
                "room ${roomName} found, member ${localSfuRoomMember!!.name ?: "Anonymous"} joined."
            )
            setupSfuRoomHandler()

            roomMembers = sfuRoom!!.members.filter { it.name != localSfuRoomMember!!.name && it.name != sfuRoomName}

            sfuRoom!!.publications.forEach { publication ->
                if (publication.contentType == Stream.ContentType.VIDEO) {
                    val videoSubscription = localSfuRoomMember!!.subscribe(publication)
                    if (videoSubscription != null) {
                        remoteRoomVideoStream = videoSubscription.stream as RemoteVideoStream
                    }
                    Log.d(TAG, "video subscribed.subscriber: ${localSfuRoomMember!!.name}")
                }
            }
            sfuRoom!!.onMemberListChangedHandler = {
                Log.d(TAG, "member list changed, member count is ${sfuRoom!!.members.size}")
                roomMembers = sfuRoom!!.members.filter { it.name != localSfuRoomMember!!.name && it.name != sfuRoomName}
            }

        }
    }

    fun leaveRoom() {
        viewModelScope.launch() {
            if (localSfuRoomMember != null) {
                Log.d(TAG, "leave room")
                localSfuRoomMember!!.leave()
            } else {
                Log.d(TAG, "localSfuRoomMember is null")
            }
        }
    }

    private fun setupSfuRoomHandler() {
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
//                    localSfuRoomMember!!.subscribe(publication)
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
            //update state
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

    override fun onCleared() {
        super.onCleared()
        Log.d(TAG, "onCleared")

    }
}