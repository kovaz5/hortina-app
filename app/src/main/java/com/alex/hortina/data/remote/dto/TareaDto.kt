package com.alex.hortina.data.remote.dto

import com.google.gson.annotations.SerializedName

data class TareaDto(
    val id_tarea: Int?,
    val cultivo: CultivoDto?,
    val regla: ReglaTareaDto?,
    val nombre_tarea: String?,
    val descripcion: String?,
    @SerializedName(
        value = "fecha_sugerida",
        alternate = ["fechaSugerida"]
    ) val fechaSugerida: String?,
    val completada: Boolean?,
    val tipo_origen: String?,
    val created_at: String?,
    val recurrente: Boolean? = null,
    val frecuenciaDias: Int? = null
)
