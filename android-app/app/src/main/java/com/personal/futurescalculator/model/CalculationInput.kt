package com.personal.futurescalculator.model

import java.math.BigDecimal

data class CalculationInput(
    val side: PositionSide = PositionSide.Long,
    val marginMode: MarginMode = MarginMode.Cross,
    val leverage: BigDecimal = BigDecimal.ONE,
    val margin: BigDecimal? = null,
    val entryPrice: BigDecimal? = null,
    val exitPrice: BigDecimal? = null,
    val quantity: BigDecimal? = null,
    val openFeeRatePercent: BigDecimal = BigDecimal("0.05"),
    val closeFeeRatePercent: BigDecimal = BigDecimal("0.05"),
    val takeProfitPrice: BigDecimal? = null,
    val stopLossPrice: BigDecimal? = null,
    val targetProfitAmount: BigDecimal? = null,
    val targetRoiPercent: BigDecimal? = null,
    val maxLossAmount: BigDecimal? = null,
    val maxLossRoiPercent: BigDecimal? = null,
    val maintenanceMarginRatePercent: BigDecimal = BigDecimal("0.5"),
    val totalFunds: BigDecimal? = null,
    val estimateLiquidation: Boolean = false
)
