package com.alex.hortina.data.remote.dto

import com.google.gson.annotations.SerializedName

data class TareaDto(
    val id_tarea: Int?,
    val id_cultivo: Int?,
    val id_regla: Int?,
    val nombre_tarea: String?,
    val descripcion: String?,
    @SerializedName("fechaSugerida")
    val fecha_sugerida: String?,
    val completada: Boolean?,
    val tipo_origen: String?,
    val created_at: String?
)
