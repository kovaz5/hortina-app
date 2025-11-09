package com.alex.hortina.ui.screens.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alex.hortina.data.remote.dto.CultivoDto
import com.alex.hortina.data.remote.dto.TareaDto
import com.alex.hortina.data.repository.CultivoRepository
import com.alex.hortina.data.repository.TareaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

sealed class DashboardUiState {
    object Loading : DashboardUiState()
    data class Success(
        val cultivos: List<CultivoDto> = emptyList(),
        val tareasPendientes: Int,
        val tareasCompletadas: Int,
        val tareasProximas: List<TareaDto>,
        val ultimaActualizacion: String
    ) : DashboardUiState()

    data class Error(val message: String) : DashboardUiState()
}

// máis adiante añadir o resto de repositorios
class DashboardViewModel(
    private val cultivoRepository: CultivoRepository, private val tareaRepository: TareaRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<DashboardUiState>(DashboardUiState.Loading)
    val uiState: StateFlow<DashboardUiState> = _uiState

    init {
        loadDashboardData()
    }

    fun loadDashboardData() {
        _uiState.value = DashboardUiState.Loading
        viewModelScope.launch {
            try {
                val cultivos = cultivoRepository.getCultivos()
                val tareas = tareaRepository.getTareas()
                val pendientes = tareas.count { it.completada == false || it.completada == null }
                val completadas = tareas.count { it.completada == true }
                val tareasProximas =
                    tareas.filter { it.completada == false }.sortedBy { it.fechaSugerida }.take(3)
                val fecha = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())

                _uiState.value = DashboardUiState.Success(
                    cultivos = cultivos,
                    tareasPendientes = pendientes,
                    tareasCompletadas = completadas,
                    tareasProximas = tareasProximas,
                    ultimaActualizacion = fecha
                )
            } catch (e: Exception) {
                _uiState.value = DashboardUiState.Error(e.message ?: "Error desconocido")
            }
        }
    }

    fun refresh() = loadDashboardData()

    fun actualizarTareasLocalmente(completadas: Int, pendientes: Int) {
        val current = _uiState.value
        if (current is DashboardUiState.Success) {
            _uiState.value = current.copy(
                tareasCompletadas = completadas, tareasPendientes = pendientes
            )
        }
    }
}
