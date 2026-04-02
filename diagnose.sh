#!/bin/bash
echo "=== Container Network Config ==="
docker exec jimureport-app cat /etc/hosts
echo ""
echo "=== Redis Connection Test ==="
docker exec jimureport-app ping -c 1 host.docker.internal 2>&1 || echo "host.docker.internal not reachable"
echo ""
echo "=== Container IP ==="
docker inspect jimureport-app --format='{{range .NetworkSettings.Networks}}{{.IPAddress}}{{end}}'
