package com.ntt.jin.family.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.ntt.jin.family.data.Room
import com.ntt.jin.family.data.User

@Composable
fun RoomCardGrid(
    navController: NavController,
    items: List<Room>,
    homeViewModel: HomeViewModel,
    localUser: User,
) {
    val onlineSfuRoomList = homeViewModel.onlineSfuRoomList
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(16.dp),
        content = {
            items(items.size) { index ->
                RoomCard(
                    room = items[index],
                    online = onlineSfuRoomList.any { it.name == items[index].name },
                    navController =  navController,
                )
            }
        }
    )
}