package org.example.springtestweb.configurer;

import org.example.springtestweb.interceptor.AuthTokenInterceptor;
import org.example.springtestweb.interceptor.LogInterceptor;
import org.example.springtestweb.interceptor.TimeInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.CacheControl;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.concurrent.TimeUnit;

@Configuration
public class WebConfigurer implements WebMvcConfigurer {

    @Autowired
    private LogInterceptor logInterceptor;
    @Autowired
    private TimeInterceptor timeInterceptor;
    @Autowired
    private AuthTokenInterceptor authTokenInterceptor;

    /**
     * 静态资源处理（优先于 Spring Boot 自动配置的 /** 兜底 Handler）：
     * - /assets/**  Vite 哈希文件，文件名含 hash，永久缓存（immutable）
     * - 根目录静态文件  短期缓存
     * - /index.html  不缓存，保证 SPA 更新能被浏览器感知
     *
     * Spring 的 ResourceHttpRequestHandler 会根据文件扩展名自动设置 Content-Type
     * （使用 MediaTypeFactory，覆盖 .js/.css/.woff2/.svg/.webmanifest 等所有常见类型）。
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Vite hashed assets: cache 1 year + immutable（文件名含 hash，内容永不变）
        registry.addResourceHandler("/assets/**")
                .addResourceLocations("classpath:/static/assets/")
                .setCacheControl(CacheControl.maxAge(365, TimeUnit.DAYS).immutable());

        // 根目录其他静态文件（favicon、manifest、robots.txt 等）
        registry.addResourceHandler("/*.ico", "/*.svg", "/*.png", "/*.webmanifest", "/*.txt")
                .addResourceLocations("classpath:/static/")
                .setCacheControl(CacheControl.maxAge(1, TimeUnit.DAYS));

        // index.html 不缓存，SPA 入口需要始终拿到最新版
        registry.addResourceHandler("/index.html")
                .addResourceLocations("classpath:/static/")
                .setCacheControl(CacheControl.noCache());
    }

    /**
     * 静态资源路径跳过 log/time 拦截器，减少无意义的日志和计时开销。
     * authTokenInterceptor 只作用于 /api/**，静态资源本就不在其范围内。
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        String[] staticPaths = {
                "/assets/**", "/index.html",
                "/*.ico", "/*.svg", "/*.png", "/*.webmanifest", "/*.txt"
        };
        registry.addInterceptor(logInterceptor).excludePathPatterns(staticPaths);
        registry.addInterceptor(timeInterceptor).excludePathPatterns(staticPaths);
        registry.addInterceptor(authTokenInterceptor).addPathPatterns("/api/**");
    }
}
