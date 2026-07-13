package org.example.springtestweb.customer.controller;

import io.jsonwebtoken.lang.Assert;
import org.example.springtestweb.customer.entity.Customer;
import org.example.springtestweb.customer.mapper.CustomerMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
public class SampleTest {
  @Autowired
  private CustomerMapper customerMapper;
  @Test
  public void testSelect() {
    System.out.println("------------ selectAll method test ------------");
    List<Customer> customerList = customerMapper.selectList(null);
    Assert.isTrue(5 == customerList.size(), "");
    customerList.forEach(System.out::println);
  }
}
