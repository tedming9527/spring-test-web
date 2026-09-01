package org.example.springtestweb.category.service;

import org.example.springtestweb.category.entity.CategoryChangeEvent;
import org.example.springtestweb.category.mapper.CategoryChangeEventMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class CategoryChangeEventService {
  @Autowired
  private CategoryChangeEventMapper categoryChangeEventMapper;
  public List<CategoryChangeEvent> claimPendingEvents(int batchSize, int leaseSeconds, String updater) {
    List<CategoryChangeEvent> categoryChangeEvents = categoryChangeEventMapper.selectClaimableEvents(batchSize);
    List<CategoryChangeEvent> winners = new ArrayList<>();
    for(CategoryChangeEvent e: categoryChangeEvents){
      String token = UUID.randomUUID().toString();
      int affectRows =  categoryChangeEventMapper.claimPendingEvent(e.getId(), token, leaseSeconds,  updater);
      if (affectRows == 1){
        e.setProcessingToken(token);
        winners.add(e);
      }
    };
    return winners;
  }
}
