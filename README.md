# calcApp

个人使用的 Android 仓位助手项目，用于本地模拟 U 本位和币本位合约仓位的收益、亏损、ROI、保证金、强平价估算、目标收益、补仓助手、方案库和多方案对比。

## 当前最重要文档

如果你要把项目交给另一个 AI 继续开发，请让它按顺序读取：

1. `AGENTS.md`：AI/代理工具必须遵守的操作限制。
2. `AI_HANDOFF.md`：快速了解接力上下文和推荐下一步。
3. `APP_DEVELOPMENT_SPEC.md`：唯一产品范围、公式和验收规范。
4. `PROJECT_STATUS.md`：唯一长期项目状态、风险和下一步。
5. `CURRENT_TASK.md`：仅记录当前任务。

其中：

- **另一个 AI 第一份应该读：`AGENTS.md`**
- **功能验收以：`APP_DEVELOPMENT_SPEC.md` 与用户最新要求共同为准**
- **`.aider.chat.history.md` 仅为历史档案，不参与当前规则判断**

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

所有代理操作限制以 `AGENTS.md` 为唯一依据。

## 文档维护规则

- 产品范围、公式或验收规则变化：更新 `APP_DEVELOPMENT_SPEC.md`。
- 长期状态、已完成内容、风险或下一步变化：更新 `PROJECT_STATUS.md`。
- 当前任务发生变化：更新 `CURRENT_TASK.md`。
- 只有交接入口或关键上下文变化时才更新 `AI_HANDOFF.md`，避免复制状态文档。
