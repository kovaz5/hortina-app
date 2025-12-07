package com.alex.hortina.ui.screens.perfil

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alex.hortina.data.local.UserPreferencesDataStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.combine

class PerfilViewModel(
    private val dataStore: UserPreferencesDataStore
) : ViewModel() {

    private val _uiState = MutableStateFlow(PerfilUiState())
    val uiState: StateFlow<PerfilUiState> = _uiState

    private val _shouldRecreate = MutableStateFlow(false)
    val shouldRecreate: StateFlow<Boolean> = _shouldRecreate

    init {
        viewModelScope.launch {

            val lang = dataStore.getLanguage() ?: "es"
            val user = dataStore.user.first()
            val notificationsEnabled = dataStore.notificationsEnabledFlow.first()
            val darkMode = dataStore.darkModeFlow.first()

            _uiState.value = PerfilUiState(
                nombre = user?.name ?: "",
                email = user?.email ?: "",
                idioma = lang,
                loading = false,
                notificacionesEnabled = notificationsEnabled,
                darkMode = darkMode
            )
        }

        viewModelScope.launch {
            dataStore.notificationsEnabledFlow.collect { enabled ->
                _uiState.value = _uiState.value.copy(notificacionesEnabled = enabled)
            }
        }

        viewModelScope.launch {
            dataStore.darkModeFlow.collect { enabled ->
                _uiState.value = _uiState.value.copy(darkMode = enabled)
            }
        }
    }

    fun cambiarIdioma(lang: String) {
        viewModelScope.launch {
            dataStore.saveLanguage(lang)
            _uiState.value = _uiState.value.copy(idioma = lang)
            _shouldRecreate.value = true
        }
    }

    fun cambiarDarkMode(enabled: Boolean) {
        viewModelScope.launch {
            dataStore.setDarkMode(enabled)
            _shouldRecreate.value = true
        }
    }

    fun resetRecreateFlag() {
        _shouldRecreate.value = false
    }

    fun cambiarNotificaciones(enabled: Boolean) {
        viewModelScope.launch {
            dataStore.setNotificationsEnabled(enabled)
        }
    }

    suspend fun logout() {
        dataStore.clearUser()
        dataStore.clearTokens()
    }
}
