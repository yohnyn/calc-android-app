package com.personal.futurescalculator.ui

import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.compose.ui.window.Dialog
import com.personal.futurescalculator.model.AmountField
import com.personal.futurescalculator.model.AveragingDecisionInput
import com.personal.futurescalculator.model.AveragingDecisionResult
import com.personal.futurescalculator.model.CalculationInput
import com.personal.futurescalculator.model.CalculationResult
import com.personal.futurescalculator.model.CoinAsset
import com.personal.futurescalculator.model.CoinMarginedCalculationMode
import com.personal.futurescalculator.model.CoinMarginedResult
import com.personal.futurescalculator.model.HomeModule
import com.personal.futurescalculator.model.HistoryCategory
import com.personal.futurescalculator.model.HistoryField
import com.personal.futurescalculator.model.HistoryRecord
import com.personal.futurescalculator.model.HistorySection
import com.personal.futurescalculator.model.ComparisonItem
import com.personal.futurescalculator.model.ComparisonResult
import com.personal.futurescalculator.model.MarginMode
import com.personal.futurescalculator.model.PositionSide
import com.personal.futurescalculator.model.SettlementMode
import com.personal.futurescalculator.model.ThemeMode
import com.personal.futurescalculator.ui.theme.LossRed
import com.personal.futurescalculator.ui.theme.LocalProfitLossPalette
import com.personal.futurescalculator.ui.theme.ProfitGreen
import com.personal.futurescalculator.ui.theme.ProfitLossPalette
import com.personal.futurescalculator.ui.theme.WarningAmber
import com.personal.futurescalculator.util.ClipboardFormatter
import com.personal.futurescalculator.util.DecimalFormatters
import com.personal.futurescalculator.data.CoinRepository
import com.personal.futurescalculator.viewmodel.CalculatorViewModel
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.roundToInt

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
            uiState.settlementMode == SettlementMode.UsdtMargined && uiState.result?.netPnl != null ->
                showMainResultDialog = true
            uiState.settlementMode == SettlementMode.CoinMargined && uiState.coinMarginedResult != null ->
                showCoinMarginedResultDialog = true
        }
    }

    LaunchedEffect(uiState.comparisonItems.map { it.id }) {
        val validIds = uiState.comparisonItems.map { it.id }.toSet() + MAIN_SCHEME_ID
        selectedComparisonIds = selectedComparisonIds.intersect(validIds)
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
    if (showMainResultDialog && uiState.result?.netPnl != null) {
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
                LeverageSelector(
                    leverage = uiState.input.leverage,
                    onLeverageChange = { viewModel.updateInput(uiState.input.copy(leverage = it)) }
                )
                if (
                    uiState.input.marginMode == MarginMode.Cross
                ) {
                    NumberInput(
                        value = uiState.input.totalFunds,
                        onValueChange = { viewModel.updateInput(uiState.input.copy(totalFunds = it)) },
                        label = "账户总资金 USDT（全仓强平估算）",
                        onSubmit = submitMainResult
                    )
                    Text(
                        text = "全仓时账户总资金会直接影响强平价估算，建议填写；不影响仓位收益计算。",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    text = if (uiState.settlementMode == SettlementMode.UsdtMargined) {
                        "U 本位使用保证金与杠杆计算仓位。"
                    } else {
                        "币本位使用币数量计算，收益按当前币种价格折算。"
                    },
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            }

            HomeModule.TargetStop -> {
            SectionPanel(title = "目标与止损") {
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
                Text(
                    text = if (uiState.settlementMode == SettlementMode.UsdtMargined) {
                        "填写平仓价计算 USDT 盈亏；留空时可用目标收益、目标 ROI 或最大亏损反推价格。"
                    } else {
                        "填写平仓价计算币本位盈亏，并按缓存币价折算为 USDT；币本位暂不支持目标收益与止损反推。"
                    },
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (uiState.settlementMode == SettlementMode.UsdtMargined) {
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
                            label = "目标 ROI %",
                            modifier = Modifier.weight(1f),
                            onSubmit = submitMainResult
                        )
                    }
                    InputRow {
                        NumberInput(
                            value = uiState.input.maxLossAmount,
                            onValueChange = { viewModel.updateInput(uiState.input.copy(maxLossAmount = it, maxLossRoiPercent = null, exitPrice = null)) },
                            label = "最大亏损 USDT",
                            modifier = Modifier.weight(1f),
                            onSubmit = submitMainResult
                        )
                        NumberInput(
                            value = uiState.input.maxLossRoiPercent,
                            onValueChange = { viewModel.updateInput(uiState.input.copy(maxLossAmount = null, maxLossRoiPercent = it, exitPrice = null)) },
                            label = "最大亏损 ROI %",
                            modifier = Modifier.weight(1f),
                            onSubmit = submitMainResult
                        )
                    }
                }
            }
            }

            HomeModule.Result -> {
            if (uiState.settlementMode == SettlementMode.UsdtMargined && hasPositionResult && RESULT_CARD_MAIN in revealedResultCards) {
                SectionPanel(title = "计算结果") {
                    CompactExpandableResultCard(
                        label = "净盈亏 · ROI",
                        value = "${pnlText(uiState.result!!.netPnl, DecimalFormatters.formatPositiveNegative(uiState.result!!.netPnl))} USDT · ${DecimalFormatters.formatPercentage(uiState.result!!.roiPercent)}",
                        valueColor = pnlColor(uiState.result!!.netPnl),
                        enabled = uiState.result?.netPnl != null,
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
private fun CoinMarketHeader(
    coin: CoinAsset?,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = MaterialTheme.shapes.small,
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.34f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.24f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            CoinIcon(coin = coin, size = 38)
            Column(
                modifier = Modifier.weight(1f).padding(start = 10.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = "当前币种",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = coin?.symbol ?: "选择币种",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = coin?.name ?: "点击选择计算币种",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = "选择",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun CoinIcon(coin: CoinAsset?, size: Int) {
    val context = LocalContext.current
    val repository = remember(context) { CoinRepository(context) }
    var lazyIconPath by remember(coin?.id, coin?.iconPath) { mutableStateOf(coin?.iconPath) }
    val resourceId = remember(coin?.iconResourceName) {
        coin?.iconResourceName?.let { name ->
            context.resources.getIdentifier(name, "drawable", context.packageName)
        } ?: 0
    }
    val bitmap = remember(lazyIconPath) {
        lazyIconPath?.let { path -> BitmapFactory.decodeFile(path)?.asImageBitmap() }
    }

    LaunchedEffect(coin?.id, coin?.iconResourceName, lazyIconPath) {
        val current = coin ?: return@LaunchedEffect
        if (current.iconResourceName == null && lazyIconPath == null && !current.isCustom) {
            val loaded = withContext(Dispatchers.IO) { repository.loadIconForCoin(current) }
            lazyIconPath = loaded.iconPath
        }
    }

    if (resourceId != 0) {
        Image(
            painter = androidx.compose.ui.res.painterResource(id = resourceId),
            contentDescription = coin?.symbol,
            modifier = Modifier.width(size.dp).height(size.dp).clip(CircleShape),
            contentScale = ContentScale.Fit
        )
    } else if (bitmap != null) {
        Image(
            bitmap = bitmap,
            contentDescription = coin?.symbol,
            modifier = Modifier.width(size.dp).height(size.dp).clip(CircleShape),
            contentScale = ContentScale.Fit
        )
    } else {
        Surface(
            modifier = Modifier.width(size.dp).height(size.dp),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surfaceVariant
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = coin?.symbol?.take(1) ?: "?",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
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
private fun CoinSelectorDialog(
    coins: List<CoinAsset>,
    selectedCoinId: String,
    onSelect: (String) -> Unit,
    onAddCustom: (String, BigDecimal) -> Unit,
    onDeleteCustom: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var search by remember { mutableStateOf("") }
    var showCustomForm by remember { mutableStateOf(false) }
    var customSymbol by remember { mutableStateOf("") }
    var customPrice by remember { mutableStateOf<BigDecimal?>(null) }
    val filtered = coins.filter {
        search.isBlank() ||
            it.symbol.contains(search, ignoreCase = true) ||
            it.name.contains(search, ignoreCase = true)
    }

    if (showCustomForm) {
        CustomCoinDialog(
            symbol = customSymbol,
            price = customPrice,
            onSymbolChange = { customSymbol = it },
            onPriceChange = { customPrice = it },
            onSave = {
                onAddCustom(customSymbol, customPrice!!)
                customSymbol = ""
                customPrice = null
                showCustomForm = false
            },
            onDismiss = {
                customSymbol = ""
                customPrice = null
                showCustomForm = false
            }
        )
        return
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth().widthIn(max = 440.dp),
            shape = MaterialTheme.shapes.medium,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("选择币种", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                OutlinedTextField(
                    value = search,
                    onValueChange = { search = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("搜索币种") },
                    singleLine = true
                )
                LazyColumn(
                    modifier = Modifier.fillMaxWidth().heightIn(max = 320.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(filtered, key = { it.id }) { coin ->
                        Surface(
                            modifier = Modifier.fillMaxWidth().clickable { onSelect(coin.id) },
                            shape = MaterialTheme.shapes.small,
                            color = if (coin.id == selectedCoinId) {
                                MaterialTheme.colorScheme.secondaryContainer
                            } else {
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.50f)
                            }
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    modifier = Modifier.weight(1f),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    CoinIcon(coin = coin, size = 30)
                                    Text(coin.symbol, fontWeight = FontWeight.SemiBold)
                                }
                                if (coin.isCustom) {
                                    TextButton(
                                        onClick = { onDeleteCustom(coin.id) },
                                        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                                    ) { Text("删除", fontWeight = FontWeight.SemiBold) }
                                }
                            }
                        }
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End)
                ) {
                    TextButton(onClick = { showCustomForm = true }) {
                        Text("添加自定义币种", fontWeight = FontWeight.SemiBold)
                    }
                    TextButton(onClick = onDismiss) { Text("关闭", fontWeight = FontWeight.SemiBold) }
                }
            }
        }
    }
}

@Composable
private fun CustomCoinDialog(
    symbol: String,
    price: BigDecimal?,
    onSymbolChange: (String) -> Unit,
    onPriceChange: (BigDecimal?) -> Unit,
    onSave: () -> Unit,
    onDismiss: () -> Unit
) {
    val canSave = symbol.isNotBlank() && price != null && price > BigDecimal.ZERO

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 400.dp),
            shape = MaterialTheme.shapes.medium,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.22f))
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "添加自定义币种",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "自定义币种与价格仅保存在本地设备。",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                CompactTextInput(
                    value = symbol,
                    onValueChange = onSymbolChange,
                    modifier = Modifier.widthIn(max = 220.dp),
                    label = "币种名称，例如 ABC"
                )
                NumberInput(
                    value = price,
                    onValueChange = onPriceChange,
                    label = "币种价格 USDT"
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End)
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("取消", fontWeight = FontWeight.SemiBold)
                    }
                    Button(
                        onClick = onSave,
                        enabled = canSave,
                        shape = MaterialTheme.shapes.small
                    ) {
                        Text("保存并使用")
                    }
                }
            }
        }
    }
}

@Composable
private fun CoinMarginedResultCard(result: CoinMarginedResult, symbol: String) {
    val color = pnlColor(result.pnlCoin)
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.small,
        color = color.copy(alpha = 0.10f),
        border = BorderStroke(2.dp, color.copy(alpha = 0.36f))
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("币本位盈亏", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(
                "${pnlText(result.pnlCoin, DecimalFormatters.formatPositiveNegative(result.pnlCoin))} $symbol",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                "折算价值 ≈ ${pnlText(result.estimatedValueUsdt, DecimalFormatters.formatPositiveNegative(result.estimatedValueUsdt))} USDT",
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                "折算使用当前缓存币种价格，所有计算均在本地完成。",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
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
private fun AveragingDecisionEntryCard(onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.small,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.52f)
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.28f))
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "补仓决策模拟",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "模拟补仓后均价、仓位和目标价收益变化。",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Button(
                onClick = onClick,
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.small
            ) {
                Text("打开补仓决策模拟")
            }
        }
    }
}

@Composable
private fun AveragingDecisionSection(
    input: AveragingDecisionInput,
    result: AveragingDecisionResult?,
    showResultCard: Boolean,
    schemes: List<ExistingScheme>,
    symbol: String,
    onInputChange: (AveragingDecisionInput) -> Unit,
    onCollapse: () -> Unit,
    onSchemeFilled: (ExistingScheme) -> Unit,
    onRequestResult: () -> Unit
) {
    var showSchemeDialog by remember { mutableStateOf(false) }
    var schemeFillMessage by remember { mutableStateOf<String?>(null) }

    if (showSchemeDialog) {
        SchemeSelectionDialog(
            schemes = schemes,
            onSelect = { scheme ->
                onInputChange(
                    input.copy(
                        side = scheme.input.side,
                        currentEntryPrice = scheme.input.entryPrice,
                        currentQuantity = scheme.result.quantity,
                        currentMargin = scheme.result.requiredMargin,
                        currentLeverage = scheme.input.leverage
                    )
                )
                onSchemeFilled(scheme)
                schemeFillMessage = "已填入${scheme.name}"
                showSchemeDialog = false
            },
            onDismiss = { showSchemeDialog = false }
        )
    }

    SectionPanel(
        title = "补仓决策模拟",
        trailing = {
            TextButton(
                onClick = onCollapse,
                colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("收起", fontWeight = FontWeight.Bold)
            }
        }
    ) {
        Text(
            text = "模拟补仓后均价、仓位和目标价收益变化。",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        if (schemes.isNotEmpty()) {
            SoftOutlinedButton(
                onClick = { showSchemeDialog = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("选择已有方案填入")
            }
        }
        schemeFillMessage?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.small,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.50f))
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("输入", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                PositionSideSelector(
                    selectedSide = input.side,
                    onSideChange = { onInputChange(input.copy(side = it)) }
                )
                Text("当前持仓", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
                InputRow {
                    NumberInput(
                        value = input.currentEntryPrice,
                        onValueChange = { onInputChange(input.copy(currentEntryPrice = it)) },
                        label = "当前均价",
                        modifier = Modifier.weight(1f)
                    )
                    NumberInput(
                        value = input.currentQuantity,
                        onValueChange = { onInputChange(input.copy(currentQuantity = it)) },
                        label = "当前 $symbol 数量",
                        modifier = Modifier.weight(1f)
                    )
                }
                InputRow {
                    NumberInput(
                        value = input.currentMargin,
                        onValueChange = { onInputChange(input.copy(currentMargin = it)) },
                        label = "当前保证金",
                        modifier = Modifier.weight(1f)
                    )
                    NumberInput(
                        value = input.currentLeverage,
                        onValueChange = { onInputChange(input.copy(currentLeverage = it)) },
                        label = "当前杠杆",
                        modifier = Modifier.weight(1f)
                    )
                }
                Text("补仓计划", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
                NumberInput(
                    value = input.addEntryPrice,
                    onValueChange = { onInputChange(input.copy(addEntryPrice = it)) },
                    label = "补仓价格"
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    NumberInput(
                        value = input.addAmount,
                        onValueChange = { onInputChange(input.copy(addAmount = it, addQuantity = null)) },
                        label = "补仓金额 USDT",
                        modifier = Modifier.weight(1f)
                    )
                    Column(
                        modifier = Modifier
                            .width(20.dp)
                            .align(Alignment.CenterVertically),
                        verticalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Spacer(modifier = Modifier.height(14.dp))
                        Box(modifier = Modifier.height(40.dp), contentAlignment = Alignment.Center) {
                            Text(
                                text = "或",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                    NumberInput(
                        value = input.addQuantity,
                        onValueChange = { onInputChange(input.copy(addQuantity = it, addAmount = null)) },
                        label = "补仓 $symbol 数量",
                        modifier = Modifier.weight(1f)
                    )
                }
                val addPrice = input.addEntryPrice
                val addLeverage = input.currentLeverage
                when {
                    input.addAmount != null &&
                        addPrice != null &&
                        addPrice > BigDecimal.ZERO &&
                        addLeverage != null &&
                        addLeverage > BigDecimal.ZERO -> {
                        Text(
                            text = "估算补仓数量：${DecimalFormatters.formatQuantity(input.addAmount.multiply(addLeverage).divide(addPrice, 16, java.math.RoundingMode.HALF_UP))} $symbol",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    input.addQuantity != null &&
                        addPrice != null &&
                        addPrice > BigDecimal.ZERO &&
                        addLeverage != null &&
                        addLeverage > BigDecimal.ZERO -> {
                        Text(
                            text = "估算补仓金额：${DecimalFormatters.formatCurrency(input.addQuantity.multiply(addPrice).divide(addLeverage, 16, java.math.RoundingMode.HALF_UP))} USDT",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                NumberInput(
                    value = input.targetExitPrice,
                    onValueChange = { onInputChange(input.copy(targetExitPrice = it)) },
                    label = "目标平仓价"
                )
                Button(
                    onClick = onRequestResult,
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.small
                ) {
                    Text("查看补仓结果")
                }
            }
        }

        if (result != null && showResultCard) {
            CompactExpandableResultCard(
                label = "目标价收益变化",
                value = "${pnlText(result.pnlChange, DecimalFormatters.formatPositiveNegative(result.pnlChange))} USDT",
                valueColor = pnlColor(result.pnlChange),
                onClick = onRequestResult
            )
        } else {
            Text(
                text = "填写完整参数后显示补仓前后目标价收益对比。",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun SchemeSelectionDialog(
    schemes: List<ExistingScheme>,
    onSelect: (ExistingScheme) -> Unit,
    onDismiss: () -> Unit
) {
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
                Text(
                    text = "选择方案填入",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "选择后会填入当前持仓参数，仍可继续手动修改。",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                schemes.forEach { scheme ->
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(scheme) },
                        shape = MaterialTheme.shapes.small,
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.54f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.14f))
                    ) {
                        Column(
                            modifier = Modifier.padding(10.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "${scheme.name} · ${scheme.symbol}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.secondary
                            )
                            Text(
                                text = "${scheme.input.side.label()} · ${scheme.input.leverage.stripTrailingZeros().toPlainString()}x",
                                style = MaterialTheme.typography.labelLarge
                            )
                            Text(
                                text = "开仓价 ${DecimalFormatters.formatCurrency(scheme.input.entryPrice)} USDT · ${scheme.symbol} 数量 ${DecimalFormatters.formatQuantity(scheme.result.quantity)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "保证金 ${DecimalFormatters.formatCurrency(scheme.result.requiredMargin)} USDT",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("取消", fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
private fun CopySchemeDialog(
    schemes: List<ComparisonSchemeView>,
    onSelect: (ComparisonSchemeView) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth().widthIn(max = 400.dp).heightIn(max = 660.dp),
            shape = MaterialTheme.shapes.medium,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.padding(14.dp).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("选择要复制的单子", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(
                    "每次只复制一个方案，避免不同单子的参数和结果混在一起。",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                schemes.forEach { scheme ->
                    Surface(
                        modifier = Modifier.fillMaxWidth().clickable { onSelect(scheme) },
                        shape = MaterialTheme.shapes.small,
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.18f))
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CoinIcon(scheme.coin, 30)
                            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                Text("${scheme.name} · ${scheme.symbol}", fontWeight = FontWeight.Bold)
                                Text(
                                    "${scheme.input.side.label()} · ${scheme.input.leverage.stripTrailingZeros().toPlainString()}x · 净盈亏 ${DecimalFormatters.formatPositiveNegative(scheme.result?.netPnl)} USDT",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Text("复制", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
                TextButton(onClick = onDismiss, modifier = Modifier.align(Alignment.End)) {
                    Text("取消", fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

private data class ExistingScheme(
    val name: String,
    val symbol: String,
    val input: CalculationInput,
    val result: CalculationResult
)

private fun comparisonValidationMessage(
    currentInput: CalculationInput,
    comparisonItems: List<ComparisonItem>
): String? {
    val missingByScheme = buildList {
        comparisonMissingFields(currentInput).takeIf { it.isNotEmpty() }?.let {
            add("方案 1 缺少${it.joinToString("、")}")
        }
        comparisonItems.forEach { item ->
            comparisonMissingFields(item.input).takeIf { it.isNotEmpty() }?.let {
                add("${item.name} 缺少${it.joinToString("、")}")
            }
        }
    }
    return missingByScheme.takeIf { it.isNotEmpty() }
        ?.joinToString("；", prefix = "请先填写完整参数后再查看收益对比：")
}

private fun comparisonMissingFields(input: CalculationInput): List<String> = buildList {
    if (input.entryPrice == null || input.entryPrice <= BigDecimal.ZERO) add("开仓价")
    if (input.exitPrice == null || input.exitPrice <= BigDecimal.ZERO) add("平仓价")
    if (
        (input.margin == null || input.margin <= BigDecimal.ZERO) &&
        (input.quantity == null || input.quantity <= BigDecimal.ZERO)
    ) {
        add("保证金或币数量")
    }
}

private fun averagingMissingFields(input: AveragingDecisionInput): List<String> = buildList {
    if (input.currentEntryPrice == null || input.currentEntryPrice <= BigDecimal.ZERO) add("当前均价")
    if (input.currentQuantity == null || input.currentQuantity <= BigDecimal.ZERO) add("当前币数量")
    if (input.currentLeverage == null || input.currentLeverage <= BigDecimal.ZERO) add("当前杠杆")
    if (input.addEntryPrice == null || input.addEntryPrice <= BigDecimal.ZERO) add("补仓价格")
    if (
        (input.addAmount == null || input.addAmount <= BigDecimal.ZERO) &&
        (input.addQuantity == null || input.addQuantity <= BigDecimal.ZERO)
    ) {
        add("补仓金额或补仓数量")
    }
    if (input.targetExitPrice == null || input.targetExitPrice <= BigDecimal.ZERO) add("目标平仓价")
}

@Composable
private fun MainResultDialog(
    input: CalculationInput,
    result: CalculationResult,
    symbol: String,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth().widthIn(max = 400.dp).heightIn(max = 660.dp),
            shape = MaterialTheme.shapes.medium,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.28f))
        ) {
            Column(
                modifier = Modifier.padding(14.dp).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("计算结果", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                ResultCard(
                    result = result,
                    symbol = symbol
                )
                CalculationInputDetails(input = input, symbol = symbol, settlementMode = SettlementMode.UsdtMargined)
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.small
                ) {
                    Text("关闭")
                }
            }
        }
    }
}

@Composable
private fun CoinMarginedResultDialog(
    input: CalculationInput,
    result: CoinMarginedResult,
    symbol: String,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth().widthIn(max = 400.dp).heightIn(max = 660.dp),
            shape = MaterialTheme.shapes.medium,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.padding(14.dp).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("币本位计算结果", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                CoinMarginedResultCard(result, symbol)
                CalculationInputDetails(input = input, symbol = symbol, settlementMode = SettlementMode.CoinMargined)
                Button(onClick = onDismiss, modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.small) {
                    Text("关闭")
                }
            }
        }
    }
}

@Composable
private fun CalculationInputDetails(
    input: CalculationInput,
    symbol: String,
    settlementMode: SettlementMode
) {
    SectionPanel(title = "交易参数") {
        Text(
            text = "${input.side.label()} · ${input.marginMode.label()} · ${input.leverage.stripTrailingZeros().toPlainString()}x · ${if (settlementMode == SettlementMode.UsdtMargined) "U 本位" else "币本位"}",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        InputRow {
            MetricTile(
                label = "开仓价",
                value = "${DecimalFormatters.formatCurrency(input.entryPrice)} USDT",
                modifier = Modifier.weight(1f)
            )
            MetricTile(
                label = "平仓价",
                value = "${DecimalFormatters.formatCurrency(input.exitPrice)} USDT",
                modifier = Modifier.weight(1f)
            )
        }
        InputRow {
            MetricTile(
                label = "输入保证金",
                value = input.margin?.let { "${DecimalFormatters.formatCurrency(it)} USDT" } ?: "未填写",
                modifier = Modifier.weight(1f)
            )
            MetricTile(
                label = "输入币数量",
                value = input.quantity?.let { "${DecimalFormatters.formatQuantity(it)} $symbol" } ?: "未填写",
                modifier = Modifier.weight(1f)
            )
        }
        InputRow {
            MetricTile(
                label = "开仓 / 平仓费率",
                value = "${input.openFeeRatePercent.stripTrailingZeros().toPlainString()}% / ${input.closeFeeRatePercent.stripTrailingZeros().toPlainString()}%",
                modifier = Modifier.weight(1f)
            )
            MetricTile(
                label = "维持保证金率",
                value = "${input.maintenanceMarginRatePercent.stripTrailingZeros().toPlainString()}%",
                modifier = Modifier.weight(1f)
            )
        }
        if (input.marginMode == MarginMode.Cross) {
            MetricTile(
                label = "账户总资金",
                value = input.totalFunds?.let { "${DecimalFormatters.formatCurrency(it)} USDT" } ?: "未填写"
            )
        }
        if (
            input.targetProfitAmount != null ||
            input.targetRoiPercent != null ||
            input.maxLossAmount != null ||
            input.maxLossRoiPercent != null
        ) {
            InputRow {
                MetricTile(
                    label = "目标收益",
                    value = input.targetProfitAmount?.let { "${DecimalFormatters.formatCurrency(it)} USDT" }
                        ?: input.targetRoiPercent?.let { "${DecimalFormatters.formatPercentage(it)} ROI" }
                        ?: "未填写",
                    modifier = Modifier.weight(1f)
                )
                MetricTile(
                    label = "最大亏损",
                    value = input.maxLossAmount?.let { "${DecimalFormatters.formatCurrency(it)} USDT" }
                        ?: input.maxLossRoiPercent?.let { "${DecimalFormatters.formatPercentage(it)} ROI" }
                        ?: "未填写",
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun AveragingResultDialog(
    input: AveragingDecisionInput,
    result: AveragingDecisionResult,
    symbol: String,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth().widthIn(max = 400.dp).heightIn(max = 660.dp),
            shape = MaterialTheme.shapes.medium,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.28f))
        ) {
            Column(
                modifier = Modifier.padding(14.dp).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("补仓计算完成", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                AveragingDecisionResultCard(result, symbol)
                AveragingInputDetails(input, symbol)
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.small
                ) {
                    Text("关闭")
                }
            }
        }
    }
}

@Composable
private fun AveragingInputDetails(input: AveragingDecisionInput, symbol: String) {
    SectionPanel(title = "补仓参数") {
        Text(input.side.label(), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        InputRow {
            MetricTile("当前均价", "${DecimalFormatters.formatCurrency(input.currentEntryPrice)} USDT", modifier = Modifier.weight(1f))
            MetricTile("当前 $symbol 数量", "${DecimalFormatters.formatQuantity(input.currentQuantity)} $symbol", modifier = Modifier.weight(1f))
        }
        InputRow {
            MetricTile("当前保证金", "${DecimalFormatters.formatCurrency(input.currentMargin)} USDT", modifier = Modifier.weight(1f))
            MetricTile("当前杠杆", "${DecimalFormatters.formatQuantity(input.currentLeverage)}x", modifier = Modifier.weight(1f))
        }
        InputRow {
            MetricTile("补仓价格", "${DecimalFormatters.formatCurrency(input.addEntryPrice)} USDT", modifier = Modifier.weight(1f))
            MetricTile("目标平仓价", "${DecimalFormatters.formatCurrency(input.targetExitPrice)} USDT", modifier = Modifier.weight(1f))
        }
        MetricTile(
            label = if (input.addAmount != null) "补仓金额" else "补仓 $symbol 数量",
            value = input.addAmount?.let { "${DecimalFormatters.formatCurrency(it)} USDT" }
                ?: "${DecimalFormatters.formatQuantity(input.addQuantity)} $symbol"
        )
    }
}

@Composable
private fun ComparisonResultDialog(
    schemes: List<ComparisonSchemeView>,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth().widthIn(max = 400.dp).heightIn(max = 660.dp),
            shape = MaterialTheme.shapes.medium,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.28f))
        ) {
            Column(
                modifier = Modifier.padding(14.dp).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("收益对比完成", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                ComparisonResultsOverview(schemes)
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.small
                ) {
                    Text("关闭")
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

@Composable
private fun CompactExpandableResultCard(
    label: String,
    value: String,
    valueColor: Color,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth().clickable(enabled = enabled, onClick = onClick),
        shape = MaterialTheme.shapes.small,
        color = valueColor.copy(alpha = 0.10f),
        border = BorderStroke(1.dp, valueColor.copy(alpha = 0.38f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(label, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = valueColor)
            }
            Text(
                "展开",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun AveragingDecisionResultCard(result: AveragingDecisionResult, symbol: String) {
    val palette = LocalProfitLossPalette.current
    val changeColor = if (result.pnlChange >= BigDecimal.ZERO) palette.profit else palette.loss

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.small,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("模拟结果", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
                color = changeColor.copy(alpha = 0.16f),
                border = BorderStroke(2.dp, changeColor.copy(alpha = 0.58f))
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "目标价收益变化",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = changeColor
                    )
                    Text(
                        text = "${pnlText(result.pnlChange, DecimalFormatters.formatPositiveNegative(result.pnlChange))} USDT",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = changeColor
                    )
                    Text(
                        text = if (result.pnlChange >= BigDecimal.ZERO) "补仓后目标价收益更高" else "补仓后目标价收益降低",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                MetricTile(
                    label = "新平均开仓价",
                    value = "${DecimalFormatters.formatCurrency(result.newAveragePrice)} USDT",
                    modifier = Modifier.weight(1f)
                )
                MetricTile(
                    label = "新 $symbol 数量",
                    value = "${DecimalFormatters.formatQuantity(result.newQuantity)} $symbol",
                    modifier = Modifier.weight(1f)
                )
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                MetricTile(
                    label = "不补仓目标价收益",
                    value = "${pnlText(result.pnlWithoutAdding, DecimalFormatters.formatPositiveNegative(result.pnlWithoutAdding))} USDT",
                    valueColor = pnlColor(result.pnlWithoutAdding),
                    modifier = Modifier.weight(1f)
                )
                MetricTile(
                    label = "补仓后目标价收益",
                    value = "${pnlText(result.pnlAfterAdding, DecimalFormatters.formatPositiveNegative(result.pnlAfterAdding))} USDT",
                    valueColor = pnlColor(result.pnlAfterAdding),
                    modifier = Modifier.weight(1f)
                )
            }
            Text(
                text = "收益按目标平仓价估算，未扣手续费；结果仅供比较，不构成投资建议。",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun ResultCard(
    result: CalculationResult,
    symbol: String = "币"
) {
    val palette = LocalProfitLossPalette.current
    val netPnl = result.netPnl
    val isProfit = netPnl == null || netPnl >= BigDecimal.ZERO
    val resultAccent = if (isProfit) palette.profit else palette.loss
    val resultBackground = resultAccent.copy(alpha = 0.10f)

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.small,
        color = resultBackground,
        border = BorderStroke(2.dp, resultAccent.copy(alpha = 0.42f))
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.small,
            color = resultBackground,
            contentColor = MaterialTheme.colorScheme.onSurface,
            border = BorderStroke(1.dp, resultAccent.copy(alpha = 0.36f))
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = when {
                        netPnl == null -> "等待盈利或亏损结果"
                        isProfit -> "净盈利"
                        else -> "净亏损"
                    },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = resultAccent
                )
                Text(
                    text = resultSourceLabel(result),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "${pnlText(result.netPnl, DecimalFormatters.formatCurrency(result.netPnl))} USDT",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = resultAccent
                )
                ResultMiniMetric(
                    label = "ROI",
                    value = pnlText(result.roiPercent, DecimalFormatters.formatPercentage(result.roiPercent)),
                    color = resultAccent
                )
            }
        }
        Text(
            text = "核心结果",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        if (result.liquidationPrice != null) {
            RiskLevelCard(distancePercent = result.distanceToLiquidationPercent)
            MetricTile(
                label = "估算强平价",
                value = "${DecimalFormatters.formatCurrency(result.liquidationPrice)} USDT",
                supporting = if (result.usedTotalFundsForLiquidation) {
                    "已使用总资金参与强平计算；未计其他仓位、手续费、资金费率与阶梯维持保证金"
                } else {
                    "逐仓简化估算，未计手续费、资金费率与阶梯维持保证金"
                },
                valueColor = WarningAmber
            )
        } else {
            MetricTile(
                label = "全仓强平价与风险",
                value = "无法可靠估算"
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            MetricTile(
                label = "开仓手续费约",
                value = "${DecimalFormatters.formatCurrency(result.openFee)} USDT",
                modifier = Modifier.weight(1f)
            )
            MetricTile(
                label = "平仓手续费约",
                value = "${DecimalFormatters.formatCurrency(result.closeFee)} USDT",
                modifier = Modifier.weight(1f)
            )
        }
        result.distanceToLiquidationPercent?.let {
            MetricTile(
                label = "距离估算强平价",
                value = DecimalFormatters.formatPercentage(it),
                valueColor = WarningAmber
            )
        }
        val hasTargetOrStop = result.targetProfitPriceByAmount != null ||
            result.targetProfitPriceByRoi != null ||
            result.stopLossPriceByAmount != null ||
            result.stopLossPriceByRoi != null

        if (hasTargetOrStop) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MetricTile(
                    label = "目标收益价",
                    value = targetPriceText(result.targetProfitPriceByAmount, result.targetProfitPriceByRoi),
                    supporting = targetPriceSupporting(result.targetProfitPriceByAmount, result.targetProfitPriceByRoi),
                    valueColor = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )
                MetricTile(
                    label = "止损价",
                    value = targetPriceText(result.stopLossPriceByAmount, result.stopLossPriceByRoi),
                    supporting = targetPriceSupporting(result.stopLossPriceByAmount, result.stopLossPriceByRoi),
                    valueColor = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
    }
}

@Composable
private fun RiskLevelCard(distancePercent: BigDecimal?) {
    val level = when {
        distancePercent == null -> Triple("初始风险未知", MaterialTheme.colorScheme.onSurfaceVariant, "缺少可计算数据")
        distancePercent > BigDecimal("30") -> Triple("初始风险：低风险", MaterialTheme.colorScheme.tertiary, "开仓价距估算强平价超过 30%")
        distancePercent >= BigDecimal("10") -> Triple("初始风险：中风险", WarningAmber, "开仓价距估算强平价在 10% 至 30%")
        else -> Triple("初始风险：高风险", LossRed, "开仓价距估算强平价不足 10%")
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.small,
        color = level.second.copy(alpha = 0.14f),
        border = BorderStroke(1.dp, level.second.copy(alpha = 0.30f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = level.first,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = level.second
                )
                Text(
                    text = level.third,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = "${DecimalFormatters.formatPercentage(distancePercent)}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = level.second
            )
        }
    }
}

@Composable
private fun ResultMiniMetric(label: String, value: String, color: Color) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.SemiBold,
            color = color
        )
    }
}

private fun resultSourceLabel(result: CalculationResult): String {
    val hasTarget = result.targetProfitPriceByAmount != null || result.targetProfitPriceByRoi != null
    val hasStop = result.stopLossPriceByAmount != null || result.stopLossPriceByRoi != null
    return when {
        hasTarget && hasStop -> "已反推止盈与止损，未选择单一结果"
        result.targetProfitPriceByAmount != null -> "按目标收益反推价计算"
        result.targetProfitPriceByRoi != null -> "按目标 ROI 反推价计算"
        result.stopLossPriceByAmount != null -> "按最大亏损反推价计算"
        result.stopLossPriceByRoi != null -> "按最大亏损 ROI 反推价计算"
        result.netPnl != null -> "按平仓价计算"
        else -> "等待平仓价或目标/止损输入"
    }
}

@Composable
private fun MetricTile(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    supporting: String? = null,
    highlight: Boolean = false,
    valueColor: Color? = null
) {
    val resolvedValueColor = valueColor ?: MaterialTheme.colorScheme.onSurface

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.small,
        color = if (highlight) {
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.42f)
        } else {
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.42f)
        },
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.10f))
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = if (highlight) MaterialTheme.typography.titleLarge else MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = resolvedValueColor
            )
            if (supporting != null) {
                Text(
                    text = supporting,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun EmptyResult() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "填写开仓价和保证金后显示仓位，填写平仓价后显示盈亏",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private data class ComparisonSchemeView(
    val id: String,
    val name: String,
    val symbol: String,
    val coin: CoinAsset?,
    val settlementMode: SettlementMode,
    val coinMarginedCalculationMode: CoinMarginedCalculationMode,
    val input: CalculationInput,
    val result: CalculationResult?,
    val isMain: Boolean
)

private fun buildComparisonSchemes(
    mainInput: CalculationInput,
    mainResult: CalculationResult?,
    mainSymbol: String,
    mainSettlementMode: SettlementMode,
    mainCoinMarginedCalculationMode: CoinMarginedCalculationMode,
    items: List<ComparisonItem>,
    results: List<ComparisonResult>,
    coins: List<CoinAsset>
): List<ComparisonSchemeView> = buildList {
    add(
        ComparisonSchemeView(
            MAIN_SCHEME_ID,
            "主方案",
            mainSymbol,
            coins.firstOrNull { it.symbol == mainSymbol },
            mainSettlementMode,
            mainCoinMarginedCalculationMode,
            mainInput,
            mainResult,
            true
        )
    )
    items.forEach { item ->
        add(
            ComparisonSchemeView(
                id = item.id,
                name = item.name,
                symbol = coins.firstOrNull { it.id == item.coinId }?.symbol ?: "币",
                coin = coins.firstOrNull { it.id == item.coinId },
                settlementMode = item.settlementMode,
                coinMarginedCalculationMode = item.coinMarginedCalculationMode,
                input = item.input,
                result = results.firstOrNull { it.item.id == item.id }?.result,
                isMain = false
            )
        )
    }
}

private fun settlementModeLabel(mode: SettlementMode): String = when (mode) {
    SettlementMode.UsdtMargined -> "U 本位"
    SettlementMode.CoinMargined -> "币本位"
}

private fun coinMarginedCalculationModeShortLabel(mode: CoinMarginedCalculationMode): String = when (mode) {
    CoinMarginedCalculationMode.CoinQuantity -> "币数量"
    CoinMarginedCalculationMode.InverseContract -> "反向合约"
}

private fun comparisonSettlementDisplay(
    settlementMode: SettlementMode,
    coinMarginedCalculationMode: CoinMarginedCalculationMode
): String = when (settlementMode) {
    SettlementMode.UsdtMargined -> settlementModeLabel(settlementMode)
    SettlementMode.CoinMargined -> "${settlementModeLabel(settlementMode)}(${coinMarginedCalculationModeShortLabel(coinMarginedCalculationMode)})"
}

private fun comparisonSettlementHistoryDisplay(
    settlementMode: SettlementMode,
    coinMarginedCalculationMode: CoinMarginedCalculationMode
): String = when (settlementMode) {
    SettlementMode.UsdtMargined -> settlementModeLabel(settlementMode)
    SettlementMode.CoinMargined -> "${settlementModeLabel(settlementMode)}（${coinMarginedCalculationModeShortLabel(coinMarginedCalculationMode)}）"
}

private fun selectedComparisonValidationMessage(schemes: List<ComparisonSchemeView>): String? {
    if (schemes.size < 2) return "请至少选择两个完整方案进行对比。"
    val invalid = schemes.mapNotNull { scheme ->
        val missing = comparisonMissingFields(scheme.input)
        when {
            missing.isNotEmpty() -> "${scheme.name}缺少${missing.joinToString("、")}"
            scheme.result?.netPnl == null -> "${scheme.name}无法计算结果"
            else -> null
        }
    }
    return invalid.takeIf { it.isNotEmpty() }?.joinToString(prefix = "参数不完整：", separator = "；")
}

@Composable
private fun ComparisonSchemeListCard(
    scheme: ComparisonSchemeView,
    selected: Boolean,
    onSelectedChange: (Boolean) -> Unit,
    onClick: (() -> Unit)?
) {
    val result = scheme.result
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.small,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.48f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.14f))
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(checked = selected, onCheckedChange = onSelectedChange)
            CoinIcon(coin = scheme.coin, size = 32)
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "${scheme.name} · ${scheme.symbol}",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold
                )
                if (result?.netPnl != null) {
                    Text(
                        text = "净收益 ${pnlText(result.netPnl, DecimalFormatters.formatPositiveNegative(result.netPnl))} USDT",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = pnlColor(result.netPnl)
                    )
                } else {
                    Text(
                        text = if (scheme.isMain) "主方案参数不完整，不可参与对比" else "参数不完整，不可参与对比",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    text = "${scheme.symbol} | ${comparisonSettlementDisplay(scheme.settlementMode, scheme.coinMarginedCalculationMode)} | ${scheme.input.side.label()}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                if (!scheme.isMain && onClick != null) {
                    TextButton(
                        onClick = onClick,
                        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text("编辑", fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

@Composable
private fun ComparisonSchemeEditorDialog(
    initialItem: ComparisonItem,
    coins: List<CoinAsset>,
    onSave: (ComparisonItem) -> Unit,
    onDelete: (ComparisonItem) -> Unit,
    onDismiss: () -> Unit
) {
    var item by remember(initialItem.id) { mutableStateOf(initialItem) }
    var showCoinDialog by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val coin = coins.firstOrNull { it.id == item.coinId }
    if (showCoinDialog) {
        ComparisonCoinSelectorDialog(
            coins = coins,
            selectedCoinId = item.coinId,
            onSelect = {
                item = item.copy(coinId = it)
                showCoinDialog = false
            },
            onDismiss = { showCoinDialog = false }
        )
    }
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth().widthIn(max = 400.dp).heightIn(max = 660.dp),
            shape = MaterialTheme.shapes.medium,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.padding(14.dp).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = if (initialItem.input == CalculationInput()) "添加对比方案" else "编辑对比方案",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                CompactTextInput(
                    value = item.name,
                    onValueChange = { item = item.copy(name = it) },
                    modifier = Modifier.widthIn(max = 220.dp),
                    label = "方案名称"
                )
                SoftOutlinedButton(onClick = { showCoinDialog = true }, modifier = Modifier.fillMaxWidth()) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CoinIcon(coin = coin, size = 26)
                        Text("币种：${coin?.symbol ?: "请选择"}")
                    }
                }
                Text("结算模式", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                TwoOptionModeSelector(
                    firstText = "U 本位",
                    secondText = "币本位",
                    firstSelected = item.settlementMode == SettlementMode.UsdtMargined,
                    onFirstClick = { item = item.copy(settlementMode = SettlementMode.UsdtMargined) },
                    onSecondClick = { item = item.copy(settlementMode = SettlementMode.CoinMargined) }
                )
                if (item.settlementMode == SettlementMode.CoinMargined) {
                    Text("币本位计算方式", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    CoinMarginedModeOption(
                        mode = CoinMarginedCalculationMode.CoinQuantity,
                        selectedMode = item.coinMarginedCalculationMode,
                        onSelect = { item = item.copy(coinMarginedCalculationMode = it) }
                    )
                    CoinMarginedModeOption(
                        mode = CoinMarginedCalculationMode.InverseContract,
                        selectedMode = item.coinMarginedCalculationMode,
                        onSelect = { item = item.copy(coinMarginedCalculationMode = it) }
                    )
                }
                PositionSideSelector(item.input.side, { item = item.copy(input = item.input.copy(side = it)) })
                MarginModeSelector(item.input.marginMode, { item = item.copy(input = item.input.copy(marginMode = it)) })
                LeverageSelector(item.input.leverage, { item = item.copy(input = item.input.copy(leverage = it)) })
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    NumberInput(
                        value = item.input.margin,
                        onValueChange = { item = item.copy(input = item.input.copy(margin = it, quantity = null), lastEditedAmountField = AmountField.Margin) },
                        label = "保证金",
                        modifier = Modifier.weight(1f)
                    )
                    Column(
                        modifier = Modifier.width(20.dp),
                        verticalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Spacer(modifier = Modifier.height(14.dp))
                        Box(modifier = Modifier.height(40.dp), contentAlignment = Alignment.Center) {
                            Text(
                                text = "或",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                    NumberInput(
                        value = item.input.quantity,
                        onValueChange = { item = item.copy(input = item.input.copy(quantity = it, margin = null), lastEditedAmountField = AmountField.Quantity) },
                        label = "${coin?.symbol ?: "币"} 数量",
                        modifier = Modifier.weight(1f)
                    )
                }
                InputRow {
                    NumberInput(
                        value = item.input.entryPrice,
                        onValueChange = { item = item.copy(input = item.input.copy(entryPrice = it)) },
                        label = "开仓价",
                        modifier = Modifier.weight(1f)
                    )
                    NumberInput(
                        value = item.input.exitPrice,
                        onValueChange = { item = item.copy(input = item.input.copy(exitPrice = it)) },
                        label = "平仓价",
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End)
                ) {
                    TextButton(
                        onClick = { onDelete(item) },
                        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                    ) { Text("删除", fontWeight = FontWeight.SemiBold) }
                    TextButton(onClick = onDismiss) { Text("取消", fontWeight = FontWeight.SemiBold) }
                    Button(
                        onClick = {
                            focusManager.clearFocus(force = true)
                            keyboardController?.hide()
                            onSave(item.copy(name = item.name.ifBlank { "未命名方案" }))
                        },
                        shape = MaterialTheme.shapes.small
                    ) { Text("保存") }
                }
            }
        }
    }
}

@Composable
fun ComparisonItemCard(
    item: ComparisonItem,
    result: CalculationResult?,
    coins: List<CoinAsset>,
    onChange: (ComparisonItem) -> Unit,
    onRemove: () -> Unit
) {
    var showCoinDialog by remember { mutableStateOf(false) }
    val coin = coins.firstOrNull { it.id == item.coinId }
    if (showCoinDialog) {
        ComparisonCoinSelectorDialog(
            coins = coins,
            selectedCoinId = item.coinId,
            onSelect = {
                onChange(item.copy(coinId = it))
                showCoinDialog = false
            },
            onDismiss = { showCoinDialog = false }
        )
    }
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.small,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.10f))
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text(
                        text = item.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "${coin?.symbol ?: "币"} · ${item.input.side.label()} · ${item.input.marginMode.label()} · ${item.input.leverage.stripTrailingZeros().toPlainString()}x",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                TextButton(
                    onClick = onRemove,
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("删除", fontWeight = FontWeight.SemiBold)
                }
            }
            SoftOutlinedButton(
                onClick = { showCoinDialog = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("币种：${coin?.symbol ?: "请选择"}")
            }

            PositionSideSelector(
                selectedSide = item.input.side,
                onSideChange = { side ->
                    onChange(item.copy(input = item.input.copy(side = side)))
                }
            )
            MarginModeSelector(
                selectedMode = item.input.marginMode,
                onModeChange = { mode ->
                    onChange(item.copy(input = item.input.copy(marginMode = mode)))
                }
            )
            LeverageSelector(
                leverage = item.input.leverage,
                onLeverageChange = { leverage ->
                    onChange(item.copy(input = item.input.copy(leverage = leverage)))
                }
            )
            InputRow {
                NumberInput(
                    value = item.input.margin,
                    onValueChange = { margin ->
                        onChange(
                            item.copy(
                                input = item.input.copy(margin = margin, quantity = null),
                                lastEditedAmountField = AmountField.Margin
                            )
                        )
                    },
                    label = "保证金",
                    modifier = Modifier.weight(1f)
                )
                NumberInput(
                    value = item.input.entryPrice,
                    onValueChange = { entryPrice ->
                        onChange(item.copy(input = item.input.copy(entryPrice = entryPrice)))
                    },
                    label = "开仓价",
                    modifier = Modifier.weight(1f)
                )
            }
            InputRow {
                NumberInput(
                    value = item.input.exitPrice,
                    onValueChange = { exitPrice ->
                        onChange(
                            item.copy(
                                input = item.input.copy(
                                    exitPrice = exitPrice,
                                    targetProfitAmount = null,
                                    targetRoiPercent = null,
                                    maxLossAmount = null,
                                    maxLossRoiPercent = null
                                )
                            )
                        )
                    },
                    label = "平仓价",
                    modifier = Modifier.weight(1f)
                )
                NumberInput(
                    value = item.input.quantity,
                    onValueChange = { quantity ->
                        onChange(
                            item.copy(
                                input = item.input.copy(quantity = quantity, margin = null),
                                lastEditedAmountField = AmountField.Quantity
                            )
                        )
                    },
                    label = "${coin?.symbol ?: "币"} 数量",
                    modifier = Modifier.weight(1f)
                )
            }

            if (result != null) {
                MetricTile(
                    label = "净盈亏",
                    value = "${pnlText(result.netPnl, DecimalFormatters.formatCurrency(result.netPnl))} USDT",
                    valueColor = pnlColor(result.netPnl)
                )
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

@Composable
private fun ComparisonCoinSelectorDialog(
    coins: List<CoinAsset>,
    selectedCoinId: String,
    onSelect: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var search by remember { mutableStateOf("") }
    val filtered = coins.filter {
        search.isBlank() ||
            it.symbol.contains(search, ignoreCase = true) ||
            it.name.contains(search, ignoreCase = true)
    }
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth().widthIn(max = 440.dp),
            shape = MaterialTheme.shapes.medium,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("选择方案币种", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                OutlinedTextField(
                    value = search,
                    onValueChange = { search = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("搜索币种") },
                    singleLine = true
                )
                LazyColumn(
                    modifier = Modifier.fillMaxWidth().heightIn(max = 320.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(filtered, key = { it.id }) { coin ->
                        Surface(
                            modifier = Modifier.fillMaxWidth().clickable { onSelect(coin.id) },
                            shape = MaterialTheme.shapes.small,
                            color = if (coin.id == selectedCoinId) {
                                MaterialTheme.colorScheme.secondaryContainer
                            } else {
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            }
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(8.dp),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CoinIcon(coin, 30)
                                Text(coin.symbol, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
                TextButton(onClick = onDismiss, modifier = Modifier.align(Alignment.End)) {
                    Text("取消", fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
private fun ComparisonResultsOverview(
    schemes: List<ComparisonSchemeView>
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.small,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.50f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.32f))
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "收益差距",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            ComparisonDifferenceSummary(schemes)
            Text(
                text = "方案详情",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            rankComparisonSchemes(schemes).forEach { scheme ->
                ComparisonSchemeSummary(scheme)
            }
        }
    }
}

private data class RankedComparisonScheme(
    val id: String,
    val netRank: Int?,
    val roiRank: Int?,
    val name: String,
    val symbol: String,
    val coin: CoinAsset?,
    val settlementMode: SettlementMode,
    val coinMarginedCalculationMode: CoinMarginedCalculationMode,
    val input: CalculationInput,
    val result: CalculationResult?
)

private fun rankComparisonSchemes(schemes: List<ComparisonSchemeView>): List<RankedComparisonScheme> {
    val netRanks = schemes.filter { it.result?.netPnl != null }
        .sortedByDescending { it.result?.netPnl }
        .mapIndexed { index, scheme -> scheme.id to index + 1 }
        .toMap()
    val roiRanks = schemes.filter { it.result?.roiPercent != null }
        .sortedByDescending { it.result?.roiPercent }
        .mapIndexed { index, scheme -> scheme.id to index + 1 }
        .toMap()
    return schemes.map {
        RankedComparisonScheme(
            it.id,
            netRanks[it.id],
            roiRanks[it.id],
            it.name,
            it.symbol,
            it.coin,
            it.settlementMode,
            it.coinMarginedCalculationMode,
            it.input,
            it.result
        )
    }
}

@Composable
private fun ComparisonDifferenceSummary(schemes: List<ComparisonSchemeView>) {
    val sorted = schemes.filter { it.result?.netPnl != null }.sortedByDescending { it.result?.netPnl }
    sorted.zipWithNext().forEach { (higher, lower) ->
        val diff = higher.result!!.netPnl!! - lower.result!!.netPnl!!
        ComparisonFormulaCard(
            title = "${higher.name} 与 ${lower.name} 收益差距",
            leftPnl = higher.result.netPnl,
            rightPnl = lower.result.netPnl,
            diff = diff,
            summary = "${higher.name} 比 ${lower.name} 多 ${DecimalFormatters.formatPositiveNegative(diff)} USDT"
        )
    }
}

@Composable
private fun ComparisonSchemeSummary(scheme: RankedComparisonScheme) {
    var expanded by rememberSaveable(scheme.id) { mutableStateOf(false) }
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.small,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.16f))
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CoinIcon(coin = scheme.coin, size = 28)
                    Text(
                        text = "${scheme.name} · ${scheme.symbol} · ${comparisonSettlementHistoryDisplay(scheme.settlementMode, scheme.coinMarginedCalculationMode)}",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                TextButton(
                    onClick = { expanded = !expanded },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text(if (expanded) "收起" else "展开", fontWeight = FontWeight.Bold)
                }
            }
            MetricTile(
                label = scheme.netRank?.let { "净收益排名 #$it" } ?: "净收益",
                value = "${pnlText(scheme.result?.netPnl, DecimalFormatters.formatPositiveNegative(scheme.result?.netPnl))} USDT",
                valueColor = pnlColor(scheme.result?.netPnl)
            )
            Text(
                text = "${scheme.symbol} | ${comparisonSettlementDisplay(scheme.settlementMode, scheme.coinMarginedCalculationMode)} | ${scheme.input.side.label()}",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (expanded) {
                Text(
                    text = "${scheme.input.side.label()} · ${scheme.input.marginMode.label()} · ${scheme.input.leverage.stripTrailingZeros().toPlainString()}x · ${comparisonSettlementHistoryDisplay(scheme.settlementMode, scheme.coinMarginedCalculationMode)}",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                InputRow {
                    MetricTile(
                        label = "开仓价",
                        value = "${DecimalFormatters.formatCurrency(scheme.input.entryPrice)} USDT",
                        modifier = Modifier.weight(1f)
                    )
                    MetricTile(
                        label = "平仓价",
                        value = "${DecimalFormatters.formatCurrency(scheme.input.exitPrice)} USDT",
                        modifier = Modifier.weight(1f)
                    )
                }
                InputRow {
                    MetricTile(
                        label = if (scheme.input.margin != null) "保证金" else "${scheme.symbol} 数量",
                        value = scheme.input.margin?.let { "${DecimalFormatters.formatCurrency(it)} USDT" }
                            ?: "${DecimalFormatters.formatQuantity(scheme.input.quantity)} ${scheme.symbol}",
                        modifier = Modifier.weight(1f)
                    )
                    MetricTile(
                        label = "仓位价值",
                        value = "${DecimalFormatters.formatCurrency(scheme.result?.positionValue)} USDT",
                        modifier = Modifier.weight(1f)
                    )
                }
                InputRow {
                    MetricTile(
                        label = "ROI",
                        value = DecimalFormatters.formatPercentage(scheme.result?.roiPercent),
                        supporting = scheme.roiRank?.let { "ROI 排名 #$it" },
                        valueColor = pnlColor(scheme.result?.roiPercent),
                        modifier = Modifier.weight(1f)
                    )
                    MetricTile(
                        label = "总手续费约",
                        value = "${DecimalFormatters.formatCurrency(scheme.result?.totalFee)} USDT",
                        modifier = Modifier.weight(1f)
                    )
                }
                MetricTile(
                    label = "未扣手续费盈亏",
                    value = "${DecimalFormatters.formatCurrency(scheme.result?.grossPnl)} USDT",
                    valueColor = pnlColor(scheme.result?.grossPnl)
                )
                MetricTile(
                    label = "估算强平价",
                    value = comparisonLiquidationPrice(scheme.input, scheme.result)?.let { "${DecimalFormatters.formatCurrency(it)} USDT" }
                        ?: "无法可靠估算"
                )
            }
        }
    }
}

private fun comparisonLiquidationPrice(input: CalculationInput, result: CalculationResult?): BigDecimal? {
    if (result?.liquidationPrice != null) return result.liquidationPrice
    val entryPrice = input.entryPrice ?: return null
    val leverage = input.leverage.takeIf { it > BigDecimal.ZERO } ?: return null
    val maintenanceRate = input.maintenanceMarginRatePercent.divide(BigDecimal("100"))
    val initialMarginRate = BigDecimal.ONE.divide(leverage, 18, RoundingMode.HALF_UP)
    return when (input.side) {
        PositionSide.Long -> entryPrice.multiply(BigDecimal.ONE - initialMarginRate + maintenanceRate)
            .takeIf { it > BigDecimal.ZERO }
        PositionSide.Short -> entryPrice.multiply(BigDecimal.ONE + initialMarginRate - maintenanceRate)
            .takeIf { it > BigDecimal.ZERO }
    }
}

@Composable
private fun ComparisonFormulaCard(
    title: String,
    leftPnl: BigDecimal?,
    rightPnl: BigDecimal?,
    diff: BigDecimal?,
    summary: String
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.72f),
        border = BorderStroke(2.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.72f))
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(text = title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(
                text = "${DecimalFormatters.formatCurrency(leftPnl)} - ${DecimalFormatters.formatCurrency(rightPnl)} = ${pnlText(diff, DecimalFormatters.formatPositiveNegative(diff))} USDT",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Text(
                text = summary,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = pnlColor(diff)
            )
        }
    }
}

@Composable
private fun SchemePnlRow(name: String, netPnl: BigDecimal?) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
        Text(
            text = "${pnlText(netPnl, DecimalFormatters.formatPositiveNegative(netPnl))} USDT",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = pnlColor(netPnl)
        )
    }
}

private fun comparisonDiffSentence(
    itemName: String,
    referenceName: String = "方案 1",
    referenceNetPnl: BigDecimal?,
    comparisonNetPnl: BigDecimal?,
    comparisonMinusReference: BigDecimal?
): String {
    if (comparisonMinusReference == null || referenceNetPnl == null || comparisonNetPnl == null) {
        return "$itemName 与$referenceName 暂无可比收益差距"
    }

    val amountText = "${DecimalFormatters.formatCurrency(comparisonMinusReference.abs())} USDT"
    val lossDetail = if (comparisonNetPnl < BigDecimal.ZERO) {
        "，且$itemName 亏损 ${DecimalFormatters.formatCurrency(comparisonNetPnl.abs())} USDT"
    } else {
        ""
    }

    return when {
        comparisonMinusReference > BigDecimal.ZERO && comparisonNetPnl < BigDecimal.ZERO ->
            "$itemName 比$referenceName 少亏 $amountText$lossDetail"
        comparisonMinusReference > BigDecimal.ZERO && referenceNetPnl < BigDecimal.ZERO ->
            "$itemName 相比$referenceName 净收益提高 $amountText"
        comparisonMinusReference > BigDecimal.ZERO ->
            "$itemName 比$referenceName 多赚 $amountText"
        comparisonMinusReference < BigDecimal.ZERO && comparisonNetPnl < BigDecimal.ZERO && referenceNetPnl < BigDecimal.ZERO ->
            "$itemName 比$referenceName 多亏 $amountText$lossDetail"
        comparisonMinusReference < BigDecimal.ZERO && comparisonNetPnl < BigDecimal.ZERO ->
            "$itemName 相比$referenceName 净收益减少 $amountText$lossDetail"
        comparisonMinusReference < BigDecimal.ZERO ->
            "$itemName 比$referenceName 少赚 $amountText"
        else -> "$itemName 与$referenceName 净盈亏相同$lossDetail"
    }
}

private fun findOptimalScheme(
    currentResult: CalculationResult?,
    comparisons: List<ComparisonResult>
): Pair<String, BigDecimal>? {
    val candidates = buildList {
        currentResult?.netPnl?.let { add("方案 1" to it) }
        comparisons.forEach { comparison ->
            comparison.result?.netPnl?.let { add(comparison.item.name to it) }
        }
    }

    return candidates.maxByOrNull { it.second }
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

private fun createComparisonHistorySnapshot(schemes: List<ComparisonSchemeView>): HistoryRecord {
    val ranked = rankComparisonSchemes(schemes)
    val sorted = schemes.sortedByDescending { it.result?.netPnl }
    val differences = sorted.zipWithNext().map { (higher, lower) ->
        HistoryField(
            "${higher.name} 与 ${lower.name}",
            "${higher.name} 多 ${DecimalFormatters.formatPositiveNegative(higher.result!!.netPnl!! - lower.result!!.netPnl!!)} USDT"
        )
    }
    return HistoryRecord(
        id = "history_${System.currentTimeMillis()}",
        category = HistoryCategory.SchemeComparison,
        title = "${schemes.size} 个方案收益对比",
        summary = ranked.firstOrNull()?.let { "#1 ${it.name} ${DecimalFormatters.formatPositiveNegative(it.result?.netPnl)} USDT" } ?: "无结果",
        roiSummary = null,
        savedAt = System.currentTimeMillis(),
        sections = schemes.map { scheme ->
            HistorySection("${scheme.name} · ${scheme.symbol}", listOf(
                HistoryField("结算模式", comparisonSettlementHistoryDisplay(scheme.settlementMode, scheme.coinMarginedCalculationMode)),
                HistoryField("币本位计算方式", if (scheme.settlementMode == SettlementMode.CoinMargined) scheme.coinMarginedCalculationMode.label else "不适用"),
                HistoryField("方向", scheme.input.side.label()),
                HistoryField("模式", scheme.input.marginMode.label()),
                HistoryField("杠杆", "${scheme.input.leverage.stripTrailingZeros().toPlainString()}x"),
                HistoryField("保证金", "${DecimalFormatters.formatCurrency(scheme.result?.requiredMargin)} USDT"),
                HistoryField("数量", "${DecimalFormatters.formatQuantity(scheme.result?.quantity)} ${scheme.symbol}"),
                HistoryField("开仓价", "${DecimalFormatters.formatCurrency(scheme.input.entryPrice)} USDT"),
                HistoryField("平仓价", "${DecimalFormatters.formatCurrency(scheme.input.exitPrice)} USDT"),
                HistoryField("净收益", "${DecimalFormatters.formatPositiveNegative(scheme.result?.netPnl)} USDT"),
                HistoryField("ROI", DecimalFormatters.formatPercentage(scheme.result?.roiPercent)),
                HistoryField("手续费", "${DecimalFormatters.formatCurrency(scheme.result?.totalFee)} USDT"),
                HistoryField("强平价", scheme.result?.liquidationPrice?.let { "${DecimalFormatters.formatCurrency(it)} USDT" } ?: "无法可靠估算")
            ))
        } + HistorySection("最终排序", ranked.map {
            HistoryField("${it.netRank ?: "-"} · ${it.name}", "${DecimalFormatters.formatPositiveNegative(it.result?.netPnl)} USDT · ROI ${DecimalFormatters.formatPercentage(it.result?.roiPercent)}")
        }) + HistorySection("收益差距", differences.ifEmpty { listOf(HistoryField("收益差距", "暂无")) })
    )
}

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

@Composable
private fun HistoryScreen(
    records: List<HistoryRecord>,
    onToggleFavorite: (String) -> Unit,
    onDelete: (Set<String>) -> Unit,
    onClearCategory: (HistoryCategory?) -> Unit,
    onBack: () -> Unit
) {
    var category by rememberSaveable { mutableStateOf<HistoryCategory?>(null) }
    var selectedRecord by remember { mutableStateOf<HistoryRecord?>(null) }
    var selectedIds by rememberSaveable { mutableStateOf(emptySet<String>()) }
    BackHandler {
        if (selectedRecord != null) {
            selectedRecord = null
        } else {
            onBack()
        }
    }
    selectedRecord?.let { record ->
        HistoryDetailScreen(
            record = records.firstOrNull { it.id == record.id } ?: record,
            onToggleFavorite = onToggleFavorite,
            onDelete = {
                onDelete(setOf(record.id))
                selectedRecord = null
            },
            onBack = { selectedRecord = null }
        )
        return
    }
    val filtered = records.filter { category == null || it.category == category }
        .sortedWith(compareByDescending<HistoryRecord> { it.favorite }.thenByDescending { it.savedAt })
    SettingsPageLayout(title = "历史记录", onBack = onBack) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            HistoryCategoryButton("全部", category == null, { category = null }, Modifier.weight(1f))
            HistoryCategoryButton(HistoryCategory.ProfitCalculation.label, category == HistoryCategory.ProfitCalculation, { category = HistoryCategory.ProfitCalculation }, Modifier.weight(1f))
            HistoryCategoryButton(HistoryCategory.SchemeComparison.label, category == HistoryCategory.SchemeComparison, { category = HistoryCategory.SchemeComparison }, Modifier.weight(1f))
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            HistoryCategoryButton(HistoryCategory.AveragingSimulation.label, category == HistoryCategory.AveragingSimulation, { category = HistoryCategory.AveragingSimulation }, Modifier.weight(1f))
            HistoryCategoryButton(HistoryCategory.TargetProfitReverse.label, category == HistoryCategory.TargetProfitReverse, { category = HistoryCategory.TargetProfitReverse }, Modifier.weight(1f))
            HistoryCategoryButton(HistoryCategory.StopLossReverse.label, category == HistoryCategory.StopLossReverse, { category = HistoryCategory.StopLossReverse }, Modifier.weight(1f))
        }
        Text(
            text = "仅在点击“保存本次结果”后保存，详情直接读取保存快照。",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        if (filtered.isEmpty()) {
            Text("暂无历史记录", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        filtered.forEach { record ->
            Surface(
                modifier = Modifier.fillMaxWidth().clickable { selectedRecord = record },
                shape = MaterialTheme.shapes.small,
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = record.id in selectedIds,
                        onCheckedChange = { checked ->
                            selectedIds = if (checked) selectedIds + record.id else selectedIds - record.id
                        }
                    )
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        Text("${if (record.favorite) "★ " else ""}${record.title}", fontWeight = FontWeight.Bold)
                        Text(
                            "${record.summary}${record.roiSummary?.let { " · ROI $it" }.orEmpty()}",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(formatTimestamp(record.savedAt), style = MaterialTheme.typography.labelSmall)
                    }
                    Text(record.category.label, style = MaterialTheme.typography.labelSmall)
                }
            }
        }
        if (selectedIds.isNotEmpty()) {
            Button(onClick = { onDelete(selectedIds); selectedIds = emptySet() }, modifier = Modifier.fillMaxWidth()) {
                Text("删除选中记录（${selectedIds.size}）")
            }
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            SoftOutlinedButton(onClick = { onClearCategory(category) }, modifier = Modifier.weight(1f)) {
                Text(if (category == null) "清空全部" else "清空分类")
            }
            SoftOutlinedButton(onClick = onBack, modifier = Modifier.weight(1f)) { Text("返回") }
        }
    }
}

@Composable
private fun HistoryCategoryButton(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(36.dp),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 4.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
            contentColor = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
        )
    ) { Text(text, style = MaterialTheme.typography.labelSmall) }
}

@Composable
private fun HistoryDetailScreen(
    record: HistoryRecord,
    onToggleFavorite: (String) -> Unit,
    onDelete: () -> Unit,
    onBack: () -> Unit
) {
    SettingsPageLayout(title = "历史详情", onBack = onBack) {
        Text(record.title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text("${record.category.label} · ${formatTimestamp(record.savedAt)}", color = MaterialTheme.colorScheme.onSurfaceVariant)
        record.sections.forEach { section ->
            SectionPanel(title = section.title) {
                section.fields.forEach { field ->
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(field.label, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(field.value, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.End, modifier = Modifier.weight(1f).padding(start = 12.dp))
                    }
                }
            }
        }
        Button(onClick = { onToggleFavorite(record.id) }, modifier = Modifier.fillMaxWidth()) {
            Text(if (record.favorite) "取消收藏" else "收藏")
        }
        SoftOutlinedButton(onClick = onDelete, modifier = Modifier.fillMaxWidth()) { Text("删除记录") }
    }
}

@Composable
private fun SettingsScreen(
    pnlDisplayMode: PnlDisplayMode,
    onPnlDisplayModeChange: (PnlDisplayMode) -> Unit,
    themeMode: ThemeMode,
    onThemeModeChange: (ThemeMode) -> Unit,
    coinMarginedCalculationMode: CoinMarginedCalculationMode,
    onCoinMarginedCalculationModeChange: (CoinMarginedCalculationMode) -> Unit,
    moduleOrder: List<HomeModule>,
    visibleModules: Set<HomeModule>,
    onModuleOrderChange: (List<HomeModule>) -> Unit,
    onResetModuleOrder: () -> Unit,
    onModuleVisibilityChange: (HomeModule, Boolean) -> Unit,
    onResetModuleVisibility: () -> Unit,
    feedbackText: String,
    onFeedbackChange: (String) -> Unit,
    priceUpdatedAt: Long?,
    onBack: () -> Unit
) {
    var page by rememberSaveable { mutableStateOf(SettingsPage.Main) }
    BackHandler {
        if (page == SettingsPage.Main) {
            onBack()
        } else {
            page = SettingsPage.Main
        }
    }

    when (page) {
        SettingsPage.Feedback -> FeedbackScreen(
            feedbackText = feedbackText,
            onFeedbackChange = onFeedbackChange,
            onBack = { page = SettingsPage.Main }
        )
        SettingsPage.About -> AboutScreen(
            priceUpdatedAt = priceUpdatedAt,
            onBack = { page = SettingsPage.Main }
        )
        SettingsPage.Privacy -> PrivacyPolicyScreen(onBack = { page = SettingsPage.Main })
        SettingsPage.Disclaimer -> DisclaimerScreen(onBack = { page = SettingsPage.Main })
        SettingsPage.ModuleOrder -> ModuleOrderScreen(
            moduleOrder = moduleOrder,
            onModuleOrderChange = onModuleOrderChange,
            onReset = onResetModuleOrder,
            onBack = { page = SettingsPage.Main }
        )
        SettingsPage.ModuleVisibility -> ModuleVisibilityScreen(
            visibleModules = visibleModules,
            onVisibilityChange = onModuleVisibilityChange,
            onReset = onResetModuleVisibility,
            onBack = { page = SettingsPage.Main }
        )
        SettingsPage.AppTheme -> AppThemeScreen(
            themeMode = themeMode,
            onThemeModeChange = onThemeModeChange,
            onBack = { page = SettingsPage.Main }
        )
        SettingsPage.CoinMarginedMode -> CoinMarginedModeSettingsScreen(
            mode = coinMarginedCalculationMode,
            onModeChange = onCoinMarginedCalculationModeChange,
            onBack = { page = SettingsPage.Main }
        )
        SettingsPage.PnlColor -> PnlColorScreen(
            pnlDisplayMode = pnlDisplayMode,
            onPnlDisplayModeChange = onPnlDisplayModeChange,
            onBack = { page = SettingsPage.Main }
        )
        SettingsPage.Main -> SettingsHome(
            onOpenPage = { page = it },
            onBack = onBack
        )
    }
}

private enum class SettingsPage {
    Main,
    Feedback,
    About,
    Privacy,
    Disclaimer,
    ModuleOrder,
    ModuleVisibility,
    AppTheme,
    CoinMarginedMode,
    PnlColor,
}

private enum class PnlDisplayMode {
    ProfitGreen,
    ProfitRed,
    IconsOnly
}

@Composable
private fun SettingsHome(
    onOpenPage: (SettingsPage) -> Unit,
    onBack: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "设置",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            SettingsMenuButton("App 主题", "选择界面明暗模式") { onOpenPage(SettingsPage.AppTheme) }
            SettingsMenuButton("币本位计算方式", "选择币数量模式或反向合约模式") { onOpenPage(SettingsPage.CoinMarginedMode) }
            SettingsMenuButton("盈利亏损配色", "设置盈亏结果颜色与图标显示") { onOpenPage(SettingsPage.PnlColor) }
            SettingsMenuButton("首页模块排序", "长按拖动调整首页功能顺序") { onOpenPage(SettingsPage.ModuleOrder) }
            SettingsMenuButton("模块显示管理", "开启或关闭首页功能模块") { onOpenPage(SettingsPage.ModuleVisibility) }
            SettingsMenuButton("用户反馈", "提交建议并创建 GitHub Issue") { onOpenPage(SettingsPage.Feedback) }
            SettingsMenuButton("关于 App", "版本信息、数据来源与项目地址") { onOpenPage(SettingsPage.About) }
            SettingsMenuButton("隐私政策", "查看设备信息与数据使用说明") { onOpenPage(SettingsPage.Privacy) }
            SettingsMenuButton("免责声明", "查看投资风险与强平价说明") { onOpenPage(SettingsPage.Disclaimer) }
            SoftOutlinedButton(
                onClick = onBack,
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.small
            ) {
                Text("返回计算器")
            }
        }
    }
}

@Composable
private fun AppThemeScreen(
    themeMode: ThemeMode,
    onThemeModeChange: (ThemeMode) -> Unit,
    onBack: () -> Unit
) {
    SettingsPageLayout(title = "App 主题", onBack = onBack) {
        SectionPanel(title = "明暗模式") {
            Text(
                text = "选择界面明暗模式，设置会保存在本地。",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ThemeModeButton("跟随系统", ThemeMode.System, themeMode, onThemeModeChange, Modifier.weight(1f))
                ThemeModeButton("浅色", ThemeMode.Light, themeMode, onThemeModeChange, Modifier.weight(1f))
                ThemeModeButton("深色", ThemeMode.Dark, themeMode, onThemeModeChange, Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun PnlColorScreen(
    pnlDisplayMode: PnlDisplayMode,
    onPnlDisplayModeChange: (PnlDisplayMode) -> Unit,
    onBack: () -> Unit
) {
    SettingsPageLayout(title = "盈利亏损配色", onBack = onBack) {
        SectionPanel(title = "结果配色") {
            Text(
                text = "配色仅应用于盈亏结果，交易方向统一使用主题色。",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ColorModeButton(
                    text = "盈利绿 · 亏损红",
                    selected = pnlDisplayMode == PnlDisplayMode.ProfitGreen,
                    profitColor = ProfitGreen,
                    lossColor = LossRed,
                    onClick = { onPnlDisplayModeChange(PnlDisplayMode.ProfitGreen) }
                )
                ColorModeButton(
                    text = "盈利红 · 亏损绿",
                    selected = pnlDisplayMode == PnlDisplayMode.ProfitRed,
                    profitColor = LossRed,
                    lossColor = ProfitGreen,
                    onClick = { onPnlDisplayModeChange(PnlDisplayMode.ProfitRed) }
                )
                ColorModeButton(
                    text = "仅图标区分",
                    selected = pnlDisplayMode == PnlDisplayMode.IconsOnly,
                    profitColor = MaterialTheme.colorScheme.onSurface,
                    lossColor = MaterialTheme.colorScheme.onSurface,
                    profitIndicator = "▲ ",
                    lossIndicator = "▼ ",
                    onClick = { onPnlDisplayModeChange(PnlDisplayMode.IconsOnly) }
                )
            }
        }
    }
}

@Composable
private fun CoinMarginedModeSettingsScreen(
    mode: CoinMarginedCalculationMode,
    onModeChange: (CoinMarginedCalculationMode) -> Unit,
    onBack: () -> Unit
) {
    SettingsPageLayout(title = "币本位计算方式", onBack = onBack) {
        SectionPanel(title = "计算模式") {
            Text(
                text = "币本位支持按持仓币数量计算，也支持反向合约公式。设置会保存在本地。",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            CoinMarginedModeOption(
                mode = CoinMarginedCalculationMode.CoinQuantity,
                selectedMode = mode,
                onSelect = onModeChange
            )
            CoinMarginedModeOption(
                mode = CoinMarginedCalculationMode.InverseContract,
                selectedMode = mode,
                onSelect = onModeChange
            )
        }
    }
}

@Composable
private fun CoinMarginedModeDialog(
    initialMode: CoinMarginedCalculationMode,
    onConfirm: (CoinMarginedCalculationMode, Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedMode by rememberSaveable { mutableStateOf(initialMode) }
    var rememberChoice by rememberSaveable { mutableStateOf(true) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("选择币本位计算方式", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "首次切换到币本位时请选择计算方式，之后也可在设置中修改。",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                CoinMarginedModeOption(
                    mode = CoinMarginedCalculationMode.CoinQuantity,
                    selectedMode = selectedMode,
                    onSelect = { selectedMode = it }
                )
                CoinMarginedModeOption(
                    mode = CoinMarginedCalculationMode.InverseContract,
                    selectedMode = selectedMode,
                    onSelect = { selectedMode = it }
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Checkbox(checked = rememberChoice, onCheckedChange = { rememberChoice = it })
                    Text("记住选择，下次不再提示")
                }
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(selectedMode, rememberChoice) }) {
                Text("确认")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("稍后再选")
            }
        }
    )
}

@Composable
private fun CoinMarginedModeOption(
    mode: CoinMarginedCalculationMode,
    selectedMode: CoinMarginedCalculationMode,
    onSelect: (CoinMarginedCalculationMode) -> Unit
) {
    val selected = mode == selectedMode
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect(mode) },
        shape = MaterialTheme.shapes.small,
        color = if (selected) {
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.62f)
        } else {
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.48f)
        },
        border = BorderStroke(
            if (selected) 2.dp else 1.dp,
            if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.16f)
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(mode.label, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(
                mode.shortDescription,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ThemeModeButton(
    text: String,
    mode: ThemeMode,
    selectedMode: ThemeMode,
    onSelect: (ThemeMode) -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = { onSelect(mode) },
        modifier = modifier.height(40.dp),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 6.dp, vertical = 0.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (mode == selectedMode) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
            contentColor = if (mode == selectedMode) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
        ),
        shape = MaterialTheme.shapes.small
    ) {
        Text(text, style = MaterialTheme.typography.labelMedium)
    }
}

@Composable
private fun SettingsMenuButton(title: String, supporting: String, onClick: () -> Unit) {
    SoftOutlinedButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.small
    ) {
        Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(text = title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(text = supporting, style = MaterialTheme.typography.labelMedium)
        }
    }
}

@Composable
private fun ModuleOrderScreen(
    moduleOrder: List<HomeModule>,
    onModuleOrderChange: (List<HomeModule>) -> Unit,
    onReset: () -> Unit,
    onBack: () -> Unit
) {
    var localOrder by remember(moduleOrder) { mutableStateOf(moduleOrder) }
    val hapticFeedback = LocalHapticFeedback.current
    SettingsPageLayout(title = "首页模块排序", onBack = onBack) {
        SectionPanel(title = "长按拖动排序") {
            Text(
                text = "长按模块后上下拖动，顺序会立即保存到本地。",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            localOrder.forEach { module ->
                key(module) {
                var dragDistance by remember(module) { mutableStateOf(0f) }
                var itemHeight by remember(module) { mutableStateOf(0f) }
                var isDragging by remember(module) { mutableStateOf(false) }
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .onSizeChanged { itemHeight = it.height.toFloat() }
                        .offset { IntOffset(0, if (isDragging) dragDistance.roundToInt() else 0) }
                        .zIndex(if (isDragging) 1f else 0f)
                        .shadow(if (isDragging) 12.dp else 0.dp, MaterialTheme.shapes.small)
                        .pointerInput(module) {
                            detectDragGesturesAfterLongPress(
                                onDragStart = {
                                    isDragging = true
                                    hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                },
                                onDragEnd = {
                                    isDragging = false
                                    dragDistance = 0f
                                },
                                onDragCancel = {
                                    isDragging = false
                                    dragDistance = 0f
                                },
                                onDrag = { change, amount ->
                                    change.consume()
                                    dragDistance += amount.y
                                    val currentIndex = localOrder.indexOf(module)
                                    val swapDistance = (itemHeight * 0.55f).coerceAtLeast(48f)
                                    val target = when {
                                        dragDistance > swapDistance -> currentIndex + 1
                                        dragDistance < -swapDistance -> currentIndex - 1
                                        else -> currentIndex
                                    }
                                    if (target in localOrder.indices && target != currentIndex) {
                                        val updated = localOrder.toMutableList()
                                        updated[currentIndex] = localOrder[target]
                                        updated[target] = module
                                        localOrder = updated
                                        onModuleOrderChange(updated)
                                        dragDistance -= if (target > currentIndex) itemHeight else -itemHeight
                                        hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    }
                                }
                            )
                        },
                    shape = MaterialTheme.shapes.small,
                    color = if (isDragging) {
                        MaterialTheme.colorScheme.primaryContainer
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
                    },
                    border = BorderStroke(
                        if (isDragging) 2.dp else 1.dp,
                        if (isDragging) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.18f)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(module.label, fontWeight = FontWeight.SemiBold)
                        Text(
                            if (isDragging) "正在移动" else "长按拖动",
                            style = MaterialTheme.typography.labelMedium,
                            color = if (isDragging) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                }
            }
            SoftOutlinedButton(
                onClick = {
                    localOrder = HomeModule.defaultOrder
                    onReset()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("恢复默认排序")
            }
        }
    }
}

@Composable
private fun ModuleVisibilityScreen(
    visibleModules: Set<HomeModule>,
    onVisibilityChange: (HomeModule, Boolean) -> Unit,
    onReset: () -> Unit,
    onBack: () -> Unit
) {
    SettingsPageLayout(title = "模块显示管理", onBack = onBack) {
        SectionPanel(title = "首页模块") {
            Text(
                text = "仓位参数、目标与止损、计算结果为核心模块，始终显示。这里只管理收益方案对比、补仓决策模拟和历史记录。",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            HomeModule.configurableVisibility.forEach { module ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(module.label, fontWeight = FontWeight.SemiBold)
                    Switch(
                        checked = module in visibleModules,
                        onCheckedChange = { onVisibilityChange(module, it) }
                    )
                }
            }
            SoftOutlinedButton(
                onClick = onReset,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("恢复默认显示")
            }
        }
    }
}

@Composable
private fun FeedbackScreen(
    feedbackText: String,
    onFeedbackChange: (String) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    SettingsPageLayout(title = "用户反馈", onBack = onBack) {
        SectionPanel(title = "反馈内容") {
            OutlinedTextField(
                value = feedbackText,
                onValueChange = onFeedbackChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("建议或问题") },
                minLines = 5,
                maxLines = 9
            )
            Text(
                text = "提交后将打开 GitHub Issue 页面，并附带 App 版本、Android 版本、手机型号和提交时间。请确认内容后再创建 Issue。",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "不会附带收益计算参数、收款地址或其他应用输入内容。",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Button(
                onClick = { openFeedbackIssue(context, feedbackText) },
                enabled = feedbackText.isNotBlank(),
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.small
            ) {
                Text("提交反馈")
            }
        }
    }
}

@Composable
private fun AboutScreen(priceUpdatedAt: Long?, onBack: () -> Unit) {
    val context = LocalContext.current
    val version = appVersion(context)
    SettingsPageLayout(title = "关于 App", onBack = onBack) {
        SectionPanel(title = "仓位助手") {
            DisclaimerParagraph("版本：$version")
            DisclaimerParagraph("用于多币种 U 本位、币本位合约的收益、风险、目标止损、补仓和方案对比估算。")
            DisclaimerParagraph("价格数据来源：CoinGecko 公共 API。")
            DisclaimerParagraph("价格更新时间：${priceUpdatedAt?.let(::formatTimestamp) ?: "暂无缓存"}")
            DisclaimerParagraph("本工具仅提供计算和模拟功能，不构成投资建议。")
            DisclaimerParagraph("用户应自行承担交易风险。")
            SoftOutlinedButton(
                onClick = { openUrl(context, GITHUB_REPOSITORY_URL) },
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.small
            ) {
                Text("打开 GitHub 项目")
            }
        }
    }
}

@Composable
private fun PrivacyPolicyScreen(onBack: () -> Unit) {
    SettingsPageLayout(title = "隐私政策", onBack = onBack) {
        SectionPanel(title = "数据与隐私") {
            DisclaimerParagraph("App 每次启动时会联网获取市值前 100 币种的公开价格，并将价格和更新时间缓存到本地设备。")
            DisclaimerParagraph("所有收益、补仓和方案对比计算均在设备本地完成，不上传交易数据。")
            DisclaimerParagraph("本软件不连接交易所账户，不读取账户，也不执行任何交易操作。")
            DisclaimerParagraph("用户添加的自定义币种名称和价格仅保存在本地设备，不会上传服务器。")
            DisclaimerParagraph("只有当你主动点击“提交反馈”时，系统才会打开 GitHub Issue 页面，并预填你输入的反馈、App 版本、Android 版本、手机型号和提交时间。")
            DisclaimerParagraph("创建 Issue 前，你可以在 GitHub 页面检查、修改或取消提交。Issue 创建后，其内容将受 GitHub 的隐私政策和仓库可见性规则约束。")
        }
    }
}

@Composable
private fun DisclaimerScreen(onBack: () -> Unit) {
    SettingsPageLayout(title = "免责声明", onBack = onBack) {
        SectionPanel(title = "风险说明") {
            DisclaimerParagraph("本工具仅用于收益、风险和补仓价格估算，仅供参考，不构成任何投资建议。")
            DisclaimerParagraph("强平价采用简化公式估算。真实强平价会受到交易所规则、钱包余额、阶梯维持保证金、其他仓位、手续费和资金费率等因素影响。")
            DisclaimerParagraph("数字资产交易具有高风险，请根据自身情况独立判断，并以交易所实际数据为准。")
        }
    }
}

@Composable
private fun SettingsPageLayout(
    title: String,
    onBack: () -> Unit,
    content: @Composable () -> Unit
) {
    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(text = title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            content()
            SoftOutlinedButton(
                onClick = onBack,
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.small
            ) {
                Text("返回设置")
            }
        }
    }
}

@Composable
private fun ColorModeButton(
    text: String,
    selected: Boolean,
    profitColor: Color,
    lossColor: Color,
    profitIndicator: String = "",
    lossIndicator: String = "",
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    SoftOutlinedButton(
        onClick = onClick,
        modifier = modifier,
        shape = MaterialTheme.shapes.small,
        border = BorderStroke(
            if (selected) 2.dp else 1.dp,
            if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)
        )
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                ColorResultPreview(
                    text = "${profitIndicator}+100",
                    color = profitColor,
                    modifier = Modifier.weight(1f)
                )
                ColorResultPreview(
                    text = "${lossIndicator}-100",
                    color = lossColor,
                    modifier = Modifier.weight(1f)
                )
            }
            Text(text = text, fontWeight = FontWeight.Bold)
            Text(
                text = if (selected) "当前使用" else "点击切换",
                style = MaterialTheme.typography.labelSmall,
                color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ColorResultPreview(
    text: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.small,
        color = color.copy(alpha = 0.16f),
        border = BorderStroke(1.dp, color.copy(alpha = 0.46f))
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = color,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun DonationScreen(onBack: () -> Unit) {
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current
    BackHandler(onBack = onBack)

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "🌟 一起让它更好",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.small,
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.52f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.24f))
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "♥",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = LossRed
                        )
                        Text(
                            text = "谢谢你的支持",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                    Text(
                        text = "如果你愿意支持后续更新，可以随心表达心意。每一份支持，都会让我更有动力把这个小工具继续做好。",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            SectionPanel(title = "收款地址") {
                Text(
                    text = DONATION_ADDRESS,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
                Button(
                    onClick = {
                        clipboardManager.setText(AnnotatedString(DONATION_ADDRESS))
                        Toast.makeText(context, "收款地址已复制", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.small
                ) {
                    Text("点击复制收款地址")
                }
            }
            SoftOutlinedButton(
                onClick = onBack,
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.small
            ) {
                Text("返回计算器")
            }
        }
    }
}

@Composable
private fun DisclaimerParagraph(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

@Composable
private fun pnlColor(value: BigDecimal?): androidx.compose.ui.graphics.Color {
    val palette = LocalProfitLossPalette.current
    return when {
        value == null -> MaterialTheme.colorScheme.onSurface
        value >= BigDecimal.ZERO -> palette.profit
        else -> palette.loss
    }
}

@Composable
private fun pnlText(value: BigDecimal?, text: String): String {
    val palette = LocalProfitLossPalette.current
    val indicator = when {
        value == null -> ""
        value >= BigDecimal.ZERO -> palette.profitIndicator
        else -> palette.lossIndicator
    }
    return indicator + text
}

private fun PositionSide.label(): String = if (this == PositionSide.Long) "做多" else "做空"

private fun MarginMode.label(): String = if (this == MarginMode.Cross) "全仓" else "逐仓"

private fun targetPriceText(amountPrice: BigDecimal?, roiPrice: BigDecimal?): String {
    val price = amountPrice ?: roiPrice
    return "${DecimalFormatters.formatCurrency(price)} USDT"
}

private fun targetPriceSupporting(amountPrice: BigDecimal?, roiPrice: BigDecimal?): String? {
    return when {
        amountPrice != null && roiPrice != null -> "ROI 价 ${DecimalFormatters.formatCurrency(roiPrice)} USDT"
        amountPrice != null -> "按 USDT 金额反推"
        roiPrice != null -> "按 ROI 反推"
        else -> null
    }
}

private fun openFeedbackIssue(context: Context, feedbackText: String) {
    val titleSummary = feedbackText.lineSequence().firstOrNull().orEmpty().take(42)
    val submittedAt = SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z", Locale.getDefault()).format(Date())
    val issueBody = buildString {
        appendLine("## 用户反馈")
        appendLine()
        appendLine(feedbackText.trim())
        appendLine()
        appendLine("## 设备信息")
        appendLine()
        appendLine("- App 版本：${appVersion(context)}")
        appendLine("- Android 版本：${Build.VERSION.RELEASE}（API ${Build.VERSION.SDK_INT}）")
        appendLine("- 手机型号：${Build.MANUFACTURER} ${Build.MODEL}")
        appendLine("- 提交时间：$submittedAt")
        appendLine()
        append("> 此 Issue 由仓位助手反馈页面预填，用户已在 GitHub 页面确认后提交。")
    }
    val issueUrl = Uri.parse(GITHUB_ISSUE_URL)
        .buildUpon()
        .appendQueryParameter("title", "[用户反馈] $titleSummary")
        .appendQueryParameter("body", issueBody)
        .build()

    runCatching {
        context.startActivity(Intent(Intent.ACTION_VIEW, issueUrl))
    }.onFailure {
        Toast.makeText(context, "无法打开 GitHub，请检查浏览器设置", Toast.LENGTH_SHORT).show()
    }
}

private fun openUrl(context: Context, url: String) {
    runCatching {
        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
    }.onFailure {
        Toast.makeText(context, "无法打开链接", Toast.LENGTH_SHORT).show()
    }
}

@Suppress("DEPRECATION")
private fun appVersion(context: Context): String {
    return runCatching {
        context.packageManager.getPackageInfo(context.packageName, 0).versionName
    }.getOrNull() ?: "未知"
}

private fun formatTimestamp(timestamp: Long): String {
    return SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date(timestamp))
}

private const val DONATION_ADDRESS = "请在此处添加你的收款地址"
private const val GITHUB_REPOSITORY_URL = "https://github.com/yohnyn/calc-android-app"
private const val GITHUB_ISSUE_URL = "$GITHUB_REPOSITORY_URL/issues/new"
private const val MAIN_SCHEME_ID = "main_scheme"
