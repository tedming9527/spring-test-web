package org.example.springtestweb.model;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class User {
  @NotBlank(message = "名称不能为空")
  private String name;
  @Min(value = -1, message = "年龄不能小于-1")
  private int age;
}
