package com.ntt.jin.skywaycomposequickstart

import android.app.Application
import kotlinx.coroutines.Dispatchers

class MainApplication: Application() {
    val skyWayDefaultScope: SkyWayCoroutineScope by lazy {
        SkyWayCoroutineScope(Dispatchers.Default)
    }
}