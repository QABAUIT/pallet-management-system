import React, { createContext, useContext, useState, useCallback, useEffect } from 'react'
import { thongBaoApi } from '../api/thongBaoApi'
import { tokenStore } from './tokenStore'

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

  // Gọi khi mount lần đầu để lấy số lượng hiện tại
  useEffect(() => {
    refreshBadge()
  }, [refreshBadge])

  // Lắng nghe sự kiện Realtime qua Server-Sent Events (SSE)
  useEffect(() => {
    const token = tokenStore.getAccessToken()
    if (!token) return

    const baseUrl = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api/v1'
    const sseUrl = `${baseUrl}/thong-bao/subscribe?token=${encodeURIComponent(token)}`

    let eventSource = null
    try {
      eventSource = new EventSource(sseUrl)

      // Khi có thông báo mới được backend push về qua SSE
      eventSource.addEventListener('NOTIFICATION', (event) => {
        try {
          // Tăng ngay badge số thông báo chưa đọc lên 1
          setUnreadCount((prev) => prev + 1)
          // Phát ra custom event để trang danh sách (nếu đang mở) tự động thêm vào danh sách tức thì
          window.dispatchEvent(new CustomEvent('app:new-notification', { detail: JSON.parse(event.data) }))
        } catch (e) {
          refreshBadge()
        }
      })

      eventSource.onerror = () => {
        // EventSource tự động reconnect khi mất kết nối
      }
    } catch (e) {
      console.warn('Không thể mở kết nối SSE:', e)
    }

    return () => {
      if (eventSource) {
        eventSource.close()
      }
    }
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
