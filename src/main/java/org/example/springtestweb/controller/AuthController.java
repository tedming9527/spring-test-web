package org.example.springtestweb.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.example.springtestweb.mapper.UserMapper;
import org.example.springtestweb.model.User;
import org.example.springtestweb.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

@Tag(name = "认证管理")
@RestController
@RequestMapping("/api/auth")
public class AuthController {

  @Autowired
  private UserMapper userMapper;

  @Autowired
  private JwtUtil jwtUtil;

  private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder(10);

  @Operation(summary = "用户登录")
  @PostMapping("/login")
  public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
    if (request.getUsername() == null || request.getPassword() == null) {
      return ResponseEntity.status(401).build();
    }

    User user = userMapper.selectOne(
        new LambdaQueryWrapper<User>().eq(User::getUsername, request.getUsername()));
    if (user == null) {
      return ResponseEntity.status(401).build();
    }

    if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
      return ResponseEntity.status(401).build();
    }

    List<String> roles = user.getRoles() != null
        ? List.of(user.getRoles().split(","))
        : List.of();

    String token = jwtUtil.generateToken(user.getId(), roles);

    user.setLastLogin(LocalDateTime.now());
    userMapper.updateById(user);

    LoginResponse response = new LoginResponse();
    response.setToken(token);
    return ResponseEntity.ok(response);
  }

  @Getter
  @Setter
  public static class LoginRequest {
    @NotBlank(message = "用户名不能为空")
    private String username;
    @NotBlank(message = "密码不能为空")
    private String password;
  }

  @Getter
  @Setter
  public static class LoginResponse {
    private String token;
  }
}
