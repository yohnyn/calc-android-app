import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.personal.futurescalculator.model.CalculationInput
import com.personal.futurescalculator.model.CalculationResult
import com.personal.futurescalculator.model.ComparisonItem
import com.personal.futurescalculator.model.ComparisonResult
import com.personal.futurescalculator.model.AmountField
import com.personal.futurescalculator.model.PositionSide
import com.personal.futurescalculator.model.MarginMode
import com.personal.futurescalculator.domain.FuturesCalculator
import com.personal.futurescalculator.domain.ComparisonCalculator
import java.math.BigDecimal

data class CalculatorUiState(
    val input: CalculationInput = CalculationInput(),
    val lastEditedAmountField: AmountField = AmountField.Margin,
    val result: CalculationResult? = null,
    val comparisonItems: List<ComparisonItem> = emptyList(),
    val comparisonResults: List<ComparisonResult> = emptyList()
)

class CalculatorViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(CalculatorUiState())
    val uiState: StateFlow<CalculatorUiState> = _uiState.asStateFlow()
    
    private val futuresCalculator = FuturesCalculator()
    private val comparisonCalculator = ComparisonCalculator()
    
    fun updateInput(input: CalculationInput) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(input = input)
            calculateResult()
        }
    }
    
    fun updateLastEditedAmountField(field: AmountField) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(lastEditedAmountField = field)
        }
    }
    
    fun addComparisonItem(item: ComparisonItem) {
        viewModelScope.launch {
            val currentItems = _uiState.value.comparisonItems
            if (currentItems.size < 3) {
                _uiState.value = _uiState.value.copy(comparisonItems = currentItems + item)
                calculateComparisonResults()
            }
        }
    }
    
    fun removeComparisonItem(index: Int) {
        viewModelScope.launch {
            val currentItems = _uiState.value.comparisonItems
            if (index >= 0 && index < currentItems.size) {
                _uiState.value = _uiState.value.copy(comparisonItems = currentItems.filterIndexed { i, _ -> i != index })
                calculateComparisonResults()
            }
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
    
    fun copyCurrentToComparison() {
        viewModelScope.launch {
            val currentItems = _uiState.value.comparisonItems
            if (currentItems.size < 3) {
                val currentItem = ComparisonItem(
                    id = "item_${System.currentTimeMillis()}",
                    name = "方案 ${currentItems.size + 1}",
                    input = _uiState.value.input,
                    lastEditedAmountField = _uiState.value.lastEditedAmountField
                )
                _uiState.value = _uiState.value.copy(comparisonItems = currentItems + currentItem)
                calculateComparisonResults()
            }
        }
    }
    
    fun reset() {
        viewModelScope.launch {
            _uiState.value = CalculatorUiState()
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
}
