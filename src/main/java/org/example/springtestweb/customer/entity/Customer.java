package org.example.springtestweb.customer.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigInteger;

@TableName("customer")
@Data
public class Customer {
  @TableId(value = "id", type = IdType.AUTO)
  private Long id; // 没映射数据库之前可能为null，因此使用包装类型
  @TableField("name")
  private String name;
  @TableField("age")
  private Integer age;
  @TableField("email")
  private String email;
}
