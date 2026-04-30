package org.example.springtestweb.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AuthTokenInterceptor implements HandlerInterceptor {
	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
		// 只拦截受保护接口（如 /users, /welcome 等），放行 /auth/login
		String uri = request.getRequestURI();
		if (uri.startsWith("/auth/login") || uri.startsWith("/static") || uri.startsWith("/swagger")
				|| uri.startsWith("/v3/api-docs")) {
			return true;
		}
		String token = request.getHeader("Authorization");
		if (token != null && token.startsWith("Bearer ")) {
			// 简单校验（实际应校验JWT）
			return true;
		}
		response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
		response.getWriter().write("Unauthorized");
		return false;
	}
}
