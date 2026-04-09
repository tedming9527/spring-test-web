package org.example.springtestweb.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDate;

@Data
@Entity
@Table(indexes={@Index(name="uk_email", columnList="email", unique=true)})
public class User {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  @NotBlank(message = "名称不能为空")
  @Column(nullable = false, columnDefinition = "varchar(20) comment '姓名'")
  private String name;
  @Min(value = -1, message = "年龄不能小于-1")
  @Transient
  private int age;
  @Column(nullable = false, length = 50)
  private String email;
  private LocalDate birthDay;
}
