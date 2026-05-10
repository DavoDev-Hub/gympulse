package com.example.gympulse.ui.estadisticas

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.gympulse.data.WorkoutRepository
import com.example.gympulse.ui.theme.*

@Composable
fun EstadisticasScreen(repository: WorkoutRepository) {
    val viewModel: EstadisticasViewModel = viewModel(
        factory = EstadisticasViewModel.Factory(repository)
    )
    val stats by viewModel.stats.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Estadísticas",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 20.dp)
            )
        }

        // --- Tarjetas resumen ---
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatCard(
                    modifier = Modifier.weight(1f),
                    titulo   = "Entrenamientos",
                    valor    = "${stats.totalWorkouts}",
                    icono    = Icons.Default.FitnessCenter
                )
                StatCard(
                    modifier = Modifier.weight(1f),
                    titulo   = "Minutos totales",
                    valor    = "${stats.totalMinutos}",
                    icono    = Icons.Default.Timer
                )
            }
        }

        item {
            StatCard(
                modifier = Modifier.fillMaxWidth(),
                titulo   = "Racha actual",
                valor    = "${stats.rachaActual} días consecutivos",
                icono    = Icons.Default.FitnessCenter
            )
        }

        // --- Gráfica días de la semana ---
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape    = RoundedCornerShape(12.dp),
                colors   = CardDefaults.cardColors(containerColor = CardDark)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Actividad por día",
                        fontWeight = FontWeight.Bold,
                        fontSize   = 15.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    BarChart(data = stats.workoutsPorSemana)
                }
            }
        }

        // --- Ejercicios más frecuentes ---
        if (stats.ejerciciosMasFrecuentes.isNotEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape    = RoundedCornerShape(12.dp),
                    colors   = CardDefaults.cardColors(containerColor = CardDark)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "Ejercicios más frecuentes",
                            fontWeight = FontWeight.Bold,
                            fontSize   = 15.sp
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        stats.ejerciciosMasFrecuentes.forEachIndexed { index, (nombre, veces) ->
                            EjercicioFrecuenteRow(
                                posicion = index + 1,
                                nombre   = nombre,
                                veces    = veces,
                                max      = stats.ejerciciosMasFrecuentes.first().second
                            )
                            if (index < stats.ejerciciosMasFrecuentes.lastIndex) {
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        }
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(80.dp)) }
    }
}

@Composable
fun StatCard(
    modifier: Modifier = Modifier,
    titulo: String,
    valor: String,
    icono: androidx.compose.ui.graphics.vector.ImageVector
) {
    Card(
        modifier = modifier,
        shape    = RoundedCornerShape(12.dp),
        colors   = CardDefaults.cardColors(containerColor = CardDark)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Icon(
                imageVector = icono,
                contentDescription = null,
                tint = CyanPrimary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text  = valor,
                fontWeight = FontWeight.Bold,
                fontSize   = 22.sp
            )
            Text(
                text  = titulo,
                color = TextSecondary,
                fontSize = 12.sp
            )
        }
    }
}

@Composable
fun BarChart(data: List<Pair<String, Int>>) {
    val maxVal = data.maxOfOrNull { it.second }?.takeIf { it > 0 } ?: 1

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.Bottom
    ) {
        data.forEach { (dia, cantidad) ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Bottom,
                modifier = Modifier.weight(1f)
            ) {
                // Barra
                val fraccion = cantidad.toFloat() / maxVal
                Box(
                    modifier = Modifier
                        .width(20.dp)
                        .fillMaxHeight(fraccion.coerceAtLeast(0.04f))
                        .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                        .background(
                            if (cantidad > 0) CyanPrimary
                            else CardDark.copy(alpha = 0.3f)
                        )
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(dia, fontSize = 10.sp, color = TextSecondary)
            }
        }
    }
}

@Composable
fun EjercicioFrecuenteRow(
    posicion: Int,
    nombre: String,
    veces: Int,
    max: Int
) {
    val fraccion = veces.toFloat() / max

    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text  = "$posicion",
                    color = CyanPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize   = 13.sp
                )
                Text(nombre, fontSize = 13.sp)
            }
            Text("$veces veces", color = TextSecondary, fontSize = 12.sp)
        }
        Spacer(modifier = Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(SurfaceDark)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(fraccion)
                    .fillMaxHeight()
                    .background(CyanPrimary)
            )
        }
    }
}