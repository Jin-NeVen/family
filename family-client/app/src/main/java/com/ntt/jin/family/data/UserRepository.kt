package com.ntt.jin.family.data

import com.ntt.skyway.room.sfu.LocalSFURoomMember
import com.ntt.skyway.room.sfu.SFURoom
import kotlin.random.Random

interface UserRepository {
    suspend fun getLocalUser(): User
    suspend fun setLocalUserCurrentRoom(sfuRoom: SFURoom?)
    suspend fun setLocalUserCurrentMember(sfuRoomMember: LocalSFURoomMember?)
}


class UserRepositoryImpl() : UserRepository {
    lateinit var localUser: User
    private fun createUser() {
        localUser = User(name = "Jin-${generateRandomString(5)}")
    }
    override suspend fun getLocalUser(): User {
        if (!::localUser.isInitialized) {
            createUser()
        }
        return localUser as User
    }

    override suspend fun setLocalUserCurrentRoom(sfuRoom: SFURoom?) {
        localUser.joinedRoom = sfuRoom
    }

    override suspend fun setLocalUserCurrentMember(sfuRoomMember: LocalSFURoomMember?) {
        localUser.localSFURoomMember = sfuRoomMember
    }

    //TODO this should be in domain layer(use case)
    private fun generateRandomString(length: Int = 5): String {
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"
        return (1..length)
            .map { Random.nextInt(0, chars.length) }
            .map(chars::get)
            .joinToString("")
    }
}