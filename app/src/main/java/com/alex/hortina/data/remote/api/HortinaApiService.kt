package com.alex.hortina.data.remote.api

import com.alex.hortina.data.remote.dto.CrearTareaRequest
import com.alex.hortina.data.remote.dto.CultivoDetalleDto
import retrofit2.http.GET
import retrofit2.http.Path
import com.alex.hortina.data.remote.dto.CultivoDto
import com.alex.hortina.data.remote.dto.GoogleLoginRequest
import com.alex.hortina.data.remote.dto.LoginRequest
import com.alex.hortina.data.remote.dto.PlantProfileDto
import com.alex.hortina.data.remote.dto.RegistroRequest
import com.alex.hortina.data.remote.dto.TareaDto
import com.alex.hortina.data.remote.dto.TokenResponse
import com.alex.hortina.data.remote.dto.UsuarioDto
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Query

interface HortinaApiService {

    @GET("api/usuarios/me")
    suspend fun getProfile(): UsuarioDto

    @GET("api/cultivos")
    suspend fun getCultivos(): List<CultivoDto>

    @GET("api/cultivos/{id}")
    suspend fun getCultivoById(@Path("id") id: Int): CultivoDto

    @GET("api/cultivos/{id}/detalle")
    suspend fun getCultivoDetalle(@Path("id") id: Int): CultivoDetalleDto

    // para búsquedas (de momento vai a futuro)
    @GET("api/cultivos/search")
    suspend fun searchCultivos(@Query("q") query: String): List<CultivoDto>

    @GET("api/plants/search")
    suspend fun searchPlants(@Query("query") query: String): List<PlantProfileDto>

    @GET("api/plants/{externalId}")
    suspend fun getPlantById(@Path("externalId") externalId: Int): PlantProfileDto

    @GET("api/tareas")
    suspend fun getTareas(): List<TareaDto>

    @GET("api/tareas/cultivo/{id}")
    suspend fun getTareasPorCultivo(@Path("id") cultivoId: Int): List<TareaDto>

    @PATCH("api/tareas/{id}/estado")
    suspend fun actualizarEstadoTarea(
        @Path("id") id: Int, @Query("completada") completada: Boolean
    ): TareaDto

    @POST("api/auth/login")
    suspend fun login(@Body req: LoginRequest): TokenResponse

    @POST("api/auth/google")
    suspend fun loginWithGoogle(@Body req: GoogleLoginRequest): TokenResponse

    @POST("api/auth/refresh")
    suspend fun refreshToken(@Body refreshToken: String): TokenResponse

    @POST("api/usuarios/registro")
    suspend fun registerUser(@Body req: RegistroRequest): UsuarioDto

    @POST("api/cultivos")
    suspend fun createCultivo(@Body dto: CultivoDto): CultivoDto

    @POST("api/tareas")
    suspend fun crearTarea(@Body req: CrearTareaRequest): TareaDto

    @PUT("api/cultivos/{id}")
    suspend fun updateCultivo(@Path("id") id: Int, @Body cultivo: CultivoDto): CultivoDto

    @DELETE("api/cultivos/{id}")
    suspend fun deleteCultivo(@Path("id") id: Int)

}