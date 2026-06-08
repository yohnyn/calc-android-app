package com.personal.futurescalculator.data

import android.content.Context
import com.personal.futurescalculator.model.CoinMarginedCalculationMode
import com.personal.futurescalculator.model.ThemeMode

class UiPreferencesRepository(context: Context) {
    private val preferences = context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE)

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

    fun loadCoinMarginedCalculationMode(): CoinMarginedCalculationMode {
        val saved = preferences.getString(
            KEY_COIN_MARGINED_CALCULATION_MODE,
            CoinMarginedCalculationMode.CoinQuantity.name
        )
        return CoinMarginedCalculationMode.entries.firstOrNull { it.name == saved }
            ?: CoinMarginedCalculationMode.CoinQuantity
    }

    fun saveCoinMarginedCalculationMode(mode: CoinMarginedCalculationMode) {
        preferences.edit().putString(KEY_COIN_MARGINED_CALCULATION_MODE, mode.name).apply()
    }

    fun loadCoinMarginedCalculationModeRemembered(): Boolean =
        preferences.getBoolean(KEY_COIN_MARGINED_CALCULATION_MODE_REMEMBERED, false)

    fun saveCoinMarginedCalculationModeRemembered(remembered: Boolean) {
        preferences.edit().putBoolean(KEY_COIN_MARGINED_CALCULATION_MODE_REMEMBERED, remembered).apply()
    }

    fun loadHasSeenCoinMarginedModeDialog(): Boolean = loadCoinMarginedCalculationModeRemembered()

    fun saveHasSeenCoinMarginedModeDialog(hasSeen: Boolean) {
        saveCoinMarginedCalculationModeRemembered(hasSeen)
    }

    private companion object {
        const val PREFERENCES = "ui_preferences"
        const val KEY_AVERAGING_EXPANDED = "averaging_expanded"
        const val KEY_THEME_MODE = "theme_mode"
        const val KEY_COIN_MARGINED_CALCULATION_MODE = "coin_margined_calculation_mode"
        const val KEY_COIN_MARGINED_CALCULATION_MODE_REMEMBERED = "coin_margined_calculation_mode_remembered"
    }
}
