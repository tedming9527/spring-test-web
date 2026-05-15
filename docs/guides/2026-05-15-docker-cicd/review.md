# Code Review 指南：Docker CI/CD 与多阶段构建

**日期**：2026-05-15  
**涉及文件**：
- [Dockerfile](../../../Dockerfile)
- [.github/workflows/build-docker-local.yml](../../../.github/workflows/build-docker-local.yml)

---

## 架构图

```
git push → GitHub Actions Workflow
    │
    ├─ Step 1: Checkout
    ├─ Step 2: docker build（多阶段）
    │   ├─ Stage frontend-build
    │   │   └─ npm ci && npm run build → dist/
    │   ├─ Stage backend-build
    │   │   ├─ COPY dist/ → src/main/resources/static/   ← 必须在 mvnw 之前
    │   │   └─ ./mvnw package → target/*.jar（含静态文件）
    │   └─ Stage runtime
    │       └─ COPY *.jar → app.jar
    ├─ Step 3: 停止旧容器（避免端口冲突）
    │   ├─ docker stop/rm spring-test-web
    │   └─ docker stop/rm 占用 8080 的其他容器
    └─ Step 4: docker run（注入环境变量）
```

---

## 检查点清单

### Dockerfile

- [ ] **前端产物复制顺序是否正确**
  ```dockerfile
  # ✅ 正确顺序
  COPY . .
  COPY --from=frontend-build /frontend/dist/ src/main/resources/static/
  RUN ./mvnw clean package -DskipTests
  ```
  验证方法：`jar tf target/*.jar | grep "static/assets"` 应有输出。

- [ ] **运行时镜像是否最小化**  
  当前用 `eclipse-temurin:17-jdk`（含编译工具），生产环境可考虑换 `eclipse-temurin:17-jre`（更小）。

- [ ] **`EXPOSE` 端口与 `docker run -p` 一致**  
  `EXPOSE 8080` 只是文档声明，实际端口映射由 `-p 8080:8080` 控制。

### GitHub Actions Workflow

- [ ] **旧容器清理是否健壮**
  ```yaml
  run: |
    docker stop spring-test-web || true   # 容器不存在时不报错
    docker rm spring-test-web || true
    CONTAINER=$(docker ps -q --filter "publish=8080")
    [ -n "$CONTAINER" ] && docker stop $CONTAINER && docker rm $CONTAINER || true
  ```
  `|| true` 确保命令失败时 CI 不中断。

- [ ] **JWT_SECRET 空字符串处理**
  ```yaml
  -e JWT_SECRET="${{ secrets.JWT_SECRET || 'default-secret-change-me-in-prod-32x' }}"
  ```
  GitHub 未配置 secret 时传入空字符串，`||` 运算符在 shell 层面兜底。

- [ ] **secrets.JWT_SECRET 是否在 GitHub 仓库 Settings 中配置**  
  路径：Settings → Secrets and variables → Actions → New repository secret。

---

## 常见错误排查

| 症状 | 可能原因 | 排查方法 |
|---|---|---|
| `Error: port is already allocated` | 旧容器未清理 | `docker ps \| grep 8080` |
| 访问页面只有 API，无前端 | 前端未打入 jar | `jar tf app.jar \| grep index.html` |
| 容器启动后立即退出 | 应用启动失败 | `docker logs spring-test-web` |
| `WeakKeyException` | JWT_SECRET 为空或太短 | `docker exec spring-test-web env \| grep JWT` |
