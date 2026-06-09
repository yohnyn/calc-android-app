package com.personal.futurescalculator.model

enum class HomeModule(val label: String) {
    Position("仓位参数"),
    Result("结果缩略"),
    Comparison("开仓方案对比"),
    Averaging("补仓助手"),
    TargetProfit("目标价格"),
    LowPriorityActions("低权重入口");

    companion object {
        val defaultOrder = listOf(Position, Result, Comparison, Averaging, TargetProfit, LowPriorityActions)
    }
}
