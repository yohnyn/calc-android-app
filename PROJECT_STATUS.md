# PROJECT_STATUS

> 维护说明：本文档记录项目整体状态；当前正在处理的单次任务见 `CURRENT_TASK.md`；给下一位 AI/开发者的接力上下文见 `AI_HANDOFF.md`。

## 项目名称

USDT合约收益计算器（CalcApp）

---

## 项目目标

开发一个纯本地运行的 Android 合约收益计算器。

特点：

- 不连接交易所
- 不联网
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

当前项目已经完成：

- 项目基础架构
- 核心计算逻辑
- 对比计算逻辑
- ViewModel
- 第一版UI
- 领域模型、仓储、偏好设置、主题与工具类的基础文件
- 项目状态文档初始化：`PROJECT_STATUS.md`、`CURRENT_TASK.md`、`AI_HANDOFF.md`

项目已经进入：

【功能完善 + 测试验证 + 文档接力维护阶段】

---

## 已完成功能

### 核心计算

完成：

- 做多做空
- 全仓逐仓
- 杠杆计算
- 保证金计算
- 仓位价值计算
- 手续费计算
- ROI计算
- 强平价估算
- 强平距离计算

核心文件：

- FuturesCalculator.kt
- NumberRules.kt

---

### 对比分析

完成：

- 多方案比较
- ROI差异分析
- 风险等级分析
- 保证金差异分析

核心文件：

- ComparisonCalculator.kt

---

### 状态管理

完成：

- ViewModel状态管理
- 实时计算
- 对比方案管理

核心文件：

- CalculatorViewModel.kt

---

### UI

完成：

- CalculatorScreen
- CalculatorComponents
- 输入区域
- 结果区域
- 对比区域

核心文件：

- CalculatorScreen.kt
- CalculatorComponents.kt
- Theme.kt
- ProfitLossPalette.kt

---

### 本地数据与偏好

已存在：

- 币种列表/自定义币种相关仓储
- 计算历史相关仓储
- UI 偏好相关仓储
- 主题模式与盈亏配色模型

核心文件：

- CoinRepository.kt
- HistoryRepository.kt
- UiPreferencesRepository.kt
- CoinAsset.kt
- ThemeMode.kt

---

## 当前已知问题

待验证：

- UI是否完全符合APP_DEVELOPMENT_SPEC.md
- 是否存在编译错误
- 是否存在运行时异常
- 数值边界是否正确
- ROI计算是否与交易所一致
- 文档中“第一版不联网”和“当前版本启动获取 CoinGecko 价格”的范围描述存在差异，后续实现前需要明确当前版本是否允许首次联网获取公开价格
- 仓库中同时存在 `android-app/` 与 `futures-calculator/android-app/` 的相似目录，后续开发前需要确认主工程目录，避免改错路径
- 本次文档维护按用户要求未执行终端、shell、Gradle、编译或测试命令

---

## 下一步任务

优先级 P0：

- 确认主工程目录是根目录下的 `android-app/`
- 由用户本地执行 Gradle 同步/编译验证
- 根据用户本地编译结果修复编译错误
- 真机运行验证
- 对照 `APP_DEVELOPMENT_SPEC.md` 做功能验收

优先级 P1：

- 优化UI细节
- 输入校验
- 错误提示
- 明确联网范围：纯本地计算器 / 启动获取公开币价二选一或分阶段处理
- 检查补仓计算、补仓决策模拟、复制战绩、设置页、隐私政策和免责声明是否完整符合规范

优先级 P2：

- 深色模式
- 动画效果
- 体验优化
- 历史记录、行情、更多设置等后续扩展必须在明确范围后再做

---

## 最近真实Git提交

执行以下命令更新：

git log --oneline -10

不要手动填写。

---

## AI开发接力规则

每次启动Aider后必须：

1. 阅读 APP_DEVELOPMENT_SPEC.md
2. 阅读 PROJECT_STATUS.md
3. 阅读 CURRENT_TASK.md
4. 阅读 AI_HANDOFF.md
5. 分析当前代码状态
6. 不得重复实现已有功能
7. 不得删除已验证功能

每次完成任务后必须：

1. 更新 PROJECT_STATUS.md
2. 更新 CURRENT_TASK.md
3. 更新 AI_HANDOFF.md
4. 更新已完成功能
5. 更新下一步任务
6. 更新已知问题

未经明确允许：

- 不执行 git commit
- 不执行 git push
- 不删除文件
- 不重构无关模块
- 不执行终端命令、shell 命令、Gradle 编译/测试，除非用户明确解除限制

---

## 最近一次开发时间

2026-06-06

## 最近一次完成内容

- 创建并维护项目接力文档体系
- 更新 `PROJECT_STATUS.md`，补充当前状态、已知问题、下一步任务和接力规则
- 创建 `CURRENT_TASK.md`，记录当前任务目标、约束、完成情况和后续建议
- 创建 `AI_HANDOFF.md`，记录下一位 AI/开发者继续工作的上下文
- 本次按用户要求未执行任何终端命令、shell 命令、Gradle 命令、编译或测试

## 下一次打开项目应该做什么

1. 先阅读 `APP_DEVELOPMENT_SPEC.md`、`PROJECT_STATUS.md`、`CURRENT_TASK.md`、`AI_HANDOFF.md`
2. 确认是否继续沿用“只使用文件编辑工具，不执行终端/Gradle”的限制
3. 确认主工程目录，优先检查根目录 `android-app/`
4. 由用户本地执行编译/测试/真机运行后，把结果反馈给 AI
5. AI 根据用户反馈修复代码或继续完善功能
