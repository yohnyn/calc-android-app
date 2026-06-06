# calcApp

个人使用的 Android 合约收益计算器项目，用于模拟 USDT 本位/相关合约仓位的收益、亏损、ROI、保证金、强平价估算、补仓计算和多方案对比。

## 当前最重要文档

如果你要把项目交给另一个 AI 继续开发，请让它按顺序读取：

1. `AI_HANDOFF.md`：最快了解当前项目接力上下文、限制、风险和下一步。
2. `APP_DEVELOPMENT_SPEC.md`：产品和开发总规范，是功能验收的最高依据。
3. `PROJECT_STATUS.md`：当前项目进度、已知问题和下一步任务。
4. `CURRENT_TASK.md`：最近一次任务记录和当前待办。
5. `AGENTS.md`：AI/代理工具必须遵守的构建与命令限制。

其中：

- **另一个 AI 第一份应该读：`AI_HANDOFF.md`**
- **功能实现最终以：`APP_DEVELOPMENT_SPEC.md` 为准**

## 主要文件

- `APP_DEVELOPMENT_SPEC.md`：产品结构、功能范围、公式、约束、开发步骤和验收清单。
- `PROJECT_STATUS.md`：项目长期状态，记录已完成内容、已知问题和下一步。
- `CURRENT_TASK.md`：当前/最近一次任务记录。
- `AI_HANDOFF.md`：给下一位 AI 或开发者的交接说明。
- `AGENTS.md`：AI 工作规则，尤其是禁止执行构建/编译/测试命令。
- `android-app/`：Android 主工程目录。

## 打开项目

用 Android Studio 打开：

```text
android-app
```

如 Android Studio 提示，请安装 Android 16 SDK / API 36。

## 环境需求

- JDK 17 或 Android Studio bundled JDK。
- Android SDK API 36。
- Android Studio Gradle Sync。

## 重要限制

根据 `AGENTS.md` 和当前用户要求：

- AI 不应执行终端命令。
- AI 不应运行 Gradle。
- AI 不应使用 shell。
- AI 不应执行 build / assemble / compile / install / test。
- 编译、构建、测试、真机运行由用户本地执行。

## 文档维护规则

每次完成开发或文档任务后，应同步维护：

- `PROJECT_STATUS.md`
- `CURRENT_TASK.md`
- `AI_HANDOFF.md`

### 自动更新要求

从现在开始，AI 每次完成任何任务后，即使用户没有特别说明，也必须自动检查并更新上述文档中需要更新的内容。

更新原则：

- 项目长期状态、已完成内容、已知问题、下一步任务：更新 `PROJECT_STATUS.md`。
- 当前任务目标、执行结果、剩余待办：更新 `CURRENT_TASK.md`。
- 给下一位 AI/开发者的接力上下文、限制、风险、推荐下一步：更新 `AI_HANDOFF.md`。
- 如果任务只影响其中一个文档，只更新相关文档；如果不确定，优先同时检查这三个文档。
