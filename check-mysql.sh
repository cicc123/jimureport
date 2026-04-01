#!/bin/bash
echo "=== MySQL Users ==="
docker exec jimureport-mysql mysql -uroot -proot123 -e "SELECT user, host FROM mysql.user;"
echo -e "\n=== Check Database ==="
docker exec jimureport-mysql mysql -uroot -proot123 -e "USE jimureport_enhancement; SHOW TABLES;"
