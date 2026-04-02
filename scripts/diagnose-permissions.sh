#!/bin/bash
# 诊断权限问题

echo "=== 1. 检查角色信息 ==="
docker exec jimureport-mysql mysql -uroot -proot123 jimureport_enhancement -e "
SELECT id, role_name, role_key, del_flag FROM sys_role;
" 2>&1 | grep -v "Warning"

echo ""
echo "=== 2. 检查用户角色关联 ==="
docker exec jimureport-mysql mysql -uroot -proot123 jimureport_enhancement -e "
SELECT u.username, r.role_name, r.role_key
FROM sys_user u
JOIN sys_user_role ur ON u.id = ur.user_id
JOIN sys_role r ON ur.role_id = r.id
WHERE u.username = 'admin';
" 2>&1 | grep -v "Warning"

echo ""
echo "=== 3. 测试登录响应 ==="
LOGIN_RESP=$(docker exec jimureport-app curl -s -X POST http://127.0.0.1:8085/api/user/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}' 2>/dev/null)

echo "完整响应:"
echo "$LOGIN_RESP" | python3 -m json.tool 2>/dev/null || echo "$LOGIN_RESP"

echo ""
echo "=== 4. 提取Token ==="
TOKEN=$(echo "$LOGIN_RESP" | grep -o '"token":"[^"]*"' | cut -d'"' -f4)
if [ -n "$TOKEN" ]; then
    echo "Token: ${TOKEN:0:80}..."
else
    echo "Token提取失败"
fi

echo ""
echo "=== 5. 测试权限检查 ==="
if [ -n "$TOKEN" ]; then
    echo "测试用户列表接口:"
    docker exec jimureport-app curl -s -w "\nHTTP状态码: %{http_code}\n" "http://127.0.0.1:8085/api/user/list?pageNum=1&pageSize=10" \
      -H "Authorization: Bearer $TOKEN" 2>/dev/null
    
    echo ""
    echo "测试菜单接口:"
    docker exec jimureport-app curl -s -w "\nHTTP状态码: %{http_code}\n" "http://127.0.0.1:8085/api/permission/menus" \
      -H "Authorization: Bearer $TOKEN" 2>/dev/null
fi

echo ""
echo "=== 6. 检查权限加载逻辑 ==="
echo "检查admin用户的权限集合:"
docker exec jimureport-app curl -s "http://127.0.0.1:8085/api/permission/user/menus" \
  -H "Authorization: Bearer $TOKEN" 2>/dev/null | python3 -m json.tool 2>/dev/null || echo "响应解析失败"
