package com.personal.futurescalculator.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.personal.futurescalculator.model.AmountField
import com.personal.futurescalculator.model.CalculationResult
import com.personal.futurescalculator.model.ComparisonItem
import com.personal.futurescalculator.model.ComparisonResult
import com.personal.futurescalculator.model.ComparisonRiskLevel
import com.personal.futurescalculator.model.MarginMode
import com.personal.futurescalculator.model.PositionSide
import com.personal.futurescalculator.ui.theme.LossRed
import com.personal.futurescalculator.ui.theme.ProfitGreen
import com.personal.futurescalculator.ui.theme.WarningAmber
import com.personal.futurescalculator.util.DecimalFormatters
import com.personal.futurescalculator.viewmodel.CalculatorViewModel
import java.math.BigDecimal

@Composable
fun CalculatorScreen(
    viewModel: CalculatorViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()
    var feeFieldsUnlocked by remember { mutableStateOf(false) }
    var feeFieldTapCount by remember { mutableIntStateOf(0) }
    var showComparisonDiff by remember { mutableStateOf(false) }
    val unlockFeeFields = {
        if (!feeFieldsUnlocked) {
            feeFieldTapCount += 1
            if (feeFieldTapCount >= 3) {
                feeFieldsUnlocked = true
            }
        }
    }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            SectionPanel(title = "开单参数") {
                PositionSideSelector(
                    selectedSide = uiState.input.side,
                    onSideChange = { viewModel.updateInput(uiState.input.copy(side = it)) }
                )
                MarginModeSelector(
                    selectedMode = uiState.input.marginMode,
                    onModeChange = { viewModel.updateInput(uiState.input.copy(marginMode = it)) }
                )
                LeverageSelector(
                    leverage = uiState.input.leverage,
                    onLeverageChange = { viewModel.updateInput(uiState.input.copy(leverage = it)) }
                )
            }

            SectionPanel(title = "价格与仓位") {
                NumberInput(
                        value = uiState.input.margin,
                        onValueChange = {
                            viewModel.updateLastEditedAmountField(AmountField.Margin)
                        viewModel.updateInput(uiState.input.copy(margin = it, quantity = null))
                    },
                    label = "投入保证金 USDT"
                )
                InputRow {
                    NumberInput(
                        value = uiState.input.entryPrice,
                        onValueChange = { viewModel.updateInput(uiState.input.copy(entryPrice = it)) },
                        label = "开仓价",
                        modifier = Modifier.weight(1f)
                    )
                    NumberInput(
                        value = uiState.input.exitPrice,
                        onValueChange = {
                            viewModel.updateInput(
                                uiState.input.copy(
                                    exitPrice = it,
                                    targetProfitAmount = null,
                                    targetRoiPercent = null,
                                    maxLossAmount = null,
                                    maxLossRoiPercent = null
                                )
                            )
                        },
                        label = "平仓价",
                        modifier = Modifier.weight(1f)
                    )
                }
                NumberInput(
                        value = uiState.input.quantity,
                        onValueChange = {
                            viewModel.updateLastEditedAmountField(AmountField.Quantity)
                        viewModel.updateInput(uiState.input.copy(quantity = it, margin = null))
                    },
                    label = "成交数量 BTC 可选"
                )
                InputRow {
                    NumberInput(
                        value = uiState.input.openFeeRatePercent,
                        onValueChange = { viewModel.updateInput(uiState.input.copy(openFeeRatePercent = it ?: BigDecimal.ZERO)) },
                        label = "开仓费率 %",
                        modifier = Modifier.weight(1f),
                        readOnly = !feeFieldsUnlocked,
                        onReadOnlyTap = unlockFeeFields
                    )
                    NumberInput(
                        value = uiState.input.closeFeeRatePercent,
                        onValueChange = { viewModel.updateInput(uiState.input.copy(closeFeeRatePercent = it ?: BigDecimal.ZERO)) },
                        label = "平仓费率 %",
                        modifier = Modifier.weight(1f),
                        readOnly = !feeFieldsUnlocked,
                        onReadOnlyTap = unlockFeeFields
                    )
                }
                NumberInput(
                    value = uiState.input.maintenanceMarginRatePercent,
                    onValueChange = { viewModel.updateInput(uiState.input.copy(maintenanceMarginRatePercent = it ?: BigDecimal.ZERO)) },
                    label = "维持保证金率 %",
                    readOnly = !feeFieldsUnlocked,
                    onReadOnlyTap = unlockFeeFields
                )
                Text(
                    text = if (feeFieldsUnlocked) "费率设置已解锁" else "开仓、平仓和维持保证金率使用默认值，连续点击任一费率输入框三次可编辑",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            SectionPanel(title = "目标与止损") {
                Text(
                    text = if (uiState.input.exitPrice != null) {
                        "当前按平仓价计算。填写任意目标或止损后，会自动清空平仓价并改为反推价格。"
                    } else {
                        "当前为反推模式。填写平仓价会清空目标与止损输入。"
                    },
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                InputRow {
                    NumberInput(
                        value = uiState.input.targetProfitAmount,
                        onValueChange = { viewModel.updateInput(uiState.input.copy(targetProfitAmount = it, exitPrice = null)) },
                        label = "目标收益 USDT",
                        modifier = Modifier.weight(1f)
                    )
                    NumberInput(
                        value = uiState.input.targetRoiPercent,
                        onValueChange = { viewModel.updateInput(uiState.input.copy(targetRoiPercent = it, exitPrice = null)) },
                        label = "目标 ROI %",
                        modifier = Modifier.weight(1f)
                    )
                }
                InputRow {
                    NumberInput(
                        value = uiState.input.maxLossAmount,
                        onValueChange = { viewModel.updateInput(uiState.input.copy(maxLossAmount = it, exitPrice = null)) },
                        label = "最大亏损 USDT",
                        modifier = Modifier.weight(1f)
                    )
                    NumberInput(
                        value = uiState.input.maxLossRoiPercent,
                        onValueChange = { viewModel.updateInput(uiState.input.copy(maxLossRoiPercent = it, exitPrice = null)) },
                        label = "最大亏损 ROI %",
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            SectionPanel(title = "计算结果") {
                val result = uiState.result
                if (result != null) {
                    ResultCard(result = result)
                } else {
                    EmptyResult()
                }
            }

            SectionPanel(
                title = "对比方案",
                trailing = {
                    Text(
                        text = "${uiState.comparisonItems.size}/3",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            ) {
                if (uiState.comparisonItems.isNotEmpty()) {
                    uiState.comparisonItems.forEachIndexed { index, item ->
                        ComparisonItemCard(
                            item = item,
                            result = uiState.comparisonResults.getOrNull(index)?.result,
                            diff = uiState.comparisonResults.getOrNull(index),
                            showDiff = showComparisonDiff,
                            onChange = { updatedItem -> viewModel.updateComparisonItem(index, updatedItem) },
                            onRemove = { viewModel.removeComparisonItem(index) }
                        )
                    }
                } else {
                    Text(
                        text = "暂无对比方案。复制当前参数后，可以直接在下方方案卡片里修改对比项。",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Button(
                    onClick = { viewModel.copyCurrentToComparison() },
                    enabled = uiState.comparisonItems.size < 3,
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.small
                ) {
                    Text("复制为对比方案")
                }
                if (uiState.comparisonItems.isNotEmpty()) {
                    OutlinedButton(
                        onClick = { showComparisonDiff = !showComparisonDiff },
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.small
                    ) {
                        Text(if (showComparisonDiff) "收起收益差距" else "查看收益差距")
                    }
                }
            }

            OutlinedButton(
                onClick = { viewModel.reset() },
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.small
            ) {
                Text("重置")
            }
        }
    }
}

@Composable
private fun HeaderMiniMetric(label: String, value: String) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.72f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun InputRow(content: @Composable RowScope.() -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        content = content
    )
}

@Composable
fun ResultCard(result: CalculationResult) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.small,
            color = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "净盈亏",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.78f)
                )
                Text(
                    text = "${DecimalFormatters.formatCurrency(result.netPnl)} USDT",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                Row(horizontalArrangement = Arrangement.spacedBy(18.dp)) {
                    HeaderMiniMetric(label = "ROI", value = DecimalFormatters.formatPercentage(result.roiPercent))
                    HeaderMiniMetric(label = "强平价", value = "${DecimalFormatters.formatCurrency(result.liquidationPrice)} USDT")
                }
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            MetricTile(
                label = "仓位价值",
                value = "${DecimalFormatters.formatCurrency(result.positionValue)} USDT",
                modifier = Modifier.weight(1f)
            )
            MetricTile(
                label = "初始保证金",
                value = "${DecimalFormatters.formatCurrency(result.requiredMargin)} USDT",
                modifier = Modifier.weight(1f)
            )
        }
        MetricTile(
            label = "BTC 数量",
            value = "${DecimalFormatters.formatQuantity(result.quantity)} BTC"
        )
        MetricTile(
            label = "估算强平价",
            value = "${DecimalFormatters.formatCurrency(result.liquidationPrice)} USDT",
            supporting = "全仓结果仅为简化估算",
            valueColor = WarningAmber
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            MetricTile(
                label = "未扣手续费盈亏",
                value = "${DecimalFormatters.formatCurrency(result.grossPnl)} USDT",
                modifier = Modifier.weight(1f),
                valueColor = pnlColor(result.grossPnl)
            )
            MetricTile(
                label = "总手续费",
                value = "${DecimalFormatters.formatCurrency(result.totalFee)} USDT",
                supporting = "开 ${DecimalFormatters.formatCurrency(result.openFee)} / 平 ${DecimalFormatters.formatCurrency(result.closeFee)}",
                modifier = Modifier.weight(1f)
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            MetricTile(
                label = "目标收益价",
                value = targetPriceText(result.targetProfitPriceByAmount, result.targetProfitPriceByRoi),
                supporting = targetPriceSupporting(result.targetProfitPriceByAmount, result.targetProfitPriceByRoi),
                valueColor = ProfitGreen,
                modifier = Modifier.weight(1f)
            )
            MetricTile(
                label = "止损价",
                value = targetPriceText(result.stopLossPriceByAmount, result.stopLossPriceByRoi),
                supporting = targetPriceSupporting(result.stopLossPriceByAmount, result.stopLossPriceByRoi),
                valueColor = LossRed,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun MetricTile(
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
private fun EmptyResult() {
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
fun ComparisonItemCard(
    item: ComparisonItem,
    result: CalculationResult?,
    diff: ComparisonResult?,
    showDiff: Boolean,
    onChange: (ComparisonItem) -> Unit,
    onRemove: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.small,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.10f))
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text(
                        text = item.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "${item.input.side.label()} · ${item.input.marginMode.label()} · ${item.input.leverage.stripTrailingZeros().toPlainString()}x",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                TextButton(onClick = onRemove) {
                    Text("删除")
                }
            }

            PositionSideSelector(
                selectedSide = item.input.side,
                onSideChange = { side ->
                    onChange(item.copy(input = item.input.copy(side = side)))
                }
            )
            MarginModeSelector(
                selectedMode = item.input.marginMode,
                onModeChange = { mode ->
                    onChange(item.copy(input = item.input.copy(marginMode = mode)))
                }
            )
            LeverageSelector(
                leverage = item.input.leverage,
                onLeverageChange = { leverage ->
                    onChange(item.copy(input = item.input.copy(leverage = leverage)))
                }
            )
            NumberInput(
                value = item.input.margin,
                onValueChange = { margin ->
                    onChange(
                        item.copy(
                            input = item.input.copy(margin = margin, quantity = null),
                            lastEditedAmountField = AmountField.Margin
                        )
                    )
                },
                label = "投入保证金 USDT"
            )
            InputRow {
                NumberInput(
                    value = item.input.entryPrice,
                    onValueChange = { entryPrice ->
                        onChange(item.copy(input = item.input.copy(entryPrice = entryPrice)))
                    },
                    label = "开仓价",
                    modifier = Modifier.weight(1f)
                )
                NumberInput(
                    value = item.input.exitPrice,
                    onValueChange = { exitPrice ->
                        onChange(item.copy(input = item.input.copy(exitPrice = exitPrice)))
                    },
                    label = "平仓价",
                    modifier = Modifier.weight(1f)
                )
            }
            NumberInput(
                value = item.input.quantity,
                onValueChange = { quantity ->
                    onChange(
                        item.copy(
                            input = item.input.copy(quantity = quantity, margin = null),
                            lastEditedAmountField = AmountField.Quantity
                        )
                    )
                },
                label = "成交数量 BTC 可选"
            )

            if (result != null) {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                    MetricTile(
                        label = "净盈亏",
                        value = "${DecimalFormatters.formatCurrency(result.netPnl)} USDT",
                        valueColor = pnlColor(result.netPnl),
                        modifier = Modifier.weight(1f)
                    )
                    MetricTile(
                        label = "ROI",
                        value = DecimalFormatters.formatPercentage(result.roiPercent),
                        valueColor = pnlColor(result.roiPercent),
                        modifier = Modifier.weight(1f)
                    )
                }
                if (showDiff) {
                    ComparisonDiffCard(diff = diff)
                }
            } else {
                Text(
                    text = "计算结果不可用",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun ComparisonDiffCard(diff: ComparisonResult?) {
    val netDiff = diff?.netPnlDiff
    val accentColor = pnlColor(netDiff)
    val backgroundColor = when {
        netDiff == null -> MaterialTheme.colorScheme.surface
        netDiff >= BigDecimal.ZERO -> ProfitGreen.copy(alpha = 0.14f)
        else -> LossRed.copy(alpha = 0.14f)
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.small,
        color = backgroundColor,
        border = BorderStroke(1.dp, accentColor.copy(alpha = 0.32f))
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "收益差距",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "${DecimalFormatters.formatPositiveNegative(netDiff)} USDT",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = accentColor
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "ROI 差值",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "${DecimalFormatters.formatPositiveNegative(diff?.roiDiffPercent)}%",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = pnlColor(diff?.roiDiffPercent)
                )
            }
            Text(
                text = riskLabel(diff?.riskLevel),
                style = MaterialTheme.typography.labelMedium,
                color = when (diff?.riskLevel) {
                    ComparisonRiskLevel.HigherRisk -> LossRed
                    ComparisonRiskLevel.LowerRisk -> ProfitGreen
                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
        }
    }
}

@Composable
private fun pnlColor(value: BigDecimal?): androidx.compose.ui.graphics.Color {
    return when {
        value == null -> MaterialTheme.colorScheme.onSurface
        value >= BigDecimal.ZERO -> ProfitGreen
        else -> LossRed
    }
}

private fun PositionSide.label(): String = if (this == PositionSide.Long) "做多" else "做空"

private fun MarginMode.label(): String = if (this == MarginMode.Cross) "全仓" else "逐仓"

private fun targetPriceText(amountPrice: BigDecimal?, roiPrice: BigDecimal?): String {
    val price = amountPrice ?: roiPrice
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

private fun riskLabel(level: ComparisonRiskLevel?): String {
    return when (level) {
        ComparisonRiskLevel.LowerRisk -> "风险：距离强平更远"
        ComparisonRiskLevel.HigherRisk -> "风险：更接近强平"
        ComparisonRiskLevel.SimilarRisk -> "风险：接近当前方案"
        else -> "风险：暂无可比数据"
    }
}
