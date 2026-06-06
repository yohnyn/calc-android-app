package com.personal.futurescalculator.domain

import com.personal.futurescalculator.model.CoinMarginedCalculationMode
import com.personal.futurescalculator.model.CoinMarginedResult
import com.personal.futurescalculator.model.PositionSide
import java.math.BigDecimal
import java.math.RoundingMode

class CoinMarginedCalculator {
    fun calculate(
        calculationMode: CoinMarginedCalculationMode,
        side: PositionSide,
        quantity: BigDecimal?,
        entryPrice: BigDecimal?,
        exitPrice: BigDecimal?,
        currentPrice: BigDecimal?
    ): CoinMarginedResult? {
        if (
            quantity == null || quantity <= BigDecimal.ZERO ||
            entryPrice == null || entryPrice <= BigDecimal.ZERO ||
            exitPrice == null || exitPrice <= BigDecimal.ZERO ||
            currentPrice == null || currentPrice <= BigDecimal.ZERO
        ) {
            return null
        }

        val pnlCoin = when (calculationMode) {
            CoinMarginedCalculationMode.CoinQuantity -> {
                val priceDiff = if (side == PositionSide.Long) exitPrice - entryPrice else entryPrice - exitPrice
                quantity.multiply(priceDiff).divide(currentPrice, 16, RoundingMode.HALF_UP)
            }
            CoinMarginedCalculationMode.InverseContract -> {
                val notionalUsdt = quantity * entryPrice
                val entryInverse = BigDecimal.ONE.divide(entryPrice, 16, RoundingMode.HALF_UP)
                val exitInverse = BigDecimal.ONE.divide(exitPrice, 16, RoundingMode.HALF_UP)
                if (side == PositionSide.Long) {
                    notionalUsdt * (entryInverse - exitInverse)
                } else {
                    notionalUsdt * (exitInverse - entryInverse)
                }
            }
        }
        return CoinMarginedResult(
            calculationMode = calculationMode,
            pnlCoin = pnlCoin,
            estimatedValueUsdt = pnlCoin * currentPrice
        )
    }
}
