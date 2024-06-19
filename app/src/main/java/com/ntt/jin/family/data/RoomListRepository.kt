package com.ntt.jin.family.data

interface RoomListRepository {
    suspend fun getRoomNameList(): List<String>
}

class RoomListRepositoryImpl() : RoomListRepository {
    override suspend fun getRoomNameList(): List<String> {
        return listOf(
            "LivingRoom",
            "BedRoom",
            "Kitchen",
            "BabyRoom",
            "PetRoom"
        )
    }
}