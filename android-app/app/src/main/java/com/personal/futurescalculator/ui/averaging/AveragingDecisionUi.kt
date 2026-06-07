package com.personal.futurescalculator.ui.averaging

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.personal.futurescalculator.model.AveragingDecisionInput
import com.personal.futurescalculator.model.AveragingDecisionResult
import com.personal.futurescalculator.model.CalculationInput
import com.personal.futurescalculator.model.CalculationResult
import com.personal.futurescalculator.model.PositionSide
import com.personal.futurescalculator.ui.NumberInput
import com.personal.futurescalculator.ui.PositionSideSelector
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
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.small,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.52f)
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.28f))
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "补仓决策模拟",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "模拟补仓后均价、仓位和目标价收益变化。",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Button(
                onClick = onClick,
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.small
            ) {
                Text("打开补仓决策模拟")
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
        title = "补仓决策模拟",
        trailing = {
            TextButton(
                onClick = onCollapse,
                colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("收起", fontWeight = FontWeight.Bold)
            }
        }
    ) {
        Text(
            text = "模拟补仓后均价、仓位和目标价收益变化。",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        if (schemes.isNotEmpty()) {
            AveragingSoftOutlinedButton(
                onClick = { showSchemeDialog = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("选择已有方案填入")
            }
        }
        schemeFillMessage?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.small,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.50f))
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("输入", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                PositionSideSelector(
                    selectedSide = input.side,
                    onSideChange = { onInputChange(input.copy(side = it)) }
                )
                Text("当前持仓", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
                AveragingInputRow {
                    NumberInput(
                        value = input.currentEntryPrice,
                        onValueChange = { onInputChange(input.copy(currentEntryPrice = it)) },
                        label = "当前均价",
                        modifier = Modifier.weight(1f)
                    )
                    NumberInput(
                        value = input.currentQuantity,
                        onValueChange = { onInputChange(input.copy(currentQuantity = it)) },
                        label = "当前 $symbol 数量",
                        modifier = Modifier.weight(1f)
                    )
                }
                AveragingInputRow {
                    NumberInput(
                        value = input.currentMargin,
                        onValueChange = { onInputChange(input.copy(currentMargin = it)) },
                        label = "当前保证金",
                        modifier = Modifier.weight(1f)
                    )
                    NumberInput(
                        value = input.currentLeverage,
                        onValueChange = { onInputChange(input.copy(currentLeverage = it)) },
                        label = "当前杠杆",
                        modifier = Modifier.weight(1f)
                    )
                }
                Text("补仓计划", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
                NumberInput(
                    value = input.addEntryPrice,
                    onValueChange = { onInputChange(input.copy(addEntryPrice = it)) },
                    label = "补仓价格"
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    NumberInput(
                        value = input.addAmount,
                        onValueChange = { onInputChange(input.copy(addAmount = it, addQuantity = null)) },
                        label = "补仓金额 USDT",
                        modifier = Modifier.weight(1f)
                    )
                    Column(
                        modifier = Modifier
                            .width(20.dp)
                            .align(Alignment.CenterVertically),
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
                        value = input.addQuantity,
                        onValueChange = { onInputChange(input.copy(addQuantity = it, addAmount = null)) },
                        label = "补仓 $symbol 数量",
                        modifier = Modifier.weight(1f)
                    )
                }
                val addPrice = input.addEntryPrice
                val addLeverage = input.currentLeverage
                when {
                    input.addAmount != null &&
                        addPrice != null &&
                        addPrice > BigDecimal.ZERO &&
                        addLeverage != null &&
                        addLeverage > BigDecimal.ZERO -> {
                        Text(
                            text = "估算补仓数量：${DecimalFormatters.formatQuantity(input.addAmount.multiply(addLeverage).divide(addPrice, 16, RoundingMode.HALF_UP))} $symbol",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    input.addQuantity != null &&
                        addPrice != null &&
                        addPrice > BigDecimal.ZERO &&
                        addLeverage != null &&
                        addLeverage > BigDecimal.ZERO -> {
                        Text(
                            text = "估算补仓金额：${DecimalFormatters.formatCurrency(input.addQuantity.multiply(addPrice).divide(addLeverage, 16, RoundingMode.HALF_UP))} USDT",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
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
                label = "目标价收益变化",
                value = "${averagingPnlText(result.pnlChange, DecimalFormatters.formatPositiveNegative(result.pnlChange))} USDT",
                valueColor = averagingPnlColor(result.pnlChange),
                onClick = onRequestResult
            )
        } else {
            Text(
                text = "填写完整参数后显示补仓前后目标价收益对比。",
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
                AveragingDecisionResultCard(result, symbol)
                AveragingInputDetails(input, symbol)
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
                    text = "选择方案填入",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "选择后会填入当前持仓参数，仍可继续手动修改。",
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
    SectionPanel(title = "补仓参数") {
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
private fun AveragingDecisionResultCard(result: AveragingDecisionResult, symbol: String) {
    val palette = LocalProfitLossPalette.current
    val changeColor = if (result.pnlChange >= BigDecimal.ZERO) palette.profit else palette.loss

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.small,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("模拟结果", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
                color = changeColor.copy(alpha = 0.16f),
                border = BorderStroke(2.dp, changeColor.copy(alpha = 0.58f))
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "目标价收益变化",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = changeColor
                    )
                    Text(
                        text = "${averagingPnlText(result.pnlChange, DecimalFormatters.formatPositiveNegative(result.pnlChange))} USDT",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = changeColor
                    )
                    Text(
                        text = if (result.pnlChange >= BigDecimal.ZERO) "补仓后目标价收益更高" else "补仓后目标价收益降低",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                AveragingMetricTile(
                    label = "新平均开仓价",
                    value = "${DecimalFormatters.formatCurrency(result.newAveragePrice)} USDT",
                    modifier = Modifier.weight(1f)
                )
                AveragingMetricTile(
                    label = "新 $symbol 数量",
                    value = "${DecimalFormatters.formatQuantity(result.newQuantity)} $symbol",
                    modifier = Modifier.weight(1f)
                )
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                AveragingMetricTile(
                    label = "不补仓目标价收益",
                    value = "${averagingPnlText(result.pnlWithoutAdding, DecimalFormatters.formatPositiveNegative(result.pnlWithoutAdding))} USDT",
                    valueColor = averagingPnlColor(result.pnlWithoutAdding),
                    modifier = Modifier.weight(1f)
                )
                AveragingMetricTile(
                    label = "补仓后目标价收益",
                    value = "${averagingPnlText(result.pnlAfterAdding, DecimalFormatters.formatPositiveNegative(result.pnlAfterAdding))} USDT",
                    valueColor = averagingPnlColor(result.pnlAfterAdding),
                    modifier = Modifier.weight(1f)
                )
            }
            Text(
                text = "收益按目标平仓价估算，未扣手续费；结果仅供比较，不构成投资建议。",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
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
