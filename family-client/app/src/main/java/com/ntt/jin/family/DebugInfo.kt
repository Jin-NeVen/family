package com.ntt.jin.family

import android.util.Log

object DebugInfo {
    fun logCurrentThreadInfo(tag: String, message: String? = null) {
        val thread = Thread.currentThread()
        Log.d(tag, "$message. Thread name: ${thread.name}, Thread id: ${thread.id}")
    }
}
