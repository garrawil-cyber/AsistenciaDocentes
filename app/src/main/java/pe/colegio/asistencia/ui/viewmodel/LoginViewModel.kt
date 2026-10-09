package pe.colegio.asistencia.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pe.colegio.asistencia.data.repository.AuthRepository
import pe.colegio.asistencia.data.session.SessionManager

// Estado de la pantalla de login
data class LoginUiState(
    val username: String = "",
    val password: String = "",
    val cargando: Boolean = false,
    val error: String? = null,
    val loginExitoso: Boolean = false
)

class LoginViewModel(app: Application) : AndroidViewModel(app) {

    private val repo = AuthRepository()
    private val session = SessionManager(app)

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun onUsernameChange(value: String) {
        _uiState.update { it.copy(username = value, error = null) }
    }

    fun onPasswordChange(value: String) {
        _uiState.update { it.copy(password = value, error = null) }
    }

    fun login() {
        val usuario = _uiState.value.username.trim()
        val pass = _uiState.value.password

        // Validación local primero
        if (usuario.isEmpty() || pass.isEmpty()) {
            _uiState.update { it.copy(error = "Ingresa usuario y contraseña") }
            return
        }

        _uiState.update { it.copy(cargando = true, error = null) }

        viewModelScope.launch {
            val resultado = repo.login(usuario, pass)

            resultado.fold(
                onSuccess = { response ->
                    val data = response.data
                    if (data != null) {
                        session.guardarSesion(
                            token = data.token,
                            username = data.usuario.username,
                            rol = data.usuario.rol,
                            docenteId = data.usuario.docenteId
                        )
                        _uiState.update {
                            it.copy(cargando = false, loginExitoso = true)
                        }
                    } else {
                        _uiState.update {
                            it.copy(cargando = false, error = "Respuesta inválida del servidor")
                        }
                    }
                },
                onFailure = { e ->
                    _uiState.update {
                        it.copy(
                            cargando = false,
                            error = e.message ?: "Error de conexión"
                        )
                    }
                }
            )
        }
    }

    fun limpiarError() {
        _uiState.update { it.copy(error = null) }
    }
}