import { NavLink } from "react-router-dom";
import {
  Info,
  FileText,
  Package,
  Truck,
  Users,
  UserRound,
  Warehouse,
  Wallet,
  ChevronsLeft,
  ChevronsRight,
} from "lucide-react";
import "../layout/style.css";

const MENU_ITEMS = [
  { label: "About us", icon: Info, to: "/" },
  { label: "Quản lí hóa đơn", icon: FileText, to: "/hoa-don" },
  { label: "Quản lí pallet", icon: Package, to: "/pallet" },
  { label: "Nhà cung cấp", icon: Truck, to: "/nha-cung-cap" },
  { label: "Quản lí nhân viên", icon: Users, to: "/nhan-vien" },
  { label: "Quản lí khách hàng", icon: UserRound, to: "/khach-hang" },
  { label: "Quản lí kho", icon: Warehouse, to: "/kho" },
  { label: "Quản lí tài chính", icon: Wallet, to: "/tai-chinh" },
];

export default function Sidebar({ isCollapsed = false, onToggle }) {
  return (
    <aside className={"sidebar" + (isCollapsed ? " collapsed" : "")}>
      <div className="sidebar-logo">
        <div className="logo-icon">
          <Warehouse size={20} color="var(--accent)" />
        </div>
        {!isCollapsed && (
          <div className="logo-text">
            <p className="logo-title">Pallet Út Xíu</p>
            <p className="logo-subtitle">Kho Tân Bình - TP.HCM</p>
          </div>
        )}
      </div>

      <nav className="sidebar-nav">
        {MENU_ITEMS.map(({ label, icon: Icon, to }) => (
          <NavLink
            key={label}
            to={to}
            end={to === "/"}
            title={isCollapsed ? label : undefined}
            className={({ isActive }) =>
              "nav-link" + (isActive ? " active" : "")
            }
          >
            <Icon size={18} className="nav-icon" />
            {!isCollapsed && <span>{label}</span>}
          </NavLink>
        ))}
      </nav>

      {onToggle && (
        <button
          type="button"
          className="sidebar-toggle"
          onClick={onToggle}
          aria-label={isCollapsed ? "Mở rộng menu" : "Thu gọn menu"}
        >
          {isCollapsed ? <ChevronsRight size={16} /> : <ChevronsLeft size={16} />}
        </button>
      )}
    </aside>
  );
}
