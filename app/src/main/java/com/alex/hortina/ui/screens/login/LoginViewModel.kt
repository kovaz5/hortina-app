package com.alex.hortina.ui.screens.login

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.alex.hortina.data.local.UserPreferencesDataStore
import com.alex.hortina.data.remote.api.HortinaApiService
import com.alex.hortina.data.remote.api.RetrofitClient
import com.alex.hortina.data.remote.dto.LoginRequest
import com.alex.hortina.data.remote.dto.UsuarioDto
import com.alex.hortina.data.remote.dto.GoogleLoginRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential

sealed class LoginUiState {
    object Idle : LoginUiState()
    object Loading : LoginUiState()
    data class Success(val usuario: UsuarioDto) : LoginUiState()
    data class Error(val message: String) : LoginUiState()
}

class LoginViewModel(application: Application) : AndroidViewModel(application) {

    private val api = RetrofitClient.instance.create(HortinaApiService::class.java)
    private val dataStore = UserPreferencesDataStore(application.applicationContext)

    private val _uiState = MutableStateFlow<LoginUiState>(LoginUiState.Idle)
    val uiState: StateFlow<LoginUiState> = _uiState

    fun login(email: String, password: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _uiState.value = LoginUiState.Loading
            try {
                val result = api.login(LoginRequest(email, password))

                dataStore.saveTokens(result.accessToken, result.refreshToken)

                result.usuario?.let { u ->
                    dataStore.saveUser(
                        id = u.id_usuario.toString(),
                        name = u.nombre.toString(),
                        email = u.email.toString()
                    )
                }

                _uiState.value = LoginUiState.Success(result.usuario!!)
                onSuccess()

            } catch (e: Exception) {
                _uiState.value = LoginUiState.Error("Credenciales inválidas o error de conexión")
            }
        }
    }

    fun loginWithGoogle(idToken: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _uiState.value = LoginUiState.Loading
            try {
                val result = api.loginWithGoogle(GoogleLoginRequest(idToken))

                dataStore.saveTokens(result.accessToken, result.refreshToken)

                result.usuario?.let { u ->
                    dataStore.saveUser(
                        id = u.id_usuario.toString(),
                        name = u.nombre.toString(),
                        email = u.email.toString()
                    )
                }

                _uiState.value = LoginUiState.Success(result.usuario!!)
                onSuccess()

            } catch (e: Exception) {
                _uiState.value = LoginUiState.Error("Error al iniciar sesión con Google")
            }
        }
    }

    fun signInWithGoogle(
        context: Context,
        credentialManager: CredentialManager,
        request: GetCredentialRequest,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            try {
                val result = credentialManager.getCredential(
                    context = context, request = request
                )

                val credential = result.credential

                if (credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {

                    val googleCred = GoogleIdTokenCredential.createFrom(credential.data)
                    val idToken = googleCred.idToken

                    loginWithGoogle(idToken) {
                        onSuccess()
                    }

                } else {
                    Log.e("GOOGLE", "Tipo inesperado: ${credential.type}")
                    _uiState.value = LoginUiState.Error("No se pudo obtener el token de Google")
                }

            } catch (e: Exception) {
                Log.e("GOOGLE", "Error en signInWithGoogle", e)
                _uiState.value = LoginUiState.Error("Error iniciando sesión con Google")
            }
        }
    }

}
