package com.personal.futurescalculator.model

data class ComparisonItem(
    val id: String,
    val name: String,
    val coinId: String = "bitcoin",
    val input: CalculationInput = CalculationInput(),
    val lastEditedAmountField: AmountField = AmountField.Margin
)
