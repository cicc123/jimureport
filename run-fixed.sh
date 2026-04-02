#!/bin/bash
# 停止旧容器
docker rm -f jimureport-app 2>/dev/null

# 获取 Docker 桥接 IP（通常是 172.17.0.1）
DOCKER_HOST_IP=$(ip addr show docker0 2>/dev/null | grep "inet " | awk '{print $2}' | cut -d/ -f1)
if [ -z "$DOCKER_HOST_IP" ]; then
    DOCKER_HOST_IP="172.17.0.1"
fi
echo "Using Docker host IP: $DOCKER_HOST_IP"

# 启动容器
docker run -d \
  --name jimureport-app \
  -p 8085:8085 \
  --add-host=host.docker.internal:${DOCKER_HOST_IP} \
  -e SPRING_DATASOURCE_URL="jdbc:mysql://${DOCKER_HOST_IP}:3306/jimureport?useSSL=false&serverTimezone=Asia/Shanghai&characterEncoding=utf8&allowPublicKeyRetrieval=true" \
  -e SPRING_DATASOURCE_USERNAME="root" \
  -e SPRING_DATASOURCE_PASSWORD="password" \
  -e SPRING_REDIS_HOST="${DOCKER_HOST_IP}" \
  -e SPRING_REDIS_PORT="6379" \
  jimureport-app:latest

echo "Container started. Waiting for health check..."
sleep 15
docker logs jimureport-app --tail 30
