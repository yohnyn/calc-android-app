# CURRENT_TASK

## 当前任务

按顺序继续拆分 `CalculatorScreen.kt`，已拆出仓位参数、费率设置、首页动作、历史快照和辅助弹窗模块，提升优化 2.0 Final 后的代码可维护性。

本次根据用户真机/本地查看反馈，优先修复优化 2.0 UI 验收问题：止盈止损开关区域空白与点击无效、添加对比方案弹窗结算模式/币本位模式控件形态、自定义币种输入框宽度不统一。

## 用户约束

- 不运行在线、远程或本地编译。
- 不运行 Gradle build / assemble / compile / install / test，也不运行 Android Studio 构建。
- 用户会在本地自行执行编译、构建和测试。
- 本轮不提交 git。

## 本轮处理内容

- 新增 `android-app/app/src/main/java/com/personal/futurescalculator/ui/position/PositionInputUi.kt`。
- 将 `仓位参数` Section 从 `CalculatorScreen.kt` 拆为 `PositionInputSection(...)`。
- 迁出的仓位参数内容包括：
  - 做多 / 做空
  - 全仓 / 逐仓
  - 开仓价
  - 保证金 / 币数量
  - 平仓价
  - 杠杆
  - U 本位全仓模式账户总资产复选框与输入框
  - 仓位参数内的止盈止损复选项与止盈价 / 止损价输入
  - 费率设置入口
- `CalculatorScreen.kt` 只保留仓位参数模块调用和 ViewModel 回调接线。
- 新增 `android-app/app/src/main/java/com/personal/futurescalculator/ui/fees/FeeSettingsUi.kt`，迁出 `FeeSettingsDialog(...)`。
- 新增 `android-app/app/src/main/java/com/personal/futurescalculator/ui/home/HomeActionsUi.kt`，迁出 `SupportAuthorCard(...)` 和底部 `HomeBottomActions(...)`。
- 新增 `android-app/app/src/main/java/com/personal/futurescalculator/ui/history/HistorySnapshots.kt`，迁出 `createProfitHistorySnapshot(...)` 和 `createAveragingHistorySnapshot(...)`。
- 新增 `android-app/app/src/main/java/com/personal/futurescalculator/ui/dialogs/RequirementDialogs.kt`，迁出 `MissingParametersDialog(...)` 和 `OperationRequirementDialog(...)`。
- 本轮补齐首页顶部标题 `仓位助手` 与副标题 `我负责计算，你负责决策`，并调整补仓与对比区域的标题文案。
- 本轮不改变计算逻辑或 ViewModel 行为。
- 修复 `android-app/app/src/main/java/com/personal/futurescalculator/ui/comparison/ComparisonUi.kt`：
  - 添加/编辑对比方案弹窗删除 `基础信息` 标题区。
  - 币种与 U 本位/币本位各占半行。
  - U 本位/币本位改为下拉框选择，不再使用切换模式。
  - 只有选择币本位时才显示币本位计算模式。
  - 币数量模式/反向合约模式改为下拉框选择。
- 修复 `android-app/app/src/main/java/com/personal/futurescalculator/ui/coin/CoinSelection.kt`：
  - 添加自定义币种弹窗中，币种名称输入框改为 `fillMaxWidth()`，与价格输入框宽度统一。

## 上一轮 Freeze 处理内容

- 将产品规范更新为“仓位助手 / 合约收益与风险测算工具”，记录优化 2.0 Final 封板原则。
- 固定首页模块顺序为 `币种选择 -> U 本位/币本位 -> 仓位参数 -> 止盈止损 -> 收益方案对比 -> 补仓决策模拟 -> 支持作者 -> 底部导航`，不再读取用户自定义模块顺序或显隐。
- 删除设置页的首页模块排序、模块显示管理入口及其页面实现；同步移除 ViewModel 和偏好仓储中的模块排序/显隐状态与保存逻辑。
- `高级设置` 文案改为 `费率设置`。
- U 本位全仓模式在仓位参数中新增 `全仓模式` 复选项：默认不启用账户总资产；启用后显示 `账户总资产 USDT` 输入，用于全仓强平价估算。
- `止盈止损` 已收进仓位参数卡片内：默认未勾选；勾选后在仓位参数内显示 `止盈价`、`止损价`；取消勾选会清空止盈止损相关输入。
- `CalculationInput` 新增直接 `takeProfitPrice` / `stopLossPrice` 字段。
- `CalculationResult` 新增 `takeProfitNetPnl`、`stopLossNetPnl`、`rewardRiskRatio` 字段。
- `FuturesCalculator` 已基于直接止盈价/止损价计算止盈收益、止损亏损和盈亏比；旧目标收益/ROI、最大亏损/ROI 反推字段仅保留兼容。
- 结果页删除风险等级卡，不再展示风险高低评价；保留估算强平价、距离强平价、手续费、ROI、止盈收益、止损亏损和盈亏比等客观结果。
- 复制文案删除风险等级和最优方案，改为复制强平价、距离强平价、止盈价/止损价、止盈收益/止损亏损和盈亏比等客观结果。
- 收益方案对比删除排名、最终排序和最优方案口径；历史快照只保存各方案结果和收益差距。
- 添加对比方案弹窗不再显示方案名称输入框；方案名继续自动生成，编辑已有方案时仍可重命名。
- 对比方案添加/编辑弹窗改为 `币种 ▼` 与 `U 本位/币本位 ▼` 半行布局；结算模式和币本位计算模式均使用下拉框。
- 杠杆选择器默认显示 `1x / 3x / 5x / 10x / 20x`，`更多 ▼` 展开后显示 `50x / 100x / 125x / 自定义`。
- 设置首页按封板方向分组为外观、币本位、反馈、关于；模块排序/显隐相关设置已删除。
- 免责声明中移除“高风险”这种风险等级化表述。

## 本轮静态检查

已执行非编译静态检查：

- `git diff --check` 无空白错误。
- `CalculatorScreen.kt` 从约 1191 行降至约 706 行。
- 新增 `PositionInputUi.kt` 约 155 行。
- 新增 `FeeSettingsUi.kt` 约 131 行。
- 新增 `HomeActionsUi.kt` 约 148 行。
- 新增 `HistorySnapshots.kt` 约 91 行。
- 新增 `RequirementDialogs.kt` 约 94 行。
- 搜索确认仓位参数 UI 控件已迁入 `ui/position/PositionInputUi.kt`，主屏通过 `PositionInputSection(...)` 调用。
- 搜索确认费率设置、首页动作、历史快照和辅助弹窗已迁入对应新模块，主屏只保留调用。
- 搜索确认业务源码中不再命中：`风险等级`、`低风险`、`中风险`、`高风险`、`最优`、`最佳`、`推荐方案`、`建议开仓`、`建议补仓`、`首页模块排序`、`模块显示管理`、`高级设置`、`目标与止损`、`排名`、`最终排序`。
- 搜索确认业务源码中不再存在 `moduleOrder`、`visibleModules`、模块排序/显隐仓储方法和旧配置集合引用。
- 简单括号计数确认以下文件结构平衡：
  - `CalculatorScreen.kt`
  - `ResultUi.kt`
  - `ComparisonUi.kt`
  - `SettingsScreen.kt`
  - `FuturesCalculator.kt`
- 本次 UI 修复后再次执行 `git diff --check`，无空白错误。
- 本次 UI 修复后简单括号计数确认以下文件结构平衡：
  - `ComparisonUi.kt`
  - `CoinSelection.kt`
- 搜索确认本次 UI 修复后的关键状态：
  - 添加对比方案弹窗不再命中 `基础信息`。
  - 旧的 `ComparisonCoinMarginedModeOption` 已移除。
  - `ComparisonDropdownSelector` 已用于结算模式和币本位计算模式。

未执行：

- 未运行 Gradle。
- 未编译。
- 未运行测试。
- 未提交 git。

## 注意事项

- 本轮未实现“首次复制选择摘要版/详细版并记住选择”的完整持久化流程，当前只是移除了复制文案中的风险等级和最优方案。
- 本轮未新增设置页的“复制结果默认格式”“历史记录管理”“方案库管理”“更新日志”具体页面。
- 收益方案对比 UI 仍可选择币本位和币本位计算方式，但 `ComparisonCalculator.kt` 仍未贯通币本位对比计算链路；发布前应隐藏入口或补齐计算。
- 本轮没有运行编译，新增 `CalculationInput` / `CalculationResult` 字段需要用户本地编译验证。
- 仓位参数模块拆分后尚未由用户本地编译验证。
- 止盈止损交互修复尚未由用户本地编译验证。
- 费率设置、首页动作、历史快照和辅助弹窗拆分后尚未由用户本地编译验证。
- 本次止盈止损空白/点击修复、对比方案弹窗下拉框修复、自定义币种输入框宽度修复尚未由用户本地编译和真机验证。

## 待用户本地验证

- Android Studio 同步/编译是否通过。
- 首页顺序是否固定，设置页是否不再出现模块排序/显隐。
- 全仓模式默认不显示账户总资产；勾选后显示输入框，取消后清空。
- 止盈止损默认未勾选；勾选显示止盈价/止损价；取消后清空相关结果。
- 填写止盈价、止损价后，结果弹窗是否展示止盈收益、止损亏损、盈亏比和强平价。
- 收益方案对比不再展示排名、最终排序、最优/最佳方案。
- 杠杆默认行和更多展开是否符合预期。
- 仓位参数拆分后，做多/做空、全仓/逐仓、开仓价、保证金/币数量、平仓价、杠杆、全仓模式和费率设置入口是否保持原行为。
- 止盈止损拆分后，默认未勾选、勾选显示止盈价/止损价、取消勾选清空相关输入是否保持原行为。
- 费率设置弹窗、支持作者入口、底部历史/重置/设置、保存历史快照和参数提示弹窗是否保持原行为。
- 止盈止损行是否不再出现下方长空白；点击文字区域和 Checkbox 是否都能启用/关闭；关闭后相关结果是否清空。
- 添加对比方案弹窗中，币种与 U 本位/币本位是否各占半行；U 本位/币本位是否为下拉框；只有币本位时才显示币数量/反向合约模式下拉框。
- 添加自定义币种弹窗里，币种名称和币种价格输入框宽度是否统一。
