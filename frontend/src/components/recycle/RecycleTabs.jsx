import { NavLink } from "react-router-dom";

const TABS = [
  { to: "/kho/tai-che/phan-loai", label: "Phân loại lô" },
  { to: "/kho/tai-che/thao-do", label: "Tháo dỡ" },
  { to: "/kho/tai-che/xu-ly-them", label: "Xử lý thêm" },
  { to: "/kho/tai-che/dinh-muc", label: "Định mức" },
  { to: "/kho/tai-che/dong-pallet", label: "Đóng pallet" },
  { to: "/kho/tai-che/dich-vu-khach", label: "Dịch vụ cho khách" },
];

/** Thanh chuyển giữa các bước tái chế (nằm dưới tab "Tái chế" của khu Kho). */
export default function RecycleTabs() {
  return (
    <div style={{ display: "flex", gap: 8, marginBottom: 16, flexWrap: "wrap" }}>
      {TABS.map((t, i) => (
        <NavLink
          key={t.to}
          to={t.to}
          style={({ isActive }) => ({
            padding: "6px 14px",
            fontSize: 13,
            fontWeight: 600,
            textDecoration: "none",
            borderRadius: 9999,
            border: `1px solid ${isActive ? "#d46b08" : "#d9d9d9"}`,
            background: isActive ? "#fff7e6" : "#fff",
            color: isActive ? "#d46b08" : "#595959",
          })}
        >
          {i + 1}. {t.label}
        </NavLink>
      ))}
    </div>
  );
}
