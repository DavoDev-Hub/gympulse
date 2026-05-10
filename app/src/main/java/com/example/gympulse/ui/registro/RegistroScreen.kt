package com.example.gympulse.ui.registro
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.gympulse.data.WorkoutEntity
import com.example.gympulse.data.WorkoutRepository
import com.example.gympulse.ui.theme.CardDark
import com.example.gympulse.ui.theme.CyanPrimary
import com.example.gympulse.ui.theme.TextSecondary
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun RegistroScreen(
    repository: WorkoutRepository,
    onNuevoWorkout: () -> Unit
) {
    val viewModel: RegistroViewModel = viewModel(
        factory = RegistroViewModel.Factory(repository)
    )
    val workoutsWithSets by viewModel.workoutsWithSets.collectAsStateWithLifecycle()

    Box(modifier = Modifier.fillMaxSize()) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            // Header
            Text(
                text = mesActual(),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 20.dp)
            )

            if (workoutsWithSets.isEmpty()) {
                // Estado vacío
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Sin entrenamientos aún",
                            color = TextSecondary,
                            fontSize = 16.sp
                        )
                        Text(
                            text = "Pulsa + para registrar uno",
                            color = TextSecondary,
                            fontSize = 14.sp
                        )
                    }
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(workoutsWithSets) { item ->
                        WorkoutCard(
                            workoutWithSets = item,
                            onDelete = { viewModel.deleteWorkout(item.workout) }
                        )
                    }
                    item { Spacer(modifier = Modifier.height(80.dp)) }
                }
            }
        }

        // FAB
        FloatingActionButton(
            onClick = onNuevoWorkout,
            containerColor = CyanPrimary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Nuevo entrenamiento")
        }
    }
}

@Composable
fun WorkoutCard(
    workoutWithSets: RegistroViewModel.WorkoutWithSets,
    onDelete: () -> Unit
) {
    val workout = workoutWithSets.workout
    val sets = workoutWithSets.sets

    // Agrupar ejercicios únicos
    val ejerciciosUnicos = sets
        .groupBy { it.exerciseName }
        .map { (nombre, series) -> "${ series.size }x $nombre" }

    val duracion = duracionMinutos(workout.startTime, workout.endTime)

    Row(verticalAlignment = Alignment.Top) {

        // Fecha lateral
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(52.dp)
        ) {
            Text(
                text = diaSemana(workout.date),
                color = TextSecondary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = numeroDia(workout.date),
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = mes(workout.date),
                color = TextSecondary,
                fontSize = 11.sp
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Card del workout
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = CardDark)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = workout.name.ifEmpty { "Entrenamiento" }.uppercase(),
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (duracion != null) {
                            Text(
                                text = "$duracion min",
                                color = TextSecondary,
                                fontSize = 13.sp
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(
                            onClick = onDelete,
                            modifier = Modifier.size(20.dp)
                        ) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Eliminar",
                                tint = TextSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                if (ejerciciosUnicos.isEmpty()) {
                    Text(
                        text = "Sin ejercicios",
                        color = TextSecondary,
                        fontSize = 13.sp
                    )
                } else {
                    ejerciciosUnicos.forEach { ejercicio ->
                        Text(
                            text = ejercicio,
                            color = TextSecondary,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }
    }
}

// --- Helpers de fecha ---

fun mesActual(): String {
    val sdf = SimpleDateFormat("MMMM", Locale("es"))
    return sdf.format(Date()).replaceFirstChar { it.uppercase() }
}

fun diaSemana(timestamp: Long): String {
    val sdf = SimpleDateFormat("EEE", Locale("es"))
    return sdf.format(Date(timestamp)).uppercase()
}

fun numeroDia(timestamp: Long): String {
    val sdf = SimpleDateFormat("d", Locale("es"))
    return sdf.format(Date(timestamp))
}

fun mes(timestamp: Long): String {
    val sdf = SimpleDateFormat("MMM", Locale("es"))
    return sdf.format(Date(timestamp)).uppercase()
}

fun duracionMinutos(start: Long, end: Long?): Long? {
    if (end == null) return null
    val diff = end - start
    return diff / 60000
}