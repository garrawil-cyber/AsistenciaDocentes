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
import pe.colegio.asistencia.ui.screens.HistorialScreen
import pe.colegio.asistencia.ui.screens.QrScannerScreen

import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import pe.colegio.asistencia.data.api.RetrofitClient
import pe.colegio.asistencia.ui.screens.SplashScreen
import pe.colegio.asistencia.ui.viewmodel.SplashState
import pe.colegio.asistencia.ui.viewmodel.SplashViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // ⚠️ NUEVO: Inicializar RetrofitClient con el SessionManager
        RetrofitClient.init(applicationContext)
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
    val nombreCompleto by session.nombreCompletoFlow.collectAsState(initial = null)
    val rol by session.rolFlow.collectAsState(initial = "")
    var mensajeResultado by remember { mutableStateOf<String?>(null) }

    // SplashViewModel para verificar sesión
    val splashViewModel: SplashViewModel = viewModel()
    val splashState by splashViewModel.estado.collectAsStateWithLifecycle()

    NavHost(
        navController = navController,
        startDestination = "splash"   // ← Cambio: empieza en splash
    ) {
        // ============ SPLASH ============
        composable("splash") {
            SplashScreen()

            // Cuando termina de verificar → navegar
            LaunchedEffect(splashState) {
                when (splashState) {
                    is SplashState.Autenticado -> {
                        navController.navigate("home") {
                            popUpTo("splash") { inclusive = true }
                        }
                    }
                    is SplashState.NoAutenticado -> {
                        navController.navigate("login") {
                            popUpTo("splash") { inclusive = true }
                        }
                    }
                    else -> { /* sigue en splash */ }
                }
            }
        }

        // ============ LOGIN ============
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

        // ============ HOME ============
        composable("home") {
            HomeScreen(
                username = username ?: "Docente",
                nombreCompleto = nombreCompleto,    // ← NUEVO
                rol = rol ?: "docente",
                mensaje = mensajeResultado,
                onMensajeConsumido = { mensajeResultado = null },
                onMarcarAsistencia = {
                    navController.navigate("scanner")
                },
                onVerHistorial = {
                    navController.navigate("historial")
                },
                onLogout = {
                    scope.launch {
                        session.limpiarSesion()
                    }
                    navController.navigate("login") {
                        popUpTo(0) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }

        // ============ SCANNER QR ============
        composable("scanner") {
            QrScannerScreen(
                onResultado = { mensaje ->
                    mensajeResultado = mensaje
                    navController.popBackStack()
                },
                onCancelar = {
                    navController.popBackStack()
                }
            )
        }

        // ============ HISTORIAL ============
        composable("historial") {
            HistorialScreen(
                onVolver = {
                    navController.popBackStack()
                }
            )
        }
    }
}