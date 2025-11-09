package com.alex.hortina.data.remote.dto

data class TareaDto(
    val id_tarea: Int?,
    val cultivo: CultivoDto?,
    val regla: ReglaTareaDto?,
    val nombre_tarea: String?,
    val descripcion: String?,
    val fechaSugerida: String?,
    val completada: Boolean?,
    val tipo_origen: String?,
    val created_at: String?
)
