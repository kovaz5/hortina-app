package com.alex.hortina.data.repository

import com.alex.hortina.data.remote.api.HortinaApiService
import com.alex.hortina.data.remote.api.RetrofitClient
import com.alex.hortina.data.remote.dto.PlantProfileDto

class PlantasRepository {

    private val api = RetrofitClient.instance.create(HortinaApiService::class.java)

    suspend fun searchPlants(query: String): List<PlantProfileDto> {
        return api.searchPlants(query)
    }

    suspend fun getPlantById(externalId: Int): PlantProfileDto {
        return api.getPlantById(externalId)
    }
}