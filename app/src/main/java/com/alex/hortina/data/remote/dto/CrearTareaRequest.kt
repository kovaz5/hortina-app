package com.alex.hortina.data.remote.dto

import com.google.gson.annotations.SerializedName

data class CrearTareaRequest(
    @SerializedName("id_cultivo") val idCultivo: Int,
    @SerializedName("nombre_tarea") val nombreTarea: String,
    val descripcion: String?,
    @SerializedName("fecha_sugerida") val fechaSugerida: String,
    val completada: Boolean = false,
    @SerializedName("tipo_origen") val tipoOrigen: String = "manual",
    val recurrente: Boolean,
    val frecuenciaDias: Int
)

