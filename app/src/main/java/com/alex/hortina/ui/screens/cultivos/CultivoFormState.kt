package com.alex.hortina.ui.screens.cultivos

import com.alex.hortina.data.remote.dto.PlantProfileDto

data class CultivoFormState(
    val selectedPlant: PlantProfileDto? = null,
    val estado: String = "semilla",
    val fechaPlantacion: String = java.time.LocalDate.now().toString(),
    val error: String? = null
) {
    fun isValid(): Boolean = selectedPlant != null && estado.isNotBlank()
}

