# 新办公自动化系统 - 技术架构说明

## 一、系统概述

基于 **RBAC (Role-Based Access Control)** 模型设计的 Web 办公自动化系统，提供完整的组织管理、考勤打卡、公文流转、通知公告、日程管理等核心功能，采用 **Vue 3 + Spring Boot** 前后端分离架构。

---

## 二、技术栈

### 2.1 后端技术栈

| 层级 | 技术选型 | 说明 |
|------|----------|------|
| 核心框架 | Spring Boot 3.4.3 | Java 17 |
| 持久层 | MyBatis 3.0.3 | ORM 映射框架 |
| 分页组件 | PageHelper 2.1.0 | MyBatis 物理分页 |
| 数据库 | MySQL 8.0.33 | 关系型数据库 |
| 缓存 | Spring Data Redis | 数据缓存 |
| 安全框架 | 自定义拦截器 | 与 Spring Security 解耦的独立鉴权方案 |
| 身份令牌 | JWT (jjwt 0.11.5) | HMAC-SHA256 签名，24h 有效期 |
| API 文档 | springdoc-openapi 2.8.6 | Swagger UI |
| 工具库 | Lombok 1.18.42 | 简化 POJO 代码 |
| 构建工具 | Maven | 依赖管理 |

### 2.2 前端技术栈

| 层级 | 技术选型 | 说明 |
|------|----------|------|
| 核心框架 | Vue 3.5.29 | 渐进式 JavaScript 框架 |
| 路由 | Vue Router 4.5.0 | 单页面应用路由管理 |
| 状态管理 | Pinia 3.0.4 | Vue 3 专用状态管理 |
| UI 组件库 | Element Plus 2.9.7 | 基于 Vue 3 的组件库 |
| HTTP 客户端 | Axios 1.7.9 | HTTP 请求库 |
| 构建工具 | Vite 7.3.1 |下一代前端构建工具 |
| 图标库 | @element-plus/icons-vue 2.3.1 | Element Plus 图标 |

---

## 三、系统功能模块

### 3.1 用户与权限管理 (User & Permission)

**后端接口：** `/user/*`

- 用户注册、登录、登出
- 个人信息查看与修改
- 密码修改（本人修改 + 管理员重置）
- 用户分页列表 + 模糊搜索
- 角色分配
- 管理员重置用户密码

**后端接口：** `/role/*`

- 角色 CRUD
- 角色-权限绑定

**后端接口：** `/permission/*`

- 权限定义（菜单、按钮、API）
- 树形权限结构
- 权限-角色绑定

**前端页面：** `User.vue`, `Role.vue`, `Permission.vue`

### 3.2 组织架构 (Department)

**后端接口：** `/department/*`

- 部门 CRUD
- 部门树形结构查询
- 用户归属部门管理

**前端页面：** `Department.vue`

### 3.3 考勤管理 (Attendance)

**后端接口：** `/attendance/*`

- 签到 / 签退（自动判断当前状态）
- 考勤位置（经纬度）& IP 记录
- 个人考勤历史查询
- 月度考勤统计

**前端页面：** `Attendance.vue`

### 3.4 公文流转 (Document)

**后端接口：** `/document/*`

- 公文起草、编辑、提交
- 多级审批流程
- 审批意见记录
- 视角隔离（创建者看所有状态，审批人员看待审批+已通过，普通人只看已通过）
- 状态：草稿(0) → 审核中(1) → 已通过(2) / 已驳回(3)

**前端页面：** `Document.vue`

### 3.5 通知公告 (Notice)

**后端接口：** `/notice/*`

- 公告发布、编辑、删除
- 阅读量统计
- 分页 + 关键字搜索
- 状态：草稿(0) / 已发布(1)

**前端页面：** `Notice.vue`

### 3.6 日程管理 (Schedule)

**后端接口：** `/schedule/*`

- 个人日程 CRUD
- 状态：未开始(0) / 进行中(1) / 已完成(2)
- 按用户隔离查询

**前端页面：** `Schedule.vue`

### 3.7 文件管理 (File)

**后端接口：** `/file/*`

- 文件上传、下载、删除
- 文件类型与大小校验

**前端页面：** `File.vue`

### 3.8 数据看板 (Stats)

**后端接口：** `/stats/*`

- 首页仪表盘数据聚合
- 用户/部门/考勤/公文等多维度统计

**前端页面：** `Dashboard.vue`

---

## 四、系统架构

### 4.1 整体架构图

```
┌─────────────────────────────────────────────────────────────────────┐
│                         前端 (Frontend)                              │
│   Vue 3 + Vue Router + Pinia + Element Plus + Axios + Vite         │
└──────────────────────────────┬──────────────────────────────────────┘
                               │ HTTP / REST API (JSON)
┌──────────────────────────────▼──────────────────────────────────────┐
│                      Controller Layer                               │
│  UserController  DocumentController  AttendanceController         │
│  DepartmentController  NoticeController  ScheduleController         │
│  FileController  StatsController  RoleController  PermissionController│
└──────────────────────────────┬──────────────────────────────────────┘
                               │
┌──────────────────────────────▼──────────────────────────────────────┐
│                       Service Layer                                 │
│    UserService  DocumentService  AttendanceService  ...            │
└──────────────────────────────┬──────────────────────────────────────┘
                               │
┌──────────────────────────────▼──────────────────────────────────────┐
│                       Mapper Layer                                  │
│      MyBatis + PageHelper 物理分页 + MySQL                         │
└──────────────────────────────┬──────────────────────────────────────┘
                               │
┌──────────────────────────────▼──────────────────────────────────────┐
│                    Redis Cache (可选)                               │
└─────────────────────────────────────────────────────────────────────┘
```

### 4.2 前端项目结构

```
office-frontend/
├── src/
│   ├── api/              # API 请求模块
│   ├── assets/           # 静态资源
│   ├── components/       # 公共组件
│   ├── layout/           # 布局组件（Layout.vue）
│   ├── router/           # 路由配置
│   ├── stores/           # Pinia 状态管理
│   ├── utils/            # 工具函数
│   ├── views/            # 页面组件
│   │   ├── system/       # 系统管理（User/Role/Permission/Department）
│   │   ├── Attendance.vue
│   │   ├── Dashboard.vue
│   │   ├── Document.vue
│   │   ├── File.vue
│   │   ├── Login.vue
│   │   ├── Notice.vue
│   │   ├── Profile.vue
│   │   ├── Register.vue
│   │   ├── Schedule.vue
│   │   └── UpdatePassword.vue
│   ├── App.vue
│   └── main.js
├── public/               # 公共静态资源
├── package.json
└── vite.config.js
```

### 4.3 后端项目结构

```
src/main/java/com/office/newofficeautomationbackend/
├── NewOfficeAutomationBackendApplication.java    # 启动类
├── common/                                         # 公共模块
│   ├── Result.java                               # 统一响应封装
│   ├── ResultCode.java                           # 响应状态码
│   ├── BusinessException.java                    # 自定义业务异常
│   ├── annotation/
│   │   ├── CheckPermission.java                  # 权限注解
│   │   └── Logical.java                         # 权限逻辑（AND/OR）
├── config/                                        # 配置模块
│   ├── GlobalExceptionHandler.java               # 全局异常处理
│   ├── LoginInterceptor.java                     # 登录拦截器
│   ├── RedisConfig.java                          # Redis 配置
│   ├── SecurityConfig.java                       # 安全配置
│   ├── SwaggerConfig.java                        # Swagger 配置
│   └── WebConfig.java                            # Web 配置（拦截器注册）
├── controller/                                    # 控制器层
│   ├── AttendanceController.java
│   ├── DepartmentController.java
│   ├── DocumentController.java
│   ├── FileController.java
│   ├── NoticeController.java
│   ├── PermissionController.java
│   ├── RoleController.java
│   ├── ScheduleController.java
│   ├── StatsController.java
│   └── UserController.java
├── dto/                                           # 数据传输对象
│   ├── LoginDTO.java                             # 登录请求
│   ├── LoginResponseDTO.java                     # 登录响应
│   ├── RegisterDTO.java                          # 注册请求
│   ├── ResetPwdDTO.java                          # 管理员重置密码
│   ├── ScheduleDTO.java                          # 日程分页响应
│   ├── UserDTO.java                              # 用户详情
│   └── DashboardDTO.java                         # 看板数据
├── entity/                                        # 实体类
│   ├── Attendance.java
│   ├── Department.java
│   ├── Document.java
│   ├── File.java
│   ├── Notice.java
│   ├── Permission.java
│   ├── Role.java
│   ├── Schedule.java
│   └── User.java
├── mapper/                                        # MyBatis Mapper 接口
├── service/                                       # 服务层
└── utils/                                         # 工具类
    └── JwtUtils.java                             # JWT 工具类
```

---

## 五、核心设计详解

### 5.1 细粒度权限控制 - `@CheckPermission`

**自定义注解**实现方法级权限拦截，支持 AND / OR 两种逻辑：

```java
// AND: 必须同时拥有所有权限
@CheckPermission({"user:add", "user:edit"})

// OR: 拥有任意一个权限即可
@CheckPermission(value = {"document:create", "document:edit"}, logical = Logical.OR)
```

**工作流程**：

```
请求进入
    ↓
LoginInterceptor 拦截请求
    ↓
解析目标方法的 @CheckPermission 注解
    ↓
从数据库实时查询用户权限列表
    ↓
根据 Logical 枚举执行权限匹配判定
    ↓
有权限 → 放行继续执行
无权限 → 返回 403 Forbidden
```

**源码位置**：

- `common/annotation/CheckPermission.java` - 权限注解定义
- `common/annotation/Logical.java` - AND/OR 逻辑枚举
- `config/LoginInterceptor.java` - 拦截器实现
- `config/WebConfig.java` - 拦截器注册

### 5.2 JWT 无状态认证

**Token 生成**：

- 包含：用户名、签发时间（iat）、过期时间（exp）
- 签名算法：HMAC-SHA256
- 密钥：从配置文件读取
- 有效期：24 小时

**Token 验证流程**：

```
请求携带 Token → 拦截器提取 Token → 验证签名 → 检查是否过期
    ↓
验证通过 → 解析用户名 → 查询用户权限 → 存入 ThreadLocal → 放行
    ↓
验证失败 → 返回 401 Unauthorized
```

**源码位置**：`utils/JwtUtils.java`

### 5.3 统一响应封装 - `Result<T>`

所有 API 返回统一格式：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": { ... }
}
```

**常用响应码**：

| code | 说明 |
|------|------|
| 200 | 操作成功 |
| 400 | 请求参数错误 |
| 401 | 未登录或 Token 无效 |
| 403 | 无权限访问 |
| 404 | 资源不存在 |
| 500 | 服务器内部错误 |

**源码位置**：`common/Result.java`, `common/ResultCode.java`

### 5.4 全局异常处理 - `GlobalExceptionHandler`

统一捕获并格式化输出所有异常：

```java
@ExceptionHandler(Exception.class)
public Result<?> handleException(Exception e) {
    log.error("系统异常: {}", e.getMessage());
    return Result.error("系统繁忙，请稍后重试");
}
```

**源码位置**：`config/GlobalExceptionHandler.java`

### 5.5 自定义业务异常 - `BusinessException`

用于在业务逻辑中抛出可预期的错误，由全局异常处理器统一捕获并返回友好提示：

```java
// 在业务代码中抛出
if (user == null) {
    throw new BusinessException("用户不存在");
}
```

**源码位置**：`common/BusinessException.java`

### 5.6 视角隔离 - 数据安全设计

以 **公文系统** 为典型：

| 用户角色 | 可见的公文范围 |
|----------|----------------|
| 普通员工 | 仅已通过(status=2)的公文 |
| 公文创建者 | 本人创建的所有公文（包含草稿、审核中、已驳回） |
| 审批人员 | 所有已通过公文 + 待审批公文 |

实现方式：在 Service 层根据用户角色动态拼接查询条件。

### 5.7 物理分页 - PageHelper

使用 PageHelper 实现高效的数据库物理分页：

```java
PageHelper.startPage(pageNum, pageSize);
List<T> list = mapper.selectList();
return new PageInfo<>(list);
```

生成的 SQL 自动添加 `LIMIT` 和 `COUNT`，避免全表扫描。

---

## 六、API 设计规范

### 6.1 认证方式

请求头携带 Token：

```
Authorization: Bearer <token>
```

### 6.2 RESTful API 规范

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/xxx/page` | 分页查询列表 |
| GET | `/xxx/list` | 全量列表 |
| GET | `/xxx/{id}` | 详情查询 |
| POST | `/xxx/save` | 新增/修改 |
| POST | `/xxx/submit/{id}` | 提交申请 |
| POST | `/xxx/approve/{id}` | 审批操作 |
| PUT | `/xxx/edit` | 更新 |
| DELETE | `/xxx/delete/{id}` | 删除 |

### 6.3 统一响应格式

```json
// 成功
{
  "code": 200,
  "message": "操作成功",
  "data": { ... }
}

// 失败
{
  "code": 400,
  "message": "参数错误",
  "data": null
}
```

---

## 七、数据库核心表结构

### 7.1 用户权限相关

| 表名 | 说明 | 关键字段 |
|------|------|----------|
| `user` | 用户表 | id, username, password, nickname, email, phone, avatar, dept_id |
| `role` | 角色表 | id, name, code, description, status |
| `permission` | 权限表 | id, name, code, type(menu/button/api), parent_id, path |
| `user_role` | 用户-角色关联表 | user_id, role_id |
| `role_permission` | 角色-权限关联表 | role_id, permission_id |
| `department` | 部门表 | id, name, parent_id, leader, phone |

### 7.2 业务相关

| 表名 | 说明 | 关键字段 |
|------|------|----------|
| `attendance` | 考勤表 | id, user_id, type(sign_in/sign_out), location, ip, create_time |
| `document` | 公文表 | id, title, content, status, create_user_id, approver_id |
| `notice` | 通知表 | id, title, content, status, view_count, publish_time |
| `schedule` | 日程表 | id, user_id, title, start_time, end_time, status |
| `file` | 文件表 | id, original_name, stored_name, path, size, type |

---

## 八、技术总结

| 亮点 | 说明 |
|------|------|
| **自定义权限拦截体系** | `LoginInterceptor` + `@CheckPermission` 注解实现方法级细粒度权限控制，与 Spring Security 解耦 |
| **RBAC 权限模型** | 基于角色的动态权限分配，支持 AND/OR 逻辑组合 |
| **JWT 无状态认证** | HMAC-SHA256 签名，24h 有效期，支持集群部署与水平扩展 |
| **统一响应封装** | `Result<T>` 规范 API 输出格式，`GlobalExceptionHandler` 统一异常处理 |
| **视角隔离设计** | 按角色实现数据分级可见性，保障敏感信息隔离 |
| **物理分页** | PageHelper 高效分页，避免全表扫描与内存溢出 |
| **三层架构** | Controller/Service/Mapper 职责清晰，便于维护扩展 |
| **前后端分离** | Vue 3 + Spring Boot 分离架构，各司其职，独立开发部署 |
| **Redis 缓存支持** | Spring Data Redis 提供缓存支持，提升系统性能 |
| **Swagger API 文档** | springdoc-openapi 自动生成可交互 API 文档 |

---

## 九、开发环境

| 环境 | 版本要求 |
|------|----------|
| JDK | 17+ |
| Node.js | 20.19.0+ |
| MySQL | 8.0+ |
| Redis | 最新稳定版 |
| Maven | 3.x |
| Bun | 1.x（前端构建可选） |

---

*文档更新时间：2026-04-16*
*作者：谦渡尘*
