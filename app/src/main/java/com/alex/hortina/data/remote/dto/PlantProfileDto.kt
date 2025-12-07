package com.alex.hortina.data.remote.dto

data class PlantProfileDto(
    val id: Int?,
    val externalId: Int?,
    val commonName: String?,
    val scientificName: String?,
    val imageUrl: String?,
    val watering: String?,
    val sunlight: String?,
    val careLevel: String?,
    val lifeCycle: String?,
    val height: String?,
    val edibleParts: String?
)
