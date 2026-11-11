import { useState, useEffect, useCallback, useMemo } from 'react'
import { User } from 'lucide-react'
import { thongBaoApi } from '../api/thongBaoApi'
import { useNotificationBadge } from '../store/NotificationContext'
import { matchTime } from '../utils/matchTime'

const PAGE_SIZE = 8

// ---------------------------------------------------------------------------
// Helper: chuyển đổi bản ghi backend thành định dạng hiển thị UI
// ---------------------------------------------------------------------------
function transformBackendItem(item) {
  let iconType = 'package'
  let link = '/'
  let actionLabel = 'Xem chi tiết'

  const loai = item.doiTuongLoai ?? ''
  const loaiTB = item.loaiThongBao?.toLowerCase() ?? ''

  if (loai === 'hoa_don' || loaiTB.includes('hoa_don')) {
    iconType = 'invoice'
    link = item.doiTuongId ? `/hoa-don/${item.doiTuongId}` : '/hoa-don'
    actionLabel = 'Xem chi tiết hóa đơn'
  } else if (loai === 'phieu_xuat' || loaiTB.includes('xuat')) {
    iconType = 'truck'
    link = item.doiTuongId ? `/kho/xuat-kho/${item.doiTuongId}` : '/kho/xuat-kho'
    actionLabel = 'Xem phiếu xuất kho'
  } else if (loai === 'phieu_nhap' || loaiTB.includes('nhap')) {
    iconType = 'warehouse'
    link = item.doiTuongId ? `/kho/nhap-kho/${item.doiTuongId}` : '/kho/nhap-kho'
    actionLabel = 'Xem phiếu nhập'
  }

  return {
    id: item.id,
    type: item.loaiThongBao?.toUpperCase() ?? 'HỆ THỐNG',
    code: item.tieuDe ?? `#TB-${item.id}`,
    subtext: item.tenNguoiNhan ?? item.tenVaiTroNhan ?? 'Thông báo hệ thống',
    time: item.thoiGianTao
      ? new Date(item.thoiGianTao).toLocaleString('vi-VN')
      : 'Vừa xong',
    createdAt: item.thoiGianTao ?? null,
    isRead: Boolean(item.daDoc),
    description: item.noiDung,
    meta: [{ icon: User, text: `Đối tượng: ${item.doiTuongLoai ?? 'Chung'}` }],
    primaryAction: { label: actionLabel, link },
    iconType,
  }
}

/** Đổi preset của trang thông báo sang cấu trúc matchTime hiểu được. */
function thanhLocThoiGian(preset) {
  const now = new Date()

  if (preset === 'today') {
    return { preset: 'custom', from: now, to: now }
  }
  if (preset === '7days') {
    const from = new Date(now)
    from.setDate(from.getDate() - 6)
    return { preset: 'custom', from, to: now }
  }
  // Mặc định: tháng này
  return { preset: 'month', from: null, to: null }
}

// ---------------------------------------------------------------------------
// Hook chính
// ---------------------------------------------------------------------------
export function useNotifications() {
  const [notifications, setNotifications] = useState([])
  const [stats, setStats] = useState({ tongSo: 0, chuaDoc: 0, daDoc: 0 })
  const [statusFilter, setStatusFilter] = useState('all')
  const [timePreset, setTimePreset] = useState('today')
  const [currentPage, setCurrentPage] = useState(1)
  const [loading, setLoading] = useState(false)

  const { refreshBadge, decrementBadge, clearBadge } = useNotificationBadge()

  // ----- Fetch danh sách -----
  const fetchNotifications = useCallback(
    async (isSilent = false) => {
      if (!isSilent) {
        setLoading(true)
      }
      try {
        const params = {}
        if (statusFilter === 'unread') params.daDoc = false
        if (statusFilter === 'read') params.daDoc = true

        const [resList, resStats] = await Promise.allSettled([
          thongBaoApi.danhSach(params),
          thongBaoApi.thongKe(),
        ])

        if (resList.status === 'fulfilled' && resList.value?.data) {
          setNotifications(resList.value.data.map(transformBackendItem))
        }

        if (resStats.status === 'fulfilled' && resStats.value?.data) {
          setStats(resStats.value.data)
          // Đồng bộ badge header với kết quả thực từ server
          refreshBadge()
        }
      } catch (err) {
        console.error('Lỗi tải thông báo:', err)
      } finally {
        if (!isSilent) {
          setLoading(false)
        }
      }
    },
    [statusFilter, refreshBadge],
  )

  // Tải lại khi đổi bộ lọc trạng thái
  useEffect(() => {
    fetchNotifications(false)
  }, [fetchNotifications])

  // Lắng nghe SSE một lần duy nhất (không phụ thuộc bộ lọc)
  useEffect(() => {
    const handleNewNotification = (e) => {
      const raw = e.detail
      if (!raw) return
      const newItem = transformBackendItem(raw)
      setNotifications((prev) => {
        if (prev.some((x) => x.id === newItem.id)) return prev
        return [newItem, ...prev]
      })
      setStats((prev) => ({
        ...prev,
        tongSo: prev.tongSo + 1,
        chuaDoc: prev.chuaDoc + 1,
      }))
    }

    window.addEventListener('app:new-notification', handleNewNotification)
    return () => window.removeEventListener('app:new-notification', handleNewNotification)
  }, [])

  // Đổi bộ lọc thì quay về trang 1
  const doiStatusFilter = useCallback((next) => {
    setStatusFilter(next)
    setCurrentPage(1)
  }, [])

  const doiTimePreset = useCallback((next) => {
    setTimePreset(next)
    setCurrentPage(1)
  }, [])

  // ----- Lọc client-side theo trạng thái + thời gian -----
  const filteredNotifications = useMemo(() => {
    const time = thanhLocThoiGian(timePreset)

    return notifications.filter((item) => {
      if (statusFilter === 'unread' && item.isRead) return false
      if (statusFilter === 'read' && !item.isRead) return false
      if (item.createdAt && !matchTime(item.createdAt, time)) return false
      return true
    })
  }, [notifications, statusFilter, timePreset])

  // ----- Phân trang -----
  const totalFiltered = filteredNotifications.length
  const totalPages = Math.max(1, Math.ceil(totalFiltered / PAGE_SIZE))
  // Nếu xóa bớt làm trang hiện tại vượt quá số trang -> tự hiển thị trang cuối
  const safePage = Math.min(currentPage, totalPages)

  const pagedNotifications = useMemo(() => {
    const start = (safePage - 1) * PAGE_SIZE
    return filteredNotifications.slice(start, start + PAGE_SIZE)
  }, [filteredNotifications, safePage])

  const unreadList = pagedNotifications.filter((n) => !n.isRead)
  const readList = pagedNotifications.filter((n) => n.isRead)

  // ----- Actions -----

  const handleMarkAllRead = useCallback(async () => {
    try {
      await thongBaoApi.danhDauTatCaDaDoc()
      setNotifications((prev) => prev.map((item) => ({ ...item, isRead: true })))
      setStats((prev) => ({ ...prev, chuaDoc: 0, daDoc: prev.tongSo }))
      clearBadge() // badge → 0 ngay lập tức
    } catch (err) {
      console.error('Lỗi đánh dấu tất cả đã đọc:', err)
    }
  }, [clearBadge])

  const handleToggleRead = useCallback(
    async (id) => {
      const target = notifications.find((n) => n.id === id)
      const newStatus = target ? !target.isRead : true

      try {
        await thongBaoApi.danhDauDaDoc(id, newStatus)
        setNotifications((prev) =>
          prev.map((item) => (item.id === id ? { ...item, isRead: newStatus } : item)),
        )
        setStats((prev) => ({
          ...prev,
          chuaDoc: Math.max(0, prev.chuaDoc + (newStatus ? -1 : 1)),
          daDoc: Math.max(0, prev.daDoc + (newStatus ? 1 : -1)),
        }))
        // Cập nhật badge: đánh dấu đã đọc → giảm 1; bỏ đọc → tăng lại từ server
        if (newStatus) {
          decrementBadge(1)
        } else {
          refreshBadge()
        }
      } catch (err) {
        console.error('Lỗi cập nhật trạng thái đọc:', err)
      }
    },
    [notifications, decrementBadge, refreshBadge],
  )

  const handleDelete = useCallback(
    async (id) => {
      try {
        const target = notifications.find((n) => n.id === id)
        await thongBaoApi.xoa(id)
        setNotifications((prev) => prev.filter((item) => item.id !== id))
        if (target) {
          setStats((prev) => ({
            ...prev,
            tongSo: Math.max(0, prev.tongSo - 1),
            chuaDoc: Math.max(0, prev.chuaDoc - (target.isRead ? 0 : 1)),
            daDoc: Math.max(0, prev.daDoc - (target.isRead ? 1 : 0)),
          }))
        }
        // Nếu thông báo bị xóa chưa đọc → giảm badge
        if (target && !target.isRead) {
          decrementBadge(1)
        }
      } catch (err) {
        console.error('Lỗi xóa thông báo:', err)
      }
    },
    [notifications, decrementBadge],
  )

  return {
    // Data
    notifications,
    filteredNotifications,
    pagedNotifications,
    unreadList,
    readList,
    stats,
    loading,

    // Filters & pagination
    statusFilter,
    setStatusFilter: doiStatusFilter,
    timePreset,
    setTimePreset: doiTimePreset,
    currentPage: safePage,
    setCurrentPage,

    // Derived counts
    totalCount: stats.tongSo ?? 0,
    unreadCount: stats.chuaDoc ?? 0,
    readCount: stats.daDoc ?? 0,
    filteredCount: totalFiltered,

    // Actions
    fetchNotifications,
    handleMarkAllRead,
    handleToggleRead,
    handleDelete,
  }
}
