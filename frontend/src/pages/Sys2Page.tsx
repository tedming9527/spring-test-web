import React, { useEffect, useState } from "react";
import { authFetch } from "../utils/authFetch";
import Layout from "../components/Layout";

const Sys2Page: React.FC = () => {
  const [hello, setHello] = useState<string | null>(null);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    authFetch("/api/hello")
      .then((res) => {
        if (res.status === 401)
          throw new Error("JWT 校验失败 (401 Unauthorized)");
        return res.text();
      })
      .then(setHello)
      .catch((e) => setError(e.message));
  }, []);

  return (
    <Layout title="子系统2" subtitle="这是受保护的子系统2内容。">
      <div className="bg-white dark:bg-gray-800 border border-gray-200 dark:border-gray-700 rounded-lg p-4 inline-flex flex-col gap-2.5 min-w-80 shadow-sm">
        <span className="text-xs font-semibold text-gray-400 uppercase tracking-wide">JWT 验证结果（GET /hello）</span>
        {hello && <span className="text-sm text-green-600 dark:text-green-400">{hello}</span>}
        {error && <span className="text-sm text-red-600 dark:text-red-400">{error}</span>}
        {!hello && !error && (
          <span className="text-sm text-gray-400">请求中...</span>
        )}
      </div>
    </Layout>
  );
};

export default Sys2Page;
