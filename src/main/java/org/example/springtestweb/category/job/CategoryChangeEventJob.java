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
      if (batchSize == null || batchSize < 1){
        throw new IllegalStateException("categoryChangeEventProbe 并发梳理参数非法");
      }
    } catch (Exception e) {
      batchSize = 2;
    }
    int claimedCount = categoryChangeEventService.claimPendingEvents(batchSize, 60, "xxl-job").size();

    XxlJobHelper.log("category change event probe parameter={}, claimedCount={}", parameter, claimedCount);
  }
}
