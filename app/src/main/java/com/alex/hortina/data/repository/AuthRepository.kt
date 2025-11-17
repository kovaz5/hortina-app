package com.alex.hortina.data.repository

import com.alex.hortina.data.local.UserPreferencesDataStore
import com.alex.hortina.data.remote.api.HortinaApiService
import com.alex.hortina.data.remote.api.RetrofitClient

class AuthRepository(private val dataStore: UserPreferencesDataStore) {

    private val api = RetrofitClient.instance.create(HortinaApiService::class.java)

    suspend fun checkSession(): Boolean {
        val access = dataStore.getAccessToken()
        val refresh = dataStore.getRefreshToken()

        if (access == null || refresh == null) return false

        try {
            api.getProfile()
            return true
        } catch (_: Exception) {
        }

        return try {
            val newTokens = api.refreshToken(refresh)
            dataStore.saveTokens(newTokens.accessToken, newTokens.refreshToken)
            true
        } catch (_: Exception) {
            false
        }
    }
}
