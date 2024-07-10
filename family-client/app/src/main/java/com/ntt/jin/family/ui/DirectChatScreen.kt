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
    LaunchedEffect(Unit) {
//        //NOTICE if we run this in main thread, when server response gets latency, it would cause crash
//        withContext(Dispatchers.Default) {
//            directChatViewModel.chatWith(context, memberName)
//        }
        Log.d(TAG, "DirectChatScreen launched")
        directChatViewModel.startDirectChat(applicationContext, remoteMemberName)
        directChatViewModel.updateLocalMemberName()
    }

    DisposableEffect(Unit) {
        onDispose {
            Log.d(TAG, "DirectChatScreen onDispose")
        }
    }


    Column(modifier = Modifier.fillMaxSize()) {
        Text("Direct Chat Room's localMember: $localMemberName")
        Text("Direct Chat Room's remoteMember: $remoteMemberName")
        localVideoStream?.let {
            val renderView = SurfaceViewRenderer(context)
            AndroidView(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp)
                    .background(Color.Blue)
                    .padding(16.dp),
                factory = { renderView },
            ) { view ->
                renderView.setup()
                it.addRenderer(view)
            }
        }
        remotedVideoStream?.let {
            val renderView = SurfaceViewRenderer(context)
            AndroidView(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp)
                    .background(Color.Red)
                    .padding(16.dp),
                factory = { renderView },
            ) { view ->
                renderView.setup()
                it.addRenderer(view)
            }
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