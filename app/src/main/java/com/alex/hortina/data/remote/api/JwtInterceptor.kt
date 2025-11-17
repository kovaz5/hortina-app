package com.alex.hortina.data.remote.api

import com.alex.hortina.data.local.UserPreferencesDataStore
import com.alex.hortina.data.remote.dto.TokenResponse
import com.google.gson.Gson
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response

class JwtInterceptor(
    private val dataStore: UserPreferencesDataStore
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {

        val access = runBlocking { dataStore.getAccessToken() }

        val requestWithAuth = if (access != null) {
            chain.request().newBuilder().addHeader("Authorization", "Bearer $access").build()
        } else {
            chain.request()
        }

        val response = chain.proceed(requestWithAuth)

        if (response.code != 401) {
            return response
        }

        val refresh = runBlocking { dataStore.getRefreshToken() }
        if (refresh == null) return response // nada que refrescar

        val refreshBody = refresh.toRequestBody("text/plain".toMediaType())

        val refreshRequest =
            Request.Builder().url("http://10.0.2.2:8080/api/auth/refresh").post(refreshBody).build()

        val refreshClient = OkHttpClient()
        val refreshResponse = refreshClient.newCall(refreshRequest).execute()

        if (!refreshResponse.isSuccessful) {
            return response
        }

        val bodyString = refreshResponse.body?.string()
        val tokenResponse = Gson().fromJson(bodyString, TokenResponse::class.java)

        runBlocking {
            dataStore.saveTokens(tokenResponse.accessToken, tokenResponse.refreshToken)
        }

        refreshResponse.close()

        val newRequest = chain.request().newBuilder()
            .header("Authorization", "Bearer ${tokenResponse.accessToken}").build()

        return chain.proceed(newRequest)
    }
}
