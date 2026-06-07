package com.personal.futurescalculator.ui.history

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import com.personal.futurescalculator.ui.SectionPanel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HistoryScreen(
    records: List<HistoryRecord>,
    onToggleFavorite: (String) -> Unit,
    onDelete: (Set<String>) -> Unit,
    onClearCategory: (HistoryCategory?) -> Unit,
    onBack: () -> Unit
) {
    var category by rememberSaveable { mutableStateOf<HistoryCategory?>(null) }
    var selectedRecord by remember { mutableStateOf<HistoryRecord?>(null) }
    var selectedIds by rememberSaveable { mutableStateOf(emptySet<String>()) }
    BackHandler {
        if (selectedRecord != null) {
            selectedRecord = null
        } else {
            onBack()
        }
    }
    selectedRecord?.let { record ->
        HistoryDetailScreen(
            record = records.firstOrNull { it.id == record.id } ?: record,
            onToggleFavorite = onToggleFavorite,
            onDelete = {
                onDelete(setOf(record.id))
                selectedRecord = null
            },
            onBack = { selectedRecord = null }
        )
        return
    }
    val filtered = records.filter { category == null || it.category == category }
        .sortedWith(compareByDescending<HistoryRecord> { it.favorite }.thenByDescending { it.savedAt })
    HistoryPageLayout(title = "历史记录", onBack = onBack) {
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
            text = "仅在点击“保存本次结果”后保存，详情直接读取保存快照。",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        if (filtered.isEmpty()) {
            Text("暂无历史记录", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        filtered.forEach { record ->
            Surface(
                modifier = Modifier.fillMaxWidth().clickable { selectedRecord = record },
                shape = MaterialTheme.shapes.small,
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = record.id in selectedIds,
                        onCheckedChange = { checked ->
                            selectedIds = if (checked) selectedIds + record.id else selectedIds - record.id
                        }
                    )
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
        if (selectedIds.isNotEmpty()) {
            Button(onClick = { onDelete(selectedIds); selectedIds = emptySet() }, modifier = Modifier.fillMaxWidth()) {
                Text("删除选中记录（${selectedIds.size}）")
            }
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            HistorySoftOutlinedButton(onClick = { onClearCategory(category) }, modifier = Modifier.weight(1f)) {
                Text(if (category == null) "清空全部" else "清空分类")
            }
            HistorySoftOutlinedButton(onClick = onBack, modifier = Modifier.weight(1f)) { Text("返回") }
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
    onDelete: () -> Unit,
    onBack: () -> Unit
) {
    HistoryPageLayout(title = "历史详情", onBack = onBack) {
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
        HistorySoftOutlinedButton(onClick = onDelete, modifier = Modifier.fillMaxWidth()) { Text("删除记录") }
    }
}

@Composable
private fun HistoryPageLayout(
    title: String,
    onBack: () -> Unit,
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
            HistorySoftOutlinedButton(
                onClick = onBack,
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.small
            ) {
                Text("返回设置")
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