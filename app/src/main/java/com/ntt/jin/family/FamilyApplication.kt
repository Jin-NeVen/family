package com.ntt.jin.family

import android.app.Application
import com.ntt.jin.family.data.AuthTokenDataSource
import com.ntt.jin.family.data.AuthTokenLocalDataSource
import com.ntt.jin.family.data.AuthTokenRepository
import com.ntt.jin.family.data.AuthTokenRepositoryImpl
import com.ntt.jin.family.data.HomeRepository
import com.ntt.jin.family.data.HomeRepositoryImpl
import com.ntt.jin.family.data.RoomRepository
import com.ntt.jin.family.data.RoomRepositoryImpl
import com.ntt.jin.family.data.UserRepository
import com.ntt.jin.family.data.UserRepositoryImpl

class FamilyApplication: Application() {
    val homeRepository: HomeRepository by lazy {
        HomeRepositoryImpl()
    }
    val authTokenRepository: AuthTokenRepository by lazy {
        val authTokenDataSource: AuthTokenDataSource = AuthTokenLocalDataSource(this)
        AuthTokenRepositoryImpl(authTokenDataSource)
    }
    val userRepository: UserRepository by lazy {
        UserRepositoryImpl()
    }
    val roomRepository: RoomRepository by lazy {
        RoomRepositoryImpl()
    }
}