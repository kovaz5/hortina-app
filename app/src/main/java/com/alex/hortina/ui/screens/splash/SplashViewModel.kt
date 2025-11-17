package com.alex.hortina.ui.screens.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alex.hortina.data.local.UserPreferencesDataStore
import com.alex.hortina.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class SplashViewModel(
    private val dataStore: UserPreferencesDataStore
) : ViewModel() {

    private val authRepository = AuthRepository(dataStore)

    private val _isSessionValid = MutableStateFlow<Boolean?>(null)
    val isSessionValid: StateFlow<Boolean?> = _isSessionValid

    fun checkLogin() {
        viewModelScope.launch {
            val valid = authRepository.checkSession()
            if (!valid) dataStore.clearUser()
            _isSessionValid.value = valid
        }
    }
}

