package pe.colegio.asistencia.data.model



data class LoginRequest(
    val username: String,
    val password: String,
    val dispositivo: String = "Android"
)