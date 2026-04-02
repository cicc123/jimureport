#!/bin/bash

# JimuReport Enhancement 手动启动脚本（不依赖 docker-compose）

set -e

# 颜色输出
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m'

NETWORK_NAME="jimureport-net"
MYSQL_CONTAINER="jimureport-mysql"
REDIS_CONTAINER="jimureport-redis"
APP_CONTAINER="jimureport-app"

cd "$(dirname "$0")/.."

echo "============================================"
echo "  JimuReport Enhancement 手动启动"
echo "============================================"

# 清理旧容器
echo -e "${YELLOW}清理旧容器...${NC}"
docker rm -f $MYSQL_CONTAINER $REDIS_CONTAINER $APP_CONTAINER 2>/dev/null || true

# 创建网络
echo -e "${YELLOW}创建 Docker 网络...${NC}"
docker network create $NETWORK_NAME 2>/dev/null || true

# 启动 MySQL
echo -e "${YELLOW}启动 MySQL...${NC}"
docker run -d \
    --name $MYSQL_CONTAINER \
    --network $NETWORK_NAME \
    -e MYSQL_ROOT_PASSWORD=root123 \
    -e MYSQL_DATABASE=jimureport_enhancement \
    -e MYSQL_CHARACTER_SET_SERVER=utf8mb4 \
    -e MYSQL_COLLATION_SERVER=utf8mb4_general_ci \
    -p 3306:3306 \
    -v "$(pwd)/sql/mysql/init.sql:/docker-entrypoint-initdb.d/init.sql" \
    mysql:8.0 \
    --default-authentication-plugin=mysql_native_password \
    --character-set-server=utf8mb4 \
    --collation-server=utf8mb4_general_ci

# 启动 Redis
echo -e "${YELLOW}启动 Redis...${NC}"
docker run -d \
    --name $REDIS_CONTAINER \
    --network $NETWORK_NAME \
    -p 6379:6379 \
    redis:7-alpine

# 等待 MySQL 启动
echo -e "${YELLOW}等待 MySQL 启动...${NC}"
for i in {1..60}; do
    if docker exec $MYSQL_CONTAINER mysqladmin ping -h localhost -u root -proot123 --silent 2>/dev/null; then
        echo -e "${GREEN}MySQL 已启动${NC}"
        break
    fi
    echo -n "."
    sleep 2
done

# 等待 Redis 启动
echo -e "${YELLOW}等待 Redis 启动...${NC}"
for i in {1..30}; do
    if docker exec $REDIS_CONTAINER redis-cli ping 2>/dev/null | grep -q PONG; then
        echo -e "${GREEN}Redis 已启动${NC}"
        break
    fi
    echo -n "."
    sleep 1
done

# 构建应用镜像
echo -e "${YELLOW}构建应用镜像（这可能需要几分钟）...${NC}"
docker build -t jimureport-app:latest .

# 启动应用
echo -e "${YELLOW}启动应用...${NC}"
docker run -d \
    --name $APP_CONTAINER \
    --network $NETWORK_NAME \
    -p 8085:8085 \
    -e SPRING_DATASOURCE_URL="jdbc:mysql://mysql:3306/jimureport_enhancement?useUnicode=true&characterEncoding=utf-8&useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true" \
    -e SPRING_DATASOURCE_USERNAME=root \
    -e SPRING_DATASOURCE_PASSWORD=root123 \
    -e SPRING_REDIS_HOST=redis \
    -e SPRING_REDIS_PORT=6379 \
    jimureport-app:latest

# 等待应用启动
echo -e "${YELLOW}等待应用启动...${NC}"
for i in {1..60}; do
    if curl -s http://localhost:8085/actuator/health > /dev/null 2>&1; then
        echo -e "${GREEN}应用已启动${NC}"
        break
    fi
    echo -n "."
    sleep 3
done

echo ""
echo "============================================"
echo -e "${GREEN}所有服务已启动${NC}"
echo "============================================"
echo "MySQL: localhost:3306"
echo "Redis: localhost:6379"
echo "应用:  http://localhost:8085"
echo "============================================"
echo ""
echo "查看日志: docker logs -f $APP_CONTAINER"
echo "停止服务: docker rm -f $MYSQL_CONTAINER $REDIS_CONTAINER $APP_CONTAINER"
