package pe.colegio.asistencia.data.repository

import pe.colegio.asistencia.data.api.RetrofitClient
import pe.colegio.asistencia.data.model.LoginRequest
import pe.colegio.asistencia.data.model.LoginResponse

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
}