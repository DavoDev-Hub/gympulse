package com.example.gympulse.ui.registro
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.gympulse.data.ExerciseSetEntity
import com.example.gympulse.data.WorkoutEntity
import com.example.gympulse.data.WorkoutRepository
import com.example.gympulse.ui.estadisticas.Filtros
import com.example.gympulse.ui.estadisticas.extraerAño
import com.example.gympulse.ui.estadisticas.extraerMes
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar

class RegistroViewModel(private val repository: WorkoutRepository) : ViewModel() {

    data class WorkoutWithSets(
        val workout: WorkoutEntity,
        val sets: List<ExerciseSetEntity>
    )

    private val _workoutsWithSets = MutableStateFlow<List<WorkoutWithSets>>(emptyList())
    val workoutsWithSets: StateFlow<List<WorkoutWithSets>> = _workoutsWithSets

    var filtros by mutableStateOf(Filtros())

    private val _yearsAvailable = MutableStateFlow<List<Int>>(emptyList())
    val yearsAvailable: StateFlow<List<Int>> = _yearsAvailable

    private var allWorkouts: List<WorkoutEntity> = emptyList()

    init {
        val now = Calendar.getInstance()
        filtros = Filtros(año = now.get(Calendar.YEAR), mes = now.get(Calendar.MONTH) + 1)
        loadWorkouts()
    }

    private fun loadWorkouts() {
        viewModelScope.launch {
            repository.getAllWorkouts().collect { workouts ->
                allWorkouts = workouts
                val years = workouts.map { extraerAño(it.date) }.toSet()
                val currentYear = Calendar.getInstance().get(Calendar.YEAR)
                val minYear = minOf(years.minOrNull() ?: currentYear, currentYear)
                _yearsAvailable.value = (minYear..currentYear).toList().reversed()
                aplicarFiltros()
            }
        }
    }

    fun setFiltroAño(año: Int?) {
        filtros = filtros.copy(año = año, mes = null)
        aplicarFiltros()
    }

    fun setFiltroMes(mes: Int?) {
        filtros = filtros.copy(mes = mes)
        aplicarFiltros()
    }

    private fun aplicarFiltros() {
        viewModelScope.launch {
            var filtered = allWorkouts
            val a = filtros.año
            val m = filtros.mes
            if (a != null) filtered = filtered.filter { extraerAño(it.date) == a }
            if (m != null) filtered = filtered.filter { extraerMes(it.date) == m }

            val result = filtered.map { workout ->
                val sets = repository.getSetsForWorkout(workout.id).first()
                WorkoutWithSets(workout, sets)
            }
            _workoutsWithSets.value = result
        }
    }

    fun deleteWorkout(workout: WorkoutEntity) {
        viewModelScope.launch {
            repository.deleteAllSetsFromWorkout(workout.id)
            repository.deleteWorkout(workout)
        }
    }

    class Factory(private val repository: WorkoutRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return RegistroViewModel(repository) as T
        }
    }
}
