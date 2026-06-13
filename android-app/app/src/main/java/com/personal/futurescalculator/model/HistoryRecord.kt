package com.personal.futurescalculator.model

enum class HistoryCategory(val label: String) {
    ProfitCalculation("收益测算"),
    SchemeComparison("方案对比"),
    AveragingSimulation("补仓模拟"),
    TargetProfitReverse("止盈"),
    StopLossReverse("止损")
}

data class HistoryField(
    val label: String,
    val value: String
)

data class HistorySection(
    val title: String,
    val fields: List<HistoryField>
)

data class HistoryRecord(
    val id: String,
    val category: HistoryCategory,
    val title: String,
    val summary: String,
    val roiSummary: String?,
    val savedAt: Long,
    val favorite: Boolean = false,
    val sections: List<HistorySection>,
    val fingerprint: String? = null,
    val schemaVersion: Int = 1
)

fun HistoryRecord.dedupeFingerprint(): String = fingerprint ?: contentFingerprint()

fun HistoryRecord.contentFingerprint(): String = buildString {
    append(category.name)
    append('|')
    append(title)
    append('|')
    append(summary)
    append('|')
    append(roiSummary.orEmpty())
    sections.forEach { section ->
        append('|')
        append(section.title)
        section.fields.forEach { field ->
            append('|')
            append(field.label)
            append('=')
            append(field.value)
        }
    }
}
