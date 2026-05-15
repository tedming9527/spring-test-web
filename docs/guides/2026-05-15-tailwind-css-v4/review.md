# Code Review 指南：Tailwind CSS v4 迁移

**日期**：2026-05-15  
**涉及文件**：
- [vite.config.ts](../../../frontend/vite.config.ts)
- [src/index.css](../../../frontend/src/index.css)
- [src/components/Layout.tsx](../../../frontend/src/components/Layout.tsx)
- [src/pages/auth/LoginForm.tsx](../../../frontend/src/pages/auth/LoginForm.tsx)
- [src/pages/auth/LoginPage.tsx](../../../frontend/src/pages/auth/LoginPage.tsx)

---

## 架构图：样式层变化

```
迁移前：
  index.css ──── :root { CSS 变量 }
            ├─── .navbar { ... }         ← 全局组件类（污染全局）
            ├─── .btn { ... }
            ├─── .login-card { ... }
            └─── .status-card { ... }

  组件.tsx ──── className="navbar"       ← 依赖全局，难以追踪

迁移后：
  index.css ──── @import "tailwindcss"   ← 单行，Preflight + utility 全由 Tailwind 提供

  组件.tsx ──── className="sticky top-0 z-50 h-14 bg-white ..."
                                          ← 样式完全内聚在组件文件内
```

---

## 检查点清单

### 1. 安装与配置

- [ ] **`package.json` 中 `tailwindcss` 和 `@tailwindcss/vite` 是否在 `devDependencies`？**  
  Tailwind 是纯编译时工具，不应出现在 `dependencies` 中。

- [ ] **`vite.config.ts` 插件顺序是否正确？**  
  ```ts
  plugins: [react(), tailwindcss()]
  ```
  `react()` 在前（处理 JSX），`tailwindcss()` 在后（处理 CSS）。顺序颠倒不会报错但属规范问题。

- [ ] **`index.css` 是否只有 `@import "tailwindcss"` 一行（加注释）？**  
  不应再有任何手写的组件类或 `:root` 变量。如需自定义 token，使用 `@theme {}` 块。

- [ ] **Node.js 版本是否 ≥ 20？**  
  `@tailwindcss/oxide`（Rust native addon）要求 Node ≥ 20。  
  开发机/CI 必须使用 `nvm use 22`（或 `.nvmrc` 锁定版本）。  
  Docker 构建镜像使用 `node:22`，不受影响。

---

### 2. Layout.tsx

- [ ] **sticky 导航栏 `z-index` 是否足够？**  
  当前 `z-50`（= z-index: 50）。如果后续添加 Modal/Drawer（通常 `z-50` 或 `z-[100]`），需要协调层级。

- [ ] **NavLink active 状态是否用了函数形式，而非硬编码字符串？**  
  ```tsx
  // ✅ 正确
  className={({ isActive }) => isActive ? "...active..." : "...default..."}
  // ❌ 错误（不会响应 isActive）
  className="nav-link"
  ```

- [ ] **退出登录按钮是否用 `<button>` 而非 `<div>`/`<span>`？**  
  语义化和无障碍访问要求可交互元素使用 `<button>`。

- [ ] **`bg-transparent` + `border-none` 是否用于重置 `<button>` 默认样式？**  
  浏览器对 `<button>` 有默认 `border` 和 `background`，Tailwind 的 Preflight 不完全重置，需手动加 `bg-transparent border-none`（或 `appearance-none`）。

---

### 3. LoginForm.tsx

- [ ] **输入框 focus ring 是否使用了 `focus:ring-2 focus:ring-indigo-500/20`？**  
  纯 `border-color` 变化有时不够明显，`ring` 提供额外的外框视觉反馈，符合 WCAG 可访问性要求。

- [ ] **disabled 按钮是否有 `cursor-not-allowed` 和 `opacity` 降低？**  
  ```tsx
  className="... disabled:opacity-55 disabled:cursor-not-allowed"
  ```

- [ ] **错误提示 `<div>` 是否有充足的视觉对比度？**  
  `text-red-600` 在白色背景上对比度 ≥ 4.5:1，符合 WCAG AA。  
  Dark mode 使用 `dark:text-red-400`（避免 `red-600` 在深色背景上过暗）。

- [ ] **表单提交时是否防止了默认行为？**  
  ```tsx
  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault(); // ← 必须有
    ...
  };
  ```

---

### 4. 通用 Tailwind 规范

- [ ] **长 className 是否有合理的换行或提取？**  
  超过 5-6 个 utility 的重复片段建议提取为变量或组件 prop：
  ```tsx
  const cardClass = "bg-white dark:bg-gray-800 border border-gray-200 dark:border-gray-700 rounded-lg p-4 shadow-sm";
  ```

- [ ] **是否避免了 `!important`（`!` 前缀）的滥用？**  
  Tailwind 的 `!text-red-500` 会生成 `!important`，只应在覆盖第三方样式时使用。

- [ ] **颜色是否统一使用了 Tailwind 调色板，而非任意十六进制？**  
  自定义颜色应通过 `@theme {}` 注册，而不是 `text-[#6366f1]` 散落在各处。

---

### 5. 构建产物检查

- [ ] **构建后 CSS 体积是否合理？**  
  Tailwind v4（Oxide 引擎）会 tree-shake 未使用的 utility。  
  当前构建：`dist/assets/index-*.css ≈ 17.76 kB (gzip: 4.36 kB)`，属于正常范围。

- [ ] **`dist/` 目录是否被 `.gitignore` 排除？**  
  构建产物不应提交到仓库，由 CI/Docker 在构建时生成。

---

## 常见错误与排查

| 问题 | 原因 | 解决 |
|------|------|------|
| `CustomEvent is not defined` 构建报错 | Node.js < 20，Vite 8 不支持 | `nvm use 22` |
| `Cannot find module 'tailwindcss-oxide.darwin-arm64.node'` | 用错 Node 版本安装了 oxide native | 切换 Node 22 后 `npm install` |
| 样式完全没有生效 | `index.css` 没有 `@import "tailwindcss"` 或 vite.config 缺 plugin | 检查两处配置 |
| Dark mode 不响应 | 使用了 `class` 策略但没有给 `<html>` 加 `.dark` | 确认是 `media` 策略（v4 默认）还是 `class` 策略 |
| NavLink 不高亮 | `className` 用了字符串而非函数 | 改为 `className={({ isActive }) => ...}` |
