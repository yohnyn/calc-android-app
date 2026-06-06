package com.personal.futurescalculator.model

enum class CoinMarginedCalculationMode(
    val label: String,
    val shortDescription: String
) {
    CoinQuantity(
        label = "币数量模式",
        shortDescription = "按照持仓币数量计算盈亏，适合大多数普通用户。"
    ),
    InverseContract(
        label = "反向合约模式",
        shortDescription = "采用传统 Inverse Contract（反向合约）计算方式。"
    )
}