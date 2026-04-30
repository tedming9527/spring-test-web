import React, { useState } from "react";
import { useNavigate } from "react-router-dom";
import LoginForm from "./LoginForm";

const loginApi = async (username: string, password: string) => {
  const resp = await fetch("/auth/login", {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify({ username, password }),
  });
  if (!resp.ok) {
    if (resp.status === 401) {
      throw new Error("用户名或密码错误");
    } else {
      throw new Error("登录失败，请稍后重试");
    }
  }
  const data = await resp.json();
  if (!data.token) {
    throw new Error("登录响应异常");
  }
  return { token: data.token };
};

const LoginPage: React.FC = () => {
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | undefined>();
  const navigate = useNavigate();

  const handleLogin = async (username: string, password: string) => {
    setLoading(true);
    setError(undefined);
    try {
      const result = await loginApi(username, password);
      // 持久化 token
      localStorage.setItem("token", result.token);
      // 跳转到欢迎页
      navigate("/welcome");
    } catch (err: any) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="login-page">
      <LoginForm onLogin={handleLogin} loading={loading} error={error} />
    </div>
  );
};

export default LoginPage;
