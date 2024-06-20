package com.ntt.jin.family.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.ntt.jin.family.LocalAppContext
import com.ntt.skyway.core.content.sink.SurfaceViewRenderer

@Composable
fun DirectChatScreen(
    memberName: String,
    directChatViewModel: DirectChatViewModel = viewModel(factory = DirectChatViewModel.Factory),
    navController: NavHostController) {
    var expanded by remember { mutableStateOf(false) }
    var selectedOption by remember { mutableStateOf("Select Camera") }
    val context = LocalAppContext.current
    val localVideoSources = directChatViewModel.localVideoSources
    val localVideoStream = directChatViewModel.localVideoStream
    val remotedVideoStream = directChatViewModel.remoteVideoStream
    LaunchedEffect(Unit) {
        directChatViewModel.chatWith(context, memberName)
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Text("Direct Chat Room: $memberName")
        Box(modifier = Modifier.fillMaxSize()) {
            remotedVideoStream?.let {
                val renderView = SurfaceViewRenderer(context)
                AndroidView(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(600.dp)
                        .background(Color.Red)
                        .padding(16.dp),
                    factory = { renderView },
                ) { view ->
                    renderView.setup()
                    it.addRenderer(view)
                }
            }
            Column(modifier = Modifier.align(Alignment.BottomEnd)) {
                Text(text = "Selected: $selectedOption")
                Button(onClick = { expanded = true }) {
                    Text(text = "Show Menu")
                }
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = {
                        expanded = false
                    }
                ) {
                    localVideoSources.forEach { videoSource ->
                        DropdownMenuItem(
                            text = { Text(videoSource) },
                            onClick = {
                                selectedOption = videoSource
                                expanded = false
                                directChatViewModel.changeCamera(videoSource)
                            }
                        )
                    }
                }
                localVideoStream?.let {
                    val renderView = SurfaceViewRenderer(context)
                    AndroidView(
                        modifier = Modifier
                            .width(200.dp)
                            .height(300.dp)
                            .background(Color.Red)
                            .padding(16.dp),
                        factory = { renderView },
                    ) { view ->
                        renderView.setup()
                        it.addRenderer(view)
                    }
                }
            }

        }
        Box(
            modifier = Modifier
                .width(200.dp)
                .align(Alignment.Start)
        ) {

        }
    }
}