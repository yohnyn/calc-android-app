package com.personal.futurescalculator.ui.settings

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.personal.futurescalculator.model.CoinMarginedCalculationMode
import com.personal.futurescalculator.model.ThemeMode
import com.personal.futurescalculator.ui.SectionPanel
import com.personal.futurescalculator.ui.staticpages.AboutScreen
import com.personal.futurescalculator.ui.staticpages.DisclaimerScreen
import com.personal.futurescalculator.ui.staticpages.FeedbackScreen
import com.personal.futurescalculator.ui.staticpages.PrivacyPolicyScreen
import com.personal.futurescalculator.ui.theme.LossRed
import com.personal.futurescalculator.ui.theme.ProfitGreen

@Composable
fun SettingsScreen(
    pnlDisplayMode: PnlDisplayMode,
    onPnlDisplayModeChange: (PnlDisplayMode) -> Unit,
    themeMode: ThemeMode,
    onThemeModeChange: (ThemeMode) -> Unit,
    coinMarginedCalculationMode: CoinMarginedCalculationMode,
    onCoinMarginedCalculationModeChange: (CoinMarginedCalculationMode) -> Unit,
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
    AppTheme,
    CoinMarginedMode,
    PnlColor,
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
            SectionPanel(title = "外观") {
                SettingsMenuButton("App 主题", "选择界面明暗模式") { onOpenPage(SettingsPage.AppTheme) }
                SettingsMenuButton("盈利亏损配色", "设置盈亏结果颜色与图标显示") { onOpenPage(SettingsPage.PnlColor) }
            }
            SectionPanel(title = "币本位") {
                SettingsMenuButton("币本位默认模式", "选择币数量模式或反向合约模式") { onOpenPage(SettingsPage.CoinMarginedMode) }
            }
            SectionPanel(title = "反馈") {
                SettingsMenuButton("用户反馈", "提交建议并创建 GitHub Issue") { onOpenPage(SettingsPage.Feedback) }
            }
            SectionPanel(title = "关于") {
                SettingsMenuButton("关于 App", "版本信息、数据来源与项目地址") { onOpenPage(SettingsPage.About) }
                SettingsMenuButton("隐私政策", "查看设备信息与数据使用说明") { onOpenPage(SettingsPage.Privacy) }
                SettingsMenuButton("免责声明", "查看客观估算与责任边界") { onOpenPage(SettingsPage.Disclaimer) }
            }
            SettingsSoftOutlinedButton(
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
fun CoinMarginedModeDialog(
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
    SettingsSoftOutlinedButton(
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
            SettingsSoftOutlinedButton(
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
    SettingsSoftOutlinedButton(
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
private fun SettingsSoftOutlinedButton(
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
