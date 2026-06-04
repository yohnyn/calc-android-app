package com.personal.futurescalculator.util

import java.math.BigDecimal
import java.math.RoundingMode

object DecimalFormatters {
    private const val CURRENCY_SCALE = 2
    private const val PERCENTAGE_SCALE = 2
    private const val QUANTITY_SCALE = 8
    
    fun formatCurrency(value: BigDecimal?): String {
        if (value == null) return "--"
        return value.setScale(CURRENCY_SCALE, RoundingMode.HALF_UP).toString()
    }
    
    fun formatPercentage(value: BigDecimal?): String {
        if (value == null) return "--"
        return value.setScale(PERCENTAGE_SCALE, RoundingMode.HALF_UP).toString() + "%"
    }
    
    fun formatPositiveNegative(value: BigDecimal?): String {
        if (value == null) return "--"
        val formattedValue = value.setScale(PERCENTAGE_SCALE, RoundingMode.HALF_UP)
        return if (formattedValue >= BigDecimal.ZERO) {
            "+${formattedValue}"
        } else {
            formattedValue.toString()
        }
    }

    fun formatQuantity(value: BigDecimal?): String {
        if (value == null) return "--"
        return value.setScale(QUANTITY_SCALE, RoundingMode.HALF_UP)
            .stripTrailingZeros()
            .toPlainString()
    }
}
