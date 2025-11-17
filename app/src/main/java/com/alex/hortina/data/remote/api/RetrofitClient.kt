package com.alex.hortina.data.remote.api

import android.annotation.SuppressLint
import android.content.Context
import com.alex.hortina.data.local.UserPreferencesDataStore
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {

    private const val BASE_URL = "http://10.0.2.2:8080/"

    @SuppressLint("StaticFieldLeak")
    lateinit var dataStore: UserPreferencesDataStore
        private set

    fun init(context: Context) {
        dataStore = UserPreferencesDataStore(context)
    }

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val client by lazy {
        OkHttpClient.Builder().addInterceptor(JwtInterceptor(dataStore))
            .addInterceptor(loggingInterceptor).build()
    }

    val instance: Retrofit by lazy {
        Retrofit.Builder().baseUrl(BASE_URL).addConverterFactory(GsonConverterFactory.create())
            .client(client).build()
    }
}
