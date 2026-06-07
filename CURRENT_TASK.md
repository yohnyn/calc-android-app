# CURRENT_TASK

## 当前任务

创建并持续维护以下项目交接与状态文档：

- `PROJECT_STATUS.md`
- `CURRENT_TASK.md`
- `AI_HANDOFF.md`

## 用户约束

- 不执行任何终端命令。
- 不运行 Gradle。
- 不使用 shell。
- 只使用文件编辑工具。

## 本轮处理内容

- 读取并维护 `PROJECT_STATUS.md`、`CURRENT_TASK.md`、`AI_HANDOFF.md`。
- 记录用户明确约束：不执行终端命令、不运行 Gradle、不使用 shell、只使用文件编辑工具。
- 同步最近源码状态：`CalculatorScreen.kt` 中收益方案对比 UI 已增加/展示结算模式与币本位计算方式相关字段。
- 同步静态检查结论：`ComparisonCalculator.kt` 仍只调用 `FuturesCalculator()`，没有根据 `ComparisonItem.settlementMode` 或 `coinMarginedCalculationMode` 执行币本位计算。
- 按用户新反馈优化币图标加载性能：避免启动时同步批量下载图标，热门币使用内置 drawable，非内置币按需后台加载缓存图标。
- 按用户新反馈再次调整首页底部“支持作者”入口：比上一版更显眼，但仍避免红色、绿色、金色、广告化或博彩化观感。
- 按用户新反馈执行一次低风险静态 UI 拆分：将用户反馈、关于 App、隐私政策、免责声明和打赏页面迁移到 `ui/staticpages/` 包。
- 记录当前 Android App 代码状态、未验证事项与下一步建议。

## 本轮静态页面拆分

- 新增目录：`android-app/app/src/main/java/com/personal/futurescalculator/ui/staticpages/`。
- 新增 `StaticPageComponents.kt`，包含静态页面共用布局、柔和描边按钮和说明段落组件。
- 新增 `FeedbackScreen.kt`，保留用户反馈输入、GitHub Issue 预填、设备信息和 Toast 失败提示。
- 新增 `AboutScreen.kt`，保留版本信息、价格更新时间、CoinGecko 数据来源和 GitHub 项目入口。
- 新增 `PrivacyPolicyScreen.kt` 与 `DisclaimerScreen.kt`，保留原隐私和风险说明文案。
- 新增 `DonationScreen.kt`，保留支持作者页面 UI、复制收款地址和系统返回处理。
- `CalculatorScreen.kt` 改为导入以上静态页面函数；已移除这些页面及其专用 helper 的原内联实现。
- `CalculatorScreen.kt` 仍保留历史记录所需的 `formatTimestamp(...)`，因为历史列表和详情仍在原文件内使用它。

## 本轮 UI 调整

支持作者入口位于首页底部，当前调整为：

- 保留 `SupportAuthorCard(onClick = { showDonation = true })`。
- Card 使用 `MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.30f)` 背景。
- 边框使用低透明度 `primary`。
- 左侧增加 34dp 圆形咖啡图标区域。
- 标题为：`支持作者`。
- 副标题为：`如果这个工具帮助到了你，\n欢迎支持后续更新。`
- 右侧增加 `查看` 操作提示。
- 标题使用 `titleMedium` 和 `FontWeight.SemiBold`，副标题使用 `bodySmall`。
- 视觉比上一版更明显，但仍应避免广告化或博彩化。

## 本轮图标加载调整

- `CoinAsset` 已增加 `iconResourceName` 字段，用于记录内置 drawable 图标资源名。
- `CoinRepository.fetchTopCoins()` 已取消 `parseMarketResponse(body).map(::downloadIcon)` 这类启动期同步批量下载图标流程。
- `CoinRepository.parseMarketResponse()` 会为已内置的热门币绑定 `coin_btc`、`coin_eth` 等资源名。
- `CoinRepository.withAvailableLocalIcon()` 优先使用内置图标；无内置图标时只绑定已存在的本地缓存文件。
- `CoinRepository.loadIconForCoin()` 用于非内置、非自定义币的后台按需图标加载，并记录失败 ID，避免反复请求失败资源。
- `CalculatorScreen.CoinIcon()` 优先渲染 `painterResource(iconResourceName)`，其次渲染本地缓存 bitmap，最后显示字母占位；当非内置币暂无缓存图标时，会通过 `LaunchedEffect` 在 IO 线程尝试加载。
- `CoinSelectorDialog` 与 `ComparisonCoinSelectorDialog` 当前保留 `LazyColumn` + `items`，避免打开弹窗时一次性组合全部币种项导致卡顿。
- 当前含义：启动只拉取价格和基础币种数据，不再等待 100 个图标下载；热门币图标随包立即显示，长尾币图标可按需补全。

## 进行中代码上下文

最近涉及的功能域是收益方案对比 UI 与参数展示：

- `CalculatorScreen.kt` 的 `buildComparisonSchemes(...)` 已为主方案传入：
  - `uiState.settlementMode`
  - `uiState.coinMarginedCalculationMode`
- `ComparisonSchemeView` 和 `RankedComparisonScheme` 包含：
  - `settlementMode`
  - `coinMarginedCalculationMode`
- 方案列表卡片会展示：币种、U 本位/币本位、币本位计算方式、方向。
- 方案编辑弹窗会提供：
  - U 本位 / 币本位切换
  - 币本位计算方式选择（币数量 / 反向合约）
- 对比结果详情与历史快照会展示结算模式及币本位计算方式。
- 本轮未运行编译。静态检查已确认 `ComparisonCalculator.kt` 尚未对币本位对比计算完整贯通；当前应视为 UI/保存/展示层面的增强。
- 发布前建议二选一：
  - 隐藏收益方案对比编辑里的 U 本位/币本位切换和币本位计算方式入口，保持对比方案仅按 U 本位计算；或
  - 补齐 ViewModel/ComparisonCalculator 的币本位对比计算链路，并补充用户本地验证。

---

历史相关上下文：

之前涉及的功能域是首页模块显示管理：

- `HomeModule` 增加了 `History`，用于让“历史记录”参与可见性配置。
- `HomeModule.configurableVisibility` 限定可被用户隐藏/显示的模块：
  - 收益方案对比
  - 补仓决策模拟
  - 历史记录
- 核心模块应始终显示：
  - 仓位参数
  - 目标与止损
  - 计算结果
- 设置页“模块显示管理”应只展示上述三个可配置模块，并提供“恢复默认显示”。

## 待用户本地验证

由于项目规则禁止我运行构建或测试，请用户本地验证：

- Android Studio 同步/编译是否通过。
- 设置页 → 用户反馈、关于 App、隐私政策、免责声明是否都能正常打开、返回，并保持原文案与交互。
- 首页底部“支持作者”入口是否仍能打开打赏页面；复制收款地址 Toast 是否正常。
- App 启动是否明显减少卡顿；CoinGecko 价格加载不应被前 100 币图标下载阻塞。
- 币种选择弹窗和对比方案币种选择弹窗是否能快速打开、正常滚动，热门币内置图标是否正常显示。
- 非内置币是否在没有缓存时显示字母占位，且在网络可用时可后台补全缓存图标。
- 首页底部“支持作者”入口是否比上一版更显眼，但仍不应出现红色、绿色、金色、广告化或博彩化视觉。
- 如果发布前隐藏币本位对比入口：收益方案对比新增/编辑弹窗应只保留当前可真实计算的 U 本位输入路径。
- 如果发布前保留币本位对比入口：必须先补齐 `CalculatorViewModel.kt` 与 `ComparisonCalculator.kt` 计算链路，再验证币本位实际计算结果是否符合预期。
- 收益方案对比列表、结果详情、历史快照展示的结算模式和币本位计算方式，目前不代表计算链路已贯通。
- 设置页 → 模块显示管理是否只显示三个可配置项。
- 关闭“收益方案对比”或“补仓决策模拟”后，首页对应模块是否隐藏。
- 恢复默认显示后，三个可配置模块是否全部重新显示。

## 当前状态

- 文档维护已更新到当前静态检查结论、币种图标加载性能优化、支持作者入口 UI 调整与静态页面拆分。
- 未执行任何终端命令、shell、Gradle、编译或测试。
- 等待用户本地编译/运行反馈。
