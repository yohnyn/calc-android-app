package com.personal.futurescalculator.model

import java.math.BigDecimal

data class AveragingResult(
    val totalQuantity: BigDecimal,
    val newAveragePrice: BigDecimal,
    val currentPositionValue: BigDecimal,
    val addedPositionValue: BigDecimal,
    val totalPositionValue: BigDecimal,
    val liquidationPriceBefore: BigDecimal?,
    val liquidationPriceAfter: BigDecimal?,
    val liquidationPriceChangePercent: BigDecimal?
)
