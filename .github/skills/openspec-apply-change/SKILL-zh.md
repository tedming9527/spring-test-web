---
name: openspec-apply-change
description: 从 OpenSpec 变更中实施任务。用于用户想要开始实施、继续实施或处理任务时。
license: MIT
compatibility: 需要 openspec CLI。
metadata:
  author: openspec
  version: "1.0"
  generatedBy: "1.3.1"
---

从 OpenSpec 变更中实施任务。

**输入**：可选指定变更名。如未指定，尝试从对话上下文推断。如果不明确或有歧义，必须提示用户选择可用变更。

**步骤**

1. **选择变更**

   如果提供了名称，则直接使用。否则：
   - 如果用户在对话中提到变更，则自动推断
   - 如果只有一个活动变更，则自动选择
   - 如果有歧义，运行 `openspec list --json` 获取可用变更，并用 **AskUserQuestion 工具**让用户选择

   始终公告：“使用变更：<name>”，并说明如何覆盖（如 `/opsx:apply <other>`）。

2. **检查状态以理解 schema**
   ```bash
   openspec status --change "<name>" --json
   ```
   解析 JSON，理解：
   - `schemaName`：所用工作流（如“spec-driven”）
   - 哪个 artifact 包含任务（通常为“tasks”，具体见 status）

3. **获取 apply 指令**
   ```bash
   openspec instructions apply --change "<name>" --json
   ```
   返回内容：
   - `contextFiles`：artifact ID -> 具体文件路径数组（随 schema 变化）
   - 进度（总数、已完成、剩余）
   - 任务列表及状态
   - 基于当前状态的动态指令

   **处理状态：**
   - `state: "blocked"`（缺少 artifact）：显示消息，建议用 openspec-continue-change
   - `state: "all_done"`：祝贺，建议归档
   - 其他：继续实施

4. **读取上下文文件**

   读取 apply 指令输出中 `contextFiles` 下的每个文件路径。
   文件依赖于所用 schema：
   - **spec-driven**：proposal、specs、design、tasks
   - 其他 schema：按 CLI 输出的 contextFiles 读取

5. **显示当前进度**

   展示：
   - 所用 schema
   - 进度：“N/M 任务已完成”
   - 剩余任务概览
   - CLI 动态指令

6. **实施任务（循环直到完成或阻塞）**

   对每个待办任务：
   - 显示当前处理的任务
   - 进行所需代码更改
   - 保持更改最小且聚焦
   - 在任务文件中将 `- [ ]` 改为 `- [x]`
   - 继续下一个任务

   **暂停条件：**
   - 任务不清楚 → 询问澄清
   - 实施中发现设计问题 → 建议更新 artifact
   - 遇到错误或阻塞 → 报告并等待指导
   - 用户中断

7. **完成或暂停时，显示状态**

   展示：
   - 本次会话已完成任务
   - 总体进度：“N/M 任务已完成”
   - 全部完成时：建议归档
   - 暂停时：说明原因并等待指导

**实施过程中的输出**

```
## 正在实施：<change-name>（schema: <schema-name>）

正在处理任务 3/7：<task description>
[...正在实施...]
✓ 任务完成

正在处理任务 4/7：<task description>
[...正在实施...]
✓ 任务完成
```

**完成时输出**

```
## 实施完成

**变更：**<change-name>
**Schema：**<schema-name>
**进度：**7/7 任务已完成 ✓

### 本次会话已完成
- [x] 任务1
- [x] 任务2
...

全部任务已完成！可归档。
```

**暂停时输出（遇到问题）**

```
## 实施已暂停

**变更：**<change-name>
**Schema：**<schema-name>
**进度：**4/7 任务已完成

### 遇到的问题
<问题描述>

**选项：**
1. <选项1>
2. <选项2>
3. 其他方式

请问你想怎么做？
```

**护栏**
- 持续处理任务直到完成或阻塞
- 实施前务必读取上下文文件（来自 apply 指令输出）
- 任务不明确时暂停并询问
- 实施中发现问题时暂停并建议更新 artifact
- 每次代码更改都要最小且聚焦
- 每完成一个任务立即更新任务勾选框
- 遇到错误、阻塞或不明确时暂停，不要猜测
- 只用 CLI 输出的 contextFiles，不要假定具体文件名

**流式工作流集成**

本技能支持“变更上的动作”模型：

- **可随时调用**：无论 artifact 是否全部完成，只要有任务即可，支持部分实施、与其他动作交错
- **允许 artifact 更新**：实施中发现设计问题可建议更新 artifact，不锁定阶段，灵活工作

