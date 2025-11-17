package com.alex.hortina.ui.screens.registro

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alex.hortina.data.remote.dto.RegistroRequest
import com.alex.hortina.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class RegistroUiState {
    object Idle : RegistroUiState()
    object Loading : RegistroUiState()
    object Success : RegistroUiState()
    data class Error(val message: String) : RegistroUiState()
}

class RegistroViewModel : ViewModel() {

    private val repo = UserRepository()

    private val _uiState = MutableStateFlow<RegistroUiState>(RegistroUiState.Idle)
    val uiState: StateFlow<RegistroUiState> = _uiState

    fun registrar(nombre: String, email: String, password: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _uiState.value = RegistroUiState.Loading
            try {
                repo.register(RegistroRequest(nombre, email, password))
                _uiState.value = RegistroUiState.Success
                onSuccess()
            } catch (e: Exception) {
                _uiState.value = RegistroUiState.Error(
                    e.message ?: "Error al registrar usuario"
                )
            }
        }
    }
}

