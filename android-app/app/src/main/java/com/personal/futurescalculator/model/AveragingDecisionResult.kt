package com.personal.futurescalculator.model

import java.math.BigDecimal

data class AveragingDecisionResult(
    val newAveragePrice: BigDecimal,
    val newQuantity: BigDecimal,
    val pnlWithoutAdding: BigDecimal?,
    val pnlAfterAdding: BigDecimal?,
    val pnlChange: BigDecimal?,
    val averagePriceImprovement: BigDecimal,
    val quantityIncrease: BigDecimal,
    val addAmount: BigDecimal
)
