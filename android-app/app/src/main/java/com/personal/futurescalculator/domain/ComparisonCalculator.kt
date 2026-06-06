package com.personal.futurescalculator.domain

import com.personal.futurescalculator.model.CalculationResult
import com.personal.futurescalculator.model.ComparisonItem
import com.personal.futurescalculator.model.ComparisonResult
import java.math.BigDecimal

class ComparisonCalculator {
    private val calculator = FuturesCalculator()

    fun compare(
        current: CalculationResult?,
        items: List<ComparisonItem>,
        totalFundsForCrossLiquidation: BigDecimal? = null
    ): List<ComparisonResult> {
        return items.map { item ->
            val input = if (item.input.totalFunds == null) {
                item.input.copy(totalFunds = totalFundsForCrossLiquidation)
            } else {
                item.input
            }
            val result = calculator.calculate(input)
            val netPnlDiff = if (current?.netPnl != null && result?.netPnl != null) {
                result.netPnl - current.netPnl
            } else {
                null
            }

            ComparisonResult(
                item = item,
                result = result,
                netPnlDiff = netPnlDiff
            )
        }
    }
}
