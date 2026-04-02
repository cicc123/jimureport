#!/bin/bash

# JimuReport Enhancement API Test Script
# 测试所有核心API端点

BASE_URL="http://localhost:8085"

echo "=== JimuReport Enhancement API 测试 ==="
echo ""

# 1. 登录获取token
echo "1. 测试登录接口..."
LOGIN_RESPONSE=$(curl -s -X POST "$BASE_URL/api/user/login" \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}')

if echo "$LOGIN_RESPONSE" | grep -q "token"; then
  TOKEN=$(echo "$LOGIN_RESPONSE" | grep -o '"token":"[^"]*"' | cut -d'"' -f4)
  echo "✅ 登录成功，获取到token"
else
  echo "❌ 登录失败"
  exit 1
fi

echo ""

# 2. 测试用户相关接口
echo "2. 测试用户管理接口..."
echo "   - 获取当前用户信息..."
curl -s -X GET "$BASE_URL/api/user/current" \
  -H "Authorization: Bearer $TOKEN" | jq -r '.username' 2>/dev/null || echo "❌ 获取用户信息失败"

echo "   - 获取用户列表..."
curl -s -X GET "$BASE_URL/api/user/list" \
  -H "Authorization: Bearer $TOKEN" | jq -r '.total' 2>/dev/null || echo "❌ 获取用户列表失败"

echo ""

# 3. 测试部门管理接口
echo "3. 测试部门管理接口..."
echo "   - 获取部门列表..."
curl -s -X GET "$BASE_URL/api/department/list" \
  -H "Authorization: Bearer $TOKEN" | jq -r 'length' 2>/dev/null || echo "❌ 获取部门列表失败"

echo "   - 获取部门树..."
curl -s -X GET "$BASE_URL/api/department/tree" \
  -H "Authorization: Bearer $TOKEN" | jq -r 'length' 2>/dev/null || echo "❌ 获取部门树失败"

echo ""

# 4. 测试角色管理接口
echo "4. 测试角色管理接口..."
echo "   - 获取角色列表..."
curl -s -X GET "$BASE_URL/api/role/list" \
  -H "Authorization: Bearer $TOKEN" | jq -r 'length' 2>/dev/null || echo "❌ 获取角色列表失败"

echo "   - 获取角色详情..."
ROLE_ID=$(curl -s -X GET "$BASE_URL/api/role/list" \
  -H "Authorization: Bearer $TOKEN" | jq -r '.[0].id' 2>/dev/null)
if [ -n "$ROLE_ID" ] && [ "$ROLE_ID" != "null" ]; then
  curl -s -X GET "$BASE_URL/api/role/$ROLE_ID" \
    -H "Authorization: Bearer $TOKEN" | jq -r '.roleName' 2>/dev/null || echo "❌ 获取角色详情失败"
else
  echo "❌ 无法获取角色ID"
fi

echo ""

# 5. 测试菜单管理接口
echo "5. 测试菜单管理接口..."
echo "   - 获取菜单列表..."
curl -s -X GET "$BASE_URL/api/menu/list" \
  -H "Authorization: Bearer $TOKEN" | jq -r 'length' 2>/dev/null || echo "❌ 获取菜单列表失败"

echo "   - 获取菜单树..."
curl -s -X GET "$BASE_URL/api/menu/tree" \
  -H "Authorization: Bearer $TOKEN" | jq -r 'length' 2>/dev/null || echo "❌ 获取菜单树失败"

echo ""

# 6. 测试权限管理接口
echo "6. 测试权限管理接口..."
echo "   - 获取当前用户权限..."
curl -s -X GET "$BASE_URL/api/permission/current/permissions" \
  -H "Authorization: Bearer $TOKEN" | jq -r 'length' 2>/dev/null || echo "❌ 获取用户权限失败"

echo "   - 获取当前用户角色..."
curl -s -X GET "$BASE_URL/api/permission/current/roles" \
  -H "Authorization: Bearer $TOKEN" | jq -r 'length' 2>/dev/null || echo "❌ 获取用户角色失败"

echo "   - 获取当前用户菜单..."
curl -s -X GET "$BASE_URL/api/permission/current/menus" \
  -H "Authorization: Bearer $TOKEN" | jq -r 'length' 2>/dev/null || echo "❌ 获取用户菜单失败"

echo ""

# 7. 测试JimuReport接口
echo "7. 测试JimuReport接口..."
echo "   - 访问报表列表页面..."
curl -s -I "$BASE_URL/jmreport/list" | head -1 | grep -q "200" && echo "✅ 报表列表页面可访问" || echo "❌ 报表列表页面不可访问"

echo "   - 测试报表token验证..."
curl -s -X GET "$BASE_URL/jmreport/list?token=$TOKEN" | head -1 | grep -q "html" && echo "✅ 报表token验证成功" || echo "❌ 报表token验证失败"

echo ""

# 8. 测试Swagger接口
echo "8. 测试Swagger接口..."
curl -s -I "$BASE_URL/swagger-ui.html" | head -1 | grep -q "200" && echo "✅ Swagger UI可访问" || echo "❌ Swagger UI不可访问"

echo ""

# 9. 测试Actuator接口
echo "9. 测试Actuator接口..."
curl -s -I "$BASE_URL/actuator/health" | head -1 | grep -q "200" && echo "✅ Actuator健康检查正常" || echo "❌ Actuator健康检查失败"

echo ""
echo "=== 测试完成 ==="
