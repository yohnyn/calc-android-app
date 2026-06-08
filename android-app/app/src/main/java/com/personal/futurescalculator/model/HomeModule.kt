package com.personal.futurescalculator.model

enum class HomeModule(val label: String) {
    Position("仓位参数"),
    Result("计算结果"),
    TargetProfit("目标价格"),
    Comparison("开仓方案对比"),
    Averaging("补仓助手");

    companion object {
        val defaultOrder = listOf(Position, TargetProfit, Comparison, Averaging)
    }
}
