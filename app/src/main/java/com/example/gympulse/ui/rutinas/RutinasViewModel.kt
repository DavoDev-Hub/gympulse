package com.example.gympulse.ui.rutinas

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.gympulse.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class RutinasViewModel(private val repository: WorkoutRepository) : ViewModel() {

    data class RoutineWithExercises(
        val routine: RoutineEntity,
        val exercises: List<RoutineExerciseEntity>
    )

    private val _routines = MutableStateFlow<List<RoutineWithExercises>>(emptyList())
    val routines: StateFlow<List<RoutineWithExercises>> = _routines

    val allExercises: StateFlow<List<ExerciseEntity>> =
        repository.getAllExercises()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())

    // Estado formulario nueva rutina
    var nombreRutina by mutableStateOf("")
    var showCrearDialog by mutableStateOf(false)

    // Ejercicios seleccionados al crear rutina
    private val _ejerciciosSeleccionados = MutableStateFlow<List<RoutineExerciseEntity>>(emptyList())
    val ejerciciosSeleccionados: StateFlow<List<RoutineExerciseEntity>> = _ejerciciosSeleccionados

    init {
        loadRoutines()
    }

    private fun loadRoutines() {
        viewModelScope.launch {
            repository.getAllRoutines().collect { routines ->
                val result = routines.map { routine ->
                    val exercises = repository.getExercisesForRoutine(routine.id).first()
                    RoutineWithExercises(routine, exercises)
                }
                _routines.value = result
            }
        }
    }

    fun agregarEjercicioANuevaRutina(exercise: ExerciseEntity) {
        val current = _ejerciciosSeleccionados.value.toMutableList()
        val yaExiste = current.any { it.exerciseName == exercise.name }
        if (!yaExiste) {
            current.add(
                RoutineExerciseEntity(
                    routineId    = 0,
                    exerciseName = exercise.name,
                    category     = exercise.category,
                    order        = current.size
                )
            )
            _ejerciciosSeleccionados.value = current
        }
    }

    fun quitarEjercicioDeNuevaRutina(exerciseName: String) {
        _ejerciciosSeleccionados.value =
            _ejerciciosSeleccionados.value.filter { it.exerciseName != exerciseName }
    }

    fun guardarRutina(onDone: () -> Unit) {
        if (nombreRutina.isBlank()) return
        viewModelScope.launch {
            val routineId = repository.insertRoutine(RoutineEntity(name = nombreRutina))
            val exercises = _ejerciciosSeleccionados.value.mapIndexed { index, e ->
                e.copy(routineId = routineId, order = index)
            }
            repository.insertRoutineExercises(exercises)
            // Reset
            nombreRutina = ""
            _ejerciciosSeleccionados.value = emptyList()
            showCrearDialog = false
            onDone()
        }
    }

    fun deleteRoutine(routine: RoutineEntity) {
        viewModelScope.launch { repository.deleteRoutine(routine) }
    }

    class Factory(private val repository: WorkoutRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return RutinasViewModel(repository) as T
        }
    }
}