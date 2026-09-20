package org.example.springtestweb.category.job;

import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import org.example.springtestweb.category.entity.Category;
import org.example.springtestweb.category.entity.CategoryChangeEvent;
import org.example.springtestweb.category.mapper.CategoryChangeEventMapper;
import org.example.springtestweb.category.replica.mapper.ReplicaCategoryMapper;
import org.example.springtestweb.category.service.CategoryChangeEventService;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

/**
 * @author dongdeming
 */
@Component
public class CategoryChangeEventJob {

  final CategoryChangeEventMapper categoryChangeEventMapper;
  final CategoryChangeEventService categoryChangeEventService;
  final ReplicaCategoryMapper replicaCategoryMapper;
  final ObjectMapper objectMapper;

  public CategoryChangeEventJob(CategoryChangeEventMapper categoryChangeEventMapper,
      CategoryChangeEventService categoryChangeEventService, ReplicaCategoryMapper replicaCategoryMapper,
      ObjectMapper objectMapper) {
    this.categoryChangeEventMapper = categoryChangeEventMapper;
    this.categoryChangeEventService = categoryChangeEventService;
    this.replicaCategoryMapper = replicaCategoryMapper;
    this.objectMapper = objectMapper;
  }

  @XxlJob("categoryChangeEventProbe")
  public void categoryChangeEventProbe() {
    String parameter = XxlJobHelper.getJobParam();
    Integer batchSize = null;
    try {
      batchSize = Integer.valueOf(parameter.trim());
      if (batchSize < 1) {
        throw new IllegalStateException("categoryChangeEventProbe 并发梳理参数非法");
      }
    } catch (Exception e) {
      XxlJobHelper.handleFail(
          "任务参数 batchSize 必须是正整数；空参数默认 2，当前值：" + parameter);
      return;
    }
    List<CategoryChangeEvent> events = categoryChangeEventService.claimPendingEvents(batchSize, 60, "xxl-job");
    int successCnt = 0;
    int failCnt = 0;
    int retryCnt = 0;
    int notUpdatedCnt = 0;

    for (CategoryChangeEvent event : events) {
      ProcessResult result = null;
      try {
        result = processEvent(event);
      } catch (Exception e) {
        XxlJobHelper.log("事件处理异常, eventId = {}, message={}", event.getId(), e.getMessage());
        event.setLastError(e.getMessage());
        try {
          result = scheduleRetry(event);
        } catch (Exception retryException) {
          XxlJobHelper.log("事件重试写回异常，eventId={}, message={}", event.getId(), retryException.getMessage());
          result = ProcessResult.STATE_NOT_UPDATED;
        }
      }
      switch (result) {
        case SUCCESS:
          successCnt++;
          break;
        case FAILED:
          failCnt++;
          break;
        case RETRY_SCHEDULED:
          retryCnt++;
          break;
        case STATE_NOT_UPDATED:
          notUpdatedCnt++;
          break;
      }

    }

    int claimedCount = events.size();

    XxlJobHelper.log(
        "category change event probe parameter={}, claimedCount={}, success={}, failed={}, retry={}, notUpdate={}",
        parameter, claimedCount, successCnt, failCnt, retryCnt, notUpdatedCnt);
  }

  private enum ProcessResult {
    /**
     * 事件已成功处理并标记成功。
     */
    SUCCESS,

    /**
     * 本次处理失败，已安排后续重试。
     */
    RETRY_SCHEDULED,

    /**
     * 重试次数耗尽，事件已标记失败。
     */
    FAILED,

    /**
     * 处理令牌或状态不匹配，未能更新事件状态。
     */
    STATE_NOT_UPDATED
  }

  private ProcessResult processEvent(CategoryChangeEvent event) {
    String name = null;
    try {
      JsonNode jsonNode = objectMapper.readTree(event.getPayload());
      name = jsonNode.get("name").asString();
    } catch (Exception e) {
      String error = "事件payload非法, eventId=" + event.getId();
      XxlJobHelper.log(error);
      event.setLastError(error);
      return scheduleRetry(event);
    }
    Category replicaCategory = replicaCategoryMapper.findById(event.getCategoryId());
    if (replicaCategory == null) {
      return scheduleRetry(event);
    } else if (replicaCategory.getCategoryVersion() < event.getCategoryVersion()) {
      int effectRows = replicaCategoryMapper.syncReplicaNameIfVersionMatches(
          event.getCategoryId(), name, event.getCategoryVersion());
      if (effectRows == 0) {
        return scheduleRetry(event);
      }
    }
    int updated = categoryChangeEventMapper.markSuccess(event.getId(), event.getProcessingToken(), "system");
    if (updated == 1) {
      return ProcessResult.SUCCESS;
    } else {
      return ProcessResult.STATE_NOT_UPDATED;
    }
  }

  private ProcessResult scheduleRetry(CategoryChangeEvent event) {
    if (event.getRetryCount() > 3) {
      int updated = categoryChangeEventMapper.markFailed(event.getId(), event.getProcessingToken(), "system",
          event.getLastError());
      return updated == 1 ? ProcessResult.FAILED : ProcessResult.STATE_NOT_UPDATED;
    }
    int updated = categoryChangeEventMapper.rescheduleForRetry(event.getId(), event.getProcessingToken(), "system",
        event.getLastError());
    return updated == 1 ? ProcessResult.RETRY_SCHEDULED : ProcessResult.STATE_NOT_UPDATED;
  }
}
