package org.example.springtestweb.category.service;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.example.springtestweb.category.entity.CategoryChangeEvent;
import org.example.springtestweb.category.mapper.CategoryChangeEventMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class CategoryChangeEventServiceTest {
  @Autowired
  private CategoryChangeEventMapper categoryChangeEventMapper;
  @Autowired
  private CategoryChangeEventService categoryChangeEventService;
  @Test
  void claimPendingEvents_shouldClaimOnlyDueEvents() {
    // 场景：存在3条pending数据，next_retry_at分别为null、过期、未过期；两个线程并发认领时，
    // 单个线程拿到几条取决于交错顺序，真正的不变量是：合计恰好认领2条、互不重复、未到期的不被认领
    CategoryChangeEvent event1 = createCategoryChangeEvent(null);
    CategoryChangeEvent event2 = createCategoryChangeEvent(LocalDateTime.now().minusDays(1));
    CategoryChangeEvent event3 = createCategoryChangeEvent(LocalDateTime.now().plusDays(1));

    List<CategoryChangeEvent> list = categoryChangeEventMapper.selectClaimableEvents(1000);
    Set<Long> excludeIds = list.stream().map(CategoryChangeEvent::getId).collect(Collectors.toSet());

    int batchSize = list.size() + 3;

    categoryChangeEventMapper.insert(event1);
    categoryChangeEventMapper.insert(event2);
    categoryChangeEventMapper.insert(event3);

    List<CategoryChangeEvent> allClaimed = new ArrayList<>();
    ExecutorService executorService = Executors.newFixedThreadPool(2);

    try {

      CountDownLatch claimLatch = new CountDownLatch(2);
      CountDownLatch startLatch = new CountDownLatch(1);

      Future<List<CategoryChangeEvent>> future1 = executorService.submit(() -> {
        claimLatch.countDown();
        startLatch.await();

        return categoryChangeEventService.claimPendingEvents(batchSize, 10, "system");
      });

      Future<List<CategoryChangeEvent>> future2 = executorService.submit(() -> {
        claimLatch.countDown();
        startLatch.await();

        return categoryChangeEventService.claimPendingEvents(batchSize, 10, "system");
      });

      claimLatch.await();
      startLatch.countDown();

      List<CategoryChangeEvent> result1 = future1.get(10, TimeUnit.SECONDS);
      List<CategoryChangeEvent> result2 = future2.get(10, TimeUnit.SECONDS);

      List<CategoryChangeEvent> claimed1 = result1.stream().filter(e -> !excludeIds.contains(e.getId())).toList();
      List<CategoryChangeEvent> claimed2 = result2.stream().filter(e -> !excludeIds.contains(e.getId())).toList();
      allClaimed.addAll(claimed1);
      allClaimed.addAll(claimed2);

      // 两线程合计恰好认领 event1+event2，且同一事件不会被认领两次
      assertEquals(2, allClaimed.size());
      Set<Long> claimedIds = allClaimed.stream().map(CategoryChangeEvent::getId).collect(Collectors.toSet());
      assertEquals(Set.of(event1.getId(), event2.getId()), claimedIds);

      allClaimed.forEach(e -> {
         CategoryChangeEvent event =  categoryChangeEventMapper.selectById(e.getId());
         assertNotNull(event);
         assertEquals("PROCESSING", event.getStatus());
         assertEquals(e.getProcessingToken(), event.getProcessingToken());
         assertNotNull(event.getProcessingLeaseUntil());
         assertTrue(event.getProcessingLeaseUntil().isAfter(LocalDateTime.now()));
      });

      CategoryChangeEvent dbEvent3 =  categoryChangeEventMapper.selectById(event3.getId());
      assertNotNull(dbEvent3);
      assertEquals("PENDING", dbEvent3.getStatus());
      assertNull(dbEvent3.getProcessingToken());



    } catch (Exception e) {
      throw new RuntimeException(e);
    } finally {
      categoryChangeEventMapper.deleteById(event1.getId());
      categoryChangeEventMapper.deleteById(event2.getId());
      categoryChangeEventMapper.deleteById(event3.getId());
      for (CategoryChangeEvent e : allClaimed) {
        if (!excludeIds.contains(e.getId())) {
          continue;
        }
        categoryChangeEventMapper.update(null,
          new LambdaUpdateWrapper<CategoryChangeEvent>()
            .eq(CategoryChangeEvent::getId, e.getId())
            .set(CategoryChangeEvent::getStatus, "PENDING")
            .set(CategoryChangeEvent::getProcessingToken, null)
            .set(CategoryChangeEvent::getProcessingLeaseUntil, null)
            .set(CategoryChangeEvent::getUpdater, null)
        );
      }
      executorService.shutdown();
    }

  }
  private CategoryChangeEvent createCategoryChangeEvent(LocalDateTime ldt) {
    CategoryChangeEvent event = new CategoryChangeEvent();
    event.setCategoryId(1101L);
    event.setCategoryVersion(System.currentTimeMillis());
    event.setEventType("CATEGORY_NAME_CHANGED");
    event.setPayload("{\"name\":\"CLAIM_TEST\"}");
    event.setNextRetryAt(ldt);

    return event;
  }
}
