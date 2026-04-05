package com.office.newofficeautomationbackend.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web 配置类
 * 负责注册拦截器并配置静态资源映射等
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private LoginInterceptor loginInterceptor;

    // 注入配置文件中的存储路径
    @Value("${file.avatar-path}")
    private String avatarPath;

    @Value("${file.office-path}")
    private String officePath;

    /**
     * 注册自定义拦截器
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(loginInterceptor)
                // 拦截所有路径
                .addPathPatterns("/**")
                // 排除登录和注册接口
                .excludePathPatterns("/user/login", "/user/register")
                // 放行 Swagger 相关路径
                .excludePathPatterns(
                        "/swagger-ui/**",
                        "/swagger-ui.html",
                        "/v3/api-docs/**",
                        "/swagger-resources/**",
                        "/webjars/**"
                );
    }

    /**
     * 配置静态资源映射
     * 作用：让前端可以通过 http://localhost:8080/uploads/xxx 访问服务器本地的图片或文档
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 1. 映射头像目录
        registry.addResourceHandler("/uploads/avatar/**")
                .addResourceLocations("file:" + avatarPath);
        
        // 2. 映射办公文件目录
        registry.addResourceHandler("/uploads/office/**")
                .addResourceLocations("file:" + officePath);
    }
}
