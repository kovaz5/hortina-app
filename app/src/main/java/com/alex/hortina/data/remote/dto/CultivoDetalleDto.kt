package com.alex.hortina.data.remote.dto

data class CultivoDetalleDto(
    val cultivo: CultivoDto,
    val tareas: List<TareaDto>
)
