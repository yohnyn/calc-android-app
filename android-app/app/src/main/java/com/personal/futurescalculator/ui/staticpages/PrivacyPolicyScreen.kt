package com.personal.futurescalculator.ui.staticpages

import androidx.compose.runtime.Composable
import com.personal.futurescalculator.ui.SectionPanel

@Composable
fun PrivacyPolicyScreen(onBack: () -> Unit) {
    StaticSettingsPageLayout(title = "隐私政策", onBack = onBack) {
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