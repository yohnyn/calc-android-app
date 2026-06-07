package com.personal.futurescalculator.ui.staticpages

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.personal.futurescalculator.ui.SectionPanel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun FeedbackScreen(
    feedbackText: String,
    onFeedbackChange: (String) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    StaticSettingsPageLayout(title = "用户反馈", onBack = onBack) {
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

@Suppress("DEPRECATION")
private fun appVersion(context: Context): String {
    return runCatching {
        context.packageManager.getPackageInfo(context.packageName, 0).versionName
    }.getOrNull() ?: "未知"
}

private const val GITHUB_REPOSITORY_URL = "https://github.com/yohnyn/calc-android-app"
private const val GITHUB_ISSUE_URL = "$GITHUB_REPOSITORY_URL/issues/new"