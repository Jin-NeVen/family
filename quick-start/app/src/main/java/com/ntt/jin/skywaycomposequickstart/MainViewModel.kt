package com.ntt.jin.skywaycomposequickstart

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ntt.skyway.core.SkyWayContext
import com.ntt.skyway.core.util.Logger
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val applicationContext: Context = application.applicationContext
    var skyWayInitialized by mutableStateOf(false)
        private set

    fun initializeSkyWayContext() {
        viewModelScope.launch {
            val option = SkyWayContext.Options(
                authToken = "PLEASE_SET_YOUR_AUTH_TOKEN",
                logLevel = Logger.LogLevel.VERBOSE
            )
            skyWayInitialized = SkyWayContext.setup(applicationContext, option) { error ->
                Log.d("TAG", "initializeSkyWayContext: ${error.message}")
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        SkyWayContext.dispose()
    }
}