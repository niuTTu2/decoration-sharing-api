/**package com.huang.decorationsharingapi.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.Arrays;


 * CorsConfig.java
 *
 * 功能: 配置跨域资源共享 (CORS)。当您的前端应用和后端 API 不在同一个域名或端口时，浏览器会阻止跨域请求。
 *         此类允许您定义哪些来源、方法和头部信息是被允许的。
 * 实现方式: 通常会实现 WebMvcConfigurer 接口，并重写 addCorsMappings 方法来配置CORS规则。
 *
@Configuration
public class CorsConfig {

    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();

        // 明确指定允许的源
        config.setAllowedOrigins(Arrays.asList(
                "http://localhost:5173",  // Vite默认开发端口
                "http://localhost:5174",
                "http://localhost:3000",
                "http://localhost:8081",
                "http://127.0.0.1:5173"   // 可能会通过IP访问
        ));

        // 或者使用模式匹配
//         config.setAllowedOriginPatterns(Arrays.asList("http://localhost:*"));

        // 允许的HTTP方法
        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));

        // 允许的请求头
        config.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "X-Requested-With"));

        // 允许凭证
        config.setAllowCredentials(true);

        // 预检请求的有效期（秒）
        config.setMaxAge(3600L);

        // 对所有URL应用这个配置
        source.registerCorsConfiguration("/**", config);

        return new CorsFilter(source);
    }
}*/