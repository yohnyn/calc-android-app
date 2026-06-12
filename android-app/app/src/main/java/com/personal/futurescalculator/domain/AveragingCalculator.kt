package com.personal.futurescalculator.domain

import com.personal.futurescalculator.model.AveragingInput
import com.personal.futurescalculator.model.AveragingResult
import com.personal.futurescalculator.model.MarginMode
import com.personal.futurescalculator.model.PositionSide
import java.math.BigDecimal
import java.math.RoundingMode

class AveragingCalculator {
    fun calculate(
        input: AveragingInput,
        side: PositionSide,
        marginMode: MarginMode,
        leverage: BigDecimal,
        maintenanceMarginRatePercent: BigDecimal
    ): AveragingResult? {
        val currentEntryPrice = input.currentEntryPrice ?: return null
        val currentQuantity = input.currentQuantity ?: return null
        val addEntryPrice = input.addEntryPrice ?: return null
        val addQuantity = input.addQuantity ?: return null

        if (
            currentEntryPrice <= BigDecimal.ZERO ||
            currentQuantity <= BigDecimal.ZERO ||
            addEntryPrice <= BigDecimal.ZERO ||
            addQuantity <= BigDecimal.ZERO ||
            leverage < BigDecimal.ONE ||
            leverage > BigDecimal("125") ||
            maintenanceMarginRatePercent < BigDecimal.ZERO ||
            maintenanceMarginRatePercent >= BigDecimal("100")
        ) {
            return null
        }

        val currentPositionValue = currentEntryPrice * currentQuantity
        val addedPositionValue = addEntryPrice * addQuantity
        val totalQuantity = currentQuantity + addQuantity
        val totalPositionValue = currentPositionValue + addedPositionValue
        val newAveragePrice = totalPositionValue.divide(totalQuantity, DIVIDE_SCALE, RoundingMode.HALF_UP)
        val liquidationPriceBefore = if (marginMode == MarginMode.Isolated) {
            calculateLiquidationPrice(currentEntryPrice, side, leverage, maintenanceMarginRatePercent)
        } else {
            null
        }
        val liquidationPriceAfter = if (marginMode == MarginMode.Isolated) {
            calculateLiquidationPrice(newAveragePrice, side, leverage, maintenanceMarginRatePercent)
        } else {
            null
        }
        val liquidationPriceChangePercent = if (
            liquidationPriceBefore == null ||
            liquidationPriceAfter == null ||
            liquidationPriceBefore.compareTo(BigDecimal.ZERO) == 0
        ) {
            null
        } else {
            liquidationPriceAfter
                .subtract(liquidationPriceBefore)
                .multiply(BigDecimal(100))
                .divide(liquidationPriceBefore, DIVIDE_SCALE, RoundingMode.HALF_UP)
        }

        return AveragingResult(
            totalQuantity = totalQuantity,
            newAveragePrice = newAveragePrice,
            currentPositionValue = currentPositionValue,
            addedPositionValue = addedPositionValue,
            totalPositionValue = totalPositionValue,
            liquidationPriceBefore = liquidationPriceBefore,
            liquidationPriceAfter = liquidationPriceAfter,
            liquidationPriceChangePercent = liquidationPriceChangePercent
        )
    }

    private fun calculateLiquidationPrice(
        entryPrice: BigDecimal,
        side: PositionSide,
        leverage: BigDecimal,
        maintenanceMarginRatePercent: BigDecimal
    ): BigDecimal {
        val maintenanceMarginRate = maintenanceMarginRatePercent
            .divide(BigDecimal(100), DIVIDE_SCALE, RoundingMode.HALF_UP)
        val leverageRate = BigDecimal.ONE.divide(leverage, DIVIDE_SCALE, RoundingMode.HALF_UP)

        return if (side == PositionSide.Long) {
            entryPrice
                .multiply(BigDecimal.ONE - leverageRate + maintenanceMarginRate)
        } else {
            entryPrice
                .multiply(BigDecimal.ONE + leverageRate - maintenanceMarginRate)
        }
    }
}
