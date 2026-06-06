package com.personal.futurescalculator.model

import java.math.BigDecimal

data class AveragingInput(
    val currentEntryPrice: BigDecimal? = null,
    val currentQuantity: BigDecimal? = null,
    val addEntryPrice: BigDecimal? = null,
    val addQuantity: BigDecimal? = null
)
