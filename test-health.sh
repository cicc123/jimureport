#!/bin/bash
docker exec jimureport-app curl -v http://localhost:8085/actuator/health
