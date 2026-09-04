package org.example.springtestweb.category.job;

import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import org.example.springtestweb.category.entity.CategoryChangeEvent;
import org.example.springtestweb.category.mapper.CategoryChangeEventMapper;
import org.example.springtestweb.category.mapper.CategoryMapper;
import org.example.springtestweb.category.replica.mapper.ReplicaCategoryMapper;
import org.example.springtestweb.category.service.CategoryChangeEventService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
public class CategoryChangeEventJob {
  @Autowired
  CategoryChangeEventMapper categoryChangeEventMapper;
  @Autowired
  CategoryChangeEventService categoryChangeEventService;
  @Autowired
  CategoryMapper categoryMapper;
  @Autowired
  ReplicaCategoryMapper replicaCategoryMapper;
  @Autowired
  ObjectMapper objectMapper;
  @XxlJob("categoryChangeEventProbe")
  public void categoryChangeEventProbe(){
    String parameter = XxlJobHelper.getJobParam();
    Integer batchSize = null;
    try {
      batchSize = Integer.valueOf(parameter.trim());
      if (batchSize < 1){
        throw new IllegalStateException("categoryChangeEventProbe 并发梳理参数非法");
      }
    } catch (Exception e) {
      XxlJobHelper.handleFail(
        "任务参数 batchSize 必须是正整数；空参数默认 2，当前值：" + parameter
      );
      return;
    }
    List<CategoryChangeEvent> events = categoryChangeEventService.claimPendingEvents(batchSize, 60, "xxl-job");
    for (CategoryChangeEvent event : events) {
      String name = null;
      try {
        JsonNode jsonNode = objectMapper.readTree(event.getPayload());
        name = jsonNode.get("name").asString();
      } catch (Exception e) {
        throw new IllegalStateException("分类变更事件 payload 非法, eventId=" + event.getId());
      }
      int effectRows = replicaCategoryMapper.syncReplicaNameIfVersionMatches(event.getCategoryId(), name, event.getCategoryVersion());
      if (effectRows > 0){
        event.setStatus("SUCCESS");
      } else {
        if (event.getRetryCount() > 3){
          event.setStatus("FAILED");
        } else {
          int nextRetryCount = event.getRetryCount() + 1;
          event.setRetryCount(nextRetryCount);

          long delaySeconds = nextRetryCount * 30L;
          event.setNextRetryAt(LocalDateTime.now().plusSeconds(delaySeconds));
        }
      }
      categoryChangeEventMapper.updateById(event);
    }

    int claimedCount = events.size();

    XxlJobHelper.log("category change event probe parameter={}, claimedCount={}", parameter, claimedCount);
  }
}
