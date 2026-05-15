# 学习清单：Tailwind CSS v4 迁移

**日期**：2026-05-15  
**问题背景**：项目原本使用全局 CSS（`index.css` 混放组件样式）+ CSS Modules，样式命名繁琐，维护成本高。  
迁移目标：使用 **Tailwind CSS v4 + Vite 插件**，以 utility-first 方式彻底消除全局组件类。

---

## 一、必须理解的概念

### 1. Utility-First 与传统 CSS 的本质区别

```
传统方式（语义类）：
  CSS → .login-card { padding: 40px; border-radius: 14px; ... }
  JSX → <div className="login-card">

Utility-First（Tailwind）：
  CSS → 不写（Tailwind 自动生成）
  JSX → <div className="p-10 rounded-xl bg-white shadow-md border border-gray-200">
```

**核心思路**：把样式决策从 CSS 文件移到组件里，组件文件是唯一的真相来源。

### 2. Tailwind v4 与 v3 的架构变化

| 特性 | v3 | v4 |
|------|----|----|
| 配置文件 | `tailwind.config.js`（必须） | 可选（CSS 文件内 `@theme` 替代） |
| Vite 接入 | `postcss.config.js` + plugin | `@tailwindcss/vite` 直接作为 Vite 插件 |
| CSS 入口 | `@tailwind base/components/utilities` | 单行 `@import "tailwindcss"` |
| 引擎 | JIT（JavaScript） | Oxide（Rust，更快） |
| 自定义主题 | `theme.extend` in JS | `@theme {}` in CSS |

**v4 接入只需两步：**
```ts
// vite.config.ts
import tailwindcss from '@tailwindcss/vite'
export default defineConfig({ plugins: [react(), tailwindcss()] })
```
```css
/* index.css */
@import "tailwindcss";
```

### 3. Tailwind Preflight（内置 Reset）

`@import "tailwindcss"` 自动包含 **Preflight**，它基于 `modern-normalize`，会：
- 重置所有元素 `box-sizing: border-box`
- 移除默认 `margin`/`padding`
- 统一字体继承

因此不再需要手写 reset。

### 4. Dark Mode 支持

Tailwind v4 默认使用 `@media (prefers-color-scheme: dark)` 策略：

```tsx
// 同一个元素，light/dark 不同背景
<div className="bg-white dark:bg-gray-800">
```

**常用颜色对组合：**

| 用途 | Light | Dark |
|------|-------|------|
| 页面背景 | `bg-gray-100` | `dark:bg-gray-900` |
| 卡片/表面 | `bg-white` | `dark:bg-gray-800` |
| 边框 | `border-gray-200` | `dark:border-gray-700` |
| 正文 | `text-gray-600` | `dark:text-gray-400` |
| 标题 | `text-gray-900` | `dark:text-gray-100` |
| 次要文字 | `text-gray-400` | （无需变，自适应） |
| 主色 | `text-indigo-500` | `dark:text-indigo-400` |

### 5. NavLink active 状态处理

React Router 的 `NavLink` 提供 `className` 函数形式：

```tsx
// ✅ 正确做法：利用 isActive 切换 Tailwind 类
<NavLink
  to="/welcome"
  className={({ isActive }) =>
    isActive
      ? "px-3 py-1.5 rounded text-sm bg-indigo-50 text-indigo-500"
      : "px-3 py-1.5 rounded text-sm text-gray-600 hover:bg-indigo-50"
  }
>
  首页
</NavLink>
```

提取为函数避免重复：
```tsx
const navLinkClass = ({ isActive }: { isActive: boolean }) =>
  `px-3 py-1.5 rounded text-sm font-medium ${
    isActive ? "bg-indigo-50 text-indigo-500" : "text-gray-600 hover:bg-indigo-50"
  }`;
```

---

## 二、本次迁移涉及的关键改动

### 文件变更清单

| 文件 | 改动 |
|------|------|
| `frontend/package.json` | 新增 `tailwindcss`、`@tailwindcss/vite` devDependency |
| `frontend/vite.config.ts` | 添加 `tailwindcss()` 插件 |
| `frontend/src/index.css` | 替换为 `@import "tailwindcss"` |
| `frontend/src/App.css` | 清空（保留注释） |
| `frontend/src/components/Layout.tsx` | 全部改为 Tailwind class |
| `frontend/src/pages/auth/LoginForm.tsx` | 全部改为 Tailwind class |
| `frontend/src/pages/auth/LoginPage.tsx` | wrapper div 改为 Tailwind |
| `frontend/src/pages/WelcomePage.tsx` | status card 改为 Tailwind |
| `frontend/src/pages/Sys1Page.tsx` | status card 改为 Tailwind |
| `frontend/src/pages/Sys2Page.tsx` | status card 改为 Tailwind |
| ~~`*.module.css`~~ | 全部删除 |

---

## 三、思考题

1. **什么情况下仍然推荐 CSS Modules 而非 Tailwind？**  
   提示：团队规模、CSS 复杂度、与设计系统集成

2. **Tailwind 的 `className` 字符串很长，如何保持可读性？**  
   提示：提取为变量、`clsx`/`cva` 工具库

3. **如果需要定义全局主色 `--brand: #6366f1`，在 Tailwind v4 中怎么做？**  
   提示：`@theme {}` 块

4. **`bg-indigo-50` 对应的实际颜色值是什么？Tailwind 如何保证构建后只包含用到的类？**  
   提示：Oxide 引擎扫描源文件、PurgeCSS 概念

5. **Node.js 18 为什么无法运行这套构建？**  
   提示：`@tailwindcss/oxide` 依赖的 Node 版本要求、native addon

---

## 四、延伸阅读

- [Tailwind CSS v4 官方文档](https://tailwindcss.com/docs)
- [@tailwindcss/vite 插件](https://tailwindcss.com/docs/installation/using-vite)
- [Tailwind v4 升级指南](https://tailwindcss.com/docs/upgrade-guide)
- [clsx — 条件 className 工具](https://github.com/lukeed/clsx)
- [cva — 组件 variant 抽象](https://cva.style)
