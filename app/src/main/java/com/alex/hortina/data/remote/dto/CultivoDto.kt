package com.alex.hortina.data.remote.dto

data class CultivoDto(
    val id_cultivo: Int?,
    val id_usuario: Int?,
    val plantExternalId: Int?,
    val id_ubicacion: Int?,
    val nombre: String?,
    val tipo: String?,
    val fecha_plantacion: String?,
    val estado: String?,
    val imagen: String?,
    val fecha_estimada_cosecha: String?
)
