package com.personal.futurescalculator.ui.comparison

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.personal.futurescalculator.model.AmountField
import com.personal.futurescalculator.model.CalculationInput
import com.personal.futurescalculator.model.CalculationResult
import com.personal.futurescalculator.model.CoinAsset
import com.personal.futurescalculator.model.CoinMarginedCalculationMode
import com.personal.futurescalculator.model.CoinMarginedResult
import com.personal.futurescalculator.model.ComparisonItem
import com.personal.futurescalculator.model.ComparisonResult
import com.personal.futurescalculator.model.CopyFormat
import com.personal.futurescalculator.model.HistoryCategory
import com.personal.futurescalculator.model.HistoryField
import com.personal.futurescalculator.model.HistoryRecord
import com.personal.futurescalculator.model.HistorySection
import com.personal.futurescalculator.model.MarginMode
import com.personal.futurescalculator.model.PositionSide
import com.personal.futurescalculator.model.SavedPlan
import com.personal.futurescalculator.model.SettlementMode
import com.personal.futurescalculator.model.toComparisonItem
import com.personal.futurescalculator.ui.CompactTextInput
import com.personal.futurescalculator.ui.AppDropdownMenu
import com.personal.futurescalculator.ui.AppDropdownMenuItemPadding
import com.personal.futurescalculator.ui.AppDropdownMenuText
import com.personal.futurescalculator.ui.DropdownChevronIcon
import com.personal.futurescalculator.ui.LeverageSelector
import com.personal.futurescalculator.ui.MarginModeSelector
import com.personal.futurescalculator.ui.NumberInput
import com.personal.futurescalculator.ui.PositionSideSelector
import com.personal.futurescalculator.ui.coin.CoinIcon
import com.personal.futurescalculator.ui.position.AmountUnitInput
import com.personal.futurescalculator.ui.results.MetricTile
import com.personal.futurescalculator.ui.results.pnlColor
import com.personal.futurescalculator.ui.results.pnlText
import com.personal.futurescalculator.util.DecimalFormatters
import java.math.BigDecimal
import java.math.RoundingMode

const val MAIN_SCHEME_ID = "main_scheme"

data class ComparisonSchemeView(
    val id: String,
    val name: String,
    val symbol: String,
    val coin: CoinAsset?,
    val settlementMode: SettlementMode,
    val coinMarginedCalculationMode: CoinMarginedCalculationMode,
    val input: CalculationInput,
    val result: CalculationResult?,
    val coinMarginedResult: CoinMarginedResult?,
    val isMain: Boolean
)

fun buildComparisonSchemes(
    mainInput: CalculationInput,
    mainResult: CalculationResult?,
    mainCoinMarginedResult: CoinMarginedResult?,
    mainSymbol: String,
    mainSettlementMode: SettlementMode,
    mainCoinMarginedCalculationMode: CoinMarginedCalculationMode,
    items: List<ComparisonItem>,
    results: List<ComparisonResult>,
    coins: List<CoinAsset>
): List<ComparisonSchemeView> = buildList {
    add(
        ComparisonSchemeView(
            MAIN_SCHEME_ID,
            "主方案",
            mainSymbol,
            coins.firstOrNull { it.symbol == mainSymbol },
            mainSettlementMode,
            mainCoinMarginedCalculationMode,
            mainInput,
            mainResult,
            mainCoinMarginedResult,
            true
        )
    )
    items.forEach { item ->
        add(
            ComparisonSchemeView(
                id = item.id,
                name = item.name,
                symbol = coins.firstOrNull { it.id == item.coinId }?.symbol ?: "币",
                coin = coins.firstOrNull { it.id == item.coinId },
                settlementMode = item.settlementMode,
                coinMarginedCalculationMode = item.coinMarginedCalculationMode,
                input = item.input,
                result = results.firstOrNull { it.item.id == item.id }?.result,
                coinMarginedResult = results.firstOrNull { it.item.id == item.id }?.coinMarginedResult,
                isMain = false
            )
        )
    }
}

fun comparisonValidationMessage(
    currentInput: CalculationInput,
    comparisonItems: List<ComparisonItem>
): String? {
    val missingByScheme = buildList {
        comparisonMissingFields(currentInput).takeIf { it.isNotEmpty() }?.let {
            add("方案 1 缺少${it.joinToString("、")}")
        }
        comparisonItems.forEach { item ->
            comparisonMissingFields(item.input).takeIf { it.isNotEmpty() }?.let {
                add("${item.name} 缺少${it.joinToString("、")}")
            }
        }
    }
    return missingByScheme.takeIf { it.isNotEmpty() }
        ?.joinToString("；", prefix = "请先填写完整参数后再查看方案对比：")
}

fun selectedComparisonValidationMessage(schemes: List<ComparisonSchemeView>): String? {
    if (schemes.size < 2) return "请至少选择两个完整方案进行对比。"
    val invalid = schemes.mapNotNull { scheme ->
        val missing = comparisonMissingFields(scheme.input)
        when {
            missing.isNotEmpty() -> "${scheme.name}缺少${missing.joinToString("、")}"
            scheme.comparablePnlUsdt() == null -> "${scheme.name}无法计算结果"
            else -> null
        }
    }
    return invalid.takeIf { it.isNotEmpty() }?.joinToString(prefix = "参数不完整：", separator = "；")
}

@Composable
fun CopySchemeDialog(
    schemes: List<ComparisonSchemeView>,
    copyFormat: CopyFormat,
    onSelect: (ComparisonSchemeView, CopyFormat) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth().widthIn(max = 400.dp).heightIn(max = 660.dp),
            shape = MaterialTheme.shapes.medium,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.padding(14.dp).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("选择要复制的单子", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(
                    "每次只复制一个方案，避免不同单子的参数和结果混在一起。",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                schemes.forEach { scheme ->
                    Surface(
                        modifier = Modifier.fillMaxWidth().clickable(enabled = copyFormat != CopyFormat.Ask) {
                            onSelect(scheme, copyFormat)
                        },
                        shape = MaterialTheme.shapes.small,
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.18f))
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CoinIcon(scheme.coin, 30)
                            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                Text("${scheme.name} · ${scheme.symbol}", fontWeight = FontWeight.Bold)
                                Text(
                                    "${scheme.input.side.label()} · ${scheme.input.leverage.stripTrailingZeros().toPlainString()}x · ${scheme.primaryPnlLabel()} ${scheme.primaryPnlText()}",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            if (copyFormat == CopyFormat.Ask) {
                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    TextButton(onClick = { onSelect(scheme, CopyFormat.Summary) }) {
                                        Text("简洁", fontWeight = FontWeight.SemiBold)
                                    }
                                    TextButton(onClick = { onSelect(scheme, CopyFormat.Detail) }) {
                                        Text("详细", fontWeight = FontWeight.SemiBold)
                                    }
                                }
                            } else {
                                Text("复制", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
                TextButton(onClick = onDismiss, modifier = Modifier.align(Alignment.End)) {
                    Text("取消", fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
fun ComparisonResultDialog(
    schemes: List<ComparisonSchemeView>,
    onCopySummary: () -> Unit,
    onDismiss: () -> Unit
) {
    val orderedSchemes = schemes.filter { it.comparablePnlUsdt() != null }
    val mainScheme = orderedSchemes.firstOrNull { it.id == MAIN_SCHEME_ID } ?: orderedSchemes.firstOrNull()
    val comparedSchemes = orderedSchemes.filter { it.id != mainScheme?.id }
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
                Text("开仓方案对比", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                if (mainScheme != null && comparedSchemes.isNotEmpty()) {
                    Text("相对主方案差值列表", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    comparedSchemes.forEach { scheme ->
                        ComparisonRelativeDiffCard(
                            scheme = scheme,
                            mainScheme = mainScheme,
                            diff = scheme.comparablePnlUsdt()!! - mainScheme.comparablePnlUsdt()!!
                        )
                    }
                }
                Text("方案摘要", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                orderedSchemes.forEach { scheme ->
                    ComparisonSchemeSummary(scheme)
                }
                TextButton(onClick = { showFormula = !showFormula }, modifier = Modifier.align(Alignment.Start)) {
                    Text(if (showFormula) "收起计算公式 ▲" else "查看计算公式 ▼", fontWeight = FontWeight.Bold)
                }
                if (showFormula && mainScheme != null && comparedSchemes.isNotEmpty()) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.small,
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.46f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.14f))
                    ) {
                        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = "对比方案净收益 - 主方案净收益 = 相对差值",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            comparedSchemes.forEach { scheme ->
                                val diff = scheme.comparablePnlUsdt()!! - mainScheme.comparablePnlUsdt()!!
                                Text(
                                    text = "${scheme.name} - ${mainScheme.name} = ${pnlText(diff, DecimalFormatters.formatPositiveNegative(diff))} USDT",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
                OutlinedButton(
                    onClick = onCopySummary,
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.small
                ) {
                    Text("复制对比摘要")
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
fun ComparisonSchemeListCard(
    scheme: ComparisonSchemeView,
    selected: Boolean,
    enabled: Boolean,
    onSelectedChange: (Boolean) -> Unit,
    onClick: (() -> Unit)?
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.small,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.48f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.14f))
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(checked = selected, enabled = enabled, onCheckedChange = onSelectedChange)
            CoinIcon(coin = scheme.coin, size = 32)
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "${scheme.name} · ${scheme.symbol}",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold
                )
                if (scheme.comparablePnlUsdt() != null) {
                    Text(
                        text = "${scheme.primaryPnlLabel()} ${scheme.primaryPnlText()}",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = pnlColor(scheme.primaryPnlValue())
                    )
                } else {
                    Text(
                        text = if (scheme.isMain) "主方案参数不完整，不可参与对比" else "参数不完整，不可参与对比",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    text = "${scheme.symbol} | ${comparisonSettlementDisplay(scheme.settlementMode, scheme.coinMarginedCalculationMode)} | ${scheme.input.side.label()}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                if (!scheme.isMain && onClick != null) {
                    TextButton(
                        onClick = onClick,
                        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text("编辑", fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

@Composable
fun ComparisonSchemeEditorDialog(
    initialItem: ComparisonItem,
    coins: List<CoinAsset>,
    showDelete: Boolean,
    onSave: (ComparisonItem) -> Unit,
    onDelete: (ComparisonItem) -> Unit,
    onDismiss: () -> Unit
) {
    var item by remember(initialItem.id) { mutableStateOf(initialItem) }
    var showCoinDialog by remember { mutableStateOf(false) }
    var amountMenuExpanded by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val coin = coins.firstOrNull { it.id == item.coinId }
    if (showCoinDialog) {
        ComparisonCoinSelectorDialog(
            coins = coins,
            selectedCoinId = item.coinId,
            onSelect = {
                item = item.copy(coinId = it)
                showCoinDialog = false
            },
            onDismiss = { showCoinDialog = false }
        )
    }
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth().widthIn(max = 400.dp).heightIn(max = 660.dp),
            shape = MaterialTheme.shapes.medium,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().heightIn(max = 660.dp)
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = if (initialItem.input == CalculationInput()) "添加对比方案" else "编辑对比方案",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    if (initialItem.input != CalculationInput()) {
                        CompactTextInput(
                            value = item.name,
                            onValueChange = { item = item.copy(name = it) },
                            modifier = Modifier.fillMaxWidth(),
                            label = "方案名称"
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ComparisonSoftOutlinedButton(onClick = { showCoinDialog = true }, modifier = Modifier.weight(1f)) {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                CoinIcon(coin = coin, size = 24)
                                Text(coin?.symbol ?: "币", fontWeight = FontWeight.SemiBold)
                                DropdownChevronIcon(iconSize = 16.dp)
                            }
                        }
                        ComparisonOptionChips(
                            firstText = "U 本位",
                            secondText = "币本位",
                            firstSelected = item.settlementMode == SettlementMode.UsdtMargined,
                            onFirstClick = { item = item.copy(settlementMode = SettlementMode.UsdtMargined) },
                            onSecondClick = { item = item.copy(settlementMode = SettlementMode.CoinMargined) },
                            modifier = Modifier.weight(1f),
                        )
                    }
                    PositionSideSelector(item.input.side, { item = item.copy(input = item.input.copy(side = it)) })
                    MarginModeSelector(item.input.marginMode, { item = item.copy(input = item.input.copy(marginMode = it)) })
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(modifier = Modifier.weight(1.35f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            val amountIsMargin = item.settlementMode == SettlementMode.UsdtMargined &&
                                item.lastEditedAmountField == AmountField.Margin
                            val selectedAmountUnit = when {
                                item.settlementMode == SettlementMode.UsdtMargined && amountIsMargin -> "USDT"
                                item.settlementMode == SettlementMode.UsdtMargined -> coin?.symbol ?: "币"
                                item.coinMarginedCalculationMode == CoinMarginedCalculationMode.CoinQuantity -> coin?.symbol ?: "币"
                                else -> "张"
                            }
                            Text(
                                text = "仓位",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            AmountUnitInput(
                                value = if (amountIsMargin) item.input.margin else item.input.quantity,
                                onValueChange = {
                                    item = if (amountIsMargin) {
                                        item.copy(input = item.input.copy(margin = it, quantity = null))
                                    } else {
                                        item.copy(input = item.input.copy(quantity = it, margin = null))
                                    }
                                },
                                selectedUnit = selectedAmountUnit,
                                coinUnit = coin?.symbol ?: "币",
                                expanded = amountMenuExpanded,
                                onExpandedChange = { amountMenuExpanded = it },
                                onUnitSelect = { next ->
                                    amountMenuExpanded = false
                                    item = item.copy(
                                        input = if (next == AmountField.Margin) {
                                            item.input.copy(margin = item.input.margin, quantity = null)
                                        } else {
                                            item.input.copy(quantity = item.input.quantity, margin = null)
                                        },
                                        lastEditedAmountField = next
                                    )
                                },
                                onSubmit = {
                                    focusManager.clearFocus(force = true)
                                    keyboardController?.hide()
                                },
                                unitSelectable = item.settlementMode == SettlementMode.UsdtMargined
                            )
                        }
                        LeverageSelector(
                            leverage = item.input.leverage,
                            onLeverageChange = { item = item.copy(input = item.input.copy(leverage = it)) },
                            modifier = Modifier.weight(0.85f)
                        )
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        NumberInput(
                            value = item.input.entryPrice,
                            onValueChange = { item = item.copy(input = item.input.copy(entryPrice = it)) },
                            label = "开仓价",
                            modifier = Modifier.weight(1f)
                        )
                        NumberInput(
                            value = item.input.exitPrice,
                            onValueChange = { item = item.copy(input = item.input.copy(exitPrice = it)) },
                            label = "平仓价",
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End)
                ) {
                    if (showDelete) {
                        TextButton(
                            onClick = { onDelete(item) },
                            colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                        ) { Text("删除", fontWeight = FontWeight.SemiBold) }
                    }
                    TextButton(onClick = onDismiss) { Text("取消", fontWeight = FontWeight.SemiBold) }
                    Button(
                        onClick = {
                            focusManager.clearFocus(force = true)
                            keyboardController?.hide()
                            onSave(item.copy(name = item.name.ifBlank { "未命名方案" }))
                        },
                        shape = MaterialTheme.shapes.small
                    ) { Text("保存") }
                }
            }
        }
    }
}

@Composable
fun PlanSelectionDialog(
    plans: List<SavedPlan>,
    coins: List<CoinAsset>,
    coinMarginedCalculationMode: CoinMarginedCalculationMode,
    onSelect: (ComparisonItem) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth().widthIn(max = 400.dp).heightIn(max = 560.dp),
            shape = MaterialTheme.shapes.medium,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.padding(12.dp).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("从方案库选择", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                if (plans.isEmpty()) {
                    Text(
                        "暂无保存的开仓方案",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                plans.forEach { plan ->
                    val coin = coins.firstOrNull { it.id == plan.coinId }
                    Surface(
                        modifier = Modifier.fillMaxWidth().clickable {
                            onSelect(
                                plan.toComparisonItem().copy(
                                    id = "item_${System.currentTimeMillis()}",
                                    name = plan.name,
                                    coinMarginedCalculationMode = coinMarginedCalculationMode
                                )
                            )
                        },
                        shape = MaterialTheme.shapes.small,
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.50f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.14f))
                    ) {
                        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(plan.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                            Text(
                                "${coin?.symbol ?: "币"} · ${settlementModeLabel(plan.settlementMode)} · ${plan.input.side.label()} · ${plan.input.leverage.stripTrailingZeros().toPlainString()}x",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                TextButton(onClick = onDismiss, modifier = Modifier.align(Alignment.End)) {
                    Text("取消", fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

fun createComparisonHistorySnapshot(schemes: List<ComparisonSchemeView>): HistoryRecord {
    val sorted = schemes.sortedByDescending { it.comparablePnlUsdt() }
    val differences = sorted.zipWithNext().map { (higher, lower) ->
        HistoryField(
            "${higher.name} 与 ${lower.name}",
            "${higher.name} 多 ${DecimalFormatters.formatPositiveNegative(higher.comparablePnlUsdt()!! - lower.comparablePnlUsdt()!!)} USDT"
        )
    }
    return HistoryRecord(
        id = "history_${System.currentTimeMillis()}",
        category = HistoryCategory.SchemeComparison,
        title = "${schemes.size} 个方案对比",
        summary = schemes.firstOrNull()?.let { "${it.name} ${it.primaryPnlText()}" } ?: "无结果",
        roiSummary = null,
        savedAt = System.currentTimeMillis(),
        sections = schemes.map { scheme ->
            val fields = buildList {
                add(HistoryField("结算模式", comparisonSettlementHistoryDisplay(scheme.settlementMode, scheme.coinMarginedCalculationMode)))
                add(HistoryField("方向", scheme.input.side.label()))
                add(HistoryField("模式", scheme.input.marginMode.label()))
                add(HistoryField("杠杆", "${scheme.input.leverage.stripTrailingZeros().toPlainString()}x"))
                add(HistoryField("保证金", "${DecimalFormatters.formatCurrency(scheme.result?.requiredMargin)} USDT"))
                add(HistoryField("数量", "${DecimalFormatters.formatQuantity(scheme.result?.quantity)} ${scheme.symbol}"))
                add(HistoryField("开仓价", "${DecimalFormatters.formatCurrency(scheme.input.entryPrice)} USDT"))
                add(HistoryField("平仓价", "${DecimalFormatters.formatCurrency(scheme.input.exitPrice)} USDT"))
                add(HistoryField(scheme.primaryPnlLabel(), scheme.primaryPnlText()))
                add(HistoryField("折算收益", "${DecimalFormatters.formatPositiveNegative(scheme.comparablePnlUsdt())} USDT"))
                add(HistoryField("ROI", if (scheme.settlementMode == SettlementMode.UsdtMargined) DecimalFormatters.formatPercentage(scheme.result?.roiPercent) else "不适用"))
                add(HistoryField("手续费", if (scheme.settlementMode == SettlementMode.UsdtMargined) "${DecimalFormatters.formatCurrency(scheme.result?.totalFee)} USDT" else "不适用"))
                if (scheme.input.estimateLiquidation && scheme.result?.liquidationPrice != null) {
                    add(HistoryField("强平价格", "${DecimalFormatters.formatCurrency(scheme.result.liquidationPrice)} USDT"))
                }
            }
            HistorySection("${scheme.name} · ${scheme.symbol}", fields)
        } + HistorySection("收益差距", differences.ifEmpty { listOf(HistoryField("收益差距", "暂无")) })
    )
}

@Composable
fun ComparisonItemCard(
    item: ComparisonItem,
    result: CalculationResult?,
    coins: List<CoinAsset>,
    onChange: (ComparisonItem) -> Unit,
    onRemove: () -> Unit
) {
    var showCoinDialog by remember { mutableStateOf(false) }
    val coin = coins.firstOrNull { it.id == item.coinId }
    if (showCoinDialog) {
        ComparisonCoinSelectorDialog(
            coins = coins,
            selectedCoinId = item.coinId,
            onSelect = {
                onChange(item.copy(coinId = it))
                showCoinDialog = false
            },
            onDismiss = { showCoinDialog = false }
        )
    }
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
                        text = "${coin?.symbol ?: "币"} · ${item.input.side.label()} · ${item.input.marginMode.label()} · ${item.input.leverage.stripTrailingZeros().toPlainString()}x",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                TextButton(
                    onClick = onRemove,
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("删除", fontWeight = FontWeight.SemiBold)
                }
            }
            ComparisonSoftOutlinedButton(
                onClick = { showCoinDialog = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("币种：${coin?.symbol ?: "请选择"}")
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
            ComparisonInputRow {
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
                    label = "保证金",
                    modifier = Modifier.weight(1f)
                )
                NumberInput(
                    value = item.input.entryPrice,
                    onValueChange = { entryPrice ->
                        onChange(item.copy(input = item.input.copy(entryPrice = entryPrice)))
                    },
                    label = "开仓价",
                    modifier = Modifier.weight(1f)
                )
            }
            ComparisonInputRow {
                NumberInput(
                    value = item.input.exitPrice,
                    onValueChange = { exitPrice ->
                        onChange(
                            item.copy(
                                input = item.input.copy(
                                    exitPrice = exitPrice,
                                    targetProfitAmount = null,
                                    targetRoiPercent = null,
                                    maxLossAmount = null,
                                    maxLossRoiPercent = null
                                )
                            )
                        )
                    },
                    label = "平仓价",
                    modifier = Modifier.weight(1f)
                )
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
                    label = "${coin?.symbol ?: "币"} 数量",
                    modifier = Modifier.weight(1f)
                )
            }

            if (result != null) {
                MetricTile(
                    label = "净盈亏",
                    value = "${pnlText(result.netPnl, DecimalFormatters.formatCurrency(result.netPnl))} USDT",
                    valueColor = pnlColor(result.netPnl)
                )
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
private fun ComparisonDropdownSelector(
    text: String,
    options: List<Pair<String, () -> Unit>>,
    modifier: Modifier = Modifier
) {
    var expanded by rememberSaveable { mutableStateOf(false) }
    Box(modifier = modifier) {
        ComparisonSoftOutlinedButton(
            onClick = { expanded = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text)
                DropdownChevronIcon(iconSize = 16.dp)
            }
        }
        AppDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { (label, onSelect) ->
                DropdownMenuItem(
                    text = { AppDropdownMenuText(label) },
                    onClick = {
                        expanded = false
                        onSelect()
                    },
                    contentPadding = AppDropdownMenuItemPadding
                )
            }
        }
    }
}

@Composable
private fun ComparisonOptionChips(
    firstText: String,
    secondText: String,
    firstSelected: Boolean,
    onFirstClick: () -> Unit,
    onSecondClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(modifier = modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        ComparisonOptionChip(
            text = firstText,
            selected = firstSelected,
            onClick = onFirstClick,
            modifier = Modifier.weight(1f)
        )
        ComparisonOptionChip(
            text = secondText,
            selected = !firstSelected,
            onClick = onSecondClick,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun ComparisonOptionChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.height(34.dp).clickable(onClick = onClick),
        shape = MaterialTheme.shapes.small,
        color = if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.16f)
            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.50f),
        border = BorderStroke(
            1.dp,
            if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.46f)
            else MaterialTheme.colorScheme.outline.copy(alpha = 0.12f)
        )
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = text,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ComparisonCoinSelectorDialog(
    coins: List<CoinAsset>,
    selectedCoinId: String,
    onSelect: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var search by remember { mutableStateOf("") }
    val filtered = coins.filter {
        search.isBlank() ||
            it.symbol.contains(search, ignoreCase = true) ||
            it.name.contains(search, ignoreCase = true)
    }
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth().widthIn(max = 440.dp),
            shape = MaterialTheme.shapes.medium,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("选择方案币种", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                OutlinedTextField(
                    value = search,
                    onValueChange = { search = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("搜索币种") },
                    singleLine = true
                )
                LazyColumn(
                    modifier = Modifier.fillMaxWidth().heightIn(max = 320.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(filtered, key = { it.id }) { coin ->
                        Surface(
                            modifier = Modifier.fillMaxWidth().clickable { onSelect(coin.id) },
                            shape = MaterialTheme.shapes.small,
                            color = if (coin.id == selectedCoinId) {
                                MaterialTheme.colorScheme.secondaryContainer
                            } else {
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            }
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(8.dp),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CoinIcon(coin, 30)
                                Text(coin.symbol, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
                TextButton(onClick = onDismiss, modifier = Modifier.align(Alignment.End)) {
                    Text("取消", fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
private fun ComparisonResultsOverview(
    schemes: List<ComparisonSchemeView>
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.small,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.50f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.32f))
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "收益差距",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            ComparisonDifferenceSummary(schemes)
            Text(
                text = "方案详情",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            schemes.forEach { scheme ->
                ComparisonSchemeSummary(scheme)
            }
        }
    }
}

@Composable
private fun ComparisonDifferenceSummary(schemes: List<ComparisonSchemeView>) {
    val sorted = schemes.filter { it.comparablePnlUsdt() != null }.sortedByDescending { it.comparablePnlUsdt() }
    if (sorted.size < 2) {
        Text(
            text = "至少选择两个有结果的方案后显示收益差距。",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        return
    }
    sorted.zipWithNext().forEach { (higher, lower) ->
        val diff = higher.comparablePnlUsdt()!! - lower.comparablePnlUsdt()!!
        ComparisonDifferenceCard(
            higher = higher,
            lower = lower,
            diff = diff
        )
    }
}

@Composable
private fun ComparisonSchemeSummary(scheme: ComparisonSchemeView) {
    var expanded by rememberSaveable(scheme.id) { mutableStateOf(false) }
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.small,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.16f))
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CoinIcon(coin = scheme.coin, size = 28)
                    Text(
                        text = "${scheme.name} · ${scheme.symbol} · ${comparisonSettlementHistoryDisplay(scheme.settlementMode, scheme.coinMarginedCalculationMode)}",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                TextButton(
                    onClick = { expanded = !expanded },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text(if (expanded) "收起" else "展开", fontWeight = FontWeight.Bold)
                }
            }
            MetricTile(
                label = scheme.primaryPnlLabel(),
                value = scheme.primaryPnlText(),
                valueColor = pnlColor(scheme.primaryPnlValue())
            )
            if (scheme.settlementMode == SettlementMode.CoinMargined) {
                MetricTile(
                    label = "折算收益",
                    value = "${pnlText(scheme.comparablePnlUsdt(), DecimalFormatters.formatPositiveNegative(scheme.comparablePnlUsdt()))} USDT",
                    valueColor = pnlColor(scheme.comparablePnlUsdt())
                )
            }
            Text(
                text = "${scheme.symbol} | ${comparisonSettlementDisplay(scheme.settlementMode, scheme.coinMarginedCalculationMode)} | ${scheme.input.side.label()}",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (expanded) {
                Text(
                    text = "${scheme.input.side.label()} · ${scheme.input.marginMode.label()} · ${scheme.input.leverage.stripTrailingZeros().toPlainString()}x · ${comparisonSettlementHistoryDisplay(scheme.settlementMode, scheme.coinMarginedCalculationMode)}",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                ComparisonInputRow {
                    MetricTile(
                        label = "开仓价",
                        value = "${DecimalFormatters.formatCurrency(scheme.input.entryPrice)} USDT",
                        modifier = Modifier.weight(1f)
                    )
                    MetricTile(
                        label = "平仓价",
                        value = "${DecimalFormatters.formatCurrency(scheme.input.exitPrice)} USDT",
                        modifier = Modifier.weight(1f)
                    )
                }
                ComparisonInputRow {
                    MetricTile(
                        label = if (scheme.input.margin != null) "保证金" else "${scheme.symbol} 数量",
                        value = scheme.input.margin?.let { "${DecimalFormatters.formatCurrency(it)} USDT" }
                            ?: "${DecimalFormatters.formatQuantity(scheme.input.quantity)} ${scheme.symbol}",
                        modifier = Modifier.weight(1f)
                    )
                    MetricTile(
                        label = "仓位价值",
                        value = "${DecimalFormatters.formatCurrency(scheme.result?.positionValue)} USDT",
                        modifier = Modifier.weight(1f)
                    )
                }
                ComparisonInputRow {
                    if (scheme.settlementMode == SettlementMode.UsdtMargined) {
                        MetricTile(
                            label = "ROI",
                            value = DecimalFormatters.formatPercentage(scheme.result?.roiPercent),
                            valueColor = pnlColor(scheme.result?.roiPercent),
                            modifier = Modifier.weight(1f)
                        )
                        MetricTile(
                            label = "手续费估算",
                            value = "${DecimalFormatters.formatCurrency(scheme.result?.totalFee)} USDT",
                            modifier = Modifier.weight(1f)
                        )
                    } else {
                        MetricTile(
                            label = "币本位模式",
                            value = scheme.coinMarginedCalculationMode.label,
                            modifier = Modifier.weight(1f)
                        )
                        MetricTile(
                            label = "折算收益",
                            value = "${DecimalFormatters.formatPositiveNegative(scheme.comparablePnlUsdt())} USDT",
                            valueColor = pnlColor(scheme.comparablePnlUsdt()),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                if (scheme.settlementMode == SettlementMode.UsdtMargined) {
                    MetricTile(
                        label = "未扣手续费盈亏",
                        value = "${DecimalFormatters.formatCurrency(scheme.result?.grossPnl)} USDT",
                        valueColor = pnlColor(scheme.result?.grossPnl)
                    )
                }
                if (scheme.input.estimateLiquidation) {
                    comparisonLiquidationPrice(scheme.input, scheme.result)?.let {
                        MetricTile(
                            label = "强平价格",
                            value = "${DecimalFormatters.formatCurrency(it)} USDT"
                        )
                    }
                }
            }
        }
    }
}

private fun comparisonLiquidationPrice(input: CalculationInput, result: CalculationResult?): BigDecimal? {
    if (result?.liquidationPrice != null) return result.liquidationPrice
    val entryPrice = input.entryPrice ?: return null
    val leverage = input.leverage.takeIf { it > BigDecimal.ZERO } ?: return null
    val maintenanceRate = input.maintenanceMarginRatePercent.divide(BigDecimal("100"))
    val initialMarginRate = BigDecimal.ONE.divide(leverage, 18, RoundingMode.HALF_UP)
    return when (input.side) {
        PositionSide.Long -> entryPrice.multiply(BigDecimal.ONE - initialMarginRate + maintenanceRate)
            .takeIf { it > BigDecimal.ZERO }
        PositionSide.Short -> entryPrice.multiply(BigDecimal.ONE + initialMarginRate - maintenanceRate)
            .takeIf { it > BigDecimal.ZERO }
    }
}

@Composable
private fun ComparisonDifferenceCard(
    higher: ComparisonSchemeView,
    lower: ComparisonSchemeView,
    diff: BigDecimal?
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.72f),
        border = BorderStroke(2.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.72f))
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SchemePnlRow(higher)
            SchemePnlRow(lower)
            Text("收益差距", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(
                text = "${pnlText(diff, DecimalFormatters.formatPositiveNegative(diff))} USDT",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = pnlColor(diff)
            )
        }
    }
}

@Composable
private fun ComparisonRelativeDiffCard(
    scheme: ComparisonSchemeView,
    mainScheme: ComparisonSchemeView,
    diff: BigDecimal?
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.small,
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.50f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.32f))
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = "${scheme.name} 相对 ${mainScheme.name}",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "${pnlText(diff, DecimalFormatters.formatPositiveNegative(diff))} USDT",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = pnlColor(diff)
            )
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(mainScheme.name, style = MaterialTheme.typography.labelMedium)
                Text(mainScheme.primaryPnlText(), style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(scheme.name, style = MaterialTheme.typography.labelMedium)
                Text(scheme.primaryPnlText(), style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
private fun SchemePnlRow(scheme: ComparisonSchemeView) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(text = scheme.name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
            Text(
                text = scheme.primaryPnlLabel(),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Text(
            text = scheme.primaryPnlText(),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = pnlColor(scheme.primaryPnlValue())
        )
    }
}

private fun ComparisonSchemeView.comparablePnlUsdt(): BigDecimal? = when (settlementMode) {
    SettlementMode.UsdtMargined -> result?.netPnl
    SettlementMode.CoinMargined -> coinMarginedResult?.estimatedValueUsdt
}

private fun ComparisonSchemeView.primaryPnlValue(): BigDecimal? = when (settlementMode) {
    SettlementMode.UsdtMargined -> result?.netPnl
    SettlementMode.CoinMargined -> coinMarginedResult?.pnlCoin
}

private fun ComparisonSchemeView.primaryPnlLabel(): String = when (settlementMode) {
    SettlementMode.UsdtMargined -> "净收益"
    SettlementMode.CoinMargined -> "币本位收益"
}

private fun ComparisonSchemeView.primaryPnlText(): String = when (settlementMode) {
    SettlementMode.UsdtMargined ->
        "${DecimalFormatters.formatPositiveNegative(result?.netPnl)} USDT"

    SettlementMode.CoinMargined ->
        "${DecimalFormatters.formatPositiveNegative(coinMarginedResult?.pnlCoin)} $symbol"
}

private fun comparisonMissingFields(input: CalculationInput): List<String> = buildList {
    if (input.entryPrice == null || input.entryPrice <= BigDecimal.ZERO) add("开仓价")
    if (input.exitPrice == null || input.exitPrice <= BigDecimal.ZERO) add("平仓价")
    if (
        (input.margin == null || input.margin <= BigDecimal.ZERO) &&
        (input.quantity == null || input.quantity <= BigDecimal.ZERO)
    ) {
        add("保证金或币数量")
    }
}

private fun settlementModeLabel(mode: SettlementMode): String = when (mode) {
    SettlementMode.UsdtMargined -> "U 本位"
    SettlementMode.CoinMargined -> "币本位"
}

private fun comparisonSettlementDisplay(
    settlementMode: SettlementMode,
    coinMarginedCalculationMode: CoinMarginedCalculationMode
): String = when (settlementMode) {
    SettlementMode.UsdtMargined -> settlementModeLabel(settlementMode)
    SettlementMode.CoinMargined -> settlementModeLabel(settlementMode)
}

private fun comparisonSettlementHistoryDisplay(
    settlementMode: SettlementMode,
    coinMarginedCalculationMode: CoinMarginedCalculationMode
): String = when (settlementMode) {
    SettlementMode.UsdtMargined -> settlementModeLabel(settlementMode)
    SettlementMode.CoinMargined -> settlementModeLabel(settlementMode)
}

@Composable
private fun ComparisonInputRow(content: @Composable RowScope.() -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        content = content
    )
}

@Composable
private fun ComparisonSoftOutlinedButton(
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
private fun ComparisonTwoOptionModeSelector(
    firstText: String,
    secondText: String,
    firstSelected: Boolean,
    onFirstClick: () -> Unit,
    onSecondClick: () -> Unit
) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Button(
            onClick = onFirstClick,
            modifier = Modifier.weight(1f).height(34.dp),
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (firstSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                contentColor = if (firstSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
            ),
            shape = MaterialTheme.shapes.small
        ) { Text(firstText) }
        Button(
            onClick = onSecondClick,
            modifier = Modifier.weight(1f).height(34.dp),
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (!firstSelected) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.surfaceVariant,
                contentColor = if (!firstSelected) MaterialTheme.colorScheme.onSecondary else MaterialTheme.colorScheme.onSurfaceVariant
            ),
            shape = MaterialTheme.shapes.small
        ) { Text(secondText) }
    }
}

private fun PositionSide.label(): String = if (this == PositionSide.Long) "做多" else "做空"

private fun MarginMode.label(): String = if (this == MarginMode.Cross) "全仓" else "逐仓"
