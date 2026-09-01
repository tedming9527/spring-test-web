package org.example.springtestweb.category.mapper;

import org.example.springtestweb.category.entity.CategoryChangeEvent;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class CategoryChangeEventMapperTest {
  @Autowired
  private CategoryChangeEventMapper eventMapper;

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

}
