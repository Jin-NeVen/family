package com.ntt.jin.family.data

import android.os.Build
import androidx.annotation.RequiresApi
import com.ntt.jin.family.R
import java.time.Instant

interface HomeRepository {
    suspend fun getRooms(): List<Room>
}

class HomeRepositoryImpl() : HomeRepository {
    @RequiresApi(Build.VERSION_CODES.O)
    override suspend fun getRooms(): List<Room> {
        return listOf(
            Room(
                id = "LivingRoom",
                name = "LivingRoom",
                headerImageResId = R.drawable.room_card_header_iamge_rose,
                createdAt = Instant.parse("2024-06-11T00:00:00.000Z")
            ),
            Room(
                id = "BedRoom",
                name = "BedRoom",
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
                id = "BabyRoom",
                name = "BabyRoom",
                headerImageResId = R.drawable.room_card_header_iamge_rose,
                createdAt = Instant.parse("2024-06-14T00:00:00.000Z")
            ),
            Room(
                id = "PetRoom",
                name = "PetRoom",
                headerImageResId = R.drawable.room_card_header_iamge_rose,
                createdAt = Instant.parse("2024-06-14T00:00:00.000Z")
            ),
        )
    }
}