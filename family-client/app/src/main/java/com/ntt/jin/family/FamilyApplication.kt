package com.ntt.jin.family

import android.app.Application
import android.util.Log
import com.ntt.jin.family.data.AuthTokenDataSource
import com.ntt.jin.family.data.AuthTokenLocalDataSource
import com.ntt.jin.family.data.AuthTokenRepository
import com.ntt.jin.family.data.AuthTokenRepositoryImpl
import com.ntt.jin.family.data.HomeRepository
import com.ntt.jin.family.data.HomeRepositoryImpl
import com.ntt.jin.family.data.RoomListRepository
import com.ntt.jin.family.data.RoomListRepositoryImpl
import com.ntt.jin.family.data.UserRepository
import com.ntt.jin.family.data.UserRepositoryImpl
import com.ntt.skyway.core.SkyWayContext
import com.ntt.skyway.core.util.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

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
    val roomListRepository: RoomListRepository by lazy {
        RoomListRepositoryImpl()
    }
    val skyWayDefaultScope: SkyWayCoroutineScope by lazy {
        SkyWayCoroutineScope(Dispatchers.Default)
    }
}