package com.personal.futurescalculator.model

import java.math.BigDecimal

data class CalculationInput(
    val side: PositionSide = PositionSide.Long,
    val marginMode: MarginMode = MarginMode.Cross,
    val leverage: BigDecimal = BigDecimal.TEN,
    val margin: BigDecimal? = null,
    val entryPrice: BigDecimal? = null,
    val exitPrice: BigDecimal? = null,
    val quantity: BigDecimal? = null,
    val feeRatePercent: BigDecimal = BigDecimal("0.05"),
    val maintenanceMarginRatePercent: BigDecimal = BigDecimal("0.5")
)
