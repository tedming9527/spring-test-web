# Spring Boot 企业后端实战学习进度

更新时间：2026-09-02

## 使用规则

- 本文件是后续课程进度的唯一事实源；聊天记忆只用于辅助，不得覆盖本文件。
- 每次教学开始前，先复核本文件、当前代码、Git 分支、最近提交和工作区状态。
- 状态只分为：
  - **已学习**：能够解释机制或完成指导问答。
  - **已实现**：当前仓库存在实现，并通过基本编译或静态检查。
  - **已验收**：取得运行日志、Redis `MONITOR`、MyBatis SQL次数或自动化测试证据。
- 手工实验如果没有把原始日志保存进仓库，必须注明证据边界；后续仍应固化为自动化测试。
- 每次教学任务或学习进度发生修改后，应在同一轮自动创建本地 commit；提交前检查完整差异，确保只包含本轮教学任务、学习进度及对应学习改动，无需再次询问是否提交。
- 教学与进度 commit 默认不得自动 push；push、merge 或部署仍需用户明确要求。

## 学习目标与项目边界

- 目标时间：2026年9月底。
- 总体目标：达到Java后端准中级水平，能够独立实现、调试和验收中小型Spring Boot功能。
- 当前主线：以企业真实功能交付为背景，贯通Spring事务、Redis、XXL-JOB、RabbitMQ、异步任务、Nacos、Sentinel与可观测性。
- 练习和验收唯一项目：`spring-test-web`。
- 企业参考项目：`/Users/dongdeming/Documents/vanke/daojia/mephisto/`，仅用于工程对照，不计入完成度。
- 教学方法：一个企业业务切片贯穿多个组件；每次只引入一个新机制和一个故障场景，按“需求 → 设计 → 实现 → 故障注入 → 运行验收 → 复盘”推进。

## 教学方案校正（2026-08-26）

### 已确认的教学误区

- 过去把Redis专项的“生产级完整闭环”误当成进入其他企业组件的前置条件，导致连续纵向深挖、横向交付不足。
- 过去围绕锁、TTL、事务时序和连接池等基础设施机制组织课程，真实业务需求、接口契约、任务调度、消息流转和运维结果不够突出。
- 手工实验帮助建立了正确机制认知，但同类时序证据重复较多；继续重复验收的边际收益已经低于进入XXL-JOB、RabbitMQ等新场景的收益。
- “严格验收”本身没有错；错误在于验收对象长期停留在单一组件，而没有升级为跨组件的企业交付验收。

### 新教学原则

1. **企业交付优先**：每一阶段先定义业务目标、接口或任务、数据变化、失败边界、回滚/补偿和可观测结果，再选择技术组件。
2. **螺旋式提升**：第一轮完成可运行功能，第二轮解决幂等与一致性，第三轮处理容量、降级和运维；不要求单个组件一次学到生产级才进入下一个组件。
3. **纵向业务切片**：围绕“分类变更”持续扩展，而不是为学习组件另造互不相关的Demo。
4. **一个新机制、一个真实故障**：每轮仍保持小步实验，但实验必须服务于当前企业业务切片。
5. **交付物验收**：至少同时检查接口/任务结果、数据库状态、缓存或消息状态、失败日志；必要时补自动化测试，不以口头复述或仅编译通过判定掌握。
6. **参考项目对照**：`mephisto`用于识别企业代码形态、命名和风险；实现、提交和验收仍只发生在`spring-test-web`。

### 贯穿课程的企业业务背景

以“商品分类变更及下游同步”作为主业务链：

```text
管理员修改分类
→ MySQL事务提交
→ Redis分类缓存失效
→ 记录待发布的分类变更事件
→ RabbitMQ发布事件
→ 下游消费者幂等更新同步记录
→ XXL-JOB扫描并补偿发送失败/超时事件
→ Nacos管理批次、超时和开关
→ Sentinel保护查询、更新和补偿入口
→ 日志与指标能够定位失败批次和恢复结果
```

这条链路对应企业开发中的接口交付、事务一致性、异步解耦、失败补偿、任务调度、动态配置和运行治理。

## 当前仓库基线

- 分支：`master`。
- 当前正式代码基线：`f698219`；该提交移除默认业务路径中的事务故障开关、两秒暂停和极小HikariCP参数。
- 当前`master`领先`origin/master` 2个本地提交；2026-08-26同步进度前只有 `docs/learning/PROGRESS.md` 待提交。
- 最近进度提交：
  - `f698219`：移除分类事务实验开关与默认极小连接池配置，调整回滚自动化测试。
  - `29755c5`：补齐分类事务提交与回滚的最小自动化回归。
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

**Redis与事务一致性的第一轮训练、事务最小自动化回归和默认业务路径清理已完成。阶段A退出条件已满足，当前主课题正式进入XXL-JOB企业任务：先设计“分类变更补偿任务”的任务表与状态机。**

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

证据边界：本次为手工容量实验；并发请求结果、Hikari异常和MySQL事务表由学员在会话中提供，原始HTTP响应、应用完整日志和连续连接池指标尚未保存进仓库。`TX_POOL_TEST` 暂停及HikariCP `maximum-pool-size=2`、`connection-timeout=1000ms` 已在 `f698219` 中从默认业务代码和配置中删除。

### 12. 分类事务提交与回滚的最小自动化回归

- 验收日期：2026-08-26。
- `CategoryTransactionalServiceTest` 已使用真实父分类缓存Key `category:children:{parentId}`，不再使用错误的分类ID和缺少冒号的Key。
- 正常提交测试验证：数据库保存新名称，事务 `afterCommit()` 删除旧缓存。
- 显式回滚测试验证：事务内 `UPDATE` 成功后，通过 `TestTransaction.flagForRollback()` 和同步 `end()` 结束外层测试事务；事务外重新查询时MySQL保持旧名称，Redis原缓存与旧名称均保留。
- 两条测试都在 `finally` 中恢复数据库名称并删除测试缓存，主要业务断言在清理前完成。
- 运行证据：`./mvnw -Dtest=CategoryTransactionalServiceTest test` 输出 `Tests run: 2, Failures: 0, Errors: 0, Skipped: 0`，`BUILD SUCCESS`。

证据边界：当前是依赖种子分类 `1101` 以及本机真实MySQL、Redis的集成测试；已通过前置断言在种子数据缺失时快速失败。回滚用例验证的是“外层事务显式回滚时数据与 `afterCommit()` 边界”，不再作为 `RuntimeException` 自动回滚的自动化证据；该异常规则仍保留之前的手工验收结论。

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

### 事务资源最小持有范围（已验收，证据待固化）

- 学员已理解事务不是免费的代码括号，会占用数据库连接、事务上下文，并可能在写入后持有行锁和MVCC/Undo资源。
- 学员能够推演连接池最大2、五个并发写请求时：前两个请求获得连接并进入Redis锁竞争，后三个请求先等待数据库连接；超过连接池等待时间才会失败。
- 已掌握原则：资源应在保证正确性的最小安全范围内持有，而不是无条件越早释放越好。
- 已将Redis锁等待移到非事务协调层；独立事务Bean只执行更新和提交后缓存失效，`./mvnw -q -DskipTests compile` 已通过。
- 学员于2026-08-26确认该拆分路径此前已完成运行验收；原始运行输出未保存进仓库，后续在业务链自动化测试中补证据，不再单独重复手工课程。

### Spring代理与同类自调用（已学习，分层已验收）

- 学员已理解：Spring为需要增强的Bean创建代理对象，代理再根据当前被调用方法是否匹配 `@Transactional` 决定是否开启事务。
- 学员已能区分：从一个Bean调用Spring注入的另一个Bean，调用可经过代理；同一对象内的 `this.method()` 只是普通Java内部调用，不会重新经过外层代理。
- 已确认本项目不应将事务更新逻辑移入同类 `private @Transactional` 方法，否则会因同类自调用绕过事务代理。
- 当前实现：`CategoryServiceImpl` 作为非事务锁协调层，Spring注入并调用独立的 `CategoryTransactionalService`；后者的 `@Transactional` 是唯一写事务边界。

状态边界：两个Bean拆分、编译和运行路径均已完成；自动化证据仍不完整，但不阻塞进入企业组件阶段。

### 锁协调层与事务更新层拆分（已验收，待自动化固化）

- `CategoryServiceImpl.updateName()` 已移除 `@Transactional`，负责查询parentId、等待和获得Redis锁，并在事务Bean代理返回后于 `finally` 解锁。
- 新增 `CategoryTransactionalService.updateName()`，作为独立Spring Bean，使用 `@Transactional` 执行数据库更新，并在 `afterCommit()` 删除缓存。
- 由于调用跨越两个Spring Bean，事务代理会在内层方法返回协调层之前完成提交或回滚；外层 `finally` 解锁不再早于事务完成。
- 静态证据：本轮 `./mvnw -q -DskipTests compile` 通过。

证据边界：学员确认已完成运行验收，原始日志未保存进仓库；后续通过跨组件业务链测试逐步固化，不再重复相同手工实验。

### 当前实验代码债务

- `TX_ROLLBACK_TEST`、`TX_POOL_TEST` 与两秒暂停已从默认业务代码删除。
- HikariCP `maximum-pool-size=2`、`connection-timeout=1000ms` 已从默认 `application.yml` 删除，应用恢复框架默认连接池配置。
- `TX_CONCURRENCY_TEST` 的600毫秒暂停已在验收后删除，未保留在正式实现中。
- Redis等待、锁和缓存Key仍有重复代码，当前先保留以利于学习；在一致性闭环后再重构。

## 企业交付导向的后续教学路线

### 阶段A：整理当前可交付基线（最多2课）

1. **已完成（2026-08-26）**：修正`CategoryTransactionalServiceTest`的真实缓存Key和测试清理，保留正常提交、回滚两条最小回归证据。
2. **已完成（2026-08-26，`f698219`）**：删除`TX_ROLLBACK_TEST`、`TX_POOL_TEST`、两秒暂停和默认极小HikariCP参数；回滚测试改为显式控制外层测试事务，默认应用恢复可交付状态。

退出条件：**已满足**。分类更新接口默认路径不再含实验开关与暂停，默认连接池配置已恢复，提交/显式回滚已有最小自动化保护。下一课直接进入XXL-JOB，不继续扩展Redis专项。

### 阶段B：XXL-JOB企业任务（约5～7课）

业务目标：实现“分类变更补偿任务”，扫描待处理或处理超时的记录并安全重试。

#### 2026-08-28：可靠同步模型与任务表（已实现，待自动化验收）

- 已从业务视角完成XXL-JOB与可靠同步的第一轮学习：本地变更与待同步记录、任务状态机、原子领取、幂等、版本防乱序、处理租约、退避重试、熔断限流、批处理及高低优先级让路。
- 已新增 `category_change_event` 表迁移 `V20260828__create_category_change_event_table.sql`，字段覆盖分类ID、分类版本、事件类型、最小同步快照、状态、重试、失败摘要、下次重试时间和审计字段；已为待处理扫描与分类版本查询建立组合索引。
- 已确认本机数据库表存在。`src/main/resources/db/rollback/R20260828__create_category_change_event_table.sql` 是开发环境人工回滚脚本，不由当前Flyway自动执行；任务表存在数据后不得用该脚本回滚。
- 2026-08-31（`b2994b7`）：已创建 `CategoryChangeEvent` 实体与 Mapper；已通过 `V20260831__add_category_version_to_goods_category.sql` 为 `goods_category` 增加分类版本字段。
- 2026-08-31：已实现分类名称的条件更新：仅当 `category_version` 与读取版本一致时才更新名称并原子递增版本；事务提交与显式回滚测试会断言名称、缓存和版本结果。
- 2026-08-31：已实现并验收本地事件表写入：分类条件更新成功后，在同一事务插入 `CATEGORY_NAME_CHANGED` 事件，依赖数据库默认值写入 `PENDING` 与 `retryCount=0`。`CategoryTransactionalServiceTest` 真实连接 MySQL 与 Redis 运行 2 项测试通过：提交路径验证分类、事件和缓存删除；显式回滚路径验证分类与缓存恢复且事件不存在。测试使用 UUID 隔离数据并清理提交或异常残留的测试事件。
- 2026-08-31（迁移 `ab19661`）：已增加 `processing_token`、`processing_lease_until` 及领取扫描索引，修正实体映射；已实现 XML 条件更新，仅允许到达执行时间的 `PENDING` 事件变为 `PROCESSING`，并使用数据库时间写入租约与审计时间。
- 2026-08-31：单条原子领取基础实验已验收。`CategoryChangeEventMapperTest` 真实连接 MySQL 执行：token-A 首次领取更新 1 行，token-B 再次领取更新 0 行，最终记录保持 `PROCESSING/token-A` 且租约晚于当前时间，测试事件最终删除。该证据验证了顺序竞争和条件更新，尚未验证两个独立线程同时竞争。
- 2026-09-01：两线程并发原子领取已验收。`CategoryChangeEventMapperTest` 使用两线程池、准备门闩和起跑门闩，让 token-A 与 token-B 同时竞争同一条 `PENDING` 事件。MyBatis 日志显示两个独立 `SqlSession` 和两个数据库连接执行同一条条件 UPDATE，影响行数分别为 1 和 0；本次 token-B 获胜，最终数据库记录为 `PROCESSING/token-B` 且租约有效。`./mvnw -Dtest=CategoryChangeEventMapperTest test` 运行 1 项测试通过，`BUILD SUCCESS`，测试事件最终删除。

- 2026-09-01：Service 级"扫描+批量原子领取"切片已验收。`CategoryChangeEventService.claimPendingEvents(batchSize, leaseSeconds, updater)`：`selectClaimableEvents`（XML，到期 PENDING，`ORDER BY id ASC LIMIT`）→ 每条独立生成 UUID token → 逐条 `claimPendingEvent` → 仅影响行数=1 的进入返回列表并回写 token；方法不加事务（claim 逐条独立、行锁/连接逐条释放）。`CategoryChangeEventServiceTest` 两线程门闩并发验收：合计恰好领取 2 条到期事件且集合恰为该 2 条（规模+集合双重断言，等价于"不漏+不重"）、未到期事件不被领取、DB 断言 PROCESSING/token 与返回值一致/租约在未来。测试实现共享库隔离三件套：快照差集（`excludeIds`）、动态 batchSize、finally 中删除自带数据并用 `LambdaUpdateWrapper` 显式 `set(..., null)` 还原被顺手领取的存量（`updateById` 忽略 null 字段，不能用于置空）。`./mvnw -Dtest=CategoryChangeEventServiceTest,CategoryChangeEventMapperTest,CategoryTransactionalServiceTest test` 输出 4 项测试全部通过，`BUILD SUCCESS`。教学观察：学员先后经历"指定赢家断言"失败（并发胜负不确定，必须断言整体一致性）与"`break` 误替 `continue` 导致还原失效"（库内留下 PROCESSING 残留实证），两个坑均已通过真实红灯纠正。

2026-09-02：真实从库结构与独立 Flyway 已验收。已在同一 MySQL 实例创建独立 schema `springtestweb_replica`；其 `goods_category` 由主表结构克隆，字段级双向比对均为 25 列且无差异，主库 40 条、从库初始 0 条。`ReplicaDataSourceConfig` 为从库注册独立 `DataSource` 和 `replicaFlyway`，迁移位于 `db/replica/migration`；首次运行将现有结构基线登记为 `20260901`，并应用/登记 `V20260902__create_goods_category.sql`。`ReplicaFlywayIntegrationTest` 使用真实 MySQL 断言从库 Flyway 当前版本 `20260902` 且从库 `goods_category` 存在；`./mvnw -Dtest=ReplicaFlywayIntegrationTest test` 输出 1 项测试通过、`BUILD SUCCESS`。本项由完整示例完成，已验收“多数据源 Flyway 分库管理”，尚未开始数据全量同步。

2026-09-02：从库分类全量初始化已验收。`ReplicaCategoryBootstrapService` 从主库 `CategoryMapper` 读取 40 条分类，使用从库限定 `JdbcTemplate` 显式保留主库 ID 并批量 `INSERT IGNORE`；写入后校验从库行数与主库一致。`ReplicaBootstrapRunner` 仅在 `replica-bootstrap` profile 启动，避免普通应用启动反复执行全量复制。`ReplicaCategoryBootstrapServiceTest` 对真实 MySQL 连续执行两次初始化，断言主从行数、ID、名称和版本一致；`./mvnw -Dtest=ReplicaCategoryBootstrapServiceTest test` 输出 1 项测试通过、`BUILD SUCCESS`。测试已经将 40 条主库分类写入从库；当前为基础全量初始化，尚未处理初始化期间并发写入，必须在增量任务启用前或业务写入暂停窗口执行。

下一步：接入 XXL-JOB 执行器，由任务领取本地事件并对已初始化的从库做增量同步。

2026-09-02：为跨电脑快速启动新增了 Docker 基础设施模板（**基础设施已验收**）。`docker/compose.yaml` 声明独立的 MySQL、Redis 与 XXL-JOB Admin；`docker/mysql/init/01-create-databases.sql` 首次创建主库、从库和调度库；`scripts/bootstrap-local-infra.sh` 先等待 MySQL 就绪，再仅在 `xxl_job` 无表时导入官方 v3.4.0 调度表，随后启动 Admin。`README.md` 固化了一条首次启动命令、服务地址及日常查看/停止命令。本机已重建独立 `xxl_job`（8 张调度表、4 条官方示例任务）；Docker 的失效 USTC 镜像站已移除，但官方 Docker Hub 当时持续 TLS 超时，因此基于已下载的官方 v3.4.0 源码和本机 Java 基础镜像构建了本地镜像 `spring-test-web-xxl-job-admin:3.4.0`。运行证据：容器 `spring-test-web-xxl-job-admin` 显示 `Up` 且映射 `0.0.0.0:8081->8080`，容器日志出现 `xxl-job admin start success`，`http://127.0.0.1:8081/xxl-job-admin/` 与当前局域网 IP 均返回 200 和登录页。当前尚未接入业务执行器，下一步验收为“执行器注册 → 人工/Cron 触发”。

2026-09-02：已学习执行器注册的最小模型（**已学习，未实现**）。学员能够区分执行器名称（业务项目在调度中心的“牌号”）与 `IP:port`（调度中心回调业务项目的实际地址），并能从 Docker 容器视角判断 `127.0.0.1` 指向容器自身、不能作为访问宿主机业务项目的注册地址。尚未向 `pom.xml` 引入 `xxl-job-core`、尚未创建执行器配置或 Job Handler；下一会话从这一个最小实现开始，不提前实现分类同步。

2026-09-02：XXL-JOB 执行器注册已验收。已引入与本机 Admin 对齐的 `xxl-job-core:3.4.0`，新增 `XxlJobConfig` 注册 `XxlJobSpringExecutor`，配置执行器名称 `spring-test-web-executor`、回调地址 `10.39.3.71:9999`、本地日志目录和 30 天保留期。`./mvnw -q -DskipTests compile` 已通过；应用启动后 `http://127.0.0.1:8080/` 返回 200，日志出现 `xxl-job remoting server start success` 且端口为 9999。Admin 中已人工创建同 AppName 的自动注册分组，名称为“Spring Test Web 执行器”；数据库 `xxl_job_group` 的运行证据显示该分组在线地址为 `http://10.39.3.71:9999/`，更新时间为 `2026-09-02 15:02:38`。未创建 Job Handler，未触及增量同步。生产级闭环未完成：当前 access token 为空，仅适用于本地学习环境。

2026-09-02：分类变更探针 Job Handler 已实现，待人工触发验收。新增 `category/job/CategoryChangeEventJob`，以 `@XxlJob("categoryChangeEventProbe")` 暴露最小 Handler；方法读取 `XxlJobHelper.getJobParam()` 并通过 `XxlJobHelper.log(...)` 输出执行参数。`./mvnw -q -DskipTests compile` 通过；应用启动日志已出现 `xxl-job register jobhandler success, name:categoryChangeEventProbe`，随后 9999 端口启动成功。该 Handler 当前不调用 `CategoryChangeEventService`，确保本课仅验证 Admin 到业务 JVM 的远程调度入口。缺失证据：尚未在 Admin 对该 Handler 执行一次并确认调度日志中的参数输出，故状态为**已实现，未验收**。下一步：创建/保存 BEAN 任务后以参数 `lesson-01` 执行一次，核对执行成功与 Handler 日志，再进入事件领取到从库同步。

2026-09-03：分类变更探针 Job Handler 已验收。执行器自动注册记录为 `http://10.39.3.71:9999/`；Admin 任务“分类变更探针”在 15:18:30 人工触发（参数为 `hello world`）的 `xxl_job_log` 记录显示 `trigger_code=200`、`handle_code=200`、执行器地址为该 URL、Handler 为 `categoryChangeEventProbe`。执行器本地文件 `data/applogs/xxl-job/jobhandler/2026-09-03/267.log` 进一步记录 `Param:hello world` 与 `category change event probe paramter=hello world`，证明 Admin 参数已经进入 Java Handler。其后 15:20 的 Cron 触发也得到双 200。本课已验收“执行器自动注册 → Admin 人工/Cron 触发 → Handler 文件日志”的最小闭环；当前仍未触及事件领取或从库增量同步。下一步：让 Job 入口只负责解析批量参数并调用既有 `CategoryChangeEventService.claimPendingEvents()`，先验收一次任务领取，不写从库。

2026-09-03：分类变更事件 Job 参数解析与领取已验收。`CategoryChangeEventJob` 将 XXL-JOB 参数解析为批量大小，并调用既有 `CategoryChangeEventService.claimPendingEvents(batchSize, 60, "xxl-job")`；Job 入口未写入从库。运行日志记录 `Param:2`、`parameter=2, claimedCount=2`、`handleCode=200`，证明参数进入 Java Handler、Service 实际领取 2 条且执行成功。此前数据库查询已记录两条教学事件（`id=89`、`id=90`）从 `PENDING` 变为 `PROCESSING`，均有不同的 `processing_token`、60 秒租约及 `updater=xxl-job`。同一时段分类更新接口已在主库写入 `CATEGORY_NAME_CHANGED` 事件；本轮不将从库未更新视为失败，因为领取 Job 的职责止于状态迁移，尚未实现下游同步。证据边界：学员确认以现有日志和数据库状态验收，未额外保存该次日志对应业务事件行的最终查询结果。生产级边界未闭环：当前非法、空或非正批量参数会被捕获并静默回退为 `2`，尚未形成可见失败。下一步：只实现已领取事件向从库写入的最小同步切片。

1. 设计任务表和状态机，明确待处理、处理中、成功、失败及重试次数。
2. 接入XXL-JOB执行器，Job入口只负责参数解析和调用Service。
3. 验证人工触发、Cron触发、失败上报和执行日志。
4. 故障实验：同一批次重复执行，使用数据库状态或唯一约束保证幂等。
5. 故障实验：两个执行器并发处理，验证抢占、超时恢复和任务不重复完成。

退出条件：任务能够重复触发、失败可见、重试可恢复，数据库最终结果正确。

### 阶段C：RabbitMQ业务事件（约10～15课）

业务目标：分类更新提交后发布变更事件，下游消费者异步记录同步结果。

1. 学习Exchange、Queue、Binding、Routing Key并完成一次真实发送与消费。
2. 明确消息契约、事件ID、业务ID、版本和发生时间。
3. 验证手动ACK/NACK、消费异常、重试和死信队列。
4. 故障实验：同一消息投递两次，消费者使用事件ID或业务唯一约束实现幂等。
5. 故障实验：数据库提交成功但MQ发送失败，引入本地事件表/Outbox，由阶段B的XXL-JOB补偿发送。
6. 验证消息积压、消费速度、失败日志和恢复结果。

退出条件：消息不因瞬时失败永久丢失，重复消息不造成重复业务结果，失败事件可查询、可补偿。

### 阶段D：异步、配置与流量治理（约8～12课）

1. `@Async`与线程池：线程数、队列、拒绝策略、上下文和异常边界；对比何时使用本地异步、何时使用MQ。
2. Nacos：按环境管理任务批次、重试次数、超时和功能开关，验证配置变化与错误配置风险。
3. Sentinel：保护分类查询、更新和补偿入口，验证限流、熔断、降级和恢复。
4. 可观测性：记录业务事件ID、任务批次、锁等待、事务耗时、消息重试和最终状态。

退出条件：能够从一次失败请求或事件ID追踪接口、事务、缓存、消息、任务补偿的完整链路。

### 第二轮回访的Redis课题（不阻塞当前主线）

- 数据库操作超过5秒时的锁租约、续期或Fencing Token。
- 数据库提交后Redis删除失败的补偿与告警。
- 退避、随机抖动、热点Key治理和容量指标。

这些问题在XXL-JOB幂等、MQ可靠投递和Sentinel治理阶段分别回访，使同一原则在不同组件中螺旋加深。

## 每次继续课程前的复核清单

```text
1. 读取本文件。
2. 查看 git branch --show-current、git status -sb、git log -5。
3. 检查 CategoryServiceImpl、RedisService 与相关测试的当前代码。
4. 确认没有遗留临时sleep、短锁TTL或调试输出。
5. 先确认当前企业业务切片和本课交付物；只引入一个新机制和一个故障场景。
6. 每完成一课判断“已学习、已实现、已验收”，但不得因单个基础设施边界未生产级闭环而长期阻塞下一业务阶段。
```
