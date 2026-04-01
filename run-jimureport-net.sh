#!/bin/bash
# 停止旧容器
docker rm -f jimureport-app 2>/dev/null

# 启动容器，使用 jimureport-net 网络
docker run -d \
  --name jimureport-app \
  --network jimureport-net \
  -p 8085:8085 \
  -e SPRING_DATASOURCE_URL="jdbc:mysql://jimureport-mysql:3306/jimureport?useSSL=false&serverTimezone=Asia/Shanghai&characterEncoding=utf8&allowPublicKeyRetrieval=true" \
  -e SPRING_DATASOURCE_USERNAME="root" \
  -e SPRING_DATASOURCE_PASSWORD="password" \
  -e SPRING_REDIS_HOST="jimureport-redis" \
  -e SPRING_REDIS_PORT="6379" \
  jimureport-app:latest

echo "Container started. Waiting for health check..."
sleep 20
docker logs jimureport-app --tail 30
