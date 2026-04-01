#!/bin/bash
# 全面诊断JimuReport访问问题

echo "=========================================="
echo "  JimuReport Enhancement 诊断报告"
echo "  $(date '+%Y-%m-%d %H:%M:%S')"
echo "=========================================="

echo ""
echo "=== 1. Docker 容器状态 ==="
docker ps --filter name=jimureport --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}"

echo ""
echo "=== 2. 网络配置 ==="
echo "容器网络:"
docker network inspect jimureport-net 2>/dev/null | grep -A 10 "jimureport-app" || echo "网络检查失败"

echo ""
echo "=== 3. 容器内端口监听 ==="
docker exec jimureport-app netstat -tlnp 2>/dev/null | grep 8085 || echo "netstat不可用，尝试ss:"
docker exec jimureport-app ss -tlnp 2>/dev/null | grep 8085 || echo "ss不可用"

echo ""
echo "=== 4. 应用日志 (最近错误) ==="
docker logs jimureport-app --tail 50 2>&1 | grep -i "error\|exception\|fail\|token\|permission" | tail -20

echo ""
echo "=== 5. 测试容器内访问 ==="
echo "测试健康检查:"
docker exec jimureport-app curl -s -w "\nHTTP状态码: %{http_code}\n" http://localhost:8085/actuator/health 2>&1 | tail -5

echo ""
echo "测试jmreport/list (无Token):"
docker exec jimureport-app curl -s -w "\nHTTP状态码: %{http_code}\n" http://localhost:8085/jmreport/list 2>&1 | tail -5

echo ""
echo "=== 6. 登录测试 ==="
LOGIN_RESP=$(docker exec jimureport-app curl -s -X POST http://localhost:8085/api/user/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}' 2>&1)

echo "登录响应: $LOGIN_RESP"

TOKEN=$(echo "$LOGIN_RESP" | grep -o '"token":"[^"]*"' | cut -d'"' -f4)
if [ -n "$TOKEN" ]; then
    echo "Token获取成功: ${TOKEN:0:50}..."
    
    echo ""
    echo "=== 7. 测试带Token访问 ==="
    echo "测试jmreport/list (带Token):"
    docker exec jimureport-app curl -s -w "\nHTTP状态码: %{http_code}\n" http://localhost:8085/jmreport/list \
      -H "X-Access-Token: $TOKEN" 2>&1 | tail -10
    
    echo ""
    echo "测试jmreport/token/validate:"
    docker exec jimureport-app curl -s -w "\nHTTP状态码: %{http_code}\n" http://localhost:8085/jmreport/token/validate \
      -H "X-Access-Token: $TOKEN" 2>&1 | tail -5
    
    echo ""
    echo "测试jmreport/user/info:"
    docker exec jimureport-app curl -s -w "\nHTTP状态码: %{http_code}\n" http://localhost:8085/jmreport/user/info \
      -H "X-Access-Token: $TOKEN" 2>&1 | tail -5
else
    echo "Token获取失败"
fi

echo ""
echo "=== 8. 检查JimuReport配置 ==="
docker exec jimureport-app cat /app/application.yml 2>/dev/null | grep -A 20 "jimureport:" || echo "配置文件读取失败"

echo ""
echo "=== 9. 检查安全配置 ==="
docker exec jimureport-app cat /app/application.yml 2>/dev/null | grep -A 10 "security:" || echo "安全配置读取失败"

echo ""
echo "=== 10. 检查数据库连接 ==="
docker exec jimureport-app curl -s http://localhost:8085/actuator/health 2>&1 | grep -i "db\|database\|mysql" || echo "数据库状态检查失败"

echo ""
echo "=========================================="
echo "  诊断完成"
echo "=========================================="
