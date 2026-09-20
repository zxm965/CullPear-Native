package com.zxm965.cullpear.core.preferences

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.edit

enum class ThemeMode { SYSTEM, LIGHT, DARK }
enum class Accent { PEAR, OCEAN, BERRY, AMBER }

class ThemePreferences(context: Context) {
    private val preferences = context.getSharedPreferences("appearance", Context.MODE_PRIVATE)

    var mode by mutableStateOf(readMode())
        private set
    var accent by mutableStateOf(readAccent())
        private set

    fun updateMode(value: ThemeMode) {
        mode = value
        preferences.edit { putString(KEY_MODE, value.name) }
    }

    fun updateAccent(value: Accent) {
        accent = value
        preferences.edit { putString(KEY_ACCENT, value.name) }
    }

    private fun readMode() = runCatching {
        ThemeMode.valueOf(preferences.getString(KEY_MODE, null) ?: ThemeMode.SYSTEM.name)
    }.getOrDefault(ThemeMode.SYSTEM)

    private fun readAccent() = runCatching {
        Accent.valueOf(preferences.getString(KEY_ACCENT, null) ?: Accent.PEAR.name)
    }.getOrDefault(Accent.PEAR)

    private companion object {
        const val KEY_MODE = "theme_mode"
        const val KEY_ACCENT = "theme_accent"
    }
}
