# JimuReport Enhancement 测试计划

## 测试概述

**测试版本**: v1.0.0  
**测试日期**: 2026-03-31  
**测试范围**: 用户管理、权限管理、填报功能、积木报表集成

---

## 1. 环境准备

### 1.1 数据库准备

```bash
# 1. 创建数据库
mysql -u root -p -e "CREATE DATABASE IF NOT EXISTS jimureport_enhancement CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;"

# 2. 执行初始化脚本
mysql -u root -p jimureport_enhancement < sql/mysql/init.sql

# 3. 验证表创建
mysql -u root -p -e "USE jimureport_enhancement; SHOW TABLES;"
```

### 1.2 Redis 准备

```bash
# 确保 Redis 运行
redis-cli ping
# 应返回 PONG
```

### 1.3 应用启动

```bash
# 编译项目
cd jimureport-enhancement
mvn clean package -DskipTests

# 启动应用
java -jar target/jimureport-enhancement-1.0.0.jar

# 验证启动
curl http://localhost:8085/actuator/health
```

---

## 2. 用户管理模块测试

### 2.1 用户注册

**接口**: `POST /api/user/register`

**测试用例**:

| 用例ID | 测试场景 | 输入数据 | 预期结果 |
|--------|---------|---------|---------|
| TC-001 | 正常注册 | `{ "username": "testuser", "password": "Test123456", "realName": "测试用户" }` | 注册成功，返回用户信息 |
| TC-002 | 用户名已存在 | `{ "username": "admin", "password": "Test123456" }` | 返回"用户名已存在"错误 |
| TC-003 | 用户名过短 | `{ "username": "ab", "password": "Test123456" }` | 返回用户名长度验证错误 |
| TC-004 | 密码过短 | `{ "username": "testuser2", "password": "123" }` | 返回密码强度验证错误 |

**测试命令**:

```bash
# TC-001: 正常注册
curl -X POST http://localhost:8085/api/user/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "password": "Test123456",
    "realName": "测试用户",
    "email": "test@example.com",
    "phone": "13800138000"
  }'

# TC-002: 用户名已存在
curl -X POST http://localhost:8085/api/user/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "Test123456"
  }'
```

### 2.2 用户登录

**接口**: `POST /api/user/login`

**测试用例**:

| 用例ID | 测试场景 | 输入数据 | 预期结果 |
|--------|---------|---------|---------|
| TC-005 | 正常登录 | `{ "username": "admin", "password": "admin123" }` | 返回 JWT Token |
| TC-006 | 密码错误 | `{ "username": "admin", "password": "wrong" }` | 返回"密码错误" |
| TC-007 | 用户不存在 | `{ "username": "nonexist", "password": "123" }` | 返回"用户不存在" |
| TC-008 | 用户被禁用 | 使用被禁用用户登录 | 返回"用户已被停用" |

**测试命令**:

```bash
# TC-005: 正常登录
curl -X POST http://localhost:8085/api/user/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "admin123"
  }'

# 保存返回的 Token
TOKEN="eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

### 2.3 用户列表查询

**接口**: `GET /api/user/list`

**测试用例**:

| 用例ID | 测试场景 | 输入参数 | 预期结果 |
|--------|---------|---------|---------|
| TC-009 | 查询所有用户 | `?pageNum=1&pageSize=10` | 返回用户列表 |
| TC-010 | 关键词搜索 | `?keyword=admin` | 返回匹配用户 |
| TC-011 | 状态过滤 | `?status=0` | 返回正常状态用户 |
| TC-012 | 无权限访问 | 不带 Token | 返回 401 错误 |

**测试命令**:

```bash
# TC-009: 查询用户列表
curl -X GET "http://localhost:8085/api/user/list?pageNum=1&pageSize=10" \
  -H "Authorization: Bearer $TOKEN"

# TC-010: 关键词搜索
curl -X GET "http://localhost:8085/api/user/list?keyword=admin" \
  -H "Authorization: Bearer $TOKEN"

# TC-012: 无权限访问
curl -X GET "http://localhost:8085/api/user/list"
```

### 2.4 用户修改

**接口**: `PUT /api/user/update`

**测试命令**:

```bash
# 获取用户ID
USER_ID="从列表查询获取的用户ID"

# 修改用户信息
curl -X PUT http://localhost:8085/api/user/update \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d "{
    \"id\": \"$USER_ID\",
    \"realName\": \"修改后的姓名\",
    \"email\": \"updated@example.com\"
  }"
```

### 2.5 用户删除

**接口**: `DELETE /api/user/delete/{id}`

**测试命令**:

```bash
# 删除用户（逻辑删除）
curl -X DELETE "http://localhost:8085/api/user/delete/$USER_ID" \
  -H "Authorization: Bearer $TOKEN"
```

---

## 3. 角色管理模块测试

### 3.1 角色列表

**接口**: `GET /api/permission/roles`

**测试命令**:

```bash
curl -X GET http://localhost:8085/api/permission/roles \
  -H "Authorization: Bearer $TOKEN"
```

### 3.2 新增角色

**接口**: `POST /api/permission/role/add`

**测试命令**:

```bash
curl -X POST http://localhost:8085/api/permission/role/add \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "roleName": "数据分析师",
    "roleKey": "analyst",
    "roleSort": 10,
    "dataScope": "3",
    "remark": "负责数据分析工作"
  }'
```

### 3.3 分配角色菜单

**接口**: `POST /api/permission/role/{roleId}/menus`

**测试命令**:

```bash
ROLE_ID="从角色列表获取的ID"
MENU_IDS='["menu1", "menu2", "menu3"]'

curl -X POST "http://localhost:8085/api/permission/role/$ROLE_ID/menus" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d "{\"menuIds\": $MENU_IDS}"
```

---

## 4. 菜单管理模块测试

### 4.1 菜单树

**接口**: `GET /api/permission/menus`

**测试命令**:

```bash
curl -X GET http://localhost:8085/api/permission/menus \
  -H "Authorization: Bearer $TOKEN"
```

### 4.2 用户菜单权限

**接口**: `GET /api/permission/user/menus`

**测试命令**:

```bash
curl -X GET http://localhost:8085/api/permission/user/menus \
  -H "Authorization: Bearer $TOKEN"
```

---

## 5. 报表权限测试

### 5.1 分配报表权限

**接口**: `POST /api/permission/report/assign`

**测试命令**:

```bash
curl -X POST http://localhost:8085/api/permission/report/assign \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "reportId": "report_001",
    "roleId": "role_001",
    "permissionType": "view",
    "dataScope": "1"
  }'
```

### 5.2 查询报表权限

**接口**: `GET /api/permission/report/{reportId}`

**测试命令**:

```bash
curl -X GET "http://localhost:8085/api/permission/report/report_001" \
  -H "Authorization: Bearer $TOKEN"
```

---

## 6. 填报表单测试

### 6.1 创建填报表单

**接口**: `POST /api/fill/form/create`

**测试命令**:

```bash
curl -X POST http://localhost:8085/api/fill/form/create \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "formName": "员工信息表",
    "reportId": "report_001",
    "description": "收集员工基本信息",
    "allowDuplicate": "0",
    "fields": [
      {
        "fieldName": "name",
        "fieldLabel": "姓名",
        "fieldType": "text",
        "isRequired": "1",
        "maxLength": 50,
        "orderNum": 1
      },
      {
        "fieldName": "age",
        "fieldLabel": "年龄",
        "fieldType": "number",
        "isRequired": "1",
        "minValue": 18,
        "maxValue": 100,
        "orderNum": 2
      },
      {
        "fieldName": "department",
        "fieldLabel": "部门",
        "fieldType": "select",
        "isRequired": "1",
        "options": "[{\"label\":\"技术部\",\"value\":\"tech\"},{\"label\":\"市场部\",\"value\":\"market\"}]",
        "orderNum": 3
      }
    ]
  }'
```

### 6.2 查询表单配置

**接口**: `GET /api/fill/form/config/{reportId}`

**测试命令**:

```bash
curl -X GET "http://localhost:8085/api/fill/form/config/report_001" \
  -H "Authorization: Bearer $TOKEN"
```

### 6.3 提交填报数据

**接口**: `POST /api/fill/submit`

**测试命令**:

```bash
FORM_ID="从表单配置获取的formId"

curl -X POST http://localhost:8085/api/fill/submit \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d "{
    \"formId\": \"$FORM_ID\",
    \"data\": {
      \"name\": \"张三\",
      \"age\": 28,
      \"department\": \"tech\"
    }
  }"
```

### 6.4 保存草稿

**接口**: `POST /api/fill/draft/save`

**测试命令**:

```bash
curl -X POST http://localhost:8085/api/fill/draft/save \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d "{
    \"formId\": \"$FORM_ID\",
    \"data\": {
      \"name\": \"李四\",
      \"age\": 30
    }
  }"
```

### 6.5 查询提交记录

**接口**: `GET /api/fill/records/{formId}`

**测试命令**:

```bash
curl -X GET "http://localhost:8085/api/fill/records/$FORM_ID?pageNum=1&pageSize=10" \
  -H "Authorization: Bearer $TOKEN"
```

---

## 7. 积木报表集成测试

### 7.1 Token 验证

**接口**: `GET /jmreport/token/validate`

**测试命令**:

```bash
# 使用 X-Access-Token 头
curl -X GET http://localhost:8085/jmreport/token/validate \
  -H "X-Access-Token: $TOKEN"
```

### 7.2 获取用户信息

**接口**: `GET /jmreport/user/info`

**测试命令**:

```bash
curl -X GET http://localhost:8085/jmreport/user/info \
  -H "X-Access-Token: $TOKEN"
```

---

## 8. 安全测试

### 8.1 SQL 注入测试

```bash
# 尝试 SQL 注入
curl -X POST http://localhost:8085/api/user/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin' OR '1'='1",
    "password": "anything"
  }'

# 预期：应该返回"用户不存在"，而不是登录成功
```

### 8.2 XSS 测试

```bash
# 尝试 XSS 攻击
curl -X POST http://localhost:8085/api/user/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "password": "Test123456",
    "realName": "<script>alert(1)</script>"
  }'

# 预期：应该对特殊字符进行转义或拒绝
```

### 8.3 Token 过期测试

```bash
# 使用过期的 Token
curl -X GET http://localhost:8085/api/user/list \
  -H "Authorization: Bearer expired_token_here"

# 预期：返回 401 错误
```

---

## 9. 性能测试

### 9.1 并发登录测试

```bash
# 使用 Apache Bench 测试并发登录
ab -n 1000 -c 100 -p login.json -T application/json \
  http://localhost:8085/api/user/login
```

### 9.2 大数据量查询测试

```bash
# 测试大量数据分页查询
curl -X GET "http://localhost:8085/api/fill/records/$FORM_ID?pageNum=1&pageSize=100" \
  -H "Authorization: Bearer $TOKEN"
```

---

## 10. 测试检查清单

### 10.1 功能测试清单

- [ ] 用户注册功能正常
- [ ] 用户登录返回 Token
- [ ] Token 验证正常
- [ ] 用户列表查询正常
- [ ] 用户修改功能正常
- [ ] 用户删除（逻辑删除）正常
- [ ] 角色列表查询正常
- [ ] 角色新增功能正常
- [ ] 角色菜单分配正常
- [ ] 菜单树查询正常
- [ ] 报表权限分配正常
- [ ] 数据权限配置正常
- [ ] 填报表单创建正常
- [ ] 填报数据提交正常
- [ ] 草稿保存功能正常
- [ ] 重复提交控制正常
- [ ] 积木报表 Token 集成正常

### 10.2 安全测试清单

- [ ] 无 Token 访问返回 401
- [ ] Token 过期返回 401
- [ ] 权限不足返回 403
- [ ] SQL 注入防护正常
- [ ] XSS 攻击防护正常
- [ ] 密码加密存储正常
- [ ] 敏感信息不泄露

### 10.3 性能测试清单

- [ ] 登录响应时间 < 500ms
- [ ] 列表查询响应时间 < 1s
- [ ] 并发 100 用户无错误
- [ ] 大数据量分页正常

---

## 11. 测试报告模板

```markdown
# 测试执行报告

**测试日期**: YYYY-MM-DD
**测试人员**: XXX
**测试环境**: 开发环境

## 测试结果汇总

| 模块 | 用例数 | 通过 | 失败 | 阻塞 | 通过率 |
|------|--------|------|------|------|--------|
| 用户管理 | 15 | 14 | 1 | 0 | 93.3% |
| 角色管理 | 8 | 8 | 0 | 0 | 100% |
| 菜单管理 | 5 | 5 | 0 | 0 | 100% |
| 报表权限 | 6 | 6 | 0 | 0 | 100% |
| 填报功能 | 10 | 9 | 1 | 0 | 90% |
| 集成测试 | 5 | 5 | 0 | 0 | 100% |
| 安全测试 | 7 | 7 | 0 | 0 | 100% |

## 缺陷列表

| 缺陷ID | 模块 | 严重程度 | 描述 | 状态 |
|--------|------|----------|------|------|
| BUG-001 | 用户管理 | 中 | XXX | 待修复 |

## 测试结论

项目整体功能正常，存在 X 个缺陷需要修复...
```

---

## 12. 常见问题排查

### 12.1 数据库连接失败

```bash
# 检查数据库连接
mysql -u root -p -h localhost -e "SELECT 1"

# 检查防火墙
sudo ufw status
```

### 12.2 Redis 连接失败

```bash
# 检查 Redis
redis-cli ping

# 检查 Redis 配置
cat /etc/redis/redis.conf | grep bind
```

### 12.3 应用启动失败

```bash
# 查看日志
tail -f logs/jimureport-enhancement.log

# 检查端口占用
lsof -i :8085
```

---

## 测试工具推荐

1. **Postman**: API 测试
2. **Apache Bench**: 压力测试
3. **MySQL Workbench**: 数据库管理
4. **Redis Desktop Manager**: Redis 管理
5. **JMeter**: 性能测试
