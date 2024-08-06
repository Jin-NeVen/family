package com.ntt.jin.skywaycomposequickstart

import android.content.Context
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ntt.skyway.core.SkyWayContext
import com.ntt.skyway.core.util.Logger
import kotlinx.coroutines.launch

class MainViewModel(val applicationContext: Context) : ViewModel() {
    var skyWayInitialized by mutableStateOf(false)
        private set

    init {
        viewModelScope.launch {
            initializeSkyWayContext()
        }
    }

    private suspend fun initializeSkyWayContext() {
        val option = SkyWayContext.Options(
//            authToken = "PLEASE_SET_YOUR_AUTH_TOKEN",
            authToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJqdGkiOiIzZGQ1ZDhiZC0wMGNmLTQ2MDMtYWI0MS05YjY4NTAzMjMxOWUiLCJpYXQiOjE3MjI5MTI1NzEsImV4cCI6MTcyMjk5ODk3MSwic2NvcGUiOnsiYXBwIjp7ImlkIjoiYjcyYmE3NDUtNGY4Ny00OGU1LWE4MjMtZmEzMTA3NDQwMzUwIiwidHVybiI6dHJ1ZSwiYWN0aW9ucyI6WyJyZWFkIl0sImNoYW5uZWxzIjpbeyJpZCI6IioiLCJuYW1lIjoiKiIsImFjdGlvbnMiOlsid3JpdGUiXSwibWVtYmVycyI6W3siaWQiOiIqIiwibmFtZSI6IioiLCJhY3Rpb25zIjpbIndyaXRlIl0sInB1YmxpY2F0aW9uIjp7ImFjdGlvbnMiOlsid3JpdGUiXX0sInN1YnNjcmlwdGlvbiI6eyJhY3Rpb25zIjpbIndyaXRlIl19fV0sInNmdUJvdHMiOlt7ImFjdGlvbnMiOlsid3JpdGUiXSwiZm9yd2FyZGluZ3MiOlt7ImFjdGlvbnMiOlsid3JpdGUiXX1dfV19XX19fQ.gF-G83KRuza7o91RwbqU00WQ3PyoBH80gRzyYTgo61s",
            logLevel = Logger.LogLevel.VERBOSE
        )
        skyWayInitialized = SkyWayContext.setup(applicationContext, option) { error ->
            Log.d("TAG", "initializeSkyWayContext: ${error.message}")
        }
    }

    override fun onCleared() {
        super.onCleared()
        SkyWayContext.dispose()
    }
}