# CURRENT_TASK

## 当前任务

继续拆分 `CalculatorScreen.kt`，本轮只拆分一个独立大功能模块：币种选择与币种图标相关 UI。

## 用户约束

- 不运行在线、远程或本地编译。
- 不运行 Gradle build / assemble / compile / install / test，也不运行 Android Studio 构建。
- 用户会在本地自行执行编译、构建和测试。
- 本轮不提交 git。
- 每次只拆分一个大功能模块。
- 不修改 UI，不影响主要计算功能。

## 本轮处理内容

- 新增 `android-app/app/src/main/java/com/personal/futurescalculator/ui/coin/CoinSelection.kt`。
- 从 `CalculatorScreen.kt` 迁出以下币种相关 UI：
  - `CoinMarketHeader(...)`
  - `CoinIcon(...)`
  - `CoinSelectorDialog(...)`
  - 私有 `CustomCoinDialog(...)`
- `CalculatorScreen.kt` 改为导入并调用：
  - `com.personal.futurescalculator.ui.coin.CoinMarketHeader`
  - `com.personal.futurescalculator.ui.coin.CoinIcon`
  - `com.personal.futurescalculator.ui.coin.CoinSelectorDialog`
- 保持原 UI 文案、布局、LazyColumn 列表策略、图标加载策略和自定义币种保存入口不变。
- 没有改动 ViewModel、仓储、计算公式或业务状态流。

## 本轮静态检查

已执行非编译静态检查：

- 搜索确认 `CalculatorScreen.kt` 不再保留旧的币种选择模块函数。
- 搜索确认 `CoinMarketHeader`、`CoinIcon`、`CoinSelectorDialog` 现在由 `ui.coin` 包提供并被主文件调用。
- 搜索确认 `CalculatorScreen.kt` 不再残留币图标加载专用的 `BitmapFactory`、`asImageBitmap`、`ContentScale`、`CoinRepository`、`Dispatchers`、`withContext` 等引用。
- 使用简单括号计数确认 `CalculatorScreen.kt` 与 `CoinSelection.kt` 结构平衡。
- `git diff --check` 无空白错误。

未执行：

- 未运行 Gradle。
- 未编译。
- 未运行测试。
- 未提交 git。

## 当前代码状态

- `CalculatorScreen.kt` 约 3149 行，已完成静态页、历史页、设置页、币种选择/图标模块拆分。
- 币种选择与图标 UI 位于 `android-app/app/src/main/java/com/personal/futurescalculator/ui/coin/CoinSelection.kt`。
- 历史页面位于 `android-app/app/src/main/java/com/personal/futurescalculator/ui/history/HistoryScreen.kt`。
- 设置页面位于 `android-app/app/src/main/java/com/personal/futurescalculator/ui/settings/SettingsScreen.kt`。
- 静态辅助页面位于 `android-app/app/src/main/java/com/personal/futurescalculator/ui/staticpages/`。

## 待用户本地验证

请用户本地验证：

- Android Studio 同步/编译是否通过。
- 首页当前币种卡片是否正常显示并打开币种选择弹窗。
- 币种选择弹窗搜索、选择、自定义币种新增和自定义币种删除是否保持原交互。
- 内置币图标、缓存图标、字母占位和非内置图标按需后台加载是否保持原行为。
- 收益方案对比和补仓中引用的币种图标是否正常显示。

## 下一步建议

下一轮如果继续拆分 `CalculatorScreen.kt`，应只选择一个新的独立大功能模块，例如：

- 收益方案对比编辑/结果展示。
- 补仓决策模拟。
- 主结果/币本位结果详情弹窗。

仍需保持：不改 UI、不改计算、不运行编译、不提交 git。
