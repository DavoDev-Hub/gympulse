package com.example.gympulse.ui.registro

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.gympulse.data.ExerciseEntity
import com.example.gympulse.data.WorkoutRepository
import com.example.gympulse.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NuevoWorkoutScreen(
    repository: WorkoutRepository,
    onTerminar: () -> Unit
) {
    val viewModel: NuevoWorkoutViewModel = viewModel(
        factory = NuevoWorkoutViewModel.Factory(repository)
    )

    val sets      by viewModel.sets.collectAsStateWithLifecycle()
    val exercises by viewModel.exercises.collectAsStateWithLifecycle()

    var showExerciseSelector by remember { mutableStateOf(false) }

    // Agrupar series por ejercicio para mostrarlas
    val groupedSets = sets.groupBy { it.exerciseName }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "may 8", fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    TextButton(onClick = onTerminar) {
                        Icon(
                            Icons.Default.KeyboardArrowDown,
                            contentDescription = null,
                            tint = CyanPrimary
                        )
                        Text("Terminar", color = CyanPrimary)
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
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            // --- Formulario superior ---
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = viewModel.nombre,
                        onValueChange = { viewModel.nombre = it },
                        placeholder = { Text("Nombre", color = TextSecondary) },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        colors = outlinedTextFieldColors()
                    )
                    OutlinedTextField(
                        value = "",
                        onValueChange = {},
                        placeholder = { Text("Mi Peso...", color = TextSecondary) },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        colors = outlinedTextFieldColors()
                    )
                }
            }

            item {
                OutlinedTextField(
                    value = viewModel.notas,
                    onValueChange = { viewModel.notas = it },
                    placeholder = { Text("Notas", color = TextSecondary) },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    colors = outlinedTextFieldColors()
                )
            }

            item { HorizontalDivider(color = CardDark, thickness = 1.dp) }

            // --- Series agrupadas por ejercicio ---
            groupedSets.forEach { (exerciseName, seriesDelEjercicio) ->
                item {
                    Text(
                        text = exerciseName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = CyanPrimary,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                items(seriesDelEjercicio) { set ->
                    SerieRow(
                        set       = set,
                        numero    = seriesDelEjercicio.indexOf(set) + 1,
                        onReps    = { viewModel.updateSet(set.id, reps = it) },
                        onWeight  = { viewModel.updateSet(set.id, weight = it) },
                        onRemove  = { viewModel.removeSet(set.id) }
                    )
                }

                item {
                    TextButton(
                        onClick = {
                            val cat = seriesDelEjercicio.first().category
                            viewModel.addSet(exerciseName, cat)
                        }
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = CyanPrimary)
                        Text("Agregar serie", color = CyanPrimary)
                    }
                }
            }

            // --- Botón añadir ejercicio ---
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = { showExerciseSelector = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Añadir ejercicio", fontWeight = FontWeight.SemiBold)
                }
            }

            // --- Botón guardar ---
            item {
                Button(
                    onClick = { viewModel.guardarWorkout(onTerminar) },
                    enabled = sets.isNotEmpty(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Icon(Icons.Default.Save, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Guardar entrenamiento", fontWeight = FontWeight.SemiBold)
                }
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }

    // --- Dialog selector de ejercicios ---
    if (showExerciseSelector) {
        ExerciseSelectorDialog(
            exercises = exercises,
            onSelect  = { exercise ->
                viewModel.addSet(exercise.name, exercise.category)
                showExerciseSelector = false
            },
            onDismiss = { showExerciseSelector = false }
        )
    }
}

@Composable
fun SerieRow(
    set: NuevoWorkoutViewModel.SetUI,
    numero: Int,
    onReps: (String) -> Unit,
    onWeight: (String) -> Unit,
    onRemove: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        // Número de serie
        Text(
            text = "$numero",
            color = TextSecondary,
            fontSize = 14.sp,
            modifier = Modifier.width(20.dp)
        )

        OutlinedTextField(
            value = set.reps,
            onValueChange = onReps,
            placeholder = { Text("Reps", color = TextSecondary, fontSize = 12.sp) },
            modifier = Modifier.weight(1f),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            colors = outlinedTextFieldColors()
        )

        OutlinedTextField(
            value = set.weight,
            onValueChange = onWeight,
            placeholder = { Text("Kg", color = TextSecondary, fontSize = 12.sp) },
            modifier = Modifier.weight(1f),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            colors = outlinedTextFieldColors()
        )

        IconButton(onClick = onRemove, modifier = Modifier.size(32.dp)) {
            Icon(
                Icons.Default.Close,
                contentDescription = "Eliminar serie",
                tint = TextSecondary,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
fun ExerciseSelectorDialog(
    exercises: List<ExerciseEntity>,
    onSelect: (ExerciseEntity) -> Unit,
    onDismiss: () -> Unit
) {
    val categorias = exercises.groupBy { it.category }.keys.sorted()
    var categoriaSeleccionada by remember { mutableStateOf<String?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.8f),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceDark)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {

                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = {
                        if (categoriaSeleccionada != null) categoriaSeleccionada = null
                        else onDismiss()
                    }) {
                        Icon(
                            if (categoriaSeleccionada != null) Icons.Default.ArrowBack
                            else Icons.Default.Close,
                            contentDescription = null,
                            tint = CyanPrimary
                        )
                    }
                    Text(
                        text = categoriaSeleccionada ?: "Seleccionar ejercicio",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 18.sp
                    )
                }

                HorizontalDivider(color = CardDark)

                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    if (categoriaSeleccionada == null) {
                        // Mostrar categorías
                        items(categorias) { categoria ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { categoriaSeleccionada = categoria }
                                    .padding(horizontal = 24.dp, vertical = 16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = categoria, fontSize = 16.sp)
                                Icon(
                                    Icons.Default.ChevronRight,
                                    contentDescription = null,
                                    tint = TextSecondary
                                )
                            }
                            HorizontalDivider(color = CardDark, thickness = 0.5.dp)
                        }
                    } else {
                        // Mostrar ejercicios de la categoría
                        val ejerciciosFiltrados = exercises.filter {
                            it.category == categoriaSeleccionada
                        }
                        items(ejerciciosFiltrados) { exercise ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onSelect(exercise) }
                                    .padding(horizontal = 24.dp, vertical = 16.dp)
                            ) {
                                Text(text = exercise.name, fontSize = 16.sp)
                            }
                            HorizontalDivider(color = CardDark, thickness = 0.5.dp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun outlinedTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor   = CyanPrimary,
    unfocusedBorderColor = CardDark,
    focusedTextColor     = TextPrimary,
    unfocusedTextColor   = TextPrimary,
    cursorColor          = CyanPrimary
)