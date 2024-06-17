package com.ntt.jin.family.data

interface UserRepository {
    suspend fun getUser(userId: String): User

}