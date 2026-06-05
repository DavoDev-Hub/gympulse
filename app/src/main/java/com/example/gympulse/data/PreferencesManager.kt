package com.example.gympulse.data

import android.content.Context

class PreferencesManager(context: Context) {
    private val prefs = context.getSharedPreferences("gympulse_prefs", Context.MODE_PRIVATE)

    var restDayDialogDismissedDate: String?
        get() = prefs.getString("rest_day_dismissed", null)
        set(value) = prefs.edit().putString("rest_day_dismissed", value).apply()

    var restDays: Set<Int>
        get() = prefs.getStringSet("rest_days", emptySet())?.mapNotNull { it.toIntOrNull() }?.toSet() ?: emptySet()
        set(value) {
            prefs.edit().putStringSet("rest_days", value.map { it.toString() }.toSet()).apply()
        }
}
