package org.example.springtestweb.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;

import org.example.springtestweb.util.JwtUtil;
import lombok.Data;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;

@Tag(name = "认证中心")
@RestController
@RequestMapping("/api/auth")
public class AuthController {
	@Autowired
	private JwtUtil jwtUtil;

	@Operation(summary = "登录，返回token")
	@PostMapping("/login")
	public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest req, HttpServletResponse response) {
		// 简单内存校验
		if (Objects.equals(req.getUsername(), "admin") && Objects.equals(req.getPassword(), "123456")) {
			// 生成 JWT（sub 使用示例用户 id=1）
			String token = jwtUtil.generateToken(1L, java.util.List.of("ROLE_ADMIN"));
			LoginResponse loginResponse = new LoginResponse(token);
			return ResponseEntity.ok(loginResponse);
		} else {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
		}
	}

	@Data
	public static class LoginRequest {
		private String username;
		private String password;
	}

	@Data
	public static class LoginResponse {
		private final String token;
	}
}
