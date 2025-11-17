package com.alex.hortina.data.remote.dto

data class TokenResponse(
    val accessToken: String, val refreshToken: String, val usuario: UsuarioDto?
)
