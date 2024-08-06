package com.ntt.jin.skywaycomposequickstart

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ntt.skyway.core.content.sink.SurfaceViewRenderer



@Composable
fun LiveChatScreen(
    liveChatViewModel: LiveChatViewModel = viewModel( factory = LiveChatViewModel.Factory),
    modifier: Modifier = Modifier
) {
    val localVideoStream = liveChatViewModel.localVideoStream
    var localRenderView by remember { mutableStateOf<SurfaceViewRenderer?>(null) }
    val remoteVideoStream = liveChatViewModel.remoteVideoStream
    var remoteRenderView by remember { mutableStateOf<SurfaceViewRenderer?>(null) }

    Column(modifier = Modifier.fillMaxSize()) {
        Button(
            onClick = {
            liveChatViewModel.startLiveChat()
            },
            modifier = Modifier.padding(16.dp)
        ) {
            Text("Start Live Chat")
        }
        HorizontalDivider()
        Text("Me")
        if (localVideoStream != null) {
            AndroidView(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp)
                    .background(Color.Blue)
                    .padding(16.dp),
                factory = { context ->
                    localRenderView = SurfaceViewRenderer(context)
                    localRenderView!!.apply {
                        setup()
                        localVideoStream.addRenderer(this)
                    }
                },
                update = {
                    localVideoStream.removeRenderer(localRenderView!!)
                    localVideoStream.addRenderer(localRenderView!!)
                }
            )
        } else {
            Text("localVideoStream is null")
        }
        HorizontalDivider()
        Text("Partner")
        if (remoteVideoStream != null) {
            AndroidView(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp)
                    .background(Color.Red)
                    .padding(16.dp),
                factory = { context ->
                    remoteRenderView = SurfaceViewRenderer(context)
                    remoteRenderView!!.apply {
                        setup()
                        remoteVideoStream.addRenderer(this)
                    }
                },
                update = {
                    remoteVideoStream.removeRenderer(remoteRenderView!!)
                    remoteVideoStream.addRenderer(remoteRenderView!!)
                }
            )
        } else {
            Text("remoteVideoStream is null")
        }
        HorizontalDivider()
    }

}