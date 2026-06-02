package com.example.gympulse.ui.estadisticas

import java.util.Calendar

data class Filtros(
    val año: Int? = null,
    val mes: Int? = null
)

data class SemanaInfo(
    val startDate: Long,
    val endDate: Long,
    val label: String
)

fun getYearsRange(workoutYears: Set<Int>): List<Int> {
    val currentYear = Calendar.getInstance().get(Calendar.YEAR)
    val minYear = minOf(workoutYears.minOrNull() ?: currentYear, currentYear)
    return (minYear..currentYear).toList().reversed()
}

fun getMonthName(month: Int): String {
    return listOf("Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
        "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre")
        .getOrElse(month - 1) { "" }
}

fun getShortMonthName(month: Int): String {
    return listOf("Ene", "Feb", "Mar", "Abr", "May", "Jun",
        "Jul", "Ago", "Sep", "Oct", "Nov", "Dic")
        .getOrElse(month - 1) { "" }
}

fun extraerAño(timestamp: Long): Int {
    val cal = Calendar.getInstance()
    cal.timeInMillis = timestamp
    return cal.get(Calendar.YEAR)
}

fun extraerMes(timestamp: Long): Int {
    val cal = Calendar.getInstance()
    cal.timeInMillis = timestamp
    return cal.get(Calendar.MONTH) + 1
}

fun getWeeksForMonth(year: Int, month: Int): List<SemanaInfo> {
    val cal = Calendar.getInstance()
    cal.set(year, month - 1, 1, 0, 0, 0)
    cal.set(Calendar.MILLISECOND, 0)
    val daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
    val dow = ((cal.get(Calendar.DAY_OF_WEEK) + 5) % 7)

    val firstMonday = cal.clone() as Calendar
    firstMonday.add(Calendar.DAY_OF_MONTH, -dow)

    val weeks = mutableListOf<SemanaInfo>()
    val current = firstMonday.clone() as Calendar

    var maxWeeks = 6
    while (maxWeeks > 0) {
        val start = current.clone() as Calendar
        val end = current.clone() as Calendar
        end.add(Calendar.DAY_OF_MONTH, 6)
        end.set(Calendar.HOUR_OF_DAY, 23)
        end.set(Calendar.MINUTE, 59)
        end.set(Calendar.SECOND, 59)
        end.set(Calendar.MILLISECOND, 999)

        val lastDayOfMonth = Calendar.getInstance()
        lastDayOfMonth.set(year, month - 1, daysInMonth, 23, 59, 59)
        lastDayOfMonth.set(Calendar.MILLISECOND, 999)

        if (end.before(lastDayOfMonth) || (start.timeInMillis <= lastDayOfMonth.timeInMillis && start.timeInMillis >= firstMonday.timeInMillis) || weeks.isEmpty()) {
            val months = listOf("Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
                "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre")
            val startDay = start.get(Calendar.DAY_OF_MONTH)
            val endDay = end.get(Calendar.DAY_OF_MONTH)
            val endMonth = months[end.get(Calendar.MONTH)]
            val endYear = end.get(Calendar.YEAR)
            val label = "$startDay - $endDay $endMonth $endYear"
            weeks.add(SemanaInfo(start.timeInMillis, end.timeInMillis, label))
            current.add(Calendar.DAY_OF_MONTH, 7)
            maxWeeks--
        } else break
    }
    return weeks
}

fun generarDiasCalendario(year: Int, month: Int): List<Int?> {
    val cal = Calendar.getInstance()
    cal.set(year, month - 1, 1)
    val daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
    val startDow = ((cal.get(Calendar.DAY_OF_WEEK) + 5) % 7)

    val dias = mutableListOf<Int?>()
    repeat(startDow) { dias.add(null) }
    for (d in 1..daysInMonth) dias.add(d)
    while (dias.size % 7 != 0) dias.add(null)
    return dias
}

fun semanaIndexForDate(year: Int, month: Int, timestamp: Long): Int? {
    val weeks = getWeeksForMonth(year, month)
    return weeks.indexOfFirst { timestamp in it.startDate..it.endDate }
}

fun minutosEnRango(workouts: List<com.example.gympulse.data.WorkoutEntity>, start: Long, end: Long): Long {
    return workouts.filter { w ->
        w.date in start..end
    }.sumOf { w ->
        if (w.endTime != null) maxOf(0L, (w.endTime - w.startTime) / 60000) else 0L
    }
}
