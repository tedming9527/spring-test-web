package org.example.springtestweb.customer.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.example.springtestweb.customer.entity.Customer;
import org.springframework.stereotype.Component;

@Component
public interface CustomerMapper extends BaseMapper<Customer> {
}
