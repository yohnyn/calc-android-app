package com.personal.futurescalculator.model

import java.math.BigDecimal

data class ComparisonResult(
    val item: ComparisonItem,
    val result: CalculationResult?,
    val netPnlDiff: BigDecimal?,
    val roiDiffPercent: BigDecimal?,
    val requiredMarginDiff: BigDecimal?,
    val positionValueDiff: BigDecimal?,
    val liquidationDistanceDiffPercent: BigDecimal?,
    val riskLevel: ComparisonRiskLevel
)
