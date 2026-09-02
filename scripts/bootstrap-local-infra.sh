#!/usr/bin/env bash

set -euo pipefail

project_root="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
compose_file="$project_root/docker/compose.yaml"

docker compose -f "$compose_file" up -d mysql redis

until docker compose -f "$compose_file" exec -T mysql mysqladmin ping -h localhost -uroot --silent >/dev/null; do
  sleep 2
done

table_count="$(docker compose -f "$compose_file" exec -T mysql mysql -uroot -N -e \"SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = 'xxl_job'\")"
if [[ "$table_count" == "0" ]]; then
  schema_file="$(mktemp)"
  trap 'rm -f "$schema_file"' EXIT
  curl --fail --location --silent --show-error \
    https://raw.githubusercontent.com/xuxueli/xxl-job/5af44150503024e86409fbb30b103faa46d4af10/doc/db/tables_xxl_job.sql \
    --output "$schema_file"
  docker compose -f "$compose_file" exec -T mysql mysql -uroot xxl_job < "$schema_file"
fi

docker compose -f "$compose_file" up -d xxl-job-admin
