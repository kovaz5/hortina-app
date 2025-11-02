package com.alex.hortina.data.repository

import com.alex.hortina.data.remote.api.HortinaApiService
import com.alex.hortina.data.remote.api.RetrofitClient
import com.alex.hortina.data.remote.dto.TareaDto

class TareaRepository {

    private val api = RetrofitClient.instance.create(HortinaApiService::class.java)

    suspend fun getTareas(): List<TareaDto> {
        return api.getTareas()
    }

    suspend fun getTareasPorCultivo(cultivoId: Int): List<TareaDto> {
        return api.getTareasPorCultivo(cultivoId)
    }
}