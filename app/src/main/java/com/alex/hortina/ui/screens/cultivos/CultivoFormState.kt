package com.alex.hortina.ui.screens.cultivos

import com.alex.hortina.data.remote.dto.PlantProfileDto

data class CultivoFormState(
    val nombre: String = "",
    val tipo: String = "",
    val fechaPlantacion: String = "",
    val estado: String = "activo",
    val imagen: String? = null,
    val selectedPlant: PlantProfileDto? = null,
    val error: String? = null
) {
    fun isValid(): Boolean = nombre.isNotBlank() && tipo.isNotBlank()
}
