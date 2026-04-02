#!/bin/bash
TOKEN=$(docker exec jimureport-app curl -s -X POST http://localhost:8085/api/user/login -H "Content-Type: application/json" -d '{"username":"admin","password":"admin123"}' | grep -o '"token":"[^"]*"' | cut -d'"' -f4)

echo "=== Token: ${TOKEN:0:50}... ==="
echo ""

echo "=== 1. /api/user/current (get current user info) ==="
docker exec jimureport-app curl -s http://localhost:8085/api/user/current -H "X-Access-Token: $TOKEN"
echo ""

echo ""
echo "=== 2. /api/user/list (get user list) ==="
docker exec jimureport-app curl -s http://localhost:8085/api/user/list -H "X-Access-Token: $TOKEN"
echo ""

echo ""
echo "=== 3. /api/department/list (get department list) ==="
docker exec jimureport-app curl -s http://localhost:8085/api/department/list -H "X-Access-Token: $TOKEN"
echo ""

echo ""
echo "=== 4. /api/role/list (get role list) ==="
docker exec jimureport-app curl -s http://localhost:8085/api/role/list -H "X-Access-Token: $TOKEN"
echo ""

echo ""
echo "=== 5. /api/menu/list (get menu list) ==="
docker exec jimureport-app curl -s http://localhost:8085/api/menu/list -H "X-Access-Token: $TOKEN"
echo ""

echo ""
echo "=== 6. /api/menu/tree (get menu tree) ==="
docker exec jimureport-app curl -s http://localhost:8085/api/menu/tree -H "X-Access-Token: $TOKEN"
echo ""
