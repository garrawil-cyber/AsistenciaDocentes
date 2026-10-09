package pe.colegio.asistencia

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import pe.colegio.asistencia.data.session.SessionManager
import pe.colegio.asistencia.ui.screens.HomeScreen
import pe.colegio.asistencia.ui.screens.LoginScreen
import pe.colegio.asistencia.ui.theme.AsistenciaDocentesTheme

import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch

import pe.colegio.asistencia.ui.screens.QrScannerScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AsistenciaDocentesTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation()
                }
            }
        }
    }
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val session = remember { SessionManager(context) }
    val scope = rememberCoroutineScope()

    val username by session.usernameFlow.collectAsState(initial = "")

    // ✅ Nuevo: mensaje para mostrar en el Home
    var mensajeResultado by remember { mutableStateOf<String?>(null) }

    NavHost(
        navController = navController,
        startDestination = "login"
    ) {
        composable("login") {
            LoginScreen(
                onLoginExitoso = {
                    navController.navigate("home") {
                        popUpTo("login") { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }

        composable("home") {
            HomeScreen(
                username = username ?: "Docente",
                mensaje = mensajeResultado,                      // ← NUEVO
                onMensajeConsumido = { mensajeResultado = null }, // ← NUEVO
                onMarcarAsistencia = {
                    navController.navigate("scanner")
                },
                onVerHistorial = { },
                onLogout = {
                    scope.launch { session.limpiarSesion() }
                    navController.navigate("login") {
                        popUpTo(0) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }

        composable("scanner") {
            QrScannerScreen(
                onResultado = { mensaje ->
                    mensajeResultado = mensaje       // ← guardar
                    navController.popBackStack()
                },
                onCancelar = {
                    navController.popBackStack()
                }
            )
        }

    }
}