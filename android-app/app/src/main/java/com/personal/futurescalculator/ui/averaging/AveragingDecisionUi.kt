package com.personal.futurescalculator.ui.averaging

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.personal.futurescalculator.model.AveragingDecisionInput
import com.personal.futurescalculator.model.AveragingDecisionResult
import com.personal.futurescalculator.model.AmountField
import com.personal.futurescalculator.model.CalculationInput
import com.personal.futurescalculator.model.CalculationResult
import com.personal.futurescalculator.model.PositionSide
import com.personal.futurescalculator.ui.DropdownChevronIcon
import com.personal.futurescalculator.ui.LeverageSelector
import com.personal.futurescalculator.ui.NumberInput
import com.personal.futurescalculator.ui.PositionSideSelector
import com.personal.futurescalculator.ui.SectionPanel
import com.personal.futurescalculator.ui.position.AmountUnitInput
import com.personal.futurescalculator.ui.theme.LocalProfitLossPalette
import com.personal.futurescalculator.util.DecimalFormatters
import java.math.BigDecimal
import java.math.RoundingMode

data class ExistingScheme(
    val name: String,
    val symbol: String,
    val input: CalculationInput,
    val result: CalculationResult
)

fun averagingMissingFields(input: AveragingDecisionInput): List<String> = buildList {
    if (input.currentEntryPrice == null || input.currentEntryPrice <= BigDecimal.ZERO) add("当前均价")
    if (input.currentQuantity == null || input.currentQuantity <= BigDecimal.ZERO) add("当前币数量")
    if (input.currentLeverage == null || input.currentLeverage <= BigDecimal.ZERO) add("当前杠杆")
    if (input.addEntryPrice == null || input.addEntryPrice <= BigDecimal.ZERO) add("补仓价格")
    if (
        (input.addAmount == null || input.addAmount <= BigDecimal.ZERO) &&
        (input.addQuantity == null || input.addQuantity <= BigDecimal.ZERO)
    ) {
        add("补仓金额或补仓数量")
    }
}

@Composable
fun AveragingDecisionEntryCard(
    input: AveragingDecisionInput,
    result: AveragingDecisionResult?,
    symbol: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = MaterialTheme.shapes.small,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.18f))
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "补仓助手",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "展开",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Box(contentAlignment = Alignment.Center) {
                        DropdownChevronIcon(iconSize = 16.dp)
                    }
                }
            }
            Text(
                text = when {
                    result != null -> "补仓后均价 ${DecimalFormatters.formatCurrency(result.newAveragePrice)} · 新增保证金 ${DecimalFormatters.formatCurrency(result.addAmount)} USDT"
                    input.currentEntryPrice != null -> "$symbol · ${input.side.label()} · ${DecimalFormatters.formatQuantity(input.currentLeverage)}x · 均价 ${DecimalFormatters.formatCurrency(input.currentEntryPrice)}"
                    else -> "模拟补仓后的均价、资金与仓位变化"
                },
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun AveragingDecisionSection(
    input: AveragingDecisionInput,
    result: AveragingDecisionResult?,
    schemes: List<ExistingScheme>,
    symbol: String,
    modifier: Modifier = Modifier,
    onInputChange: (AveragingDecisionInput) -> Unit,
    onCollapse: () -> Unit,
    onSchemeFilled: (ExistingScheme) -> Unit,
    onManualPositionStarted: () -> Unit,
    onRequestResult: () -> Unit
) {
    var showSchemeDialog by remember { mutableStateOf(false) }
    var positionEditorInitial by remember { mutableStateOf<AveragingDecisionInput?>(null) }
    var useAddQuantity by rememberSaveable { mutableStateOf(input.addQuantity != null) }

    if (showSchemeDialog) {
        SchemeSelectionDialog(
            schemes = schemes,
            onSelect = { scheme ->
                onInputChange(
                    input.copy(
                        side = scheme.input.side,
                        currentEntryPrice = scheme.input.entryPrice,
                        currentQuantity = scheme.result.quantity,
                        currentMargin = scheme.result.requiredMargin,
                        currentLeverage = scheme.input.leverage
                    )
                )
                onSchemeFilled(scheme)
                showSchemeDialog = false
            },
            onDismiss = { showSchemeDialog = false }
        )
    }
    positionEditorInitial?.let { editorInput ->
        AveragingPositionEditorDialog(
            initialInput = editorInput,
            symbol = symbol,
            onSave = {
                onInputChange(it)
                positionEditorInitial = null
            },
            onDismiss = { positionEditorInitial = null }
        )
    }

    SectionPanel(
        title = "补仓助手",
        modifier = modifier,
        trailing = {
            TextButton(
                onClick = onCollapse,
                colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("收起", fontWeight = FontWeight.SemiBold)
            }
        }
    ) {
        CurrentPositionCard(
            input = input,
            symbol = symbol,
            onEdit = { positionEditorInitial = input },
            onClear = {
                onInputChange(input.clearedCurrentPosition())
                onManualPositionStarted()
            }
        )
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            AveragingSoftOutlinedButton(
                onClick = {
                    onManualPositionStarted()
                    positionEditorInitial = input.clearedCurrentPosition()
                },
                modifier = Modifier.weight(1f)
            ) {
                Text("手动创建")
            }
            AveragingSoftOutlinedButton(
                onClick = { showSchemeDialog = true },
                modifier = Modifier.weight(1f),
                enabled = schemes.isNotEmpty()
            ) {
                Text("从方案库选择")
            }
        }
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("补仓设置", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            NumberInput(
                value = input.addEntryPrice,
                onValueChange = { onInputChange(input.copy(addEntryPrice = it)) },
                label = "补仓价格"
            )
            AveragingAmountModeSelector(
                useQuantity = useAddQuantity,
                onUseAmount = {
                    useAddQuantity = false
                    onInputChange(input.copy(addQuantity = null))
                },
                onUseQuantity = {
                    useAddQuantity = true
                    onInputChange(input.copy(addAmount = null))
                }
            )
            if (useAddQuantity) {
                NumberInput(
                    value = input.addQuantity,
                    onValueChange = { onInputChange(input.copy(addQuantity = it, addAmount = null)) },
                    label = "补仓币数量 $symbol"
                )
            } else {
                Text(
                    text = "按当前保证金比例",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                AveragingRatioSelector(
                    currentMargin = input.currentMargin,
                    selectedAmount = input.addAmount,
                    onSelect = { onInputChange(input.copy(addAmount = it, addQuantity = null)) }
                )
                NumberInput(
                    value = input.addAmount,
                    onValueChange = { onInputChange(input.copy(addAmount = it, addQuantity = null)) },
                    label = "补仓金额 USDT"
                )
                input.addAmount?.takeIf { it > BigDecimal.ZERO }?.let { amount ->
                    val currentMargin = input.currentMargin
                    val totalMargin = currentMargin?.plus(amount)
                    val multiple = if (currentMargin != null && currentMargin > BigDecimal.ZERO) {
                        totalMargin?.divide(currentMargin, 4, RoundingMode.HALF_UP)
                    } else {
                        null
                    }
                    Text(
                        text = buildString {
                            append("本次增加 ${DecimalFormatters.formatCurrency(amount)} USDT")
                            totalMargin?.let { append(" · 总保证金 ${DecimalFormatters.formatCurrency(it)} USDT") }
                            multiple?.let { append(" · 约 ${it.stripTrailingZeros().toPlainString()} 倍") }
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        result?.let {
            AveragingInlinePreview(input = input, result = it, symbol = symbol)
        }

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("收益模拟（可选）", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(
                text = "填写目标平仓价后，对比补仓前后的目标价收益。",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            NumberInput(
                value = input.targetExitPrice,
                onValueChange = { onInputChange(input.copy(targetExitPrice = it)) },
                label = "目标平仓价"
            )
            Button(
                onClick = onRequestResult,
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.small,
                enabled = result != null
            ) {
                Text("查看完整补仓结果")
            }
        }

    }
}

private fun AveragingDecisionInput.clearedCurrentPosition(): AveragingDecisionInput = copy(
    side = PositionSide.Long,
    currentEntryPrice = null,
    currentQuantity = null,
    currentMargin = null,
    currentLeverage = null
)

@Composable
private fun CurrentPositionCard(
    input: AveragingDecisionInput,
    symbol: String,
    onEdit: () -> Unit,
    onClear: () -> Unit
) {
    val hasCurrentPositionInput = input.currentEntryPrice != null ||
        input.currentQuantity != null ||
        input.currentMargin != null ||
        input.currentLeverage != null

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.small,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.14f))
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("当前持仓", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                if (!hasCurrentPositionInput) {
                    Text(
                        text = "尚未创建，可手动创建或从方案库选择",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    Text(
                        text = "${input.side.label()} · ${DecimalFormatters.formatQuantity(input.currentLeverage)}x · 均价 ${DecimalFormatters.formatCurrency(input.currentEntryPrice)}",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "数量 ${DecimalFormatters.formatDetailedQuantity(input.currentQuantity)} $symbol · 保证金 ${DecimalFormatters.formatCurrency(input.currentMargin)} USDT",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                if (hasCurrentPositionInput) {
                    TextButton(onClick = onEdit) {
                        Text("编辑", fontWeight = FontWeight.SemiBold)
                    }
                    TextButton(onClick = onClear) {
                        Text("清空", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

@Composable
private fun AveragingPositionEditorDialog(
    initialInput: AveragingDecisionInput,
    symbol: String,
    onSave: (AveragingDecisionInput) -> Unit,
    onDismiss: () -> Unit
) {
    var draft by remember(initialInput) {
        mutableStateOf(
            initialInput.copy(
                currentMargin = initialInput.currentMargin?.setScale(2, RoundingMode.HALF_UP),
                currentQuantity = initialInput.currentQuantity?.setScale(6, RoundingMode.HALF_UP),
                currentLeverage = initialInput.currentLeverage ?: BigDecimal.ONE
            )
        )
    }
    var amountField by remember(initialInput) {
        mutableStateOf(
            if (initialInput.currentQuantity != null && initialInput.currentMargin == null) {
                AmountField.Quantity
            } else {
                AmountField.Margin
            }
        )
    }
    var amountMenuExpanded by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth().widthIn(max = 400.dp).heightIn(max = 560.dp),
            shape = MaterialTheme.shapes.medium,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.fillMaxWidth().heightIn(max = 560.dp)) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = if (initialInput.currentEntryPrice == null && initialInput.currentQuantity == null) {
                            "手动创建当前持仓"
                        } else {
                            "编辑当前持仓"
                        },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "仅用于本次补仓计算，不会保存到方案库。",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "当前币种：$symbol",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    PositionSideSelector(
                        selectedSide = draft.side,
                        onSideChange = { draft = draft.copy(side = it) }
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(modifier = Modifier.weight(1.35f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = "当前仓位",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            AmountUnitInput(
                                value = if (amountField == AmountField.Margin) draft.currentMargin else draft.currentQuantity,
                                onValueChange = {
                                    draft = if (amountField == AmountField.Margin) {
                                        draft.copy(currentMargin = it)
                                    } else {
                                        draft.copy(currentQuantity = it)
                                    }
                                },
                                selectedUnit = if (amountField == AmountField.Margin) "USDT" else symbol,
                                coinUnit = symbol,
                                expanded = amountMenuExpanded,
                                onExpandedChange = { amountMenuExpanded = it },
                                onUnitSelect = {
                                    amountField = it
                                    amountMenuExpanded = false
                                },
                                onSubmit = {
                                    focusManager.clearFocus(force = true)
                                    keyboardController?.hide()
                                }
                            )
                        }
                        LeverageSelector(
                            leverage = draft.currentLeverage ?: BigDecimal.ONE,
                            onLeverageChange = { draft = draft.copy(currentLeverage = it) },
                            modifier = Modifier.weight(0.85f)
                        )
                    }
                    NumberInput(
                        value = draft.currentEntryPrice,
                        onValueChange = { draft = draft.copy(currentEntryPrice = it) },
                        label = "当前均价"
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End)
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("取消", fontWeight = FontWeight.SemiBold)
                    }
                    Button(
                        onClick = {
                            focusManager.clearFocus(force = true)
                            keyboardController?.hide()
                            onSave(draft.normalizedCurrentPosition(amountField))
                        },
                        shape = MaterialTheme.shapes.small
                    ) {
                        Text("保存")
                    }
                }
            }
        }
    }
}

private fun AveragingDecisionInput.normalizedCurrentPosition(amountField: AmountField): AveragingDecisionInput {
    val price = currentEntryPrice
    val leverage = currentLeverage
    if (price == null || price <= BigDecimal.ZERO || leverage == null || leverage <= BigDecimal.ZERO) {
        return when (amountField) {
            AmountField.Margin -> copy(currentQuantity = null)
            AmountField.Quantity -> copy(currentMargin = null)
        }
    }
    return when (amountField) {
        AmountField.Margin -> copy(
            currentQuantity = currentMargin?.takeIf { it > BigDecimal.ZERO }
                ?.multiply(leverage)
                ?.divide(price, 16, RoundingMode.HALF_UP)
        )
        AmountField.Quantity -> copy(
            currentMargin = currentQuantity?.takeIf { it > BigDecimal.ZERO }
                ?.multiply(price)
                ?.divide(leverage, 16, RoundingMode.HALF_UP)
        )
    }
}

@Composable
private fun AveragingRatioSelector(
    currentMargin: BigDecimal?,
    selectedAmount: BigDecimal?,
    onSelect: (BigDecimal) -> Unit
) {
    val ratios = listOf(
        BigDecimal("0.25") to "25%",
        BigDecimal("0.5") to "50%",
        BigDecimal.ONE to "100%"
    )
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        ratios.forEach { (ratio, label) ->
            val amount = currentMargin?.multiply(ratio)?.setScale(2, RoundingMode.HALF_UP)
            val selected = amount != null && selectedAmount?.compareTo(amount) == 0
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .clickable(enabled = amount != null && amount > BigDecimal.ZERO) {
                        onSelect(amount!!)
                    },
                shape = MaterialTheme.shapes.small,
                color = if (selected) MaterialTheme.colorScheme.primaryContainer
                else MaterialTheme.colorScheme.surface,
                border = BorderStroke(
                    1.dp,
                    if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                    else MaterialTheme.colorScheme.outline.copy(alpha = 0.18f)
                )
            ) {
                Box(modifier = Modifier.padding(vertical = 9.dp), contentAlignment = Alignment.Center) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = if (amount == null) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f)
                        else MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
    if (currentMargin == null || currentMargin <= BigDecimal.ZERO) {
        Text(
            text = "填写当前保证金后可使用比例快捷补仓",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun AveragingAmountModeSelector(
    useQuantity: Boolean,
    onUseAmount: () -> Unit,
    onUseQuantity: () -> Unit
) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        AveragingModeOption(
            label = "按金额",
            selected = !useQuantity,
            onClick = onUseAmount,
            modifier = Modifier.weight(1f)
        )
        AveragingModeOption(
            label = "按币数量",
            selected = useQuantity,
            onClick = onUseQuantity,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun AveragingModeOption(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier
) {
    Surface(
        modifier = modifier.clickable(onClick = onClick),
        shape = MaterialTheme.shapes.small,
        color = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
        border = BorderStroke(
            1.dp,
            if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
            else MaterialTheme.colorScheme.outline.copy(alpha = 0.18f)
        )
    ) {
        Box(modifier = Modifier.padding(vertical = 9.dp), contentAlignment = Alignment.Center) {
            Text(label, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun AveragingInlinePreview(
    input: AveragingDecisionInput,
    result: AveragingDecisionResult,
    symbol: String
) {
    val currentQuantity = input.currentQuantity ?: BigDecimal.ZERO
    val positionMultiple = if (currentQuantity > BigDecimal.ZERO) {
        result.newQuantity.divide(currentQuantity, 4, RoundingMode.HALF_UP)
    } else {
        null
    }
    val improvementPercent = input.currentEntryPrice?.takeIf { it > BigDecimal.ZERO }?.let {
        result.averagePriceImprovement.multiply(BigDecimal("100")).divide(it, 4, RoundingMode.HALF_UP)
    }
    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("补仓后预览", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        AveragingMetricTile(
            label = "补仓后均价 / 回本价",
            value = "${DecimalFormatters.formatCurrency(result.newAveragePrice)} USDT",
            emphasized = true
        )
        AveragingInputRow {
            AveragingMetricTile(
                label = "均价改善",
                value = "${DecimalFormatters.formatCurrency(result.averagePriceImprovement)} USDT",
                supporting = improvementPercent?.let { DecimalFormatters.formatPercentage(it) },
                modifier = Modifier.weight(1f)
            )
            AveragingMetricTile(
                label = "新增保证金",
                value = "${DecimalFormatters.formatCurrency(result.addAmount)} USDT",
                modifier = Modifier.weight(1f)
            )
        }
        Text(
            text = buildString {
                append("补仓后持有 ${DecimalFormatters.formatDetailedQuantity(result.newQuantity)} $symbol")
                positionMultiple?.let { append(" · 仓位约扩大至 ${it.stripTrailingZeros().toPlainString()} 倍") }
            },
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = "补仓会降低回本价格，但仓位规模和潜在亏损也会同步增加。",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.error
        )
    }
}

@Composable
fun AveragingResultDialog(
    input: AveragingDecisionInput,
    result: AveragingDecisionResult,
    symbol: String,
    onCopyResult: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth().widthIn(max = 400.dp).heightIn(max = 660.dp),
            shape = MaterialTheme.shapes.medium,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.28f))
        ) {
            Column(
                modifier = Modifier.padding(14.dp).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("补仓计算完成", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                AveragingDecisionResultCard(input, result, symbol)
                OutlinedButton(
                    onClick = onCopyResult,
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.small
                ) {
                    Text("复制补仓结果")
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
private fun SchemeSelectionDialog(
    schemes: List<ExistingScheme>,
    onSelect: (ExistingScheme) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 400.dp),
            shape = MaterialTheme.shapes.medium,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.16f))
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "从方案库选择",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "选择后会复制当前持仓参数，仅用于本次补仓计算，不会修改原方案。",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                schemes.forEach { scheme ->
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(scheme) },
                        shape = MaterialTheme.shapes.small,
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.54f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.14f))
                    ) {
                        Column(
                            modifier = Modifier.padding(10.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "${scheme.name} · ${scheme.symbol}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.secondary
                            )
                            Text(
                                text = "${scheme.input.side.label()} · ${scheme.input.leverage.stripTrailingZeros().toPlainString()}x",
                                style = MaterialTheme.typography.labelLarge
                            )
                            Text(
                                text = "开仓价 ${DecimalFormatters.formatCurrency(scheme.input.entryPrice)} USDT · ${scheme.symbol} 数量 ${DecimalFormatters.formatDetailedQuantity(scheme.result.quantity)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "保证金 ${DecimalFormatters.formatCurrency(scheme.result.requiredMargin)} USDT",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("取消", fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
private fun AveragingInputDetails(input: AveragingDecisionInput, symbol: String) {
    SectionPanel(title = "详细数据") {
        Text(input.side.label(), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        AveragingInputRow {
            AveragingMetricTile("当前均价", "${DecimalFormatters.formatCurrency(input.currentEntryPrice)} USDT", modifier = Modifier.weight(1f))
            AveragingMetricTile("当前 $symbol 数量", "${DecimalFormatters.formatDetailedQuantity(input.currentQuantity)} $symbol", modifier = Modifier.weight(1f))
        }
        AveragingInputRow {
            AveragingMetricTile("当前保证金", "${DecimalFormatters.formatCurrency(input.currentMargin)} USDT", modifier = Modifier.weight(1f))
            AveragingMetricTile("当前杠杆", "${DecimalFormatters.formatQuantity(input.currentLeverage)}x", modifier = Modifier.weight(1f))
        }
        AveragingInputRow {
            AveragingMetricTile("补仓价格", "${DecimalFormatters.formatCurrency(input.addEntryPrice)} USDT", modifier = Modifier.weight(1f))
            AveragingMetricTile("目标平仓价", "${DecimalFormatters.formatCurrency(input.targetExitPrice)} USDT", modifier = Modifier.weight(1f))
        }
        AveragingMetricTile(
            label = if (input.addAmount != null) "补仓金额" else "补仓 $symbol 数量",
            value = input.addAmount?.let { "${DecimalFormatters.formatCurrency(it)} USDT" }
                ?: "${DecimalFormatters.formatDetailedQuantity(input.addQuantity)} $symbol"
        )
    }
}

@Composable
private fun AveragingDecisionResultCard(
    input: AveragingDecisionInput,
    result: AveragingDecisionResult,
    symbol: String
) {
    val changeColor = averagingPnlColor(result.pnlChange)
    var detailsExpanded by rememberSaveable { mutableStateOf(false) }
    val totalMargin = (input.currentMargin ?: BigDecimal.ZERO) + result.addAmount
    val targetRoi = result.pnlAfterAdding?.takeIf { totalMargin > BigDecimal.ZERO }
        ?.multiply(BigDecimal("100"))
        ?.divide(totalMargin, 8, RoundingMode.HALF_UP)
    val newPositionValue = result.newQuantity.multiply(result.newAveragePrice)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.small,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            AveragingMetricTile(
                label = "补仓后成本 / 回本价",
                value = "${DecimalFormatters.formatCurrency(result.newAveragePrice)} USDT",
                emphasized = true
            )
            result.pnlAfterAdding?.let {
                AveragingMetricTile(
                    label = "目标价收益",
                    value = "${averagingPnlText(it, DecimalFormatters.formatPositiveNegative(it))} USDT",
                    valueColor = averagingPnlColor(it)
                )
            }
            result.pnlChange?.let {
                AveragingMetricTile(
                    label = "相比不补仓",
                    value = "${averagingPnlText(it, DecimalFormatters.formatPositiveNegative(it))} USDT",
                    valueColor = changeColor
                )
            }
            TextButton(onClick = { detailsExpanded = !detailsExpanded }) {
                Text(if (detailsExpanded) "收起详细数据" else "查看详细数据", fontWeight = FontWeight.SemiBold)
            }
            if (detailsExpanded) {
                AveragingInputRow {
                    AveragingMetricTile("补仓数量", "${DecimalFormatters.formatDetailedQuantity(result.quantityIncrease)} $symbol", modifier = Modifier.weight(1f))
                    AveragingMetricTile("补仓金额", "${DecimalFormatters.formatCurrency(result.addAmount)} USDT", modifier = Modifier.weight(1f))
                }
                AveragingInputRow {
                    AveragingMetricTile("新仓位价值", "${DecimalFormatters.formatCurrency(newPositionValue)} USDT", modifier = Modifier.weight(1f))
                    AveragingMetricTile("当前杠杆", "${DecimalFormatters.formatQuantity(input.currentLeverage)}x", modifier = Modifier.weight(1f))
                }
                AveragingInputRow {
                    AveragingMetricTile("当前均价", "${DecimalFormatters.formatCurrency(input.currentEntryPrice)} USDT", modifier = Modifier.weight(1f))
                    AveragingMetricTile("当前保证金", "${DecimalFormatters.formatCurrency(input.currentMargin)} USDT", modifier = Modifier.weight(1f))
                }
                targetRoi?.let {
                    AveragingMetricTile("保证金收益率（ROI）", DecimalFormatters.formatPercentage(it))
                }
            }
            Text(
                text = if (result.pnlChange != null) "收益差值仅表示填写目标价时的模拟结果"
                else "未填写目标平仓价，本次仅展示补仓后的仓位变化",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun AveragingBeforeAfterBlock(
    beforeLabel: String,
    beforeValue: String,
    afterLabel: String,
    afterValue: String,
    emphasized: Boolean = false
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.small,
        color = if (emphasized) {
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.58f)
        } else {
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.48f)
        },
        border = BorderStroke(
            if (emphasized) 2.dp else 1.dp,
            if (emphasized) MaterialTheme.colorScheme.primary.copy(alpha = 0.52f)
            else MaterialTheme.colorScheme.outline.copy(alpha = 0.14f)
        )
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                AveragingMetricTile(beforeLabel, beforeValue, modifier = Modifier.weight(1f))
                Box(
                    modifier = Modifier.width(18.dp).align(Alignment.CenterVertically),
                    contentAlignment = Alignment.Center
                ) {
                    Text("↓", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                }
                AveragingMetricTile(afterLabel, afterValue, modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun AveragingMetricTile(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    supporting: String? = null,
    emphasized: Boolean = false,
    valueColor: Color = MaterialTheme.colorScheme.onSurface
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.small,
        color = if (emphasized) {
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.38f)
        } else {
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.48f)
        },
        border = BorderStroke(
            1.dp,
            if (emphasized) MaterialTheme.colorScheme.primary.copy(alpha = 0.28f)
            else MaterialTheme.colorScheme.outline.copy(alpha = 0.12f)
        )
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 9.dp),
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = if (emphasized) MaterialTheme.typography.titleLarge else MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = valueColor
            )
            supporting?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun AveragingSoftOutlinedButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: Shape = MaterialTheme.shapes.small,
    border: BorderStroke? = null,
    content: @Composable RowScope.() -> Unit
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        shape = shape,
        border = border ?: BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outline.copy(alpha = 0.24f)
        ),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.58f),
            contentColor = MaterialTheme.colorScheme.onSurface,
            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.24f),
            disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.42f)
        ),
        content = content
    )
}

@Composable
private fun AveragingInputRow(content: @Composable RowScope.() -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        content = content
    )
}

@Composable
private fun averagingPnlColor(value: BigDecimal?): Color {
    val palette = LocalProfitLossPalette.current
    return when {
        value == null -> MaterialTheme.colorScheme.onSurface
        value >= BigDecimal.ZERO -> palette.profit
        else -> palette.loss
    }
}

@Composable
private fun averagingPnlText(value: BigDecimal?, text: String): String {
    val palette = LocalProfitLossPalette.current
    val indicator = when {
        value == null -> ""
        value >= BigDecimal.ZERO -> palette.profitIndicator
        else -> palette.lossIndicator
    }
    return indicator + text
}

private fun PositionSide.label(): String = if (this == PositionSide.Long) "做多" else "做空"
