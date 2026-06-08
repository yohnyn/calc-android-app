package com.personal.futurescalculator.ui

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.ImeAction
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
        firstSelectedColor = MaterialTheme.colorScheme.primary,
        secondSelectedColor = MaterialTheme.colorScheme.primary,
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
        firstSelectedColor = MaterialTheme.colorScheme.primary,
        secondSelectedColor = MaterialTheme.colorScheme.primary,
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
    firstSelectedColor: Color,
    secondSelectedColor: Color,
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
                selectedColor = firstSelectedColor,
                onClick = onFirstClick,
                modifier = Modifier.weight(1f)
            )
            SelectorButton(
                text = secondText,
                selected = secondSelected,
                selectedColor = secondSelectedColor,
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
    selectedColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = if (selected) {
        ButtonDefaults.buttonColors(
            containerColor = selectedColor,
            contentColor = Color.White
        )
    } else {
        ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }

    Button(
        onClick = onClick,
        modifier = modifier.height(30.dp),
        shape = MaterialTheme.shapes.small,
        colors = colors,
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 6.dp, vertical = 0.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium
        )
    }
}

@Composable
fun LeverageSelector(
    leverage: BigDecimal,
    onLeverageChange: (BigDecimal) -> Unit,
    selectedColor: Color = MaterialTheme.colorScheme.primary,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val leverageOptions = listOf("1x", "3x", "5x", "10x", "20x", "50x")
    val selectedLeverage = leverage.stripTrailingZeros().toPlainString() + "x"
    val selectedIsPreset = selectedLeverage in leverageOptions
    var customTapCount by remember { mutableStateOf(0) }
    var customEditing by remember { mutableStateOf(false) }
    var customText by remember { mutableStateOf(leverage.stripTrailingZeros().toPlainString()) }

    LaunchedEffect(leverage) {
        if (!customEditing) {
            customText = leverage.stripTrailingZeros().toPlainString()
        }
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "杠杆",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            leverageOptions.forEach { option ->
                LeverageButton(
                    text = option,
                    selected = option == selectedLeverage,
                    selectedColor = selectedColor,
                    onClick = {
                        customEditing = false
                        onLeverageChange(BigDecimal(option.removeSuffix("x")))
                    },
                    modifier = Modifier.weight(1f)
                )
            }
            CustomLeverageInput(
                text = customText,
                editing = customEditing,
                selected = !selectedIsPreset,
                selectedColor = selectedColor,
                modifier = Modifier.weight(1.15f),
                onTapWhenLocked = {
                    customTapCount += 1
                    if (customTapCount >= 2) {
                        customEditing = true
                        customTapCount = 0
                    }
                },
                onTextChange = { nextText ->
                    val normalized = nextText.trim()
                    customText = normalized
                    val parsed = runCatching { BigDecimal(normalized) }.getOrNull()
                    when {
                        parsed != null && parsed > BigDecimal("125") -> {
                            customText = ""
                            onLeverageChange(BigDecimal.ONE)
                            Toast.makeText(
                                context,
                                "杠杆不能超过 125x，已恢复为 1x",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        parsed != null && parsed >= BigDecimal.ONE -> {
                            onLeverageChange(parsed)
                        }
                    }
                }
            )
        }
    }
}

@Composable
private fun LeverageButton(
    text: String,
    selected: Boolean,
    selectedColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        shape = MaterialTheme.shapes.small,
        modifier = modifier.height(32.dp),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 2.dp, vertical = 0.dp),
        colors = if (selected) {
            ButtonDefaults.buttonColors(
                containerColor = selectedColor,
                contentColor = Color.White
            )
        } else {
            ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.62f),
                contentColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
    ) {
        Text(text = text, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun CustomLeverageInput(
    text: String,
    editing: Boolean,
    selected: Boolean,
    selectedColor: Color,
    modifier: Modifier = Modifier,
    onTapWhenLocked: () -> Unit,
    onTextChange: (String) -> Unit
) {
    val containerColor = if (selected) {
        selectedColor
    } else {
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.62f)
    }
    val contentColor = if (selected) {
        Color.White
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    Surface(
        modifier = modifier
            .height(32.dp)
            .pointerInput(editing) {
                if (!editing) {
                    detectTapGestures { onTapWhenLocked() }
                }
            },
        shape = MaterialTheme.shapes.small,
        color = containerColor
    ) {
        Box(
            modifier = Modifier.padding(horizontal = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            if (editing) {
                BasicTextField(
                    value = text,
                    onValueChange = onTextChange,
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    textStyle = MaterialTheme.typography.labelLarge.copy(
                        color = contentColor,
                        fontWeight = FontWeight.SemiBold
                    )
                )
            } else {
                Text(
                    text = if (selected) "${text}x" else "自定义",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Medium,
                    color = contentColor
                )
            }
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
    onReadOnlyTap: () -> Unit = {},
    onSubmit: (() -> Unit)? = null
) {
    val textValue = value?.stripTrailingZeros()?.toPlainString() ?: ""
    var textFieldValue by remember(label) { mutableStateOf(TextFieldValue(textValue)) }
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val finishInput = {
        focusManager.clearFocus(force = true)
        keyboardController?.hide()
        onSubmit?.invoke()
    }

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

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        BasicTextField(
            value = textFieldValue,
            onValueChange = {
                if (readOnly) {
                    return@BasicTextField
                }

                val normalizedText = it.text.trim()
                textFieldValue = it.copy(text = normalizedText)
                if (normalizedText.isEmpty()) {
                    onValueChange(null)
                    return@BasicTextField
                }

                runCatching { BigDecimal(normalizedText) }
                    .onSuccess(onValueChange)
            },
            modifier = Modifier
                .fillMaxWidth()
                .pointerInput(readOnly) {
                    if (readOnly) {
                        detectTapGestures { onReadOnlyTap() }
                    }
                },
            readOnly = readOnly,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Decimal,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(onDone = { finishInput() }),
            singleLine = true,
            textStyle = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurface),
            decorationBox = { innerTextField ->
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(40.dp),
                    shape = MaterialTheme.shapes.small,
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.55f))
                ) {
                    Box(
                        modifier = Modifier.padding(horizontal = 10.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        innerTextField()
                    }
                }
            }
        )
    }
}

@Composable
fun CompactTextInput(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    onSubmit: (() -> Unit)? = null
) {
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val finishInput = {
        focusManager.clearFocus(force = true)
        keyboardController?.hide()
        onSubmit?.invoke()
    }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { finishInput() }),
            textStyle = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurface),
            decorationBox = { innerTextField ->
                Surface(
                    modifier = Modifier.fillMaxWidth().height(40.dp),
                    shape = MaterialTheme.shapes.small,
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.55f))
                ) {
                    Box(
                        modifier = Modifier.padding(horizontal = 10.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        innerTextField()
                    }
                }
            }
        )
    }
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
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
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
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.16f))
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
