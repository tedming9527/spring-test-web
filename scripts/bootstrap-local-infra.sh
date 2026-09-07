#!/usr/bin/env bash

set -euo pipefail

project_root="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
compose_file="$project_root/docker/compose.yaml"

# MySQL is managed on the host. Never start a second instance in Docker.
command -v mysql >/dev/null || { echo "请先安装本机 MySQL。" >&2; exit 1; }
mysql --protocol=TCP -h127.0.0.1 -P3306 -uroot -e 'SELECT 1' >/dev/null
for database in springtestweb springtestweb_replica xxl_job; do
  mysql --protocol=TCP -h127.0.0.1 -P3306 -uroot "$database" -e 'SELECT 1' >/dev/null
done
# Refuse to start Admin against an uninitialized scheduling database.
mysql --protocol=TCP -h127.0.0.1 -P3306 -uroot xxl_job \
  -e 'SELECT COUNT(*) FROM xxl_job_info' >/dev/null

docker compose -f "$compose_file" up -d redis xxl-job-admin
