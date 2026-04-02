#!/bin/bash

# JimuReport Enhancement 停止脚本

set -e

# 颜色输出
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

cd "$(dirname "$0")/.."

echo -e "${YELLOW}停止 JimuReport Enhancement 服务...${NC}"

if command -v docker-compose &> /dev/null; then
    docker-compose down
else
    docker compose down
fi

echo -e "${GREEN}所有服务已停止${NC}"
