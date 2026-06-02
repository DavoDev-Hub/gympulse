package com.example.gympulse.ui.registro

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.gympulse.data.ExerciseEntity
import com.example.gympulse.data.ExerciseSetEntity
import com.example.gympulse.data.WorkoutEntity
import com.example.gympulse.data.WorkoutRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class NuevoWorkoutViewModel(
    private val repository: WorkoutRepository,
    private val routineId: Long? = null        // ← agregado
) : ViewModel() {

    var nombre       by mutableStateOf("")
    var notas        by mutableStateOf("")
    var pesocorporal by mutableStateOf("")
    val startTime = System.currentTimeMillis()

    data class SetUI(
        val id: Int,
        val exerciseName: String,
        val category: String,
        val reps: String = "",
        val weight: String = ""
    )

    private val _sets = MutableStateFlow<List<SetUI>>(emptyList())
    val sets: StateFlow<List<SetUI>> = _sets

    private val _exercises = MutableStateFlow<List<ExerciseEntity>>(emptyList())
    val exercises: StateFlow<List<ExerciseEntity>> = _exercises

    private var nextSetId = 0

    init {
        viewModelScope.launch {
            repository.getAllExercises().collect { _exercises.value = it }
        }
        // Si viene de una rutina, pre-cargar sus ejercicios
        if (routineId != null) {
            cargarEjerciciosDeRutina(routineId)
        }
    }

    private fun cargarEjerciciosDeRutina(routineId: Long) {
        viewModelScope.launch {
            repository.getExercisesForRoutine(routineId).first().forEach { exercise ->
                addSet(exercise.exerciseName, exercise.category)
            }
        }
    }

    fun addSet(exerciseName: String, category: String) {
        val current = _sets.value.toMutableList()
        current.add(SetUI(id = nextSetId++, exerciseName = exerciseName, category = category))
        _sets.value = current
    }

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

    fun guardarWorkout(onDone: () -> Unit) {
        viewModelScope.launch {
            val workoutId = repository.insertWorkout(
                WorkoutEntity(
                    name       = nombre,
                    notes      = notas,
                    startTime  = startTime,
                    endTime    = System.currentTimeMillis(),
                    date       = startTime,
                    bodyWeight = pesocorporal.toFloatOrNull()
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

    class Factory(
        private val repository: WorkoutRepository,
        private val routineId: Long? = null        // ← agregado
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return NuevoWorkoutViewModel(repository, routineId) as T  // ← agregado
        }
    }
}