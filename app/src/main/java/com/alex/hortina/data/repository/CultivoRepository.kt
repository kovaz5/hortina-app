package com.alex.hortina.data.repository

import com.alex.hortina.data.remote.api.HortinaApiService
import com.alex.hortina.data.remote.api.RetrofitClient
import com.alex.hortina.data.remote.dto.CultivoDetalleDto
import com.alex.hortina.data.remote.dto.CultivoDto

class CultivoRepository {

    private val api = RetrofitClient.instance.create(HortinaApiService::class.java)

    suspend fun getCultivos(): List<CultivoDto> {
        return api.getCultivos()
    }

    suspend fun getCultivoById(id: Int): CultivoDto {
        return api.getCultivoById(id)
    }

    suspend fun getCultivoDetalle(id: Int): CultivoDetalleDto {
        return api.getCultivoDetalle(id)
    }

    suspend fun createCultivo(cultivo: CultivoDto): CultivoDto {
        return api.createCultivo(cultivo)
    }

    suspend fun updateCultivo(id: Int, dto: CultivoDto): CultivoDto {
        return api.updateCultivo(id, dto)
    }

    suspend fun deleteCultivo(id: Int) {
        api.deleteCultivo(id)
    }
}

