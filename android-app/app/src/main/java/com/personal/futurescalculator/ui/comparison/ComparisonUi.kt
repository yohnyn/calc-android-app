package com.personal.futurescalculator.ui.comparison

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
import androidx.compose.material3.DropdownMenu
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.personal.futurescalculator.model.AmountField
import com.personal.futurescalculator.model.CalculationInput
import com.personal.futurescalculator.model.CalculationResult
import com.personal.futurescalculator.model.CoinAsset
import com.personal.futurescalculator.model.CoinMarginedCalculationMode
import com.personal.futurescalculator.model.ComparisonItem
import com.personal.futurescalculator.model.ComparisonResult
import com.personal.futurescalculator.model.HistoryCategory
import com.personal.futurescalculator.model.HistoryField
import com.personal.futurescalculator.model.HistoryRecord
import com.personal.futurescalculator.model.HistorySection
import com.personal.futurescalculator.model.MarginMode
import com.personal.futurescalculator.model.PositionSide
import com.personal.futurescalculator.model.SettlementMode
import com.personal.futurescalculator.ui.CompactTextInput
import com.personal.futurescalculator.ui.LeverageSelector
import com.personal.futurescalculator.ui.MarginModeSelector
import com.personal.futurescalculator.ui.NumberInput
import com.personal.futurescalculator.ui.PositionSideSelector
import com.personal.futurescalculator.ui.coin.CoinIcon
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
    val isMain: Boolean
)

fun buildComparisonSchemes(
    mainInput: CalculationInput,
    mainResult: CalculationResult?,
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
        ?.joinToString("；", prefix = "请先填写完整参数后再查看收益对比：")
}

fun selectedComparisonValidationMessage(schemes: List<ComparisonSchemeView>): String? {
    if (schemes.size < 2) return "请至少选择两个完整方案进行对比。"
    val invalid = schemes.mapNotNull { scheme ->
        val missing = comparisonMissingFields(scheme.input)
        when {
            missing.isNotEmpty() -> "${scheme.name}缺少${missing.joinToString("、")}"
            scheme.result?.netPnl == null -> "${scheme.name}无法计算结果"
            else -> null
        }
    }
    return invalid.takeIf { it.isNotEmpty() }?.joinToString(prefix = "参数不完整：", separator = "；")
}

@Composable
fun CopySchemeDialog(
    schemes: List<ComparisonSchemeView>,
    onSelect: (ComparisonSchemeView) -> Unit,
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
                        modifier = Modifier.fillMaxWidth().clickable { onSelect(scheme) },
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
                                    "${scheme.input.side.label()} · ${scheme.input.leverage.stripTrailingZeros().toPlainString()}x · 净盈亏 ${DecimalFormatters.formatPositiveNegative(scheme.result?.netPnl)} USDT",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Text("复制", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
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
                Text("收益对比完成", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                ComparisonResultsOverview(schemes)
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
    onSelectedChange: (Boolean) -> Unit,
    onClick: (() -> Unit)?
) {
    val result = scheme.result
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
            Checkbox(checked = selected, onCheckedChange = onSelectedChange)
            CoinIcon(coin = scheme.coin, size = 32)
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "${scheme.name} · ${scheme.symbol}",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold
                )
                if (result?.netPnl != null) {
                    Text(
                        text = "净收益 ${pnlText(result.netPnl, DecimalFormatters.formatPositiveNegative(result.netPnl))} USDT",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = pnlColor(result.netPnl)
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
    onSave: (ComparisonItem) -> Unit,
    onDelete: (ComparisonItem) -> Unit,
    onDismiss: () -> Unit
) {
    var item by remember(initialItem.id) { mutableStateOf(initialItem) }
    var showCoinDialog by remember { mutableStateOf(false) }
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
                modifier = Modifier.padding(14.dp).verticalScroll(rememberScrollState()),
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
                        modifier = Modifier.widthIn(max = 220.dp),
                        label = "方案名称"
                    )
                }
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ComparisonSoftOutlinedButton(onClick = { showCoinDialog = true }, modifier = Modifier.weight(1f)) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CoinIcon(coin = coin, size = 24)
                                Text("${coin?.symbol ?: "币"} ▼")
                            }
                        }
                        ComparisonDropdownSelector(
                            text = "${settlementModeLabel(item.settlementMode)} ▼",
                            modifier = Modifier.weight(1f),
                            options = listOf(
                                "U 本位" to { item = item.copy(settlementMode = SettlementMode.UsdtMargined) },
                                "币本位" to { item = item.copy(settlementMode = SettlementMode.CoinMargined) }
                            )
                        )
                    }
                    if (item.settlementMode == SettlementMode.CoinMargined) {
                        ComparisonDropdownSelector(
                            text = "${coinMarginedCalculationModeShortLabel(item.coinMarginedCalculationMode)}模式 ▼",
                            modifier = Modifier.fillMaxWidth(),
                            options = listOf(
                                "币数量模式" to {
                                    item = item.copy(coinMarginedCalculationMode = CoinMarginedCalculationMode.CoinQuantity)
                                },
                                "反向合约模式" to {
                                    item = item.copy(coinMarginedCalculationMode = CoinMarginedCalculationMode.InverseContract)
                                }
                            )
                        )
                    }
                }
                ComparisonEditorSection(title = "交易设置") {
                    PositionSideSelector(item.input.side, { item = item.copy(input = item.input.copy(side = it)) })
                    MarginModeSelector(item.input.marginMode, { item = item.copy(input = item.input.copy(marginMode = it)) })
                    LeverageSelector(item.input.leverage, { item = item.copy(input = item.input.copy(leverage = it)) })
                }
                ComparisonEditorSection(title = "价格与仓位") {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        NumberInput(
                            value = item.input.margin,
                            onValueChange = { item = item.copy(input = item.input.copy(margin = it, quantity = null), lastEditedAmountField = AmountField.Margin) },
                            label = "保证金",
                            modifier = Modifier.weight(1f)
                        )
                        Column(
                            modifier = Modifier.width(20.dp),
                            verticalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Spacer(modifier = Modifier.height(14.dp))
                            Box(modifier = Modifier.height(40.dp), contentAlignment = Alignment.Center) {
                                Text(
                                    text = "或",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                        NumberInput(
                            value = item.input.quantity,
                            onValueChange = { item = item.copy(input = item.input.copy(quantity = it, margin = null), lastEditedAmountField = AmountField.Quantity) },
                            label = "${coin?.symbol ?: "币"} 数量",
                            modifier = Modifier.weight(1f)
                        )
                    }
                    ComparisonInputRow {
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
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End)
                ) {
                    TextButton(
                        onClick = { onDelete(item) },
                        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                    ) { Text("删除", fontWeight = FontWeight.SemiBold) }
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

fun createComparisonHistorySnapshot(schemes: List<ComparisonSchemeView>): HistoryRecord {
    val sorted = schemes.sortedByDescending { it.result?.netPnl }
    val differences = sorted.zipWithNext().map { (higher, lower) ->
        HistoryField(
            "${higher.name} 与 ${lower.name}",
            "${higher.name} 多 ${DecimalFormatters.formatPositiveNegative(higher.result!!.netPnl!! - lower.result!!.netPnl!!)} USDT"
        )
    }
    return HistoryRecord(
        id = "history_${System.currentTimeMillis()}",
        category = HistoryCategory.SchemeComparison,
        title = "${schemes.size} 个方案收益对比",
        summary = schemes.firstOrNull()?.let { "${it.name} ${DecimalFormatters.formatPositiveNegative(it.result?.netPnl)} USDT" } ?: "无结果",
        roiSummary = null,
        savedAt = System.currentTimeMillis(),
        sections = schemes.map { scheme ->
            HistorySection("${scheme.name} · ${scheme.symbol}", listOf(
                HistoryField("结算模式", comparisonSettlementHistoryDisplay(scheme.settlementMode, scheme.coinMarginedCalculationMode)),
                HistoryField("币本位计算方式", if (scheme.settlementMode == SettlementMode.CoinMargined) scheme.coinMarginedCalculationMode.label else "不适用"),
                HistoryField("方向", scheme.input.side.label()),
                HistoryField("模式", scheme.input.marginMode.label()),
                HistoryField("杠杆", "${scheme.input.leverage.stripTrailingZeros().toPlainString()}x"),
                HistoryField("保证金", "${DecimalFormatters.formatCurrency(scheme.result?.requiredMargin)} USDT"),
                HistoryField("数量", "${DecimalFormatters.formatQuantity(scheme.result?.quantity)} ${scheme.symbol}"),
                HistoryField("开仓价", "${DecimalFormatters.formatCurrency(scheme.input.entryPrice)} USDT"),
                HistoryField("平仓价", "${DecimalFormatters.formatCurrency(scheme.input.exitPrice)} USDT"),
                HistoryField("净收益", "${DecimalFormatters.formatPositiveNegative(scheme.result?.netPnl)} USDT"),
                HistoryField("ROI", DecimalFormatters.formatPercentage(scheme.result?.roiPercent)),
                HistoryField("手续费", "${DecimalFormatters.formatCurrency(scheme.result?.totalFee)} USDT"),
                HistoryField("强平价", scheme.result?.liquidationPrice?.let { "${DecimalFormatters.formatCurrency(it)} USDT" } ?: "无法可靠估算")
            ))
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
            Text(text)
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { (label, onSelect) ->
                DropdownMenuItem(
                    text = { Text(label) },
                    onClick = {
                        expanded = false
                        onSelect()
                    }
                )
            }
        }
    }
}

@Composable
private fun ComparisonEditorSection(
    title: String,
    supporting: String? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.small,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.34f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.12f))
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold
            )
            if (supporting != null) {
                Text(
                    text = supporting,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            content()
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
            schemes.map { it.toComparisonSummary() }.forEach { scheme ->
                ComparisonSchemeSummary(scheme)
            }
        }
    }
}

private data class RankedComparisonScheme(
    val id: String,
    val netRank: Int?,
    val roiRank: Int?,
    val name: String,
    val symbol: String,
    val coin: CoinAsset?,
    val settlementMode: SettlementMode,
    val coinMarginedCalculationMode: CoinMarginedCalculationMode,
    val input: CalculationInput,
    val result: CalculationResult?
)

private fun ComparisonSchemeView.toComparisonSummary(): RankedComparisonScheme = RankedComparisonScheme(
    id = id,
    netRank = null,
    roiRank = null,
    name = name,
    symbol = symbol,
    coin = coin,
    settlementMode = settlementMode,
    coinMarginedCalculationMode = coinMarginedCalculationMode,
    input = input,
    result = result
)

@Composable
private fun ComparisonDifferenceSummary(schemes: List<ComparisonSchemeView>) {
    val sorted = schemes.filter { it.result?.netPnl != null }.sortedByDescending { it.result?.netPnl }
    sorted.zipWithNext().forEach { (higher, lower) ->
        val diff = higher.result!!.netPnl!! - lower.result!!.netPnl!!
        ComparisonFormulaCard(
            title = "${higher.name} 与 ${lower.name} 收益差距",
            leftPnl = higher.result.netPnl,
            rightPnl = lower.result.netPnl,
            diff = diff,
            summary = "${higher.name} 比 ${lower.name} 多 ${DecimalFormatters.formatPositiveNegative(diff)} USDT"
        )
    }
}

@Composable
private fun ComparisonSchemeSummary(scheme: RankedComparisonScheme) {
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
                label = "净收益",
                value = "${pnlText(scheme.result?.netPnl, DecimalFormatters.formatPositiveNegative(scheme.result?.netPnl))} USDT",
                valueColor = pnlColor(scheme.result?.netPnl)
            )
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
                    MetricTile(
                        label = "ROI",
                        value = DecimalFormatters.formatPercentage(scheme.result?.roiPercent),
                        valueColor = pnlColor(scheme.result?.roiPercent),
                        modifier = Modifier.weight(1f)
                    )
                    MetricTile(
                        label = "总手续费约",
                        value = "${DecimalFormatters.formatCurrency(scheme.result?.totalFee)} USDT",
                        modifier = Modifier.weight(1f)
                    )
                }
                MetricTile(
                    label = "未扣手续费盈亏",
                    value = "${DecimalFormatters.formatCurrency(scheme.result?.grossPnl)} USDT",
                    valueColor = pnlColor(scheme.result?.grossPnl)
                )
                MetricTile(
                    label = "估算强平价",
                    value = comparisonLiquidationPrice(scheme.input, scheme.result)?.let { "${DecimalFormatters.formatCurrency(it)} USDT" }
                        ?: "无法可靠估算"
                )
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
private fun ComparisonFormulaCard(
    title: String,
    leftPnl: BigDecimal?,
    rightPnl: BigDecimal?,
    diff: BigDecimal?,
    summary: String
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.72f),
        border = BorderStroke(2.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.72f))
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(text = title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(
                text = "${DecimalFormatters.formatCurrency(leftPnl)} - ${DecimalFormatters.formatCurrency(rightPnl)} = ${pnlText(diff, DecimalFormatters.formatPositiveNegative(diff))} USDT",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Text(
                text = summary,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = pnlColor(diff)
            )
        }
    }
}

@Composable
private fun SchemePnlRow(name: String, netPnl: BigDecimal?) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
        Text(
            text = "${pnlText(netPnl, DecimalFormatters.formatPositiveNegative(netPnl))} USDT",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = pnlColor(netPnl)
        )
    }
}

private fun comparisonDiffSentence(
    itemName: String,
    referenceName: String = "方案 1",
    referenceNetPnl: BigDecimal?,
    comparisonNetPnl: BigDecimal?,
    comparisonMinusReference: BigDecimal?
): String {
    if (comparisonMinusReference == null || referenceNetPnl == null || comparisonNetPnl == null) {
        return "$itemName 与$referenceName 暂无可比收益差距"
    }

    val amountText = "${DecimalFormatters.formatCurrency(comparisonMinusReference.abs())} USDT"
    val lossDetail = if (comparisonNetPnl < BigDecimal.ZERO) {
        "，且$itemName 亏损 ${DecimalFormatters.formatCurrency(comparisonNetPnl.abs())} USDT"
    } else {
        ""
    }

    return when {
        comparisonMinusReference > BigDecimal.ZERO && comparisonNetPnl < BigDecimal.ZERO ->
            "$itemName 比$referenceName 少亏 $amountText$lossDetail"
        comparisonMinusReference > BigDecimal.ZERO && referenceNetPnl < BigDecimal.ZERO ->
            "$itemName 相比$referenceName 净收益提高 $amountText"
        comparisonMinusReference > BigDecimal.ZERO ->
            "$itemName 比$referenceName 多赚 $amountText"
        comparisonMinusReference < BigDecimal.ZERO && comparisonNetPnl < BigDecimal.ZERO && referenceNetPnl < BigDecimal.ZERO ->
            "$itemName 比$referenceName 多亏 $amountText$lossDetail"
        comparisonMinusReference < BigDecimal.ZERO && comparisonNetPnl < BigDecimal.ZERO ->
            "$itemName 相比$referenceName 净收益减少 $amountText$lossDetail"
        comparisonMinusReference < BigDecimal.ZERO ->
            "$itemName 比$referenceName 少赚 $amountText"
        else -> "$itemName 与$referenceName 净盈亏相同$lossDetail"
    }
}

fun findOptimalScheme(
    currentResult: CalculationResult?,
    comparisons: List<ComparisonResult>
): Pair<String, BigDecimal>? {
    val candidates = buildList {
        currentResult?.netPnl?.let { add("方案 1" to it) }
        comparisons.forEach { comparison ->
            comparison.result?.netPnl?.let { add(comparison.item.name to it) }
        }
    }

    return candidates.maxByOrNull { it.second }
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

private fun coinMarginedCalculationModeShortLabel(mode: CoinMarginedCalculationMode): String = when (mode) {
    CoinMarginedCalculationMode.CoinQuantity -> "币数量"
    CoinMarginedCalculationMode.InverseContract -> "反向合约"
}

private fun comparisonSettlementDisplay(
    settlementMode: SettlementMode,
    coinMarginedCalculationMode: CoinMarginedCalculationMode
): String = when (settlementMode) {
    SettlementMode.UsdtMargined -> settlementModeLabel(settlementMode)
    SettlementMode.CoinMargined -> "${settlementModeLabel(settlementMode)}(${coinMarginedCalculationModeShortLabel(coinMarginedCalculationMode)})"
}

private fun comparisonSettlementHistoryDisplay(
    settlementMode: SettlementMode,
    coinMarginedCalculationMode: CoinMarginedCalculationMode
): String = when (settlementMode) {
    SettlementMode.UsdtMargined -> settlementModeLabel(settlementMode)
    SettlementMode.CoinMargined -> "${settlementModeLabel(settlementMode)}（${coinMarginedCalculationModeShortLabel(coinMarginedCalculationMode)}）"
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
