#!/bin/bash
docker run -d --name jimureport-app -p 8085:8085 \
  -e SPRING_DATASOURCE_URL="jdbc:mysql://host.docker.internal:3306/jimureport?useSSL=false&serverTimezone=Asia/Shanghai&characterEncoding=utf8&allowPublicKeyRetrieval=true" \
  -e SPRING_DATASOURCE_USERNAME="root" \
  -e SPRING_DATASOURCE_PASSWORD="password" \
  -e SPRING_REDIS_HOST="host.docker.internal" \
  -e SPRING_REDIS_PORT="6379" \
  jimureport-app:latest
