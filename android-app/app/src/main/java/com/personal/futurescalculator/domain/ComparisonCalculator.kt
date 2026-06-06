package com.personal.futurescalculator.domain

import com.personal.futurescalculator.model.CalculationResult
import com.personal.futurescalculator.model.ComparisonItem
import com.personal.futurescalculator.model.ComparisonResult

class ComparisonCalculator {
    private val calculator = FuturesCalculator()

    fun compare(
        current: CalculationResult?,
        items: List<ComparisonItem>
    ): List<ComparisonResult> {
        return items.map { item ->
            val result = calculator.calculate(item.input)
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
