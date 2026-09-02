package org.example.springtestweb.category.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("replica-bootstrap")
public class ReplicaBootstrapRunner implements ApplicationRunner {

  private static final Logger log = LoggerFactory.getLogger(ReplicaBootstrapRunner.class);
  private final ReplicaCategoryBootstrapService replicaCategoryBootstrapService;

  public ReplicaBootstrapRunner(ReplicaCategoryBootstrapService replicaCategoryBootstrapService) {
    this.replicaCategoryBootstrapService = replicaCategoryBootstrapService;
  }

  @Override
  public void run(ApplicationArguments args) {
    int inserted = replicaCategoryBootstrapService.initializeGoodsCategories();
    log.info("Replica category bootstrap completed, insertedRows={}", inserted);
  }
}
