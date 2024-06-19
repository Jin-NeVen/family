package com.ntt.jin.family.ui

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.ntt.jin.family.LocalAppContext
import com.ntt.jin.family.LocalHomeViewModel
import com.ntt.jin.family.LocalRoomViewModel
import com.ntt.jin.family.R
import com.ntt.jin.family.data.HomeRepositoryImpl
import com.ntt.jin.family.data.Room
import com.ntt.jin.family.data.User
import com.ntt.jin.family.ui.PreviewParameterData.rooms
import com.ntt.jin.family.ui.theme.FamilyTheme
import java.time.Instant


@Composable
fun RoomCard(
    room: Room,
    localUser: User,
    online: Boolean = false,
    roomViewModel: RoomViewModel,
    navController: NavController,
    modifier: Modifier = Modifier,
) {
    val homeViewModel = LocalHomeViewModel.current
    if (homeViewModel.joinedRoomName == room.name) {
        navController.navigate("room/${room.name}")
    }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clickable(onClick = {
                //TODO
//                roomViewModel.joinRoom(room.name, localUser.name)
                homeViewModel.joinRoom(room.name, localUser.name)
            }),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        //onlineかどうかは色で表示
        colors = CardDefaults.cardColors(containerColor = if (online) Color.Green else Color.LightGray),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = when {
                    room.headerImageResId != -1 -> painterResource(room.headerImageResId)
                    else -> painterResource(R.drawable.room_card_header_iamge_rose)
                },
                contentDescription = "RoomCard header image",
                modifier = Modifier
                    .size(150.dp)
                    .padding(bottom = 8.dp),
                contentScale = ContentScale.Crop
            )
            Text(
                text = room.name,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}



@Composable
fun RoomTitle(
    title: String,
    modifier: Modifier = Modifier,
) {
    Text(title, style = MaterialTheme.typography.headlineSmall, modifier = modifier)
}

@Composable
fun RoomSurfaceViewRender(
    modifier: Modifier
) {
    //TODO
    Box(
        modifier = Modifier.height(180.dp)
    ) {
        Text("Room Surface View Render")
    }
}

@Preview("RoomCard")
@Composable
private fun RoomCardPreview(
    @PreviewParameter(RoomPreviewParameterProvider::class)
    rooms: List<Room>,
) {
    CompositionLocalProvider(
        LocalInspectionMode provides true,
    ) {
        FamilyTheme {
            Surface {
                RoomCard(
                    room = rooms[0],
                    roomViewModel = LocalRoomViewModel.current,
                    navController = NavController(LocalAppContext.current),
                    localUser = User(name = "Jin")
                )
            }
        }
    }
}

class RoomPreviewParameterProvider : PreviewParameterProvider<List<Room>> {

    @RequiresApi(Build.VERSION_CODES.O)
    override val values: Sequence<List<Room>> = sequenceOf(rooms)
}

object PreviewParameterData {
    @RequiresApi(Build.VERSION_CODES.O)
    val rooms = listOf(
        Room(
            id = "Living Room",
            name = "Living Room",
            headerImageResId = R.drawable.room_card_header_iamge_rose,
            createdAt = Instant.parse("2024-06-11T00:00:00.000Z")
        ),
        Room(
            id = "Bed Room",
            name = "Bed Room",
            headerImageResId = R.drawable.room_card_header_iamge_rose,
            createdAt = Instant.parse("2024-06-13T00:00:00.000Z")
        ),
        Room(
            id = "Kitchen",
            name = "Kitchen",
            headerImageResId = R.drawable.room_card_header_iamge_rose,
            createdAt = Instant.parse("2024-06-14T00:00:00.000Z")
        ),
        Room(
            id = "Baby Room",
            name = "Baby Room",
            headerImageResId = R.drawable.room_card_header_iamge_rose,
            createdAt = Instant.parse("2024-06-14T00:00:00.000Z")
        ),
        Room(
            id = "Pet Room",
            name = "Pet Room",
            headerImageResId = R.drawable.room_card_header_iamge_rose,
            createdAt = Instant.parse("2024-06-14T00:00:00.000Z")
        ),
    )
}
