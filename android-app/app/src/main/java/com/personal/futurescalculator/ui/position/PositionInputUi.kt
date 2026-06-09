package com.personal.futurescalculator.ui.position

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import com.personal.futurescalculator.model.CoinAsset
import com.personal.futurescalculator.model.CoinMarginedCalculationMode
import com.personal.futurescalculator.model.MarginMode
import com.personal.futurescalculator.model.SettlementMode
import com.personal.futurescalculator.ui.LeverageSelector
import com.personal.futurescalculator.ui.MarginModeSelector
import com.personal.futurescalculator.ui.NumberInput
import com.personal.futurescalculator.ui.PositionSideSelector
import com.personal.futurescalculator.ui.coin.CoinMarketHeader
import java.math.BigDecimal
import kotlinx.coroutines.delay

@Composable
fun PositionInputSection(
    input: CalculationInput,
    coin: CoinAsset?,
    settlementMode: SettlementMode,
    coinMarginedCalculationMode: CoinMarginedCalculationMode,
    amountInputMode: AmountField,
    targetStopEnabled: Boolean,
    targetStopHighlightKey: Int = 0,
    onCoinClick: () -> Unit,
    onSettlementModeChange: (SettlementMode) -> Unit,
    onTargetStopEnabledChange: (Boolean) -> Unit,
    onInputChange: (CalculationInput) -> Unit,
    onAmountInputChange: (AmountField, BigDecimal?) -> Unit,
    onSubmit: () -> Unit
) {
    var targetStopHighlighted by remember { mutableStateOf(false) }
    var amountMenuExpanded by remember { mutableStateOf(false) }
    var settlementMenuExpanded by remember { mutableStateOf(false) }

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
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CoinMarketHeader(
                    coin = coin,
                    onClick = onCoinClick,
                    modifier = Modifier.weight(1f)
                )
                SettlementModeDropdown(
                    settlementMode = settlementMode,
                    expanded = settlementMenuExpanded,
                    onExpandedChange = { settlementMenuExpanded = it },
                    onSelect = onSettlementModeChange,
                    modifier = Modifier.weight(1f)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = amountTitle(settlementMode, amountInputMode, coinMarginedCalculationMode),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (settlementMode == SettlementMode.UsdtMargined) {
                            AmountModeDropdownChip(
                                text = if (amountInputMode == AmountField.Margin) "按保证金" else "按币数量",
                                expanded = amountMenuExpanded,
                                onExpandedChange = { amountMenuExpanded = it },
                                onSelect = { next ->
                                    amountMenuExpanded = false
                                    onAmountInputChange(
                                        next,
                                        if (next == AmountField.Margin) input.margin else input.quantity
                                    )
                                }
                            )
                        } else {
                            Text(
                                text = globalCoinModeLabel(coinMarginedCalculationMode),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    NumberInput(
                        value = amountValue(settlementMode, amountInputMode, input),
                        onValueChange = {
                            onAmountInputChange(
                                if (settlementMode == SettlementMode.UsdtMargined && amountInputMode == AmountField.Margin) {
                                    AmountField.Margin
                                } else {
                                    AmountField.Quantity
                                },
                                it
                            )
                        },
                        label = amountFieldLabel(settlementMode, amountInputMode, coinMarginedCalculationMode),
                        onSubmit = onSubmit
                    )
                }
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "杠杆",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.small,
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.52f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.14f))
                    ) {
                        Box(
                            modifier = Modifier.height(40.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${input.leverage.stripTrailingZeros().toPlainString()}x",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            LeverageSelector(
                leverage = input.leverage,
                onLeverageChange = { onInputChange(input.copy(leverage = it)) }
            )

            if (settlementMode == SettlementMode.UsdtMargined) {
                Text(
                    text = "价格",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    NumberInput(
                        value = input.entryPrice,
                        onValueChange = { onInputChange(input.copy(entryPrice = it)) },
                        label = "开仓价",
                        modifier = Modifier.weight(1f),
                        onSubmit = onSubmit
                    )
                    Text(
                        text = "→",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
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

                Row(
                    modifier = Modifier.fillMaxWidth().height(32.dp),
                    horizontalArrangement = Arrangement.spacedBy(28.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CompactTextToggle(
                        text = "止盈止损",
                        checked = targetStopEnabled,
                        onClick = {
                            val checked = !targetStopEnabled
                            onTargetStopEnabledChange(checked)
                            if (!checked) {
                                onInputChange(input.clearedTargetStop())
                            }
                        }
                    )
                    CompactTextToggle(
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
                        }
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
                    NumberInput(
                        value = input.totalFunds,
                        onValueChange = { onInputChange(input.copy(totalFunds = it)) },
                        label = "账户总资产 USDT",
                        onSubmit = onSubmit
                    )
                }
            }
        }
    }
}

@Composable
private fun SettlementModeDropdown(
    settlementMode: SettlementMode,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    onSelect: (SettlementMode) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onExpandedChange(true) },
            shape = MaterialTheme.shapes.small,
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.52f),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.14f))
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (settlementMode == SettlementMode.UsdtMargined) "U 本位" else "币本位",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text("˅", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { onExpandedChange(false) }) {
            DropdownMenuItem(
                text = { Text("U 本位") },
                onClick = {
                    onExpandedChange(false)
                    onSelect(SettlementMode.UsdtMargined)
                }
            )
            DropdownMenuItem(
                text = { Text("币本位") },
                onClick = {
                    onExpandedChange(false)
                    onSelect(SettlementMode.CoinMargined)
                }
            )
        }
    }
}

@Composable
private fun AmountModeDropdownChip(
    text: String,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    onSelect: (AmountField) -> Unit
) {
    Box {
        Surface(
            modifier = Modifier.clickable { onExpandedChange(true) },
            shape = MaterialTheme.shapes.small,
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.52f),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.14f))
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = text, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold)
                Text("˅", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { onExpandedChange(false) }) {
            DropdownMenuItem(
                text = { Text("按保证金") },
                onClick = {
                    onExpandedChange(false)
                    onSelect(AmountField.Margin)
                }
            )
            DropdownMenuItem(
                text = { Text("按币数量") },
                onClick = {
                    onExpandedChange(false)
                    onSelect(AmountField.Quantity)
                }
            )
        }
    }
}

@Composable
private fun CompactTextToggle(
    text: String,
    checked: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier.clickable(onClick = onClick),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = if (checked) "☑" else "☐",
            style = MaterialTheme.typography.labelLarge,
            color = if (checked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = if (checked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
        )
    }
}

private fun amountTitle(
    settlementMode: SettlementMode,
    amountInputMode: AmountField,
    coinMarginedCalculationMode: CoinMarginedCalculationMode
): String = when (settlementMode) {
    SettlementMode.UsdtMargined -> if (amountInputMode == AmountField.Margin) "保证金" else "币数量"
    SettlementMode.CoinMargined -> if (coinMarginedCalculationMode == CoinMarginedCalculationMode.CoinQuantity) "币数量" else "合约张数"
}

private fun amountFieldLabel(
    settlementMode: SettlementMode,
    amountInputMode: AmountField,
    coinMarginedCalculationMode: CoinMarginedCalculationMode
): String = when (settlementMode) {
    SettlementMode.UsdtMargined -> if (amountInputMode == AmountField.Margin) "保证金 USDT" else "币数量"
    SettlementMode.CoinMargined -> if (coinMarginedCalculationMode == CoinMarginedCalculationMode.CoinQuantity) "币数量" else "合约张数"
}

private fun amountValue(
    settlementMode: SettlementMode,
    amountInputMode: AmountField,
    input: CalculationInput
): BigDecimal? = when (settlementMode) {
    SettlementMode.UsdtMargined -> if (amountInputMode == AmountField.Margin) input.margin else input.quantity
    SettlementMode.CoinMargined -> input.quantity
}

private fun globalCoinModeLabel(mode: CoinMarginedCalculationMode): String = when (mode) {
    CoinMarginedCalculationMode.CoinQuantity -> "按币数量"
    CoinMarginedCalculationMode.InverseContract -> "按合约张数"
}

private fun CalculationInput.clearedTargetStop(): CalculationInput = copy(
    takeProfitPrice = null,
    stopLossPrice = null
)
