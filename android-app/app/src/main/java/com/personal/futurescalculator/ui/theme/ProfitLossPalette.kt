package com.personal.futurescalculator.ui.theme

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

data class ProfitLossPalette(
    val profit: Color,
    val loss: Color,
    val profitIndicator: String = "",
    val lossIndicator: String = ""
)

val LocalProfitLossPalette = staticCompositionLocalOf {
    ProfitLossPalette(profit = ProfitGreen, loss = LossRed)
}
