package com.example.gympulse.ui.registro

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.gympulse.data.ExerciseSetEntity
import com.example.gympulse.data.WorkoutEntity
import com.example.gympulse.data.WorkoutRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class RegistroViewModel(private val repository: WorkoutRepository) : ViewModel() {

    // Lista de workouts con sus series agrupadas
    data class WorkoutWithSets(
        val workout: WorkoutEntity,
        val sets: List<ExerciseSetEntity>
    )

    private val _workoutsWithSets = MutableStateFlow<List<WorkoutWithSets>>(emptyList())
    val workoutsWithSets: StateFlow<List<WorkoutWithSets>> = _workoutsWithSets

    init {
        loadWorkouts()
    }

    private fun loadWorkouts() {
        viewModelScope.launch {
            repository.getAllWorkouts().collect { workouts ->
                val result = workouts.map { workout ->
                    val sets = repository.getSetsForWorkout(workout.id).first()
                    WorkoutWithSets(workout, sets)
                }
                _workoutsWithSets.value = result
            }
        }
    }

    fun deleteWorkout(workout: WorkoutEntity) {
        viewModelScope.launch {
            repository.deleteAllSetsFromWorkout(workout.id)
            repository.deleteWorkout(workout)
        }
    }

    // Factory para crear el ViewModel sin Hilt
    class Factory(private val repository: WorkoutRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return RegistroViewModel(repository) as T
        }
    }
}