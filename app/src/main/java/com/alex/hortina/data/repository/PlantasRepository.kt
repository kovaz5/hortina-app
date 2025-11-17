package com.alex.hortina.data.repository

import com.alex.hortina.data.remote.api.HortinaApiService
import com.alex.hortina.data.remote.api.RetrofitClient
import com.alex.hortina.data.remote.dto.PlantProfileDto

class PlantasRepository(
    private val api: HortinaApiService = RetrofitClient.instance.create(HortinaApiService::class.java),
    private val translator: TranslationRepository = TranslationRepository()
) {

    suspend fun searchPlants(query: String, uiLang: String = "ES"): List<PlantProfileDto> {
        val translatedQuery = translator.translate(query, uiLang, "EN")

        val results = api.searchPlants(translatedQuery)

        return results.map { plant ->
            plant.copy(
                commonName = translator.translate(plant.commonName ?: "", "EN", uiLang),
                scientificName = plant.scientificName,
                watering = translator.translate(plant.watering ?: "", "EN", uiLang),
                sunlight = translator.translate(plant.sunlight ?: "", "EN", uiLang)
            )
        }
    }

    suspend fun getPlantById(id: Int): PlantProfileDto {
        return api.getPlantById(id)
    }
}
