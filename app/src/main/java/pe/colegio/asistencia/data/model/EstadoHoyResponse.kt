package pe.colegio.asistencia.data.model

import com.google.gson.annotations.SerializedName

data class EstadoHoyResponse(
    val success: Boolean,
    val message: String,
    val data: EstadoHoy?
)

data class EstadoHoy(
    val estado: String, // "sin_marcar", "entrada_marcada", "completo"
    val turno: String?, // "manana", "tarde"
    @SerializedName("turno_nombre")
    val turnoNombre: String?, // "Turno Mañana", "Turno Tarde"
    @SerializedName("hora_entrada")
    val horaEntrada: String?,
    @SerializedName("hora_salida")
    val horaSalida: String?,
    @SerializedName("minutos_tardanza")
    val minutosTardanza: Int,
    @SerializedName("estado_asistencia")
    val estadoAsistencia: String?,
    @SerializedName("estado_salida")
    val estadoSalida: String?
)