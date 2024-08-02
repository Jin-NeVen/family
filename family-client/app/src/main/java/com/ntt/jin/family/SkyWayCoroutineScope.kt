package com.ntt.jin.family

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlin.coroutines.CoroutineContext

class SkyWayCoroutineScope(private val dispatcher: CoroutineDispatcher) : CoroutineScope {
    private val job = Job()
    override val coroutineContext: CoroutineContext
        get() = job + dispatcher

    fun cleanUp() {
        job.cancel()
    }
}