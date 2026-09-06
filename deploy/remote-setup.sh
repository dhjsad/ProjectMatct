#!/bin/bash
set -euo pipefail

mkdir -p /opt/projectmatch/data/uploads /var/www/projectmatch
if [ -f /opt/projectmatch/setup-remote-mysql.sh ]; then
  bash /opt/projectmatch/setup-remote-mysql.sh
fi
cp /opt/projectmatch/projectmatch.service /etc/systemd/system/projectmatch.service
cp /opt/projectmatch/nginx-projectmatch.conf /etc/nginx/conf.d/projectmatch.conf

systemctl daemon-reload
systemctl enable projectmatch
systemctl restart projectmatch

nginx -t
systemctl reload nginx || systemctl restart nginx

if command -v firewall-cmd >/dev/null 2>&1; then
  firewall-cmd --permanent --add-port=80/tcp || true
  firewall-cmd --permanent --add-port=443/tcp || true
  firewall-cmd --permanent --add-port=8088/tcp || true
  firewall-cmd --reload || true
fi

if command -v setsebool >/dev/null 2>&1; then
  setsebool -P httpd_can_network_connect 1 || true
fi

sleep 3
systemctl is-active projectmatch
systemctl is-active nginx
curl -sI http://127.0.0.1:8088/ | head -5
curl -s http://127.0.0.1:18080/api/projects?size=1 | head -c 200
echo
echo "deploy-ok http://caseyai.cn/  http://www.caseyai.cn/  http://118.31.12.48:8088/"
