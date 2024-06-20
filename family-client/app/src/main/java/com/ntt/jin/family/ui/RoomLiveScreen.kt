package com.ntt.jin.family.ui

import android.view.ViewGroup
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavHostController
import com.ntt.jin.family.LocalAppContext
import com.ntt.skyway.core.content.remote.RemoteVideoStream
import com.ntt.skyway.core.content.sink.SurfaceViewRenderer
import com.ntt.skyway.room.RoomSubscription

@Composable
fun RoomLiveScreen(
    roomName: String,
    roomViewModel: RoomViewModel,
    navController: NavHostController
) {
    val context = LocalAppContext.current
    val remoteVideoStream = roomViewModel.remoteRoomVideoStream
    val memberList = roomViewModel.roomMembers
    LaunchedEffect(remoteVideoStream) {
        roomViewModel.subscribeRoomLive(roomName)
    }
    Column(modifier = Modifier.fillMaxSize()) {
        Text("Room Live Screen: $roomName")
        remoteVideoStream?.let {
            val renderView = SurfaceViewRenderer(context)
            AndroidView(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp)
                    .background(Color.Red)
                    .padding(16.dp),
                factory = { renderView },
            ) { view ->
                renderView.setup()
                it.addRenderer(view)
            }
        }
        LazyColumn {
            items(memberList) { member ->
                MemberItem(
                    member = member,
                    onClick = {
                        navController.navigate("member/${member.name}")
                    }
                )
            }
        }
    }

//        if (roomViewModel.roomVideoSubscription != null) {
//            RoomViewRender(roomViewModel.roomVideoSubscription!!)
//        }
//        RoomMember(0)
//        RoomMemberList()
//    }

}

@Composable
fun RoomViewRender(roonVideoSubscription: RoomSubscription) {
    val context = LocalAppContext.current
    AndroidView(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .background(Color.Red)
            .padding(16.dp),
        factory = {
            SurfaceViewRenderer(context).apply {
                layoutParams = ViewGroup.LayoutParams(
                    (ViewGroup.LayoutParams.MATCH_PARENT),
                    (ViewGroup.LayoutParams.MATCH_PARENT)
                )
            }
        },
        update = {
            it.setup()
            (roonVideoSubscription.stream as RemoteVideoStream).addRenderer(it)
        }
    )

}