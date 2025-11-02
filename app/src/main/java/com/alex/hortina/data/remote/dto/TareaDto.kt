package com.alex.hortina.data.remote.dto

data class TareaDto(
    val id_tarea: Int?,
    val id_cultivo: Int?,
    val id_regla: Int?,
    val nombre_tarea: String?,
    val descripcion: String?,
    val fecha_sugerida: String?,
    val completada: Boolean?,
    val tipo_origen: String?,
    val created_at: String?
)
