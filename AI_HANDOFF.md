# AI_HANDOFF

## 交接摘要

本仓库是 Android/Kotlin/Jetpack Compose 项目，应用领域为个人合约收益计算器。当前用户正在推进 `CalculatorScreen.kt` 拆分，要求每次只拆一个大功能模块，不改 UI，不影响主要计算功能，不运行编译/测试，不提交 git。

## 关键约束

- 必须遵守根目录 `AGENTS.md`：禁止运行任何构建、编译、安装、测试命令，包括 Gradle。
- 用户明确要求：不要在线编译，不要本地编译，不提交 git。
- 允许源码阅读、局部编辑、静态推理和非编译检查。
- 完成源码任务后要同步项目文档。

## 重要文件

- `AGENTS.md`：代理操作限制，尤其是禁止构建/编译/测试。
- `APP_DEVELOPMENT_SPEC.md`：产品范围、公式和验收规范。
- `PROJECT_STATUS.md`：长期项目状态、风险和下一步。
- `CURRENT_TASK.md`：最近一次任务记录。
- `AI_HANDOFF.md`：当前交接说明。
- `android-app/app/src/main/java/com/personal/futurescalculator/ui/CalculatorScreen.kt`：主计算页面，仍然较大，后续拆分从这里继续。
- `android-app/app/src/main/java/com/personal/futurescalculator/ui/coin/CoinSelection.kt`：已拆出的币种头部、币种图标、币种选择弹窗和自定义币种弹窗。
- `android-app/app/src/main/java/com/personal/futurescalculator/ui/staticpages/`：已拆出的用户反馈、关于、隐私政策、免责声明和打赏页面。
- `android-app/app/src/main/java/com/personal/futurescalculator/ui/history/HistoryScreen.kt`：已拆出的历史列表与历史详情页面。
- `android-app/app/src/main/java/com/personal/futurescalculator/ui/settings/SettingsScreen.kt`：已拆出的设置首页、设置子页面、模块排序/显隐、主题、盈亏配色和币本位模式弹窗。
- `android-app/app/src/main/java/com/personal/futurescalculator/ui/settings/PnlDisplayMode.kt`：已拆出的盈亏显示模式枚举。

## 最近完成内容

本轮完成上一轮未收尾的历史/设置拆分：

- `CalculatorScreen.kt` 已导入并调用 `ui.history.HistoryScreen`。
- `CalculatorScreen.kt` 已导入并调用 `ui.settings.SettingsScreen`。
- `CalculatorScreen.kt` 已使用 `ui.settings.PnlDisplayMode`。
- `CalculatorScreen.kt` 已使用 `ui.settings.CoinMarginedModeDialog`。
- `CalculatorScreen.kt` 中旧的内联历史页、设置页、币本位模式弹窗和私有 `PnlDisplayMode` 已删除。
- 旧 `formatTimestamp(...)` 残留已从主文件删除；历史文件有自己的时间格式化实现。
- 用户本地编译反馈后，已移除 `ui/settings/SettingsScreen.kt` 中错误的 `androidx.compose.ui.input.pointer.consume` 导入；模块排序拖拽仍保留 `change.consume()` 成员调用。
- 用户本地编译日志显示 `CalculatorScreen.kt` 中 `CoinMarginedModeOption` unresolved reference；已新增主文件本地私有 `ComparisonCoinMarginedModeOption(...)`，并替换对比方案编辑弹窗中的残留调用。
- 后续用户确认本地编译通过后，本轮又拆分了一个独立模块：`CoinMarketHeader`、`CoinIcon`、`CoinSelectorDialog` 与私有 `CustomCoinDialog` 已迁入 `ui/coin/CoinSelection.kt`；`CalculatorScreen.kt` 通过 `ui.coin` 公开函数继续调用，UI 和图标加载策略保持不变。

本轮没有改动计算公式、ViewModel 计算链路或 UI 文案。

## 静态检查结果

已做非编译检查：

- 搜索确认 `CalculatorScreen.kt` 不再残留旧的 `HistoryScreen`、`SettingsScreen`、`CoinMarginedModeDialog`、私有 `PnlDisplayMode` 等内联实现。
- 搜索确认主文件调用已指向 `ui.history` / `ui.settings` 包。
- 搜索确认设置页中错误的 `androidx.compose.ui.input.pointer.consume` 导入已移除，`change.consume()` 调用保留。
- 搜索确认 `CalculatorScreen.kt` 不再引用设置页私有 `CoinMarginedModeOption`。
- 搜索确认 `CalculatorScreen.kt` 不再保留旧币种选择模块函数，`ui.coin` 公开入口已被主文件调用。
- 简单括号计数确认 `CalculatorScreen.kt`、`SettingsScreen.kt`、`HistoryScreen.kt`、`CoinSelection.kt` 结构平衡。
- `git diff --check` 无空白错误。

未做：

- 未运行 Gradle。
- 未编译。
- 未运行测试。
- 未提交 git。

## 仍需关注的代码风险

- `CalculatorScreen.kt` 仍约 3149 行，后续维护风险仍高。
- 最近拆分后的历史、设置、静态页面和币种选择模块尚未由用户本地编译/真机验收。
- 如果用户再次反馈编译错误，优先让用户贴出完整错误日志；当前上下文中已移除 `consume` 错误导入，并修复 `CalculatorScreen.kt` 对 `CoinMarginedModeOption` 的残留私有 helper 调用。
- 收益方案对比 UI 已可展示/选择结算模式和币本位计算方式，但此前静态检查确认 `ComparisonCalculator.kt` 仍只调用 `FuturesCalculator()`，尚未完整贯通币本位对比计算链路。
- 仓库仍跟踪 `futures-calculator/android-app/` 下的早期遗留 Kotlin 文件，处理前不要随意删除。

## 建议下一步

继续拆分 `CalculatorScreen.kt` 时，一次只选择一个大功能模块，并保持 UI/文案/计算行为不变。可选方向：

- 收益方案对比编辑/结果展示。
- 补仓决策模拟。
- 主结果/币本位结果详情弹窗。

每轮完成后只做静态检查并同步文档，构建、编译和测试交给用户本地执行。
