package com.personal.futurescalculator.ui

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.personal.futurescalculator.model.AmountField
import com.personal.futurescalculator.model.AveragingDecisionInput
import com.personal.futurescalculator.model.AveragingDecisionResult
import com.personal.futurescalculator.model.CalculationInput
import com.personal.futurescalculator.model.CalculationResult
import com.personal.futurescalculator.model.HomeModule
import com.personal.futurescalculator.model.HistoryCategory
import com.personal.futurescalculator.model.HistoryField
import com.personal.futurescalculator.model.HistoryRecord
import com.personal.futurescalculator.model.HistorySection
import com.personal.futurescalculator.model.ComparisonItem
import com.personal.futurescalculator.model.MarginMode
import com.personal.futurescalculator.model.PositionSide
import com.personal.futurescalculator.model.SettlementMode
import com.personal.futurescalculator.model.ThemeMode
import com.personal.futurescalculator.ui.theme.LossRed
import com.personal.futurescalculator.ui.theme.LocalProfitLossPalette
import com.personal.futurescalculator.ui.theme.ProfitGreen
import com.personal.futurescalculator.ui.theme.ProfitLossPalette
import com.personal.futurescalculator.ui.staticpages.DonationScreen
import com.personal.futurescalculator.ui.averaging.AveragingDecisionEntryCard
import com.personal.futurescalculator.ui.averaging.AveragingDecisionSection
import com.personal.futurescalculator.ui.averaging.AveragingResultDialog
import com.personal.futurescalculator.ui.averaging.ExistingScheme
import com.personal.futurescalculator.ui.averaging.averagingMissingFields
import com.personal.futurescalculator.ui.coin.CoinMarketHeader
import com.personal.futurescalculator.ui.coin.CoinSelectorDialog
import com.personal.futurescalculator.ui.comparison.ComparisonResultDialog
import com.personal.futurescalculator.ui.comparison.ComparisonSchemeEditorDialog
import com.personal.futurescalculator.ui.comparison.ComparisonSchemeListCard
import com.personal.futurescalculator.ui.comparison.CopySchemeDialog
import com.personal.futurescalculator.ui.comparison.MAIN_SCHEME_ID
import com.personal.futurescalculator.ui.comparison.buildComparisonSchemes
import com.personal.futurescalculator.ui.comparison.createComparisonHistorySnapshot
import com.personal.futurescalculator.ui.comparison.selectedComparisonValidationMessage
import com.personal.futurescalculator.ui.history.HistoryScreen
import com.personal.futurescalculator.ui.results.CoinMarginedResultDialog
import com.personal.futurescalculator.ui.results.CompactExpandableResultCard
import com.personal.futurescalculator.ui.results.EmptyResult
import com.personal.futurescalculator.ui.results.MainResultDialog
import com.personal.futurescalculator.ui.results.pnlColor
import com.personal.futurescalculator.ui.results.pnlText
import com.personal.futurescalculator.ui.settings.CoinMarginedModeDialog
import com.personal.futurescalculator.ui.settings.PnlDisplayMode
import com.personal.futurescalculator.ui.settings.SettingsScreen
import com.personal.futurescalculator.util.ClipboardFormatter
import com.personal.futurescalculator.util.DecimalFormatters
import com.personal.futurescalculator.viewmodel.CalculatorViewModel
import java.math.BigDecimal
import kotlinx.coroutines.launch

private const val RESULT_CARD_MAIN = "main"
private const val RESULT_CARD_COIN = "coin"
private const val RESULT_CARD_AVERAGING = "averaging"

@Composable
fun CalculatorScreen(
    viewModel: CalculatorViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()
    var showFeeSettings by rememberSaveable { mutableStateOf(false) }
    var showCoinSelector by rememberSaveable { mutableStateOf(false) }
    var showSettings by remember { mutableStateOf(false) }
    var showDonation by remember { mutableStateOf(false) }
    var showHistory by remember { mutableStateOf(false) }
    var showMainResultDialog by rememberSaveable { mutableStateOf(false) }
    var showCoinMarginedResultDialog by rememberSaveable { mutableStateOf(false) }
    var showAveragingResultDialog by rememberSaveable { mutableStateOf(false) }
    var showComparisonResultDialog by rememberSaveable { mutableStateOf(false) }
    var revealedResultCards by rememberSaveable { mutableStateOf(emptySet<String>()) }
    var showCopySchemeDialog by rememberSaveable { mutableStateOf(false) }
    var comparisonEditorItem by remember { mutableStateOf<ComparisonItem?>(null) }
    var selectedComparisonIds by rememberSaveable { mutableStateOf(setOf<String>(MAIN_SCHEME_ID)) }
    var averagingValidationMessage by rememberSaveable { mutableStateOf<String?>(null) }
    var operationRequirementMessage by rememberSaveable { mutableStateOf<String?>(null) }
    var averagingSymbolOverride by rememberSaveable { mutableStateOf<String?>(null) }
    var pnlDisplayMode by rememberSaveable { mutableStateOf(PnlDisplayMode.ProfitGreen) }
    var feedbackText by rememberSaveable { mutableStateOf("") }
    var comparisonSectionExpanded by rememberSaveable { mutableStateOf(true) }
    val hasTargetStopPlan = uiState.input.totalFunds != null ||
        uiState.input.targetProfitAmount != null ||
        uiState.input.targetRoiPercent != null ||
        uiState.input.maxLossAmount != null ||
        uiState.input.maxLossRoiPercent != null
    var targetStopExpanded by rememberSaveable { mutableStateOf(hasTargetStopPlan) }
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val hasPositionResult = uiState.result != null
    val hasPnlResult = uiState.result?.netPnl != null
    val profitLossPalette = when (pnlDisplayMode) {
        PnlDisplayMode.ProfitGreen -> ProfitLossPalette(ProfitGreen, LossRed)
        PnlDisplayMode.ProfitRed -> ProfitLossPalette(LossRed, ProfitGreen)
        PnlDisplayMode.IconsOnly -> ProfitLossPalette(
            profit = MaterialTheme.colorScheme.onSurface,
            loss = MaterialTheme.colorScheme.onSurface,
            profitIndicator = "▲ ",
            lossIndicator = "▼ "
        )
    }
    val selectedCoin = uiState.coins.firstOrNull { it.id == uiState.selectedCoinId }
    val hasValidComparisonScheme = uiState.comparisonItems.indices.any { index ->
        uiState.comparisonResults.getOrNull(index)?.result?.netPnl != null
    }
    val averagingSchemes = if (hasValidComparisonScheme) {
        buildList {
            uiState.result?.takeIf { it.netPnl != null }?.let {
                add(ExistingScheme("当前主方案", selectedCoin?.symbol ?: "币", uiState.input, it))
            }
            uiState.comparisonItems.forEachIndexed { index, item ->
                uiState.comparisonResults.getOrNull(index)?.result?.takeIf { it.netPnl != null }?.let {
                    add(
                        ExistingScheme(
                            item.name,
                            uiState.coins.firstOrNull { coin -> coin.id == item.coinId }?.symbol ?: "币",
                            item.input,
                            it
                        )
                    )
                }
            }
        }
    } else {
        emptyList()
    }
    val comparisonSchemes = buildComparisonSchemes(
        mainInput = uiState.input,
        mainResult = uiState.result,
        mainSymbol = selectedCoin?.symbol ?: "币",
        mainSettlementMode = uiState.settlementMode,
        mainCoinMarginedCalculationMode = uiState.coinMarginedCalculationMode,
        items = uiState.comparisonItems,
        results = uiState.comparisonResults,
        coins = uiState.coins
    )
    val hideKeyboardAndClearFocus: () -> Unit = {
        focusManager.clearFocus(force = true)
        keyboardController?.hide()
    }
    val submitMainResult: () -> Unit = {
        hideKeyboardAndClearFocus()
        when {
            uiState.settlementMode == SettlementMode.UsdtMargined && uiState.result != null ->
                showMainResultDialog = true
            uiState.settlementMode == SettlementMode.CoinMargined && uiState.coinMarginedResult != null ->
                showCoinMarginedResultDialog = true
        }
    }

    LaunchedEffect(uiState.comparisonItems.map { it.id }) {
        val validIds = uiState.comparisonItems.map { it.id }.toSet() + MAIN_SCHEME_ID
        selectedComparisonIds = selectedComparisonIds.intersect(validIds)
    }
    LaunchedEffect(hasTargetStopPlan) {
        if (hasTargetStopPlan) {
            targetStopExpanded = true
        }
    }
    CompositionLocalProvider(LocalProfitLossPalette provides profitLossPalette) {
    if (showSettings) {
        SettingsScreen(
            pnlDisplayMode = pnlDisplayMode,
            onPnlDisplayModeChange = { pnlDisplayMode = it },
            moduleOrder = uiState.moduleOrder,
            visibleModules = uiState.visibleModules,
            onModuleOrderChange = viewModel::updateModuleOrder,
            onResetModuleOrder = viewModel::resetModuleOrder,
            onModuleVisibilityChange = viewModel::setModuleVisible,
            onResetModuleVisibility = viewModel::resetModuleVisibility,
            feedbackText = feedbackText,
            onFeedbackChange = { feedbackText = it },
            priceUpdatedAt = uiState.priceUpdatedAt,
            themeMode = uiState.themeMode,
            onThemeModeChange = viewModel::updateThemeMode,
            coinMarginedCalculationMode = uiState.coinMarginedCalculationMode,
            onCoinMarginedCalculationModeChange = viewModel::updateCoinMarginedCalculationMode,
            onBack = { showSettings = false }
        )
        return@CompositionLocalProvider
    }
    if (showDonation) {
        DonationScreen(onBack = { showDonation = false })
        return@CompositionLocalProvider
    }
    if (showHistory) {
        HistoryScreen(
            records = uiState.historyRecords,
            onToggleFavorite = viewModel::toggleHistoryFavorite,
            onDelete = viewModel::deleteHistoryRecords,
            onClearCategory = viewModel::clearHistoryCategory,
            onBack = { showHistory = false }
        )
        return@CompositionLocalProvider
    }
    if (showFeeSettings) {
        FeeSettingsDialog(
            input = uiState.input,
            onConfirm = {
                viewModel.updateInput(it)
                showFeeSettings = false
            },
            onDismiss = { showFeeSettings = false }
        )
    }
    if (showCoinSelector) {
        CoinSelectorDialog(
            coins = uiState.coins,
            selectedCoinId = uiState.selectedCoinId,
            onSelect = {
                viewModel.selectCoin(it)
                showCoinSelector = false
            },
            onAddCustom = viewModel::addCustomCoin,
            onDeleteCustom = viewModel::deleteCustomCoin,
            onDismiss = { showCoinSelector = false }
        )
    }
    if (showMainResultDialog && uiState.result != null) {
        MainResultDialog(
            input = uiState.input,
            result = uiState.result!!,
            symbol = selectedCoin?.symbol ?: "币",
            onDismiss = {
                revealedResultCards = revealedResultCards + RESULT_CARD_MAIN
                showMainResultDialog = false
            }
        )
    }
    if (showAveragingResultDialog && uiState.averagingDecisionResult != null) {
        AveragingResultDialog(
            input = uiState.averagingDecisionInput,
            result = uiState.averagingDecisionResult!!,
            symbol = averagingSymbolOverride ?: selectedCoin?.symbol ?: "币",
            onDismiss = {
                revealedResultCards = revealedResultCards + RESULT_CARD_AVERAGING
                showAveragingResultDialog = false
            }
        )
    }
    if (showCoinMarginedResultDialog && uiState.coinMarginedResult != null) {
        CoinMarginedResultDialog(
            input = uiState.input,
            result = uiState.coinMarginedResult!!,
            symbol = selectedCoin?.symbol ?: "币",
            onDismiss = {
                revealedResultCards = revealedResultCards + RESULT_CARD_COIN
                showCoinMarginedResultDialog = false
            }
        )
    }
    if (showComparisonResultDialog) {
        ComparisonResultDialog(
            schemes = comparisonSchemes.filter { it.id in selectedComparisonIds },
            onDismiss = { showComparisonResultDialog = false }
        )
    }
    if (uiState.showCoinMarginedModeDialog) {
        CoinMarginedModeDialog(
            initialMode = uiState.coinMarginedCalculationMode,
            onConfirm = viewModel::onCoinMarginedModeSelected,
            onDismiss = viewModel::dismissCoinMarginedModeDialog
        )
    }
    if (showCopySchemeDialog) {
        CopySchemeDialog(
            schemes = comparisonSchemes.filter { it.result?.netPnl != null },
            onSelect = { scheme ->
                clipboardManager.setText(
                    AnnotatedString(
                        ClipboardFormatter.formatSinglePerformance(
                            name = scheme.name,
                            symbol = scheme.symbol,
                            input = scheme.input,
                            result = scheme.result
                        )
                    )
                )
                showCopySchemeDialog = false
                Toast.makeText(context, "${scheme.name}战绩已复制", Toast.LENGTH_SHORT).show()
            },
            onDismiss = { showCopySchemeDialog = false }
        )
    }
    comparisonEditorItem?.let { item ->
        ComparisonSchemeEditorDialog(
            initialItem = item,
            coins = uiState.coins,
            onSave = {
                hideKeyboardAndClearFocus()
                viewModel.saveComparisonItem(it)
                selectedComparisonIds = selectedComparisonIds + it.id
                comparisonEditorItem = null
            },
            onDelete = {
                viewModel.removeComparisonItem(it.id)
                selectedComparisonIds = selectedComparisonIds - it.id
                comparisonEditorItem = null
            },
            onDismiss = { comparisonEditorItem = null }
        )
    }
    averagingValidationMessage?.let { message ->
        MissingParametersDialog(
            message = message,
            onDismiss = { averagingValidationMessage = null }
        )
    }
    operationRequirementMessage?.let { message ->
        OperationRequirementDialog(
            message = message,
            onDismiss = { operationRequirementMessage = null }
        )
    }
    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(scrollState)
                    .padding(horizontal = 16.dp, vertical = 18.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.small,
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 1.dp,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.12f))
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CoinMarketHeader(
                        coin = selectedCoin,
                        onClick = { showCoinSelector = true }
                    )
                    TwoOptionModeSelector(
                        firstText = "U 本位",
                        secondText = "币本位",
                        firstSelected = uiState.settlementMode == SettlementMode.UsdtMargined,
                        onFirstClick = { viewModel.onContractModeChanged(SettlementMode.UsdtMargined) },
                        onSecondClick = { viewModel.onContractModeChanged(SettlementMode.CoinMargined) }
                    )
                }
            }

            uiState.moduleOrder.filter { it in uiState.visibleModules }.forEach { module ->
            when (module) {
            HomeModule.Position -> {
            SectionPanel(
                title = "仓位参数",
                trailing = {
                    TextButton(onClick = { showFeeSettings = true }) {
                        Text("高级设置")
                    }
                }
            ) {
                InputRow {
                    PositionSideSelector(
                        selectedSide = uiState.input.side,
                        onSideChange = { viewModel.updateInput(uiState.input.copy(side = it)) },
                        modifier = Modifier.weight(1f)
                    )
                    MarginModeSelector(
                        selectedMode = uiState.input.marginMode,
                        onModeChange = { viewModel.updateInput(uiState.input.copy(marginMode = it)) },
                        modifier = Modifier.weight(1f)
                    )
                }
                InputRow {
                    NumberInput(
                        value = uiState.input.entryPrice,
                        onValueChange = { viewModel.updateInput(uiState.input.copy(entryPrice = it)) },
                        label = "开仓价",
                        modifier = Modifier.weight(1f),
                        onSubmit = submitMainResult
                    )
                    if (uiState.settlementMode == SettlementMode.UsdtMargined) {
                        NumberInput(
                            value = uiState.input.margin,
                            onValueChange = { viewModel.updateAmountInput(AmountField.Margin, it) },
                            label = "保证金 USDT",
                            modifier = Modifier.weight(1f),
                            onSubmit = submitMainResult
                        )
                    } else {
                        NumberInput(
                            value = uiState.input.quantity,
                            onValueChange = { viewModel.updateAmountInput(AmountField.Quantity, it) },
                            label = "${selectedCoin?.symbol ?: "币"} 数量",
                            modifier = Modifier.weight(1f),
                            onSubmit = submitMainResult
                        )
                    }
                }
                NumberInput(
                    value = uiState.input.exitPrice,
                    onValueChange = {
                        viewModel.updateInput(
                            uiState.input.copy(
                                exitPrice = it,
                                targetProfitAmount = null,
                                targetRoiPercent = null,
                                maxLossAmount = null,
                                maxLossRoiPercent = null
                            )
                        )
                    },
                    label = "平仓价",
                    onSubmit = submitMainResult
                )
                LeverageSelector(
                    leverage = uiState.input.leverage,
                    onLeverageChange = { viewModel.updateInput(uiState.input.copy(leverage = it)) }
                )
                Text(
                    text = if (uiState.settlementMode == SettlementMode.UsdtMargined) {
                        "U 本位使用保证金与杠杆计算仓位，填写平仓价后可查看本单实际盈亏。"
                    } else {
                        "币本位使用币数量计算，填写平仓价后收益按当前币种价格折算。"
                    },
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            }

            HomeModule.TargetStop -> {
            SectionPanel(
                title = "止盈止损",
                trailing = {
                    TextButton(
                        onClick = { targetStopExpanded = !targetStopExpanded },
                        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text(if (targetStopExpanded) "收起" else "展开", fontWeight = FontWeight.Bold)
                    }
                }
            ) {
                if (!targetStopExpanded) {
                    Text(
                        text = "可选。只想先开单时不用填写；需要规划盈利、亏损或全仓强平风险时再展开。",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else if (uiState.settlementMode == SettlementMode.UsdtMargined) {
                    Text(
                        text = "这里用于计划止盈止损，不影响仓位建立。填写任一目标后，会反推出对应止盈价或止损价；仓位参数里的平仓价会优先按实际平仓价计算。",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (uiState.input.marginMode == MarginMode.Cross) {
                        NumberInput(
                            value = uiState.input.totalFunds,
                            onValueChange = { viewModel.updateInput(uiState.input.copy(totalFunds = it)) },
                            label = "账户总资金 USDT（全仓强平估算）",
                            onSubmit = submitMainResult
                        )
                        Text(
                            text = "全仓时账户总资金会影响强平价估算；只关心本单盈亏时可先不填。",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(
                        text = "止盈：按收益目标反推止盈价",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                    InputRow {
                        NumberInput(
                            value = uiState.input.targetProfitAmount,
                            onValueChange = { viewModel.updateInput(uiState.input.copy(targetProfitAmount = it, targetRoiPercent = null, exitPrice = null)) },
                            label = "目标收益 USDT",
                            modifier = Modifier.weight(1f),
                            onSubmit = submitMainResult
                        )
                        NumberInput(
                            value = uiState.input.targetRoiPercent,
                            onValueChange = { viewModel.updateInput(uiState.input.copy(targetProfitAmount = null, targetRoiPercent = it, exitPrice = null)) },
                            label = "目标收益 ROI %",
                            modifier = Modifier.weight(1f),
                            onSubmit = submitMainResult
                        )
                    }
                    Text(
                        text = "止损：按亏损目标反推止损价",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                    InputRow {
                        NumberInput(
                            value = uiState.input.maxLossAmount,
                            onValueChange = { viewModel.updateInput(uiState.input.copy(maxLossAmount = it, maxLossRoiPercent = null, exitPrice = null)) },
                            label = "目标亏损 USDT",
                            modifier = Modifier.weight(1f),
                            onSubmit = submitMainResult
                        )
                        NumberInput(
                            value = uiState.input.maxLossRoiPercent,
                            onValueChange = { viewModel.updateInput(uiState.input.copy(maxLossAmount = null, maxLossRoiPercent = it, exitPrice = null)) },
                            label = "目标亏损 ROI %",
                            modifier = Modifier.weight(1f),
                            onSubmit = submitMainResult
                        )
                    }
                } else {
                    Text(
                        text = "币本位当前先按仓位参数里的平仓价计算盈亏，暂不支持通过收益目标反推止盈止损价格。",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            }

            HomeModule.Result -> {
            if (uiState.settlementMode == SettlementMode.UsdtMargined && hasPositionResult && RESULT_CARD_MAIN in revealedResultCards) {
                SectionPanel(title = "计算结果") {
                    CompactExpandableResultCard(
                        label = if (uiState.result!!.netPnl != null) "净盈亏 · ROI" else "止盈止损计划",
                        value = if (uiState.result!!.netPnl != null) {
                            "${pnlText(uiState.result!!.netPnl, DecimalFormatters.formatPositiveNegative(uiState.result!!.netPnl))} USDT · ${DecimalFormatters.formatPercentage(uiState.result!!.roiPercent)}"
                        } else {
                            "已反推目标价，点击查看详情"
                        },
                        valueColor = pnlColor(uiState.result!!.netPnl),
                        onClick = {
                            hideKeyboardAndClearFocus()
                            showMainResultDialog = true
                        }
                    )
                    if (uiState.result?.netPnl != null) {
                        SoftOutlinedButton(
                            onClick = {
                                viewModel.saveHistoryRecord(
                                    createProfitHistorySnapshot(
                                        input = uiState.input,
                                        result = uiState.result!!,
                                        symbol = selectedCoin?.symbol ?: "币",
                                        settlementMode = uiState.settlementMode
                                    )
                                )
                                Toast.makeText(context, "本次收益测算已保存", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) { Text("保存本次结果") }
                    }
                }
            } else if (uiState.settlementMode == SettlementMode.CoinMargined && uiState.coinMarginedResult != null && RESULT_CARD_COIN in revealedResultCards) {
                SectionPanel(title = "币本位结果") {
                    CompactExpandableResultCard(
                        label = "币本位盈亏",
                        value = "${DecimalFormatters.formatPositiveNegative(uiState.coinMarginedResult!!.pnlCoin)} ${selectedCoin?.symbol ?: "币"}",
                        valueColor = pnlColor(uiState.coinMarginedResult!!.pnlCoin),
                        onClick = {
                            hideKeyboardAndClearFocus()
                            showCoinMarginedResultDialog = true
                        }
                    )
                }
            }
            }

            HomeModule.Averaging -> {
            if (uiState.averagingExpanded) {
                AveragingDecisionSection(
                    input = uiState.averagingDecisionInput,
                    result = uiState.averagingDecisionResult,
                    showResultCard = RESULT_CARD_AVERAGING in revealedResultCards,
                    schemes = averagingSchemes,
                    symbol = averagingSymbolOverride ?: selectedCoin?.symbol ?: "币",
                    onInputChange = viewModel::updateAveragingDecisionInput,
                    onCollapse = { viewModel.setAveragingExpanded(false) },
                    onSchemeFilled = { averagingSymbolOverride = it.symbol },
                    onRequestResult = {
                        hideKeyboardAndClearFocus()
                        val hasCurrentHolding =
                            uiState.averagingDecisionInput.currentEntryPrice?.let { it > BigDecimal.ZERO } == true &&
                                uiState.averagingDecisionInput.currentQuantity?.let { it > BigDecimal.ZERO } == true
                        val missing = averagingMissingFields(uiState.averagingDecisionInput)
                        if (!hasCurrentHolding) {
                            operationRequirementMessage = "您必须先持有一单，才能进行补仓操作。"
                        } else if (missing.isNotEmpty()) {
                            averagingValidationMessage = missing.joinToString("、")
                        } else {
                            showAveragingResultDialog = true
                        }
                    }
                )
                if (uiState.averagingDecisionResult != null) {
                    SoftOutlinedButton(
                        onClick = {
                            viewModel.saveHistoryRecord(
                                createAveragingHistorySnapshot(
                                    uiState.averagingDecisionInput,
                                    uiState.averagingDecisionResult!!,
                                    averagingSymbolOverride ?: selectedCoin?.symbol ?: "币"
                                )
                            )
                            Toast.makeText(context, "本次补仓模拟已保存", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("保存本次结果") }
                }
            } else {
                AveragingDecisionEntryCard(onClick = { viewModel.setAveragingExpanded(true) })
            }
            }

            HomeModule.Comparison -> {
            SectionPanel(
                title = "收益方案对比",
                trailing = {
                    TextButton(
                        onClick = { comparisonSectionExpanded = !comparisonSectionExpanded },
                        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text(if (comparisonSectionExpanded) "收起" else "展开", fontWeight = FontWeight.Bold)
                    }
                }
            ) {
                if (!comparisonSectionExpanded) {
                    Text(
                        text = "共 ${comparisonSchemes.size} 个方案，点击展开继续编辑或查看收益对比。",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                Text(
                    text = "比较多个交易方案最终收益",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                    Text(
                        text = "选择对比",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                    comparisonSchemes.forEach { scheme ->
                        ComparisonSchemeListCard(
                            scheme = scheme,
                            selected = scheme.id in selectedComparisonIds,
                            onSelectedChange = { selected ->
                                selectedComparisonIds = if (selected && selectedComparisonIds.size >= 3) {
                                    operationRequirementMessage = "一次最多比较3个方案。"
                                    selectedComparisonIds
                                } else if (selected) {
                                    selectedComparisonIds + scheme.id
                                } else {
                                    selectedComparisonIds - scheme.id
                                }
                            },
                            onClick = if (scheme.isMain) null else {
                                { comparisonEditorItem = uiState.comparisonItems.firstOrNull { it.id == scheme.id } }
                            }
                        )
                    }
                    SoftOutlinedButton(
                        onClick = {
                            comparisonEditorItem = ComparisonItem(
                                id = "item_${System.currentTimeMillis()}",
                                name = "方案 ${uiState.comparisonItems.size + 2}",
                                coinId = uiState.selectedCoinId,
                                settlementMode = uiState.settlementMode,
                                coinMarginedCalculationMode = uiState.coinMarginedCalculationMode,
                                input = CalculationInput()
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.small
                    ) {
                        Text("+ 添加方案")
                    }
                    Button(
                        onClick = {
                            val selectedSchemes = comparisonSchemes.filter { it.id in selectedComparisonIds }
                            val message = selectedComparisonValidationMessage(selectedSchemes)
                            if (message != null) {
                                operationRequirementMessage = message
                            } else {
                                hideKeyboardAndClearFocus()
                                showComparisonResultDialog = true
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.small
                    ) {
                        Text("展开收益对比结果")
                    }
                }
            }
            }
            HomeModule.History -> Unit
            }
            }

            if (uiState.settlementMode == SettlementMode.UsdtMargined && hasPnlResult) {
            Button(
                onClick = {
                    showCopySchemeDialog = true
                },
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.small
            ) {
                Text("一键复制战绩")
            }
            }
            SupportAuthorCard(onClick = { showDonation = true })
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                SoftOutlinedButton(
                    onClick = { showHistory = true },
                    modifier = Modifier.weight(1f),
                    shape = MaterialTheme.shapes.small
                ) {
                    Text("历史")
                }
                SoftOutlinedButton(
                    onClick = {
                        showFeeSettings = false
                        showMainResultDialog = false
                        showCoinMarginedResultDialog = false
                        showComparisonResultDialog = false
                        showAveragingResultDialog = false
                        revealedResultCards = emptySet()
                        averagingValidationMessage = null
                        operationRequirementMessage = null
                        averagingSymbolOverride = null
                        viewModel.reset()
                        coroutineScope.launch { scrollState.animateScrollTo(0) }
                    },
                    modifier = Modifier.weight(1f),
                    shape = MaterialTheme.shapes.small
                ) {
                    Text("↺ 重置")
                }
                Button(
                    onClick = { showSettings = true },
                    modifier = Modifier.weight(1f),
                    shape = MaterialTheme.shapes.small
                ) {
                    Text("⚙ 设置")
                }
            }
        }
    }
    }
}

@Composable
private fun SupportAuthorCard(onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = MaterialTheme.shapes.small,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.30f)
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.24f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.width(34.dp).height(34.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.78f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.18f))
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = "☕",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Text(
                    text = "支持作者",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "如果这个工具帮助到了你，\n欢迎支持后续更新。",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = "查看",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun InputRow(content: @Composable RowScope.() -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        content = content
    )
}

@Composable
private fun SoftOutlinedButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: Shape = MaterialTheme.shapes.small,
    border: BorderStroke? = null,
    content: @Composable RowScope.() -> Unit
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        shape = shape,
        border = border ?: BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outline.copy(alpha = 0.24f)
        ),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.58f),
            contentColor = MaterialTheme.colorScheme.onSurface,
            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.24f),
            disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.42f)
        ),
        content = content
    )
}

@Composable
private fun TwoOptionModeSelector(
    firstText: String,
    secondText: String,
    firstSelected: Boolean,
    onFirstClick: () -> Unit,
    onSecondClick: () -> Unit
) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Button(
            onClick = onFirstClick,
            modifier = Modifier.weight(1f).height(34.dp),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 0.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (firstSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                contentColor = if (firstSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
            ),
            shape = MaterialTheme.shapes.small
        ) { Text(firstText) }
        Button(
            onClick = onSecondClick,
            modifier = Modifier.weight(1f).height(34.dp),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 0.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (!firstSelected) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.surfaceVariant,
                contentColor = if (!firstSelected) MaterialTheme.colorScheme.onSecondary else MaterialTheme.colorScheme.onSurfaceVariant
            ),
            shape = MaterialTheme.shapes.small
        ) { Text(secondText) }
    }
}

@Composable
private fun FeeSettingsDialog(
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
                InputRow {
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
private fun MissingParametersDialog(
    message: String,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth().widthIn(max = 400.dp),
            shape = MaterialTheme.shapes.medium,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.18f))
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "补仓参数未填写完整",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "请先填写：$message",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.small
                ) {
                    Text("知道了")
                }
            }
        }
    }
}

@Composable
private fun OperationRequirementDialog(
    message: String,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth().widthIn(max = 400.dp),
            shape = MaterialTheme.shapes.medium,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.24f))
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "暂时无法操作",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.small
                ) {
                    Text("知道了")
                }
            }
        }
    }
}

private fun createProfitHistorySnapshot(
    input: CalculationInput,
    result: CalculationResult,
    symbol: String,
    settlementMode: SettlementMode
): HistoryRecord = HistoryRecord(
    id = "history_${System.currentTimeMillis()}",
    category = HistoryCategory.ProfitCalculation,
    title = "$symbol ${input.side.label()}",
    summary = "${DecimalFormatters.formatPositiveNegative(result.netPnl)} USDT",
    roiSummary = DecimalFormatters.formatPercentage(result.roiPercent),
    savedAt = System.currentTimeMillis(),
    sections = listOf(
        HistorySection("交易参数", listOf(
            HistoryField("币种", symbol),
            HistoryField("合约模式", if (settlementMode == SettlementMode.UsdtMargined) "U 本位" else "币本位"),
            HistoryField("方向", input.side.label()),
            HistoryField("保证金模式", input.marginMode.label()),
            HistoryField("杠杆", "${input.leverage.stripTrailingZeros().toPlainString()}x"),
            HistoryField("保证金", "${DecimalFormatters.formatCurrency(input.margin)} USDT"),
            HistoryField("数量", "${DecimalFormatters.formatQuantity(result.quantity)} $symbol"),
            HistoryField("开仓价", "${DecimalFormatters.formatCurrency(input.entryPrice)} USDT"),
            HistoryField("平仓价", "${DecimalFormatters.formatCurrency(input.exitPrice)} USDT"),
            HistoryField("开仓费率", "${input.openFeeRatePercent.stripTrailingZeros().toPlainString()}%"),
            HistoryField("平仓费率", "${input.closeFeeRatePercent.stripTrailingZeros().toPlainString()}%"),
            HistoryField("维持保证金率", "${input.maintenanceMarginRatePercent.stripTrailingZeros().toPlainString()}%"),
            HistoryField("总资金", input.totalFunds?.let { "${DecimalFormatters.formatCurrency(it)} USDT" } ?: "未填写")
        )),
        HistorySection("保存时结果", listOf(
            HistoryField("仓位价值", "${DecimalFormatters.formatCurrency(result.positionValue)} USDT"),
            HistoryField("总手续费约", "${DecimalFormatters.formatCurrency(result.totalFee)} USDT"),
            HistoryField("净盈亏", "${DecimalFormatters.formatPositiveNegative(result.netPnl)} USDT"),
            HistoryField("ROI", DecimalFormatters.formatPercentage(result.roiPercent)),
            HistoryField("估算强平价", result.liquidationPrice?.let { "${DecimalFormatters.formatCurrency(it)} USDT" } ?: "无法可靠估算"),
            HistoryField("距离强平", DecimalFormatters.formatPercentage(result.distanceToLiquidationPercent))
        ))
    )
)
private fun createAveragingHistorySnapshot(
    input: AveragingDecisionInput,
    result: AveragingDecisionResult,
    symbol: String
): HistoryRecord = HistoryRecord(
    id = "history_${System.currentTimeMillis()}",
    category = HistoryCategory.AveragingSimulation,
    title = "$symbol 补仓模拟",
    summary = "${DecimalFormatters.formatPositiveNegative(result.pnlChange)} USDT",
    roiSummary = null,
    savedAt = System.currentTimeMillis(),
    sections = listOf(
        HistorySection("补仓参数", listOf(
            HistoryField("方向", input.side.label()),
            HistoryField("当前均价", "${DecimalFormatters.formatCurrency(input.currentEntryPrice)} USDT"),
            HistoryField("当前数量", "${DecimalFormatters.formatQuantity(input.currentQuantity)} $symbol"),
            HistoryField("当前保证金", "${DecimalFormatters.formatCurrency(input.currentMargin)} USDT"),
            HistoryField("当前杠杆", "${DecimalFormatters.formatQuantity(input.currentLeverage)}x"),
            HistoryField("补仓价格", "${DecimalFormatters.formatCurrency(input.addEntryPrice)} USDT"),
            HistoryField("补仓金额", "${DecimalFormatters.formatCurrency(result.addAmount)} USDT"),
            HistoryField("补仓数量", "${DecimalFormatters.formatQuantity(result.quantityIncrease)} $symbol"),
            HistoryField("目标平仓价", "${DecimalFormatters.formatCurrency(input.targetExitPrice)} USDT")
        )),
        HistorySection("保存时结果", listOf(
            HistoryField("补仓后均价", "${DecimalFormatters.formatCurrency(result.newAveragePrice)} USDT"),
            HistoryField("补仓后总仓位", "${DecimalFormatters.formatQuantity(result.newQuantity)} $symbol"),
            HistoryField("补仓前收益", "${DecimalFormatters.formatPositiveNegative(result.pnlWithoutAdding)} USDT"),
            HistoryField("补仓后收益", "${DecimalFormatters.formatPositiveNegative(result.pnlAfterAdding)} USDT"),
            HistoryField("收益变化", "${DecimalFormatters.formatPositiveNegative(result.pnlChange)} USDT"),
            HistoryField("风险变化", "未纳入本次模拟")
        ))
    )
)

private fun PositionSide.label(): String = if (this == PositionSide.Long) "做多" else "做空"

private fun MarginMode.label(): String = if (this == MarginMode.Cross) "全仓" else "逐仓"
