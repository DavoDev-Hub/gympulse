package com.example.gympulse.ui.registro

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.gympulse.data.ExerciseSetEntity
import com.example.gympulse.data.WorkoutEntity
import com.example.gympulse.data.WorkoutRepository
import com.example.gympulse.ui.components.RestDayPickerDialog
import com.example.gympulse.ui.estadisticas.getMonthName
import java.text.SimpleDateFormat
import java.util.*

private val GreenColor = Color(0xFF4CAF50)
private val YellowColor = Color(0xFFFFC107)
private val RedColor = Color(0xFFF44336)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistroScreen(
    repository: WorkoutRepository,
    onNuevoWorkout: () -> Unit
) {
    val viewModel: RegistroViewModel = viewModel(
        factory = RegistroViewModel.Factory(repository)
    )
    val workoutsWithSets by viewModel.workoutsWithSets.collectAsStateWithLifecycle()
    val years by viewModel.yearsAvailable.collectAsStateWithLifecycle()

    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.refresh()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    var showDayPicker by remember { mutableStateOf(false) }
    var añoExpanded by remember { mutableStateOf(false) }
    var mesExpanded by remember { mutableStateOf(false) }
    val filtros = viewModel.filtros

    val titulo = remember(filtros.año, filtros.mes) {
        if (filtros.año != null && filtros.mes != null) {
            "${getMonthName(filtros.mes!!)} ${filtros.año}"
        } else if (filtros.año != null) {
            "Año ${filtros.año}"
        } else {
            "Registro"
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = titulo,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            IconButton(onClick = { showDayPicker = true }) {
                Icon(
                    Icons.Default.CalendarMonth,
                    contentDescription = "Días de descanso",
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    ExposedDropdownMenuBox(
                        expanded = añoExpanded,
                        onExpandedChange = { añoExpanded = it }
                    ) {
                        OutlinedTextField(
                            value = if (filtros.año == null) "Todo" else filtros.año.toString(),
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = añoExpanded) },
                            modifier = Modifier.menuAnchor(),
                            singleLine = true,
                            textStyle = LocalTextStyle.current.copy(fontSize = 14.sp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary, unfocusedBorderColor = MaterialTheme.colorScheme.surfaceVariant,
                                focusedTextColor = MaterialTheme.colorScheme.onSurface, unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                            )
                        )
                        ExposedDropdownMenu(
                            expanded = añoExpanded,
                            onDismissRequest = { añoExpanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Todo") },
                                onClick = { viewModel.setFiltroAño(null); añoExpanded = false }
                            )
                            years.forEach { y ->
                                DropdownMenuItem(
                                    text = { Text(y.toString()) },
                                    onClick = { viewModel.setFiltroAño(y); añoExpanded = false }
                                )
                            }
                        }
                    }
                }

                Box(modifier = Modifier.weight(1f)) {
                    ExposedDropdownMenuBox(
                        expanded = mesExpanded && filtros.año != null,
                        onExpandedChange = { if (filtros.año != null) mesExpanded = it }
                    ) {
                        OutlinedTextField(
                            value = if (filtros.mes == null) "Todo" else getMonthName(filtros.mes!!),
                            onValueChange = {},
                            readOnly = true,
                            enabled = filtros.año != null,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = mesExpanded) },
                            modifier = Modifier.menuAnchor(),
                            singleLine = true,
                            textStyle = LocalTextStyle.current.copy(fontSize = 14.sp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary, unfocusedBorderColor = MaterialTheme.colorScheme.surfaceVariant,
                                focusedTextColor = MaterialTheme.colorScheme.onSurface, unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                            )
                        )
                        ExposedDropdownMenu(
                            expanded = mesExpanded,
                            onDismissRequest = { mesExpanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Todo") },
                                onClick = { viewModel.setFiltroMes(null); mesExpanded = false }
                            )
                            (1..12).forEach { m ->
                                DropdownMenuItem(
                                    text = { Text(getMonthName(m)) },
                                    onClick = { viewModel.setFiltroMes(m); mesExpanded = false }
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (workoutsWithSets.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Sin entrenamientos aún",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 16.sp
                    )
                }
            }
        } else {
            LazyColumn {
                itemsIndexed(workoutsWithSets) { index, item ->
                    if (index > 0) {
                        val prev = workoutsWithSets[index - 1]
                        if (!mismoDia(item.workout.date, prev.workout.date)) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                HorizontalDivider(
                                    modifier = Modifier.fillMaxWidth(0.9f),
                                    thickness = 1.5.dp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f)
                                )
                            }
                        }
                    }
                    WorkoutCard(
                        workoutWithSets = item,
                        onDelete = { viewModel.deleteWorkout(item.workout) }
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }
                item { Spacer(modifier = Modifier.height(80.dp)) }
            }
        }
    }

    if (showDayPicker) {
        RestDayPickerDialog(onDismiss = { showDayPicker = false })
    }
}

@Composable
fun WorkoutCard(
    workoutWithSets: RegistroViewModel.WorkoutWithSets,
    onDelete: () -> Unit
) {
    val workout = workoutWithSets.workout
    val sets = workoutWithSets.sets

    data class EjercicioResumen(
        val nombre: String,
        val color: Color,
        val completados: Int,
        val total: Int
    )

    val ejerciciosResumen = sets
        .groupBy { it.exerciseName }
        .map { (nombre, series) ->
            val total = series.size
            val completados = series.count { it.completed }
            val color = when {
                completados == total -> GreenColor
                completados > 0 -> YellowColor
                else -> RedColor
            }
            EjercicioResumen(nombre, color, completados, total)
        }

    val duracion = duracionMinutos(workout.startTime, workout.endTime)

    Row(verticalAlignment = Alignment.Top) {

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(52.dp)
        ) {
            Text(
                text = diaSemana(workout.date),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
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
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 11.sp
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
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
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
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
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                if (ejerciciosResumen.isEmpty()) {
                    Text(
                        text = "Sin ejercicios",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 13.sp
                    )
                } else {
                    ejerciciosResumen.forEach { ej ->
                        Text(
                            text = "${ej.completados}/${ej.total} ${ej.nombre}",
                            color = ej.color,
                            fontSize = 13.sp,
                            fontWeight = if (ej.completados == ej.total) FontWeight.Medium else FontWeight.Normal
                        )
                    }
                }
            }
        }
    }
}

// --- Helpers de fecha ---

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

private fun mismoDia(a: Long, b: Long): Boolean {
    val cal = Calendar.getInstance()
    cal.timeInMillis = a
    val ya = cal.get(Calendar.YEAR); val ma = cal.get(Calendar.MONTH); val da = cal.get(Calendar.DAY_OF_MONTH)
    cal.timeInMillis = b
    return ya == cal.get(Calendar.YEAR) && ma == cal.get(Calendar.MONTH) && da == cal.get(Calendar.DAY_OF_MONTH)
}


