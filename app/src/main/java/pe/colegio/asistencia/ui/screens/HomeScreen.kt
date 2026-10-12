package pe.colegio.asistencia.ui.screens

import android.content.Context
import android.media.RingtoneManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import pe.colegio.asistencia.data.model.EstadoHoy
import pe.colegio.asistencia.ui.viewmodel.HomeViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Composable
fun HomeScreen(
    username: String,
    nombreCompleto: String?,
    rol: String,
    mensaje: String? = null,
    onMensajeConsumido: () -> Unit = {},
    onMarcarAsistencia: () -> Unit,
    onVerHistorial: () -> Unit,
    onLogout: () -> Unit,
    viewModel: HomeViewModel = viewModel()
) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Feedback al mostrar mensaje: sonido + vibración
    LaunchedEffect(mensaje) {
        if (!mensaje.isNullOrEmpty()) {
            if (mensaje.startsWith("✅")) {
                reproducirSonidoExito(context)
                vibrar(context, 200)
            } else {
                vibrar(context, 100)
            }

            // Recargar el estado del día
            viewModel.cargarEstadoHoy()

            // Mostrar snackbar
            snackbarHostState.showSnackbar(mensaje)
            onMensajeConsumido()
        }
    }

    val nombreDisplay = nombreCompleto?.takeIf { it.isNotBlank() } ?: username

    val iniciales = remember(nombreDisplay) {
        nombreDisplay
            .split(" ")
            .filter { it.isNotBlank() }
            .take(2)
            .joinToString("") { it.first().uppercase() }
    }

    val saludo = remember {
        val hora = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        when (hora) {
            in 0..11 -> "Buenos días 👋"
            in 12..18 -> "Buenas tardes 👋"
            else -> "Buenas noches 👋"
        }
    }

    val fechaActual = remember {
        val formato = SimpleDateFormat("EEEE, d 'de' MMMM 'de' yyyy", Locale("es", "PE"))
        formato.format(Calendar.getInstance().time)
            .replaceFirstChar { it.uppercase() }
    }

    val esDocente = rol.equals("docente", ignoreCase = true)

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
        ) {
            // ===== Tarjeta de bienvenida =====
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(20.dp)
                    )
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .background(
                            color = MaterialTheme.colorScheme.primary,
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = iniciales,
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = saludo,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = nombreDisplay,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontSize = 20.sp,
                        lineHeight = 24.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "📅 $fechaActual",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.75f),
                        fontSize = 12.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ===== Card de estado del día =====
            if (esDocente) {
                EstadoHoyCard(
                    cargando = uiState.cargandoEstado,
                    estado = uiState.estadoHoy
                )
                Spacer(modifier = Modifier.height(24.dp))
            }

            // ===== Acciones =====
            Text(
                text = "ACCIONES",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(12.dp))

            if (esDocente) {
                val (textoBoton, botonHabilitado) = when (uiState.estadoHoy?.estado) {
                    "sin_marcar" -> "📷  Marcar Asistencia" to true
                    "entrada_marcada" -> "📷  Marcar Salida" to true
                    "completo" -> "✅  Ya marcaste hoy" to false
                    else -> "📷  Marcar Asistencia" to true
                }

                Button(
                    onClick = onMarcarAsistencia,
                    enabled = botonHabilitado,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text(textoBoton, fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedButton(
                    onClick = onVerHistorial,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text("📋  Ver Historial", fontSize = 16.sp)
                }

            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(14.dp)
                        )
                        .padding(20.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = "ℹ️  Sesión iniciada como ${rol.replaceFirstChar { it.uppercase() }}",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Para gestionar docentes, reportes, justificaciones y más, accede al Panel Admin desde tu navegador web.",
                            style = MaterialTheme.typography.bodyMedium,
                            lineHeight = 20.sp
                        )
                        Text(
                            text = "🌐  192.168.0.19/asistencia-api/admin/",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 13.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Text(
                text = "Cerrar sesión",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onLogout() }
                    .padding(16.dp)
            )
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
        ) { data ->
            Snackbar(
                containerColor = if (data.visuals.message.startsWith("✅"))
                    MaterialTheme.colorScheme.primaryContainer
                else
                    MaterialTheme.colorScheme.errorContainer
            ) {
                Text(
                    text = data.visuals.message,
                    color = if (data.visuals.message.startsWith("✅"))
                        MaterialTheme.colorScheme.onPrimaryContainer
                    else
                        MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }
    }
}

@Composable
private fun EstadoHoyCard(cargando: Boolean, estado: EstadoHoy?) {
    val colorEstado = when (estado?.estado) {
        "sin_marcar" -> MaterialTheme.colorScheme.surfaceVariant
        "entrada_marcada" -> MaterialTheme.colorScheme.tertiaryContainer
        "completo" -> MaterialTheme.colorScheme.secondaryContainer
        else -> MaterialTheme.colorScheme.surfaceVariant
    }

    val iconoTitulo = when (estado?.estado) {
        "sin_marcar" -> "⏳"
        "entrada_marcada" -> "🟢"
        "completo" -> "✅"
        else -> "ℹ️"
    }

    val titulo = when (estado?.estado) {
        "sin_marcar" -> "Aún no has marcado hoy"
        "entrada_marcada" -> "Entrada registrada"
        "completo" -> "Jornada completa"
        else -> "Estado de hoy"
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(colorEstado, RoundedCornerShape(16.dp))
            .padding(18.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                text = "$iconoTitulo  $titulo",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )

            if (cargando) {
                Text(
                    "Cargando...",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else if (estado != null) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "⬇️  Entrada:",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = estado.entrada?.let { formatearHora(it) } ?: "—",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (estado.entrada != null)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    if (estado.estadoAsistencia == "tarde") {
                        Text(
                            "  ⏰ ${estado.minutosTardanza} min",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "⬆️  Salida:",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = estado.salida?.let { formatearHora(it) } ?: "—",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (estado.salida != null)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

private fun formatearHora(timestamp: String): String {
    return try {
        val input = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val output = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        output.format(input.parse(timestamp)!!)
    } catch (e: Exception) {
        timestamp
    }
}

private fun reproducirSonidoExito(context: Context) {
    try {
        val uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val ringtone = RingtoneManager.getRingtone(context, uri)
        ringtone.play()
    } catch (e: Exception) {
        // Silencioso
    }
}

private fun vibrar(context: Context, duracionMs: Long) {
    try {
        val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val manager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            manager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(duracionMs, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(duracionMs)
        }
    } catch (e: Exception) {
        // Silencioso
    }
}