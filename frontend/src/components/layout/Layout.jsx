import { useEffect, useState } from 'react'
import { Outlet } from 'react-router-dom'
import Sidebar from '../ui/Sidebar'
import Header from '../ui/Header'
import { useNotificationBadge } from '../../store/NotificationContext'
import './style.css'

export default function Layout() {
  const { unreadCount } = useNotificationBadge()

  const [isSidebarCollapsed, setIsSidebarCollapsed] = useState(
    () => window.localStorage.getItem('wecamp-sidebar-collapsed') === 'true',
  )

  const toggleSidebar = () => setIsSidebarCollapsed((prev) => !prev)

  useEffect(() => {
    window.localStorage.setItem('wecamp-sidebar-collapsed', String(isSidebarCollapsed))
  }, [isSidebarCollapsed])

  return (
    <div className="app-shell">
      <Sidebar isCollapsed={isSidebarCollapsed} onToggle={toggleSidebar} />
      <div className="app-main">
        <Header notificationCount={unreadCount} />
        <div className="app-content">
          <Outlet />
        </div>
      </div>
    </div>
  )
}
