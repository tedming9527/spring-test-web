package org.example.springtestweb.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("user")
public class User {
  @TableId(type = IdType.AUTO)
  private Long id;
  @NotBlank(message = "名称不能为空")
  private String name;
  @Min(value = -1, message = "年龄不能小于-1")
  @TableField(exist = false)
  private int age;
  private String email;
  private LocalDate birthDay;

  private String username;

  private String passwordHash;

  private String roles;

  private LocalDateTime lastLogin;
}
