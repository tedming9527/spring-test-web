import React from "react";
import { NavLink, useNavigate } from "react-router-dom";

interface LayoutProps {
  children: React.ReactNode;
  title?: string;
  subtitle?: string;
}

const navLinkClass = ({ isActive }: { isActive: boolean }) =>
  `px-3 py-1.5 rounded text-sm font-medium no-underline transition-colors ${
    isActive
      ? "bg-indigo-50 text-indigo-500 dark:bg-indigo-500/10 dark:text-indigo-400"
      : "text-gray-600 hover:bg-indigo-50 hover:text-indigo-500 dark:text-gray-400 dark:hover:bg-indigo-500/10 dark:hover:text-indigo-400"
  }`;

const Layout: React.FC<LayoutProps> = ({ children, title, subtitle }) => {
  const navigate = useNavigate();

  const logout = () => {
    localStorage.removeItem("token");
    navigate("/auth/login", { replace: true });
  };

  return (
    <div className="min-h-screen flex flex-col bg-gray-100 dark:bg-gray-900">
      <nav className="sticky top-0 z-50 h-14 bg-white dark:bg-gray-800 border-b border-gray-200 dark:border-gray-700 flex items-center px-6 gap-4 shadow-sm">
        <NavLink
          to="/welcome"
          className="text-indigo-500 font-bold text-sm no-underline shrink-0 hover:opacity-80"
        >
          Spring Demo
        </NavLink>
        <div className="flex items-center gap-0.5 flex-1">
          <NavLink to="/welcome" className={navLinkClass}>
            首页
          </NavLink>
          <NavLink to="/sys1" className={navLinkClass}>
            子系统1
          </NavLink>
          <NavLink to="/sys2" className={navLinkClass}>
            子系统2
          </NavLink>
        </div>
        <button
          className="px-3 py-1 text-sm font-medium border border-red-500 text-red-500 rounded hover:bg-red-50 dark:hover:bg-red-500/10 transition-colors cursor-pointer bg-transparent"
          onClick={logout}
        >
          退出登录
        </button>
      </nav>
      <main className="flex-1 max-w-4xl mx-auto px-6 py-10 w-full">
        {title && (
          <h1 className="text-xl font-bold text-gray-900 dark:text-gray-100 mb-2">
            {title}
          </h1>
        )}
        {subtitle && <p className="text-sm text-gray-400 mb-7">{subtitle}</p>}
        {children}
      </main>
    </div>
  );
};

export default Layout;
