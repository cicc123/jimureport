#!/bin/bash
echo "=== Container Status ==="
docker ps --filter name=jimureport-app --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}"
echo ""
echo "=== Health Check ==="
docker exec jimureport-app curl -s http://127.0.0.1:8085/actuator/health
echo ""
echo "=== Swagger UI Available ==="
echo "http://localhost:8085/swagger-ui.html"
echo ""
echo "=== JimuReport Console ==="
echo "http://localhost:8085/jmreport/list"
