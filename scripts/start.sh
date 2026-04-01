#!/bin/bash

# JimuReport Enhancement 启动脚本
# 使用方法: chmod +x start.sh && ./start.sh

set -e

# 颜色输出
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m'

echo "============================================"
echo "  JimuReport Enhancement 启动脚本"
echo "============================================"

# 检查 Docker
if ! command -v docker &> /dev/null; then
    echo -e "${RED}错误: 未安装 Docker${NC}"
    exit 1
fi

# 检查 Docker Compose
if ! command -v docker-compose &> /dev/null; then
    echo -e "${YELLOW}警告: 未安装 docker-compose，尝试使用 docker compose${NC}"
    COMPOSE_CMD="docker compose"
else
    COMPOSE_CMD="docker-compose"
fi

# 进入项目目录
cd "$(dirname "$0")/.."

echo -e "${YELLOW}步骤 1: 停止旧容器（如果有）${NC}"
$COMPOSE_CMD down 2>/dev/null || true

echo -e "${YELLOW}步骤 2: 构建并启动服务${NC}"
$COMPOSE_CMD up -d --build

echo -e "${YELLOW}步骤 3: 等待服务启动${NC}"
echo "等待 MySQL 启动..."
for i in {1..60}; do
    if docker exec jimureport-mysql mysqladmin ping -h localhost -u root -proot123 --silent 2>/dev/null; then
        echo -e "${GREEN}MySQL 已启动${NC}"
        break
    fi
    echo -n "."
    sleep 2
done

echo "等待 Redis 启动..."
for i in {1..30}; do
    if docker exec jimureport-redis redis-cli ping 2>/dev/null | grep -q PONG; then
        echo -e "${GREEN}Redis 已启动${NC}"
        break
    fi
    echo -n "."
    sleep 1
done

echo "等待应用启动..."
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
echo "API文档: http://localhost:8085/swagger-ui.html"
echo "============================================"
echo ""
echo "运行测试: ./scripts/test-api.sh"
echo "查看日志: docker logs -f jimureport-app"
echo "停止服务: docker-compose down"
