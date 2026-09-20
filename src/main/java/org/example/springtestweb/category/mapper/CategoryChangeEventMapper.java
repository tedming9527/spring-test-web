package org.example.springtestweb.category.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.example.springtestweb.category.entity.CategoryChangeEvent;

import java.util.List;

public interface CategoryChangeEventMapper extends BaseMapper<CategoryChangeEvent> {
  int claimPendingEvent(
    @Param("id") Long id,
    @Param("processingToken") String processingToken,
    @Param("leaseSeconds") Integer leaseSeconds,
    @Param("updater") String updater
  );
  List<CategoryChangeEvent> selectClaimableEvents(int batchSize);

  int rescheduleForRetry(
    @Param("id") Long id,
    @Param("processingToken") String processingToken,
    @Param("updater") String updater,
    @Param("lastError")  String lastError
  );
  int markFailed(
    @Param("id") Long id,
    @Param("processingToken") String processingToken,
    @Param("updater") String updater,
    @Param("lastError")  String lastError
  );
  int markSuccess(
    @Param("id") Long id,
    @Param("processingToken") String processingToken,
    @Param("updater") String updater
  );
  int recoverExpiredProcessingEvents(
    @Param("updater") String updater
  );
}
