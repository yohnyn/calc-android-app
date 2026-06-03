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
        
        // 计算未扣手续费盈亏
        val grossPnl = calculateGrossPnl(calculatedInput)
        
        // 计算手续费
        val openFee = calculateOpenFee(calculatedInput)
        val closeFee = calculateCloseFee(calculatedInput)
        val totalFee = openFee + closeFee
        
        // 计算净盈亏
        val netPnl = if (grossPnl != null) {
            grossPnl - totalFee
        } else {
            null
        }
        
        // 计算 ROI
        val roiPercent = if (requiredMargin > BigDecimal.ZERO && netPnl != null) {
            netPnl.multiply(BigDecimal(100)).divide(requiredMargin, DIVIDE_SCALE, RoundingMode.HALF_UP)
        } else {
            null
        }
        
        // 计算强平价
        val liquidationPrice = calculateLiquidationPrice(calculatedInput)
        
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
            liquidationPrice = liquidationPrice,
            distanceToLiquidationPercent = distanceToLiquidationPercent
        )
    }

    private fun isValidInput(input: CalculationInput): Boolean {
        // 杠杆必须大于等于1
        if (input.leverage < BigDecimal.ONE) {
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
        
        // 成交数量如果存在必须大于0
        if (input.quantity != null && input.quantity <= BigDecimal.ZERO) {
            return false
        }
        
        // 投入保证金如果存在必须大于0
        if (input.margin != null && input.margin <= BigDecimal.ZERO) {
            return false
        }
        
        // 手续费率必须大于等于0
        if (input.feeRatePercent < BigDecimal.ZERO) {
            return false
        }
        
        // 维持保证金率必须大于等于0
        if (input.maintenanceMarginRatePercent < BigDecimal.ZERO) {
            return false
        }
        
        // 检查是否至少有一个可以计算仓位价值的组合
        val hasPositionValueInputs = (input.entryPrice != null && 
            (input.margin != null || input.quantity != null || input.leverage != null))
        
        if (!hasPositionValueInputs) {
            // 如果没有足够的输入来计算仓位价值，但有平仓价，也可以计算盈亏
            if (input.exitPrice != null && input.entryPrice != null && 
                (input.quantity != null || input.margin != null || input.leverage != null)) {
                return true
            }
            return false
        }
        
        return true
    }

    private fun calculateMissingValues(input: CalculationInput): CalculationInput {
        val newInput = input.copy()
        
        // 如果成交数量为空，根据投入保证金计算
        if (newInput.quantity == null && newInput.margin != null && newInput.entryPrice != null && newInput.leverage != null) {
            val positionValue = newInput.margin * newInput.leverage
            newInput.quantity = positionValue.divide(newInput.entryPrice!!, DIVIDE_SCALE, RoundingMode.HALF_UP)
        }
        
        // 如果投入保证金为空，根据成交数量计算
        if (newInput.margin == null && newInput.quantity != null && newInput.entryPrice != null && newInput.leverage != null) {
            val positionValue = newInput.quantity * newInput.entryPrice
            newInput.margin = positionValue.divide(newInput.leverage, DIVIDE_SCALE, RoundingMode.HALF_UP)
        }
        
        return newInput
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
            // 全仓强平价估算（简化模型）
            if (input.side == PositionSide.Long) {
                input.entryPrice * (BigDecimal.ONE - BigDecimal.ONE.divide(input.leverage, DIVIDE_SCALE, RoundingMode.HALF_UP) + maintenanceMarginRate)
            } else {
                input.entryPrice * (BigDecimal.ONE + BigDecimal.ONE.divide(input.leverage, DIVIDE_SCALE, RoundingMode.HALF_UP) - maintenanceMarginRate)
            }
        } else {
            // 逐仓强平价估算（简化模型）
            if (input.side == PositionSide.Long) {
                input.entryPrice * (BigDecimal.ONE - BigDecimal.ONE.divide(input.leverage, DIVIDE_SCALE, RoundingMode.HALF_UP) + maintenanceMarginRate)
            } else {
                input.entryPrice * (BigDecimal.ONE + BigDecimal.ONE.divide(input.leverage, DIVIDE_SCALE, RoundingMode.HALF_UP) - maintenanceMarginRate)
            }
        }
    }

    private fun calculateDistanceToLiquidation(input: CalculationInput, liquidationPrice: BigDecimal?): BigDecimal? {
        if (liquidationPrice == null) {
            return null
        }
        
        val referencePrice = input.exitPrice ?: input.entryPrice!!
        
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
        
        val feeRate = input.feeRatePercent.divide(BigDecimal(100), DIVIDE_SCALE, RoundingMode.HALF_UP)
        return input.entryPrice!! * input.quantity!! * feeRate
    }
    
    private fun calculateCloseFee(input: CalculationInput): BigDecimal {
        if (input.exitPrice == null || input.quantity == null) {
            return BigDecimal.ZERO
        }
        
        val feeRate = input.feeRatePercent.divide(BigDecimal(100), DIVIDE_SCALE, RoundingMode.HALF_UP)
        return input.exitPrice!! * input.quantity!! * feeRate
    }
}
