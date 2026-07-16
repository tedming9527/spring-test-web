package org.example.springtestweb.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * SPA 路由过滤器：以最高优先级运行，在所有 Spring 内置过滤器之前分流请求：
 * - /api/**       → 交给 Spring MVC（API 控制器）
 * - 含扩展名路径   → 直接进入过滤器链，由 ResourceHttpRequestHandler 提供静态文件
 *                   ResourceHttpRequestHandler 使用 MediaTypeFactory 自动推断 Content-Type，
 *                   覆盖 .js / .css / .woff2 / .svg / .webmanifest 等所有现代 web 类型。
 * - 其他路径       → forward 到 /index.html，由 React Router 处理 SPA 路由
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class SpaRoutingFilter extends OncePerRequestFilter {

	@Override
	protected void doFilterInternal(HttpServletRequest request,
			HttpServletResponse response,
			FilterChain filterChain) throws ServletException, IOException {
		String path = request.getRequestURI();

		// API、springdoc 文档，或含文件扩展名的路径（.js/.css/.png 等）→ 正常处理
		if (path.startsWith("/api/") || path.startsWith("/v3/") || path.startsWith("/swagger-") || hasFileExtension(path)) {
			filterChain.doFilter(request, response);
			return;
		}

		// SPA 路由 → forward 到 index.html，由 React Router 处理
		request.getRequestDispatcher("/index.html").forward(request, response);
	}

	private boolean hasFileExtension(String path) {
		int lastSlash = path.lastIndexOf('/');
		String filename = path.substring(lastSlash + 1);
		return filename.contains(".");
	}
}
