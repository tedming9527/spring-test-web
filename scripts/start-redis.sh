#!/usr/bin/env bash

set -euo pipefail

SCRIPT_DIR="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_DIR="$(cd -- "${SCRIPT_DIR}/.." && pwd)"
COMPOSE_FILE="${PROJECT_DIR}/compose.redis.yaml"

export REDIS_CONTAINER_NAME="${REDIS_CONTAINER_NAME:-spring-test-web-redis}"
export REDIS_IMAGE="${REDIS_IMAGE:-spring-test-web-redis:local}"
export REDIS_PORT="${REDIS_PORT:-6379}"
export REDIS_PASSWORD="${REDIS_PASSWORD:-123456}"
export REDIS_DATA_VOLUME="${REDIS_DATA_VOLUME:-spring-test-web-redis-data}"

if ! command -v docker >/dev/null 2>&1; then
  echo "错误：未找到 Docker，请先安装并启动 Docker。" >&2
  exit 1
fi

if ! docker info >/dev/null 2>&1; then
  echo "错误：Docker 服务未运行，请先启动 Docker。" >&2
  exit 1
fi

if ! docker compose version >/dev/null 2>&1; then
  echo "错误：未找到 Docker Compose。" >&2
  exit 1
fi

docker compose -f "${COMPOSE_FILE}" up -d --build

echo "Redis 容器 ${REDIS_CONTAINER_NAME} 已构建并启动。"
echo "连接地址：redis://:${REDIS_PASSWORD}@127.0.0.1:${REDIS_PORT}"
