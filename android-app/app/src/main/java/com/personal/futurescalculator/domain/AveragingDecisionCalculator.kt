package com.personal.futurescalculator.domain

import com.personal.futurescalculator.model.AveragingDecisionInput
import com.personal.futurescalculator.model.AveragingDecisionResult
import com.personal.futurescalculator.model.PositionSide
import java.math.BigDecimal
import java.math.RoundingMode

class AveragingDecisionCalculator {
    fun calculate(input: AveragingDecisionInput): AveragingDecisionResult? {
        val currentEntryPrice = input.currentEntryPrice ?: return null
        val currentQuantity = input.currentQuantity ?: return null
        val addEntryPrice = input.addEntryPrice ?: return null
        val leverage = input.currentLeverage ?: BigDecimal.ONE

        if (
            currentEntryPrice <= BigDecimal.ZERO ||
            currentQuantity <= BigDecimal.ZERO ||
            addEntryPrice <= BigDecimal.ZERO ||
            (input.addAmount != null && leverage <= BigDecimal.ZERO)
        ) {
            return null
        }

        val addQuantity = when {
            input.addQuantity != null -> input.addQuantity
            input.addAmount != null -> input.addAmount
                .multiply(leverage)
                .divide(addEntryPrice, DIVIDE_SCALE, RoundingMode.HALF_UP)
            else -> return null
        }
        val addAmount = input.addAmount ?: addEntryPrice.multiply(addQuantity)
            .divide(leverage, DIVIDE_SCALE, RoundingMode.HALF_UP)

        if (
            addQuantity <= BigDecimal.ZERO ||
            addAmount <= BigDecimal.ZERO
        ) {
            return null
        }

        val newQuantity = currentQuantity + addQuantity
        val newAveragePrice = (currentEntryPrice * currentQuantity + addEntryPrice * addQuantity)
            .divide(newQuantity, DIVIDE_SCALE, RoundingMode.HALF_UP)
        val pnlWithoutAdding = input.targetExitPrice?.takeIf { it > BigDecimal.ZERO }?.let {
            calculatePnl(input.side, currentEntryPrice, it, currentQuantity)
        }
        val pnlAfterAdding = input.targetExitPrice?.takeIf { it > BigDecimal.ZERO }?.let {
            calculatePnl(input.side, newAveragePrice, it, newQuantity)
        }

        return AveragingDecisionResult(
            newAveragePrice = newAveragePrice,
            newQuantity = newQuantity,
            pnlWithoutAdding = pnlWithoutAdding,
            pnlAfterAdding = pnlAfterAdding,
            pnlChange = if (pnlAfterAdding != null && pnlWithoutAdding != null) {
                pnlAfterAdding - pnlWithoutAdding
            } else {
                null
            },
            averagePriceImprovement = if (input.side == PositionSide.Long) {
                currentEntryPrice - newAveragePrice
            } else {
                newAveragePrice - currentEntryPrice
            },
            quantityIncrease = addQuantity,
            addAmount = addAmount
        )
    }

    private fun calculatePnl(
        side: PositionSide,
        entryPrice: BigDecimal,
        exitPrice: BigDecimal,
        quantity: BigDecimal
    ): BigDecimal {
        return if (side == PositionSide.Long) {
            (exitPrice - entryPrice) * quantity
        } else {
            (entryPrice - exitPrice) * quantity
        }
    }
}
