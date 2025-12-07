package com.alex.hortina.ui.screens.tareas

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alex.hortina.data.remote.dto.CrearTareaRequest
import com.alex.hortina.data.repository.TareaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

data class CrearTareaUiState(
    val nombre: String = "",
    val descripcion: String = "",
    val fecha: LocalDate = LocalDate.now(),
    val recurrente: Boolean = false,
    val frecuenciaDias: String = "",
    val loading: Boolean = false,
    val error: String? = null,
    val success: Boolean = false
)

class CrearTareaViewModel(
    private val cultivoId: Int, private val repo: TareaRepository = TareaRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(CrearTareaUiState())
    val uiState: StateFlow<CrearTareaUiState> = _uiState

    fun updateNombre(s: String) {
        _uiState.value = _uiState.value.copy(nombre = s)
    }

    fun updateDescripcion(s: String) {
        _uiState.value = _uiState.value.copy(descripcion = s)
    }

    fun updateFecha(date: LocalDate) {
        _uiState.value = _uiState.value.copy(fecha = date)
    }

    fun updateRecurrente(b: Boolean) {
        _uiState.value = _uiState.value.copy(recurrente = b)
    }

    fun updateFrecuencia(s: String) {
        _uiState.value = _uiState.value.copy(frecuenciaDias = s)
    }

    fun crearTarea() {
        val state = _uiState.value

        if (state.nombre.isBlank()) {
            _uiState.value = state.copy(error = "Introduce un nombre")
            return
        }

        if (state.recurrente && state.frecuenciaDias.toIntOrNull() == null) {
            _uiState.value = state.copy(error = "Frecuencia inválida")
            return
        }

        val fmt = DateTimeFormatter.ISO_LOCAL_DATE

        // Crear request nuevo
        val req = CrearTareaRequest(
            idCultivo = cultivoId,
            nombreTarea = state.nombre,
            descripcion = state.descripcion,
            fechaSugerida = state.fecha.format(fmt),
            completada = false,
            tipoOrigen = "manual",
            recurrente = state.recurrente,
            frecuenciaDias = if (state.recurrente) state.frecuenciaDias.toInt() else 0
        )

        // Ejecutar llamada
        _uiState.value = state.copy(loading = true, error = null)

        viewModelScope.launch {
            try {
                repo.crearTarea(req)
                _uiState.value = _uiState.value.copy(success = true)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    loading = false, error = e.message ?: "Error inesperado"
                )
            }
        }
    }
}
