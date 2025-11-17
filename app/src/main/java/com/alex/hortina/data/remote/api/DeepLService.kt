package com.alex.hortina.data.remote.api

import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface DeepLService {
    @FormUrlEncoded
    @POST("v2/translate")
    suspend fun translate(
        @Field("text") text: String,
        @Field("source_lang") sourceLang: String,
        @Field("target_lang") targetLang: String,
        @Field("enable_beta_languages") enableBeta: Int = 1
    ): DeepLResponse
}

data class DeepLResponse(val translations: List<DeepLTranslation>)
data class DeepLTranslation(
    val detected_source_language: String, val text: String
)
