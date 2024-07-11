package com.ntt.jin.family.ui

import android.util.Log
import androidx.activity.compose.BackHandler
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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.ntt.jin.family.LocalAppContext
import com.ntt.jin.family.ui.DirectChatViewModel.Companion.TAG
import com.ntt.skyway.core.content.sink.SurfaceViewRenderer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun DirectChatScreen(
    remoteMemberName: String,
    directChatViewModel: DirectChatViewModel = viewModel(factory = DirectChatViewModel.Factory),
    navController: NavHostController)
{

    var expanded by remember { mutableStateOf(false) }
    var selectedOption by remember { mutableStateOf("Select Camera") }
    val applicationContext = LocalAppContext.current.applicationContext
    val context = LocalContext.current
    val localVideoSources = directChatViewModel.localVideoSources
    val localVideoStream = directChatViewModel.localVideoStream
    val remotedVideoStream = directChatViewModel.remoteVideoStream
    val localMemberName = directChatViewModel.localMemberName
    var localRenderView by remember { mutableStateOf<SurfaceViewRenderer?>(null) }
    var remoteRenderView by remember { mutableStateOf<SurfaceViewRenderer?>(null) }
    LaunchedEffect(Unit) {
        Log.d(TAG, "DirectChatScreen launched")
        directChatViewModel.startDirectChat(applicationContext, remoteMemberName)
        directChatViewModel.updateLocalMemberName()
    }

    DisposableEffect(Unit) {
        onDispose {
            Log.d(TAG, "DirectChatScreen onDispose")
            if (localRenderView != null) {
                localRenderView!!.dispose()
                localRenderView = null
            }
            if (remoteRenderView != null) {
                remoteRenderView!!.dispose()
                remoteRenderView = null
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Text("Direct Chat Room's localMember: $localMemberName")
        Text("Direct Chat Room's remoteMember: $remoteMemberName")
        if (localVideoStream != null) {
            AndroidView(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp)
                    .background(Color.Blue)
                    .padding(16.dp),
                factory = { context ->
                    Log.d(TAG, "create SurfaceViewRenderer")
                    localRenderView = SurfaceViewRenderer(context)
                    localRenderView!!.apply {
                        setup()
                        localVideoStream.addRenderer(this)
                    }
                },
                update = {
                    Log.d(TAG, "update SurfaceViewRenderer")
                    localVideoStream.removeRenderer(localRenderView!!)
                    localVideoStream.addRenderer(localRenderView!!)
                }
            )
        } else {
            Text("localVideoStream is null")
        }

        if (remotedVideoStream != null) {
            AndroidView(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp)
                    .background(Color.Red)
                    .padding(16.dp),
                factory = { context ->
                    Log.d(TAG, "create SurfaceViewRenderer")
                    remoteRenderView = SurfaceViewRenderer(context)
                    remoteRenderView!!.apply {
                        setup()
                        remotedVideoStream.addRenderer(this)
                    }
                },
                update = {
                    Log.d(TAG, "update SurfaceViewRenderer")
                    remotedVideoStream.removeRenderer(remoteRenderView!!)
                    remotedVideoStream.addRenderer(remoteRenderView!!)
                }
            )
        } else {
            Text("remoteVideoStream is null")
        }

        Row() {
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
        }
    }
}