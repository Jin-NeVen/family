package com.ntt.jin.skywaycomposequickstart

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ntt.skyway.core.content.sink.SurfaceViewRenderer


@Composable
fun LiveChatScreen(
    liveChatViewModel: LiveChatViewModel = viewModel( factory = LiveChatViewModel.Factory),
    modifier: Modifier = Modifier
) {
    val localVideoStream = liveChatViewModel.localVideoStream
    var localRenderView by remember { mutableStateOf<SurfaceViewRenderer?>(null) }

}