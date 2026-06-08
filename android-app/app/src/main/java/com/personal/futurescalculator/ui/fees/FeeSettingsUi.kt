package com.personal.futurescalculator.ui.fees

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.personal.futurescalculator.model.CalculationInput
import com.personal.futurescalculator.ui.NumberInput
import java.math.BigDecimal

@Composable
fun FeeSettingsDialog(
    input: CalculationInput,
    onConfirm: (CalculationInput) -> Unit,
    onDismiss: () -> Unit
) {
    var openFee by remember(input.openFeeRatePercent) { mutableStateOf(input.openFeeRatePercent) }
    var closeFee by remember(input.closeFeeRatePercent) { mutableStateOf(input.closeFeeRatePercent) }
    var maintenanceMarginRate by remember(input.maintenanceMarginRatePercent) {
        mutableStateOf(input.maintenanceMarginRatePercent)
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 400.dp),
            shape = MaterialTheme.shapes.medium,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.16f))
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "费率设置",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "费率单位为百分比，修改后点击确认生效。",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                FeeInputRow {
                    NumberInput(
                        value = openFee,
                        onValueChange = { openFee = it ?: BigDecimal.ZERO },
                        label = "开仓费率 %",
                        modifier = Modifier.weight(1f)
                    )
                    NumberInput(
                        value = closeFee,
                        onValueChange = { closeFee = it ?: BigDecimal.ZERO },
                        label = "平仓费率 %",
                        modifier = Modifier.weight(1f)
                    )
                }
                NumberInput(
                    value = maintenanceMarginRate,
                    onValueChange = { maintenanceMarginRate = it ?: BigDecimal.ZERO },
                    label = "维持保证金率 %"
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = {
                            openFee = BigDecimal("0.05")
                            closeFee = BigDecimal("0.05")
                            maintenanceMarginRate = BigDecimal("0.5")
                        }
                    ) {
                        Text("恢复默认")
                    }
                    TextButton(onClick = onDismiss) {
                        Text("取消", fontWeight = FontWeight.SemiBold)
                    }
                    Button(
                        onClick = {
                            onConfirm(
                                input.copy(
                                    openFeeRatePercent = openFee,
                                    closeFeeRatePercent = closeFee,
                                    maintenanceMarginRatePercent = maintenanceMarginRate
                                )
                            )
                        },
                        shape = MaterialTheme.shapes.small
                    ) {
                        Text("确认")
                    }
                }
            }
        }
    }
}

@Composable
private fun FeeInputRow(content: @Composable RowScope.() -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        content = content
    )
}
