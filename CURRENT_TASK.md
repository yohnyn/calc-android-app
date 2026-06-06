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
- 记录当前 Android App 代码状态、未验证事项与下一步建议。

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
- 本轮未运行编译，需用户确认 ViewModel/ComparisonCalculator 是否已对币本位对比计算完整贯通；若未贯通，当前可能只是 UI/保存/展示层面的增强。

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
- 收益方案对比新增/编辑弹窗是否能正常切换 U 本位/币本位和币本位计算方式。
- 收益方案对比列表、结果详情、历史快照是否正确展示结算模式和币本位计算方式。
- 如果选择币本位对比方案，实际计算结果是否符合预期；如不符合，需要继续检查 `CalculatorViewModel.kt` 与 `ComparisonCalculator.kt`。
- 设置页 → 模块显示管理是否只显示三个可配置项。
- 关闭“收益方案对比”或“补仓决策模拟”后，首页对应模块是否隐藏。
- 恢复默认显示后，三个可配置模块是否全部重新显示。

## 当前状态

- 文档维护进行中。
- 未执行任何终端命令、shell、Gradle、编译或测试。
- 等待用户本地编译/运行反馈。
