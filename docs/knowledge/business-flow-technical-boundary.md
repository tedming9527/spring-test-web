# 知识卡：业务流程与技术边界

- 领域：后端 / 代码设计
- 日期：2026-09-28
- 关联课程：基础可观测性第 1 课（XXL-JOB 事件处理与异常翻译）
- 网站专题候选：第一性原理

## 一句话核心

当责任或语义发生变化时建立边界：业务流程负责决定“什么时候做什么”，技术边界负责实现“具体怎么做”；实现方式可以替换，但不能把业务决定藏起来。

## 三步推导

1. 业务代码需要直接表达状态判断、重试、成功和失败等决策，否则读者无法确认系统会做什么。
2. 数据库、缓存、消息和 HTTP 调用包含框架 API、返回值和技术异常，这些细节会遮挡业务决策，也会随技术方案变化。
3. 因此主流程保留业务判断与明确的 `return`，边界函数封装外部调用，并把技术异常翻译成上层可理解的业务阶段，同时保留原始 `cause`。

## 检验尺

> 不打开边界函数，能否从主流程直接读出“遇到什么情况，系统做什么决定”？能，并且边界函数只回答“具体怎么做”，说明职责基本清晰。

提取方法前再检查：

1. 方法名是否准确表达职责？
2. 输入和输出是否表达真实语义？
3. 调用方是否仍能看见关键控制流？
4. 是否隐藏了 `return`、重试或状态转换？
5. 提取后是否确实降低理解成本？

## 项目实例

```text
processEvent()：决定解析失败、从库缺失、版本落后、同步失败时如何处理
→ findReplica()/syncReplica()：调用 Mapper 并翻译技术异常
→ scheduleRetry()/markSuccess()：执行明确的事件状态转换
```

边界翻译示例：

```java
try {
  return replicaCategoryMapper.findById(event.getCategoryId());
} catch (Exception error) {
  throw new RuntimeException(
      "QUERY_REPLICA_FAILED: 查询从库分类失败",
      error
  );
}
```

## 易错点

- 把完整业务判断提取成返回 `boolean` 的方法，导致 `true`、`false` 隐藏真实业务含义。
- 在返回 `void` 的辅助方法中悄悄重试或修改状态，调用方仍继续执行成功路径。
- 捕获底层异常后只改错误文字，没有把原异常作为 `cause` 保留下来。
- 为了让方法变短而切分代码，却使读者必须在多个方法之间跳转才能理解业务顺序。
- 把“技术边界”误解成固定框架术语；它是通用设计思想，具体实现常被称为 adapter、infrastructure boundary 或 exception translation。

## 出处

- 项目代码：`src/main/java/org/example/springtestweb/category/job/CategoryChangeEventJob.java`
- 教学策略：`docs/learning/TEACHING_STRATEGY.md`
- 课程讨论：2026-09-28 业务流程可读性、边界函数封装与异常翻译练习
