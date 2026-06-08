package com.personal.futurescalculator.model

import java.math.BigDecimal

data class ComparisonResult(
    val item: ComparisonItem,
    val result: CalculationResult?,
    val coinMarginedResult: CoinMarginedResult? = null,
    val netPnlDiff: BigDecimal?
)
