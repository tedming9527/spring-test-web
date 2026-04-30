---
name: openspec-propose
description: 一步生成所有工件，提出新的变更。用于用户想快速描述要构建的内容，并获得包含设计、规格和任务的完整 proposal。
license: MIT
compatibility: 需要 openspec CLI。
metadata:
  author: openspec
  version: "1.0"
  generatedBy: "1.3.1"
---

提出新变更——一步创建变更并生成所有工件。

我会为变更创建以下工件：
- proposal.md（做什么 & 为什么）
- design.md（怎么做）
- tasks.md（实施步骤）

准备实施时，运行 /opsx:apply

---

**输入**：用户请求应包含变更名（kebab-case）或要构建内容的描述。

**步骤**

1. **如未提供明确信息，询问要构建什么**

   用 **AskUserQuestion 工具**（开放式，无预设选项）提问：
   > “你想做什么变更？请描述你要构建或修复的内容。”

   根据描述生成 kebab-case 名称（如“add user authentication”→`add-user-auth`）。

   **重要**：未明确用户需求前不得继续。

2. **创建变更目录**
   ```bash
   openspec new change "<name>"
   ```
   这将在 `openspec/changes/<name>/` 下创建 scaffold 及 `.openspec.yaml`。

3. **获取工件生成顺序**
   ```bash
   openspec status --change "<name>" --json
   ```
   解析 JSON，获取：
   - `applyRequires`：实施前需完成的工件 ID 数组（如 `["tasks"]`）
   - `artifacts`：所有工件及其状态和依赖

4. **按顺序生成工件直到可实施**

   用 **TodoWrite 工具**跟踪工件生成进度。

   按依赖顺序循环生成工件（无未完成依赖的工件优先）：

   a. **对每个 `ready`（依赖已满足）的工件：**
      - 获取指令：
        ```bash
        openspec instructions <artifact-id> --change "<name>" --json
        ```
      - 指令 JSON 包含：
        - `context`：项目背景（仅供你参考，不写入文件）
        - `rules`：工件专属规则（仅供你参考，不写入文件）
        - `template`：输出文件结构
        - `instruction`：该工件类型的 schema 指南
        - `outputPath`：工件写入路径
        - `dependencies`：需读取的已完成工件
      - 读取所有依赖工件内容
      - 按 `template` 结构生成工件文件
      - 应用 `context` 和 `rules` 作为约束——但不要写入文件
      - 简要提示：“已创建 <artifact-id>”

   b. **直到所有 `applyRequires` 工件完成**
      - 每创建一个工件后，重新运行 `openspec status --change "<name>" --json`
      - 检查 `applyRequires` 中每个工件 ID 是否 `status: "done"`
      - 全部完成即停止

   c. **如工件需用户输入**（上下文不明）：
      - 用 **AskUserQuestion 工具**澄清
      - 然后继续生成

5. **显示最终状态**
   ```bash
   openspec status --change "<name>"
   ```

**输出**

所有工件生成后，简要总结：
- 变更名及位置
- 已生成工件及简要说明
- 当前状态：“所有工件已生成！可实施。”
- 提示：“运行 `/opsx:apply` 或让我开始实施任务。”

**工件生成指南**

- 遵循每种工件类型的 `openspec instructions` 指令
- schema 定义了每个工件应包含的内容——严格遵循
- 生成新工件前，先读取依赖工件内容
- 按 `template` 结构生成输出文件，填充各部分
- **重要**：`context` 和 `rules` 仅供你参考，不写入文件
  - 不要将 `<context>`、`<rules>`、`<project_context>` 块写入工件
  - 这些内容仅指导你写作，不应出现在输出中

**护栏**
- 生成所有实施所需工件（由 schema 的 `apply.requires` 定义）
- 生成新工件前务必读取依赖工件
- 如上下文极不明晰，先询问用户——但优先做合理决策以保持进度
- 如同名变更已存在，询问用户是继续还是新建
- 每写完一个工件文件都要确认其存在再继续

