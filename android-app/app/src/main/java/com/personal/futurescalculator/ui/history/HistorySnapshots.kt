package com.personal.futurescalculator.ui.history

import com.personal.futurescalculator.model.AveragingDecisionInput
import com.personal.futurescalculator.model.AveragingDecisionResult
import com.personal.futurescalculator.model.CalculationInput
import com.personal.futurescalculator.model.CalculationResult
import com.personal.futurescalculator.model.CoinMarginedResult
import com.personal.futurescalculator.model.HistoryCategory
import com.personal.futurescalculator.model.HistoryField
import com.personal.futurescalculator.model.HistoryRecord
import com.personal.futurescalculator.model.HistorySection
import com.personal.futurescalculator.model.MarginMode
import com.personal.futurescalculator.model.PositionSide
import com.personal.futurescalculator.model.SettlementMode
import com.personal.futurescalculator.model.stableFingerprint
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
    summary = "${DecimalFormatters.formatSignedAmount(result.netPnl)} USDT",
    roiSummary = DecimalFormatters.formatPercentage(result.roiPercent),
    savedAt = System.currentTimeMillis(),
    fingerprint = "profit|$symbol|${settlementMode.name}|${input.stableFingerprint()}",
    sections = listOf(
        HistorySection("交易参数", listOf(
            HistoryField("币种", symbol),
            HistoryField("合约模式", if (settlementMode == SettlementMode.UsdtMargined) "U 本位" else "币本位"),
            HistoryField("方向", input.side.label()),
            HistoryField("保证金模式", input.marginMode.label()),
            HistoryField("杠杆", "${DecimalFormatters.formatLeverage(input.leverage)}x"),
            HistoryField("保证金", "${DecimalFormatters.formatAmount(input.margin)} USDT"),
            HistoryField("数量", "${DecimalFormatters.formatQuantity(result.quantity)} $symbol"),
            HistoryField("开仓价", "${DecimalFormatters.formatPrice(input.entryPrice)} USDT"),
            HistoryField("平仓价", "${DecimalFormatters.formatPrice(input.exitPrice)} USDT"),
            HistoryField("开仓费率", DecimalFormatters.formatRate(input.openFeeRatePercent)),
            HistoryField("平仓费率", DecimalFormatters.formatRate(input.closeFeeRatePercent)),
            HistoryField("维持保证金率", DecimalFormatters.formatRate(input.maintenanceMarginRatePercent)),
            HistoryField("总资金", input.totalFunds?.let { "${DecimalFormatters.formatAmount(it)} USDT" } ?: "未填写")
        )),
        HistorySection("保存时结果", buildList {
            add(HistoryField("仓位价值", "${DecimalFormatters.formatAmount(result.positionValue)} USDT"))
            add(HistoryField("手续费估算", "${DecimalFormatters.formatAmount(result.totalFee)} USDT"))
            add(HistoryField("净盈亏", "${DecimalFormatters.formatSignedAmount(result.netPnl)} USDT"))
            add(HistoryField("保证金收益率（ROI）", DecimalFormatters.formatPercentage(result.roiPercent)))
            if (input.estimateLiquidation && result.liquidationPrice != null) {
                add(HistoryField("强平价格", "${DecimalFormatters.formatPrice(result.liquidationPrice)} USDT"))
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
    summary = result.pnlChange?.let { "${DecimalFormatters.formatSignedAmount(it)} USDT" }
        ?: "均价改善 ${DecimalFormatters.formatPrice(result.averagePriceImprovement)} USDT",
    roiSummary = null,
    savedAt = System.currentTimeMillis(),
    fingerprint = "averaging|$symbol|${input.stableFingerprint()}",
    sections = listOf(
        HistorySection("详细数据", listOf(
            HistoryField("方向", input.side.label()),
            HistoryField("当前均价", "${DecimalFormatters.formatPrice(input.currentEntryPrice)} USDT"),
            HistoryField("当前数量", "${DecimalFormatters.formatCoinAmount(input.currentQuantity)} $symbol"),
            HistoryField("当前保证金", "${DecimalFormatters.formatAmount(input.currentMargin)} USDT"),
            HistoryField("当前杠杆", "${DecimalFormatters.formatLeverage(input.currentLeverage)}x"),
            HistoryField("补仓价格", "${DecimalFormatters.formatPrice(input.addEntryPrice)} USDT"),
            HistoryField("补仓金额", "${DecimalFormatters.formatAmount(result.addAmount)} USDT"),
            HistoryField("补仓数量", "${DecimalFormatters.formatCoinAmount(result.quantityIncrease)} $symbol"),
            HistoryField("目标平仓价", input.targetExitPrice?.let { "${DecimalFormatters.formatPrice(it)} USDT" } ?: "未填写")
        )),
        HistorySection("保存时结果", buildList {
            add(HistoryField("补仓后均价", "${DecimalFormatters.formatPrice(result.newAveragePrice)} USDT"))
            add(HistoryField("补仓后总仓位", "${DecimalFormatters.formatCoinAmount(result.newQuantity)} $symbol"))
            add(HistoryField("均价改善", "${DecimalFormatters.formatPrice(result.averagePriceImprovement)} USDT"))
            result.pnlWithoutAdding?.let { add(HistoryField("补仓前收益", "${DecimalFormatters.formatSignedAmount(it)} USDT")) }
            result.pnlAfterAdding?.let { add(HistoryField("补仓后收益", "${DecimalFormatters.formatSignedAmount(it)} USDT")) }
            result.pnlChange?.let { add(HistoryField("收益变化", "${DecimalFormatters.formatSignedAmount(it)} USDT")) }
        })
    )
)

fun createCoinMarginedHistorySnapshot(
    input: CalculationInput,
    result: CoinMarginedResult,
    symbol: String
): HistoryRecord = HistoryRecord(
    id = "history_${System.currentTimeMillis()}",
    category = HistoryCategory.ProfitCalculation,
    title = "$symbol 币本位 ${input.side.label()}",
    summary = "${DecimalFormatters.formatSignedCoinAmount(result.pnlCoin)} $symbol",
    roiSummary = null,
    savedAt = System.currentTimeMillis(),
    fingerprint = "coin_profit|$symbol|${result.calculationMode.name}|${input.stableFingerprint()}",
    sections = listOf(
        HistorySection("交易参数", listOf(
            HistoryField("币种", symbol),
            HistoryField("合约模式", "币本位"),
            HistoryField("计算方式", result.calculationMode.label),
            HistoryField("方向", input.side.label()),
            HistoryField("保证金模式", input.marginMode.label()),
            HistoryField("杠杆", "${DecimalFormatters.formatLeverage(input.leverage)}x"),
            HistoryField("数量", "${DecimalFormatters.formatCoinAmount(input.quantity)} $symbol"),
            HistoryField("开仓价", "${DecimalFormatters.formatPrice(input.entryPrice)} USDT"),
            HistoryField("平仓价", "${DecimalFormatters.formatPrice(input.exitPrice)} USDT")
        )),
        HistorySection("保存时结果", listOf(
            HistoryField("币本位盈亏", "${DecimalFormatters.formatSignedCoinAmount(result.pnlCoin)} $symbol"),
            HistoryField("折算价值", "${DecimalFormatters.formatSignedAmount(result.estimatedValueUsdt)} USDT")
        ))
    )
)

private fun PositionSide.label(): String = if (this == PositionSide.Long) "做多" else "做空"

private fun MarginMode.label(): String = if (this == MarginMode.Cross) "全仓" else "逐仓"
