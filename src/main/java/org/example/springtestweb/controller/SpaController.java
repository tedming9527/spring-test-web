package org.example.springtestweb.controller;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.IOException;

/**
 * SPA fallback：实现 ErrorController 接管 /error 端点。
 *
 * 路由逻辑：
 *  - 404 + 路径不含 '.'  → forward 到 /index.html（React Router 处理）
 *  - 404 + 路径含 '.'    → 静态资源缺失，返回 404（避免把 html 当 JS 加载）
 *  - 其他状态码           → 返回简单错误信息
 */
@Controller
@RequestMapping("${server.error.path:${error.path:/error}}")
public class SpaController implements ErrorController {

    @RequestMapping(produces = MediaType.TEXT_HTML_VALUE)
    public void handleError(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Integer statusCode = (Integer) request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        String requestUri = (String) request.getAttribute(RequestDispatcher.ERROR_REQUEST_URI);
        if (requestUri == null) requestUri = "/";

        if (statusCode != null && statusCode == 404 && !hasFileExtension(requestUri)) {
            // SPA 路由 → 交给 React Router
            request.getRequestDispatcher("/index.html").forward(request, response);
        } else if (statusCode != null && statusCode == 404) {
            // 静态资源找不到 → 真正的 404，直接写响应，不再触发 /error（避免 MIME 错误）
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            response.setContentType(MediaType.TEXT_PLAIN_VALUE);
            response.getWriter().write("404 Not Found: " + requestUri);
        } else {
            response.setStatus(statusCode != null ? statusCode : 500);
            response.setContentType(MediaType.TEXT_PLAIN_VALUE);
            response.getWriter().write("Error " + statusCode);
        }
    }

    private boolean hasFileExtension(String path) {
        int lastSlash = path.lastIndexOf('/');
        String filename = path.substring(lastSlash + 1);
        return filename.contains(".");
    }
}
