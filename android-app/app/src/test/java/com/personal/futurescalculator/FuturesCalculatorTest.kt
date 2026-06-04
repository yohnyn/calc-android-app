package com.personal.futurescalculator

import com.personal.futurescalculator.domain.FuturesCalculator
import com.personal.futurescalculator.model.CalculationInput
import com.personal.futurescalculator.model.PositionSide
import java.math.BigDecimal
import java.math.RoundingMode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class FuturesCalculatorTest {
    private val calculator = FuturesCalculator()

    @Test
    fun returnsNoResultWithoutEntryPrice() {
        assertNull(calculator.calculate(CalculationInput()))
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

    private fun assertScaledEquals(expected: String, actual: BigDecimal?) {
        assertNotNull(actual)
        assertEquals(
            BigDecimal(expected).setScale(8, RoundingMode.HALF_UP),
            actual!!.setScale(8, RoundingMode.HALF_UP)
        )
    }
}
