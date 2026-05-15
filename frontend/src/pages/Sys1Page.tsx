import React, { useEffect, useState } from "react";
import { authFetch } from "../utils/authFetch";
import Layout from "../components/Layout";

const Sys1Page: React.FC = () => {
  const [userCount, setUserCount] = useState<number | null>(null);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    authFetch("/api/users?page=0&size=1")
      .then((res) => {
        if (res.status === 401)
          throw new Error("JWT 校验失败 (401 Unauthorized)");
        return res.json();
      })
      .then((data) => setUserCount(data.totalElements ?? 0))
      .catch((e) => setError(e.message));
  }, []);

  return (
    <Layout title="子系统1" subtitle="这是受保护的子系统1内容。">
      <div className="bg-white dark:bg-gray-800 border border-gray-200 dark:border-gray-700 rounded-lg p-4 inline-flex flex-col gap-2.5 min-w-80 shadow-sm">
        <span className="text-xs font-semibold text-gray-400 uppercase tracking-wide">JWT 验证结果（GET /users）</span>
        {userCount !== null && (
          <span className="text-sm text-green-600 dark:text-green-400">
            当前用户总数：{userCount}
          </span>
        )}
        {error && <span className="text-sm text-red-600 dark:text-red-400">{error}</span>}
        {userCount === null && !error && (
          <span className="text-sm text-gray-400">请求中...</span>
        )}
      </div>
    </Layout>
  );
};

export default Sys1Page;
