package com.alex.hortina.data.repository

import com.alex.hortina.data.remote.api.HortinaApiService
import com.alex.hortina.data.remote.api.RetrofitClient
import com.alex.hortina.data.remote.dto.CultivoDetalleDto
import com.alex.hortina.data.remote.dto.CultivoDto

class CultivoRepository {

    private val api = RetrofitClient.instance.create(HortinaApiService::class.java)
    private val translator = TranslationRepository()

    suspend fun getCultivos(uiLang: String = "ES"): List<CultivoDto> {
        val list = api.getCultivos()

        return list.map { cultivo ->
            cultivo.copy(
                nombre = translator.translateAuto(cultivo.nombre ?: "", uiLang),
                tipo = translator.translateAuto(cultivo.tipo ?: "", uiLang)
            )
        }
    }

    suspend fun getCultivoById(id: Int, uiLang: String = "ES"): CultivoDto {
        val c = api.getCultivoById(id)

        return c.copy(
            nombre = translator.translateAuto(c.nombre ?: "", uiLang),
            tipo = translator.translateAuto(c.tipo ?: "", uiLang)
        )
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
