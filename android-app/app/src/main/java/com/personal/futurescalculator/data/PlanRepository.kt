package com.personal.futurescalculator.data

import android.content.Context
import com.personal.futurescalculator.model.AmountField
import com.personal.futurescalculator.model.CalculationInput
import com.personal.futurescalculator.model.CoinMarginedCalculationMode
import com.personal.futurescalculator.model.MarginMode
import com.personal.futurescalculator.model.PositionSide
import com.personal.futurescalculator.model.SavedPlan
import com.personal.futurescalculator.model.SettlementMode
import java.math.BigDecimal
import org.json.JSONArray
import org.json.JSONObject

class PlanRepository(context: Context) {
    private val preferences = context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE)

    fun load(): List<SavedPlan> = runCatching {
        val array = JSONArray(preferences.getString(KEY_PLANS, "[]"))
        buildList {
            for (index in 0 until array.length()) {
                val item = array.getJSONObject(index)
                add(
                    SavedPlan(
                        id = item.getString("id"),
                        name = item.getString("name"),
                        coinId = item.optString("coin_id", "bitcoin"),
                        settlementMode = SettlementMode.valueOf(item.optString("settlement_mode", SettlementMode.UsdtMargined.name)),
                        coinMarginedCalculationMode = CoinMarginedCalculationMode.valueOf(
                            item.optString("coin_mode", CoinMarginedCalculationMode.CoinQuantity.name)
                        ),
                        input = parseInput(item.getJSONObject("input")),
                        lastEditedAmountField = AmountField.valueOf(item.optString("amount_field", AmountField.Margin.name)),
                        note = item.optString("note", ""),
                        createdAt = item.optLong("created_at", System.currentTimeMillis()),
                        updatedAt = item.optLong("updated_at", System.currentTimeMillis())
                    )
                )
            }
        }
    }.getOrDefault(emptyList())

    fun save(plans: List<SavedPlan>) {
        preferences.edit().putString(
            KEY_PLANS,
            JSONArray().apply {
                plans.forEach { plan ->
                    put(
                        JSONObject()
                            .put("id", plan.id)
                            .put("name", plan.name)
                            .put("coin_id", plan.coinId)
                            .put("settlement_mode", plan.settlementMode.name)
                            .put("coin_mode", plan.coinMarginedCalculationMode.name)
                            .put("amount_field", plan.lastEditedAmountField.name)
                            .put("note", plan.note)
                            .put("created_at", plan.createdAt)
                            .put("updated_at", plan.updatedAt)
                            .put("input", serializeInput(plan.input))
                    )
                }
            }.toString()
        ).apply()
    }

    private fun parseInput(json: JSONObject): CalculationInput = CalculationInput(
        side = PositionSide.valueOf(json.optString("side", PositionSide.Long.name)),
        marginMode = MarginMode.valueOf(json.optString("margin_mode", MarginMode.Cross.name)),
        leverage = json.optDecimal("leverage") ?: BigDecimal.ONE,
        margin = json.optDecimal("margin"),
        entryPrice = json.optDecimal("entry_price"),
        exitPrice = json.optDecimal("exit_price"),
        quantity = json.optDecimal("quantity"),
        openFeeRatePercent = json.optDecimal("open_fee_rate") ?: BigDecimal("0.05"),
        closeFeeRatePercent = json.optDecimal("close_fee_rate") ?: BigDecimal("0.05"),
        takeProfitPrice = json.optDecimal("take_profit_price"),
        stopLossPrice = json.optDecimal("stop_loss_price"),
        targetProfitAmount = json.optDecimal("target_profit_amount"),
        targetRoiPercent = json.optDecimal("target_roi_percent"),
        maxLossAmount = json.optDecimal("max_loss_amount"),
        maxLossRoiPercent = json.optDecimal("max_loss_roi_percent"),
        maintenanceMarginRatePercent = json.optDecimal("maintenance_margin_rate") ?: BigDecimal("0.5"),
        totalFunds = json.optDecimal("total_funds"),
        estimateLiquidation = json.optBoolean("estimate_liquidation", false),
        calculateMaxOpen = json.optBoolean("calculate_max_open", false)
    )

    private fun serializeInput(input: CalculationInput): JSONObject = JSONObject()
        .put("side", input.side.name)
        .put("margin_mode", input.marginMode.name)
        .putDecimal("leverage", input.leverage)
        .putDecimal("margin", input.margin)
        .putDecimal("entry_price", input.entryPrice)
        .putDecimal("exit_price", input.exitPrice)
        .putDecimal("quantity", input.quantity)
        .putDecimal("open_fee_rate", input.openFeeRatePercent)
        .putDecimal("close_fee_rate", input.closeFeeRatePercent)
        .putDecimal("take_profit_price", input.takeProfitPrice)
        .putDecimal("stop_loss_price", input.stopLossPrice)
        .putDecimal("target_profit_amount", input.targetProfitAmount)
        .putDecimal("target_roi_percent", input.targetRoiPercent)
        .putDecimal("max_loss_amount", input.maxLossAmount)
        .putDecimal("max_loss_roi_percent", input.maxLossRoiPercent)
        .putDecimal("maintenance_margin_rate", input.maintenanceMarginRatePercent)
        .putDecimal("total_funds", input.totalFunds)
        .put("estimate_liquidation", input.estimateLiquidation)
        .put("calculate_max_open", input.calculateMaxOpen)

    private fun JSONObject.optDecimal(key: String): BigDecimal? =
        optString(key).takeIf { it.isNotBlank() && it != "null" }?.let(::BigDecimal)

    private fun JSONObject.putDecimal(key: String, value: BigDecimal?): JSONObject =
        put(key, value?.toPlainString())

    private companion object {
        const val PREFERENCES = "saved_plans"
        const val KEY_PLANS = "plans"
    }
}
