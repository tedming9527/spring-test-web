package org.example.springtestweb.category.mapper;

import org.example.springtestweb.category.entity.CategoryChangeEvent;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;

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

    try {
      eventMapper.insert(event);
      int firstResult = eventMapper.claimPendingEvent(event.getId(), "token-A", 60, "system");
      int secondResult = eventMapper.claimPendingEvent(event.getId(), "token-B", 60, "system");

      assertEquals(1, firstResult);
      assertEquals(0, secondResult);

      CategoryChangeEvent claimedEvent = eventMapper.selectById(event.getId());
      assertEquals("PROCESSING", claimedEvent.getStatus());
      assertEquals("token-A", claimedEvent.getProcessingToken());
      assertNotNull(claimedEvent.getProcessingLeaseUntil());
      assertTrue(claimedEvent.getProcessingLeaseUntil().isAfter(LocalDateTime.now()));
    } finally {
      if (event.getId() != null) {
        eventMapper.deleteById(event.getId());
      }
    }
  }

}
