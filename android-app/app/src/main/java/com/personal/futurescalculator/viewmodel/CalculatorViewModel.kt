package com.personal.futurescalculator.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.personal.futurescalculator.data.CoinRepository
import com.personal.futurescalculator.data.UiPreferencesRepository
import com.personal.futurescalculator.data.HistoryRepository
import com.personal.futurescalculator.data.PlanRepository
import com.personal.futurescalculator.domain.AveragingCalculator
import com.personal.futurescalculator.domain.AveragingDecisionCalculator
import com.personal.futurescalculator.domain.ComparisonCalculator
import com.personal.futurescalculator.domain.CoinMarginedCalculator
import com.personal.futurescalculator.domain.FuturesCalculator
import com.personal.futurescalculator.model.AmountField
import com.personal.futurescalculator.model.AveragingInput
import com.personal.futurescalculator.model.AveragingResult
import com.personal.futurescalculator.model.AveragingDecisionInput
import com.personal.futurescalculator.model.AveragingDecisionResult
import com.personal.futurescalculator.model.CalculationInput
import com.personal.futurescalculator.model.CalculationResult
import com.personal.futurescalculator.model.CoinAsset
import com.personal.futurescalculator.model.CoinMarginedCalculationMode
import com.personal.futurescalculator.model.CoinMarginedResult
import com.personal.futurescalculator.model.ComparisonItem
import com.personal.futurescalculator.model.ComparisonResult
import com.personal.futurescalculator.model.CopyFormat
import com.personal.futurescalculator.model.SettlementMode
import com.personal.futurescalculator.model.SavedPlan
import com.personal.futurescalculator.model.ThemeMode
import com.personal.futurescalculator.model.HistoryRecord
import com.personal.futurescalculator.model.HistoryCategory
import com.personal.futurescalculator.model.MarginMode
import com.personal.futurescalculator.model.MaxOpenResult
import com.personal.futurescalculator.model.PositionSide
import com.personal.futurescalculator.model.toComparisonItem
import com.personal.futurescalculator.model.toSavedPlan
import java.math.BigDecimal
import java.math.RoundingMode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class CalculatorUiState(
    val input: CalculationInput = CalculationInput(),
    val lastEditedAmountField: AmountField = AmountField.Margin,
    val result: CalculationResult? = null,
    val maxOpenResult: MaxOpenResult? = null,
    val comparisonItems: List<ComparisonItem> = emptyList(),
    val comparisonResults: List<ComparisonResult> = emptyList(),
    val averagingInput: AveragingInput = AveragingInput(),
    val averagingResult: AveragingResult? = null,
    val averagingDecisionInput: AveragingDecisionInput = AveragingDecisionInput(),
    val averagingDecisionResult: AveragingDecisionResult? = null,
    val settlementMode: SettlementMode = SettlementMode.UsdtMargined,
    val coins: List<CoinAsset> = emptyList(),
    val selectedCoinId: String = "bitcoin",
    val priceUpdatedAt: Long? = null,
    val isLoadingPrices: Boolean = false,
    val priceLoadError: Boolean = false,
    val coinMarginedCalculationMode: CoinMarginedCalculationMode = CoinMarginedCalculationMode.CoinQuantity,
    val coinMarginedResult: CoinMarginedResult? = null,
    val hasSeenCoinMarginedModeDialog: Boolean = false,
    val showCoinMarginedModeDialog: Boolean = false,
    val averagingExpanded: Boolean = false,
    val themeMode: ThemeMode = ThemeMode.System,
    val copyFormat: CopyFormat = CopyFormat.Summary,
    val historyRecords: List<HistoryRecord> = emptyList(),
    val savedPlans: List<SavedPlan> = emptyList()
)

class CalculatorViewModel(context: Context) : ViewModel() {
    private val _uiState = MutableStateFlow(CalculatorUiState())
    val uiState: StateFlow<CalculatorUiState> = _uiState.asStateFlow()
    
    private val futuresCalculator = FuturesCalculator()
    private val comparisonCalculator = ComparisonCalculator()
    private val averagingCalculator = AveragingCalculator()
    private val averagingDecisionCalculator = AveragingDecisionCalculator()
    private val coinMarginedCalculator = CoinMarginedCalculator()
    private val coinRepository = CoinRepository(context)
    private val uiPreferencesRepository = UiPreferencesRepository(context)
    private val historyRepository = HistoryRepository(context)
    private val planRepository = PlanRepository(context)

    init {
        val cached = coinRepository.loadCachedCoins()
        val custom = coinRepository.loadCustomCoins()
        _uiState.value = _uiState.value.copy(
            coins = cached + custom,
            selectedCoinId = coinRepository.loadSelectedCoinId(),
            priceUpdatedAt = coinRepository.loadUpdatedAt(),
            averagingExpanded = uiPreferencesRepository.loadAveragingExpanded(),
            themeMode = uiPreferencesRepository.loadThemeMode(),
            copyFormat = uiPreferencesRepository.loadCopyFormat(),
            coinMarginedCalculationMode = uiPreferencesRepository.loadCoinMarginedCalculationMode()
                ?: CoinMarginedCalculationMode.CoinQuantity,
            hasSeenCoinMarginedModeDialog = uiPreferencesRepository.loadHasSeenCoinMarginedModeDialog(),
            historyRecords = historyRepository.load(),
            savedPlans = planRepository.load()
        )
        refreshPrices()
    }
    
    fun updateInput(input: CalculationInput) {
        viewModelScope.launch {
            val normalizedInput = normalizeAmountFields(input, _uiState.value.lastEditedAmountField)
            _uiState.value = _uiState.value.copy(input = normalizedInput)
            calculateResult()
            calculateComparisonResults()
            calculateAveragingResult()
            calculateCoinMarginedResult()
        }
    }
    
    fun updateAmountInput(field: AmountField, value: BigDecimal?) {
        viewModelScope.launch {
            val currentInput = _uiState.value.input
            val updatedInput = when (field) {
                AmountField.Margin -> currentInput.copy(margin = value)
                AmountField.Quantity -> currentInput.copy(quantity = value)
            }
            _uiState.value = _uiState.value.copy(
                input = normalizeAmountFields(updatedInput, field),
                lastEditedAmountField = field
            )
            calculateResult()
            calculateComparisonResults()
            calculateCoinMarginedResult()
        }
    }
    
    fun removeComparisonItem(index: Int) {
        viewModelScope.launch {
            val currentItems = _uiState.value.comparisonItems
            if (index >= 0 && index < currentItems.size) {
                _uiState.value = _uiState.value.copy(comparisonItems = currentItems.filterIndexed { itemIndex, _ ->
                    itemIndex != index
                })
                calculateComparisonResults()
            }
        }
    }

    fun removeComparisonItem(id: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                comparisonItems = _uiState.value.comparisonItems.filterNot { it.id == id }
            )
            calculateComparisonResults()
        }
    }
    
    fun updateComparisonItem(index: Int, item: ComparisonItem) {
        viewModelScope.launch {
            val currentItems = _uiState.value.comparisonItems
            if (index >= 0 && index < currentItems.size) {
                val newItems = currentItems.toMutableList()
                newItems[index] = item
                _uiState.value = _uiState.value.copy(comparisonItems = newItems)
                calculateComparisonResults()
            }
        }
    }

    fun saveComparisonItem(item: ComparisonItem) {
        viewModelScope.launch {
            val normalizedItem = if (item.settlementMode == SettlementMode.CoinMargined) {
                item.copy(coinMarginedCalculationMode = _uiState.value.coinMarginedCalculationMode)
            } else {
                item
            }
            val items = _uiState.value.comparisonItems
            val updated = if (items.any { it.id == normalizedItem.id }) {
                items.map { if (it.id == normalizedItem.id) normalizedItem else it }
            } else {
                items + normalizedItem
            }
            _uiState.value = _uiState.value.copy(comparisonItems = updated)
            calculateComparisonResults()
        }
    }
    
    fun addEmptyComparisonScheme() {
        viewModelScope.launch {
            val currentItems = _uiState.value.comparisonItems
            val currentItem = ComparisonItem(
                id = "item_${System.currentTimeMillis()}",
                name = "方案 ${currentItems.size + 2}",
                coinId = _uiState.value.selectedCoinId,
                input = CalculationInput(),
                lastEditedAmountField = AmountField.Margin
            )
            _uiState.value = _uiState.value.copy(comparisonItems = currentItems + currentItem)
            calculateComparisonResults()
        }
    }
    
    fun reset() {
        viewModelScope.launch {
            val state = _uiState.value
            _uiState.value = CalculatorUiState(
                settlementMode = state.settlementMode,
                coins = state.coins,
                selectedCoinId = state.selectedCoinId,
                priceUpdatedAt = state.priceUpdatedAt,
                priceLoadError = state.priceLoadError,
                averagingExpanded = state.averagingExpanded,
                themeMode = state.themeMode,
                copyFormat = state.copyFormat,
                coinMarginedCalculationMode = state.coinMarginedCalculationMode,
                hasSeenCoinMarginedModeDialog = state.hasSeenCoinMarginedModeDialog,
                historyRecords = state.historyRecords,
                savedPlans = state.savedPlans
            )
        }
    }

    fun setAveragingExpanded(expanded: Boolean) {
        uiPreferencesRepository.saveAveragingExpanded(expanded)
        _uiState.value = _uiState.value.copy(averagingExpanded = expanded)
    }

    fun updateThemeMode(mode: ThemeMode) {
        uiPreferencesRepository.saveThemeMode(mode)
        _uiState.value = _uiState.value.copy(themeMode = mode)
    }

    fun updateCopyFormat(format: CopyFormat) {
        uiPreferencesRepository.saveCopyFormat(format)
        _uiState.value = _uiState.value.copy(copyFormat = format)
    }

    fun updateCoinMarginedCalculationMode(mode: CoinMarginedCalculationMode) {
        uiPreferencesRepository.saveCoinMarginedCalculationMode(mode)
        _uiState.value = _uiState.value.copy(coinMarginedCalculationMode = mode)
        calculateCoinMarginedResult()
    }

    fun onContractModeChanged(mode: SettlementMode) {
        updateSettlementMode(mode)
    }

    fun onCoinMarginedModeSelected(mode: CoinMarginedCalculationMode) {
        uiPreferencesRepository.saveCoinMarginedCalculationMode(mode)
        uiPreferencesRepository.saveHasSeenCoinMarginedModeDialog(true)
        _uiState.value = _uiState.value.copy(
            coinMarginedCalculationMode = mode,
            hasSeenCoinMarginedModeDialog = true,
            input = normalizeAmountFields(_uiState.value.input, AmountField.Quantity),
            lastEditedAmountField = AmountField.Quantity,
            settlementMode = SettlementMode.CoinMargined,
            showCoinMarginedModeDialog = false
        )
        calculateCoinMarginedResult()
        calculateResult()
        calculateComparisonResults()
    }

    fun dismissCoinMarginedModeDialog() {
        _uiState.value = _uiState.value.copy(showCoinMarginedModeDialog = false)
    }

    fun saveHistoryRecord(record: HistoryRecord) {
        val updated = listOf(record) + _uiState.value.historyRecords
        historyRepository.save(updated)
        _uiState.value = _uiState.value.copy(historyRecords = updated)
    }

    fun toggleHistoryFavorite(id: String) {
        val updated = _uiState.value.historyRecords.map {
            if (it.id == id) it.copy(favorite = !it.favorite) else it
        }
        historyRepository.save(updated)
        _uiState.value = _uiState.value.copy(historyRecords = updated)
    }

    fun deleteHistoryRecords(ids: Set<String>) {
        val updated = _uiState.value.historyRecords.filterNot { it.id in ids }
        historyRepository.save(updated)
        _uiState.value = _uiState.value.copy(historyRecords = updated)
    }

    fun clearHistoryCategory(category: HistoryCategory?) {
        val updated = if (category == null) emptyList() else {
            _uiState.value.historyRecords.filterNot { it.category == category }
        }
        historyRepository.save(updated)
        _uiState.value = _uiState.value.copy(historyRecords = updated)
    }

    fun saveCurrentPlan(name: String) {
        val state = _uiState.value
        val plan = ComparisonItem(
            id = "plan_${System.currentTimeMillis()}",
            name = name.ifBlank { "开仓方案 ${state.savedPlans.size + 1}" },
            coinId = state.selectedCoinId,
            settlementMode = state.settlementMode,
            coinMarginedCalculationMode = state.coinMarginedCalculationMode,
            input = state.input,
            lastEditedAmountField = state.lastEditedAmountField
        ).toSavedPlan()
        val updated = listOf(plan) + state.savedPlans
        planRepository.save(updated)
        _uiState.value = state.copy(savedPlans = updated)
    }

    fun deleteSavedPlan(id: String) {
        val updated = _uiState.value.savedPlans.filterNot { it.id == id }
        planRepository.save(updated)
        _uiState.value = _uiState.value.copy(savedPlans = updated)
    }

    fun clearSavedPlans() {
        planRepository.save(emptyList())
        uiPreferencesRepository.saveAveragingExpanded(false)
        _uiState.value = _uiState.value.copy(
            savedPlans = emptyList(),
            comparisonItems = emptyList(),
            comparisonResults = emptyList(),
            averagingDecisionInput = AveragingDecisionInput(),
            averagingDecisionResult = null,
            averagingExpanded = false
        )
    }

    fun renameSavedPlan(id: String, name: String) {
        val resolvedName = name.trim().ifBlank { "未命名方案" }
        val updated = _uiState.value.savedPlans.map { plan ->
            if (plan.id == id) plan.copy(name = resolvedName, updatedAt = System.currentTimeMillis()) else plan
        }
        planRepository.save(updated)
        _uiState.value = _uiState.value.copy(savedPlans = updated)
    }

    fun updateSavedPlanNote(id: String, note: String) {
        val updated = _uiState.value.savedPlans.map { plan ->
            if (plan.id == id) plan.copy(note = note.trim(), updatedAt = System.currentTimeMillis()) else plan
        }
        planRepository.save(updated)
        _uiState.value = _uiState.value.copy(savedPlans = updated)
    }

    fun duplicateSavedPlan(plan: SavedPlan) {
        val now = System.currentTimeMillis()
        val copy = plan.copy(
            id = "plan_$now",
            name = duplicatePlanName(plan.name),
            createdAt = now,
            updatedAt = now
        )
        val updated = listOf(copy) + _uiState.value.savedPlans
        planRepository.save(updated)
        _uiState.value = _uiState.value.copy(savedPlans = updated)
    }

    fun saveHistoryAsPlan(record: HistoryRecord): Boolean {
        val plan = record.toSavedPlanFromHistory(_uiState.value.coins, _uiState.value.coinMarginedCalculationMode)
            ?: return false
        val updated = listOf(plan) + _uiState.value.savedPlans
        planRepository.save(updated)
        _uiState.value = _uiState.value.copy(savedPlans = updated)
        return true
    }

    fun openSavedPlan(plan: SavedPlan) {
        _uiState.value = _uiState.value.copy(
            input = normalizeAmountFields(
                plan.input.copy(totalFunds = null, estimateLiquidation = false, calculateMaxOpen = false),
                plan.lastEditedAmountField
            ),
            selectedCoinId = plan.coinId,
            settlementMode = plan.settlementMode,
            lastEditedAmountField = plan.lastEditedAmountField
        )
        coinRepository.saveSelectedCoin(plan.coinId)
        calculateResult()
        calculateComparisonResults()
        calculateAveragingResult()
        calculateCoinMarginedResult()
    }

    fun addSavedPlanToComparison(plan: SavedPlan) {
        val item = plan.toComparisonItem().copy(
            id = "item_${System.currentTimeMillis()}",
            coinMarginedCalculationMode = _uiState.value.coinMarginedCalculationMode
        )
        val updated = _uiState.value.comparisonItems + item
        _uiState.value = _uiState.value.copy(comparisonItems = updated)
        calculateComparisonResults()
    }

    fun updateSettlementMode(mode: SettlementMode) {
        val source = if (mode == SettlementMode.UsdtMargined) {
            AmountField.Margin
        } else {
            AmountField.Quantity
        }
        if (
            mode == SettlementMode.CoinMargined &&
            _uiState.value.settlementMode != SettlementMode.CoinMargined &&
            !_uiState.value.hasSeenCoinMarginedModeDialog
        ) {
            _uiState.value = _uiState.value.copy(showCoinMarginedModeDialog = true)
            return
        }
        _uiState.value = _uiState.value.copy(
            settlementMode = mode,
            input = normalizeAmountFields(_uiState.value.input, source),
            lastEditedAmountField = source,
            showCoinMarginedModeDialog = false
        )
        calculateResult()
        calculateComparisonResults()
        calculateCoinMarginedResult()
    }

    fun selectCoin(id: String) {
        coinRepository.saveSelectedCoin(id)
        _uiState.value = _uiState.value.copy(selectedCoinId = id)
        calculateCoinMarginedResult()
    }

    fun addCustomCoin(symbol: String, price: BigDecimal) {
        val normalized = symbol.trim().uppercase()
        if (normalized.isBlank() || price <= BigDecimal.ZERO) return
        val existingCustom = coinRepository.loadCustomCoins()
        val coin = CoinAsset(
            id = "custom_${normalized.lowercase()}",
            symbol = normalized,
            name = normalized,
            priceUsdt = price,
            isCustom = true
        )
        val updatedCustom = (existingCustom.filterNot { it.id == coin.id } + coin).sortedBy { it.symbol }
        coinRepository.saveCustomCoins(updatedCustom)
        _uiState.value = _uiState.value.copy(
            coins = _uiState.value.coins.filterNot { it.id == coin.id } + coin,
            selectedCoinId = coin.id
        )
        coinRepository.saveSelectedCoin(coin.id)
        calculateCoinMarginedResult()
    }

    fun deleteCustomCoin(id: String) {
        val updatedCustom = coinRepository.loadCustomCoins().filterNot { it.id == id }
        coinRepository.saveCustomCoins(updatedCustom)
        val remaining = _uiState.value.coins.filterNot { it.id == id }
        val selected = if (_uiState.value.selectedCoinId == id) remaining.firstOrNull()?.id ?: "bitcoin" else _uiState.value.selectedCoinId
        _uiState.value = _uiState.value.copy(coins = remaining, selectedCoinId = selected)
        coinRepository.saveSelectedCoin(selected)
        calculateCoinMarginedResult()
    }

    fun updateAveragingInput(input: AveragingInput) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                averagingInput = input
            )
            calculateAveragingResult()
        }
    }

    fun updateAveragingDecisionInput(input: AveragingDecisionInput) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                averagingDecisionInput = input,
                averagingDecisionResult = averagingDecisionCalculator.calculate(input)
            )
        }
    }
    
    private fun calculateResult() {
        val input = _uiState.value.input
        val result = futuresCalculator.calculate(input)
        val maxOpenResult = futuresCalculator.calculateMaxOpen(input)
        _uiState.value = _uiState.value.copy(result = result, maxOpenResult = maxOpenResult)
    }
    
    private fun calculateComparisonResults() {
        val currentInput = _uiState.value.input
        val currentResult = _uiState.value.result
        val currentCoinMarginedResult = calculateCoinMarginedResultFor(
            input = currentInput,
            calculationMode = _uiState.value.coinMarginedCalculationMode,
            coinId = _uiState.value.selectedCoinId
        )
        val comparisonItems = _uiState.value.comparisonItems
        val comparisonResults = comparisonCalculator.compare(
            current = currentResult,
            currentCoinMargined = currentCoinMarginedResult,
            currentSettlementMode = _uiState.value.settlementMode,
            coinMarginedCalculationMode = _uiState.value.coinMarginedCalculationMode,
            items = comparisonItems,
            coinPricesById = _uiState.value.coins.associate { it.id to it.priceUsdt }
        )
        _uiState.value = _uiState.value.copy(comparisonResults = comparisonResults)
    }

    private fun calculateAveragingResult() {
        val state = _uiState.value
        val result = averagingCalculator.calculate(
            input = state.averagingInput,
            side = state.input.side,
            marginMode = state.input.marginMode,
            leverage = state.input.leverage,
            maintenanceMarginRatePercent = state.input.maintenanceMarginRatePercent
        )
        _uiState.value = state.copy(averagingResult = result)
    }

    private fun refreshPrices() {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.value = _uiState.value.copy(isLoadingPrices = true, priceLoadError = false)
            runCatching { coinRepository.fetchTopCoins() }
                .onSuccess { fetched ->
                    val custom = coinRepository.loadCustomCoins()
                    val selectedId = _uiState.value.selectedCoinId
                    val resolvedSelectedId = selectedId.takeIf { id ->
                        (fetched + custom).any { it.id == id }
                    } ?: fetched.firstOrNull()?.id ?: custom.firstOrNull()?.id ?: "bitcoin"
                    coinRepository.saveSelectedCoin(resolvedSelectedId)
                    _uiState.value = _uiState.value.copy(
                        coins = fetched + custom,
                        selectedCoinId = resolvedSelectedId,
                        priceUpdatedAt = coinRepository.loadUpdatedAt(),
                        isLoadingPrices = false
                    )
                    calculateCoinMarginedResult()
                }
                .onFailure {
                    _uiState.value = _uiState.value.copy(isLoadingPrices = false, priceLoadError = true)
                }
        }
    }

    private fun calculateCoinMarginedResult() {
        val state = _uiState.value
        _uiState.value = state.copy(
            coinMarginedResult = calculateCoinMarginedResultFor(
                input = state.input,
                calculationMode = state.coinMarginedCalculationMode,
                coinId = state.selectedCoinId
            )
        )
    }

    private fun calculateCoinMarginedResultFor(
        input: CalculationInput,
        calculationMode: CoinMarginedCalculationMode,
        coinId: String
    ): CoinMarginedResult? {
        val coin = _uiState.value.coins.firstOrNull { it.id == coinId }
        return coinMarginedCalculator.calculate(
            calculationMode = calculationMode,
            side = input.side,
            quantity = input.quantity,
            entryPrice = input.entryPrice,
            exitPrice = input.exitPrice,
            currentPrice = coin?.priceUsdt
        )
    }

}

internal fun normalizeAmountFields(input: CalculationInput, source: AmountField): CalculationInput {
    val entryPrice = input.entryPrice
    val leverage = input.leverage
    if (entryPrice == null || entryPrice <= BigDecimal.ZERO || leverage <= BigDecimal.ZERO) {
        return when (source) {
            AmountField.Margin -> input.copy(quantity = null)
            AmountField.Quantity -> input.copy(margin = null)
        }
    }

    return when (source) {
        AmountField.Margin -> {
            val margin = input.margin
            input.copy(
                quantity = if (margin != null && margin > BigDecimal.ZERO) {
                    margin.multiply(leverage).divide(entryPrice, 16, RoundingMode.HALF_UP)
                } else {
                    null
                }
            )
        }
        AmountField.Quantity -> {
            val quantity = input.quantity
            input.copy(
                margin = if (quantity != null && quantity > BigDecimal.ZERO) {
                    quantity.multiply(entryPrice).divide(leverage, 16, RoundingMode.HALF_UP)
                } else {
                    null
                }
            )
        }
    }
}

private fun duplicatePlanName(name: String): String =
    if (name.contains("副本")) "$name 2" else "$name 副本"

private fun HistoryRecord.toSavedPlanFromHistory(
    coins: List<CoinAsset>,
    coinMarginedCalculationMode: CoinMarginedCalculationMode
): SavedPlan? {
    if (category != HistoryCategory.ProfitCalculation) return null
    val fields = sections.flatMap { it.fields }.associate { it.label to it.value }
    val symbol = fields["币种"] ?: return null
    val coinId = coins.firstOrNull { it.symbol.equals(symbol, ignoreCase = true) }?.id
        ?: coins.firstOrNull()?.id
        ?: return null
    val now = System.currentTimeMillis()
    val input = CalculationInput(
        side = if (fields["方向"] == "做空") PositionSide.Short else PositionSide.Long,
        marginMode = if (fields["保证金模式"] == "逐仓") MarginMode.Isolated else MarginMode.Cross,
        leverage = fields["杠杆"].toDecimalOrNull() ?: BigDecimal.ONE,
        margin = fields["保证金"].toDecimalOrNull(),
        quantity = fields["数量"].toDecimalOrNull(),
        entryPrice = fields["开仓价"].toDecimalOrNull(),
        exitPrice = fields["平仓价"].toDecimalOrNull(),
        openFeeRatePercent = fields["开仓费率"].toDecimalOrNull() ?: BigDecimal("0.05"),
        closeFeeRatePercent = fields["平仓费率"].toDecimalOrNull() ?: BigDecimal("0.05"),
        maintenanceMarginRatePercent = fields["维持保证金率"].toDecimalOrNull() ?: BigDecimal("0.5")
    )
    if (input.entryPrice == null || input.exitPrice == null || (input.margin == null && input.quantity == null)) {
        return null
    }
    return SavedPlan(
        id = "plan_$now",
        name = "${symbol} 历史方案",
        coinId = coinId,
        settlementMode = if (fields["合约模式"] == "币本位") SettlementMode.CoinMargined else SettlementMode.UsdtMargined,
        coinMarginedCalculationMode = coinMarginedCalculationMode,
        input = input,
        lastEditedAmountField = if (input.margin != null) AmountField.Margin else AmountField.Quantity,
        note = "来自历史记录：${title}",
        createdAt = now,
        updatedAt = now
    )
}

private fun String?.toDecimalOrNull(): BigDecimal? {
    val cleaned = this
        ?.replace(",", "")
        ?.replace(Regex("[^0-9.\\-]"), "")
        ?.takeIf { it.isNotBlank() && it != "-" && it != "." }
        ?: return null
    return runCatching { BigDecimal(cleaned) }.getOrNull()
}
