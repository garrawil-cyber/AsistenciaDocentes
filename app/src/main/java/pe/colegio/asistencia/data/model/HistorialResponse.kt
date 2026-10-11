package pe.colegio.asistencia.data.model


data class HistorialResponse(
    val success: Boolean,
    val message: String,
    val data: List<AsistenciaDia>?
)

data class AsistenciaDia(
    val id: Int,
    val fecha: String,
    val hora_entrada: String?,
    val hora_salida: String?,
    val minutos_tardanza: Int,
    val metodo_marcacion: String?,
    val observacion: String?,
    val estado: String
)

