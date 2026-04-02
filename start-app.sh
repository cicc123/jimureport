#!/bin/bash
# 停止并删除旧容器
docker stop jimureport-app 2>/dev/null
docker rm jimureport-app 2>/dev/null

# 启动新容器
docker run -d \
  --name jimureport-app \
  --network jimureport-net \
  -p 8085:8085 \
  -e 'SPRING_DATASOURCE_URL=jdbc:mysql://jimureport-mysql:3306/jimureport_enhancement?useSSL=false&serverTimezone=Asia/Shanghai&characterEncoding=utf8&allowPublicKeyRetrieval=true' \
  -e 'SPRING_DATASOURCE_USERNAME=root' \
  -e 'SPRING_DATASOURCE_PASSWORD=root123' \
  -e 'SPRING_REDIS_HOST=jimureport-redis' \
  -e 'SPRING_REDIS_PORT=6379' \
  jimureport-app:latest

echo "容器已启动，等待初始化..."
sleep 25
echo "=== 应用日志 ==="
docker logs jimureport-app --tail 40
