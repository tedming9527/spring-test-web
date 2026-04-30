---
name: openspec-archive-change
description: 在实验性工作流中归档已完成的变更。用于用户在实施完成后想要归档变更时。
license: MIT
compatibility: 需要 openspec CLI。
metadata:
  author: openspec
  version: "1.0"
  generatedBy: "1.3.1"
---

在实验性工作流中归档已完成的变更。

**输入**：可选指定变更名。如未指定，尝试从对话上下文推断。如果不明确或有歧义，必须提示用户选择可用变更。

**步骤**

1. **如未提供变更名，提示选择**

   运行 `openspec list --json` 获取可用变更。用 **AskUserQuestion 工具**让用户选择。

   只展示未归档的活动变更。
   如有可用，显示每个变更所用 schema。

   **重要**：不要猜测或自动选择变更，必须让用户选择。

2. **检查工件完成状态**

   运行 `openspec status --change "<name>" --json` 检查工件完成情况。

   解析 JSON，获取：
   - `schemaName`：所用工作流
   - `artifacts`：所有工件及其状态（`done` 或其他）

   **如有未完成工件：**
   - 显示警告，列出未完成工件
   - 用 **AskUserQuestion 工具**确认用户是否继续
   - 用户确认后继续

3. **检查任务完成状态**

   读取任务文件（通常为 `tasks.md`），检查未完成任务。

   统计 `- [ ]`（未完成）与 `- [x]`（已完成）任务数。

   **如有未完成任务：**
   - 显示警告，列出未完成任务数
   - 用 **AskUserQuestion 工具**确认用户是否继续
   - 用户确认后继续

   **如无任务文件：** 直接继续，无需任务相关警告。

4. **评估 delta spec 同步状态**

   检查 `openspec/changes/<name>/specs/` 下是否有 delta spec。如无，直接继续。

   **如有 delta spec：**
   - 与主 spec（`openspec/specs/<capability>/spec.md`）对比，分析将应用的变更（新增、修改、删除、重命名）
   - 归纳展示所有变更摘要

   **提示选项：**
   - 如需同步：“立即同步（推荐）”、“归档但不同步”
   - 如已同步：“立即归档”、“强制同步”、“取消”

   用户选择同步时，用 Task 工具（subagent_type: "general-purpose"，prompt: "用 Skill 工具调用 openspec-sync-specs 处理变更 '<name>'。Delta spec 分析：<包含分析摘要>"）。无论选择如何，均继续归档。

5. **执行归档**

   如归档目录不存在，先创建：
   ```bash
   mkdir -p openspec/changes/archive
   ```

   归档目标名用当前日期：`YYYY-MM-DD-<change-name>`

   **如目标已存在：**
   - 报错，建议重命名现有归档或用不同日期
   - 否则，将变更目录移动到归档目录
   ```bash
   mv openspec/changes/<name> openspec/changes/archive/YYYY-MM-DD-<name>
   ```

6. **显示归档摘要**

   展示归档完成摘要，包括：
   - 变更名
   - 所用 schema
   - 归档位置
   - 是否已同步 spec（如适用）
   - 任何警告（未完成工件/任务）

**成功归档时输出**

```
## 归档完成

**变更：**<change-name>
**Schema：**<schema-name>
**归档位置：**openspec/changes/archive/YYYY-MM-DD-<name>/
**Specs：**✓ 已同步到主 spec（或“无 delta spec”或“跳过同步”）

所有工件已完成。所有任务已完成。
```

**护栏**
- 未指定变更时必须提示选择
- 用工件图（openspec status --json）检查完成情况
- 不因警告阻止归档——只需提示并确认
- 归档时保留 .openspec.yaml（随目录一起移动）
- 明确展示归档结果
- 如需同步，采用 openspec-sync-specs 方式（agent 驱动）
- 如有 delta spec，始终先分析并展示摘要再提示

