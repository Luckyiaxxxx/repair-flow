package com.repair.config;

import com.repair.interceptor.JwtInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private JwtInterceptor jwtInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(jwtInterceptor)
                .addPathPatterns("/api/**")                    // 拦截所有 /api/** 请求
                .excludePathPatterns(
                        "/api/owner/register",                // 放行注册
                        "/api/owner/login",                   // 放行登录
                        "/api/dispatcher/hello",               // 测试接口

                        "/api/worker/hello",

                        "/api/admin/hello",
                        "/api/owner/hello"
                );
    }
}