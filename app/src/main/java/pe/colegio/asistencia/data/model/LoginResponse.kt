package pe.colegio.asistencia.data.model

import com.google.gson.annotations.SerializedName

data class LoginResponse(
    val success: Boolean,
    val message: String,
    val data: LoginData?
)

data class LoginData(
    val token: String,
    val usuario: UsuarioData
)

data class UsuarioData(
    val id: Int,
    val username: String,
    val rol: String,
    @SerializedName("docente_id")
    val docenteId: Int?,
    @SerializedName("nombre_completo")
    val nombreCompleto: String?
)