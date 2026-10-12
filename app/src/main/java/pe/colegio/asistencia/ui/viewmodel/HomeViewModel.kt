package pe.colegio.asistencia.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pe.colegio.asistencia.data.model.EstadoHoy
import pe.colegio.asistencia.data.repository.AsistenciaRepository

data class HomeUiState(
    val cargandoEstado: Boolean = false,
    val estadoHoy: EstadoHoy? = null,
    val error: String? = null
)

class HomeViewModel(app: Application) : AndroidViewModel(app) {

    private val repo = AsistenciaRepository()

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        cargarEstadoHoy()
    }

    fun cargarEstadoHoy() {
        _uiState.update { it.copy(cargandoEstado = true, error = null) }

        viewModelScope.launch {
            val resultado = repo.estadoHoy()
            resultado.fold(
                onSuccess = { response ->
                    _uiState.update {
                        it.copy(
                            cargandoEstado = false,
                            estadoHoy = response.data,
                            error = null
                        )
                    }
                },
                onFailure = { e ->
                    _uiState.update {
                        it.copy(
                            cargandoEstado = false,
                            error = e.message ?: "Error al cargar estado"
                        )
                    }
                }
            )
        }
    }
}