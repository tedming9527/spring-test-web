import React, { useState } from "react";

interface LoginFormProps {
  onLogin: (username: string, password: string) => void;
  loading?: boolean;
  error?: string;
}

const LoginForm: React.FC<LoginFormProps> = ({ onLogin, loading, error }) => {
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [touched, setTouched] = useState(false);

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    setTouched(true);
    if (username && password) {
      onLogin(username, password);
    }
  };

  const isValid = username.length > 0 && password.length > 0;

  const inputClass =
    "w-full px-3 py-2 border border-gray-200 dark:border-gray-600 rounded text-sm " +
    "bg-white dark:bg-gray-800 text-gray-900 dark:text-gray-100 outline-none " +
    "focus:border-indigo-500 focus:ring-2 focus:ring-indigo-500/20 transition " +
    "placeholder:text-gray-400";

  return (
    <div className="bg-white dark:bg-gray-800 border border-gray-200 dark:border-gray-700 rounded-xl p-10 w-full max-w-sm shadow-md">
      <div className="text-center mb-7">
        <h1 className="text-xl font-bold text-gray-900 dark:text-white mb-1">欢迎登录</h1>
        <p className="text-sm text-gray-400">Spring Demo 管理系统</p>
      </div>
      <form onSubmit={handleSubmit}>
        <div className="flex flex-col gap-1.5 mb-4">
          <label className="text-sm font-medium text-gray-900 dark:text-gray-100" htmlFor="username">
            用户名
          </label>
          <input
            id="username"
            className={inputClass}
            type="text"
            value={username}
            onChange={(e) => setUsername(e.target.value)}
            autoComplete="username"
            placeholder="请输入用户名"
          />
        </div>
        <div className="flex flex-col gap-1.5 mb-4">
          <label className="text-sm font-medium text-gray-900 dark:text-gray-100" htmlFor="password">
            密码
          </label>
          <input
            id="password"
            className={inputClass}
            type="password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            autoComplete="current-password"
            placeholder="请输入密码"
          />
        </div>
        {touched && !isValid && (
          <div className="text-sm text-red-600 dark:text-red-400 px-3 py-2 bg-red-50 dark:bg-red-500/10 rounded mb-3">
            请输入用户名和密码
          </div>
        )}
        {error && (
          <div className="text-sm text-red-600 dark:text-red-400 px-3 py-2 bg-red-50 dark:bg-red-500/10 rounded mb-3">
            {error}
          </div>
        )}
        <button
          type="submit"
          className="w-full py-2.5 bg-indigo-500 hover:bg-indigo-600 disabled:opacity-55 text-white font-medium text-sm rounded cursor-pointer border-none transition-colors"
          disabled={loading}
        >
          {loading ? "登录中..." : "登录"}
        </button>
      </form>
    </div>
  );
};

export default LoginForm;
