package com.ntt.jin.skywaycomposequickstart

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job

class SkyWayCoroutineScope(private val dispatcher: CoroutineDispatcher) : CoroutineScope {
    private val job = Job()
    override val coroutineContext
        get() = job + dispatcher

    fun cleanUp() {
        job.cancel()
    }
}