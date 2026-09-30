package com.appruntime;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Chống cache dữ liệu màn hình terminal: mọi response {@code /api/terminal/**} mang {@code
 * Cache-Control: no-store}. Screen data là dữ liệu phiên nhạy cảm (user, account, card…) — không
 * được để browser/proxy cache. Tương đương header no-store của trang {@code /terminal} thời
 * server-render (GAP-CACHE-01); interceptor 1 điểm thay vì rải từng {@code ResponseEntity} return
 * site trong TerminalController.
 */
@Configuration
public class TerminalNoStoreConfig implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(
                        new HandlerInterceptor() {
                            @Override
                            public boolean preHandle(
                                    HttpServletRequest request,
                                    HttpServletResponse response,
                                    Object handler) {
                                response.setHeader("Cache-Control", "no-store");
                                return true;
                            }
                        })
                .addPathPatterns("/api/terminal/**");
    }
}
