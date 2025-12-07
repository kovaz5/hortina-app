package com.alex.hortina.data.repository

import com.alex.hortina.data.remote.api.DeepLClient

class TranslationRepository {

    private val service = DeepLClient.instance

    private val cache = mutableMapOf<String, String>()

    suspend fun translate(text: String, sourceLang: String, targetLang: String): String {
        if (text.isBlank()) return text

        val key = "$sourceLang=>$targetLang::$text"
        cache[key]?.let { return it }

        return try {
            val response = if (sourceLang.isBlank()) {
                service.translateAutoSource(text, targetLang)
            } else {
                service.translateWithSource(text, sourceLang, targetLang)
            }

            val translated = response.translations.firstOrNull()?.text ?: text
            cache[key] = translated
            translated

        } catch (e: Exception) {
            text
        }
    }

    suspend fun translateAuto(text: String, targetLang: String): String {
        if (text.isBlank()) return text

        val response = try {
            service.translateAutoSource(text, targetLang)
        } catch (e: Exception) {
            return text
        }

        val translated = response.translations.firstOrNull()?.text ?: text
        val detected =
            response.translations.firstOrNull()?.detected_source_language?.uppercase() ?: ""

        if (text.trim().split(" ").size == 1 && text.length <= 5) {

            val likelyLangs = listOf("ES", "GL", "EN")

            if (detected !in likelyLangs) {
                val fallback = try {
                    service.translateWithSource(text, "ES", targetLang)
                        .translations.firstOrNull()?.text ?: translated
                } catch (_: Exception) {
                    translated
                }
                return fallback
            }
        }

        return translated
    }

}
