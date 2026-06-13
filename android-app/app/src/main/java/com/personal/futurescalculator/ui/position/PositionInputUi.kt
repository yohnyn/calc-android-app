package com.personal.futurescalculator.ui.position

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.semantics.Role
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
import com.personal.futurescalculator.ui.AppDropdownMenu
import com.personal.futurescalculator.ui.AppDropdownMenuItemPadding
import com.personal.futurescalculator.ui.AppDropdownMenuText
import com.personal.futurescalculator.ui.DropdownChevronIcon
import com.personal.futurescalculator.ui.coin.CoinMarketHeader
import java.math.BigDecimal
import java.math.RoundingMode
import kotlinx.coroutines.delay

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PositionInputSection(
    input: CalculationInput,
    coin: CoinAsset?,
    settlementMode: SettlementMode,
    coinMarginedCalculationMode: CoinMarginedCalculationMode,
    amountInputMode: AmountField,
    targetStopEnabled: Boolean,
    targetPriceExpanded: Boolean,
    targetStopHighlightKey: Int = 0,
    onCoinClick: () -> Unit,
    onClearClick: () -> Unit,
    onSettlementModeChange: (SettlementMode) -> Unit,
    onTargetStopEnabledChange: (Boolean) -> Unit,
    onTargetPriceExpandedChange: (Boolean) -> Unit,
    targetPriceContent: @Composable (() -> Unit)? = null,
    maxOpenContent: @Composable (() -> Unit)? = null,
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
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "仓位参数",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
                TextButton(onClick = onClearClick) {
                    Text("清空参数", fontWeight = FontWeight.SemiBold)
                }
            }
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
                verticalAlignment = Alignment.CenterVertically
            ) {
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

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1.35f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = amountTitle(settlementMode, amountInputMode, coinMarginedCalculationMode),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (settlementMode == SettlementMode.UsdtMargined) {
                        AmountUnitInput(
                            value = amountValue(settlementMode, amountInputMode, input),
                            onValueChange = { onAmountInputChange(amountInputMode, it) },
                            selectedUnit = if (amountInputMode == AmountField.Margin) "USDT" else (coin?.symbol ?: "币"),
                            coinUnit = coin?.symbol ?: "币",
                            expanded = amountMenuExpanded,
                            onExpandedChange = { amountMenuExpanded = it },
                            onUnitSelect = { next ->
                                amountMenuExpanded = false
                                onAmountInputChange(
                                    next,
                                    if (next == AmountField.Margin) input.margin else input.quantity
                                )
                            },
                            onSubmit = onSubmit
                        )
                    } else {
                        AmountUnitInput(
                            value = amountValue(settlementMode, amountInputMode, input),
                            onValueChange = { onAmountInputChange(AmountField.Quantity, it) },
                            selectedUnit = if (coinMarginedCalculationMode == CoinMarginedCalculationMode.CoinQuantity) {
                                coin?.symbol ?: "币"
                            } else {
                                "张"
                            },
                            coinUnit = coin?.symbol ?: "币",
                            expanded = false,
                            onExpandedChange = {},
                            onUnitSelect = {},
                            onSubmit = onSubmit,
                            unitSelectable = false
                        )
                    }
                }
                Column(modifier = Modifier.weight(0.85f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    LeverageSelector(
                        leverage = input.leverage,
                        onLeverageChange = { onInputChange(input.copy(leverage = it)) }
                    )
                }
            }

            if (settlementMode == SettlementMode.UsdtMargined) {
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
                        onSubmit = onSubmit,
                        requirePositive = true,
                        maxDecimalPlaces = 6
                    )
                    Text(
                        text = "→",
                        modifier = Modifier.padding(top = 18.dp),
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
                        onSubmit = onSubmit,
                        requirePositive = true,
                        maxDecimalPlaces = 6
                    )
                }

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    maxItemsInEachRow = 4
                ) {
                    CompactTextToggle(
                        text = "止盈/止损",
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
                        text = "目标价格",
                        checked = targetPriceExpanded,
                        onClick = { onTargetPriceExpandedChange(!targetPriceExpanded) }
                    )
                    CompactTextToggle(
                        text = "强平价格",
                        checked = input.estimateLiquidation,
                        onClick = {
                            onInputChange(input.copy(estimateLiquidation = !input.estimateLiquidation))
                        }
                    )
                    CompactTextToggle(
                        text = "可开",
                        checked = input.calculateMaxOpen,
                        onClick = {
                            onInputChange(input.copy(calculateMaxOpen = !input.calculateMaxOpen))
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
                                onSubmit = onSubmit,
                                requirePositive = true,
                                maxDecimalPlaces = 6
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
                                onSubmit = onSubmit,
                                requirePositive = true,
                                maxDecimalPlaces = 6
                            )
                        }
                    }
                }

                if (targetPriceExpanded && targetPriceContent != null) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.small,
                        color = MaterialTheme.colorScheme.surface
                    ) {
                        Column(
                            modifier = Modifier.padding(0.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            targetPriceContent()
                        }
                    }
                }

                if (input.estimateLiquidation || input.calculateMaxOpen) {
                    Text(
                        text = accountFundsExplanation(input),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (
                    input.calculateMaxOpen ||
                    (input.estimateLiquidation && input.marginMode == MarginMode.Cross)
                ) {
                    NumberInput(
                        value = input.totalFunds,
                        onValueChange = { onInputChange(input.copy(totalFunds = it)) },
                        label = "账户总资金 USDT",
                        onSubmit = onSubmit,
                        requirePositive = true,
                        maxDecimalPlaces = 2
                    )
                }
                if (input.calculateMaxOpen && maxOpenContent != null) {
                    maxOpenContent()
                }
            }
        }
    }
}

private fun accountFundsExplanation(input: CalculationInput): String = when {
    input.estimateLiquidation && input.calculateMaxOpen && input.marginMode == MarginMode.Cross ->
        "账户总资金同时用于全仓强平与最大可开仓位估算"
    input.estimateLiquidation && input.calculateMaxOpen ->
        "逐仓强平为简化估算；账户总资金用于最大可开仓位估算"
    input.estimateLiquidation && input.marginMode == MarginMode.Cross ->
        "全仓强平为简化估算，需要账户总资金；未计其他仓位与资金费率"
    input.estimateLiquidation ->
        "逐仓强平为简化估算；未计阶梯维持保证金、手续费与资金费率"
    else ->
        "账户总资金用于估算最大可开仓位"
}

@Composable
private fun SettlementModeDropdown(
    settlementMode: SettlementMode,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    onSelect: (SettlementMode) -> Unit,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(modifier = modifier) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .clickable { onExpandedChange(true) },
            shape = MaterialTheme.shapes.small,
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.18f))
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 7.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                    Text(
                        text = "结算",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = if (settlementMode == SettlementMode.UsdtMargined) "U 本位" else "币本位",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                DropdownChevronIcon()
            }
        }
        AppDropdownMenu(
            expanded = expanded,
            onDismissRequest = { onExpandedChange(false) },
            modifier = Modifier.width(maxWidth)
        ) {
            DropdownMenuItem(
                text = { AppDropdownMenuText("U 本位") },
                onClick = {
                    onExpandedChange(false)
                    onSelect(SettlementMode.UsdtMargined)
                },
                contentPadding = AppDropdownMenuItemPadding
            )
            DropdownMenuItem(
                text = { AppDropdownMenuText("币本位") },
                onClick = {
                    onExpandedChange(false)
                    onSelect(SettlementMode.CoinMargined)
                },
                contentPadding = AppDropdownMenuItemPadding
            )
        }
    }
}

@Composable
fun AmountUnitInput(
    value: BigDecimal?,
    onValueChange: (BigDecimal?) -> Unit,
    selectedUnit: String,
    coinUnit: String,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    onUnitSelect: (AmountField) -> Unit,
    onSubmit: () -> Unit,
    unitSelectable: Boolean = true
) {
    val displayScale = if (selectedUnit == "USDT") 2 else 6
    val textValue = value.toDisplayText(displayScale)
    var textFieldValue by remember(selectedUnit) { mutableStateOf(TextFieldValue(textValue)) }
    val parsedTextValue = runCatching { BigDecimal(textFieldValue.text) }.getOrNull()
    val inputError = when {
        textFieldValue.text.isBlank() -> null
        parsedTextValue == null -> "请输入有效数字"
        parsedTextValue <= BigDecimal.ZERO -> "请输入大于 0 的数字"
        else -> null
    }
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    LaunchedEffect(value) {
        val updatedValue = value.toDisplayText(displayScale)
        val currentParsedValue = runCatching { BigDecimal(textFieldValue.text) }.getOrNull()
        val currentMatchesValue = currentParsedValue != null &&
            value != null &&
            currentParsedValue.compareTo(value) == 0

        if (value == null && textFieldValue.text.isNotEmpty()) {
            textFieldValue = TextFieldValue("")
        } else if (!currentMatchesValue && updatedValue != textFieldValue.text) {
            textFieldValue = TextFieldValue(updatedValue)
        }
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        BoxWithConstraints {
            Surface(
                modifier = Modifier.fillMaxWidth().height(44.dp),
                shape = MaterialTheme.shapes.small,
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(
                    1.dp,
                    if (inputError != null) MaterialTheme.colorScheme.error
                    else MaterialTheme.colorScheme.outline.copy(alpha = 0.55f)
                )
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(start = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    BasicTextField(
                        value = textFieldValue,
                        onValueChange = {
                            val normalizedText = it.text.trim()
                            val parsedValue = runCatching { BigDecimal(normalizedText) }.getOrNull()
                            if (parsedValue != null && parsedValue.scale() > displayScale) {
                                return@BasicTextField
                            }
                            textFieldValue = it.copy(text = normalizedText)
                            if (normalizedText.isEmpty()) {
                                onValueChange(null)
                                return@BasicTextField
                            }
                            parsedValue?.let(onValueChange)
                        },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Decimal,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                focusManager.clearFocus(force = true)
                                keyboardController?.hide()
                                onSubmit()
                            }
                        ),
                        textStyle = MaterialTheme.typography.titleMedium.copy(
                            color = if (inputError != null) MaterialTheme.colorScheme.error
                            else MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                    Surface(
                        modifier = Modifier.height(22.dp),
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.16f)
                    ) {
                        Box(modifier = Modifier.size(width = 1.dp, height = 22.dp))
                    }
                    Row(
                        modifier = Modifier
                            .height(44.dp)
                            .clickable(enabled = unitSelectable) { onExpandedChange(true) }
                            .padding(horizontal = 10.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = selectedUnit,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (unitSelectable) {
                            DropdownChevronIcon(iconSize = 16.dp)
                        }
                    }
                }
            }
            if (unitSelectable) {
                AppDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { onExpandedChange(false) },
                    modifier = Modifier.width(maxWidth)
                ) {
                    DropdownMenuItem(
                        text = { AppDropdownMenuText("USDT（保证金）") },
                        onClick = {
                            onExpandedChange(false)
                            onUnitSelect(AmountField.Margin)
                        },
                        contentPadding = AppDropdownMenuItemPadding
                    )
                    DropdownMenuItem(
                        text = { AppDropdownMenuText("$coinUnit（币数量）") },
                        onClick = {
                            onExpandedChange(false)
                            onUnitSelect(AmountField.Quantity)
                        },
                        contentPadding = AppDropdownMenuItemPadding
                    )
                }
            }
        }
        if (inputError != null) {
            Text(
                text = inputError,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.error
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
    val interactionSource = remember { MutableInteractionSource() }
    Row(
        modifier = Modifier
            .heightIn(min = 48.dp)
            .toggleable(
                value = checked,
                interactionSource = interactionSource,
                indication = null,
                role = Role.Checkbox,
                onValueChange = { onClick() }
            )
            .padding(horizontal = 4.dp, vertical = 6.dp),
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

private fun BigDecimal?.toDisplayText(scale: Int): String = this
    ?.setScale(scale, RoundingMode.HALF_UP)
    ?.stripTrailingZeros()
    ?.toPlainString()
    ?: ""

private fun globalCoinModeLabel(mode: CoinMarginedCalculationMode): String = when (mode) {
    CoinMarginedCalculationMode.CoinQuantity -> "按币数量"
    CoinMarginedCalculationMode.InverseContract -> "按合约张数"
}

private fun CalculationInput.clearedTargetStop(): CalculationInput = copy(
    takeProfitPrice = null,
    stopLossPrice = null
)
