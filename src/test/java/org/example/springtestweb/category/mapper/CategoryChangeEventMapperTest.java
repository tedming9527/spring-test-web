package org.example.springtestweb.category.mapper;

import org.example.springtestweb.category.entity.Category;
import org.example.springtestweb.category.entity.CategoryChangeEvent;
import org.example.springtestweb.category.job.BatchProcessSummary;
import org.example.springtestweb.category.job.CategoryChangeEventJob;
import org.example.springtestweb.category.replica.mapper.ReplicaCategoryMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SpringBootTest
public class CategoryChangeEventMapperTest {
  @Autowired
  private CategoryChangeEventMapper eventMapper;
  @Autowired
  private CategoryChangeEventJob categoryChangeEventJob;

  @Test
  void claimPendingEvent_shouldAllowOnlyFirstClaim() {
    CategoryChangeEvent event = new CategoryChangeEvent();
    event.setCategoryId(1101L);
    event.setCategoryVersion(System.currentTimeMillis());
    event.setEventType("CATEGORY_NAME_CHANGED");
    event.setPayload("{\"name\":\"CLAIM_TEST\"}");



    ExecutorService executor = Executors.newFixedThreadPool(2);
    try {
      CountDownLatch readyLatch = new CountDownLatch(2);
      CountDownLatch startLatch = new CountDownLatch(1);

      eventMapper.insert(event);

      Future<Integer> futureA = executor.submit(() -> {
        readyLatch.countDown();
        startLatch.await();
        return eventMapper.claimPendingEvent(event.getId(), "token-A", 60, "system");
      });

      Future<Integer> futureB = executor.submit(() -> {
        readyLatch.countDown();
        startLatch.await();
        return eventMapper.claimPendingEvent(event.getId(), "token-B", 60, "system");
      });
      readyLatch.await();
      startLatch.countDown();

      int resultA = futureA.get();
      int resultB = futureB.get();

      assertEquals(1, resultA + resultB);
      CategoryChangeEvent claimedEvent = eventMapper.selectById(event.getId());
      assertEquals("PROCESSING", claimedEvent.getStatus());
      assertNotNull(claimedEvent.getProcessingLeaseUntil());
      assertTrue(claimedEvent.getProcessingLeaseUntil().isAfter(LocalDateTime.now()));
      if (resultA == 1) {
        assertEquals("token-A", claimedEvent.getProcessingToken());
      }
      if (resultB == 1) {
        assertEquals("token-B", claimedEvent.getProcessingToken());
      }

    } catch (Exception e) {
      throw new RuntimeException(e);
    } finally {
      if (event.getId() != null) {
        eventMapper.deleteById(event.getId());
      }
      executor.shutdown();
    }
  }

  @Test
  void markSuccess_shouldRejectStaleProcessingToken() {
    CategoryChangeEvent event = new CategoryChangeEvent();
    event.setCategoryId(1101L);
    event.setCategoryVersion(System.currentTimeMillis());
    event.setEventType("CATEGORY_NAME_CHANGED");
    event.setPayload("{\"name\":\"CLAIM_TEST\"}");
    try {
      eventMapper.insert(event);

      int claimed = eventMapper.claimPendingEvent(event.getId(), "token-B", 60, "system");
      assertEquals(1, claimed);
      int updated = eventMapper.markSuccess(event.getId(), "token-A", "system");
      assertEquals(0, updated);

      CategoryChangeEvent dbEvent = eventMapper.selectById(event.getId());
      assertNotNull(dbEvent);
      assertEquals("PROCESSING", dbEvent.getStatus());
      assertEquals("token-B", dbEvent.getProcessingToken());
    } finally {
      if (event.getId() != null) {
        eventMapper.deleteById(event.getId());
      }
    }
  }
  @Test
  void rescheduleForRetry_shouldRejectStaleProcessingToken() {
    CategoryChangeEvent event = new CategoryChangeEvent();
    event.setCategoryId(1101L);
    event.setCategoryVersion(System.currentTimeMillis());
    event.setEventType("CATEGORY_NAME_CHANGED");
    event.setPayload("{\"name\":\"CLAIM_TEST\"}");
    try {
      int effectRows = eventMapper.insert(event);
      assertEquals(1, effectRows);
      int claimedB =  eventMapper.claimPendingEvent(event.getId(), "token-B", 60, "system");
      assertEquals(1, claimedB);

      int updated = eventMapper.rescheduleForRetry(event.getId(), "token-A", "system", "stale");
      assertEquals(0, updated);
      CategoryChangeEvent dbEvent = eventMapper.selectById(event.getId());
      assertEquals("PROCESSING", dbEvent.getStatus());
      assertEquals("token-B", dbEvent.getProcessingToken());
      assertEquals(0, dbEvent.getRetryCount());
    } finally {
      if (event.getId() != null) {
        eventMapper.deleteById(event.getId());
      }
    }
  }
  @Test
  void markFailed_shouldRejectStaleProcessingToken() {
    CategoryChangeEvent event = new CategoryChangeEvent();
    event.setCategoryId(1101L);
    event.setCategoryVersion(System.currentTimeMillis());
    event.setEventType("CATEGORY_NAME_CHANGED");
    event.setPayload("{\"name\":\"CLAIM_TEST\"}");
    try {
      int effectRows = eventMapper.insert(event);
      assertEquals(1, effectRows);
      int claimedB =  eventMapper.claimPendingEvent(event.getId(), "token-B", 60, "system");
      assertEquals(1, claimedB);

      int updated = eventMapper.markFailed(event.getId(), "token-A", "system", "stale");
      assertEquals(0, updated);
      CategoryChangeEvent dbEvent = eventMapper.selectById(event.getId());
      assertEquals("PROCESSING", dbEvent.getStatus());
      assertEquals("token-B", dbEvent.getProcessingToken());
      assertEquals(0, dbEvent.getRetryCount());

    } finally {
      if (event.getId() != null) {
        eventMapper.deleteById(event.getId());
      }
    }
  }
  @Test
  void processEvents_shouldRetryTranslatedExceptionAndContinueNextEvent() {

    Long version = System.currentTimeMillis();


    Category category = new Category();
    category.setId(1101L);
    category.setName("CLAIM_TEST");
    category.setCategoryVersion(version);

    CategoryChangeEvent event1 = new CategoryChangeEvent();
    event1.setCategoryId(1101L);
    event1.setCategoryVersion(version);
    event1.setProcessingToken("token-1");
    event1.setEventType("CATEGORY_NAME_CHANGED");
    event1.setPayload("{\"name\":\"CLAIM_TEST\"}");

    CategoryChangeEvent event2 = new CategoryChangeEvent();
    event2.setCategoryId(1101L);
    event2.setCategoryVersion(version);
    event2.setProcessingToken("token-2");
    event2.setEventType("CATEGORY_NAME_CHANGED");
    event2.setPayload("{\"name\":\"CLAIM_TEST\"}");

    List<CategoryChangeEvent> events = new ArrayList<>();
    events.add(event1);
    events.add(event2);

    String batchId = UUID.randomUUID().toString();

    ReplicaCategoryMapper replicaCategoryMapper = mock(ReplicaCategoryMapper.class);
    CategoryChangeEventMapper categoryChangeEventMapper = mock(CategoryChangeEventMapper.class);

    when(replicaCategoryMapper.findById(1101L)).thenThrow(new IllegalStateException("replica timeout")).thenReturn(category);
    when(categoryChangeEventMapper.rescheduleForRetry(anyLong(), anyString(), anyString(), anyString())).thenReturn(1);

    when(replicaCategoryMapper.syncReplicaNameIfVersionMatches(anyLong(), anyString(), anyLong())).thenReturn(1);
    when(categoryChangeEventMapper.markSuccess(anyLong(), anyString(), anyString())).thenReturn(1);

    CategoryChangeEventJob job = new CategoryChangeEventJob(
      categoryChangeEventMapper,
      null,
      replicaCategoryMapper,
      new ObjectMapper()
    );

    BatchProcessSummary summary = job.processEvents(events, batchId);

    assertTrue(event1.getLastError().matches("QUERY_REPLICA_FAILED"));
    assertTrue(summary.isConserved());
    assertEquals(1, summary.getRetryCnt());
    assertEquals(1, summary.getSuccessCnt());
    assertEquals(0, summary.getFailCnt());
    assertEquals(0, summary.getNotUpdateCnt());
  }
  @Test
  void processEvents_shouldScheduleRetryWithQueryFailureReasonWhenFindByIdThrows() {
    CategoryChangeEvent event = new CategoryChangeEvent();
    event.setId(1L);
    event.setCategoryId(1101L);
    event.setRetryCount(0);
    event.setCategoryVersion(System.currentTimeMillis());
    event.setProcessingToken("token-A");
    event.setPayload("{\"name\":\"CLAIM_TEST\"}");

    String batchId = "test-batch";

    ReplicaCategoryMapper replicaCategoryMapper = mock(ReplicaCategoryMapper.class);
    CategoryChangeEventMapper categoryChangeEventMapper = mock(CategoryChangeEventMapper.class);
    CategoryChangeEventJob job = new CategoryChangeEventJob(
      categoryChangeEventMapper,
      null,
      replicaCategoryMapper,
      new ObjectMapper()
    );

    when(replicaCategoryMapper.findById(1101L)).thenThrow(new IllegalStateException("replica timeout"));
    when(categoryChangeEventMapper.rescheduleForRetry(
      event.getId(),
      event.getProcessingToken(),
      "system",
      "QUERY_REPLICA_FAILED: 查询从库分类失败"
    )).thenReturn(1);

    BatchProcessSummary summary = job.processEvents(List.of(event), batchId);

    assertTrue(summary.isConserved());
    assertEquals(1, summary.getClaimedCount());
    assertEquals(1, summary.getRetryCnt());
    assertEquals(0, summary.getSuccessCnt());
    assertEquals(0, summary.getFailCnt());
    assertEquals(0, summary.getNotUpdateCnt());
    assertEquals(
      "QUERY_REPLICA_FAILED: 查询从库分类失败",
      event.getLastError()
    );
  }
}
