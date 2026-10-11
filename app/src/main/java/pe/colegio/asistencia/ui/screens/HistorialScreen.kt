package pe.colegio.asistencia.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import pe.colegio.asistencia.data.model.AsistenciaDia
import pe.colegio.asistencia.ui.viewmodel.HistorialViewModel
import pe.colegio.asistencia.ui.viewmodel.RangoFecha
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistorialScreen(
    onVolver: () -> Unit,
    viewModel: HistorialViewModel = viewModel()
) {
    val estado by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mi Historial") },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Filtros de rango
            RangoSelector(
                seleccionado = estado.rangoSeleccionado,
                onSeleccionar = { viewModel.cargarHistorial(it) }
            )

            when {
                // Cargando
                estado.cargando -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator()
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("Cargando historial...")
                        }
                    }
                }

                // Error
                estado.error != null -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(32.dp)
                        ) {
                            Text("⚠️", fontSize = 48.sp)
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                estado.error ?: "Error desconocido",
                                color = MaterialTheme.colorScheme.error,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                // Sin datos
                estado.asistencias.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("📭", fontSize = 64.sp)
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                "Sin asistencias en este período",
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Con datos
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(estado.asistencias) { asistencia ->
                            AsistenciaCard(asistencia)
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RangoSelector(
    seleccionado: RangoFecha,
    onSeleccionar: (RangoFecha) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        RangoFecha.entries.forEach { rango ->
            FilterChip(
                selected = rango == seleccionado,
                onClick = { onSeleccionar(rango) },
                label = {
                    Text(
                        rango.label,
                        fontSize = 11.sp,
                        fontWeight = if (rango == seleccionado) FontWeight.Bold else FontWeight.Normal
                    )
                },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun AsistenciaCard(asistencia: AsistenciaDia) {
    val colorEstado = when (asistencia.estado) {
        "presente" -> Color(0xFF10b981)
        "tarde" -> Color(0xFFf59e0b)
        "ausente" -> Color(0xFFef4444)
        "justificado" -> Color(0xFF3b82f6)
        "falta_salida" -> Color(0xFF6b7280)
        else -> Color(0xFF6b7280)
    }

    val iconoEstado = when (asistencia.estado) {
        "presente" -> "✓"
        "tarde" -> "⏰"
        "ausente" -> "✕"
        "justificado" -> "📋"
        "falta_salida" -> "⚠"
        else -> "•"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Círculo con icono según estado
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(colorEstado.copy(alpha = 0.15f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    iconoEstado,
                    fontSize = 20.sp,
                    color = colorEstado,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            // Info de la asistencia
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = formatearFecha(asistencia.fecha),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(4.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    if (asistencia.hora_entrada != null && asistencia.hora_entrada.length >= 16) {
                        Text(
                            "↓ ${asistencia.hora_entrada.substring(11, 16)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF10b981),
                            fontWeight = FontWeight.Medium
                        )
                    }
                    if (asistencia.hora_salida != null && asistencia.hora_salida.length >= 16) {
                        Text(
                            "↑ ${asistencia.hora_salida.substring(11, 16)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF3b82f6),
                            fontWeight = FontWeight.Medium
                        )
                    }
                    if (asistencia.minutos_tardanza > 0) {
                        Text(
                            "⏱ ${asistencia.minutos_tardanza} min",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFf59e0b)
                        )
                    }
                }
            }

            // Badge con el estado
            Box(
                modifier = Modifier
                    .background(colorEstado.copy(alpha = 0.15f), RoundedCornerShape(20.dp))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = asistencia.estado.replace("_", " ").uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = colorEstado,
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp
                )
            }
        }
    }
}

/**
 * Convierte "2026-10-09" a "Miércoles, 9 de octubre"
 */
private fun formatearFecha(fecha: String): String {
    return try {
        val input = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val output = SimpleDateFormat("EEEE, d 'de' MMMM", Locale("es", "PE"))
        val date = input.parse(fecha)
        val texto = output.format(date!!)
        texto.replaceFirstChar { it.uppercase() }
    } catch (e: Exception) {
        fecha
    }
}