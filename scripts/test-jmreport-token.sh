#!/bin/bash
BASE_URL="http://127.0.0.1:8085"

echo "=== 1. 获取Token ==="
LOGIN_RESP=$(docker exec jimureport-app curl -s -X POST ${BASE_URL}/api/user/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}' 2>/dev/null)

TOKEN=$(echo "$LOGIN_RESP" | grep -o '"token":"[^"]*"' | cut -d'"' -f4)
echo "Token: $TOKEN"

echo ""
echo "=== 2. 测试 jmreport/token/validate (X-Access-Token) ==="
docker exec jimureport-app curl -s -v "${BASE_URL}/jmreport/token/validate" \
  -H "X-Access-Token: $TOKEN" 2>&1

echo ""
echo "=== 3. 测试 jmreport/token/validate (Authorization Bearer) ==="
docker exec jimureport-app curl -s -v "${BASE_URL}/jmreport/token/validate" \
  -H "Authorization: Bearer $TOKEN" 2>&1

echo ""
echo "=== 4. 测试 jmreport/list 页面 ==="
docker exec jimureport-app curl -s -w "\nHTTP: %{http_code}" "${BASE_URL}/jmreport/list" 2>&1 | tail -5

echo ""
echo "=== 5. 检查JimuReport配置 ==="
docker exec jimureport-app env 2>/dev/null | grep -i "jimu\|token\|jmreport" || echo "无环境变量配置"
