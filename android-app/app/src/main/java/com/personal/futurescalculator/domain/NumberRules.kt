package com.personal.futurescalculator.domain

import java.math.MathContext
import java.math.RoundingMode

val CALCULATION_CONTEXT: MathContext = MathContext(34, RoundingMode.HALF_UP)
const val DIVIDE_SCALE: Int = 18
val DISPLAY_ROUNDING: RoundingMode = RoundingMode.HALF_UP
