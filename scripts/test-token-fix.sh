#!/bin/bash
TOKEN=$(docker exec jimureport-app curl -s -X POST http://localhost:8085/api/user/login -H "Content-Type: application/json" -d '{"username":"admin","password":"admin123"}' | grep -o '"token":"[^"]*"' | cut -d'"' -f4)

echo "=== Token: ${TOKEN:0:50}... ==="
echo ""

echo "=== 1. X-Access-Token header ==="
docker exec jimureport-app curl -s -o /dev/null -w "HTTP %{http_code}" http://localhost:8085/jmreport/list -H "X-Access-Token: $TOKEN"
echo ""

echo "=== 2. token header ==="
docker exec jimureport-app curl -s -o /dev/null -w "HTTP %{http_code}" http://localhost:8085/jmreport/list -H "token: $TOKEN"
echo ""

echo "=== 3. Authorization: Bearer header ==="
docker exec jimureport-app curl -s -o /dev/null -w "HTTP %{http_code}" http://localhost:8085/jmreport/list -H "Authorization: Bearer $TOKEN"
echo ""

echo "=== 4. token query parameter ==="
docker exec jimureport-app curl -s -o /dev/null -w "HTTP %{http_code}" "http://localhost:8085/jmreport/list?token=$TOKEN"
echo ""

echo ""
echo "=== 5. Test /api/user/info with X-Access-Token ==="
docker exec jimureport-app curl -s http://localhost:8085/api/user/info -H "X-Access-Token: $TOKEN"
echo ""

echo "=== 6. Test /api/user/list with X-Access-Token ==="
docker exec jimureport-app curl -s -o /dev/null -w "HTTP %{http_code}" http://localhost:8085/api/user/list -H "X-Access-Token: $TOKEN"
echo ""
