# 可观测性核心讲义

- 整理人：沈砚舟 · 后端培训导师
- 日期：2026-09-24（结构：第一性原理前置；术语：过程源为"当时"状态）
- 定位：课程参考资料（基础可观测性第 1 课的知识沉淀）
- 性质说明：本讲义分「本课推演」（第一性原理与推导，教学整理）与「公认概念」（有出处，标注引用）两类。**先从第一性原理出发**，用它解释三支柱与各种区分；公认定义置于其后作印证，阅读时勿混淆两类内容。

## 1. 第一性原理（本课推演，先行）

> 系统内部的状态变化默认是静默的；可观测性就是主动为关键状态变化留下"可由外部证据还原"的痕迹，使"发生了什么、为什么"能够从事后重建。

三步推导：
1. **系统是黑箱**——外部看不见内部怎么走；
2. **出问题时不能重放**——生产已出事，唯一能依赖的是痕迹（测试才可控制输入重来）；
3. **"可观测"不是"有没有日志"，而是"痕迹完备到能还原因果"**——每个关键状态变化都要有：定位键（是谁）、关联键（属于哪个批次/事件）、原因（为什么）。

**时态区分（关键）**：系统可被推断的对象有两类——
- **当前状态**：现在系统处于什么状态（由数据库这类"状态源"回答）；
- **当时的内部状态**：某一时刻系统内部发生了什么（由日志这类"过程源"回答）。

第一性原理关心的"还原因果"主要依赖**过程痕迹**，因此对过程源，正确的表述是"推断**当时的**内部状态"，而不是笼统的"推断内部状态"——后者会让"日志能反映当前状态"的错觉钻进来。

**检验尺**（每课可套用）：
> 某条状态变化的因果，能否仅凭日志被外部完整还原？能 → 可观测；还原不出 → 这里该留痕迹。

**为什么因果要"正常+异常"都覆盖**：缺了正常路径的痕迹，因果链同样断裂——这是本课场景 A（成功无日志）缺口的依据。

## 2. 用第一性解释三支柱（公认概念）

系统需要"留痕迹"来还原因果，具体留下什么类型的痕迹，演化出三大类——即**三支柱**。框架出处：Peter Bourgon 2017 年 2 月文章 *Metrics, tracing, and logging*。
> 来源：Ascheriit — The Three Pillars: Logs, Metrics, Traces
> https://alivedise.github.io/backend-engineering-essentials/observability/three-pillars-logs-metrics-traces
> 另见 ClickHouse — The three pillars... and where the model breaks
> https://clickhouse.com/resources/engineering/three-pillars-of-observability

| 支柱 | 回答的问题 | 还原的对象 | 性质 |
|---|---|---|---|
| 日志 Logs | 发生了什么（what happened） | **当时的**事件细节 | 离散、带时间戳的不可变事件记录，高信息量 |
| 指标 Metrics | 随时间表现如何（how behaving over time） | 随时间的变化趋势 | 预聚合的数值时间序列，低基数，适合告警/看板 |
| 追踪 Traces | 这条请求去哪、哪步失败（where/why） | 单条请求的路径 | 跨服务的一条请求的 span 树 |

> 来源：CodeWithVenu — Observability: Logging, Metrics, Tracing
> https://www.codewithvenu.com/blog/SystemDesign/16-Production-Engineering/02-Observability-Logging-Metrics-Tracing

**三支柱的局限（呼应第一性）**：支柱描述的是**数据类型**，不是**结果**——"数据齐了"不等于"能还原因果"。这个 gap 正是三支柱模型断裂的地方。
> 来源：ClickHouse（同上）。

> 本课印证：我们有计数日志（类指标）、有单条日志，但场景 A/B/C 仍还原不出——**数据存在 ≠ 因果可还原**。

**日志的核心性质**（支撑"过程源"论述）：日志是不可变（immutable）、追加式（append-only）、带时间戳的离散事件记录——一旦写入不能被修改，因此只能还原"当时"，不能反映"当前"。
> 来源：Azion — Three Pillars of Observability
> https://www.azion.com/en/learning/observability/observability-three-pillars/
> 另见 ClickHouse（同上）。

## 3. 用第一性解释两个区分（本课推演，概念有支撑）

### 3.1 数据库（状态源）vs 日志（过程源）——不是两套事实源，是分层

| | 数据库（事件表） | 日志 |
|---|---|---|
| 回答 | 现在是什么状态 | **当时**发生了什么 |
| 性质 | 可查询的当前事实（终态/重试/lastError） | 不可变、追加的过程痕迹 |
| 角色 | 状态的事实源（当前） | 过程的事实源（当时） |

"单一事实源"在状态层成立：判断**当前**状态唯一信数据库。日志是**当时**的过程源，两者各管各的，不构成竞争。日志的不可变/追加性正是它只能作为"当时过程"的根基（见第 2 节引用）。

### 3.2 日志聚合（summary 行）——常见且规范，但有判据

- 聚合在日志中很常见：批次结束打一行 summary 是结构化日志的标准做法，给观测者"失败面一眼可见"的聚合视图。
- 判据（本课推演）：聚合是否合理，看它**是否冒充状态事实源**——
  - 合理：日志聚合 = "本批当时处理了哪些、结果如何"（过程观测，不声称当前状态）；
  - 错误：告警/查询依赖日志摘要判断**当前**系统状态——状态必须查数据库。

### 3.3 一致性与对账

日志摘要和数据库终态可以短暂不一致（不同视图、不同时点：日志是"当时"，DB 是"当前"），不是 bug。对账时以数据库终态为准（状态事实源），日志只作过程证据——这正是课程中"真实 XXL-JOB 对账"的做法：DB 终态 + 批次日志两两核对，验证 Job 行为。

## 4. 公认定义（印证，含时态修正注）

可观测性的经典定义：一种系统属性——无需在每次意外发生时新增 instrumentation，仅凭外部输出就能理解系统内部状态。
> "Observability is the property of a system that allows you to understand its internal state from its external outputs — without having to add new instrumentation every time something unexpected happens."
> 来源：Cloud Tuned — The Three Pillars of Observability
> https://cloudtuned.hashnode.dev/the-three-pillars-of-observability-and-why-you-need-all-three

**时态修正注（本课观点）**：上述经典定义中的"internal state"未区分时态。结合第 1、3 节：对**过程源**（日志）而言，能推断的是"**当时的** internal state"；对**状态源**（数据库）而言，才是"当前的 internal state"。工程落地时按此分层理解，避免把日志当成当前状态。

**词源**：可观测性（observability）源于控制理论，后被引入软件系统；其最根本形态是"根据系统产生的数据推断内部状态"。
> 来源：Centreon — Observability glossary
> https://www.centreon.com/glossary/observability/

与监控的区别：监控是"越过阈值才告诉你"（预设了问题）；可观测性是"让你能回答任意问题"——包括没预料到的问题。
> 来源同 Cloud Tuned。

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

> 注：第 1、3 节的「第一性推导 / DB-日志分层 / 聚合判据 / 时态修正」为本课教学整理，非外部文献直接表述；仅"日志不可变、三支柱框架、经典定义、控制论根源"有出处可锚。
