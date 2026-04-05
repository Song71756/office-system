package com.office.newofficeautomationbackend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * 安全配置类
 * 负责配置请求拦截规则，当前阶段暂时放行所有请求以便于开发调试
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * 配置安全过滤链
     * 目前配置为：放行所有 HTTP 请求 (permitAll)
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // 禁用 CSRF (开发阶段跨域请求常用)
            .csrf(csrf -> csrf.disable())
            // 配置请求授权
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/user/login", "/user/register").permitAll() // 显式放行登录注册
                .anyRequest().permitAll() // 目前仍保持放行，让自定义拦截器 LoginInterceptor 去处理具体的业务逻辑
            );
        
        return http.build();
    }
}
