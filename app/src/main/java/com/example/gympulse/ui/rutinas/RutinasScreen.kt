package com.example.gympulse.ui.rutinas

import androidx.compose.foundation.clickable
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.gympulse.data.ExerciseEntity
import com.example.gympulse.data.RoutineEntity
import com.example.gympulse.data.WorkoutRepository
import com.example.gympulse.ui.registro.ExerciseSelectorDialog
import com.example.gympulse.ui.theme.*

@Composable
fun RutinasScreen(repository: WorkoutRepository) {
    val viewModel: RutinasViewModel = viewModel(
        factory = RutinasViewModel.Factory(repository)
    )
    val routines     by viewModel.routines.collectAsStateWithLifecycle()
    val exercises    by viewModel.allExercises.collectAsStateWithLifecycle()
    val seleccionados by viewModel.ejerciciosSeleccionados.collectAsStateWithLifecycle()

    var showExerciseSelector by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            Text(
                text = "Rutinas",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 20.dp)
            )

            if (routines.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Sin rutinas aún", color = TextSecondary, fontSize = 16.sp)
                        Text("Pulsa + para crear una", color = TextSecondary, fontSize = 14.sp)
                    }
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(routines) { item ->
                        RutinaCard(
                            routineWithExercises = item,
                            onDelete = { viewModel.deleteRoutine(item.routine) }
                        )
                    }
                    item { Spacer(modifier = Modifier.height(80.dp)) }
                }
            }
        }

        // FAB
        FloatingActionButton(
            onClick = { viewModel.showCrearDialog = true },
            containerColor = CyanPrimary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Nueva rutina")
        }
    }

    // Dialog crear rutina
    if (viewModel.showCrearDialog) {
        CrearRutinaDialog(
            nombre        = viewModel.nombreRutina,
            onNombreChange = { viewModel.nombreRutina = it },
            seleccionados = seleccionados.map { it.exerciseName },
            onAddEjercicio = { showExerciseSelector = true },
            onRemoveEjercicio = { viewModel.quitarEjercicioDeNuevaRutina(it) },
            onGuardar     = { viewModel.guardarRutina {} },
            onDismiss     = { viewModel.showCrearDialog = false }
        )
    }

    // Selector de ejercicios
    if (showExerciseSelector) {
        ExerciseSelectorDialog(
            exercises = exercises,
            onSelect  = { exercise ->
                viewModel.agregarEjercicioANuevaRutina(exercise)
                showExerciseSelector = false
            },
            onDismiss = { showExerciseSelector = false }
        )
    }
}

@Composable
fun RutinaCard(
    routineWithExercises: RutinasViewModel.RoutineWithExercises,
    onDelete: () -> Unit
) {
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
                    text = routineWithExercises.routine.name.uppercase(),
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
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

            Spacer(modifier = Modifier.height(8.dp))

            if (routineWithExercises.exercises.isEmpty()) {
                Text("Sin ejercicios", color = TextSecondary, fontSize = 13.sp)
            } else {
                routineWithExercises.exercises.forEach { exercise ->
                    Text("• ${exercise.exerciseName}", color = TextSecondary, fontSize = 13.sp)
                }
            }
        }
    }
}

@Composable
fun CrearRutinaDialog(
    nombre: String,
    onNombreChange: (String) -> Unit,
    seleccionados: List<String>,
    onAddEjercicio: () -> Unit,
    onRemoveEjercicio: (String) -> Unit,
    onGuardar: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.75f),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceDark)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    "Nueva Rutina",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )

                OutlinedTextField(
                    value = nombre,
                    onValueChange = onNombreChange,
                    placeholder = { Text("Nombre de la rutina", color = TextSecondary) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = com.example.gympulse.ui.registro.outlinedTextFieldColors()
                )

                Text("Ejercicios", fontWeight = FontWeight.SemiBold, color = TextSecondary)

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(seleccionados) { nombre ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(nombre, fontSize = 14.sp)
                            IconButton(
                                onClick = { onRemoveEjercicio(nombre) },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = null,
                                    tint = TextSecondary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                    item {
                        TextButton(onClick = onAddEjercicio) {
                            Icon(Icons.Default.Add, contentDescription = null, tint = CyanPrimary)
                            Text("Agregar ejercicio", color = CyanPrimary)
                        }
                    }
                }

                Button(
                    onClick = onGuardar,
                    enabled = nombre.isNotBlank() && seleccionados.isNotEmpty(),
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary)
                ) {
                    Text("Guardar rutina", fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}