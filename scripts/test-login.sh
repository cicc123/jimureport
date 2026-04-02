#!/bin/bash

# Test login endpoint
echo "Testing login endpoint..."
curl -s -X POST http://localhost:8085/api/user/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}'
