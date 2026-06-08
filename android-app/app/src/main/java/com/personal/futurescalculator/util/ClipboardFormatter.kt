package com.personal.futurescalculator.util

import com.personal.futurescalculator.model.CalculationInput
import com.personal.futurescalculator.model.CalculationResult
import com.personal.futurescalculator.model.CopyFormat
import com.personal.futurescalculator.model.MarginMode
import com.personal.futurescalculator.model.PositionSide

object ClipboardFormatter {
    fun formatSinglePerformance(
        name: String,
        symbol: String,
        input: CalculationInput,
        result: CalculationResult?,
        format: CopyFormat = CopyFormat.Summary
    ): String {
        if (format == CopyFormat.Summary) {
            return buildString {
                appendLine("$name · $symbol")
                appendLine("${input.side.label()} · ${input.marginMode.label()} · ${input.leverage.stripTrailingZeros().toPlainString()}x")
                appendLine("开仓价：${DecimalFormatters.formatCurrency(input.entryPrice)} USDT")
                appendLine("平仓价：${DecimalFormatters.formatCurrency(input.exitPrice)} USDT")
                appendLine("净盈亏：${DecimalFormatters.formatPositiveNegative(result?.netPnl)} USDT")
                append("ROI：${DecimalFormatters.formatPercentage(result?.roiPercent)}")
            }
        }
        return buildString {
            appendLine("$name · $symbol 合约收益战绩")
            appendLine("${input.side.label()} · ${input.marginMode.label()} · ${input.leverage.stripTrailingZeros().toPlainString()}x")
            appendLine("开仓价：${DecimalFormatters.formatCurrency(input.entryPrice)} USDT")
            appendLine("平仓价：${DecimalFormatters.formatCurrency(input.exitPrice)} USDT")
            appendLine("投入保证金：${DecimalFormatters.formatCurrency(result?.requiredMargin)} USDT")
            appendLine("币数量：${DecimalFormatters.formatQuantity(result?.quantity)} $symbol")
            appendLine("净盈亏：${DecimalFormatters.formatPositiveNegative(result?.netPnl)} USDT")
            appendLine("ROI：${DecimalFormatters.formatPercentage(result?.roiPercent)}")
            appendLine("总手续费约：${DecimalFormatters.formatCurrency(result?.totalFee)} USDT")
            appendLine(
                "估算强平价：${result?.liquidationPrice?.let { DecimalFormatters.formatCurrency(it) + " USDT" } ?: "无法可靠估算"}"
            )
            append("强平价为估算值，仅供参考，不构成投资建议。")
        }
    }

    private fun PositionSide.label(): String = if (this == PositionSide.Long) "做多" else "做空"

    private fun MarginMode.label(): String = if (this == MarginMode.Cross) "全仓" else "逐仓"

}
