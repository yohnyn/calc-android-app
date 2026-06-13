package com.personal.futurescalculator.util

import java.math.BigDecimal
import java.math.RoundingMode

object DecimalFormatters {
    private val ZERO = BigDecimal.ZERO

    fun formatAmount(value: BigDecimal?): String = formatBounded(value, scale = 2)

    fun formatSignedAmount(value: BigDecimal?): String = formatBounded(value, scale = 2, showPositiveSign = true)

    fun formatPrice(value: BigDecimal?): String {
        if (value == null) return "--"
        val scale = if (value.abs() >= BigDecimal.ONE) 2 else 6
        return formatBounded(value, scale)
    }

    fun formatQuantity(value: BigDecimal?): String {
        if (value == null) return "--"
        val scale = if (value.abs() >= BigDecimal("0.0001")) 4 else 6
        return formatBounded(value, scale)
    }

    fun formatCoinAmount(value: BigDecimal?): String = formatBounded(value, scale = 6)

    fun formatSignedCoinAmount(value: BigDecimal?): String = formatBounded(value, scale = 6, showPositiveSign = true)

    fun formatPercentage(value: BigDecimal?): String {
        val formatted = formatBounded(value, scale = 2)
        return if (formatted == "--") formatted else "$formatted%"
    }

    fun formatSignedPercentage(value: BigDecimal?): String {
        val formatted = formatBounded(value, scale = 2, showPositiveSign = true)
        return if (formatted == "--") formatted else "$formatted%"
    }

    fun formatRate(value: BigDecimal?): String {
        val formatted = formatBounded(value, scale = 4)
        return if (formatted == "--") formatted else "$formatted%"
    }

    fun formatRatio(value: BigDecimal?): String = formatBounded(value, scale = 2)

    fun formatLeverage(value: BigDecimal?): String {
        if (value == null) return "--"
        return value.setScale(0, RoundingMode.HALF_UP).toPlainString()
    }

    fun formatFormulaAmount(value: BigDecimal?): String = formatBounded(value, scale = 4)

    // Compatibility aliases. Prefer semantic functions above for new call sites.
    fun formatCurrency(value: BigDecimal?): String = formatAmount(value)

    fun formatPositiveNegative(value: BigDecimal?): String = formatSignedAmount(value)

    fun formatPositionQuantity(value: BigDecimal?): String = formatQuantity(value)

    fun formatDetailedQuantity(value: BigDecimal?): String = formatCoinAmount(value)

    private fun formatBounded(
        value: BigDecimal?,
        scale: Int,
        showPositiveSign: Boolean = false
    ): String {
        if (value == null) return "--"
        if (value.compareTo(ZERO) == 0) return "0"

        val threshold = BigDecimal.ONE.movePointLeft(scale)
        if (value.abs() < threshold) {
            return if (value < ZERO) {
                ">-${threshold.toPlainString()}"
            } else {
                "<${threshold.toPlainString()}"
            }
        }

        val formatted = value
            .setScale(scale, RoundingMode.HALF_UP)
            .stripTrailingZeros()
            .toPlainString()
        return if (showPositiveSign && value > ZERO) "+$formatted" else formatted
    }
}
