package org.example.springtestweb.controller;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * SPA 路由过滤器：在 Spring MVC 之前拦截请求，按规则分流：
 *  - /api/**              → 交给 Spring MVC（API 控制器）
 *  - 含扩展名的路径        → 交给 Spring ResourceHandler（静态资源，找不到则返回 404）
 *  - 其他路径（SPA 路由） → forward 到 /index.html，由 React Router 处理
 *
 * 使用 Filter 而非 ErrorController/MVC catch-all，可彻底避免
 * 静态资源被 forward 成 text/html 导致的 MIME 类型错误。
 */
@Component
public class SpaController extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String path = request.getRequestURI();

        // API 请求或含文件扩展名的路径（.js/.css/.png 等）→ 正常处理
        if (path.startsWith("/api/") || hasFileExtension(path)) {
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

