package com.personal.futurescalculator.model

enum class CoinMarginedCalculationMode(
    val label: String,
    val shortDescription: String
) {
    CoinQuantity(
        label = "按币数量",
        shortDescription = "直接输入持仓币数量，适合大多数场景。"
    ),
    InverseContract(
        label = "按合约张数",
        shortDescription = "按反向合约张数计算，适合按张数记录仓位的用户。"
    )
}
