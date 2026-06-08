package com.personal.futurescalculator.ui

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.personal.futurescalculator.model.CalculationInput
import com.personal.futurescalculator.model.HomeModule
import com.personal.futurescalculator.model.ComparisonItem
import com.personal.futurescalculator.model.SettlementMode
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
import com.personal.futurescalculator.ui.dialogs.MissingParametersDialog
import com.personal.futurescalculator.ui.dialogs.OperationRequirementDialog
import com.personal.futurescalculator.ui.fees.FeeSettingsDialog
import com.personal.futurescalculator.ui.history.HistoryScreen
import com.personal.futurescalculator.ui.history.createAveragingHistorySnapshot
import com.personal.futurescalculator.ui.history.createProfitHistorySnapshot
import com.personal.futurescalculator.ui.home.HomeBottomActions
import com.personal.futurescalculator.ui.home.SupportAuthorCard
import com.personal.futurescalculator.ui.position.PositionInputSection
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
    var fullCrossTotalFundsEnabled by rememberSaveable { mutableStateOf(uiState.input.totalFunds != null) }
    val hasTargetStopPlan = uiState.input.takeProfitPrice != null ||
        uiState.input.stopLossPrice != null ||
        uiState.input.targetProfitAmount != null ||
        uiState.input.targetRoiPercent != null ||
        uiState.input.maxLossAmount != null ||
        uiState.input.maxLossRoiPercent != null
    var targetStopEnabled by rememberSaveable { mutableStateOf(hasTargetStopPlan) }
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
            targetStopEnabled = true
        }
    }
    LaunchedEffect(uiState.input.totalFunds) {
        if (uiState.input.totalFunds != null) {
            fullCrossTotalFundsEnabled = true
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
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Text(
                    text = "仓位助手",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "我负责计算，你负责决策",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
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

            HomeModule.defaultOrder.forEach { module ->
            when (module) {
            HomeModule.Position -> {
            PositionInputSection(
                input = uiState.input,
                settlementMode = uiState.settlementMode,
                symbol = selectedCoin?.symbol ?: "币",
                fullCrossTotalFundsEnabled = fullCrossTotalFundsEnabled,
                targetStopEnabled = targetStopEnabled,
                onFullCrossTotalFundsEnabledChange = { fullCrossTotalFundsEnabled = it },
                onTargetStopEnabledChange = { targetStopEnabled = it },
                onInputChange = viewModel::updateInput,
                onAmountInputChange = viewModel::updateAmountInput,
                onShowFeeSettings = { showFeeSettings = true },
                onSubmit = submitMainResult
            )
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
                    settlementMode = uiState.settlementMode,
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
                    text = "多个开仓方案横向比较",
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
            HomeBottomActions(
                onHistoryClick = { showHistory = true },
                onResetClick = {
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
