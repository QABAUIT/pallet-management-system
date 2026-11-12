import { useEffect, useRef, useState } from 'react'
import { Outlet, useLocation } from 'react-router-dom'
import Sidebar from '../ui/Sidebar'
import Header from '../ui/Header'
import { useNotificationBadge } from '../../store/NotificationContext'
import './style.css'

export default function Layout() {
  const { unreadCount } = useNotificationBadge()
  const location = useLocation()

  const [isSidebarCollapsed, setIsSidebarCollapsed] = useState(
    () => window.localStorage.getItem('wecamp-sidebar-collapsed') === 'true',
  )
  const [isMobileNavOpen, setIsMobileNavOpen] = useState(false)
  const contentRef = useRef(null)

  const toggleSidebar = () => setIsSidebarCollapsed((prev) => !prev)
  const closeMobileNav = () => setIsMobileNavOpen(false)

  useEffect(() => {
    window.localStorage.setItem('wecamp-sidebar-collapsed', String(isSidebarCollapsed))
  }, [isSidebarCollapsed])

  // Đóng menu mobile và đưa nội dung về đầu trang mỗi khi đổi trang
  useEffect(() => {
    setIsMobileNavOpen(false)
    contentRef.current?.scrollTo({ top: 0, left: 0, behavior: 'instant' })
  }, [location.pathname])

  return (
    <div className={'app-shell' + (isMobileNavOpen ? ' nav-open' : '')}>
      <Sidebar
        isCollapsed={isSidebarCollapsed}
        onToggle={toggleSidebar}
        mobileOpen={isMobileNavOpen}
        onMobileClose={closeMobileNav}
      />
      {isMobileNavOpen && (
        <button
          type="button"
          className="sidebar-scrim"
          aria-label="Đóng menu điều hướng"
          onClick={closeMobileNav}
        />
      )}
      <div className="app-main">
        <Header
          notificationCount={unreadCount}
          onMenuClick={() => setIsMobileNavOpen((open) => !open)}
        />
        <div className="app-content" ref={contentRef}>
          <Outlet />
        </div>
      </div>
    </div>
  )
}
