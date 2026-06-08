# AI_HANDOFF

## 交接摘要

本仓库是 Android/Kotlin/Jetpack Compose 项目，当前产品已按用户最新输入进入“仓位助手（Futures Calculator）优化 2.0 Final Freeze”口径。优化 2.0 到此结束；后续新增功能、重大调整和交互重构统一进入优化 3.0 讨论。

本轮已从“合约收益计算器”收敛为“仓位助手 / 合约收益与风险测算工具”，核心原则是：我负责计算，你负责决策；App 只展示客观计算结果，不展示投资建议、推荐方案、最佳方案、最优方案、风险等级或风险高低评价。

## 关键约束

- 必须遵守根目录 `AGENTS.md`：禁止运行任何构建、编译、安装、测试命令，包括 Gradle。
- 用户明确要求：不要在线编译，不要本地编译，不提交 git。
- 允许源码阅读、局部编辑、静态推理和非编译检查。
- 完成源码任务后要同步项目文档。

## 重要文件

- `APP_DEVELOPMENT_SPEC.md`：已加入优化 2.0 Final 封板原则，并修正主要冲突条款。
- `CURRENT_TASK.md`：记录本轮 Freeze 源码修改、静态检查和待本地验证点。
- `PROJECT_STATUS.md`：长期项目状态、风险和下一步。
- `android-app/app/src/main/java/com/personal/futurescalculator/model/CalculationInput.kt`：新增 `takeProfitPrice` / `stopLossPrice`。
- `android-app/app/src/main/java/com/personal/futurescalculator/model/CalculationResult.kt`：新增 `takeProfitNetPnl` / `stopLossNetPnl` / `rewardRiskRatio`。
- `android-app/app/src/main/java/com/personal/futurescalculator/domain/FuturesCalculator.kt`：新增直接止盈价/止损价的净盈亏与盈亏比计算。
- `android-app/app/src/main/java/com/personal/futurescalculator/ui/CalculatorScreen.kt`：固定首页顺序；调整费率设置、全仓模式、止盈止损复选框和主屏模块调用。
- `android-app/app/src/main/java/com/personal/futurescalculator/ui/position/PositionInputUi.kt`：本轮新增，承载仓位参数 Section。
- `android-app/app/src/main/java/com/personal/futurescalculator/ui/position/PositionInputUi.kt`：本轮承载仓位参数 Section，并并入止盈止损与全仓资产轻量选项。
- `android-app/app/src/main/java/com/personal/futurescalculator/ui/fees/FeeSettingsUi.kt`：本轮新增，承载费率设置弹窗。
- `android-app/app/src/main/java/com/personal/futurescalculator/ui/home/HomeActionsUi.kt`：本轮新增，承载支持作者入口和底部首页操作条。
- `android-app/app/src/main/java/com/personal/futurescalculator/ui/history/HistorySnapshots.kt`：本轮新增，承载收益测算和补仓模拟历史快照构造。
- `android-app/app/src/main/java/com/personal/futurescalculator/ui/dialogs/RequirementDialogs.kt`：本轮新增，承载缺参和操作要求提示弹窗。
- `android-app/app/src/main/java/com/personal/futurescalculator/ui/results/ResultUi.kt`：删除风险等级卡；展示止盈收益、止损亏损、盈亏比和客观强平价信息。
- `android-app/app/src/main/java/com/personal/futurescalculator/ui/comparison/ComparisonUi.kt`：删除排名/最终排序/最优方案口径；添加方案时隐藏方案名称输入框；添加/编辑弹窗使用币种与结算模式半行布局，结算模式和币本位计算模式使用下拉框。
- `android-app/app/src/main/java/com/personal/futurescalculator/ui/settings/SettingsScreen.kt`：删除模块排序/显隐入口和页面实现；设置页按外观、币本位、反馈、关于分组。
- `android-app/app/src/main/java/com/personal/futurescalculator/ui/CalculatorComponents.kt`：杠杆默认显示 `1x / 3x / 5x / 10x / 20x`，更多展开高倍和自定义。
- `android-app/app/src/main/java/com/personal/futurescalculator/data/UiPreferencesRepository.kt`：移除模块排序/显隐偏好读写。
- `android-app/app/src/main/java/com/personal/futurescalculator/viewmodel/CalculatorViewModel.kt`：移除模块排序/显隐状态与更新函数。

## 最近完成内容

- 固定首页顺序，不再允许用户调整或隐藏模块。
- 删除首页模块排序、模块显示管理、恢复默认排序/显示相关 UI、状态和偏好仓储逻辑。
- 将主屏 `高级设置` 改为 `费率设置`。
- 将 U 本位全仓账户总资产改为仓位参数里的 `全仓模式` 复选项：默认不勾选，勾选后显示 `账户总资产 USDT`。
- 将 `止盈止损` 改为默认未勾选的复选框；勾选后显示 `止盈价` / `止损价`，取消勾选清空相关输入。
- 计算链路支持直接止盈价、止损价，并输出止盈收益、止损亏损和盈亏比。
- 结果弹窗删除风险等级/风险高低评价，只保留客观数值。
- 收益方案对比删除排名、最终排序、最优/最佳方案口径。
- 添加对比方案弹窗不再显示方案名称输入框，自动生成方案名；编辑已有方案仍可重命名。
- 杠杆选择器改为常用倍数默认显示，`更多` 展开高倍和自定义。
- 复制文案删除风险等级和最优方案。
- 免责声明删除“高风险”等等级化表述。
- 已同步 `APP_DEVELOPMENT_SPEC.md`、`CURRENT_TASK.md`、`AI_HANDOFF.md`；`PROJECT_STATUS.md` 仍需本轮末尾追加状态。
- 2026-06-08 继续按顺序拆分 `CalculatorScreen.kt`：已新增 `ui/position/PositionInputUi.kt`，将仓位参数模块拆为 `PositionInputSection(...)`；主屏只保留调用和 ViewModel 回调接线。`CalculatorScreen.kt` 约从 1191 行降至 1095 行。本轮未改 UI 文案、计算逻辑或 ViewModel 行为。
- 2026-06-08 继续第二刀拆分后又回收：止盈止损最初曾短暂拆入 `ui/targetstop/TargetStopUi.kt`，后因用户验收反馈收回 `ui/position/PositionInputUi.kt`，并作为仓位参数中的轻量选项展示；主屏不再保留独立止盈止损模块。本轮未改计算逻辑或 ViewModel 行为。
- 2026-06-08 继续后续拆分：已新增 `ui/fees/FeeSettingsUi.kt`、`ui/home/HomeActionsUi.kt`、`ui/history/HistorySnapshots.kt`、`ui/dialogs/RequirementDialogs.kt`，分别迁出费率设置弹窗、支持作者/底部动作、历史快照构造和辅助提示弹窗。`CalculatorScreen.kt` 进一步降至约 706 行。本轮未改 UI 文案、计算逻辑或 ViewModel 行为。
- 2026-06-08 根据用户验收反馈修复优化 2.0 UI 问题：止盈止损已从独立模块收回到 `PositionInputUi.kt` 中，未启用时不再渲染下方空白，启用且 U 本位时才显示 `止盈价` / `止损价`；`ComparisonUi.kt` 中添加/编辑对比方案弹窗删除 `基础信息` 标题，币种与 U 本位/币本位各占半行，结算模式改为下拉框，只有币本位时显示币数量/反向合约模式下拉框；`CoinSelection.kt` 中自定义币种名称输入框改为全宽，与价格输入框统一。

## 静态检查结果

已做非编译检查：

- `git diff --check` 无空白错误。
- 搜索确认业务源码不再命中封板禁用口径：`风险等级`、`低风险`、`中风险`、`高风险`、`最优`、`最佳`、`推荐方案`、`建议开仓`、`建议补仓`、`首页模块排序`、`模块显示管理`、`高级设置`、`目标与止损`、`排名`、`最终排序`。
- 搜索确认业务源码不再存在模块排序/显隐状态、仓储方法和配置集合引用。
- 简单括号计数确认 `CalculatorScreen.kt`、`ResultUi.kt`、`ComparisonUi.kt`、`SettingsScreen.kt`、`FuturesCalculator.kt` 结构平衡。
- 本轮仓位参数拆分后，`git diff --check` 无空白错误；搜索确认仓位参数 UI 控件已迁入 `PositionInputUi.kt`，主屏通过 `PositionInputSection(...)` 调用。
- 本轮继续拆分后，`git diff --check` 无空白错误；搜索确认费率设置、首页动作、历史快照和辅助弹窗已迁入对应新模块，主屏只保留调用。
- 本次 UI 修复后，`git diff --check` 无空白错误；简单括号计数确认 `ComparisonUi.kt`、`CoinSelection.kt` 结构平衡；搜索确认添加对比方案弹窗不再命中 `基础信息`，旧 `ComparisonCoinMarginedModeOption` 已移除，结算模式和币本位计算模式改用 `ComparisonDropdownSelector`。

未做：

- 未运行 Gradle。
- 未编译。
- 未运行测试。
- 未提交 git。

## 仍需关注的代码风险

- 本轮新增了 `CalculationInput` / `CalculationResult` 字段并改动核心计算链路，必须由用户本地编译验证。
- 仓位参数模块拆分尚未由用户本地编译验证。
- 费率设置、首页动作、历史快照和辅助弹窗拆分尚未由用户本地编译验证。
- 止盈止损空白/点击修复、添加对比方案弹窗下拉框修复、自定义币种输入框宽度修复尚未由用户本地编译和真机验证。
- 首次复制选择摘要版/详细版并记住选择尚未完整实现；当前只清理了复制内容里的风险等级和最优方案。
- 设置页尚未补齐“复制结果默认格式”“历史记录管理”“方案库管理”“更新日志”具体页面。
- 收益方案对比 UI 仍可选择币本位和币本位计算方式，但 `ComparisonCalculator.kt` 仍未贯通币本位对比计算链路；发布前应隐藏入口或补齐计算。
- 历史记录“收藏到方案库”和方案库管理尚未完整实现。
- 仓库仍跟踪 `futures-calculator/android-app/` 下的早期遗留 Kotlin 文件，处理前不要随意删除。

## 建议下一步

优先让用户本地编译并反馈错误日志。若编译通过，建议真机重点验证：

- 首页固定顺序和设置页删除模块管理。
- 全仓模式账户总资产复选框。
- 止盈止损复选框、止盈价/止损价、止盈收益、止损亏损、盈亏比。
- 收益方案对比无排名、无最优/最佳方案。
- 杠杆更多展开。
- 复制文案无风险等级和最优方案。
- 仓位参数拆分后，重点验证做多/做空、全仓/逐仓、开仓价、保证金/币数量、平仓价、杠杆、全仓模式和费率设置入口。
- 本次 UI 修复后，重点验证止盈止损在仓位参数内无长空白且可点击启用/关闭；添加对比方案弹窗中币种与结算模式各半行、结算模式是下拉框、仅币本位时显示币数量/反向合约模式下拉框；自定义币种两个输入框同宽。

按用户要求，后续继续按顺序拆分。下一刀建议整理 `CalculatorScreen.kt` 中剩余的主屏对比/补仓编排与通用 helper，或先让用户本地编译验证本轮多个新增模块。

后续新增功能进入优化 3.0 讨论；不要继续在优化 2.0 范围内扩功能。
