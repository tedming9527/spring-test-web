# 学习清单：Docker CI/CD 与多阶段构建

**日期**：2026-05-15  
**问题背景**：CI 构建的 Docker 镜像缺少前端静态文件；自托管 Runner 上存在端口冲突。

---

## 一、必须理解的概念

### 1. Docker 多阶段构建

```dockerfile
# 阶段 1：构建前端
FROM node:22 AS frontend-build
WORKDIR /frontend
COPY frontend/ .
RUN npm ci && npm run build
# 产物在 /frontend/dist/

# 阶段 2：构建后端 jar
FROM eclipse-temurin:17-jdk AS backend-build
WORKDIR /app
COPY . .
# ✅ 关键：必须在 mvnw package 之前复制前端产物
COPY --from=frontend-build /frontend/dist/ src/main/resources/static/
RUN ./mvnw clean package -DskipTests
# 产物在 /app/target/*.jar，jar 内包含 BOOT-INF/classes/static/

# 阶段 3：运行时镜像
FROM eclipse-temurin:17-jdk
WORKDIR /app
COPY --from=backend-build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

> 关键顺序：`COPY --from=frontend-build` **必须在** `RUN ./mvnw package` **之前**。  
> Maven 打包时会把 `src/main/resources/static/` 下的所有文件打入 jar，  
> 如果在打包后复制，静态文件就不在 jar 里。

### 2. GitHub Actions 自托管 Runner

- 自托管 Runner 在本机持续运行，每次 CI 都在同一台机器上执行
- **端口冲突根因**：上次部署的容器仍在运行，新部署时 `docker run -p 8080:8080` 失败
- **解决方法**：部署前先停止并删除旧容器

```bash
docker stop spring-test-web || true
docker rm spring-test-web || true
# 也清理可能占用 8080 的其他容器
CONTAINER=$(docker ps -q --filter "publish=8080")
[ -n "$CONTAINER" ] && docker stop $CONTAINER && docker rm $CONTAINER || true
```

### 3. GitHub Secrets 与环境变量的坑

Spring Boot `application.yml` 中：
```yaml
jwt:
  secret: ${JWT_SECRET:default-value}
```

- `JWT_SECRET` **未设置**（absent）→ 使用 `default-value` ✅
- `JWT_SECRET` **设置为空字符串** → 使用空字符串 ❌（不触发默认值）

GitHub Actions 中未配置的 secret 会传入**空字符串**，所以 CI workflow 需要：
```yaml
-e JWT_SECRET="${{ secrets.JWT_SECRET || 'fallback-value-32-bytes-minimum' }}"
```

### 4. `docker run` 常用参数

```bash
docker run \
  -d \               # 后台运行（detached）
  --name spring-test-web \   # 容器名，方便后续 stop/rm
  --restart always \ # 宿主机重启后自动启动
  -e KEY=VALUE \     # 环境变量
  -p 8080:8080 \     # 端口映射：宿主机端口:容器端口
  image-name
```

---

## 二、延伸阅读

- [Docker 多阶段构建官方文档](https://docs.docker.com/build/building/multi-stage/)
- [GitHub Actions 自托管 Runner](https://docs.github.com/en/actions/hosting-your-own-runners)
- [Spring Boot 外部化配置](https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.external-config)
