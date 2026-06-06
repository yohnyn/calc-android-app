package com.personal.futurescalculator.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.personal.futurescalculator.data.CoinRepository
import com.personal.futurescalculator.data.UiPreferencesRepository
import com.personal.futurescalculator.data.HistoryRepository
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
import com.personal.futurescalculator.model.CoinMarginedResult
import com.personal.futurescalculator.model.HomeModule
import com.personal.futurescalculator.model.ComparisonItem
import com.personal.futurescalculator.model.ComparisonResult
import com.personal.futurescalculator.model.SettlementMode
import com.personal.futurescalculator.model.ThemeMode
import com.personal.futurescalculator.model.HistoryRecord
import com.personal.futurescalculator.model.HistoryCategory
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
    val coinMarginedResult: CoinMarginedResult? = null,
    val moduleOrder: List<HomeModule> = HomeModule.defaultOrder,
    val visibleModules: Set<HomeModule> = HomeModule.entries.toSet(),
    val averagingExpanded: Boolean = true,
    val themeMode: ThemeMode = ThemeMode.System,
    val downloadingIconIds: Set<String> = emptySet(),
    val iconDownloadNotice: String? = null,
    val historyRecords: List<HistoryRecord> = emptyList()
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

    init {
        val cached = coinRepository.loadCachedCoins()
        val custom = coinRepository.loadCustomCoins()
        _uiState.value = _uiState.value.copy(
            coins = cached + custom,
            selectedCoinId = coinRepository.loadSelectedCoinId(),
            priceUpdatedAt = coinRepository.loadUpdatedAt(),
            moduleOrder = uiPreferencesRepository.loadModuleOrder(),
            visibleModules = uiPreferencesRepository.loadVisibleModules(),
            averagingExpanded = uiPreferencesRepository.loadAveragingExpanded(),
            themeMode = uiPreferencesRepository.loadThemeMode(),
            historyRecords = historyRepository.load()
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
            val items = _uiState.value.comparisonItems
            val updated = if (items.any { it.id == item.id }) {
                items.map { if (it.id == item.id) item else it }
            } else {
                items + item
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
                moduleOrder = state.moduleOrder,
                visibleModules = state.visibleModules,
                averagingExpanded = state.averagingExpanded,
                themeMode = state.themeMode
                ,
                historyRecords = state.historyRecords
            )
        }
    }

    fun updateModuleOrder(order: List<HomeModule>) {
        uiPreferencesRepository.saveModuleOrder(order)
        _uiState.value = _uiState.value.copy(moduleOrder = order)
    }

    fun resetModuleOrder() {
        updateModuleOrder(HomeModule.defaultOrder)
    }

    fun setModuleVisible(module: HomeModule, visible: Boolean) {
        val updated = _uiState.value.visibleModules.toMutableSet().apply {
            if (visible) add(module) else remove(module)
        }
        uiPreferencesRepository.saveVisibleModules(updated)
        _uiState.value = _uiState.value.copy(visibleModules = updated)
    }

    fun setAveragingExpanded(expanded: Boolean) {
        uiPreferencesRepository.saveAveragingExpanded(expanded)
        _uiState.value = _uiState.value.copy(averagingExpanded = expanded)
    }

    fun updateThemeMode(mode: ThemeMode) {
        uiPreferencesRepository.saveThemeMode(mode)
        _uiState.value = _uiState.value.copy(themeMode = mode)
    }

    fun downloadCoinIcon(id: String) {
        if (id in _uiState.value.downloadingIconIds) return
        val symbol = _uiState.value.coins.firstOrNull { it.id == id }?.symbol ?: "币种"
        _uiState.value = _uiState.value.copy(downloadingIconIds = _uiState.value.downloadingIconIds + id)
        viewModelScope.launch(Dispatchers.IO) {
            val updated = coinRepository.downloadCoinIcon(id)
            _uiState.value = _uiState.value.copy(
                coins = if (updated == null) {
                    _uiState.value.coins
                } else {
                    _uiState.value.coins.map { if (it.id == id) updated else it }
                },
                downloadingIconIds = _uiState.value.downloadingIconIds - id,
                iconDownloadNotice = if (updated == null) "$symbol 图标下载失败，请稍后重试" else "$symbol 图标已下载"
            )
        }
    }

    fun consumeIconDownloadNotice() {
        _uiState.value = _uiState.value.copy(iconDownloadNotice = null)
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

    fun updateSettlementMode(mode: SettlementMode) {
        val source = if (mode == SettlementMode.UsdtMargined) {
            AmountField.Margin
        } else {
            AmountField.Quantity
        }
        _uiState.value = _uiState.value.copy(
            settlementMode = mode,
            input = normalizeAmountFields(_uiState.value.input, source),
            lastEditedAmountField = source
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
        _uiState.value = _uiState.value.copy(result = result)
    }
    
    private fun calculateComparisonResults() {
        val currentResult = _uiState.value.result
        val comparisonItems = _uiState.value.comparisonItems
        val comparisonResults = comparisonCalculator.compare(currentResult, comparisonItems)
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
        val selectedCoin = state.coins.firstOrNull { it.id == state.selectedCoinId }
        _uiState.value = state.copy(
            coinMarginedResult = coinMarginedCalculator.calculate(
                side = state.input.side,
                quantity = state.input.quantity,
                entryPrice = state.input.entryPrice,
                exitPrice = state.input.exitPrice,
                currentPrice = selectedCoin?.priceUsdt
            )
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
