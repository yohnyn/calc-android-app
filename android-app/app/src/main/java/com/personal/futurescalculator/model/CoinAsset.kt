package com.personal.futurescalculator.model

import java.math.BigDecimal

data class CoinAsset(
    val id: String,
    val symbol: String,
    val name: String,
    val priceUsdt: BigDecimal,
    val isCustom: Boolean = false,
    val marketRank: Int? = null,
    val iconUrl: String? = null,
    val iconPath: String? = null
)
