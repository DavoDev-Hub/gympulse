package com.example.gympulse.ui.rutinas

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.gympulse.data.*
import com.example.gympulse.ui.theme.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IniciarRutinaScreen(
    repository: WorkoutRepository,
    routineId: Long,
    onBack: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var ejercicios by remember { mutableStateOf<List<EjercicioEnCurso>>(emptyList()) }
    var nombreRutina by remember { mutableStateOf("") }
    var saved by remember { mutableStateOf(false) }
    var showTimer by remember { mutableStateOf(false) }
    var showInfo by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(routineId) {
        val routine = repository.getRoutineById(routineId) ?: return@LaunchedEffect
        nombreRutina = routine.name
        val exercises = repository.getExercisesForRoutineOnce(routineId)
        val exerciseIds = exercises.map { it.id }
        val allSets = if (exerciseIds.isNotEmpty()) repository.getSetsForExercises(exerciseIds) else emptyList()
        val setsByExercise = allSets.groupBy { it.routineExerciseId }

        ejercicios = exercises.map { ex ->
            val sets = setsByExercise[ex.id] ?: emptyList()
            EjercicioEnCurso(
                exerciseName = ex.exerciseName,
                category = ex.category,
                sets = sets.map { s ->
                    SetEnCurso(
                        reps = s.reps,
                        weight = s.weight,
                        weightUnit = s.weightUnit,
                        completado = false
                    )
                },
                horaInicio = "",
                horaFinal = ""
            )
        }
    }

    fun algunaCompletada(): Boolean {
        return ejercicios.any { ex -> ex.sets.any { it.completado } }
    }

    fun puedeFinalizar(): Boolean {
        if (saved) return false
        if (!algunaCompletada()) return false
        return ejercicios.all { ex ->
            if (ex.sets.any { it.completado }) {
                ex.horaInicio.isNotBlank() && ex.horaFinal.isNotBlank()
            } else true
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(nombreRutina, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    TextButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Text("Atrás", color = MaterialTheme.colorScheme.primary)
                    }
                },
                actions = {
                    IconButton(onClick = { showTimer = true }) {
                        Icon(Icons.Default.Timer, contentDescription = "Temporizador", tint = MaterialTheme.colorScheme.primary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            itemsIndexed(ejercicios) { exIdx, ejercicio ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = ejercicio.exerciseName,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.primary
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            TimeField(
                                label = "Inicio",
                                value = ejercicio.horaInicio,
                                onSelected = { h, m ->
                                    ejercicios = ejercicios.toMutableList().also { list ->
                                        list[exIdx] = list[exIdx].copy(horaInicio = "%02d:%02d".format(h, m))
                                    }
                                }
                            )
                            Text("a", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
                            TimeField(
                                label = "Final",
                                value = ejercicio.horaFinal,
                                onSelected = { h, m ->
                                    val nueva = "%02d:%02d".format(h, m)
                                    val ok = ejercicio.horaInicio.isBlank() || nueva >= ejercicio.horaInicio
                                    if (ok) {
                                        ejercicios = ejercicios.toMutableList().also { list ->
                                            list[exIdx] = list[exIdx].copy(horaFinal = nueva)
                                        }
                                    } else {
                                        scope.launch {
                                            snackbarHostState.showSnackbar(
                                                message = "La hora final no puede ser menor a la hora de inicio",
                                                duration = SnackbarDuration.Short
                                            )
                                        }
                                    }
                                }
                            )
                            IconButton(
                                onClick = { showInfo = true },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    Icons.Default.Info,
                                    contentDescription = "Información",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        if (ejercicio.horaInicio.isNotBlank() && ejercicio.horaFinal.isNotBlank()) {
                            val diff = minutosEntre(ejercicio.horaInicio, ejercicio.horaFinal)
                            if (diff >= 0) {
                                Text(
                                    text = "Duración: $diff min",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 12.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        ejercicio.sets.forEachIndexed { setIdx, set ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                            ) {
                                Checkbox(
                                    checked = set.completado,
                                    onCheckedChange = { checked ->
                                        ejercicios = ejercicios.toMutableList().also { list ->
                                            val sets = list[exIdx].sets.toMutableList()
                                            sets[setIdx] = sets[setIdx].copy(completado = checked)
                                            list[exIdx] = list[exIdx].copy(sets = sets)
                                        }
                                    },
                                    colors = CheckboxDefaults.colors(
                                        checkedColor = MaterialTheme.colorScheme.primary,
                                        uncheckedColor = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                )
                                Text(
                                    text = "Serie ${setIdx + 1}: ${set.weight} ${set.weightUnit} x ${set.reps} reps",
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontSize = 14.sp,
                                    modifier = Modifier.padding(start = 4.dp)
                                )
                            }
                        }

                        if (ejercicio.sets.all { it.completado }) {
                            Text(
                                text = "Completado",
                                color = Color(0xFF4CAF50),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            item {
                Button(
                    onClick = {
                        guardarRutina(repository, nombreRutina, ejercicios, scope, onBack)
                        saved = true
                    },
                    enabled = puedeFinalizar(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Icon(Icons.Default.Stop, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Finalizar rutina", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                }
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }

    if (showTimer) {
        TimerDialog(onDismiss = { showTimer = false })
    }

    if (showInfo) {
        InfoDialog(onDismiss = { showInfo = false })
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimeField(
    label: String,
    value: String,
    onSelected: (Int, Int) -> Unit
) {
    var showPicker by remember { mutableStateOf(false) }

    Box(modifier = Modifier.width(100.dp)) {
        OutlinedTextField(
            value = value,
            onValueChange = {},
            placeholder = { Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp) },
            modifier = Modifier.fillMaxWidth(),
            readOnly = true,
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.surfaceVariant,
                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                unfocusedTextColor = MaterialTheme.colorScheme.onSurface
            ),
            textStyle = LocalTextStyle.current.copy(fontSize = 13.sp, textAlign = TextAlign.Center)
        )
        Box(
            modifier = Modifier
                .matchParentSize()
                .clickable { showPicker = true }
        )
    }

    if (showPicker) {
        TimePickerDialog(
            initialHour = value.split(":").getOrNull(0)?.toIntOrNull() ?: 0,
            initialMinute = value.split(":").getOrNull(1)?.toIntOrNull() ?: 0,
            onConfirm = { h, m ->
                onSelected(h, m)
                showPicker = false
            },
            onDismiss = { showPicker = false }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimePickerDialog(
    initialHour: Int,
    initialMinute: Int,
    onConfirm: (Int, Int) -> Unit,
    onDismiss: () -> Unit
) {
    val state = rememberTimePickerState(
        initialHour = initialHour,
        initialMinute = initialMinute,
        is24Hour = true
    )

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Seleccionar hora",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                Spacer(modifier = Modifier.height(16.dp))
                TimePicker(state = state)
                Spacer(modifier = Modifier.height(24.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancelar", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Button(
                        onClick = {
                            onConfirm(state.hour, state.minute)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text("Aceptar")
                    }
                }
            }
        }
    }
}

@Composable
private fun InfoDialog(onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "¿Para qué sirve?",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Registra la hora de inicio y la hora final que te tomó completar cada ejercicio. " +
                            "La app calculará automáticamente la duración en minutos y la sumará al tiempo " +
                            "total de tu entrenamiento.\n\n" +
                            "Toca cada campo para abrir el selector de hora. " +
                            "La hora final no puede ser menor a la hora de inicio.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(20.dp))
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("Entendido")
                }
            }
        }
    }
}

private data class SetEnCurso(
    val reps: String,
    val weight: String,
    val weightUnit: String,
    val completado: Boolean
)

private data class EjercicioEnCurso(
    val exerciseName: String,
    val category: String,
    val sets: List<SetEnCurso>,
    val horaInicio: String,
    val horaFinal: String
)

private fun minutosEntre(inicio: String, final: String): Int {
    val partsI = inicio.split(":").mapNotNull { it.toIntOrNull() }
    val partsF = final.split(":").mapNotNull { it.toIntOrNull() }
    if (partsI.size != 2 || partsF.size != 2) return -1
    return (partsF[0] * 60 + partsF[1]) - (partsI[0] * 60 + partsI[1])
}

private fun guardarRutina(
    repository: WorkoutRepository,
    nombreRutina: String,
    ejercicios: List<EjercicioEnCurso>,
    scope: CoroutineScope,
    onDone: () -> Unit
) {
    scope.launch {
        val startTime = System.currentTimeMillis()

        val totalMinutos = ejercicios.sumOf { ex ->
            if (ex.horaInicio.isNotBlank() && ex.horaFinal.isNotBlank()) {
                maxOf(minutosEntre(ex.horaInicio, ex.horaFinal), 0)
            } else 0
        }
        val endTime = startTime + (totalMinutos * 60000L)

        val workoutId = repository.insertWorkout(
            WorkoutEntity(
                name = nombreRutina,
                startTime = startTime,
                endTime = endTime,
                date = startTime
            )
        )
        for (ex in ejercicios) {
            val tiempo = if (ex.horaInicio.isNotBlank() && ex.horaFinal.isNotBlank()) {
                maxOf(minutosEntre(ex.horaInicio, ex.horaFinal), 0)
            } else null
            ex.sets.forEachIndexed { setIdx, set ->
                repository.insertSet(
                    ExerciseSetEntity(
                        workoutId = workoutId,
                        exerciseId = 0L,
                        exerciseName = ex.exerciseName,
                        setNumber = setIdx + 1,
                        reps = set.reps.toIntOrNull(),
                        weight = set.weight.toFloatOrNull(),
                        completed = set.completado,
                        timeTakenMinutes = tiempo
                    )
                )
            }
        }
        onDone()
    }
}

@Composable
fun TimerDialog(onDismiss: () -> Unit) {
    var seconds by remember { mutableStateOf(0) }
    var isRunning by remember { mutableStateOf(false) }

    LaunchedEffect(isRunning) {
        if (isRunning) {
            while (true) {
                delay(1000)
                seconds++
            }
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Temporizador",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )

                Text(
                    text = "Gestiona tus descansos entre series",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = formatTimer(seconds),
                    fontSize = 56.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Button(
                        onClick = { isRunning = !isRunning },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isRunning) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                        ),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(16.dp)
                    ) {
                        Icon(
                            if (isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (isRunning) "Pausar" else "Iniciar",
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    OutlinedButton(
                        onClick = { seconds = 0; isRunning = false },
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(16.dp)
                    ) {
                        Icon(
                            Icons.Default.Refresh,
                            contentDescription = "Reiniciar",
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }

                TextButton(onClick = onDismiss) {
                    Text("Cerrar", color = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}

private fun formatTimer(totalSeconds: Int): String {
    val mins = totalSeconds / 60
    val secs = totalSeconds % 60
    return "%02d:%02d".format(mins, secs)
}
