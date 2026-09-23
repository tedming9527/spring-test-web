package org.example.springtestweb.category.job;

import org.example.springtestweb.category.entity.Category;
import org.example.springtestweb.category.entity.CategoryChangeEvent;
import org.example.springtestweb.category.mapper.CategoryChangeEventMapper;
import org.example.springtestweb.category.replica.mapper.ReplicaCategoryMapper;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

import java.lang.reflect.Proxy;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CategoryChangeEventJobTest {

  @Test
  void processEvents_shouldKeepFourResultCountsConserved() {
    Category staleReplica = category(9L);
    Category upToDateReplica = category(10L);
    CategoryChangeEvent successEvent = event(1L, 1001L, 10L, 0, "token-A");
    CategoryChangeEvent retryEvent = event(2L, 1002L, 10L, 0, "token-B");
    CategoryChangeEvent failedEvent = event(3L, 1003L, 10L, 4, "token-C");
    CategoryChangeEvent notUpdatedEvent = event(4L, 1004L, 10L, 0, "token-D");

    AtomicInteger syncCalls = new AtomicInteger();
    ReplicaCategoryMapper replicaMapper = replicaMapper(staleReplica, upToDateReplica, syncCalls);
    CategoryChangeEventMapper eventMapper = eventMapper();

    CategoryChangeEventJob job = new CategoryChangeEventJob(
        eventMapper, null, replicaMapper, new ObjectMapper());

    BatchProcessSummary summary = job.processEvents(List.of(
        successEvent, retryEvent, failedEvent, notUpdatedEvent));

    assertEquals(4, summary.getClaimedCount());
    assertEquals(1, summary.getSuccessCnt());
    assertEquals(1, summary.getRetryCnt());
    assertEquals(1, summary.getFailCnt());
    assertEquals(1, summary.getNotUpdateCnt());
    assertTrue(summary.isConserved());
    assertEquals(1, syncCalls.get());
  }

  private static CategoryChangeEventMapper eventMapper() {
    return (CategoryChangeEventMapper) Proxy.newProxyInstance(
        CategoryChangeEventMapper.class.getClassLoader(),
        new Class<?>[]{CategoryChangeEventMapper.class},
        (proxy, method, args) -> switch (method.getName()) {
          case "markSuccess" -> ((Long) args[0]) == 1L ? 1 : 0;
          case "rescheduleForRetry", "markFailed" -> 1;
          default -> throw new AssertionError("未预期的 EventMapper 调用: " + method.getName());
        });
  }

  private static ReplicaCategoryMapper replicaMapper(
      Category staleReplica, Category upToDateReplica, AtomicInteger syncCalls) {
    return (ReplicaCategoryMapper) Proxy.newProxyInstance(
        ReplicaCategoryMapper.class.getClassLoader(),
        new Class<?>[]{ReplicaCategoryMapper.class},
        (proxy, method, args) -> switch (method.getName()) {
          case "findById" -> switch (((Long) args[0]).intValue()) {
            case 1001 -> staleReplica;
            case 1002, 1003 -> null;
            case 1004 -> upToDateReplica;
            default -> throw new AssertionError("未预期的 categoryId: " + args[0]);
          };
          case "syncReplicaNameIfVersionMatches" -> {
            syncCalls.incrementAndGet();
            yield 1;
          }
          default -> throw new AssertionError("未预期的 ReplicaMapper 调用: " + method.getName());
        });
  }

  private static Category category(long version) {
    Category category = new Category();
    category.setCategoryVersion(version);
    return category;
  }

  private static CategoryChangeEvent event(
      long id, long categoryId, long categoryVersion, int retryCount, String processingToken) {
    CategoryChangeEvent event = new CategoryChangeEvent();
    event.setId(id);
    event.setCategoryId(categoryId);
    event.setCategoryVersion(categoryVersion);
    event.setPayload("{\"name\":\"lesson-name\"}");
    event.setRetryCount(retryCount);
    event.setProcessingToken(processingToken);
    return event;
  }
}
