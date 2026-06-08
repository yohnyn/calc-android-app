package com.personal.futurescalculator.ui.position

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.personal.futurescalculator.model.AmountField
import com.personal.futurescalculator.model.CalculationInput
import com.personal.futurescalculator.model.CalculationResult
import com.personal.futurescalculator.model.MarginMode
import com.personal.futurescalculator.model.SettlementMode
import com.personal.futurescalculator.ui.LeverageSelector
import com.personal.futurescalculator.ui.MarginModeSelector
import com.personal.futurescalculator.ui.NumberInput
import com.personal.futurescalculator.ui.PositionSideSelector
import com.personal.futurescalculator.ui.theme.LocalProfitLossPalette
import com.personal.futurescalculator.util.DecimalFormatters
import java.math.BigDecimal
import kotlinx.coroutines.delay

@Composable
fun PositionInputSection(
    input: CalculationInput,
    result: CalculationResult?,
    settlementMode: SettlementMode,
    symbol: String,
    targetStopEnabled: Boolean,
    targetStopHighlightKey: Int = 0,
    onTargetStopEnabledChange: (Boolean) -> Unit,
    onInputChange: (CalculationInput) -> Unit,
    onAmountInputChange: (AmountField, BigDecimal?) -> Unit,
    onShowFeeSettings: () -> Unit,
    onResultClick: () -> Unit,
    onSubmit: () -> Unit
) {
    var targetStopHighlighted by remember { mutableStateOf(false) }
    LaunchedEffect(targetStopHighlightKey) {
        if (targetStopHighlightKey > 0) {
            targetStopHighlighted = true
            delay(1200)
            targetStopHighlighted = false
        }
    }
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.small,
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.16f))
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("仓位参数", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
                Text(
                    modifier = Modifier.clickable(onClick = onShowFeeSettings),
                    text = "费率设置",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            PositionInputRow {
                PositionSideSelector(
                    selectedSide = input.side,
                    onSideChange = { onInputChange(input.copy(side = it)) },
                    modifier = Modifier.weight(1f)
                )
                MarginModeSelector(
                    selectedMode = input.marginMode,
                    onModeChange = { mode ->
                        if (mode == MarginMode.Isolated) {
                            onInputChange(input.copy(marginMode = mode, totalFunds = null))
                        } else {
                            onInputChange(input.copy(marginMode = mode))
                        }
                    },
                    modifier = Modifier.weight(1f)
                )
            }
            PositionInputRow {
                NumberInput(
                    value = input.entryPrice,
                    onValueChange = { onInputChange(input.copy(entryPrice = it)) },
                    label = "开仓价",
                    modifier = Modifier.weight(1f),
                    onSubmit = onSubmit
                )
                NumberInput(
                    value = input.exitPrice,
                    onValueChange = {
                        onInputChange(
                            input.copy(
                                exitPrice = it,
                                targetProfitAmount = null,
                                targetRoiPercent = null,
                                maxLossAmount = null,
                                maxLossRoiPercent = null
                            )
                        )
                    },
                    label = "平仓价",
                    modifier = Modifier.weight(1f),
                    onSubmit = onSubmit
                )
            }
            PositionInputRow {
                if (settlementMode == SettlementMode.UsdtMargined) {
                    NumberInput(
                        value = input.margin,
                        onValueChange = { onAmountInputChange(AmountField.Margin, it) },
                        label = "保证金 USDT",
                        modifier = Modifier.weight(1f),
                        onSubmit = onSubmit
                    )
                }
                NumberInput(
                    value = input.quantity,
                    onValueChange = { onAmountInputChange(AmountField.Quantity, it) },
                    label = "$symbol 数量",
                    modifier = Modifier.weight(1f),
                    onSubmit = onSubmit
                )
            }
            if (settlementMode == SettlementMode.UsdtMargined) {
                PositionInputRow {
                    CompactToggleChip(
                        text = "止盈止损",
                        checked = targetStopEnabled,
                        onClick = {
                            val checked = !targetStopEnabled
                            onTargetStopEnabledChange(checked)
                            if (!checked) {
                                onInputChange(input.clearedTargetStop())
                            }
                        },
                        modifier = Modifier.weight(1f)
                    )
                    CompactToggleChip(
                        text = "估算强平价",
                        checked = input.estimateLiquidation,
                        onClick = {
                            val checked = !input.estimateLiquidation
                            onInputChange(
                                input.copy(
                                    estimateLiquidation = checked,
                                    totalFunds = if (checked) input.totalFunds else null
                                )
                            )
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
                if (targetStopEnabled) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.small,
                        color = MaterialTheme.colorScheme.surface,
                        border = if (targetStopHighlighted) {
                            BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.72f))
                        } else {
                            BorderStroke(0.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0f))
                        }
                    ) {
                        Row(
                            modifier = Modifier.padding(if (targetStopHighlighted) 6.dp else 0.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            NumberInput(
                                value = input.takeProfitPrice,
                                onValueChange = {
                                    onInputChange(
                                        input.copy(
                                            takeProfitPrice = it,
                                            targetProfitAmount = null,
                                            targetRoiPercent = null
                                        )
                                    )
                                },
                                label = "止盈价",
                                modifier = Modifier.weight(1f),
                                onSubmit = onSubmit
                            )
                            NumberInput(
                                value = input.stopLossPrice,
                                onValueChange = {
                                    onInputChange(
                                        input.copy(
                                            stopLossPrice = it,
                                            maxLossAmount = null,
                                            maxLossRoiPercent = null
                                        )
                                    )
                                },
                                label = "止损价",
                                modifier = Modifier.weight(1f),
                                onSubmit = onSubmit
                            )
                        }
                    }
                }
                if (input.estimateLiquidation && input.marginMode == MarginMode.Cross) {
                    PositionInputRow {
                        NumberInput(
                            value = input.totalFunds,
                            onValueChange = { onInputChange(input.copy(totalFunds = it)) },
                            label = "账户总资产 USDT",
                            modifier = Modifier.weight(1f),
                            onSubmit = onSubmit
                        )
                        Button(
                            onClick = { onInputChange(input.copy(totalFunds = input.margin)) },
                            enabled = input.margin != null && input.margin > BigDecimal.ZERO,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("使用保证金")
                        }
                    }
                }
            }
            LeverageSelector(
                leverage = input.leverage,
                onLeverageChange = { onInputChange(input.copy(leverage = it)) }
            )
            result?.let {
                PositionCompactResult(
                    input = input,
                    result = it,
                    targetStopEnabled = targetStopEnabled,
                    onClick = onResultClick
                )
            }
        }
    }
}

@Composable
private fun CompactToggleChip(
    text: String,
    checked: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = MaterialTheme.shapes.small,
        color = if (checked) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.42f)
            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.42f),
        border = BorderStroke(
            1.dp,
            if (checked) MaterialTheme.colorScheme.primary.copy(alpha = 0.52f)
            else MaterialTheme.colorScheme.outline.copy(alpha = 0.16f)
        )
    ) {
        Text(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
            text = "${if (checked) "☑" else "☐"} $text",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = if (checked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun PositionCompactResult(
    input: CalculationInput,
    result: CalculationResult,
    targetStopEnabled: Boolean,
    onClick: () -> Unit
) {
    val hasPnl = result.netPnl != null || result.takeProfitNetPnl != null || result.stopLossNetPnl != null
    if (!hasPnl) return
    val hasTargetStop = targetStopEnabled && (input.takeProfitPrice != null || input.stopLossPrice != null)
    val palette = LocalProfitLossPalette.current
    val primaryValue = when {
        hasTargetStop && result.takeProfitNetPnl != null -> result.takeProfitNetPnl
        else -> result.netPnl
    }
    val accent = if (primaryValue == null || primaryValue >= BigDecimal.ZERO) palette.profit else palette.loss
    Surface(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = MaterialTheme.shapes.small,
        color = accent.copy(alpha = 0.10f),
        border = BorderStroke(1.dp, accent.copy(alpha = 0.36f))
    ) {
        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            if (hasTargetStop) {
                result.takeProfitNetPnl?.let { PositionResultMetric("止盈收益", it, accent) }
                result.stopLossNetPnl?.let { PositionResultMetric("止损亏损", it, LocalProfitLossPalette.current.loss) }
                PositionResultMetric("当前平仓收益", result.netPnl, accent)
                PositionResultText("收益率", DecimalFormatters.formatPercentage(result.roiPercent))
                if (input.estimateLiquidation) {
                    result.liquidationPrice?.let { PositionResultText("估算强平价", "${DecimalFormatters.formatCurrency(it)} USDT") }
                }
                PositionResultText("手续费", "${DecimalFormatters.formatCurrency(result.totalFee ?: result.openFee)} USDT")
            } else {
                PositionResultMetric("预计收益", result.netPnl, accent)
                if (input.estimateLiquidation) {
                    result.liquidationPrice?.let { PositionResultText("估算强平价", "${DecimalFormatters.formatCurrency(it)} USDT") }
                    result.distanceToLiquidationPercent?.let { PositionResultText("距离强平", DecimalFormatters.formatPercentage(it)) }
                }
                PositionResultText("收益率", DecimalFormatters.formatPercentage(result.roiPercent))
                PositionResultText("手续费", "${DecimalFormatters.formatCurrency(result.totalFee ?: result.openFee)} USDT")
            }
        }
    }
}

@Composable
private fun PositionResultMetric(label: String, value: BigDecimal?, color: androidx.compose.ui.graphics.Color) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(
            "${DecimalFormatters.formatPositiveNegative(value)} USDT",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = if (value == null || value >= BigDecimal.ZERO) color else LocalProfitLossPalette.current.loss
        )
    }
}

@Composable
private fun PositionResultText(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun PositionInputRow(content: @Composable RowScope.() -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        content = content
    )
}

private fun CalculationInput.clearedTargetStop(): CalculationInput = copy(
    takeProfitPrice = null,
    stopLossPrice = null
)
