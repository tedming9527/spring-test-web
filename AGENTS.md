# spring-test-web

Spring Boot Web 学习项目，用于验证 Spring Boot 4.x + MyBatis-Plus 的集成方案。

## 技术栈

| 组件 | 版本 |
|---|---|
| Java | 17 |
| Spring Boot | 4.1.0-M2 |
| MyBatis-Plus | 3.5.17（`mybatis-plus-spring-boot4-starter`） |
| MySQL | 8.x（`springtestweb` 库） |
| 分页 | MyBatis-Plus `PaginationInnerInterceptor` + `mybatis-plus-jsqlparser` |
| 密码加密 | `spring-security-crypto` BCrypt |
| JWT | `jjwt` 0.11.5 |
| API 文档 | `springdoc-openapi` 3.0.0 |
| 前端 | Vite + React + Tailwind CSS（`frontend/`） |

## 包结构

```
org.example.springtestweb
├── config/          MybatisPlusConfig（分页拦截器）
├── configurer/      CorsConfig、WebConfigurer（拦截器）
├── controller/      AuthController、UserController、HelloController
├── filter/          SpaRoutingFilter（SPA 路由转发）
├── interceptor/     AuthTokenInterceptor、LogInterceptor、TimeInterceptor
├── mapper/          UserMapper extends BaseMapper<User>
├── model/           User 实体类
└── util/            JwtUtil
```

## 关键决策

- **从 JPA 迁移到 MyBatis-Plus**：因 Spring Boot 4.1.0-M2 与 JPA 的兼容性问题
- **使用 `mybatis-plus-spring-boot4-starter`**：MyBatis-Plus 官方对 Boot 4.x 的 starter，一个依赖替代了之前的三个手动管理
- **语言混合**：项目同时包含 Java 和 Kotlin 编译插件
- **前端 SPA 路由**：`SpaRoutingFilter` 将非 API/非文件请求 forward 到 `/index.html`

## 运行方式

```bash
# 确保本地 MySQL 可访问（端口 3306，库 springtestweb）
./mvnw spring-boot:run

# 前端构建输出到 src/main/resources/static/
cd frontend && npm run build
```

## 文档规则

所有学习笔记、知识文档记录到 iCloud，不在项目 `docs/` 内保存：

**文档目录**: `/Users/dongdeming/Library/Mobile Documents/com~apple~CloudDocs/mephisto解析/`

## 数据库

- 本地 MySQL，root 无密码，主机 `127.0.0.1`
- 默认值可通过 `DB_HOST` 环境变量覆盖
- 所有账号密码统一设为 `123456` 的 BCrypt hash
