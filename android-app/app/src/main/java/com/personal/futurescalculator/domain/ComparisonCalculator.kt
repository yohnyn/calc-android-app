package com.personal.futurescalculator.domain

import com.personal.futurescalculator.model.CalculationResult
import com.personal.futurescalculator.model.CoinMarginedResult
import com.personal.futurescalculator.model.ComparisonItem
import com.personal.futurescalculator.model.ComparisonResult
import com.personal.futurescalculator.model.SettlementMode
import java.math.BigDecimal

class ComparisonCalculator {
    private val futuresCalculator = FuturesCalculator()
    private val coinMarginedCalculator = CoinMarginedCalculator()

    fun compare(
        current: CalculationResult?,
        currentCoinMargined: CoinMarginedResult? = null,
        currentSettlementMode: SettlementMode = SettlementMode.UsdtMargined,
        items: List<ComparisonItem>,
        coinPricesById: Map<String, BigDecimal> = emptyMap(),
        totalFundsForCrossLiquidation: BigDecimal? = null
    ): List<ComparisonResult> {
        val currentComparablePnl = comparablePnl(currentSettlementMode, current, currentCoinMargined)
        return items.map { item ->
            val input = if (item.input.totalFunds == null) {
                item.input.copy(totalFunds = totalFundsForCrossLiquidation)
            } else {
                item.input
            }

            val result = if (item.settlementMode == SettlementMode.UsdtMargined) {
                futuresCalculator.calculate(input)
            } else {
                null
            }
            val coinMarginedResult = if (item.settlementMode == SettlementMode.CoinMargined) {
                coinMarginedCalculator.calculate(
                    calculationMode = item.coinMarginedCalculationMode,
                    side = input.side,
                    quantity = input.quantity,
                    entryPrice = input.entryPrice,
                    exitPrice = input.exitPrice,
                    currentPrice = coinPricesById[item.coinId]
                )
            } else {
                null
            }
            val itemComparablePnl = comparablePnl(item.settlementMode, result, coinMarginedResult)
            val netPnlDiff = if (currentComparablePnl != null && itemComparablePnl != null) {
                itemComparablePnl - currentComparablePnl
            } else {
                null
            }

            ComparisonResult(
                item = item,
                result = result,
                coinMarginedResult = coinMarginedResult,
                netPnlDiff = netPnlDiff
            )
        }
    }

    private fun comparablePnl(
        settlementMode: SettlementMode,
        result: CalculationResult?,
        coinMarginedResult: CoinMarginedResult?
    ): BigDecimal? = when (settlementMode) {
        SettlementMode.UsdtMargined -> result?.netPnl
        SettlementMode.CoinMargined -> coinMarginedResult?.estimatedValueUsdt
    }
}
