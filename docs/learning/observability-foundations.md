# 可观测性核心讲义

- 整理人：沈砚舟 · 后端培训导师
- 日期：2026-09-24
- 定位：课程参考资料（基础可观测性第 1 课的知识沉淀）
- 性质说明：本讲义分「公认概念」（有出处，标注引用）与「本课推演」（教学整理，非外部文献直接表述）两类，阅读时勿混淆。

## 0. 材料性质说明

- 公认概念：可观测性领域有明确出处的定义、框架，附真实来源。
- 本课推演：从第一性原理推导出的判断框架（DB/日志分层、聚合判据），为本课教学整理，非外部文献直接表述。

## 1. 可观测性是什么（公认概念）

**经典定义**：可观测性是一种系统属性——你无需在每次意外发生时新增 instrumentation，仅凭系统的外部输出就能理解其内部状态。

> "Observability is the property of a system that allows you to understand its internal state from its external outputs — without having to add new instrumentation every time something unexpected happens."
> 来源：Cloud Tuned — The Three Pillars of Observability
> https://cloudtuned.hashnode.dev/the-three-pillars-of-observability-and-why-you-need-all-three

**词源**：可观测性（observability）源于控制理论，后被引入软件系统；其最根本形态就是"根据系统产生的数据推断内部状态"。
> 来源：Centreon — Observability glossary
> https://www.centreon.com/glossary/observability/

与监控的区别：监控是"越过阈值才告诉你"（你预设了问题）；可观测性是"让你能回答任意问题"——包括你没预料到的问题。
> 来源同上（Cloud Tuned）。

## 2. 第一性原理（本课推演）

> 系统内部的状态变化默认是静默的；可观测性就是主动为关键状态变化留下"可由外部证据还原"的痕迹，使"发生了什么、为什么"能够从事后重建。

三步推导：
1. 系统是黑箱——外部看不见内部怎么走；
2. 出问题时不能重放——生产已出事，唯一能依赖的是痕迹（测试才可控制输入重来）；
3. "可观测"不是"有没有日志"，而是"痕迹完备到能还原因果"——每个关键状态变化都要有：定位键（是谁）、关联键（属于哪个批次/事件）、原因（为什么）。

**检验尺**（每课可套用）：
> 某条状态变化的因果，能否仅凭日志被外部完整还原？能 → 可观测；还原不出 → 这里该留痕迹。

**为什么因果要"正常+异常"都覆盖**：缺了正常路径的痕迹，因果链同样断裂——这是本课场景 A（成功无日志）缺口的依据。

## 3. 三支柱：日志 / 指标 / 追踪（公认概念）

框架出处：Peter Bourgon 2017 年 2 月文章 *Metrics, tracing, and logging*，后成为可观测性架构的标准心智模型。
> 来源：Ascheriit — The Three Pillars: Logs, Metrics, Traces
> https://alivedise.github.io/backend-engineering-essentials/observability/three-pillars-logs-metrics-traces
> 另见 ClickHouse — The three pillars... and where the model breaks
> https://clickhouse.com/resources/engineering/three-pillars-of-observability

| 支柱 | 回答的问题 | 性质 |
|---|---|---|
| 日志 Logs | 发生了什么（what happened） | 离散、带时间戳的不可变事件记录，高信息量 |
| 指标 Metrics | 随时间表现如何（how behaving over time） | 预聚合的数值时间序列，低基数，适合告警/看板 |
| 追踪 Traces | 这条请求去哪、哪步失败（where/why） | 跨服务的一条请求的 span 树 |

> 来源：CodeWithVenu — Observability: Logging, Metrics, Tracing
> https://www.codewithvenu.com/blog/SystemDesign/16-Production-Engineering/02-Observability-Logging-Metrics-Tracing

**三支柱的局限（重要）**：支柱描述的是数据类型，不是结果——"数据齐了"不等于"能还原因果"。这个 gap 正是三支柱模型断裂的地方。
> 来源：ClickHouse（同上）。

> 本课印证：我们有计数日志（类指标）、有单条日志，但场景 A/B/C 仍还原不出——数据存在 ≠ 因果可还原。

**日志的核心性质**（支撑本课"过程源"论述）：日志是不可变（immutable）、追加式（append-only）、带时间戳的离散事件记录——一旦写入不能被修改。
> 来源：Azion — Three Pillars of Observability
> https://www.azion.com/en/learning/observability/observability-three-pillars/
> 另见 ClickHouse（同上）。

## 4. 两个关键区分（本课推演，概念有支撑）

### 4.1 数据库（状态源）vs 日志（过程源）——不是两套事实源，是分层

| | 数据库（事件表） | 日志 |
|---|---|---|
| 回答 | 现在是什么状态 | 当时发生了什么 |
| 性质 | 可查询的当前事实（终态/重试/lastError） | 不可变、追加的过程痕迹 |
| 角色 | 状态的事实源 | 过程的事实源 |

"单一事实源"在状态层成立：判断当前状态唯一信数据库。日志是过程源，两者各管各的，不构成竞争。日志的不可变/追加性正是它可作为"过程事实"的根基（见第 3 节引用）。

### 4.2 日志聚合（summary 行）——常见且规范，但有判据

- 聚合在日志中很常见：批次结束打一行 summary 是结构化日志的标准做法，给观测者"失败面一眼可见"的聚合视图。
- 判据（本课推演）：聚合是否合理，看它是否冒充状态事实源——
  - 合理：日志聚合 = "本批当时处理了哪些、结果如何"（过程观测，不声称当前状态）；
  - 错误：告警/查询依赖日志摘要判断当前系统状态——状态必须查数据库。

### 4.3 一致性与对账

日志摘要和数据库终态可以短暂不一致（不同视图、不同时点），不是 bug。对账时以数据库终态为准（状态事实源），日志只作过程证据——这正是课程中"真实 XXL-JOB 对账"的做法：DB 终态 + 批次日志两两核对，验证 Job 行为。

## 5. 与本课培训的联系

- 可靠性 = 正确 + 可诊断。前面课程把系统做"正确"（事务/Redis/原子领取/token/重试状态机/计数守恒）；可观测性补"可诊断"。
- 它防止三类失败：静默失败（没人知道）、事后无法定位（无从下手）、批次失明（知道有失败但不知道是哪几条/为什么）。
- 本课落点：事件级结果日志放 switch（不是 finally——收尾职责；兜底失败由 STATE_NOT_UPDATED 兜住）。A=事件级日志、B=失败原因补全、C=批次摘要（过程聚合，不违反事实源）、D=真实失败场景验收。

## 6. 参考引用

1. Cloud Tuned — The Three Pillars of Observability（可观测性经典定义/与监控区别）：https://cloudtuned.hashnode.dev/the-three-pillars-of-observability-and-why-you-need-all-three
2. Centreon — Observability glossary（控制理论根源）：https://www.centreon.com/glossary/observability/
3. Ascheriit — The Three Pillars: Logs, Metrics, Traces（Peter Bourgon 2017 出处）：https://alivedise.github.io/backend-engineering-essentials/observability/three-pillars-logs-metrics-traces
4. ClickHouse — The three pillars… and where the model breaks（支柱=数据类型非结果、日志不可变）：https://clickhouse.com/resources/engineering/three-pillars-of-observability
5. Azion — Three Pillars of Observability（日志不可变/时间戳/高基数）：https://www.azion.com/en/learning/observability/observability-three-pillars/
6. CodeWithVenu — Observability: Logging, Metrics, Tracing（三支柱各回答什么问题）：https://www.codewithvenu.com/blog/SystemDesign/16-Production-Engineering/02-Observability-Logging-Metrics-Tracing

> 注：第 2、4 节的「第一性推导 / DB-日志分层 / 聚合判据」为本课教学整理，非上述文献直接表述；仅"日志不可变、三支柱框架、经典定义、控制论根源"有出处可锚。
