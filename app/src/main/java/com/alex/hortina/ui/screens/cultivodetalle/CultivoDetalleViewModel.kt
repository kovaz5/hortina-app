package com.alex.hortina.ui.screens.cultivodetalle

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alex.hortina.data.remote.dto.CultivoDetalleDto
import com.alex.hortina.data.repository.CultivoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class CultivoDetalleUiState {
    object Loading : CultivoDetalleUiState()
    data class Success(val detalle: CultivoDetalleDto) : CultivoDetalleUiState()
    data class Error(val message: String) : CultivoDetalleUiState()
}

class CultivoDetalleViewModel(
    private val repo: CultivoRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<CultivoDetalleUiState>(CultivoDetalleUiState.Loading)
    val uiState: StateFlow<CultivoDetalleUiState> = _uiState

    fun load(cultivoId: Int) {
        println("Cargando detalle del cultivo con id=$cultivoId")
        _uiState.value = CultivoDetalleUiState.Loading
        viewModelScope.launch {
            try {
                val detalle = repo.getCultivoDetalle(cultivoId)
                println("Cultivo cargado: ${detalle.cultivo.nombre}, tareas: ${detalle.tareas.size}")
                _uiState.value = CultivoDetalleUiState.Success(detalle)
            } catch (t: Throwable) {
                println("Error al cargar detalle: ${t.message}")
                _uiState.value = CultivoDetalleUiState.Error(t.message ?: "Error al cargar detalle")
            }
        }
    }

}
