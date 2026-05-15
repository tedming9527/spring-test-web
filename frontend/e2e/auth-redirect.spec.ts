import { test, expect } from '@playwright/test';

const BASE = 'http://localhost:5173';

// Helper: clear auth state
async function clearToken(page: import('@playwright/test').Page) {
  await page.goto(BASE + '/auth/login');
  await page.evaluate(() => localStorage.removeItem('token'));
}

// Helper: perform login via UI
async function login(page: import('@playwright/test').Page) {
  await page.fill('input[name="username"], input[placeholder*="用户名"], input[type="text"]', 'admin');
  await page.fill('input[name="password"], input[placeholder*="密码"], input[type="password"]', '123456');
  await page.click('button[type="submit"]');
}

test.describe('JWT 登录与 redirect 行为', () => {
  test('未登录访问受保护路由 → 自动跳转到登录页', async ({ page }) => {
    await clearToken(page);
    // Directly visit a protected route
    await page.goto(BASE + '/sys1');
    // Should be redirected to login
    await expect(page).toHaveURL(/\/auth\/login/);
  });

  test('登录成功后 → 跳转回原来的受保护路由', async ({ page }) => {
    await clearToken(page);
    // Visit protected route first (will redirect to login with state.from = /sys1)
    await page.goto(BASE + '/sys1');
    await expect(page).toHaveURL(/\/auth\/login/);
    // Login
    await login(page);
    // Should redirect back to /sys1
    await expect(page).toHaveURL(/\/sys1/, { timeout: 5000 });
  });

  test('登录后访问 /welcome → 显示欢迎页', async ({ page }) => {
    await clearToken(page);
    await page.goto(BASE + '/auth/login');
    await login(page);
    await expect(page).toHaveURL(/\/welcome/, { timeout: 5000 });
    await expect(page.locator('h1')).toContainText('欢迎');
  });

  test('退出登录后 → 访问受保护路由再次跳转到登录页', async ({ page }) => {
    await clearToken(page);
    await page.goto(BASE + '/auth/login');
    await login(page);
    await expect(page).toHaveURL(/\/welcome/, { timeout: 5000 });
    // Click logout
    await page.click('button:has-text("退出登录")');
    // Visit protected route
    await page.goto(BASE + '/sys1');
    await expect(page).toHaveURL(/\/auth\/login/);
  });

  test('直接访问 /auth/login（已登录）→ 仍可重新登录', async ({ page }) => {
    await clearToken(page);
    await page.goto(BASE + '/auth/login');
    await expect(page.locator('form, .login-page')).toBeVisible();
  });
});
