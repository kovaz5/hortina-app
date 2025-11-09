package com.alex.hortina.ui.screens.tareas

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alex.hortina.data.remote.dto.TareaDto
import com.alex.hortina.data.repository.TareaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class TareaUiState {
    object Loading : TareaUiState()
    data class Success(
        val pendientes: List<TareaDto>, val completadas: List<TareaDto>
    ) : TareaUiState()

    data class Error(val message: String) : TareaUiState()
}

class TareaViewModel(
    private val repository: TareaRepository = TareaRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow<TareaUiState>(TareaUiState.Loading)
    val uiState: StateFlow<TareaUiState> = _uiState

    init {
        loadTareas()
    }

    fun loadTareas() {
        _uiState.value = TareaUiState.Loading
        viewModelScope.launch {
            try {
                val tareas = repository.getTareas()
                println("🧾 Tareas cargadas: ${tareas.map { it.nombre_tarea to it.fechaSugerida }}")
                val pendientes = tareas.filter { it.completada == false }
                val completadas = tareas.filter { it.completada == true }
                _uiState.value = TareaUiState.Success(pendientes, completadas)
            } catch (t: Throwable) {
                _uiState.value =
                    TareaUiState.Error(t.message ?: "Error desconocido al cargar tareas")
            }
        }
    }

    fun refresh() = loadTareas()

    fun cambiarEstado(
        id: Int, nuevoEstado: Boolean, onContadoresActualizados: ((Int, Int) -> Unit)? = null
    ) {
        viewModelScope.launch {
            val currentState = _uiState.value
            if (currentState is TareaUiState.Success) {
                val pendientes = currentState.pendientes.toMutableList()
                val completadas = currentState.completadas.toMutableList()
                val tarea = (pendientes + completadas).find { it.id_tarea == id }
                if (tarea != null) {
                    val tareaActualizada = tarea.copy(completada = nuevoEstado)
                    pendientes.removeIf { it.id_tarea == id }
                    completadas.removeIf { it.id_tarea == id }
                    if (nuevoEstado) completadas.add(0, tareaActualizada)
                    else pendientes.add(0, tareaActualizada)
                    _uiState.value = TareaUiState.Success(pendientes, completadas)
                    onContadoresActualizados?.invoke(completadas.size, pendientes.size)
                    try {
                        repository.actualizarEstado(id, nuevoEstado)
                    } catch (t: Throwable) {
                        _uiState.value = currentState
                        onContadoresActualizados?.invoke(
                            currentState.completadas.size,
                            currentState.pendientes.size
                        )
                    }
                }
            }
        }
    }


}
