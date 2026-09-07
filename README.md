# spring-test-web

Spring Boot、Redis、MySQL 与 XXL-JOB 的学习项目。

## 本地部署

MySQL 使用本机 Homebrew 安装的单实例，不部署到 Docker。Redis 和 XXL-JOB Admin 使用 Docker。

| 服务 | 地址 | 用途 |
| --- | --- | --- |
| 本机 MySQL | `127.0.0.1:3306` | 主库 `springtestweb`、从库 `springtestweb_replica`、调度库 `xxl_job` |
| Redis | `127.0.0.1:6379` | 分类缓存与分布式锁 |
| XXL-JOB Admin | `http://127.0.0.1:8081/xxl-job-admin/` | 调度中心 |

两个业务库是同一个 MySQL 实例中的独立 schema，通过应用任务同步，不是 MySQL 原生主从复制。`33060` 是同一进程的 MySQL X Protocol 端口，不代表第二个实例。

已安装 MySQL 的本机可执行：

```bash
brew services start mysql
./scripts/bootstrap-local-infra.sh
./mvnw spring-boot:run
```

启动脚本先检查本机 MySQL、三个数据库和调度任务表，再启动 Redis 与 Admin；不会创建 MySQL 容器、导入数据或覆盖已有数据库。新电脑需先准备三个数据库，并导入对应版本的 XXL-JOB 官方调度表。主库和从库业务表由应用的两套 Flyway 迁移分别管理；从库全量数据初始化是单独步骤，不在基础设施启动时自动执行。

Admin 容器通过 `host.docker.internal:3306` 连接本机 MySQL。若连接失败，检查 Docker Desktop 到宿主机的连通性和 MySQL 账号授权，不要另启 MySQL 容器。

```bash
# 查看本机 MySQL 与容器状态
brew services list
docker compose -f docker/compose.yaml ps

# 查看调度中心日志
docker compose -f docker/compose.yaml logs -f xxl-job-admin

# 仅停止 Redis 和调度中心，保留数据
docker compose -f docker/compose.yaml stop
```

2026-09-07 本机核查：MySQL 9.3.0，数据目录 `/opt/homebrew/var/mysql/`，仅一个 mysqld 进程；主库分类 40 条、从库分类 0 条。此为环境快照，不表示从库同步已完成。保留现有数据目录及 Docker volume，不通过删除数据来解决端口冲突。
