package com.personal.futurescalculator.model

import java.math.BigDecimal

data class CalculationResult(
    val positionValue: BigDecimal,
    val requiredMargin: BigDecimal,
    val quantity: BigDecimal,
    val grossPnl: BigDecimal?,
    val openFee: BigDecimal,
    val closeFee: BigDecimal?,
    val totalFee: BigDecimal?,
    val netPnl: BigDecimal?,
    val roiPercent: BigDecimal?,
    val targetProfitPriceByAmount: BigDecimal?,
    val targetProfitPriceByRoi: BigDecimal?,
    val stopLossPriceByAmount: BigDecimal?,
    val stopLossPriceByRoi: BigDecimal?,
    val liquidationPrice: BigDecimal?,
    val distanceToLiquidationPercent: BigDecimal?,
    val usedTotalFundsForLiquidation: Boolean = false
)
