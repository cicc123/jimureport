#!/bin/bash
echo "=== 测试本地访问 jmreport ==="
curl -s -w "\nHTTP状态码: %{http_code}\n" http://localhost:8085/jmreport/list 2>&1 | tail -5

echo ""
echo "=== 测试 127.0.0.1 访问 ==="
curl -s -w "\nHTTP状态码: %{http_code}\n" http://127.0.0.1:8085/jmreport/list 2>&1 | tail -5

echo ""
echo "=== 检查容器日志 (最近20行) ==="
docker logs jimureport-app --tail 20 2>&1

echo ""
echo "=== 检查防火墙状态 ==="
iptables -L -n 2>&1 | head -20 || echo "iptables not available"

echo ""
echo "=== 检查端口监听 ==="
netstat -tlnp 2>/dev/null | grep 8085 || ss -tlnp 2>/dev/null | grep 8085
