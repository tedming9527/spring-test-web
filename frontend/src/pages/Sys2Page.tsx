import React, { useEffect, useState } from "react";
import { useNavigate, Link } from "react-router-dom";
import { authFetch } from "../utils/authFetch";

const Sys2Page: React.FC = () => {
  const navigate = useNavigate();
  const [hello, setHello] = useState<string | null>(null);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    authFetch("/hello")
      .then((res) => {
        if (res.status === 401) throw new Error("JWT 校验失败 (401 Unauthorized)");
        return res.text();
      })
      .then(setHello)
      .catch((e) => setError(e.message));
  }, []);

  const logout = () => {
    localStorage.removeItem("token");
    navigate("/auth/login", { replace: true });
  };

  return (
    <div style={{ textAlign: "center", marginTop: "4rem" }}>
      <h1>子系统2页面</h1>
      <p>这是受保护的子系统2内容。</p>

      <div style={{ margin: "1.5rem 0", padding: "1rem", border: "1px solid #ccc", borderRadius: 8, display: "inline-block", minWidth: 300 }}>
        <strong>JWT 验证结果（GET /hello）：</strong>
        {hello && <p style={{ color: "green" }}>{hello}</p>}
        {error && <p style={{ color: "red" }}>{error}</p>}
        {!hello && !error && <p style={{ color: "#888" }}>请求中...</p>}
      </div>

      <div style={{ marginTop: "1.5rem", display: "flex", justifyContent: "center", gap: "1rem" }}>
        <Link to="/welcome">返回首页</Link>
        <Link to="/sys1">子系统1</Link>
        <button onClick={logout} style={{ color: "red", background: "none", border: "1px solid red", borderRadius: 4, padding: "2px 12px", cursor: "pointer" }}>退出登录</button>
      </div>
    </div>
  );
};

export default Sys2Page;
