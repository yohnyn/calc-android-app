package com.personal.futurescalculator

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.personal.futurescalculator.ui.CalculatorScreen
import com.personal.futurescalculator.ui.theme.FuturesCalculatorTheme
import com.personal.futurescalculator.viewmodel.CalculatorViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val viewModel = CalculatorViewModel(applicationContext)
        
        setContent {
            val uiState by viewModel.uiState.collectAsState()
            FuturesCalculatorTheme(themeMode = uiState.themeMode) {
                CalculatorScreen(viewModel = viewModel)
            }
        }
    }
}
