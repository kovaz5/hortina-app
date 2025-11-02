package com.alex.hortina.data.remote.api

import retrofit2.http.GET
import retrofit2.http.Path
import com.alex.hortina.data.remote.dto.CultivoDto
import retrofit2.http.Query

interface HortinaApiService {

    @GET("cultivos")
    suspend fun getCultivos(): List<CultivoDto>

    @GET("cultivos/{id}/detalle")
    suspend fun getCultivoDetalle(@Path("id") id: Int): CultivoDto

    // para búsquedas (de momento vai a futuro)
    @GET("cultivos/search")
    suspend fun searchCultivos(@Query("q") query: String): List<CultivoDto>

}