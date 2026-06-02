package com.example.gympulse.ui.rutinas

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.gympulse.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class CrearRutinaViewModel(
    private val repository: WorkoutRepository,
    private val routineId: Long? = null
) : ViewModel() {

    var nombre by mutableStateOf("")
    var notas by mutableStateOf("")

    data class SetUI(
        val id: Int,
        val reps: String = "",
        val weight: String = "",
        val weightUnit: String = "kg"
    )

    data class ExerciseUI(
        val exerciseName: String,
        val category: String,
        val sets: List<SetUI> = emptyList()
    )

    private val _ejercicios = MutableStateFlow<List<ExerciseUI>>(emptyList())
    val ejercicios: StateFlow<List<ExerciseUI>> = _ejercicios

    private var nextSetId = 0

    val allExercises: StateFlow<List<ExerciseEntity>> =
        repository.getAllExercises()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())

    init {
        if (routineId != null) {
            cargarRutina(routineId)
        }
    }

    private fun cargarRutina(routineId: Long) {
        viewModelScope.launch {
            val routine = repository.getRoutineById(routineId) ?: return@launch
            nombre = routine.name
            notas = routine.notes
            val exercises = repository.getExercisesForRoutineOnce(routineId)
            val exerciseIds = exercises.map { it.id }
            val allSets = repository.getSetsForExercises(exerciseIds)
            val setsByExercise = allSets.groupBy { it.routineExerciseId }

            val exerciseUIList = exercises.map { ex ->
                val sets = setsByExercise[ex.id] ?: emptyList()
                ExerciseUI(
                    exerciseName = ex.exerciseName,
                    category = ex.category,
                    sets = sets.map { s ->
                        SetUI(
                            id = nextSetId++,
                            reps = s.reps,
                            weight = s.weight,
                            weightUnit = s.weightUnit
                        )
                    }
                )
            }
            _ejercicios.value = exerciseUIList
        }
    }

    fun addExercise(exerciseName: String, category: String) {
        val current = _ejercicios.value.toMutableList()
        val yaExiste = current.any { it.exerciseName == exerciseName }
        if (!yaExiste) {
            val newSet = SetUI(id = nextSetId++)
            current.add(
                ExerciseUI(
                    exerciseName = exerciseName,
                    category = category,
                    sets = listOf(newSet)
                )
            )
            _ejercicios.value = current
        }
    }

    fun removeExercise(exerciseName: String) {
        _ejercicios.value = _ejercicios.value.filter { it.exerciseName != exerciseName }
    }

    fun addSet(exerciseName: String) {
        val newSet = SetUI(id = nextSetId++)
        _ejercicios.value = _ejercicios.value.map { ex ->
            if (ex.exerciseName == exerciseName) {
                ex.copy(sets = ex.sets + newSet)
            } else ex
        }
    }

    fun updateSet(setId: Int, reps: String? = null, weight: String? = null, weightUnit: String? = null) {
        _ejercicios.value = _ejercicios.value.map { ex ->
            ex.copy(sets = ex.sets.map { s ->
                if (s.id == setId) s.copy(
                    reps = reps ?: s.reps,
                    weight = weight ?: s.weight,
                    weightUnit = weightUnit ?: s.weightUnit
                ) else s
            })
        }
    }

    fun duplicateSet(setId: Int) {
        _ejercicios.value = _ejercicios.value.map { ex ->
            val setToDup = ex.sets.find { it.id == setId } ?: return@map ex
            val newSet = setToDup.copy(id = nextSetId++)
            val sets = ex.sets.toMutableList()
            val idx = sets.indexOfFirst { it.id == setId }
            sets.add(idx + 1, newSet)
            ex.copy(sets = sets)
        }
    }

    fun removeSet(setId: Int) {
        _ejercicios.value = _ejercicios.value.map { ex ->
            ex.copy(sets = ex.sets.filter { it.id != setId })
        }
    }

    fun isValid(): Boolean {
        if (nombre.isBlank() || _ejercicios.value.isEmpty()) return false
        for (ex in _ejercicios.value) {
            for (s in ex.sets) {
                if (s.reps.isBlank() || s.weight.isBlank()) return false
            }
        }
        return true
    }

    fun guardar(onDone: () -> Unit) {
        if (!isValid()) return
        viewModelScope.launch {
            if (routineId != null) {
                repository.updateRoutine(RoutineEntity(id = routineId, name = nombre, notes = notas))
                repository.deleteSetsForRoutine(routineId)
                repository.deleteExercisesFromRoutine(routineId)
            }
            val rid = routineId ?: repository.insertRoutine(RoutineEntity(name = nombre, notes = notas))
            var order = 0
            for (exUI in _ejercicios.value) {
                val exId = repository.insertRoutineExercise(
                    RoutineExerciseEntity(
                        routineId = rid,
                        exerciseName = exUI.exerciseName,
                        category = exUI.category,
                        order = order++
                    )
                )
                val sets = exUI.sets.mapIndexed { i, s ->
                    RoutineSetEntity(
                        routineExerciseId = exId,
                        setNumber = i + 1,
                        reps = s.reps,
                        weight = s.weight,
                        weightUnit = s.weightUnit
                    )
                }
                repository.insertRoutineSets(sets)
            }
            onDone()
        }
    }

    class Factory(
        private val repository: WorkoutRepository,
        private val routineId: Long? = null
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            CrearRutinaViewModel(repository, routineId) as T
    }
}
