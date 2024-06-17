package com.ntt.jin.family

import android.app.Application
import com.ntt.jin.family.data.AuthTokenDataSource
import com.ntt.jin.family.data.AuthTokenLocalDataSource
import com.ntt.jin.family.data.AuthTokenRepository
import com.ntt.jin.family.data.AuthTokenRepositoryImpl
import com.ntt.jin.family.data.HomeRepository
import com.ntt.jin.family.data.HomeRepositoryImpl

class FamilyApplication: Application() {
    val homeRepository: HomeRepository by lazy {
        HomeRepositoryImpl()
    }
    val authTokenRepository: AuthTokenRepository by lazy {
        val authTokenDataSource: AuthTokenDataSource = AuthTokenLocalDataSource(this)
        AuthTokenRepositoryImpl(authTokenDataSource)
    }
}