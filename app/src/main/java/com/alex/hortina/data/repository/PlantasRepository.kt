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

        return results
    }

    suspend fun translatePlantForDisplay(
        plant: PlantProfileDto, targetLang: String
    ): PlantProfileDto {

        return plant.copy(
            commonName = translator.translateAuto(plant.commonName ?: "", targetLang),
            watering = translator.translateAuto(plant.watering ?: "", targetLang),
            sunlight = translator.translateAuto(plant.sunlight ?: "", targetLang),
            careLevel = translator.translateAuto(plant.careLevel ?: "", targetLang),
            lifeCycle = translator.translateAuto(plant.lifeCycle ?: "", targetLang),
            edibleParts = translator.translateAuto(plant.edibleParts ?: "", targetLang)
        )
    }


    suspend fun getPlantById(id: Int): PlantProfileDto {
        return api.getPlantById(id)
    }
}
