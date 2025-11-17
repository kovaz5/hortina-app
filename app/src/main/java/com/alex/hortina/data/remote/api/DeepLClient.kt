package com.alex.hortina.data.remote.api

import com.alex.hortina.BuildConfig
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object DeepLClient {

    private const val BASE_URL = "https://api-free.deepl.com/"

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val authInterceptor = Interceptor { chain ->
        val request = chain.request().newBuilder()
            .addHeader("Authorization", "DeepL-Auth-Key ${BuildConfig.DEEPL_API_KEY}").build()
        chain.proceed(request)
    }

    private val httpClient =
        OkHttpClient.Builder().addInterceptor(authInterceptor).addInterceptor(loggingInterceptor)
            .build()

    val instance: DeepLService by lazy {
        Retrofit.Builder().baseUrl(BASE_URL).addConverterFactory(GsonConverterFactory.create())
            .client(httpClient).build().create(DeepLService::class.java)
    }
}
