package com.personal.futurescalculator.ui.history

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
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
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.personal.futurescalculator.model.HistoryCategory
import com.personal.futurescalculator.model.HistoryRecord
import com.personal.futurescalculator.model.CoinAsset
import com.personal.futurescalculator.model.MarginMode
import com.personal.futurescalculator.model.PositionSide
import com.personal.futurescalculator.model.SavedPlan
import com.personal.futurescalculator.model.SettlementMode
import com.personal.futurescalculator.ui.SectionPanel
import com.personal.futurescalculator.util.DecimalFormatters
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HistoryScreen(
    records: List<HistoryRecord>,
    plans: List<SavedPlan>,
    coins: List<CoinAsset>,
    onToggleFavorite: (String) -> Unit,
    onDelete: (Set<String>) -> Unit,
    onClearCategory: (HistoryCategory?) -> Unit,
    onOpenPlan: (SavedPlan) -> Unit,
    onAddPlanToComparison: (SavedPlan) -> Unit,
    onDeletePlan: (String) -> Unit,
    onBack: () -> Unit
) {
    var tab by rememberSaveable { mutableStateOf(HistoryTab.Records) }
    var category by rememberSaveable { mutableStateOf<HistoryCategory?>(null) }
    var selectedRecord by remember { mutableStateOf<HistoryRecord?>(null) }
    var selectedIds by rememberSaveable { mutableStateOf(emptySet<String>()) }
    var showDeleteConfirm by rememberSaveable { mutableStateOf(false) }
    BackHandler {
        if (selectedRecord != null) {
            selectedRecord = null
        } else if (selectedIds.isNotEmpty()) {
            selectedIds = emptySet()
        } else {
            onBack()
        }
    }
    selectedRecord?.let { record ->
        HistoryDetailScreen(
            record = records.firstOrNull { it.id == record.id } ?: record,
            onToggleFavorite = onToggleFavorite,
            onBack = { selectedRecord = null }
        )
        return
    }
    val filtered = records.filter { category == null || it.category == category }
        .sortedWith(compareByDescending<HistoryRecord> { it.favorite }.thenByDescending { it.savedAt })
    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("确认删除") },
            text = { Text("将删除选中的 ${selectedIds.size} 条历史记录。") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDelete(selectedIds)
                        selectedIds = emptySet()
                        showDeleteConfirm = false
                    }
                ) { Text("删除", fontWeight = FontWeight.SemiBold) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("取消", fontWeight = FontWeight.SemiBold)
                }
            }
        )
    }
    HistoryPageLayout(title = "历史记录", onBack = onBack, showBottomBack = false) {
        HistoryTopTabs(
            selected = tab,
            onSelect = {
                selectedIds = emptySet()
                tab = it
            }
        )
        if (tab == HistoryTab.Plans) {
            PlanLibrarySection(
                plans = plans,
                coins = coins,
                onOpenPlan = onOpenPlan,
                onAddPlanToComparison = onAddPlanToComparison,
                onDeletePlan = onDeletePlan,
                onBack = onBack
            )
            return@HistoryPageLayout
        }
        if (records.isEmpty()) {
            EmptyHistoryState(onBack = onBack)
            return@HistoryPageLayout
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            HistoryCategoryButton("全部", category == null, { category = null }, Modifier.weight(1f))
            HistoryCategoryButton(HistoryCategory.ProfitCalculation.label, category == HistoryCategory.ProfitCalculation, { category = HistoryCategory.ProfitCalculation }, Modifier.weight(1f))
            HistoryCategoryButton(HistoryCategory.SchemeComparison.label, category == HistoryCategory.SchemeComparison, { category = HistoryCategory.SchemeComparison }, Modifier.weight(1f))
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            HistoryCategoryButton(HistoryCategory.AveragingSimulation.label, category == HistoryCategory.AveragingSimulation, { category = HistoryCategory.AveragingSimulation }, Modifier.weight(1f))
            HistoryCategoryButton(HistoryCategory.TargetProfitReverse.label, category == HistoryCategory.TargetProfitReverse, { category = HistoryCategory.TargetProfitReverse }, Modifier.weight(1f))
            HistoryCategoryButton(HistoryCategory.StopLossReverse.label, category == HistoryCategory.StopLossReverse, { category = HistoryCategory.StopLossReverse }, Modifier.weight(1f))
        }
        Text(
            text = "有效计算会自动记录快照，详情直接读取保存时数据。",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        if (filtered.isEmpty()) {
            Text("当前分类暂无历史记录", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        filtered.forEach { record ->
            HistoryRecordRow(
                record = record,
                selected = record.id in selectedIds,
                selectionMode = selectedIds.isNotEmpty(),
                onClick = {
                    if (selectedIds.isNotEmpty()) {
                        selectedIds = if (record.id in selectedIds) selectedIds - record.id else selectedIds + record.id
                    } else {
                        selectedRecord = record
                    }
                },
                onLongClick = { selectedIds = selectedIds + record.id }
            )
        }
        if (selectedIds.isNotEmpty()) {
            Button(onClick = { showDeleteConfirm = true }, modifier = Modifier.fillMaxWidth()) {
                Text("删除选中记录（${selectedIds.size}）")
            }
            HistorySoftOutlinedButton(onClick = { selectedIds = emptySet() }, modifier = Modifier.fillMaxWidth()) {
                Text("取消选择")
            }
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            if (filtered.isNotEmpty()) {
                HistorySoftOutlinedButton(onClick = { onClearCategory(category) }, modifier = Modifier.weight(1f)) {
                    Text(if (category == null) "清空全部" else "清空分类")
                }
            }
            HistorySoftOutlinedButton(onClick = onBack, modifier = Modifier.weight(1f)) { Text("返回计算器") }
        }
    }
}

private enum class HistoryTab {
    Records,
    Plans
}

@Composable
private fun HistoryTopTabs(
    selected: HistoryTab,
    onSelect: (HistoryTab) -> Unit
) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        HistoryCategoryButton("历史记录", selected == HistoryTab.Records, { onSelect(HistoryTab.Records) }, Modifier.weight(1f))
        HistoryCategoryButton("方案库", selected == HistoryTab.Plans, { onSelect(HistoryTab.Plans) }, Modifier.weight(1f))
    }
}

@Composable
private fun PlanLibrarySection(
    plans: List<SavedPlan>,
    coins: List<CoinAsset>,
    onOpenPlan: (SavedPlan) -> Unit,
    onAddPlanToComparison: (SavedPlan) -> Unit,
    onDeletePlan: (String) -> Unit,
    onBack: () -> Unit
) {
    if (plans.isEmpty()) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.small,
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.42f),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.14f))
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("暂无保存的开仓方案", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(
                    "保存开仓方案后会显示在这里",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
                Button(onClick = onBack, modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.small) {
                    Text("返回计算器")
                }
            }
        }
        return
    }
    plans.forEach { plan ->
        val coin = coins.firstOrNull { it.id == plan.coinId }
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.small,
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.48f),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.14f))
        ) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(plan.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(
                    "${coin?.symbol ?: "币"} · ${if (plan.settlementMode == SettlementMode.UsdtMargined) "U 本位" else "币本位"} · ${plan.input.side.label()} · ${plan.input.marginMode.label()} · ${plan.input.leverage.stripTrailingZeros().toPlainString()}x",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    "开仓价 ${DecimalFormatters.formatCurrency(plan.input.entryPrice)} · 平仓价 ${DecimalFormatters.formatCurrency(plan.input.exitPrice)}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = { onOpenPlan(plan) }, modifier = Modifier.weight(1f), shape = MaterialTheme.shapes.small) {
                        Text("打开")
                    }
                    HistorySoftOutlinedButton(onClick = { onAddPlanToComparison(plan) }, modifier = Modifier.weight(1f)) {
                        Text("加入对比")
                    }
                    TextButton(
                        onClick = { onDeletePlan(plan.id) },
                        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("删除", fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
    HistorySoftOutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
        Text("返回计算器")
    }
}

@Composable
private fun EmptyHistoryState(onBack: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.small,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.42f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.14f))
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("暂无历史记录", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(
                "完成一次有效计算后会自动记录快照",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Button(onClick = onBack, modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.small) {
                Text("返回计算器")
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun HistoryRecordRow(
    record: HistoryRecord,
    selected: Boolean,
    selectionMode: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            ),
        shape = MaterialTheme.shapes.small,
        color = if (selected) {
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.52f)
        } else {
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        }
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (selectionMode) {
                Checkbox(
                    checked = selected,
                    onCheckedChange = { onClick() }
                )
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text("${if (record.favorite) "★ " else ""}${record.title}", fontWeight = FontWeight.Bold)
                Text(
                    "${record.summary}${record.roiSummary?.let { " · ROI $it" }.orEmpty()}",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(formatTimestamp(record.savedAt), style = MaterialTheme.typography.labelSmall)
            }
            Text(record.category.label, style = MaterialTheme.typography.labelSmall)
        }
    }
}

@Composable
private fun HistoryCategoryButton(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(36.dp),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 4.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
            contentColor = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
        )
    ) { Text(text, style = MaterialTheme.typography.labelSmall) }
}

@Composable
private fun HistoryDetailScreen(
    record: HistoryRecord,
    onToggleFavorite: (String) -> Unit,
    onBack: () -> Unit
) {
    HistoryPageLayout(title = "历史详情", onBack = onBack, backLabel = "返回历史记录") {
        Text(record.title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text("${record.category.label} · ${formatTimestamp(record.savedAt)}", color = MaterialTheme.colorScheme.onSurfaceVariant)
        record.sections.forEach { section ->
            SectionPanel(title = section.title) {
                section.fields.forEach { field ->
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(field.label, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(field.value, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.End, modifier = Modifier.weight(1f).padding(start = 12.dp))
                    }
                }
            }
        }
        Button(onClick = { onToggleFavorite(record.id) }, modifier = Modifier.fillMaxWidth()) {
            Text(if (record.favorite) "取消收藏" else "收藏")
        }
    }
}

@Composable
private fun HistoryPageLayout(
    title: String,
    onBack: () -> Unit,
    showBottomBack: Boolean = true,
    backLabel: String = "返回计算器",
    content: @Composable () -> Unit
) {
    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(text = title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            content()
            if (showBottomBack) {
                HistorySoftOutlinedButton(
                    onClick = onBack,
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(backLabel)
                }
            }
        }
    }
}

@Composable
private fun HistorySoftOutlinedButton(
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

private fun formatTimestamp(timestamp: Long): String {
    return SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date(timestamp))
}

private fun PositionSide.label(): String = if (this == PositionSide.Long) "做多" else "做空"

private fun MarginMode.label(): String = if (this == MarginMode.Cross) "全仓" else "逐仓"
