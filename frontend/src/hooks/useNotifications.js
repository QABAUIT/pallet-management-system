import { useState, useEffect, useCallback, useMemo } from 'react'
import { User } from 'lucide-react'
import { thongBaoApi } from '../api/thongBaoApi'
import { useNotificationBadge } from '../store/NotificationContext'

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
    isRead: Boolean(item.daDoc),
    description: item.noiDung,
    meta: [{ icon: User, text: `Đối tượng: ${item.doiTuongLoai ?? 'Chung'}` }],
    primaryAction: { label: actionLabel, link },
    iconType,
  }
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
  const fetchNotifications = useCallback(async () => {
    setLoading(true)
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
        const s = resStats.value.data
        setStats(s)
        // Đồng bộ badge header với kết quả thực từ server
        if (s.chuaDoc !== undefined) {
          refreshBadge()
        }
      }
    } catch (err) {
      console.error('Lỗi tải thông báo:', err)
    } finally {
      setLoading(false)
    }
  }, [statusFilter, refreshBadge])

  useEffect(() => {
    fetchNotifications()
  }, [fetchNotifications])

  // ----- Lọc client-side -----
  const filteredNotifications = useMemo(() => {
    return notifications.filter((item) => {
      if (statusFilter === 'unread') return !item.isRead
      if (statusFilter === 'read') return item.isRead
      return true
    })
  }, [notifications, statusFilter])

  const unreadList = filteredNotifications.filter((n) => !n.isRead)
  const readList = filteredNotifications.filter((n) => n.isRead)

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
    unreadList,
    readList,
    stats,
    loading,

    // Filters & pagination
    statusFilter,
    setStatusFilter,
    timePreset,
    setTimePreset,
    currentPage,
    setCurrentPage,

    // Derived counts
    totalCount: stats.tongSo ?? 0,
    unreadCount: stats.chuaDoc ?? unreadList.length,
    readCount: stats.daDoc ?? readList.length,

    // Actions
    fetchNotifications,
    handleMarkAllRead,
    handleToggleRead,
    handleDelete,
  }
}
