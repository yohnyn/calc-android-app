package com.personal.futurescalculator.model

enum class HistoryCategory(val label: String) {
    ProfitCalculation("收益测算"),
    SchemeComparison("方案对比"),
    AveragingSimulation("补仓模拟"),
    TargetProfitReverse("目标收益反推"),
    StopLossReverse("止损反推")
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
    val schemaVersion: Int = 1
)
