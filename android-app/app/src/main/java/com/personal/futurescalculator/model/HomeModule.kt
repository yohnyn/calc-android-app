package com.personal.futurescalculator.model

enum class HomeModule(val label: String) {
    Position("仓位参数"),
    TargetStop("目标与止损"),
    Result("计算结果"),
    Comparison("收益方案对比"),
    Averaging("补仓决策模拟"),
    History("历史记录");

    companion object {
        val defaultOrder = listOf(Position, TargetStop, Result, Comparison, Averaging)
        val configurableVisibility = setOf(Comparison, Averaging, History)
        val alwaysVisible = entries.filterNot { it in configurableVisibility }.toSet()
    }
}
