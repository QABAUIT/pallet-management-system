import React, { createContext, useContext, useState, useCallback, useEffect } from 'react'
import { thongBaoApi } from '../api/thongBaoApi'

const NotificationContext = createContext(null)

/**
 * Provider toàn cục quản lý số thông báo chưa đọc.
 * Đặt bên trong <AuthProvider> để có token khi gọi API.
 */
export function NotificationProvider({ children }) {
  const [unreadCount, setUnreadCount] = useState(0)

  // Fetch số thông báo chưa đọc từ server
  const refreshBadge = useCallback(() => {
    thongBaoApi
      .thongKe()
      .then((res) => {
        if (res?.data?.chuaDoc !== undefined) {
          setUnreadCount(res.data.chuaDoc)
        }
      })
      .catch(() => {
        // Giữ nguyên giá trị cũ nếu lỗi mạng
      })
  }, [])

  // Gọi khi mount lần đầu
  useEffect(() => {
    refreshBadge()
  }, [refreshBadge])

  // Polling mỗi 60 giây để bắt thông báo mới từ server
  useEffect(() => {
    const id = setInterval(refreshBadge, 60_000)
    return () => clearInterval(id)
  }, [refreshBadge])

  // Giảm badge ngay lập tức (optimistic) khi đọc 1 thông báo
  const decrementBadge = useCallback((amount = 1) => {
    setUnreadCount((prev) => Math.max(0, prev - amount))
  }, [])

  // Reset badge về 0 (khi đánh dấu tất cả đã đọc)
  const clearBadge = useCallback(() => {
    setUnreadCount(0)
  }, [])

  return (
    <NotificationContext.Provider value={{ unreadCount, refreshBadge, decrementBadge, clearBadge }}>
      {children}
    </NotificationContext.Provider>
  )
}

/**
 * Hook để đọc / cập nhật badge thông báo từ bất kỳ component nào.
 * Phải dùng bên trong <NotificationProvider>.
 */
export function useNotificationBadge() {
  const ctx = useContext(NotificationContext)
  if (!ctx) throw new Error('useNotificationBadge phải dùng bên trong <NotificationProvider>')
  return ctx
}
