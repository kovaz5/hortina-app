package com.alex.hortina.data.repository

import com.alex.hortina.data.remote.api.HortinaApiService
import com.alex.hortina.data.remote.api.RetrofitClient
import com.alex.hortina.data.remote.dto.LoginRequest
import com.alex.hortina.data.remote.dto.RegistroRequest
import com.alex.hortina.data.remote.dto.UsuarioDto

class UserRepository {

    private val api = RetrofitClient.instance.create(HortinaApiService::class.java)

    suspend fun register(usuario: RegistroRequest): UsuarioDto {
        return api.registerUser(usuario)
    }

    suspend fun login(loginRequest: LoginRequest): UsuarioDto {
        return api.login(loginRequest)
    }

    suspend fun getProfile(): UsuarioDto {
        return api.getProfile()
    }
}
