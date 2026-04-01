#!/bin/bash
# 检查网络和配置问题

echo "=== 1. 检查Docker网络配置 ==="
docker network ls
echo ""
docker network inspect jimureport-net 2>/dev/null | head -50

echo ""
echo "=== 2. 检查容器端口映射 ==="
docker port jimureport-app

echo ""
echo "=== 3. 检查防火墙规则 ==="
iptables -L -n 2>/dev/null | head -30 || echo "iptables不可用"

echo ""
echo "=== 4. 检查系统端口监听 ==="
netstat -tlnp 2>/dev/null | grep 8085 || ss -tlnp 2>/dev/null | grep 8085 || echo "端口检查工具不可用"

echo ""
echo "=== 5. 测试外部IP访问 ==="
# 获取主机IP地址
echo "主机IP地址:"
hostname -I 2>/dev/null || ip addr show 2>/dev/null | grep -E "inet " | awk '{print $2}' | head -5

echo ""
echo "=== 6. 检查JimuReport配置 ==="
docker exec jimureport-app cat /app/application.yml 2>/dev/null | grep -A 30 "jimureport:" || echo "配置读取失败"

echo ""
echo "=== 7. 检查安全配置 ==="
docker exec jimureport-app cat /app/application.yml 2>/dev/null | grep -A 20 "security:" || echo "安全配置读取失败"

echo ""
echo "=== 8. 检查应用启动日志 ==="
docker logs jimureport-app 2>&1 | grep -i "started\|port\|token\|security" | tail -20

echo ""
echo "=== 9. 测试不同IP访问 ==="
for ip in localhost 127.0.0.1 0.0.0.0; do
    echo "测试 $ip:"
    curl -s -w "HTTP状态码: %{http_code}\n" http://$ip:8085/actuator/health 2>&1 | tail -2
    echo ""
done

echo ""
echo "=== 10. 检查JimuReport版本 ==="
docker exec jimureport-app ls -la /app/ 2>/dev/null | head -10
docker exec jimureport-app find /app -name "*.jar" 2>/dev/null | grep -i jimu | head -5
