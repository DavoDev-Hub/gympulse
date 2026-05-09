package com.example.gympulse.ui.registro

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.gympulse.data.ExerciseEntity
import com.example.gympulse.data.ExerciseSetEntity
import com.example.gympulse.data.WorkoutEntity
import com.example.gympulse.data.WorkoutRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class NuevoWorkoutViewModel(private val repository: WorkoutRepository) : ViewModel() {

    // --- Estado del formulario ---
    var nombre by mutableStateOf("")
    var notas  by mutableStateOf("")
    val startTime = System.currentTimeMillis()

    // Ejercicios agregados al workout actual
    // Mapa: exerciseName -> lista de series
    data class SetUI(
        val id: Int,                  // id local temporal
        val exerciseName: String,
        val category: String,
        val reps: String = "",
        val weight: String = ""
    )

    private val _sets = MutableStateFlow<List<SetUI>>(emptyList())
    val sets: StateFlow<List<SetUI>> = _sets

    // Ejercicios disponibles para seleccionar
    private val _exercises = MutableStateFlow<List<ExerciseEntity>>(emptyList())
    val exercises: StateFlow<List<ExerciseEntity>> = _exercises

    private var nextSetId = 0

    init {
        viewModelScope.launch {
            repository.getAllExercises().collect { _exercises.value = it }
        }
    }

    // Agregar una serie nueva para un ejercicio
    fun addSet(exerciseName: String, category: String) {
        val current = _sets.value.toMutableList()
        current.add(SetUI(id = nextSetId++, exerciseName = exerciseName, category = category))
        _sets.value = current
    }

    // Actualizar reps o weight de una serie
    fun updateSet(id: Int, reps: String? = null, weight: String? = null) {
        _sets.value = _sets.value.map { set ->
            if (set.id == id) set.copy(
                reps   = reps   ?: set.reps,
                weight = weight ?: set.weight
            ) else set
        }
    }

    fun removeSet(id: Int) {
        _sets.value = _sets.value.filter { it.id != id }
    }

    // Guardar workout completo
    fun guardarWorkout(onDone: () -> Unit) {
        viewModelScope.launch {
            val workoutId = repository.insertWorkout(
                WorkoutEntity(
                    name      = nombre,
                    notes     = notas,
                    startTime = startTime,
                    endTime   = System.currentTimeMillis(),
                    date      = startTime
                )
            )
            val entities = _sets.value.mapIndexed { index, set ->
                ExerciseSetEntity(
                    workoutId    = workoutId,
                    exerciseId   = 0L,
                    exerciseName = set.exerciseName,
                    setNumber    = index + 1,
                    reps         = set.reps.toIntOrNull(),
                    weight       = set.weight.toFloatOrNull()
                )
            }
            repository.insertSets(entities)
            onDone()
        }
    }

    class Factory(private val repository: WorkoutRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return NuevoWorkoutViewModel(repository) as T
        }
    }
}