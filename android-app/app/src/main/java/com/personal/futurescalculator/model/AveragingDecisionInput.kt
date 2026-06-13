package com.personal.futurescalculator.model

import java.math.BigDecimal

data class AveragingDecisionInput(
    val side: PositionSide = PositionSide.Long,
    val currentEntryPrice: BigDecimal? = null,
    val currentQuantity: BigDecimal? = null,
    val currentMargin: BigDecimal? = null,
    val currentLeverage: BigDecimal? = null,
    val addEntryPrice: BigDecimal? = null,
    val addAmount: BigDecimal? = null,
    val addQuantity: BigDecimal? = null,
    val targetExitPrice: BigDecimal? = null
)

fun AveragingDecisionInput.stableFingerprint(): String = listOf(
    side.name,
    currentEntryPrice.canonicalFingerprint(),
    currentQuantity.canonicalFingerprint(),
    currentMargin.canonicalFingerprint(),
    currentLeverage.canonicalFingerprint(),
    addEntryPrice.canonicalFingerprint(),
    addAmount.canonicalFingerprint(),
    addQuantity.canonicalFingerprint(),
    targetExitPrice.canonicalFingerprint()
).joinToString("|")
