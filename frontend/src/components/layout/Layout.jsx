import { useState, useEffect } from "react";
import { Outlet } from "react-router-dom";
import Sidebar from "../ui/Sidebar";
import Header from "../ui/Header";
import "./style.css";

export default function Layout() {
  const [isSidebarCollapsed, setIsSidebarCollapsed] = useState(
    () => window.localStorage.getItem("wecamp-sidebar-collapsed") === "true"
  );

  const toggleSidebar = () => setIsSidebarCollapsed((prev) => !prev);

  useEffect(() => {
    window.localStorage.setItem(
      "wecamp-sidebar-collapsed",
      String(isSidebarCollapsed)
    );
  }, [isSidebarCollapsed]);

  return (
    <div className="app-shell">
      <Sidebar isCollapsed={isSidebarCollapsed} onToggle={toggleSidebar} />
      <div className="app-main">
        <Header />
        <div className="app-content">
          <Outlet />
        </div>
      </div>
    </div>
  );
}
