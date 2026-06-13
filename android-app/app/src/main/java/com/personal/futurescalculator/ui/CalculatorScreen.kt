package com.personal.futurescalculator.ui

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.personal.futurescalculator.model.CalculationInput
import com.personal.futurescalculator.model.ComparisonItem
import com.personal.futurescalculator.model.CopyFormat
import com.personal.futurescalculator.model.PositionSide
import com.personal.futurescalculator.model.SettlementMode
import com.personal.futurescalculator.domain.FuturesCalculator
import com.personal.futurescalculator.ui.theme.LossRed
import com.personal.futurescalculator.ui.theme.LocalProfitLossPalette
import com.personal.futurescalculator.ui.theme.ProfitGreen
import com.personal.futurescalculator.ui.theme.ProfitLossPalette
import com.personal.futurescalculator.ui.averaging.AveragingDecisionEntryCard
import com.personal.futurescalculator.ui.averaging.AveragingDecisionSection
import com.personal.futurescalculator.ui.averaging.AveragingResultDialog
import com.personal.futurescalculator.ui.averaging.ExistingScheme
import com.personal.futurescalculator.ui.averaging.averagingMissingFields
import com.personal.futurescalculator.ui.coin.CoinSelectorDialog
import com.personal.futurescalculator.ui.comparison.ComparisonResultDialog
import com.personal.futurescalculator.ui.comparison.ComparisonSchemeEditorDialog
import com.personal.futurescalculator.ui.comparison.ComparisonSchemeListCard
import com.personal.futurescalculator.ui.comparison.MAIN_SCHEME_ID
import com.personal.futurescalculator.ui.comparison.PlanSelectionDialog
import com.personal.futurescalculator.ui.comparison.buildComparisonSchemes
import com.personal.futurescalculator.ui.comparison.createComparisonHistorySnapshot
import com.personal.futurescalculator.ui.comparison.selectedComparisonValidationMessage
import com.personal.futurescalculator.ui.dialogs.MissingParametersDialog
import com.personal.futurescalculator.ui.dialogs.OperationRequirementDialog
import com.personal.futurescalculator.ui.fees.FeeSettingsDialog
import com.personal.futurescalculator.ui.history.HistoryScreen
import com.personal.futurescalculator.ui.history.createAveragingHistorySnapshot
import com.personal.futurescalculator.ui.history.createCoinMarginedHistorySnapshot
import com.personal.futurescalculator.ui.history.createProfitHistorySnapshot
import com.personal.futurescalculator.ui.home.HomeBottomActions
import com.personal.futurescalculator.ui.home.SupportAuthorCard
import com.personal.futurescalculator.ui.position.PositionInputSection
import com.personal.futurescalculator.ui.results.CoinMarginedResultDialog
import com.personal.futurescalculator.ui.results.CompactExpandableResultCard
import com.personal.futurescalculator.ui.results.EmptyResult
import com.personal.futurescalculator.ui.results.MainResultDialog
import com.personal.futurescalculator.ui.results.MetricTile
import com.personal.futurescalculator.ui.results.pnlColor
import com.personal.futurescalculator.ui.results.pnlText
import com.personal.futurescalculator.ui.settings.CoinMarginedModeDialog
import com.personal.futurescalculator.ui.settings.PnlDisplayMode
import com.personal.futurescalculator.ui.settings.SettingsScreen
import com.personal.futurescalculator.ui.staticpages.DonationScreen
import com.personal.futurescalculator.util.ClipboardFormatter
import com.personal.futurescalculator.util.DecimalFormatters
import com.personal.futurescalculator.viewmodel.CalculatorViewModel
import java.math.BigDecimal
import kotlinx.coroutines.delay
import kotlin.math.roundToInt

private const val RESULT_CARD_MAIN = "main"
private const val RESULT_CARD_COIN = "coin"

@Composable
fun CalculatorScreen(
    viewModel: CalculatorViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()
    val futuresCalculator = remember { FuturesCalculator() }
    var showFeeSettings by rememberSaveable { mutableStateOf(false) }
    var showCoinSelector by rememberSaveable { mutableStateOf(false) }
    var showSettings by remember { mutableStateOf(false) }
    var showDonation by remember { mutableStateOf(false) }
    var showHistory by remember { mutableStateOf(false) }
    var showHistoryPlans by rememberSaveable { mutableStateOf(false) }
    var showMainResultDialog by rememberSaveable { mutableStateOf(false) }
    var showCoinMarginedResultDialog by rememberSaveable { mutableStateOf(false) }
    var showAveragingResultDialog by rememberSaveable { mutableStateOf(false) }
    var showComparisonResultDialog by rememberSaveable { mutableStateOf(false) }
    var revealedResultCards by rememberSaveable { mutableStateOf(emptySet<String>()) }
    var showPlanSelectionDialog by rememberSaveable { mutableStateOf(false) }
    var showClearParametersConfirmation by rememberSaveable { mutableStateOf(false) }
    var showCopyFormatSelection by rememberSaveable { mutableStateOf(false) }
    var showSavePlanDialog by rememberSaveable { mutableStateOf(false) }
    var savePlanName by rememberSaveable { mutableStateOf("") }
    var comparisonEditorItem by remember { mutableStateOf<ComparisonItem?>(null) }
    var selectedComparisonIds by rememberSaveable { mutableStateOf(setOf<String>(MAIN_SCHEME_ID)) }
    var comparisonBaselineId by rememberSaveable { mutableStateOf(MAIN_SCHEME_ID) }
    var averagingValidationMessage by rememberSaveable { mutableStateOf<String?>(null) }
    var operationRequirementMessage by rememberSaveable { mutableStateOf<String?>(null) }
    var averagingSymbolOverride by rememberSaveable { mutableStateOf<String?>(null) }
    var pendingScrollToAveraging by rememberSaveable { mutableStateOf(false) }
    var averagingSectionOffset by rememberSaveable { mutableStateOf(0) }
    var pnlDisplayMode by rememberSaveable { mutableStateOf(PnlDisplayMode.ProfitGreen) }
    var feedbackText by rememberSaveable { mutableStateOf("") }
    var comparisonSectionExpanded by rememberSaveable { mutableStateOf(false) }
    var targetPriceExpanded by rememberSaveable { mutableStateOf(false) }
    val hasTargetStopPlan = uiState.input.takeProfitPrice != null ||
        uiState.input.stopLossPrice != null
    var targetStopEnabled by rememberSaveable { mutableStateOf(hasTargetStopPlan) }
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
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
    val averagingSchemes = buildList {
        uiState.savedPlans.forEach { plan ->
            futuresCalculator.calculate(plan.input)?.let { result ->
                add(
                    ExistingScheme(
                        plan.name,
                        uiState.coins.firstOrNull { coin -> coin.id == plan.coinId }?.symbol ?: "币",
                        plan.input,
                        result
                    )
                )
            }
        }
    }
    val comparisonSchemes = buildComparisonSchemes(
        mainInput = uiState.input,
        mainResult = uiState.result,
        mainCoinMarginedResult = uiState.coinMarginedResult,
        mainSymbol = selectedCoin?.symbol ?: "币",
        mainSettlementMode = uiState.settlementMode,
        mainCoinMarginedCalculationMode = uiState.coinMarginedCalculationMode,
        items = uiState.comparisonItems,
        results = uiState.comparisonResults,
        coins = uiState.coins
    )
    val mainComparisonComplete = comparisonSchemes.firstOrNull { it.id == MAIN_SCHEME_ID }?.let { scheme ->
        when (scheme.settlementMode) {
            SettlementMode.UsdtMargined -> scheme.result?.netPnl != null
            SettlementMode.CoinMargined -> scheme.coinMarginedResult?.estimatedValueUsdt != null
        }
    } == true
    val completeComparisonIds = comparisonSchemes.filter { scheme ->
        when (scheme.settlementMode) {
            SettlementMode.UsdtMargined -> scheme.result?.netPnl != null
            SettlementMode.CoinMargined -> scheme.coinMarginedResult?.estimatedValueUsdt != null
        }
    }.map { it.id }.toSet()
    val selectedCompleteComparisonCount = selectedComparisonIds.count { it in completeComparisonIds }
    val canOpenComparisonResult = selectedCompleteComparisonCount >= 2
    val mainInputForPlanMatch = uiState.input.copy(
        targetProfitAmount = null,
        targetRoiPercent = null,
        maxLossAmount = null,
        maxLossRoiPercent = null,
        totalFunds = null,
        estimateLiquidation = false,
        calculateMaxOpen = false
    )
    val availableComparisonPlans = uiState.savedPlans.filterNot { plan ->
        val planInputForMainMatch = plan.input.copy(
            targetProfitAmount = null,
            targetRoiPercent = null,
            maxLossAmount = null,
            maxLossRoiPercent = null,
            totalFunds = null,
            estimateLiquidation = false,
            calculateMaxOpen = false
        )
        val matchesMain = plan.coinId == uiState.selectedCoinId &&
            plan.settlementMode == uiState.settlementMode &&
            plan.coinMarginedCalculationMode == uiState.coinMarginedCalculationMode &&
            planInputForMainMatch == mainInputForPlanMatch
        val alreadyAdded = uiState.comparisonItems.any { item ->
            item.coinId == plan.coinId &&
                item.settlementMode == plan.settlementMode &&
                item.coinMarginedCalculationMode == plan.coinMarginedCalculationMode &&
                item.input == plan.input &&
                item.lastEditedAmountField == plan.lastEditedAmountField
        }
        matchesMain || alreadyAdded
    }
    val hideKeyboardAndClearFocus: () -> Unit = {
        focusManager.clearFocus(force = true)
        keyboardController?.hide()
    }
    val copyCurrentResult: (CopyFormat) -> Unit = { format ->
        clipboardManager.setText(
            AnnotatedString(
                ClipboardFormatter.formatSinglePerformance(
                    name = "当前方案",
                    symbol = selectedCoin?.symbol ?: "币",
                    input = uiState.input,
                    result = uiState.result,
                    format = format
                )
            )
        )
        showCopyFormatSelection = false
        Toast.makeText(context, "计算结果已复制", Toast.LENGTH_SHORT).show()
    }
    val saveCurrentProfitHistory: () -> Unit = {
        val result = uiState.result
        if (uiState.settlementMode == SettlementMode.UsdtMargined && result?.netPnl != null) {
            val saved = viewModel.saveHistoryRecord(
                createProfitHistorySnapshot(
                    input = uiState.input,
                    result = result,
                    symbol = selectedCoin?.symbol ?: "币",
                    settlementMode = uiState.settlementMode
                )
            )
            if (saved) {
                Toast.makeText(context, "已加入记录", Toast.LENGTH_SHORT).show()
            }
        }
    }
    val saveCurrentAveragingHistory: () -> Unit = {
        val result = uiState.averagingDecisionResult
        if (result != null) {
            val saved = viewModel.saveHistoryRecord(
                createAveragingHistorySnapshot(
                    uiState.averagingDecisionInput,
                    result,
                    averagingSymbolOverride ?: selectedCoin?.symbol ?: "币"
                )
            )
            if (saved) {
                Toast.makeText(context, "已加入记录", Toast.LENGTH_SHORT).show()
            }
        }
    }
    val saveCurrentCoinMarginedHistory: () -> Unit = {
        val result = uiState.coinMarginedResult
        if (uiState.settlementMode == SettlementMode.CoinMargined && result != null) {
            val saved = viewModel.saveHistoryRecord(
                createCoinMarginedHistorySnapshot(
                    input = uiState.input,
                    result = result,
                    symbol = selectedCoin?.symbol ?: "币"
                )
            )
            if (saved) {
                Toast.makeText(context, "已加入记录", Toast.LENGTH_SHORT).show()
            }
        }
    }
    val submitMainResult: () -> Unit = {
        hideKeyboardAndClearFocus()
        when {
            uiState.settlementMode == SettlementMode.UsdtMargined && uiState.result != null -> {
                saveCurrentProfitHistory()
                showMainResultDialog = true
            }
            uiState.settlementMode == SettlementMode.CoinMargined && uiState.coinMarginedResult != null -> {
                saveCurrentCoinMarginedHistory()
                showCoinMarginedResultDialog = true
            }
        }
    }

    LaunchedEffect(uiState.comparisonItems.map { it.id }, completeComparisonIds) {
        val validIds = uiState.comparisonItems.map { it.id }.toSet() + MAIN_SCHEME_ID
        selectedComparisonIds = selectedComparisonIds.intersect(validIds).intersect(completeComparisonIds)
    }
    LaunchedEffect(mainComparisonComplete) {
        if (mainComparisonComplete && selectedComparisonIds.isEmpty()) {
            selectedComparisonIds = setOf(MAIN_SCHEME_ID)
            comparisonBaselineId = MAIN_SCHEME_ID
        }
    }
    LaunchedEffect(selectedComparisonIds, completeComparisonIds) {
        if (comparisonBaselineId !in selectedComparisonIds || comparisonBaselineId !in completeComparisonIds) {
            comparisonBaselineId = selectedComparisonIds.firstOrNull { it in completeComparisonIds } ?: MAIN_SCHEME_ID
        }
    }
    LaunchedEffect(hasTargetStopPlan) {
        if (hasTargetStopPlan) {
            targetStopEnabled = true
        }
    }
    LaunchedEffect(uiState.averagingExpanded, pendingScrollToAveraging) {
        if (uiState.averagingExpanded && pendingScrollToAveraging) {
            delay(100)
            scrollState.animateScrollTo(averagingSectionOffset.coerceIn(0, scrollState.maxValue))
            pendingScrollToAveraging = false
        }
    }
    CompositionLocalProvider(LocalProfitLossPalette provides profitLossPalette) {
    if (showSettings) {
        SettingsScreen(
            pnlDisplayMode = pnlDisplayMode,
            onPnlDisplayModeChange = { pnlDisplayMode = it },
            feedbackText = feedbackText,
            onFeedbackChange = { feedbackText = it },
            priceUpdatedAt = uiState.priceUpdatedAt,
            themeMode = uiState.themeMode,
            onThemeModeChange = viewModel::updateThemeMode,
            coinMarginedCalculationMode = uiState.coinMarginedCalculationMode,
            onCoinMarginedCalculationModeChange = viewModel::updateCoinMarginedCalculationMode,
            copyFormat = uiState.copyFormat,
            onCopyFormatChange = viewModel::updateCopyFormat,
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
            plans = uiState.savedPlans,
            coins = uiState.coins,
            startOnPlans = showHistoryPlans,
            onToggleFavorite = viewModel::toggleHistoryFavorite,
            onDelete = viewModel::deleteHistoryRecords,
            onClearCategory = viewModel::clearHistoryCategory,
            onOpenPlan = {
                viewModel.openSavedPlan(it)
                showHistory = false
            },
            onAddPlanToComparison = viewModel::addSavedPlanToComparison,
            onSendPlanToAveraging = { plan ->
                val result = futuresCalculator.calculate(plan.input)
                if (result != null) {
                    viewModel.updateAveragingDecisionInput(
                        uiState.averagingDecisionInput.copy(
                            side = plan.input.side,
                            currentEntryPrice = plan.input.entryPrice,
                            currentQuantity = result.quantity,
                            currentMargin = result.requiredMargin,
                            currentLeverage = plan.input.leverage
                        )
                    )
                    averagingSymbolOverride = uiState.coins.firstOrNull { it.id == plan.coinId }?.symbol ?: "币"
                    viewModel.setAveragingExpanded(true)
                    pendingScrollToAveraging = true
                } else {
                    operationRequirementMessage = "该方案参数不完整，暂时无法带入补仓助手。"
                }
                showHistory = false
            },
            onDeletePlan = { planId ->
                val clearsOpenedPlan = uiState.openedPlanId == planId
                viewModel.deleteSavedPlan(planId)
                if (clearsOpenedPlan) {
                    targetStopEnabled = false
                    targetPriceExpanded = false
                    selectedComparisonIds = setOf(MAIN_SCHEME_ID)
                    comparisonBaselineId = MAIN_SCHEME_ID
                }
            },
            onClearPlans = {
                viewModel.clearSavedPlans()
                averagingSymbolOverride = null
                targetStopEnabled = false
                targetPriceExpanded = false
                selectedComparisonIds = setOf(MAIN_SCHEME_ID)
                comparisonBaselineId = MAIN_SCHEME_ID
            },
            onRenamePlan = viewModel::renameSavedPlan,
            onUpdatePlanNote = viewModel::updateSavedPlanNote,
            onDuplicatePlan = viewModel::duplicateSavedPlan,
            onSaveHistoryAsPlan = viewModel::saveHistoryAsPlan,
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
            onShowFeeSettings = { showFeeSettings = true },
            onCopyResult = {
                if (uiState.copyFormat == CopyFormat.Ask) {
                    showCopyFormatSelection = true
                } else {
                    copyCurrentResult(uiState.copyFormat)
                }
            },
            onSimulateAveraging = {
                viewModel.updateAveragingDecisionInput(
                    uiState.averagingDecisionInput.copy(
                        side = uiState.input.side,
                        currentEntryPrice = uiState.input.entryPrice,
                        currentQuantity = uiState.result!!.quantity,
                        currentMargin = uiState.result!!.requiredMargin,
                        currentLeverage = uiState.input.leverage
                    )
                )
                averagingSymbolOverride = selectedCoin?.symbol ?: "币"
                viewModel.setAveragingExpanded(true)
                pendingScrollToAveraging = true
                showMainResultDialog = false
            },
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
            onCopyResult = {
                clipboardManager.setText(
                    AnnotatedString(
                        ClipboardFormatter.formatAveragingResult(
                            input = uiState.averagingDecisionInput,
                            result = uiState.averagingDecisionResult!!
                        )
                    )
                )
                Toast.makeText(context, "补仓结果已复制", Toast.LENGTH_SHORT).show()
            },
            onDismiss = {
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
            baselineId = comparisonBaselineId,
            onBaselineChange = { comparisonBaselineId = it },
            onCopySummary = {
                clipboardManager.setText(
                    AnnotatedString(
                        ClipboardFormatter.formatComparisonSummary(
                            comparisonSchemes.filter { it.id in selectedComparisonIds },
                            comparisonBaselineId
                        )
                    )
                )
                Toast.makeText(context, "对比摘要已复制", Toast.LENGTH_SHORT).show()
            },
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
    if (showClearParametersConfirmation) {
        AlertDialog(
            onDismissRequest = { showClearParametersConfirmation = false },
            title = { Text("清空当前参数？") },
            text = { Text("仅清空首页当前填写内容，不会删除历史记录或方案库。") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.reset()
                        targetStopEnabled = false
                        targetPriceExpanded = false
                        selectedComparisonIds = setOf(MAIN_SCHEME_ID)
                        comparisonBaselineId = MAIN_SCHEME_ID
                        showClearParametersConfirmation = false
                    }
                ) {
                    Text("清空", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearParametersConfirmation = false }) {
                    Text("取消")
                }
            }
        )
    }
    if (showCopyFormatSelection) {
        AlertDialog(
            onDismissRequest = { showCopyFormatSelection = false },
            title = { Text("选择复制格式") },
            text = { Text("复制当前计算结果。") },
            confirmButton = {
                TextButton(onClick = { copyCurrentResult(CopyFormat.Detail) }) {
                    Text("详细版", fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                TextButton(onClick = { copyCurrentResult(CopyFormat.Summary) }) {
                    Text("简洁版", fontWeight = FontWeight.SemiBold)
                }
            }
        )
    }
    if (showSavePlanDialog) {
        AlertDialog(
            onDismissRequest = { showSavePlanDialog = false },
            title = { Text("存为方案") },
            text = {
                OutlinedTextField(
                    value = savePlanName,
                    onValueChange = { savePlanName = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("方案名称") },
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val saved = viewModel.saveCurrentPlan(savePlanName)
                        showSavePlanDialog = false
                        Toast.makeText(
                            context,
                            if (saved) "方案已保存" else "相同方案已存在，无需重复保存",
                            Toast.LENGTH_SHORT
                        ).show()
                    },
                    enabled = savePlanName.isNotBlank()
                ) {
                    Text("保存", fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showSavePlanDialog = false }) {
                    Text("取消")
                }
            }
        )
    }
    if (showPlanSelectionDialog) {
        PlanSelectionDialog(
            plans = availableComparisonPlans,
            coins = uiState.coins,
            coinMarginedCalculationMode = uiState.coinMarginedCalculationMode,
            onSelect = { item ->
                viewModel.saveComparisonItem(item)
                selectedComparisonIds = selectedComparisonIds + item.id
                if (selectedComparisonIds.intersect(completeComparisonIds).isEmpty()) {
                    comparisonBaselineId = item.id
                }
                showPlanSelectionDialog = false
            },
            onDismiss = { showPlanSelectionDialog = false }
        )
    }
    comparisonEditorItem?.let { item ->
        ComparisonSchemeEditorDialog(
            initialItem = item,
            coins = uiState.coins,
            showDelete = uiState.comparisonItems.any { it.id == item.id },
            onSave = {
                hideKeyboardAndClearFocus()
                viewModel.saveComparisonItem(it)
                selectedComparisonIds = selectedComparisonIds + it.id
                if (selectedComparisonIds.intersect(completeComparisonIds).isEmpty()) {
                    comparisonBaselineId = it.id
                }
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
            PositionInputSection(
                input = uiState.input,
                coin = selectedCoin,
                settlementMode = uiState.settlementMode,
                coinMarginedCalculationMode = uiState.coinMarginedCalculationMode,
                amountInputMode = uiState.lastEditedAmountField,
                targetStopEnabled = targetStopEnabled,
                targetPriceExpanded = targetPriceExpanded,
                onCoinClick = { showCoinSelector = true },
                onClearClick = { showClearParametersConfirmation = true },
                onSettlementModeChange = viewModel::onContractModeChanged,
                onTargetStopEnabledChange = { targetStopEnabled = it },
                onTargetPriceExpandedChange = { targetPriceExpanded = it },
                targetPriceContent = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            NumberInput(
                                value = uiState.input.targetProfitAmount,
                                onValueChange = {
                                    viewModel.updateInput(
                                        uiState.input.copy(
                                            targetProfitAmount = it,
                                            targetRoiPercent = null
                                        )
                                    )
                                },
                                label = "目标盈利 USDT",
                                requirePositive = true,
                                maxDecimalPlaces = 2
                            )
                            uiState.result?.targetProfitPriceByAmount?.let { price ->
                                MetricTile(
                                    label = "对应价格",
                                    value = "${DecimalFormatters.formatPrice(price)} USDT"
                                )
                            }
                        }
                        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            NumberInput(
                                value = uiState.input.maxLossAmount,
                                onValueChange = {
                                    viewModel.updateInput(
                                        uiState.input.copy(
                                            maxLossAmount = it,
                                            maxLossRoiPercent = null
                                        )
                                    )
                                },
                                label = "最大亏损 USDT",
                                requirePositive = true,
                                maxDecimalPlaces = 2
                            )
                            uiState.result?.stopLossPriceByAmount?.let { price ->
                                MetricTile(
                                    label = "对应价格",
                                    value = "${DecimalFormatters.formatPrice(price)} USDT"
                                )
                            }
                        }
                    }
                },
                maxOpenContent = {
                    uiState.maxOpenResult?.let { maxOpen ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            MetricTile(
                                label = "可开仓位",
                                value = "${DecimalFormatters.formatAmount(maxOpen.positionValue)} USDT",
                                modifier = Modifier.weight(1f)
                            )
                            MetricTile(
                                label = "可开数量",
                                value = "${DecimalFormatters.formatQuantity(maxOpen.quantity)} ${selectedCoin?.symbol ?: "币"}",
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                },
                onInputChange = viewModel::updateInput,
                onAmountInputChange = viewModel::updateAmountInput,
                onSubmit = submitMainResult
            )

            if (uiState.settlementMode == SettlementMode.UsdtMargined && hasPnlResult) {
                val netPnl = uiState.result!!.netPnl
                CompactExpandableResultCard(
                    primaryLabel = if (netPnl != null && netPnl < BigDecimal.ZERO) "净亏损" else "净盈利",
                    primaryValue = "${pnlText(netPnl, DecimalFormatters.formatSignedAmount(netPnl))} USDT",
                    primaryColor = pnlColor(netPnl),
                    secondaryLabel = "保证金收益率",
                    secondaryValue = DecimalFormatters.formatSignedPercentage(uiState.result!!.roiPercent),
                    secondaryColor = pnlColor(netPnl),
                    actionText = "存为方案",
                    onActionClick = {
                        val sideLabel = if (uiState.input.side == PositionSide.Long) "做多" else "做空"
                        savePlanName = "${selectedCoin?.symbol ?: "币"} $sideLabel ${DecimalFormatters.formatLeverage(uiState.input.leverage)}x"
                        showSavePlanDialog = true
                    },
                    onClick = {
                        hideKeyboardAndClearFocus()
                        saveCurrentProfitHistory()
                        showMainResultDialog = true
                    }
                )
            } else if (uiState.settlementMode == SettlementMode.UsdtMargined && uiState.result != null) {
                CompactExpandableResultCard(
                    primaryLabel = "仓位价值",
                    primaryValue = "${DecimalFormatters.formatAmount(uiState.result!!.positionValue)} USDT",
                    primaryColor = MaterialTheme.colorScheme.primary,
                    secondaryLabel = "仓位币数量",
                    secondaryValue = "${DecimalFormatters.formatQuantity(uiState.result!!.quantity)} ${selectedCoin?.symbol ?: "币"}",
                    secondaryColor = MaterialTheme.colorScheme.onSurface,
                    supportingText = "填写平仓价后查看净盈亏和保证金收益率",
                    onClick = {
                        hideKeyboardAndClearFocus()
                        showMainResultDialog = true
                    }
                )
            } else if (uiState.settlementMode == SettlementMode.CoinMargined && uiState.coinMarginedResult != null) {
                CompactExpandableResultCard(
                    primaryLabel = "币本位盈亏",
                    primaryValue = "${DecimalFormatters.formatSignedCoinAmount(uiState.coinMarginedResult!!.pnlCoin)} ${selectedCoin?.symbol ?: "币"}",
                    primaryColor = pnlColor(uiState.coinMarginedResult!!.pnlCoin),
                    secondaryLabel = "折算价值",
                    secondaryValue = "${DecimalFormatters.formatSignedAmount(uiState.coinMarginedResult!!.estimatedValueUsdt)} USDT",
                    secondaryColor = pnlColor(uiState.coinMarginedResult!!.pnlCoin),
                    onClick = {
                        hideKeyboardAndClearFocus()
                        saveCurrentCoinMarginedHistory()
                        showCoinMarginedResultDialog = true
                    }
                )
            } else if (uiState.settlementMode == SettlementMode.UsdtMargined) {
                EmptyResult(input = uiState.input)
            }

            SectionPanel(
                title = "开仓方案对比",
                trailing = {
                    if (comparisonSectionExpanded) {
                        TextButton(
                            onClick = { comparisonSectionExpanded = false },
                            colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Text("收起", fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            ) {
                if (!comparisonSectionExpanded) {
                    Text(
                        text = if (uiState.comparisonItems.isEmpty()) {
                            "比较不同价格、杠杆和仓位下的收益差异。"
                        } else {
                            "已有 ${completeComparisonIds.size} 个可用方案，展开后继续编辑或查看对比。"
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    SoftOutlinedButton(
                        onClick = { comparisonSectionExpanded = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.small
                    ) {
                        Text(if (uiState.comparisonItems.isEmpty()) "开始对比" else "继续对比")
                    }
                } else {
                    if (!mainComparisonComplete) {
                        Text(
                            text = "当前参数不完整，不参与对比；仍可直接创建或从方案库选择方案。",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    val visibleComparisonSchemes = comparisonSchemes.filterNot { it.isMain && !mainComparisonComplete }
                    if (visibleComparisonSchemes.isNotEmpty()) {
                        Text(
                            text = "选择最多 3 个完整方案，进入结果后选择对比基准",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    visibleComparisonSchemes.forEach { scheme ->
                        val schemeComplete = when (scheme.settlementMode) {
                            SettlementMode.UsdtMargined -> scheme.result?.netPnl != null
                            SettlementMode.CoinMargined -> scheme.coinMarginedResult?.estimatedValueUsdt != null
                        }
                        val selectionEnabled = schemeComplete
                        ComparisonSchemeListCard(
                            scheme = scheme,
                            selected = scheme.id in selectedComparisonIds && selectionEnabled,
                            enabled = selectionEnabled,
                            onSelectedChange = { selected ->
                                selectedComparisonIds = if (selected && selectedComparisonIds.size >= 3) {
                                    operationRequirementMessage = "一次最多比较3个方案。"
                                    selectedComparisonIds
                                } else if (selected) {
                                    if (selectedComparisonIds.isEmpty()) comparisonBaselineId = scheme.id
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
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
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
                            modifier = Modifier.weight(1f),
                            shape = MaterialTheme.shapes.small
                        ) {
                            Text("手动创建")
                        }
                        SoftOutlinedButton(
                            onClick = { showPlanSelectionDialog = true },
                            modifier = Modifier.weight(1f),
                            enabled = uiState.savedPlans.isNotEmpty(),
                            shape = MaterialTheme.shapes.small
                        ) {
                            Text("从方案库选择")
                        }
                    }
                    if (canOpenComparisonResult) {
                        Button(
                            onClick = {
                                val selectedSchemes = comparisonSchemes.filter { it.id in selectedComparisonIds }
                                val message = selectedComparisonValidationMessage(selectedSchemes)
                                if (message != null) {
                                    operationRequirementMessage = message
                                } else {
                                    hideKeyboardAndClearFocus()
                                    val saved = viewModel.saveHistoryRecord(
                                        createComparisonHistorySnapshot(selectedSchemes)
                                    )
                                    if (saved) {
                                        Toast.makeText(context, "已加入记录", Toast.LENGTH_SHORT).show()
                                    }
                                    showComparisonResultDialog = true
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = MaterialTheme.shapes.small
                        ) {
                            Text("展开方案对比结果")
                        }
                    } else {
                        Text(
                            text = "添加并选择至少 2 个完整方案后可查看结果",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            if (uiState.averagingExpanded) {
                AveragingDecisionSection(
                    input = uiState.averagingDecisionInput,
                    result = uiState.averagingDecisionResult,
                    schemes = averagingSchemes,
                    symbol = averagingSymbolOverride ?: selectedCoin?.symbol ?: "币",
                    modifier = Modifier.onGloballyPositioned {
                        averagingSectionOffset = it.positionInParent().y.roundToInt()
                    },
                    onInputChange = viewModel::updateAveragingDecisionInput,
                    onCollapse = { viewModel.setAveragingExpanded(false) },
                    onSchemeFilled = { averagingSymbolOverride = it.symbol },
                    onManualPositionStarted = { averagingSymbolOverride = null },
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
                            saveCurrentAveragingHistory()
                            showAveragingResultDialog = true
                        }
                    }
                )
            } else {
                AveragingDecisionEntryCard(
                    input = uiState.averagingDecisionInput,
                    result = uiState.averagingDecisionResult,
                    symbol = averagingSymbolOverride ?: selectedCoin?.symbol ?: "币",
                    modifier = Modifier.onGloballyPositioned {
                        averagingSectionOffset = it.positionInParent().y.roundToInt()
                    },
                    onClick = {
                        viewModel.setAveragingExpanded(true)
                    }
                )
            }

            SupportAuthorCard(onClick = { showDonation = true })
            }
            HomeBottomActions(
                onHistoryClick = {
                    showHistoryPlans = false
                    showHistory = true
                },
                onPlanClick = {
                    showHistoryPlans = true
                    showHistory = true
                },
                onSettingsClick = { showSettings = true }
            )
        }
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
