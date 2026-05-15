# 开发指南清单

每个指南对应一次具体的功能实现，包含学习清单和 Code Review 要点。

## 目录结构

```
guides/
├── README.md              ← 本文件，总索引
├── YYYY-MM-DD-<topic>/
│   ├── checklist.md       ← 学习清单（概念 + 问题）
│   └── review.md          ← Code Review 指南（检查点 + 架构图）
```

## 指南索引

| 日期 | 主题 | 目录 |
|------|------|------|
| 2026-05-15 | SPA 路由 + 静态资源 MIME 处理 | [2026-05-15-spa-routing-and-static-assets](./2026-05-15-spa-routing-and-static-assets/) |
| 2026-05-15 | JWT 认证实现 | [2026-05-15-jwt-authentication](./2026-05-15-jwt-authentication/) |
| 2026-05-15 | Docker CI/CD 与多阶段构建 | [2026-05-15-docker-cicd](./2026-05-15-docker-cicd/) |

---

> 添加新指南时，在上方表格末尾追加一行，并在 `guides/` 下新建对应目录。
