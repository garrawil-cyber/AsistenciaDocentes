package pe.colegio.asistencia.data.repository

import pe.colegio.asistencia.data.api.RetrofitClient
import pe.colegio.asistencia.data.model.LoginRequest
import pe.colegio.asistencia.data.model.LoginResponse
import retrofit2.HttpException

class AuthRepository {

    private val api = RetrofitClient.api

    suspend fun login(username: String, password: String): Result<LoginResponse> {
        return try {
            val response = api.login(LoginRequest(username, password))
            if (response.success) {
                Result.success(response)
            } else {
                Result.failure(Exception(response.message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Verifica si el token guardado (en DataStore) sigue siendo válido.
     * El Interceptor agrega automáticamente el Bearer.
     */
    suspend fun verificarSesion(): Result<Boolean> {
        return try {
            val response = api.me()
            if (response.success) {
                Result.success(true)
            } else {
                Result.failure(Exception("Token inválido"))
            }
        } catch (e: HttpException) {
            Result.failure(Exception("Token expirado"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}