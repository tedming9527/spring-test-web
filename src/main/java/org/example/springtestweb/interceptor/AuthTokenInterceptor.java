package org.example.springtestweb.interceptor;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.springtestweb.context.RequestUserContext;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;
import io.jsonwebtoken.JwtException;
import org.example.springtestweb.util.JwtUtil;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AuthTokenInterceptor implements HandlerInterceptor {
	@Autowired
	private JwtUtil jwtUtil;

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
		RequestUserContext.clear();
		String uri = request.getRequestURI();
		// 只保护 /api/** 路径；/api/auth/login 本身无需 token
		if (!uri.startsWith("/api/") || uri.startsWith("/api/auth/login")) {
			return true;
		}
		String token = request.getHeader("Authorization");
		if (token != null && token.startsWith("Bearer ")) {
			String jwt = token.substring("Bearer ".length());
			try {
				Jws<Claims> claimsJws =  jwtUtil.parseToken(jwt);
				String userId = claimsJws.getBody().getSubject();
				if (userId != null && !userId.isBlank()) {
					RequestUserContext.setUserId(userId);
				}
				return true;
			} catch (JwtException ex) {
				// fallthrough to unauthorized
			}
		}
		response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
		response.getWriter().write("Unauthorized");
		return false;
	}

	@Override
	public void afterCompletion(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler, Exception ex) {
		RequestUserContext.clear();
	}
}
