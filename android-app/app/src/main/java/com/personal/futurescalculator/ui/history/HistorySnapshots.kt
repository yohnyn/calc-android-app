package com.personal.futurescalculator.ui.history

import com.personal.futurescalculator.model.AveragingDecisionInput
import com.personal.futurescalculator.model.AveragingDecisionResult
import com.personal.futurescalculator.model.CalculationInput
import com.personal.futurescalculator.model.CalculationResult
import com.personal.futurescalculator.model.HistoryCategory
import com.personal.futurescalculator.model.HistoryField
import com.personal.futurescalculator.model.HistoryRecord
import com.personal.futurescalculator.model.HistorySection
import com.personal.futurescalculator.model.MarginMode
import com.personal.futurescalculator.model.PositionSide
import com.personal.futurescalculator.model.SettlementMode
import com.personal.futurescalculator.util.DecimalFormatters

fun createProfitHistorySnapshot(
    input: CalculationInput,
    result: CalculationResult,
    symbol: String,
    settlementMode: SettlementMode
): HistoryRecord = HistoryRecord(
    id = "history_${System.currentTimeMillis()}",
    category = HistoryCategory.ProfitCalculation,
    title = "$symbol ${input.side.label()}",
    summary = "${DecimalFormatters.formatPositiveNegative(result.netPnl)} USDT",
    roiSummary = DecimalFormatters.formatPercentage(result.roiPercent),
    savedAt = System.currentTimeMillis(),
    sections = listOf(
        HistorySection("交易参数", listOf(
            HistoryField("币种", symbol),
            HistoryField("合约模式", if (settlementMode == SettlementMode.UsdtMargined) "U 本位" else "币本位"),
            HistoryField("方向", input.side.label()),
            HistoryField("保证金模式", input.marginMode.label()),
            HistoryField("杠杆", "${input.leverage.stripTrailingZeros().toPlainString()}x"),
            HistoryField("保证金", "${DecimalFormatters.formatCurrency(input.margin)} USDT"),
            HistoryField("数量", "${DecimalFormatters.formatQuantity(result.quantity)} $symbol"),
            HistoryField("开仓价", "${DecimalFormatters.formatCurrency(input.entryPrice)} USDT"),
            HistoryField("平仓价", "${DecimalFormatters.formatCurrency(input.exitPrice)} USDT"),
            HistoryField("开仓费率", "${input.openFeeRatePercent.stripTrailingZeros().toPlainString()}%"),
            HistoryField("平仓费率", "${input.closeFeeRatePercent.stripTrailingZeros().toPlainString()}%"),
            HistoryField("维持保证金率", "${input.maintenanceMarginRatePercent.stripTrailingZeros().toPlainString()}%"),
            HistoryField("总资金", input.totalFunds?.let { "${DecimalFormatters.formatCurrency(it)} USDT" } ?: "未填写")
        )),
        HistorySection("保存时结果", buildList {
            add(HistoryField("仓位价值", "${DecimalFormatters.formatCurrency(result.positionValue)} USDT"))
            add(HistoryField("手续费估算", "${DecimalFormatters.formatCurrency(result.totalFee)} USDT"))
            add(HistoryField("净盈亏", "${DecimalFormatters.formatPositiveNegative(result.netPnl)} USDT"))
            add(HistoryField("ROI", DecimalFormatters.formatPercentage(result.roiPercent)))
            if (input.estimateLiquidation && result.liquidationPrice != null) {
                add(HistoryField("强平价格", "${DecimalFormatters.formatCurrency(result.liquidationPrice)} USDT"))
                add(HistoryField("距离强平", DecimalFormatters.formatPercentage(result.distanceToLiquidationPercent)))
            }
        })
    )
)

fun createAveragingHistorySnapshot(
    input: AveragingDecisionInput,
    result: AveragingDecisionResult,
    symbol: String
): HistoryRecord = HistoryRecord(
    id = "history_${System.currentTimeMillis()}",
    category = HistoryCategory.AveragingSimulation,
    title = "$symbol 补仓模拟",
    summary = "${DecimalFormatters.formatPositiveNegative(result.pnlChange)} USDT",
    roiSummary = null,
    savedAt = System.currentTimeMillis(),
    sections = listOf(
        HistorySection("详细数据", listOf(
            HistoryField("方向", input.side.label()),
            HistoryField("当前均价", "${DecimalFormatters.formatCurrency(input.currentEntryPrice)} USDT"),
            HistoryField("当前数量", "${DecimalFormatters.formatQuantity(input.currentQuantity)} $symbol"),
            HistoryField("当前保证金", "${DecimalFormatters.formatCurrency(input.currentMargin)} USDT"),
            HistoryField("当前杠杆", "${DecimalFormatters.formatQuantity(input.currentLeverage)}x"),
            HistoryField("补仓价格", "${DecimalFormatters.formatCurrency(input.addEntryPrice)} USDT"),
            HistoryField("补仓金额", "${DecimalFormatters.formatCurrency(result.addAmount)} USDT"),
            HistoryField("补仓数量", "${DecimalFormatters.formatQuantity(result.quantityIncrease)} $symbol"),
            HistoryField("目标平仓价", "${DecimalFormatters.formatCurrency(input.targetExitPrice)} USDT")
        )),
        HistorySection("保存时结果", listOf(
            HistoryField("补仓后均价", "${DecimalFormatters.formatCurrency(result.newAveragePrice)} USDT"),
            HistoryField("补仓后总仓位", "${DecimalFormatters.formatQuantity(result.newQuantity)} $symbol"),
            HistoryField("补仓前收益", "${DecimalFormatters.formatPositiveNegative(result.pnlWithoutAdding)} USDT"),
            HistoryField("补仓后收益", "${DecimalFormatters.formatPositiveNegative(result.pnlAfterAdding)} USDT"),
            HistoryField("收益变化", "${DecimalFormatters.formatPositiveNegative(result.pnlChange)} USDT")
        ))
    )
)

private fun PositionSide.label(): String = if (this == PositionSide.Long) "做多" else "做空"

private fun MarginMode.label(): String = if (this == MarginMode.Cross) "全仓" else "逐仓"
