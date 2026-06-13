# AI_HANDOFF

## 项目摘要

本仓库是 Android/Kotlin/Jetpack Compose 项目“仓位助手”。主工程位于 `android-app/`。

App 是本地仓位计算、模拟和方案对比工具，不登录交易所、不读取账户、不下单，也不提供投资建议、推荐方案、最佳方案或风险等级。

## 必须遵守

- 先阅读根目录 `AGENTS.md`。
- 禁止代理运行 Gradle、构建、编译、安装和测试命令。
- 可以阅读与修改源码、执行静态检查并提交 Git。
- 产品规则以 `APP_DEVELOPMENT_SPEC.md` 和用户最新要求为准。
- 不要删除或回退用户已有变更。

## 当前功能状态

- 首页固定为仓位参数、结果卡、开仓方案对比、补仓助手、支持作者和底部低权重入口。
- U 本位和币本位计算已接入；币本位手续费、初始保证金和保证金收益率仍没有确认口径。
- 首页使用渐进结果卡，完整结果支持存为方案和查看详情；保存方案按参数去重。
- 结果详情突出净盈亏、保证金收益率、可选止盈/止损和仓位安全边界；固定头部和底部操作，中间内容滚动。
- 方案对比支持不完整首页状态下独立导入方案；首页仅选择与编辑，结果页选择基准；不完整方案不能保存。
- 方案库保存完整输入，直接展示打开和加入对比，其余操作收进更多菜单。
- 打开方案会记录来源；删除仍关联的来源方案会清理首页工作区，手动修改后解除来源关联。
- 查看完整收益、币本位、补仓或对比结果后自动加入记录，使用规范化参数指纹跨重启去重，与方案库分离。
- 补仓助手默认折叠，普通展开不自动带入首页结果；当前持仓可手动创建、从方案库选择、从结果详情模拟补仓或由方案库明确带入。
- 补仓目标平仓价可选；核心预览实时展示补仓后均价、数量、保证金和仓位变化。
- 数字展示按语义格式化，兜底最多六位；杠杆只允许整数。
- 设置首页展示关键设置当前值；强平与可开使用统一的账户总资金术语和动态说明。

## 关键文件

- `APP_DEVELOPMENT_SPEC.md`：产品与验收规则。
- `PROJECT_STATUS.md`：当前状态与风险。
- `android-app/app/src/main/java/com/personal/futurescalculator/viewmodel/CalculatorViewModel.kt`：主状态、方案来源关联、保存去重和删除联动。
- `android-app/app/src/main/java/com/personal/futurescalculator/ui/CalculatorScreen.kt`：首页编排、方案基准、结果与补仓联动。
- `android-app/app/src/main/java/com/personal/futurescalculator/ui/position/PositionInputUi.kt`：仓位参数。
- `android-app/app/src/main/java/com/personal/futurescalculator/ui/results/ResultUi.kt`：首页结果卡与结果详情。
- `android-app/app/src/main/java/com/personal/futurescalculator/ui/comparison/ComparisonUi.kt`：开仓方案对比。
- `android-app/app/src/main/java/com/personal/futurescalculator/ui/averaging/AveragingDecisionUi.kt`：补仓助手。
- `android-app/app/src/main/java/com/personal/futurescalculator/ui/history/HistoryScreen.kt`：历史记录与方案库。
- `android-app/app/src/main/java/com/personal/futurescalculator/domain/FuturesCalculator.kt`：U 本位核心计算与强平估算。
- `android-app/app/src/main/java/com/personal/futurescalculator/domain/AveragingDecisionCalculator.kt`：补仓决策计算。

## 当前风险

- 最新源码尚未由用户本地完成编译、测试和真机验证。
- 最新变更跨越多个 Compose 调用签名、输入校验和结果详情布局，优先确认是否存在编译错误。
- 固定结果操作栏、自动历史去重、小额币数量、方案删除联动和补仓页面自动定位需要真机验证。
- 币本位手续费、初始保证金和保证金收益率未定义，禁止擅自套用 U 本位公式。
- 仓库仍跟踪 `futures-calculator/android-app/` 下的早期遗留 Kotlin 文件，未确认前不要删除。

## 推荐下一步

用户本地完成 Sync、编译和测试后，根据错误日志修复；编译通过后重点走通结果保存、方案对比基准、方案删除联动和补仓助手完整流程。
