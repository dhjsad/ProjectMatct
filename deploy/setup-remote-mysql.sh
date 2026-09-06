#!/bin/bash
set -euo pipefail

mysql -uroot -p'Aa123456' <<'SQL'
CREATE DATABASE IF NOT EXISTS projectmatch DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER IF NOT EXISTS 'root'@'%' IDENTIFIED BY 'Aa123456';
ALTER USER 'root'@'%' IDENTIFIED BY 'Aa123456';
ALTER USER 'root'@'localhost' IDENTIFIED BY 'Aa123456';
GRANT ALL PRIVILEGES ON *.* TO 'root'@'%' WITH GRANT OPTION;
GRANT ALL PRIVILEGES ON *.* TO 'root'@'localhost' WITH GRANT OPTION;
FLUSH PRIVILEGES;
SHOW DATABASES;
SQL

if command -v firewall-cmd >/dev/null 2>&1; then
  firewall-cmd --permanent --add-port=3306/tcp || true
  firewall-cmd --reload || true
fi

ss -lnt | grep 3306 || true
echo "remote-mysql-ready"
