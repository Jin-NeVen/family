package com.ntt.jin.family.data

interface RoomRepository {
    suspend fun getRoomNameList(): List<String>
}

class RoomRepositoryImpl() : RoomRepository {
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