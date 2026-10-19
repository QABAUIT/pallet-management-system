import React from 'react'
import { Flex, Skeleton } from 'antd'
import Pagination from '../components/common/Pagination'
import NotificationToolbar from '../components/notifications/NotificationToolbar'
import NotificationSection from '../components/notifications/NotificationSection'
import NotificationEmptyState from '../components/notifications/NotificationEmptyState'
import { useNotifications } from '../hooks/useNotifications'

export default function Notifications() {
  const {
    notifications,
    filteredNotifications,
    unreadList,
    readList,
    loading,
    statusFilter,
    setStatusFilter,
    timePreset,
    setTimePreset,
    currentPage,
    setCurrentPage,
    totalCount,
    unreadCount,
    readCount,
    fetchNotifications,
    handleMarkAllRead,
    handleToggleRead,
    handleDelete,
  } = useNotifications()

  return (
    <Flex vertical style={{ maxWidth: 1280, margin: '0 auto', width: '100%' }}>
      {/* 1. Header Toolbar chung */}
      <NotificationToolbar
        statusFilter={statusFilter}
        setStatusFilter={setStatusFilter}
        totalCount={totalCount}
        unreadCount={unreadCount}
        readCount={readCount}
        timePreset={timePreset}
        setTimePreset={setTimePreset}
        onRefresh={fetchNotifications}
        loading={loading}
        onMarkAllRead={handleMarkAllRead}
      />

      {/* Loading Skeleton chỉ hiện khi lần đầu tải dữ liệu */}
      {loading && notifications.length === 0 ? (
        <Skeleton
          active
          paragraph={{ rows: 6 }}
          style={{ padding: 24, background: '#ffffff', borderRadius: 12 }}
        />
      ) : (
        <>
          {/* 2. Nhóm Chưa đọc - Cần xử lý */}
          {(statusFilter === 'all' || statusFilter === 'unread') && (
            <NotificationSection
              title="CHƯA ĐỌC - CẦN XỬ LÝ"
              count={unreadList.length}
              subtitle="Ưu tiên xử lý nhanh"
              items={unreadList}
              isReadSection={false}
              onToggleRead={handleToggleRead}
              onDelete={handleDelete}
            />
          )}

          {/* 3. Nhóm Đã đọc - Nhật ký hệ thống */}
          {(statusFilter === 'all' || statusFilter === 'read') && (
            <NotificationSection
              title="ĐÃ ĐỌC - NHẬT KÝ HỆ THỐNG"
              count={readList.length}
              subtitle="Lịch sử hoạt động hệ thống"
              items={readList}
              isReadSection={true}
              onToggleRead={handleToggleRead}
              onDelete={handleDelete}
            />
          )}

          {/* 4. Empty State khi danh sách trống */}
          {filteredNotifications.length === 0 && <NotificationEmptyState />}
        </>
      )}

      {/* 5. Phân trang */}
      <div style={{ marginTop: 12 }}>
        <Pagination
          currentPage={currentPage}
          totalItems={totalCount}
          itemsPerPage={8}
          itemLabel="thông báo"
          onPageChange={setCurrentPage}
        />
      </div>
    </Flex>
  )
}
