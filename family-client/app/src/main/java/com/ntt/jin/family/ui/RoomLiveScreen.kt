package com.ntt.jin.family.ui

import android.app.PictureInPictureParams
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toAndroidRectF
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.app.PictureInPictureModeChangedInfo
import androidx.core.graphics.toRect
import androidx.core.util.Consumer
import androidx.navigation.NavHostController
import com.ntt.jin.family.infra.findActivity
import com.ntt.skyway.core.content.sink.SurfaceViewRenderer

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun RoomLiveScreen(
    roomViewModel: RoomViewModel,
    navController: NavHostController,
    modifier: Modifier = Modifier,
) {
    BackHandler {
        roomViewModel.leaveRoom()
        navController.navigate("home")
    }
    val context = LocalContext.current
    val remoteVideoStream = roomViewModel.remoteRoomVideoStream
    val memberList = roomViewModel.roomMembers
    Column(modifier = Modifier.fillMaxSize()) {
        if (!isInPipMode()) {
            Text("Room Live Screen: ${roomViewModel.roomName}")
        }

        remoteVideoStream?.let {
            val renderView = SurfaceViewRenderer(context)
            AndroidView(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp)
                    .background(Color.Red)
                    .padding(16.dp)
                    .onGloballyPositioned { layoutCoordinates ->
                        val builder = PictureInPictureParams.Builder()
                        val sourceRect = layoutCoordinates.boundsInWindow().toAndroidRectF().toRect()
                        builder.setSourceRectHint(sourceRect)

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                            builder.setAutoEnterEnabled(true)
                        }
                        context.findActivity().setPictureInPictureParams(builder.build())
                    }
                    .focusable(),
                factory = { renderView },
            ) { view ->
                renderView.setup()
                it.addRenderer(view)
            }
        }
        if (!isInPipMode()) {
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
}

@Composable
fun isInPipMode(): Boolean {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val activity = LocalContext.current.findActivity()
        var pipMode by remember { mutableStateOf(activity.isInPictureInPictureMode) }
        DisposableEffect(activity) {
            val observer = Consumer<PictureInPictureModeChangedInfo> { info ->
                pipMode = info.isInPictureInPictureMode
            }
            activity.addOnPictureInPictureModeChangedListener(
                observer,
            )
            onDispose { activity.removeOnPictureInPictureModeChangedListener(observer) }
        }
        return pipMode
    } else {
        return false
    }
}

