package com.personal.futurescalculator.domain

import com.personal.futurescalculator.model.CalculationResult
import com.personal.futurescalculator.model.ComparisonItem
import com.personal.futurescalculator.model.ComparisonResult
import com.personal.futurescalculator.model.ComparisonRiskLevel
import java.math.BigDecimal

class ComparisonCalculator {
    fun compare(
        current: CalculationResult?,
        items: List<ComparisonItem>
    ): List<ComparisonResult> {
        if (current == null) {
            return items.map { item ->
                ComparisonResult(
                    item = item,
                    result = null,
                    netPnlDiff = null,
                    roiDiffPercent = null,
                    requiredMarginDiff = null,
                    positionValueDiff = null,
                    liquidationDistanceDiffPercent = null,
                    riskLevel = ComparisonRiskLevel.Unknown
                )
            }
        }

        return items.map { item ->
            val result = calculateComparisonResult(current, item)
            val netPnlDiff = calculateNetPnlDiff(current, result)
            val roiDiffPercent = calculateRoiDiff(current, result)
            val requiredMarginDiff = calculateRequiredMarginDiff(current, result)
            val positionValueDiff = calculatePositionValueDiff(current, result)
            val liquidationDistanceDiffPercent = calculateLiquidationDistanceDiff(current, result)
            val riskLevel = calculateRiskLevel(current, result)

            ComparisonResult(
                item = item,
                result = result,
                netPnlDiff = netPnlDiff,
                roiDiffPercent = roiDiffPercent,
                requiredMarginDiff = requiredMarginDiff,
                positionValueDiff = positionValueDiff,
                liquidationDistanceDiffPercent = liquidationDistanceDiffPercent,
                riskLevel = riskLevel
            )
        }
    }

    private fun calculateComparisonResult(current: CalculationResult, item: ComparisonItem): CalculationResult? {
        val calculator = FuturesCalculator()
        return calculator.calculate(item.input)
    }

    private fun calculateNetPnlDiff(current: CalculationResult, compared: CalculationResult?): BigDecimal? {
        if (compared?.netPnl == null || current.netPnl == null) {
            return null
        }
        return compared.netPnl - current.netPnl
    }

    private fun calculateRoiDiff(current: CalculationResult, compared: CalculationResult?): BigDecimal? {
        if (compared?.roiPercent == null || current.roiPercent == null) {
            return null
        }
        return compared.roiPercent - current.roiPercent
    }

    private fun calculateRequiredMarginDiff(current: CalculationResult, compared: CalculationResult?): BigDecimal? {
        if (compared?.requiredMargin == null || current.requiredMargin == null) {
            return null
        }
        return compared.requiredMargin - current.requiredMargin
    }

    private fun calculatePositionValueDiff(current: CalculationResult, compared: CalculationResult?): BigDecimal? {
        if (compared?.positionValue == null || current.positionValue == null) {
            return null
        }
        return compared.positionValue - current.positionValue
    }

    private fun calculateLiquidationDistanceDiff(current: CalculationResult, compared: CalculationResult?): BigDecimal? {
        if (compared?.distanceToLiquidationPercent == null || current.distanceToLiquidationPercent == null) {
            return null
        }
        return compared.distanceToLiquidationPercent - current.distanceToLiquidationPercent
    }

    private fun calculateRiskLevel(current: CalculationResult, compared: CalculationResult?): ComparisonRiskLevel {
        if (compared == null || current.distanceToLiquidationPercent == null || compared.distanceToLiquidationPercent == null) {
            return ComparisonRiskLevel.Unknown
        }

        val diff = compared.distanceToLiquidationPercent - current.distanceToLiquidationPercent

        return when {
            diff < BigDecimal("-5") -> ComparisonRiskLevel.HigherRisk
            diff > BigDecimal("5") -> ComparisonRiskLevel.LowerRisk
            else -> ComparisonRiskLevel.SimilarRisk
        }
    }
}
