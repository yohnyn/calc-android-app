package com.personal.futurescalculator.model

enum class HomeModule(val label: String) {
    Position("仓位参数"),
    Result("计算结果"),
    Comparison("收益方案对比"),
    Averaging("补仓决策模拟");

    companion object {
        val defaultOrder = listOf(Position, Result, Comparison, Averaging)
    }
}
