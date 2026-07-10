package org.example.springtestweb.customer.controller;

import org.example.springtestweb.customer.entity.Customer;
import org.example.springtestweb.customer.mapper.CustomerMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController()
@RequestMapping("/api/customers")
public class CustomerController {
  @Autowired
  private CustomerMapper customerMapper;
  @GetMapping("/{id}")
  public Customer getCustomer(@PathVariable Long id) {
    return customerMapper.selectById(id);
  }
}
