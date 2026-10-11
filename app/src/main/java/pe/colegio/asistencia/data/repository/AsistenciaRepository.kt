package pe.colegio.asistencia.data.repository

import com.google.gson.Gson
import pe.colegio.asistencia.data.api.RetrofitClient
import pe.colegio.asistencia.data.model.HistorialResponse
import pe.colegio.asistencia.data.model.MarcarRequest
import pe.colegio.asistencia.data.model.MarcarResponse
import retrofit2.HttpException

class AsistenciaRepository {

    private val api = RetrofitClient.api
    private val gson = Gson()

    suspend fun marcar(qrToken: String, jwt: String): Result<MarcarResponse> {
        return try {
            val response = api.marcar(
                token = "Bearer $jwt",
                request = MarcarRequest(qrToken)
            )
            if (response.success) {
                Result.success(response)
            } else {
                Result.failure(Exception(response.message))
            }
        } catch (e: HttpException) {
            val mensaje = try {
                val errorBody = e.response()?.errorBody()?.string()
                if (!errorBody.isNullOrEmpty()) {
                    val apiError = gson.fromJson(errorBody, MarcarResponse::class.java)
                    apiError.message.ifEmpty { mensajePorCodigo(e.code()) }
                } else {
                    mensajePorCodigo(e.code())
                }
            } catch (parseError: Exception) {
                mensajePorCodigo(e.code())
            }
            Result.failure(Exception(mensaje))
        } catch (e: Exception) {
            Result.failure(Exception("Error de conexión: ${e.message ?: "desconocido"}"))
        }
    }

    suspend fun historial(
        desde: String,
        hasta: String,
        jwt: String
    ): Result<HistorialResponse> {
        return try {
            val response = api.historial(
                token = "Bearer $jwt",
                desde = desde,
                hasta = hasta
            )
            if (response.success) {
                Result.success(response)
            } else {
                Result.failure(Exception(response.message))
            }
        } catch (e: HttpException) {
            val mensaje = try {
                val errorBody = e.response()?.errorBody()?.string()
                if (!errorBody.isNullOrEmpty()) {
                    val apiError = gson.fromJson(errorBody, HistorialResponse::class.java)
                    apiError.message.ifEmpty { mensajePorCodigo(e.code()) }
                } else {
                    mensajePorCodigo(e.code())
                }
            } catch (parseError: Exception) {
                mensajePorCodigo(e.code())
            }
            Result.failure(Exception(mensaje))
        } catch (e: Exception) {
            Result.failure(Exception("Error de conexión: ${e.message ?: "desconocido"}"))
        }
    }

    private fun mensajePorCodigo(code: Int): String {
        return when (code) {
            410 -> "El código QR ya expiró. Intenta con el nuevo QR."
            409 -> "Ya completaste tu marcación de hoy."
            403 -> "No tienes permiso para marcar asistencia."
            401 -> "Tu sesión expiró. Vuelve a iniciar sesión."
            422 -> "Faltan datos para procesar la marcación."
            500 -> "Error del servidor. Intenta más tarde."
            else -> "Error inesperado (código $code)."
        }
    }
}