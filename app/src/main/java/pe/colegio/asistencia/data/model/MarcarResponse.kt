package pe.colegio.asistencia.data.model

data class MarcarResponse(
    val success: Boolean,
    val message: String,
    val data: MarcarData?
)

data class MarcarData(
    val tipo: String,              // "entrada" | "salida"
    val hora: String,
    val estado: String? = null,    // "presente" | "tarde" (solo entrada)
    val minutos_tardanza: Int? = null,
    val mensaje: String,
    val asistencia_id: Int? = null
)