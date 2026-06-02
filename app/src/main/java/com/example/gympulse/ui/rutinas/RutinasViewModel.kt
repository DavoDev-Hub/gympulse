package com.example.gympulse.ui.rutinas

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.gympulse.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class RutinasViewModel(private val repository: WorkoutRepository) : ViewModel() {

    data class SetDisplay(
        val reps: String,
        val weight: String,
        val weightUnit: String
    )

    data class ExerciseDisplay(
        val exerciseName: String,
        val category: String,
        val sets: List<SetDisplay>
    )

    data class RoutineWithFullData(
        val routine: RoutineEntity,
        val exercises: List<ExerciseDisplay>
    )

    private val _routines = MutableStateFlow<List<RoutineWithFullData>>(emptyList())
    val routines: StateFlow<List<RoutineWithFullData>> = _routines

    init {
        loadRoutines()
    }

    fun loadRoutines() {
        viewModelScope.launch {
            val routines = repository.getAllRoutines().first()
            val result = routines.map { routine ->
                val exercises = repository.getExercisesForRoutineOnce(routine.id)
                val exerciseIds = exercises.map { it.id }
                val allSets = if (exerciseIds.isNotEmpty()) {
                    repository.getSetsForExercises(exerciseIds)
                } else emptyList()
                val setsByExercise = allSets.groupBy { it.routineExerciseId }

                val exercisesDisplay = exercises.map { ex ->
                    val sets = setsByExercise[ex.id] ?: emptyList()
                    ExerciseDisplay(
                        exerciseName = ex.exerciseName,
                        category = ex.category,
                        sets = sets.map { s ->
                            SetDisplay(
                                reps = s.reps,
                                weight = s.weight,
                                weightUnit = s.weightUnit
                            )
                        }
                    )
                }
                RoutineWithFullData(routine, exercisesDisplay)
            }
            _routines.value = result
        }
    }

    fun duplicateRoutine(routineId: Long) {
        viewModelScope.launch {
            val routines = repository.getAllRoutines().first()
            val original = routines.find { it.id == routineId } ?: return@launch
            val newName = "${original.name} - copia"
            val newRoutineId = repository.insertRoutine(
                RoutineEntity(id = 0, name = newName, notes = original.notes)
            )
            val exercises = repository.getExercisesForRoutineOnce(routineId)
            val exerciseIds = exercises.map { it.id }
            val allSets = repository.getSetsForExercises(exerciseIds)
            val setsByExercise = allSets.groupBy { it.routineExerciseId }

            for (ex in exercises) {
                val newExId = repository.insertRoutineExercise(
                    ex.copy(id = 0, routineId = newRoutineId)
                )
                val sets = setsByExercise[ex.id] ?: emptyList()
                val newSets = sets.mapIndexed { i, s ->
                    s.copy(id = 0, routineExerciseId = newExId, setNumber = i + 1)
                }
                repository.insertRoutineSets(newSets)
            }
            loadRoutines()
        }
    }

    fun deleteRoutine(routine: RoutineEntity) {
        viewModelScope.launch {
            repository.deleteRoutine(routine)
            loadRoutines()
        }
    }

    class Factory(private val repository: WorkoutRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return RutinasViewModel(repository) as T
        }
    }
}
