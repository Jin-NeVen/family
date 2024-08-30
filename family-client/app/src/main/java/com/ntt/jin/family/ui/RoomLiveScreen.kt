package com.ntt.jin.family.ui

import android.view.ViewGroup
import androidx.activity.compose.BackHandler
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.ntt.jin.family.LocalAppContext
import com.ntt.jin.family.LocalHomeViewModel
import com.ntt.skyway.core.content.sink.SurfaceViewRenderer

@Composable
fun RoomLiveScreen(
    roomViewModel: RoomViewModel,
    navController: NavHostController
) {
    BackHandler {
        roomViewModel.leaveRoom()
        navController.navigate("home")
    }
    val context = LocalAppContext.current
    val remoteVideoStream = roomViewModel.remoteRoomVideoStream
    val memberList = roomViewModel.roomMembers

    Column(modifier = Modifier.fillMaxSize()) {
        Text("Room Live Screen: ${roomViewModel.roomName}")
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
}