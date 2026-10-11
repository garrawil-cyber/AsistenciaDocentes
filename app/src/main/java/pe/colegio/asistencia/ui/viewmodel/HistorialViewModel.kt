package pe.colegio.asistencia.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pe.colegio.asistencia.data.model.AsistenciaDia
import pe.colegio.asistencia.data.repository.AsistenciaRepository
import pe.colegio.asistencia.data.session.SessionManager
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

// Estado de la pantalla de historial
data class HistorialUiState(
    val cargando: Boolean = false,
    val error: String? = null,
    val asistencias: List<AsistenciaDia> = emptyList(),
    val rangoSeleccionado: RangoFecha = RangoFecha.SEMANA
)

enum class RangoFecha(val label: String) {
    SEMANA("Semana"),
    MES("Este mes"),
    MES_ANTERIOR("Mes ant."),
    ANIO("Este año")
}

class HistorialViewModel(app: Application) : AndroidViewModel(app) {

    private val repo = AsistenciaRepository()
    private val session = SessionManager(app)

    private val _uiState = MutableStateFlow(HistorialUiState())
    val uiState: StateFlow<HistorialUiState> = _uiState.asStateFlow()

    init {
        cargarHistorial(RangoFecha.SEMANA)
    }

    fun cargarHistorial(rango: RangoFecha) {
        _uiState.update {
            it.copy(cargando = true, error = null, rangoSeleccionado = rango)
        }

        viewModelScope.launch {
            try {
                val jwt = session.tokenFlow.first()
                if (jwt.isNullOrEmpty()) {
                    _uiState.update {
                        it.copy(cargando = false, error = "Sesión expirada. Vuelve a iniciar sesión.")
                    }
                    return@launch
                }

                val (desde, hasta) = calcularRango(rango)
                val result = repo.historial(desde, hasta, jwt)

                result.fold(
                    onSuccess = { response ->
                        _uiState.update {
                            it.copy(
                                cargando = false,
                                asistencias = response.data ?: emptyList(),
                                error = null
                            )
                        }
                    },
                    onFailure = { e ->
                        _uiState.update {
                            it.copy(
                                cargando = false,
                                error = e.message ?: "Error al cargar historial"
                            )
                        }
                    }
                )
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        cargando = false,
                        error = e.message ?: "Error inesperado"
                    )
                }
            }
        }
    }

    /**
     * Calcula el par (desde, hasta) en formato YYYY-MM-DD
     * según el rango seleccionado.
     */
    private fun calcularRango(rango: RangoFecha): Pair<String, String> {
        val formato = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val hoy = Calendar.getInstance()
        val desde = Calendar.getInstance()

        when (rango) {
            RangoFecha.SEMANA -> {
                desde.add(Calendar.DAY_OF_YEAR, -7)
            }
            RangoFecha.MES -> {
                desde.set(Calendar.DAY_OF_MONTH, 1)
            }
            RangoFecha.MES_ANTERIOR -> {
                // Último día del mes anterior
                desde.set(Calendar.DAY_OF_MONTH, 1)
                desde.add(Calendar.DAY_OF_YEAR, -1)
                // Primer día del mes anterior
                desde.set(Calendar.DAY_OF_MONTH, 1)

                // "hoy" se convierte en el último día del mes anterior
                hoy.set(Calendar.DAY_OF_MONTH, 1)
                hoy.add(Calendar.DAY_OF_YEAR, -1)
            }
            RangoFecha.ANIO -> {
                desde.set(Calendar.DAY_OF_YEAR, 1)
            }
        }

        return Pair(formato.format(desde.time), formato.format(hoy.time))
    }
}