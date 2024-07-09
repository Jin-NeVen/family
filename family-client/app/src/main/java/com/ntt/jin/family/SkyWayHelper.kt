package com.ntt.jin.family

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.util.Log
import com.ntt.jin.family.data.AuthTokenRepository
import com.ntt.skyway.core.SkyWayContext
import com.ntt.skyway.core.util.Logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class SkyWayHelper(
    val authTokenRepository: AuthTokenRepository,
    val applicationContext: Context) {
    private val localScope = CoroutineScope(Job() + Dispatchers.Default)
    fun initialize() {
        localScope.launch {
            val option = authTokenRepository.getAuthToken()?.let {
                SkyWayContext.Options(
                    authToken = it,
                    logLevel = Logger.LogLevel.VERBOSE
                )
            }
            val result =  SkyWayContext.setup(applicationContext, option!!, onErrorHandler = { error ->
                Log.d("App", "skyway setup failed: ${error.message}")
            })
            Log.d("SkyWayHelper", "SkyWayHelper: initialize")
        }
    }

    fun cleanUp() {
        SkyWayContext.dispose()
        localScope.cancel()
        Log.d("SkyWayHelper", "SkyWayHelper: cleanUp")
    }
}