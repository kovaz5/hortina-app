package com.alex.hortina.data

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {

    @POST("api/usuarios/registro.php")
    fun registrarUsuario(@Body usuario: Usuario): Call<ApiResponse>
}