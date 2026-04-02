#!/bin/bash
# JimuReport Enhancement 自动化测试脚本
# 执行 TEST_PLAN.md 中的测试用例

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
    fi
}

echo "============================================"
echo "  JimuReport Enhancement 测试执行"
echo "  $(date '+%Y-%m-%d %H:%M:%S')"
echo "============================================"
echo ""

# ========== 1. 健康检查 ==========
echo "--- 1. 环境检查 ---"
HEALTH=$(docker exec jimureport-app curl -s ${BASE_URL}/actuator/health 2>/dev/null)
if echo "$HEALTH" | grep -q '"status":"UP"'; then
    test_result 0 "健康检查 [TC-ENV-001]"
else
    test_result 1 "健康检查 [TC-ENV-001] - $HEALTH"
fi

# ========== 2. 用户登录 ==========
echo ""
echo "--- 2. 用户管理模块 ---"

# TC-005: 正常登录
LOGIN_RESP=$(docker exec jimureport-app curl -s -X POST ${BASE_URL}/api/user/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}' 2>/dev/null)

TOKEN=$(echo "$LOGIN_RESP" | grep -o '"token":"[^"]*"' | cut -d'"' -f4)
if [ -n "$TOKEN" ]; then
    test_result 0 "用户登录 - 正常登录 [TC-005]"
else
    # 尝试其他可能的字段名
    TOKEN=$(echo "$LOGIN_RESP" | grep -o '"accessToken":"[^"]*"' | cut -d'"' -f4)
    if [ -n "$TOKEN" ]; then
        test_result 0 "用户登录 - 正常登录 [TC-005]"
    else
        test_result 1 "用户登录 - 正常登录 [TC-005] - $LOGIN_RESP"
    fi
fi
echo "  Token: ${TOKEN:0:50}..."

# TC-006: 密码错误
WRONG_PWD=$(docker exec jimureport-app curl -s -X POST ${BASE_URL}/api/user/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"wrongpassword"}' 2>/dev/null)
if echo "$WRONG_PWD" | grep -qiE "密码错误|password|error|fail|invalid"; then
    test_result 0 "用户登录 - 密码错误 [TC-006]"
else
    test_result 1 "用户登录 - 密码错误 [TC-006] - $WRONG_PWD"
fi

# TC-007: 用户不存在
NO_USER=$(docker exec jimureport-app curl -s -X POST ${BASE_URL}/api/user/login \
  -H "Content-Type: application/json" \
  -d '{"username":"nonexist_user_12345","password":"123456"}' 2>/dev/null)
if echo "$NO_USER" | grep -qiE "不存在|not found|error|fail|invalid"; then
    test_result 0 "用户登录 - 用户不存在 [TC-007]"
else
    test_result 1 "用户登录 - 用户不存在 [TC-007] - $NO_USER"
fi

# ========== 3. 用户注册 ==========
# TC-001: 正常注册
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
    test_result 0 "用户注册 - 正常注册 [TC-001]"
else
    test_result 1 "用户注册 - 正常注册 [TC-001] - $REGISTER_RESP"
fi

# TC-002: 用户名已存在
DUP_USER=$(docker exec jimureport-app curl -s -X POST ${BASE_URL}/api/user/register \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"Test123456"}' 2>/dev/null)
if echo "$DUP_USER" | grep -qiE "已存在|exist|duplicate|error"; then
    test_result 0 "用户注册 - 用户名已存在 [TC-002]"
else
    test_result 1 "用户注册 - 用户名已存在 [TC-002] - $DUP_USER"
fi

# ========== 4. 用户列表查询 ==========
echo ""
echo "--- 3. 用户列表查询 ---"

# TC-009: 查询用户列表
USER_LIST=$(docker exec jimureport-app curl -s "${BASE_URL}/api/user/list?pageNum=1&pageSize=10" \
  -H "Authorization: Bearer $TOKEN" 2>/dev/null)
if echo "$USER_LIST" | grep -qiE "admin|records|list|total"; then
    test_result 0 "用户列表 - 查询所有用户 [TC-009]"
else
    test_result 1 "用户列表 - 查询所有用户 [TC-009] - ${USER_LIST:0:100}"
fi

# TC-010: 关键词搜索
SEARCH_USER=$(docker exec jimureport-app curl -s "${BASE_URL}/api/user/list?keyword=admin" \
  -H "Authorization: Bearer $TOKEN" 2>/dev/null)
if echo "$SEARCH_USER" | grep -qiE "admin|records|list"; then
    test_result 0 "用户列表 - 关键词搜索 [TC-010]"
else
    test_result 1 "用户列表 - 关键词搜索 [TC-010] - ${SEARCH_USER:0:100}"
fi

# TC-012: 无权限访问
NO_AUTH=$(docker exec jimureport-app curl -s -w "\n%{http_code}" "${BASE_URL}/api/user/list" 2>/dev/null)
HTTP_CODE=$(echo "$NO_AUTH" | tail -1)
if [ "$HTTP_CODE" = "401" ] || [ "$HTTP_CODE" = "403" ]; then
    test_result 0 "用户列表 - 无权限访问 [TC-012] (HTTP $HTTP_CODE)"
else
    test_result 1 "用户列表 - 无权限访问 [TC-012] - HTTP $HTTP_CODE"
fi

# ========== 5. 角色管理 ==========
echo ""
echo "--- 4. 角色管理模块 ---"

# 角色列表
ROLE_LIST=$(docker exec jimureport-app curl -s "${BASE_URL}/api/permission/roles" \
  -H "Authorization: Bearer $TOKEN" 2>/dev/null)
if echo "$ROLE_LIST" | grep -qiE "role|admin|list|\["; then
    test_result 0 "角色列表 - 查询角色 [TC-ROLE-001]"
else
    test_result 1 "角色列表 - 查询角色 [TC-ROLE-001] - ${ROLE_LIST:0:100}"
fi

# 新增角色
ADD_ROLE=$(docker exec jimureport-app curl -s -X POST "${BASE_URL}/api/permission/role/add" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "roleName": "数据分析师_'$(date +%s)'",
    "roleKey": "analyst",
    "roleSort": 10,
    "dataScope": "3",
    "remark": "负责数据分析工作"
  }' 2>/dev/null)
if echo "$ADD_ROLE" | grep -qiE "success|成功|id"; then
    test_result 0 "角色新增 - 创建角色 [TC-ROLE-002]"
else
    test_result 1 "角色新增 - 创建角色 [TC-ROLE-002] - $ADD_ROLE"
fi

# ========== 6. 菜单管理 ==========
echo ""
echo "--- 5. 菜单管理模块 ---"

# 菜单树
MENU_TREE=$(docker exec jimureport-app curl -s "${BASE_URL}/api/permission/menus" \
  -H "Authorization: Bearer $TOKEN" 2>/dev/null)
if echo "$MENU_TREE" | grep -qiE "menu|name|children|\["; then
    test_result 0 "菜单树 - 查询菜单 [TC-MENU-001]"
else
    test_result 1 "菜单树 - 查询菜单 [TC-MENU-001] - ${MENU_TREE:0:100}"
fi

# 用户菜单权限
USER_MENUS=$(docker exec jimureport-app curl -s "${BASE_URL}/api/permission/user/menus" \
  -H "Authorization: Bearer $TOKEN" 2>/dev/null)
if echo "$USER_MENUS" | grep -qiE "menu|name|\["; then
    test_result 0 "用户菜单 - 查询权限菜单 [TC-MENU-002]"
else
    test_result 1 "用户菜单 - 查询权限菜单 [TC-MENU-002] - ${USER_MENUS:0:100}"
fi

# ========== 7. 报表权限 ==========
echo ""
echo "--- 6. 报表权限模块 ---"

# 分配报表权限
REPORT_PERM=$(docker exec jimureport-app curl -s -X POST "${BASE_URL}/api/permission/report/assign" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "reportId": "report_001",
    "roleId": "role_001",
    "permissionType": "view",
    "dataScope": "1"
  }' 2>/dev/null)
if echo "$REPORT_PERM" | grep -qiE "success|成功|id"; then
    test_result 0 "报表权限 - 分配权限 [TC-RPT-001]"
else
    test_result 1 "报表权限 - 分配权限 [TC-RPT-001] - $REPORT_PERM"
fi

# 查询报表权限
QUERY_PERM=$(docker exec jimureport-app curl -s "${BASE_URL}/api/permission/report/report_001" \
  -H "Authorization: Bearer $TOKEN" 2>/dev/null)
if echo "$QUERY_PERM" | grep -qiE "permission|report|\["; then
    test_result 0 "报表权限 - 查询权限 [TC-RPT-002]"
else
    test_result 1 "报表权限 - 查询权限 [TC-RPT-002] - ${QUERY_PERM:0:100}"
fi

# ========== 8. 填报表单 ==========
echo ""
echo "--- 7. 填报表单模块 ---"

# 创建填报表单
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
    test_result 0 "填报表单 - 创建表单 [TC-FILL-001]"
    FORM_ID=$(echo "$CREATE_FORM" | grep -o '"id":"[^"]*"' | head -1 | cut -d'"' -f4)
    if [ -z "$FORM_ID" ]; then
        FORM_ID=$(echo "$CREATE_FORM" | grep -o '"formId":"[^"]*"' | head -1 | cut -d'"' -f4)
    fi
else
    test_result 1 "填报表单 - 创建表单 [TC-FILL-001] - $CREATE_FORM"
    FORM_ID=""
fi

# 查询表单配置
FORM_CONFIG=$(docker exec jimureport-app curl -s "${BASE_URL}/api/fill/form/config/report_001" \
  -H "Authorization: Bearer $TOKEN" 2>/dev/null)
if echo "$FORM_CONFIG" | grep -qiE "form|field|config"; then
    test_result 0 "填报表单 - 查询配置 [TC-FILL-002]"
else
    test_result 1 "填报表单 - 查询配置 [TC-FILL-002] - ${FORM_CONFIG:0:100}"
fi

# 保存草稿
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
    test_result 0 "填报表单 - 保存草稿 [TC-FILL-003]"
else
    test_result 1 "填报表单 - 保存草稿 [TC-FILL-003] - $DRAFT_RESP"
fi

# 提交填报数据
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
    test_result 0 "填报表单 - 提交数据 [TC-FILL-004]"
else
    test_result 1 "填报表单 - 提交数据 [TC-FILL-004] - $SUBMIT_RESP"
fi

# 查询提交记录
RECORDS=$(docker exec jimureport-app curl -s "${BASE_URL}/api/fill/records/form_001?pageNum=1&pageSize=10" \
  -H "Authorization: Bearer $TOKEN" 2>/dev/null)
if echo "$RECORDS" | grep -qiE "record|list|\[|total"; then
    test_result 0 "填报表单 - 查询记录 [TC-FILL-005]"
else
    test_result 1 "填报表单 - 查询记录 [TC-FILL-005] - ${RECORDS:0:100}"
fi

# ========== 9. 积木报表集成 ==========
echo ""
echo "--- 8. 积木报表集成 ---"

# Token 验证
JM_TOKEN=$(docker exec jimureport-app curl -s "${BASE_URL}/jmreport/token/validate" \
  -H "X-Access-Token: $TOKEN" 2>/dev/null)
if echo "$JM_TOKEN" | grep -qiE "success|true|valid|ok"; then
    test_result 0 "积木报表 - Token验证 [TC-JM-001]"
else
    test_result 1 "积木报表 - Token验证 [TC-JM-001] - $JM_TOKEN"
fi

# 获取用户信息
JM_USER=$(docker exec jimureport-app curl -s "${BASE_URL}/jmreport/user/info" \
  -H "X-Access-Token: $TOKEN" 2>/dev/null)
if echo "$JM_USER" | grep -qiE "user|admin|name|id"; then
    test_result 0 "积木报表 - 获取用户信息 [TC-JM-002]"
else
    test_result 1 "积木报表 - 获取用户信息 [TC-JM-002] - $JM_USER"
fi

# ========== 10. 安全测试 ==========
echo ""
echo "--- 9. 安全测试 ---"

# SQL 注入测试
SQL_INJ=$(docker exec jimureport-app curl -s -X POST "${BASE_URL}/api/user/login" \
  -H "Content-Type: application/json" \
  -d '{"username":"admin'\'' OR '\''1'\''='\''1","password":"anything"}' 2>/dev/null)
if echo "$SQL_INJ" | grep -qiE "不存在|error|fail|invalid"; then
    test_result 0 "安全测试 - SQL注入防护 [TC-SEC-001]"
else
    test_result 1 "安全测试 - SQL注入防护 [TC-SEC-001] - $SQL_INJ"
fi

# 无效 Token 测试
INVALID_TOKEN=$(docker exec jimureport-app curl -s -w "\n%{http_code}" "${BASE_URL}/api/user/list" \
  -H "Authorization: Bearer invalid_token_12345" 2>/dev/null)
INV_HTTP=$(echo "$INVALID_TOKEN" | tail -1)
if [ "$INV_HTTP" = "401" ] || [ "$INV_HTTP" = "403" ]; then
    test_result 0 "安全测试 - 无效Token [TC-SEC-002] (HTTP $INV_HTTP)"
else
    test_result 1 "安全测试 - 无效Token [TC-SEC-002] - HTTP $INV_HTTP"
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
    echo -e "${GREEN}所有测试通过！${NC}"
else
    echo -e "${YELLOW}有 ${FAIL} 个测试失败，请检查日志。${NC}"
fi
