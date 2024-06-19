package com.ntt.jin.family.data

import kotlin.random.Random

interface UserRepository {
    suspend fun getLocalUser(): User
}


class UserRepositoryImpl() : UserRepository {
    override suspend fun getLocalUser(): User {
        return User(name = "Jin-${generateRandomString(5)}")
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