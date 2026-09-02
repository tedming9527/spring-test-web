package org.example.springtestweb.category.job;

import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import org.springframework.stereotype.Component;

@Component
public class CategoryChangeEventJob {
  @XxlJob("categoryChangeEventProbe")
  public void categoryChangeEventProbe(){
    String parameter = XxlJobHelper.getJobParam();
    XxlJobHelper.log("category change event probe paramter={}", parameter);
  }
}
