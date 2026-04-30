import { useState } from "react";
import {
  BrowserRouter as Router,
  Routes,
  Route,
  Navigate,
} from "react-router-dom";

import LoginPage from "./pages/auth/LoginPage";
import WelcomePage from "./pages/WelcomePage";
import Sys1Page from "./pages/Sys1Page";
import Sys2Page from "./pages/Sys2Page";
import React from "react";
import reactLogo from "./assets/react.svg";
import viteLogo from "./assets/vite.svg";
import heroImg from "./assets/hero.png";
import "./App.css";

// 路由守卫组件
const RequireAuth: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const token = localStorage.getItem("token");
  if (!token) {
    return <Navigate to="/auth/login" replace />;
  }
  return <>{children}</>;
};

function App() {
  return (
    <Router>
      <Routes>
        <Route path="/auth/login" element={<LoginPage />} />
        <Route
          path="/welcome"
          element={
            <RequireAuth>
              <WelcomePage />
            </RequireAuth>
          }
        />
        <Route
          path="/sys1"
          element={
            <RequireAuth>
              <Sys1Page />
            </RequireAuth>
          }
        />
        <Route
          path="/sys2"
          element={
            <RequireAuth>
              <Sys2Page />
            </RequireAuth>
          }
        />
        <Route path="*" element={<Navigate to="/auth/login" replace />} />
      </Routes>
    </Router>
  );
}

export default App;
