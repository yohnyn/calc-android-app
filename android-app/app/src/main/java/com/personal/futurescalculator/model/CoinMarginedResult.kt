package com.personal.futurescalculator.model

import java.math.BigDecimal

data class CoinMarginedResult(
    val calculationMode: CoinMarginedCalculationMode,
    val pnlCoin: BigDecimal,
    val estimatedValueUsdt: BigDecimal
)
