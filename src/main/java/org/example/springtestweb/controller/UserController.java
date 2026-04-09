package org.example.springtestweb.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.springtestweb.model.User;
import org.example.springtestweb.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

@Tag(name = "用户管理")
@RestController
@RequestMapping("/users")
public class UserController {
  @Autowired
  private UserRepository userRepository;

  @Operation(summary = "根据id获取用户信息")
  @RequestMapping("/{id}")
  public User get(@PathVariable Long id) {
    return userRepository.findById(id).orElseThrow();
  }
  @Operation(summary = "创建用户")
  @PostMapping
  public User create(@RequestBody User user) {
    return userRepository.save(user);
  }
  @Operation(summary = "更新用户")
  @PutMapping
  public User update(@RequestBody User user) {
    return userRepository.save(user);
  }
  @Operation(summary = "删除用户")
  @DeleteMapping("/{id}")
  public void delete (@PathVariable Long id) {
    userRepository.deleteById(id);
  }

  @Operation(summary = "获取用户列表")
  @GetMapping
  public Page<User> list(
      @RequestParam(defaultValue = "id") String property,
      @RequestParam(defaultValue = "ASC") Sort.Direction direction,
      @RequestParam(defaultValue = "0") Integer page,
      @RequestParam(defaultValue = "10") Integer size
                         ) {
    Pageable pageable = PageRequest.of(page, size, direction, property);
    return userRepository.findAll(pageable);
  }
}
