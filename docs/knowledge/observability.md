# 知识卡：可观测性（Observability）

- 领域：后端 / 可观测性
- 日期：2026-09-24
- 关联课程：基础可观测性第 1 课（XXL-JOB 分类变更补偿任务）
- 完整讲义：`docs/learning/observability-foundations.md`

## 一句话核心

系统内部状态变化默认是**静默**的；可观测性 = 主动为关键状态变化留下**可由外部证据还原**的痕迹，使"发生了什么、为什么"能从事后重建。

## 关键要点

1. **第一性（黑箱 + 痕迹）**：系统是黑箱，出事时不能重放（那是测试的特权），唯一能依赖的是痕迹。
2. **可观测 ≠ 有日志**：而是"痕迹完备到能还原因果"——关键状态变化要有定位键（是谁）、关联键（批次/事件）、原因（为什么）。
3. **时态区分**：状态源（数据库）回答"**当前**状态"；过程源（日志）回答"**当时**发生了什么"。对日志，正确表述是"推断**当时的**内部状态"，不是"推断内部状态"。
4. **三支柱**：日志（当时的发生了什么，不可变事件）、指标（随时间趋势，聚合数值）、追踪（单条请求路径）。框架出处：Peter Bourgon 2017。
5. **三支柱局限**：支柱描述数据类型，不是结果——"数据齐 ≠ 因果可还原"。
6. **DB（状态源）vs 日志（过程源）**：不是两套事实源，是分层；当前状态唯一信 DB。
7. **日志聚合（summary 行）常见且规范**，判据：聚合不冒充状态事实源（状态必须查 DB）。

## 检验尺

> 某条状态变化的因果，能否仅凭日志被外部完整还原？能 → 可观测；还原不出 → 这里该留痕迹。

## 易错点

- 只记异常、不记成功 → 因果链断裂（缺正常路径痕迹）。
- 业务结果日志放 `finally` → 错误。`finally` 是收尾职责（释放资源）；兜底失败由 `STATE_NOT_UPDATED` 兜住，结果日志应放 `switch`。
- 把日志当成"当前状态" → 错误。日志只能还原"当时"，当前状态查 DB。
- 日志摘要与 DB 终态短暂不一致不是 bug（不同视图、不同时点）；对账以 DB 终态为准。

## 出处

- 经典定义 / 与监控区别：https://cloudtuned.hashnode.dev/the-three-pillars-of-observability-and-why-you-need-all-three
- 控制理论根源：https://www.centreon.com/glossary/observability/
- 三支柱出处（Peter Bourgon 2017）：https://alivedise.github.io/backend-engineering-essentials/observability/three-pillars-logs-metrics-traces
- 三支柱局限 / 日志不可变：https://clickhouse.com/resources/engineering/three-pillars-of-observability
- 日志不可变/时间戳：https://www.azion.com/en/learning/observability/observability-three-pillars/
