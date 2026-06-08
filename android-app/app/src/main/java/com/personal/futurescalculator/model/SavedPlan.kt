package com.personal.futurescalculator.model

data class SavedPlan(
    val id: String,
    val name: String,
    val coinId: String,
    val settlementMode: SettlementMode,
    val coinMarginedCalculationMode: CoinMarginedCalculationMode,
    val input: CalculationInput,
    val lastEditedAmountField: AmountField = AmountField.Margin,
    val createdAt: Long,
    val updatedAt: Long
)

fun SavedPlan.toComparisonItem(): ComparisonItem = ComparisonItem(
    id = id,
    name = name,
    coinId = coinId,
    settlementMode = settlementMode,
    coinMarginedCalculationMode = coinMarginedCalculationMode,
    input = input,
    lastEditedAmountField = lastEditedAmountField
)

fun ComparisonItem.toSavedPlan(createdAt: Long = System.currentTimeMillis()): SavedPlan = SavedPlan(
    id = id,
    name = name,
    coinId = coinId,
    settlementMode = settlementMode,
    coinMarginedCalculationMode = coinMarginedCalculationMode,
    input = input.copy(
        exitPrice = input.exitPrice,
        targetProfitAmount = null,
        targetRoiPercent = null,
        maxLossAmount = null,
        maxLossRoiPercent = null
    ),
    lastEditedAmountField = lastEditedAmountField,
    createdAt = createdAt,
    updatedAt = System.currentTimeMillis()
)
