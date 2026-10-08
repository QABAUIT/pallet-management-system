import { NavLink } from 'react-router-dom'

// Thứ tự giống Figma. "Báo cáo tồn kho" nằm ở /kho (Header.jsx đã khai báo tiêu đề cho path này).
const TABS = [
  { to: '/kho/nhap-kho', label: 'Lịch sử nhập kho' },
  { to: '/kho/xuat-kho', label: 'Lịch sử xuất kho' },
  { to: '/kho/kiem-ke', label: 'Kiểm kê kho' },
  { to: '/kho', label: 'Báo cáo tồn kho', end: true },
]

function InventoryTabs() {
  return (
    <nav
      style={{
        display: 'flex',
        gap: 28,
        borderBottom: '1px solid #f0f0f0',
        marginBottom: 20,
      }}
    >
      {TABS.map((t) => (
        <NavLink
          key={t.to}
          to={t.to}
          end={t.end}
          style={({ isActive }) => ({
            padding: '10px 2px',
            marginBottom: -1,
            fontSize: 14,
            fontWeight: 600,
            textDecoration: 'none',
            color: isActive ? '#d46b08' : '#595959',
            borderBottom: `2px solid ${isActive ? '#d46b08' : 'transparent'}`,
          })}
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
    <div style={{ padding: 24, background: '#fcfcfc', minHeight: '100%', textAlign: 'left' }}>
      {showTabs && <InventoryTabs />}
      {children}
    </div>
  )
}
