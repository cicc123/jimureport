#!/bin/bash
# 测试带Token访问jmreport

echo "=== 1. 登录获取Token ==="
LOGIN_RESP=$(curl -s -X POST http://localhost:8085/api/user/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}')

echo "登录响应: $LOGIN_RESP"

TOKEN=$(echo "$LOGIN_RESP" | grep -o '"token":"[^"]*"' | cut -d'"' -f4)
echo "Token: ${TOKEN:0:50}..."

echo ""
echo "=== 2. 带Token访问jmreport/list ==="
curl -s -w "\nHTTP状态码: %{http_code}\n" http://localhost:8085/jmreport/list \
  -H "X-Access-Token: $TOKEN" 2>&1 | tail -10

echo ""
echo "=== 3. 测试jmreport/token/validate ==="
curl -s -w "\nHTTP状态码: %{http_code}\n" http://localhost:8085/jmreport/token/validate \
  -H "X-Access-Token: $TOKEN" 2>&1

echo ""
echo "=== 4. 测试jmreport/user/info ==="
curl -s -w "\nHTTP状态码: %{http_code}\n" http://localhost:8085/jmreport/user/info \
  -H "X-Access-Token: $TOKEN" 2>&1
