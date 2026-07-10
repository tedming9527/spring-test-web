package org.example.springtestweb.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.springtestweb.mapper.UserMapper;
import org.example.springtestweb.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@Tag(name = "用户管理")
@RestController
@RequestMapping("/api/users")
public class UserController {
  @Autowired
  private UserMapper userMapper;

  private static final Set<String> ALLOWED_SORT_FIELDS =
      Set.of("id", "name", "email", "birth_day", "username", "last_login");

  @Operation(summary = "根据id获取用户信息")
  @RequestMapping("/{id}")
  public User get(@PathVariable Long id) {
    return userMapper.selectById(id);
  }

  @Operation(summary = "创建用户")
  @PostMapping
  public User create(@RequestBody User user) {
    userMapper.insert(user);
    return user;
  }

  @Operation(summary = "更新用户")
  @PutMapping
  public User update(@RequestBody User user) {
    userMapper.updateById(user);
    return user;
  }

  @Operation(summary = "删除用户")
  @DeleteMapping("/{id}")
  public void delete(@PathVariable Long id) {
    userMapper.deleteById(id);
  }

  @Operation(summary = "获取用户列表")
  @GetMapping
  public IPage<User> list(
      @RequestParam(defaultValue = "id") String property,
      @RequestParam(defaultValue = "ASC") String direction,
      @RequestParam(defaultValue = "0") Integer page,
      @RequestParam(defaultValue = "10") Integer size
  ) {
    Page<User> pageParam = new Page<>(page + 1, size);
    LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
    boolean isAsc = !"DESC".equalsIgnoreCase(direction);
    switch (property) {
      case "name" -> wrapper.orderBy(true, isAsc, User::getName);
      case "email" -> wrapper.orderBy(true, isAsc, User::getEmail);
      case "birthDay" -> wrapper.orderBy(true, isAsc, User::getBirthDay);
      case "username" -> wrapper.orderBy(true, isAsc, User::getUsername);
      case "lastLogin" -> wrapper.orderBy(true, isAsc, User::getLastLogin);
      default -> wrapper.orderBy(true, isAsc, User::getId);
    }
    return userMapper.selectPage(pageParam, wrapper);
  }
}
