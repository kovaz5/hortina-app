package com.alex.hortina.data.repository

import com.alex.hortina.data.remote.api.HortinaApiService
import com.alex.hortina.data.remote.api.RetrofitClient

class AuthRepository {
    private val api = RetrofitClient.instance.create(HortinaApiService::class.java)

    suspend fun checkSession(): Boolean {
        return try {
            val response = api.getProfile()
            response.email?.isNotEmpty() == true
        } catch (e: Exception) {
            false
        }
    }
}
