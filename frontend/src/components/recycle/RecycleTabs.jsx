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
    <div className="pill-tabs">
      {TABS.map((t, i) => (
        <NavLink
          key={t.to}
          to={t.to}
          className={({ isActive }) => "pill-tab" + (isActive ? " active" : "")}
        >
          {i + 1}. {t.label}
        </NavLink>
      ))}
    </div>
  );
}
