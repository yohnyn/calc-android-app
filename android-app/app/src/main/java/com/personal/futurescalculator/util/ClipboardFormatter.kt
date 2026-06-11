package com.personal.futurescalculator.util

import com.personal.futurescalculator.model.CalculationInput
import com.personal.futurescalculator.model.CalculationResult
import com.personal.futurescalculator.model.AveragingDecisionInput
import com.personal.futurescalculator.model.AveragingDecisionResult
import com.personal.futurescalculator.ui.comparison.ComparisonSchemeView
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
        if (format == CopyFormat.Summary || format == CopyFormat.Ask) {
            return buildString {
                appendLine("$symbol ${input.side.label()} ${input.leverage.stripTrailingZeros().toPlainString()}x")
                appendLine("开仓价：${DecimalFormatters.formatCurrency(input.entryPrice)} USDT")
                appendLine("平仓价：${DecimalFormatters.formatCurrency(input.exitPrice)} USDT")
                appendLine("保证金：${DecimalFormatters.formatCurrency(result?.requiredMargin)} USDT")
                appendLine("净盈亏：${DecimalFormatters.formatPositiveNegative(result?.netPnl)} USDT")
                append("保证金收益率（ROI）：${DecimalFormatters.formatPercentage(result?.roiPercent)}")
            }
        }
        return buildString {
            appendLine("【仓位记录】")
            appendLine("币种：$symbol")
            appendLine("结算模式：U 本位")
            appendLine("方向：${input.side.label()}")
            appendLine("模式：${input.marginMode.label()}")
            appendLine("杠杆：${input.leverage.stripTrailingZeros().toPlainString()}x")
            appendLine()
            appendLine("【价格】")
            appendLine("开仓价：${DecimalFormatters.formatCurrency(input.entryPrice)} USDT")
            appendLine("平仓价：${DecimalFormatters.formatCurrency(input.exitPrice)} USDT")
            appendLine()
            appendLine("【结果】")
            appendLine("净盈亏：${DecimalFormatters.formatPositiveNegative(result?.netPnl)} USDT")
            appendLine("保证金收益率（ROI）：${DecimalFormatters.formatPercentage(result?.roiPercent)}")
            appendLine("交易费用约：${DecimalFormatters.formatCurrency(result?.totalFee)} USDT")
            appendLine()
            append("说明：仅为本地计算结果，不构成交易依据。")
        }
    }

    fun formatComparisonSummary(schemes: List<ComparisonSchemeView>): String {
        val ordered = schemes.filter { it.comparablePnlUsdt() != null }
        val main = ordered.firstOrNull { it.isMain } ?: ordered.firstOrNull()
        return buildString {
            appendLine("【开仓方案对比】")
            if (main != null) {
                appendLine("${main.name}：${DecimalFormatters.formatPositiveNegative(main.comparablePnlUsdt())} USDT")
                ordered.filter { it.id != main.id }.forEach { scheme ->
                    val pnl = scheme.comparablePnlUsdt()
                    appendLine("${scheme.name}：${DecimalFormatters.formatPositiveNegative(pnl)} USDT")
                    if (pnl != null) {
                        appendLine("${scheme.name} 相对${main.name}：${DecimalFormatters.formatPositiveNegative(pnl - main.comparablePnlUsdt()!!)} USDT")
                    }
                }
            } else {
                appendLine("暂无可复制的对比结果")
            }
            appendLine()
            append("说明：仅展示客观计算差值，不代表排序依据。")
        }
    }

    fun formatAveragingResult(
        input: AveragingDecisionInput,
        result: AveragingDecisionResult
    ): String = buildString {
        appendLine("【补仓模拟】")
        appendLine("补仓后成本：${DecimalFormatters.formatCurrency(result.newAveragePrice)}")
        appendLine("回本价：${DecimalFormatters.formatCurrency(result.newAveragePrice)}")
        input.targetExitPrice?.let {
            appendLine("目标价收益：${DecimalFormatters.formatPositiveNegative(result.pnlAfterAdding)} USDT")
        }
        appendLine("相比不补仓：${DecimalFormatters.formatPositiveNegative(result.pnlChange)} USDT")
        appendLine()
        append("说明：仅表示目标价下的收益差值。")
    }

    private fun PositionSide.label(): String = if (this == PositionSide.Long) "做多" else "做空"

    private fun MarginMode.label(): String = if (this == MarginMode.Cross) "全仓" else "逐仓"

    private fun ComparisonSchemeView.comparablePnlUsdt() = result?.netPnl ?: coinMarginedResult?.estimatedValueUsdt

}
