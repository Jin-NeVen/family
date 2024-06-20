package com.ntt.jin.family.data

import android.util.Log

interface RoomRepository {
    fun dummy()
}

class RoomRepositoryImpl() : RoomRepository {
    override fun dummy() {
        Log.d("RoomRepository", "dummy")
    }
}