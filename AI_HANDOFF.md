# AI_HANDOFF

## 交接摘要

本仓库是 Android/Kotlin/Jetpack Compose 项目，产品当前为“仓位助手（Futures Calculator）优化 2.0 Final Freeze”口径。

核心原则：

- App 是本地计算器 / 模拟器 / 对比工具，不是交易顾问。
- 不登录交易所，不绑定 API，不下单，不读取账户。
- 只展示客观计算结果，不展示投资建议、推荐方案、最佳方案、最优方案、风险等级或风险高低评价。
- 后续新增功能、重大调整和交互重构统一进入优化 3.0 讨论。

## 关键约束

- 必须遵守根目录 `AGENTS.md`：禁止运行任何构建、编译、安装、测试命令，包括 Gradle。
- 用户明确要求：不要在线编译，不要本地编译，不提交 git。
- 允许源码阅读、局部编辑、静态推理和非编译检查。
- 完成源码任务后要同步项目文档。

## 重要文件

- `APP_DEVELOPMENT_SPEC.md`：产品范围、公式、验收规则。
- `PROJECT_STATUS.md`：长期状态、已完成内容、已知风险和下一步。
- `CURRENT_TASK.md`：当前/最近一次任务状态。
- `android-app/app/src/main/java/com/personal/futurescalculator/model/CalculationInput.kt`：主输入模型，默认杠杆已改为 `1x`。
- `android-app/app/src/main/java/com/personal/futurescalculator/domain/FuturesCalculator.kt`：U 本位核心计算，包含直接止盈价/止损价、目标收益反推、强平价估算。
- `android-app/app/src/main/java/com/personal/futurescalculator/domain/ComparisonCalculator.kt`：方案对比计算，已支持 U 本位与币本位结果参与对比。
- `android-app/app/src/main/java/com/personal/futurescalculator/domain/CoinMarginedCalculator.kt`：币本位计算。
- `android-app/app/src/main/java/com/personal/futurescalculator/data/PlanRepository.kt`：方案库本地持久化。
- `android-app/app/src/main/java/com/personal/futurescalculator/model/SavedPlan.kt`：方案库模型。
- `android-app/app/src/main/java/com/personal/futurescalculator/ui/CalculatorScreen.kt`：主屏编排，接入目标收益、方案库、方案对比、补仓助手等模块。
- `android-app/app/src/main/java/com/personal/futurescalculator/ui/position/PositionInputUi.kt`：仓位参数、止盈止损轻量 chip、估算强平价 chip、全仓账户总资产输入和实时缩略结果。
- `android-app/app/src/main/java/com/personal/futurescalculator/ui/results/ResultUi.kt`：主结果和币本位结果弹窗；收益/ROI 优先展示，强平价仅在勾选估算且可计算时展示，交易参数默认折叠。
- `android-app/app/src/main/java/com/personal/futurescalculator/ui/comparison/ComparisonUi.kt`：方案对比列表、添加/编辑方案、方案库选择、相对主方案差值列表。
- `android-app/app/src/main/java/com/personal/futurescalculator/ui/averaging/AveragingDecisionUi.kt`：补仓助手；结果详细数据默认折叠。
- `android-app/app/src/main/java/com/personal/futurescalculator/ui/history/HistoryScreen.kt`：历史记录 / 方案库双标签。
- `android-app/app/src/main/java/com/personal/futurescalculator/ui/settings/SettingsScreen.kt`：设置页，包含免责声明和复制默认格式。

## 最近完成内容

- 历史页新增 `历史记录 / 方案库` 双标签。
- 方案库和历史记录分离：历史记录是自动计算快照，方案库是用户主动保存的开仓方案。
- 方案库支持打开方案、加入方案对比、删除方案。
- 方案对比添加方案入口拆成 `手动创建 / 从方案库选择`。
- 主方案参数不完整时，主方案勾选和方案对比按钮禁用。
- 方案对比结果顶部改为 `相对主方案差值列表`，逐项展示所有选中方案相对主方案的差值。
- 新增方案弹窗隐藏 `删除` 按钮，编辑已有方案才显示。
- 默认杠杆从 `3x` 改为 `1x`。
- 首页补齐 `目标收益` 模块，用目标收益金额或目标 ROI 反推目标价。
- 主结果页将强平价展示上移，并移除旧版结果分组口径。
- 补仓助手结果默认折叠详细数据，说明压缩为 `仅表示目标价下的收益差值`。
- `收益对比完成 / 展开收益对比结果` 已统一替换为 `方案对比 / 展开方案对比结果`。
- 设置页确认保留 `免责声明`。
- 用户本地编译反馈 `HistoryScreen.kt` 无法访问其他文件私有 `PositionSide.label()` / `MarginMode.label()`；已在 `HistoryScreen.kt` 本文件补本地私有扩展。
- 首页顶部 `仓位助手` 与 `我负责计算，你负责决策` 已删除，首页直接从币种选择开始。
- 仓位参数标题和 `费率设置` 已压缩到同一行。
- `止盈止损` 与 `估算强平价` 已改为同一行轻量 chip，并移动到保证金/币数量下方、杠杆上方。
- 未勾选 `估算强平价` 时不计算、不展示强平价；全仓勾选后才显示账户总资产输入和 `使用保证金`。
- 杠杆选择器已压缩为 `杠杆 1x 3x 5x 10x 20x 50x 自定义` 单行。
- 仓位参数下方已接入实时缩略结果，整卡可点击打开主结果详情。
- 目标模块改为 `目标价格    按收益/亏损反推`，支持目标收益/目标亏损切换，并通过 `填入止盈价` / `填入止损价` 自动开启止盈止损、写入价格、滚动到仓位区域、高亮输入行并 Toast 提示。
- 补仓助手展开后删除教程式说明，尽量从主方案带入当前仓位数据；补仓结果第一屏展示成本价、回本价、目标价收益和收益变化，详细数据默认折叠。
- 首页方案对比标题改为 `开仓对比    多个方案横向比较`。

## 静态检查结果

已做非编译检查：

- `git diff --check` 无空白错误。
- 搜索确认业务源码不再命中旧版结果分组、收益对比、补仓决策模拟、收益方案对比、收益反推工具、风险计划、最佳、最优、推荐、风险等级、高风险、低风险、建议补仓、建议减仓、专业模式、高级模式、专家模式等验收禁用口径。
- 搜索确认默认杠杆源码为 `BigDecimal.ONE`；测试文件中的 `3x` 只是测试案例，不是默认值。

未做：

- 未运行 Gradle。
- 未编译。
- 未运行测试。
- 未提交 git。

## 仍需关注

- 最新源码仍需要用户本地 Gradle 同步、编译和真机验证。
- 方案库、目标收益、方案对比、补仓助手、历史记录和设置页都需要真机走一遍交互。
- 首次复制弹窗选择摘要版/详细版并记住选择尚未完整实现；设置页复制默认格式已接入。
- 更新日志页面尚未实现。
- 仓库仍跟踪 `futures-calculator/android-app/` 下的早期遗留 Kotlin 文件，处理前不要随意删除。

## 建议下一步

优先让用户本地编译并反馈错误日志。若编译通过，建议真机重点验证：

- 默认杠杆是否为 `1x`。
- 目标收益模块是否能反推目标价。
- 历史页 `历史记录 / 方案库` 双标签是否正常。
- 保存开仓方案后是否进入方案库；方案库打开、加入对比、删除是否正常。
- 方案对比 `手动创建 / 从方案库选择` 是否正常。
- 主方案参数不完整时，主方案勾选和方案对比按钮是否禁用。
- 方案对比结果是否按 `相对主方案差值列表` 展示所有选中方案。
- 补仓助手结果是否默认折叠详细数据。
- 主结果页是否先展示 `强平与费用`，交易参数是否默认折叠。
- 首页顶部是否没有大标题，并直接从币种选择开始。
- 止盈止损和估算强平价是否为同一行 chip、位置是否在杠杆上方。
- 未勾选估算强平价时，首页缩略结果、主结果详情和复制/历史路径是否不出现强平价占位。
- 目标价格的 `填入止盈价` / `填入止损价` 是否会自动开启止盈止损、写入对应价格并高亮输入行。
