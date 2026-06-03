import com.personal.futurescalculator.model.CalculationResult
import com.personal.futurescalculator.model.ComparisonItem
import com.personal.futurescalculator.model.ComparisonResult
import com.personal.futurescalculator.model.ComparisonRiskLevel
import com.personal.futurescalculator.domain.FuturesCalculator
import java.math.BigDecimal
import java.math.RoundingMode

class ComparisonCalculator {
    fun compare(
        current: CalculationResult?,
        items: List<ComparisonItem>
    ): List<ComparisonResult> {
        if (current == null) {
            return items.map { item ->
                ComparisonResult(
                    item = item,
                    result = null,
                    netPnlDiff = null,
                    roiDiffPercent = null,
                    requiredMarginDiff = null,
                    positionValueDiff = null,
                    liquidationDistanceDiffPercent = null,
                    riskLevel = ComparisonRiskLevel.Unknown
                )
            }
        }
        
        val futuresCalculator = FuturesCalculator()
        return items.map { item ->
            val result = futuresCalculator.calculate(item.input)
            
            val netPnlDiff = if (result?.netPnl != null && current.netPnl != null) {
                result.netPnl - current.netPnl
            } else {
                null
            }
            
            val roiDiffPercent = if (result?.roiPercent != null && current.roiPercent != null) {
                result.roiPercent - current.roiPercent
            } else {
                null
            }
            
            val requiredMarginDiff = if (result?.requiredMargin != null && current.requiredMargin != null) {
                result.requiredMargin - current.requiredMargin
            } else {
                null
            }
            
            val positionValueDiff = if (result?.positionValue != null && current.positionValue != null) {
                result.positionValue - current.positionValue
            } else {
                null
            }
            
            val liquidationDistanceDiffPercent = if (result?.distanceToLiquidationPercent != null && current.distanceToLiquidationPercent != null) {
                result.distanceToLiquidationPercent - current.distanceToLiquidationPercent
            } else {
                null
            }
            
            val riskLevel = calculateRiskLevel(current, result)
            
            ComparisonResult(
                item = item,
                result = result,
                netPnlDiff = netPnlDiff,
                roiDiffPercent = roiDiffPercent,
                requiredMarginDiff = requiredMarginDiff,
                positionValueDiff = positionValueDiff,
                liquidationDistanceDiffPercent = liquidationDistanceDiffPercent,
                riskLevel = riskLevel
            )
        }
    }
    
    private fun calculateRiskLevel(current: CalculationResult, compared: CalculationResult?): ComparisonRiskLevel {
        if (compared == null || current.distanceToLiquidationPercent == null || compared.distanceToLiquidationPercent == null) {
            return ComparisonRiskLevel.Unknown
        }
        
        val diff = compared.distanceToLiquidationPercent - current.distanceToLiquidationPercent
        
        return when {
            diff < BigDecimal("-5") -> ComparisonRiskLevel.HigherRisk
            diff > BigDecimal("5") -> ComparisonRiskLevel.LowerRisk
            else -> ComparisonRiskLevel.SimilarRisk
        }
    }
}
