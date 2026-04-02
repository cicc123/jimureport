# JimuReport Enhancement (积木报表增强版)

基于开源积木报表（JimuReport）进行二次开发与功能增强，新增完善的用户管理、权限管理、填报增强等功能。

## 功能特性

### 1. 用户管理模块
- 用户注册、登录、信息管理
- 用户状态管理（启用/禁用）
- 支持多角色、多部门组织结构管理
- 登录日志记录

### 2. 权限管理体系
- 基于角色的权限控制（RBAC）
- 权限粒度覆盖菜单、报表查看/设计/填报
- 数据权限控制（全部、部门、个人、自定义）
- 支持不同用户/角色只能操作权限范围内的报表

### 3. 填报增强功能
- 在线填报表单配置
- 支持丰富控件（文本、数字、日期、下拉框、单选、复选、文件等）
- 校验规则（必填、长度、范围、正则）
- 草稿保存与恢复
- 重复提交控制
- 填报数据导出

### 4. 设计器增强
- 自动带入当前登录用户信息（用户ID、用户名、部门）
- 字段默认值配置
- 隐藏字段支持

## 技术栈

- **后端框架**: Spring Boot 2.7.x
- **安全框架**: Spring Security + JWT
- **ORM框架**: MyBatis-Plus 3.5.x
- **数据库**: MySQL 8.0+
- **缓存**: Redis
- **报表引擎**: JimuReport 1.6.x
- **API文档**: SpringDoc (Swagger)

## 项目结构

```
jimureport-enhancement/
├── src/main/java/com/jimureport/enhancement/
│   ├── config/                 # 配置类
│   │   ├── SecurityConfig.java
│   │   ├── MybatisPlusConfig.java
│   │   ├── WebMvcConfig.java
│   │   └── DateMetaObjectHandler.java
│   ├── controller/             # 控制器
│   │   ├── SysUserController.java
│   │   ├── SysPermissionController.java
│   │   └── JimuFillFormController.java
│   ├── entity/                 # 实体类
│   │   ├── SysUser.java
│   │   ├── SysRole.java
│   │   ├── SysDepartment.java
│   │   ├── SysMenu.java
│   │   ├── JimuReportPermission.java
│   │   ├── JimuFillFormConfig.java
│   │   └── ...
│   ├── mapper/                 # Mapper接口
│   ├── service/                # 服务接口
│   │   ├── SysUserService.java
│   │   ├── SysPermissionService.java
│   │   ├── JimuFillFormService.java
│   │   └── JimuReportTokenServiceImpl.java
│   ├── service/impl/           # 服务实现
│   ├── security/               # 安全相关
│   │   ├── JwtTokenProvider.java
│   │   └── JwtAuthenticationFilter.java
│   └── vo/                     # 视图对象
├── src/main/resources/
│   ├── application.yml         # 应用配置
│   └── mapper/                 # MyBatis XML
└── sql/
    └── mysql/
        └── init.sql            # 数据库初始化脚本
```

## 快速开始

### 1. 环境准备

- JDK 11+
- Maven 3.6+
- MySQL 8.0+
- Redis 6.0+

### 2. 数据库初始化

```bash
mysql -u root -p < sql/mysql/init.sql
```

### 3. 修改配置

编辑 `src/main/resources/application.yml`：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/jimureport_enhancement
    username: your_username
    password: your_password
  redis:
    host: localhost
    port: 6379
    password: your_redis_password
```

### 4. 编译运行

```bash
mvn clean package
java -jar target/jimureport-enhancement-1.0.0.jar
```

### 5. 访问系统

- API文档: http://localhost:8085/swagger-ui.html
- 积木报表: http://localhost:8085/jmreport/list
- 默认账号: admin / admin123

## API接口

### 用户管理

| 接口 | 方法 | 说明 | 权限 |
|------|------|------|------|
| `/api/user/login` | POST | 用户登录 | 公开 |
| `/api/user/register` | POST | 用户注册 | 公开 |
| `/api/user/list` | GET | 用户列表 | system:user:list |
| `/api/user/{id}` | GET | 用户详情 | system:user:query |
| `/api/user` | POST | 新增用户 | system:user:add |
| `/api/user` | PUT | 修改用户 | system:user:edit |
| `/api/user/{id}` | DELETE | 删除用户 | system:user:remove |
| `/api/user/status` | PUT | 修改状态 | system:user:edit |
| `/api/user/roles` | PUT | 分配角色 | system:user:edit |

### 权限管理

| 接口 | 方法 | 说明 | 权限 |
|------|------|------|------|
| `/api/permission/current/permissions` | GET | 当前用户权限 | 无需 |
| `/api/permission/current/roles` | GET | 当前用户角色 | 无需 |
| `/api/permission/current/menus` | GET | 当前用户菜单 | 无需 |
| `/api/permission/roles` | GET | 角色列表 | system:role:list |
| `/api/permission/roles` | POST | 新增角色 | system:role:add |
| `/api/permission/roles` | PUT | 修改角色 | system:role:edit |
| `/api/permission/roles/{id}` | DELETE | 删除角色 | system:role:remove |
| `/api/permission/menus/tree` | GET | 菜单树 | 无需 |
| `/api/permission/reports` | POST | 分配报表权限 | report:permission:assign |

### 填报管理

| 接口 | 方法 | 说明 | 权限 |
|------|------|------|------|
| `/api/fill/forms/{reportId}` | GET | 获取表单配置 | 无需 |
| `/api/fill/forms` | GET | 表单列表 | report:fill:list |
| `/api/fill/forms` | POST | 创建表单 | report:fill:add |
| `/api/fill/forms` | PUT | 修改表单 | report:fill:edit |
| `/api/fill/forms/{id}` | DELETE | 删除表单 | report:fill:remove |
| `/api/fill/submit` | POST | 提交数据 | 无需 |
| `/api/fill/draft` | POST | 保存草稿 | 无需 |
| `/api/fill/draft/{formId}` | GET | 获取草稿 | 无需 |
| `/api/fill/records` | GET | 提交记录 | report:fill:list |
| `/api/fill/export/{formId}` | GET | 导出数据 | report:fill:export |

## 积木报表集成

本项目通过实现 `JmReportTokenServiceI` 接口与积木报表进行集成：

```java
@Service
public class JimuReportTokenServiceImpl implements JmReportTokenServiceI {
    @Override
    public String getToken(HttpServletRequest request) {
        // 从请求头获取Token
    }

    @Override
    public boolean checkToken(String token) {
        // 验证Token
    }

    @Override
    public String getUserInfo(String token) {
        // 返回用户ID,用户名
    }

    @Override
    public String getUserPermission(String token) {
        // 返回用户权限
    }
}
```

## 默认账号

| 用户名 | 密码 | 角色 | 说明 |
|--------|------|------|------|
| admin | admin123 | 超级管理员 | 系统默认管理员 |

## 许可证

Apache License 2.0
