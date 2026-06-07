# PROJECT_STATUS

> 维护说明：本文档记录项目整体状态；当前正在处理的单次任务见 `CURRENT_TASK.md`；给下一位 AI/开发者的接力上下文见 `AI_HANDOFF.md`。

## 项目名称

合约收益计算器（CalcApp）

---

## 项目目标

开发一个个人使用的 Android 合约收益计算器；公开币价可联网获取，所有金融计算在设备本地完成。

特点：

- 不连接或登录交易所账户
- 不下单
- 不保存用户敏感信息

主要用于：

- 收益计算
- ROI计算
- 保证金计算
- 强平价估算
- 多方案对比分析

---

## 当前开发状态

当前源码已经从第一版计算器扩展为较完整的个人合约计算工具，包含核心计算、方案对比、补仓模拟、币种与价格、历史记录、设置及辅助页面。

项目当前进入：

【功能验收 + 本地编译/真机验证阶段】

当前主工程为根目录下的 `android-app/`。仓库仍跟踪少量早期遗留的 `futures-calculator/android-app/` 源文件，后续需要确认并清理，但不得在未确认前删除。

---

## 已完成功能

### 核心计算

源码已实现：

- 做多做空
- 全仓逐仓
- 杠杆计算
- 保证金计算
- 仓位价值计算
- 手续费计算
- ROI计算
- 强平价估算
- 强平距离计算
- 目标收益/目标 ROI 反推目标价
- 最大亏损金额/ROI 反推止损价
- U 本位与币本位结果计算
- 输入范围校验与 BigDecimal 计算规则

核心文件：

- FuturesCalculator.kt
- CoinMarginedCalculator.kt
- NumberRules.kt

---

### 对比分析

源码已实现：

- 主方案与最多两个对比方案
- 空白方案新增、编辑、删除和选择
- 净盈亏差值、方案排序和最优方案展示
- 方案 3 相对方案 2 的差值展示
- 对比结果历史快照

核心文件：

- ComparisonCalculator.kt

---

### 状态管理

源码已实现：

- ViewModel状态管理
- 实时计算
- 对比方案管理
- 币种选择和公开价格刷新
- 历史记录管理
- 首页模块排序、显隐和主题偏好
- 补仓计算与补仓决策模拟状态

核心文件：

- CalculatorViewModel.kt

---

### UI

源码已实现：

- 主计算页面与输入/结果区域
- 方案对比编辑和集中结果展示
- 补仓决策模拟
- 历史记录列表与详情
- 设置首页、模块排序与显隐
- 盈亏配色、主题模式
- 用户反馈、关于、隐私政策、免责声明和打赏页面
- 币种选择、自定义币种和币本位结果展示
- 结果、补仓和方案对比的滚动及展开/折叠交互

核心文件：

- CalculatorScreen.kt
- CalculatorComponents.kt
- Theme.kt
- ProfitLossPalette.kt

---

### 本地数据、网络与偏好

源码已实现：

- CoinGecko 市值币种与公开价格获取
- 币种、自定义币种及图标本地缓存；热门币使用内置 drawable 图标，非内置图标按需后台缓存
- 计算历史本地保存
- 首页模块顺序、显隐、补仓展开状态和主题模式持久化
- Android Manifest 已声明网络权限
- 自定义币种和对比方案名称使用统一紧凑输入框
- 结果、补仓和收益对比详情弹窗展示完整可滚动内容，不再重复跳回首页
- 对比方案已在 UI 层开放结算模式显示/编辑入口：方案卡片、编辑弹窗、对比详情和历史快照会展示 U 本位/币本位及币本位计算方式；保证金与币数量仍作为二选一输入
- 首页模块排序提供可感知的跟手拖拽动画和震动反馈
- 设置、历史和支持作者等整页页面支持系统返回键及侧边返回手势
- 主结果、币本位结果、补仓结果和收益对比采用首页简略展示与详细弹窗模式
- 主结果和币本位结果由键盘“完成/回车”明确提交，输入过程中不自动弹窗
- 主结果与补仓详情同时展示完整输入参数和计算结果
- 补仓方案填入同步币种单位；复制战绩支持先选择具体方案
- U 本位全仓总资金输入位于仓位参数区域
- 最近 UI 统一调整已覆盖首页展开/收起、选择类弹窗、表单类弹窗、提示类弹窗、收益对比列表/详情等视觉样式
- 币种选择弹窗与对比方案币种选择弹窗保留 `LazyColumn` 可见项组合策略，避免一次性组合全部币种行；图标加载改为内置资源优先、已有缓存优先、非内置图标后台按需加载，避免启动时同步批量下载图标造成卡顿
- 首页底部“支持作者”入口保留，并调整为更显眼但克制的主题色 Card：使用低透明度 primaryContainer 背景、primary 细边框、圆形咖啡图标、标题/副标题与“查看”操作提示
- 最近一次源码变更涉及 `CalculatorScreen.kt` 的收益方案对比 UI：主方案视图传入结算模式，对比方案编辑弹窗可切换 U 本位/币本位，列表/详情/历史快照展示结算模式和币本位计算方式；尚未编译验证
- 2026-06-07 静态检查确认：`ComparisonCalculator.kt` 当前仍只调用 `FuturesCalculator()`，尚未根据 `ComparisonItem.settlementMode` 或 `coinMarginedCalculationMode` 切换到币本位计算链路。因此收益方案对比中的币本位字段目前应视为 UI/展示/保存层状态，不能视为已完成币本位对比计算支持。
- 2026-06-07 静态页面拆分：用户反馈、关于 App、隐私政策、免责声明和打赏页面已从 `CalculatorScreen.kt` 拆到 `android-app/app/src/main/java/com/personal/futurescalculator/ui/staticpages/`；`CalculatorScreen.kt` 仍保留设置主页、模块排序/显隐、主题、盈亏配色、币本位模式、历史记录等非静态/状态相关页面。
- 2026-06-07 历史与设置页面拆分完成：`CalculatorScreen.kt` 已切换为导入 `ui/history/HistoryScreen.kt`、`ui/settings/SettingsScreen.kt`、`ui/settings/PnlDisplayMode.kt` 与 `CoinMarginedModeDialog`；旧的内联历史页、设置页、币本位模式弹窗和私有 `PnlDisplayMode` 已从主文件移除。该轮仅做静态引用与空白检查，未运行 Gradle、编译或测试。
- 2026-06-07 用户本地编译反馈后修复：移除已拆出的 `ui/settings/SettingsScreen.kt` 中错误的 `androidx.compose.ui.input.pointer.consume` 导入，保留模块排序拖拽里的 `change.consume()` 成员调用；同时复查主文件导入、公开入口和括号结构。该轮仍未运行 Gradle、编译或测试。
- 2026-06-07 用户本地编译反馈后继续修复：`CalculatorScreen.kt` 对比方案编辑弹窗仍调用已拆入设置页的私有 `CoinMarginedModeOption`，导致 unresolved reference；已改为主文件本地私有 `ComparisonCoinMarginedModeOption`，保持原 UI 样式和交互。该轮仍未运行 Gradle、编译或测试。
- 2026-06-07 币种选择模块拆分完成：`CoinMarketHeader`、`CoinIcon`、`CoinSelectorDialog` 与私有 `CustomCoinDialog` 已从 `CalculatorScreen.kt` 拆到 `android-app/app/src/main/java/com/personal/futurescalculator/ui/coin/CoinSelection.kt`；主文件改为导入 `ui.coin` 公开入口，原 UI、图标加载策略和自定义币种交互保持不变。该轮仅做非编译静态检查，未运行 Gradle、编译或测试。

当前仓储使用 `SharedPreferences`。

核心文件：

- CoinRepository.kt
- HistoryRepository.kt
- UiPreferencesRepository.kt
- CoinAsset.kt
- ThemeMode.kt

---

## 当前已知问题

待确认或验证：

- 最近模块显示管理已调整为仅管理“收益方案对比 / 补仓决策模拟 / 历史记录”，核心模块保持始终显示；尚未本地编译验证
- 最新源码尚未由用户本地完成编译、测试和真机验证；AI 未运行 Gradle、编译或测试
- 最近一次源码变更为 `CalculatorScreen.kt` 收益方案对比参数展示/编辑调整，尚未由用户本地编译或真机验收
- 最近一次币种图标加载性能优化尚未由用户本地编译或真机验收；重点验证启动速度、币种弹窗打开速度、内置图标显示和非内置图标后台补全
- 最近一次支持作者入口样式已调整为更显眼的主题色 Card，尚未由用户本地编译或真机验收
- 最近一次静态页面、历史页面和设置页面拆分尚未由用户本地编译或真机验收；重点确认设置页中的用户反馈、关于、隐私政策、免责声明、模块排序/显隐、主题/盈亏配色、币本位模式，以及首页底部支持作者入口和历史列表/详情均可正常打开和返回
- 最近一次币种选择与图标 UI 拆分尚未由用户本地编译或真机验收；重点确认首页币种卡片、币种选择弹窗搜索/选择、自定义币种新增/删除、内置图标/缓存图标/字母占位和非内置图标按需加载
- 用户本地编译曾反馈 `CalculatorScreen.kt` 拆分后存在错误，本轮已按日志修复对设置页私有 `CoinMarginedModeOption` 的残留调用，并移除设置页拖拽 `consume` 的错误导入；仍需用户再次本地编译确认是否还有其他错误日志
- 对比方案 UI 已可选择币本位和币本位计算方式，但静态检查已确认 `ComparisonCalculator.kt` 尚未使用这些字段进行币本位计算；当前属于展示/保存层面的增强，发布前建议隐藏入口或补齐计算链路
- 本轮拆分只执行源码阅读、局部编辑和非编译静态检查；未运行 Gradle、编译或测试，也未提交 git
- UI 是否完整符合 `APP_DEVELOPMENT_SPEC.md`，尤其是竖屏布局、深色模式和交互细节
- 数值边界、ROI、币本位公式及强平估算是否符合预期
- 仓库仍跟踪 `futures-calculator/android-app/` 下的 3 个早期遗留 Kotlin 文件，存在误改风险
- `CalculatorScreen.kt` 仍约 3149 行，后续维护风险较高；后续拆分应继续遵循“每次只拆一个大功能模块、保持 UI 与计算行为不变”的节奏
- 当前网络与本地持久化实现需要在真机上验证失败降级、隐私说明和数据兼容性

---

## 文档自动维护规则

每次完成任何开发、修复、提交、文档或分析任务后，即使用户没有额外提醒，也必须自动检查是否需要同步项目文档。

更新分工：

- 产品范围、公式或验收规则变化：更新 `APP_DEVELOPMENT_SPEC.md`
- 长期状态、已完成内容、风险或下一步变化：更新 `PROJECT_STATUS.md`
- 当前任务目标、执行结果和剩余待办变化：更新 `CURRENT_TASK.md`
- 交接入口、关键上下文、限制或推荐下一步变化：更新 `AI_HANDOFF.md`

如果不确定应该更新哪个文档，至少检查 `PROJECT_STATUS.md`、`CURRENT_TASK.md`、`AI_HANDOFF.md`，并只修改确实需要变化的部分。

---

## 下一步任务

优先级 P0：

- 由用户本地执行 Gradle 同步、编译和测试，并反馈结果
- 根据用户反馈修复编译或运行问题
- 用户本地进行真机验证，重点检查主计算、方案对比、补仓、历史和设置
- 重点验证币种图标加载性能：App 启动不应被前 100 币图标下载阻塞，币种选择弹窗应快速打开，热门币应直接显示内置图标，非内置币可按需显示缓存图标或字母占位
- 重点处理收益方案对比新增/编辑流程：发布前如不补齐币本位对比计算链路，应隐藏 U 本位/币本位切换和币本位计算方式选择入口，避免误导用户

优先级 P1：

- 对照规范逐项验收输入校验、错误提示和空状态
- 验证 CoinGecko 请求失败、无网络和缓存回退
- 验证历史记录、主题与模块偏好持久化
- 检查反馈、隐私政策、免责声明和打赏文案
- 确认并处理 `futures-calculator/android-app/` 遗留文件

优先级 P2：

- 在用户允许后评估拆分超大的 `CalculatorScreen.kt`
- 继续按低风险方式拆分 `CalculatorScreen.kt`：下一步应选择一个独立大功能模块，例如对比方案、补仓决策模拟或结果详情弹窗；每次只迁移一组页面/组件并保持文案、UI 与计算行为不变
- 继续优化 UI 细节和体验
- 新增功能前先更新规范并确认范围

## 最近一次开发时间

2026-06-07

## 最近一次同步进度

2026-06-07

## 最近一次完成内容

- 已完成上一轮未完成的历史/设置拆分迁移：`CalculatorScreen.kt` 改为使用 `ui.history` 与 `ui.settings` 包中的公开页面/枚举/弹窗实现，并删除主文件中的旧内联实现
- 已完成币种选择与图标 UI 拆分：新增 `ui/coin/CoinSelection.kt`，迁出 `CoinMarketHeader`、`CoinIcon`、`CoinSelectorDialog` 与私有 `CustomCoinDialog`
- 已根据用户本地编译反馈移除 `SettingsScreen.kt` 中错误的 `androidx.compose.ui.input.pointer.consume` 导入，并保留拖拽手势里的 `change.consume()` 调用
- 已根据用户本地编译日志修复 `CalculatorScreen.kt` 中 `CoinMarginedModeOption` unresolved reference：新增本地 `ComparisonCoinMarginedModeOption` 并替换对比方案编辑弹窗中的残留调用
- 已执行非编译静态检查：确认主文件中不再残留旧历史/设置实现和旧币种选择模块实现，且 `git diff --check` 无空白错误
- 已按用户要求同步三份交接文档；未运行 Gradle、编译、测试，也未提交 git
- 已记录最近 `CalculatorScreen.kt` 收益方案对比 UI 变更：对比方案编辑弹窗支持结算模式切换与币本位计算方式选择，方案列表/详情/历史快照展示对应字段
- 已按最新性能目标调整币图标相关列表与加载方式：币种选择弹窗和对比方案币种选择弹窗保留 `LazyColumn` / `items` 可见项组合策略，避免一次性组合全部币种行；图标改为内置资源优先、缓存优先、非内置图标按需后台加载
- 已按用户新反馈提高首页底部“支持作者”入口可见度：保留入口，改为主题色 Card，带圆形咖啡图标、标题、副标题和“查看”提示，仍避免红/绿/金及博彩化视觉
- 已补充静态检查结论：`ComparisonCalculator.kt` 尚未基于 `ComparisonItem.settlementMode` / `coinMarginedCalculationMode` 执行币本位对比计算，相关入口当前存在“看起来支持但计算未贯通”的发布风险
- 已完成币种图标加载性能优化记录：`CoinAsset` 增加 `iconResourceName`；`CoinRepository.fetchTopCoins()` 不再同步批量下载图标，改为解析市场数据后绑定内置图标或已有本地缓存；`loadIconForCoin()` 支持非内置币图标按需下载并记录失败 ID；`CalculatorScreen.CoinIcon()` 优先渲染 drawable 内置图标，其次渲染缓存 bitmap，最后显示字母占位并后台尝试加载
- 已将币种选择弹窗和对比方案币种选择弹窗恢复/保留为 `LazyColumn`，避免打开弹窗时一次性组合全部币种行；这与上一轮“全部直接加载”的记录不同，当前以解决启动和弹窗卡顿为准
- 已记录该源码状态尚未由用户本地编译、测试或真机验证
- 本轮使用源码读取、局部编辑和非编译静态检查；没有执行 Gradle、编译或测试
- 业务源码状态仍等待用户本地编译、测试和真机验证反馈
- 2026-06-07 19:40 文档维护补充：记录过一次被中断的历史/设置页面拆分尝试；该中间态已在后续任务中完成迁移。
- 2026-06-07 19:50 文档维护补充：记录过一次 `CalculatorScreen.kt` 迁移补丁失败且未应用；该中间态已在后续任务中完成迁移。
