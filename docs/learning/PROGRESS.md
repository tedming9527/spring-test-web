# Spring Boot + Redis 学习进度

更新时间：2026-08-26

## 使用规则

- 本文件是后续课程进度的唯一事实源；聊天记忆只用于辅助，不得覆盖本文件。
- 每次教学开始前，先复核本文件、当前代码、Git 分支、最近提交和工作区状态。
- 状态只分为：
  - **已学习**：能够解释机制或完成指导问答。
  - **已实现**：当前仓库存在实现，并通过基本编译或静态检查。
  - **已验收**：取得运行日志、Redis `MONITOR`、MyBatis SQL次数或自动化测试证据。
- 手工实验如果没有把原始日志保存进仓库，必须注明证据边界；后续仍应固化为自动化测试。
- 每次更新本文件后，应在同一轮创建本地 commit；提交前检查完整差异，确保只包含本轮学习进度及对应学习改动。
- 进度 commit 不代表自动 push、merge 或部署；这些操作仍需用户明确要求。

## 学习目标与项目边界

- 目标时间：2026年9月底。
- 总体目标：达到Java后端准中级水平，能够独立实现、调试和验收中小型Spring Boot功能。
- 当前专项：Redis缓存正确性、并发控制、数据库事务与缓存一致性。
- 练习和验收唯一项目：`spring-test-web`。
- 企业参考项目：`/Users/dongdeming/Documents/vanke/daojia/mephisto/`，仅用于工程对照，不计入完成度。
- 教学方法：一个机制、一个真实接口、一个故障场景，按“复现 → 分析 → 修复 → 运行验收 → 顶层原则”推进。

## 当前仓库基线

- 分支：`master`。
- 当前正式代码基线：`6e04208`；该提交将Redis锁协调与数据库事务更新拆分为两个Spring Bean。
- 学习进度同步提交：`b72e123`（直接提交到个人训练仓库 `master`，未推送）。
- 最近进度提交：
  - `6e04208`：拆分事务边界，等待Redis锁不再位于写事务内。
  - `014ad79`：删除 `TX_CONCURRENCY_TEST` 的600毫秒暂停，保留事务监听实现。
  - `b9975e0`：加入 `TX_CONCURRENCY_TEST` 的600毫秒并发故障注入（提交信息为“添加事务监听”）。
  - `b6f6c5f`：添加事务监听并同步事务时序学习进度。
  - `f8ce45d`：验收Spring事务提交与回滚实验。
  - `86d68df`：添加运行时异常回滚实验开关。
  - `d470323`：写请求添加方法级事务。
  - `e6fbe31`：写请求参与同一把Redis锁。
  - `7fce08b`、`33b781f`、`548e915`：缓存等待、时间预算与超时收尾。
  - `ee65ca2`、`2cba3c3`、`24dff02`：Lua原子解锁。
- 2026-08-25同步进度前工作区干净；该次同步只修改 `docs/learning/PROGRESS.md`。

## 当前阶段

Redis缓存击穿保护、锁所有权、时间预算、10并发重建、基础读写共享锁、Spring方法级事务提交与运行时异常回滚均已完成手工运行验收。

Spring代理调用边界已学习：学员已理解代理对象包裹Spring Bean，增强规则按方法匹配；Bean之间通过Spring注入引用的外部调用通常经过代理，同类 `this` 自调用不会重新经过代理。

**当前主课题：连接池容量实验已完成手工运行验收；随后回到锁协调层与事务更新层拆分的缺失证据，并将事务实验固化为自动化测试。**

当前代码在 `updateName()` 内执行：

```text
非事务协调层查询parentId并等待Redis锁
→ 获得Redis锁
→ 调用独立事务Bean
→ UPDATE（事务内，尚未提交）
→ Spring事务代理提交或回滚MySQL
→ afterCommit删除Redis缓存（仅提交成功）
→ 事务代理返回协调层
→ 协调层finally释放Redis锁
```

正常提交、回滚和并发修复路径均已手工验收。

资源持有原则已学习：资源应在保证正确性的最小安全范围内持有；数据库连接应尽量晚获取、尽早释放，但Redis写锁必须覆盖到数据库事务完成。

## 已验收

### 1. Cache-Aside与缓存语义

- 接口：`GET /api/interfaces/categories/1100/children`。
- 缓存：`category:children:1100`。
- 锁：`lock:category:children:1100`。
- 首次缓存未命中时查询MySQL并写入Redis；第二次请求不再出现SQL。
- 更新分类后删除对应父分类缓存，下一次读取获得数据库新值。
- `null` 表示缓存未命中；`[]` 表示数据库查询成功但业务结果为空。
- 空列表TTL为2分钟，正常列表TTL为10分钟。

证据边界：课程中通过MyBatis日志和Redis状态手工验证，原始输出未保存进仓库。

### 2. Lua锁所有权与原子解锁

- Lua使用“比较UUID后删除”的单个原子脚本。
- `MONITOR` 已观察到 `EVALSHA`，并在 `[lua]` 上下文中出现连续的 `get` / `del`。
- 已验收正确UUID能够删除锁。
- 已验收错误UUID返回失败且不能删除锁。
- 已验收旧租约A过期后，A不能删除后来创建的新租约B。
- 锁TTL已恢复为正式实验值5秒。

证据边界：课程中保留了完整Redis `MONITOR` 输出，但尚未固化为自动化测试。

### 3. 等待者重试与900ms时间预算

- 使用 `System.nanoTime()` 计算单调截止时间。
- 总等待预算900ms，单次最多等待300ms。
- 每轮回到循环顶部重新观察缓存；超时后进行最后一次缓存读取。
- 已复现并修复“第三次sleep结束后缓存已建立，却直接抛超时”的缺陷。
- 外部锁持续存在时，实测约901.5ms后结束等待，没有绕过锁访问数据库。
- 外部锁在预算内过期时，请求能够重新获得新租约、查询数据库、写缓存并安全解锁。

### 4. 10个冷缓存并发请求

- `MONITOR` 同时出现10次初始缓存 `GET` 和10次 `SET NX` 竞争。
- 只有一个UUID获得锁，完成获锁后二次检查、一次缓存 `SET EX 600` 和一次Lua解锁。
- 其余9个请求约300ms后读取缓存并命中，没有继续竞争锁。
- 学员现场确认10个请求全部成功、响应一致、MyBatis仅出现一次数据库查询、锁无残留、缓存TTL接近600秒。

证据边界：手工运行验收通过，原始curl、SQL和MONITOR输出未保存进仓库。

### 5. 基础读写一致性与共享锁

- 已复现未协调时的真实竞态：写请求先 `DEL` 缓存，早先开始的读请求随后 `SET` 旧值并获得新的600秒TTL，最终MySQL为新值、Redis为旧值。
- 写请求现已参与与读请求相同的 `lock:category:children:{parentId}`。
- 通过临时600ms读临界区验证：
  - 读请求A持锁并写入旧缓存后，用UUID-A执行Lua解锁。
  - 写请求B等待A，随后以UUID-B获得新租约、更新数据库、删除A写入的旧缓存，并执行第二次Lua解锁。
  - 最终缓存和锁均不存在，下一次读取能够从MySQL重建新值。
- 基础实验阶段写锁释放曾放入 `finally`；当前事务时序修复已改为在 `afterCompletion` 中释放。中断处理会恢复中断标记。

证据边界：手工Redis `MONITOR` 验收通过，尚未固化为自动化测试。

### 6. Spring方法级事务基础

- `updateName()` 已添加 `@Transactional`。
- 学员能够区分“SQL语句执行成功”“Java方法返回”“Spring事务代理提交”三个时间点。
- 正常路径：使用 `TX_COMMIT_TEST` 调用更新接口，确认请求成功且MySQL保留新名称。
- 回滚路径：`TX_ROLLBACK_TEST` 在数据库 `UPDATE` 后触发未捕获的 `RuntimeException`，确认接口失败且MySQL未保留该次名称。
- 已理解默认规则：未捕获的运行时异常回滚；受检异常默认不回滚。

关联提交：`d470323`、`86d68df`、`f8ce45d`。

证据边界：手工运行验收通过，尚未建立事务自动化测试。

### 7. Redis解锁早于MySQL事务提交的竞态复现

- 验收日期：2026-08-25。
- 在 `updateName()` 的 Redis 解锁后、事务方法返回前临时暂停3秒，稳定放大事务尚未提交的窗口。
- 写请求在事务内完成 `UPDATE` 和缓存删除，随后释放Redis锁；并发读请求在写事务提交前获得锁，从MySQL读取旧的已提交值并写回Redis。
- 写事务最终提交后，学员现场确认最终状态为：MySQL保存新值，Redis保留旧值。
- 该结果证明基础读写共享锁没有覆盖Spring事务真正提交的时刻，旧值回写是可复现竞态，不是理论推测。

证据边界：本次为手工故障注入实验；学员确认了最终MySQL与Redis值，原始请求、MyBatis事务日志和Redis `MONITOR` 输出尚未保存进仓库。临时3秒暂停不属于正式实现，修复前必须删除。

### 8. 事务提交后的缓存失效与锁释放——正常提交路径

- 验收日期：2026-08-25。
- `updateName()` 已通过 `TransactionSynchronizationManager.registerSynchronization()` 注册事务回调。
- 数据库更新成功后，`afterCommit()` 删除 `category:children:{parentId}`；`afterCompletion()` 在事务结束后释放对应Redis锁。
- `./mvnw -q -DskipTests compile` 已通过。
- 学员按正常更新路径现场确认：接口成功、MySQL保存新值、提交后Redis旧缓存不存在、锁无残留，下一次GET能够读取新值并重建缓存。

证据边界：本次为手工运行验收，原始HTTP响应、MySQL查询、Redis `MONITOR` 与Redis CLI输出未保存进仓库。

### 9. 事务回滚时的缓存保留与锁释放

- 验收日期：2026-08-25。
- 使用 `TX_ROLLBACK_TEST` 在数据库 `UPDATE` 后抛出未捕获的 `RuntimeException`。
- 学员现场确认接口失败、MySQL保持回滚前名称、Redis缓存内容与回滚前完全一致、Redis锁无残留。
- 该结果证明回滚路径不会执行 `afterCommit()` 删除正确缓存，但会执行 `afterCompletion()` 释放锁。

证据边界：本次为手工运行验收，原始HTTP响应、MySQL查询、Redis `MONITOR` 与Redis CLI输出未保存进仓库；故障注入开关仍需在自动化测试建立后删除。

### 10. 事务提交前的并发读写窗口修复

- 验收日期：2026-08-25。
- 在缓存和锁初始不存在、写请求先获得锁的前提下，于 `UPDATE` 后、事务提交前临时暂停600毫秒，读请求总等待预算仍为900毫秒。
- 学员现场确认读请求成功且未超时，最终MySQL与Redis缓存名称均为 `TX_CONCURRENCY_TEST`，Redis锁无残留。
- 结果证明读请求没有在写事务提交前获得锁并把旧数据库值写回缓存；写事务完成后，读请求能够在预算内获得锁、读取已提交新值并重建缓存。

证据边界：本次为手工故障注入实验；学员确认了前置条件与最终状态，原始并发请求、MyBatis事务日志和Redis `MONITOR` 输出未保存进仓库。临时600毫秒暂停已在验收后删除，正式实现重新编译通过。

### 11. HikariCP连接池容量与等待超时实验

- 验收日期：2026-08-26。
- 实验配置：HikariCP `maximum-pool-size=2`、`connection-timeout=1000ms`；`TX_POOL_TEST` 在事务完成数据库更新后持有连接2秒。
- 同时发起5个写请求，现场结果为2个成功、3个失败。
- 三个失败请求均抛出：`SQLTransientConnectionException: HikariPool-1 - Connection is not available, request timed out after 1001ms (total=2, active=2, idle=0, waiting=1)`。
- MySQL `information_schema.innodb_trx` 同时观察到连接 `15484`、`15487`，二者开始时间一致、状态为 `RUNNING`、`trx_query=NULL`；这表示两个请求已经取得连接并处于事务内Java暂停阶段，当时没有SQL正在执行。
- 已确认失败发生在HikariCP借连接阶段，三个失败请求尚未获得数据库连接，不是SQL执行超时、锁等待超时或HTTP客户端超时。
- 已理解容量关系：两个事务持有连接约2000ms，大于后续请求1000ms的借连接等待上限，因此连接池满载时后续请求必然超时。
- 已区分浏览器请求与后端资源超时：浏览器原生 `fetch` 默认没有业务请求总超时，本次约1秒失败由HikariCP主动抛错并由Spring返回响应。
- 另观察到旧连接 `8305` 属于本机 `springtestweb` 的空闲事务；其锁定行、修改行和锁表数量均为0，后续容量实验事务列表中已不再出现，确认它不属于本轮5并发请求。

证据边界：本次为手工容量实验；并发请求结果、Hikari异常和MySQL事务表由学员在会话中提供，原始HTTP响应、应用完整日志和连续连接池指标尚未保存进仓库。当前 `TX_POOL_TEST` 暂停及极小连接池参数是故障注入配置，不是生产推荐值；自动化容量测试建立后应移除或隔离。

## 已实现但尚未完成生产级闭环

### 事务与Redis时序

当前 `@Transactional updateName()` 已将缓存删除移至 `afterCommit()`，并将锁释放移至 `afterCompletion()`。正常提交、回滚与并发修复路径均已验收：

```text
写事务持有Redis锁并执行UPDATE
→ MySQL提交
→ afterCommit删除旧缓存
→ afterCompletion释放Redis锁
→ 读请求获得锁并读取已提交的新值
```

验收不能只观察Redis，必须同时记录MyBatis/事务日志、最终MySQL值和最终缓存值。

### 事务资源最小持有范围（已学习，拆分已实现待验收）

- 学员已理解事务不是免费的代码括号，会占用数据库连接、事务上下文，并可能在写入后持有行锁和MVCC/Undo资源。
- 学员能够推演连接池最大2、五个并发写请求时：前两个请求获得连接并进入Redis锁竞争，后三个请求先等待数据库连接；超过连接池等待时间才会失败。
- 已掌握原则：资源应在保证正确性的最小安全范围内持有，而不是无条件越早释放越好。
- 已将Redis锁等待移到非事务协调层；独立事务Bean只执行更新和提交后缓存失效，`./mvnw -q -DskipTests compile` 已通过。
- 尚缺运行证据：等待Redis锁期间未开启数据库事务，以及正常提交、回滚和并发路径在新边界下仍正确。

### Spring代理与同类自调用（已学习，分层已实现待验收）

- 学员已理解：Spring为需要增强的Bean创建代理对象，代理再根据当前被调用方法是否匹配 `@Transactional` 决定是否开启事务。
- 学员已能区分：从一个Bean调用Spring注入的另一个Bean，调用可经过代理；同一对象内的 `this.method()` 只是普通Java内部调用，不会重新经过外层代理。
- 已确认本项目不应将事务更新逻辑移入同类 `private @Transactional` 方法，否则会因同类自调用绕过事务代理。
- 当前实现：`CategoryServiceImpl` 作为非事务锁协调层，Spring注入并调用独立的 `CategoryTransactionalService`；后者的 `@Transactional` 是唯一写事务边界。

状态边界：两个Bean拆分和编译均已完成；尚未完成“等待Redis锁时事务未开启”的运行验收。

### 锁协调层与事务更新层拆分（已实现，待运行验收）

- `CategoryServiceImpl.updateName()` 已移除 `@Transactional`，负责查询parentId、等待和获得Redis锁，并在事务Bean代理返回后于 `finally` 解锁。
- 新增 `CategoryTransactionalService.updateName()`，作为独立Spring Bean，使用 `@Transactional` 执行数据库更新，并在 `afterCommit()` 删除缓存。
- 由于调用跨越两个Spring Bean，事务代理会在内层方法返回协调层之前完成提交或回滚；外层 `finally` 解锁不再早于事务完成。
- 静态证据：本轮 `./mvnw -q -DskipTests compile` 通过。

证据边界：尚未重启应用并运行正常提交、回滚、并发及外部锁等待实验；不能仅凭编译认定运行时事务边界正确。

### 当前实验代码债务

- `TX_ROLLBACK_TEST` 是手工故障注入开关，不应作为正式业务逻辑长期保留。
- `TX_POOL_TEST` 的2秒暂停以及 HikariCP `maximum-pool-size=2`、`connection-timeout=1000ms` 是容量故障注入，不应作为正式配置长期保留。
- `TX_CONCURRENCY_TEST` 的600毫秒暂停已在验收后删除，未保留在正式实现中。
- 完成事务自动化测试后，应删除该业务代码开关。
- Redis等待、锁和缓存Key仍有重复代码，当前先保留以利于学习；在一致性闭环后再重构。

## 下一步顺序

1. 重启应用，补齐新边界下正常提交、回滚、并发读写与外部Redis锁等待路径的运行证据，尤其证明等待Redis锁时未开启数据库事务。
2. 完成并运行 `CategoryTransactionalServiceTest`，将事务提交、回滚和并发旧值回写固化为JUnit 5自动化测试。
3. 将容量实验固化为可重复测试或脚本，保存5并发结果、Hikari指标和MySQL事务快照；随后删除或隔离 `TX_POOL_TEST` 与极小连接池配置。
4. 验证数据库查询时间超过5秒锁租约、数据库异常和缓存删除失败。
5. 学习限流、熔断、退避、随机抖动、隔离和可观测性。

## 每次继续课程前的复核清单

```text
1. 读取本文件。
2. 查看 git branch --show-current、git status -sb、git log -5。
3. 检查 CategoryServiceImpl、RedisService 与相关测试的当前代码。
4. 确认没有遗留临时sleep、短锁TTL或调试输出。
5. 只推进“下一步顺序”中的第一项，取得证据后再更新本文件。
```
