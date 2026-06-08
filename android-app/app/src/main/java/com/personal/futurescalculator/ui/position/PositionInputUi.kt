package com.personal.futurescalculator.ui.position

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.personal.futurescalculator.model.AmountField
import com.personal.futurescalculator.model.CalculationInput
import com.personal.futurescalculator.model.MarginMode
import com.personal.futurescalculator.model.SettlementMode
import com.personal.futurescalculator.ui.LeverageSelector
import com.personal.futurescalculator.ui.MarginModeSelector
import com.personal.futurescalculator.ui.NumberInput
import com.personal.futurescalculator.ui.PositionSideSelector
import com.personal.futurescalculator.ui.SectionPanel
import java.math.BigDecimal

@Composable
fun PositionInputSection(
    input: CalculationInput,
    settlementMode: SettlementMode,
    symbol: String,
    fullCrossTotalFundsEnabled: Boolean,
    targetStopEnabled: Boolean,
    onFullCrossTotalFundsEnabledChange: (Boolean) -> Unit,
    onTargetStopEnabledChange: (Boolean) -> Unit,
    onInputChange: (CalculationInput) -> Unit,
    onAmountInputChange: (AmountField, BigDecimal?) -> Unit,
    onShowFeeSettings: () -> Unit,
    onSubmit: () -> Unit
) {
    SectionPanel(
        title = "仓位参数",
        trailing = {
            TextButton(onClick = onShowFeeSettings) {
                Text("费率设置")
            }
        }
    ) {
        PositionInputRow {
            PositionSideSelector(
                selectedSide = input.side,
                onSideChange = { onInputChange(input.copy(side = it)) },
                modifier = Modifier.weight(1f)
            )
            MarginModeSelector(
                selectedMode = input.marginMode,
                onModeChange = { onInputChange(input.copy(marginMode = it)) },
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
            if (settlementMode == SettlementMode.UsdtMargined) {
                NumberInput(
                    value = input.margin,
                    onValueChange = { onAmountInputChange(AmountField.Margin, it) },
                    label = "保证金 USDT",
                    modifier = Modifier.weight(1f),
                    onSubmit = onSubmit
                )
            } else {
                NumberInput(
                    value = input.quantity,
                    onValueChange = { onAmountInputChange(AmountField.Quantity, it) },
                    label = "$symbol 数量",
                    modifier = Modifier.weight(1f),
                    onSubmit = onSubmit
                )
            }
        }
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
            onSubmit = onSubmit
        )
        LeverageSelector(
            leverage = input.leverage,
            onLeverageChange = { onInputChange(input.copy(leverage = it)) }
        )
        if (settlementMode == SettlementMode.UsdtMargined && input.marginMode == MarginMode.Cross) {
            InlineOptionRow(
                title = "全仓资产",
                checked = fullCrossTotalFundsEnabled,
                onCheckedChange = { checked ->
                    onFullCrossTotalFundsEnabledChange(checked)
                    if (!checked) {
                        onInputChange(input.copy(totalFunds = null))
                    }
                }
            )
            if (fullCrossTotalFundsEnabled) {
                NumberInput(
                    value = input.totalFunds,
                    onValueChange = { onInputChange(input.copy(totalFunds = it)) },
                    label = "账户总资产 USDT",
                    onSubmit = onSubmit
                )
            }
        }
        if (settlementMode == SettlementMode.UsdtMargined) {
            InlineOptionRow(
                title = "止盈止损",
                checked = targetStopEnabled,
                onCheckedChange = { checked ->
                    onTargetStopEnabledChange(checked)
                    if (!checked) {
                        onInputChange(input.clearedTargetStop())
                    }
                }
            )
            if (targetStopEnabled) {
                PositionInputRow {
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
    }
}

@Composable
private fun InlineOptionRow(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) },
        shape = MaterialTheme.shapes.small,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.42f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.14f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold
            )
            Checkbox(
                checked = checked,
                onCheckedChange = null
            )
        }
    }
}

@Composable
private fun PositionInputRow(content: @Composable RowScope.() -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        content = content
    )
}

private fun CalculationInput.clearedTargetStop(): CalculationInput = copy(
    takeProfitPrice = null,
    stopLossPrice = null,
    targetProfitAmount = null,
    targetRoiPercent = null,
    maxLossAmount = null,
    maxLossRoiPercent = null
)
