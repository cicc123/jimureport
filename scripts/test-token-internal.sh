#!/bin/bash
# 从容器内部测试token验证

echo "=== 1. 从容器内部登录获取Token ==="
LOGIN_RESP=$(docker exec jimureport-app curl -s -X POST http://localhost:8085/api/user/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}')

echo "登录响应: $LOGIN_RESP"

TOKEN=$(echo "$LOGIN_RESP" | grep -o '"token":"[^"]*"' | cut -d'"' -f4)
echo "Token: ${TOKEN:0:50}..."

if [ -z "$TOKEN" ]; then
    echo "Token获取失败，退出"
    exit 1
fi

echo ""
echo "=== 2. 测试不同Token传递方式 ==="

echo "方式1: X-Access-Token header"
docker exec jimureport-app curl -s -w "\nHTTP状态码: %{http_code}\n" http://localhost:8085/jmreport/list \
  -H "X-Access-Token: $TOKEN" 2>&1 | tail -5

echo ""
echo "方式2: token header"
docker exec jimureport-app curl -s -w "\nHTTP状态码: %{http_code}\n" http://localhost:8085/jmreport/list \
  -H "token: $TOKEN" 2>&1 | tail -5

echo ""
echo "方式3: token query parameter"
docker exec jimureport-app curl -s -w "\nHTTP状态码: %{http_code}\n" "http://localhost:8085/jmreport/list?token=$TOKEN" 2>&1 | tail -5

echo ""
echo "方式4: Authorization header"
docker exec jimureport-app curl -s -w "\nHTTP状态码: %{http_code}\n" http://localhost:8085/jmreport/list \
  -H "Authorization: Bearer $TOKEN" 2>&1 | tail -5

echo ""
echo "=== 3. 测试JimuReport API端点 ==="

echo "测试 /jmreport/getToken:"
docker exec jimureport-app curl -s -w "\nHTTP状态码: %{http_code}\n" http://localhost:8085/jmreport/getToken \
  -H "X-Access-Token: $TOKEN" 2>&1 | tail -5

echo ""
echo "测试 /jmreport/checkToken:"
docker exec jimureport-app curl -s -w "\nHTTP状态码: %{http_code}\n" http://localhost:8085/jmreport/checkToken \
  -H "X-Access-Token: $TOKEN" 2>&1 | tail -5

echo ""
echo "测试 /jmreport/verifyToken:"
docker exec jimureport-app curl -s -w "\nHTTP状态码: %{http_code}\n" http://localhost:8085/jmreport/verifyToken \
  -H "X-Access-Token: $TOKEN" 2>&1 | tail -5

echo ""
echo "=== 4. 测试用户信息API ==="
docker exec jimureport-app curl -s -w "\nHTTP状态码: %{http_code}\n" http://localhost:8085/api/user/info \
  -H "X-Access-Token: $TOKEN" 2>&1 | tail -5

echo ""
echo "=== 5. 测试权限API ==="
docker exec jimureport-app curl -s -w "\nHTTP状态码: %{http_code}\n" http://localhost:8085/api/user/list \
  -H "X-Access-Token: $TOKEN" 2>&1 | tail -5
