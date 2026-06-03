package com.personal.futurescalculator.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.personal.futurescalculator.model.PositionSide
import com.personal.futurescalculator.model.MarginMode
import java.math.BigDecimal

@Composable
fun PositionSideSelector(
    selectedSide: PositionSide,
    onSideChange: (PositionSide) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Button(
            onClick = { onSideChange(PositionSide.Long) },
            modifier = Modifier.weight(1f),
            colors = if (selectedSide == PositionSide.Long) {
                ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface
                )
            }
        ) {
            Text("做多")
        }
        Button(
            onClick = { onSideChange(PositionSide.Short) },
            modifier = Modifier.weight(1f),
            colors = if (selectedSide == PositionSide.Short) {
                ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface
                )
            }
        ) {
            Text("做空")
        }
    }
}

@Composable
fun MarginModeSelector(
    selectedMode: MarginMode,
    onModeChange: (MarginMode) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Button(
            onClick = { onModeChange(MarginMode.Cross) },
            modifier = Modifier.weight(1f),
            colors = if (selectedMode == MarginMode.Cross) {
                ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface
                )
            }
        ) {
            Text("全仓")
        }
        Button(
            onClick = { onModeChange(MarginMode.Isolated) },
            modifier = Modifier.weight(1f),
            colors = if (selectedMode == MarginMode.Isolated) {
                ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface
                )
            }
        ) {
            Text("逐仓")
        }
    }
}

@Composable
fun LeverageSelector(
    leverage: BigDecimal,
    onLeverageChange: (BigDecimal) -> Unit,
    modifier: Modifier = Modifier
) {
    val leverageOptions = listOf("1x", "2x", "3x", "5x", "10x", "20x", "50x", "100x")
    val selectedLeverage = leverageOptions.find { it.replace("x", "") == leverage.toString() } ?: "10x"
    
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        leverageOptions.forEach { option ->
            val selected = option == selectedLeverage
            Button(
                onClick = { onLeverageChange(BigDecimal(option.replace("x", ""))) },
                modifier = Modifier.weight(1f),
                colors = if (selected) {
                    ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = MaterialTheme.colorScheme.onSurface
                    )
                }
            ) {
                Text(option)
            }
        }
    }
}

@Composable
fun NumberInput(
    value: BigDecimal?,
    onValueChange: (BigDecimal?) -> Unit,
    label: String,
    modifier: Modifier = Modifier
) {
    val textValue = value?.toString() ?: ""
    var textFieldValue by remember { mutableStateOf(TextFieldValue(textValue)) }
    
    OutlinedTextField(
        value = textFieldValue,
        onValueChange = { 
            textFieldValue = it
            if (it.text.isEmpty()) {
                onValueChange(null)
            } else {
                try {
                    onValueChange(BigDecimal(it.text))
                } catch (e: NumberFormatException) {
                    // Ignore invalid input
                }
            }
        },
        label = { Text(label) },
        modifier = modifier,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        singleLine = true
    )
}
