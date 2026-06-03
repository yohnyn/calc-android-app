import com.personal.futurescalculator.model.CalculationInput
import com.personal.futurescalculator.model.CalculationResult
import com.personal.futurescalculator.model.PositionSide
import com.personal.futurescalculator.model.MarginMode
import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode

const val DIVIDE_SCALE = 18
val CALCULATION_CONTEXT = MathContext(34, RoundingMode.HALF_UP)
val DISPLAY_ROUNDING = RoundingMode.HALF_UP

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
        
        // 计算所需保证金
        val requiredMargin = positionValue.divide(calculatedInput.leverage, DIVIDE_SCALE, RoundingMode.HALF_UP)
        
        // 计算成交数量
        val quantity = calculatedInput.quantity!!
        
        // 计算手续费
        val openFee = calculatedInput.entryPrice!! * quantity * calculatedInput.feeRatePercent.divide(BigDecimal(100), DIVIDE_SCALE, RoundingMode.HALF_UP)
        val closeFee = calculatedInput.exitPrice?.let { 
            it * quantity * calculatedInput.feeRatePercent.divide(BigDecimal(100), DIVIDE_SCALE, RoundingMode.HALF_UP)
        } ?: BigDecimal.ZERO
        
        val totalFee = openFee + closeFee
        
        // 计算未扣手续费盈亏
        val grossPnl = calculateGrossPnl(calculatedInput)
        
        // 计算净盈亏
        val netPnl = grossPnl - totalFee
        
        // 计算ROI
        val roiPercent = if (requiredMargin.compareTo(BigDecimal.ZERO) != 0) {
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
            quantity = quantity,
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
        
        // 成交数量和投入保证金至少有一个可用于推导仓位
        if (input.quantity == null && input.margin == null) {
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
        
        return true
    }
    
    private fun calculateMissingValues(input: CalculationInput): CalculationInput {
        val newInput = input.copy()
        
        // 如果成交数量为空，根据投入保证金计算
        if (newInput.quantity == null && newInput.margin != null) {
            val positionValue = newInput.margin * newInput.leverage
            newInput.quantity = positionValue.divide(newInput.entryPrice!!, DIVIDE_SCALE, RoundingMode.HALF_UP)
        }
        
        // 如果投入保证金为空，根据成交数量计算
        if (newInput.margin == null && newInput.quantity != null) {
            val positionValue = newInput.entryPrice!! * newInput.quantity!!
            newInput.margin = positionValue.divide(newInput.leverage, DIVIDE_SCALE, RoundingMode.HALF_UP)
        }
        
        return newInput
    }
    
    private fun calculateGrossPnl(input: CalculationInput): BigDecimal {
        if (input.exitPrice == null) {
            return BigDecimal.ZERO
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
        if (liquidationPrice == null || input.exitPrice == null) {
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
}
