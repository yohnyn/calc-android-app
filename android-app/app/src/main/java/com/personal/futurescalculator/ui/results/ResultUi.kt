package com.personal.futurescalculator.ui.results

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
    onCopyResult: () -> Unit,
    onSimulateAveraging: () -> Unit,
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
                modifier = Modifier.fillMaxWidth().heightIn(max = 660.dp)
            ) {
                ResultDialogHeader(title = "交易预览", onDismiss = onDismiss)
                Column(
                    modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MainOrderedResultSection(input = input, result = result, symbol = symbol)
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
                                positionMargin = result.requiredMargin,
                                positionQuantity = result.quantity,
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
                    if (result.netPnl != null) {
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
                                    value = "${formulaOperand(result.grossPnl)} - ${formulaOperand(result.totalFee ?: result.openFee)} = ${DecimalFormatters.formatSignedAmount(result.netPnl)} USDT"
                                )
                                MetricTile(
                                    label = "保证金收益率（ROI）",
                                    value = "${formulaOperand(result.netPnl)} / ${formulaOperand(result.requiredMargin)} × 100% = ${DecimalFormatters.formatSignedPercentage(result.roiPercent)}"
                                )
                            } else {
                                Text(
                                    text = "净盈亏与保证金收益率计算过程",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onSimulateAveraging,
                        modifier = Modifier.weight(1f),
                        shape = MaterialTheme.shapes.small
                    ) {
                        Text("模拟补仓")
                    }
                    if (result.netPnl != null) {
                        Button(
                            onClick = onCopyResult,
                            modifier = Modifier.weight(1f),
                            shape = MaterialTheme.shapes.small
                        ) {
                            Text("复制结果")
                        }
                    }
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
                modifier = Modifier.fillMaxWidth().heightIn(max = 660.dp)
            ) {
                ResultDialogHeader(title = "币本位计算结果", onDismiss = onDismiss)
                Column(
                    modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    CoinMarginedResultCard(result, symbol)
                    TextButton(
                        onClick = { showInputDetails = !showInputDetails },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            if (showInputDetails) "收起交易参数" else "查看交易参数",
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    if (showInputDetails) {
                        CalculationInputDetails(input = input, symbol = symbol, settlementMode = SettlementMode.CoinMargined)
                    }
                }
            }
        }
    }
}

@Composable
private fun MainOrderedResultSection(input: CalculationInput, result: CalculationResult, symbol: String) {
    val palette = LocalProfitLossPalette.current
    val hasTargetStop = input.takeProfitPrice != null || input.stopLossPrice != null
    val leadValue = result.netPnl
    val accent = when {
        leadValue == null -> MaterialTheme.colorScheme.primary
        leadValue >= BigDecimal.ZERO -> palette.profit
        else -> palette.loss
    }
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.small,
            color = accent.copy(alpha = 0.10f),
            border = BorderStroke(2.dp, accent.copy(alpha = 0.42f))
        ) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (result.netPnl == null) {
                    Text(
                        text = "仓位预览",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    ResultInputRow {
                        MetricTile(
                            label = "仓位价值",
                            value = "${DecimalFormatters.formatAmount(result.positionValue)} USDT",
                            modifier = Modifier.weight(1f)
                        )
                        MetricTile(
                            label = "仓位币数量",
                            value = "${DecimalFormatters.formatQuantity(result.quantity)} $symbol",
                            modifier = Modifier.weight(1f)
                        )
                    }
                    if (hasTargetStop && (result.takeProfitNetPnl != null || result.stopLossNetPnl != null)) {
                        ResultInputRow {
                            result.takeProfitNetPnl?.let {
                                MetricTile(
                                    label = "止盈收益",
                                    value = "${DecimalFormatters.formatSignedAmount(it)} USDT",
                                    valueColor = palette.profit,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            result.stopLossNetPnl?.let {
                                MetricTile(
                                    label = "止损亏损",
                                    value = "${DecimalFormatters.formatSignedAmount(it)} USDT",
                                    valueColor = palette.loss,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                    Text(
                        text = "等待填写平仓价后计算净盈亏和保证金收益率",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        DetailResultMetric(
                            label = if (result.netPnl < BigDecimal.ZERO) "净亏损" else "净盈利",
                            value = "${DecimalFormatters.formatSignedAmount(result.netPnl)} USDT",
                            color = pnlColor(result.netPnl),
                            modifier = Modifier.weight(1f)
                        )
                        Surface(
                            modifier = Modifier.width(1.dp).height(52.dp),
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.18f)
                        ) {}
                        DetailResultMetric(
                            label = "保证金收益率",
                            value = DecimalFormatters.formatSignedPercentage(result.roiPercent),
                            color = pnlColor(result.netPnl),
                            modifier = Modifier.weight(1f)
                        )
                    }
                    if (result.takeProfitNetPnl != null || result.stopLossNetPnl != null) {
                        ResultInputRow {
                            result.takeProfitNetPnl?.let {
                                MetricTile(
                                    label = "止盈收益",
                                    value = "${DecimalFormatters.formatSignedAmount(it)} USDT",
                                    valueColor = palette.profit,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            result.stopLossNetPnl?.let {
                                MetricTile(
                                    label = "止损亏损",
                                    value = "${DecimalFormatters.formatSignedAmount(it)} USDT",
                                    valueColor = palette.loss,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
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
                                "${DecimalFormatters.formatPrice(it)} USDT"
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
private fun DetailResultMetric(
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = color,
            maxLines = 1
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
                text = "${pnlText(result.netPnl, DecimalFormatters.formatSignedAmount(result.netPnl))} USDT",
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
                value = "${DecimalFormatters.formatPrice(result.liquidationPrice)} USDT",
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
                value = "${DecimalFormatters.formatAmount(result.openFee)} USDT",
                modifier = Modifier.weight(1f)
            )
            MetricTile(
                label = "平仓手续费",
                value = "${DecimalFormatters.formatAmount(result.closeFee)} USDT",
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
                value = result.takeProfitNetPnl?.let { "${DecimalFormatters.formatSignedAmount(it)} USDT" }
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
                value = result.stopLossNetPnl?.let { "${DecimalFormatters.formatSignedAmount(it)} USDT" }
                    ?: targetPriceSupporting(result.stopLossPriceByAmount, result.stopLossPriceByRoi)
                    ?: "未计算",
                valueColor = pnlColor(result.stopLossNetPnl),
                modifier = Modifier.weight(1f)
            )
        }
        result.rewardRiskRatio?.let {
            MetricTile(
                label = "盈亏比",
                value = "1:${DecimalFormatters.formatRatio(it)}"
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
                "${pnlText(result.pnlCoin, DecimalFormatters.formatSignedCoinAmount(result.pnlCoin))} $symbol",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                "折算价值 ≈ ${pnlText(result.estimatedValueUsdt, DecimalFormatters.formatSignedAmount(result.estimatedValueUsdt))} USDT",
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
    positionMargin: BigDecimal? = null,
    positionQuantity: BigDecimal? = null,
    showSection: Boolean = true,
    onShowFeeSettings: (() -> Unit)? = null
) {
    val content: @Composable () -> Unit = {
        Text(
            text = "${input.side.label()} · ${input.marginMode.label()} · ${DecimalFormatters.formatLeverage(input.leverage)}x · ${if (settlementMode == SettlementMode.UsdtMargined) "U 本位" else "币本位"}",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        ResultInputRow {
            MetricTile(
                label = "开仓价",
                value = "${DecimalFormatters.formatPrice(input.entryPrice)} USDT",
                modifier = Modifier.weight(1f)
            )
            MetricTile(
                label = "平仓价",
                value = "${DecimalFormatters.formatPrice(input.exitPrice)} USDT",
                modifier = Modifier.weight(1f)
            )
        }
        ResultInputRow {
            MetricTile(
                label = "仓位保证金",
                value = (positionMargin ?: input.margin)?.let { "${DecimalFormatters.formatAmount(it)} USDT" }
                    ?: "未计算",
                modifier = Modifier.weight(1f)
            )
            MetricTile(
                label = "仓位币数量",
                value = (positionQuantity ?: input.quantity)?.let { "${DecimalFormatters.formatQuantity(it)} $symbol" }
                    ?: "未计算",
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
                        "${DecimalFormatters.formatRate(input.openFeeRatePercent)} / ${DecimalFormatters.formatRate(input.closeFeeRatePercent)}",
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
        if (input.totalFunds != null && (input.marginMode == MarginMode.Cross || input.calculateMaxOpen)) {
            MetricTile(
                label = "账户总资金",
                value = input.totalFunds?.let { "${DecimalFormatters.formatAmount(it)} USDT" } ?: "未填写"
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
private fun ResultDialogHeader(title: String, onDismiss: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(start = 12.dp, end = 4.dp, top = 4.dp, bottom = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        TextButton(onClick = onDismiss) {
            Text("关闭", fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
fun CompactExpandableResultCard(
    primaryLabel: String,
    primaryValue: String,
    primaryColor: Color,
    secondaryLabel: String? = null,
    secondaryValue: String? = null,
    secondaryColor: Color = primaryColor,
    supportingText: String? = null,
    enabled: Boolean = true,
    actionText: String? = null,
    onActionClick: (() -> Unit)? = null,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.small,
        color = primaryColor.copy(alpha = 0.10f),
        border = BorderStroke(1.dp, primaryColor.copy(alpha = 0.38f))
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(enabled = enabled, onClick = onClick)
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CompactResultMetric(
                        label = primaryLabel,
                        value = primaryValue,
                        color = primaryColor,
                        modifier = Modifier.weight(1f)
                    )
                    if (secondaryLabel != null && secondaryValue != null) {
                        Surface(
                            modifier = Modifier.width(1.dp).height(42.dp),
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.18f)
                        ) {}
                        CompactResultMetric(
                            label = secondaryLabel,
                            value = secondaryValue,
                            color = secondaryColor,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                supportingText?.let {
                    Text(
                        text = it,
                        modifier = Modifier.fillMaxWidth(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (actionText != null && onActionClick != null) {
                    ResultCardAction(
                        text = actionText,
                        onClick = onActionClick,
                        modifier = Modifier.weight(1f),
                        emphasized = false
                    )
                }
                ResultCardAction(
                    text = "查看详情",
                    onClick = onClick,
                    modifier = Modifier.weight(1f),
                    emphasized = true
                )
            }
        }
    }
}

@Composable
private fun CompactResultMetric(
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = color,
            maxLines = 1
        )
    }
}

@Composable
private fun ResultCardAction(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier,
    emphasized: Boolean
) {
    Surface(
        modifier = modifier.clickable(onClick = onClick),
        shape = MaterialTheme.shapes.small,
        color = if (emphasized) MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)
        else MaterialTheme.colorScheme.surface.copy(alpha = 0.72f),
        border = BorderStroke(
            1.dp,
            if (emphasized) MaterialTheme.colorScheme.primary.copy(alpha = 0.42f)
            else MaterialTheme.colorScheme.outline.copy(alpha = 0.18f)
        )
    ) {
        Box(modifier = Modifier.padding(vertical = 8.dp), contentAlignment = Alignment.Center) {
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = if (emphasized) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
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
                        text = "${pnlText(result.netPnl, DecimalFormatters.formatSignedAmount(result.netPnl))} USDT",
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
                    value = "${DecimalFormatters.formatPrice(result.liquidationPrice)} USDT",
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
                    value = "${DecimalFormatters.formatAmount(result.openFee)} USDT",
                    modifier = Modifier.weight(1f)
                )
                MetricTile(
                    label = "平仓手续费估算",
                    value = "${DecimalFormatters.formatAmount(result.closeFee)} USDT",
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
                        supporting = result.takeProfitNetPnl?.let { "止盈收益 ${DecimalFormatters.formatSignedAmount(it)} USDT" }
                            ?: targetPriceSupporting(result.targetProfitPriceByAmount, result.targetProfitPriceByRoi),
                        valueColor = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.weight(1f)
                    )
                    MetricTile(
                        label = "止损价",
                        value = targetPriceText(input?.stopLossPrice, result.stopLossPriceByAmount, result.stopLossPriceByRoi),
                        supporting = result.stopLossNetPnl?.let { "止损亏损 ${DecimalFormatters.formatSignedAmount(it)} USDT" }
                            ?: targetPriceSupporting(result.stopLossPriceByAmount, result.stopLossPriceByRoi),
                        valueColor = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.weight(1f)
                    )
                }
                result.rewardRiskRatio?.let {
                    MetricTile(
                        label = "盈亏比",
                        value = "1:${DecimalFormatters.formatRatio(it)}"
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
fun EmptyResult(input: CalculationInput? = null) {
    val hasAmount = input?.margin?.let { it > BigDecimal.ZERO } == true ||
        input?.quantity?.let { it > BigDecimal.ZERO } == true
    val hasEntryPrice = input?.entryPrice?.let { it > BigDecimal.ZERO } == true
    val guidance = when {
        !hasAmount && !hasEntryPrice -> "填写保证金或币数量与开仓价，即可查看仓位预览。"
        !hasAmount -> "请填写保证金或币数量，即可查看仓位预览。"
        !hasEntryPrice -> "请填写开仓价，即可查看仓位预览。"
        else -> "请检查当前输入是否有效。"
    }
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        shape = MaterialTheme.shapes.small,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.42f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.14f))
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Text(
                text = "等待计算仓位",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = guidance,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
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
    return "${DecimalFormatters.formatPrice(price)} USDT"
}

private fun targetPriceSupporting(amountPrice: BigDecimal?, roiPrice: BigDecimal?): String? {
    return when {
        amountPrice != null && roiPrice != null -> "ROI 价 ${DecimalFormatters.formatPrice(roiPrice)} USDT"
        amountPrice != null -> "按 USDT 金额反推"
        roiPrice != null -> "按 ROI 反推"
        else -> null
    }
}

private fun formulaOperand(value: BigDecimal?): String {
    val formatted = DecimalFormatters.formatFormulaAmount(value)
    return if (value != null && value < BigDecimal.ZERO) "($formatted)" else formatted
}
