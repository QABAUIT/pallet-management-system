import React, { createContext, useContext, useState, useCallback, useEffect } from 'react'
import { thongBaoApi } from '../api/thongBaoApi'
import { tokenStore } from './tokenStore'
import { useAuth } from './AuthContext'

const NotificationContext = createContext(null)

const BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api/v1'
const RECONNECT_MIN_MS = 3000
const RECONNECT_MAX_MS = 30000

/** Tách 1 block sự kiện SSE thành { eventName, data }. */
function parseSseEvent(rawEvent) {
  let eventName = 'message'
  const dataLines = []

  rawEvent.split(/\r?\n/).forEach((line) => {
    if (line.startsWith('event:')) {
      eventName = line.slice(6).trim()
    } else if (line.startsWith('data:')) {
      dataLines.push(line.slice(5).replace(/^ /, ''))
    }
  })

  return { eventName, data: dataLines.join('\n') }
}

/**
 * Provider toàn cục quản lý số thông báo chưa đọc.
 * Đặt bên trong <AuthProvider> để biết trạng thái đăng nhập.
 *
 * SSE dùng fetch + Authorization header (EventSource không gửi được header,
 * nên không thể dùng token qua query param vì sẽ lộ token trong log).
 */
export function NotificationProvider({ children }) {
  const { daDangNhap } = useAuth()
  const [unreadCount, setUnreadCount] = useState(0)

  // Fetch số thông báo chưa đọc từ server
  const refreshBadge = useCallback(() => {
    if (!tokenStore.getAccessToken()) return Promise.resolve()

    return thongBaoApi
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

  useEffect(() => {
    // Chưa đăng nhập (hoặc vừa đăng xuất) -> reset badge và không mở SSE.
    if (!daDangNhap) {
      setUnreadCount(0)
      return undefined
    }

    refreshBadge()

    const controller = new AbortController()
    let daDung = false
    let retryTimer = null
    let retryDelay = RECONNECT_MIN_MS

    const xuLySuKien = ({ eventName, data }) => {
      if (eventName !== 'NOTIFICATION' || !data) return
      try {
        const payload = JSON.parse(data)
        setUnreadCount((prev) => prev + 1)
        // Cho trang danh sách (nếu đang mở) tự thêm thông báo tức thì
        window.dispatchEvent(new CustomEvent('app:new-notification', { detail: payload }))
      } catch {
        // Payload lỗi -> đồng bộ lại từ server cho chắc
        refreshBadge()
      }
    }

    const ketNoi = async () => {
      if (daDung) return

      try {
        const response = await fetch(`${BASE_URL}/thong-bao/subscribe`, {
          headers: {
            Accept: 'text/event-stream',
            Authorization: `Bearer ${tokenStore.getAccessToken() ?? ''}`,
          },
          signal: controller.signal,
        })

        if (response.status === 401) {
          // Gọi 1 API thường để interceptor tự refresh access token,
          // sau đó vòng lặp reconnect sẽ dùng token mới.
          await refreshBadge()
          throw new Error('SSE 401')
        }
        if (!response.ok || !response.body) {
          throw new Error(`SSE HTTP ${response.status}`)
        }

        retryDelay = RECONNECT_MIN_MS

        const reader = response.body.getReader()
        const decoder = new TextDecoder()
        let buffer = ''

        // eslint-disable-next-line no-constant-condition
        while (true) {
          const { value, done } = await reader.read()
          if (done || daDung) break

          buffer += decoder.decode(value, { stream: true })

          let viTri
          while ((viTri = buffer.search(/\r?\n\r?\n/)) >= 0) {
            const rawEvent = buffer.slice(0, viTri)
            buffer = buffer.slice(viTri).replace(/^\r?\n\r?\n/, '')
            xuLySuKien(parseSseEvent(rawEvent))
          }
        }
      } catch {
        // Mất kết nối / bị hủy -> sẽ thử lại bên dưới (trừ khi đã unmount)
      }

      if (!daDung) {
        retryTimer = setTimeout(ketNoi, retryDelay)
        retryDelay = Math.min(retryDelay * 2, RECONNECT_MAX_MS)
      }
    }

    ketNoi()

    return () => {
      daDung = true
      controller.abort()
      if (retryTimer) clearTimeout(retryTimer)
    }
  }, [daDangNhap, refreshBadge])

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
