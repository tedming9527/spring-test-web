package org.example.springtestweb.controller;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
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
  public PageInfo<User> list(
      @RequestParam(defaultValue = "id") String property,
      @RequestParam(defaultValue = "ASC") String direction,
      @RequestParam(defaultValue = "0") Integer page,
      @RequestParam(defaultValue = "10") Integer size
  ) {
    // validate sort field to prevent SQL injection
    String sortField = ALLOWED_SORT_FIELDS.contains(property) ? property : "id";
    String sortDir = "DESC".equalsIgnoreCase(direction) ? "desc" : "asc";
    PageHelper.startPage(page + 1, size);
    PageHelper.orderBy(sortField + " " + sortDir);
    return new PageInfo<>(userMapper.selectList(null));
  }
}
