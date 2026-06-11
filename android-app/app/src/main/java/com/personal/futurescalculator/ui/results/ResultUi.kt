package com.personal.futurescalculator.ui.results

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.personal.futurescalculator.model.CalculationInput
import com.personal.futurescalculator.model.CalculationResult
import com.personal.futurescalculator.model.CoinMarginedResult
import com.personal.futurescalculator.model.MarginMode
import com.personal.futurescalculator.model.PositionSide
import com.personal.futurescalculator.model.SettlementMode
import com.personal.futurescalculator.ui.SectionPanel
import com.personal.futurescalculator.ui.theme.LocalProfitLossPalette
import com.personal.futurescalculator.ui.theme.WarningAmber
import com.personal.futurescalculator.util.DecimalFormatters
import java.math.BigDecimal

@Composable
fun MainResultDialog(
    input: CalculationInput,
    result: CalculationResult,
    symbol: String,
    onShowFeeSettings: () -> Unit,
    onDismiss: () -> Unit
) {
    var showInputDetails by rememberSaveable { mutableStateOf(true) }
    var showFormula by rememberSaveable { mutableStateOf(false) }
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth().widthIn(max = 400.dp).heightIn(max = 660.dp),
            shape = MaterialTheme.shapes.medium,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.28f))
        ) {
            Column(
                modifier = Modifier.padding(12.dp).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("交易预览", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                MainOrderedResultSection(input = input, result = result)
                SectionPanel(
                    title = "交易参数",
                    trailing = {
                        TextButton(onClick = { showInputDetails = !showInputDetails }) {
                            Text(if (showInputDetails) "收起" else "展开", fontWeight = FontWeight.SemiBold)
                        }
                    }
                ) {
                    if (showInputDetails) {
                        CalculationInputDetails(
                            input = input,
                            symbol = symbol,
                            settlementMode = SettlementMode.UsdtMargined,
                            showSection = false,
                            onShowFeeSettings = onShowFeeSettings
                        )
                    } else {
                        Text(
                            text = "方向、仓位、杠杆、价格与费率",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                SectionPanel(
                    title = "计算公式",
                    trailing = {
                        TextButton(onClick = { showFormula = !showFormula }) {
                            Text(if (showFormula) "收起" else "展开", fontWeight = FontWeight.SemiBold)
                        }
                    }
                ) {
                    if (showFormula) {
                        MetricTile(
                            label = "净盈亏",
                            value = "${formulaOperand(result.grossPnl)} - ${formulaOperand(result.totalFee ?: result.openFee)} = ${DecimalFormatters.formatPositiveNegative(result.netPnl)} USDT"
                        )
                        MetricTile(
                            label = "保证金收益率（ROI）",
                            value = "${formulaOperand(result.netPnl)} / ${formulaOperand(result.requiredMargin)} × 100% = ${DecimalFormatters.formatPositiveNegative(result.roiPercent)}%"
                        )
                    } else {
                        Text(
                            text = "净盈亏与保证金收益率计算过程",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.small
                ) {
                    Text("关闭")
                }
            }
        }
    }
}

@Composable
fun CoinMarginedResultDialog(
    input: CalculationInput,
    result: CoinMarginedResult,
    symbol: String,
    onDismiss: () -> Unit
) {
    var showInputDetails by rememberSaveable { mutableStateOf(false) }
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth().widthIn(max = 400.dp).heightIn(max = 660.dp),
            shape = MaterialTheme.shapes.medium,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.padding(12.dp).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("币本位计算结果", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                CoinMarginedResultCard(result, symbol)
                TextButton(
                    onClick = { showInputDetails = !showInputDetails },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        if (showInputDetails) "收起交易参数 ▲" else "查看交易参数 ▼",
                        fontWeight = FontWeight.SemiBold
                    )
                }
                if (showInputDetails) {
                    CalculationInputDetails(input = input, symbol = symbol, settlementMode = SettlementMode.CoinMargined)
                }
                Button(onClick = onDismiss, modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.small) {
                    Text("关闭")
                }
            }
        }
    }
}

@Composable
private fun MainOrderedResultSection(input: CalculationInput, result: CalculationResult) {
    val palette = LocalProfitLossPalette.current
    val hasTargetStop = input.takeProfitPrice != null || input.stopLossPrice != null
    val leadValue = result.netPnl
    val accent = if (leadValue == null || leadValue >= BigDecimal.ZERO) palette.profit else palette.loss
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.small,
            color = accent.copy(alpha = 0.10f),
            border = BorderStroke(2.dp, accent.copy(alpha = 0.42f))
        ) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (hasTargetStop) {
                    result.netPnl?.let {
                        ResultPrimaryMetric("净盈亏", "${DecimalFormatters.formatPositiveNegative(it)} USDT", pnlColor(it))
                    }
                    ResultMiniMetric(
                        label = "保证金收益率（ROI）",
                        value = pnlText(result.roiPercent, DecimalFormatters.formatPercentage(result.roiPercent)),
                        color = pnlColor(result.netPnl)
                    )
                    result.takeProfitNetPnl?.let {
                        ResultPrimaryMetric("止盈收益", "${DecimalFormatters.formatPositiveNegative(it)} USDT", palette.profit)
                    }
                    result.stopLossNetPnl?.let {
                        ResultPrimaryMetric("止损亏损", "${DecimalFormatters.formatPositiveNegative(it)} USDT", palette.loss)
                    }
                } else {
                    ResultPrimaryMetric(
                        label = "净盈亏",
                        value = "${DecimalFormatters.formatPositiveNegative(result.netPnl)} USDT",
                        color = pnlColor(result.netPnl)
                    )
                }
                if (!hasTargetStop) {
                    ResultMiniMetric(
                        label = "保证金收益率（ROI）",
                        value = pnlText(result.roiPercent, DecimalFormatters.formatPercentage(result.roiPercent)),
                        color = pnlColor(result.netPnl)
                    )
                }
            }
        }
        if (input.estimateLiquidation) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.small,
                color = WarningAmber.copy(alpha = 0.10f),
                border = BorderStroke(1.dp, WarningAmber.copy(alpha = 0.42f))
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("仓位安全边界", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = WarningAmber)
                    ResultInputRow {
                        MetricTile(
                            label = "强平价格",
                            value = result.liquidationPrice?.let {
                                "${DecimalFormatters.formatCurrency(it)} USDT"
                            } ?: if (input.marginMode == MarginMode.Cross && input.totalFunds == null) {
                                "请填写账户总资金"
                            } else {
                                "无正数强平价格"
                            },
                            modifier = Modifier.weight(1f),
                            valueColor = WarningAmber
                        )
                        MetricTile(
                            label = "距离强平",
                            value = result.distanceToLiquidationPercent?.let(DecimalFormatters::formatPercentage) ?: "--",
                            modifier = Modifier.weight(1f),
                            valueColor = WarningAmber
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ResultPrimaryMetric(label: String, value: String, color: Color) {
    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
        Text(label, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(
            text = value,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

@Composable
private fun MainResultSummary(result: CalculationResult) {
    val palette = LocalProfitLossPalette.current
    val netPnl = result.netPnl
    val resultAccent = if (netPnl == null || netPnl >= BigDecimal.ZERO) palette.profit else palette.loss
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.small,
        color = resultAccent.copy(alpha = 0.10f),
        border = BorderStroke(2.dp, resultAccent.copy(alpha = 0.42f))
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("净盈亏", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(
                text = "${pnlText(result.netPnl, DecimalFormatters.formatCurrency(result.netPnl))} USDT",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = resultAccent
            )
            ResultMiniMetric(
                label = "保证金收益率（ROI）",
                value = pnlText(result.roiPercent, DecimalFormatters.formatPercentage(result.roiPercent)),
                color = resultAccent
            )
        }
    }
}

@Composable
private fun MainRiskCostSection(result: CalculationResult) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("强平与费用", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        if (result.liquidationPrice != null) {
            MetricTile(
                label = "强平价格",
                value = "${DecimalFormatters.formatCurrency(result.liquidationPrice)} USDT",
                supporting = if (result.usedTotalFundsForLiquidation) {
                    "已使用总资金参与强平计算；未计其他仓位、手续费、资金费率与阶梯维持保证金"
                } else {
                    "逐仓简化估算，未计手续费、资金费率与阶梯维持保证金"
                },
                valueColor = WarningAmber
            )
        }
        result.distanceToLiquidationPercent?.let {
            MetricTile(
                label = "距离强平",
                value = DecimalFormatters.formatPercentage(it),
                valueColor = WarningAmber
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            MetricTile(
                label = "开仓手续费",
                value = "${DecimalFormatters.formatCurrency(result.openFee)} USDT",
                modifier = Modifier.weight(1f)
            )
            MetricTile(
                label = "平仓手续费",
                value = "${DecimalFormatters.formatCurrency(result.closeFee)} USDT",
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun MainTargetStopSection(input: CalculationInput, result: CalculationResult) {
    val hasTargetOrStop = input.takeProfitPrice != null ||
        input.stopLossPrice != null ||
        result.targetProfitPriceByAmount != null ||
        result.targetProfitPriceByRoi != null ||
        result.stopLossPriceByAmount != null ||
        result.stopLossPriceByRoi != null
    if (!hasTargetOrStop) return

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("止盈/止损", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            MetricTile(
                label = "止盈价",
                value = targetPriceText(input.takeProfitPrice, result.targetProfitPriceByAmount, result.targetProfitPriceByRoi),
                valueColor = MaterialTheme.colorScheme.primary,
                modifier = Modifier.weight(1f)
            )
            MetricTile(
                label = "止盈收益",
                value = result.takeProfitNetPnl?.let { "${DecimalFormatters.formatPositiveNegative(it)} USDT" }
                    ?: targetPriceSupporting(result.targetProfitPriceByAmount, result.targetProfitPriceByRoi)
                    ?: "未计算",
                valueColor = pnlColor(result.takeProfitNetPnl),
                modifier = Modifier.weight(1f)
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            MetricTile(
                label = "止损价",
                value = targetPriceText(input.stopLossPrice, result.stopLossPriceByAmount, result.stopLossPriceByRoi),
                valueColor = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.weight(1f)
            )
            MetricTile(
                label = "止损亏损",
                value = result.stopLossNetPnl?.let { "${DecimalFormatters.formatPositiveNegative(it)} USDT" }
                    ?: targetPriceSupporting(result.stopLossPriceByAmount, result.stopLossPriceByRoi)
                    ?: "未计算",
                valueColor = pnlColor(result.stopLossNetPnl),
                modifier = Modifier.weight(1f)
            )
        }
        result.rewardRiskRatio?.let {
            MetricTile(
                label = "盈亏比",
                value = "1:${DecimalFormatters.formatQuantity(it)}"
            )
        }
    }
}

@Composable
private fun CoinMarginedResultCard(result: CoinMarginedResult, symbol: String) {
    val color = pnlColor(result.pnlCoin)
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.small,
        color = color.copy(alpha = 0.10f),
        border = BorderStroke(2.dp, color.copy(alpha = 0.36f))
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("币本位盈亏", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(
                "${pnlText(result.pnlCoin, DecimalFormatters.formatPositiveNegative(result.pnlCoin))} $symbol",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                "折算价值 ≈ ${pnlText(result.estimatedValueUsdt, DecimalFormatters.formatPositiveNegative(result.estimatedValueUsdt))} USDT",
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                "折算使用当前缓存币种价格，所有计算均在本地完成。",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun CalculationInputDetails(
    input: CalculationInput,
    symbol: String,
    settlementMode: SettlementMode,
    showSection: Boolean = true,
    onShowFeeSettings: (() -> Unit)? = null
) {
    val content: @Composable () -> Unit = {
        Text(
            text = "${input.side.label()} · ${input.marginMode.label()} · ${input.leverage.stripTrailingZeros().toPlainString()}x · ${if (settlementMode == SettlementMode.UsdtMargined) "U 本位" else "币本位"}",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        ResultInputRow {
            MetricTile(
                label = "开仓价",
                value = "${DecimalFormatters.formatCurrency(input.entryPrice)} USDT",
                modifier = Modifier.weight(1f)
            )
            MetricTile(
                label = "平仓价",
                value = "${DecimalFormatters.formatCurrency(input.exitPrice)} USDT",
                modifier = Modifier.weight(1f)
            )
        }
        ResultInputRow {
            MetricTile(
                label = "输入保证金",
                value = input.margin?.let { "${DecimalFormatters.formatCurrency(it)} USDT" } ?: "未填写",
                modifier = Modifier.weight(1f)
            )
            MetricTile(
                label = "输入币数量",
                value = input.quantity?.let { "${DecimalFormatters.formatQuantity(it)} $symbol" } ?: "未填写",
                modifier = Modifier.weight(1f)
            )
        }
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.small,
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.42f),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.10f))
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(start = 12.dp, end = 4.dp, top = 8.dp, bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("费率", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        "${input.openFeeRatePercent.stripTrailingZeros().toPlainString()}% / ${input.closeFeeRatePercent.stripTrailingZeros().toPlainString()}%",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                onShowFeeSettings?.let { onClick ->
                    TextButton(onClick = onClick) {
                        Text("设置费率", fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
        if (input.marginMode == MarginMode.Cross) {
            MetricTile(
                label = "账户总资产",
                value = input.totalFunds?.let { "${DecimalFormatters.formatCurrency(it)} USDT" } ?: "未填写"
            )
        }
    }
    if (showSection) {
        SectionPanel(title = "交易参数") { content() }
    } else {
        content()
    }
}

@Composable
fun CompactExpandableResultCard(
    label: String,
    value: String,
    valueColor: Color,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth().clickable(enabled = enabled, onClick = onClick),
        shape = MaterialTheme.shapes.small,
        color = valueColor.copy(alpha = 0.10f),
        border = BorderStroke(1.dp, valueColor.copy(alpha = 0.38f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(label, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = valueColor)
            }
            Text(
                "查看详情",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun ResultCard(
    input: CalculationInput? = null,
    result: CalculationResult,
    symbol: String = "币"
) {
    val palette = LocalProfitLossPalette.current
    val netPnl = result.netPnl
    val hasTargetOrStop = input?.takeProfitPrice != null ||
        input?.stopLossPrice != null ||
        result.targetProfitPriceByAmount != null ||
        result.targetProfitPriceByRoi != null ||
        result.stopLossPriceByAmount != null ||
        result.stopLossPriceByRoi != null
    val isProfit = netPnl == null || netPnl >= BigDecimal.ZERO
    val resultAccent = if (isProfit) palette.profit else palette.loss
    val resultBackground = resultAccent.copy(alpha = 0.10f)

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.small,
        color = resultBackground,
        border = BorderStroke(2.dp, resultAccent.copy(alpha = 0.42f))
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.small,
                color = resultBackground,
                contentColor = MaterialTheme.colorScheme.onSurface,
                border = BorderStroke(1.dp, resultAccent.copy(alpha = 0.36f))
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = when {
                            netPnl == null && hasTargetOrStop -> "交易预览"
                            netPnl == null -> "等待盈利或亏损结果"
                            isProfit -> "净盈利"
                            else -> "净亏损"
                        },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = resultAccent
                    )
                    Text(
                        text = resultSourceLabel(input, result),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${pnlText(result.netPnl, DecimalFormatters.formatCurrency(result.netPnl))} USDT",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = resultAccent
                    )
                    ResultMiniMetric(
                        label = "保证金收益率（ROI）",
                        value = pnlText(result.roiPercent, DecimalFormatters.formatPercentage(result.roiPercent)),
                        color = resultAccent
                    )
                }
            }
            Text(
                text = "核心结果",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            if (result.liquidationPrice != null) {
                MetricTile(
                label = "强平价格",
                    value = "${DecimalFormatters.formatCurrency(result.liquidationPrice)} USDT",
                    supporting = if (result.usedTotalFundsForLiquidation) {
                        "已使用总资金参与强平计算；未计其他仓位、手续费、资金费率与阶梯维持保证金"
                    } else {
                        "逐仓简化估算，未计手续费、资金费率与阶梯维持保证金"
                    },
                    valueColor = WarningAmber
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MetricTile(
                    label = "开仓手续费估算",
                    value = "${DecimalFormatters.formatCurrency(result.openFee)} USDT",
                    modifier = Modifier.weight(1f)
                )
                MetricTile(
                    label = "平仓手续费估算",
                    value = "${DecimalFormatters.formatCurrency(result.closeFee)} USDT",
                    modifier = Modifier.weight(1f)
                )
            }
            result.distanceToLiquidationPercent?.let {
                MetricTile(
                    label = "距离强平",
                    value = DecimalFormatters.formatPercentage(it),
                    valueColor = WarningAmber
                )
            }
            if (hasTargetOrStop) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MetricTile(
                        label = "止盈价",
                        value = targetPriceText(input?.takeProfitPrice, result.targetProfitPriceByAmount, result.targetProfitPriceByRoi),
                        supporting = result.takeProfitNetPnl?.let { "止盈收益 ${DecimalFormatters.formatPositiveNegative(it)} USDT" }
                            ?: targetPriceSupporting(result.targetProfitPriceByAmount, result.targetProfitPriceByRoi),
                        valueColor = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.weight(1f)
                    )
                    MetricTile(
                        label = "止损价",
                        value = targetPriceText(input?.stopLossPrice, result.stopLossPriceByAmount, result.stopLossPriceByRoi),
                        supporting = result.stopLossNetPnl?.let { "止损亏损 ${DecimalFormatters.formatPositiveNegative(it)} USDT" }
                            ?: targetPriceSupporting(result.stopLossPriceByAmount, result.stopLossPriceByRoi),
                        valueColor = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.weight(1f)
                    )
                }
                result.rewardRiskRatio?.let {
                    MetricTile(
                        label = "盈亏比",
                        value = "1:${DecimalFormatters.formatQuantity(it)}"
                    )
                }
            }
        }
    }
}

@Composable
private fun ResultMiniMetric(label: String, value: String, color: Color) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.SemiBold,
            color = color
        )
    }
}

private fun resultSourceLabel(input: CalculationInput?, result: CalculationResult): String {
    val hasTarget = input?.takeProfitPrice != null ||
        result.targetProfitPriceByAmount != null ||
        result.targetProfitPriceByRoi != null
    val hasStop = input?.stopLossPrice != null ||
        result.stopLossPriceByAmount != null ||
        result.stopLossPriceByRoi != null
    return when {
        hasTarget && hasStop -> "已计算止盈与止损"
        input?.takeProfitPrice != null -> "按止盈价计算"
        input?.stopLossPrice != null -> "按止损价计算"
        result.targetProfitPriceByAmount != null -> "按目标收益反推价计算"
        result.targetProfitPriceByRoi != null -> "按目标收益 ROI 反推价计算"
        result.stopLossPriceByAmount != null -> "按目标亏损反推价计算"
        result.stopLossPriceByRoi != null -> "按目标亏损 ROI 反推价计算"
        result.netPnl != null -> "按平仓价计算"
        else -> "等待平仓价或止盈/止损输入"
    }
}

@Composable
fun MetricTile(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    supporting: String? = null,
    highlight: Boolean = false,
    valueColor: Color? = null
) {
    val resolvedValueColor = valueColor ?: MaterialTheme.colorScheme.onSurface

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.small,
        color = if (highlight) {
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.42f)
        } else {
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.42f)
        },
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.10f))
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = if (highlight) MaterialTheme.typography.titleLarge else MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = resolvedValueColor
            )
            if (supporting != null) {
                Text(
                    text = supporting,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun EmptyResult() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "填写开仓价和保证金后显示仓位，填写平仓价后显示盈亏",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun pnlColor(value: BigDecimal?): Color {
    val palette = LocalProfitLossPalette.current
    return when {
        value == null -> MaterialTheme.colorScheme.onSurface
        value >= BigDecimal.ZERO -> palette.profit
        else -> palette.loss
    }
}

@Composable
fun pnlText(value: BigDecimal?, text: String): String {
    val palette = LocalProfitLossPalette.current
    val indicator = when {
        value == null -> ""
        value >= BigDecimal.ZERO -> palette.profitIndicator
        else -> palette.lossIndicator
    }
    return indicator + text
}

@Composable
private fun ResultInputRow(content: @Composable androidx.compose.foundation.layout.RowScope.() -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        content = content
    )
}

private fun PositionSide.label(): String = if (this == PositionSide.Long) "做多" else "做空"

private fun MarginMode.label(): String = if (this == MarginMode.Cross) "全仓" else "逐仓"

private fun targetPriceText(directPrice: BigDecimal?, amountPrice: BigDecimal?, roiPrice: BigDecimal?): String {
    val price = directPrice ?: amountPrice ?: roiPrice
    return "${DecimalFormatters.formatCurrency(price)} USDT"
}

private fun targetPriceSupporting(amountPrice: BigDecimal?, roiPrice: BigDecimal?): String? {
    return when {
        amountPrice != null && roiPrice != null -> "ROI 价 ${DecimalFormatters.formatCurrency(roiPrice)} USDT"
        amountPrice != null -> "按 USDT 金额反推"
        roiPrice != null -> "按 ROI 反推"
        else -> null
    }
}

private fun formulaOperand(value: BigDecimal?): String {
    val formatted = DecimalFormatters.formatCurrency(value)
    return if (value != null && value < BigDecimal.ZERO) "($formatted)" else formatted
}
