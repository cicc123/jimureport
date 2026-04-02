#!/bin/bash
# 测试JimuReport token验证逻辑

echo "=== 1. 登录获取Token ==="
LOGIN_RESP=$(curl -s -X POST http://localhost:8085/api/user/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}')

echo "登录响应: $LOGIN_RESP"

TOKEN=$(echo "$LOGIN_RESP" | grep -o '"token":"[^"]*"' | cut -d'"' -f4)
echo "Token: $TOKEN"

if [ -z "$TOKEN" ]; then
    echo "Token获取失败，退出"
    exit 1
fi

echo ""
echo "=== 2. 测试不同Token传递方式 ==="

echo "方式1: X-Access-Token header"
curl -s -w "\nHTTP状态码: %{http_code}\n" http://localhost:8085/jmreport/list \
  -H "X-Access-Token: $TOKEN" 2>&1 | tail -5

echo ""
echo "方式2: token header"
curl -s -w "\nHTTP状态码: %{http_code}\n" http://localhost:8085/jmreport/list \
  -H "token: $TOKEN" 2>&1 | tail -5

echo ""
echo "方式3: token query parameter"
curl -s -w "\nHTTP状态码: %{http_code}\n" "http://localhost:8085/jmreport/list?token=$TOKEN" 2>&1 | tail -5

echo ""
echo "方式4: Authorization header"
curl -s -w "\nHTTP状态码: %{http_code}\n" http://localhost:8085/jmreport/list \
  -H "Authorization: Bearer $TOKEN" 2>&1 | tail -5

echo ""
echo "=== 3. 测试JimuReport API端点 ==="

echo "测试 /jmreport/getToken:"
curl -s -w "\nHTTP状态码: %{http_code}\n" http://localhost:8085/jmreport/getToken \
  -H "X-Access-Token: $TOKEN" 2>&1 | tail -5

echo ""
echo "测试 /jmreport/checkToken:"
curl -s -w "\nHTTP状态码: %{http_code}\n" http://localhost:8085/jmreport/checkToken \
  -H "X-Access-Token: $TOKEN" 2>&1 | tail -5

echo ""
echo "测试 /jmreport/verifyToken:"
curl -s -w "\nHTTP状态码: %{http_code}\n" http://localhost:8085/jmreport/verifyToken \
  -H "X-Access-Token: $TOKEN" 2>&1 | tail -5

echo ""
echo "=== 4. 测试用户信息API ==="
curl -s -w "\nHTTP状态码: %{http_code}\n" http://localhost:8085/api/user/info \
  -H "X-Access-Token: $TOKEN" 2>&1 | tail -5

echo ""
echo "=== 5. 测试权限API ==="
curl -s -w "\nHTTP状态码: %{http_code}\n" http://localhost:8085/api/user/list \
  -H "X-Access-Token: $TOKEN" 2>&1 | tail -5
