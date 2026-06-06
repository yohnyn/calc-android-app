package com.personal.futurescalculator.data

import android.content.Context
import com.personal.futurescalculator.model.HomeModule
import com.personal.futurescalculator.model.ThemeMode

class UiPreferencesRepository(context: Context) {
    private val preferences = context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE)

    fun loadModuleOrder(): List<HomeModule> {
        val saved = preferences.getString(KEY_MODULE_ORDER, null)
            ?.split(",")
            ?.mapNotNull { name -> HomeModule.entries.firstOrNull { it.name == name } }
            .orEmpty()
        return (saved + HomeModule.defaultOrder).distinct()
    }

    fun saveModuleOrder(modules: List<HomeModule>) {
        preferences.edit().putString(KEY_MODULE_ORDER, modules.joinToString(",") { it.name }).apply()
    }

    fun loadVisibleModules(): Set<HomeModule> {
        val hidden = preferences.getStringSet(KEY_HIDDEN_MODULES, emptySet()).orEmpty()
        return HomeModule.entries.filterNot { it.name in hidden }.toSet()
    }

    fun saveVisibleModules(modules: Set<HomeModule>) {
        val hidden = HomeModule.entries.filterNot { it in modules }.map { it.name }.toSet()
        preferences.edit().putStringSet(KEY_HIDDEN_MODULES, hidden).apply()
    }

    fun loadAveragingExpanded(): Boolean = preferences.getBoolean(KEY_AVERAGING_EXPANDED, true)

    fun saveAveragingExpanded(expanded: Boolean) {
        preferences.edit().putBoolean(KEY_AVERAGING_EXPANDED, expanded).apply()
    }

    fun loadThemeMode(): ThemeMode {
        val saved = preferences.getString(KEY_THEME_MODE, ThemeMode.System.name)
        return ThemeMode.entries.firstOrNull { it.name == saved } ?: ThemeMode.System
    }

    fun saveThemeMode(mode: ThemeMode) {
        preferences.edit().putString(KEY_THEME_MODE, mode.name).apply()
    }

    private companion object {
        const val PREFERENCES = "ui_preferences"
        const val KEY_MODULE_ORDER = "module_order"
        const val KEY_HIDDEN_MODULES = "hidden_modules"
        const val KEY_AVERAGING_EXPANDED = "averaging_expanded"
        const val KEY_THEME_MODE = "theme_mode"
    }
}
