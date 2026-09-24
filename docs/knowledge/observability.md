# 知识卡：可观测性（Observability）

- 领域：后端 / 可观测性
- 日期：2026-09-24
- 关联课程：基础可观测性第 1 课（XXL-JOB 分类变更补偿任务）
- 完整讲义：`docs/learning/observability-foundations.md`

## 一句话核心

系统内部状态变化默认是**静默**的；可观测性 = 主动为关键状态变化留下**可由外部证据还原**的痕迹，使"发生了什么、为什么"能从事后重建。

## 关键要点

1. **黑箱 + 痕迹**：系统是黑箱，出事时不能重放（那是测试的特权），唯一能依赖的是痕迹。
2. **可观测 ≠ 有日志**：而是"痕迹完备到能还原因果"——每个关键状态变化要有定位键（是谁）、关联键（属于哪个批次/事件）、原因（为什么）。
3. **三支柱**：日志（发生了什么，不可变事件）、指标（随时间表现，聚合数值）、追踪（单条请求路径）。框架出处：Peter Bourgon 2017。
4. **三支柱的局限**：支柱描述数据类型，不是结果——"数据齐 ≠ 因果可还原"。
5. **DB（状态源）vs 日志（过程源）**：数据库回答"现在是什么状态"（状态事实源）；日志回答"当时发生了什么"（过程源，不可变追加）。不是两套事实源，是分层。
6. **日志聚合（summary 行）常见且规范**，判据：聚合不冒充状态事实源（状态必须查 DB）。

## 检验尺

> 某条状态变化的因果，能否仅凭日志被外部完整还原？能 → 可观测；还原不出 → 这里该留痕迹。

## 易错点

- 只记异常、不记成功 → 因果链断裂（缺正常路径痕迹）。
- 业务结果日志放 `finally` → 错误。`finally` 是收尾职责（释放资源）；兜底失败由 `STATE_NOT_UPDATED` 兜住，结果日志应放 `switch`。
- 日志摘要与 DB 终态短暂不一致不是 bug（不同视图、不同时点）；对账以 DB 终态为准。

## 出处

- 经典定义 / 与监控区别：https://cloudtuned.hashnode.dev/the-three-pillars-of-observability-and-why-you-need-all-three
- 控制理论根源：https://www.centreon.com/glossary/observability/
- 三支柱出处（Peter Bourgon 2017）：https://alivedise.github.io/backend-engineering-essentials/observability/three-pillars-logs-metrics-traces
- 三支柱局限 / 日志不可变：https://clickhouse.com/resources/engineering/three-pillars-of-observability
- 日志不可变/时间戳：https://www.azion.com/en/learning/observability/observability-three-pillars/
