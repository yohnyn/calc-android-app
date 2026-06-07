# AI_HANDOFF

## 交接摘要

本仓库是一个 Android/Kotlin/Jetpack Compose 项目，应用领域为期货仓位计算器。用户明确要求：不要执行任何终端命令、不要运行 Gradle、不要使用 shell，只允许通过文件编辑工具维护项目状态文档。

## 关键约束

- 适用根目录 `AGENTS.md`：禁止运行任何构建、编译、安装、测试命令，包括 Gradle。
- 不应使用终端命令进行验证。
- 如需验证，只能建议用户在本地自行运行。

## 重要文件

- `PROJECT_STATUS.md`：项目总体状态。
- `CURRENT_TASK.md`：当前任务、约束、待验证事项。
- `AI_HANDOFF.md`：给下一位 AI/协作者的交接说明。
- `android-app/app/src/main/java/com/personal/futurescalculator/model/HomeModule.kt`：首页模块枚举与默认排序/可配置可见性规则。
- `android-app/app/src/main/java/com/personal/futurescalculator/model/ComparisonItem.kt`：对比方案数据模型，包含结算模式与币本位计算方式字段。
- `android-app/app/src/main/java/com/personal/futurescalculator/data/UiPreferencesRepository.kt`：UI 偏好持久化，包括模块排序与隐藏模块集合。
- `android-app/app/src/main/java/com/personal/futurescalculator/viewmodel/CalculatorViewModel.kt`：UI 状态与模块显示更新逻辑。
- `android-app/app/src/main/java/com/personal/futurescalculator/domain/ComparisonCalculator.kt`：收益方案对比计算逻辑，需重点确认是否完整支持币本位对比方案。
- `android-app/app/src/main/java/com/personal/futurescalculator/model/CoinAsset.kt`：币种模型，包含 `iconUrl`、本地缓存 `iconPath` 与内置图标 `iconResourceName`。
- `android-app/app/src/main/java/com/personal/futurescalculator/data/CoinRepository.kt`：币价获取、自定义币、图标资源映射与按需图标缓存。
- `android-app/app/src/main/java/com/personal/futurescalculator/ui/CalculatorScreen.kt`：Compose UI，包括首页、设置页、模块显示管理页面和 `CoinIcon` 图标渲染。

## 最近代码状态

### 收益方案对比 UI

最近一次源码修改集中在 `CalculatorScreen.kt`：

- `buildComparisonSchemes(...)` 为主方案传入 `uiState.settlementMode` 与 `uiState.coinMarginedCalculationMode`。
- `ComparisonSchemeView`、`RankedComparisonScheme` 增加/携带 `settlementMode` 与 `coinMarginedCalculationMode`。
- 对比方案列表卡片展示币种、结算模式、币本位计算方式和方向。
- `ComparisonSchemeEditorDialog` 中新增 U 本位/币本位切换；当选择币本位时展示“币数量 / 反向合约”计算方式选项。
- 对比结果详情页与对比历史快照会展示结算模式和币本位计算方式。
- 未运行编译或测试。需要用户本地验证是否存在 Compose 调用、数据模型或计算链路问题。

特别注意：静态检查已确认 `ComparisonCalculator.kt` 当前仍只调用 `FuturesCalculator()`，没有根据 `ComparisonItem.settlementMode` 或 `coinMarginedCalculationMode` 切换币本位计算。因此当前文档只确认 UI/展示/保存路径已被调整，不能把收益方案对比视为已支持币本位真实计算。下一位 AI 如要继续修复，应在不运行 Gradle 的前提下优先处理该发布风险。

### 首页模块可见性

首页模块可见性管理被设计为：

- 核心模块始终显示：`Position`、`TargetStop`、`Result`。
- 可配置显示模块：`Comparison`、`Averaging`、`History`。
- `HomeModule.defaultOrder` 不包含 `History`，因为历史记录目前位于底部按钮，不在首页可拖拽模块列表中。
- `UiPreferencesRepository.saveVisibleModules()` 只持久化可配置模块的隐藏状态，并强制核心模块始终可见。
- `CalculatorViewModel.resetModuleVisibility()` 将所有模块恢复为可见。

### 支持作者入口

用户要求首页底部继续保留“支持作者”入口。先前低调版用户认为太低调，最新要求是更显眼一些，但仍不能像广告或博彩软件。当前 `CalculatorScreen.kt` 的 `SupportAuthorCard` 已调整为：

- `Card` 使用 `MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.30f)` 背景。
- 低透明度 `primary` 边框。
- 左侧 34dp 圆形咖啡图标。
- 标题：`支持作者`。
- 副标题：`如果这个工具帮助到了你，\n欢迎支持后续更新。`
- 右侧显示 `查看` 操作提示。
- 未使用红色、绿色、金色作为入口强调色。

### 币图标加载方式

最近一次源码修改改为优先解决启动和弹窗卡顿。当前实现要点：

- `CoinAsset` 新增 `iconResourceName`。
- `CoinRepository.fetchTopCoins()` 不再同步批量下载图标，只解析市场数据并绑定内置图标或已有缓存。
- `CoinRepository.BUILT_IN_ICON_RESOURCES` 将 BTC、ETH、BNB、SOL、XRP、DOGE、ADA、TRX、LINK、AVAX、TON、SUI、DOT、SHIB、BCH、LTC、NEAR、APT、HBAR、PEPE 映射到 `res/drawable/coin_*.xml`。
- `CoinRepository.loadIconForCoin()` 用于非内置币按需下载图标，失败 ID 会记录到 `KEY_FAILED_ICON_IDS`，避免重复失败请求。
- `CalculatorScreen.CoinIcon()` 优先使用 `painterResource(iconResourceName)`，其次使用本地 bitmap 缓存，最后显示字母占位，并在 IO 线程尝试按需加载非内置币图标。
- `CoinSelectorDialog` 与 `ComparisonCoinSelectorDialog` 当前保留 `LazyColumn` / `items`，以避免一次性组合全部币种行。
- 注意：这覆盖了上一轮“直接组合全部过滤币种项”的做法；当前以性能优化后的 `LazyColumn` + 内置图标/按需图标加载方案为准。

## 注意事项

- 严格遵守用户当前任务约束：不要执行终端命令、不要运行 Gradle、不要使用 shell。只能使用文件读取/编辑工具。
- 历史记录目前仍通过首页底部“历史”按钮打开；若要让隐藏历史真正影响底部按钮显示，还需要在 `CalculatorScreen` 底部按钮区域依据 `HomeModule.History in uiState.visibleModules` 条件渲染。
- 如果未来希望“历史记录”也出现在首页模块拖拽排序中，需要将 `History` 纳入 `HomeModule.defaultOrder`，并在首页模块循环中实现对应 UI，而不只是 `Unit`。
- 当前未执行编译或测试，需用户本地验证。
- 本轮文档与静态检查只使用文件读取/编辑工具，没有执行终端、shell、Gradle、编译或测试。
- `CalculatorScreen.kt` 文件体积很大，后续维护风险高；拆分前需用户明确允许。

## 建议下一步

1. 发布前先决定收益方案对比的币本位入口策略：若不补齐计算链路，应隐藏编辑弹窗中的 U 本位/币本位切换与币本位计算方式选择，避免 UI 误导。
2. 如决定补齐链路，应静态修改 `CalculatorViewModel.kt` / `ComparisonCalculator.kt`，让 `ComparisonItem.settlementMode` 和 `coinMarginedCalculationMode` 真正参与计算；仍不要运行 Gradle。
3. 用户本地编译，确认新增参数与 Compose 调用链无编译错误。
4. 手动检查币种图标性能：App 启动不应等待全部图标下载，币种选择弹窗和对比方案币种选择弹窗应快速打开，列表能滚动，搜索过滤正常，热门币内置图标正常显示。
5. 手动检查首页底部“支持作者”入口是否更显眼但不广告化，且点击仍能进入支持作者页面。
6. 手动测试收益方案对比：新增方案、编辑方案、方案列表展示、对比结果详情、历史快照。
7. 手动测试设置页“模块显示管理”。
8. 决定“历史记录”隐藏开关是否应控制底部历史按钮显示。
