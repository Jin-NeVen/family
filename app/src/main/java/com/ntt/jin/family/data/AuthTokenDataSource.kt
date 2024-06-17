package com.ntt.jin.family.data

import android.content.Context
import com.ntt.jin.family.R

interface AuthTokenDataSource {
    suspend fun getAuthToken(): String?
}

class AuthTokenLocalDataSource(private val context: Context): AuthTokenDataSource {
    override suspend fun getAuthToken(): String? {
        return readTextFile(context, R.raw.auth_token)
    }

    private fun readTextFile(context: Context, fileResourceId: Int): String? {
        return try {
            context.resources.openRawResource(fileResourceId).bufferedReader().use { it.readText() }
        } catch (e: Exception) {
            null
        }
    }
}