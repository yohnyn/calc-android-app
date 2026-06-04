package com.personal.futurescalculator.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.personal.futurescalculator.model.MarginMode
import com.personal.futurescalculator.model.PositionSide
import java.math.BigDecimal

@Composable
fun PositionSideSelector(
    selectedSide: PositionSide,
    onSideChange: (PositionSide) -> Unit,
    modifier: Modifier = Modifier
) {
    TwoOptionSelector(
        firstText = "做多",
        secondText = "做空",
        firstSelected = selectedSide == PositionSide.Long,
        secondSelected = selectedSide == PositionSide.Short,
        onFirstClick = { onSideChange(PositionSide.Long) },
        onSecondClick = { onSideChange(PositionSide.Short) },
        modifier = modifier
    )
}

@Composable
fun MarginModeSelector(
    selectedMode: MarginMode,
    onModeChange: (MarginMode) -> Unit,
    modifier: Modifier = Modifier
) {
    TwoOptionSelector(
        firstText = "全仓",
        secondText = "逐仓",
        firstSelected = selectedMode == MarginMode.Cross,
        secondSelected = selectedMode == MarginMode.Isolated,
        onFirstClick = { onModeChange(MarginMode.Cross) },
        onSecondClick = { onModeChange(MarginMode.Isolated) },
        modifier = modifier
    )
}

@Composable
private fun TwoOptionSelector(
    firstText: String,
    secondText: String,
    firstSelected: Boolean,
    secondSelected: Boolean,
    onFirstClick: () -> Unit,
    onSecondClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.small,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.62f)
    ) {
        Row(
            modifier = Modifier.padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            SelectorButton(
                text = firstText,
                selected = firstSelected,
                onClick = onFirstClick,
                modifier = Modifier.weight(1f)
            )
            SelectorButton(
                text = secondText,
                selected = secondSelected,
                onClick = onSecondClick,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun SelectorButton(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = if (selected) {
        ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        )
    } else {
        ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }

    Button(
        onClick = onClick,
        modifier = modifier.heightIn(min = 42.dp),
        shape = MaterialTheme.shapes.small,
        colors = colors,
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
    ) {
        Text(text = text, fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium)
    }
}

@Composable
fun LeverageSelector(
    leverage: BigDecimal,
    onLeverageChange: (BigDecimal) -> Unit,
    modifier: Modifier = Modifier
) {
    val leverageValue = leverage.toFloat().coerceIn(1f, 125f)

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "杠杆",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "${leverageValue.toInt()}x",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
        Slider(
            value = leverageValue,
            onValueChange = { value ->
                onLeverageChange(BigDecimal(value.toInt().coerceIn(1, 125)))
            },
            valueRange = 1f..125f,
            steps = 123
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("1x", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("125x", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun NumberInput(
    value: BigDecimal?,
    onValueChange: (BigDecimal?) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    readOnly: Boolean = false,
    onReadOnlyTap: () -> Unit = {}
) {
    val textValue = value?.stripTrailingZeros()?.toPlainString() ?: ""
    var textFieldValue by remember { mutableStateOf(TextFieldValue(textValue)) }

    LaunchedEffect(value) {
        val updatedValue = value?.stripTrailingZeros()?.toPlainString() ?: ""
        val currentParsedValue = runCatching { BigDecimal(textFieldValue.text) }.getOrNull()
        val currentMatchesValue = currentParsedValue != null &&
            value != null &&
            currentParsedValue.compareTo(value) == 0

        if (value == null && textFieldValue.text.isNotEmpty()) {
            textFieldValue = TextFieldValue("")
        } else if (!currentMatchesValue && updatedValue != textFieldValue.text) {
            textFieldValue = TextFieldValue(updatedValue)
        }
    }

    OutlinedTextField(
        value = textFieldValue,
        onValueChange = {
            if (readOnly) {
                return@OutlinedTextField
            }

            val normalizedText = it.text.trim()
            textFieldValue = it.copy(text = normalizedText)
            if (normalizedText.isEmpty()) {
                onValueChange(null)
                return@OutlinedTextField
            }

            runCatching { BigDecimal(normalizedText) }
                .onSuccess(onValueChange)
        },
        label = { Text(label) },
        modifier = modifier
            .fillMaxWidth()
            .pointerInput(readOnly) {
                if (readOnly) {
                    detectTapGestures { onReadOnlyTap() }
                }
            },
        readOnly = readOnly,
        shape = MaterialTheme.shapes.small,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.55f),
            focusedContainerColor = MaterialTheme.colorScheme.surface,
            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
            cursorColor = MaterialTheme.colorScheme.primary
        )
    )
}

@Composable
fun SectionPanel(
    title: String,
    modifier: Modifier = Modifier,
    trailing: (@Composable () -> Unit)? = null,
    content: @Composable () -> Unit
) {
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Box {
                trailing?.invoke()
            }
        }
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.small,
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 1.dp,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.12f))
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                content()
            }
        }
    }
}
