package com.personal.futurescalculator.ui

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.Dp
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
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        SelectorChip(
            text = firstText,
            selected = firstSelected,
            selectedColor = firstSelectedColor,
            onClick = onFirstClick,
            modifier = Modifier.weight(1f)
        )
        SelectorChip(
            text = secondText,
            selected = secondSelected,
            selectedColor = secondSelectedColor,
            onClick = onSecondClick,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun SelectorChip(
    text: String,
    selected: Boolean,
    selectedColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .height(32.dp)
            .clickable(onClick = onClick),
        shape = MaterialTheme.shapes.small,
        color = if (selected) selectedColor.copy(alpha = 0.16f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.52f),
        border = BorderStroke(
            1.dp,
            if (selected) selectedColor.copy(alpha = 0.50f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.14f)
        )
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                text = text,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                color = if (selected) selectedColor else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun LeverageSelector(
    leverage: BigDecimal,
    onLeverageChange: (BigDecimal) -> Unit,
    modifier: Modifier = Modifier
) {
    val leverageOptions = listOf("1x", "3x", "5x", "10x", "20x", "50x", "100x")
    val selectedLeverage = leverage.stripTrailingZeros().toPlainString() + "x"
    val selectedIsPreset = selectedLeverage in leverageOptions
    var expanded by remember { mutableStateOf(false) }
    var showCustomDialog by remember { mutableStateOf(false) }
    var customText by remember { mutableStateOf(leverage.stripTrailingZeros().toPlainString()) }

    LaunchedEffect(leverage) {
        if (!showCustomDialog) {
            customText = leverage.stripTrailingZeros().toPlainString()
        }
    }

    if (showCustomDialog) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showCustomDialog = false },
            title = { Text("自定义杠杆", fontWeight = FontWeight.Bold) },
            text = {
                androidx.compose.material3.OutlinedTextField(
                    value = customText,
                    onValueChange = { customText = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("杠杆") },
                    singleLine = true
                )
            },
            confirmButton = {
                androidx.compose.material3.TextButton(onClick = {
                    val parsed = runCatching { BigDecimal(customText.trim()) }.getOrNull()
                    if (parsed != null && parsed >= BigDecimal.ONE && parsed <= BigDecimal("125")) {
                        onLeverageChange(parsed)
                        showCustomDialog = false
                    }
                }) {
                    Text("确认", fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                androidx.compose.material3.TextButton(onClick = {
                    showCustomDialog = false
                    customText = leverage.stripTrailingZeros().toPlainString()
                }) {
                    Text("取消", fontWeight = FontWeight.SemiBold)
                }
            }
        )
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = "杠杆",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Surface(
            modifier = Modifier.fillMaxWidth().height(40.dp).clickable { expanded = true },
            shape = MaterialTheme.shapes.small,
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.45f))
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (selectedIsPreset) selectedLeverage else "${leverage.stripTrailingZeros().toPlainString()}x",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                DropdownChevronIcon()
            }
        }
        AppDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            leverageOptions.forEach { option ->
                DropdownMenuItem(
                    text = { AppDropdownMenuText(option) },
                    onClick = {
                        expanded = false
                        onLeverageChange(BigDecimal(option.removeSuffix("x")))
                    },
                    contentPadding = AppDropdownMenuItemPadding
                )
            }
            DropdownMenuItem(
                text = { AppDropdownMenuText("自定义") },
                onClick = {
                    expanded = false
                    showCustomDialog = true
                    customText = leverage.stripTrailingZeros().toPlainString()
                },
                contentPadding = AppDropdownMenuItemPadding
            )
        }
    }
}

val AppDropdownMenuItemPadding = PaddingValues(horizontal = 18.dp, vertical = 10.dp)

@Composable
fun AppDropdownMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismissRequest,
        modifier = modifier.widthIn(min = 150.dp),
        shape = MaterialTheme.shapes.small,
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp,
        shadowElevation = 3.dp,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.14f)),
        content = content
    )
}

@Composable
fun AppDropdownMenuText(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurface
    )
}

@Composable
fun DropdownChevronIcon(
    modifier: Modifier = Modifier,
    iconSize: Dp = 18.dp,
    color: Color = MaterialTheme.colorScheme.primary
) {
    Canvas(modifier = modifier.size(iconSize)) {
        val stroke = 1.8.dp.toPx()
        drawLine(
            color = color,
            start = Offset(size.width * 0.28f, size.height * 0.42f),
            end = Offset(size.width * 0.50f, size.height * 0.62f),
            strokeWidth = stroke,
            cap = StrokeCap.Round
        )
        drawLine(
            color = color,
            start = Offset(size.width * 0.72f, size.height * 0.42f),
            end = Offset(size.width * 0.50f, size.height * 0.62f),
            strokeWidth = stroke,
            cap = StrokeCap.Round
        )
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
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
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
                modifier = Modifier.padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                content()
            }
        }
    }
}
