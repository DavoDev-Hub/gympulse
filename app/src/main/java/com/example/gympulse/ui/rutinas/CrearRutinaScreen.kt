package com.example.gympulse.ui.rutinas

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.gympulse.data.WorkoutRepository
import com.example.gympulse.ui.registro.ExerciseSelectorDialog
import com.example.gympulse.ui.registro.outlinedTextFieldColors
import com.example.gympulse.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CrearRutinaScreen(
    repository: WorkoutRepository,
    routineId: Long? = null,
    onBack: () -> Unit
) {
    val viewModel: CrearRutinaViewModel = viewModel(
        factory = CrearRutinaViewModel.Factory(repository, routineId)
    )

    val ejercicios by viewModel.ejercicios.collectAsStateWithLifecycle()
    val allExercises by viewModel.allExercises.collectAsStateWithLifecycle()
    var showExerciseSelector by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (routineId != null) "Editar Rutina" else "Nueva Rutina",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    TextButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Text("Atrás", color = MaterialTheme.colorScheme.primary)
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
            item {
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = viewModel.nombre,
                    onValueChange = { viewModel.nombre = it },
                    placeholder = { Text("Nombre de la rutina", color = MaterialTheme.colorScheme.onSurfaceVariant) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = outlinedTextFieldColors()
                )
            }

            item {
                OutlinedTextField(
                    value = viewModel.notas,
                    onValueChange = { viewModel.notas = it },
                    placeholder = { Text("Notas (opcional)", color = MaterialTheme.colorScheme.onSurfaceVariant) },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    colors = outlinedTextFieldColors()
                )
            }

            item { HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant, thickness = 1.dp) }

            itemsIndexed(ejercicios) { _, ejercicio ->
                EjercicioConSets(
                    ejercicio = ejercicio,
                    onAddSet = { viewModel.addSet(ejercicio.exerciseName) },
                    onUpdateSet = { setId, reps, weight, weightUnit ->
                        viewModel.updateSet(setId, reps, weight, weightUnit)
                    },
                    onDuplicateSet = { viewModel.duplicateSet(it) },
                    onRemoveSet = { viewModel.removeSet(it) },
                    onRemoveEjercicio = { viewModel.removeExercise(ejercicio.exerciseName) }
                )
            }

            item {
                Button(
                    onClick = { showExerciseSelector = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Agregar ejercicio", fontWeight = FontWeight.SemiBold)
                }
            }

            item {
                Button(
                    onClick = { viewModel.guardar(onBack) },
                    enabled = viewModel.isValid(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (viewModel.isValid()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                        disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Icon(Icons.Default.Save, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Guardar rutina", fontWeight = FontWeight.SemiBold)
                }
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }

    if (showExerciseSelector) {
        ExerciseSelectorDialog(
            exercises = allExercises,
            onSelect = { exercise ->
                viewModel.addExercise(exercise.name, exercise.category)
                showExerciseSelector = false
            },
            onDismiss = { showExerciseSelector = false }
        )
    }
}

@Composable
private fun EjercicioConSets(
    ejercicio: CrearRutinaViewModel.ExerciseUI,
    onAddSet: () -> Unit,
    onUpdateSet: (Int, String?, String?, String?) -> Unit,
    onDuplicateSet: (Int) -> Unit,
    onRemoveSet: (Int) -> Unit,
    onRemoveEjercicio: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = ejercicio.exerciseName,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.primary
                )
                IconButton(onClick = onRemoveEjercicio, modifier = Modifier.size(24.dp)) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Quitar ejercicio",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            ejercicio.sets.forEach { set ->
                SerieRowEditable(
                    set = set,
                    onUpdate = { weightVal, repsVal, unitVal ->
                        onUpdateSet(set.id, repsVal, weightVal, unitVal)
                    },
                    onDuplicate = { onDuplicateSet(set.id) },
                    onRemove = { onRemoveSet(set.id) }
                )
                Spacer(modifier = Modifier.height(6.dp))
            }

            TextButton(onClick = onAddSet) {
                Icon(Icons.Default.Add, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Text("Agregar serie", color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

@Composable
private fun SerieRowEditable(
    set: CrearRutinaViewModel.SetUI,
    onUpdate: (String?, String?, String?) -> Unit,
    onDuplicate: () -> Unit,
    onRemove: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = set.weight,
            onValueChange = { onUpdate(it, null, null) },
            placeholder = { Text("Peso", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp) },
            modifier = Modifier.weight(1f),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            colors = outlinedTextFieldColors(),
            textStyle = LocalTextStyle.current.copy(fontSize = 13.sp)
        )

        Spacer(modifier = Modifier.width(4.dp))

        var expanded by remember { mutableStateOf(false) }
        Box {
            TextButton(onClick = { expanded = true }) {
                Text(set.weightUnit, color = MaterialTheme.colorScheme.primary, fontSize = 13.sp)
                Icon(
                    Icons.Default.ArrowDropDown,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
            }
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                DropdownMenuItem(
                    text = { Text("kg") },
                    onClick = {
                        onUpdate(null, null, "kg")
                        expanded = false
                    }
                )
                DropdownMenuItem(
                    text = { Text("lb") },
                    onClick = {
                        onUpdate(null, null, "lb")
                        expanded = false
                    }
                )
            }
        }

        Spacer(modifier = Modifier.width(4.dp))

        OutlinedTextField(
            value = set.reps,
            onValueChange = { onUpdate(null, it, null) },
            placeholder = { Text("Reps", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp) },
            modifier = Modifier.weight(1f),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            colors = outlinedTextFieldColors(),
            textStyle = LocalTextStyle.current.copy(fontSize = 13.sp)
        )

        Box {
            IconButton(onClick = { showMenu = true }, modifier = Modifier.size(32.dp)) {
                Icon(
                    Icons.Default.MoreVert,
                    contentDescription = "Opciones",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }
            DropdownMenu(
                expanded = showMenu,
                onDismissRequest = { showMenu = false }
            ) {
                DropdownMenuItem(
                    text = { Text("Duplicar") },
                    onClick = {
                        onDuplicate()
                        showMenu = false
                    },
                    leadingIcon = {
                        Icon(
                            Icons.Default.ContentCopy,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                )
                DropdownMenuItem(
                    text = { Text("Eliminar", color = MaterialTheme.colorScheme.error) },
                    onClick = {
                        onRemove()
                        showMenu = false
                    },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                )
            }
        }
    }
}
