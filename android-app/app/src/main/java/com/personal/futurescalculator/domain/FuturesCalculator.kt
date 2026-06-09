package com.personal.futurescalculator.domain

import com.personal.futurescalculator.model.CalculationInput
import com.personal.futurescalculator.model.CalculationResult
import com.personal.futurescalculator.model.PositionSide
import com.personal.futurescalculator.model.MarginMode
import java.math.BigDecimal
import java.math.RoundingMode

class FuturesCalculator {
    fun calculate(input: CalculationInput): CalculationResult? {
        // 验证输入
        if (!isValidInput(input)) {
            return null
        }

        // 根据最后编辑字段计算缺失的值
        val calculatedInput = calculateMissingValues(input)
        
        // 计算仓位价值
        val positionValue = calculatedInput.entryPrice!! * calculatedInput.quantity!!
        
        // 计算所需初始保证金
        val requiredMargin = positionValue.divide(calculatedInput.leverage, DIVIDE_SCALE, RoundingMode.HALF_UP)
        
        val targetProfitPriceByAmount = calculateTargetProfitPriceByAmount(calculatedInput)
        val targetProfitPriceByRoi = calculateTargetProfitPriceByRoi(calculatedInput, requiredMargin)
        val stopLossPriceByAmount = calculateStopLossPriceByAmount(calculatedInput)
        val stopLossPriceByRoi = calculateStopLossPriceByRoi(calculatedInput, requiredMargin)
        val takeProfitPrice = calculatedInput.takeProfitPrice ?: targetProfitPriceByAmount ?: targetProfitPriceByRoi
        val stopLossPrice = calculatedInput.stopLossPrice ?: stopLossPriceByAmount ?: stopLossPriceByRoi
        val reversePrices = listOfNotNull(
            takeProfitPrice,
            stopLossPrice
        )
        val effectiveExitPrice = calculatedInput.exitPrice
            ?: reversePrices.singleOrNull()
        val calculationInput = calculatedInput.copy(exitPrice = effectiveExitPrice)

        // 计算未扣手续费盈亏
        val grossPnl = calculateGrossPnl(calculationInput)
        
        // 计算手续费
        val openFee = calculateOpenFee(calculationInput)
        val closeFee = calculateCloseFee(calculationInput)
        val totalFee = closeFee?.let { openFee + it }
        
        // 计算净盈亏
        val netPnl = if (grossPnl != null) {
            grossPnl - (totalFee ?: openFee)
        } else {
            null
        }
        
        // 计算 ROI
        val roiPercent = if (requiredMargin > BigDecimal.ZERO && netPnl != null) {
            netPnl.multiply(BigDecimal(100)).divide(requiredMargin, DIVIDE_SCALE, RoundingMode.HALF_UP)
        } else {
            null
        }
        val takeProfitNetPnl = takeProfitPrice?.let { calculateNetPnlAtExit(calculatedInput, it, openFee) }
        val stopLossNetPnl = stopLossPrice?.let { calculateNetPnlAtExit(calculatedInput, it, openFee) }
        val rewardRiskRatio = if (
            takeProfitNetPnl != null &&
            stopLossNetPnl != null &&
            takeProfitNetPnl > BigDecimal.ZERO &&
            stopLossNetPnl < BigDecimal.ZERO
        ) {
            takeProfitNetPnl.divide(stopLossNetPnl.abs(), DIVIDE_SCALE, RoundingMode.HALF_UP)
        } else {
            null
        }
        
        // 计算强平价
        val liquidationPrice = if (calculatedInput.estimateLiquidation) {
            calculateLiquidationPrice(calculatedInput)
        } else {
            null
        }

        // 计算到强平价距离
        val distanceToLiquidationPercent = calculateDistanceToLiquidation(calculatedInput, liquidationPrice)
        
        return CalculationResult(
            positionValue = positionValue,
            requiredMargin = requiredMargin,
            quantity = calculatedInput.quantity!!,
            grossPnl = grossPnl,
            openFee = openFee,
            closeFee = closeFee,
            totalFee = totalFee,
            netPnl = netPnl,
            roiPercent = roiPercent,
            takeProfitNetPnl = takeProfitNetPnl,
            stopLossNetPnl = stopLossNetPnl,
            rewardRiskRatio = rewardRiskRatio,
            targetProfitPriceByAmount = targetProfitPriceByAmount,
            targetProfitPriceByRoi = targetProfitPriceByRoi,
            stopLossPriceByAmount = stopLossPriceByAmount,
            stopLossPriceByRoi = stopLossPriceByRoi,
            liquidationPrice = liquidationPrice,
            distanceToLiquidationPercent = distanceToLiquidationPercent,
            usedTotalFundsForLiquidation = calculatedInput.estimateLiquidation &&
                calculatedInput.marginMode == MarginMode.Cross &&
                calculatedInput.totalFunds != null &&
                liquidationPrice != null
        )
    }

    private fun isValidInput(input: CalculationInput): Boolean {
        // 杠杆必须大于等于1
        if (input.leverage < BigDecimal.ONE || input.leverage > BigDecimal("125")) {
            return false
        }
        
        // 开仓价必须大于0
        if (input.entryPrice == null || input.entryPrice <= BigDecimal.ZERO) {
            return false
        }
        
        // 平仓价如果存在必须大于0
        if (input.exitPrice != null && input.exitPrice <= BigDecimal.ZERO) {
            return false
        }

        if (input.takeProfitPrice != null && input.takeProfitPrice <= BigDecimal.ZERO) {
            return false
        }

        if (input.stopLossPrice != null && input.stopLossPrice <= BigDecimal.ZERO) {
            return false
        }
        
        // 成交数量如果存在必须大于0
        if (input.quantity != null && input.quantity <= BigDecimal.ZERO) {
            return false
        }
        
        // 投入保证金如果存在必须大于0
        if (input.margin != null && input.margin <= BigDecimal.ZERO) {
            return false
        }

        if (input.totalFunds != null && input.totalFunds <= BigDecimal.ZERO) {
            return false
        }
        
        // 手续费率必须大于等于0
        if (
            input.openFeeRatePercent < BigDecimal.ZERO ||
            input.closeFeeRatePercent < BigDecimal.ZERO ||
            input.openFeeRatePercent >= BigDecimal("100") ||
            input.closeFeeRatePercent >= BigDecimal("100")
        ) {
            return false
        }

        if (input.targetProfitAmount != null && input.targetProfitAmount < BigDecimal.ZERO) {
            return false
        }

        if (input.targetRoiPercent != null && input.targetRoiPercent < BigDecimal.ZERO) {
            return false
        }

        if (input.maxLossAmount != null && input.maxLossAmount < BigDecimal.ZERO) {
            return false
        }

        if (input.maxLossRoiPercent != null && input.maxLossRoiPercent < BigDecimal.ZERO) {
            return false
        }
        
        // 维持保证金率必须大于等于0
        if (
            input.maintenanceMarginRatePercent < BigDecimal.ZERO ||
            input.maintenanceMarginRatePercent >= BigDecimal("100")
        ) {
            return false
        }
        
        // 检查是否至少有一个可以计算仓位价值的组合
        val hasPositionValueInputs = (input.entryPrice != null && 
            (input.margin != null || input.quantity != null))
        
        if (!hasPositionValueInputs) {
            // 如果没有足够的输入来计算仓位价值，但有平仓价，也可以计算盈亏
            if (input.exitPrice != null && input.entryPrice != null && 
                (input.quantity != null || input.margin != null)) {
                return true
            }
            return false
        }
        
        return true
    }

    private fun calculateMissingValues(input: CalculationInput): CalculationInput {
        // 如果成交数量为空，根据投入保证金计算
        if (input.quantity == null && input.margin != null && input.entryPrice != null && input.leverage != null) {
            val positionValue = input.margin * input.leverage
            val calculatedQuantity = positionValue.divide(input.entryPrice!!, DIVIDE_SCALE, RoundingMode.HALF_UP)
            return input.copy(quantity = calculatedQuantity)
        }
        
        // 如果投入保证金为空，根据成交数量计算
        if (input.margin == null && input.quantity != null && input.entryPrice != null && input.leverage != null) {
            val positionValue = input.quantity * input.entryPrice
            val calculatedMargin = positionValue.divide(input.leverage, DIVIDE_SCALE, RoundingMode.HALF_UP)
            return input.copy(margin = calculatedMargin)
        }
        
        return input
    }

    private fun calculateGrossPnl(input: CalculationInput): BigDecimal? {
        if (input.exitPrice == null) {
            return null
        }
        
        val pnl = if (input.side == PositionSide.Long) {
            (input.exitPrice!! - input.entryPrice!!) * input.quantity!!
        } else {
            (input.entryPrice!! - input.exitPrice!!) * input.quantity!!
        }
        
        return pnl
    }

    private fun calculateLiquidationPrice(input: CalculationInput): BigDecimal? {
        if (input.entryPrice == null || input.leverage == null || input.maintenanceMarginRatePercent == null) {
            return null
        }
        
        val maintenanceMarginRate = input.maintenanceMarginRatePercent.divide(BigDecimal(100), DIVIDE_SCALE, RoundingMode.HALF_UP)
        
        return if (input.marginMode == MarginMode.Cross) {
            val quantity = input.quantity ?: return null
            val positionNotional = input.entryPrice.multiply(quantity)
            val totalFunds = input.totalFunds ?: return null
            val denominator = quantity.multiply(
                if (input.side == PositionSide.Long) {
                    BigDecimal.ONE - maintenanceMarginRate
                } else {
                    BigDecimal.ONE + maintenanceMarginRate
                }
            )
            if (denominator <= BigDecimal.ZERO) {
                null
            } else {
                val estimatedPrice = if (input.side == PositionSide.Long) {
                    positionNotional.subtract(totalFunds)
                        .divide(denominator, DIVIDE_SCALE, RoundingMode.HALF_UP)
                } else {
                    positionNotional.add(totalFunds)
                        .divide(denominator, DIVIDE_SCALE, RoundingMode.HALF_UP)
                }
                estimatedPrice.takeIf { it > BigDecimal.ZERO }
            }
        } else {
            // 逐仓简化估算：强平时，仓位权益等于按强平价计算的维持保证金。
            val leverageRate = BigDecimal.ONE.divide(input.leverage, DIVIDE_SCALE, RoundingMode.HALF_UP)
            if (input.side == PositionSide.Long) {
                input.entryPrice
                    .multiply(BigDecimal.ONE - leverageRate)
                    .divide(BigDecimal.ONE - maintenanceMarginRate, DIVIDE_SCALE, RoundingMode.HALF_UP)
            } else {
                input.entryPrice
                    .multiply(BigDecimal.ONE + leverageRate)
                    .divide(BigDecimal.ONE + maintenanceMarginRate, DIVIDE_SCALE, RoundingMode.HALF_UP)
            }
        }
    }

    private fun calculateDistanceToLiquidation(input: CalculationInput, liquidationPrice: BigDecimal?): BigDecimal? {
        if (liquidationPrice == null) {
            return null
        }
        
        val referencePrice = input.entryPrice!!
        
        return if (input.side == PositionSide.Long) {
            (referencePrice - liquidationPrice).multiply(BigDecimal(100))
                .divide(referencePrice, DIVIDE_SCALE, RoundingMode.HALF_UP)
        } else {
            (liquidationPrice - referencePrice).multiply(BigDecimal(100))
                .divide(referencePrice, DIVIDE_SCALE, RoundingMode.HALF_UP)
        }
    }

    private fun calculateOpenFee(input: CalculationInput): BigDecimal {
        if (input.entryPrice == null || input.quantity == null) {
            return BigDecimal.ZERO
        }
        
        val feeRate = input.openFeeRatePercent.divide(BigDecimal(100), DIVIDE_SCALE, RoundingMode.HALF_UP)
        return input.entryPrice!! * input.quantity!! * feeRate
    }
    
    private fun calculateCloseFee(input: CalculationInput): BigDecimal? {
        if (input.exitPrice == null || input.quantity == null) {
            return null
        }
        
        val feeRate = input.closeFeeRatePercent.divide(BigDecimal(100), DIVIDE_SCALE, RoundingMode.HALF_UP)
        return input.exitPrice!! * input.quantity!! * feeRate
    }

    private fun calculateNetPnlAtExit(
        input: CalculationInput,
        exitPrice: BigDecimal,
        openFee: BigDecimal
    ): BigDecimal? {
        val quantity = input.quantity ?: return null
        val grossPnl = if (input.side == PositionSide.Long) {
            (exitPrice - input.entryPrice!!) * quantity
        } else {
            (input.entryPrice!! - exitPrice) * quantity
        }
        val closeFeeRate = input.closeFeeRatePercent.divide(BigDecimal(100), DIVIDE_SCALE, RoundingMode.HALF_UP)
        val closeFee = exitPrice * quantity * closeFeeRate
        return grossPnl - openFee - closeFee
    }

    private fun calculateTargetProfitPriceByAmount(input: CalculationInput): BigDecimal? {
        val amount = input.targetProfitAmount ?: return null
        return calculateExitPriceForNetPnl(input, amount)
    }

    private fun calculateTargetProfitPriceByRoi(
        input: CalculationInput,
        requiredMargin: BigDecimal
    ): BigDecimal? {
        val roiPercent = input.targetRoiPercent ?: return null
        val targetNetPnl = requiredMargin
            .multiply(roiPercent)
            .divide(BigDecimal(100), DIVIDE_SCALE, RoundingMode.HALF_UP)
        return calculateExitPriceForNetPnl(input, targetNetPnl)
    }

    private fun calculateStopLossPriceByAmount(input: CalculationInput): BigDecimal? {
        val amount = input.maxLossAmount ?: return null
        return calculateExitPriceForNetPnl(input, amount.negate())
    }

    private fun calculateStopLossPriceByRoi(
        input: CalculationInput,
        requiredMargin: BigDecimal
    ): BigDecimal? {
        val roiPercent = input.maxLossRoiPercent ?: return null
        val targetNetPnl = requiredMargin
            .multiply(roiPercent)
            .divide(BigDecimal(100), DIVIDE_SCALE, RoundingMode.HALF_UP)
            .negate()
        return calculateExitPriceForNetPnl(input, targetNetPnl)
    }

    private fun calculateExitPriceForNetPnl(input: CalculationInput, targetNetPnl: BigDecimal): BigDecimal? {
        val entryPrice = input.entryPrice ?: return null
        val quantity = input.quantity ?: return null
        if (quantity <= BigDecimal.ZERO) return null

        val openFeeRate = input.openFeeRatePercent.divide(BigDecimal(100), DIVIDE_SCALE, RoundingMode.HALF_UP)
        val closeFeeRate = input.closeFeeRatePercent.divide(BigDecimal(100), DIVIDE_SCALE, RoundingMode.HALF_UP)
        val targetPerQuantity = targetNetPnl.divide(quantity, DIVIDE_SCALE, RoundingMode.HALF_UP)

        val price = if (input.side == PositionSide.Long) {
            val denominator = BigDecimal.ONE - closeFeeRate
            if (denominator <= BigDecimal.ZERO) return null
            val numerator = targetPerQuantity + entryPrice * (BigDecimal.ONE + openFeeRate)
            numerator.divide(denominator, DIVIDE_SCALE, RoundingMode.HALF_UP)
        } else {
            val denominator = BigDecimal.ONE + closeFeeRate
            val numerator = entryPrice * (BigDecimal.ONE - openFeeRate) - targetPerQuantity
            numerator.divide(denominator, DIVIDE_SCALE, RoundingMode.HALF_UP)
        }

        return price.takeIf { it > BigDecimal.ZERO }
    }
}
