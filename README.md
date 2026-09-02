# spring-test-web

Spring Boot、Redis、MySQL 与 XXL-JOB 的学习项目。

## 新电脑首次启动

安装并启动 Docker Desktop 后，在项目根目录执行：

```bash
chmod +x scripts/bootstrap-local-infra.sh
./scripts/bootstrap-local-infra.sh
```

该脚本会启动本项目需要的 MySQL 8.4、Redis 7 与 XXL-JOB Admin，并且仅在 `xxl_job` 还没有表时导入官方调度中心表结构。因此重复执行安全，不会覆盖已经存在的业务库、从库或调度记录。

服务地址：

| 服务 | 地址 | 用途 |
| --- | --- | --- |
| MySQL | `127.0.0.1:3306` | 主库 `springtestweb`、从库 `springtestweb_replica`、调度库 `xxl_job` |
| Redis | `127.0.0.1:6379` | 分类缓存与分布式锁 |
| XXL-JOB Admin | http://127.0.0.1:8081/xxl-job-admin/ 或 `http://<局域网IP>:8081/xxl-job-admin/` | 调度中心；默认账号 `admin` / `123456` |

主库和从库的业务表不是由 Docker SQL 直接创建，而是由应用启动时的两套 Flyway 迁移创建；这样数据库结构始终随应用版本演进。

基础设施启动成功后，另开一个终端运行应用：

```bash
./mvnw spring-boot:run
```

### 日常命令

```bash
# 查看基础设施状态
docker compose -f docker/compose.yaml ps

# 查看 XXL-JOB Admin 日志
docker compose -f docker/compose.yaml logs -f xxl-job-admin

# 停止容器但保留数据
docker compose -f docker/compose.yaml stop
```

首次启动若本机已占用 `3306`、`6379` 或 `8081`，先停止对应的本地服务，或在 `docker/compose.yaml` 中修改端口映射。切勿在不确认数据影响的情况下删除 Docker volume。
