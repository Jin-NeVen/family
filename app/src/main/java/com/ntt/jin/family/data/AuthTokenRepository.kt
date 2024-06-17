package com.ntt.jin.family.data

interface AuthTokenRepository {
    suspend fun getAuthToken(): String?
}

class AuthTokenRepositoryImpl(
    private val authTokenDataSource: AuthTokenDataSource
) : AuthTokenRepository {
    override suspend fun getAuthToken(): String? {
        return authTokenDataSource.getAuthToken()
    }
}