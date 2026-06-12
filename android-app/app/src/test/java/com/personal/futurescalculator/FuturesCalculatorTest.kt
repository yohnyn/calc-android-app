package com.personal.futurescalculator

import com.personal.futurescalculator.domain.FuturesCalculator
import com.personal.futurescalculator.domain.AveragingCalculator
import com.personal.futurescalculator.domain.AveragingDecisionCalculator
import com.personal.futurescalculator.domain.ComparisonCalculator
import com.personal.futurescalculator.domain.CoinMarginedCalculator
import com.personal.futurescalculator.model.AmountField
import com.personal.futurescalculator.model.AveragingInput
import com.personal.futurescalculator.model.AveragingDecisionInput
import com.personal.futurescalculator.model.CalculationInput
import com.personal.futurescalculator.model.CoinMarginedCalculationMode
import com.personal.futurescalculator.model.ComparisonItem
import com.personal.futurescalculator.model.CopyFormat
import com.personal.futurescalculator.model.MarginMode
import com.personal.futurescalculator.model.PositionSide
import com.personal.futurescalculator.model.SettlementMode
import com.personal.futurescalculator.model.toSavedPlan
import com.personal.futurescalculator.util.ClipboardFormatter
import com.personal.futurescalculator.viewmodel.normalizeAmountFields
import java.math.BigDecimal
import java.math.RoundingMode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class FuturesCalculatorTest {
    private val calculator = FuturesCalculator()
    private val coinMarginedCalculator = CoinMarginedCalculator()

    @Test
    fun returnsNoResultWithoutEntryPrice() {
        assertNull(calculator.calculate(CalculationInput()))
    }

    @Test
    fun isolatedLiquidationMatchesDocumentedSimplifiedFormula() {
        val longResult = calculator.calculate(
            CalculationInput(
                side = PositionSide.Long,
                marginMode = MarginMode.Isolated,
                quantity = BigDecimal("0.1"),
                entryPrice = BigDecimal("20000"),
                leverage = BigDecimal("10"),
                maintenanceMarginRatePercent = BigDecimal("0.5"),
                estimateLiquidation = true
            )
        )
        val shortResult = calculator.calculate(
            CalculationInput(
                side = PositionSide.Short,
                marginMode = MarginMode.Isolated,
                quantity = BigDecimal("0.1"),
                entryPrice = BigDecimal("20000"),
                leverage = BigDecimal("10"),
                maintenanceMarginRatePercent = BigDecimal("0.5"),
                estimateLiquidation = true
            )
        )

        assertScaledEquals("18100", longResult!!.liquidationPrice)
        assertScaledEquals("21900", shortResult!!.liquidationPrice)
    }

    @Test
    fun copiedPerformanceIncludesDisclaimerAndDetailedTargetResults() {
        val input = CalculationInput(
            quantity = BigDecimal("0.1"),
            entryPrice = BigDecimal("20000"),
            exitPrice = BigDecimal("21000"),
            takeProfitPrice = BigDecimal("22000"),
            stopLossPrice = BigDecimal("19000"),
            leverage = BigDecimal("10"),
            openFeeRatePercent = BigDecimal.ZERO,
            closeFeeRatePercent = BigDecimal.ZERO
        )
        val result = calculator.calculate(input)

        val summary = ClipboardFormatter.formatSinglePerformance("方案 1", "BTC", input, result, CopyFormat.Summary)
        val detail = ClipboardFormatter.formatSinglePerformance("方案 1", "BTC", input, result, CopyFormat.Detail)

        assertTrue(summary.contains("仅为本地计算结果，不构成交易依据"))
        assertTrue(detail.contains("止盈价：22000 USDT"))
        assertTrue(detail.contains("止盈收益：+200 USDT"))
        assertTrue(detail.contains("止损价：19000 USDT"))
        assertTrue(detail.contains("止损亏损：-100 USDT"))
    }

    @Test
    fun savedPlanPreservesCompleteInput() {
        val input = CalculationInput(
            quantity = BigDecimal("0.1"),
            entryPrice = BigDecimal("20000"),
            exitPrice = BigDecimal("21000"),
            targetProfitAmount = BigDecimal("100"),
            maxLossAmount = BigDecimal("50"),
            totalFunds = BigDecimal("1000"),
            estimateLiquidation = true,
            calculateMaxOpen = true
        )

        val saved = ComparisonItem(id = "plan", name = "完整方案", input = input).toSavedPlan()

        assertEquals(input, saved.input)
    }

    @Test
    fun coinMarginedLongReturnsCoinProfitAndUsdtValue() {
        val result = coinMarginedCalculator.calculate(
            calculationMode = CoinMarginedCalculationMode.CoinQuantity,
            side = PositionSide.Long,
            quantity = BigDecimal("0.1"),
            entryPrice = BigDecimal("80000"),
            exitPrice = BigDecimal("84000"),
            currentPrice = BigDecimal("84000")
        )

        assertNotNull(result)
        assertScaledEquals("0.0047619047616", result!!.pnlCoin)
        assertScaledEquals("380.952380928", result.estimatedValueUsdt)
    }

    @Test
    fun coinMarginedShortReturnsCoinProfitAndUsdtValue() {
        val result = coinMarginedCalculator.calculate(
            calculationMode = CoinMarginedCalculationMode.CoinQuantity,
            side = PositionSide.Short,
            quantity = BigDecimal("0.1"),
            entryPrice = BigDecimal("80000"),
            exitPrice = BigDecimal("76000"),
            currentPrice = BigDecimal("76000")
        )

        assertNotNull(result)
        assertScaledEquals("0.0052631578944", result!!.pnlCoin)
        assertScaledEquals("421.052631552", result.estimatedValueUsdt)
    }

    @Test
    fun inverseContractDoesNotUseLinearUsdtMarginedFormula() {
        val result = coinMarginedCalculator.calculate(
            calculationMode = CoinMarginedCalculationMode.InverseContract,
            side = PositionSide.Long,
            quantity = BigDecimal("1"),
            entryPrice = BigDecimal("100000"),
            exitPrice = BigDecimal("110000"),
            currentPrice = BigDecimal("110000")
        )

        assertNotNull(result)
        assertScaledEquals("0.09090909090909", result!!.pnlCoin)
        assertScaledEquals("10000", result.estimatedValueUsdt)
    }

    @Test
    fun calculatesQuantityWhenQuantityIsEmpty() {
        val result = calculator.calculate(
            CalculationInput(
                margin = BigDecimal("100"),
                entryPrice = BigDecimal("20000"),
                exitPrice = BigDecimal("21000"),
                leverage = BigDecimal("10"),
                openFeeRatePercent = BigDecimal.ZERO,
                closeFeeRatePercent = BigDecimal.ZERO
            )
        )

        assertNotNull(result)
        assertScaledEquals("0.05", result!!.quantity)
        assertScaledEquals("1000", result.positionValue)
        assertScaledEquals("50", result.netPnl)
        assertScaledEquals("50", result.roiPercent)
    }

    @Test
    fun usesSeparateOpenAndCloseFeeRates() {
        val result = calculator.calculate(
            CalculationInput(
                margin = BigDecimal("100"),
                entryPrice = BigDecimal("20000"),
                exitPrice = BigDecimal("21000"),
                leverage = BigDecimal("10"),
                openFeeRatePercent = BigDecimal("0.05"),
                closeFeeRatePercent = BigDecimal("0.1")
            )
        )

        assertNotNull(result)
        assertScaledEquals("0.5", result!!.openFee)
        assertScaledEquals("1.05", result.closeFee)
        assertScaledEquals("1.55", result.totalFee)
        assertScaledEquals("48.45", result.netPnl)
    }

    @Test
    fun reversesLongTargetProfitPriceByAmount() {
        val result = calculator.calculate(
            CalculationInput(
                side = PositionSide.Long,
                margin = BigDecimal("100"),
                entryPrice = BigDecimal("20000"),
                leverage = BigDecimal("10"),
                openFeeRatePercent = BigDecimal.ZERO,
                closeFeeRatePercent = BigDecimal.ZERO,
                targetProfitAmount = BigDecimal("50")
            )
        )

        assertNotNull(result)
        assertScaledEquals("21000", result!!.targetProfitPriceByAmount)
        assertScaledEquals("50", result.netPnl)
        assertScaledEquals("50", result.roiPercent)
    }

    @Test
    fun reversesLongStopLossPriceByAmount() {
        val result = calculator.calculate(
            CalculationInput(
                side = PositionSide.Long,
                margin = BigDecimal("100"),
                entryPrice = BigDecimal("20000"),
                leverage = BigDecimal("10"),
                openFeeRatePercent = BigDecimal.ZERO,
                closeFeeRatePercent = BigDecimal.ZERO,
                maxLossAmount = BigDecimal("50")
            )
        )

        assertNotNull(result)
        assertScaledEquals("19000", result!!.stopLossPriceByAmount)
        assertScaledEquals("-50", result.netPnl)
        assertScaledEquals("-50", result.roiPercent)
    }

    @Test
    fun reversesShortTargetProfitPriceByRoi() {
        val result = calculator.calculate(
            CalculationInput(
                side = PositionSide.Short,
                margin = BigDecimal("100"),
                entryPrice = BigDecimal("20000"),
                leverage = BigDecimal("10"),
                openFeeRatePercent = BigDecimal.ZERO,
                closeFeeRatePercent = BigDecimal.ZERO,
                targetRoiPercent = BigDecimal("50")
            )
        )

        assertNotNull(result)
        assertScaledEquals("19000", result!!.targetProfitPriceByRoi)
    }

    @Test
    fun calculatesAveragingAfterAddingPosition() {
        val result = AveragingCalculator().calculate(
            input = AveragingInput(
                currentEntryPrice = BigDecimal("20000"),
                currentQuantity = BigDecimal("0.1"),
                addEntryPrice = BigDecimal("18000"),
                addQuantity = BigDecimal("0.1")
            ),
            side = PositionSide.Long,
            marginMode = MarginMode.Isolated,
            leverage = BigDecimal("10"),
            maintenanceMarginRatePercent = BigDecimal("0.5")
        )

        assertNotNull(result)
        assertScaledEquals("19000", result!!.newAveragePrice)
        assertScaledEquals("0.2", result.totalQuantity)
        assertScaledEquals("3800", result.totalPositionValue)
        assertScaledEquals("18100", result.liquidationPriceBefore)
        assertScaledEquals("17195", result.liquidationPriceAfter)
        assertScaledEquals("-5", result.liquidationPriceChangePercent)
    }

    @Test
    fun comparesTargetPricePnlBeforeAndAfterAddingPosition() {
        val result = AveragingDecisionCalculator().calculate(
            AveragingDecisionInput(
                side = PositionSide.Long,
                currentEntryPrice = BigDecimal("20000"),
                currentQuantity = BigDecimal("0.1"),
                addEntryPrice = BigDecimal("18000"),
                addQuantity = BigDecimal("0.1"),
                targetExitPrice = BigDecimal("21000")
            )
        )

        assertNotNull(result)
        assertScaledEquals("19000", result!!.newAveragePrice)
        assertScaledEquals("0.2", result.newQuantity)
        assertScaledEquals("100", result.pnlWithoutAdding)
        assertScaledEquals("400", result.pnlAfterAdding)
        assertScaledEquals("300", result.pnlChange)
        assertScaledEquals("1000", result.averagePriceImprovement)
    }

    @Test
    fun calculatesAveragingPreviewWithoutTargetExitPrice() {
        val result = AveragingDecisionCalculator().calculate(
            AveragingDecisionInput(
                side = PositionSide.Long,
                currentEntryPrice = BigDecimal("20000"),
                currentQuantity = BigDecimal("0.1"),
                currentMargin = BigDecimal("200"),
                currentLeverage = BigDecimal("10"),
                addEntryPrice = BigDecimal("18000"),
                addAmount = BigDecimal("200")
            )
        )

        assertNotNull(result)
        assertScaledEquals("18947.36842105", result!!.newAveragePrice)
        assertScaledEquals("1052.63157895", result.averagePriceImprovement)
        assertScaledEquals("0.11111111", result.quantityIncrease)
        assertNull(result.pnlWithoutAdding)
        assertNull(result.pnlAfterAdding)
        assertNull(result.pnlChange)
    }

    @Test
    fun comparisonDiffIsComparedSchemeMinusSchemeOne() {
        val schemeOneInput = CalculationInput(
            margin = BigDecimal("100"),
            entryPrice = BigDecimal("20000"),
            exitPrice = BigDecimal("21000"),
            leverage = BigDecimal("10"),
            openFeeRatePercent = BigDecimal.ZERO,
            closeFeeRatePercent = BigDecimal.ZERO
        )
        val schemeTwoInput = schemeOneInput.copy(exitPrice = BigDecimal("22000"))
        val schemeOneResult = calculator.calculate(schemeOneInput)

        val comparison = ComparisonCalculator().compare(
            current = schemeOneResult,
            items = listOf(ComparisonItem(id = "2", name = "方案 2", input = schemeTwoInput))
        ).single()

        assertScaledEquals("50", schemeOneResult!!.netPnl)
        assertScaledEquals("100", comparison.result!!.netPnl)
        assertScaledEquals("50", comparison.netPnlDiff)
    }

    @Test
    fun comparisonDiffHandlesProfitAndLossTransitions() {
        val scenarios = listOf(
            BigDecimal("1000") to BigDecimal("2000"),
            BigDecimal("1000") to BigDecimal("100"),
            BigDecimal("1000") to BigDecimal("-1000"),
            BigDecimal("-1000") to BigDecimal("1000")
        )
        val expectedDiffs = listOf("1000", "-900", "-2000", "2000")

        scenarios.zip(expectedDiffs).forEach { (scenario, expectedDiff) ->
            assertScaledEquals(expectedDiff, scenario.second - scenario.first)
        }
    }

    @Test
    fun comparisonSupportsCoinMarginedSchemesByEstimatedUsdtValue() {
        val current = calculator.calculate(
            CalculationInput(
                quantity = BigDecimal("1"),
                entryPrice = BigDecimal("100000"),
                exitPrice = BigDecimal("110000"),
                leverage = BigDecimal("3"),
                openFeeRatePercent = BigDecimal.ZERO,
                closeFeeRatePercent = BigDecimal.ZERO
            )
        )
        val comparison = ComparisonCalculator().compare(
            current = current,
            items = listOf(
                ComparisonItem(
                    id = "coin",
                    name = "币本位方案",
                    coinId = "bitcoin",
                    settlementMode = SettlementMode.CoinMargined,
                    coinMarginedCalculationMode = CoinMarginedCalculationMode.CoinQuantity,
                    input = CalculationInput(
                        quantity = BigDecimal("1"),
                        entryPrice = BigDecimal("100000"),
                        exitPrice = BigDecimal("110000")
                    )
                )
            ),
            coinPricesById = mapOf("bitcoin" to BigDecimal("100000"))
        ).single()

        assertNull(comparison.result)
        assertNotNull(comparison.coinMarginedResult)
        assertScaledEquals("0.1", comparison.coinMarginedResult!!.pnlCoin)
        assertScaledEquals("10000", comparison.coinMarginedResult.estimatedValueUsdt)
        assertScaledEquals("0", comparison.netPnlDiff)
    }

    @Test
    fun isolatedLongProfitUsesPositionInitialMarginAndBothFees() {
        val result = calculateIsolatedCase(PositionSide.Long, "84000")

        assertScaledEquals("8000", result.positionValue)
        assertScaledEquals("800", result.requiredMargin)
        assertScaledEquals("400", result.grossPnl)
        assertScaledEquals("4", result.openFee)
        assertScaledEquals("4.2", result.closeFee)
        assertScaledEquals("8.2", result.totalFee)
        assertScaledEquals("391.8", result.netPnl)
        assertScaledEquals("48.975", result.roiPercent)
    }

    @Test
    fun isolatedLongLossUsesCorrectPnlDirection() {
        val result = calculateIsolatedCase(PositionSide.Long, "76000")

        assertScaledEquals("-400", result.grossPnl)
        assertScaledEquals("3.8", result.closeFee)
        assertScaledEquals("-407.8", result.netPnl)
        assertScaledEquals("-50.975", result.roiPercent)
    }

    @Test
    fun isolatedShortProfitUsesCorrectPnlDirection() {
        val result = calculateIsolatedCase(PositionSide.Short, "76000")

        assertScaledEquals("400", result.grossPnl)
        assertScaledEquals("3.8", result.closeFee)
        assertScaledEquals("392.2", result.netPnl)
        assertScaledEquals("49.025", result.roiPercent)
    }

    @Test
    fun isolatedShortLossUsesCorrectPnlDirection() {
        val result = calculateIsolatedCase(PositionSide.Short, "84000")

        assertScaledEquals("-400", result.grossPnl)
        assertScaledEquals("4.2", result.closeFee)
        assertScaledEquals("-408.2", result.netPnl)
        assertScaledEquals("-51.025", result.roiPercent)
    }

    @Test
    fun isolatedModeDerivesQuantityFromMargin() {
        val result = calculator.calculate(
            CalculationInput(
                marginMode = MarginMode.Isolated,
                margin = BigDecimal("800"),
                entryPrice = BigDecimal("80000"),
                leverage = BigDecimal("10")
            )
        )

        assertNotNull(result)
        assertScaledEquals("0.1", result!!.quantity)
        assertScaledEquals("8000", result.positionValue)
        assertScaledEquals("800", result.requiredMargin)
    }

    @Test
    fun isolatedModeDerivesInitialMarginFromQuantity() {
        val result = calculator.calculate(
            CalculationInput(
                marginMode = MarginMode.Isolated,
                quantity = BigDecimal("0.1"),
                entryPrice = BigDecimal("80000"),
                leverage = BigDecimal("10")
            )
        )

        assertNotNull(result)
        assertScaledEquals("0.1", result!!.quantity)
        assertScaledEquals("8000", result.positionValue)
        assertScaledEquals("800", result.requiredMargin)
    }

    @Test
    fun marginModeRemainsSourceWhenEntryPriceAndLeverageChange() {
        val normalized = normalizeAmountFields(
            CalculationInput(
                margin = BigDecimal("800"),
                entryPrice = BigDecimal("80000"),
                leverage = BigDecimal("10")
            ),
            AmountField.Margin
        )
        val changedLeverage = normalizeAmountFields(normalized.copy(leverage = BigDecimal("20")), AmountField.Margin)

        assertScaledEquals("0.1", normalized.quantity)
        assertScaledEquals("0.2", changedLeverage.quantity)
        assertScaledEquals("800", changedLeverage.margin)
    }

    @Test
    fun quantityModeRemainsSourceWhenEntryPriceAndLeverageChange() {
        val normalized = normalizeAmountFields(
            CalculationInput(
                quantity = BigDecimal("0.1"),
                entryPrice = BigDecimal("80000"),
                leverage = BigDecimal("10")
            ),
            AmountField.Quantity
        )
        val changedLeverage = normalizeAmountFields(normalized.copy(leverage = BigDecimal("20")), AmountField.Quantity)

        assertScaledEquals("800", normalized.margin)
        assertScaledEquals("400", changedLeverage.margin)
        assertScaledEquals("0.1", changedLeverage.quantity)
    }

    @Test
    fun shortAcceptanceCasesMatchForMarginAndQuantityInputs() {
        val base = CalculationInput(
            side = PositionSide.Short,
            entryPrice = BigDecimal("80000"),
            exitPrice = BigDecimal("76000"),
            leverage = BigDecimal("10"),
            openFeeRatePercent = BigDecimal.ZERO,
            closeFeeRatePercent = BigDecimal.ZERO
        )
        val fromMargin = calculator.calculate(base.copy(margin = BigDecimal("800")))
        val fromQuantity = calculator.calculate(base.copy(quantity = BigDecimal("0.1")))

        listOf(fromMargin, fromQuantity).forEach { result ->
            assertNotNull(result)
            assertScaledEquals("0.1", result!!.quantity)
            assertScaledEquals("8000", result.positionValue)
            assertScaledEquals("800", result.requiredMargin)
            assertScaledEquals("400", result.grossPnl)
        }
    }

    @Test
    fun crossModeFallsBackToInitialMarginForLiquidationPrice() {
        val result = calculator.calculate(
            CalculationInput(
                marginMode = MarginMode.Cross,
                quantity = BigDecimal("0.1"),
                entryPrice = BigDecimal("80000"),
                leverage = BigDecimal("10")
            )
        )

        assertNotNull(result)
        assertScaledEquals("72361.80904523", result!!.liquidationPrice)
        assertScaledEquals("9.54773869", result.distanceToLiquidationPercent)
    }

    @Test
    fun crossModeUsesTotalFundsToMoveLiquidationPriceFartherAway() {
        val baseInput = CalculationInput(
            marginMode = MarginMode.Cross,
            quantity = BigDecimal("0.1"),
            entryPrice = BigDecimal("80000"),
            leverage = BigDecimal("10")
        )
        val initialMarginResult = calculator.calculate(baseInput)
        val totalFundsResult = calculator.calculate(baseInput.copy(totalFunds = BigDecimal("2000")))

        assertNotNull(initialMarginResult)
        assertNotNull(totalFundsResult)
        assertScaledEquals("72361.80904523", initialMarginResult!!.liquidationPrice)
        assertScaledEquals("60301.50753769", totalFundsResult!!.liquidationPrice)
    }

    @Test
    fun isolatedLongInitialLiquidationBufferUsesEntryPriceNotExitPrice() {
        val result = calculator.calculate(
            CalculationInput(
                marginMode = MarginMode.Isolated,
                quantity = BigDecimal("0.1"),
                entryPrice = BigDecimal("80000"),
                exitPrice = BigDecimal("160000"),
                leverage = BigDecimal("10"),
                maintenanceMarginRatePercent = BigDecimal("0.5")
            )
        )

        assertNotNull(result)
        assertScaledEquals("72361.80904523", result!!.liquidationPrice)
        assertScaledEquals("9.54773869", result.distanceToLiquidationPercent)
    }

    @Test
    fun isolatedShortLiquidationPriceUsesMaintenanceMarginAtLiquidationPrice() {
        val result = calculator.calculate(
            CalculationInput(
                side = PositionSide.Short,
                marginMode = MarginMode.Isolated,
                quantity = BigDecimal("0.1"),
                entryPrice = BigDecimal("80000"),
                leverage = BigDecimal("10"),
                maintenanceMarginRatePercent = BigDecimal("0.5")
            )
        )

        assertNotNull(result)
        assertScaledEquals("87562.18905473", result!!.liquidationPrice)
        assertScaledEquals("9.45273632", result.distanceToLiquidationPercent)
    }

    @Test
    fun simultaneousTargetAndStopDoNotSelectOneAsNetPnl() {
        val result = calculator.calculate(
            CalculationInput(
                margin = BigDecimal("100"),
                entryPrice = BigDecimal("20000"),
                leverage = BigDecimal("10"),
                openFeeRatePercent = BigDecimal.ZERO,
                closeFeeRatePercent = BigDecimal.ZERO,
                targetProfitAmount = BigDecimal("50"),
                maxLossAmount = BigDecimal("25")
            )
        )

        assertNotNull(result)
        assertScaledEquals("21000", result!!.targetProfitPriceByAmount)
        assertScaledEquals("19500", result.stopLossPriceByAmount)
        assertNull(result.netPnl)
        assertNull(result.roiPercent)
    }

    @Test
    fun rejectsOutOfRangeLeverageAndRates() {
        val base = CalculationInput(
            margin = BigDecimal("100"),
            entryPrice = BigDecimal("20000")
        )

        assertNull(calculator.calculate(base.copy(leverage = BigDecimal("126"))))
        assertNull(calculator.calculate(base.copy(openFeeRatePercent = BigDecimal("100"))))
        assertNull(calculator.calculate(base.copy(closeFeeRatePercent = BigDecimal("100"))))
        assertNull(calculator.calculate(base.copy(maintenanceMarginRatePercent = BigDecimal("100"))))
    }

    @Test
    fun averagingDoesNotDivideByZeroWhenLiquidationPriceIsZero() {
        val result = AveragingCalculator().calculate(
            input = AveragingInput(
                currentEntryPrice = BigDecimal("20000"),
                currentQuantity = BigDecimal("0.1"),
                addEntryPrice = BigDecimal("18000"),
                addQuantity = BigDecimal("0.1")
            ),
            side = PositionSide.Long,
            marginMode = MarginMode.Isolated,
            leverage = BigDecimal.ONE,
            maintenanceMarginRatePercent = BigDecimal.ZERO
        )

        assertNotNull(result)
        assertScaledEquals("0", result!!.liquidationPriceBefore)
        assertScaledEquals("0", result.liquidationPriceAfter)
        assertNull(result.liquidationPriceChangePercent)
    }

    @Test
    fun crossAveragingCalculatesAverageWithoutPretendingToKnowLiquidationPrices() {
        val result = AveragingCalculator().calculate(
            input = AveragingInput(
                currentEntryPrice = BigDecimal("20000"),
                currentQuantity = BigDecimal("0.1"),
                addEntryPrice = BigDecimal("18000"),
                addQuantity = BigDecimal("0.1")
            ),
            side = PositionSide.Long,
            marginMode = MarginMode.Cross,
            leverage = BigDecimal("10"),
            maintenanceMarginRatePercent = BigDecimal("0.5")
        )

        assertNotNull(result)
        assertScaledEquals("19000", result!!.newAveragePrice)
        assertNull(result.liquidationPriceBefore)
        assertNull(result.liquidationPriceAfter)
        assertNull(result.liquidationPriceChangePercent)
    }

    @Test
    fun averagingRejectsOutOfRangeLeverageAndMaintenanceMarginRate() {
        val input = AveragingInput(
            currentEntryPrice = BigDecimal("20000"),
            currentQuantity = BigDecimal("0.1"),
            addEntryPrice = BigDecimal("18000"),
            addQuantity = BigDecimal("0.1")
        )
        val averagingCalculator = AveragingCalculator()

        assertNull(
            averagingCalculator.calculate(
                input,
                PositionSide.Long,
                MarginMode.Isolated,
                BigDecimal("126"),
                BigDecimal("0.5")
            )
        )
        assertNull(
            averagingCalculator.calculate(
                input,
                PositionSide.Long,
                MarginMode.Isolated,
                BigDecimal("10"),
                BigDecimal("100")
            )
        )
    }

    @Test
    fun averagingDecisionHandlesShortAndAdverseAddPrice() {
        val result = AveragingDecisionCalculator().calculate(
            AveragingDecisionInput(
                side = PositionSide.Short,
                currentEntryPrice = BigDecimal("20000"),
                currentQuantity = BigDecimal("0.1"),
                addEntryPrice = BigDecimal("18000"),
                addQuantity = BigDecimal("0.1"),
                targetExitPrice = BigDecimal("19000")
            )
        )

        assertNotNull(result)
        assertScaledEquals("19000", result!!.newAveragePrice)
        assertScaledEquals("100", result.pnlWithoutAdding)
        assertScaledEquals("0", result.pnlAfterAdding)
        assertScaledEquals("-100", result.pnlChange)
        assertScaledEquals("-1000", result.averagePriceImprovement)
    }

    private fun calculateIsolatedCase(side: PositionSide, exitPrice: String) = calculator.calculate(
        CalculationInput(
            side = side,
            marginMode = MarginMode.Isolated,
            entryPrice = BigDecimal("80000"),
            exitPrice = BigDecimal(exitPrice),
            quantity = BigDecimal("0.1"),
            leverage = BigDecimal("10"),
            openFeeRatePercent = BigDecimal("0.05"),
            closeFeeRatePercent = BigDecimal("0.05")
        )
    )!!

    private fun assertScaledEquals(expected: String, actual: BigDecimal?) {
        assertNotNull(actual)
        assertEquals(
            BigDecimal(expected).setScale(8, RoundingMode.HALF_UP),
            actual!!.setScale(8, RoundingMode.HALF_UP)
        )
    }
}
