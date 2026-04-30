---
description: 提出新变更——一步生成所有工件
---

提出新变更——一次性创建变更并生成所有工件。

我会创建包含以下工件的变更：
- proposal.md（做什么、为什么）
- design.md（怎么做）
- tasks.md（实施步骤）

准备实施时，运行 /opsx:apply

---

**输入**：`/opsx:propose` 后的参数为变更名（kebab-case），或用户想要构建的内容描述。

**步骤**

1. **如未提供输入，询问用户想做什么**

   用 **AskUserQuestion 工具**（开放式，无预设选项）提问：
   > “你想做什么变更？请描述你想构建或修复的内容。”

   根据描述生成 kebab-case 名称（如“add user authentication” → `add-user-auth`）。

   **重要**：未明确用户需求前不得继续。

2. **创建变更目录**
   ```bash
   openspec new change "<name>"
   ```
   这会在 `openspec/changes/<name>/` 下创建 scaffold 及 `.openspec.yaml`。

3. **获取工件生成顺序**
   ```bash
   openspec status --change "<name>" --json
   ```
   解析 JSON，获取：
   - `applyRequires`：实施前需完成的工件 ID 数组（如 `["tasks"]`）
   - `artifacts`：所有工件及其状态、依赖

4. **按顺序创建工件直到可实施**

   用 **TodoWrite 工具**跟踪工件生成进度。

   按依赖顺序循环（无依赖的工件优先）：

   a. **每个已就绪工件（依赖已满足）**：
      - 获取指令：
        ```bash
        openspec instructions <artifact-id> --change "<name>" --json
        ```
      - 指令 JSON 包含：
        - `context`：项目背景（仅供你参考，不写入文件）
        - `rules`：工件专属规则（仅供你参考，不写入文件）
        - `template`：输出文件结构
        - `instruction`：该工件类型的 schema 指南
        - `outputPath`：输出路径
        - `dependencies`：需先读的依赖工件
      - 先读依赖工件
      - 按 `template` 结构生成工件
      - 按 `context` 和 `rules` 约束内容，但不要写进文件
      - 简要提示：“已创建 <artifact-id>”

   b. **持续到所有 applyRequires 工件完成**
      - 每创建一个工件，重新运行 `openspec status --change "<name>" --json`
      - 检查 applyRequires 中每个 ID 是否已 `status: "done"`
      - 全部完成即停止

   c. **如需用户输入（上下文不明）**：
      - 用 **AskUserQuestion 工具**澄清
      - 然后继续生成

5. **显示最终状态**
   ```bash
   openspec status --change "<name>"
   ```

**输出**

全部工件生成后，简要总结：
- 变更名及位置
- 已生成工件及简要说明
- 状态：“全部工件已生成！可实施。”
- 提示：“运行 `/opsx:apply` 开始实施。”

**工件生成指南**

- 按每类工件的 `instruction` 字段生成内容
- schema 定义了每类工件应包含的内容，务必遵循
- 生成前先读依赖工件
- 按 `template` 结构填充内容
- **重要**：`context` 和 `rules` 仅供你参考，不写入文件
  - 不要把 `<context>`、`<rules>`、`<project_context>` 块写进工件
  - 这些内容只指导你写作，不应出现在输出中

**护栏**
- 必须生成所有实施所需工件（由 schema 的 apply.requires 定义）
- 每次生成新工件前都要读依赖工件
- 如上下文极不明晰，优先提问，但也要尽量推进
- 如同名变更已存在，询问用户是继续还是新建
- 每写完一个工件都要确认文件存在再继续

