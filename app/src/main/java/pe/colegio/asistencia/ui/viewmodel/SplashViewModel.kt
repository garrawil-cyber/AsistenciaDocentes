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
import pe.colegio.asistencia.data.repository.AuthRepository
import pe.colegio.asistencia.data.session.SessionManager

sealed class SplashState {
    data object Cargando : SplashState()
    data object Autenticado : SplashState()
    data object NoAutenticado : SplashState()
}

class SplashViewModel(app: Application) : AndroidViewModel(app) {

    private val session = SessionManager(app)

    private val _estado = MutableStateFlow<SplashState>(SplashState.Cargando)
    val estado: StateFlow<SplashState> = _estado.asStateFlow()

    init {
        verificarSesion()
    }

    /**
     * Verifica si el token guardado sigue siendo válido.
     * Si es válido → Autenticado
     * Si no hay token o está expirado → NoAutenticado
     */
    private fun verificarSesion() {
        viewModelScope.launch {
            try {
                // Leer el token del DataStore
                val token = session.tokenFlow.first()
                val username = session.usernameFlow.first()

                // Si no hay token → NoAutenticado
                if (token.isNullOrEmpty() || username.isNullOrEmpty()) {
                    _estado.update { SplashState.NoAutenticado }
                    return@launch
                }

                // Verificar contra el backend
                val repo = AuthRepository()
                val resultado = repo.verificarSesion(token)

                resultado.fold(
                    onSuccess = {
                        // Token válido → Autenticado
                        _estado.update { SplashState.Autenticado }
                    },
                    onFailure = {
                        // Token inválido o expirado → limpiar y pedir login
                        session.limpiarSesion()
                        _estado.update { SplashState.NoAutenticado }
                    }
                )
            } catch (e: Exception) {
                // Error de red u otro → asumir NoAutenticado por seguridad
                _estado.update { SplashState.NoAutenticado }
            }
        }
    }
}