#!/bin/bash
echo "=== Health Check ==="
docker exec jimureport-app curl -s http://localhost:8085/actuator/health
echo -e "\n\n=== Login Test ==="
docker exec jimureport-app curl -s -X POST http://localhost:8085/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}'
echo ""
