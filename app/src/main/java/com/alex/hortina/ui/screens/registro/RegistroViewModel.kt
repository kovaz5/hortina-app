package com.alex.hortina.ui.screens.registro

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.alex.hortina.data.local.UserPreferencesDataStore
import com.alex.hortina.data.remote.dto.LoginRequest
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

class RegistroViewModel(application: Application) : AndroidViewModel(application) {

    private val repo = UserRepository()
    private val dataStore = UserPreferencesDataStore(application.applicationContext)

    private val _uiState = MutableStateFlow<RegistroUiState>(RegistroUiState.Idle)
    val uiState: StateFlow<RegistroUiState> = _uiState

    fun registrar(nombre: String, email: String, password: String, onSuccessNavigate: () -> Unit) {
        viewModelScope.launch {
            _uiState.value = RegistroUiState.Loading

            try {
                repo.register(RegistroRequest(nombre, email, password))

                val loginResponse = repo.login(LoginRequest(email, password))

                dataStore.saveTokens(
                    loginResponse.accessToken, loginResponse.refreshToken
                )

                loginResponse.usuario?.let { u ->
                    val userId = u.id_usuario.toString()

                    dataStore.saveUser(
                        id = userId,
                        name = u.nombre ?: "",
                        email = u.email ?: ""
                    )

                    dataStore.setHasSeenOnboarding(userId, false)
                }

                _uiState.value = RegistroUiState.Success

                onSuccessNavigate()

            } catch (e: Exception) {
                _uiState.value = RegistroUiState.Error(
                    e.message ?: "Error al registrar o iniciar sesión"
                )
            }
        }
    }
}
