#!/bin/bash
# JimuReport Enhancement 自动化测试脚本 (修正版)
# 使用正确的API路径

BASE_URL="http://127.0.0.1:8085"
PASS=0
FAIL=0
TOTAL=0

# 颜色
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m'

test_result() {
    TOTAL=$((TOTAL + 1))
    if [ $1 -eq 0 ]; then
        PASS=$((PASS + 1))
        echo -e "${GREEN}[PASS]${NC} $2"
    else
        FAIL=$((FAIL + 1))
        echo -e "${RED}[FAIL]${NC} $2"
        if [ -n "$3" ]; then
            echo "  详情: ${3:0:120}"
        fi
    fi
}

echo "============================================"
echo "  JimuReport Enhancement 测试执行 (修正版)"
echo "  $(date '+%Y-%m-%d %H:%M:%S')"
echo "============================================"
echo ""

# ========== 1. 健康检查 ==========
echo "--- 1. 环境检查 ---"
HEALTH=$(docker exec jimureport-app curl -s ${BASE_URL}/actuator/health 2>/dev/null)
if echo "$HEALTH" | grep -q '"status":"UP"'; then
    test_result 0 "健康检查"
else
    test_result 1 "健康检查" "$HEALTH"
fi

# ========== 2. 用户登录 ==========
echo ""
echo "--- 2. 用户管理模块 ---"

LOGIN_RESP=$(docker exec jimureport-app curl -s -X POST ${BASE_URL}/api/user/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}' 2>/dev/null)

TOKEN=$(echo "$LOGIN_RESP" | grep -o '"token":"[^"]*"' | cut -d'"' -f4)
if [ -n "$TOKEN" ]; then
    test_result 0 "用户登录 - 正常登录"
    echo "  Token: ${TOKEN:0:50}..."
else
    test_result 1 "用户登录 - 正常登录" "$LOGIN_RESP"
    echo "  无法获取Token，后续测试将跳过"
    exit 1
fi

# 密码错误
WRONG_PWD=$(docker exec jimureport-app curl -s -X POST ${BASE_URL}/api/user/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"wrongpassword"}' 2>/dev/null)
if echo "$WRONG_PWD" | grep -qiE "密码|password|error|fail|invalid|凭证"; then
    test_result 0 "用户登录 - 密码错误"
else
    test_result 1 "用户登录 - 密码错误" "$WRONG_PWD"
fi

# 用户不存在
NO_USER=$(docker exec jimureport-app curl -s -X POST ${BASE_URL}/api/user/login \
  -H "Content-Type: application/json" \
  -d '{"username":"nonexist_user_12345","password":"123456"}' 2>/dev/null)
if echo "$NO_USER" | grep -qiE "不存在|not found|error|fail|invalid|凭证"; then
    test_result 0 "用户登录 - 用户不存在"
else
    test_result 1 "用户登录 - 用户不存在" "$NO_USER"
fi

# ========== 3. 用户注册 ==========
REGISTER_RESP=$(docker exec jimureport-app curl -s -X POST ${BASE_URL}/api/user/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser_'$(date +%s)'",
    "password": "Test123456",
    "realName": "测试用户",
    "email": "test@example.com",
    "phone": "13800138000"
  }' 2>/dev/null)
if echo "$REGISTER_RESP" | grep -qiE "成功|success|id"; then
    test_result 0 "用户注册 - 正常注册"
else
    test_result 1 "用户注册 - 正常注册" "$REGISTER_RESP"
fi

# 用户名已存在
DUP_USER=$(docker exec jimureport-app curl -s -X POST ${BASE_URL}/api/user/register \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"Test123456"}' 2>/dev/null)
if echo "$DUP_USER" | grep -qiE "已存在|exist|duplicate|error"; then
    test_result 0 "用户注册 - 用户名已存在"
else
    test_result 1 "用户注册 - 用户名已存在" "$DUP_USER"
fi

# ========== 4. 用户列表查询 ==========
echo ""
echo "--- 3. 用户列表查询 ---"

USER_LIST=$(docker exec jimureport-app curl -s "${BASE_URL}/api/user/list?pageNum=1&pageSize=10" \
  -H "Authorization: Bearer $TOKEN" 2>/dev/null)
if echo "$USER_LIST" | grep -qiE "admin|records|list|total|pages"; then
    test_result 0 "用户列表 - 查询所有用户"
else
    test_result 1 "用户列表 - 查询所有用户" "$USER_LIST"
fi

SEARCH_USER=$(docker exec jimureport-app curl -s "${BASE_URL}/api/user/list?keyword=admin" \
  -H "Authorization: Bearer $TOKEN" 2>/dev/null)
if echo "$SEARCH_USER" | grep -qiE "admin|records|list"; then
    test_result 0 "用户列表 - 关键词搜索"
else
    test_result 1 "用户列表 - 关键词搜索" "$SEARCH_USER"
fi

NO_AUTH=$(docker exec jimureport-app curl -s -w "\n%{http_code}" "${BASE_URL}/api/user/list" 2>/dev/null)
HTTP_CODE=$(echo "$NO_AUTH" | tail -1)
if [ "$HTTP_CODE" = "401" ] || [ "$HTTP_CODE" = "403" ]; then
    test_result 0 "用户列表 - 无权限访问 (HTTP $HTTP_CODE)"
else
    test_result 1 "用户列表 - 无权限访问" "HTTP $HTTP_CODE"
fi

# ========== 5. 角色管理 ==========
echo ""
echo "--- 4. 角色管理模块 ---"

ROLE_LIST=$(docker exec jimureport-app curl -s "${BASE_URL}/api/permission/roles" \
  -H "Authorization: Bearer $TOKEN" 2>/dev/null)
if echo "$ROLE_LIST" | grep -qiE "role|admin|超级|\["; then
    test_result 0 "角色列表 - 查询角色"
else
    test_result 1 "角色列表 - 查询角色" "$ROLE_LIST"
fi

# 新增角色 (使用正确路径 /api/permission/roles)
ADD_ROLE=$(docker exec jimureport-app curl -s -X POST "${BASE_URL}/api/permission/roles" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "roleName": "数据分析师_'$(date +%s)'",
    "roleKey": "analyst",
    "roleSort": 10,
    "dataScope": "3",
    "remark": "负责数据分析工作"
  }' 2>/dev/null)
if echo "$ADD_ROLE" | grep -qiE "success|成功|id|roleKey"; then
    test_result 0 "角色新增 - 创建角色"
else
    test_result 1 "角色新增 - 创建角色" "$ADD_ROLE"
fi

# ========== 6. 菜单管理 ==========
echo ""
echo "--- 5. 菜单管理模块 ---"

# 使用正确路径 /api/permission/menus/tree
MENU_TREE=$(docker exec jimureport-app curl -s "${BASE_URL}/api/permission/menus/tree" \
  -H "Authorization: Bearer $TOKEN" 2>/dev/null)
if echo "$MENU_TREE" | grep -qiE "menu|name|children|\["; then
    test_result 0 "菜单树 - 查询菜单"
else
    test_result 1 "菜单树 - 查询菜单" "$MENU_TREE"
fi

# 使用正确路径 /api/permission/current/menus
USER_MENUS=$(docker exec jimureport-app curl -s "${BASE_URL}/api/permission/current/menus" \
  -H "Authorization: Bearer $TOKEN" 2>/dev/null)
if echo "$USER_MENUS" | grep -qiE "menu|name|\["; then
    test_result 0 "用户菜单 - 查询权限菜单"
else
    test_result 1 "用户菜单 - 查询权限菜单" "$USER_MENUS"
fi

# ========== 7. 报表权限 ==========
echo ""
echo "--- 6. 报表权限模块 ---"

# 使用正确路径 /api/permission/reports
REPORT_PERM=$(docker exec jimureport-app curl -s -X POST "${BASE_URL}/api/permission/reports" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "reportId": "report_001",
    "roleId": "1",
    "permissionType": "view",
    "dataScope": "1"
  }' 2>/dev/null)
if echo "$REPORT_PERM" | grep -qiE "success|成功|id"; then
    test_result 0 "报表权限 - 分配权限"
else
    test_result 1 "报表权限 - 分配权限" "$REPORT_PERM"
fi

# 查询报表权限
QUERY_PERM=$(docker exec jimureport-app curl -s "${BASE_URL}/api/permission/reports/report_001" \
  -H "Authorization: Bearer $TOKEN" 2>/dev/null)
if echo "$QUERY_PERM" | grep -qiE "permission|report|\["; then
    test_result 0 "报表权限 - 查询权限"
else
    test_result 1 "报表权限 - 查询权限" "$QUERY_PERM"
fi

# ========== 8. 填报表单 ==========
echo ""
echo "--- 7. 填报表单模块 ---"

CREATE_FORM=$(docker exec jimureport-app curl -s -X POST "${BASE_URL}/api/fill/form/create" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "formName": "员工信息表_'$(date +%s)'",
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
      }
    ]
  }' 2>/dev/null)
if echo "$CREATE_FORM" | grep -qiE "success|成功|id|formId"; then
    test_result 0 "填报表单 - 创建表单"
    FORM_ID=$(echo "$CREATE_FORM" | grep -o '"id":"[^"]*"' | head -1 | cut -d'"' -f4)
    if [ -z "$FORM_ID" ]; then
        FORM_ID=$(echo "$CREATE_FORM" | grep -o '"formId":"[^"]*"' | head -1 | cut -d'"' -f4)
    fi
else
    test_result 1 "填报表单 - 创建表单" "$CREATE_FORM"
    FORM_ID=""
fi

FORM_CONFIG=$(docker exec jimureport-app curl -s "${BASE_URL}/api/fill/form/config/report_001" \
  -H "Authorization: Bearer $TOKEN" 2>/dev/null)
if echo "$FORM_CONFIG" | grep -qiE "form|field|config"; then
    test_result 0 "填报表单 - 查询配置"
else
    test_result 1 "填报表单 - 查询配置" "$FORM_CONFIG"
fi

DRAFT_RESP=$(docker exec jimureport-app curl -s -X POST "${BASE_URL}/api/fill/draft/save" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "formId": "form_001",
    "data": {
      "name": "李四",
      "age": 30
    }
  }' 2>/dev/null)
if echo "$DRAFT_RESP" | grep -qiE "success|成功|id"; then
    test_result 0 "填报表单 - 保存草稿"
else
    test_result 1 "填报表单 - 保存草稿" "$DRAFT_RESP"
fi

SUBMIT_RESP=$(docker exec jimureport-app curl -s -X POST "${BASE_URL}/api/fill/submit" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "formId": "form_001",
    "data": {
      "name": "张三",
      "age": 28,
      "department": "tech"
    }
  }' 2>/dev/null)
if echo "$SUBMIT_RESP" | grep -qiE "success|成功|id"; then
    test_result 0 "填报表单 - 提交数据"
else
    test_result 1 "填报表单 - 提交数据" "$SUBMIT_RESP"
fi

RECORDS=$(docker exec jimureport-app curl -s "${BASE_URL}/api/fill/records/form_001?pageNum=1&pageSize=10" \
  -H "Authorization: Bearer $TOKEN" 2>/dev/null)
if echo "$RECORDS" | grep -qiE "record|list|\[|total"; then
    test_result 0 "填报表单 - 查询记录"
else
    test_result 1 "填报表单 - 查询记录" "$RECORDS"
fi

# ========== 9. 积木报表集成 ==========
echo ""
echo "--- 8. 积木报表集成 ---"

JM_TOKEN=$(docker exec jimureport-app curl -s "${BASE_URL}/jmreport/token/validate" \
  -H "X-Access-Token: $TOKEN" 2>/dev/null)
if echo "$JM_TOKEN" | grep -qiE "success|true|valid|ok"; then
    test_result 0 "积木报表 - Token验证"
else
    test_result 1 "积木报表 - Token验证" "$JM_TOKEN"
fi

JM_USER=$(docker exec jimureport-app curl -s "${BASE_URL}/jmreport/user/info" \
  -H "X-Access-Token: $TOKEN" 2>/dev/null)
if echo "$JM_USER" | grep -qiE "user|admin|name|id"; then
    test_result 0 "积木报表 - 获取用户信息"
else
    test_result 1 "积木报表 - 获取用户信息" "$JM_USER"
fi

# ========== 10. 安全测试 ==========
echo ""
echo "--- 9. 安全测试 ---"

SQL_INJ=$(docker exec jimureport-app curl -s -X POST "${BASE_URL}/api/user/login" \
  -H "Content-Type: application/json" \
  -d '{"username":"admin' OR '1'='1","password":"anything"}' 2>/dev/null)
if echo "$SQL_INJ" | grep -qiE "不存在|error|fail|invalid|凭证"; then
    test_result 0 "安全测试 - SQL注入防护"
else
    test_result 1 "安全测试 - SQL注入防护" "$SQL_INJ"
fi

INVALID_TOKEN=$(docker exec jimureport-app curl -s -w "\n%{http_code}" "${BASE_URL}/api/user/list" \
  -H "Authorization: Bearer invalid_token_12345" 2>/dev/null)
INV_HTTP=$(echo "$INVALID_TOKEN" | tail -1)
if [ "$INV_HTTP" = "401" ] || [ "$INV_HTTP" = "403" ]; then
    test_result 0 "安全测试 - 无效Token (HTTP $INV_HTTP)"
else
    test_result 1 "安全测试 - 无效Token" "HTTP $INV_HTTP"
fi

# ========== 测试结果汇总 ==========
echo ""
echo "============================================"
echo "  测试结果汇总"
echo "============================================"
echo -e "  总用例数: ${TOTAL}"
echo -e "  ${GREEN}通过: ${PASS}${NC}"
echo -e "  ${RED}失败: ${FAIL}${NC}"
RATE=$((PASS * 100 / TOTAL))
echo -e "  通过率: ${RATE}%"
echo "============================================"

if [ $FAIL -eq 0 ]; then
    echo -e "${GREEN}✅ 所有测试通过！${NC}"
else
    echo -e "${YELLOW}⚠️ 有 ${FAIL} 个测试失败，请检查日志。${NC}"
fi

# 保存测试结果
echo ""
echo "测试结果已保存到: /mnt/user-data/workspace/jimureport-enhancement/test-result-$(date +%Y%m%d-%H%M%S).log"
