package com.office.newofficeautomationbackend;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 项目主启动类 (Spring Boot Application)
 * 整个办公自动化后台系统的核心入口
 */
@SpringBootApplication
/**
 * 配置 MyBatis Mapper 接口的扫描路径
 * 作用：让 Spring 能够自动发现并注入 com.office.newofficeautomationbackend.mapper 包下的所有持久层接口
 */
@MapperScan("com.office.newofficeautomationbackend.mapper")
public class NewOfficeAutomationBackendApplication {

    /**
     * 系统 main 方法
     * 用于引导启动整个 Spring Boot 服务
     * @param args 命令行参数
     */
    public static void main(String[] args) {
        SpringApplication.run(NewOfficeAutomationBackendApplication.class, args);
    }

}
