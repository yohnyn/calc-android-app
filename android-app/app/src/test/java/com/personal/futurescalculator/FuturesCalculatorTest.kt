package com.personal.futurescalculator

import org.junit.Assert.assertNull
import org.junit.Test
import com.personal.futurescalculator.domain.FuturesCalculator
import com.personal.futurescalculator.model.CalculationInput

class FuturesCalculatorTest {
    @Test
    fun scaffoldReturnsNoResultBeforeImplementation() {
        val calculator = FuturesCalculator()

        assertNull(calculator.calculate(CalculationInput()))
    }
}
