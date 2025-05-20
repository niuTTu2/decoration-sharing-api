package com.huang.decorationsharingapi.config;


import com.huang.decorationsharingapi.security.JwtAuthenticationEntryPoint;
import com.huang.decorationsharingapi.security.JwtAuthenticationFilter;
import com.huang.decorationsharingapi.service.UserDetailsServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

/**
 * 功能: 配置 Spring Security。这里定义了应用程序的安全规则，例如哪些 API 路径需要认证，哪些是公开访问的，配置密码编码器，以及如何处理认证和授权失败等。
 * 主要组件:
 * @Configuration 和 @EnableWebSecurity: 启用 Spring Security。
 * SecurityFilterChain Bean: 定义安全过滤链，配置 HTTP 安全规则（如 authorizeHttpRequests, csrf, sessionManagement）。
 * AuthenticationManager Bean: 处理认证请求。
 * PasswordEncoder Bean: 定义密码的加密方式（例如 Bcrypt）。
 * 可能会集成 JwtAuthenticationFilter 来处理 JWT 认证。
 *
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    private final UserDetailsServiceImpl userDetailsService;
    private final JwtAuthenticationEntryPoint unauthorizedHandler;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userDetailsService).passwordEncoder(passwordEncoder());
    }

    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .cors().and().csrf().disable()
                .exceptionHandling().authenticationEntryPoint(unauthorizedHandler).and()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS).and()
                .authorizeRequests()
                // 公开接口
                .antMatchers("/auth/login", "/auth/register").permitAll()
                .antMatchers(HttpMethod.GET, "/categories/**").permitAll()
                .antMatchers(HttpMethod.GET, "/materials/**").permitAll()
                // 用户接口
                .antMatchers("/user/**").authenticated()
                // 管理员接口
                .antMatchers("/admin/**").hasAuthority("ROLE_ADMIN")
                // 默认其他所有请求需要认证
                .anyRequest().authenticated();

        // 添加JWT过滤器
        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("*"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "X-Requested-With"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
 */

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    private final UserDetailsServiceImpl userDetailsService;
    private final JwtAuthenticationEntryPoint unauthorizedHandler;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userDetailsService).passwordEncoder(passwordEncoder());
    }

    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .cors() // 启用CORS, 将使用下面的corsConfigurationSource() Bean
                .and()
                .csrf().disable()
                .exceptionHandling().authenticationEntryPoint(unauthorizedHandler).and()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS).and()
                .authorizeRequests()
                // 公开接口
                .antMatchers("/auth/login", "/auth/register").permitAll()
                .antMatchers(HttpMethod.GET, "/categories/**").permitAll()
                .antMatchers(HttpMethod.GET, "/materials/**").permitAll()
                // 用户接口
                .antMatchers("/user/**").authenticated() // 示例，具体路径根据你的Controller调整
                .antMatchers("/api/profile/**").authenticated() // 假设ProfileController的路径
                // 管理员接口
                .antMatchers("/admin/**").hasAuthority("ROLE_ADMIN") // 示例，具体路径根据你的Controller调整
                // 默认其他所有请求需要认证
                .anyRequest().authenticated();

        // 添加JWT过滤器
        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        // 使用您在CorsConfig.java中定义的更安全的来源列表
        configuration.setAllowedOrigins(Arrays.asList(
                "http://localhost:5173",
                "http://localhost:5174",
                "http://localhost:3000",
                "http://localhost:8081",
                "http://127.0.0.1:5173"
        ));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "X-Requested-With", "Accept")); // 添加 "Accept" 等常用头部
        configuration.setExposedHeaders(Arrays.asList("Authorization", "Content-Disposition")); // 如果前端需要访问自定义响应头，例如下载文件时的Content-Disposition
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L); // 预检请求的有效期（秒）

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration); // 对所有URL应用这个配置
        return source;
    }
}