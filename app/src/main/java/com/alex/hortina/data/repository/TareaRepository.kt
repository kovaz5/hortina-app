package com.alex.hortina.data.repository

import com.alex.hortina.data.remote.api.HortinaApiService
import com.alex.hortina.data.remote.api.RetrofitClient
import com.alex.hortina.data.remote.dto.CrearTareaRequest
import com.alex.hortina.data.remote.dto.TareaDto

class TareaRepository {

    private val api = RetrofitClient.instance.create(HortinaApiService::class.java)
    private val translator = TranslationRepository()

    suspend fun crearTarea(req: CrearTareaRequest): TareaDto {
        return api.crearTarea(req)
    }

    suspend fun getTareas(uiLang: String = "ES"): List<TareaDto> {
        val list = api.getTareas()

        return list.map { tarea ->
            tarea.copy(
                nombre_tarea = translator.translateAuto(tarea.nombre_tarea ?: "", uiLang),
                descripcion = translator.translateAuto(tarea.descripcion ?: "", uiLang)
            )
        }
    }

    suspend fun getTareasPorCultivo(cultivoId: Int, uiLang: String = "ES"): List<TareaDto> {
        val list = api.getTareasPorCultivo(cultivoId)

        return list.map { tarea ->
            tarea.copy(
                nombre_tarea = translator.translateAuto(tarea.nombre_tarea ?: "", uiLang),
                descripcion = translator.translateAuto(tarea.descripcion ?: "", uiLang)
            )
        }
    }

    suspend fun actualizarEstado(id: Int, completada: Boolean): TareaDto {
        return api.actualizarEstadoTarea(id, completada)
    }
}
