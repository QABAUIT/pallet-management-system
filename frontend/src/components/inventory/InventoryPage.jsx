import { NavLink } from 'react-router-dom'

// Thứ tự giống Figma. "Báo cáo tồn kho" nằm ở /kho (Header.jsx đã khai báo tiêu đề cho path này).
// "Tái chế" không có `end` nên sáng cho cả /kho/tai-che/phan-loai, /thao-do, /xu-ly-them.
const TABS = [
  { to: '/kho/nhap-kho', label: 'Lịch sử nhập kho' },
  { to: '/kho/xuat-kho', label: 'Lịch sử xuất kho' },
  { to: '/kho/kiem-ke', label: 'Kiểm kê kho' },
  { to: '/kho/don-mua', label: 'Đơn mua' },
  { to: '/kho/tai-che', label: 'Tái chế' },
  { to: '/kho', label: 'Báo cáo tồn kho', end: true },
]

function InventoryTabs() {
  return (
    <nav className="inv-tabs" aria-label="Điều hướng kho">
      {TABS.map((t) => (
        <NavLink
          key={t.to}
          to={t.to}
          end={t.end}
          className={({ isActive }) => 'inv-tab' + (isActive ? ' active' : '')}
        >
          {t.label}
        </NavLink>
      ))}
    </nav>
  )
}

/** Khung chung cho các trang kho: nền, padding, căn trái (vì #root đang text-align: center). */
export default function InventoryPage({ showTabs = true, children }) {
  return (
    <div className="page-shell">
      {showTabs && <InventoryTabs />}
      {children}
    </div>
  )
}
