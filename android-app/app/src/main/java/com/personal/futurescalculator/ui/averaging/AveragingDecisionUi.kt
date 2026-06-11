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
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.personal.futurescalculator.model.AveragingDecisionInput
import com.personal.futurescalculator.model.AveragingDecisionResult
import com.personal.futurescalculator.model.CalculationInput
import com.personal.futurescalculator.model.CalculationResult
import com.personal.futurescalculator.model.PositionSide
import com.personal.futurescalculator.model.SettlementMode
import com.personal.futurescalculator.ui.DropdownChevronIcon
import com.personal.futurescalculator.ui.NumberInput
import com.personal.futurescalculator.ui.SectionPanel
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
    if (input.targetExitPrice == null || input.targetExitPrice <= BigDecimal.ZERO) add("目标平仓价")
}

@Composable
fun AveragingDecisionEntryCard(onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = MaterialTheme.shapes.small,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.18f))
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "补仓助手",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
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
    }
}

@Composable
fun AveragingDecisionSection(
    input: AveragingDecisionInput,
    result: AveragingDecisionResult?,
    showResultCard: Boolean,
    schemes: List<ExistingScheme>,
    symbol: String,
    settlementMode: SettlementMode,
    onInputChange: (AveragingDecisionInput) -> Unit,
    onCollapse: () -> Unit,
    onSchemeFilled: (ExistingScheme) -> Unit,
    onRequestResult: () -> Unit
) {
    var showSchemeDialog by remember { mutableStateOf(false) }
    var schemeFillMessage by remember { mutableStateOf<String?>(null) }

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
                schemeFillMessage = "已填入${scheme.name}"
                showSchemeDialog = false
            },
            onDismiss = { showSchemeDialog = false }
        )
    }

    SectionPanel(
        title = "补仓助手",
        trailing = {
            TextButton(
                onClick = onCollapse,
                colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("收起", fontWeight = FontWeight.Bold)
            }
        }
    ) {
        if (schemes.isNotEmpty()) {
            AveragingSoftOutlinedButton(
                onClick = { showSchemeDialog = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("从方案库选择")
            }
        }
        schemeFillMessage?.let { Text(text = it, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant) }
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.small,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.50f))
        ) {
            Column(
                modifier = Modifier.padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CurrentPositionSummary(input = input, symbol = symbol)
                NumberInput(
                    value = input.addEntryPrice,
                    onValueChange = { onInputChange(input.copy(addEntryPrice = it)) },
                    label = "补仓价格"
                )
                NumberInput(
                    value = input.addAmount,
                    onValueChange = { onInputChange(input.copy(addAmount = it, addQuantity = null)) },
                    label = "补仓金额 USDT"
                )
                NumberInput(
                    value = input.targetExitPrice,
                    onValueChange = { onInputChange(input.copy(targetExitPrice = it)) },
                    label = "目标平仓价"
                )
                Button(
                    onClick = onRequestResult,
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.small
                ) {
                    Text("查看补仓结果")
                }
            }
        }

        if (result != null && showResultCard) {
            AveragingCompactExpandableResultCard(
                label = "补仓结果",
                value = "${DecimalFormatters.formatPositiveNegative(result.pnlChange)} USDT",
                valueColor = averagingPnlColor(result.pnlChange),
                onClick = onRequestResult
            )
        }
    }
}

@Composable
private fun CurrentPositionSummary(input: AveragingDecisionInput, symbol: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.small,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.14f))
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text("当前仓位", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
            Text(
                text = "${input.side.label()} · ${DecimalFormatters.formatQuantity(input.currentLeverage)}x · 均价 ${DecimalFormatters.formatCurrency(input.currentEntryPrice)} · 数量 ${DecimalFormatters.formatQuantity(input.currentQuantity)} $symbol",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
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
                    text = "选择后会带入方案库中的当前持仓参数。",
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
                                text = "开仓价 ${DecimalFormatters.formatCurrency(scheme.input.entryPrice)} USDT · ${scheme.symbol} 数量 ${DecimalFormatters.formatQuantity(scheme.result.quantity)}",
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
            AveragingMetricTile("当前 $symbol 数量", "${DecimalFormatters.formatQuantity(input.currentQuantity)} $symbol", modifier = Modifier.weight(1f))
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
                ?: "${DecimalFormatters.formatQuantity(input.addQuantity)} $symbol"
        )
    }
}

@Composable
private fun AveragingDecisionResultCard(
    input: AveragingDecisionInput,
    result: AveragingDecisionResult,
    symbol: String
) {
    val palette = LocalProfitLossPalette.current
    val changeColor = if (result.pnlChange >= BigDecimal.ZERO) palette.profit else palette.loss
    var detailsExpanded by rememberSaveable { mutableStateOf(false) }
    val totalMargin = (input.currentMargin ?: BigDecimal.ZERO) + result.addAmount
    val targetRoi = if (totalMargin > BigDecimal.ZERO) {
        result.pnlAfterAdding.multiply(BigDecimal("100")).divide(totalMargin, 8, RoundingMode.HALF_UP)
    } else {
        null
    }
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
                value = "${DecimalFormatters.formatCurrency(result.newAveragePrice)} USDT"
            )
            AveragingMetricTile(
                label = "目标价收益",
                value = "${averagingPnlText(result.pnlAfterAdding, DecimalFormatters.formatPositiveNegative(result.pnlAfterAdding))} USDT",
                valueColor = averagingPnlColor(result.pnlAfterAdding)
            )
            AveragingMetricTile(
                label = "相比不补仓",
                value = "${averagingPnlText(result.pnlChange, DecimalFormatters.formatPositiveNegative(result.pnlChange))} USDT",
                valueColor = changeColor
            )
            TextButton(onClick = { detailsExpanded = !detailsExpanded }) {
                Text(if (detailsExpanded) "收起详细数据 ▲" else "查看详细数据 ▼", fontWeight = FontWeight.SemiBold)
            }
            if (detailsExpanded) {
                AveragingInputRow {
                    AveragingMetricTile("补仓数量", "${DecimalFormatters.formatQuantity(result.quantityIncrease)} $symbol", modifier = Modifier.weight(1f))
                    AveragingMetricTile("补仓金额", "${DecimalFormatters.formatCurrency(result.addAmount)} USDT", modifier = Modifier.weight(1f))
                }
                AveragingInputRow {
                    AveragingMetricTile("新仓位价值", "${DecimalFormatters.formatCurrency(newPositionValue)} USDT", modifier = Modifier.weight(1f))
                    targetRoi?.let {
                        AveragingMetricTile("保证金收益率（ROI）", DecimalFormatters.formatPercentage(it), modifier = Modifier.weight(1f))
                    }
                }
                AveragingInputRow {
                    AveragingMetricTile("当前均价", "${DecimalFormatters.formatCurrency(input.currentEntryPrice)} USDT", modifier = Modifier.weight(1f))
                    AveragingMetricTile("当前保证金", "${DecimalFormatters.formatCurrency(input.currentMargin)} USDT", modifier = Modifier.weight(1f))
                }
                AveragingMetricTile("当前杠杆", "${DecimalFormatters.formatQuantity(input.currentLeverage)}x")
            }
            Text(
                text = "仅表示目标价下的收益差值",
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
private fun AveragingCompactExpandableResultCard(
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
                "展开",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun AveragingMetricTile(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    supporting: String? = null,
    valueColor: Color = MaterialTheme.colorScheme.onSurface
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.small,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.48f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.12f))
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
                style = MaterialTheme.typography.titleSmall,
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

private fun SettlementMode.shortLabel(): String = when (this) {
    SettlementMode.UsdtMargined -> "U 本位"
    SettlementMode.CoinMargined -> "币本位"
}
