package org.example.springtestweb.category.job;

import lombok.Data;

@Data
public class BatchProcessSummary {
  public BatchProcessSummary(
    int claimedCount,
    int successCnt,
    int failCnt,
    int retryCnt,
    int notUpdateCnt
  ) {
    this.claimedCount = claimedCount;
    this.successCnt = successCnt;
    this.failCnt = failCnt;
    this.retryCnt = retryCnt;
    this.notUpdateCnt = notUpdateCnt;
  }
  int claimedCount = 0;
  int successCnt = 0;
  int failCnt = 0;
  int retryCnt = 0;
  int notUpdateCnt = 0;

  public boolean isConserved() {
    return claimedCount == successCnt + failCnt + retryCnt + notUpdateCnt;
  }
}
