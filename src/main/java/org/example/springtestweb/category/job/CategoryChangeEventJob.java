package org.example.springtestweb.category.job;

import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import org.example.springtestweb.category.service.CategoryChangeEventService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CategoryChangeEventJob {

  @Autowired
  CategoryChangeEventService categoryChangeEventService;
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
    int claimedCount = categoryChangeEventService.claimPendingEvents(batchSize, 60, "xxl-job").size();

    XxlJobHelper.log("category change event probe parameter={}, claimedCount={}", parameter, claimedCount);
  }
}
