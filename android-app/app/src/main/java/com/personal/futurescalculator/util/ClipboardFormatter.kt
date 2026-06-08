package com.personal.futurescalculator.util

import com.personal.futurescalculator.model.CalculationInput
import com.personal.futurescalculator.model.CalculationResult
import com.personal.futurescalculator.model.ComparisonResult
import com.personal.futurescalculator.model.MarginMode
import com.personal.futurescalculator.model.PositionSide

object ClipboardFormatter {
    fun formatSinglePerformance(
        name: String,
        symbol: String,
        input: CalculationInput,
        result: CalculationResult?
    ): String {
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

    fun formatPerformance(
        input: CalculationInput,
        result: CalculationResult?,
        comparisons: List<ComparisonResult>,
        optimalSchemeName: String?
    ): String {
        return buildString {
            appendLine("合约收益计算战绩")
            appendLine("方案 1：${input.side.label()} · ${input.marginMode.label()} · ${input.leverage.stripTrailingZeros().toPlainString()}x")
            appendLine("开仓价：${DecimalFormatters.formatCurrency(input.entryPrice)} USDT")
            appendLine("平仓价：${DecimalFormatters.formatCurrency(input.exitPrice)} USDT")
            appendLine("投入保证金：${DecimalFormatters.formatCurrency(result?.requiredMargin)} USDT")
            appendLine("净盈亏：${DecimalFormatters.formatPositiveNegative(result?.netPnl)} USDT")
            appendLine("ROI：${DecimalFormatters.formatPercentage(result?.roiPercent)}")
            if (result?.liquidationPrice != null) {
                appendLine("估算强平价：${DecimalFormatters.formatCurrency(result.liquidationPrice)} USDT")
                appendLine("距离估算强平价：${DecimalFormatters.formatPercentage(result.distanceToLiquidationPercent)}")
            } else {
                appendLine("估算强平价：无法可靠估算")
            }
            val targetPrice = input.takeProfitPrice ?: result?.targetProfitPriceByAmount ?: result?.targetProfitPriceByRoi
            val stopPrice = input.stopLossPrice ?: result?.stopLossPriceByAmount ?: result?.stopLossPriceByRoi
            if (targetPrice != null) {
                appendLine("止盈价：${DecimalFormatters.formatCurrency(targetPrice)} USDT")
                result?.takeProfitNetPnl?.let {
                    appendLine("止盈收益：${DecimalFormatters.formatPositiveNegative(it)} USDT")
                }
            }
            if (stopPrice != null) {
                appendLine("止损价：${DecimalFormatters.formatCurrency(stopPrice)} USDT")
                result?.stopLossNetPnl?.let {
                    appendLine("止损亏损：${DecimalFormatters.formatPositiveNegative(it)} USDT")
                }
            }
            result?.rewardRiskRatio?.let {
                appendLine("盈亏比：1:${DecimalFormatters.formatQuantity(it)}")
            }

            comparisons.forEach { comparison ->
                appendLine()
                appendLine("${comparison.item.name}：${comparison.item.input.side.label()} · ${comparison.item.input.marginMode.label()} · ${comparison.item.input.leverage.stripTrailingZeros().toPlainString()}x")
                appendLine("净盈亏：${DecimalFormatters.formatPositiveNegative(comparison.result?.netPnl)} USDT")
                appendLine("相比方案 1：${DecimalFormatters.formatPositiveNegative(comparison.netPnlDiff)} USDT")
            }
            val schemeTwo = comparisons.firstOrNull { it.item.name == "方案 2" }?.result?.netPnl
            val schemeThree = comparisons.firstOrNull { it.item.name == "方案 3" }?.result?.netPnl
            if (schemeTwo != null && schemeThree != null) {
                appendLine("方案 3 相比方案 2：${DecimalFormatters.formatPositiveNegative(schemeThree - schemeTwo)} USDT")
            }

            appendLine()
            append("强平价为估算值，仅供参考，不构成投资建议。")
        }
    }

    private fun PositionSide.label(): String = if (this == PositionSide.Long) "做多" else "做空"

    private fun MarginMode.label(): String = if (this == MarginMode.Cross) "全仓" else "逐仓"

}
