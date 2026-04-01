#!/bin/bash

# JimuReport Enhancement API 测试脚本
# 使用方法: chmod +x test-api.sh && ./test-api.sh

BASE_URL="http://localhost:8085"
TOKEN=""

# 颜色输出
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m'

# 打印分隔线
print_separator() {
    echo "============================================"
}

# 打印测试结果
print_result() {
    if [ $1 -eq 0 ]; then
        echo -e "${GREEN}✓ $2${NC}"
    else
        echo -e "${RED}✗ $2${NC}"
    fi
}

# 等待服务启动
wait_for_service() {
    echo -e "${YELLOW}等待服务启动...${NC}"
    for i in {1..30}; do
        if curl -s "$BASE_URL/actuator/health" > /dev/null 2>&1; then
            echo -e "${GREEN}服务已启动${NC}"
            return 0
        fi
        echo -n "."
        sleep 2
    done
    echo -e "${RED}服务启动超时${NC}"
    return 1
}

# 测试用户注册
test_register() {
    print_separator
    echo "测试: 用户注册"
    
    RESPONSE=$(curl -s -X POST "$BASE_URL/api/user/register" \
        -H "Content-Type: application/json" \
        -d '{
            "username": "testuser",
            "password": "Test123456",
            "realName": "测试用户",
            "email": "test@example.com",
            "phone": "13800138000"
        }')
    
    echo "响应: $RESPONSE"
    
    if echo "$RESPONSE" | grep -q '"code":200'; then
        print_result 0 "用户注册成功"
    else
        print_result 1 "用户注册失败"
    fi
}

# 测试用户登录
test_login() {
    print_separator
    echo "测试: 用户登录"
    
    RESPONSE=$(curl -s -X POST "$BASE_URL/api/user/login" \
        -H "Content-Type: application/json" \
        -d '{
            "username": "admin",
            "password": "admin123"
        }')
    
    echo "响应: $RESPONSE"
    
    # 提取 Token
    TOKEN=$(echo "$RESPONSE" | grep -o '"token":"[^"]*"' | cut -d'"' -f4)
    
    if [ -n "$TOKEN" ]; then
        print_result 0 "用户登录成功，Token: ${TOKEN:0:20}..."
    else
        print_result 1 "用户登录失败"
        exit 1
    fi
}

# 测试用户列表
test_user_list() {
    print_separator
    echo "测试: 查询用户列表"
    
    RESPONSE=$(curl -s -X GET "$BASE_URL/api/user/list?pageNum=1&pageSize=10" \
        -H "Authorization: Bearer $TOKEN")
    
    echo "响应: $RESPONSE"
    
    if echo "$RESPONSE" | grep -q '"code":200'; then
        print_result 0 "查询用户列表成功"
    else
        print_result 1 "查询用户列表失败"
    fi
}

# 测试角色列表
test_role_list() {
    print_separator
    echo "测试: 查询角色列表"
    
    RESPONSE=$(curl -s -X GET "$BASE_URL/api/permission/roles" \
        -H "Authorization: Bearer $TOKEN")
    
    echo "响应: $RESPONSE"
    
    if echo "$RESPONSE" | grep -q '"code":200'; then
        print_result 0 "查询角色列表成功"
    else
        print_result 1 "查询角色列表失败"
    fi
}

# 测试菜单树
test_menu_tree() {
    print_separator
    echo "测试: 查询菜单树"
    
    RESPONSE=$(curl -s -X GET "$BASE_URL/api/permission/menus" \
        -H "Authorization: Bearer $TOKEN")
    
    echo "响应: $RESPONSE"
    
    if echo "$RESPONSE" | grep -q '"code":200'; then
        print_result 0 "查询菜单树成功"
    else
        print_result 1 "查询菜单树失败"
    fi
}

# 测试创建填报表单
test_create_form() {
    print_separator
    echo "测试: 创建填报表单"
    
    RESPONSE=$(curl -s -X POST "$BASE_URL/api/fill/form/create" \
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
                }
            ]
        }')
    
    echo "响应: $RESPONSE"
    
    if echo "$RESPONSE" | grep -q '"code":200'; then
        print_result 0 "创建填报表单成功"
    else
        print_result 1 "创建填报表单失败"
    fi
}

# 测试无权限访问
test_unauthorized() {
    print_separator
    echo "测试: 无权限访问（应返回401）"
    
    RESPONSE=$(curl -s -o /dev/null -w "%{http_code}" -X GET "$BASE_URL/api/user/list")
    
    echo "HTTP状态码: $RESPONSE"
    
    if [ "$RESPONSE" = "401" ]; then
        print_result 0 "无权限访问正确返回401"
    else
        print_result 1 "无权限访问返回错误状态码"
    fi
}

# 测试积木报表Token验证
test_jmreport_token() {
    print_separator
    echo "测试: 积木报表Token验证"
    
    RESPONSE=$(curl -s -X GET "$BASE_URL/jmreport/token/validate" \
        -H "X-Access-Token: $TOKEN")
    
    echo "响应: $RESPONSE"
    
    if echo "$RESPONSE" | grep -q '"code":200'; then
        print_result 0 "积木报表Token验证成功"
    else
        print_result 1 "积木报表Token验证失败"
    fi
}

# 主函数
main() {
    echo "============================================"
    echo "  JimuReport Enhancement API 测试"
    echo "============================================"
    
    # 等待服务启动
    wait_for_service || exit 1
    
    # 执行测试
    test_unauthorized
    test_register
    test_login
    test_user_list
    test_role_list
    test_menu_tree
    test_create_form
    test_jmreport_token
    
    print_separator
    echo -e "${GREEN}测试完成${NC}"
    print_separator
}

main
