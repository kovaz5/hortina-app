package com.alex.hortina.data.repository

import com.alex.hortina.data.remote.api.HortinaApiService
import com.alex.hortina.data.remote.api.RetrofitClient
import com.alex.hortina.data.remote.dto.CultivoDto

class CultivoRepository {

    private val api = RetrofitClient.instance.create(HortinaApiService::class.java)

    suspend fun getCultivos(): List<CultivoDto> {
        return api.getCultivos()
    }

    suspend fun getCultivoDetalle(id: Int): CultivoDto {
        return api.getCultivoDetalle(id)
    }
}
