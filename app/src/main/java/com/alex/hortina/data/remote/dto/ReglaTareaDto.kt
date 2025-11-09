package com.alex.hortina.data.remote.dto

data class ReglaTareaDto(
    val id_regla: Int?,
    val tipo_cultivo: String?,
    val accion: String?,
    val frecuencia_dias: Int?,
    val condicion_meteo: String?,
    val activo: Boolean?
)