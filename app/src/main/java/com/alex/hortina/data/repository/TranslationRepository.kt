package com.alex.hortina.data.repository

import android.content.Context
import com.alex.hortina.data.remote.api.DeepLClient

class TranslationRepository {

    private val service = DeepLClient.instance

    private val cache = mutableMapOf<String, String>()

    suspend fun translate(text: String, sourceLang: String, targetLang: String): String {
        if (text.isBlank()) return text

        val key = "$targetLang:$text"

        cache[key]?.let { return it }

        return try {
            val response = service.translate(
                text = text, sourceLang = sourceLang, targetLang = targetLang
            )
            val translated = response.translations.firstOrNull()?.text ?: text

            cache[key] = translated

            translated

        } catch (e: Exception) {
            text
        }
    }

    suspend fun translateAuto(text: String, targetLang: String): String {
        return translate(text, "auto", targetLang)
    }
}


