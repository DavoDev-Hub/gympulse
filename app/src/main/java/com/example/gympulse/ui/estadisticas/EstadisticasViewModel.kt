package com.example.gympulse.ui.estadisticas

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.gympulse.data.ExerciseSetEntity
import com.example.gympulse.data.WorkoutEntity
import com.example.gympulse.data.WorkoutRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar

class EstadisticasViewModel(private val repository: WorkoutRepository) : ViewModel() {

    // --- Modelos de UI ---
    data class EstadisticasUI(
        val totalWorkouts: Int = 0,
        val totalMinutos: Long = 0,
        val rachaActual: Int = 0,
        val workoutsPorSemana: List<Pair<String, Int>> = emptyList(), // "Lun" -> cantidad
        val ejerciciosMasFrecuentes: List<Pair<String, Int>> = emptyList() // nombre -> veces
    )

    private val _stats = MutableStateFlow(EstadisticasUI())
    val stats: StateFlow<EstadisticasUI> = _stats

    init {
        loadStats()
    }

    private fun loadStats() {
        viewModelScope.launch {
            repository.getAllWorkouts().collect { workouts ->
                if (workouts.isEmpty()) {
                    _stats.value = EstadisticasUI()
                    return@collect
                }

                // Total workouts
                val total = workouts.size

                // Total minutos
                val minutos = workouts.sumOf { w ->
                    if (w.endTime != null) (w.endTime - w.startTime) / 60000 else 0L
                }

                // Racha actual (dias consecutivos)
                val racha = calcularRacha(workouts)

                // Workouts por dia de la semana
                val porSemana = workoutsPorDiaSemana(workouts)

                // Ejercicios mas frecuentes
                val frecuentes = ejerciciosFrecuentes(workouts)

                _stats.value = EstadisticasUI(
                    totalWorkouts          = total,
                    totalMinutos           = minutos,
                    rachaActual            = racha,
                    workoutsPorSemana      = porSemana,
                    ejerciciosMasFrecuentes = frecuentes
                )
            }
        }
    }

    private fun calcularRacha(workouts: List<WorkoutEntity>): Int {
        if (workouts.isEmpty()) return 0
        val cal = Calendar.getInstance()
        val hoy = cal.get(Calendar.DAY_OF_YEAR)

        val diasConWorkout = workouts.map { w ->
            val c = Calendar.getInstance()
            c.timeInMillis = w.date
            c.get(Calendar.DAY_OF_YEAR)
        }.toSet()

        var racha = 0
        var dia = hoy
        while (diasConWorkout.contains(dia)) {
            racha++
            dia--
        }
        return racha
    }

    private fun workoutsPorDiaSemana(workouts: List<WorkoutEntity>): List<Pair<String, Int>> {
        val dias = listOf("Lun", "Mar", "Mié", "Jue", "Vie", "Sáb", "Dom")
        val conteo = IntArray(7)
        workouts.forEach { w ->
            val cal = Calendar.getInstance()
            cal.timeInMillis = w.date
            // Calendar.DAY_OF_WEEK: 1=Dom, 2=Lun... ajustamos a 0=Lun
            val idx = (cal.get(Calendar.DAY_OF_WEEK) + 5) % 7
            conteo[idx]++
        }
        return dias.mapIndexed { i, nombre -> nombre to conteo[i] }
    }

    private suspend fun ejerciciosFrecuentes(
        workouts: List<WorkoutEntity>
    ): List<Pair<String, Int>> {
        val conteo = mutableMapOf<String, Int>()
        workouts.forEach { workout ->
            val sets = repository.getSetsForWorkout(workout.id).first()
            sets.forEach { set ->
                conteo[set.exerciseName] = (conteo[set.exerciseName] ?: 0) + 1
            }
        }
        return conteo.entries
            .sortedByDescending { it.value }
            .take(5)
            .map { it.key to it.value }
    }

    class Factory(private val repository: WorkoutRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return EstadisticasViewModel(repository) as T
        }
    }
}