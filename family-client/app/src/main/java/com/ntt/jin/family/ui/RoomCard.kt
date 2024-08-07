package com.ntt.jin.family.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.ntt.jin.family.R
import com.ntt.jin.family.data.Room


@Composable
fun RoomCard(
    room: Room,
    online: Boolean = false,
    navController: NavController,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clickable(onClick = {
                navController.navigate("room/${room.name}")
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
