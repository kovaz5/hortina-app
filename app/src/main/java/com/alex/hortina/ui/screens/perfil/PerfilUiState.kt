package com.alex.hortina.ui.screens.perfil

data class PerfilUiState(
    val nombre: String = "",
    val email: String = "",
    val idioma: String = "es",
    val loading: Boolean = true,
    val error: String? = null,
    val notificacionesEnabled: Boolean = true

)
