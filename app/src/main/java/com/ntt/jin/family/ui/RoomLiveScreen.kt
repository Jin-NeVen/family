package com.ntt.jin.family.ui

import android.view.ViewGroup
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.ntt.jin.family.LocalAppContext
import com.ntt.skyway.core.content.sink.SurfaceViewRenderer

@Composable
fun RoomLiveScreen(
    roomId: String,
    roomViewModel: RoomViewModel
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Text("Room Live Screen: $roomId")
        RoomViewRender()
//        RoomMember(0)
//        RoomMemberList()
    }

}

@Composable
fun RoomViewRender() {
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
        }
    )

}