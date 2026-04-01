#!/bin/bash
# Rebuild and redeploy JimuReport Enhancement

echo "=== 1. Stopping existing container ==="
docker stop jimureport-app 2>/dev/null
docker rm jimureport-app 2>/dev/null

echo ""
echo "=== 2. Building new image ==="
cd /mnt/user-data/workspace/jimureport-enhancement
docker build -t jimureport-app:latest .

echo ""
echo "=== 3. Starting new container ==="
docker run -d \
  --name jimureport-app \
  --network jimureport-net \
  -p 8085:8085 \
  -e SPRING_DATASOURCE_URL="jdbc:mysql://jimureport-mysql:3306/jimureport_enhancement?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true" \
  -e SPRING_DATASOURCE_USERNAME=root \
  -e SPRING_DATASOURCE_PASSWORD=root123 \
  -e SPRING_REDIS_HOST=jimureport-redis \
  -e SPRING_REDIS_PORT=6379 \
  -e JIMUREPORT_SECURITY_JWT_SECRET="JimuReportEnhancementSecretKey2024VeryLongSecret" \
  jimureport-app:latest

echo ""
echo "=== 4. Waiting for startup (60s for Spring Boot) ==="
sleep 60

echo ""
echo "=== 5. Checking container status ==="
docker ps --filter name=jimureport-app --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}"

echo ""
echo "=== 6. Testing health check ==="
docker exec jimureport-app curl -s http://localhost:8085/actuator/health

echo ""
echo "=== 7. Testing login ==="
docker exec jimureport-app curl -s -X POST http://localhost:8085/api/user/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}'
