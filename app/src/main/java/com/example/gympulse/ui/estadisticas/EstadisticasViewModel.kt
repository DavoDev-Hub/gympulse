package com.example.gympulse.ui.estadisticas

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.gympulse.data.PreferencesManager
import com.example.gympulse.data.WorkoutRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar

class EstadisticasViewModel(
    application: Application,
    private val repository: WorkoutRepository
) : AndroidViewModel(application) {
    private val prefs = PreferencesManager(application)

    data class EstadisticasUI(
        val totalWorkouts: Int = 0,
        val totalMinutos: Long = 0,
        val rachaActual: Int = 0,
        val grafico: List<BarraDato> = emptyList(),
        val tituloGrafico: String = "Actividad",
        val unidadGrafico: String = "min",
        val ejerciciosMasFrecuentes: List<Pair<String, Int>> = emptyList()
    )

    data class BarraDato(
        val label: String,
        val valor: Long
    )

    private val _stats = MutableStateFlow(EstadisticasUI())
    val stats: StateFlow<EstadisticasUI> = _stats

    var filtros by mutableStateOf(Filtros())
    var semanaSeleccionada by mutableStateOf<SemanaInfo?>(null)

    private val _yearsAvailable = MutableStateFlow<List<Int>>(emptyList())
    val yearsAvailable: StateFlow<List<Int>> = _yearsAvailable

    private var allWorkouts: List<com.example.gympulse.data.WorkoutEntity> = emptyList()

    init {
        loadWorkouts()
    }

    private fun loadWorkouts() {
        viewModelScope.launch {
            repository.getAllWorkouts().collect { workouts ->
                allWorkouts = workouts
                val years = workouts.map { extraerAño(it.date) }.toSet()
                _yearsAvailable.value = getYearsRange(years)
                actualizarStats()
            }
        }
    }

    fun actualizarStats() {
        val ws = allWorkouts
        if (ws.isEmpty()) {
            _stats.value = EstadisticasUI()
            return
        }

        val filtrados = aplicarFiltros(ws)
        val total = filtrados.size
        val minutos = filtrados.sumOf { w ->
            if (w.endTime != null) maxOf(0L, (w.endTime - w.startTime) / 60000) else 0L
        }
        val racha = calcularRacha(ws)

        val (grafico, titulo, unidad) = computarGrafico(filtrados)

        _stats.value = EstadisticasUI(
            totalWorkouts = total,
            totalMinutos = minutos,
            rachaActual = racha,
            grafico = grafico,
            tituloGrafico = titulo,
            unidadGrafico = unidad,
            ejerciciosMasFrecuentes = emptyList()
        )

        viewModelScope.launch {
            val frecuentes = ejerciciosFrecuentes(filtrados)
            _stats.value = _stats.value.copy(ejerciciosMasFrecuentes = frecuentes.take(5))
        }
    }

    fun setFiltroAño(año: Int?) {
        filtros = filtros.copy(año = año, mes = null)
        semanaSeleccionada = null
        actualizarStats()
    }

    fun setFiltroMes(mes: Int?) {
        filtros = filtros.copy(mes = mes)
        semanaSeleccionada = null
        actualizarStats()
    }

    fun setSemana(semana: SemanaInfo?) {
        semanaSeleccionada = semana
        actualizarStats()
    }

    private fun aplicarFiltros(ws: List<com.example.gympulse.data.WorkoutEntity>): List<com.example.gympulse.data.WorkoutEntity> {
        var result = ws
        val a = filtros.año
        val m = filtros.mes
        val s = semanaSeleccionada

        if (a != null) result = result.filter { extraerAño(it.date) == a }
        if (m != null) result = result.filter { extraerMes(it.date) == m }
        if (s != null) result = result.filter { it.date in s.startDate..s.endDate }
        return result
    }

    private fun computarGrafico(ws: List<com.example.gympulse.data.WorkoutEntity>): Triple<List<BarraDato>, String, String> {
        val a = filtros.año
        val m = filtros.mes
        val s = semanaSeleccionada

        if (a == null) return Triple(porAños(ws), "Horas por año", "h")
        if (m == null) return Triple(porMeses(ws, a), "Minutos por mes", "min")
        if (s == null) return Triple(porDiasDelMes(ws, a, m), "Minutos por día", "min")
        return Triple(porSemana(ws), "Minutos por día", "min")
    }

    private fun porAños(ws: List<com.example.gympulse.data.WorkoutEntity>): List<BarraDato> {
        val disponibles = _yearsAvailable.value.ifEmpty { listOf(Calendar.getInstance().get(Calendar.YEAR)) }
        return disponibles.map { año ->
            val mins = ws.filter { extraerAño(it.date) == año }.sumOf { w ->
                if (w.endTime != null) maxOf(0L, (w.endTime - w.startTime) / 60000) else 0L
            }
            BarraDato(año.toString(), if (mins > 0) maxOf(1, mins / 60) else 0)
        }
    }

    private fun porMeses(ws: List<com.example.gympulse.data.WorkoutEntity>, año: Int): List<BarraDato> {
        return (1..12).map { mes ->
            val mins = ws.filter { extraerAño(it.date) == año && extraerMes(it.date) == mes }.sumOf { w ->
                if (w.endTime != null) maxOf(0L, (w.endTime - w.startTime) / 60000) else 0L
            }
            BarraDato(getShortMonthName(mes), mins)
        }
    }

    private fun porDiasDelMes(ws: List<com.example.gympulse.data.WorkoutEntity>, year: Int, month: Int): List<BarraDato> {
        val cal = Calendar.getInstance()
        cal.set(year, month - 1, 1)
        val daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
        return (1..daysInMonth).map { day ->
            val dayStart = Calendar.getInstance().apply {
                set(year, month - 1, day, 0, 0, 0)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis
            val dayEnd = Calendar.getInstance().apply {
                set(year, month - 1, day, 23, 59, 59)
                set(Calendar.MILLISECOND, 999)
            }.timeInMillis
            val mins = ws.filter { w -> w.date in dayStart..dayEnd }.sumOf { w ->
                if (w.endTime != null) maxOf(0L, (w.endTime - w.startTime) / 60000) else 0L
            }
            BarraDato(day.toString(), mins)
        }
    }

    private fun porSemana(ws: List<com.example.gympulse.data.WorkoutEntity>): List<BarraDato> {
        val dias = listOf("Lun", "Mar", "Mié", "Jue", "Vie", "Sáb", "Dom")
        val s = semanaSeleccionada ?: return dias.map { BarraDato(it, 0L) }
        return dias.mapIndexed { idx, nombre ->
            val cal = Calendar.getInstance()
            cal.timeInMillis = s.startDate
            cal.add(Calendar.DAY_OF_MONTH, idx)
            cal.set(Calendar.HOUR_OF_DAY, 0)
            cal.set(Calendar.MINUTE, 0)
            cal.set(Calendar.SECOND, 0)
            cal.set(Calendar.MILLISECOND, 0)
            val diaStart = cal.timeInMillis
            cal.add(Calendar.DAY_OF_MONTH, 1)
            val diaEnd = cal.timeInMillis - 1

            val mins = ws.filter { w -> w.date in diaStart..diaEnd }.sumOf { w ->
                if (w.endTime != null) maxOf(0L, (w.endTime - w.startTime) / 60000) else 0L
            }
            BarraDato(nombre, mins)
        }
    }

    private fun calcularRacha(workouts: List<com.example.gympulse.data.WorkoutEntity>): Int {
        if (workouts.isEmpty()) return 0
        val restDays = prefs.restDays
        val cal = Calendar.getInstance()
        val hoy = cal.get(Calendar.DAY_OF_YEAR)
        val hoyAno = cal.get(Calendar.YEAR)
        val diasConWorkout = workouts.map { w ->
            val c = Calendar.getInstance().apply { timeInMillis = w.date }
            c.get(Calendar.YEAR) * 1000 + c.get(Calendar.DAY_OF_YEAR)
        }.toSet()
        fun diaId(ano: Int, dia: Int) = ano * 1000 + dia
        fun diaSemana(ano: Int, diaDelAno: Int): Int {
            cal.set(Calendar.YEAR, ano)
            cal.set(Calendar.DAY_OF_YEAR, diaDelAno)
            return cal.get(Calendar.DAY_OF_WEEK) // 1=Sunday, 2=Monday, ..., 7=Saturday
        }

        var racha = 0
        var diaActual = hoy
        var anoActual = hoyAno

        // Start from today if there's activity, otherwise yesterday
        if (diaId(anoActual, diaActual) !in diasConWorkout) {
            diaActual--
            if (diaActual <= 0) { diaActual = 365; anoActual-- }
        }

        while (true) {
            val id = diaId(anoActual, diaActual)
            if (id in diasConWorkout) {
                racha++
            } else {
                // Check if it's a rest day → skip without breaking
                val dow = diaSemana(anoActual, diaActual)
                val restIndex = if (dow == Calendar.SUNDAY) 7 else dow - 1
                if (restIndex in restDays) {
                    // skip this day, don't break streak
                } else {
                    break
                }
            }
            diaActual--
            if (diaActual <= 0) { diaActual = 365; anoActual-- }
            // Safety: avoid infinite loop
            if (racha > 2000) break
        }
        return racha
    }

    private suspend fun ejerciciosFrecuentes(ws: List<com.example.gympulse.data.WorkoutEntity>): List<Pair<String, Int>> {
        val conteo = mutableMapOf<String, Int>()
        ws.forEach { workout ->
            val sets = repository.getSetsForWorkoutOnce(workout.id)
            sets.filter { it.completed }.forEach { set ->
                conteo[set.exerciseName] = (conteo[set.exerciseName] ?: 0) + 1
            }
        }
        return conteo.entries.sortedByDescending { it.value }.map { it.key to it.value }
    }

    class Factory(
        private val application: Application,
        private val repository: WorkoutRepository
    ) : ViewModelProvider.Factory {
        override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return EstadisticasViewModel(application, repository) as T
        }
    }
}
