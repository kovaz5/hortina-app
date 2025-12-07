package com.alex.hortina.ui.screens.tareas

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alex.hortina.data.local.UserPreferencesDataStore
import com.alex.hortina.data.remote.dto.TareaDto
import com.alex.hortina.data.repository.TareaRepository
import com.alex.hortina.data.repository.TranslationRepository
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
    private val repository: TareaRepository = TareaRepository(),
    private val dataStore: UserPreferencesDataStore
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
                val lang = dataStore.getLanguage()?.uppercase() ?: "ES"
                val translator = TranslationRepository()
                val tareasRaw = repository.getTareas()

                val tareasTrad = tareasRaw.map { t ->
                    t.copy(
                        nombre_tarea = t.nombre_tarea?.let {
                        translator.translateAuto(
                            it, lang
                        )
                    },
                        descripcion = t.descripcion?.let { translator.translateAuto(it, lang) },
                        tipo_origen = t.tipo_origen?.let { translator.translateAuto(it, lang) },
                        cultivo = t.cultivo?.copy(
                            nombre = t.cultivo.nombre?.let { translator.translateAuto(it, lang) })
                    )
                }

                val tareasConFecha = tareasTrad.mapNotNull { tarea ->
                    val fecha = tarea.fechaSugerida?.let { LocalDate.parse(it) }
                    if (fecha != null) fecha to tarea else null
                }

                val tareasPorDia = tareasConFecha.groupBy(
                    keySelector = { it.first },
                    valueTransform = { it.second })

                _uiState.value = TareaUiState.Success(
                    tareasPorDia = tareasPorDia, fechaSeleccionada = LocalDate.now()
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

