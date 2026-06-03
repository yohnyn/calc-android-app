package com.personal.futurescalculator.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.personal.futurescalculator.model.PositionSide
import com.personal.futurescalculator.model.MarginMode
import com.personal.futurescalculator.model.CalculationResult
import com.personal.futurescalculator.util.DecimalFormatters
import com.personal.futurescalculator.viewmodel.CalculatorViewModel
import java.math.BigDecimal

@Composable
fun CalculatorScreen(
    viewModel: CalculatorViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    
    val scrollState = rememberScrollState()
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Title
        Text(
            text = "合约收益计算器",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        
        Text(
            text = "USDT 本位 · 本地估算",
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        
        // Position Side Selector
        Text(
            text = "交易方向",
            style = MaterialTheme.typography.titleSmall
        )
        PositionSideSelector(
            selectedSide = uiState.input.side,
            onSideChange = { viewModel.updateInput(uiState.input.copy(side = it)) }
        )
        
        // Margin Mode Selector
        Text(
            text = "保证金模式",
            style = MaterialTheme.typography.titleSmall
        )
        MarginModeSelector(
            selectedMode = uiState.input.marginMode,
            onModeChange = { viewModel.updateInput(uiState.input.copy(marginMode = it)) }
        )
        
        // Leverage
        Text(
            text = "杠杆",
            style = MaterialTheme.typography.titleSmall
        )
        LeverageSelector(
            leverage = uiState.input.leverage,
            onLeverageChange = { viewModel.updateInput(uiState.input.copy(leverage = it)) }
        )
        
        // Input Fields
        Text(
            text = "输入项",
            style = MaterialTheme.typography.titleSmall
        )
        
        NumberInput(
            value = uiState.input.margin,
            onValueChange = { viewModel.updateInput(uiState.input.copy(margin = it)) },
            label = "投入保证金 (USDT)"
        )
        
        NumberInput(
            value = uiState.input.entryPrice,
            onValueChange = { viewModel.updateInput(uiState.input.copy(entryPrice = it)) },
            label = "开仓价 (USDT)"
        )
        
        NumberInput(
            value = uiState.input.exitPrice,
            onValueChange = { viewModel.updateInput(uiState.input.copy(exitPrice = it)) },
            label = "平仓价 (USDT)"
        )
        
        NumberInput(
            value = uiState.input.quantity,
            onValueChange = { viewModel.updateInput(uiState.input.copy(quantity = it)) },
            label = "成交数量 (币) - 可选"
        )
        
        NumberInput(
            value = uiState.input.feeRatePercent,
            onValueChange = { viewModel.updateInput(uiState.input.copy(feeRatePercent = it)) },
            label = "手续费率 (%)"
        )
        
        NumberInput(
            value = uiState.input.maintenanceMarginRatePercent,
            onValueChange = { viewModel.updateInput(uiState.input.copy(maintenanceMarginRatePercent = it)) },
            label = "维持保证金率 (%)"
        )
        
        // Results
        Text(
            text = "计算结果",
            style = MaterialTheme.typography.titleSmall
        )
        
        if (uiState.result != null) {
            ResultCard(result = uiState.result)
        } else {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "--",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        
        // Comparison Section
        Text(
            text = "对比方案",
            style = MaterialTheme.typography.titleSmall
        )
        
        if (uiState.comparisonItems.isNotEmpty()) {
            uiState.comparisonItems.forEachIndexed { index, item ->
                ComparisonItemCard(
                    item = item,
                    result = uiState.comparisonResults.getOrNull(index)?.result,
                    diff = uiState.comparisonResults.getOrNull(index),
                    onRemove = { viewModel.removeComparisonItem(index) }
                )
            }
        } else {
            Text(
                text = "暂无对比方案",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        // Action Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = { viewModel.reset() },
                modifier = Modifier.weight(1f)
            ) {
                Text("重置")
            }
            
            Button(
                onClick = { /* Copy to clipboard */ },
                modifier = Modifier.weight(1f)
            ) {
                Text("复制结果")
            }
        }
        
        // Add Comparison Button
        Button(
            onClick = { viewModel.copyCurrentToComparison() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("添加对比方案")
        }
    }
}

@Composable
fun ResultCard(result: CalculationResult) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "仓位价值",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "${DecimalFormatters.formatCurrency(result.positionValue)} USDT",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold
            )
            
            Text(
                text = "初始保证金",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "${DecimalFormatters.formatCurrency(result.requiredMargin)} USDT",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold
            )
            
            Text(
                text = "未扣手续费盈亏",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "${DecimalFormatters.formatCurrency(result.grossPnl)} USDT",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = if (result.grossPnl != null && result.grossPnl >= BigDecimal.ZERO) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.error
                }
            )
            
            Text(
                text = "手续费",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "${DecimalFormatters.formatCurrency(result.totalFee)} USDT",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold
            )
            
            Text(
                text = "净盈亏",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "${DecimalFormatters.formatCurrency(result.netPnl)} USDT",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = if (result.netPnl != null && result.netPnl >= BigDecimal.ZERO) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.error
                }
            )
            
            Text(
                text = "ROI",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "${DecimalFormatters.formatPercentage(result.roiPercent)}",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = if (result.roiPercent != null && result.roiPercent >= BigDecimal.ZERO) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.error
                }
            )
            
            Text(
                text = "估算强平价",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "${DecimalFormatters.formatCurrency(result.liquidationPrice)} USDT",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun ComparisonItemCard(
    item: com.personal.futurescalculator.model.ComparisonItem,
    result: CalculationResult?,
    diff: com.personal.futurescalculator.model.ComparisonResult?,
    onRemove: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.titleMedium
                )
                IconButton(onClick = onRemove) {
                    Icon(
                        imageVector = androidx.compose.material.icons.Icons.Default.Delete,
                        contentDescription = "删除对比方案"
                    )
                }
            }
            
            Text(
                text = "${if (item.input.side == PositionSide.Long) "做多" else "做空"} | " +
                       "${if (item.input.marginMode == MarginMode.Cross) "全仓" else "逐仓"} | " +
                       "杠杆 ${item.input.leverage}x",
                style = MaterialTheme.typography.bodySmall
            )
            
            if (result != null) {
                Text(
                    text = "净盈亏：${DecimalFormatters.formatCurrency(result.netPnl)} USDT",
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (result.netPnl != null && result.netPnl >= BigDecimal.ZERO) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.error
                    }
                )
                
                Text(
                    text = "ROI：${DecimalFormatters.formatPercentage(result.roiPercent)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (result.roiPercent != null && result.roiPercent >= BigDecimal.ZERO) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.error
                    }
                )
                
                if (diff?.netPnlDiff != null) {
                    Text(
                        text = "比当前：${DecimalFormatters.formatPositiveNegative(diff.netPnlDiff)} USDT",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                
                if (diff?.roiDiffPercent != null) {
                    Text(
                        text = "ROI 差值：${DecimalFormatters.formatPositiveNegative(diff.roiDiffPercent)}%",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            } else {
                Text(
                    text = "计算结果不可用",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
