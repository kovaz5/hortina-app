package com.alex.hortina.ui.screens.tareas

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alex.hortina.data.remote.dto.TareaDto
import com.alex.hortina.data.repository.TareaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth

sealed class TareaUiState {
    object Loading : TareaUiState()
    data class Success(
        val tareasPorDia: Map<LocalDate, List<TareaDto>>,
        val fechaSeleccionada: LocalDate,
        val mesActual: YearMonth = YearMonth.now()
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

                val tareasConFecha = tareas.mapNotNull { tarea ->
                    val fecha = tarea.fechaSugerida?.let { LocalDate.parse(it) }
                    if (fecha != null) fecha to tarea else null
                }

                val tareasPorDia = tareasConFecha.groupBy(
                    keySelector = { it.first },
                    valueTransform = { it.second })

                val hoy = LocalDate.now()

                _uiState.value = TareaUiState.Success(
                    tareasPorDia = tareasPorDia, fechaSeleccionada = hoy
                )

            } catch (e: Exception) {
                _uiState.value = TareaUiState.Error(e.message ?: "Error al cargar tareas")
            }
        }
    }

    fun seleccionarDia(dia: LocalDate) {
        val state = _uiState.value
        if (state is TareaUiState.Success) {
            _uiState.value = state.copy(fechaSeleccionada = dia)
        }
    }
}

