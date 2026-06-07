package com.personal.futurescalculator.ui.staticpages

import androidx.compose.runtime.Composable
import com.personal.futurescalculator.ui.SectionPanel

@Composable
fun DisclaimerScreen(onBack: () -> Unit) {
    StaticSettingsPageLayout(title = "免责声明", onBack = onBack) {
        SectionPanel(title = "风险说明") {
            DisclaimerParagraph("本工具仅用于收益、风险和补仓价格估算，仅供参考，不构成任何投资建议。")
            DisclaimerParagraph("强平价采用简化公式估算。真实强平价会受到交易所规则、钱包余额、阶梯维持保证金、其他仓位、手续费和资金费率等因素影响。")
            DisclaimerParagraph("数字资产交易具有高风险，请根据自身情况独立判断，并以交易所实际数据为准。")
        }
    }
}