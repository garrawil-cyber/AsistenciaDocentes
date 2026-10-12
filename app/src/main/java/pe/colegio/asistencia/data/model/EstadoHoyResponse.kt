package pe.colegio.asistencia.data.model

import com.google.gson.annotations.SerializedName

data class EstadoHoyResponse(
    val success: Boolean,
    val message: String,
    val data: EstadoHoy?
)

data class EstadoHoy(
    val estado: String,
    val entrada: String?,
    val salida: String?,
    @SerializedName("minutos_tardanza")
    val minutosTardanza: Int,
    @SerializedName("estado_asistencia")
    val estadoAsistencia: String?
)