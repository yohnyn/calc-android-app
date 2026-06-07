package com.personal.futurescalculator.ui.staticpages

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.personal.futurescalculator.ui.SectionPanel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AboutScreen(priceUpdatedAt: Long?, onBack: () -> Unit) {
    val context = LocalContext.current
    val version = appVersion(context)
    StaticSettingsPageLayout(title = "关于 App", onBack = onBack) {
        SectionPanel(title = "仓位助手") {
            DisclaimerParagraph("版本：$version")
            DisclaimerParagraph("用于多币种 U 本位、币本位合约的收益、风险、目标止损、补仓和方案对比估算。")
            DisclaimerParagraph("价格数据来源：CoinGecko 公共 API。")
            DisclaimerParagraph("价格更新时间：${priceUpdatedAt?.let(::formatTimestamp) ?: "暂无缓存"}")
            DisclaimerParagraph("本工具仅提供计算和模拟功能，不构成投资建议。")
            DisclaimerParagraph("用户应自行承担交易风险。")
            StaticSoftOutlinedButton(
                onClick = { openUrl(context, GITHUB_REPOSITORY_URL) },
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.small
            ) {
                Text("打开 GitHub 项目")
            }
        }
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

private const val GITHUB_REPOSITORY_URL = "https://github.com/yohnyn/calc-android-app"