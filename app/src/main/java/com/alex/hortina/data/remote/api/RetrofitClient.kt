package com.alex.hortina.data.remote.api

import android.annotation.SuppressLint
import android.content.Context
import com.alex.hortina.data.local.UserPreferencesDataStore
import kotlinx.coroutines.runBlocking
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {

    private const val BASE_URL = "http://10.0.2.2:8080/"

    @SuppressLint("StaticFieldLeak")
    private lateinit var dataStore: UserPreferencesDataStore

    fun init(context: Context) {
        dataStore = UserPreferencesDataStore(context)
    }

    private val cookieJar = object : CookieJar {
        private val cookieStore = mutableMapOf<String, List<Cookie>>()

        override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
            cookieStore[url.host] = cookies

            val jsession = cookies.find { it.name == "JSESSIONID" }
            if (jsession != null) {
                val cookieString = "${jsession.name}=${jsession.value}"
                runBlocking {
                    dataStore.saveSessionCookie(cookieString)
                }
            }
        }

        override fun loadForRequest(url: HttpUrl): List<Cookie> {
            cookieStore[url.host]?.let { return it }

            val cookieStr = runBlocking { dataStore.getSessionCookie() }
            if (cookieStr != null) {
                val parsed = Cookie.parse(url, cookieStr)
                if (parsed != null) {
                    cookieStore[url.host] = listOf(parsed)
                    return listOf(parsed)
                }
            }
            return emptyList()
        }
    }

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val client =
        OkHttpClient.Builder().cookieJar(cookieJar).addInterceptor(loggingInterceptor).build()

    val instance: Retrofit by lazy {
        Retrofit.Builder().baseUrl(BASE_URL).addConverterFactory(GsonConverterFactory.create())
            .client(client).build()
    }
}
