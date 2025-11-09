package com.alex.hortina.data.remote.dto

data class CultivoDto(
    val idCultivo: Int? = null,
    val id_usuario: Int? = null,
    val plantExternalId: Int? = null,
    val id_ubicacion: Int? = null,
    val nombre: String? = null,
    val tipo: String? = null,
    val fecha_plantacion: String? = null,
    val estado: String? = null,
    val imagen: String? = null,
    val fecha_estimada_cosecha: String? = null
)

