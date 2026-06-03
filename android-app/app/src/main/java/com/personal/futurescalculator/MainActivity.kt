package com.personal.futurescalculator

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.personal.futurescalculator.ui.CalculatorScreen
import com.personal.futurescalculator.ui.theme.FuturesCalculatorTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            FuturesCalculatorTheme {
                CalculatorScreen()
            }
        }
    }
}
