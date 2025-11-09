package com.alex.hortina.data.remote.dto

data class PlantProfileDto(
    val id: Int?,
    val externalId: Int?,
    val scientificName: String?,
    val commonName: String?,
    val watering: String?,
    val sunlight: String?,
    val careLevel: String?,
    val lifeCycle: String?,
    val height: String?,
    val edibleParts: String?,
    val imageUrl: String?
)