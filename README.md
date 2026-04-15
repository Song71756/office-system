# 新办公自动化系统

<p align="center">
  <img src="https://img.shields.io/badge/Java-17-blue.svg" alt="Java">
  <img src="https://img.shields.io/badge/Spring%20Boot-3.4.3-brightgreen.svg" alt="Spring Boot">
  <img src="https://img.shields.io/badge/Vue-3.5-green.svg" alt="Vue">
  <img src="https://img.shields.io/badge/License-MIT-yellow.svg" alt="License">
</p>

> 基于 RBAC 权限模型设计的 Web 办公自动化系统，采用 Vue 3 + Spring Boot 前后端分离架构，实现用户权限管理、考勤打卡、公文流转、通知公告、日程管理等核心功能。

---

## 功能特性

### 核心模块

| 模块 | 说明 |
|------|------|
| **用户权限管理** | 基于 RBAC 的角色-权限体系，细粒度方法级权限控制 |
| **组织架构** | 部门树形结构管理 |
| **考勤管理** | 签到/签退、位置/IP 记录、月度考勤统计 |
| **公文流转** | 多级审批流程，状态流转（草稿→审核中→已通过/已驳回） |
| **通知公告** | 发布管理、阅读量统计、关键字搜索 |
| **日程管理** | 个人日程 CRUD，按用户隔离查询 |
| **文件管理** | 文件上传下载、类型大小校验 |
| **数据看板** | 首页多维度统计聚合 |

### 技术亮点

- **自定义权限拦截体系**：`@CheckPermission` 注解 + `LoginInterceptor` 实现方法级细粒度权限控制
- **JWT 无状态认证**：HMAC-SHA256 签名，支持集群部署与水平扩展
- **统一响应封装**：`Result<T>` + 全局异常处理器规范 API 输出
- **视角隔离设计**：公文系统按角色实现数据分级可见性
- **物理分页**：PageHelper 高效分页，避免全表扫描

---

## 技术栈

### 后端

| 技术 | 说明 |
|------|------|
| Spring Boot 3.4.3 | 核心框架 |
| Java 17 | 编程语言 |
| MyBatis 3.0.3 | 持久层框架 |
| PageHelper 2.1.0 | 分页插件 |
| MySQL 8.0.33 | 数据库 |
| Redis | 缓存 |
| JWT (jjwt 0.11.5) | 身份令牌 |
| springdoc-openapi 2.8.6 | API 文档 |

### 前端

| 技术 | 说明 |
|------|------|
| Vue 3.5.29 | 核心框架 |
| Vue Router 4.5.0 | 路由管理 |
| Pinia 3.0.4 | 状态管理 |
| Element Plus 2.9.7 | UI 组件库 |
| Axios 1.7.9 | HTTP 客户端 |
| Vite 7.3.1 | 构建工具 |

---

## 项目结构

```
new-office-automation-backend/
├── docs/                           # 技术文档
│   └── 系统架构说明_更新版.md        # 详细架构说明
├── office-frontend/                # 前端项目
│   ├── src/
│   │   ├── api/                   # API 请求
│   │   ├── components/            # 公共组件
│   │   ├── layout/               # 布局组件
│   │   ├── router/               # 路由配置
│   │   ├── stores/               # 状态管理
│   │   ├── views/                # 页面组件
│   │   └── main.js               # 入口文件
│   └── package.json
├── src/                           # 后端项目
│   └── main/java/com/office/
│       └── newofficeautomationbackend/
│           ├── common/           # 公共模块（Result, 注解）
│           ├── config/           # 配置模块（拦截器, 异常处理）
│           ├── controller/       # 控制器层
│           ├── entity/           # 实体类
│           ├── mapper/           # MyBatis Mapper
│           ├── service/          # 服务层
│           └── utils/            # 工具类
├── pom.xml                        # 后端依赖配置
└── README.md
```

---

## 快速开始

### 环境要求

| 环境 | 版本 |
|------|------|
| JDK | 17+ |
| Node.js | 20.19.0+ |
| MySQL | 8.0+ |
| Redis | 最新稳定版 |

### 1. 克隆项目

```bash
git clone <repository-url>
cd new-office-automation-backend
```

### 2. 配置数据库

创建数据库 `new_office_automation`，然后执行 SQL 初始化脚本。

修改 `src/main/resources/application.yml` 中的数据库配置：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/new_office_automation?useSSL=false&serverTimezone=Asia/Shanghai
    username: your_username
    password: your_password
```

### 3. 启动后端

```bash
# 使用 Maven
./mvnw spring-boot:run

# 或打包后运行
./mvnw clean package
java -jar target/new-office-automation-backend-0.0.1-SNAPSHOT.jar
```

后端启动地址：http://localhost:8080

### 4. 启动前端

```bash
cd office-frontend
npm install
npm run dev
```

前端启动地址：http://localhost:5173

### 5. 访问系统

打开浏览器访问 http://localhost:5173，使用默认账号登录（具体账号密码见数据库初始化脚本）。

---

## API 文档

启动后端后，访问 Swagger UI 查看完整 API 文档：

```
http://localhost:8080/swagger-ui.html
```

---

## 项目文档

- [系统架构说明](./docs/Architecture.md) - 详细技术架构文档

---

## License

This project is licensed under the MIT License.

---

*欢迎 Star 和 Fork，如有问题请提交 Issue。*
