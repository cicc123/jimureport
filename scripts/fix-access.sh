#!/bin/bash
# 解决JimuReport访问问题

echo "=========================================="
echo "  JimuReport Enhancement 问题修复"
echo "  $(date '+%Y-%m-%d %H:%M:%S')"
echo "=========================================="

echo ""
echo "=== 问题分析 ==="
echo "1. 容器内部访问正常 (localhost:8085)"
echo "2. 外部访问失败 (10.8.0.1:8085)"
echo "3. 可能原因："
echo "   - 端口映射问题"
echo "   - 防火墙限制"
echo "   - 网络配置问题"

echo ""
echo "=== 1. 检查当前端口映射 ==="
docker port jimureport-app

echo ""
echo "=== 2. 检查容器网络模式 ==="
docker inspect jimureport-app --format '{{.HostConfig.NetworkMode}}'

echo ""
echo "=== 3. 检查容器IP地址 ==="
docker inspect jimureport-app --format '{{range .NetworkSettings.Networks}}{{.IPAddress}}{{end}}'

echo ""
echo "=== 4. 测试容器内部访问 ==="
docker exec jimureport-app curl -s -w "\nHTTP状态码: %{http_code}\n" http://localhost:8085/actuator/health 2>&1 | tail -3

echo ""
echo "=== 5. 测试宿主机访问 ==="
echo "宿主机IP地址:"
hostname -I 2>/dev/null || ip addr show 2>/dev/null | grep -E "inet " | awk '{print $2}' | head -5

echo ""
echo "=== 6. 检查防火墙状态 ==="
if command -v ufw &> /dev/null; then
    echo "UFW防火墙状态:"
    ufw status
elif command -v firewall-cmd &> /dev/null; then
    echo "Firewalld状态:"
    firewall-cmd --state
else
    echo "未检测到防火墙管理工具"
fi

echo ""
echo "=== 7. 检查端口监听 ==="
echo "检查8085端口监听状态:"
netstat -tlnp 2>/dev/null | grep 8085 || ss -tlnp 2>/dev/null | grep 8085 || echo "端口检查工具不可用"

echo ""
echo "=== 8. 测试从宿主机访问 ==="
echo "测试localhost:8085:"
curl -s -w "HTTP状态码: %{http_code}\n" http://localhost:8085/actuator/health 2>&1 | tail -2

echo ""
echo "测试127.0.0.1:8085:"
curl -s -w "HTTP状态码: %{http_code}\n" http://127.0.0.1:8085/actuator/health 2>&1 | tail -2

echo ""
echo "=== 9. 检查Docker守护进程配置 ==="
if [ -f /etc/docker/daemon.json ]; then
    echo "Docker守护进程配置:"
    cat /etc/docker/daemon.json
else
    echo "未找到Docker守护进程配置文件"
fi

echo ""
echo "=== 10. 检查系统端口转发 ==="
echo "检查iptables规则:"
iptables -L -n 2>/dev/null | grep 8085 || echo "iptables不可用或无相关规则"

echo ""
echo "=========================================="
echo "  修复建议"
echo "=========================================="

echo ""
echo "如果外部访问失败，可以尝试以下方法："
echo ""
echo "方法1: 重启Docker服务"
echo "sudo systemctl restart docker"
echo ""
echo "方法2: 重新创建容器"
echo "docker-compose down && docker-compose up -d"
echo ""
echo "方法3: 检查防火墙设置"
echo "sudo ufw allow 8085/tcp"
echo ""
echo "方法4: 使用宿主机网络模式"
echo "修改docker-compose.yml，添加: network_mode: host"
echo ""
echo "方法5: 检查云服务商安全组"
echo "如果是在云服务器上，检查安全组是否开放8085端口"
