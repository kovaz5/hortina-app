package com.alex.hortina.ui.screens.cultivos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alex.hortina.data.remote.dto.CultivoDto
import com.alex.hortina.data.repository.CultivoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class CultivoUiState {
    object Loading : CultivoUiState()
    data class Success(val cultivos: List<CultivoDto>) : CultivoUiState()
    data class Error(val message: String) : CultivoUiState()
    object Empty : CultivoUiState()
}

class CultivoViewModel(
    private val repository: CultivoRepository = CultivoRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow<CultivoUiState>(CultivoUiState.Loading)
    val uiState: StateFlow<CultivoUiState> = _uiState

    init {
        loadCultivos()
    }

    fun loadCultivos() {
        _uiState.value = CultivoUiState.Loading
        viewModelScope.launch {
            try {
                val list = repository.getCultivos()
                if (list.isNullOrEmpty()) {
                    _uiState.value = CultivoUiState.Empty
                } else {
                    _uiState.value = CultivoUiState.Success(list)
                }
            } catch (t: Throwable) {
                _uiState.value = CultivoUiState.Error(t.message ?: "Error desconocido")
            }
        }
    }

    fun refresh() = loadCultivos()
}
