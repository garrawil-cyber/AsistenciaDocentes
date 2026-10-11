package pe.colegio.asistencia.data.api

import pe.colegio.asistencia.data.model.HistorialResponse
import pe.colegio.asistencia.data.model.LoginRequest
import pe.colegio.asistencia.data.model.LoginResponse
import pe.colegio.asistencia.data.model.MarcarRequest
import pe.colegio.asistencia.data.model.MarcarResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Query

interface ApiService {

    @POST("api/v1/auth/login")
    suspend fun login(@Body request: LoginRequest): LoginResponse

    @GET("api/v1/auth/me")
    suspend fun me(@Header("Authorization") token: String): LoginResponse

    @POST("api/v1/asistencia/marcar")
    suspend fun marcar(
        @Header("Authorization") token: String,
        @Body request: MarcarRequest
    ): MarcarResponse

    @GET("api/v1/asistencia/historial")
    suspend fun historial(
        @Header("Authorization") token: String,
        @Query("desde") desde: String,
        @Query("hasta") hasta: String
    ): HistorialResponse
}